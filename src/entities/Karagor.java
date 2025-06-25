package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.Gorilla_Animation_rows;
import utilz.HelpMethods;
import utilz.LoadSave;

/**
 * Reprezintă entitatea Karagor în joc, care poate funcționa ca un boss.
 * Această clasă extinde clasa Player, sugerând că poate împărtăși unele mecanici
 * cu jucătorul, dar are propriul set de animații, comportament de atac și atribute.
 * Gestionează stările, mișcarea, atacurile și interacțiunea cu mediul și jucătorul.
 */
public class Karagor extends Player {
    /** Matrice bidimensională pentru stocarea animațiilor normale ale lui Karagor. */
    private BufferedImage[][] animations;
    /** Contor pentru tick-urile de animație. */
    private int animationTick;
    /** Indexul frame-ului curent al animației. */
    private int animationIndex;
    /** Viteza animației (numărul de tick-uri per frame). */
    private int animationSpeed = 3;
    /** Acțiunea curentă a lui Karagor (indexul rândului din sprite sheet). */
    private int karagorAction = Gorilla_Animation_rows.PUNCH_STANDING.getRowIndex();
    /** Direcția curentă a lui Karagor (-1 pentru stânga, 1 pentru dreapta - deși neutilizat direct în logica de mișcare). */
    private int karagorDirection = -1; // Not directly used for movement logic like Player's direction
    /** Indicator dacă Karagor se mișcă. */
    private boolean moving = false;
    /** Indicator dacă Karagor atacă. */
    private boolean attack = false;
    /** Indicator dacă animația de pumn este în desfășurare. */
    private boolean isPunching = false;
    /** Indicator dacă atacul curent a lovit deja ceva. */
    private boolean hasHit = false;
    /** Daunele provocate de atacurile lui Karagor. */
    private int attackDamage = 5;
    /** Indicator dacă Karagor se mișcă la stânga. */
    private boolean left = false;
    /** Indicator dacă Karagor se mișcă la dreapta. */
    private boolean right = false;
    /** Indicator dacă Karagor se mișcă în sus (neutilizat pentru mișcare, poate pentru săritură). */
    private boolean up = false;
    /** Indicator dacă Karagor se mișcă în jos (neutilizat pentru mișcare, poate pentru ghemuire). */
    private boolean down = false;
    /** Indicator dacă Karagor este orientat spre dreapta. */
    private boolean facingRight = false;
    /** Viteza de mișcare a lui Karagor. */
    private float playerSpeed = 1.5f; // Renamed from karagorSpeed for consistency with Player class
    /** Indicator dacă Karagor este ghemuit. */
    private boolean crouch = false;
    /** Starea anterioară a butonului de ghemuire. */
    private boolean wasCrouchPressed = false;
    /** Indicator dacă Karagor este într-o animație de tranziție (ex: stand to crouch). */
    private boolean isTransitioning = false;
    /** Datele nivelului curent, folosite pentru coliziuni și navigație. */
    private int levelData[][];
    /** Indicator dacă Karagor încearcă să sară. */
    private boolean jump = false;
    
    // Health system
    /** Sănătatea maximă a lui Karagor. */
    private int maxHealth = 150;
    /** Sănătatea curentă a lui Karagor. */
    private int currentHealth = 150;
    /** Indicator dacă Karagor este în starea "hurt" (lovit). */
    private boolean isHurt = false;
    /** Cronometru pentru durata animației "hurt". */
    private int hurtTimer = 0;
    /** Durata animației "hurt" în frame-uri. */
    private final int HURT_ANIMATION_DURATION = 30;


    //Drawing hitbox
    /** Decalajul pe axa X pentru desenarea sprite-ului lui Karagor. */
    private float xDrawOffset = 66 * Game.SCALE;
    /** Decalajul pe axa Y pentru desenarea sprite-ului lui Karagor. */
    private float yDrawOffset = 39 * Game.SCALE;

