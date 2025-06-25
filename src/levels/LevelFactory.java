package levels;

import utilz.LoadSave;

/**
 * Clasă de tip Factory responsabilă pentru crearea obiectelor de tip {@link Level}.
 * Implementează modelul de proiectare Factory pentru a abstractiza procesul de creare a nivelurilor.
 */
public class LevelFactory {
    /** Numărul de rânduri în tileset-ul pentru Nivelul 1. */
    public static final int LEVEL1_ROWS = 8;
    /** Numărul de coloane în tileset-ul pentru Nivelul 1. */
    public static final int LEVEL1_COLS = 12;
    /** Dimensiunea unui tile (în pixeli) pentru Nivelul 1. */
    public static final int LEVEL1_TILE_SIZE = 32;
    
    /** Numărul de rânduri în tileset-ul pentru Nivelul 2. */
    public static final int LEVEL2_ROWS = 9;
    /** Numărul de coloane în tileset-ul pentru Nivelul 2. */
    public static final int LEVEL2_COLS = 9;
    /** Dimensiunea unui tile (în pixeli) pentru Nivelul 2. */
    public static final int LEVEL2_TILE_SIZE = 32;
    
    /** Numărul de rânduri în tileset-ul pentru Nivelul 3. */
    public static final int LEVEL3_ROWS = 18;
    /** Numărul de coloane în tileset-ul pentru Nivelul 3. */
    public static final int LEVEL3_COLS = 15;
    /** Dimensiunea unui tile (în pixeli) pentru Nivelul 3. */
    public static final int LEVEL3_TILE_SIZE = 16;
    
    /**
     * Returnează numărul de rânduri din tileset pentru nivelul specificat.
     * 
     * @param levelNumber Numărul nivelului.
     * @return Numărul de rânduri din tileset.
     */
    public int getTilesetRows(int levelNumber) {
        switch (levelNumber) {
            case 1: return LEVEL1_ROWS;
            case 2: return LEVEL2_ROWS;
            case 3: return LEVEL3_ROWS;
            default: return LEVEL1_ROWS; // Implicit pentru Nivelul 1
        }
    }
    
    /**
     * Returnează numărul de coloane din tileset pentru nivelul specificat.
     * 
     * @param levelNumber Numărul nivelului.
     * @return Numărul de coloane din tileset.
     */
    public int getTilesetCols(int levelNumber) {
        switch (levelNumber) {
            case 1: return LEVEL1_COLS;
            case 2: return LEVEL2_COLS;
            case 3: return LEVEL3_COLS;
            default: return LEVEL1_COLS; // Implicit pentru Nivelul 1
        }
    }
    
    /**
     * Returnează dimensiunea unui tile (în pixeli) pentru nivelul specificat.
     * 
     * @param levelNumber Numărul nivelului.
     * @return Dimensiunea unui tile în pixeli.
     */
    public int getTileSize(int levelNumber) {
        switch (levelNumber) {
            case 1: return LEVEL1_TILE_SIZE;
            case 2: return LEVEL2_TILE_SIZE;
            case 3: return LEVEL3_TILE_SIZE;
            default: return LEVEL1_TILE_SIZE; // Implicit pentru Nivelul 1
        }
    }
    
    /**
     * Creează un obiect {@link Level} pe baza numărului de nivel specificat.
     * 
     * @param levelNumber Numărul nivelului de creat.
     * @return Obiectul {@link Level} creat.
     */
    public Level createLevel(int levelNumber) {
        switch (levelNumber) {
            case 1:
                return createLevel1();
            case 2:
                return createLevel2();
            case 3:
                return createLevel3();
            default:
                // Implicit, creează Nivelul 1 dacă numărul este invalid
                System.out.println("Număr de nivel invalid: " + levelNumber + ". Se încarcă Nivelul 1 implicit.");
                return createLevel1();
        }
    }
    
