package gamestates;

import database.InsertGet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

/**
 * Reprezintă un overlay pentru introducerea numelui jucătorului.
 * Această stare permite jucătorului să introducă un nume, care este apoi folosit
 * pentru a salva sau încărca progresul jocului.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class EnterNameOverlay extends State implements Statemethods {

    private BufferedImage startbgImg; // Imaginea de fundal pentru ecranul de start/introducere nume
    private BufferedImage frameImg; // Imaginea cadrului pentru câmpul de introducere a numelui
    private int startbgX, startbgY, startbgWidth, startbgHeight; // Coordonate și dimensiuni pentru fundal
    private int frameX, frameY, frameWidth, frameHeight; // Coordonate și dimensiuni pentru cadru
    private MenuButton startButton; // Butonul pentru a începe jocul după introducerea numelui
    private Rectangle closeButtonBounds; // Limitele pentru un eventual buton de închidere (X)
    private StringBuilder username = new StringBuilder(); // Numele de utilizator introdus de jucător
    /** Lungimea maximă permisă pentru numele de utilizator. */
    private final int MAX_NAME_LENGTH = 15;
    /** Indică dacă input-ul de la tastatură este activ pentru acest overlay. */
    private boolean inputActive = true;
    // private boolean needsRedraw = true; // Flag pentru redesenare (neutilizat activ în codul furnizat)
    private Gamestate previousState = null; // Starea anterioară a jocului, pentru a reveni dacă este cazul
    
    private Font airstrikeFont; // Fontul personalizat folosit pentru text
    private String errorMessage = ""; // Mesaj de eroare afișat utilizatorului (de ex., nume invalid)
    /** Indică dacă se afișează prompt-ul pentru încărcarea unui joc salvat existent. */
    private boolean showLoadPrompt = false;
    // Dreptunghiuri pentru butoanele YES/NO din prompt-ul de încărcare
    private Rectangle yesButton = new Rectangle(775, 650, 100, 40);
    private Rectangle noButton = new Rectangle(925, 650, 100, 40);
    
    // Câmpuri legate de baza de date
    /** Calea către fișierul bazei de date. */
    private static final String DB_FILE = "data/gamedatabase.db";
    /** Numele tabelului principal pentru progresul jucătorului (neutilizat direct, se folosește username-ul). */
    // private static final String PLAYER_TABLE = "player_progress"; // Comentat deoarece nu este folosit direct
    // Câmpuri pentru stocarea datelor unui joc existent, dacă este găsit
    private int existingLevel = 0;
    private int existingScore = 0;
    private int existingHealth = 0;
    private int existingCoconuts = 0;
    private float existingPosX = 0;
    private float existingPosY = 0;

    /**
     * Constructor pentru EnterNameOverlay.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public EnterNameOverlay(Game game) {
        super(game);
        loadButtons();
        loadStartImg();
        loadFrame();
        loadCustomFont();
        closeButtonBounds = new Rectangle(1168, 278, 63, 63); // Coordonate și dimensiuni pentru butonul X
    }

    /**
     * Încarcă și configurează imaginea de fundal.
     */
    private void loadStartImg() {
        startbgImg = LoadSave.getSpriteAtlas(LoadSave.START_BACKGROUND);
        startbgWidth = (int) (startbgImg.getWidth() * Game.SCALE);
        startbgHeight = (int) (startbgImg.getHeight() * Game.SCALE);
        startbgX = Game.GAME_WIDTH / 2 - startbgWidth / 2;
        startbgY = (int) (0 * Game.SCALE);
    }

    /**
     * Încarcă și configurează imaginea cadrului pentru introducerea numelui.
     */
    private void loadFrame(){
        frameImg = LoadSave.getSpriteAtlas(LoadSave.ENTER_NAME_FRAME);
        frameWidth = (int) (frameImg.getWidth() * Game.SCALE);
        frameHeight = (int) (frameImg.getHeight() * Game.SCALE);
        frameX = Game.GAME_WIDTH / 2 - frameWidth / 2;
        frameY = (int) (45 * Game.SCALE);
    }

    /**
     * Inițializează butonul de start.
     */
     private void loadButtons() {
        startButton = new MenuButton(Game.GAME_WIDTH / 2, (int) (720 * Game.SCALE), 0, Gamestate.ENTER_NAME); // Indexul 0 pentru tipul de buton
     }

    /**
     * Actualizează starea overlay-ului. Momentan, actualizează doar butonul de start.
     */
    @Override
    public void update() {
        startButton.update();
    }

    /**
     * Desenează elementele overlay-ului pe ecran.
     * Include fundalul, cadrul, câmpul pentru nume, mesajele de eroare și prompt-ul de încărcare.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        // Fundal
        g.drawImage(startbgImg, startbgX, startbgY, startbgWidth, startbgHeight, null);
        g.drawImage(frameImg, frameX, frameY, frameWidth, frameHeight, null);
        // Buton START GAME
        startButton.draw(g);
        // X (poți desena un X roșu sau doar hitbox)

        // Username transparent (doar text, fără dreptunghi)
        g.setFont(airstrikeFont.deriveFont(36f));
        g.setColor(new Color(255, 255, 255, 200)); // alb semi-transparent
        // Poziționează textul în dreptul casetei din imagine
        g.drawString(username.toString(), 775, 550);

        // Draw error message if exists
        if (!errorMessage.isEmpty()) {
            g.setColor(Color.RED);
            g.setFont(airstrikeFont.deriveFont(24f));
            g.drawString(errorMessage, 775, 600);
        }

        // Draw load progress prompt if showing
        if (showLoadPrompt) {
            // Draw semi-transparent background
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(700, 500, 400, 200);
            
            // Draw prompt text
            g.setColor(Color.WHITE);
            g.setFont(airstrikeFont.deriveFont(20f));
            g.drawString("Existing save found:", 775, 530);
            g.drawString("Level: " + existingLevel, 775, 560);
            g.drawString("Score: " + existingScore, 775, 590);
            g.drawString("Health: " + existingHealth, 775, 620);
            g.drawString("Load this save?", 775, 650);

            // Draw buttons
            g.setColor(new Color(0, 100, 0, 200));
            g.fillRect(yesButton.x, yesButton.y, yesButton.width, yesButton.height);
            g.setColor(new Color(100, 0, 0, 200));
            g.fillRect(noButton.x, noButton.y, noButton.width, noButton.height);
            
            g.setColor(Color.WHITE);
            g.setFont(airstrikeFont.deriveFont(20f));
            g.drawString("YES", yesButton.x + 30, yesButton.y + 25);
            g.drawString("NO", noButton.x + 35, noButton.y + 25);
        }
    }

    /**
     * Procesează numele de utilizator introdus.
     * Verifică dacă există un joc salvat pentru acest nume. Dacă da, afișează un prompt pentru încărcare.
     * Dacă nu, creează o nouă salvare și trece la meniul principal.
     *
     * @param playerName Numele de utilizator de procesat.
     */
    private void processUsername(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            errorMessage = "Numele nu poate fi gol!";
            return;
        }
        // Asigură-te că username este actualizat înainte de a-l folosi pentru operațiuni DB
        this.username = new StringBuilder(playerName.trim());

        // Verifică dacă jucătorul există încercând să încarce datele
        existingLevel = InsertGet.LoadLevelIndex(DB_FILE, this.username.toString());
        if (existingLevel <= 0) { // Considerăm 0 sau negativ ca jucător nou sau eroare la încărcare
            // Jucător nou - creează o salvare inițială
            InsertGet.SaveIntoDatabase(
                DB_FILE,
                this.username.toString(),
                1, // Level index
                0, // Score
                100, // Health
                0, // Coconuts
                100 * Game.SCALE, // PosX (valori implicite de start)
                770 * Game.SCALE, // PosY
                0 // Timer
            );
            game.setSessionUsername(this.username.toString());
            if (game.getMenu() != null) game.getMenu().setPlayerName(this.username.toString());
            System.out.println("Joc încărcat cu utilizator nou: " + this.username.toString());
            Gamestate.state = Gamestate.MENU;
            errorMessage = "";
            showLoadPrompt = false; // Asigură-te că prompt-ul nu este afișat pentru jucători noi
        } else {
            // Încarcă progresul jucătorului existent
            existingScore = InsertGet.LoadScore(DB_FILE, this.username.toString());
            existingHealth = InsertGet.LoadCurrentHealth(DB_FILE, this.username.toString());
            existingCoconuts = InsertGet.LoadCoconutNumber(DB_FILE, this.username.toString());
            existingPosX = InsertGet.LoadXPosition(DB_FILE, this.username.toString());
            existingPosY = InsertGet.LoadYPosition(DB_FILE, this.username.toString());
            // Ar trebui încărcat și timer-ul dacă este relevant
            // existingTimer = InsertGet.LoadTimer(DB_FILE, this.username.toString());
            showLoadPrompt = true; // Afișează prompt-ul pentru încărcare
            errorMessage = "";
        }
    }

    /**
     * Gestionează evenimentele de click al mouse-ului.
     * Procesează click-urile pe butonul de start sau pe opțiunile din prompt-ul de încărcare.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (showLoadPrompt) {
            Point click = e.getPoint();
            if (yesButton.contains(click)) {
                // Încarcă progresul existent
                game.setSessionUsername(this.username.toString());
                if (game.getMenu() != null) game.getMenu().setPlayerName(this.username.toString());
                // Aici ar trebui să încarci și nivelul, scorul, etc. în starea jocului (Playing)
                // De exemplu: game.getPlaying().loadProgress(existingLevel, existingScore, ...);
                System.out.println("Progres existent încărcat pentru: " + this.username.toString());
                Gamestate.state = Gamestate.MENU;
                showLoadPrompt = false; // Ascunde prompt-ul după acțiune
            } else if (noButton.contains(click)) {
                // Jucătorul a ales să nu încarce salvarea. Creează o nouă salvare.
                InsertGet.SaveIntoDatabase(
                    DB_FILE,
                    this.username.toString(),
                    1, 0, 100, 0, 100 * Game.SCALE, 770 * Game.SCALE, 0
                );
                game.setSessionUsername(this.username.toString());
                if (game.getMenu() != null) game.getMenu().setPlayerName(this.username.toString());
                System.out.println("S-a început un joc nou pentru: " + this.username.toString());
                Gamestate.state = Gamestate.MENU;
                showLoadPrompt = false;
                errorMessage = "";
            }
        } else if (startButton.getBounds().contains(e.getPoint())) {
            processUsername(this.username.toString().trim()); // Trimite numele curățat
        }
    }

    /**
     * Gestionează evenimentele de apăsare a butonului mouse-ului.
     * Setează starea de apăsare pentru butonul de start și gestionează butonul de închidere.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY());
        // System.out.println("Close button bounds: " + closeButtonBounds);
        
        if (isIn(e, startButton)) {
            startButton.setMousePressed(true);
        }
        
       if (closeButtonBounds != null && closeButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul X apăsat!");
            if (previousState != null) {
                Gamestate.state = previousState;
            } else {
                Gamestate.state = Gamestate.MENU; // Implicit, revine la meniu
            }
            // needsRedraw = true; // Acest flag nu pare a fi folosit
        }
    }

    /**
     * Gestionează evenimentele de eliberare a butonului mouse-ului.
     * Finalizează acțiunea butonului de start dacă a fost apăsat.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (isIn(e, startButton)) {
            if(startButton.isMousePressed()){
                // Acțiunea de click este gestionată în mouseClicked pentru a evita dubla procesare
                // processUsername(username.toString().trim());
            }
        }
        startButton.resetBools();
    }

    /**
     * Gestionează evenimentele de mișcare a mouse-ului.
     * Setează starea "mouse over" pentru butonul de start.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        startButton.setMouseOver(false);
        if (isIn(e, startButton)) {
            startButton.setMouseOver(true);
        }
    }

    /**
     * Gestionarea evenimentului de tragere a mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        // Neutilizat
    }

    /**
     * Gestionează evenimentele de apăsare a tastelor.
     * Permite introducerea numelui de utilizator, ștergerea caracterelor (Backspace)
     * și confirmarea numelui (Enter).
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (!inputActive || showLoadPrompt) return; // Nu procesa input dacă prompt-ul este activ

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && username.length() > 0) {
            username.deleteCharAt(username.length() - 1);
            errorMessage = ""; // Șterge mesajul de eroare la editare
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER && username.length() > 0) {
            processUsername(username.toString().trim());
        } else {
            char c = e.getKeyChar();
            // Permite litere, cifre, spațiu, underscore, cratimă
            if ((Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-') && username.length() < MAX_NAME_LENGTH) {
                username.append(c);
                errorMessage = ""; // Șterge mesajul de eroare la editare
            }
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * Încarcă fontul personalizat folosit în acest overlay.
     */
    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/font/airstrikebold.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            airstrikeFont = baseFont.deriveFont(Font.BOLD, 36f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);
        } catch (Exception e) {
            e.printStackTrace();
            airstrikeFont = new Font("Arial", Font.BOLD, 36);
        }
    }
}
