package inputs;

import gamestates.Gamestate;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import main.GamePanel;

/**
 * Gestionează input-ul de la mouse pentru joc.
 * Implementează interfețele {@link MouseListener}, {@link MouseMotionListener} și {@link MouseWheelListener}
 * și redirecționează evenimentele de mouse către starea de joc activă corespunzătoare.
 */
public class MouseInputs implements MouseListener, MouseMotionListener, MouseWheelListener {

    private GamePanel gamePanel; // Referință la panoul principal al jocului

    /**
     * Constructor pentru MouseInputs.
     *
     * @param gamePanel Panoul principal al jocului {@link GamePanel} căruia i se atașează acest listener.
     */
    public MouseInputs(GamePanel gamePanel){
        this.gamePanel=gamePanel;
    }

    /**
     * Metodă apelată la un click de mouse (apăsare și eliberare).
     * Redirecționează evenimentul către metoda {@code mouseClicked} a stării de joc active.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        switch(Gamestate.state){
            case MENU:
                gamePanel.getGame().getMenu().mouseClicked(e);
                break;
            case PLAYING:
                gamePanel.getGame().getPlaying().mouseClicked(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().mouseClicked(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mouseClicked(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().mouseClicked(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().mouseClicked(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată la apăsarea unui buton al mouse-ului.
     * Redirecționează evenimentul către metoda {@code mousePressed} a stării de joc active.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        switch(Gamestate.state){
            case MENU:
                gamePanel.getGame().getMenu().mousePressed(e);
                break;
            case PLAYING:
                gamePanel.getGame().getPlaying().mousePressed(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().mousePressed(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mousePressed(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().mousePressed(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().mousePressed(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată la eliberarea unui buton al mouse-ului.
     * Redirecționează evenimentul către metoda {@code mouseReleased} a stării de joc active.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        switch(Gamestate.state){
            case MENU:
                gamePanel.getGame().getMenu().mouseReleased(e);
                break;
            case PLAYING:
                gamePanel.getGame().getPlaying().mouseReleased(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().mouseReleased(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mouseReleased(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().mouseReleased(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().mouseReleased(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată când cursorul mouse-ului intră în componentă.
     * Momentan neutilizată.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // Neutilizat
    }

    /**
     * Metodă apelată când cursorul mouse-ului iese din componentă.
     * Momentan neutilizată.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseExited(MouseEvent e) {
        // Neutilizat
    }

    /**
     * Metodă apelată când mouse-ul este mișcat cu un buton apăsat (drag).
     * Redirecționează evenimentul către metoda {@code mouseDragged} a stării de joc active.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        switch(Gamestate.state){
            case MENU:
                gamePanel.getGame().getMenu().mouseDragged(e);
                break;
            case PLAYING:
                gamePanel.getGame().getPlaying().mouseDragged(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().mouseDragged(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mouseDragged(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().mouseDragged(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().mouseDragged(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată când mouse-ul este mișcat (fără butoane apăsate).
     * Redirecționează evenimentul către metoda {@code mouseMoved} a stării de joc active.
     *
     * @param e Evenimentul {@link MouseEvent}.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        switch(Gamestate.state){
            case MENU:
                gamePanel.getGame().getMenu().mouseMoved(e);
                break;
            case PLAYING:
                gamePanel.getGame().getPlaying().mouseMoved(e);
                break;
            case LOADGAME:
                gamePanel.getGame().getLoadgame().mouseMoved(e);
                break;
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mouseMoved(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getOptions().mouseMoved(e);
                break;
            case ENTER_NAME:
                gamePanel.getGame().getEnterNameOverlay().mouseMoved(e);
                break;
            default:
                break;
        }
    }

    /**
     * Metodă apelată la mișcarea rotiței mouse-ului.
     * Redirecționează evenimentul către metoda {@code mouseWheelMoved} a stării de joc active
     * (de ex., pentru derularea clasamentului).
     *
     * @param e Evenimentul {@link MouseWheelEvent}.
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        switch(Gamestate.state){
            case LEADERBOARD:
                gamePanel.getGame().getLeaderboard().mouseWheelMoved(e);
                break;
            default:
                break;
        }
    }
}
