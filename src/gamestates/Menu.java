package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

/**
 * Reprezintă starea de joc pentru meniul principal.
 * Afișează opțiuni precum "Start Joc", "Încarcă Joc", "Clasament", "Opțiuni" și "Ieșire".
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class Menu extends State implements Statemethods {

    /** Numele jucătorului, poate fi afișat în meniu. */
    private String playerName = ""; 
    private MenuButton[] buttons = new MenuButton[5]; // Array pentru butoanele din meniu
    private BufferedImage backgroundImg; // Imaginea specifică a meniului (cadrul cu butoane)
    private int menuX, menuY, menuWidth, menuHeight; // Coordonate și dimensiuni pentru cadrul meniului
    private BufferedImage startbgImg; // Imaginea de fundal generală
    private int startbgX, startbgY, startbgWidth, startbgHeight; // Coordonate și dimensiuni pentru fundalul general
    private Rectangle backButtonBounds; // Limitele butonului "X" de închidere/înapoi (poate fi pentru revenire la EnterName)
    private Gamestate previousState = null; // Starea anterioară a jocului
    // private boolean needsRedraw = true; // Flag pentru redesenare (neutilizat activ în codul furnizat)

    /**
     * Constructor pentru starea Menu.
     * Inițializează butoanele și imaginile de fundal.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public Menu(Game game) {
        super(game);
        loadButtons();
        loadStartImg();
        loadBackground();
        backButtonBounds = new Rectangle(1180, 150, 63, 63); // Coordonate specifice pentru butonul X din imaginea meniului
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
     * Încarcă și configurează imaginea specifică a cadrului meniului.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.MENU_BACKGROUND);
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * Game.SCALE);
    }

    /**
     * Inițializează și poziționează butoanele din meniul principal.
     */
    private void loadButtons() {
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (275 * Game.SCALE), 0, Gamestate.PLAYING);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (375 * Game.SCALE), 1, Gamestate.LOADGAME);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (475 * Game.SCALE), 2, Gamestate.LEADERBOARD);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 2, (int) (575 * Game.SCALE), 3, Gamestate.OPTIONS); // Buton Opțiuni
        buttons[4] = new MenuButton(Game.GAME_WIDTH / 2, (int) (675 * Game.SCALE), 4, Gamestate.QUIT);    // Buton Ieșire
    }

    /**
     * Actualizează starea butoanelor din meniu.
     */
    @Override
    public void update() {
        for (MenuButton mb : buttons)
            mb.update();
    }

    /**
     * Desenează elementele meniului principal.
     * Include fundalul, cadrul și butoanele.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(startbgImg, startbgX, startbgY, startbgWidth, startbgHeight, null);
        g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);
        for (MenuButton mb : buttons)
            mb.draw(g);
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
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                mb.setMousePressed(true);
            }
        }
       if (backButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul Back (X) apăsat în Meniu!");
            if (previousState != null) { // De obicei, previousState pentru Meniu ar fi null sau EnterName
                Gamestate.state = previousState;
            } else {
                // Comportament implicit dacă nu există o stare anterioară specifică (de ex., revine la EnterName sau închide jocul)
                Gamestate.state = Gamestate.ENTER_NAME; // Sau Gamestate.QUIT dacă este cazul
            }
            // needsRedraw = true; // Acest flag nu pare a fi folosit activ
        }
    }

    /**
     * Gestionază evenimentele de eliberare a butonului mouse-ului.
     * Aplică starea de joc corespunzătoare butonului apăsat.
     * Setează starea anterioară pentru ecranele de Opțiuni și Încarcă Joc.
     * Resetează jocul dacă se intră în starea PLAYING.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                if (mb.isMousePressed()) {
                    if (mb.getState() == Gamestate.OPTIONS) {
                        game.getOptions().setPreviousState(Gamestate.MENU);
                    }
                    if (mb.getState() == Gamestate.LOADGAME) {
                        game.getLoadgame().setPreviousState(Gamestate.state);
                    }
                    if (mb.getState() == Gamestate.PLAYING) {
                        game.getPlaying().setPreviousState(Gamestate.MENU);
                        game.getPlaying().setUsername(game.getSessionUsername());
                    }
                    mb.applyGamestate();
                    if (Gamestate.state == Gamestate.PLAYING) {
                        game.getPlaying().resetAll(true);
                    }
                }
                break;
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for (MenuButton mb : buttons)
            mb.resetBools();
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
            mb.setMouseOver(false);

        for (MenuButton mb : buttons)
            if (isIn(e, mb)) {
                mb.setMouseOver(true);
                break; // Doar un buton poate fi "over" la un moment dat
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
     * Tasta Enter poate fi folosită pentru a începe un joc nou.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Gamestate.state = Gamestate.PLAYING; // Schimbă starea la joc
            game.getPlaying().resetAll(true); // Resetează complet starea jocului pentru un nou început
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
        }
    }

    /**
     * Setează numele jucătorului.
     * @param name Numele jucătorului.
     */
    public void setPlayerName(String name) {
        this.playerName = name;
        System.out.println("Numele jucătorului setat la: " + name);
    }

    /**
     * Returnează numele jucătorului.
     * @return Numele jucătorului.
     */
    public String getPlayerName() {
        return playerName;
    }
}
