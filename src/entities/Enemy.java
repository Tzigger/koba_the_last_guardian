package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

// import utilz.Enemy_Animation_Rows; // Este folosit în updateAnimationTick

/**
 * Clasă abstractă de bază pentru toți inamicii din joc.
 * Definește proprietăți și comportamente comune pentru inamici, cum ar fi
 * gestionarea animațiilor, starea (activ/inactiv) și tipul inamicului.
 * Extinde clasa {@link Entity}.
 */
public abstract class Enemy extends Entity{
    /** Indexul frame-ului curent în animația inamicului. */
    protected int aniIndex;
    /** Starea curentă a animației inamicului (de ex., IDLE, RUNNING, ATTACK). Folosește valori din {@link utilz.Enemy_Animation_Rows}. */
    protected int enemyState;
    /** Tipul specific al inamicului (de ex., GOBLIN, NANITE). Folosește constante definite în subclase sau {@link utilz.Constants.EnemyConstants}. */
    protected int enemyType;
    /** Contor pentru ciclul de animație. */
    protected int aniTick;
    /** Viteza animației (numărul de tick-uri de joc per frame de animație). */
    protected int aniSpeed = 5; // Valoare implicită, poate fi suprascrisă în subclase
    /** Flag pentru a desena sau nu hitbox-ul inamicului (pentru depanare). */
    protected boolean drawHitbox = false;
    /** Indică dacă inamicul este activ în joc (viu și participă la logică/desenare). */
    protected boolean isActive = true; 

    /**
     * Constructor pentru clasa abstractă Enemy.
     *
     * @param x Poziția x inițială a inamicului.
     * @param y Poziția y inițială a inamicului.
     * @param width Lățimea inamicului.
     * @param height Înălțimea inamicului.
     * @param enemyType Tipul specific al inamicului.
     */
    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        initHitbox(x, y, width, height); // Inițializează hitbox-ul cu dimensiunile date
    }

    /**
     * Actualizează contorul și indexul animației inamicului.
     * Gestionează buclarea animațiilor și tranziția la starea inactivă după animația de moarte.
     */
    protected void updateAnimationTick(){
        aniTick++;
        if(aniTick >= aniSpeed){
            aniTick = 0;
            aniIndex++;
            // Obține numărul maxim de cadre pentru starea curentă de animație
            // Presupune că enemyState este un index valid pentru utilz.Enemy_Animation_Rows.values()
            int maxFrames = 0;
            if (enemyState >= 0 && enemyState < utilz.Enemy_Animation_Rows.values().length) {
                 maxFrames = utilz.Enemy_Animation_Rows.values()[enemyState].getFrameCount();
            } else {
                // Stare de animație invalidă, resetează la 0 sau gestionează eroarea
                System.err.println("Stare de animație invalidă pentru inamic: " + enemyState);
                aniIndex = 0; 
                return;
            }

            if(aniIndex >= maxFrames){
                // Dacă este animația de moarte (presupunând că DYING este la indexul 0 în Enemy_Animation_Rows)
                // și animația s-a terminat, marchează inamicul ca inactiv.
                if (enemyState == utilz.Enemy_Animation_Rows.DYING.getRowIndex()) { 
                    aniIndex = maxFrames - 1; // Rămâne pe ultimul cadru al animației de moarte
                    isActive = false; // Marchează inamicul ca inactiv pentru eliminare
                } else {
                    // Pentru alte animații, reia de la început (buclă)
                    aniIndex = 0;
                }
            }
        }
    }

    /**
     * Metodă de actualizare generală pentru inamic.
     * Momentan, actualizează doar animația. Subclasele ar trebui să suprascrie
     * această metodă pentru a adăuga logica specifică de comportament (AI).
     *
     * @param playerHitbox Hitbox-ul jucătorului, pentru interacțiuni.
     */
    public void update(Rectangle2D.Float playerHitbox){
        updateAnimationTick();
    }
    
    /**
     * Desenează hitbox-ul inamicului dacă flag-ul {@code drawHitbox} este activ.
     * Util pentru depanare.
     *
     * @param g Contextul grafic {@link Graphics}.
     * @param xLvlOffset Offset-ul orizontal al nivelului pentru scrolling.
     */
    public void drawHitbox(Graphics g, int xLvlOffset) {
        if (drawHitbox) {
            g.setColor(Color.RED); // Culoare distinctă pentru hitbox-ul inamicilor
            g.drawRect((int)hitbox.x - xLvlOffset, (int)hitbox.y, (int)hitbox.width, (int)hitbox.height);
        }
    }

    /**
     * Returnează indexul frame-ului curent al animației.
     * @return Indexul frame-ului.
     */
    public int getAniIndex(){
        return aniIndex;
    }

    /**
     * Returnează starea curentă a animației inamicului.
     * @return Starea animației (valoare din {@link utilz.Enemy_Animation_Rows}).
     */
    public int getEnemyState(){
        return enemyState;
    }
    
    /**
     * Setează starea de animație a inamicului și resetează indexul și contorul animației.
     * @param state Noua stare de animație.
     */
    public void setEnemyState(int state){
        this.enemyState = state;
        this.aniIndex = 0; 
        this.aniTick = 0;  
    }

    /**
     * Returnează tipul specific al inamicului.
     * @return Tipul inamicului.
     */
    public int getEnemyType(){
        return enemyType;
    }
    
    /**
     * Returnează hitbox-ul inamicului (moștenit din {@link Entity}).
     * @return Hitbox-ul.
     */
    @Override // Suprascrie metoda din Entity (dacă Entity are getHitbox public)
    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }
    
    /**
     * Verifică dacă inamicul este activ în joc.
     * @return {@code true} dacă este activ, {@code false} altfel.
     */
    public boolean isActive() {
        return isActive;
    }
}
