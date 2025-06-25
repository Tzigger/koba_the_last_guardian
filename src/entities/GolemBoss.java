package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Color; // Added import for Color

import main.Game;
import utilz.Constants;
import utilz.Enemy_Animation_Rows;
import utilz.LoadSave;
import utilz.HelpMethods;
import gamestates.Playing;

/**
 * Reprezintă entitatea Golem Boss în joc.
 * Această clasă extinde clasa Enemy și definește comportamentul specific
 * și atributele pentru Golem Boss, un inamic puternic cu atacuri devastatoare.
 * Include gestionarea animațiilor, stărilor de acțiune, logicii de atac și
 * interacțiunii cu jucătorul.
 */
public class GolemBoss extends Enemy {

    /** Matrice bidimensională pentru stocarea animațiilor normale ale boss-ului. */
    private BufferedImage[][] animations;
    /** Matrice bidimensională pentru stocarea animațiilor inversate (flipped) ale boss-ului. */
    private BufferedImage[][] flippedAnimations;

    /** Sănătatea maximă a Golem Boss-ului. */
    private int maxHealthBoss = 450;
    /** Sănătatea curentă a Golem Boss-ului. */
    private int currentHealthBoss;
    /** Daunele provocate de atacul Golem Boss-ului. */
    private int attackDamageBoss = 20; // Golem hits harder

    /** Factorul de scalare specific pentru dimensiunile Golem Boss-ului. */
    private static final float BOSS_SCALE_FACTOR = 3.0f;

    /** Lățimea de desenare a Golem Boss-ului, ajustată cu factorul de scalare și scala jocului. */
    private final int DRAW_WIDTH = (int) (Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT * BOSS_SCALE_FACTOR * Game.SCALE);
    /** Înălțimea de desenare a Golem Boss-ului, ajustată cu factorul de scalare și scala jocului. */
    private final int DRAW_HEIGHT = (int) (Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT * BOSS_SCALE_FACTOR * Game.SCALE);
    /** Decalajul pe axa X pentru desenarea sprite-ului Golem Boss-ului. */
    private float xDrawOffset = Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_X * BOSS_SCALE_FACTOR;
    /** Decalajul pe axa Y pentru desenarea sprite-ului Golem Boss-ului. */
    private float yDrawOffset = Constants.EnemyConstants.GOBLIN_DRAW_OFFSET_Y * BOSS_SCALE_FACTOR;

    /** Direcția curentă a Golem Boss-ului (stânga sau dreapta). */
    protected int direction = Enemy_Animation_Rows.Directions.LEFT;
    /** Datele nivelului curent, folosite pentru coliziuni și navigație. */
    private int[][] levelData;
    /** Referință la starea de joc "Playing". */
    private Playing playing;

    /** Enumerare pentru stările de acțiune specifice Golem Boss-ului. */
    private enum ActionState {
        IDLE, DETECTED,
        WALKING_TOWARDS_PLAYER, CHASING, // Chasing for faster run
        PREPARING_ATTACK, ATTACKING_MELEE, // Generic melee, specific animation set in initiate
        HURT, DYING
    }
    /** Starea de acțiune curentă a Golem Boss-ului. */
    private ActionState currentActionState = ActionState.IDLE;

    /** Indicator dacă jucătorul a fost detectat de Golem Boss. */
    private boolean playerDetected = false;
    /** Distanța la care Golem Boss-ul poate detecta jucătorul. */
    private float detectionRange = 500f * Game.SCALE; // Golem sees a bit further
    /** Distanța vizuală a Golem Boss-ului. */
    private float sightRange = 600f * Game.SCALE;
    /** Distanța de la care Golem Boss-ul poate efectua un atac melee. */
    private float meleeAttackRange;

    /** Viteza de mers a Golem Boss-ului. */
    private float walkSpeed = 0.4f * Game.SCALE; // Slower walk
    /** Viteza de alergare a Golem Boss-ului. */
    private float runSpeed = 1.2f * Game.SCALE;  // Slower run than GoblinBoss
    
