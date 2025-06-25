package gamestates;

import database.InsertGet;
import entities.EnemyManager;
import entities.Player;
import entities.Gem;
import entities.Banana;
import entities.Coconut;
import entities.Projectile;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import levels.LevelManager;
import main.Game;
import utilz.LoadSave;
import utilz.Enemy_Animation_Rows;

/**
 * Reprezintă starea principală de joc (gameplay-ul propriu-zis).
 * Gestionează jucătorul, nivelurile, inamicii, interfața utilizator (HUD),
 * coliziunile, colectabilele și logica generală a jocului în desfășurare.
 * Extinde {@link State} și implementează {@link Statemethods}.
 */
public class Playing extends State implements Statemethods {
    private Player player; // Instanța jucătorului
    private LevelManager levelManager; // Managerul pentru niveluri
    private EnemyManager enemyManager; // Managerul pentru inamici
    private boolean paused = false; // Flag pentru starea de pauză a jocului
    private String playerName = ""; // Numele jucătorului (poate fi setat din meniu/overlay)
    private String username = ""; // Numele de utilizator curent al sesiunii

    /** Offset-ul orizontal al nivelului pentru scrolling. */
    private int xLvlOffset = 0;
    /** Limita stângă a ecranului pentru inițierea scrolling-ului. */
    private int leftBorder = (int) (0.2 * Game.GAME_WIDTH);
    /** Limita dreaptă a ecranului pentru inițierea scrolling-ului. */
    private int rightBorder = (int) (0.8 * Game.GAME_WIDTH);
    /** Lățimea totală a nivelului curent, în tile-uri. */
    private int lvlTilesWide;
    /** Offset-ul maxim posibil al tile-urilor pentru scrolling. */
    private int maxTilesOffset;
    /** Offset-ul maxim posibil al nivelului în pixeli pentru scrolling. */
    private int maxLvlOffsetX;
    
    private BufferedImage gameUI; // Imaginea pentru interfața utilizator (HUD)
    private BufferedImage coconutIcon; // Iconița pentru nuci de cocos în HUD
    private BufferedImage crystalIcon; // Iconița pentru cristale/gem-uri în HUD (dacă este cazul)

    /** Timpul de start al nivelului curent, în milisecunde. */
    private long levelStartTime; 
    /** Numărul de secunde scurs de la începerea nivelului. */
    private int elapsedSeconds;  

    private Font airstrikeFont; // Fontul personalizat pentru HUD

    /** Indică dacă cronometrul nivelului a pornit. */
    private boolean timerStarted = false;

    private Rectangle backButtonBounds; // Limitele butonului "Înapoi" din HUD (dacă există)

    private Gamestate previousState = null; // Starea anterioară a jocului

    // Flag-uri și obiecte pentru overlay-urile de final de nivel și game over
    // private boolean isLevelFinished = false; // Neutilizat direct, se folosește levelFinished
    private LevelFinishedOverlay levelFinishedOverlay;
    /** Indică dacă nivelul curent a fost finalizat. */
    private boolean levelFinished = false;
    /** Indică dacă jocul s-a terminat (Game Over). */
    private boolean gameOver = false;
    private GameOverOverlay gameOverOverlay;

    // private boolean showDebugHitbox = false; // Moștenit din State

    /** Nivelul curent la care se joacă (index). */
    private int currentLevel = 1; // Inițializat, dar actualizat de LevelManager
    /** Scorul curent al jucătorului în nivel. */
    private int currentScore = 0;
    /** Numărul curent de nuci de cocos deținute de jucător. */
    public int currentCoconuts = 0; // Public pentru acces din alte clase (de ex., Player pentru colectare)

