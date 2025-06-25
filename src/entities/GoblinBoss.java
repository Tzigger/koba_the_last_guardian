package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.Constants;
import utilz.Enemy_Animation_Rows;
import utilz.LoadSave;
import utilz.HelpMethods;
import gamestates.Playing;

/**
 * Reprezintă entitatea Goblin Boss în joc.
 * Această clasă extinde clasa Enemy și definește comportamentul specific
 * și atributele pentru Goblin Boss, inclusiv animațiile, stările de acțiune,
 * logica de atac și interacțiunea cu jucătorul.
 */
public class GoblinBoss extends Enemy {

    /** Matrice bidimensională pentru stocarea animațiilor normale ale boss-ului. */
    private BufferedImage[][] animations;
    /** Matrice bidimensională pentru stocarea animațiilor inversate (flipped) ale boss-ului. */
    private BufferedImage[][] flippedAnimations;

    /** Sănătatea maximă a Goblin Boss-ului. */
    private int maxHealthBoss = 300;
    /** Sănătatea curentă a Goblin Boss-ului. */
    private int currentHealthBoss;
    /** Daunele provocate de atacul Goblin Boss-ului. */
    private int attackDamageBoss = 10;

    /** Factorul de scalare specific pentru dimensiunile Goblin Boss-ului. */
    private static final float BOSS_SCALE_FACTOR = 2.0f;

    /** Lățimea de desenare a Goblin Boss-ului, ajustată cu factorul de scalare și scala jocului. */
    private final int DRAW_WIDTH = (int) (Constants.EnemyConstants.GOBLIN_DRAW_WIDTH_DEFAULT * BOSS_SCALE_FACTOR * Game.SCALE);
    /** Înălțimea de desenare a Goblin Boss-ului, ajustată cu factorul de scalare și scala jocului. */
    private final int DRAW_HEIGHT = (int) (Constants.EnemyConstants.GOBLIN_DRAW_HEIGHT_DEFAULT * BOSS_SCALE_FACTOR * Game.SCALE);

    /** Decalajul pe axa X pentru desenarea sprite-ului Goblin Boss-ului. */
    private float xDrawOffset = Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_X * BOSS_SCALE_FACTOR;
    /** Decalajul pe axa Y pentru desenarea sprite-ului Goblin Boss-ului. */
    private float yDrawOffset = Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_Y * BOSS_SCALE_FACTOR;

    /** Direcția curentă a Goblin Boss-ului (stânga sau dreapta). */
    protected int direction = Enemy_Animation_Rows.Directions.LEFT;
    /** Datele nivelului curent, folosite pentru coliziuni și navigație. */
    private int[][] levelData;
    /** Referință la starea de joc "Playing". */
    private Playing playing;

    /** Enumerare pentru stările de acțiune posibile ale Goblin Boss-ului. */
    private enum ActionState { IDLE, DETECTED, CHASING, PREPARING_ATTACK, ATTACKING_MELEE, ATTACKING_RUN_SLASH, REPOSITIONING_WALK, REPOSITIONING_SLIDE, HURT, DYING }
    /** Starea de acțiune curentă a Goblin Boss-ului. */
    private ActionState currentActionState = ActionState.IDLE;

    /** Indicator dacă jucătorul a fost detectat de Goblin Boss. */
    private boolean playerDetected = false;
    /** Distanța la care Goblin Boss-ul poate detecta jucătorul. */
    private float detectionRange = 450f * Game.SCALE;
    /** Distanța vizuală a Goblin Boss-ului. */
    private float sightRange = 500f * Game.SCALE;
    /** Distanța de la care Goblin Boss-ul poate efectua un atac melee. */
    private float meleeAttackRange = hitbox.width * 0.7f;

    /** Viteza de mers a Goblin Boss-ului. */
    private float walkSpeed = 0.6f * Game.SCALE;
    /** Viteza de alergare a Goblin Boss-ului. */
    private float runSpeed = 1.8f * Game.SCALE;
    /** Viteza de alunecare (slide) a Goblin Boss-ului. */
    private float slideSpeed = 3.0f * Game.SCALE;

    /** Cronometru pentru durata acțiunilor curente. */
    private int actionTimer = 0;
    /** Cronometru pentru cooldown-ul dintre acțiuni. */
    private int actionCooldown = 0;
    /** Durata minimă a stării IDLE. */
    private final int IDLE_DURATION_MIN = 60;
    /** Durata maximă a stării IDLE. */
    private final int IDLE_DURATION_MAX = 120;
    /** Durata pregătirii unui atac. */
    private final int PREPARE_ATTACK_DURATION = 45;
    /** Cooldown-ul maxim după un atac. */
    private final int ATTACK_COOLDOWN_MAX = 90;
    /** Cooldown-ul maxim după o repoziționare. */
    private final int REPOSITION_COOLDOWN_MAX = 75;

