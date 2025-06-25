package entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import main.Game;
import utilz.LoadSave;

/**
 * Reprezintă un obiect de tip "Gem" (piatră prețioasă) în joc.
 * Gem-urile sunt probabil obiecte speciale, posibil lăsate de boși la înfrângere.
 * Această clasă gestionează animația de plutire și scalare a gem-ului.
 * Nu extinde {@link Entity} direct, ci gestionează propriul hitbox.
 */
public class Gem {
    /** Coordonata x a colțului stânga-sus al gem-ului (pentru poziționarea inițială a hitbox-ului). */
    private float x;
    /** Coordonata y a colțului stânga-sus al gem-ului (pentru poziționarea inițială a hitbox-ului). */
    private float y;
    /** Imaginea (sprite-ul) gem-ului. */
    private BufferedImage image;
    /** Dreptunghiul de coliziune (hitbox) al gem-ului. */
    private Rectangle2D.Float hitbox;

    // Parametri pentru animația de plutire
    /** Coordonata Y inițială (de bază) pentru animația de plutire. */
    private float originalY;
    /** Viteza animației de plutire. */
    private float floatSpeed = 0.05f * Game.SCALE;
    /** Amplitudinea mișcării de plutire. */
    private float floatAmplitude = 1.0f * Game.SCALE;
    /** Unghiul curent în ciclul de plutire. */
    private float floatAngle = 0;

    // Parametri pentru animația de "respirație" (scalare)
    /** Lățimea originală a imaginii gem-ului, scalată cu {@link Game#SCALE}. */
    private float originalWidth;
    /** Înălțimea originală a imaginii gem-ului, scalată cu {@link Game#SCALE}. */
    private float originalHeight;
    /** Factorul de scalare curent pentru animația de respirație. */
    private float scaleFactor = 1.0f;
    /** Viteza cu care se schimbă factorul de scalare. */
    private float scaleSpeed = 0.005f;
    /** Factorul minim de scalare. */
    private float minScale = 0.9f;
    /** Factorul maxim de scalare. */
    private float maxScale = 1.1f;
    /** Indică dacă gem-ul se mărește (true) sau se micșorează (false) în animația de respirație. */
    private boolean scalingUp = true;

    /** Indică dacă gem-ul este activ (poate fi colectat și desenat). */
    private boolean active = true;
    /** ID-ul nivelului în care a fost generat gem-ul, pentru a determina tipul de gem (culoarea). */
    private int levelId;

    /**
     * Constructor pentru clasa Gem.
     * Inițializează gem-ul la o poziție specificată, cu un sprite corespunzător ID-ului nivelului.
     *
     * @param x Poziția x unde va fi centrat gem-ul.
     * @param y Poziția y unde va fi centrat gem-ul.
     * @param levelId ID-ul nivelului, folosit pentru a alege sprite-ul gem-ului.
     */
    public Gem(float x, float y, int levelId) {
        this.levelId = levelId;
        String gemSpritePath;
        // Alege calea către sprite-ul gem-ului în funcție de ID-ul nivelului
        switch (levelId) {
            case 1:
                gemSpritePath = LoadSave.GREEN_GEM;
                break;
            case 2:
                gemSpritePath = LoadSave.ORANGE_GEM;
                break;
            case 3:
            default: // Implicit, gem mov dacă ID-ul nivelului este neașteptat
                gemSpritePath = LoadSave.PURPLE_GEM;
                break;
        }
        this.image = LoadSave.getSpriteAtlas(gemSpritePath);

        if (this.image != null) {
            this.originalWidth = image.getWidth() * Game.SCALE;
            this.originalHeight = image.getHeight() * Game.SCALE;
            // Setează coordonatele x, y astfel încât gem-ul să fie centrat pe x, y-ul primit ca parametru
            this.x = x - (originalWidth / 2); 
            this.y = y - (originalHeight / 2); 
            this.originalY = this.y; // Y-ul original pentru animația de plutire
            this.hitbox = new Rectangle2D.Float(this.x, this.y, originalWidth, originalHeight);
        } else {
            System.err.println("Imaginea pentru Gem nu a fost încărcată! Sprite: " + gemSpritePath);
            this.active = false; // Dezactivează gem-ul dacă imaginea lipsește
        }
    }

    /**
     * Actualizează starea gem-ului.
     * Gestionează animația de plutire și de "respirație" (scalare).
     * Rulează doar dacă gem-ul este activ și are o imagine validă.
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
        // Actualizează poziția y a gem-ului (nu a hitbox-ului direct, pentru a menține originalY ca referință)
        this.y = originalY + (float) (Math.sin(floatAngle) * floatAmplitude);

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

        // Actualizează hitbox-ul pe baza poziției și scalei curente
        float scaledWidth = originalWidth * scaleFactor;
        float scaledHeight = originalHeight * scaleFactor;
        // Recalculează x-ul hitbox-ului pentru a-l menține centrat pe this.x (care este colțul stânga-sus al hitbox-ului nescalat)
        hitbox.x = this.x - (scaledWidth - originalWidth) / 2; 
        // Recalculează y-ul hitbox-ului pentru a-l menține centrat pe this.y (care este actualizat de animația de plutire)
        hitbox.y = this.y - (scaledHeight - originalHeight) / 2; 
        hitbox.width = scaledWidth;
        hitbox.height = scaledHeight;
    }

    /**
     * Desenează gem-ul pe ecran.
     * Imaginea este desenată la dimensiunile și poziția hitbox-ului curent (care include efectele de animație).
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     * @param xLvlOffset Offset-ul orizontal al nivelului pentru scrolling.
     */
    public void draw(Graphics g, int xLvlOffset) {
        if (!active || image == null) {
            return;
        }
        // Desenează imaginea la coordonatele și dimensiunile hitbox-ului actualizat
        // Hitbox-ul este deja ajustat pentru animația de plutire și scalare
        float drawX = hitbox.x - xLvlOffset;
        g.drawImage(image, (int) drawX, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height, null);
        
        // Pentru depanare, se poate desena hitbox-ul
        // g.setColor(Color.CYAN);
        // g.drawRect((int) drawX, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    /**
     * Returnează hitbox-ul gem-ului.
     * @return Obiectul {@link Rectangle2D.Float} reprezentând hitbox-ul.
     */
    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    /**
     * Verifică dacă gem-ul este activ.
     * @return {@code true} dacă gem-ul este activ, {@code false} altfel.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setează starea de activare a gem-ului.
     * Un gem inactiv nu este actualizat sau desenat și nu poate fi colectat.
     * @param active Noua stare de activare.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