    //Jumping + Gravity
    /** Viteza verticală a lui Karagor în aer. */
    private float airSpeed = 0f;
    /** Valoarea gravitației aplicate lui Karagor. */
    private float gravity = 0.12f * Game.SCALE;
    /** Viteza inițială a săriturii lui Karagor. */
    private float jumpSpeed = -6.5f * Game.SCALE;
    /** Viteza de cădere după o coliziune verticală (cu tavanul). */
    private float fallSpeedAfterCollision = 2.4f * Game.SCALE;
    /** Indicator dacă Karagor se află în aer. */
    private boolean inAir = false;

    /** Indicator dacă Karagor este în animația de aterizare. */
    private boolean isLanding = false;
    /** Frame-ul curent al animației de aterizare. */
    private int landingFrame = 7; // Start frame for landing animation
    /** Matrice bidimensională pentru stocarea animațiilor inversate (flipped) ale lui Karagor. */
    private BufferedImage[][] flippedAnimations;
    /** Indicator dacă hitbox-ul trebuie desenat (pentru debugging). */
    public boolean drawHitbox = true; // Should be false for release
    /** Indicator dacă această instanță de Karagor este un boss. */
    private boolean isBoss = true;
    /** Limita dreaptă a platformei pe care se află Karagor (pentru patrulare). */
    private float platformRightBound;
    /** Limita stângă a platformei pe care se află Karagor (pentru patrulare). */
    private float platformLeftBound;
    /** Cooldown-ul dintre atacuri. */
    private int attackCooldown = 120; // Ticks
    /** Distanța la care Karagor poate detecta jucătorul. */
    private float detectionRange = 500f;
    /** Indicator dacă Karagor execută un atac. */
    private boolean isAttacking = false;
    /** Cooldown-ul maxim dintre atacuri (în tick-uri). */
    private final int ATTACK_COOLDOWN = 120; // Ticks

    
    /**
     * Constructor pentru clasa Karagor.
     * Inițializează Karagor cu o poziție, dimensiuni și specifică dacă este un boss.
     * Setează atributele (sănătate, daune) în funcție de rolul de boss.
     * Încarcă animațiile și inițializează hitbox-ul.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param width Lățimea entității.
     * @param height Înălțimea entității.
     * @param isBoss true dacă această instanță este un boss, false altfel.
     */
    public Karagor(float x, float y, int width, int height, boolean isBoss) {
        super(x, y, width, height); // Apelează constructorul clasei Player
        this.isBoss = isBoss;
        if (isBoss) {
            // Boss-ul are mai multă sănătate și provoacă mai multe daune
            maxHealth = 200;
            currentHealth = 200;
            attackDamage = 5; // Daunele pot fi ajustate
            detectionRange = 500f; // Raza de detecție
        }
        loadAnimations();
        // Inițializează hitbox-ul la centrul sprite-ului
        initHitbox(x, y, 103*Game.SCALE, 124*Game.SCALE); // Dimensiuni specifice pentru Karagor
    }
    
    /**
     * Setează limitele platformei pe care Karagor poate patrula.
     * @param left Limita stângă a platformei.
     * @param right Limita dreaptă a platformei.
     */
    public void setPlatformBounds(float left, float right) {
        this.platformLeftBound = left;
        this.platformRightBound = right;
    }
    
    /**
     * Actualizează poziția lui Karagor în funcție de poziția jucătorului (dacă este boss).
     * Karagor se va întoarce către jucător și se va deplasa spre el dacă nu este în raza de atac.
     * Dacă este în raza de atac și cooldown-ul permite, va efectua un atac.
     * @param playerHitbox Hitbox-ul jucătorului.
     */
    public void updatePlayerPosition(Rectangle2D.Float playerHitbox) {
        if (!isBoss || !isAlive() || isHurt) return; // Nu face nimic dacă nu e boss, e mort sau lovit
        
        float playerCenterX = playerHitbox.x + playerHitbox.width/2;
        float bossCenterX = hitbox.x + hitbox.width/2;
        
        // Verifică dacă jucătorul este în raza de detecție
        if (Math.abs(playerCenterX - bossCenterX) > detectionRange) {
            // Oprește mișcarea dacă jucătorul este prea departe
            setLeft(false);
            setRight(false);
            return;
        }
        
        // Se întoarce către jucător
        facingRight = (playerCenterX > bossCenterX);
        
        // Se mișcă către jucător dacă nu este în raza de atac
        float attackRange = hitbox.width * 0.7f; // Raza de atac
        if (Math.abs(playerCenterX - bossCenterX) > attackRange) {
            if (playerCenterX < bossCenterX) {
                setLeft(true);
                setRight(false);
            } else {
                setRight(true);
                setLeft(false);
            }
        } else {
            // În raza de atac, oprește mișcarea și atacă
            setLeft(false);
            setRight(false);
            if (attackCooldown <= 0) {
                performAttack();
                attackCooldown = ATTACK_COOLDOWN; // Resetează cooldown-ul atacului
            }
        }
        
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }
    
