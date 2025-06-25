package entities;

import java.awt.geom.Rectangle2D;

import main.Game;
import utilz.HelpMethods;
import utilz.Enemy_Animation_Rows;
import utilz.Constants; // Added import

/**
 * Reprezintă entitatea Goblin în joc.
 * Această clasă extinde clasa Enemy și definește comportamentul specific
 * și atributele pentru Goblini, inclusiv tipurile (Noob, Hard), stările,
 * logica de patrulare, atac și interacțiunea cu jucătorul.
 */
public class Goblin extends Enemy {

    // Goblin Types (as used in EnemyManager)
    /** Tipul de Goblin "Noob". */
    public static final int GOBLIN_NOOB = 0; // Example type, ensure matches usage
    /** Tipul de Goblin "Hard". */
    public static final int GOBLIN_HARD = 1; // Example type, ensure matches usage


    // Goblin states
    /** Starea IDLE a Goblinului. */
    public static final int IDLE = 3;
    /** Starea RUNNING (alergare) a Goblinului. */
    public static final int RUNNING = 10;
    /** Starea ATTACK (atac) a Goblinului (corespunde animației SLASHING). */
    public static final int ATTACK = 12; // SLASHING
    /** Starea HURT (lovit) a Goblinului. */
    public static final int HURT = 2;
    /** Starea DYING (moarte) a Goblinului. */
    public static final int DYING = 0;
    
    // Goblin attributes
    /** Sănătatea curentă a Goblinului. */
    private int health;
    /** Sănătatea maximă a Goblinului. */
    private int maxHealth;
    /** Daunele provocate de atacul Goblinului. */
    private int damage;
    /** Indicator dacă Goblinul este activ în joc. */
    private boolean isActive = true;
    /** Distanța de atac a Goblinului. */
    private int attackRange = 30;
    /** Numărul de tick-uri petrecute în starea curentă. */
    private int ticksInState = 0;
    /** Cooldown-ul dintre atacuri. */
    private int attackCooldown = 0;
    /** Cooldown-ul maxim dintre atacuri (aproximativ 2 secunde la 60 FPS). */
    private static final int ATTACK_COOLDOWN_MAX = 120;
    /** Indicator dacă lovitura de atac a fost verificată în animația curentă. */
    private boolean attackChecked = false;
    /** Indicator dacă jucătorul a fost detectat de Goblin. */
    private boolean playerDetected = false;
    
    // Patrol boundaries (calculated based on spawn point)
    /** Limita stângă a zonei de patrulare. */
    private float leftPatrolLimit;
    /** Limita dreaptă a zonei de patrulare. */
    private float rightPatrolLimit;
    /** Indicator dacă limitele de patrulare au fost setate. */
    private boolean patrolBoundariesSet = false;
    
    // Movement
    /** Indicator dacă Goblinul se mișcă. */
    private boolean isMoving = false;
    /** Direcția de mișcare a Goblinului (1 = dreapta, -1 = stânga). */
    private int direction = 1;
    /** Viteza de mișcare curentă a Goblinului. */
    private float moveSpeed = 0.5f;
    /** Viteza de mișcare în timpul patrulării. */
    private float patrolMoveSpeed;
    /** Viteza de mișcare în timpul urmăririi jucătorului. */
    private float chaseMoveSpeed;
    /** Datele nivelului curent, folosite pentru coliziuni și navigație. */
    private int[][] levelData;
    /** Indicator dacă Goblinul se află în aer. */
    private boolean inAir = false;
    /** Viteza verticală a Goblinului în aer. */
    private float airSpeed = 0f;
    /** Valoarea gravitației aplicate Goblinului. */
    private float gravity = 0.04f;
    /** Viteza de săritură a Goblinului. */
    private float jumpSpeed = -3.5f * Game.SCALE;
    
