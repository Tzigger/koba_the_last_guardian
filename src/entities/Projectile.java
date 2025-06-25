package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage; // For potential sprite
import utilz.LoadSave; // For potential sprite

/**
 * Reprezintă un proiectil în joc.
 * Această clasă extinde clasa Entity și definește comportamentul specific
 * și atributele pentru proiectile, cum ar fi direcția, viteza, daunele
 * și starea (activ/inactiv). Poate fi desenat ca un simplu dreptunghi
 * sau folosind un sprite.
 */
public class Projectile extends Entity {

    /** Indicator dacă proiectilul este activ și ar trebui actualizat/desenat. */
    private boolean active = true;
    /** Direcția proiectilului (0 pentru stânga, 1 pentru dreapta, folosind constantele din Enemy_Animation_Rows.Directions). */
    private int direction;
    /** Viteza de deplasare a proiectilului, scalată cu dimensiunea jocului. */
    private float speed = 4.0f * main.Game.SCALE;
    /** Imaginea (sprite-ul) pentru proiectil. Poate fi null dacă se dorește un dreptunghi simplu. */
    private BufferedImage img;
    /** Daunele pe care le provoacă proiectilul la impact. */
    private int damage;

    /**
     * Constructor pentru proiectile simple, desenate ca dreptunghiuri.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param width Lățimea proiectilului.
     * @param height Înălțimea proiectilului.
     * @param direction Direcția de mișcare (0 = stânga, 1 = dreapta).
     * @param damage Daunele provocate de proiectil.
     */
    public Projectile(float x, float y, int width, int height, int direction, int damage) {
        super(x, y, width, height);
        initHitbox(x, y, width, height); 
        this.direction = direction;
        this.img = null; // Fără sprite pentru acest constructor
        this.damage = damage;
    }

    /**
     * Constructor pentru proiectile bazate pe sprite-uri.
     * Lățimea și înălțimea sunt derivate din imagine și scalate.
     * @param x Poziția inițială pe axa X.
     * @param y Poziția inițială pe axa Y.
     * @param direction Direcția de mișcare (0 = stânga, 1 = dreapta).
     * @param img Imaginea (sprite-ul) pentru proiectil.
     * @param damage Daunele provocate de proiectil.
     */
    public Projectile(float x, float y, int direction, BufferedImage img, int damage) {
        // Lățimea și înălțimea vor fi derivate din imagine, scalate
        super(x, y, (int)(img.getWidth() * main.Game.SCALE), (int)(img.getHeight() * main.Game.SCALE));
        this.img = img;
        this.direction = direction;
        this.damage = damage;
        // Hitbox-ul ar trebui să corespundă dimensiunii imaginii scalate
        initHitbox(x, y, this.width, this.height);
    }


    /**
     * Actualizează poziția proiectilului.
     * Dacă proiectilul nu este activ, nu face nimic.
     * Mișcă proiectilul în direcția specificată cu viteza sa.
     * Verificarea ieșirii din ecran este gestionată de EnemyManager.
     */
    public void update() {
        if (!active) return; // Dacă nu este activ, nu actualiza

        // Mișcă proiectilul în funcție de direcție
        if (direction == utilz.Enemy_Animation_Rows.Directions.LEFT) {
            hitbox.x -= speed;
        } else {
            hitbox.x += speed;
        }
        // Verificarea ieșirii din ecran va fi gestionată de EnemyManager pe baza limitelor nivelului
    }

    /**
     * Desenează proiectilul pe ecran.
     * Dacă proiectilul nu este activ, nu desenează nimic.
     * Desenează sprite-ul dacă este disponibil, altfel desenează un dreptunghi galben.
     * Include logica pentru inversarea sprite-ului dacă se mișcă spre stânga.
     * @param g Contextul grafic pentru desenare.
     * @param xLvlOffset Decalajul pe axa X al nivelului, pentru scrolling.
     */
    public void draw(Graphics g, int xLvlOffset) {
        if (!active) return; // Nu desena dacă nu este activ

        // Desenează hitbox-ul pentru debugging, sau desenează sprite-ul dacă este disponibil
        if (img != null) { // Dacă există un sprite
            if (direction == utilz.Enemy_Animation_Rows.Directions.LEFT) { // Dacă se mișcă la stânga, inversează imaginea
                g.drawImage(img, (int) (hitbox.x - xLvlOffset + hitbox.width), (int) hitbox.y, (int)-hitbox.width, (int)hitbox.height, null);
            } else { // Altfel, desenează normal
                g.drawImage(img, (int) (hitbox.x - xLvlOffset), (int) hitbox.y, (int)hitbox.width, (int)hitbox.height, null);
            }
        } else { // Dacă nu există sprite, desenează un dreptunghi simplu
            g.setColor(java.awt.Color.YELLOW); // Culoare simplă pentru proiectil
            g.fillRect((int) (hitbox.x - xLvlOffset), (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
        }
        // Pentru a desena hitbox-ul proiectilului pentru debugging:
        // g.setColor(Color.RED);
        // g.drawRect((int) (hitbox.x - xLvlOffset), (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    /**
     * Verifică dacă proiectilul este activ.
     * @return true dacă proiectilul este activ, false altfel.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setează starea de activitate a proiectilului.
     * @param active Noua stare de activitate (true pentru activ, false pentru inactiv).
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returnează hitbox-ul proiectilului.
     * @return Un obiect {@link Rectangle2D.Float} reprezentând hitbox-ul.
     */
    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    /**
     * Returnează daunele pe care le provoacă proiectilul.
     * @return Cantitatea de daune.
     */
    public int getDamage() {
        return damage;
    }
}