    /**
     * Efectuează un atac aleatoriu din lista de animații de atac valide.
     * Setează acțiunea Karagorului la animația de atac aleasă și resetează cooldown-ul.
     */
    private void performAttack() {
        // Listă rafinată de animații de atac reale pentru Karagor
        Gorilla_Animation_rows[] validAttackAnimations = {
            Gorilla_Animation_rows.PUNCH_CROUCHED,
            Gorilla_Animation_rows.COMBO_STANDING,
            Gorilla_Animation_rows.STANDING_JUMP_SLAM,
            Gorilla_Animation_rows.PUNCH_STANDING,
            Gorilla_Animation_rows.CROUCH_SLAM,
            Gorilla_Animation_rows.STAND_SLAM
        };

        // Alege aleatoriu o animație de atac din lista validă
        if (validAttackAnimations.length > 0) { // Asigură-te că lista nu este goală
            int randomIndex = (int) (Math.random() * validAttackAnimations.length);
            Gorilla_Animation_rows attackAnimation = validAttackAnimations[randomIndex];

            // Setează acțiunea Karagorului la animația de atac aleasă
            karagorAction = attackAnimation.getRowIndex();
            setAttack(true); // Această metodă probabil gestionează setarea isPunching și animationIndex
            isAttacking = true; // Urmărește explicit starea de atac pentru altă logică, dacă este necesar
            attackCooldown = ATTACK_COOLDOWN; // Resetează cooldown-ul atacului
        }
    }

    /**
     * Metoda principală de actualizare pentru Karagor.
     * Actualizează poziția în funcție de jucător, gravitația, knockback-ul,
     * animațiile și starea de "hurt".
     * @param playerHitbox Hitbox-ul jucătorului.
     */
    public void update(Rectangle2D.Float playerHitbox) {
        updatePlayerPosition(playerHitbox); // Actualizează logica specifică de boss
        updateGravity(); // Aplică gravitația
        updateKnockback(); // Actualizează efectul de knockback
        updatePos(); // Actualizează poziția pe baza input-ului (left/right/jump)
        updateAnimationTick(); // Actualizează frame-ul animației
        updateHurtState(); // Gestionează starea "hurt"
        setAnimation(); // Setează animația corectă pe baza stării curente
    }

    /**
     * Actualizează starea "hurt" (lovit).
     * Dacă Karagor este lovit, incrementează un cronometru; după o durată specifică, iese din starea "hurt".
     */
    private void updateHurtState() {
        if (isHurt) {
            hurtTimer++;
            if (hurtTimer >= HURT_ANIMATION_DURATION) {
                isHurt = false;
                hurtTimer = 0;
            }
        }
    }