    // Tracking
    /** Hitbox-ul pentru atacul Goblinului. */
    private Rectangle2D.Float attackBox;
    /** Cooldown-ul pentru daunele provocate prin atingerea jucătorului. */
    private int playerTouchCooldown = 0;
    /** Cooldown-ul dintre daunele provocate prin atingere (în frame-uri). */
    private int touchDamageCooldown = 60;
     
    // Patrolling
    /** Distanța pe care Goblinul o patrulează în fiecare direcție de la punctul de start. */
    private float patrolDistance = 100.0f;
    /** Distanța la care Goblinul poate detecta jucătorul. */
    private int detectionRange = 300;
    
    /**
     * Constructor pentru clasa Goblin.
     * Inițializează Goblinul cu o poziție, dimensiuni și tip specific.
     * Setează atributele în funcție de tip (Noob sau Hard), inițializează parametrii de patrulare,
     * starea inițială și hitbox-ul.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param width Lățimea entității.
     * @param height Înălțimea entității.
     * @param enemyType Tipul Goblinului (GOBLIN_NOOB sau GOBLIN_HARD).
     */
    public Goblin(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height, enemyType); // enemyType will be GOBLIN_NOOB or GOBLIN_HARD
        
        if (enemyType == GOBLIN_HARD) {
            this.maxHealth = 75; // Bigger health for Hard
            this.health = this.maxHealth;
            this.damage = 5;     // Damage set to 5 for Hard
        } else { // Default to Noob stats
            this.maxHealth = 50;
            this.health = this.maxHealth;
            this.damage = 4;
        }
        
        // Set up patrolling parameters
        this.patrolMoveSpeed = 0.4f * Game.SCALE;
        this.chaseMoveSpeed = 0.9f * Game.SCALE;
        this.moveSpeed = this.patrolMoveSpeed; // Start with patrol speed
        
        // Set up patrol boundaries
        this.leftPatrolLimit = x - patrolDistance;
        this.rightPatrolLimit = x + patrolDistance;
        
        // Initialize state
        setState(IDLE);

        // --- Hitbox Calculation ---
        // Original hitbox parameters relative to the 100x100 SOURCE sprite
        final float SRC_SPRITE_WIDTH_DEFAULT = Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_WIDTH_DEFAULT; // 100f
        final float SRC_SPRITE_HEIGHT_DEFAULT = Constants.EnemyConstants.GOBLIN_SPRITE_SOURCE_HEIGHT_DEFAULT; // 100f

        // Original hitbox was implicitly: width = 100-55=45, height = 100-23=77
        // The Y offset was 37.
        final float SRC_HITBOX_WIDTH = SRC_SPRITE_WIDTH_DEFAULT - 55f; // 45f
        final float SRC_HITBOX_HEIGHT = SRC_SPRITE_HEIGHT_DEFAULT - 36f; // Corrected from -29f to align with Nanite/intended size

        // Current default drawing dimensions (before Game.SCALE)
        final float DRAW_SPRITE_WIDTH_DEFAULT = Constants.EnemyConstants.GOBLIN_DRAW_WIDTH_DEFAULT; // 150f
        final float DRAW_SPRITE_HEIGHT_DEFAULT = Constants.EnemyConstants.GOBLIN_DRAW_HEIGHT_DEFAULT; // 150f

        // Calculate scaling factor from source sprite to draw sprite (default sizes)
        float scaleFactorX = DRAW_SPRITE_WIDTH_DEFAULT / SRC_SPRITE_WIDTH_DEFAULT; // 1.5
        float scaleFactorY = DRAW_SPRITE_HEIGHT_DEFAULT / SRC_SPRITE_HEIGHT_DEFAULT; // 1.5

        // Calculate the hitbox dimensions, scaled to the new drawing size (but still default, before Game.SCALE)
        float hitboxWidthDefault = SRC_HITBOX_WIDTH * scaleFactorX;
        float hitboxHeightDefault = SRC_HITBOX_HEIGHT * scaleFactorY;

        // Final scaled hitbox dimensions (applying Game.SCALE)
        float finalHitboxWidth = hitboxWidthDefault * Game.SCALE;
        float finalHitboxHeight = hitboxHeightDefault * Game.SCALE;