    /**
     * Constructor pentru starea Playing.
     * Inițializează managerii, jucătorul și încarcă datele inițiale.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public Playing(Game game) {
        super(game);
        initClasses();
        username = database.InsertGet.LoadUsername("data/gamedatabase.db"); // Încarcă ultimul username
    }

    /**
     * Inițializează clasele principale necesare pentru starea de joc:
     * LevelManager, Player, EnemyManager. Încarcă datele nivelului și inamicii.
     * Configurează variabilele legate de dimensiunea nivelului și scrolling.
     * Încarcă elementele UI și fonturile.
     */
    private void initClasses() {
        levelManager = new LevelManager(game);
        player = new Player(100 * (int)Game.SCALE, 770 * (int)Game.SCALE, (int) (272 * Game.SCALE), (int) (183 * Game.SCALE));
        enemyManager = new EnemyManager(this);
        
        player.loadLevelData(levelManager.getCurrentLevel().getLevelData());
        enemyManager.loadEnemiesFromLevelData(levelManager.getCurrentLevel().getLevelData(), levelManager.getCurrentLevel());

        if (levelManager.getCurrentLevelNumber() >= 2) { 
            player.setJumpSlamUnlocked(true);
        } else {
            player.setJumpSlamUnlocked(false);
        }
        
        lvlTilesWide = levelManager.getCurrentLevel().getLevelData()[0].length;
        maxTilesOffset = Math.max(0, lvlTilesWide - Game.TILES_IN_WIDTH);
        maxLvlOffsetX = maxTilesOffset * Game.TILES_SIZE;
        
        System.out.println("Level width in tiles: " + lvlTilesWide);
        System.out.println("Max tiles offset: " + maxTilesOffset);
        System.out.println("Max level offset X: " + maxLvlOffsetX);

        gameUI = LoadSave.getSpriteAtlas(LoadSave.GAME_UI);
        loadCustomFont();
        coconutIcon = LoadSave.COCONUT_IMAGE; 
        crystalIcon = LoadSave.getSpriteAtlas("crystal.png");
        backButtonBounds = new Rectangle(1750, 23, 110, 105); // Coordonate pentru butonul de back din HUD
    }

    /**
     * Actualizează logica stării de joc.
     * Include actualizarea nivelului, jucătorului, inamicilor, colectabilelor,
     * verificarea coliziunilor și a condițiilor de final de joc.
     * Rulează doar dacă jocul nu este în pauză.
     */
    @Override
    public void update() {
        if (!paused) {
            levelManager.update();
            player.update();
            checkPlayerAttackHits();
            enemyManager.update(player.getHitbox());
            updateBananas(); 
            updateCoconuts(); 
            checkGemCollision(); 
            checkBananaCollision(); 
            checkCoconutCollision(); 
            checkPlayerThrow(); 
            checkCloseToBorder(); // Pentru scrolling
            if (timerStarted) {
                elapsedSeconds = (int)((System.currentTimeMillis() - levelStartTime) / 1000);
            }
            if (player.getCurrentHealth() <= 0 && !gameOver) {
                showGameOverOverlay();
            }
        }
    }
    
    /**
     * Verifică dacă atacurile jucătorului lovesc inamici și aplică daune.
     * Asigură că un atac lovește o singură dată.
     */
    private void checkPlayerAttackHits() {
        if (player.hasHit()) { 
            return;
        }

        Rectangle2D.Float activeAttackBox = null;
        String attackType = "";

        if (player.isPunching()) {
            activeAttackBox = player.getAttackHitbox(); 
            attackType = "Punch";
        } else if (player.isWhacking()) {
            activeAttackBox = player.getWhackHitbox(); 
            attackType = "Whack";
        } else if (player.isJumpSlamming()) { 
            activeAttackBox = player.getJumpSlamHitbox();
            attackType = "JumpSlam";
        }

        if (activeAttackBox == null) {
            return; 
        }

        int damageDealt = player.getAttackDamage(); 

        // Verifică coliziunea cu fiecare tip de inamic
        for (entities.Nanite nanite : enemyManager.getNanites()) {
            if (nanite.isActive() && activeAttackBox.intersects(nanite.getHitbox())) {
                nanite.takeDamage(damageDealt);
                player.setHasHit(true);
                System.out.println("Player " + attackType + " hit nanite! Damage: " + damageDealt);
                return; 
            }
        }
        for (entities.Karagor karagor : enemyManager.getKaragors()) {
            if (karagor.isAlive() && activeAttackBox.intersects(karagor.getHitbox())) {
                karagor.takeDamage(damageDealt);
                player.setHasHit(true);
                System.out.println("Player " + attackType + " hit karagor! Damage: " + damageDealt + 
                                 ", Karagor health: " + karagor.getCurrentHealth());
                return; 
            }
        }
        for (entities.Goblin goblin : enemyManager.getGoblins()) {
            if (goblin.isActive() && activeAttackBox.intersects(goblin.getHitbox())) {
                goblin.takeDamage(damageDealt);
                player.setHasHit(true);
                System.out.println("Player " + attackType + " hit goblin! Damage: " + damageDealt);
                return; 
            }
        }
        if (enemyManager.getGoblinBosses() != null) { 
            for (entities.GoblinBoss boss : enemyManager.getGoblinBosses()) {
                if (boss.isAlive() && activeAttackBox.intersects(boss.getHitbox())) {
                    boss.takeDamage(damageDealt);
                    player.setHasHit(true);
                    System.out.println("Player " + attackType + " hit Goblin Boss! Damage: " + damageDealt);
                    return; 
                }
            }
        }
        if (enemyManager.getGolemBosses() != null) { 
            for (entities.GolemBoss boss : enemyManager.getGolemBosses()) {
                if (boss.isAlive() && activeAttackBox.intersects(boss.getHitbox())) {
                    boss.takeDamage(damageDealt);
                    player.setHasHit(true);
                    System.out.println("Player " + attackType + " hit Golem Boss! Damage: " + damageDealt);
                    return; 
                }
            }
        }
    }

