package levels;

import main.Game;
import utilz.LoadSave;
import java.awt.*;
import java.awt.image.BufferedImage;

import static main.Game.TILES_SIZE;

/**
 * Gestionează încărcarea, desenarea și tranziția între nivelurile jocului.
 * Utilizează {@link LevelFactory} pentru a crea instanțe de {@link Level}.
 */
public class LevelManager {
    private Game game; // Referință la instanța principală a jocului
    /** Array de imagini pentru tile-urile sprite-urilor nivelului curent. */
    private BufferedImage[] levelSprite;
    /** Imaginea de fundal pentru nivelul curent. */
    private BufferedImage backgroundImage;
    /** Obiectul {@link Level} curent încărcat. */
    private Level currentLevel;
    /** Fabrica utilizată pentru a crea obiecte de tip Level. */
    private LevelFactory levelFactory;
    /** Numărul (indexul) nivelului curent. */
    private int currentLevelNumber;
    
    /**
     * Constructor pentru LevelManager.
     * Inițializează fabrica de niveluri și încarcă primul nivel implicit.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public LevelManager(Game game) {
        this.game = game;
        this.levelFactory = new LevelFactory();
        this.currentLevelNumber = 1; // Implicit, începe cu Nivelul 1
        
        loadLevel(currentLevelNumber);
    }
    
    /**
     * Încarcă un nivel specific pe baza numărului său.
     * Importă sprite-urile și imaginea de fundal corespunzătoare și creează obiectul Level.
     * 
     * @param levelNumber Numărul nivelului de încărcat.
     */
    public void loadLevel(int levelNumber) {
        this.currentLevelNumber = levelNumber;
        importSpritesForLevel(levelNumber);
        importBackgroundForLevel(levelNumber);
        currentLevel = levelFactory.createLevel(levelNumber);
        // Asigură inițializarea listei de banane dacă LevelFactory nu o face
        if (currentLevel != null && currentLevel.getBananas() == null) {
            currentLevel.setBananas(new java.util.ArrayList<>());
        }
        // Similar pentru nuci de cocos, dacă este cazul
        if (currentLevel != null && currentLevel.getCoconuts() == null) {
            currentLevel.setCoconuts(new java.util.ArrayList<>());
        }
    }

    /**
     * Încarcă imaginea de fundal pentru nivelul specificat.
     *
     * @param levelNumber Numărul nivelului pentru care se încarcă fundalul.
     */
    private void importBackgroundForLevel(int levelNumber) {
        String backgroundPath = levelFactory.getBackgroundPath(levelNumber);
        backgroundImage = LoadSave.getSpriteAtlas(backgroundPath);
    }

    /**
     * Încarcă și prelucrează atlasul de sprite-uri (tileset) pentru nivelul specificat.
     * Extrage fiecare tile individual și îl stochează în array-ul {@code levelSprite}.
     *
     * @param levelNumber Numărul nivelului pentru care se încarcă sprite-urile.
     */
    private void importSpritesForLevel(int levelNumber) {
        String atlasPath = levelFactory.getLevelAtlasPath(levelNumber);
        BufferedImage img = LoadSave.getSpriteAtlas(atlasPath);
        
        if (img == null) {
            System.err.println("Eroare: Atlasul de sprite-uri pentru nivelul " + levelNumber + " nu a putut fi încărcat: " + atlasPath);
            levelSprite = new BufferedImage[0]; // Array gol pentru a evita NullPointerException
            return;
        }
        
        int rows = levelFactory.getTilesetRows(levelNumber);
        int cols = levelFactory.getTilesetCols(levelNumber);
        int tileSize = levelFactory.getTileSize(levelNumber);
        
        levelSprite = new BufferedImage[rows * cols]; // Alocă array-ul pentru tile-uri
        
        // Extrage fiecare tile din imaginea tileset
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                int index = j * cols + i;
                if (index < levelSprite.length) { // Verificare suplimentară de siguranță
                    levelSprite[index] = img.getSubimage(i * tileSize, j * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    /**
     * Desenează nivelul curent pe ecran.
     * Mai întâi desenează imaginea de fundal, apoi tile-urile nivelului.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     * @param xLvlOffset Offset-ul orizontal de derulare al nivelului.
     */
    public void draw(Graphics g, int xLvlOffset) {
        // Desenează fundalul mai întâi
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        }
        
        // Desenează tile-urile peste fundal
        if (currentLevel != null && currentLevel.getLevelData() != null && levelSprite != null) {
            for (int j = 0; j < Game.TILES_IN_HEIGHT; j++){ // Presupunând că Game.TILES_IN_HEIGHT este corect pentru înălțimea vizibilă
                // Asigură-te că j este în limitele datelor nivelului
                if (j >= currentLevel.getLevelData().length) continue;

                for (int i = 0; i < currentLevel.getLevelData()[0].length; i++) {
                    int index = currentLevel.getSpriteIndex(i, j);
                    // Desenează tile-ul doar dacă indexul este valid și în limitele array-ului levelSprite
                    if (index >= 0 && index < levelSprite.length && levelSprite[index] != null) {
                        g.drawImage(levelSprite[index], TILES_SIZE * i - xLvlOffset, TILES_SIZE * j, TILES_SIZE, TILES_SIZE, null);
                    }
                }
            }
        }
    }

    /**
     * Actualizează logica specifică managerului de niveluri.
     * Momentan, nu conține logică de actualizare specifică.
     */
    public void update() {
        // Orice actualizări specifice nivelului pot fi adăugate aici
    }

    /**
     * Returnează obiectul {@link Level} curent încărcat.
     * @return Nivelul curent.
     */
    public Level getCurrentLevel(){
        return currentLevel;
    }
    
    /**
     * Returnează numărul (indexul) nivelului curent.
     * @return Numărul nivelului curent.
     */
    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }
    
    /**
     * Trece la următorul nivel, dacă există.
     * 
     * @return {@code true} dacă s-a trecut la următorul nivel cu succes, {@code false} dacă nu mai sunt niveluri.
     */
    public boolean nextLevel() {
        if (currentLevelNumber < 3) { // Presupunând că există 3 niveluri
            loadLevel(currentLevelNumber + 1);
            return true;
        }
        return false; // Nu mai sunt niveluri
    }
    
    /**
     * Trece la nivelul anterior, dacă există.
     * 
     * @return {@code true} dacă s-a trecut la nivelul anterior cu succes, {@code false} dacă nu există un nivel anterior.
     */
    public boolean previousLevel() {
        if (currentLevelNumber > 1) {
            loadLevel(currentLevelNumber - 1);
            return true;
        }
        return false;
    }
}