        // The x, y constructor arguments are the top-left of the tile where the goblin spawns (already Game.SCALE'd).
        // We want the goblin's sprite's feet to be at the bottom of this spawn tile.
        // Visual bottom of the sprite = y_spawn_arg + Game.TILES_SIZE
        // Visual top of the sprite = (y_spawn_arg + Game.TILES_SIZE) - (DRAW_SPRITE_HEIGHT_DEFAULT * Game.SCALE)
        float spriteVisualTopY = y + Game.TILES_SIZE - (DRAW_SPRITE_HEIGHT_DEFAULT * Game.SCALE);
        
        // Let's align the visual left of the sprite with the x_spawn_arg.
        float spriteVisualLeftX = x;

        // Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_X and _Y define how the hitbox's top-left
        // is positioned relative to the sprite's visual top-left.
        // sprite_draw_pos.x = hitbox.x - GOBLIN_DRAW_OFFSET_X  => hitbox.x = sprite_draw_pos.x + GOBLIN_DRAW_OFFSET_X
        // sprite_draw_pos.y = hitbox.y - GOBLIN_DRAW_OFFSET_Y  => hitbox.y = sprite_draw_pos.y + GOBLIN_DRAW_OFFSET_Y
        float finalHitboxX = spriteVisualLeftX + Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_X;
        float finalHitboxY = spriteVisualTopY + Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_Y;
        
        initHitbox(finalHitboxX, finalHitboxY, (int)finalHitboxWidth, (int)finalHitboxHeight);
        
        // Activăm desenarea hitbox-ului pentru debugging
        drawHitbox = false; // Set to true to see the hitbox
        
