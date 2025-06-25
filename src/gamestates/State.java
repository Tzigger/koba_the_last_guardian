package gamestates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;

import main.Game;
import ui.MenuButton;
import ui.LevelButton;

/**
 * Clasă de bază pentru diferitele stări ale jocului (de ex., Meniu, Joc propriu-zis).
 * Oferă funcționalități comune și o referință la obiectul principal al jocului.
 * Deși nu este declarată abstractă, este concepută pentru a fi extinsă de stări specifice.
 */
public class State {

    /** Referință la instanța principală a jocului {@link Game}. */
    protected Game game;
    /** Dreptunghiul care definește limitele butonului "Înapoi", dacă există în această stare. */
    protected Rectangle backButtonBounds;
    /** Flag pentru a afișa sau nu hitbox-urile în scop de depanare (implicit true). */
    protected boolean showDebugHitbox = true; // Numele este auto-explicativ

    /**
     * Constructor pentru clasa State.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public State(Game game) {
        this.game = game;
    }

    /**
     * Verifică dacă evenimentul de mouse s-a produs în interiorul limitelor unui buton de meniu.
     *
     * @param e Evenimentul de mouse {@link MouseEvent}.
     * @param mb Butonul de meniu {@link MenuButton} de verificat.
     * @return {@code true} dacă mouse-ul este în interiorul butonului, {@code false} altfel.
     */
    public boolean isIn(MouseEvent e, MenuButton mb) {
        return mb.getBounds().contains(e.getX(), e.getY());
    }

    /**
     * Verifică dacă evenimentul de mouse s-a produs în interiorul limitelor unui buton de nivel.
     *
     * @param e Evenimentul de mouse {@link MouseEvent}.
     * @param lb Butonul de nivel {@link LevelButton} de verificat.
     * @return {@code true} dacă mouse-ul este în interiorul butonului, {@code false} altfel.
     */
    public boolean isIn(MouseEvent e, LevelButton lb) {
        return lb.getBounds().contains(e.getX(), e.getY());
    }

    /**
     * Returnează instanța principală a jocului.
     *
     * @return Obiectul {@link Game}.
     */
    public Game getGame() {
        return game;
    }

    /**
     * Desenează hitbox-ul butonului "Înapoi" dacă {@code showDebugHitbox} este true și
     * {@code backButtonBounds} este definit.
     * Folosit în scopuri de depanare.
     *
     * @param g2d Contextul grafic 2D {@link Graphics2D} pe care se va desena.
     */
    protected void drawBackButton(Graphics2D g2d) {
        if (showDebugHitbox && backButtonBounds != null) {
            g2d.setColor(Color.RED);
            g2d.drawRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height);
        }
    }

    /**
     * Verifică dacă butonul "Înapoi" a fost apăsat, pe baza coordonatelor evenimentului de mouse.
     *
     * @param e Evenimentul de mouse {@link MouseEvent}.
     * @return {@code true} dacă butonul "Înapoi" a fost apăsat, {@code false} altfel sau dacă nu este definit.
     */
    protected boolean isBackButtonPressed(MouseEvent e) {
        return backButtonBounds != null && backButtonBounds.contains(e.getPoint());
    }
}