    /**
     * Actualizează gravitația și mișcarea verticală a lui Karagor.
     * Gestionează starea "inAir" și coliziunile cu podeaua sau tavanul.
     */
    protected void updateGravity() {
        // Verifică dacă jucătorul este pe pământ
        if (!inAir && !isLanding) { // Dacă nu este în aer și nu aterizează
            if (!HelpMethods.isEntityOnFloor(hitbox, levelData)) { // Verifică dacă este pe podea
                inAir = true; // Dacă nu, intră în starea "inAir"
            }
        }

        if (inAir) { // Dacă este în aer
            float newY = hitbox.y + airSpeed; // Calculează noua poziție Y
            // float newX = hitbox.x; // Poziția X nu se schimbă aici (doar vertical)
            if (HelpMethods.canMoveHere(hitbox.x, newY, hitbox.width, hitbox.height, levelData)) { // Verifică dacă se poate mișca
                hitbox.y = newY; // Actualizează poziția Y
                airSpeed += gravity; // Aplică gravitația
            } else {
                // Coliziune detectată (podea sau tavan)
                if (airSpeed > 0) { // Cădea în jos - a aterizat pe podea
                    resetInAir(); // Resetează starea "inAir"
                } else { // Se mișca în sus - a lovit tavanul
                    airSpeed = fallSpeedAfterCollision; // Aplică o viteză de cădere
                }
            }
        }
    }

    /**
     * Actualizează tick-ul de animație și indexul frame-ului curent.
     * Gestionează diferite tipuri de animații (în aer, aterizare, tranziție, pumn, normală).
     */
    private void updateAnimationTick(){
        animationTick++;
        if(animationTick >= animationSpeed){
            animationTick = 0;
            if(inAir){ // Animație în aer
                if(airSpeed < 0){ // Sare
                    animationIndex = Math.min(animationIndex + 1, 6); // Limitează la frame-urile de săritură
                } else { // Cade
                    animationIndex = 7; // Frame specific pentru cădere
                }
            } else if(isLanding) { // Animație de aterizare
                landingFrame++;
                if(landingFrame > 12) { // După terminarea animației de aterizare
                    isLanding = false;
                    landingFrame = 7; // Resetează frame-ul de aterizare
                }
            } else if(isTransitioning) { // Animație de tranziție
                animationIndex++;
                if(animationIndex >= Gorilla_Animation_rows.values()[karagorAction].getFrameCount()) {
                    isTransitioning = false; // Termină tranziția
                    animationIndex = 0;
                }
            } else if(isPunching) { // Animație de pumn
                animationIndex++;
                if(animationIndex >= Gorilla_Animation_rows.values()[karagorAction].getFrameCount()) {
                    isPunching = false; // Termină pumnul
                    isAttacking = false; // Resetează și flag-ul general de atac
                    animationIndex = 0;
                }
            } else { // Animație normală (mers, idle)
                animationIndex = (animationIndex + 1) % Gorilla_Animation_rows.values()[karagorAction].getFrameCount();
            }
        }
    }

    /**
     * Randează Karagor pe ecran.
     * Desenează frame-ul curent al animației la poziția corectă, luând în considerare decalajul nivelului.
     * @param g Contextul grafic pentru desenare.
     * @param lvlOffsetX Decalajul pe axa X al nivelului, pentru scrolling.
     */
    public void render(Graphics g, int lvlOffsetX) {
        // Verificări pentru a preveni NullPointerException sau ArrayIndexOutOfBoundsException
        if (karagorAction >= animations.length || animations[karagorAction] == null ||
            animationIndex >= animations[karagorAction].length ||
            animations[karagorAction][animationIndex] == null) {
            // System.err.println("Karagor render: Animation data missing or invalid. Action: " + karagorAction + ", Index: " + animationIndex);
            return; // Nu randa dacă datele de animație sunt invalide
        }
        
        // Alege frame-ul corect (normal sau inversat) în funcție de direcție
        BufferedImage currentFrame = facingRight ?
            flippedAnimations[karagorAction][animationIndex] :
            animations[karagorAction][animationIndex];
        
        // Desenează frame-ul la poziția corectă, ajustată cu decalajul nivelului
        g.drawImage(currentFrame, (int)x - lvlOffsetX, (int)y, width, height, null);
        
        // drawHitbox(g, lvlOffsetX); // Metoda drawHitbox din Player ar trebui să primească lvlOffsetX
    }

