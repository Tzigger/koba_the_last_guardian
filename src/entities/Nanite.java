package entities;

import java.awt.geom.Rectangle2D;

import main.Game;
import utilz.HelpMethods;
import utilz.Enemy_Animation_Rows;

/**
 * Reprezintă entitatea Nanite în joc.
 * Această clasă extinde clasa Enemy și definește comportamentul specific
 * și atributele pentru Nanites, inamici mici și agili.
 * Include tipuri de Nanite (Jungla, Pestera), stări, logică de patrulare,
 * atac și interacțiune cu jucătorul.
 */
public class Nanite extends Enemy {
    
    // Nanite Types
    /** Tipul de Nanite "Jungla" (implicit). */
    public static final int NANITE_JUNGLA = 0; // Default type
    /** Tipul de Nanite "Pestera" (variantă de peșteră). */
    public static final int NANITE_PESTERA = 1; // Cave variant

    // Nanite states
    /** Starea IDLE a Nanite-ului. */
    public static final int IDLE = 3;
    /** Starea RUNNING (alergare) a Nanite-ului. */
    public static final int RUNNING = 10;
    /** Starea ATTACK (atac) a Nanite-ului (corespunde animației SLASHING). */
    public static final int ATTACK = 12; // SLASHING
    /** Starea HURT (lovit) a Nanite-ului. */
    public static final int HURT = 2;
    /** Starea DYING (moarte) a Nanite-ului. */
    public static final int DYING = 0;
    
    // Nanite attributes
    /** Sănătatea curentă a Nanite-ului. */
    private int health;
    /** Sănătatea maximă a Nanite-ului. */
    private int maxHealth;
    /** Daunele provocate de atacul Nanite-ului. */
    private int damage;
    /** Indicator dacă Nanite-ul este activ în joc. */
    private boolean isActive = true;
    /** Distanța de atac a Nanite-ului. */
    private int attackRange = 30;
    /** Numărul de tick-uri petrecute în starea curentă. */
    private int ticksInState = 0;
    /** Cooldown-ul dintre atacuri. */
    private int attackCooldown = 0;
    /** Cooldown-ul maxim dintre atacuri (aproximativ 2 secunde la 60 FPS). */
    private static final int ATTACK_COOLDOWN_MAX = 120;
    /** Indicator dacă lovitura de atac a fost verificată în animația curentă. */
    private boolean attackChecked = false;
    /** Indicator dacă jucătorul a fost detectat de Nanite. */
    private boolean playerDetected = false;
    
    // Patrol boundaries (calculated based on spawn point)
    /** Limita stângă a zonei de patrulare. */
    private float leftPatrolLimit;
    /** Limita dreaptă a zonei de patrulare. */
    private float rightPatrolLimit;
    /** Indicator dacă limitele de patrulare au fost setate. */
    private boolean patrolBoundariesSet = false;
    
    // Movement
    /** Indicator dacă Nanite-ul se mișcă. */
    private boolean isMoving = false;
    /** Direcția de mișcare a Nanite-ului (1 = dreapta, -1 = stânga). */
    private int direction = 1;
    /** Viteza de mișcare curentă a Nanite-ului. */
    private float moveSpeed = 0.5f;
    /** Viteza de mișcare în timpul patrulării. */
    private float patrolMoveSpeed;
    /** Viteza de mișcare în timpul urmăririi jucătorului. */
    private float chaseMoveSpeed;
    /** Datele nivelului curent, folosite pentru coliziuni și navigație. */
    private int[][] levelData;
    /** Indicator dacă Nanite-ul se află în aer. */
    private boolean inAir = false;
    /** Viteza verticală a Nanite-ului în aer. */
    private float airSpeed = 0f;
    /** Valoarea gravitației aplicate Nanite-ului. */
    private float gravity = 0.04f;
    /** Viteza de săritură a Nanite-ului. */
    private float jumpSpeed = -3.5f * Game.SCALE;
    