        // Initialize attack box in front of Goblin, relative to the new hitbox
        // attackRange is an unscaled pixel value for now.
        float scaledAttackRange = attackRange * Game.SCALE; // Scale attackRange if it's a default value
        attackBox = new Rectangle2D.Float(hitbox.x + hitbox.width, hitbox.y, scaledAttackRange, hitbox.height);
        // updateAttackBox() will correctly position it based on direction.
    }
    
    /**
     * Actualizează starea Goblinului.
     * Aceasta include actualizarea hitbox-ului de atac, cooldown-urilor, comportamentului
     * și poziției. De asemenea, inițializează limitele de patrulare dacă este necesar.
     * @param playerHitbox Hitbox-ul jucătorului, pentru interacțiuni.
     */
    @Override
    public void update(Rectangle2D.Float playerHitbox) { // Modified to accept playerHitbox
        super.update(playerHitbox); // Pass playerHitbox to superclass
        
        if (!isActive)
            return;
            
        updateAttackBox();
        updateCooldowns();
        updateBehavior(playerHitbox); // Pass playerHitbox to updateBehavior
        updatePosition();
        
        // Set patrol boundaries once the enemy has landed on the ground
        if (!inAir && !patrolBoundariesSet) {
            initPatrolBoundaries();
        }
    }
    
    /**
     * Actualizează poziția și dimensiunea hitbox-ului de atac în funcție de direcția Goblinului.
     */
    private void updateAttackBox() {
        if (direction > 0) {
            // Facing right - attack box on right side
            attackBox.x = hitbox.x + hitbox.width;
        } else {
            // Facing left - attack box on left side
            attackBox.x = hitbox.x - attackRange;
        }
        attackBox.y = hitbox.y;
    }
    
    /**
     * Actualizează cronometrele de cooldown pentru atac și atingerea jucătorului.
     */
    private void updateCooldowns() {
        ticksInState++;
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (playerTouchCooldown > 0) {
            playerTouchCooldown--;
        }
    }
    
    /**
     * Inițializează limitele de patrulare pentru Goblin pe baza poziției sale curente.
     * Această metodă ar trebui apelată după ce Goblinul s-a așezat pe o platformă.
     * Caută marginile platformei pentru a stabili limitele.
     */
    private void initPatrolBoundaries() {
        // Default patrol boundaries (approximately +/- 3 tiles from spawn point)
        leftPatrolLimit = Math.max(0, hitbox.x - Game.TILES_SIZE * 3);
        rightPatrolLimit = hitbox.x + Game.TILES_SIZE * 3;
        
        // Search for platform edges
        boolean leftEdgeFound = false;
        boolean rightEdgeFound = false;
        
        // Look left to find platform edge
        for (int i = 1; i <= 6; i++) {
            float checkX = hitbox.x - (i * Game.TILES_SIZE);
            if (checkX < 0 || !HelpMethods.isEntityOnFloor(new Rectangle2D.Float(checkX, hitbox.y, hitbox.width, hitbox.height), levelData)) {
                leftPatrolLimit = hitbox.x - ((i-1) * Game.TILES_SIZE); // Stay one tile from edge
                leftEdgeFound = true;
                break;
            }
        }
        
        // Look right to find platform edge
        for (int i = 1; i <= 6; i++) {
            float checkX = hitbox.x + (i * Game.TILES_SIZE);
            if (!HelpMethods.isEntityOnFloor(new Rectangle2D.Float(checkX, hitbox.y, hitbox.width, hitbox.height), levelData)) {
                rightPatrolLimit = hitbox.x + ((i-1) * Game.TILES_SIZE); // Stay one tile from edge
                rightEdgeFound = true;
                break;
            }
        }
        
        // Ensure the patrol area is at least 2 tiles wide
        if (leftEdgeFound && rightEdgeFound && rightPatrolLimit - leftPatrolLimit < Game.TILES_SIZE * 2) {
            float midpoint = (leftPatrolLimit + rightPatrolLimit) / 2;
            leftPatrolLimit = midpoint - Game.TILES_SIZE;
            rightPatrolLimit = midpoint + Game.TILES_SIZE;
        }
        
        patrolBoundariesSet = true;
    }
    
    /**
     * Verifică dacă Goblinul poate vedea jucătorul.
     * Condițiile includ distanța, alinierea verticală și direcția în care privește Goblinul.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă jucătorul este vizibil, false altfel.
     */
    private boolean canSeePlayer(Rectangle2D.Float playerHitbox) {
        // Check if player is within detection range
        int playerX = (int) playerHitbox.getCenterX();
        int GoblinX = (int) hitbox.getCenterX();
        if (Math.abs(playerX - GoblinX) > detectionRange) {
            return false;
        }

        // Check if player is vertically aligned (e.g., within one tile height)
        int playerY = (int) playerHitbox.getCenterY();
        int GoblinY = (int) hitbox.getCenterY();
        if (Math.abs(playerY - GoblinY) > Game.TILES_SIZE) { // Allow one tile difference
            return false;
        }

        // Check if player is in the direction the Goblin is facing
        if (direction > 0) { // Facing right
            return playerX > GoblinX;
        } else { // Facing left
            return playerX < GoblinX;
        }
    }
    
    /**
     * Actualizează comportamentul Goblinului pe baza stării curente și a interacțiunii cu jucătorul.
     * Gestionează tranzițiile între stări (IDLE, RUNNING, ATTACK, HURT, DYING) și logica specifică fiecărei stări.
     * @param playerHitbox Hitbox-ul jucătorului, pentru a lua decizii.
     */
    private void updateBehavior(Rectangle2D.Float playerHitbox) { // Modified to accept playerHitbox
        // Update playerDetected flag
        playerDetected = canSeePlayer(playerHitbox);

        // State behavior
        switch (getEnemyState()) {
            case IDLE:
                if (ticksInState > 60) {
                    ticksInState = 0;
                    // Start moving if not already moving
                    setState(RUNNING);
                    isMoving = true;
                }
                break;
                
            case RUNNING:
                isMoving = true;
                
                // Check if can attack player
                if (playerDetected && attackCooldown <= 0) {
                    setState(ATTACK);
                }
                // Check patrol boundaries if not chasing player
                else if (!playerDetected && patrolBoundariesSet) {
                    // If reached patrol limit, change direction
                    if ((direction > 0 && hitbox.x >= rightPatrolLimit) || 
                        (direction < 0 && hitbox.x <= leftPatrolLimit)) {
                        direction *= -1; // Reverse direction
                        
                        // Occasionally pause at patrol endpoints
                        if (Math.random() < 0.3) {
                            setState(IDLE);
                            isMoving = false;
                            ticksInState = 0;
                        }
                    }
                }
                break;
                
            case ATTACK:
                isMoving = false;
                // Check for attack hit once during animation
                if (!attackChecked && ticksInState > 6) {
                    checkAttackHit();
                    attackChecked = true;
                    attackCooldown = ATTACK_COOLDOWN_MAX; // Set cooldown after attack
                }
                
                // Return to previous state after attack animation
                if (ticksInState >= Enemy_Animation_Rows.values()[ATTACK].getFrameCount() * 5) {
                    attackChecked = false;
                    setState(IDLE);
                }
                break;
                
            case HURT:
                isMoving = false;
                if (ticksInState >= Enemy_Animation_Rows.values()[HURT].getFrameCount() * 5) {
                    if (health <= 0) {
                        setState(DYING);
                    } else {
                        setState(IDLE);
                    }
                }
                break;
                
            case DYING:
                isMoving = false;
                if (ticksInState >= Enemy_Animation_Rows.values()[DYING].getFrameCount() * 5) {
                    isActive = false;
                }
                break;
        }
    }
    
    /**
     * Actualizează poziția Goblinului.
     * Aplică gravitația, gestionează mișcarea verticală și orizontală,
     * verifică coliziunile cu pereții și podeaua, și previne căderea de pe platforme.
     */
    private void updatePosition() {
        boolean justLanded = false; // Flag to indicate if a landing occurred in this frame

        if (inAir) {
            // Apply gravity and check for vertical collision
            float airSpeedY = airSpeed;
            float nextY = hitbox.y + airSpeedY;

            if (HelpMethods.canMoveHere(hitbox.x, nextY, hitbox.width, hitbox.height, levelData)) {
                hitbox.y = nextY;
                airSpeed += gravity;
            } else {
                // Collision detected (floor or ceiling)
                if (airSpeed > 0) { // Falling down - landed on floor
                    int tileYLanding = (int) ((hitbox.y + hitbox.height + airSpeedY) / Game.TILES_SIZE);
                    float oldY = hitbox.y;
                    hitbox.y = (float) (tileYLanding * Game.TILES_SIZE - hitbox.height - 1.0f); // Place 1px higher
                    System.out.println(String.format("Goblin landing: oldY=%.2f, airSpeedY=%.2f, newY=%.2f, tileYLanding=%d", oldY, airSpeedY, hitbox.y, tileYLanding));
                    
                    inAir = false;
                    airSpeed = 0;
                    justLanded = true; // Mark that a landing just happened

                } else if (airSpeed < 0) { // Moving upwards - hit ceiling
                    int tileYHitting = (int) ((hitbox.y + airSpeedY) / Game.TILES_SIZE);
                    hitbox.y = (float) ((tileYHitting + 1) * Game.TILES_SIZE);
                    airSpeed = 0;
                }
            }
        }

        // Check if enemy is on floor, but only if it didn't just land in this exact frame.
        // This prevents inAir from being set to true immediately after a landing calculation.
        if (!justLanded && !inAir) {
            if (!HelpMethods.isEntityOnFloor(hitbox, levelData)) {
                System.out.println(String.format("Goblin at (%.2f, %.2f) found not on floor. Becoming airborne.", hitbox.x, hitbox.y));
                inAir = true;
            }
        }

        // Check if about to fall off edge and turn around if so
        if (!willLandOnGround(hitbox.x + direction * moveSpeed, hitbox.y)) {
            direction *= -1;
        }
        
        // Move horizontally
        float nextX = hitbox.x + direction * moveSpeed;
        if (HelpMethods.canMoveHere(nextX, hitbox.y, hitbox.width, hitbox.height, levelData)) {
            hitbox.x = nextX;
        } else {
            // Hit wall, change direction
            direction *= -1;
        }
        
        // Update entity position based on hitbox
        x = hitbox.x;
        y = hitbox.y;
        
        // Update attack box position
        updateAttackBox();
    }
    
    /**
     * Verifică dacă Goblinul va ateriza pe o suprafață solidă la poziția specificată.
     * @param x Coordonata X a poziției viitoare.
     * @param y Coordonata Y a poziției viitoare.
     * @return true dacă Goblinul va ateriza pe sol, false altfel.
     */
    private boolean willLandOnGround(float x, float y) {
        // Create test hitbox at the future position
        Rectangle2D.Float testHitbox = new Rectangle2D.Float(x, y, hitbox.width, hitbox.height);
        return HelpMethods.isEntityOnFloor(testHitbox, levelData);
    }
    
    /**
     * Verifică dacă atacul Goblinului a lovit jucătorul.
     * Într-un joc real, aici s-ar verifica coliziunea cu hitbox-ul jucătorului
     * și s-ar aplica daune dacă există o coliziune.
     */
    private void checkAttackHit() {
        // In a real game, you would check for collision with player hitbox here
        // and apply damage if there is a collision
    }
    
    /**
     * Aplică daune Goblinului atunci când este atacat de jucător.
     * Reduce sănătatea și gestionează tranziția la starea HURT sau DYING.
     * @param damage Cantitatea de daune primite.
     */
    public void takeDamage(int damage) {
        // Don't take damage if already dying
        if (getEnemyState() == DYING)
            return;
            
        // Apply damage
        health -= damage;
        
        // Check if goblin is dead
        if (health <= 0) {
            health = 0;
            setState(DYING); // Set to dying state
            isMoving = false; // Stop movement
            airSpeed = 0;     // Stop vertical movement
            System.out.println("Goblin is dying!"); // Debug message
        } else {
            // If not dead, just show hurt animation
            setState(HURT);
        }
    }
    
    /**
     * Verifică dacă jucătorul atinge Goblinul și aplică daune dacă este cazul.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă jucătorul a lovit Goblinul și daunele au fost aplicate, false altfel.
     */
    public boolean checkPlayerHit(Rectangle2D.Float playerHitbox) {
        if (!isActive || playerTouchCooldown > 0)
            return false;
            
        if (hitbox.intersects(playerHitbox)) {
            playerTouchCooldown = touchDamageCooldown;
            return true;
        }
        return false;
    }
    
    /**
     * Verifică dacă Goblinul poate ataca jucătorul (dacă jucătorul este în raza de atac).
     * Dacă da, inițiază starea de atac.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă Goblinul poate ataca și a inițiat atacul, false altfel.
     */
    public boolean canAttackPlayer(Rectangle2D.Float playerHitbox) {
        if (!isActive || attackCooldown > 0 || getEnemyState() == ATTACK || getEnemyState() == HURT || getEnemyState() == DYING)
            return false;
            
        if (attackBox.intersects(playerHitbox)) {
            setState(ATTACK);
            attackCooldown = ATTACK_COOLDOWN_MAX; // Use the constant instead of attackCooldownTime
            ticksInState = 0;
            attackChecked = false;
            return true;
        }
        return false;
    }
    
    /**
     * Detectează jucătorul dacă se află în raza de vizualizare și ajustează direcția
     * Goblinului pentru a se îndrepta către jucător.
     * Trece în modul de urmărire (chase) dacă jucătorul este detectat.
     * @param playerX Coordonata X a jucătorului.
     * @param playerY Coordonata Y a jucătorului.
     */
    public void playerDetected(float playerX, float playerY) {
        if (!isActive || getEnemyState() == ATTACK || getEnemyState() == HURT || getEnemyState() == DYING)
            return;
            
        // Check if player is within detection range
        float xDistance = Math.abs(playerX - hitbox.x);
        float yDistance = Math.abs(playerY - hitbox.y);
        float distance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        
        if (distance <= detectionRange) {
            // Player detected - chase mode
            playerDetected = true;
            moveSpeed = chaseMoveSpeed;
            
            // Face player
            if (playerX < hitbox.x) {
                direction = -1; // Face left
            } else {
                direction = 1;  // Face right
            }
            
            // Start moving toward player
            if (getEnemyState() == IDLE) {
                setState(RUNNING);
                isMoving = true;
            }
        } else if (playerDetected && distance > detectionRange * 1.5f) {
            // Player out of range - return to patrol mode
            playerDetected = false;
            moveSpeed = patrolMoveSpeed;
        }
    }
    
    /**
     * Setează starea curentă a Goblinului și resetează cronometrul pentru starea respectivă.
     * @param state Noua stare a Goblinului.
     */
    private void setState(int state) {
        this.setEnemyState(state);
        ticksInState = 0;
    }
    
    /**
     * Setează datele nivelului pentru Goblin.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void setLevelData(int[][] levelData) {
        this.levelData = levelData;
    }
    
    /**
     * Verifică dacă Goblinul este activ în joc.
     * @return true dacă Goblinul este activ, false altfel.
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Returnează sănătatea curentă a Goblinului.
     * @return Sănătatea curentă.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Returnează sănătatea maximă a Goblinului.
     * @return Sănătatea maximă.
     */
    public int getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Returnează daunele de atac ale Goblinului.
     * @return Daunele de atac.
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * Returnează direcția curentă a Goblinului.
     * @return Direcția (1 pentru dreapta, -1 pentru stânga).
     */
    public int getDirection() {
        return direction;
    }
    
    /**
     * Transformă acest Goblin într-un boss (Karagor).
     * Mărește dimensiunea, sănătatea, daunele și alte atribute.
     * Această metodă modifică instanța curentă de Goblin.
     */
    public void makeBoss() {
        // Increase stats for boss
        this.maxHealth = 100; // Much higher health
        this.health = maxHealth;
        this.damage = 20;      // More damage
        
        // Increase size (1.5x larger than regular Goblins)
        float bossSizeMultiplier = 1.5f;
        
        // Adjust hitbox
        float newHitboxWidth = hitbox.width * bossSizeMultiplier;
        float newHitboxHeight = hitbox.height * bossSizeMultiplier;
        
        // Center the hitbox at the same position
        float newHitboxX = hitbox.x - (newHitboxWidth - hitbox.width) / 2;
        float newHitboxY = hitbox.y - (newHitboxHeight - hitbox.height) / 2;
        
        // Update hitbox with new size
        initHitbox(newHitboxX, newHitboxY, (int)newHitboxWidth, (int)newHitboxHeight);
        
        // Increase attack range
        attackRange *= 1.5f;
        
        // Update attack box
        updateAttackBox();
        
        // Increase movement speeds
        this.patrolMoveSpeed *= 0.8f; // Slower patrol (more menacing)
        this.chaseMoveSpeed *= 1.2f;  // Faster chase
        this.moveSpeed = this.patrolMoveSpeed; // Start with patrol speed
        
        // Adjust patrol distance for boss behavior
        this.patrolDistance *= 1.5f;
        
        // Set boss detection range (larger than regular Goblins)
        this.detectionRange = 400; // Boss can see player from further away
        
        // Start in IDLE state
        setState(IDLE);
    }
    
    /**
     * Returnează hitbox-ul de atac al Goblinului.
     * @return Dreptunghiul reprezentând hitbox-ul de atac.
     */
    public Rectangle2D.Float getAttackBox() {
        return attackBox;
    }

    /**
     * Returnează tipul Goblinului (de exemplu, NOOB, HARD).
     * Acesta corespunde valorii `enemyType` transmise în constructor.
     * @return Tipul Goblinului.
     */
    public int getGoblinType() {
        return this.enemyType; // enemyType is inherited from Enemy class
    }
}