    /**
     * Verifică coliziunea jucătorului cu gem-urile (pietre prețioase).
     * La coliziune, activează overlay-ul de final de nivel.
     */
    private void checkGemCollision() {
        Rectangle2D.Float playerHitbox = player.getHitbox();
        for (Gem gem : enemyManager.getGems()) {
            if (gem.isActive() && playerHitbox.intersects(gem.getHitbox())) {
                gem.setActive(false); 
                System.out.println("Player touched a gem! Showing LevelFinishedOverlay.");
                showLevelFinishedOverlay(); 
                break; 
            }
        }
    }

    /** Actualizează starea bananelor active din nivel. */
    private void updateBananas() {
        if (levelManager.getCurrentLevel() != null && levelManager.getCurrentLevel().getBananas() != null) {
            ArrayList<Banana> bananas = levelManager.getCurrentLevel().getBananas();
            for (Banana banana : bananas) {
                if (banana.isActive()) {
                    banana.update();
                }
            }
        }
    }

    /** Verifică coliziunea jucătorului cu bananele și aplică efectul. */
    private void checkBananaCollision() {
        if (levelManager.getCurrentLevel() == null || levelManager.getCurrentLevel().getBananas() == null) {
            return;
        }
        Rectangle2D.Float playerHitbox = player.getHitbox();
        for (Banana banana : levelManager.getCurrentLevel().getBananas()) {
            if (banana.isActive() && playerHitbox.intersects(banana.getHitbox())) {
                player.collectBananaEffect();
                banana.setActive(false); 
                System.out.println("Player collected a banana!");
                break; 
            }
        }
    }
    
    /** Actualizează starea nucilor de cocos active din nivel. */
    private void updateCoconuts() {
        if (levelManager.getCurrentLevel() != null && levelManager.getCurrentLevel().getCoconuts() != null) {
            ArrayList<Coconut> coconuts = levelManager.getCurrentLevel().getCoconuts();
            for (Coconut coconut : coconuts) {
                if (coconut.isActive()) {
                    coconut.update();
                }
            }
        }
    }

    /** Verifică coliziunea jucătorului cu nucile de cocos și actualizează contorul. */
    private void checkCoconutCollision() {
        if (levelManager.getCurrentLevel() == null || levelManager.getCurrentLevel().getCoconuts() == null) {
            return;
        }
        Rectangle2D.Float playerHitbox = player.getHitbox();
        for (Coconut coconut : levelManager.getCurrentLevel().getCoconuts()) {
            if (coconut.isActive() && playerHitbox.intersects(coconut.getHitbox())) {
                player.collectCoconutEffect(); 
                coconut.setActive(false); 
                this.currentCoconuts++; 
                System.out.println("Player collected a coconut! Total coconuts: " + this.currentCoconuts);
                break; 
            }
        }
    }