    // Tracking
    /** Hitbox-ul pentru atacul Nanite-ului. */
    private Rectangle2D.Float attackBox;
    /** Cooldown-ul pentru daunele provocate prin atingerea jucătorului. */
    private int playerTouchCooldown = 0;
    /** Cooldown-ul dintre daunele provocate prin atingere (în frame-uri). */
    private int touchDamageCooldown = 60;
     
    // Patrolling
    /** Distanța pe care Nanite-ul o patrulează în fiecare direcție de la punctul de start. */
    private float patrolDistance = 100.0f;
    /** Distanța la care Nanite-ul poate detecta jucătorul. */
    private int detectionRange = 300;
    
    /**
     * Constructor pentru clasa Nanite.
     * Inițializează Nanite-ul cu o poziție, dimensiuni și tip specific (Jungla sau Pestera).
     * Setează atributele în funcție de tip, inițializează parametrii de patrulare,
     * starea inițială și hitbox-ul.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param width Lățimea entității (de obicei dimensiunea sprite-ului).
     * @param height Înălțimea entității (de obicei dimensiunea sprite-ului).
     * @param enemyType Tipul Nanite-ului (NANITE_JUNGLA sau NANITE_PESTERA).
     */
    public Nanite(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height, enemyType); // enemyType va fi NANITE_JUNGLA sau NANITE_PESTERA
        
        if (enemyType == NANITE_PESTERA) {
            this.maxHealth = 50; // Sănătate mai mare pentru varianta Pestera
            this.health = this.maxHealth;
            this.damage = 5;     // Daune mai mari pentru varianta Pestera
        } else { // Implicit statisticile pentru Nanite Jungla
            this.maxHealth = 30;
            this.health = this.maxHealth;
            this.damage = 3;
        }
        
        // Setează parametrii de patrulare
        this.patrolMoveSpeed = 0.4f * Game.SCALE;
        this.chaseMoveSpeed = 0.9f * Game.SCALE;
        this.moveSpeed = this.patrolMoveSpeed; // Începe cu viteza de patrulare
        
        // Setează limitele de patrulare inițiale
        this.leftPatrolLimit = x - patrolDistance;
        this.rightPatrolLimit = x + patrolDistance;
        
        // Inițializează starea
        setState(IDLE);
        
        // Setează hitbox-ul corespunzător pentru Nanite
        // Folosește decalaje pentru a alinia hitbox-ul cu partea vizibilă a sprite-ului
        float localXDrawOffset = 29; 
        float localYDrawOffset = 37; // Original 17, mută hitbox-ul în jos cu 20px pentru ca sprite-ul să pară mai sus
        
        // Utilizează un hitbox care se potrivește mai bine cu partea vizibilă a sprite-ului
        float hitboxWidth = width - 55;  // Ajustat pentru a fi mai potrivit pentru sprite
        float hitboxHeight = height - 23; // Ajustat pentru a fi mai potrivit pentru sprite.
        // Poziția hitbox-ului este relativă la x, y original (colțul stânga-sus al locului unde ar fi sprite-ul fără decalaj)
        // plus decalajele locale.
        float hitboxX = x + localXDrawOffset;
        float hitboxY = y + localYDrawOffset;
        
        initHitbox(hitboxX, hitboxY, (int)hitboxWidth, (int)hitboxHeight);
        
        // Activează desenarea hitbox-ului pentru debugging (setat la false pentru release)
        drawHitbox = false;
        
