package utilz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
// import java.io.FileReader; // FileReader nu este folosit, se folosește InputStreamReader
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Clasă utilitară responsabilă pentru încărcarea resurselor jocului,
 * cum ar fi imaginile (sprite atlas-uri) și datele nivelurilor din fișiere.
 * Conține constante pentru căile către fișierele de resurse și metode statice
 * pentru a accesa aceste resurse.
 */
public class LoadSave {

    /** Calea către atlasul de sprite-uri pentru jucător (Koba). */
    public static final String PLAYER_ATLAS = "koba_compressed_spritesheet.png";
    /** Calea către atlasul de sprite-uri pentru Koba în starea "Crystal Rush". */
    public static final String KOBA_RUSH = "koba_compressed_spritesheet_cristal.png";
    /** Calea către atlasul de tile-uri pentru Nivelul 1. */
    public static final String LEVEL1_ATLAS = "Tileset_niv1.png";
    /** Calea către imaginea de fundal pentru Nivelul 1. */
    public static final String LEVEL1_BACKGROUND = "lvl1_bg.png";
    /** Calea către fișierul CSV cu datele pentru Nivelul 1. */
    public static final String LEVEL1_DATA = "level1.csv";
    /** Calea către atlasul de tile-uri pentru Nivelul 2. */
    public static final String LEVEL2_ATLAS = "Tileset_niv2.png";
    /** Calea către imaginea de fundal pentru Nivelul 2. */
    public static final String LEVEL2_BACKGROUND = "lvl2_bg.png";
    /** Calea către fișierul CSV cu datele pentru Nivelul 2. */
    public static final String LEVEL2_DATA = "level2.csv";
    /** Calea către atlasul de tile-uri pentru Nivelul 3. */
    public static final String LEVEL3_ATLAS = "Tileset_niv3.png";
    /** Calea către imaginea de fundal pentru Nivelul 3. */
    public static final String LEVEL3_BACKGROUND = "lvl3_bg.png";
    /** Calea către fișierul CSV cu datele pentru Nivelul 3. */
    public static final String LEVEL3_DATA = "level3.csv";
    /** Calea către spritesheet-ul pentru butoanele de meniu. */
    public static final String MENU_BUTTONS = "sheet_but.png";
    /** Calea către imaginea de fundal pentru meniul principal. */
    public static final String MENU_BACKGROUND = "menu _background.png";
    /** Calea către imaginea de fundal pentru ecranul de start/introducere nume. */
    public static final String START_BACKGROUND = "start_bg.png";
    /** Calea către imaginea cadrului pentru ecranul de încărcare a jocului. */
    public static final String FRAME_LOADGAME = "frame_load_game.png";
    /** Calea către spritesheet-ul pentru butoanele de selecție a nivelului. */
    public static final String LEVEL_BUTTONS = "level_but.png";
    /** Calea către imaginea cadrului pentru ecranul de introducere a numelui. */
    public static final String ENTER_NAME_FRAME = "NAME.png";
    /** Calea către spritesheet-ul pentru Nanite-ii din junglă. */
    public static final String NANITE_JUNGLA = "compressed_Nanite_Negre_Jungla.png";
    /** Calea către spritesheet-ul pentru Nanite-ii din peșteră. */
    public static final String NANITE_PESTERA = "compressed_Nanite_Negre_Pestera.png";
    /** Calea către imaginea pentru interfața utilizator (HUD). */
    public static final String GAME_UI = "game UI_clean.png";
    /** Calea către spritesheet-ul pentru inamicul Karagor. */
    public static final String KARAGOR_SPRITESHEET = "karagor_compressed_spritesheet.png";
    /** Calea către imaginea pentru piatra prețioasă verde. */
    public static final String GREEN_GEM = "green_gem.png";
    /** Calea către imaginea pentru piatra prețioasă portocalie. */
    public static final String ORANGE_GEM = "orange_gem.png";
    /** Calea către imaginea pentru piatra prețioasă mov. */
    public static final String PURPLE_GEM = "purple_gem.png";
    /** Calea către sprite-ul pentru banană (colectabil). */
    public static final String BANANA_SPRITE = "banana.png";
    /** Calea către sprite-ul pentru nuca de cocos statică (colectabil). */
    public static final String COCONUT_SPRITE = "coconut_static.png";
    /** Calea către sprite-ul pentru nuca de cocos aruncabilă (proiectil). */
    public static final String COCONUT_THROWABLE_SPRITE = "coconut.png";

    /** Calea către spritesheet-ul pentru Goblinii de tip "noob". */
    public static final String GOBLIN_NOOB_SPRITESHEET = "compressed_goblin_mob_noob.png";
    /** Calea către spritesheet-ul pentru Goblinii de tip "hard". */
    public static final String GOBLIN_HARD_SPRITESHEET = "compressed_goblin_mob_hard.png";
    /** Calea către spritesheet-ul pentru Goblin Boss. */
    public static final String GOBLIN_BOSS_SPRITESHEET = "compressed_goblin_boss.png";
    /** Calea către spritesheet-ul pentru Golem Boss. */
    public static final String GOLEM_BOSS_SPRITESHEET = "compressed_golem_boss_purple.png";

