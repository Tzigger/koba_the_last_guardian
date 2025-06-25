package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

/**
 * Clasă abstractă de bază pentru toate entitățile din joc (de ex., jucător, inamici, obiecte).
 * Definește proprietăți comune precum poziția (x, y), dimensiunile (lățime, înălțime)
 * și un hitbox pentru detecția coliziunilor.
 */
public abstract class Entity {
    /** Coordonata x a colțului stânga-sus al entității (poate fi pentru desenare). */
    protected float x;
    /** Coordonata y a colțului stânga-sus al entității (poate fi pentru desenare). */
    protected float y;

    /** Lățimea entității (poate fi pentru desenare). */
    protected int width;
    /** Înălțimea entității (poate fi pentru desenare). */
    protected int height;
    /** Dreptunghiul de coliziune (hitbox) al entității. */
    protected Rectangle2D.Float hitbox;
    
    /**
     * Constructor pentru clasa Entity.
     *
     * @param x Poziția x inițială a entității.
     * @param y Poziția y inițială a entității.
     * @param width Lățimea inițială a entității.
     * @param height Înălțimea inițială a entității.
     */
    public Entity(float x, float y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        // Hitbox-ul este de obicei inițializat în subclase sau printr-o metodă separată (initHitbox)
    }       

    /**
     * Inițializează hitbox-ul entității cu coordonatele și dimensiunile specificate.
     *
     * @param x Poziția x a colțului stânga-sus al hitbox-ului.
     * @param y Poziția y a colțului stânga-sus al hitbox-ului.
     * @param width Lățimea hitbox-ului.
     * @param height Înălțimea hitbox-ului.
     */
    protected void initHitbox(float x, float y, float width, float height){
        hitbox = new Rectangle2D.Float(x, y, width, height);
    }

    // protected void updateHitbox(){ // Metodă comentată, posibil neutilizată sau înlocuită
    //     hitbox.x = (int)x;
    //     hitbox.y = (int)y;
    // }

    /**
     * Returnează hitbox-ul entității.
     *
     * @return Obiectul {@link Rectangle2D.Float} reprezentând hitbox-ul.
     */
    public Rectangle2D.Float getHitbox(){
        return hitbox;
    }
    
    /**
     * Desenează hitbox-ul entității pe ecran.
     * Utilizată în principal pentru depanare.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    protected void drawHitbox(Graphics g){
        // Setează culoarea pentru hitbox (de ex., roz pentru vizibilitate)
        g.setColor(Color.PINK);
        // Desenează conturul hitbox-ului
        g.drawRect((int)hitbox.x, (int)hitbox.y, (int)hitbox.width, (int)hitbox.height);
    }
}