    /**
     * Încarcă animațiile pentru Karagor din sprite sheet.
     * Sprite sheet-ul este încărcat folosind clasa LoadSave.
     * Animațiile sunt stocate în matricile `animations` și `flippedAnimations`.
     */
    public void loadAnimations() {
        BufferedImage image = LoadSave.getSpriteAtlas(LoadSave.KARAGOR_SPRITESHEET);
        if (image == null) {
            System.err.println("Failed to load Karagor sprite sheet!");
            // Inițializează matricile goale pentru a preveni NullPointerException
            animations = new BufferedImage[Gorilla_Animation_rows.values().length][0];
            flippedAnimations = new BufferedImage[Gorilla_Animation_rows.values().length][0];
            return;
        }
        animations = new BufferedImage[Gorilla_Animation_rows.values().length][27]; // Presupunând maxim 27 de frame-uri per animație
        flippedAnimations = new BufferedImage[Gorilla_Animation_rows.values().length][27];
        
        for (int row = 0; row < animations.length; row++) {
            // Verifică dacă rândul corespunde unei valori valide din enum
            if (row < Gorilla_Animation_rows.values().length) {
                Gorilla_Animation_rows animEnum = Gorilla_Animation_rows.values()[row];
                int frameCount = animEnum.getFrameCount(); // Obține numărul de frame-uri pentru acest rând

                for (int col = 0; col < frameCount; col++) { // Iterează doar până la numărul real de frame-uri
                    int x = col * 272; // Lățimea unui frame
                    int y = row * 183; // Înălțimea unui frame
                    if (x + 272 <= image.getWidth() && y + 183 <= image.getHeight()) {
                        animations[row][col] = image.getSubimage(x, y, 272, 183);
                        // Pre-inversează sprite-ul pentru animațiile orientate spre dreapta
                        flippedAnimations[row][col] = flipImage(animations[row][col]);
                    } else {
                        // System.err.println("Karagor loadAnimations: Sprite out of bounds for row " + row + ", col " + col);
                    }
                }
            }
        }
        System.out.println("Karagor animations loaded successfully");
    }

