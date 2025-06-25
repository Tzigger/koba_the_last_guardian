package main;

import inputs.KeyboardInputs;
import inputs.MouseInputs;


import javax.swing.*;
import java.awt.*;

import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

/**
 * Panoul principal al jocului, extinzând {@link JPanel}.
 * Acesta este componenta Swing pe care se desenează întregul joc.
 * De asemenea, inițializează și atașează listener-ii pentru input-ul de la tastatură și mouse.
 */
public class GamePanel extends JPanel {
    /** Listener pentru input-ul de la tastatură. */
    private KeyboardInputs keyboardInputs; // Redenumit pentru convenție (keyboardInputs)
    /** Listener pentru input-ul de la mouse. */
    private MouseInputs mouseInputs; // Redenumit pentru convenție (mouseInputs)
    /** Referință la instanța principală a jocului {@link Game}. */
    private Game game;

    /**
     * Constructor pentru GamePanel.
     * Inițializează referința la joc, listener-ii de input, setează dimensiunea panoului
     * și adaugă listener-ii la panou.
     *
     * @param game Referință la instanța principală a jocului {@link Game}.
     */
    public GamePanel(Game game){
        this.game = game;
        this.keyboardInputs = new KeyboardInputs(this); // Folosind this.keyboardInputs
        this.mouseInputs = new MouseInputs(this);   // Folosind this.mouseInputs
        
        setPanelSize();
        addKeyListener(this.keyboardInputs);
        addMouseListener(this.mouseInputs);
        addMouseMotionListener(this.mouseInputs);
        addMouseWheelListener(this.mouseInputs); // Adaugă și MouseWheelListener
    }

    /**
     * Setează dimensiunile preferate, maxime și minime ale panoului
     * pe baza constantelor {@code GAME_WIDTH} și {@code GAME_HEIGHT} din clasa {@link Game}.
     */
    private void setPanelSize(){
        Dimension size = new Dimension(GAME_WIDTH,GAME_HEIGHT);
        setPreferredSize(size);
        setMaximumSize(size); // Asigură că panoul nu devine mai mare
        setMinimumSize(size); // Asigură că panoul nu devine mai mic
        System.out.println("Dimensiune panou setată la: " + GAME_WIDTH + " x " + GAME_HEIGHT );
    }

    /**
     * Metodă destinată actualizării logicii jocului (momentan goală).
     * Ar putea fi apelată din bucla principală a jocului dacă GamePanel ar gestiona direct actualizările.
     */
    public void updateGame(){
        // Această metodă nu este apelată în mod activ în structura curentă,
        // actualizările sunt gestionate de bucla principală din clasa Game.
    }

    /**
     * Suprascrie metoda {@link JComponent#paintComponent(Graphics)} pentru a desena conținutul jocului.
     * Apelează metoda {@code render} a obiectului {@link Game}.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Apelează metoda din clasa părinte pentru desenarea corectă a componentelor Swing
        game.render(g); // Deleagă desenarea către obiectul Game
    }

    /**
     * Returnează referința la instanța principală a jocului.
     *
     * @return Obiectul {@link Game}.
     */
    public Game getGame(){
        return game;
    }
}
