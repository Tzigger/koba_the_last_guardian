package entities;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import gamestates.Playing;
import levels.Level;
import entities.Coconut; // Added for Coconut spawning
import main.Game;
import utilz.Constants;
import utilz.Gorilla_Animation_rows;
import utilz.LoadSave;
import utilz.Enemy_Animation_Rows; // Added import

/**
 * Gestionează toți inamicii din joc, inclusiv Nanites, Karagors, Goblins, GoblinBosses și GolemBosses.
 * Se ocupă de încărcarea imaginilor inamicilor, actualizarea stării lor, desenarea lor pe ecran,
 * gestionarea coliziunilor cu jucătorul și proiectilele, și spawn-ul inamicilor și al obiectelor colectabile.
 */
public class EnemyManager {

    private Playing playing;
    private BufferedImage[][] naniteImgs; // For Jungla Nanites
    private BufferedImage[][] nanitePesteraImgs; // For Pestera Nanites
    private BufferedImage[][] goblinNoobImgs;
    private BufferedImage[][] goblinHardImgs;
    // private BufferedImage[][] goblinBossImgs; // GoblinBoss loads its own sprites
    // private BufferedImage[][] karagorImgs; // Karagor loads its own sprites
    private ArrayList<Nanite> nanites = new ArrayList<>();
    private ArrayList<Karagor> karagors = new ArrayList<>();
    private ArrayList<Goblin> goblins = new ArrayList<>(); // For Noob and Hard Goblins
    private ArrayList<GoblinBoss> goblinBosses = new ArrayList<>();
    private ArrayList<GolemBoss> golemBosses = new ArrayList<>(); // Added for Golem Boss
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private ArrayList<Gem> gems = new ArrayList<>();
    // Bananas will be added to the Level's banana list, accessed via playing.getLevelManager().getCurrentLevel()
    private Random random = new Random(); // Added for 30% chance
    private int[][] levelData;
    private int currentLevel;
    
    // Spawn point storage
    private ArrayList<Point> spawnPoints = new ArrayList<>();
    private boolean allEnemiesSpawned = false;
    private float playerDetectionDistance = 400; // distance to player to trigger spawn

    /**
     * Constructor pentru EnemyManager.
     * Inițializează managerul cu o referință la starea de joc "Playing",
     * încarcă imaginile inamicilor și setează nivelul curent la 1.
     * @param playing Referință la starea de joc "Playing".
     */
    public EnemyManager(Playing playing){
        this.playing = playing;
        loadEnemyImgs();
        this.currentLevel = 1; // Start with level 1 by default
    }

