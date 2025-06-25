package gamestates;

import database.InsertGet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import utilz.LoadSave;

/**
 * Reprezintă overlay-ul afișat la finalizarea cu succes a unui nivel.
 * Permite jucătorului să treacă la nivelul următor sau să se întoarcă la meniul principal.
 * Afișează scorul obținut pentru nivelul finalizat.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class LevelFinishedOverlay extends State implements Statemethods {
    private BufferedImage backgroundImg; // Imaginea de fundal pentru ecranul de finalizare a nivelului
    private int bgX, bgY, bgWidth, bgHeight; // Coordonate și dimensiuni pentru fundal
    private Rectangle nextLevelButtonBounds; // Limitele butonului pentru nivelul următor
    private Rectangle menuButtonBounds; // Limitele butonului de întoarcere la meniu (butonul "X")
    private Font overlayFont; // Fontul folosit pentru textul din overlay
    private Playing playing; // Referință la starea de joc 'Playing'
    /** Calea către fișierul bazei de date pentru salvarea/încărcarea scorului. */
    private static final String DB_FILE = "data/gamedatabase.db";

    /**
     * Constructor pentru LevelFinishedOverlay.
     *
     * @param playing Referință la starea de joc {@link Playing} din care s-a ajuns aici.
     */
    public LevelFinishedOverlay(Playing playing) {
        super(playing.getGame());
        this.playing = playing;
        loadBackground();
        loadButtons();
        loadCustomFont();
    }

    /**
     * Încarcă și configurează imaginea de fundal pentru ecranul de finalizare a nivelului.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas("level_finished.png");
        bgWidth = (int) (backgroundImg.getWidth() * playing.getGame().SCALE);
        bgHeight = (int) (backgroundImg.getHeight() * playing.getGame().SCALE);
        bgX = playing.getGame().GAME_WIDTH / 2 - bgWidth / 2;
        bgY = playing.getGame().GAME_HEIGHT / 2 - bgHeight / 2;
    }

    /**
     * Inițializează limitele butoanelor pentru "Nivelul Următor" și "Meniu" (butonul X).
     * Coordonatele sunt specifice imaginii de fundal "level_finished.png".
     */
    private void loadButtons() {
        nextLevelButtonBounds = new Rectangle(738,745,367,81); // Coordonate pentru butonul "Next Level"
        menuButtonBounds = new Rectangle(1162, 232, 68, 63);    // Coordonate pentru butonul "X" (Meniu)
    }

    /**
     * Încarcă fontul personalizat folosit pentru afișarea textului în overlay.
     */
    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/font/airstrikebold.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            overlayFont = baseFont.deriveFont(Font.BOLD, 48f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);
        } catch (Exception e) {
            e.printStackTrace();
            overlayFont = new Font("Arial", Font.BOLD, 48);
        }
    }

    /**
     * Actualizează starea overlay-ului. Momentan, nu face nimic deoarece overlay-ul este static.
     */
    @Override
    public void update() {
        // Nu este necesară nicio actualizare, overlay-ul este static
    }

    /**
     * Desenează elementele overlay-ului de finalizare a nivelului.
     * Include imaginea de fundal și scorul jucătorului.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, bgX, bgY, bgWidth, bgHeight, null);

        // Obținem scorul din baza de date
        int score = InsertGet.LoadScore(DB_FILE, playing.getUsername());
        String scoreText = "Score: " + score;
        g.setFont(overlayFont.deriveFont(48f));
        g.setColor(Color.YELLOW);

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);
        int textX = bgX + (bgWidth - textWidth) / 2;
        int textY = bgY + bgHeight / 2 + fm.getAscent() / 2;

        g.drawString(scoreText, textX, textY);
    }

    /**
     * Gestionarea evenimentului de click al mouse-ului (neutilizat).
     * Acțiunile sunt gestionate în {@link #mousePressed(MouseEvent)}.
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // Neutilizat
    }

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Verifică dacă s-a apăsat pe butonul pentru nivelul următor sau pe cel de întoarcere la meniu.
     * Salvează progresul jucătorului înainte de a trece la nivelul următor.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (nextLevelButtonBounds.contains(e.getPoint())) {
            // Salvează progresul nivelului curent înainte de a avansa
            InsertGet.SaveIntoDatabase(
                DB_FILE,
                playing.getUsername(),
                playing.getLevelManager().getCurrentLevelNumber(), // Nivelul tocmai finalizat
                playing.getCurrentScore(), // Scorul obținut pentru acest nivel
                playing.getPlayer().getCurrentHealth(), // Viața la sfârșitul nivelului
                playing.getCurrentCoconuts(), // Nucile de cocos la sfârșitul nivelului
                playing.getPlayer().getHitbox().x, // Poziția X la sfârșitul nivelului
                playing.getPlayer().getHitbox().y, // Poziția Y la sfârșitul nivelului
                playing.getElapsedSeconds() // Timpul scurs pentru acest nivel
            );
            
            playing.advanceToNextLevel(); // Metoda din Playing gestionează trecerea la nivelul următor
                                          // și ar trebui să reseteze starea jucătorului și a overlay-ului.
        }
        if (menuButtonBounds.contains(e.getPoint())) {
            Gamestate.state = Gamestate.MENU;
            playing.setLevelFinished(false); // Asigură-te că overlay-ul este ascuns la revenirea în meniu
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a butonului mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {}

    /**
     * Gestionarea evenimentului de mișcare a mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {}

    /**
     * Gestionarea evenimentului de tragere a mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseDragged(MouseEvent e) {}

    /**
     * Gestionază evenimentele de apăsare a tastelor.
     * Permite trecerea la starea de joc (Enter) sau la meniu (Escape).
     * Notă: Trecerea directă la PLAYING cu Enter poate sări peste logica de avansare la nivelul următor.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // Similar cu click pe "Next Level", dar fără salvare explicită aici,
            // presupunând că advanceToNextLevel se ocupă de tot.
            // Ar fi mai consistent ca Enter să simuleze click pe butonul Next Level.
            playing.advanceToNextLevel();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
            playing.setLevelFinished(false);
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {}
}