    /**
     * Inversează (flip) o imagine pe orizontală.
     * @param image Imaginea care trebuie inversată.
     * @return Imaginea inversată.
     */
    static BufferedImage flipImage(BufferedImage image) {
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
     * Setează datele nivelului pentru Karagor.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void loadLevelData(int[][] levelData){
        this.levelData = levelData;
    }

    /**
     * Setează animația curentă a lui Karagor pe baza stării sale.
     * Gestionează prioritățile animațiilor (ex: "hurt" are prioritate maximă).
     */
    private void setAnimation() {
        int previousAction = karagorAction; // Păstrează acțiunea anterioară pentru a detecta schimbări

        // Prioritate maximă: Animația "Hurt"
        if (isHurt) {
            karagorAction = crouch ? Gorilla_Animation_rows.HURT_CROUCHED.getRowIndex() : Gorilla_Animation_rows.HURT_STANDING.getRowIndex();
            if (previousAction != karagorAction) {
                resetAnimationTick(); // Resetează animația dacă s-a schimbat
            }
            return; // Animația "Hurt" suprascrie orice altceva
        }

        // Prioritate a doua: Animația de atac (dacă isPunching este true)
        // karagorAction este setat de performAttack() și ar trebui menținut dacă isPunching.
        if (isPunching) {
            // Asigură-te că karagorAction (care ar trebui să fie o animație de atac) nu este suprascrisă.
            // Tick-ul și indexul animației sunt gestionate de setAttack și updateAnimationTick.
        }
        // Prioritate a treia: Animația de tranziție
        else if (isTransitioning && !inAir) {
            // karagorAction este deja setat pentru tranziție.
            // Nu este necesară nicio modificare aici.
        }
        // Alte stări
        else if (inAir) { // În aer (săritură/cădere)
            karagorAction = Gorilla_Animation_rows.JUMP_STANDING.getRowIndex();
        } else if (isLanding) { // Aterizare
            // Schimbă aterizarea în IDLE
            karagorAction = crouch ? Gorilla_Animation_rows.IDLE_CROUCHED.getRowIndex() : Gorilla_Animation_rows.IDLE_STANDING.getRowIndex();
        } else if (moving) { // Mișcare (alergare)
            karagorAction = crouch ? Gorilla_Animation_rows.CROUCH_RUN.getRowIndex() : Gorilla_Animation_rows.STANDING_RUN.getRowIndex();
        } else {
            // Implicit IDLE dacă nu atacă, nu e lovit, nu e în tranziție, nu e în aer, nu aterizează, nu se mișcă
            karagorAction = crouch ? Gorilla_Animation_rows.IDLE_CROUCHED.getRowIndex() : Gorilla_Animation_rows.IDLE_STANDING.getRowIndex();
        }

        // Resetează animația dacă acțiunea s-a schimbat (și nu era deja gestionată mai sus)
        if (previousAction != karagorAction) {
            resetAnimationTick();
        }
    }

    /**
     * Resetează contorul de tick-uri și indexul frame-ului animației.
     * Folosit la schimbarea tipului de animație.
     */
    private void resetAnimationTick(){
        animationTick = 0;
        animationIndex = 0;
    }


    /**
     * Actualizează poziția orizontală a lui Karagor pe baza input-ului (stânga/dreapta) și a săriturii.
     * Gestionează coliziunile cu pereții.
     */
    private void updatePos(){
        moving = false; // Presupune că nu se mișcă inițial
        
        if(jump){ // Dacă se apasă săritura
            jump(); // Execută săritura
        }
        // Dacă nu se apasă stânga, dreapta și nu este în aer, nu face nimic
        if(!left && !right && !inAir){
            return;
        }

        float xSpeed = 0; // Viteza orizontală

        if(left){ // Dacă se apasă stânga
            xSpeed -= playerSpeed;
            facingRight = false; // Se întoarce spre stânga
        }
        if(right){ // Dacă se apasă dreapta
            xSpeed += playerSpeed;
            facingRight = true; // Se întoarce spre dreapta
        }

        // Actualizează poziția X indiferent dacă este în aer sau nu (gravitația gestionează Y)
        updateXPos(xSpeed);
        // Dacă s-a aplicat viteză orizontală, înseamnă că se mișcă
        if (xSpeed != 0) {
            moving = true;
        }
    }


    /**
     * Actualizează poziția X a lui Karagor, verificând coliziunile cu pereții.
     * @param xSpeed Viteza orizontală care trebuie aplicată.
     */
    private void updateXPos(float xSpeed){
        float newX = hitbox.x + xSpeed; // Calculează noua poziție X
        if(HelpMethods.canMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, levelData)){ // Verifică dacă se poate mișca
            hitbox.x = newX; // Actualizează poziția X a hitbox-ului
        } else {
            // Când lovește un perete, păstrează poziția curentă (nu se mișcă)
            return;
        }
        
        // Actualizează poziția sprite-ului pe baza hitbox-ului
        x = hitbox.x - xDrawOffset;
        y = hitbox.y - yDrawOffset;
    }

    /**
     * Inițiază o săritură dacă Karagor nu este deja în aer.
     * Setează starea "inAir" și viteza verticală inițială.
     */
    private void jump(){
        if(inAir){ // Dacă este deja în aer, nu face nimic
            return;
        }
        inAir = true; // Intră în starea "inAir"
        airSpeed = jumpSpeed; // Setează viteza verticală inițială pentru săritură
    }

    /** @return true dacă Karagor se mișcă la stânga, false altfel. */
    public boolean isLeft() {
        return left;
    }

    /** Setează starea de mișcare la stânga. @param left true pentru a activa mișcarea la stânga. */
    public void setLeft(boolean left) {
        this.left = left;
    }

    /** @return true dacă Karagor se mișcă la dreapta, false altfel. */
    public boolean isRight() {
        return right;
    }

    /** Setează starea de mișcare la dreapta. @param right true pentru a activa mișcarea la dreapta. */
    public void setRight(boolean right) {
        this.right = right;
    }

    /** Resetează toate flag-urile de direcție și atac. */
    public void resetDirBooleans() {
        left = false;
        right = false;
        up = false;
        down = false;
        attack = false; // Deși 'attack' pare să nu fie folosit extensiv pentru logica de atac
    }

