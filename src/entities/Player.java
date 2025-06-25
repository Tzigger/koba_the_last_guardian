package entities;

import java.awt.Graphics;
import java.awt.Graphics2D; 
import java.awt.AlphaComposite; 
import java.awt.Color; 
import java.awt.image.BufferedImage;
import main.Game;
import utilz.Constants; 
import utilz.Gorilla_Animation_rows;
import utilz.HelpMethods;
import utilz.LoadSave;

public class Player extends Entity {
    private BufferedImage[][] animations;
    private BufferedImage[][] flippedAnimations;
    private BufferedImage[][] crystalRushAnimations; // For Crystal Rush state
    private BufferedImage[][] flippedCrystalRushAnimations; // For Crystal Rush state

    private int animationTick, animationIndex, animationSpeed = 3;
    private int playerAction = Gorilla_Animation_rows.PUNCH_STANDING.getRowIndex();
    private int playerDirection = -1;
    private boolean moving = false, attack = false;
    private boolean isPunching = false;
    private boolean isThrowing = false;
    private boolean isWhacking = false; 
    private boolean isJumpSlamming = false; // New for Jump Slam
    private boolean hasHit = false;
    private boolean hasThrown = false;
    private int attackDamage = 10;
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    private boolean facingRight = false;
    private float playerSpeed = 2.4f;
    private boolean crouch = false;
    private boolean wasCrouchPressed = false;
    private boolean isTransitioning = false;
    private int levelData[][];
    private boolean jump = false;
    
    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isDamaged = false;
    private int damageFlashTimer = 0;
    private final int DAMAGE_FLASH_DURATION = 20; 

    private int permanentMaxHpBonus = 0;
    private int permanentAttackDamageBonus = 0;
    
    private float knockbackX = 0;
    private float knockbackY = 0;
    private int knockbackDuration = 0;
    private final int KNOCKBACK_DURATION = 15; 

    private float xDrawOffset = 66 * Game.SCALE;
    private float yDrawOffset = 39 * Game.SCALE;

    private float airSpeed = 0f;
    private float gravity = 0.12f * Game.SCALE;
    private float jumpSpeed = -6.5f * Game.SCALE;
    private float fallSpeedAfterCollision = 2.4f * Game.SCALE; 
    private boolean inAir = false;

    private boolean isLanding = false;
    private int landingFrame = 7;

    private int punchTick = 0, punchIndex = 0;
    private int throwTick = 0, throwIndex = 0;
    private int whackTick = 0, whackIndex = 0;
    private int jumpSlamTick = 0, jumpSlamIndex = 0; // New for Jump Slam
    private boolean jumpSlamUnlocked = false; // New for Jump Slam
    private final float JUMP_SLAM_DASH_SPEED = 8.0f * Game.SCALE; // New for Jump Slam movement
    private int jumpSlamCooldownTimer = 0; // New for Jump Slam cooldown
    private final int JUMP_SLAM_COOLDOWN_DURATION = 1200; // 20 seconds at 60 UPS

    private int whackCooldownTimer = 0;
    private final int WHACK_COOLDOWN_DURATION = 45;

    private boolean isCrystalRushActive = false;
    private int crystalRushTimer = 0;
    private final int CRYSTAL_RUSH_DURATION = 1200; 
    private int crystalRushCooldownTimer = 0;
    private final int CRYSTAL_RUSH_COOLDOWN_DURATION = 3600; 
    private boolean crystalRushUnlocked = false;
    private float originalPlayerSpeed;

    public Player(float x, float y, int width, int height) {
        super(x, y, width, height);
        this.originalPlayerSpeed = this.playerSpeed; 
        loadAnimations();
        initHitbox(x, y, 103*Game.SCALE, 124*Game.SCALE);
    }

    public void update() {
        updateGravity();
        updateKnockback();
        updatePos();
        updateAnimationTick();
        setAnimation();
        updateDamageEffect();
    }
    