    /** Indicator dacă Goblin Boss-ul execută o acțiune în prezent. */
    private boolean isPerformingAction = false;
    /** Frame-ul specific din animația de atac la care se verifică aplicarea daunelor. */
    private int attackCheckFrame;
    /** Indicator dacă daunele au fost aplicate în timpul atacului curent. */
    private boolean attackDamageAppliedThisAttack;

    /** Hitbox-ul curent pentru atacurile melee. */
    private Rectangle2D.Float currentMeleeHitbox;
    /** Coordonata X țintă pentru mișcările de repoziționare. */
    private float targetX;

    /**
     * Calculează distanța euclidiană între centrele a două dreptunghiuri.
     * @param r1 Primul dreptunghi.
     * @param r2 Al doilea dreptunghi.
     * @return Distanța dintre centrele celor două dreptunghiuri.
     */
    private float getDistance(Rectangle2D.Float r1, Rectangle2D.Float r2) {
        float dx = (r1.x + r1.width / 2f) - (r2.x + r2.width / 2f);
        float dy = (r1.y + r1.height / 2f) - (r2.y + r2.height / 2f);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculează lățimea hitbox-ului pentru Goblin Boss.
     * Aceasta se bazează pe dimensiunile sprite-ului sursă și factorii de scalare.
     * @return Lățimea calculată a hitbox-ului.
     */
    private static int calculateBossHitboxWidth() {
        final float srcSpriteWidth = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT;
        final float srcHitboxWidthProportion = srcSpriteWidth - 55f;
        final float bossDrawWidthUnscaled = Constants.EnemyConstants.GOBLIN_DRAW_WIDTH_DEFAULT * BOSS_SCALE_FACTOR;
        final float scaleX = bossDrawWidthUnscaled / srcSpriteWidth;
        final float hitboxWidthDefault = srcHitboxWidthProportion * scaleX;
        return (int) (hitboxWidthDefault * Game.SCALE);
    }

    /**
     * Calculează înălțimea hitbox-ului pentru Goblin Boss.
     * Aceasta se bazează pe dimensiunile sprite-ului sursă și factorii de scalare.
     * @return Înălțimea calculată a hitbox-ului.
     */
    private static int calculateBossHitboxHeight() {
        final float srcSpriteHeight = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT;
        final float srcHitboxHeightProportion = srcSpriteHeight - 30f;
        final float bossDrawHeightUnscaled = Constants.EnemyConstants.GOBLIN_DRAW_HEIGHT_DEFAULT * BOSS_SCALE_FACTOR;
        final float scaleY = bossDrawHeightUnscaled / srcSpriteHeight;
        final float hitboxHeightDefault = srcHitboxHeightProportion * scaleY;
        return (int) (hitboxHeightDefault * Game.SCALE);
    }

    /**
     * Constructor pentru clasa GoblinBoss.
     * Inițializează Goblin Boss-ul cu o poziție specifică și o referință la starea de joc "Playing".
     * Setează sănătatea, încarcă animațiile și inițializează hitbox-ul.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param playing Referință la obiectul Playing, pentru interacțiuni cu starea jocului.
     */
    public GoblinBoss(float x, float y, Playing playing) {
        super(x, y, calculateBossHitboxWidth(), calculateBossHitboxHeight(), -1); // -1 for enemyType, as it's a boss
        this.playing = playing;
        
        this.currentHealthBoss = maxHealthBoss;
        loadAnimations();
        
        // Calculate visual top-left for sprite placement based on desired hitbox bottom alignment
        float spriteVisualTopY = y + Game.TILES_SIZE - DRAW_HEIGHT; // Align bottom of DRAW_HEIGHT with tile bottom
        float spriteVisualLeftX = x; // Align left of DRAW_WIDTH with tile left
        // Calculate final hitbox position based on sprite's visual top-left and draw offsets
        float finalBossHitboxX = spriteVisualLeftX + this.xDrawOffset;
        float finalBossHitboxY = spriteVisualTopY + this.yDrawOffset;
        
        this.initHitbox(finalBossHitboxX, finalBossHitboxY, this.hitbox.width, this.hitbox.height);
        
        this.enemyState = Enemy_Animation_Rows.IDLE.getRowIndex();
        this.drawHitbox = false; // Set to false to hide hitbox
        this.meleeAttackRange = this.hitbox.width * 0.7f; // Initialize after hitbox is set
    }

    /**
     * Încarcă animațiile pentru Goblin Boss din sprite sheet.
     * Sprite sheet-ul este încărcat folosind clasa LoadSave.
     * Animațiile sunt stocate în matricile `animations` și `flippedAnimations`.
     */
    private void loadAnimations() {
        BufferedImage sheet = LoadSave.getSpriteAtlas(LoadSave.GOBLIN_BOSS_SPRITESHEET);
        if (sheet == null) {
            System.err.println("Failed to load Goblin Boss sprite sheet!");
            return;
        }
        int spriteSheetCellWidth = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT;
        int spriteSheetCellHeight = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT;
        animations = new BufferedImage[Enemy_Animation_Rows.values().length][];
        flippedAnimations = new BufferedImage[Enemy_Animation_Rows.values().length][];
        for (Enemy_Animation_Rows animRow : Enemy_Animation_Rows.values()) {
            int rowIndex = animRow.getRowIndex();
            int frameCount = animRow.getFrameCount();
            if (rowIndex >= Enemy_Animation_Rows.values().length || frameCount == 0) continue; // Skip if no frames or invalid index
            animations[rowIndex] = new BufferedImage[frameCount];
            flippedAnimations[rowIndex] = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int ex = i * spriteSheetCellWidth;
                int ey = rowIndex * spriteSheetCellHeight;
                if (ey + spriteSheetCellHeight <= sheet.getHeight() && ex + spriteSheetCellWidth <= sheet.getWidth()) {
                    animations[rowIndex][i] = sheet.getSubimage(ex, ey, spriteSheetCellWidth, spriteSheetCellHeight);
                    flippedAnimations[rowIndex][i] = flipImage(animations[rowIndex][i]);
                }
            }
        }
    }