    /** Verifică dacă jucătorul a inițiat o aruncare și generează un proiectil (nucă de cocos). */
    private void checkPlayerThrow() {
        if (player.canSpawnProjectileAndConsume()) { 
            float projectileX = player.getHitbox().x;
            float projectileY = player.getHitbox().y + (player.getHitbox().height / 3); 
            int projectileDir;

            if (player.isFacingRight()) {
                projectileX += player.getHitbox().width; 
                projectileDir = Enemy_Animation_Rows.Directions.RIGHT;
            } else {
                float projectileWidth = (LoadSave.COCONUT_THROWABLE_IMAGE != null) ? (LoadSave.COCONUT_THROWABLE_IMAGE.getWidth() * Game.SCALE) : (16 * Game.SCALE);
                projectileX -= projectileWidth; 
                projectileDir = Enemy_Animation_Rows.Directions.LEFT;
            }

            Projectile coconutProjectile = new Projectile(
                projectileX, projectileY, projectileDir, 
                LoadSave.COCONUT_THROWABLE_IMAGE, player.getAttackDamage()
            );
            enemyManager.addProjectile(coconutProjectile);
            currentCoconuts--; 
            System.out.println("Player threw a coconut! Coconuts left: " + currentCoconuts);
        }
    }

    /**
     * Avansează la următorul nivel. Salvează progresul nivelului curent,
     * încarcă datele pentru noul nivel și resetează starea jucătorului și a jocului.
     */
    public void advanceToNextLevel() {
        int completedLevel = levelManager.getCurrentLevelNumber();
        
        try {
            InsertGet.SaveIntoDatabase(
                "data/gamedatabase.db", username, completedLevel,
                currentScore, player.getCurrentHealth(), currentCoconuts,
                player.getHitbox().x, player.getHitbox().y, elapsedSeconds
            );
            System.out.println("Progres salvat pentru nivelul " + completedLevel);
        } catch (Exception ex) {
            System.out.println("Eroare la salvarea progresului: " + ex.getMessage());
        }
        
        if (levelManager.nextLevel()) { // Verifică dacă există un nivel următor și îl încarcă
            player.loadLevelData(levelManager.getCurrentLevel().getLevelData());
            enemyManager.loadEnemiesFromLevelData(levelManager.getCurrentLevel().getLevelData(), levelManager.getCurrentLevel());

            if (levelManager.getCurrentLevelNumber() >= 2) { 
                player.setJumpSlamUnlocked(true);
            } else {
                player.setJumpSlamUnlocked(false);
            }
            System.out.println("Jump Slam Unlocked: " + (levelManager.getCurrentLevelNumber() >= 2));
        
            player.resetToStartPosition();
            currentScore = 0;
            // currentCoconuts nu se resetează, se păstrează între niveluri
            elapsedSeconds = 0;
            timerStarted = false;
            levelStartTime = System.currentTimeMillis();
            
            // Salvează starea inițială a noului nivel (cu scor 0, etc.)
            try {
                InsertGet.SaveIntoDatabase(
                    "data/gamedatabase.db", username, levelManager.getCurrentLevelNumber(),
                    0, player.getCurrentHealth(), currentCoconuts, // Păstrează viața și nucile
                    player.getHitbox().x, player.getHitbox().y, 0
                );
                System.out.println("Stare inițială salvată pentru nivelul " + levelManager.getCurrentLevelNumber());
            } catch (Exception ex) {
                System.out.println("Eroare la salvarea stării inițiale pentru noul nivel: " + ex.getMessage());
            }
        } else {
            // Nu mai sunt niveluri, poate afișează un ecran de final de joc sau revine la meniu
            System.out.println("Felicitări! Ai terminat toate nivelurile!");
            Gamestate.state = Gamestate.MENU; // Sau un ecran de "Game Completed"
        }
        setLevelFinished(false); // Ascunde overlay-ul de final de nivel
    }

    /**
     * Verifică dacă jucătorul este aproape de marginile ecranului și ajustează
     * offset-ul de scrolling al nivelului ({@code xLvlOffset}).
     */
    private void checkCloseToBorder() {
        int playerCenterX = (int) (player.getHitbox().x + player.getHitbox().width / 2);
        int newX = playerCenterX - Game.GAME_WIDTH / 2; // Calculează noul offset dorit
        // Limitează offset-ul la dimensiunile nivelului
        if (newX > maxLvlOffsetX) {
            newX = maxLvlOffsetX;
        }
        if (newX < 0) {
            newX = 0;
        }
        xLvlOffset = newX;
    }

