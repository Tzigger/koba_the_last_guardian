package inputs;

import gamestates.Gamestate;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import main.GamePanel;

/**
 * Gestionează input-ul de la tastatură pentru joc.
 * Implementează interfața {@link KeyListener} și redirecționează evenimentele de tastatură
 * către starea de joc activă corespunzătoare.
 */
public class KeyboardInputs implements KeyListener {

    private GamePanel gamePanel; // Referință la panoul principal al jocului

    /**
     * Constructor pentru KeyboardInputs.
     *
     * @param gamePanel Panoul principal al jocului {@link GamePanel} căruia i se atașează acest listener.
     */
    public KeyboardInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Metodă apelată când o tastă este "typed" (apăsată și eliberată, generând un caracter).
     * Momentan neutilizată.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO: Implementează dacă este necesar pentru input de text specific (de ex., chat).
    }

    /**
     * Metodă apelată la apăsarea unei taste.
     * Redirecționează evenimentul către metoda {@code keyPressed} a stării de joc active.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (Gamestate.state) {
            case PLAYING:
                gamePanel.getGame().getPlaying().keyPressed(e); 
                break;
            case MENU:
                gamePanel.getGame().getMenu().keyPressed(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().keyPressed(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().keyPressed(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().keyPressed(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().keyPressed(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată la eliberarea unei taste.
     * Redirecționează evenimentul către metoda {@code keyReleased} a stării de joc active.
     *
     * @param e Evenimentul {@link KeyEvent}.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch (Gamestate.state) {
            case PLAYING:
                gamePanel.getGame().getPlaying().keyReleased(e); 
                break;
            case MENU:
                gamePanel.getGame().getMenu().keyReleased(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().keyReleased(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().keyReleased(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().keyReleased(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().keyReleased(e);
                break;
            default:
                break;
        }
    }
}
