package levels;

import entities.Banana; 
import entities.Coconut; 
import java.util.ArrayList; 
import main.Game; 

/**
 * Reprezintă un nivel individual în joc.
 * Stochează datele despre tile-urile nivelului, obiectele colectabile (banane, nuci de cocos)
 * și gestionează offset-ul de derulare (scrolling) al nivelului.
 */
public class Level {
    /** Matrice bidimensională ce conține datele despre tile-urile nivelului (ID-urile sprite-urilor). */
    private int[][] lvlData;
    /** Lista bananelor prezente în nivel. */
    private ArrayList<Banana> bananas; 
    /** Lista nucilor de cocos prezente în nivel. */
    private ArrayList<Coconut> coconuts; 
    /** Offset-ul curent de derulare orizontală a nivelului, în tile-uri. */
    private int levelOffset;
    /** Offset-ul maxim de derulare orizontală a nivelului, în tile-uri. */
    private int maxTilesOffset;
    /** Offset-ul maxim de derulare orizontală a nivelului, în pixeli. */
    private int maxLevelOffsetX;
    /** Identificatorul numeric al nivelului (de ex., 1, 2, 3). */
    private int levelId; 
    // private static final int LEVEL_WIDTH_PIXELS = 32 * 32; // Lățimea implicită a nivelului în pixeli (neutilizat activ)

    /**
     * Constructor principal pentru clasa Level.
     *
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @param levelId Identificatorul numeric al nivelului.
     */
    public Level(int[][] lvlData, int levelId) {
        this.lvlData = lvlData;
        this.levelId = levelId;
        this.levelOffset = 0;
        if (lvlData != null && lvlData.length > 0 && lvlData[0] != null) {
            this.maxTilesOffset = Math.max(0, lvlData[0].length - Game.TILES_IN_WIDTH); 
        } else {
            this.maxTilesOffset = 0; 
        }
        this.maxLevelOffsetX = this.maxTilesOffset * Game.TILES_SIZE; 
    }
    
    /**
     * Constructor secundar pentru compatibilitate cu codul existent,
     * care nu specifică un ID de nivel. Implicit, se consideră nivelul 1.
     *
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     */
    public Level(int[][] lvlData) {
        this(lvlData, 1); // Presupune nivelul 1 dacă ID-ul nu este specificat
    }

    /**
     * Returnează indexul (ID-ul) sprite-ului pentru un tile la coordonatele specificate.
     *
     * @param x Coordonata x a tile-ului (în unități de tile-uri).
     * @param y Coordonata y a tile-ului (în unități de tile-uri).
     * @return ID-ul sprite-ului pentru tile-ul respectiv.
     */
    public int getSpriteIndex(int x, int y) {
        // Verificări pentru a evita ArrayIndexOutOfBoundsException
        if (lvlData == null || y < 0 || y >= lvlData.length || x < 0 || x >= lvlData[y].length) {
            return 0; // Sau o valoare implicită/de eroare
        }
        return lvlData[y][x];
    }

    /**
     * Actualizează offset-ul de derulare al nivelului pe baza poziției jucătorului.
     * Această metodă nu este apelată direct în fluxul principal de actualizare al jocului,
     * logica de scrolling fiind gestionată în clasa {@link Playing}.
     *
     * @param playerX Poziția x a jucătorului (în pixeli).
     */
    public void update(int playerX) {
        // Calculează noul offset pe baza poziției jucătorului
        // Presupunând că jucătorul ar trebui să fie la 640 pixeli (centrul ecranului)
        int newOffset = (playerX - Game.GAME_WIDTH / 2) / Game.TILES_SIZE; 
        
        // Limitează offset-ul între 0 și maxTilesOffset
        if (newOffset < 0) {
            newOffset = 0;
        } else if (newOffset > maxTilesOffset) {
            newOffset = maxTilesOffset;
        }
        
        this.levelOffset = newOffset;
    }

    /**
     * Returnează offset-ul curent de derulare orizontală a nivelului, în tile-uri.
     * @return Offset-ul nivelului.
     */
    public int getLevelOffset() {
        return levelOffset;
    }

    /**
     * Returnează offset-ul maxim de derulare orizontală a nivelului, în pixeli.
     * @return Offset-ul maxim al nivelului.
     */
    public int getMaxLevelOffsetX() {
        return maxLevelOffsetX;
    }

    /**
     * Returnează matricea 2D cu datele tile-urilor nivelului.
     * @return Datele nivelului.
     */
    public int[][] getLevelData(){
        return lvlData;
    }
    
    /**
     * Returnează ID-ul nivelului curent.
     * @return ID-ul nivelului (de ex., 1, 2, 3).
     */
    public int getLevelId() {
        return levelId;
    }
    
    /**
     * Setează ID-ul nivelului.
     * @param levelId Noul ID pentru nivel.
     */
    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    /**
     * Returnează lista de banane din nivel.
     * @return O listă de obiecte {@link Banana}. Poate fi null dacă nu au fost adăugate banane.
     */
    public ArrayList<Banana> getBananas() {
        return bananas;
    }

    /**
     * Setează lista de banane pentru nivel.
     * @param bananas Noua listă de banane.
     */
    public void setBananas(ArrayList<Banana> bananas) {
        this.bananas = bananas;
    }

    /**
     * Adaugă o banană la lista de banane a nivelului.
     * Inițializează lista dacă este null.
     * @param b Banana de adăugat.
     */
    public void addBanana(Banana b) {
        if (this.bananas == null) {
            this.bananas = new ArrayList<>();
        }
        this.bananas.add(b);
    }

    /**
     * Returnează lista de nuci de cocos din nivel.
     * @return O listă de obiecte {@link Coconut}. Poate fi null dacă nu au fost adăugate nuci de cocos.
     */
    public ArrayList<Coconut> getCoconuts() {
        return coconuts;
    }

    /**
     * Setează lista de nuci de cocos pentru nivel.
     * @param coconuts Noua listă de nuci de cocos.
     */
    public void setCoconuts(ArrayList<Coconut> coconuts) {
        this.coconuts = coconuts;
    }

    /**
     * Adaugă o nucă de cocos la lista de nuci de cocos a nivelului.
     * Inițializează lista dacă este null.
     * @param c Nuca de cocos de adăugat.
     */
    public void addCoconut(Coconut c) {
        if (this.coconuts == null) {
            this.coconuts = new ArrayList<>();
        }
        this.coconuts.add(c);
    }
}
