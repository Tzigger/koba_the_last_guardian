package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import main.Game;
import utilz.LoadSave;

/**
 * Reprezintă starea de joc pentru meniul de opțiuni.
 * Permite jucătorului să ajusteze setări precum volumul muzicii și al efectelor sonore.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class Options extends State implements Statemethods {
    private BufferedImage backgroundImg; // Imaginea specifică a meniului de opțiuni (cadrul)
    private int menuX, menuY, menuWidth, menuHeight; // Coordonate și dimensiuni pentru cadrul meniului
    private BufferedImage startbgImg; // Imaginea de fundal generală
    private int startbgX, startbgY, startbgWidth, startbgHeight; // Coordonate și dimensiuni pentru fundalul general
    private Rectangle mainMenuButtonBounds; // Limitele butonului de întoarcere la meniul principal (butonul "X")
    /** Un buffer grafic pentru a desena conținutul înainte de a-l afișa pe ecran. */
    private BufferedImage buffer;
    /** Flag care indică dacă este necesară redesenarea conținutului buffer-ului. */
    private boolean needsRedraw = true;
    private Gamestate previousState; // Starea anterioară a jocului, pentru a reveni
    
    // Componente pentru sliderele de volum
    private BufferedImage knobImg; // Imaginea pentru "butonul" slider-ului
    private BufferedImage grooverImg; // Imaginea pentru "șanțul" slider-ului
    /** Valoarea curentă a volumului muzicii (0-100). */
    private int musicValue = 50;
    /** Valoarea curentă a volumului efectelor sonore (0-100). */
    private int sfxValue = 50;
    /** Limitele dreptunghiulare pentru slider-ul de muzică. */
    private Rectangle musicSliderBounds;
    /** Limitele dreptunghiulare pentru slider-ul de efecte sonore. */
    private Rectangle sfxSliderBounds;
    /** Indică dacă utilizatorul trage de slider-ul de muzică. */
    private boolean musicSliderDragging = false;
    /** Indică dacă utilizatorul trage de slider-ul de efecte sonore. */
    private boolean sfxSliderDragging = false;
    private Font airstrikeFont; // Fontul personalizat pentru text

    /**
     * Constructor pentru starea Options.
     * Inițializează elementele UI, inclusiv sliderele de volum.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public Options(Game game) {
        super(game);
        loadStartImg();
        loadBackground();
        loadSliderImages();
        loadCustomFont();
        mainMenuButtonBounds = new Rectangle(1161, 233, 63, 63); // Coordonate specifice pentru butonul X din imaginea de opțiuni
        buffer = new BufferedImage(Game.GAME_WIDTH, Game.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        // Inițializează limitele slider-elor (coordonate și dimensiuni specifice designului UI)
        musicSliderBounds = new Rectangle(730, 465, 400, 20); // Exemplu de poziționare
        sfxSliderBounds = new Rectangle(730, 625, 400, 20);   // Exemplu de poziționare
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
     * Încarcă și configurează imaginea specifică a cadrului pentru meniul de opțiuni.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas("options_frame.png");
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = Game.GAME_HEIGHT / 2 - menuHeight / 2;
    }

    /**
     * Încarcă imaginile pentru componentele slider-elor (buton și șanț).
     */
    private void loadSliderImages() {
        knobImg = LoadSave.getSpriteAtlas("knob.png");
        grooverImg = LoadSave.getSpriteAtlas("groover.png");
    }

    /**
     * Încarcă fontul personalizat folosit pentru textul din meniul de opțiuni.
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

    /**
     * Actualizează starea meniului de opțiuni. Momentan, nu necesită actualizări logice per frame,
     * deoarece redesenarea este gestionată de flag-ul {@code needsRedraw} și interacțiunile cu mouse-ul.
     */
    @Override
    public void update() {
        // Nu este necesară actualizare logică per frame dacă interacțiunile sunt bazate pe evenimente
    }

    /**
     * Desenează elementele meniului de opțiuni.
     * Utilizează un buffer pentru a desena elementele doar atunci când este necesar ({@code needsRedraw}).
     * Include fundalul, cadrul, sliderele de volum și valorile acestora.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        if (needsRedraw) {
            Graphics2D g2d = buffer.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear the buffer
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            
            g2d.drawImage(startbgImg, startbgX, startbgY, startbgWidth, startbgHeight, null);
            g2d.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

            // Draw slider labels
            g2d.setFont(airstrikeFont);
            g2d.setColor(Color.WHITE);

            // Draw music slider
            g2d.drawImage(grooverImg, musicSliderBounds.x, musicSliderBounds.y, musicSliderBounds.width, musicSliderBounds.height, null);
            int musicKnobX = musicSliderBounds.x + (int)((musicValue / 100.0) * (musicSliderBounds.width - knobImg.getWidth()));
            g2d.drawImage(knobImg, musicKnobX, musicSliderBounds.y - 30, null);

            // Draw SFX slider
            g2d.drawImage(grooverImg, sfxSliderBounds.x, sfxSliderBounds.y, sfxSliderBounds.width, sfxSliderBounds.height, null);
            int sfxKnobX = sfxSliderBounds.x + (int)((sfxValue / 100.0) * (sfxSliderBounds.width - knobImg.getWidth()));
            g2d.drawImage(knobImg, sfxKnobX, sfxSliderBounds.y - 30, null);

            // Draw values
            g2d.drawString(musicValue + "%", musicSliderBounds.x + musicSliderBounds.width + 20, musicSliderBounds.y + 15);
            g2d.drawString(sfxValue + "%", sfxSliderBounds.x + sfxSliderBounds.width + 20, sfxSliderBounds.y + 15);

            g2d.dispose();
            needsRedraw = false;
        }
        
        // Draw the buffer to the screen
        g.drawImage(buffer, 0, 0, null); // Desenează buffer-ul pe ecranul principal
    }

    /**
     * Gestionarea evenimentului de click al mouse-ului (neutilizat).
     * Acțiunile sunt gestionate în {@link #mousePressed(MouseEvent)} și {@link #mouseDragged(MouseEvent)}.
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Verifică dacă s-a apăsat pe un slider de volum sau pe butonul de întoarcere.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        
        // Verifică dacă s-a făcut click pe slider-ul de muzică
        if (musicSliderBounds.contains(p)) { // Simplificat verificarea limitelor
            musicSliderDragging = true;
            updateMusicValue(p.x);
        }
        
        // Verifică dacă s-a făcut click pe slider-ul de SFX
        if (sfxSliderBounds.contains(p)) { // Simplificat verificarea limitelor
            sfxSliderDragging = true;
            updateSfxValue(p.x);
        }

        // Verifică butonul de întoarcere (X)
        if (mainMenuButtonBounds.contains(p)) {
            if (previousState != null) {
                Gamestate.state = previousState;
            } else {
                Gamestate.state = Gamestate.MENU; // Implicit, revine la meniu
            }
            needsRedraw = true; // Marchează pentru redesenare la revenire
        }
    }

    /**
     * Gestionază evenimentele de eliberare a butonului mouse-ului.
     * Oprește tragerea slider-elor.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        musicSliderDragging = false;
        sfxSliderDragging = false;
    }

    /**
     * Gestionarea evenimentului de mișcare a mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Gestionază evenimentele de tragere a mouse-ului (drag).
     * Actualizează valorile slider-elor dacă sunt trase.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (musicSliderDragging) {
            updateMusicValue(e.getX());
        }
        if (sfxSliderDragging) {
            updateSfxValue(e.getX());
        }
    }

    /**
     * Actualizează valoarea volumului muzicii pe baza poziției x a mouse-ului.
     * @param x Poziția x a mouse-ului.
     */
    private void updateMusicValue(int x) {
        int relativeX = x - musicSliderBounds.x;
        musicValue = Math.min(100, Math.max(0, (int)((relativeX / (float)musicSliderBounds.width) * 100)));
        needsRedraw = true; // Marchează pentru redesenare pentru a reflecta noua valoare
    }

    /**
     * Actualizează valoarea volumului efectelor sonore pe baza poziției x a mouse-ului.
     * @param x Poziția x a mouse-ului.
     */
    private void updateSfxValue(int x) {
        int relativeX = x - sfxSliderBounds.x;
        sfxValue = Math.min(100, Math.max(0, (int)((relativeX / (float)sfxSliderBounds.width) * 100)));
        needsRedraw = true; // Marchează pentru redesenare
    }

    /**
     * Returnează valoarea curentă a volumului muzicii.
     * @return Volumul muzicii (0-100).
     */
    public int getMusicVolume() {
        return musicValue;
    }

    /**
     * Returnează valoarea curentă a volumului efectelor sonore.
     * @return Volumul SFX (0-100).
     */
    public int getSfxVolume() {
        return sfxValue;
    }

    /**
     * Gestionarea evenimentului de apăsare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Gestionarea evenimentului de eliberare a tastei (neutilizat).
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Setează starea anterioară a jocului, pentru a permite revenirea la aceasta.
     * @param state Starea anterioară {@link Gamestate}.
     */
    public void setPreviousState(Gamestate state) {
        this.previousState = state;
    }
}