    /**
     * Inversează (flip) o imagine pe orizontală.
     * @param image Imaginea care trebuie inversată.
     * @return Imaginea inversată.
     */
    static BufferedImage flipImage(BufferedImage image) {
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                flipped.setRGB(image.getWidth() - 1 - x, y, image.getRGB(x, y));
            }
        }
        return flipped;
    }

    /**
     * Încarcă imaginile pentru toți inamicii (Nanites, Goblins).
     * Boss-ii (GoblinBoss, Karagor) își încarcă propriile sprite-uri în clasele lor.
     */
    private void loadEnemyImgs(){
        naniteImgs = loadSpriteSheet(LoadSave.NANITE_JUNGLA, Constants.EnemyConstants.ENEMY_SIZE, Constants.EnemyConstants.ENEMY_SIZE);
        nanitePesteraImgs = loadSpriteSheet(LoadSave.NANITE_PESTERA, Constants.EnemyConstants.ENEMY_SIZE, Constants.EnemyConstants.ENEMY_SIZE);
        
        goblinNoobImgs = loadSpriteSheet(LoadSave.GOBLIN_NOOB_SPRITESHEET, Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_WIDTH_DEFAULT, Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_HEIGHT_DEFAULT);
        goblinHardImgs = loadSpriteSheet(LoadSave.GOBLIN_HARD_SPRITESHEET, Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_WIDTH_DEFAULT, Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_HEIGHT_DEFAULT);

        // GoblinBoss instances load their own sprites, so no loading here for goblinBossImgs.
    }

    /**
     * Încarcă un sprite sheet dintr-o cale specificată și îl împarte în frame-uri individuale.
     * Determină numărul maxim de coloane necesar pe baza definițiilor din {@link Enemy_Animation_Rows}.
     * @param path Calea către fișierul sprite sheet.
     * @param spriteWidth Lățimea unui singur sprite.
     * @param spriteHeight Înălțimea unui singur sprite.
     * @return O matrice bidimensională de imagini (frame-uri de animație).
     *         Returnează o matrice goală dacă încărcarea eșuează pentru a evita NullPointerException.
     */
    private BufferedImage[][] loadSpriteSheet(String path, int spriteWidth, int spriteHeight) {
        BufferedImage img = LoadSave.getSpriteAtlas(path);
        if (img == null) {
            System.err.println("Failed to load sprite sheet: " + path);
            return new BufferedImage[Enemy_Animation_Rows.values().length][0]; // Return empty array to avoid NPE
        }
    
        // Determine max columns needed based on Enemy_Animation_Rows
        int maxCols = 0;
        for (Enemy_Animation_Rows animRow : Enemy_Animation_Rows.values()) {
            if (animRow.getFrameCount() > maxCols) {
                maxCols = animRow.getFrameCount();
            }
        }
        if (maxCols == 0) maxCols = 24; // Default if no frame counts defined, similar to Nanite
    
        BufferedImage[][] sprites = new BufferedImage[Enemy_Animation_Rows.values().length][maxCols];
    
        for (Enemy_Animation_Rows animRow : Enemy_Animation_Rows.values()) {
            int rowIndex = animRow.getRowIndex();
            int frameCount = animRow.getFrameCount();
            if (frameCount == 0) frameCount = maxCols; // Use maxCols if specific frame count is 0
    
            for (int i = 0; i < frameCount; i++) {
                int x = i * spriteWidth;
                int y = rowIndex * spriteHeight;
                if (y + spriteHeight <= img.getHeight() && x + spriteWidth <= img.getWidth()) {
                    sprites[rowIndex][i] = img.getSubimage(x, y, spriteWidth, spriteHeight);
                } else {
                    // Optional: Log if a sprite is out of bounds, or leave null
                    // System.out.println("Sprite out of bounds for " + path + " at row " + rowIndex + ", col " + i);
                }
            }
        }
        return sprites;
    }
    

    /**
     * Actualizează starea tuturor inamicilor activi, proiectilelor și gem-urilor.
     * Gestionează spawn-ul inamicilor pe baza proximității jucătorului (dacă este cazul pentru nivelul curent),
     * coliziunile, aplicarea daunelor și efectele de knockback. De asemenea, gestionează drop-urile de la inamici.
     * @param playerHitbox Hitbox-ul jucătorului, pentru interacțiuni și detectare.
     */
    public void update(Rectangle2D.Float playerHitbox) {
        // Proximity spawning only if not all enemies are set to spawn at once for the current level
        if (!allEnemiesSpawned) {
            // Example: Level 3 might use proximity spawning, while 1 and 2 spawn all at once.
            // This condition needs to be tailored to which levels use proximity vs. immediate spawning.
            // For now, let's assume only levels NOT 1 and NOT 2 use proximity.
            if (currentLevel != 1 && currentLevel != 2 && currentLevel != 3) { // Levels 1, 2, 3 spawn all at once
                 checkForNewEnemySpawn(playerHitbox);
            }
        }
        
        Player player = playing.getPlayer();
        
        // Update all active nanites
        for (int i = nanites.size() - 1; i >= 0; i--) {
            Nanite n = nanites.get(i);
            if (n.isActive()) {
                n.update(playerHitbox); // Nanite's update might set isActive to false
                if (!n.isActive()) { // Check isActive *after* its update
                    trySpawnCollectible(n.getHitbox().x + n.getHitbox().width / 2, n.getHitbox().y + n.getHitbox().height / 2);
                    nanites.remove(i);
                    continue; // Move to the next nanite in the list
                }
                // If still active, check for player collision
                if (n.checkPlayerHit(playerHitbox) && !player.isDamaged()) {
                    if (player.takeDamage(n.getDamage())) {
                        applyKnockback(player, n.getHitbox().x < playerHitbox.x);
                    }
                }
                // Nanite attack logic (if separate from touch) could go here
            } else {
                // This case should ideally not be hit if the above logic is correct,
                // but as a fallback, remove it if it's already inactive.
                nanites.remove(i);
            }
        }
        
        // Update all karagors
        for (int i = 0; i < karagors.size(); i++) {
            Karagor k = karagors.get(i);
            if (k.isAlive()) {
                k.update(playerHitbox);
                if (k.isAttacking()) {
                    Rectangle2D.Float karagorAttackBox = k.getAttackHitbox();
                    if (karagorAttackBox != null && karagorAttackBox.intersects(playerHitbox) && !player.isDamaged()) {
                        if (player.takeDamage(k.getAttackDamage())) {
                            applyKnockback(player, k.getHitbox().x < playerHitbox.x);
                        }
                    }
                }
            } else { // Karagor is not alive
                spawnGem(k.getHitbox().x + k.getHitbox().width / 2, k.getHitbox().y + k.getHitbox().height / 2);
                trySpawnCollectible(k.getHitbox().x + k.getHitbox().width / 2, k.getHitbox().y + k.getHitbox().height / 2);
                karagors.remove(i);
                i--;
            }
        }

        // Update all active Goblins (Noob and Hard)
        for (int i = 0; i < goblins.size(); i++) {
            Goblin gob = goblins.get(i);
            if (gob.isActive()) {
                gob.update(playerHitbox); // Goblin's update might need levelData if it does complex movement
                if (gob.checkPlayerHit(playerHitbox) && !player.isDamaged()) {
                    if (player.takeDamage(gob.getDamage())) {
                        applyKnockback(player, gob.getHitbox().x < playerHitbox.x);
                    }
                }
                // Goblin attack logic (if separate from touch) could go here
            }
        }
        // Explicitly iterate and remove to handle drops
        for (int i = goblins.size() - 1; i >= 0; i--) {
            Goblin gob = goblins.get(i);
            if (!gob.isActive()) { // Assuming isActive becomes false upon death
                trySpawnCollectible(gob.getHitbox().x + gob.getHitbox().width / 2, gob.getHitbox().y + gob.getHitbox().height / 2);
                goblins.remove(i);
            }
        }

        // Update all active GoblinBosses
        for (int i = 0; i < goblinBosses.size(); i++) {
            GoblinBoss gb = goblinBosses.get(i);
            if (gb.isAlive()) {
                gb.update(player, this.levelData);
            } else { // GoblinBoss is not alive
                System.out.println("EnemyManager: GoblinBoss defeated. Checking Crystal Rush unlock. Player Unlocked: " +
                                   playing.getPlayer().isCrystalRushUnlocked() + ", Current Level: " + this.currentLevel);
                if (!playing.getPlayer().isCrystalRushUnlocked() && this.currentLevel == 2) { // Crystal Rush unlocks after defeating Goblin Boss on Level 2
                    playing.getPlayer().unlockCrystalRush();
                }
                spawnGem(gb.getHitbox().x + gb.getHitbox().width / 2, gb.getHitbox().y + gb.getHitbox().height / 2);
                trySpawnCollectible(gb.getHitbox().x + gb.getHitbox().width / 2, gb.getHitbox().y + gb.getHitbox().height / 2);
                goblinBosses.remove(i);
                i--;
            }
        }
        
        // Update all active GolemBosses
        for (int i = 0; i < golemBosses.size(); i++) {
            GolemBoss glb = golemBosses.get(i);
            if (glb.isAlive()) {
                glb.update(player, this.levelData);
            } else {
                spawnGem(glb.getHitbox().x + glb.getHitbox().width / 2, glb.getHitbox().y + glb.getHitbox().height / 2);
                trySpawnCollectible(glb.getHitbox().x + glb.getHitbox().width / 2, glb.getHitbox().y + glb.getHitbox().height / 2);
                golemBosses.remove(i);
                i--;
            }
        }

        // Update projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            if (p.isActive()) {
                p.update();

                // Check if projectile is out of bounds
                if (p.isActive()) {
                    Rectangle2D.Float projHitbox = p.getHitbox();
                    float levelWidthInPixels = 0;
                    if (this.levelData != null && this.levelData[0] != null) {
                        levelWidthInPixels = this.levelData[0].length * Game.TILES_SIZE;
                    }
                    if (projHitbox.x < 0 || projHitbox.x + projHitbox.width > levelWidthInPixels) {
                        p.setActive(false);
                    }
                }

                // Check for collision with solid tiles
                if (p.isActive()) {
                    Rectangle2D.Float projHitbox = p.getHitbox();
                    if (utilz.HelpMethods.isSolid(this.levelData, projHitbox.x, projHitbox.y) ||
                        utilz.HelpMethods.isSolid(this.levelData, projHitbox.x + projHitbox.width, projHitbox.y) ||
                        utilz.HelpMethods.isSolid(this.levelData, projHitbox.x, projHitbox.y + projHitbox.height) ||
                        utilz.HelpMethods.isSolid(this.levelData, projHitbox.x + projHitbox.width, projHitbox.y + projHitbox.height)) {
                        p.setActive(false);
                    }
                }

                // Check for collision with player or enemies
                if (p.isActive()) {
                    if (p.getHitbox().intersects(player.getHitbox())) {
                        if (!player.isDamaged()) { // Apply damage only if player is not already in hit-stun
                            player.takeDamage(p.getDamage());
                            System.out.println("Player hit by projectile! Damage: " + p.getDamage());
                        }
                        p.setActive(false); // Deactivate projectile after hitting player
                    } else { // Projectile didn't hit player, check for enemy collision
                        boolean enemyHitByThisProjectile = false; // Flag to ensure projectile hits only one enemy
                        for (Nanite n : nanites) {
                            if (n.isActive() && p.getHitbox().intersects(n.getHitbox())) {
                                n.takeDamage(p.getDamage());
                                p.setActive(false);
                                enemyHitByThisProjectile = true;
                                break; // Projectile consumed
                            }
                        }
                        if (!enemyHitByThisProjectile && p.isActive()) { // Check if still active
                            for (Karagor k : karagors) {
                                if (k.isAlive() && p.getHitbox().intersects(k.getHitbox())) {
                                    k.takeDamage(p.getDamage());
                                    p.setActive(false);
                                    enemyHitByThisProjectile = true;
                                    break;
                                }
                            }
                        }
                        if (!enemyHitByThisProjectile && p.isActive()) {
                            for (Goblin gob : goblins) {
                                if (gob.isActive() && p.getHitbox().intersects(gob.getHitbox())) {
                                    gob.takeDamage(p.getDamage());
                                    p.setActive(false);
                                    enemyHitByThisProjectile = true;
                                    break;
                                }
                            }
                        }
                        if (!enemyHitByThisProjectile && p.isActive()) {
                            for (GoblinBoss gb : goblinBosses) {
                                if (gb.isAlive() && p.getHitbox().intersects(gb.getHitbox())) {
                                    gb.takeDamage(p.getDamage());
                                    p.setActive(false);
                                    enemyHitByThisProjectile = true;
                                    break;
                                }
                            }
                        }
                        if (!enemyHitByThisProjectile && p.isActive()) {
                            for (GolemBoss glb : golemBosses) {
                                if (glb.isAlive() && p.getHitbox().intersects(glb.getHitbox())) {
                                    glb.takeDamage(p.getDamage());
                                    p.setActive(false);
                                    // No need to set enemyHitByThisProjectile as it's the last check
                                    break; 
                                }
                            }
                        }
                    } 
                } 
            } 
        } 
        projectiles.removeIf(proj -> !proj.isActive()); // Remove inactive projectiles

        // Update gems
        for (Gem gem : gems) {
            gem.update();
        }
    }

    /**
     * Desenează toți inamicii activi, proiectilele și gem-urile pe ecran.
     * @param g Contextul grafic pentru desenare.
     * @param xLvlOffset Decalajul pe axa X al nivelului, pentru scrolling.
     */
    public void draw(Graphics g, int xLvlOffset){
        drawNanites(g, xLvlOffset);
        drawKaragors(g, xLvlOffset);
        drawGoblins(g, xLvlOffset);
        drawGoblinBosses(g, xLvlOffset);
        drawGolemBosses(g, xLvlOffset);
        drawProjectiles(g, xLvlOffset);
        drawGems(g, xLvlOffset);
    }   

    /**
     * Desenează toți GolemBosses activi pe ecran, inclusiv bara lor de viață.
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawGolemBosses(Graphics g, int xLvlOffset) {
        for (GolemBoss glb : golemBosses) {
            if (glb.isAlive()) {
                glb.render(g, xLvlOffset);
                drawHealthBar(g, glb, xLvlOffset);
            }
        }
    }

    /**
     * Desenează toate proiectilele active pe ecran.
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawProjectiles(Graphics g, int xLvlOffset) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.draw(g, xLvlOffset);
            }
        }
    }

    /**
     * Desenează toți GoblinBosses activi pe ecran, inclusiv bara lor de viață.
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawGoblinBosses(Graphics g, int xLvlOffset) {
        for (GoblinBoss gb : goblinBosses) {
            if (gb.isAlive()) {
                gb.render(g, xLvlOffset);
                drawHealthBar(g, gb, xLvlOffset);
            }
        }
    }

    /**
     * Desenează toate gem-urile active pe ecran.
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawGems(Graphics g, int xLvlOffset) {
        for (Gem gem : gems) {
            gem.draw(g, xLvlOffset);
        }
    }

    /**
     * Desenează toți Goblinii activi (Noob și Hard) pe ecran, inclusiv bara lor de viață și hitbox-ul (dacă este activat).
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawGoblins(Graphics g, int xLvlOffset) {
        for (Goblin gob : goblins) {
            if (!gob.isActive())
                continue;
            
            int animationIndex = gob.getAniIndex();
            int state = gob.getEnemyState();
            int type = gob.getEnemyType();
            int direction = gob.getDirection();

            BufferedImage[][] currentGoblinSheet;
            if (type == Goblin.GOBLIN_HARD) {
                currentGoblinSheet = goblinHardImgs;
            } else {
                currentGoblinSheet = goblinNoobImgs;
            }
            
            if (currentGoblinSheet != null && state >= 0 && state < currentGoblinSheet.length &&
                currentGoblinSheet[state] != null && animationIndex >= 0 &&
                animationIndex < currentGoblinSheet[state].length &&
                currentGoblinSheet[state][animationIndex] != null) {
               
                float xDrawOffsetGoblin = 25; // Decalaj specific pentru sprite-ul Goblinului
                float yDrawOffsetGoblin = 20; // Decalaj specific pentru sprite-ul Goblinului
                
                float drawWidth = Constants.EnemyConstants.GOBLIN_DRAW_WIDTH_DEFAULT * Game.SCALE;
                float drawHeight = Constants.EnemyConstants.GOBLIN_DRAW_HEIGHT_DEFAULT * Game.SCALE;
                
                float xPos = gob.getHitbox().x - xDrawOffsetGoblin - xLvlOffset;
                float yPos = gob.getHitbox().y - yDrawOffsetGoblin;

                if (direction > 0) { // Orientat spre dreapta
                    g.drawImage(currentGoblinSheet[state][animationIndex],
                        (int)xPos, (int)yPos,
                        (int)drawWidth, (int)drawHeight, null);
                } else { // Orientat spre stânga (imagine inversată)
                    g.drawImage(currentGoblinSheet[state][animationIndex],
                        (int)(xPos + drawWidth), (int)yPos, // Ajustează x pentru flip
                        (int)(-drawWidth), (int)drawHeight, null); // Lățime negativă pentru flip
                }
                drawHealthBar(g, gob, xLvlOffset);
                if (gob.drawHitbox) { // Desenează hitbox-ul dacă este activat
                    g.setColor(Color.WHITE);
                    g.drawString("X:" + (int)gob.getHitbox().x + ", Y:" + (int)gob.getHitbox().y,
                                (int)(gob.getHitbox().x - xLvlOffset), (int)gob.getHitbox().y - 5);
                }
            }
        }
    }

    /**
     * Desenează toți Nanites activi (Jungla și Pestera) pe ecran, inclusiv bara lor de viață și hitbox-ul (dacă este activat).
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawNanites(Graphics g, int xLvlOffset){
        for (Nanite n : nanites) {
            if (!n.isActive())
                continue;
            
            int animationIndex = n.getAniIndex();
            int state = n.getEnemyState();
            int type = n.getEnemyType();
            int direction = n.getDirection();
            
            BufferedImage[][] currentNaniteSheet = naniteImgs; // Implicit Nanite de Junglă
            if (type == Nanite.NANITE_PESTERA) {
                currentNaniteSheet = nanitePesteraImgs;
            }

            if (currentNaniteSheet != null && state < currentNaniteSheet.length &&
                animationIndex < currentNaniteSheet[state].length &&
                currentNaniteSheet[state][animationIndex] != null) {
               
                float xPos = n.getHitbox().x - 25 - xLvlOffset; // Decalaj pentru sprite-ul Nanite
                float yPos = n.getHitbox().y - 20; // Decalaj pentru sprite-ul Nanite
                
                float drawScaleFactor = 1.2f; // Factor de scalare specific pentru Nanite
                int scaledWidth = (int)(Constants.EnemyConstants.ENEMY_SIZE * drawScaleFactor);
                int scaledHeight = (int)(Constants.EnemyConstants.ENEMY_SIZE * drawScaleFactor);
                
                if (direction > 0) { // Orientat spre dreapta
                    g.drawImage(currentNaniteSheet[state][animationIndex],
                        (int)xPos, (int)yPos,
                        scaledWidth, scaledHeight, null);
                } else { // Orientat spre stânga (imagine inversată)
                    g.drawImage(currentNaniteSheet[state][animationIndex],
                        (int)xPos + scaledWidth, (int)yPos, // Ajustează x pentru flip
                        -scaledWidth, scaledHeight, null); // Lățime negativă pentru flip
                }
                drawHealthBar(g, n, xLvlOffset);
                if (n.drawHitbox) { // Desenează hitbox-ul dacă este activat
                    g.setColor(Color.WHITE);
                    g.drawString("X:" + (int)n.getHitbox().x + ", Y:" + (int)n.getHitbox().y,
                                (int)(n.getHitbox().x - xLvlOffset), (int)n.getHitbox().y - 5);
                }
            }
        }
    }
    
    /**
     * Desenează toți Karagors activi pe ecran, inclusiv bara lor de viață.
     * @param g Contextul grafic.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawKaragors(Graphics g, int xLvlOffset) {
        for (Karagor k : karagors) {
            if (k.isAlive()) {
                k.render(g, xLvlOffset);
                drawHealthBar(g, k, xLvlOffset);
            }
        }
    }
    
    /**
     * Desenează bara de viață pentru un Karagor.
     * @param g Contextul grafic.
     * @param karagor Karagor-ul pentru care se desenează bara de viață.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawHealthBar(Graphics g, Karagor karagor, int xLvlOffset) {
        if (karagor.getCurrentHealth() < karagor.getMaxHealth()) {
            int barWidth = 30;
            int barHeight = 5;
            int barX = (int)(karagor.getHitbox().x - xLvlOffset);
            int barY = (int)karagor.getHitbox().y - 10; // Deasupra hitbox-ului
            
            g.setColor(Color.BLACK); // Contur
            g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
            g.setColor(Color.RED); // Fundal roșu (viață pierdută)
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.GREEN); // Viață rămasă
            int currentWidth = (int)((karagor.getCurrentHealth() / (float)karagor.getMaxHealth()) * barWidth);
            g.fillRect(barX, barY, currentWidth, barHeight);
        }
    }
    
    /**
     * Desenează bara de viață pentru un Nanite.
     * @param g Contextul grafic.
     * @param nanite Nanite-ul pentru care se desenează bara de viață.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawHealthBar(Graphics g, Nanite nanite, int xLvlOffset) {
        if (nanite.getHealth() < nanite.getMaxHealth()) {
            int barWidth = 30;
            int barHeight = 5;
            int barX = (int)(nanite.getHitbox().x - xLvlOffset);
            int barY = (int)nanite.getHitbox().y - 10; // Deasupra hitbox-ului
            
            g.setColor(Color.BLACK); // Contur
            g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
            g.setColor(Color.RED); // Fundal roșu
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.GREEN); // Viață rămasă
            int currentWidth = (int)((nanite.getHealth() / (float)nanite.getMaxHealth()) * barWidth);
            g.fillRect(barX, barY, currentWidth, barHeight);
        }
    }

    /**
     * Desenează bara de viață pentru un Goblin.
     * @param g Contextul grafic.
     * @param goblin Goblin-ul pentru care se desenează bara de viață.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawHealthBar(Graphics g, Goblin goblin, int xLvlOffset) {
        if (goblin.getHealth() < goblin.getMaxHealth()) {
            int barWidth = 30;
            int barHeight = 5;
            int barX = (int)(goblin.getHitbox().x - xLvlOffset);
            int barY = (int)goblin.getHitbox().y - 10; // Deasupra hitbox-ului
            
            g.setColor(Color.BLACK); // Contur
            g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
            g.setColor(Color.RED); // Fundal roșu
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.GREEN); // Viață rămasă
            int currentWidth = (int)((goblin.getHealth() / (float)goblin.getMaxHealth()) * barWidth);
            g.fillRect(barX, barY, currentWidth, barHeight);
        }
    }

    /**
     * Desenează bara de viață pentru un GoblinBoss.
     * @param g Contextul grafic.
     * @param boss GoblinBoss-ul pentru care se desenează bara de viață.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawHealthBar(Graphics g, GoblinBoss boss, int xLvlOffset) {
        if (boss.isAlive()) { // Se afișează mereu pentru boss, chiar și cu viață plină, pentru vizibilitate
            int baseBarWidth = 50;
            int baseBarHeight = 7;
            int basePadding = 5; // Spațiu deasupra sprite-ului

            // Scalează dimensiunile barei de viață cu Game.SCALE pentru consistență vizuală
            int scaledBarWidth = (int)(baseBarWidth * Game.SCALE);
            int scaledBarHeight = (int)(baseBarHeight * Game.SCALE);
            int scaledPadding = (int)(basePadding * Game.SCALE);

            // Calculează poziția Y a vârfului vizual al sprite-ului boss-ului
            float goblinBossYDrawOffset = Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_Y * 2.0f; // Folosește factorul de scalare al boss-ului
            float spriteVisualTopY = boss.getHitbox().y - goblinBossYDrawOffset;

            // Poziționează bara deasupra vârfului vizual al sprite-ului
            int barX = (int)(boss.getHitbox().x + boss.getHitbox().width / 2 - scaledBarWidth / 2 - xLvlOffset);
            int barY = (int)(spriteVisualTopY - scaledBarHeight - scaledPadding);

            g.setColor(Color.BLACK); // Contur
            g.fillRect(barX - (int)(1 * Game.SCALE), barY - (int)(1 * Game.SCALE), scaledBarWidth + (int)(2 * Game.SCALE), scaledBarHeight + (int)(2 * Game.SCALE));
            g.setColor(Color.RED); // Fundal roșu
            g.fillRect(barX, barY, scaledBarWidth, scaledBarHeight);
            g.setColor(Color.GREEN); // Viață rămasă
            int currentHealthWidth = (int)(((float)boss.getCurrentHealth() / boss.getMaxHealth()) * scaledBarWidth);
            g.fillRect(barX, barY, currentHealthWidth, scaledBarHeight);
        }
    }
    
    /**
     * Desenează bara de viață pentru un GolemBoss.
     * @param g Contextul grafic.
     * @param boss GolemBoss-ul pentru care se desenează bara de viață.
     * @param xLvlOffset Decalajul nivelului pe axa X.
     */
    private void drawHealthBar(Graphics g, GolemBoss boss, int xLvlOffset) {
        if (boss.isAlive() && boss.getCurrentHealth() < boss.getMaxHealth()) { // Afișează doar dacă a primit daune
            int barWidth = 60; // Lățime mai mare pentru boss
            int barHeight = 8; // Înălțime mai mare
            // Poziționare relativă la hitbox-ul boss-ului
            int barX = (int)(boss.getHitbox().x + boss.getHitbox().width / 2 - barWidth / 2 - xLvlOffset);
            int barY = (int)(boss.getHitbox().y - barHeight - 7); // Puțin mai sus

            g.setColor(Color.BLACK); // Contur
            g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
            g.setColor(Color.RED); // Fundal roșu
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.GREEN); // Viață rămasă
            int currentHealthWidth = (int)(((float)boss.getCurrentHealth() / boss.getMaxHealth()) * barWidth);
            g.fillRect(barX, barY, currentHealthWidth, barHeight); // Corectat: folosește barHeight
        }
    }

    /**
     * Încarcă inamicii dintr-un nivel pe baza datelor acestuia.
     * Golește listele de inamici existenți și scanează datele nivelului pentru puncte de spawn.
     * Pentru anumite niveluri (1, 2, 3), toți inamicii sunt spawnați imediat.
     * @param levelData Datele nivelului (matrice de tile-uri).
     * @param level Obiectul Level curent, pentru a obține ID-ul nivelului.
     */
    public void loadEnemiesFromLevelData(int[][] levelData, Level level) {
        this.levelData = levelData;
        this.currentLevel = level.getLevelId();
        
        // Golește listele de inamici și punctele de spawn
        nanites.clear();
        karagors.clear();
        goblins.clear();
        goblinBosses.clear();
        golemBosses.clear();
        projectiles.clear();
        gems.clear();
        spawnPoints.clear();
        allEnemiesSpawned = false; // Resetează flag-ul de spawn
        
        scanLevelForSpawnPoints(); // Identifică noile puncte de spawn
        
        // Spawnează toți inamicii imediat pentru nivelurile 1, 2 și 3
        if (currentLevel == 1 || currentLevel == 2 || currentLevel == 3) {
            if (!allEnemiesSpawned) { // Verifică dacă nu au fost deja spawnați (deși flag-ul e resetat mai sus)
                spawnAllEnemies();
            }
        }
        // Pentru alte niveluri, spawn-ul se va face pe baza proximității jucătorului (vezi metoda update)
    }
    
    /**
     * Scanează datele nivelului pentru a identifica punctele de spawn ale inamicilor.
     * Stochează aceste puncte (coordonate și codul inamicului) într-o listă pentru spawn ulterior.
     * Afișează în consolă numărul de puncte de spawn găsite.
     */
    private void scanLevelForSpawnPoints() {
        spawnPoints.clear(); // Asigură-te că lista este goală înainte de scanare
        System.out.println("Scanez nivelul " + currentLevel + " pentru inamici...");
        
        // Codurile numerice pentru diferitele tipuri de inamici din datele nivelului
        final int NANITE_CODE = -2;
        final int KARAGOR_CODE = -3;
        final int GOBLIN_NOOB_CODE = -4;
        final int GOBLIN_HARD_CODE = -5;
        final int GOBLIN_BOSS_CODE = -6;
        final int NANITE_PESTERA_CODE = -7;
        final int GOLEM_BOSS_CODE = -9;

        int enemyCount = 0;
        
        if (levelData == null) {
            System.err.println("EnemyManager: levelData is null in scanLevelForSpawnPoints!");
            return;
        }
        
        // Parcurge matricea de date a nivelului
        for (int j = 0; j < levelData.length; j++) { // j = rând (coordonata Y)
            for (int i = 0; i < levelData[j].length; i++) { // i = coloană (coordonata X)
                int enemyCode = levelData[j][i];
                // Verifică dacă valoarea tile-ului corespunde unui cod de inamic
                if (enemyCode == NANITE_CODE || enemyCode == KARAGOR_CODE ||
                    enemyCode == GOBLIN_NOOB_CODE || enemyCode == GOBLIN_HARD_CODE ||
                    enemyCode == GOBLIN_BOSS_CODE || enemyCode == NANITE_PESTERA_CODE ||
                    enemyCode == GOLEM_BOSS_CODE) {
                    // Calculează coordonatele de spawn în pixeli
                    float x = i * Game.TILES_SIZE;
                    float y = j * Game.TILES_SIZE;
                    spawnPoints.add(new Point(x, y, enemyCode)); // Adaugă punctul de spawn la listă
                    enemyCount++;
                }
            }
        }
        System.out.println("Found " + enemyCount + " enemy spawn points for level " + currentLevel);
    }
    
    /**
     * Spawnează un inamic la o poziție specificată, pe baza codului său.
     * Adaugă inamicul nou creat la lista corespunzătoare și îi setează datele nivelului.
     * @param x Coordonata X a punctului de spawn.
     * @param y Coordonata Y a punctului de spawn.
     * @param enemyCode Codul numeric care identifică tipul de inamic.
     */
    private void spawnEnemy(float x, float y, int enemyCode) {
        switch (enemyCode) {
            case -2: // Nanite Jungla
                    if(currentLevel == 1) // Spawnează doar în nivelul 1
                    {
                        Nanite newNanite = new Nanite(x, y, Constants.EnemyConstants.ENEMY_SIZE, Constants.EnemyConstants.ENEMY_SIZE, Nanite.NANITE_JUNGLA);
                        newNanite.setLevelData(levelData);
                        nanites.add(newNanite);
                    }
                break;
            case -7: // Nanite Pestera
                if (currentLevel == 3) // Spawnează doar în nivelul 3
                {
                    Nanite newNanitePestera = new Nanite(x, y, Constants.EnemyConstants.ENEMY_SIZE, Constants.EnemyConstants.ENEMY_SIZE, Nanite.NANITE_PESTERA);
                    newNanitePestera.setLevelData(levelData);
                    nanites.add(newNanitePestera);
                }
                break;
            case -3: // Karagor
                if(currentLevel == 1) // Spawnează doar în nivelul 1
                {
                    Karagor newKaragor = new Karagor(x, y, (int)(272*Game.SCALE), (int)(183*Game.SCALE), true);
                    newKaragor.setLevelData(levelData);
                    karagors.add(newKaragor);
                }
                break;
            case -4: // Goblin Noob
                if(currentLevel == 2) // Spawnează doar în nivelul 2
                {
                    Goblin newGoblinNoob = new Goblin(x, y, Constants.EnemyConstants.GOBLIN_WIDTH, Constants.EnemyConstants.GOBLIN_HEIGHT, Goblin.GOBLIN_NOOB);
                    newGoblinNoob.setLevelData(levelData);
                    goblins.add(newGoblinNoob);
                }
                break;
            case -5: // Goblin Hard
                if(currentLevel == 3) // Spawnează doar în nivelul 3
                {
                    Goblin newGoblinHard = new Goblin(x, y, Constants.EnemyConstants.GOBLIN_WIDTH, Constants.EnemyConstants.GOBLIN_HEIGHT, Goblin.GOBLIN_HARD);
                    newGoblinHard.setLevelData(levelData);
                    goblins.add(newGoblinHard);
                }
                break;
            case -6: // Goblin Boss
                 if(currentLevel == 2) // Spawnează doar în nivelul 2
                 {
                    GoblinBoss newGoblinBoss = new GoblinBoss(x, y, this.playing);
                    newGoblinBoss.setLevelData(this.levelData);
                    goblinBosses.add(newGoblinBoss);
                 }
                break;
            case -9: // Golem Boss
                if (currentLevel == 3) // Spawnează doar în nivelul 3
                {
                    GolemBoss newGolemBoss = new GolemBoss(x, y, this.playing);
                    newGolemBoss.setLevelData(this.levelData);
                    golemBosses.add(newGolemBoss);
                }
                break;
            default:
                System.err.println("Unknown enemy code in spawnEnemy: " + enemyCode);
                break;
        }
    }
    
    /**
     * Spawnează toți inamicii identificați în punctele de spawn.
     * Marchează `allEnemiesSpawned` ca true după ce toți inamicii au fost spawnați.
     * Afișează în consolă numărul de inamici spawnați.
     */
    public void spawnAllEnemies(){
        System.out.println("Spawning all " + spawnPoints.size() + " enemies for level " + currentLevel);
        for (Point p : spawnPoints) {
            spawnEnemy(p.x, p.y, p.enemyCode);
        }
        allEnemiesSpawned = true; // Toți inamicii din listă au fost spawnați
    }
    
    /**
     * Verifică dacă trebuie spawnați noi inamici pe baza proximității jucătorului față de punctele de spawn rămase.
     * Dacă un punct de spawn este suficient de aproape de jucător, inamicul corespunzător este spawnaț și punctul este eliminat din listă.
     * @param playerHitbox Hitbox-ul jucătorului, pentru a calcula distanța.
     */
    private void checkForNewEnemySpawn(Rectangle2D.Float playerHitbox) {
        float playerX = playerHitbox.x;
        float playerY = playerHitbox.y;
        
        // Parcurge lista de puncte de spawn rămase
        for (int i = 0; i < spawnPoints.size(); i++) {
            Point p = spawnPoints.get(i);
            
            // Calculează distanța euclidiană dintre jucător și punctul de spawn
            float distance = (float) Math.sqrt(Math.pow(playerX - p.x, 2) + Math.pow(playerY - p.y, 2));
            
            // Dacă distanța este mai mică sau egală cu raza de detecție pentru spawn
            if (distance <= playerDetectionDistance) {
                spawnEnemy(p.x, p.y, p.enemyCode); // Spawnează inamicul
                spawnPoints.remove(i); // Elimină punctul de spawn din listă
                i--; // Ajustează indexul după eliminare pentru a nu sări peste elemente
            }
        }
        // Dacă nu mai sunt puncte de spawn, marchează că toți inamicii au fost spawnați
        if (spawnPoints.isEmpty()) {
            allEnemiesSpawned = true;
        }
    }
    
    /**
     * Resetează toți inamicii din nivel.
     * Golește listele de inamici și reîncarcă punctele de spawn din datele nivelului.
     * Pentru anumite niveluri (1, 2, 3), toți inamicii sunt spawnați imediat după resetare.
     */
    public void resetEnemies() {
        // Golește toate listele de inamici și obiecte
        nanites.clear();
        karagors.clear();
        goblins.clear();
        goblinBosses.clear();
        golemBosses.clear();
        gems.clear();
        spawnPoints.clear(); // Golește și lista de puncte de spawn
        allEnemiesSpawned = false; // Resetează flag-ul
        
        scanLevelForSpawnPoints(); // Re-scanează nivelul pentru puncte de spawn
        
        // Spawnează toți inamicii imediat pentru nivelurile specificate
        if (currentLevel == 1 || currentLevel == 2 || currentLevel == 3) {
            spawnAllEnemies();
        }
        // Pentru alte niveluri, spawn-ul se va face pe baza proximității
    }
    
    /**
     * Returnează ID-ul nivelului curent.
     * @return ID-ul nivelului.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Returnează lista de Nanites activi.
     * @return O {@link ArrayList} de obiecte {@link Nanite}.
     */
    public ArrayList<Nanite> getNanites() {
        return nanites;
    }
    
    /**
     * Returnează lista de Karagors activi.
     * @return O {@link ArrayList} de obiecte {@link Karagor}.
     */
    public ArrayList<Karagor> getKaragors() {
        return karagors;
    }

    /**
     * Returnează lista de Goblini activi.
     * @return O {@link ArrayList} de obiecte {@link Goblin}.
     */
    public ArrayList<Goblin> getGoblins() {
        return goblins;
    }

    /**
     * Returnează lista de GoblinBosses activi.
     * @return O {@link ArrayList} de obiecte {@link GoblinBoss}.
     */
    public ArrayList<GoblinBoss> getGoblinBosses() {
        return goblinBosses;
    }

    /**
     * Returnează lista de Gem-uri active.
     * @return O {@link ArrayList} de obiecte {@link Gem}.
     */
    public ArrayList<Gem> getGems() {
        return gems;
    }

    /**
     * Returnează lista de GolemBosses activi.
     * @return O {@link ArrayList} de obiecte {@link GolemBoss}.
     */
    public ArrayList<GolemBoss> getGolemBosses() {
        return golemBosses;
    }

    /**
     * Adaugă un proiectil la lista de proiectile active gestionate de EnemyManager.
     * @param p Proiectilul care trebuie adăugat.
     */
    public void addProjectile(Projectile p) {
        this.projectiles.add(p);
    }
    
    /**
     * Aplică un efect de knockback jucătorului.
     * Direcția knockback-ului depinde de partea din care a fost lovit jucătorul.
     * @param player Jucătorul care primește knockback.
     * @param enemyHitFromLeft true dacă inamicul a lovit din stânga jucătorului, false altfel.
     */
    private void applyKnockback(Player player, boolean enemyHitFromLeft) {
        float knockbackX = enemyHitFromLeft ? 10f : -10f; // Knockback orizontal
        float knockbackY = -1f; // Knockback vertical ușor în sus
        
        player.applyKnockback(knockbackX, knockbackY);
    }
    
    /**
     * Clasă internă simplă pentru a stoca informații despre un punct de spawn:
     * coordonatele (x, y) și codul numeric al inamicului care trebuie spawnaț.
     */
    private class Point {
        float x, y;
        int enemyCode; // Codul inamicului de spawnaț
        
        /**
         * Constructor pentru clasa Point.
         * @param x Coordonata X a punctului de spawn.
         * @param y Coordonata Y a punctului de spawn.
         * @param enemyCode Codul inamicului.
         */
        public Point(float x, float y, int enemyCode) {
            this.x = x;
            this.y = y;
            this.enemyCode = enemyCode;
        }
    }

    /**
     * Spawnează un gem la o poziție specificată.
     * Tipul gem-ului depinde de nivelul curent.
     * @param x Coordonata X a punctului de spawn pentru gem.
     * @param y Coordonata Y a punctului de spawn pentru gem.
     */
    private void spawnGem(float x, float y) {
        gems.add(new Gem(x, y, currentLevel)); // Tipul gem-ului este determinat de nivel
        System.out.println("Gem spawned at: " + x + ", " + y + " for level " + currentLevel);
    }

    /**
     * Încearcă să spawneze un obiect colectabil (banană sau nucă de cocos) la o poziție specificată,
     * cu o șansă de 50% pentru fiecare. Verifică dacă imaginile pentru colectabile sunt încărcate.
     * @param x Coordonata X a punctului de spawn pentru obiectul colectabil.
     * @param y Coordonata Y a punctului de spawn pentru obiectul colectabil.
     */
    private void trySpawnCollectible(float x, float y) {
        Level currentPlayingLevel = playing.getLevelManager().getCurrentLevel();
        if (currentPlayingLevel == null) {
            System.err.println("EnemyManager: Could not spawn collectible - current level is null.");
            return;
        }

        if (random.nextFloat() < 0.5f) { // 50% șansă pentru banană
            if (LoadSave.BANANA_IMAGE != null) {
                Banana banana = new Banana(x, y, currentPlayingLevel.getLevelData(), LoadSave.BANANA_IMAGE);
                currentPlayingLevel.addBanana(banana); // Adaugă banana la lista nivelului
                System.out.println("Banana spawned at: " + x + ", " + y + " for level " + currentLevel);
            } else {
                System.err.println("EnemyManager: Could not spawn banana - BANANA_IMAGE is null.");
            }
        } else { // Altfel, încearcă să spawneze nucă de cocos
            if (LoadSave.COCONUT_IMAGE != null) {
                Coconut coconut = new Coconut(x, y, currentPlayingLevel.getLevelData(), LoadSave.COCONUT_IMAGE);
                currentPlayingLevel.addCoconut(coconut); // Adaugă nuca de cocos la lista nivelului
                System.out.println("Coconut spawned at: " + x + ", " + y + " for level " + currentLevel);
            } else {
                System.err.println("EnemyManager: Could not spawn coconut - COCONUT_IMAGE is null.");
            }
        }
    }
}