        // Inițializează hitbox-ul de atac în fața Nanite-ului
        attackBox = new Rectangle2D.Float(hitbox.x + hitbox.width, hitbox.y, attackRange, hitbox.height);
    }
    
    /**
     * Actualizează starea Nanite-ului.
     * Aceasta include actualizarea hitbox-ului de atac, cooldown-urilor, comportamentului
     * și poziției. De asemenea, inițializează limitele de patrulare dacă este necesar.
     * @param playerHitbox Hitbox-ul jucătorului, pentru interacțiuni.
     */
    @Override
    public void update(Rectangle2D.Float playerHitbox) { // Modificat pentru a accepta playerHitbox
        super.update(playerHitbox); // Pasează playerHitbox la superclasă
        
        if (!isActive) // Dacă Nanite-ul nu este activ, nu face nimic
            return;
            
        updateAttackBox(); // Actualizează hitbox-ul de atac
        updateCooldowns(); // Actualizează cooldown-urile
        updateBehavior(playerHitbox); // Actualizează comportamentul (pasează playerHitbox)
        updatePosition(); // Actualizează poziția
        
        // Setează limitele de patrulare odată ce inamicul a aterizat pe pământ
        if (!inAir && !patrolBoundariesSet) {
            initPatrolBoundaries();
        }
    }
    
    /**
     * Actualizează poziția și dimensiunea hitbox-ului de atac în funcție de direcția Nanite-ului.
     */
    private void updateAttackBox() {
        if (direction > 0) { // Orientat spre dreapta
            // Hitbox-ul de atac este în partea dreaptă
            attackBox.x = hitbox.x + hitbox.width;
        } else { // Orientat spre stânga
            // Hitbox-ul de atac este în partea stângă
            attackBox.x = hitbox.x - attackRange;
        }
        attackBox.y = hitbox.y; // Aliniat vertical cu hitbox-ul principal
    }
    
    /**
     * Actualizează cronometrele de cooldown pentru atac și atingerea jucătorului.
     * Incrementează și contorul de tick-uri în starea curentă.
     */
    private void updateCooldowns() {
        ticksInState++; // Incrementează numărul de tick-uri petrecute în starea curentă
        if (attackCooldown > 0) { // Dacă cooldown-ul de atac este activ
            attackCooldown--; // Decrementează cooldown-ul
        }
        if (playerTouchCooldown > 0) { // Dacă cooldown-ul de atingere a jucătorului este activ
            playerTouchCooldown--; // Decrementează cooldown-ul
        }
    }
    
    /**
     * Inițializează limitele de patrulare pentru Nanite pe baza poziției sale curente.
     * Această metodă ar trebui apelată după ce Nanite-ul s-a așezat pe o platformă.
     * Caută marginile platformei pentru a stabili limitele, asigurând o zonă minimă de patrulare.
     */
    private void initPatrolBoundaries() {
        // Limite de patrulare implicite (aproximativ +/- 3 tile-uri de la punctul de spawn)
        leftPatrolLimit = Math.max(0, hitbox.x - Game.TILES_SIZE * 3);
        rightPatrolLimit = hitbox.x + Game.TILES_SIZE * 3;
        
        // Caută marginile platformei
        boolean leftEdgeFound = false;
        boolean rightEdgeFound = false;
        
        // Caută la stânga pentru a găsi marginea platformei
        for (int i = 1; i <= 6; i++) { // Verifică până la 6 tile-uri distanță
            float checkX = hitbox.x - (i * Game.TILES_SIZE);
            // Dacă iese din nivel sau nu mai este pe podea
            if (checkX < 0 || !HelpMethods.isEntityOnFloor(new Rectangle2D.Float(checkX, hitbox.y, hitbox.width, hitbox.height), levelData)) {
                leftPatrolLimit = hitbox.x - ((i-1) * Game.TILES_SIZE); // Rămâne la un tile de margine
                leftEdgeFound = true;
                break;
            }
        }
        
        // Caută la dreapta pentru a găsi marginea platformei
        for (int i = 1; i <= 6; i++) { // Verifică până la 6 tile-uri distanță
            float checkX = hitbox.x + (i * Game.TILES_SIZE);
            // Dacă nu mai este pe podea (nu e nevoie să verifici ieșirea din nivel la dreapta dacă nivelul are o limită)
            if (!HelpMethods.isEntityOnFloor(new Rectangle2D.Float(checkX, hitbox.y, hitbox.width, hitbox.height), levelData)) {
                rightPatrolLimit = hitbox.x + ((i-1) * Game.TILES_SIZE); // Rămâne la un tile de margine
                rightEdgeFound = true;
                break;
            }
        }
        
        // Asigură-te că zona de patrulare are cel puțin 2 tile-uri lățime
        if (leftEdgeFound && rightEdgeFound && rightPatrolLimit - leftPatrolLimit < Game.TILES_SIZE * 2) {
            float midpoint = (leftPatrolLimit + rightPatrolLimit) / 2;
            leftPatrolLimit = midpoint - Game.TILES_SIZE;
            rightPatrolLimit = midpoint + Game.TILES_SIZE;
        }
        
        patrolBoundariesSet = true; // Marchează că limitele au fost setate
    }
    
    /**
     * Verifică dacă Nanite-ul poate vedea jucătorul.
     * Condițiile includ distanța orizontală, alinierea verticală (aproximativ același nivel)
     * și direcția în care privește Nanite-ul.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă jucătorul este vizibil, false altfel.
     */
    private boolean canSeePlayer(Rectangle2D.Float playerHitbox) {
        // Verifică dacă jucătorul este în raza de detecție
        int playerX = (int) playerHitbox.getCenterX();
        int naniteX = (int) hitbox.getCenterX();
        if (Math.abs(playerX - naniteX) > detectionRange) { // Dacă distanța orizontală e prea mare
            return false;
        }

        // Verifică dacă jucătorul este aliniat vertical (ex: la o diferență de maxim un tile înălțime)
        int playerY = (int) playerHitbox.getCenterY();
        int naniteY = (int) hitbox.getCenterY();
        if (Math.abs(playerY - naniteY) > Game.TILES_SIZE) { // Permite o diferență de un tile
            return false;
        }

        // Verifică dacă jucătorul este în direcția în care privește Nanite-ul
        if (direction > 0) { // Privește la dreapta
            return playerX > naniteX; // Jucătorul trebuie să fie la dreapta Nanite-ului
        } else { // Privește la stânga
            return playerX < naniteX; // Jucătorul trebuie să fie la stânga Nanite-ului
        }
    }
    
    /**
     * Actualizează comportamentul Nanite-ului pe baza stării curente și a interacțiunii cu jucătorul.
     * Gestionează tranzițiile între stări (IDLE, RUNNING, ATTACK, HURT, DYING) și logica specifică fiecărei stări,
     * inclusiv patrularea și atacul.
     * @param playerHitbox Hitbox-ul jucătorului, pentru a lua decizii.
     */
    private void updateBehavior(Rectangle2D.Float playerHitbox) { // Modificat pentru a accepta playerHitbox
        // Actualizează flag-ul playerDetected
        playerDetected = canSeePlayer(playerHitbox);

        // Comportamentul în funcție de stare
        switch (getEnemyState()) {
            case IDLE:
                if (ticksInState > 60) { // După un timp în IDLE
                    ticksInState = 0;
                    // Începe să se miște dacă nu se mișca deja
                    setState(RUNNING);
                    isMoving = true;
                }
                break;
                
            case RUNNING:
                isMoving = true; // Este în mișcare
                
                // Verifică dacă poate ataca jucătorul
                if (playerDetected && attackCooldown <= 0) { // Dacă jucătorul e detectat și nu e în cooldown de atac
                    setState(ATTACK); // Trece la starea de atac
                }
                // Verifică limitele de patrulare dacă nu urmărește jucătorul
                else if (!playerDetected && patrolBoundariesSet) {
                    // Dacă a atins limita de patrulare, schimbă direcția
                    if ((direction > 0 && hitbox.x >= rightPatrolLimit) || 
                        (direction < 0 && hitbox.x <= leftPatrolLimit)) {
                        direction *= -1; // Inversează direcția
                        
                        // Ocazional, face o pauză la capetele patrulării
                        if (Math.random() < 0.3) { // 30% șansă de pauză
                            setState(IDLE);
                            isMoving = false;
                            ticksInState = 0;
                        }
                    }
                }
                break;
                
            case ATTACK:
                isMoving = false; // Nu se mișcă în timpul atacului
                // Verifică lovitura de atac o singură dată în timpul animației
                if (!attackChecked && ticksInState > 6) { // Verifică după câteva tick-uri (sincronizare cu animația)
                    checkAttackHit(); // Metodă placeholder pentru verificarea loviturii
                    attackChecked = true; // Marchează că lovitura a fost verificată
                    attackCooldown = ATTACK_COOLDOWN_MAX; // Setează cooldown-ul după atac
                }
                
                // Revine la starea anterioară după terminarea animației de atac
                // Presupunând că Enemy_Animation_Rows.values()[ATTACK] returnează informații despre animația de atac
                if (ticksInState >= Enemy_Animation_Rows.values()[ATTACK].getFrameCount() * 5) { // Durata animației (5 tick-uri per frame)
                    attackChecked = false; // Resetează pentru următorul atac
                    setState(IDLE); // Revine la IDLE
                }
                break;
                
            case HURT:
                isMoving = false; // Nu se mișcă când este lovit
                // Presupunând că Enemy_Animation_Rows.values()[HURT] returnează informații despre animația de lovire
                if (ticksInState >= Enemy_Animation_Rows.values()[HURT].getFrameCount() * 5) { // Durata animației de lovire
                    if (health <= 0) { // Dacă sănătatea a ajuns la 0 sau mai puțin
                        setState(DYING); // Trece la starea de moarte
                    } else {
                        setState(IDLE); // Altfel, revine la IDLE
                    }
                }
                break;
                
            case DYING:
                isMoving = false; // Nu se mișcă când moare
                // Presupunând că Enemy_Animation_Rows.values()[DYING] returnează informații despre animația de moarte
                if (ticksInState >= Enemy_Animation_Rows.values()[DYING].getFrameCount() * 5) { // Durata animației de moarte
                    isActive = false; // Devine inactiv după terminarea animației
                }
                break;
        }
    }
    
    /**
     * Actualizează poziția Nanite-ului.
     * Aplică gravitația, gestionează mișcarea verticală și orizontală,
     * verifică coliziunile cu pereții și podeaua, și previne căderea de pe platforme.
     */
    private void updatePosition() {
        boolean justLanded = false; // Flag pentru a indica dacă a aterizat în acest frame

        if (inAir) { // Dacă este în aer
            // Aplică gravitația și verifică coliziunea verticală
            float airSpeedY = airSpeed;
            float nextY = hitbox.y + airSpeedY; // Calculează noua poziție Y

            if (HelpMethods.canMoveHere(hitbox.x, nextY, hitbox.width, hitbox.height, levelData)) { // Verifică dacă se poate mișca
                hitbox.y = nextY; // Actualizează poziția Y
                airSpeed += gravity; // Aplică gravitația (crește viteza de cădere)
            } else {
                // Coliziune detectată (podea sau tavan)
                if (airSpeed > 0) { // Cădea în jos - a aterizat pe podea
                    int tileYLanding = (int) ((hitbox.y + hitbox.height + airSpeedY) / Game.TILES_SIZE);
                    float oldY = hitbox.y;
                    hitbox.y = (float) (tileYLanding * Game.TILES_SIZE - hitbox.height - 1.0f); // Plasează cu 1px mai sus de podea
                    System.out.println(String.format("Nanite landing: oldY=%.2f, airSpeedY=%.2f, newY=%.2f, tileYLanding=%d", oldY, airSpeedY, hitbox.y, tileYLanding));
                    
                    inAir = false; // Nu mai este în aer
                    airSpeed = 0; // Resetează viteza aeriană
                    justLanded = true; // Marchează că tocmai a aterizat
                } else if (airSpeed < 0) { // Se mișca în sus - a lovit tavanul
                    int tileYHitting = (int) ((hitbox.y + airSpeedY) / Game.TILES_SIZE);
                    hitbox.y = (float) ((tileYHitting + 1) * Game.TILES_SIZE); // Plasează sub tavan
                    airSpeed = 0; // Oprește mișcarea ascendentă
                }
            }
        }

        // Verifică dacă inamicul este pe podea, dar numai dacă nu a aterizat în acest frame exact.
        // Acest lucru previne setarea lui inAir la true imediat după un calcul de aterizare.
        if (!justLanded && !inAir) {
            if (!HelpMethods.isEntityOnFloor(hitbox, levelData)) { // Dacă nu este pe podea
                System.out.println(String.format("Nanite at (%.2f, %.2f) found not on floor. Becoming airborne.", hitbox.x, hitbox.y));
                inAir = true; // Devine aerian
            }
        }

        // Verifică dacă este pe cale să cadă de pe margine și se întoarce dacă da
        if (!willLandOnGround(hitbox.x + direction * moveSpeed, hitbox.y)) {
            direction *= -1; // Inversează direcția
        }
        
        // Mișcare orizontală
        float nextX = hitbox.x + direction * moveSpeed; // Calculează noua poziție X
        if (HelpMethods.canMoveHere(nextX, hitbox.y, hitbox.width, hitbox.height, levelData)) { // Verifică dacă se poate mișca
            hitbox.x = nextX; // Actualizează poziția X
        } else {
            // A lovit un perete, schimbă direcția
            direction *= -1;
        }
        
        // Actualizează poziția entității pe baza hitbox-ului (pentru desenare)
        x = hitbox.x; // Poziția de desenare X (poate necesita ajustare cu offset)
        y = hitbox.y; // Poziția de desenare Y (poate necesita ajustare cu offset)
        
        // Actualizează poziția hitbox-ului de atac
        updateAttackBox();
    }
    
    /**
     * Verifică dacă Nanite-ul va ateriza pe o suprafață solidă la poziția specificată.
     * Creează un hitbox de test la poziția viitoare și verifică dacă este pe podea.
     * @param x Coordonata X a poziției viitoare.
     * @param y Coordonata Y a poziției viitoare.
     * @return true dacă Nanite-ul va ateriza pe sol, false altfel.
     */
    private boolean willLandOnGround(float x, float y) {
        // Creează un hitbox de test la poziția viitoare
        Rectangle2D.Float testHitbox = new Rectangle2D.Float(x, y, hitbox.width, hitbox.height);
        return HelpMethods.isEntityOnFloor(testHitbox, levelData); // Verifică dacă este pe podea
    }
    
    /**
     * Verifică dacă atacul Nanite-ului a lovit jucătorul.
     * Într-un joc real, aici s-ar verifica coliziunea cu hitbox-ul jucătorului
     * și s-ar aplica daune dacă există o coliziune. (Momentan, este un placeholder).
     */
    private void checkAttackHit() {
        // Într-un joc real, aici s-ar verifica coliziunea cu hitbox-ul jucătorului
        // și s-ar aplica daune dacă există o coliziune
    }
    
    /**
     * Aplică daune Nanite-ului atunci când este atacat.
     * Reduce sănătatea și gestionează tranziția la starea HURT sau DYING.
     * @param damage Cantitatea de daune primite.
     */
    public void takeDamage(int damage) {
        // Nu primește daune dacă este deja în curs de moarte
        if (getEnemyState() == DYING)
            return;
            
        // Aplică daunele
        health -= damage;
        
        // Verifică dacă Nanite-ul a murit
        if (health <= 0) {
            health = 0; // Sănătatea nu poate fi negativă
            setState(DYING); // Setează starea la moarte
            isMoving = false; // Oprește mișcarea
            airSpeed = 0;     // Oprește mișcarea verticală
            System.out.println("Nanite is dying!"); // Mesaj de debug
        } else {
            // Dacă nu a murit, arată doar animația de lovire
            setState(HURT);
        }
    }
    
    /**
     * Verifică dacă jucătorul atinge Nanite-ul și aplică daune dacă este cazul și nu este în cooldown.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă jucătorul a lovit Nanite-ul și daunele au fost aplicate (sau ar trebui aplicate), false altfel.
     */
    public boolean checkPlayerHit(Rectangle2D.Float playerHitbox) {
        // Dacă Nanite-ul nu este activ sau este în cooldown de atingere, nu face nimic
        if (!isActive || playerTouchCooldown > 0)
            return false;
            
        // Dacă hitbox-urile se intersectează
        if (hitbox.intersects(playerHitbox)) {
            playerTouchCooldown = touchDamageCooldown; // Setează cooldown-ul de atingere
            return true; // Indică faptul că a avut loc o coliziune
        }
        return false; // Nu a avut loc nicio coliziune
    }
    
    /**
     * Verifică dacă Nanite-ul poate ataca jucătorul (dacă jucătorul este în raza de atac și Nanite-ul nu este în cooldown).
     * Dacă da, inițiază starea de atac.
     * @param playerHitbox Hitbox-ul jucătorului.
     * @return true dacă Nanite-ul poate ataca și a inițiat atacul, false altfel.
     */
    public boolean canAttackPlayer(Rectangle2D.Float playerHitbox) {
        // Dacă Nanite-ul nu este activ, este în cooldown de atac sau este deja într-o stare care previne atacul (ATTACK, HURT, DYING)
        if (!isActive || attackCooldown > 0 || getEnemyState() == ATTACK || getEnemyState() == HURT || getEnemyState() == DYING)
            return false;
            
        // Dacă hitbox-ul de atac se intersectează cu hitbox-ul jucătorului
        if (attackBox.intersects(playerHitbox)) {
            setState(ATTACK); // Setează starea la atac
            attackCooldown = ATTACK_COOLDOWN_MAX; // Setează cooldown-ul de atac
            ticksInState = 0; // Resetează tick-urile în noua stare
            attackChecked = false; // Resetează flag-ul de verificare a loviturii
            return true; // Indică faptul că atacul a fost inițiat
        }
        return false; // Nu poate ataca
    }
    
    /**
     * Detectează jucătorul dacă se află în raza de vizualizare și ajustează direcția
     * Nanite-ului pentru a se îndrepta către jucător.
     * Trece în modul de urmărire (chase) dacă jucătorul este detectat și Nanite-ul era în IDLE.
     * Revine la modul de patrulare dacă jucătorul iese din raza de detecție extinsă.
     * @param playerX Coordonata X a jucătorului.
     * @param playerY Coordonata Y a jucătorului.
     */
    public void playerDetected(float playerX, float playerY) {
        // Dacă Nanite-ul nu este activ sau este într-o stare care previne detectarea/urmărirea, nu face nimic
        if (!isActive || getEnemyState() == ATTACK || getEnemyState() == HURT || getEnemyState() == DYING)
            return;
            
        // Calculează distanța dintre Nanite și jucător
        float xDistance = Math.abs(playerX - hitbox.x);
        float yDistance = Math.abs(playerY - hitbox.y);
        float distance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        
        if (distance <= detectionRange) { // Dacă jucătorul este în raza de detecție
            // Jucător detectat - modul de urmărire
            playerDetected = true;
            moveSpeed = chaseMoveSpeed; // Crește viteza la cea de urmărire
            
            // Se întoarce către jucător
            if (playerX < hitbox.x) {
                direction = -1; // Privește la stânga
            } else {
                direction = 1;  // Privește la dreapta
            }
            
            // Începe să se miște către jucător dacă era în IDLE
            if (getEnemyState() == IDLE) {
                setState(RUNNING);
                isMoving = true;
            }
        } else if (playerDetected && distance > detectionRange * 1.5f) { // Dacă jucătorul a ieșit din raza de detecție extinsă
            // Jucătorul a ieșit din raza de acțiune - revine la modul de patrulare
            playerDetected = false;
            moveSpeed = patrolMoveSpeed; // Revine la viteza de patrulare
        }
    }
    
    /**
     * Setează starea curentă a Nanite-ului și resetează cronometrul pentru starea respectivă.
     * @param state Noua stare a Nanite-ului (folosind constantele definite în clasă, ex: IDLE, RUNNING).
     */
    private void setState(int state) {
        this.setEnemyState(state); // Metodă din clasa părinte Enemy
        ticksInState = 0; // Resetează numărul de tick-uri petrecute în starea curentă
    }
    
    /**
     * Setează datele nivelului pentru Nanite, folosite pentru coliziuni și navigație.
     * @param levelData O matrice bidimensională reprezentând tile-urile nivelului.
     */
    public void setLevelData(int[][] levelData) {
        this.levelData = levelData;
    }
    
    /**
     * Verifică dacă Nanite-ul este activ în joc.
     * @return true dacă Nanite-ul este activ, false altfel.
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Returnează sănătatea curentă a Nanite-ului.
     * @return Sănătatea curentă.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Returnează sănătatea maximă a Nanite-ului.
     * @return Sănătatea maximă.
     */
    public int getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Returnează daunele de atac ale Nanite-ului.
     * @return Daunele de atac.
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * Returnează direcția curentă a Nanite-ului.
     * @return Direcția (1 pentru dreapta, -1 pentru stânga).
     */
    public int getDirection() {
        return direction;
    }
    
    /**
     * Transformă acest Nanite într-un boss (Karagor).
     * Această metodă pare a fi un placeholder sau o funcționalitate specifică jocului
     * unde un Nanite se poate transforma. Mărește atributele și dimensiunea.
     */
    public void makeBoss() {
        // Crește statisticile pentru boss
        this.maxHealth = 100; // Sănătate mult mai mare
        this.health = maxHealth;
        this.damage = 20;      // Daune mai mari
        
        // Mărește dimensiunea (de 1.5x mai mare decât Nanite-urile obișnuite)
        float bossSizeMultiplier = 1.5f;
        
        // Ajustează hitbox-ul
        float newHitboxWidth = hitbox.width * bossSizeMultiplier;
        float newHitboxHeight = hitbox.height * bossSizeMultiplier;
        
        // Centrează hitbox-ul la aceeași poziție
        float newHitboxX = hitbox.x - (newHitboxWidth - hitbox.width) / 2;
        float newHitboxY = hitbox.y - (newHitboxHeight - hitbox.height) / 2;
        
        // Actualizează hitbox-ul cu noua dimensiune
        initHitbox(newHitboxX, newHitboxY, (int)newHitboxWidth, (int)newHitboxHeight);
        
        // Mărește raza de atac
        attackRange *= 1.5f;
        
        // Actualizează hitbox-ul de atac
        updateAttackBox();
        
        // Mărește vitezele de mișcare
        this.patrolMoveSpeed *= 0.8f; // Patrulare mai lentă (mai amenințător)
        this.chaseMoveSpeed *= 1.2f;  // Urmărire mai rapidă
        this.moveSpeed = this.patrolMoveSpeed; // Începe cu viteza de patrulare
        
        // Ajustează distanța de patrulare pentru comportamentul de boss
        this.patrolDistance *= 1.5f;
        
        // Setează raza de detecție a boss-ului (mai mare decât la Nanite-urile obișnuite)
        this.detectionRange = 400; // Boss-ul poate vedea jucătorul de mai departe
        
        // Începe în starea IDLE
        setState(IDLE);
    }
    
    /**
     * Returnează hitbox-ul de atac al Nanite-ului.
     * @return Dreptunghiul reprezentând hitbox-ul de atac.
     */
    public Rectangle2D.Float getAttackBox() {
        return attackBox;
    }
}