    /** Imaginea preîncărcată pentru banană. */
    public static BufferedImage BANANA_IMAGE;
    /** Imaginea preîncărcată pentru nuca de cocos statică (colectabil). */
    public static BufferedImage COCONUT_IMAGE;
    /** Imaginea preîncărcată pentru nuca de cocos aruncabilă. */
    public static BufferedImage COCONUT_THROWABLE_IMAGE;

    /**
     * Bloc static de inițializare pentru a preîncărca imaginile comune
     * pentru obiectele colectabile (banană, nucă de cocos).
     */
    static {
        BANANA_IMAGE = getSpriteAtlas(BANANA_SPRITE);
        if (BANANA_IMAGE != null) {
            System.out.println("LoadSave: BANANA_IMAGE încărcată cu succes. Lățime: " + BANANA_IMAGE.getWidth() + ", Înălțime: " + BANANA_IMAGE.getHeight());
        } else {
            System.err.println("LoadSave: Eroare la încărcarea BANANA_IMAGE!");
        }

        COCONUT_IMAGE = getSpriteAtlas(COCONUT_SPRITE);
        if (COCONUT_IMAGE != null) {
            System.out.println("LoadSave: COCONUT_IMAGE (static) încărcată cu succes. Lățime: " + COCONUT_IMAGE.getWidth() + ", Înălțime: " + COCONUT_IMAGE.getHeight());
        } else {
            System.err.println("LoadSave: Eroare la încărcarea COCONUT_IMAGE (static)!");
        }

        COCONUT_THROWABLE_IMAGE = getSpriteAtlas(COCONUT_THROWABLE_SPRITE);
        if (COCONUT_THROWABLE_IMAGE != null) {
            System.out.println("LoadSave: COCONUT_THROWABLE_IMAGE încărcată cu succes. Lățime: " + COCONUT_THROWABLE_IMAGE.getWidth() + ", Înălțime: " + COCONUT_THROWABLE_IMAGE.getHeight());
        } else {
            System.err.println("LoadSave: Eroare la încărcarea COCONUT_THROWABLE_IMAGE!");
        }
    }
    
    /**
     * Încarcă o imagine (sprite atlas) din directorul de resurse.
     *
     * @param filename Numele fișierului imagine (de ex., "player_atlas.png").
     * @return Un obiect {@link BufferedImage} reprezentând imaginea încărcată,
     *         sau {@code null} dacă încărcarea eșuează.
     */
    public static BufferedImage getSpriteAtlas(String filename) {
        BufferedImage img = null;
        InputStream is = LoadSave.class.getResourceAsStream("/res/" + filename);
        if (is == null) {
            System.err.println("Eroare: Resursa nu a fost găsită: /res/" + filename);
            return null;
        }
        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Eroare la citirea imaginii: /res/" + filename);
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return img;
    }

    /**
     * Încarcă datele unui nivel dintr-un fișier CSV specificat.
     * Fișierul CSV trebuie să fie localizat în directorul de resurse ("/res/").
     * Fiecare linie din CSV reprezintă un rând de tile-uri, iar valorile sunt separate prin spații.
     *
     * @param filePath Calea către fișierul CSV, relativă la directorul "/res/" (de ex., "level1.csv").
     * @return O matrice bidimensională de întregi ({@code int[][]}) reprezentând datele nivelului.
     *         Returnează un array gol ({@code new int[0][0]}) dacă fișierul nu este găsit sau apare o eroare.
     */
    public static int[][] getLevelData(String filePath) {
        String resourcePath = "/res/" + filePath;
        InputStream is = LoadSave.class.getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("Nu s-a putut găsi fișierul de date al nivelului: " + resourcePath);
            return new int[0][0]; 
        }

        List<int[]> tempData = new ArrayList<>();
        int maxCols = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split("\\s+"); // Separă prin unul sau mai multe spații
                if (values.length == 1 && values[0].isEmpty()) { // Ignoră liniile goale
                    continue;
                }
                int[] rowValues = new int[values.length];
                for (int col = 0; col < values.length; col++) {
                    try {
                        rowValues[col] = Integer.parseInt(values[col]);
                    } catch (NumberFormatException e) {
                        System.err.println("Eroare la parsarea numărului în datele nivelului la col " + col + ": '" + values[col] + "' în linia: " + line);
                        rowValues[col] = -1; // Valoare implicită în caz de eroare (tile gol)
                    }
                }
                tempData.add(rowValues);
                maxCols = Math.max(maxCols, values.length);
            }
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului de date al nivelului: " + resourcePath);
            e.printStackTrace();
            return new int[0][0]; 
        }

        // Converteste lista la un array 2D, asigurând lățime uniformă
        if (tempData.isEmpty()) {
            return new int[0][0];
        }
        int[][] lvlData = new int[tempData.size()][maxCols];
        for (int i = 0; i < tempData.size(); i++) {
            // Copiază rândul, completând cu 0 (sau altă valoare implicită) dacă rândul e mai scurt
            lvlData[i] = Arrays.copyOf(tempData.get(i), maxCols); 
        }
        return lvlData;
    }
}
