package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Interfață ce definește metodele comune pe care toate stările de joc (gamestates) trebuie să le implementeze.
 * Aceste metode gestionează actualizarea logicii, desenarea și procesarea input-ului (mouse și tastatură).
 */
public interface Statemethods {
    /**
     * Actualizează logica specifică stării de joc.
     * Apelată în fiecare ciclu al buclei principale a jocului.
     */
    public void update();

    /**
     * Desenează elementele grafice specifice stării de joc.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    public void draw(Graphics g);

    /**
     * Gestionază evenimentul de click al mouse-ului.
     *
     * @param e Evenimentul {@link MouseEvent} generat.
     */
    public void mouseClicked(MouseEvent e);

    /**
     * Gestionază evenimentul de apăsare a unui buton al mouse-ului.
     *
     * @param e Evenimentul {@link MouseEvent} generat.
     */
    public void mousePressed(MouseEvent e);

    /**
     * Gestionază evenimentul de eliberare a unui buton al mouse-ului.
     *
     * @param e Evenimentul {@link MouseEvent} generat.
     */
    public void mouseReleased(MouseEvent e);

    /**
     * Gestionază evenimentul de mișcare a mouse-ului (fără butoane apăsate).
     *
     * @param e Evenimentul {@link MouseEvent} generat.
     */
    public void mouseMoved(MouseEvent e);

    /**
     * Gestionază evenimentul de mișcare a mouse-ului cu un buton apăsat (drag).
     *
     * @param e Evenimentul {@link MouseEvent} generat.
     */
    public void mouseDragged(MouseEvent e);

    /**
     * Gestionază evenimentul de apăsare a unei taste.
     *
     * @param e Evenimentul {@link KeyEvent} generat.
     */
    public void keyPressed(KeyEvent e);

    /**
     * Gestionază evenimentul de eliberare a unei taste.
     *
     * @param e Evenimentul {@link KeyEvent} generat.
     */
    public void keyReleased(KeyEvent e);

}
