package gamestates;

import database.InsertGet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import main.Game;
import utilz.LoadSave;

/**
 * Reprezintă starea de joc pentru afișarea clasamentului (Leaderboard).
 * Încarcă datele jucătorilor din baza de date, le sortează după scor și le afișează
 * într-o listă derulabilă.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class Leaderboard extends State implements Statemethods{
    private BufferedImage backgroundImg; // Imaginea de fundal specifică clasamentului (cadrul)
    private int menuX, menuY, menuWidth, menuHeight; // Coordonate și dimensiuni pentru cadrul clasamentului
    private BufferedImage startbgImg; // Imaginea de fundal generală (aceeași ca la EnterName)
    private int startbgX, startbgY, startbgWidth, startbgHeight; // Coordonate și dimensiuni pentru fundalul general
    private Rectangle mainMenuButtonBounds; // Limitele butonului de întoarcere la meniul principal
    private BufferedImage playerBoxImg; // Imaginea pentru fiecare intrare (cutie) din clasament
    private Font airstrikeFont; // Fontul personalizat pentru text
    private BufferedImage knobImg; // Imaginea pentru "butonul" barei de derulare
    private BufferedImage grooverImg; // Imaginea pentru "șanțul" barei de derulare
    /** Offset-ul curent de derulare verticală a listei. */
    private int scrollOffset = 0;
    /** Spațiul vertical (în pixeli) între fiecare intrare din clasament. */
    private int boxSpacing = 110;
    /** Înălțimea zonei vizibile a listei derulabile. */
    private int visibleAreaHeight = boxSpacing * 5; // Afișează 5 intrări complet vizibile
    /** Un buffer grafic pentru a desena conținutul înainte de a-l afișa pe ecran (pentru performanță/evitarea pâlpâirii). */
    private BufferedImage buffer;
    /** Flag care indică dacă este necesară redesenarea conținutului buffer-ului. */
    private boolean needsRedraw = true;
    private Rectangle backButtonBounds; // Limitele butonului "X" de închidere/înapoi
    private Gamestate previousState = null; // Starea anterioară a jocului
    private List<PlayerData> leaderboardData = new ArrayList<>(); // Lista cu datele jucătorilor pentru clasament
    /** Calea către fișierul bazei de date. */
    private static final String DB_FILE = "data/gamedatabase.db";
    private SimpleDateFormat dateFormat; // Formatator pentru dată (neutilizat momentan în codul afișat)

    /**
     * Clasă internă statică pentru a stoca datele unui jucător afișate în clasament.
     */
    private static class PlayerData {
        String username;
        int level;
        int score;
        int health;
        int coconuts;

        /**
         * Constructor pentru PlayerData.
         * @param username Numele jucătorului.
         * @param level Nivelul atins.
         * @param score Scorul obținut.
         * @param health Viața rămasă (poate fi relevantă pentru sortare sau afișare).
         * @param coconuts Numărul de nuci de cocos.
         */
        public PlayerData(String username, int level, int score, int health, int coconuts) {
            this.username = username;
            this.level = level;
            this.score = score;
            this.health = health;
            this.coconuts = coconuts;
        }
    }

    /**
     * Constructor pentru starea Leaderboard.
     * Inițializează elementele grafice, încarcă fonturile și datele din clasament.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public Leaderboard(Game game) {
        super(game);
        loadStartImg();
        loadBackground();
        mainMenuButtonBounds = new Rectangle(1230, 817, 380, 95); // Coordonate specifice imaginii
        backButtonBounds = new Rectangle(1419, 185, 63, 63); // Coordonate specifice imaginii
        playerBoxImg = LoadSave.getSpriteAtlas("player_box.png");
        knobImg = LoadSave.getSpriteAtlas("knob.png");
        grooverImg = LoadSave.getSpriteAtlas("groover.png");
        loadCustomFont();
        buffer = new BufferedImage(Game.GAME_WIDTH, Game.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        dateFormat = new SimpleDateFormat("MM-dd HH:mm"); // Inițializat, dar nefolosit în draw()
        refreshLeaderboardData(); // Încarcă datele la inițializare
    }

    /**
     * Reîmprospătează datele clasamentului prin încărcarea informațiilor
     * despre toți jucătorii din baza de date și sortarea lor după scor.
     */
    private void refreshLeaderboardData() {
        // Obținem lista de jucători din baza de date
        List<String> players = InsertGet.getPlayerList();
        leaderboardData.clear();
        
        for (String player : players) {
            try {
                // Ne asigurăm că tabelul există înainte de a încerca să citim din el
                String levelTableName = player + "_level1"; // Creăm tabelul pentru primul nivel
                InsertGet.ensurePlayerTableExists(DB_FILE, levelTableName);
                
                int level = InsertGet.LoadLevelIndex(DB_FILE, player);
                int score = InsertGet.LoadScore(DB_FILE, player);
                int health = InsertGet.LoadCurrentHealth(DB_FILE, player);
                int coconuts = InsertGet.LoadCoconutNumber(DB_FILE, player);
                
                leaderboardData.add(new PlayerData(player, level, score, health, coconuts));
                System.out.println("Date încărcate pentru " + player + ": Level=" + level + 
                                 ", Score=" + score + ", Health=" + health + 
                                 ", Coconuts=" + coconuts);
            } catch (Exception e) {
                System.out.println("Eroare la încărcarea datelor pentru " + player + ": " + e.getMessage());
                // Adăugăm jucătorul cu valori implicite în caz de eroare
                leaderboardData.add(new PlayerData(player, 1, 0, 100, 0));
            }
        }
        
        // Sortăm lista după scor
        leaderboardData.sort((a, b) -> Integer.compare(b.score, a.score));
        needsRedraw = true; // Marchează pentru redesenare
    }

    /**
     * Încarcă fontul personalizat folosit pentru textul din clasament.
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
     * Încarcă și configurează imaginea specifică a cadrului clasamentului.
     */
    private void loadBackground() {
        backgroundImg = LoadSave.getSpriteAtlas("leaderboard_frame.png");
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = Game.GAME_HEIGHT / 2 - menuHeight / 2;
    }

    /**
     * Actualizează starea clasamentului. Momentan, nu necesită actualizări logice per frame,
     * deoarece redesenarea este gestionată de flag-ul {@code needsRedraw}.
     */
    @Override
    public void update() {
        // Doar redesenează la nevoie, nu necesită actualizare logică per frame
    }

    /**
     * Desenează clasamentul pe ecran.
     * Utilizează un buffer pentru a desena elementele doar atunci când este necesar ({@code needsRedraw}).
     * Afișează intrările jucătorilor, clasamentul, scorul și alte detalii,
     * precum și o bară de derulare.
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

            // Draw player boxes
            int boxX = 412, boxY = 316;
            g2d.setClip(boxX, boxY, playerBoxImg.getWidth(), visibleAreaHeight);
            
            // Calculate which boxes should be visible
            int startBox = scrollOffset / boxSpacing;
            int endBox = Math.min(leaderboardData.size(), startBox + (visibleAreaHeight / boxSpacing) + 1);
            
            for (int i = startBox; i < endBox; i++) {
                int y = boxY + i * boxSpacing - scrollOffset;
                PlayerData player = leaderboardData.get(i);
                
                g2d.drawImage(playerBoxImg, boxX, y, null);

                // Rank
                g2d.setFont(airstrikeFont.deriveFont(75f));
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(i + 1), boxX + 50, y + 90);

                // Name
                g2d.setFont(airstrikeFont.deriveFont(36f));
                g2d.setColor(Color.WHITE);
                g2d.drawString(player.username, boxX + 130, y + 65);

                // Score and Level
                g2d.setFont(airstrikeFont.deriveFont(28f));
                g2d.drawString("Score: " + player.score, boxX + 130, y + 95);
                g2d.drawString("Level: " + player.level, boxX + 300, y + 95);

                // Health and Coconuts
                g2d.setFont(airstrikeFont.deriveFont(20f));
                g2d.drawString("Health: " + player.health + "%", boxX + 470, y + 65);
                g2d.drawString("Coconuts: " + player.coconuts, boxX + 470, y + 85);
            }
            g2d.setClip(null);

            // Draw the scroll bar (groover and knob)
            int scrollX = boxX + playerBoxImg.getWidth() + 40;
            int scrollY = boxY;
            int scrollHeight = visibleAreaHeight;
            int maxScroll = Math.max(0, (leaderboardData.size() * boxSpacing) - visibleAreaHeight);
            if (maxScroll < 1) maxScroll = 1;
            int knobTravel = scrollHeight - knobImg.getHeight();
            int knobY = scrollY + (int)((float)scrollOffset / maxScroll * knobTravel);
            if (knobY < scrollY) knobY = scrollY;
            if (knobY > scrollY + knobTravel) knobY = scrollY + knobTravel;
            int knobX = scrollX - (knobImg.getWidth() - grooverImg.getWidth()) / 2;
            g2d.drawImage(grooverImg, scrollX, scrollY, grooverImg.getWidth(), scrollHeight, null);
            g2d.drawImage(knobImg, knobX, knobY, null);
            
            g2d.dispose();
            needsRedraw = false;
        }
        
        // Draw the buffer to the screen
        g.drawImage(buffer, 0, 0, null); // Desenează buffer-ul pe ecranul principal
    }

    /**
     * Gestionează evenimentele de rotiță a mouse-ului pentru derularea listei clasamentului.
     *
     * @param e Evenimentul {@link MouseWheelEvent}.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        int maxScroll = Math.max(0, (leaderboardData.size() * boxSpacing) - visibleAreaHeight);
        scrollOffset += e.getWheelRotation() * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        needsRedraw = true; // Marchează pentru redesenare după derulare
    }

    /**
     * Gestionarea evenimentului de click al mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Verifică dacă s-a apăsat pe butonul de întoarcere la meniul principal sau pe butonul "X".
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY()); // Pentru depanare
        if (mainMenuButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul Main Menu apăsat!");
            Gamestate.state = Gamestate.MENU;
            needsRedraw = true;
        }
        if (backButtonBounds.contains(e.getPoint())) {
            System.out.println("Butonul Back (X) apăsat!");
            if (previousState != null) {
                System.out.println("Revenire la starea anterioară: " + previousState);
                Gamestate.state = previousState;
            } else {
                System.out.println("Nicio stare anterioară, revenire la meniu.");
                Gamestate.state = Gamestate.MENU;
            }
            needsRedraw = true;
        }
    }

    /**
     * Gestionarea evenimentului de eliberare a butonului mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Gestionarea evenimentului de mișcare a mouse-ului (neutilizat, butoanele nu au efect de hover aici).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Gestionarea evenimentului de tragere a mouse-ului (neutilizat).
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
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
     * Desenează hitbox-ul butonului "Înapoi" (X).
     * Suprascrie metoda din {@link State} pentru a folosi {@code backButtonBounds} specific acestei clase.
     * @param g2d Contextul grafic 2D.
     */
    @Override // Suprascrie metoda din State
    protected void drawBackButton(Graphics2D g2d) {
        if (showDebugHitbox && backButtonBounds != null) { // showDebugHitbox este moștenit
            g2d.setColor(Color.RED);
            g2d.draw(backButtonBounds);
        }
    }

    /**
     * Setează starea anterioară a jocului, pentru a permite revenirea la aceasta.
     * @param state Starea anterioară {@link Gamestate}.
     */
    public void setPreviousState(Gamestate state) {
        this.previousState = state;
    }
}