    /** Resetează starea "inAir" și inițiază animația de aterizare. */
    public void resetInAir(){
        inAir = false;
        airSpeed = 0; // Oprește mișcarea verticală
        isLanding = true; // Începe animația de aterizare
        landingFrame = 9; // Setează frame-ul de start pentru aterizare (poate necesita ajustare)
    }

    /**
     * Setează starea de atac. Dacă se activează atacul și Karagor nu atacă deja,
     * inițiază animația de pumn și resetează starea de lovitură.
     * @param attack true pentru a iniția un atac.
     */
    public void setAttack(boolean attack) {
        if (attack && !isPunching) {  // Începe un nou pumn doar dacă nu lovește deja
            this.attack = true; // Acest flag 'attack' pare redundant dacă 'isPunching' gestionează starea
            isPunching = true; // Activează starea de pumn
            hasHit = false;     // Resetează starea de lovitură pentru noul pumn
            animationIndex = 0;  // Resetează indexul animației pentru noul pumn
        }
    }
    
    /**
     * Returnează hitbox-ul de atac al lui Karagor dacă acesta atacă și se află
     * într-un frame valid al animației de atac.
     * @return Un obiect {@link Rectangle2D.Float} reprezentând zona de atac, sau null dacă nu atacă sau nu este un frame valid.
     */
    public java.awt.geom.Rectangle2D.Float getAttackHitbox() {
        if (!isPunching) { // Dacă nu lovește, nu există hitbox de atac
            return null;
        }
        
        // Permite detectarea loviturii doar în frame-urile de mijloc ale animației de atac (3-6)
        if (animationIndex < 3 || animationIndex > 6) { // Ajustează acești indecși dacă animația diferă
            return null;
        }
        
        // Creează hitbox-ul de atac în fața jucătorului, în funcție de direcție
        float attackWidth = 60 * Game.SCALE; // Lățimea hitbox-ului de atac
        float attackHeight = hitbox.height * 0.6f; // Înălțimea hitbox-ului de atac
        float attackX; // Poziția X a hitbox-ului de atac
        
        if (facingRight) { // Dacă este orientat spre dreapta
            attackX = hitbox.x + hitbox.width; // Hitbox-ul este la dreapta hitbox-ului principal
        } else { // Dacă este orientat spre stânga
            attackX = hitbox.x - attackWidth; // Hitbox-ul este la stânga hitbox-ului principal
        }
        
        float attackY = hitbox.y + (hitbox.height - attackHeight) / 2; // Centrează vertical hitbox-ul de atac
        
        return new java.awt.geom.Rectangle2D.Float(attackX, attackY, attackWidth, attackHeight);
    }
    
    /**
     * Marchează atacul curent ca fiind efectuat (a lovit ceva),
     * pentru a preveni lovituri multiple cu același atac.
     * @param hasHit true dacă atacul a lovit, false altfel.
     */
    public void setHasHit(boolean hasHit) {
        this.hasHit = hasHit;
    }
    
    /**
     * Verifică dacă atacul curent a lovit deja ceva.
     * @return true dacă atacul a lovit, false altfel.
     */
    public boolean hasHit() {
        return hasHit;
    }
    
    /**
     * Returnează daunele provocate de atacurile lui Karagor.
     * @return Cantitatea de daune.
     */
    public int getAttackDamage() {
        return attackDamage;
    }

    /** @return true dacă Karagor este în starea de atac (flag general), false altfel. */
    public boolean isAttack() {
        return attack; // Acest flag pare să fie setat dar nu folosit extensiv pentru logica de atac
    }

    /** @return true dacă Karagor este ghemuit, false altfel. */
    public boolean isCrouch() {
        return crouch;
    }   