    /** Cronometru pentru durata acțiunilor curente. */
    private int actionTimer = 0;
    /** Cronometru pentru cooldown-ul dintre acțiuni. */
    private int actionCooldown = 0;
    /** Durata minimă a stării IDLE. */
    private final int IDLE_DURATION_MIN = 80;
    /** Durata maximă a stării IDLE. */
    private final int IDLE_DURATION_MAX = 160;
    /** Durata pregătirii unui atac (telegraph). */
    private final int PREPARE_ATTACK_DURATION = 60; // Longer telegraph
    /** Cooldown-ul maxim după un atac. */
    private final int ATTACK_COOLDOWN_MAX = 120; // Longer cooldown
    
    /** Indicator dacă Golem Boss-ul execută o acțiune în prezent. */
    private boolean isPerformingAction = false;
    /** Frame-ul specific din animația de atac la care se verifică aplicarea daunelor. */
    private int attackCheckFrame;
    /** Indicator dacă daunele au fost aplicate în timpul atacului curent. */
    private boolean attackDamageAppliedThisAttack;

    /** Hitbox-ul curent pentru atacurile melee. */
    private Rectangle2D.Float currentMeleeHitbox;

    /**
     * Constructor pentru clasa GolemBoss.
     * Inițializează Golem Boss-ul cu o poziție specifică și o referință la starea de joc "Playing".
     * Setează sănătatea, încarcă animațiile și inițializează hitbox-ul.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param playing Referință la obiectul Playing, pentru interacțiuni cu starea jocului.
     */
    public GolemBoss(float x, float y, Playing playing) {
        super(x, y, calculateBossHitboxWidth(), calculateBossHitboxHeight(), -1); // -1 for enemyType, as it's a boss
        this.playing = playing;
        this.currentHealthBoss = maxHealthBoss;
        loadAnimations();
        
        // Calculează poziția vizuală a sprite-ului pentru alinierea corectă a hitbox-ului
        float spriteVisualTopY = y + Game.TILES_SIZE - DRAW_HEIGHT;
        float spriteVisualLeftX = x;
        float finalBossHitboxX = spriteVisualLeftX + this.xDrawOffset;
        float finalBossHitboxY = spriteVisualTopY + this.yDrawOffset;
        
        this.initHitbox(finalBossHitboxX, finalBossHitboxY, this.hitbox.width, this.hitbox.height);
        
        this.enemyState = Enemy_Animation_Rows.IDLE.getRowIndex();
        this.drawHitbox = false; // Dezactivează desenarea hitbox-ului pentru gameplay normal
        this.meleeAttackRange = this.hitbox.width * 0.8f; // Golemul poate avea o rază de atac puțin mai mare datorită dimensiunii
    }

    /**
     * Calculează lățimea hitbox-ului pentru Golem Boss.
     * Se bazează pe dimensiunile sprite-ului sursă și factorii de scalare.
     * @return Lățimea calculată a hitbox-ului.
     */
    private static int calculateBossHitboxWidth() {
        final float srcSpriteWidth = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT;
        final float srcHitboxWidthProportion = srcSpriteWidth - 55f; // Proporția originală
        final float bossDrawWidthUnscaled = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT * BOSS_SCALE_FACTOR;
        final float scaleX = bossDrawWidthUnscaled / srcSpriteWidth;
        final float hitboxWidthDefault = srcHitboxWidthProportion * scaleX;
        return (int) (hitboxWidthDefault * Game.SCALE);
    }

    /**
     * Calculează înălțimea hitbox-ului pentru Golem Boss.
     * Se bazează pe dimensiunile sprite-ului sursă și factorii de scalare.
     * @return Înălțimea calculată a hitbox-ului.
     */
    private static int calculateBossHitboxHeight() {
        final float srcSpriteHeight = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT;
        final float srcHitboxHeightProportion = srcSpriteHeight - 30f;
        final float bossDrawHeightUnscaled = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT * BOSS_SCALE_FACTOR;
        final float scaleY = bossDrawHeightUnscaled / srcSpriteHeight;
        final float hitboxHeightDefault = srcHitboxHeightProportion * scaleY;
        return (int) (hitboxHeightDefault * Game.SCALE);
    }

