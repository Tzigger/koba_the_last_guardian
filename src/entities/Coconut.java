package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
// import java.awt.geom.Rectangle2D; // Nu este folosit direct Rectangle2D, ci moștenit prin Entity
import main.Game;
// import utilz.LoadSave; // Imaginea este primită prin constructor

/**
 * Reprezintă un obiect colectabil de tip "nucă de cocos" în joc.
 * Nucile de cocos pot fi folosite de jucător, probabil ca proiectile.
 * Această clasă gestionează animația de plutire și scalare a nucii de cocos.
 * Extinde clasa {@link Entity}.
 */
public class Coconut extends Entity {
    private BufferedImage image; // Imaginea sprite a nucii de cocos
    /** Indică dacă nuca de cocos este activă (poate fi colectată și desenată). */
    private boolean active = true;
    /** Datele nivelului, potențial pentru interacțiuni viitoare cu terenul (momentan neutilizat activ pentru nuci de cocos). */
    private int[][] levelData; 

    // Parametri pentru animația de plutire
    /** Coordonata Y inițială (de bază) pentru animația de plutire. */
    private float originalY; 
    /** Viteza animației de plutire. */
    private float floatSpeed = 0.05f * Game.SCALE;
    /** Amplitudinea mișcării de plutire. */
    private float floatAmplitude = 2.0f * Game.SCALE; 
    /** Unghiul curent în ciclul de plutire. */
    private float floatAngle = 0;

    // Parametri pentru animația de "respirație" (scalare)
    private float originalWidth; // Lățimea originală a imaginii (scalată)
    private float originalHeight; // Înălțimea originală a imaginii (scalată)
    /** Factorul de scalare curent. */
    private float scaleFactor = 1.0f;
    /** Viteza de schimbare a factorului de scalare. */
    private float scaleSpeed = 0.005f;
    /** Factorul minim de scalare. */
    private float minScale = 0.9f;
    /** Factorul maxim de scalare. */
    private float maxScale = 1.1f;
    /** Indică direcția scalării (mărire sau micșorare). */
    private boolean scalingUp = true;

    /**
     * Constructor pentru clasa Coconut.
     *
     * @param x Poziția x inițială a colțului stânga-sus al nucii de cocos.
     * @param y Poziția y inițială a colțului stânga-sus al nucii de cocos.
     * @param levelData Datele nivelului (momentan neutilizate activ).
     * @param image Imaginea (sprite-ul) pentru nuca de cocos.
     */
    public Coconut(float x, float y, int[][] levelData, BufferedImage image) {
        super(x, y, 0, 0); // Lățimea și înălțimea vor fi setate pe baza imaginii
        this.levelData = levelData; 
        this.image = image;

        if (this.image != null) {
            this.originalWidth = image.getWidth() * Game.SCALE;
            this.originalHeight = image.getHeight() * Game.SCALE;
            this.width = (int) originalWidth; // Setează lățimea entității
            this.height = (int) originalHeight; // Setează înălțimea entității
            initHitbox(x, y, originalWidth, originalHeight); // Inițializează hitbox-ul
            this.originalY = this.hitbox.y; // Setează Y-ul original pentru plutire
            // System.out.println("Coconut Constructor: Initialized with y = " + y + ", hitbox.y = " + this.hitbox.y);
        } else {
            System.err.println("Imaginea pentru nuca de cocos nu a fost încărcată sau pasată corect!");
            this.active = false; // Dezactivează dacă imaginea lipsește
            this.width = (int) (16 * Game.SCALE); // Dimensiuni implicite
            this.height = (int) (16 * Game.SCALE);
            initHitbox(x, y, this.width, this.height);
            this.originalY = this.hitbox.y;
        }
    }

    /**
     * Actualizează starea nucii de cocos.
     * Gestionează animația de plutire și de "respirație" (scalare).
     * Rulează doar dacă nuca de cocos este activă și are o imagine validă.
     */
    public void update() {
        if (!active || image == null) {
            return;
        }

        // Animația de plutire
        floatAngle += floatSpeed;
        if (floatAngle > Math.PI * 2) {
            floatAngle -= Math.PI * 2;
        }
        this.hitbox.y = originalY + (float) (Math.sin(floatAngle) * floatAmplitude);
        
        // Animația de "respirație" (scalare)
        if (scalingUp) {
            scaleFactor += scaleSpeed;
            if (scaleFactor >= maxScale) {
                scaleFactor = maxScale;
                scalingUp = false;
            }
        } else {
            scaleFactor -= scaleSpeed;
            if (scaleFactor <= minScale) {
                scaleFactor = minScale;
                scalingUp = true;
            }
        }
    }

    /**
     * Desenează nuca de cocos pe ecran.
     * Aplică animația de scalare pentru efectul de "respirație".
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     * @param xLvlOffset Offset-ul orizontal al nivelului pentru scrolling.
     */
    public void draw(Graphics g, int xLvlOffset) {
        if (!active || image == null) {
            return;
        }
        float drawX = hitbox.x - xLvlOffset; // Poziția X de desenare
        // Calculează dimensiunile de desenare pe baza factorului de scalare
        float drawWidth = originalWidth * scaleFactor;
        float drawHeight = originalHeight * scaleFactor;
        // Calculează offset-urile pentru a centra imaginea scalată
        float drawOffsetX = (drawWidth - hitbox.width) / 2;
        float drawOffsetY = (drawHeight - hitbox.height) / 2;

        g.drawImage(image, 
            (int) (drawX - drawOffsetX), 
            (int) (hitbox.y - drawOffsetY), // hitbox.y este deja actualizat de animația de plutire
            (int) drawWidth, 
            (int) drawHeight, null);
        
        // Pentru depanare, se poate desena hitbox-ul
        // drawHitbox(g, xLvlOffset); // Necesită suprascrierea metodei drawHitbox ca în Banana.java
    }
    
    /**
     * Verifică dacă nuca de cocos este activă.
     * @return {@code true} dacă este activă, {@code false} altfel.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setează starea de activare a nucii de cocos.
     * O nucă de cocos inactivă nu este actualizată sau desenată.
     * @param active Noua stare de activare.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