    /**
     * Pre-procesează datele nivelului pentru a gestiona punctele de spawn ale inamicilor.
     * Această metodă este apelată înainte de crearea obiectului Level pentru a se asigura
     * că tile-urile corespunzătoare punctelor de spawn ale inamicilor nu sunt interpretate
     * ca blocuri solide sau alte elemente de decor.
     * 
     * @param levelData Datele nivelului de procesat.
     */
    private void preprocessEnemySpawnPoints(int[][] levelData) {
        // Caută codurile de spawn ale inamicilor în datele nivelului
        // Aceste coduri sunt definite în fișierele CSV ale nivelurilor
        // și sunt folosite de EnemyManager pentru a plasa inamicii.
        // Aici, ne asigurăm doar că aceste tile-uri nu sunt suprascrise sau interpretate greșit.
        int regularEnemyCode = -2; // Cod pentru inamici obișnuiți în CSV
        int bossEnemyCode = -3;    // Cod pentru inamici de tip boss în CSV
        // Alte coduri specifice inamicilor pot fi adăugate aici dacă este necesar (de ex., -4, -5 etc.)
        
        int enemyCount = 0;
        int bossCount = 0;
        
        if (levelData == null) return; // Verificare pentru a evita NullPointerException

        for (int j = 0; j < levelData.length; j++) {
            for (int i = 0; i < levelData[j].length; i++) {
                int tileValue = levelData[j][i];
                if (tileValue == regularEnemyCode) { // Sau alte coduri de inamici
                    enemyCount++;
                    // Nu modificăm valoarea, EnemyManager o va folosi
                }
                else if (tileValue == bossEnemyCode) {
                    bossCount++;
                }
                // Adaugă verificări pentru alte coduri de inamici dacă există
            }
        }
        
        System.out.println("Date nivel pre-procesate: Găsit " + enemyCount + " inamici obișnuiți și " + 
                           bossCount + " boși.");
    }
    
    /**
     * Creează și returnează Nivelul 1.
     * Încarcă datele specifice nivelului și le pre-procesează.
     * 
     * @return Obiectul {@link Level} pentru Nivelul 1.
     */
    private Level createLevel1() {
        int[][] levelData = LoadSave.getLevelData(LoadSave.LEVEL1_DATA);
        preprocessEnemySpawnPoints(levelData);
        return new Level(levelData, 1);
    }
    
    /**
     * Creează și returnează Nivelul 2.
     * Încarcă datele specifice nivelului și le pre-procesează.
     * 
     * @return Obiectul {@link Level} pentru Nivelul 2.
     */
    private Level createLevel2() {
        int[][] levelData = LoadSave.getLevelData(LoadSave.LEVEL2_DATA);
        preprocessEnemySpawnPoints(levelData);
        return new Level(levelData, 2);
    }
    
    /**
     * Creează și returnează Nivelul 3.
     * Încarcă datele specifice nivelului și le pre-procesează.
     * 
     * @return Obiectul {@link Level} pentru Nivelul 3.
     */
    private Level createLevel3() {
        int[][] levelData = LoadSave.getLevelData(LoadSave.LEVEL3_DATA);
        preprocessEnemySpawnPoints(levelData);
        return new Level(levelData, 3);
    }
    
    /**
     * Returnează calea către atlasul de tile-uri (spritesheet) corespunzător numărului de nivel.
     * 
     * @param levelNumber Numărul nivelului.
     * @return Calea către fișierul atlas.
     */
    public String getLevelAtlasPath(int levelNumber) {
        switch (levelNumber) {
            case 1:
                return LoadSave.LEVEL1_ATLAS;
            case 2:
                return LoadSave.LEVEL2_ATLAS;
            case 3:
                return LoadSave.LEVEL3_ATLAS;
            default:
                return LoadSave.LEVEL1_ATLAS; // Implicit pentru Nivelul 1
        }
    }
    
    /**
     * Returnează calea către imaginea de fundal corespunzătoare numărului de nivel.
     * 
     * @param levelNumber Numărul nivelului.
     * @return Calea către fișierul imaginii de fundal.
     */
    public String getBackgroundPath(int levelNumber) {
        switch (levelNumber) {
            case 1:
                return LoadSave.LEVEL1_BACKGROUND;
            case 2:
                return LoadSave.LEVEL2_BACKGROUND;
            case 3:
                return LoadSave.LEVEL3_BACKGROUND;
            default:
                return LoadSave.LEVEL1_BACKGROUND;
        }
    }
}