    /**
     * Desenează toate elementele stării de joc, inclusiv nivelul, inamicii, jucătorul și HUD-ul.
     * De asemenea, desenează overlay-urile de final de nivel sau game over, dacă sunt active.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void draw(Graphics g) {
        levelManager.draw(g, xLvlOffset);
        enemyManager.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        drawBananas(g, xLvlOffset); 
        drawCoconuts(g, xLvlOffset); 

        g.drawImage(gameUI, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        drawHUD(g);

        if (levelFinished && levelFinishedOverlay != null) {
            g.setColor(new Color(0, 0, 0, 120)); // Fundal semi-transparent
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            levelFinishedOverlay.draw(g);
        }
        if (gameOver && gameOverOverlay != null) {
            g.setColor(new Color(0, 0, 0, 120)); // Fundal semi-transparent
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            gameOverOverlay.draw(g);
        }
    }

    /** Desenează bananele active din nivel. */
    private void drawBananas(Graphics g, int xLvlOffset) {
        if (levelManager.getCurrentLevel() != null && levelManager.getCurrentLevel().getBananas() != null) {
            ArrayList<Banana> bananas = levelManager.getCurrentLevel().getBananas();
            for (Banana banana : bananas) {
                if (banana.isActive()) {
                    banana.draw(g, xLvlOffset);
                }
            }
        }
    }

    /** Desenează nucile de cocos active din nivel. */
    private void drawCoconuts(Graphics g, int xLvlOffset) {
        if (levelManager.getCurrentLevel() != null && levelManager.getCurrentLevel().getCoconuts() != null) {
            ArrayList<Coconut> coconuts = levelManager.getCurrentLevel().getCoconuts();
            for (Coconut coconut : coconuts) {
                if (coconut.isActive()) {
                    coconut.draw(g, xLvlOffset);
                }
            }
        }
    }

