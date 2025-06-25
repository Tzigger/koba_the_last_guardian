package main;

import java.awt.Graphics;
import gamestates.*;

/**
 * Clasa principală a jocului, responsabilă pentru gestionarea stărilor de joc,
 * bucla principală a jocului (game loop) și inițializarea componentelor cheie.
 * Implementează interfața {@link Runnable} pentru a rula într-un fir de execuție separat.
 */
public class Game implements Runnable {

    /** Numele de utilizator pentru sesiunea curentă de joc. */
    private String sessionUsername; 

    /**
     * Setează numele de utilizator pentru sesiunea curentă.
     * @param username Noul nume de utilizator.
     */
    public void setSessionUsername(String username) { this.sessionUsername = username;}

    /**
     * Returnează numele de utilizator al sesiunii curente.
     * @return Numele de utilizator.
     */
    public String getSessionUsername() {return sessionUsername;}

    private GameWindow gameWindow; // Fereastra principală a jocului
    private GamePanel gamePanel;   // Panoul pe care se desenează jocul
    private Thread gameThread;     // Firul de execuție pentru bucla jocului

    /** Numărul țintă de cadre pe secundă (Frames Per Second). */
    private final double FPS_SET = 60;
    /** Numărul țintă de actualizări logice pe secundă (Updates Per Second). */
    private final double UPS_SET = 120;

    // Instanțe pentru fiecare stare de joc
    private Playing playing;
    private Menu menu;
    private Options options;
    private Loadgame loadgame;
    private Leaderboard leaderboard;
    private EnterNameOverlay enterNameOverlay;

    /** Dimensiunea implicită a unui tile (în pixeli) înainte de scalare. */
    public final static int TILES_DEFAULT_SIZE = 60;
    /** Factorul de scalare aplicat elementelor jocului. */
    public final static float SCALE = 1.0f;
    /** Numărul de tile-uri vizibile pe lățimea ecranului. */
    public final static int TILES_IN_WIDTH = 32;
    /** Numărul de tile-uri vizibile pe înălțimea ecranului. */
    public final static int TILES_IN_HEIGHT = 18;

    /** Dimensiunea finală a unui tile (în pixeli) după aplicarea scalei. */
    public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);

    /** Lățimea totală a ferestrei jocului în pixeli. */
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    /** Înălțimea totală a ferestrei jocului în pixeli. */
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    /**
     * Constructor pentru clasa Game.
     * Inițializează toate clasele necesare, panoul de joc, fereastra
     * și pornește bucla principală a jocului.
     */
    public Game() {
        initClasses();
        gamePanel = new GamePanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.requestFocus(); // Solicită focusul pentru panoul de joc pentru a primi input
        startGameLoop();
    }

    /**
     * Inițializează toate instanțele stărilor de joc.
     */
    private void initClasses() {
        menu = new Menu(this);
        playing = new Playing(this);
        options = new Options(this);
        loadgame = new Loadgame(this);
        leaderboard = new Leaderboard(this);
        enterNameOverlay = new EnterNameOverlay(this);
    }

    /**
     * Pornește bucla principală a jocului într-un nou fir de execuție.
     */
    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Actualizează logica jocului în funcție de starea curentă ({@link Gamestate}).
     * Deleagă actualizarea către metoda {@code update} a stării active.
     * În cazul stării QUIT, închide aplicația.
     */
    public void update() {
        switch (Gamestate.state) {
            case MENU:
                menu.update();
                break;
            case PLAYING:
                playing.update();
                break;
            case OPTIONS:
                options.update();
                break;
            case LOADGAME:
                loadgame.update();
                break;
            case LEADERBOARD:
                leaderboard.update();
                break;
            case ENTER_NAME:
                enterNameOverlay.update();
                break;
            case QUIT:
            default:
                System.exit(0); // Închide aplicația
                break;
        }
    }

    /**
     * Desenează conținutul jocului în funcție de starea curentă ({@link Gamestate}).
     * Deleagă desenarea către metoda {@code draw} a stării active.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    public void render(Graphics g) {
        switch (Gamestate.state) {
            case MENU:
                menu.draw(g);
                break;
            case PLAYING:
                playing.draw(g);
                break;
            case OPTIONS:
                options.draw(g);
                break;
            case LOADGAME:
                loadgame.draw(g);
                break;
            case LEADERBOARD:
                leaderboard.draw(g);
                break;
            case ENTER_NAME:
                enterNameOverlay.draw(g);
                break;
            default:
                break;
        }
    }

    /**
     * Metoda principală a buclei jocului, implementată din interfața {@link Runnable}.
     * Gestionează sincronizarea actualizărilor logice (UPS) și a cadrelor desenate (FPS).
     */
    @Override
    public void run() {
        double timePerFrame = 1_000_000_000.0 / FPS_SET; // Timpul alocat per cadru, în nanosecunde
        double timePerUpdate = 1_000_000_000.0 / UPS_SET; // Timpul alocat per actualizare logică

        long previousTime = System.nanoTime(); // Timpul anterior înregistrării

        int frames = 0; // Contor pentru cadrele desenate
        int updates = 0; // Contor pentru actualizările logice
        long lastCheck = System.currentTimeMillis(); // Pentru afișarea FPS/UPS la fiecare secundă
        
        double deltaU = 0; // Acumulator pentru timpul scurs pentru actualizări
        double deltaF = 0; // Acumulator pentru timpul scurs pentru cadre

        while (true) {  // Bucla infinită a jocului
            long currentTime = System.nanoTime(); // Timpul curent
            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            // Efectuează actualizările logice dacă s-a scurs suficient timp
            if(deltaU >= 1){
                update();
                updates++;
                deltaU--;
            }
            
            // Desenează un nou cadru dacă s-a scurs suficient timp
            if(deltaF >= 1){
                gamePanel.repaint(); // Solicită redesenarea panoului
                frames++;
                deltaF--;
            }

            // Afișează FPS și UPS la fiecare secundă (pentru depanare)
            if(System.currentTimeMillis() - lastCheck >= 1000){
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                lastCheck = System.currentTimeMillis();
                updates = 0;
            }
        }
    }

    /**
     * Metodă apelată când fereastra jocului pierde focusul.
     * Dacă starea curentă este PLAYING, resetează flag-urile de direcție ale jucătorului.
     */
    public void windowFocusLost() {
        if (Gamestate.state == Gamestate.PLAYING)
            playing.getPlayer().resetDirBooleans();
    }

    // Gettere pentru stările de joc, pentru a permite accesul din alte clase (de ex., GamePanel)
    /** @return Instanța stării de meniu. */
    public Menu getMenu() { return menu; }
    /** @return Instanța stării de joc propriu-zis. */
    public Playing getPlaying() { return playing; }
    /** @return Instanța stării de opțiuni. */
    public Options getOptions() { return options; }
    /** @return Instanța stării de încărcare a jocului. */
    public Loadgame getLoadgame() { return loadgame; }
    /** @return Instanța stării de clasament. */
    public Leaderboard getLeaderboard() { return leaderboard; }
    /** @return Instanța overlay-ului de introducere a numelui. */
    public EnterNameOverlay getEnterNameOverlay() { return enterNameOverlay; }
}
