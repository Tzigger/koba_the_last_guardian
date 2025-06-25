package database;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*! \class public class InsertGet
    \brief Clasa se ocupa de manipularea bazei de date.

    Aceasta cuprinde metode statice pentru a putea fi apelate usor.
 */
public class InsertGet
{
    private static final ReentrantLock dbLock = new ReentrantLock();
    private static final String DB_URL = "jdbc:sqlite:data/gamedatabase.db";

    private static Connection getConnection() throws SQLException {
        try {
            // Înregistrăm driverul SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driverul SQLite nu a fost găsit! Asigurați-te că biblioteca sqlite-jdbc este în calea de clasă.");
            e.printStackTrace();
            return null;
        }

        // Verificăm dacă baza de date există
        String dbPath = DB_URL.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            System.err.println("Baza de date lipsește: " + dbFile.getAbsolutePath());
            // Creăm directorul dacă nu există
            dbFile.getParentFile().mkdirs();
        }

        // Returnăm conexiunea
        return DriverManager.getConnection(DB_URL);
    }

    /*! \fn SaveIntoDatabase(String nume_fisier, String nume_tabela, int levelIndex, int currentHealth, int coconutNumber, float posX, float posY)
         \brief Functia se ocupa de salvarea datelor in baza de date.

     */
    public static void ensurePlayerTableExists(String dbPath, String numeTabela) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Statement stmt = conn.createStatement();
            
            // Verifică dacă tabela există
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + numeTabela + "'");
            if (!rs.next()) {
                // Creează tabela dacă nu există
                stmt.execute("CREATE TABLE " + numeTabela + " (" +
                    "LevelIndex INTEGER PRIMARY KEY, " +
                    "Scor INTEGER, " +
                    "CurrentHealth INTEGER, " +
                    "CoconutNumber INTEGER, " +
                    "PosX REAL, " +
                    "PosY REAL, " +
                    "Timer INTEGER DEFAULT 0" +  // Adăugăm coloana Timer cu valoare implicită
                    ")");
                System.out.println("Tabela " + numeTabela + " a fost creată cu succes!");
            } else {
                // Verifică dacă coloana Timer există
                rs = stmt.executeQuery("PRAGMA table_info(" + numeTabela + ")");
                boolean hasTimerColumn = false;
                while (rs.next()) {
                    if (rs.getString("name").equals("Timer")) {
                        hasTimerColumn = true;
                        break;
                    }
                }
                
                // Adaugă coloana Timer dacă nu există
                if (!hasTimerColumn) {
                    stmt.execute("ALTER TABLE " + numeTabela + " ADD COLUMN Timer INTEGER DEFAULT 0");
                    System.out.println("Coloana Timer a fost adăugată în tabela " + numeTabela);
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Eroare la verificarea/crearea tabelei: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void SaveIntoDatabase(String nume_fisier, String nume_tabela, int levelIndex, int Scor, int currentHealth, int coconutNumber, float posX, float posY, int timer) {
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            String levelTableName = nume_tabela + "_level" + levelIndex;
            ensurePlayerTableExists(nume_fisier, levelTableName);
            c = getConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();

            // Verifică dacă există deja un rând în tabel
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + levelTableName);
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }
            rs.close();

            String sql;
            if (count > 0) {
                // Actualizează rândul existent
                sql = "UPDATE " + levelTableName + " SET " +
                      "LevelIndex = " + levelIndex + ", " +
                      "Scor = " + Scor + ", " +
                      "CurrentHealth = " + currentHealth + ", " +
                      "CoconutNumber = " + coconutNumber + ", " +
                      "PosX = " + posX + ", " +
                      "PosY = " + posY + ", " +
                      "Timer = " + timer;
            } else {
                // Inserează un nou rând
                sql = "INSERT INTO " + levelTableName + 
                      " (LevelIndex, Scor, CurrentHealth, CoconutNumber, PosX, PosY, Timer) " +
                      "VALUES (" + levelIndex + ", " + Scor + ", " + currentHealth + 
                      ", " + coconutNumber + ", " + posX + ", " + posY + ", " + timer + ")";
            }

            stmt.executeUpdate(sql);
            c.commit();
            System.out.println("Date salvate pentru " + levelTableName + ": Level=" + levelIndex + 
                             ", Scor=" + Scor + ", Health=" + currentHealth + 
                             ", Coconuts=" + coconutNumber + ", Timer=" + timer);
        } catch (Exception e) {
            System.out.println("Eroare la salvarea datelor!");
            e.printStackTrace();
            try {
                if (c != null) {
                    c.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
    }

    /*! \fn LoadLevelIndex(String nume_fisier, String nume_tabela)
         \brief Functia se ocupa de incarcarea indexului nivelului din baza de date.

     */
    public static int LoadLevelIndex(String nume_fisier, String nume_tabela) {
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            c = getConnection();
            stmt = c.createStatement();
            
            // Găsește cel mai mare nivel salvat pentru acest jucător
            String sql = "SELECT name FROM sqlite_master " +
                        "WHERE type='table' AND name LIKE '" + nume_tabela + "_level%' " +
                        "ORDER BY name DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                String tableName = rs.getString("name");
                // Extrage numărul nivelului din numele tabelului
                String levelStr = tableName.substring(tableName.lastIndexOf("level") + 5);
                try {
                    return Integer.parseInt(levelStr);
                } catch (NumberFormatException e) {
                    return 1;
                }
            }
            return 1;
        } catch (Exception e) {
            System.out.println("Eroare la încărcarea nivelului!");
            e.printStackTrace();
            return 1;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
    }

    public static int LoadScore(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "Scor");
    }

    public static int LoadCurrentHealth(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "CurrentHealth");
    }

    public static int LoadCoconutNumber(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "CoconutNumber");
    }

    public static float LoadXPosition(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "PosX");
    }

    public static float LoadYPosition(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "PosY");
    }

    public static int LoadTimer(String nume_fisier, String nume_tabela) {
        return loadLevelData(nume_fisier, nume_tabela, "Timer");
    }

    private static int loadLevelData(String nume_fisier, String nume_tabela, String column) {
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            c = getConnection();
            stmt = c.createStatement();
            
            // Verifică dacă tabela există
            if (!checkIfTableExists(nume_fisier, nume_tabela)) {
                System.out.println("Tabela " + nume_tabela + " nu există");
                return 0;
            }
            
            // Verifică dacă coloana există
            ResultSet columns = stmt.executeQuery("PRAGMA table_info(" + nume_tabela + ")");
            boolean columnExists = false;
            while (columns.next()) {
                if (columns.getString("name").equals(column)) {
                    columnExists = true;
                    break;
                }
            }
            columns.close();
            
            if (!columnExists) {
                System.out.println("Coloana " + column + " nu există în tabela " + nume_tabela);
                return 0;
            }
            
            // Citește datele direct din tabela specificată
            String sql = "SELECT " + column + " FROM " + nume_tabela;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int value = rs.getInt(column);
                System.out.println("Valoare citită pentru " + column + " din " + nume_tabela + ": " + value);
                return value;
            }
            System.out.println("Nu s-au găsit date pentru " + column + " în " + nume_tabela);
            return 0;
        } catch (Exception e) {
            System.out.println("Eroare la încărcarea datelor pentru " + column + " din " + nume_tabela + "!");
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
    }

    public static List<String> getPlayerList() {
        List<String> players = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            c = getConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
            while (rs.next()) {
                String tableName = rs.getString("name");
                // Excludem tabelele sistem și tabelul de configurare
                if (!tableName.startsWith("sqlite_") && !tableName.equals("config")) {
                    players.add(tableName);
                }
            }
            rs.close();
            System.out.println("Lista de jucători: " + players);
        } catch (Exception e) {
            System.out.println("Error getting player list!");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
        return players;
    }

    public static String LoadUsername(String nume_fisier) {
        Connection c = null;
        Statement stmt = null;
        String username = "player"; // Valoare implicită
        try {
            dbLock.lock();
            c = getConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();

            // Verificăm dacă există tabelul de configurare
            ResultSet tableCheck = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='config';");
            if (!tableCheck.next()) {
                // Creăm tabelul de configurare dacă nu există
                String sql = "CREATE TABLE config (last_username TEXT);";
                stmt.executeUpdate(sql);
                // Inserăm username-ul implicit
                sql = "INSERT INTO config (last_username) VALUES ('player');";
                stmt.executeUpdate(sql);
                c.commit();
            } else {
                // Citim ultimul username folosit
                ResultSet rs = stmt.executeQuery("SELECT last_username FROM config LIMIT 1;");
                if (rs.next()) {
                    username = rs.getString("last_username");
                }
                rs.close();
            }
            tableCheck.close();
        } catch (Exception e) {
            System.out.println("Eroare la citirea username-ului!");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
        return username;
    }

    public static void SaveUsername(String nume_fisier, String username) {
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            c = getConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();

            // Verificăm dacă există tabelul de configurare
            ResultSet tableCheck = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='config';");
            if (!tableCheck.next()) {
                // Creăm tabelul de configurare dacă nu există
                String sql = "CREATE TABLE config (last_username TEXT);";
                stmt.executeUpdate(sql);
                c.commit();
            }

            // Actualizăm sau inserăm username-ul
            String sql = "UPDATE config SET last_username = '" + username + "';";
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                sql = "INSERT INTO config (last_username) VALUES ('" + username + "');";
                stmt.executeUpdate(sql);
            }
            c.commit();
            tableCheck.close();
        } catch (Exception e) {
            System.out.println("Eroare la salvarea username-ului!");
            e.printStackTrace();
            try {
                if (c != null) {
                    c.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
    }

    public static void createLevelProgressTable(String tableName) {
        Connection c = null;
        Statement stmt = null;
        try {
            dbLock.lock();
            c = getConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            // Creează tabelul dacă nu există
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                       "LevelIndex INTEGER DEFAULT 1, " +
                       "Scor INTEGER DEFAULT 0, " +
                       "CurrentHealth INTEGER DEFAULT 100, " +
                       "CoconutNumber INTEGER DEFAULT 0, " +
                       "PosX FLOAT DEFAULT 0, " +
                       "PosY FLOAT DEFAULT 0);";
            stmt.executeUpdate(sql);
            c.commit();
            System.out.println("Tabel progres creat pentru " + tableName);
        } catch (Exception e) {
            System.out.println("Eroare la crearea tabelului de progres pentru nivel!");
            e.printStackTrace();
            try {
                if (c != null) {
                    c.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbLock.unlock();
            }
        }
    }

    public static boolean checkIfTableExists(String dbPath, String tableName) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            System.out.println("Eroare la verificarea existenței tabelei: " + e.getMessage());
            return false;
        }
    }
}