    /**
     * Gestionează evenimentele de click al mouse-ului.
     * Inițiază cronometrul nivelului la primul click.
     * Activează atacurile jucătorului (pumn sau whack) la click stânga/dreapta.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!timerStarted) { // Pornește cronometrul la primul click în starea de joc
            levelStartTime = System.currentTimeMillis();
            timerStarted = true;
        }
        if (!gameOver && !levelFinished) { // Permite atacuri doar dacă jocul este activ
            if (e.getButton() == MouseEvent.BUTTON1) { 
                player.setAttack(true); 
            } else if (e.getButton() == MouseEvent.BUTTON3) { 
                player.setWhackAttack(true); 
            }
        }
    }

    /**
     * Gestionează evenimentele de apăsare a tastelor.
     * Controlează mișcarea jucătorului, săritura, ghemuirea, atacurile speciale și pauza.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (levelFinished && levelFinishedOverlay != null) {
            levelFinishedOverlay.keyPressed(e); // Deleagă la overlay dacă este activ
            return;
        }
        if (gameOver && gameOverOverlay != null) {
            gameOverOverlay.keyPressed(e); // Deleagă la overlay dacă este activ
            return;
        }
        if (!timerStarted) { // Pornește cronometrul la prima apăsare de tastă
            levelStartTime = System.currentTimeMillis();
            timerStarted = true;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: player.setLeft(true); break;
            case KeyEvent.VK_D: player.setRight(true); break;
            case KeyEvent.VK_SPACE: player.setJump(true); break;
            case KeyEvent.VK_CONTROL: player.setCrouch(true); break;
            case KeyEvent.VK_Q: player.setJumpSlamAttack(true); break;
            case KeyEvent.VK_R: player.activateCrystalRush(); break;
            case KeyEvent.VK_E: 
                if (currentCoconuts > 0 && !player.isThrowing() && !player.isPunching()) { 
                    player.setThrowAttack(true);
                } else if (currentCoconuts <= 0) {
                    System.out.println("No coconuts to throw!");
                }
                break;
            case KeyEvent.VK_BACK_SPACE: Gamestate.state = Gamestate.MENU; break; // Revine la meniu
            case KeyEvent.VK_ESCAPE:
                paused = !paused; // Comută starea de pauză
                if (paused) { // Salvează progresul la intrarea în pauză
                    try {
                        // currentScore este actualizat cu elapsedSeconds la pauză
                        InsertGet.SaveIntoDatabase(
                            "data/gamedatabase.db", username,
                            levelManager.getCurrentLevelNumber(), elapsedSeconds, // Salvează elapsedSeconds ca scor
                            player.getCurrentHealth(), currentCoconuts,
                            player.getHitbox().x, player.getHitbox().y, elapsedSeconds
                        );
                        System.out.println("Progres salvat pentru user: " + username + 
                                         " - Scor(Timp): " + elapsedSeconds + 
                                         ", Viață: " + player.getCurrentHealth() + 
                                         ", Nuci: " + currentCoconuts);
                    } catch (Exception ex) {
                        System.out.println("Eroare la salvarea progresului: " + ex.getMessage());
                    }
                }
                break;
        }
    }

    /**
     * Gestionează evenimentele de eliberare a tastelor.
     * Oprește mișcarea jucătorului sau ghemuirea.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: player.setLeft(false); break;
            case KeyEvent.VK_D: player.setRight(false); break;
            case KeyEvent.VK_SPACE: player.setJump(false); break;
            case KeyEvent.VK_CONTROL: player.setCrouch(false); break;
        }
    }

    /**
     * Gestionază evenimentele de apăsare a butonului mouse-ului.
     * Specific pentru butonul "Înapoi" din HUD, dacă este cazul.
     * Salvează progresul la apăsarea butonului "Înapoi".
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (levelFinished && levelFinishedOverlay != null) {
            levelFinishedOverlay.mousePressed(e); // Deleagă la overlay
            return;
        }
        if (gameOver && gameOverOverlay != null) {
            gameOverOverlay.mousePressed(e); // Deleagă la overlay
            return;
        }
        if (backButtonBounds.contains(e.getPoint())) { // Butonul "Înapoi" din HUD
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Username nu este setat!");
                Gamestate.state = Gamestate.ENTER_NAME; // Revine la introducerea numelui
                return;
            }
            timerStarted = false; // Oprește cronometrul
            paused = true; // Pune jocul în pauză
            try {
                InsertGet.SaveIntoDatabase( // Salvează progresul curent
                    "data/gamedatabase.db", username.trim(),
                    levelManager.getCurrentLevelNumber(), currentScore, // Salvează scorul curent
                    player.getCurrentHealth(), currentCoconuts,
                    player.getHitbox().x, player.getHitbox().y, elapsedSeconds
                );
                System.out.println("Progres salvat pentru user: " + username);
            } catch (Exception ex) {
                System.out.println("Eroare la salvarea progresului: " + ex.getMessage());
            }
            System.out.println("Back button pressed!");
            if (previousState != null) {
                Gamestate.state = previousState;
            } else {
                Gamestate.state = Gamestate.ENTER_NAME; // Implicit, revine la EnterName
            }
        }
    }

    /** Gestionarea evenimentului de eliberare a butonului mouse-ului (neutilizat aici). */
    @Override
    public void mouseReleased(MouseEvent e) {}
    /** Gestionarea evenimentului de mișcare a mouse-ului (neutilizat aici). */
    @Override
    public void mouseMoved(MouseEvent e) {}
    /** Gestionarea evenimentului de tragere a mouse-ului (neutilizat aici). */
    @Override
    public void mouseDragged(MouseEvent e) {}

    /** Apelată când fereastra jocului pierde focusul, pentru a reseta input-urile jucătorului. */
    public void windowFocusLost() {
        player.resetDirBooleans();
    }

    /** @return Instanța jucătorului. */
    public Player getPlayer() { return player; }
    /** @return Managerul de inamici. */
    public EnemyManager getEnemyManager() { return enemyManager; }