    protected void updateKnockback() { 
        if (knockbackDuration > 0) {
            if (levelData == null) { 
                knockbackDuration = 0; 
                return;
            }
            float knockbackXComponent = this.knockbackX * (knockbackDuration / (float)KNOCKBACK_DURATION);
            float newX = hitbox.x + knockbackXComponent;
            
            if (HelpMethods.canMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, levelData)) {
                hitbox.x = newX;
            }
            
            float knockbackYComponent = this.knockbackY * (knockbackDuration / (float)KNOCKBACK_DURATION);
            if (knockbackYComponent < 0 && !inAir) {  
                airSpeed = knockbackYComponent;
                inAir = true;
            }
            knockbackDuration--;
        }
    }
    
    private void updateDamageEffect() {
        if (isDamaged) {
            damageFlashTimer++;
            if (damageFlashTimer >= DAMAGE_FLASH_DURATION) {
                isDamaged = false;
                damageFlashTimer = 0;
            }
        }
    }

    private void updateGravity() {
        if(!inAir && !isLanding){
            if(!HelpMethods.isEntityOnFloor(hitbox, levelData)){
                inAir = true;
            }
        }
        
        if(inAir){
            float newY = hitbox.y + airSpeed;
            if(HelpMethods.canMoveHere(hitbox.x, newY, hitbox.width, hitbox.height, levelData)){
                hitbox.y = newY;
                airSpeed += gravity;
            } else {
                if(airSpeed > 0){
                    resetInAir();
                } else {
                    airSpeed = fallSpeedAfterCollision;
                }
            }
        }
    }

    private void updateAnimationTick(){
        animationTick++;
        if(animationTick >= animationSpeed){
            animationTick = 0;
            if(inAir){
                if(airSpeed < 0) animationIndex = Math.min(animationIndex + 1, 6); 
                else animationIndex = 7; 
            } else if(isLanding) {
                landingFrame++;
                if(landingFrame > 12) { 
                    isLanding = false;
                    landingFrame = 7; 
                }
            } else if(isTransitioning) {
                animationIndex++;
                if(animationIndex >= Gorilla_Animation_rows.values()[playerAction].getFrameCount()) {
                    isTransitioning = false;
                    animationIndex = 0;
                }
            } else if(isPunching && (playerAction == Gorilla_Animation_rows.PUNCH_CROUCHED.getRowIndex() || playerAction == Gorilla_Animation_rows.PUNCH_STANDING.getRowIndex())) {
                animationIndex++;
                if(animationIndex >= Gorilla_Animation_rows.values()[playerAction].getFrameCount()) animationIndex = 0; 
            } else if (isWhacking && (playerAction == Gorilla_Animation_rows.CROUCH_SLAM.getRowIndex() || playerAction == Gorilla_Animation_rows.STAND_SLAM.getRowIndex())) {
                 animationIndex++; 
                 if(animationIndex >= Gorilla_Animation_rows.values()[playerAction].getFrameCount()) animationIndex = 0;
            } else if (isJumpSlamming && playerAction == Gorilla_Animation_rows.STANDING_JUMP_SLAM.getRowIndex()) { // New for Jump Slam
                animationIndex++;
                if (animationIndex >= Gorilla_Animation_rows.values()[playerAction].getFrameCount()) animationIndex = 0;
            } else if (isThrowing && (playerAction == Gorilla_Animation_rows.CROUCH_THROW.getRowIndex() || playerAction == Gorilla_Animation_rows.STAND_THROW.getRowIndex())) {
                animationIndex++;
                if(animationIndex >= Gorilla_Animation_rows.values()[playerAction].getFrameCount()) animationIndex = 0;
            } else { 
                animationIndex = (animationIndex + 1) % Gorilla_Animation_rows.values()[playerAction].getFrameCount();
            }
        }

        if (isPunching) {
            punchTick++;
            if (punchTick >= animationSpeed) { 
                punchTick = 0;
                punchIndex++;
                Gorilla_Animation_rows currentPunchAnim = crouch ? Gorilla_Animation_rows.PUNCH_CROUCHED : Gorilla_Animation_rows.PUNCH_STANDING;
                if (punchIndex >= currentPunchAnim.getFrameCount()) isPunching = false;
            }
        }

        if (isThrowing) {
            throwTick++;
            if (throwTick >= animationSpeed) { 
                throwTick = 0;
                throwIndex++;
                Gorilla_Animation_rows currentThrowAnim = crouch ? Gorilla_Animation_rows.CROUCH_THROW : Gorilla_Animation_rows.STAND_THROW;
                int animFrameCount = currentThrowAnim.getFrameCount();
                int designatedSpawnFrame = 5; 
                if (throwIndex == designatedSpawnFrame && designatedSpawnFrame < animFrameCount && !hasThrown) hasThrown = true;
                if (throwIndex >= animFrameCount) {
                    isThrowing = false;
                    hasThrown = false; 
                }
            }
        }

        if (isWhacking) {
            whackTick++;
            if (whackTick >= animationSpeed) { 
                whackTick = 0;
                whackIndex++;
                Gorilla_Animation_rows currentWhackAnim = crouch ? Gorilla_Animation_rows.CROUCH_SLAM : Gorilla_Animation_rows.STAND_SLAM;
                if (whackIndex >= currentWhackAnim.getFrameCount()) isWhacking = false;
            }
        }

        if (isJumpSlamming) { // New for Jump Slam
            jumpSlamTick++;
            if (jumpSlamTick >= animationSpeed) {
                jumpSlamTick = 0;
                jumpSlamIndex++;
                if (jumpSlamIndex >= Gorilla_Animation_rows.STANDING_JUMP_SLAM.getFrameCount()) {
                    isJumpSlamming = false;
                    jumpSlamIndex = 0;
                }
            }
        }
        
        if (whackCooldownTimer > 0) whackCooldownTimer--;
        if (jumpSlamCooldownTimer > 0) jumpSlamCooldownTimer--; // Decrement Jump Slam cooldown
        
        if (crystalRushCooldownTimer > 0) {
            crystalRushCooldownTimer--;
        }
        if (isCrystalRushActive) {
            crystalRushTimer--;
            if (crystalRushTimer <= 0) {
                isCrystalRushActive = false;
                this.playerSpeed = originalPlayerSpeed; 
                System.out.println("Crystal Rush Ended.");
            }
        }
    }

    public void render(Graphics g, int lvlOffsetX) {
        BufferedImage currentFrame;
        BufferedImage[][] currentAnimationSetToUse = animations;
        BufferedImage[][] currentFlippedSetToUse = flippedAnimations;

        if (isCrystalRushActive && crystalRushAnimations != null && flippedCrystalRushAnimations != null) {
            currentAnimationSetToUse = crystalRushAnimations;
            currentFlippedSetToUse = flippedCrystalRushAnimations;
        }

        if (playerAction < 0 || playerAction >= currentAnimationSetToUse.length || 
            animationIndex < 0 || animationIndex >= currentAnimationSetToUse[playerAction].length || 
            currentAnimationSetToUse[playerAction][animationIndex] == null) {
            
            if (isCrystalRushActive) { 
                 currentAnimationSetToUse = animations; 
                 currentFlippedSetToUse = flippedAnimations;
                 if (playerAction < 0 || playerAction >= currentAnimationSetToUse.length || 
                     animationIndex < 0 || animationIndex >= currentAnimationSetToUse[playerAction].length || 
                     currentAnimationSetToUse[playerAction][animationIndex] == null) {
                    return; 
                 }
            } else {
                return; 
            }
        }
        
        currentFrame = facingRight ? 
            currentFlippedSetToUse[playerAction][animationIndex] : 
            currentAnimationSetToUse[playerAction][animationIndex];
        
        if (isDamaged && damageFlashTimer % 4 < 2) {
            // Don't draw if flashing
        } else {
            g.drawImage(currentFrame, (int)x - lvlOffsetX, (int)y, width, height, null);
        }
    }

    public void loadAnimations() {
        BufferedImage image = LoadSave.getSpriteAtlas(LoadSave.PLAYER_ATLAS);
        animations = new BufferedImage[Gorilla_Animation_rows.values().length][27];
        flippedAnimations = new BufferedImage[Gorilla_Animation_rows.values().length][27];
        crystalRushAnimations = new BufferedImage[Gorilla_Animation_rows.values().length][27];
        flippedCrystalRushAnimations = new BufferedImage[Gorilla_Animation_rows.values().length][27];
        
        for (int row = 0; row < animations.length; row++) {
            for (int col = 0; col < animations[row].length; col++) {
                int x_coord = col * 272; 
                int y_coord = row * 183; 
                if (image!= null && x_coord + 272 <= image.getWidth() && y_coord + 183 <= image.getHeight()) {
                    animations[row][col] = image.getSubimage(x_coord, y_coord, 272, 183);
                    flippedAnimations[row][col] = flipImage(animations[row][col]);
                }
            }
        }
        System.out.println("Normal animations loaded successfully");

        BufferedImage crystalRushImage = LoadSave.getSpriteAtlas(LoadSave.KOBA_RUSH); 
        if (crystalRushImage != null) {
            for (int row = 0; row < crystalRushAnimations.length; row++) {
                for (int col = 0; col < crystalRushAnimations[row].length; col++) {
                    int x_coord = col * 272; 
                    int y_coord = row * 183; 
                    if (x_coord + 272 <= crystalRushImage.getWidth() && y_coord + 183 <= crystalRushImage.getHeight()) {
                        crystalRushAnimations[row][col] = crystalRushImage.getSubimage(x_coord, y_coord, 272, 183);
                        flippedCrystalRushAnimations[row][col] = flipImage(crystalRushAnimations[row][col]);
                    } else {
                        if (animations[row][col] != null) { 
                             crystalRushAnimations[row][col] = animations[row][col];
                             flippedCrystalRushAnimations[row][col] = flippedAnimations[row][col];
                        }
                    }
                }
            }
            System.out.println("Crystal Rush animations loaded (using placeholder).");
        } else {
            System.err.println("Failed to load Crystal Rush placeholder spritesheet. Crystal Rush will use normal sprites.");
            for (int row = 0; row < animations.length; row++) { 
                crystalRushAnimations[row] = animations[row];
                flippedCrystalRushAnimations[row] = flippedAnimations[row];
            }
        }
    }

    static BufferedImage flipImage(BufferedImage image) {
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x_coord = 0; x_coord < image.getWidth(); x_coord++) {
            for (int y_coord = 0; y_coord < image.getHeight(); y_coord++) {
                flipped.setRGB(image.getWidth() - 1 - x_coord, y_coord, image.getRGB(x_coord, y_coord));
            }
        }
        return flipped;
    }

    public void loadLevelData(int[][] levelData){
        this.levelData = levelData;
    }

    private void setAnimation(){
        int startAnimation = playerAction;
        
        if(isTransitioning && !inAir) {
            return;
        }
        
        if(inAir){
            playerAction = Gorilla_Animation_rows.JUMP_STANDING.getRowIndex();
        } else if(isLanding) {
            playerAction = Gorilla_Animation_rows.JUMP_STANDING.getRowIndex();
        } else if (isJumpSlamming) { // New for Jump Slam
            playerAction = Gorilla_Animation_rows.STANDING_JUMP_SLAM.getRowIndex();
        } else if (isWhacking) { 
            playerAction = crouch ? Gorilla_Animation_rows.CROUCH_SLAM.getRowIndex() : Gorilla_Animation_rows.STAND_SLAM.getRowIndex();
        } else if (isThrowing) {
            playerAction = crouch ? Gorilla_Animation_rows.CROUCH_THROW.getRowIndex() : Gorilla_Animation_rows.STAND_THROW.getRowIndex();
        } else if(isPunching) {
            playerAction = crouch ? Gorilla_Animation_rows.PUNCH_CROUCHED.getRowIndex() : Gorilla_Animation_rows.PUNCH_STANDING.getRowIndex(); 
        } else if(moving){
            playerAction = crouch ? Gorilla_Animation_rows.CROUCH_RUN.getRowIndex() : Gorilla_Animation_rows.STANDING_RUN.getRowIndex();
        } else {
            playerAction = crouch ? Gorilla_Animation_rows.IDLE_CROUCHED.getRowIndex() : Gorilla_Animation_rows.IDLE_STANDING.getRowIndex();
        }

        if(startAnimation != playerAction){
            resetAnimationTick();
        }
    }

    private void resetAnimationTick(){
        animationTick = 0;
        animationIndex = 0;
    }

    private void updatePos(){
        moving = false;
        if(jump){
            jump();
        }
        
        float xSpeed = 0;
        if (isJumpSlamming) {
            xSpeed = facingRight ? JUMP_SLAM_DASH_SPEED : -JUMP_SLAM_DASH_SPEED;
            // No regular movement input if jump slamming
        } else {
            if(!left && !right && !inAir && !jump && !isPunching && !isThrowing && !isWhacking){ 
                // If not moving by any means (including not jump slamming), return
                return;
            }
            if(left){ 
                xSpeed -= playerSpeed;
                facingRight = false;
            }
            if(right){ 
                xSpeed += playerSpeed;
                facingRight = true;
            }
        }

        // Apply xSpeed if not in air and attacking (except jump slam which has its own speed)
        // Or if in air, or if not attacking at all.
        if (isJumpSlamming) {
            updateXPos(xSpeed);
            moving = (xSpeed != 0);
        } else if (inAir || (!isPunching && !isThrowing && !isWhacking)) {
            updateXPos(xSpeed);
            moving = (xSpeed != 0);
        } else if ((isPunching || isThrowing || isWhacking) && xSpeed != 0) { 
            updateXPos(xSpeed);
            moving = (xSpeed != 0);
        } else {
            // If attacking but xSpeed is 0 (e.g. standing punch), moving is false
            moving = false;
        }
    }

    private void updateXPos(float xSpeed){
        float newX = hitbox.x + xSpeed;
        if(HelpMethods.canMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, levelData)){
            hitbox.x = newX;
        }
        x = hitbox.x - xDrawOffset;
        y = hitbox.y - yDrawOffset;
    }

    private void jump(){
        if(inAir || isThrowing || isPunching || isWhacking || isJumpSlamming){ // Added isJumpSlamming
            return;
        }
        inAir = true;
        airSpeed = jumpSpeed;
    }

    public boolean isLeft() { return left; }
    public void setLeft(boolean left) { this.left = left; }
    public boolean isRight() { return right; }
    public void setRight(boolean right) { this.right = right; }
    public void resetDirBooleans() {
        left = false;
        right = false;
        up = false;
        down = false;
        attack = false; 
    }

    public void resetInAir(){
        inAir = false;
        airSpeed = 0;
        isLanding = true;
        landingFrame = 9; 
    }

    public void setAttack(boolean attack) {
        if (attack && !isPunching && !isThrowing && !isWhacking && !isJumpSlamming) {  // Added !isJumpSlamming
            this.attack = true; 
            isPunching = true;
            hasHit = false;     
            punchIndex = 0;    
            punchTick = 0;     
        }
    }

    public void setThrowAttack(boolean throwAttack) {
        if (throwAttack && !isPunching && !isThrowing && !isWhacking && !isJumpSlamming) { // Added !isJumpSlamming
            isThrowing = true;
            hasThrown = false; 
            throwIndex = 0;    
            throwTick = 0;     
        }
    }

    public void setWhackAttack(boolean whackAttack) {
        if (whackAttack && !isPunching && !isThrowing && !isWhacking && !isJumpSlamming && whackCooldownTimer <= 0) { // Added !isJumpSlamming
            isWhacking = true;
            hasHit = false; 
            whackIndex = 0;
            whackTick = 0;
            whackCooldownTimer = WHACK_COOLDOWN_DURATION;
        }
    }

    public void setJumpSlamAttack(boolean jumpSlamAttack) { // New for Jump Slam
        if (jumpSlamAttack && jumpSlamUnlocked && jumpSlamCooldownTimer <= 0 && !isPunching && !isThrowing && !isWhacking && !isJumpSlamming && !inAir && !isTransitioning) {
            isJumpSlamming = true;
            hasHit = false;
            jumpSlamIndex = 0;
            jumpSlamTick = 0;
            jumpSlamCooldownTimer = JUMP_SLAM_COOLDOWN_DURATION; // Set cooldown
            // Horizontal dash is now handled by updatePos if isJumpSlamming is true
        }
    }
    
    public java.awt.geom.Rectangle2D.Float getAttackHitbox() { 
        if (!isPunching) {
            return null;
        }
        if (punchIndex < 3 || punchIndex > 6) { 
            return null;
        }
        float attackWidth = 60 * Game.SCALE;
        float attackHeight = hitbox.height * 0.6f;
        float attackX = facingRight ? hitbox.x + hitbox.width : hitbox.x - attackWidth;
        float attackY = hitbox.y + (hitbox.height - attackHeight) / 2;
        return new java.awt.geom.Rectangle2D.Float(attackX, attackY, attackWidth, attackHeight);
    }

    public java.awt.geom.Rectangle2D.Float getWhackHitbox() {
        if (!isWhacking) {
            return null;
        }
        if (whackIndex < 4 || whackIndex > 7) { 
            return null;
        }
        float whackWidth = 80 * Game.SCALE; 
        float whackHeight = hitbox.height * 0.8f;
        float whackX = facingRight ? hitbox.x + hitbox.width * 0.5f : hitbox.x - whackWidth + hitbox.width * 0.5f;
        float whackY = hitbox.y + (hitbox.height - whackHeight) / 2; 
        return new java.awt.geom.Rectangle2D.Float(whackX, whackY, whackWidth, whackHeight);
    }

    public java.awt.geom.Rectangle2D.Float getJumpSlamHitbox() { // New for Jump Slam
        if (!isJumpSlamming) {
            return null;
        }
        // Active frames for damage, e.g., frames 7-10 of 12
        if (jumpSlamIndex < 7 || jumpSlamIndex > 10) { 
            return null;
        }
        float slamWidth = 100 * Game.SCALE;
        float slamHeight = hitbox.height * 0.6f; // A bit taller than half player height
        // Centered horizontally with player's hitbox center, positioned lower
        float slamX = hitbox.x + (hitbox.width / 2) - (slamWidth / 2);
        float slamY = hitbox.y + hitbox.height * 0.6f; // Starts from lower part of player
        return new java.awt.geom.Rectangle2D.Float(slamX, slamY, slamWidth, slamHeight);
    }
    
    public void setHasHit(boolean hasHit) { this.hasHit = hasHit; }
    public boolean hasHit() { return hasHit; } 

    public int getAttackDamage() { 
        int baseDmg = attackDamage + permanentAttackDamageBonus;
        if (isJumpSlamming) { // New for Jump Slam, check first for specific multiplier
            baseDmg = (int) (baseDmg * 2.0f);
        } else if (isWhacking) { 
            baseDmg = (int) (baseDmg * 1.2f);
        }
        // Crystal rush can stack with other attacks
        if (isCrystalRushActive) {
            baseDmg = (int) (baseDmg * 2.0f); 
        }
        return baseDmg; 
    }

    public void collectBananaEffect() {
        this.permanentMaxHpBonus += Constants.Collectibles.BANANA_HP_BONUS;
        this.permanentAttackDamageBonus += Constants.Collectibles.BANANA_ATTACK_BONUS;
        int newMaxHealth = getMaxHealth(); 
        heal(newMaxHealth); 
    }

    public void collectCoconutEffect() {
        System.out.println("Player collected a coconut pickup.");
    }

    public boolean isAttack() { return attack; } 
    public boolean isCrouch() { return crouch; }   
    public void setCrouch(boolean crouch) {
        if (crouch && !wasCrouchPressed && !inAir && !isPunching && !isThrowing && !isWhacking && !isJumpSlamming) {  // Added !isJumpSlamming
            isTransitioning = true;
            animationIndex = 0;
            playerAction = this.crouch ? 
                Gorilla_Animation_rows.CROUCH_TO_STAND.getRowIndex() : 
                Gorilla_Animation_rows.STAND_TO_CROUCH.getRowIndex();
            this.crouch = !this.crouch;  
        }
        wasCrouchPressed = crouch;
    }   

    public boolean isUp() { return up; }
    public void setUp(boolean up) { this.up = up; }
    public boolean isDown() { return down; }
    public void setDown(boolean down) { this.down = down; }
    public boolean isJump() { return jump; }
    public void setJump(boolean jump) { this.jump = jump; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth + permanentMaxHpBonus; }
    public void heal(int amount) { currentHealth = Math.min(currentHealth + amount, getMaxHealth()); }
    public boolean takeDamage(int amount) {
        currentHealth = Math.max(currentHealth - amount, 0);
        isDamaged = true;
        damageFlashTimer = 0;
        return currentHealth > 0;
    }
    public boolean isAlive() { return currentHealth > 0; }
    public boolean isDamaged() { return isDamaged; }
    public void applyKnockback(float knockbackX, float knockbackY) {
        this.knockbackX = knockbackX * Game.SCALE;
        this.knockbackY = knockbackY * Game.SCALE;
        this.knockbackDuration = KNOCKBACK_DURATION;
    }
    public void resetHealth() { currentHealth = getMaxHealth(); }
    public void setCurrentHealth(int health) { this.currentHealth = Math.min(Math.max(health, 0), getMaxHealth()); }
    public void setPosition(float x, float y) {
        this.hitbox.x = x;
        this.hitbox.y = y;
        this.x = x - xDrawOffset;
        this.y = y - yDrawOffset;
    }
    public void resetToStartPosition() { setPosition(100 * Game.SCALE, 770 * Game.SCALE); }
    public boolean isThrowing() { return isThrowing; }
    public boolean isPunching() { return isPunching; }
    public boolean isWhacking() { return isWhacking; } 
    public boolean isJumpSlamming() { return isJumpSlamming; } // New for Jump Slam
    public void setJumpSlamUnlocked(boolean unlocked) { this.jumpSlamUnlocked = unlocked; } // New for Jump Slam
    
    public boolean isCrystalRushUnlocked() { return crystalRushUnlocked; }
    public void unlockCrystalRush() { 
        this.crystalRushUnlocked = true; 
        System.out.println("PLAYER SAYS: Crystal Rush Unlocked! Flag set to true."); 
    }
    public boolean isCrystalRushActive() { return isCrystalRushActive; }

    public void activateCrystalRush() {
        System.out.println("Player.activateCrystalRush: Attempting. Unlocked=" + crystalRushUnlocked + 
                           ", Active=" + isCrystalRushActive + ", Cooldown=" + crystalRushCooldownTimer);
        if (crystalRushUnlocked && !isCrystalRushActive && crystalRushCooldownTimer <= 0) { 
            isCrystalRushActive = true;
            crystalRushTimer = CRYSTAL_RUSH_DURATION;
            crystalRushCooldownTimer = CRYSTAL_RUSH_COOLDOWN_DURATION;
            this.playerSpeed = originalPlayerSpeed * 1.5f; 
            System.out.println("Crystal Rush Activated!");
        } else if (!crystalRushUnlocked) {
            System.out.println("Crystal Rush is not unlocked yet!");
        } else if (isCrystalRushActive) {
            System.out.println("Crystal Rush is already active!");
        } else if (crystalRushCooldownTimer > 0) {
            System.out.println("Crystal Rush is on cooldown: " + (crystalRushCooldownTimer / 60) + "s remaining.");
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public int getAnimationIndex() { return animationIndex; }
    public int getWhackIndex() { return whackIndex; } 


    public boolean canSpawnProjectileAndConsume() {
        boolean canSpawn = isThrowing && hasThrown;
        
        if (canSpawn) {
            this.hasThrown = false; 
        }
        return canSpawn;
    }
}