    /**
     * Încarcă animațiile pentru Golem Boss din sprite sheet-ul specific.
     * Sprite sheet-ul este încărcat folosind clasa LoadSave.
     * Animațiile sunt stocate în matricile `animations` și `flippedAnimations`.
     */
    private void loadAnimations() {
        BufferedImage sheet = LoadSave.getSpriteAtlas(LoadSave.GOLEM_BOSS_SPRITESHEET);
        if (sheet == null) {
            System.err.println("Failed to load Golem Boss sprite sheet!");
            return;
        }
        // Dimensiunile unui singur frame din sprite sheet (presupunând că sunt aceleași ca la Goblin Boss pentru consistență)
        int spriteSheetCellWidth = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT;
        int spriteSheetCellHeight = Constants.EnemyConstants.GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT;

        animations = new BufferedImage[Enemy_Animation_Rows.values().length][];
        flippedAnimations = new BufferedImage[Enemy_Animation_Rows.values().length][];
        for (Enemy_Animation_Rows animRow : Enemy_Animation_Rows.values()) {
            int rowIndex = animRow.getRowIndex();
            int frameCount = animRow.getFrameCount();
            if (rowIndex >= Enemy_Animation_Rows.values().length || frameCount == 0) continue; // Sare peste rândurile invalide sau fără frame-uri
            animations[rowIndex] = new BufferedImage[frameCount];
            flippedAnimations[rowIndex] = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int ex = i * spriteSheetCellWidth;
                int ey = rowIndex * spriteSheetCellHeight;
                if (ey + spriteSheetCellHeight <= sheet.getHeight() && ex + spriteSheetCellWidth <= sheet.getWidth()) {
                    animations[rowIndex][i] = sheet.getSubimage(ex, ey, spriteSheetCellWidth, spriteSheetCellHeight);
                    flippedAnimations[rowIndex][i] = flipImage(animations[rowIndex][i]); // Creează și varianta inversată
                } else {
                     System.err.println("GolemBoss: Sprite out of bounds for " + animRow.name() + " frame " + i);
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
     * Actualizează starea Golem Boss-ului.
     * Aceasta include actualizarea animației, cronometrelor de acțiune, detectarea jucătorului,
     * gestionarea mașinii de stări, aplicarea gravitației și actualizarea hitbox-ului.
     * @param player Jucătorul, pentru interacțiuni și detectare.
     * @param lvlData Datele nivelului curent, pentru coliziuni.
     */
    public void update(Player player, int[][] lvlData) {
        this.levelData = lvlData;
        updateAnimationTick();
        actionTimer++;
        
        // Gestionează starea HURT
        if (currentActionState == ActionState.HURT) {
            if (actionTimer >= (Enemy_Animation_Rows.HURT.getFrameCount() * aniSpeed)) { // aniSpeed este numărul de tick-uri per frame
                 currentActionState = ActionState.IDLE;
                 isPerformingAction = false;
                 actionCooldown = IDLE_DURATION_MIN; // Cooldown înainte de următoarea acțiune
                 actionTimer = 0;
            }
            return; // Nu procesa altă logică în starea HURT
        }
        // Gestionează starea DYING
        if (currentActionState == ActionState.DYING) {
            if (actionTimer >= (Enemy_Animation_Rows.DYING.getFrameCount() * aniSpeed)) {
                isActive = false; // Marchează ca inactiv după terminarea animației de moarte
            }
            if (!isActive) {
                System.out.println("Golem Boss is fully deactivated after dying animation.");
            }
            return; // Nu procesa altă logică în starea DYING sau inactiv
        }
        if (!isActive) return; // Dacă nu este activ, nu face nimic

        updatePlayerDetection(player);
        handleStateMachine(player);  
        applyGravity();
        updateHitbox(); // Asigură corectitudinea poziției hitbox-ului
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
            // Determină direcția pe baza poziției jucătorului relativ la boss
            if (player.getHitbox().getCenterX() < this.hitbox.getCenterX()) { // Jucătorul este la stânga
                this.direction = Enemy_Animation_Rows.Directions.LEFT;
            } else { // Jucătorul este la dreapta
                this.direction = Enemy_Animation_Rows.Directions.RIGHT;
            }
        } else {
            playerDetected = false;
        }
    }

    /**
     * Gestionează mașina de stări a Golem Boss-ului.
     * Determină acțiunile curente și tranzițiile între stări pe baza detectării jucătorului,
     * cooldown-urilor și altor condiții specifice Golemului.
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
                } else if (actionTimer > IDLE_DURATION_MAX && actionCooldown <= 0) {
                    decideIdleAction(); // Acțiune simplă în IDLE
                    actionTimer = 0;
                }
                break;
            case DETECTED:
                setBossAnimation(Enemy_Animation_Rows.IDLE_NO_BLINK); // Animație de "alertă"
                if (!playerDetected) { // Jucătorul a ieșit din raza vizuală
                    currentActionState = ActionState.IDLE;
                    actionTimer = 0;
                    break;
                }
                if (actionTimer > 30) { // Scurtă pauză înainte de a decide
                     decideNextAction(player);
                }
                break;
            case WALKING_TOWARDS_PLAYER:
                setBossAnimation(Enemy_Animation_Rows.WALKING);
                moveTowardsPlayer(player.getHitbox(), walkSpeed);
                if (getDistance(this.hitbox, player.getHitbox()) <= meleeAttackRange && actionCooldown <= 0) {
                    currentActionState = ActionState.PREPARING_ATTACK;
                    actionTimer = 0;
                    isPerformingAction = true;
                    attackDamageAppliedThisAttack = false;
                } else if (getDistance(this.hitbox, player.getHitbox()) > meleeAttackRange * 2.5f) { // Dacă jucătorul e prea departe, începe să alerge
                    currentActionState = ActionState.CHASING;
                }
                break;
            case CHASING: // Mișcare mai rapidă
                setBossAnimation(Enemy_Animation_Rows.RUNNING);
                moveTowardsPlayer(player.getHitbox(), runSpeed);
                if (getDistance(this.hitbox, player.getHitbox()) <= meleeAttackRange && actionCooldown <= 0) {
                    currentActionState = ActionState.PREPARING_ATTACK;
                    actionTimer = 0;
                    isPerformingAction = true;
                    attackDamageAppliedThisAttack = false;
                } else if (getDistance(this.hitbox, player.getHitbox()) <= meleeAttackRange * 1.5f) { // Dacă se apropie, trece la mers
                     currentActionState = ActionState.WALKING_TOWARDS_PLAYER;
                }
                break;
            case PREPARING_ATTACK:
                setBossAnimation(Enemy_Animation_Rows.IDLE_NO_BLINK); // Animație de "telegraph"
                if (actionTimer > PREPARE_ATTACK_DURATION) {
                    initiateMeleeAttack(); // Va alege între Slam sau Swing
                }
                break;
            case ATTACKING_MELEE: // Acoperă atât Slam (KICKING) cât și Swing (SLASHING)
                // Animația este setată de initiateMeleeAttack()
                if (actionTimer >= (Enemy_Animation_Rows.values()[this.enemyState].getFrameCount() * aniSpeed)) { // Animația de atac s-a terminat
                    currentActionState = ActionState.IDLE;
                    isPerformingAction = false;
                    actionCooldown = ATTACK_COOLDOWN_MAX;
                    actionTimer = 0;
                } else {
                    // Verifică aplicarea daunelor la frame-ul corect al animației
                    if (!attackDamageAppliedThisAttack && aniIndex == attackCheckFrame) {
                        applyMeleeDamage(player); // Aplică daune o singură dată per atac
                    }
                }
                break;
        }
    }
    
    /**
     * Decide următoarea acțiune a Golem Boss-ului când jucătorul este detectat.
     * Aceasta poate fi un atac, urmărirea jucătorului (mers sau alergare).
     * @param player Jucătorul, pentru a evalua distanța și a lua decizii.
     */
    private void decideNextAction(Player player) {
        if (actionCooldown > 0) return; // Dacă este în cooldown, nu face nimic

        float distance = getDistance(this.hitbox, player.getHitbox());
        double roll = Math.random(); // Pentru decizii probabilistice

        if (distance <= meleeAttackRange * 1.2f) { // Raza de atac mărită puțin datorită dimensiunii
            currentActionState = ActionState.PREPARING_ATTACK;
        } else if (distance <= detectionRange / 2) { // Distanță medie, merge sau aleargă scurt
            if (roll < 0.7) {
                currentActionState = ActionState.WALKING_TOWARDS_PLAYER;
            } else {
                currentActionState = ActionState.CHASING; // Scurtă explozie de alergare
            }
        } else { // Mai departe, predominant merge, ocazional aleargă
             if (roll < 0.8) {
                currentActionState = ActionState.WALKING_TOWARDS_PLAYER;
            } else {
                currentActionState = ActionState.CHASING;
            }
        }
        actionTimer = 0;
        isPerformingAction = true; // Majoritatea deciziilor duc la o acțiune
        attackDamageAppliedThisAttack = false; // Resetează pentru orice nou atac
    }

    /**
     * Inițiază un atac melee. Golemul poate efectua un "Heavy Swing" (animația SLASHING)
     * sau un "Ground Stomp" (animația KICKING).
     * Setează animația corespunzătoare, starea de atac și actualizează hitbox-ul de atac.
     */
    private void initiateMeleeAttack() {
        double roll = Math.random();
        if (roll < 0.5) { // 50% șansă pentru un Heavy Swing
            setBossAnimation(Enemy_Animation_Rows.SLASHING);
            attackCheckFrame = Enemy_Animation_Rows.SLASHING.getFrameCount() / 2 + 2; // Lovitura se aplică puțin mai târziu în animație
        } else { // 50% șansă pentru un Ground Stomp
            setBossAnimation(Enemy_Animation_Rows.KICKING); // Folosește KICKING ca animație pentru stomp
            attackCheckFrame = Enemy_Animation_Rows.KICKING.getFrameCount() / 2;
        }
        currentActionState = ActionState.ATTACKING_MELEE;
        actionTimer = 0; // Resetează cronometrul pentru animația de atac
        actionCooldown = ATTACK_COOLDOWN_MAX;
        updateCurrentMeleeHitbox(); // Actualizează hitbox-ul în funcție de tipul de atac/direcție
    }
    
    /**
     * Decide o acțiune aleatorie pentru starea IDLE a Golemului.
     * Golemul este mai puțin agitat; poate să se întoarcă sau să rămână pe loc.
     */
    private void decideIdleAction() { // Golemul este mai puțin agitat
        if (actionCooldown > 0) return;
        double roll = Math.random();
        if (roll < 0.5) {
            this.direction *= -1; // Se întoarce
            setBossAnimation(Enemy_Animation_Rows.IDLE);
            actionCooldown = IDLE_DURATION_MIN;
            System.out.println("Golem Boss: Idle turn.");
        } else {
            // Doar stă pe loc
            setBossAnimation(Enemy_Animation_Rows.IDLE);
            actionCooldown = IDLE_DURATION_MIN / 2;
        }
    }
    
    /**
     * Mută Golem Boss-ul către jucător cu viteza specificată.
     * Include verificarea limitelor nivelului.
     * @param playerHitbox Hitbox-ul jucătorului, pentru a determina direcția.
     * @param speed Viteza cu care se mișcă boss-ul.
     */
    private void moveTowardsPlayer(Rectangle2D.Float playerHitbox, float speed) {
        float targetPlayerX = (float)playerHitbox.getCenterX();
        float currentBossX = (float)hitbox.getCenterX();
        float deadZone = 10 * Game.SCALE; // O mică zonă "moartă" pentru a preveni tremuratul

        if (Math.abs(currentBossX - targetPlayerX) > deadZone) {
            if (currentBossX < targetPlayerX) { // Încearcă să se miște la dreapta
                hitbox.x += speed; // Se mișcă indiferent de coliziune (logica de coliziune e separată sau implicită)
                this.direction = Enemy_Animation_Rows.Directions.RIGHT;
            } else { // Încearcă să se miște la stânga
                hitbox.x -= speed; // Se mișcă indiferent de coliziune
                this.direction = Enemy_Animation_Rows.Directions.LEFT;
            }
            // Logica de detectare a blocajului a fost eliminată deoarece canMoveHere este ocolită
        }
        
        // Verificarea limitelor nivelului (asigură că este mereu aplicată)
        if (levelData != null && levelData.length > 0 && levelData[0] != null) {
            float levelPixelWidth = levelData[0].length * Game.TILES_SIZE;
            if (hitbox.x < 0) hitbox.x = 0;
            if (hitbox.x + hitbox.width > levelPixelWidth) hitbox.x = levelPixelWidth - hitbox.width;
        }
    }
    
    /**
     * Actualizează hitbox-ul pentru atacurile melee ale Golemului.
     * Poziția și dimensiunea hitbox-ului de atac depind de tipul de atac (stomp sau swing) și direcția boss-ului.
     */
    private void updateCurrentMeleeHitbox() {
        float hx, hy, hw, hh;
        // Hitbox-ul melee al Golemului poate fi mai mare sau poziționat diferit
        hw = hitbox.width * 0.8f; // Acoperire mai largă pentru Golem
        hh = hitbox.height * 0.95f; // Aproape toată înălțimea
        
        if (this.enemyState == Enemy_Animation_Rows.KICKING.getRowIndex()) { // Atac de tip Stomp
            // Centrat sub Golem, mai lat
            hw = hitbox.width * 1.2f; // Stomp-ul afectează o zonă mai largă
            hx = hitbox.x + (hitbox.width / 2) - (hw / 2);
            hy = hitbox.y + hitbox.height * 0.7f; // Partea inferioară a golemului
            hh = hitbox.height * 0.3f;
        } else { // Atac de tip Swing
            if (direction == Enemy_Animation_Rows.Directions.RIGHT) {
                hx = hitbox.x + hitbox.width * 0.5f;
            } else {
                hx = hitbox.x + hitbox.width * 0.5f - hw;
            }
            hy = hitbox.y + hitbox.height * 0.1f;
        }
        currentMeleeHitbox = new Rectangle2D.Float(hx, hy, hw, hh);
    }

    /**
     * Aplică daune jucătorului dacă atacul melee al Golemului îl lovește.
     * Verifică intersecția dintre hitbox-ul de atac și hitbox-ul jucătorului.
     * Aplică și un efect de knockback puternic jucătorului.
     * @param player Jucătorul care poate fi lovit.
     */
    private void applyMeleeDamage(Player player) {
        if (currentMeleeHitbox != null && currentMeleeHitbox.intersects(player.getHitbox())) {
            player.takeDamage(attackDamageBoss);
            System.out.println("Golem Boss melee hit player! Player health: " + player.getCurrentHealth());
            attackDamageAppliedThisAttack = true; // Previne aplicarea multiplă a daunelor dintr-o singură lovitură
            // Aplică knockback
            float knockbackX = (this.direction == Enemy_Animation_Rows.Directions.RIGHT) ? 10f * Game.SCALE : -10f * Game.SCALE; // Knockback mai puternic pentru Golem
            float knockbackY = -5f * Game.SCALE;
            player.applyKnockback(knockbackX, knockbackY);
            System.out.println("Applied strong knockback to player from Golem Boss.");
        }
    }
    
    /**
     * Setează animația curentă a Golem Boss-ului.
     * Resetează indexul și tick-ul animației dacă noua animație este diferită.
     * @param animType Tipul de animație (rândul din sprite sheet) care trebuie setat.
     */
    private void setBossAnimation(Enemy_Animation_Rows animType) {
        int newAnimationState = animType.getRowIndex();
        if (this.enemyState != newAnimationState) {
             this.enemyState = newAnimationState;
             this.aniIndex = 0;
             this.aniTick = 0;
        }
        // Dacă este aceeași stare, animația continuă de la aniIndex și aniTick curente prin updateAnimationTick().
    }

    /**
     * Aplică gravitația Golem Boss-ului dacă acesta nu se află pe o suprafață solidă.
     * Golemul este mai greu și cade mai repede.
     */
    private void applyGravity() {
        if (this.levelData != null && !HelpMethods.isEntityOnFloor(hitbox, this.levelData)) {
            hitbox.y += (float)(2.5f * Game.SCALE); // Golemul este mai greu
        }
    }
    
    /**
     * Actualizează poziția hitbox-ului.
     * Momentan nu este necesară, deoarece metodele de mișcare actualizează direct `hitbox.x` și `hitbox.y`.
     */
    private void updateHitbox() { /* Not currently needed as movement methods update hitbox.x/y directly */ }

    /**
     * Setează datele nivelului pentru Golem Boss.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void setLevelData(int[][] levelData) {
        this.levelData = levelData;
    }

    /**
     * Randează Golem Boss-ul pe ecran.
     * Desenează animația curentă la poziția corectă, luând în considerare decalajul nivelului.
     * Poate desena și hitbox-ul și hitbox-ul de atac (dacă `drawHitbox` este true).
     * @param g Contextul grafic pentru desenare.
     * @param xLvlOffset Decalajul pe axa X al nivelului, pentru scrolling.
     */
    public void render(Graphics g, int xLvlOffset) {
        if (!isActive) return; // Nu randa dacă nu este activ
        if (animations == null || enemyState < 0 || enemyState >= animations.length || animations[enemyState] == null || animations[enemyState].length == 0) {
            return; // Verificare de siguranță pentru datele de animație
        }
        BufferedImage[] currentAnimationSet = (this.direction == Enemy_Animation_Rows.Directions.LEFT) ? flippedAnimations[enemyState] : animations[enemyState];
        if (aniIndex < 0) aniIndex = 0; // Verificare de siguranță
        if (aniIndex >= currentAnimationSet.length) aniIndex = currentAnimationSet.length - 1; // Verificare de siguranță

        if (currentAnimationSet[aniIndex] != null) {
            float drawX = hitbox.x - xDrawOffset - xLvlOffset;
            float drawY = hitbox.y - yDrawOffset;
            g.drawImage(currentAnimationSet[aniIndex], (int)drawX, (int)drawY, DRAW_WIDTH, DRAW_HEIGHT, null);
            if (drawHitbox) { // Va desena doar dacă drawHitbox este true (acum false implicit)
                drawHitbox(g, xLvlOffset);
                if (currentMeleeHitbox != null && (currentActionState == ActionState.ATTACKING_MELEE)) {
                    g.setColor(Color.RED); // Culoare pentru hitbox-ul de atac
                    g.drawRect((int)(currentMeleeHitbox.x - xLvlOffset), (int)currentMeleeHitbox.y, (int)currentMeleeHitbox.width, (int)currentMeleeHitbox.height);
                }
            }
        }
    }
    
    /**
     * Verifică dacă Golem Boss-ul este în viață.
     * @return true dacă sănătatea curentă este mai mare decât 0, false altfel.
     */
    public boolean isAlive() {
        return currentHealthBoss > 0;
    }

    /**
     * Aplică daune Golem Boss-ului.
     * Reduce sănătatea curentă și gestionează tranziția la starea HURT sau DYING.
     * @param damage Cantitatea de daune primite.
     */
    public void takeDamage(int damage) {
        currentHealthBoss -= damage;
        if (currentHealthBoss <= 0) {
            currentHealthBoss = 0;
            isActive = false; // Marchează ca inactiv imediat pentru logică, animația va rula
            setBossAnimation(Enemy_Animation_Rows.DYING);
            currentActionState = ActionState.DYING;
            isPerformingAction = true; // Pentru a asigura rularea animației de moarte
            actionTimer = 0; // Resetează cronometrul pentru animația de moarte
            System.out.println("Golem Boss defeated!");
        } else {
            setBossAnimation(Enemy_Animation_Rows.HURT);
            currentActionState = ActionState.HURT;
            isPerformingAction = true; // Pentru a asigura rularea animației de lovire
            actionTimer = 0; // Resetează cronometrul pentru animația de lovire
            actionCooldown = IDLE_DURATION_MIN; // Oferă boss-ului un moment după ce a fost lovit
            System.out.println("Golem Boss took " + damage + " damage. Health: " + currentHealthBoss);
        }
    }
    
    /**
     * Returnează daunele de atac ale Golem Boss-ului.
     * @return Daunele de atac.
     */
    public int getAttackDamage() {
        return attackDamageBoss;
    }

    /**
     * Returnează sănătatea curentă a Golem Boss-ului.
     * @return Sănătatea curentă.
     */
    public int getCurrentHealth() {
        return currentHealthBoss;
    }

    /**
     * Returnează sănătatea maximă a Golem Boss-ului.
     * @return Sănătatea maximă.
     */
    public int getMaxHealth() {
        return maxHealthBoss;
    }
}