    /** Resetează starea jocului (parțial, păstrând progresul). */
    public void resetAll() { resetAll(false); }
    /**
     * Resetează starea jocului.
     * @param fullReset True pentru o resetare completă (la reîncercare nivel), 
     *                  false pentru o resetare parțială (păstrând scorul, etc. la încărcare).
     */
    public void resetAll(boolean fullReset) {
        if (fullReset) {
            paused = false;
            player.resetHealth();
            player.resetToStartPosition();
            enemyManager.resetEnemies();
            currentScore = 0;
            currentCoconuts = 0;
            elapsedSeconds = 0;
            timerStarted = false;
            levelStartTime = System.currentTimeMillis();
            System.out.println("Resetare completă joc: Viață, Scor, Nuci, Cronometru, Poziție resetate la reîncercare.");
        } else { // Păstrează progresul la încărcarea unui joc salvat
            int savedHealth = player.getCurrentHealth(); // Ar trebui încărcate din DB, nu din starea curentă
            int savedScore = currentScore;
            int savedCoconuts = currentCoconuts;
            String savedUsername = username;
            int savedElapsedSeconds = elapsedSeconds; 
            
            paused = false;
            player.resetHealth(); // Setează la maxHealth
            enemyManager.resetEnemies(); // Regenerează inamicii
            
            // Aplică valorile salvate (acestea ar trebui să vină din DB la încărcarea jocului)
            player.setCurrentHealth(savedHealth);
            // Poziția jucătorului ar trebui setată la cea salvată
            this.currentScore = savedScore;
            this.currentCoconuts = savedCoconuts;
            this.username = savedUsername;
            this.elapsedSeconds = savedElapsedSeconds; 
            if (savedElapsedSeconds == 0) {
                timerStarted = false;
                levelStartTime = System.currentTimeMillis();
            } else {
                timerStarted = true;
                levelStartTime = System.currentTimeMillis() - (savedElapsedSeconds * 1000L); // Folosește L pentru long
            }
            System.out.println("Joc resetat cu valorile salvate - Viață: " + savedHealth + 
                             ", Scor: " + savedScore + 
                             ", Nuci: " + savedCoconuts +
                             ", Timp Scurs: " + savedElapsedSeconds);
        }
    }

    /** Setează numele jucătorului (informativ, username este folosit pentru salvare/încărcare). */
    public void setPlayerName(String name) { this.playerName = name; }
    /** @return Numele informativ al jucătorului. */
    public String getPlayerName() { return playerName; }

    /** Desenează elementele HUD (Head-Up Display). */
    private void drawHUD(Graphics g) {
        int playerHealthPercent = (int)((player.getCurrentHealth() / (float)player.getMaxHealth()) * 100);
        g.setColor(Color.RED);
        g.setFont(airstrikeFont.deriveFont(70f));
        g.drawString(playerHealthPercent + "%", 1378, 985);

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(currentCoconuts), 1615, 990);

        g.drawImage(coconutIcon, 1697, 957, 43, 29, null);
        g.drawImage(crystalIcon, 1792, 957, 39, 31, null);

        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        g.setColor(Color.WHITE);
        g.setFont(airstrikeFont.deriveFont(72f));
        g.drawString(timeFormatted, 1440, 100);

