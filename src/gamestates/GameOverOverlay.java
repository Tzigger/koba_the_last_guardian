package gamestates;

import database.InsertGet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import utilz.LoadSave;

/**
 * Reprezintă overlay-ul afișat la sfârșitul jocului (Game Over).
 * Permite jucătorului să reîncerce nivelul sau să se întoarcă la meniul principal.
 * Afișează scorul final.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class GameOverOverlay extends State implements Statemethods {
    private BufferedImage backgroundImg; // Imaginea de fundal pentru ecranul Game Over
    private int bgX, bgY, bgWidth, bgHeight; // Coordonate și dimensiuni pentru fundal
    private Rectangle retryButtonBounds; // Limitele butonului de reîncercare
    private Rectangle backButtonBounds; // Limitele butonului de întoarcere la meniu
    private Font overlayFont; // Fontul folosit pentru textul din overlay
    private Playing playing; // Referință la starea de joc 'Playing' pentru a reseta nivelul
    /** Calea către fișierul bazei de date pentru încărcarea scorului. */
    private static final String DB_FILE = "data/gamedatabase.db";
    // protected boolean showDebugHitbox = false; // Moștenit din State, poate fi setat pentru depanare

    /**
     * Constructor pentru GameOverOverlay.
     *
     * @param playing Referință la starea de joc {@link Playing} din care s-a ajuns aici.
     */
    public GameOverOverlay(Playing playing) {
        super(playing.getGame());
        this.playing = playing;
        loadBackground();
        // Coordonatele și dimensiunile butoanelor sunt specifice imaginii de fundal "game_over.png"
        retryButtonBounds = new Rectangle(738,745,367,81); 
        backButtonBounds = new Rectangle(1162, 232, 68, 63); 
        loadCustomFont();
        this.showDebugHitbox = false; // Implicit, nu afișa hitbox-urile butoanelor
    }

    /**
     * Încarcă și configurează imaginea de fundal pentru ecranul Game Over.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas("game_over.png");
        bgWidth = (int) (backgroundImg.getWidth() * playing.getGame().SCALE);
        bgHeight = (int) (backgroundImg.getHeight() * playing.getGame().SCALE);
        bgX = playing.getGame().GAME_WIDTH / 2 - bgWidth / 2;
        bgY = playing.getGame().GAME_HEIGHT / 2 - bgHeight / 2;
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
     * Desenează elementele overlay-ului Game Over.
     * Include imaginea de fundal și scorul jucătorului.
     * Opțional, desenează hitbox-urile butoanelor pentru depanare.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, bgX, bgY, bgWidth, bgHeight, null);
        // Debug: desenează hitbox-urile butoanelor dacă showDebugHitbox este true
        if (showDebugHitbox && g instanceof Graphics2D) {
            drawBackButton((Graphics2D) g); // Metoda drawBackButton este moștenită din State
            drawRetryButton((Graphics2D) g); // Metodă specifică pentru butonul de reîncercare
        }
        // Afișează scorul jucătorului din baza de date
        int score = InsertGet.LoadScore(DB_FILE, playing.getUsername());
        String scoreText = "Scor: " + score;
        g.setFont(overlayFont.deriveFont(48f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);
        int textX = bgX + (bgWidth - textWidth) / 2;
        int textY = 680;
        g.drawString(scoreText, textX, textY);
    }

    /**
     * Desenează hitbox-ul butonului "Înapoi" (suprascris din State pentru a folosi backButtonBounds local).
     * Folosit în scopuri de depanare.
     *
     * @param g2d Contextul grafic 2D {@link Graphics2D}.
     */
    @Override // Suprascrie metoda din State
    protected void drawBackButton(Graphics2D g2d) {
        if (showDebugHitbox && backButtonBounds != null) {
            g2d.setColor(Color.RED);
            g2d.draw(backButtonBounds);
        }
    }

    /**
     * Desenează hitbox-ul butonului "Retry".
     * Folosit în scopuri de depanare.
     *
     * @param g2d Contextul grafic 2D {@link Graphics2D}.
     */
    protected void drawRetryButton(Graphics2D g2d) {
        if (showDebugHitbox && retryButtonBounds != null) {
            g2d.setColor(Color.BLUE);
            g2d.draw(retryButtonBounds);
        }
    }

    /**
     * Gestionarea evenimentului de click al mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {}

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Verifică dacă s-a apăsat pe butonul de reîncercare sau pe cel de întoarcere la meniu.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (retryButtonBounds.contains(e.getPoint())) {
            // Reîncepe nivelul curent
            // Reîncepe nivelul curent
            playing.resetAll(true); // Resetare completă a stării de joc
            playing.setGameOver(false); // Dezactivează overlay-ul Game Over
            // Nu este necesar să reîncarci nivelul aici dacă resetAll se ocupă de asta
            // sau dacă Playing se va actualiza corect.
            // playing.getLevelManager().loadLevel(playing.getLevelManager().getCurrentLevelNumber()); // Poate fi redundant
        }
        if (backButtonBounds != null && backButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul Înapoi (X) apăsat în GameOverOverlay!");
            Gamestate.state = Gamestate.MENU; // Revine la meniul principal
            playing.setGameOver(false); // Asigură-te că overlay-ul este dezactivat
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
     * Permite reîncercarea nivelului (Enter) sau întoarcerea la meniu (Escape).
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            playing.resetAll(true); // Resetare completă
            playing.setGameOver(false);
            // playing.getLevelManager().loadLevel(playing.getLevelManager().getCurrentLevelNumber()); // Poate fi redundant
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
            playing.setGameOver(false); // Asigură-te că overlay-ul este dezactivat
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {}
}