    /**
     * Inversează (flip) o imagine pe orizontală.
     * @param image Imaginea care trebuie inversată.
     * @return Imaginea inversată sau null dacă imaginea de intrare este null.
     */
    private static BufferedImage flipImage(BufferedImage image) {
        if (image == null) return null;
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                flipped.setRGB(image.getWidth() - 1 - x, y, image.getRGB(x, y));
            }
        }
        return flipped;
    }

    /**
     * Actualizează starea Goblin Boss-ului.
     * Aceasta include actualizarea animației, cronometrelor de acțiune, detectarea jucătorului,
     * gestionarea mașinii de stări, aplicarea gravitației și actualizarea hitbox-ului.
     * @param player Jucătorul, pentru interacțiuni și detectare.
     * @param lvlData Datele nivelului curent, pentru coliziuni.
     */
    public void update(Player player, int[][] lvlData) {
        this.levelData = lvlData;
        updateAnimationTick();
        actionTimer++;
        
        if (currentActionState == ActionState.HURT) {
            if (actionTimer >= (Enemy_Animation_Rows.HURT.getFrameCount() * aniSpeed)) { // aniSpeed is ticks per frame
                 currentActionState = ActionState.IDLE;
                 isPerformingAction = false;
                 actionCooldown = IDLE_DURATION_MIN; // Cooldown before next action
                 actionTimer = 0;
            }
            return; // Do not process other logic while hurt
        }
        if (currentActionState == ActionState.DYING) {
            if (actionTimer >= (Enemy_Animation_Rows.DYING.getFrameCount() * aniSpeed)) {
                isActive = false; // Mark as inactive after dying animation finishes
            }
            if (!isActive) {
                System.out.println("Goblin Boss is fully deactivated after dying animation.");
            }
            return; // Do not process other logic while dying or inactive
        }
        if (!isActive) return;

        updatePlayerDetection(player);
        handleStateMachine(player);  
        applyGravity();
        updateHitbox(); // Ensure hitbox position is correct if entity moves by other means
    }

    /**
     * Actualizează starea de detectare a jucătorului.
     * Verifică dacă jucătorul se află în raza de vizualizare și setează direcția boss-ului către jucător.
     * @param player Jucătorul care trebuie detectat.
     */
    private void updatePlayerDetection(Player player) {
        float distanceToPlayer = getDistance(this.hitbox, player.getHitbox());
        if (distanceToPlayer <= sightRange) {
            playerDetected = true;
            // Determine direction based on player's position relative to the boss
            if (player.getHitbox().getCenterX() < this.hitbox.getCenterX()) { // Player is to the left
                this.direction = Enemy_Animation_Rows.Directions.LEFT;
            } else { // Player is to the right
                this.direction = Enemy_Animation_Rows.Directions.RIGHT;
            }
        } else {
            playerDetected = false;
        }
    }

    /**
     * Gestionează mașina de stări a Goblin Boss-ului.
     * Determină acțiunile curente și tranzițiile între stări pe baza detectării jucătorului,
     * cooldown-urilor și altor condiții.
     * @param player Jucătorul, pentru a lua decizii bazate pe poziția și starea acestuia.
     */
    private void handleStateMachine(Player player) {
        if (actionCooldown > 0) {
            actionCooldown--;
        }

        switch (currentActionState) {
            case IDLE:
                setBossAnimation(Enemy_Animation_Rows.IDLE);
                if (playerDetected) {
                    currentActionState = ActionState.DETECTED;
                    actionTimer = 0;
                } else if (actionTimer > IDLE_DURATION_MAX && actionCooldown <= 0) { // Only if not in cooldown from another action
                    decideIdleAction();
                    actionTimer = 0; // Reset timer for the new idle action or state
                }
                break;
            case DETECTED: // Player is in sight, boss is aware
                setBossAnimation(Enemy_Animation_Rows.IDLE_NO_BLINK); // Or a specific "alert" animation
                if (!playerDetected) { // Player moved out of sight
                    currentActionState = ActionState.IDLE;
                    actionTimer = 0;
                    break;
                }
                if (actionTimer > 30) { // Short delay before deciding action
                     decideNextAction(player); // Decide whether to chase, attack, or reposition
                }
                break;
            case CHASING:
                setBossAnimation(Enemy_Animation_Rows.RUNNING);
                moveTowardsPlayer(player.getHitbox(), runSpeed);
                if (getDistance(this.hitbox, player.getHitbox()) <= meleeAttackRange && actionCooldown <= 0) { // Close enough to attack and not in cooldown
                    currentActionState = ActionState.PREPARING_ATTACK;
                    actionTimer = 0;
                    isPerformingAction = true; // Boss is now busy with an attack sequence
                    attackDamageAppliedThisAttack = false;
                }
                break;
            case PREPARING_ATTACK:
                setBossAnimation(Enemy_Animation_Rows.IDLE_NO_BLINK); // Or a "charge up" animation
                if (actionTimer > PREPARE_ATTACK_DURATION) {
                    if (getDistance(this.hitbox, player.getHitbox()) <= meleeAttackRange) { // Still in range
                        initiateMeleeAttack(); // Transition to actual attack
                    } else { // Player moved out of range during preparation
                        currentActionState = ActionState.CHASING;
                        isPerformingAction = false;
                    }
                }
                break;
            case ATTACKING_MELEE: // Handles standard melee (slash/kick)
                // Animation handles movement if any (e.g., lunge). Damage applied at specific frame.
                if (actionTimer >= (Enemy_Animation_Rows.values()[this.enemyState].getFrameCount() * aniSpeed)) { // Attack animation finished
                    currentActionState = ActionState.IDLE;
                    isPerformingAction = false;
                    actionCooldown = ATTACK_COOLDOWN_MAX;
                    actionTimer = 0; // Reset for next state
                } else {
                    // Check for damage application at the correct animation frame
                    if (!attackDamageAppliedThisAttack && aniIndex == attackCheckFrame) {
                        applyMeleeDamage(player); // Apply damage once per attack
                    }
                }
                break;
            case ATTACKING_RUN_SLASH:
                setBossAnimation(Enemy_Animation_Rows.RUN_SLASING);
                float runSlashMovement = runSpeed * 0.6f; // Slower movement during run_slash
                if (direction == Enemy_Animation_Rows.Directions.RIGHT) {
                    if (HelpMethods.canMoveHere(hitbox.x + runSlashMovement, hitbox.y, hitbox.width, hitbox.height, levelData))
                        hitbox.x += runSlashMovement;
                } else {
                    if (HelpMethods.canMoveHere(hitbox.x - runSlashMovement, hitbox.y, hitbox.width, hitbox.height, levelData))
                        hitbox.x -= runSlashMovement;
                }
                updateCurrentMeleeHitbox(); // Update hitbox as boss moves

                if (actionTimer >= (Enemy_Animation_Rows.RUN_SLASING.getFrameCount() * aniSpeed)) {
                    currentActionState = ActionState.IDLE;
                    isPerformingAction = false;
                    actionCooldown = ATTACK_COOLDOWN_MAX;
                    actionTimer = 0;
                } else {
                    // Run Slash has a wider window for damage application due to movement
                    if (!attackDamageAppliedThisAttack && aniIndex >= 3 && aniIndex <= 9) { // Example frames for damage
                        applyMeleeDamage(player);
                    }
                }
                break;
            case REPOSITIONING_SLIDE:
                handleRepositionMovementLogic(slideSpeed, Enemy_Animation_Rows.SLIDING, (Enemy_Animation_Rows.SLIDING.getFrameCount() * aniSpeed * 1.5f));
                break;
            case REPOSITIONING_WALK:
                handleRepositionMovementLogic(walkSpeed, Enemy_Animation_Rows.WALKING, (Enemy_Animation_Rows.WALKING.getFrameCount() * aniSpeed * 2.0f));
                break;
        }
    }
    
    /**
     * Decide următoarea acțiune a boss-ului când jucătorul este detectat.
     * Aceasta poate fi un atac, urmărirea jucătorului sau repoziționarea.
     * @param player Jucătorul, pentru a evalua distanța și a lua decizii.
     */
    private void decideNextAction(Player player) {
        if (actionCooldown > 0) return;

        float distance = getDistance(this.hitbox, player.getHitbox());
        double roll = Math.random(); // For probabilistic decision making
        float decisionRangeForRunSlash = meleeAttackRange * 2.5f; // Extended range for considering run_slash

        if (distance <= meleeAttackRange) { // Player is very close
            if (roll < 0.6) { // 60% chance for standard melee
                currentActionState = ActionState.PREPARING_ATTACK;
            } else if (roll < 0.85) { // 25% chance for run_slash (if close, might be to create space or quick hit)
                initiateRunSlashAttack();
            } else { // 15% chance to reposition (e.g., if too crowded)
                initiateReposition(true); // Prefer fast slide if close
            }
        } else if (distance <= decisionRangeForRunSlash) { // Player is at mid-range
            if (roll < 0.5) { // 50% chance for run_slash to close gap
                initiateRunSlashAttack();
            } else if (roll < 0.8) { // 30% chance to chase
                 currentActionState = ActionState.CHASING;
            } else { // 20% chance to reposition
                initiateReposition(true); // Prefer fast slide
            }
        } else if (distance <= detectionRange) { // Player is at longer range but detected
            if (roll < 0.7) { // 70% chance to chase
                currentActionState = ActionState.CHASING;
            } else { // 30% chance to reposition (e.g., to a better vantage point or to avoid being cornered)
                initiateReposition(false); // Walk reposition is fine
            }
        } else { // Player out of detection range (should ideally be handled by DETECTED state, but as a fallback)
            currentActionState = ActionState.IDLE;
        }
        actionTimer = 0;
        isPerformingAction = true; // Most decisions here lead to an action
        attackDamageAppliedThisAttack = false; // Reset for any new attack
    }

    /**
     * Inițiază un atac melee standard (lovitură sau picior).
     * Setează animația corespunzătoare și starea de atac.
     */
    private void initiateMeleeAttack() {
        double roll = Math.random();
        if (roll < 0.6) {
            setBossAnimation(Enemy_Animation_Rows.SLASHING);
            attackCheckFrame = Enemy_Animation_Rows.SLASHING.getFrameCount() / 2; // Damage frame for slashing
        } else {
            setBossAnimation(Enemy_Animation_Rows.KICKING);
            attackCheckFrame = Enemy_Animation_Rows.KICKING.getFrameCount() / 2; // Damage frame for kicking
        }
        currentActionState = ActionState.ATTACKING_MELEE;
        actionTimer = 0; // Reset timer for the attack animation
        actionCooldown = ATTACK_COOLDOWN_MAX;
        updateCurrentMeleeHitbox();
    }

    /**
     * Inițiază un atac de tip "run slash".
     * Setează animația și starea corespunzătoare.
     */
    private void initiateRunSlashAttack() {
        setBossAnimation(Enemy_Animation_Rows.RUN_SLASING);
        currentActionState = ActionState.ATTACKING_RUN_SLASH;
        actionTimer = 0;
        actionCooldown = (int)(ATTACK_COOLDOWN_MAX * 1.2); // Slightly longer cooldown for a stronger move
        attackDamageAppliedThisAttack = false; // Ensure damage can be applied
    }
    
    /**
     * Inițiază o acțiune de repoziționare.
     * Decide dacă repoziționarea va fi o alunecare rapidă sau un mers.
     * @param preferFastSlide Indică dacă alunecarea rapidă este preferată.
     */
    private void initiateReposition(boolean preferFastSlide) {
        double roll = Math.random();
        if (preferFastSlide || roll < 0.6) { // 60% chance to slide, or if preferred
            initiateSlideReposition();
        } else { // 40% chance to walk
            initiateWalkReposition();
        }
    }
    
    /**
     * Inițiază o repoziționare prin alunecare (slide).
     * Calculează o poziție țintă și setează starea și animația corespunzătoare.
     */
    private void initiateSlideReposition() {
        setBossAnimation(Enemy_Animation_Rows.SLIDING); // Make sure this animation exists
        currentActionState = ActionState.REPOSITIONING_SLIDE;
        actionTimer = 0;
        actionCooldown = REPOSITION_COOLDOWN_MAX;
        isPerformingAction = true;
        float slideDistance = (float)(Math.random() * 100 + 50) * Game.SCALE; // Random slide distance
        // Decide direction of slide: 50% chance to slide further in current direction, 50% to slide back
        if (Math.random() < 0.5) { // Slide further or away from player
            if (direction == Enemy_Animation_Rows.Directions.LEFT) {
                targetX = hitbox.x - slideDistance;
            } else {
                targetX = hitbox.x + slideDistance;
            }
        } else { // Slide towards player or reverse direction
             if (direction == Enemy_Animation_Rows.Directions.LEFT) {
                targetX = hitbox.x + slideDistance / 2; // Shorter slide if reversing
            } else {
                targetX = hitbox.x - slideDistance / 2;
            }
        }
        // Clamp targetX to level bounds
        float levelPixelWidth = 0;
        if (levelData != null && levelData.length > 0 && levelData[0] != null) {
            levelPixelWidth = levelData[0].length * Game.TILES_SIZE;
        } else {
            levelPixelWidth = Game.GAME_WIDTH; // Fallback if levelData is not available
        }
        float levelEdgePadding = hitbox.width / 2; // Prevent getting stuck at edges
        if (targetX < levelEdgePadding) {
            targetX = levelEdgePadding;
        } else if (targetX + hitbox.width > levelPixelWidth - levelEdgePadding) {
            targetX = levelPixelWidth - levelEdgePadding - hitbox.width;
        }
        System.out.println("Goblin Boss initiates REPOSITIONING (SLIDING) towards " + targetX);
    }

    /**
     * Inițiază o repoziționare prin mers.
     * Calculează o poziție țintă și setează starea și animația corespunzătoare.
     */
    private void initiateWalkReposition() {
        setBossAnimation(Enemy_Animation_Rows.WALKING);
        currentActionState = ActionState.REPOSITIONING_WALK;
        actionTimer = 0;
        actionCooldown = REPOSITION_COOLDOWN_MAX;
        isPerformingAction = true;
        float walkDistance = (float)(Math.random() * 80 + 40) * Game.SCALE; // Random walk distance
        // Similar logic to slide for direction
        if (Math.random() < 0.6) { // 60% chance to walk further or away
            if (direction == Enemy_Animation_Rows.Directions.LEFT) {
                targetX = hitbox.x - walkDistance;
            } else {
                targetX = hitbox.x + walkDistance;
            }
        } else { // 40% chance to walk towards or reverse
             if (direction == Enemy_Animation_Rows.Directions.LEFT) {
                targetX = hitbox.x + walkDistance / 1.5f; // Shorter walk if reversing
            } else {
                targetX = hitbox.x - walkDistance / 1.5f;
            }
        }
        // Clamp targetX to level bounds
        float levelPixelWidth = 0;
        if (levelData != null && levelData.length > 0 && levelData[0] != null) {
            levelPixelWidth = levelData[0].length * Game.TILES_SIZE;
        } else {
            levelPixelWidth = Game.GAME_WIDTH; // Fallback
        }
        float levelEdgePadding = hitbox.width / 2;
        if (targetX < levelEdgePadding) targetX = levelEdgePadding;
        else if (targetX + hitbox.width > levelPixelWidth - levelEdgePadding) {
            targetX = levelPixelWidth - levelEdgePadding - hitbox.width;
        }
        System.out.println("Goblin Boss initiates REPOSITIONING (WALKING) towards " + targetX);
    }

    /**
     * Gestionează logica de mișcare pentru repoziționare (mers sau alunecare).
     * Mută boss-ul către `targetX` cu viteza specificată și gestionează timeout-ul acțiunii.
     * @param speed Viteza de mișcare.
     * @param anim Animația care trebuie redată în timpul mișcării.
     * @param timeoutTicks Numărul de tick-uri după care acțiunea de repoziționare expiră.
     */
    private void handleRepositionMovementLogic(float speed, Enemy_Animation_Rows anim, float timeoutTicks) {
        setBossAnimation(anim);
        float move = 0;
        if (hitbox.x < targetX) {
            move = speed;
            if (hitbox.x + move > targetX) { // Don't overshoot
                move = targetX - hitbox.x; // Move exactly to target
            }
        } else if (hitbox.x > targetX) {
            move = -speed;
            if (hitbox.x + move < targetX) { // Don't overshoot
                move = targetX - hitbox.x; // Move exactly to target
            }
        }

        if (move != 0 && HelpMethods.canMoveHere(hitbox.x + move, hitbox.y, hitbox.width, hitbox.height, levelData)) {
            hitbox.x += move;
        } else { // Cannot move or reached target (move is 0)
            isPerformingAction = false; // Stop the action if stuck or target reached
        }
        
        // Check if target reached or action timed out
        if (!isPerformingAction || actionTimer > timeoutTicks) { // Reached target (isPerformingAction became false) or timed out
             currentActionState = ActionState.IDLE;
             actionCooldown = IDLE_DURATION_MIN;
             isPerformingAction = false;
             actionTimer = 0;
        }
    }
    
    /**
     * Decide o acțiune aleatorie pentru starea IDLE.
     * Boss-ul poate să se întoarcă, să meargă puțin sau să rămână pe loc.
     */
    private void decideIdleAction() {
        if (actionCooldown > 0) return; // Still in cooldown from a previous major action
        double roll = Math.random();
        if (roll < 0.4) { // 40% chance to turn around
            this.direction *= -1; // Flip direction
            setBossAnimation(Enemy_Animation_Rows.IDLE); // Reset to idle animation (might trigger frame reset)
            actionCooldown = IDLE_DURATION_MIN / 2; // Short cooldown for a minor action
            System.out.println("Goblin Boss: Idle turn.");
        } else if (roll < 0.8) { // 40% chance to walk a bit (initiate a short reposition)
            initiateWalkReposition(); // This will set its own cooldown and state
            System.out.println("Goblin Boss: Idle walk.");
        } else { // 20% chance to just stay idle longer
            setBossAnimation(Enemy_Animation_Rows.IDLE);
            actionCooldown = IDLE_DURATION_MIN / 3; // Very short cooldown, effectively extending idle
        }
    }
    
    /**
     * Mută Goblin Boss-ul către jucător.
     * @param playerHitbox Hitbox-ul jucătorului, pentru a determina direcția.
     * @param speed Viteza cu care se mișcă boss-ul.
     */
    private void moveTowardsPlayer(Rectangle2D.Float playerHitbox, float speed) {
        float targetPlayerX = (float)playerHitbox.getCenterX();
        float currentBossX = (float)hitbox.getCenterX();
        // Move only if not already very close, to prevent jittering
        if (Math.abs(currentBossX - targetPlayerX) > 5 * Game.SCALE) { // Threshold to stop
            if (currentBossX < targetPlayerX) {
                hitbox.x += speed;
                this.direction = Enemy_Animation_Rows.Directions.RIGHT;
            } else {
                hitbox.x -= speed;
                this.direction = Enemy_Animation_Rows.Directions.LEFT;
            }
        }
        // Clamp position to level bounds
        if (levelData != null && levelData.length > 0 && levelData[0] != null) {
            float levelPixelWidth = levelData[0].length * Game.TILES_SIZE;
            if (hitbox.x < 0) hitbox.x = 0;
            if (hitbox.x + hitbox.width > levelPixelWidth) hitbox.x = levelPixelWidth - hitbox.width;
        }
    }
    
    /**
     * Actualizează hitbox-ul pentru atacurile melee.
     * Poziția și dimensiunea hitbox-ului de atac depind de direcția boss-ului.
     */
    private void updateCurrentMeleeHitbox() {
        float hx, hy, hw, hh;
        // Define hitbox relative to the main hitbox of the boss
        hw = hitbox.width * 0.6f; // Example: 60% of boss width
        hh = hitbox.height * 0.8f; // Example: 80% of boss height
        if (direction == Enemy_Animation_Rows.Directions.RIGHT) {
            // Position it to the right of the boss's center
            hx = hitbox.x + hitbox.width * 0.5f; // Starts from middle-right of boss
        } else { // Facing left
            // Position it to the left of the boss's center
            hx = hitbox.x - hitbox.width * 0.1f - hw; // Starts from middle-left of boss, shifted by its own width
        }
        hy = hitbox.y + hitbox.height * 0.1f; // Slightly offset from top
        currentMeleeHitbox = new Rectangle2D.Float(hx, hy, hw, hh);
    }

    /**
     * Aplică daune jucătorului dacă atacul melee îl lovește.
     * Verifică intersecția dintre hitbox-ul de atac și hitbox-ul jucătorului.
     * Aplică și un efect de knockback jucătorului.
     * @param player Jucătorul care poate fi lovit.
     */
    private void applyMeleeDamage(Player player) {
        if (currentMeleeHitbox != null && currentMeleeHitbox.intersects(player.getHitbox())) {
            player.takeDamage(attackDamageBoss);
            System.out.println("Goblin Boss melee hit player! Player health: " + player.getCurrentHealth());
            attackDamageAppliedThisAttack = true; // Prevent multiple damage applications from one attack swing
            // Apply knockback
            float knockbackX = (this.direction == Enemy_Animation_Rows.Directions.RIGHT) ? 6f * Game.SCALE : -6f * Game.SCALE;
            float knockbackY = -3f * Game.SCALE; // Slight upward knockback
            player.applyKnockback(knockbackX, knockbackY);
            System.out.println("Applied knockback to player from Goblin Boss.");
        }
    }
    
    /**
     * Setează animația curentă a Goblin Boss-ului.
     * Resetează indexul și tick-ul animației dacă noua animație este diferită
     * sau dacă boss-ul nu execută o acțiune (pentru a reseta animațiile IDLE).
     * @param animType Tipul de animație (rândul din sprite sheet) care trebuie setat.
     */
    private void setBossAnimation(Enemy_Animation_Rows animType) {
        int newAnimationState = animType.getRowIndex();
        if (this.enemyState != newAnimationState || !isPerformingAction) { // Ensure animation resets if not performing action
            this.enemyState = newAnimationState;
            this.aniIndex = 0;
            this.aniTick = 0;
        }
    }

    /**
     * Aplică gravitația Goblin Boss-ului dacă acesta nu se află pe o suprafață solidă.
     */
    private void applyGravity() {
        if (this.levelData != null && !HelpMethods.isEntityOnFloor(hitbox, this.levelData)) {
            hitbox.y += (float)(2.0f * Game.SCALE); // Explicit cast to float
        }
    }
    
    /**
     * Actualizează poziția hitbox-ului.
     * În implementarea curentă, metodele de mișcare modifică direct `hitbox.x` și `hitbox.y`,
     * deci această metodă ar putea fi goală sau folosită pentru ajustări fine dacă este necesar.
     */
    private void updateHitbox() {
        // Entity x,y is top-left of sprite usually. Hitbox might be offset.
        // This method ensures hitbox.x, hitbox.y are updated if entity's main x,y are used for positioning.
        // However, our movement methods directly modify hitbox.x, hitbox.y.
    }

    /**
     * Setează datele nivelului pentru Goblin Boss.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void setLevelData(int[][] levelData) {
        this.levelData = levelData;
    }

    /**
     * Randează Goblin Boss-ul pe ecran.
     * Desenează animația curentă la poziția corectă, luând în considerare decalajul nivelului.
     * Poate desena și hitbox-ul dacă `drawHitbox` este true.
     * @param g Contextul grafic pentru desenare.
     * @param xLvlOffset Decalajul pe axa X al nivelului, pentru scrolling.
     */
    public void render(Graphics g, int xLvlOffset) {
        if (!isActive) return; // Don't render if not active (e.g., after dying animation)
        if (animations == null || enemyState < 0 || enemyState >= animations.length || animations[enemyState] == null) {
            //System.err.println("GoblinBoss: Animation data missing or invalid state for rendering. State: " + enemyState);
            return; // Safety check
        }
        BufferedImage[] currentAnimationSet = (this.direction == Enemy_Animation_Rows.Directions.LEFT) ? flippedAnimations[enemyState] : animations[enemyState];
        if (currentAnimationSet != null && aniIndex >= 0 && aniIndex < currentAnimationSet.length && currentAnimationSet[aniIndex] != null) {
            float drawX = hitbox.x - xDrawOffset - xLvlOffset;
            float drawY = hitbox.y - yDrawOffset;
            g.drawImage(currentAnimationSet[aniIndex], (int)drawX, (int)drawY, DRAW_WIDTH, DRAW_HEIGHT, null);
            if (drawHitbox) {
                drawHitbox(g, xLvlOffset); // Method from Enemy class
            }
        } else {
            //System.err.println("GoblinBoss: Current animation frame is null or index out of bounds. AniIndex: " + aniIndex + ", State: " + enemyState);
        }
    }
    
    /**
     * Verifică dacă Goblin Boss-ul este în viață.
     * @return true dacă sănătatea curentă este mai mare decât 0, false altfel.
     */
    public boolean isAlive() {
        return currentHealthBoss > 0;
    }

    /**
     * Aplică daune Goblin Boss-ului.
     * Reduce sănătatea curentă și gestionează tranziția la starea HURT sau DYING.
     * @param damage Cantitatea de daune primite.
     */
    public void takeDamage(int damage) {
        currentHealthBoss -= damage;
        if (currentHealthBoss <= 0) {
            currentHealthBoss = 0;
            isActive = false; // Mark as inactive immediately for logic, animation will play out
            setBossAnimation(Enemy_Animation_Rows.DYING);
            currentActionState = ActionState.DYING;
            isPerformingAction = true; // To ensure dying animation plays
            actionTimer = 0; // Reset timer for dying animation
            System.out.println("Goblin Boss defeated!");
        } else {
            setBossAnimation(Enemy_Animation_Rows.HURT);
            currentActionState = ActionState.HURT;
            isPerformingAction = true; // To ensure hurt animation plays
            actionTimer = 0; // Reset timer for hurt animation
            actionCooldown = IDLE_DURATION_MIN; // Give boss a moment after being hit
            System.out.println("Goblin Boss took " + damage + " damage. Health: " + currentHealthBoss);
        }
    }
    
    /**
     * Returnează daunele de atac ale Goblin Boss-ului.
     * @return Daunele de atac.
     */
    public int getAttackDamage() {
        return attackDamageBoss;
    }

    /**
     * Returnează sănătatea curentă a Goblin Boss-ului.
     * @return Sănătatea curentă.
     */
    public int getCurrentHealth() {
        return currentHealthBoss;
    }

    /**
     * Returnează sănătatea maximă a Goblin Boss-ului.
     * @return Sănătatea maximă.
     */
    public int getMaxHealth() {
        return maxHealthBoss;
    }
}