        if (g instanceof Graphics2D) drawBackButton((Graphics2D) g);
    }

    /** Încarcă fontul personalizat. */
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

    /** Desenează hitbox-ul butonului "Înapoi" din HUD (dacă showDebugHitbox este true). */
    @Override // Suprascrie metoda din State
    protected void drawBackButton(Graphics2D g2d) {
        if (showDebugHitbox && backButtonBounds != null) {
            g2d.setColor(Color.RED);
            g2d.drawRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height);
        }
    }
    
    /** Setează starea anterioară a jocului. */
    public void setPreviousState(Gamestate state) { this.previousState = state; }
    /** Setează numele de utilizator curent al sesiunii. */
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username invalid! Folosim numele implicit 'player'");
            this.username = "player";
        } else {
            this.username = username.trim();
        }
        InsertGet.SaveUsername("data/gamedatabase.db", this.username); // Salvează noul username ca ultimul folosit
        // Reîncarcă datele pentru noul username (sau creează o intrare nouă dacă nu există)
        try {
            currentLevel = InsertGet.LoadLevelIndex("data/gamedatabase.db", this.username);
            if (currentLevel <= 0) currentLevel = 1; // Default la nivelul 1 dacă nu există salvare

            currentScore = InsertGet.LoadScore("data/gamedatabase.db", this.username + "_level" + currentLevel);
            currentCoconuts = InsertGet.LoadCoconutNumber("data/gamedatabase.db", this.username + "_level" + currentLevel);
            int loadedHealth = InsertGet.LoadCurrentHealth("data/gamedatabase.db", this.username + "_level" + currentLevel);
            if (loadedHealth <=0 && currentLevel > 0) loadedHealth = player.getMaxHealth(); // Setează la max health dacă e 0 din DB
            player.setCurrentHealth(loadedHealth);
            
            // Setează poziția jucătorului la cea salvată sau la start default
            float posX = InsertGet.LoadXPosition("data/gamedatabase.db", this.username + "_level" + currentLevel);
            float posY = InsertGet.LoadYPosition("data/gamedatabase.db", this.username + "_level" + currentLevel);
            if (posX != 0 || posY != 0) player.setPosition(posX, posY); // Setează poziția dacă e validă
            else player.resetToStartPosition();

            setTimer(InsertGet.LoadTimer("data/gamedatabase.db", this.username + "_level" + currentLevel));

            System.out.println("Date încărcate pentru user: " + this.username +
                " | Level: " + currentLevel +
                " | Scor: " + currentScore +
                " | Health: " + loadedHealth +
                " | Coconuts: " + currentCoconuts);
        } catch (Exception ex) {
            System.out.println("Eroare la încărcarea datelor pentru noul username: " + ex.getMessage());
            // Setează valori implicite dacă încărcarea eșuează
            currentLevel = 1;
            currentScore = 0;
            currentCoconuts = 0;
            player.resetHealth();
            player.resetToStartPosition();
            setTimer(0);
        }
    }

    /** Afișează overlay-ul de final de nivel. */
    public void showLevelFinishedOverlay() {
        levelFinished = true;
        levelFinishedOverlay = new LevelFinishedOverlay(this);
    }

    /** Setează starea de finalizare a nivelului. */
    public void setLevelFinished(boolean finished) {
        this.levelFinished = finished;
        if (!finished) this.levelFinishedOverlay = null; // Elimină overlay-ul dacă nu mai este finalizat
    }

    /** @return Numele de utilizator curent. */
    public String getUsername() { return username; }
    /** @return Managerul de niveluri. */
    public LevelManager getLevelManager() { return levelManager; }

    /** Afișează overlay-ul de Game Over și salvează progresul. */
    public void showGameOverOverlay() {
        try {
            InsertGet.SaveIntoDatabase(
                "data/gamedatabase.db", username,
                levelManager.getCurrentLevelNumber(), currentScore, // Salvează scorul curent
                player.getCurrentHealth(), currentCoconuts,
                player.getHitbox().x, player.getHitbox().y, elapsedSeconds
            );
            System.out.println("Progres salvat la Game Over pentru user: " + username);
        } catch (Exception ex) {
            System.out.println("Eroare la salvarea progresului la Game Over: " + ex.getMessage());
        }
        timerStarted = false; // Oprește cronometrul
        gameOver = true;
        gameOverOverlay = new GameOverOverlay(this);
    }

    /** Setează starea de Game Over. */
    public void setGameOver(boolean over) {
        this.gameOver = over;
        if (!over) this.gameOverOverlay = null; // Elimină overlay-ul dacă nu mai este game over
    }

    /** Setează scorul curent. @param score Noul scor. */
    public void setCurrentScore(int score) { this.currentScore = score; }
    /** Setează numărul curent de nuci de cocos. @param coconuts Noul număr de nuci. */
    public void setCurrentCoconuts(int coconuts) { this.currentCoconuts = coconuts; }

    /** Setează valoarea cronometrului (timpul scurs). @param timer Timpul în secunde. */
    public void setTimer(int timer) {
        this.elapsedSeconds = timer;
        if (timer > 0) {
            this.timerStarted = true;
            this.levelStartTime = System.currentTimeMillis() - (timer * 1000L);
        } else {
            this.timerStarted = false;
        }
    }
    /** @return Timpul scurs în secunde pentru nivelul curent. */
    public int getElapsedSeconds() { return elapsedSeconds; }
    /** @return Scorul curent. */
    public int getCurrentScore() { return currentScore; }
    /** @return Numărul curent de nuci de cocos. */
    public int getCurrentCoconuts() { return currentCoconuts; }
}
