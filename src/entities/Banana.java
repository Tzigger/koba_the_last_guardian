package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import main.Game;
// import utilz.HelpMethods; // Neutilizat momentan în această clasă
// import utilz.LoadSave; // Imaginea este primită prin constructor

/**
 * Reprezintă un obiect colectabil de tip "banană" în joc.
 * Bananele pot oferi bonusuri jucătorului la colectare.
 * Această clasă gestionează animația de plutire și scalare a bananei.
 * Extinde clasa {@link Entity}.
 */
public class Banana extends Entity {
    private BufferedImage image; // Imaginea sprite a bananei
    /** Indică dacă banana este activă (poate fi colectată și desenată). */
    private boolean active = true;
    /** Datele nivelului, potențial pentru interacțiuni viitoare cu terenul (momentan neutilizat activ pentru banane). */
    private int[][] levelData;

    // Parametri pentru animația de plutire
    /** Coordonata Y inițială (de bază) pentru animația de plutire. */
    private float originalY; 
    /** Viteza animației de plutire. */
    private float floatSpeed = 0.05f * Game.SCALE;
    /** Amplitudinea mișcării de plutire (cât de mult se mișcă sus/jos). */
    private float floatAmplitude = 2.0f * Game.SCALE; 
    /** Unghiul curent în ciclul de plutire (pentru funcția sinus). */
    private float floatAngle = 0;

    // Parametri pentru animația de "respirație" (scalare)
    private float originalWidth; // Lățimea originală a imaginii (scalată cu Game.SCALE)
    private float originalHeight; // Înălțimea originală a imaginii (scalată cu Game.SCALE)
    /** Factorul de scalare curent pentru animația de respirație. */
    private float scaleFactor = 1.0f;
    /** Viteza cu care se schimbă factorul de scalare. */
    private float scaleSpeed = 0.005f;
    /** Factorul minim de scalare. */
    private float minScale = 0.9f;
    /** Factorul maxim de scalare. */
    private float maxScale = 1.1f;
    /** Indică dacă banana se mărește (true) sau se micșorează (false) în animația de respirație. */
    private boolean scalingUp = true;

    /**
     * Constructor pentru clasa Banana.
     *
     * @param x Poziția x inițială a colțului stânga-sus al bananei.
     * @param y Poziția y inițială a colțului stânga-sus al bananei.
     * @param levelData Datele nivelului (momentan neutilizate activ pentru logica bananei).
     * @param image Imaginea (sprite-ul) pentru banană.
     */
    public Banana(float x, float y, int[][] levelData, BufferedImage image) {
        super(x, y, 0, 0); // Lățimea și înălțimea vor fi setate pe baza imaginii
        this.levelData = levelData;
        this.image = image;

        if (this.image != null) {
            this.originalWidth = image.getWidth() * Game.SCALE;
            this.originalHeight = image.getHeight() * Game.SCALE;
            this.width = (int) originalWidth; // Setează lățimea entității
            this.height = (int) originalHeight; // Setează înălțimea entității
            initHitbox(x, y, originalWidth, originalHeight); // Inițializează hitbox-ul cu dimensiunile imaginii
            this.originalY = this.hitbox.y; // Setează originalY pentru plutire pe baza poziției de spawn
        } else {
            System.err.println("Imaginea pentru banană nu a fost încărcată sau pasată corect!");
            this.active = false; // Dezactivează banana dacă imaginea lipsește
            // Setează dimensiuni implicite pentru a evita erori de hitbox
            this.width = (int) (16 * Game.SCALE); 
            this.height = (int) (16 * Game.SCALE);
            initHitbox(x, y, this.width, this.height);
            this.originalY = this.hitbox.y; // Setează originalY chiar și pentru hitbox-ul implicit
        }
    }

    /**
     * Actualizează starea bananei.
     * Gestionează animația de plutire și de "respirație" (scalare).
     * Rulează doar dacă banana este activă și are o imagine validă.
     */
    public void update() {
        if (!active || image == null) {
            return;
        }

        // Animația de plutire (sus și jos)
        floatAngle += floatSpeed;
        if (floatAngle > Math.PI * 2) { // Resetează unghiul pentru a menține valorile mici
            floatAngle -= Math.PI * 2;
        }
        // Calculează noua poziție Y pe baza originalY pentru a preveni deplasarea neintenționată
        this.hitbox.y = originalY + (float) (Math.sin(floatAngle) * floatAmplitude);
        
        // Animația de "respirație" (scalare)
        if (scalingUp) {
            scaleFactor += scaleSpeed;
            if (scaleFactor >= maxScale) {
                scaleFactor = maxScale;
                scalingUp = false; // Începe micșorarea
            }
        } else {
            scaleFactor -= scaleSpeed;
            if (scaleFactor <= minScale) {
                scaleFactor = minScale;
                scalingUp = true; // Începe mărirea
            }
        }
        // Notă: Hitbox-ul de coliziune rămâne constant; doar dimensiunea vizuală se schimbă.
    }

    /**
     * Desenează banana pe ecran.
     * Aplică animația de scalare pentru efectul de "respirație".
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     * @param xLvlOffset Offset-ul orizontal al nivelului pentru scrolling.
     */
    public void draw(Graphics g, int xLvlOffset) {
        if (!active || image == null) {
            return;
        }
        float drawX = hitbox.x - xLvlOffset; // Poziția X de desenare, ajustată cu offset-ul nivelului
        // Calculează dimensiunile de desenare pe baza factorului de scalare curent
        float drawWidth = originalWidth * scaleFactor;
        float drawHeight = originalHeight * scaleFactor;
        // Calculează offset-urile pentru a centra imaginea scalată pe hitbox-ul original
        float drawOffsetX = (drawWidth - hitbox.width) / 2;
        float drawOffsetY = (drawHeight - hitbox.height) / 2;

        g.drawImage(image, 
            (int) (drawX - drawOffsetX), // Ajustează X pentru centrare
            (int) (hitbox.y - drawOffsetY), // Ajustează Y pentru centrare (hitbox.y este deja actualizat de animația de plutire)
            (int) drawWidth, 
            (int) drawHeight, null);
        
        // Pentru depanare, se poate desena hitbox-ul
        // drawHitbox(g, xLvlOffset); 
    }
    
    /**
     * Desenează hitbox-ul bananei (suprascris din {@link Entity}).
     * Folosit pentru depanare.
     *
     * @param g Contextul grafic.
     * @param xLvlOffset Offset-ul orizontal al nivelului.
     */
     // Suprascrie metoda din Entity
    protected void drawHitbox(Graphics g, int xLvlOffset) {
        g.setColor(java.awt.Color.GREEN); // Culoare distinctă pentru hitbox-ul bananei
        g.drawRect((int) (hitbox.x - xLvlOffset), (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    /**
     * Verifică dacă banana este activă.
     * @return {@code true} dacă banana este activă, {@code false} altfel.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setează starea de activare a bananei.
     * O banană inactivă nu este actualizată sau desenată și nu poate fi colectată.
     * @param active Noua stare de activare.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Metoda getHitbox() este moștenită din clasa Entity.
}
