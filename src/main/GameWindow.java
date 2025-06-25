package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Reprezintă fereastra principală a jocului.
 * Această clasă creează și configurează obiectul {@link JFrame}
 * care conține panoul jocului ({@link GamePanel}).
 */
public class GameWindow {
    /** Fereastra principală a jocului (obiectul JFrame). */
    private JFrame jframe;

    /**
     * Constructor pentru GameWindow.
     * Inițializează și configurează JFrame-ul, adaugă GamePanel-ul la acesta
     * și setează un listener pentru focusul ferestrei.
     *
     * @param gamePanel Panoul principal al jocului ({@link GamePanel}) care va fi afișat în fereastră.
     */
    public GameWindow(GamePanel gamePanel){
        jframe = new JFrame();
        jframe.setTitle("Koba: The Last Guardian"); // Setează titlul ferestrei
        // Setează iconița ferestrei
        try {
            jframe.setIconImage(new ImageIcon(getClass().getResource("/res/icon.jpeg")).getImage());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea iconiței ferestrei: " + e.getMessage());
        }
        
        // jframe.setSize(1920,1080); // Dimensiunea este setată de pack() pe baza GamePanel
        
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Comportamentul la închidere
        jframe.setResizable(false); // Împiedică redimensionarea ferestrei
        jframe.add(gamePanel); // Adaugă panoul jocului la fereastră
        jframe.pack(); // Dimensionează fereastra pentru a se potrivi cu conținutul preferat (GamePanel)
        jframe.setLocationRelativeTo(null); // Centrează fereastra pe ecran
        jframe.setVisible(true); // Face fereastra vizibilă

        // Adaugă un listener pentru a detecta pierderea focusului ferestrei
        jframe.addWindowFocusListener(new WindowFocusListener() {
            /**
             * Apelată când fereastra câștigă focusul.
             * @param e Evenimentul de focus al ferestrei.
             */
            @Override
            public void windowGainedFocus(WindowEvent e) {
                System.out.println("Fereastra a câștigat focusul.");
                // Opțional: gamePanel.getGame().windowGainedFocus(); dacă este necesar
            }

            /**
             * Apelată când fereastra pierde focusul.
             * Notifică jocul pentru a gestiona această situație (de ex., pauză, resetare input).
             * @param e Evenimentul de focus al ferestrei.
             */
            @Override
            public void windowLostFocus(WindowEvent e) {
                System.out.println("Fereastra a pierdut focusul.");
                gamePanel.getGame().windowFocusLost(); // Notifică jocul
            }
        });
    }
}