    /**
     * Setează starea de ghemuire a lui Karagor.
     * Dacă se activează ghemuirea și Karagor nu era ghemuit și nu este în aer,
     * inițiază animația de tranziție corespunzătoare.
     * @param crouch true pentru a activa ghemuirea.
     */
    public void setCrouch(boolean crouch) {
        if (crouch && !wasCrouchPressed && !inAir) {  // Permite comutarea ghemuirii doar când nu este în aer
            isTransitioning = true; // Începe animația de tranziție
            animationIndex = 0; // Resetează indexul animației
            // Setează animația de tranziție corespunzătoare
            karagorAction = this.crouch ?
                Gorilla_Animation_rows.CROUCH_TO_STAND.getRowIndex() : // Dacă era ghemuit, se ridică
                Gorilla_Animation_rows.STAND_TO_CROUCH.getRowIndex(); // Dacă stătea, se ghemuiește
            this.crouch = !this.crouch;  // Comută starea de ghemuire
        }
        wasCrouchPressed = crouch; // Actualizează starea anterioară a butonului
    }   

    /** @return true dacă flag-ul "up" este activat, false altfel (neutilizat pentru mișcare). */
    public boolean isUp() {
        return up;
    }

    /** Setează flag-ul "up". @param up true pentru a activa flag-ul. */
    public void setUp(boolean up) {
        this.up = up;
    }

    /** @return true dacă flag-ul "down" este activat, false altfel (neutilizat pentru mișcare). */
    public boolean isDown() {
        return down;
    }

    /** Setează flag-ul "down". @param down true pentru a activa flag-ul. */
    public void setDown(boolean down) {
        this.down = down;
    }

    /** @return true dacă Karagor încearcă să sară, false altfel. */
    public boolean isJump() {
        return jump;
    }

    /** Setează starea de săritură. @param jump true pentru a iniția o săritură. */
    public void setJump(boolean jump) {
        this.jump = jump;
    }
    
    /**
     * Returnează sănătatea curentă a lui Karagor.
     * @return Valoarea sănătății curente.
     */
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    /**
     * Returnează sănătatea maximă a lui Karagor.
     * @return Valoarea sănătății maxime.
     */
    public int getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Crește sănătatea lui Karagor cu o anumită valoare, fără a depăși sănătatea maximă.
     * @param amount Cantitatea de sănătate adăugată.
     */
    public void heal(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }
    
    /**
     * Aplică daune lui Karagor și inițiază starea "hurt" și efectul de knockback.
     * @param amount Cantitatea de daune primite.
     * @return true dacă Karagor este încă în viață după primirea daunelor, false altfel.
     */
    public boolean takeDamage(int amount) {
        currentHealth = Math.max(currentHealth - amount, 0); // Reduce sănătatea, minim 0
        isHurt = true; // Activează starea "hurt"
        hurtTimer = 0; // Resetează cronometrul pentru animația "hurt"
        resetAnimationTick(); // Resetează animația pentru a afișa animația "hurt" de la început

        // Aplică knockback
        float knockbackDirX = facingRight ? -2f : 2f; // Knockback în direcția opusă celei în care privește
        float knockbackDirY = -1f; // Knockback ușor în sus
        applyKnockback(knockbackDirX, knockbackDirY); // Metoda applyKnockback este moștenită din Player
        
        // Dacă este boss și moare, EnemyManager va gestiona drop-ul de cristal
        if (isBoss && !isAlive()) {
            // Aceasta va fi gestionată de EnemyManager
            return false; // Indică faptul că boss-ul a murit
        }
        
        // Returnează dacă inamicul este încă în viață
        return currentHealth > 0;
    }
    
    /**
     * Verifică dacă Karagor este în viață (are sănătate mai mare decât 0).
     * @return true dacă Karagor este în viață, false altfel.
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }
    
    /**
     * Resetează sănătatea lui Karagor la valoarea maximă.
     */
    public void resetHealth() {
        currentHealth = maxHealth;
    }

    /**
     * Setează datele nivelului pentru Karagor, folosite pentru coliziuni.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void setLevelData(int[][] levelData) {
        this.levelData = levelData;
    }

    /**
     * Verifică dacă Karagor execută un atac.
     * @return true dacă Karagor atacă, false altfel.
     */
    public boolean isAttacking() {
        return isAttacking;
    }
}
