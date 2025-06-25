package gamestates;

import database.InsertGet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import main.Game;
import ui.LevelButton;
import ui.MenuButton;
import utilz.LoadSave;

/**
 * Reprezintă starea de joc pentru ecranul de încărcare a unui joc salvat.
 * Permite jucătorului să selecteze un nivel salvat pentru a continua jocul.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class Loadgame extends State implements Statemethods {
    private MenuButton[] buttons = new MenuButton[5]; // Array pentru butoane de meniu (momentan doar unul folosit)
    private LevelButton[] levelButton= new LevelButton[3]; // Array pentru butoanele de selecție a nivelului
    private BufferedImage backgroundImg; // Imaginea cadrului specific ecranului de încărcare
    private int menuX, menuY, menuWidth, menuHeight; // Coordonate și dimensiuni pentru cadrul de încărcare
    private BufferedImage startbgImg; // Imaginea de fundal generală
    private int startbgX, startbgY, startbgWidth, startbgHeight; // Coordonate și dimensiuni pentru fundalul general
    private Rectangle backButtonBounds; // Limitele butonului "X" de închidere/înapoi
    private Gamestate previousState = null; // Starea anterioară a jocului
    // private boolean needsRedraw = true; // Flag pentru redesenare (neutilizat activ în codul furnizat, desenarea se face mereu)

    /**
     * Constructor pentru starea Loadgame.
     * Inițializează butoanele, imaginile de fundal și alte elemente UI.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public Loadgame(Game game) {
        super(game);
        loadLevelButtons();
        loadButtons();
        loadStartImg();
        loadBackground();
        backButtonBounds = new Rectangle(1339, 360, 33, 33); // Coordonate specifice pentru butonul X din imaginea cadrului
    }

    /**
     * Încarcă și configurează imaginea de fundal generală.
     */
    private void loadStartImg() {
        startbgImg = LoadSave.getSpriteAtlas(LoadSave.START_BACKGROUND);
        startbgWidth = (int) (startbgImg.getWidth() * Game.SCALE);
        startbgHeight = (int) (startbgImg.getHeight() * Game.SCALE);
        startbgX = Game.GAME_WIDTH / 2 - startbgWidth / 2;
        startbgY = (int) (0 * Game.SCALE);
    }

    /**
     * Încarcă și configurează imaginea specifică a cadrului pentru ecranul de încărcare.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.FRAME_LOADGAME);
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = Game.GAME_HEIGHT / 2 - menuHeight / 2;
    }

    /**
     * Inițializează și poziționează butoanele de selecție a nivelului.
     */
    private void loadLevelButtons() {
        levelButton[0] = new LevelButton(Game.GAME_WIDTH / 2 - (int)(200 * Game.SCALE), Game.GAME_HEIGHT / 2 - 100, 0, Gamestate.PLAYING); // Nivel 1
        levelButton[1] = new LevelButton(Game.GAME_WIDTH / 2, Game.GAME_HEIGHT / 2 - 100, 1, Gamestate.PLAYING); // Nivel 2
        levelButton[2] = new LevelButton(Game.GAME_WIDTH / 2 + (int)(200 * Game.SCALE), Game.GAME_HEIGHT / 2 - 100, 2, Gamestate.PLAYING); // Nivel 3
    }

    /**
     * Inițializează butoanele de meniu (de ex., un buton "Load" general, dacă ar fi necesar).
     * Momentan, pare să inițializeze un buton care duce la starea PLAYING, posibil pentru un slot de salvare general.
     */
    private void loadButtons() {
        // Acest buton ar putea fi pentru un slot de salvare rapidă sau un concept diferit.
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2,Game.GAME_HEIGHT / 2 + 100, 0, Gamestate.PLAYING); 
    }

    /**
     * Actualizează starea butoanelor din ecranul de încărcare.
     */
    @Override
    public void update() {
        for (MenuButton mb : buttons)
            if (mb != null)
                mb.update();
        for (LevelButton lb : levelButton)
            if (lb != null)
                lb.update();
    }

    /**
     * Desenează elementele ecranului de încărcare a jocului.
     * Include fundalul, cadrul și butoanele.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(startbgImg, startbgX, startbgY, startbgWidth, startbgHeight, null);
        g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);
        for (MenuButton mb : buttons)
            if (mb != null)
                mb.draw(g);
        for (LevelButton lb : levelButton)
            if (lb != null)
                lb.draw(g);
        // Opțional: desenează hitbox-ul butonului "X" pentru depanare
        // if (showDebugHitbox && g instanceof Graphics2D) {
        //     drawBackButton((Graphics2D) g);
        // }
    }

    /**
     * Gestionarea evenimentului de click al mouse-ului (neutilizat).
     * Acțiunile sunt gestionate în {@link #mousePressed(MouseEvent)} și {@link #mouseReleased(MouseEvent)}.
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // Neutilizat
    }

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Setează starea de apăsare pentru butoanele interactive și gestionează butonul "X".
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY()); // Pentru depanare
        for (MenuButton mb : buttons) {
            if (mb != null && isIn(e, mb)) {
                mb.setMousePressed(true);
            }
        }
        for (LevelButton lb : levelButton) {
            if (lb != null && isIn(e, lb)) {
                lb.setMousePressed(true);
            }
        }
        if (backButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul Back (X) apăsat!");
            if (previousState != null) {
                Gamestate.state = previousState;
            } else {
                Gamestate.state = Gamestate.MENU; // Implicit, revine la meniu
            }
            // needsRedraw = true; // Acest flag nu pare a fi folosit activ pentru redesenare
        }
    }

    /**
     * Gestionază evenimentele de eliberare a butonului mouse-ului.
     * Aplică acțiunea corespunzătoare butonului apăsat (încărcare nivel sau altă acțiune de meniu).
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        for (MenuButton mb : buttons) {
            if (mb != null && isIn(e, mb)) {
                if (mb.isMousePressed()) {
                    mb.applyGamestate();
                    if (Gamestate.state == Gamestate.PLAYING) {
                        // Încarcă ultimul nivel salvat
                        String username = game.getPlaying().getUsername();
                        int currentLevel = game.getPlaying().getLevelManager().getCurrentLevelNumber();
                        loadLevel(currentLevel, username);
                    }
                }
                break;
            }
        }
        for (LevelButton lb : levelButton) {
            if (lb != null && isIn(e, lb)) {
                if (lb.isMousePressed()) {
                    int levelNumber = lb.getRowIndex() + 1;
                    String username = game.getPlaying().getUsername();
                    loadLevel(levelNumber, username);
                }
                break;
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for (MenuButton mb : buttons)
            if (mb != null)
                mb.resetBools();
        for (LevelButton lb : levelButton)
            if (lb != null)
                lb.resetBools();
    }

    /**
     * Gestionază evenimentele de mișcare a mouse-ului.
     * Setează starea "mouse over" pentru butoanele interactive.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        for (MenuButton mb : buttons)
            if (mb != null)
                mb.setMouseOver(false);
        for (LevelButton lb : levelButton)
            if (lb != null)
                lb.setMouseOver(false);

        for (MenuButton mb : buttons)
            if (mb != null)
                if (isIn(e, mb)) {
                    mb.setMouseOver(true);
                    break; 
                }
        for (LevelButton lb : levelButton)
            if (lb != null)
                if (isIn(e, lb)) {
                    lb.setMouseOver(true);
                    break; 
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
     * Gestionază evenimentele de apăsare a tastelor.
     * Tasta Enter poate fi folosită pentru a confirma o acțiune (de ex., încărcarea unui slot implicit).
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // Acțiunea pentru Enter ar trebui să fie consistentă cu un buton principal,
            // de ex., încărcarea primului slot sau a ultimului joc salvat.
            // Momentan, pare să reseteze jocul și să rămână în starea LOADGAME, ceea ce poate nu e intenționat.
            // Gamestate.state = Gamestate.LOADGAME; // Probabil nu se dorește rămânerea aici
            // game.getPlaying().resetAll(); // Resetarea jocului aici poate fi prematură
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // Neutilizat
    }

    /**
     * Desenează hitbox-ul butonului "Înapoi" (X).
     * Suprascrie metoda din {@link State}.
     * @param g2d Contextul grafic 2D.
     */
    @Override // Suprascrie metoda din State
    protected void drawBackButton(Graphics2D g2d) {
        if (showDebugHitbox && backButtonBounds != null) { // showDebugHitbox este moștenit
            g2d.setColor(Color.RED);
            g2d.drawRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height);
            g2d.setColor(Color.WHITE); // Adaugă text pentru vizibilitate mai bună în debug
            g2d.drawString("X", backButtonBounds.x + backButtonBounds.width / 3, backButtonBounds.y + backButtonBounds.height * 2 / 3);
        }
    }

    /**
     * Setează starea anterioară a jocului, pentru a permite revenirea la aceasta.
     * @param state Starea anterioară {@link Gamestate}.
     */
    public void setPreviousState(Gamestate state) {
        this.previousState = state;
    }

    /**
     * Încarcă un nivel specific pentru un utilizator dat.
     * Preia datele salvate din baza de date și configurează starea jocului (Playing).
     *
     * @param levelNumber Numărul nivelului de încărcat.
     * @param username Numele utilizatorului pentru care se încarcă nivelul.
     */
    private void loadLevel(int levelNumber, String username) {
        String levelTableName = username + "_level" + levelNumber;
        System.out.println("Încerc să încarc nivelul " + levelNumber + " din tabela: " + levelTableName);
        
        // Verifică dacă există salvare pentru acest nivel
        int health = InsertGet.LoadCurrentHealth("data/gamedatabase.db", levelTableName);
        System.out.println("Health găsit în baza de date: " + health);
        
        if (health > 0) {
            // Încarcă progresul din baza de date pentru acest nivel
            int score = InsertGet.LoadScore("data/gamedatabase.db", levelTableName);
            int coconuts = InsertGet.LoadCoconutNumber("data/gamedatabase.db", levelTableName);
            float posX = InsertGet.LoadXPosition("data/gamedatabase.db", levelTableName);
            float posY = InsertGet.LoadYPosition("data/gamedatabase.db", levelTableName);
            int timer = InsertGet.LoadTimer("data/gamedatabase.db", levelTableName);
            
            System.out.println("Date găsite în baza de date pentru " + levelTableName + ":");
            System.out.println("Scor: " + score);
            System.out.println("Coconuts: " + coconuts);
            System.out.println("Position: (" + posX + ", " + posY + ")");
            System.out.println("Timer: " + timer);
            
            // Resetează starea jocului
            game.getPlaying().resetAll(true);
            
            // Încarcă nivelul
            game.getPlaying().getLevelManager().loadLevel(levelNumber);
            
            // Reîncarcă datele nivelului pentru player și inamici
            game.getPlaying().getPlayer().loadLevelData(game.getPlaying().getLevelManager().getCurrentLevel().getLevelData());
            game.getPlaying().getEnemyManager().resetEnemies();
            game.getPlaying().getEnemyManager().loadEnemiesFromLevelData(
                game.getPlaying().getLevelManager().getCurrentLevel().getLevelData(),
                game.getPlaying().getLevelManager().getCurrentLevel()
            );
            
            // Setează valorile în Playing/Player
            game.getPlaying().getPlayer().setCurrentHealth(health);
            game.getPlaying().getPlayer().setPosition(posX, posY);
            game.getPlaying().setCurrentScore(score);
            game.getPlaying().setCurrentCoconuts(coconuts);
            game.getPlaying().setTimer(timer);
            
            System.out.println("Nivel " + levelNumber + " încărcat cu succes pentru " + username);
            
            Gamestate.state = Gamestate.PLAYING;
        } else {
            System.out.println("Nu există salvare pentru nivelul " + levelNumber + " în tabela " + levelTableName);
            // Verifică dacă tabela există
            if (InsertGet.checkIfTableExists("data/gamedatabase.db", levelTableName)) {
                System.out.println("Tabela " + levelTableName + " există dar nu conține date valide");
            } else {
                System.out.println("Tabela " + levelTableName + " nu există în baza de date");
            }
        }
    }
}
