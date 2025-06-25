package ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import gamestates.Gamestate;
import utilz.LoadSave;
import static utilz.Constants.UI.Buttons.*; // Importă constantele pentru dimensiunile butoanelor

/**
 * Reprezintă un buton generic pentru meniurile din interfața utilizator.
 * Gestionează stările vizuale ale butonului (normal, mouse over, apăsat) și acțiunea asociată (schimbarea stării de joc).
 */
public class MenuButton {
    private int xPos, yPos; // Poziția butonului
    /** Indexul rândului din spritesheet-ul de butoane de unde se încarcă imaginile pentru acest tip de buton. */
    private int rowIndex;
    /** Indexul imaginii curente din array-ul {@code imgs}, determinat de starea mouse-ului. */
    private int index;
    /** Offset orizontal pentru centrarea imaginii butonului față de {@code xPos}. */
    private int xOffsetCenter = B_WIDTH / 2; // B_WIDTH este constanta pentru lățimea butonului de meniu
    /** Starea de joc {@link Gamestate} care va fi activată la apăsarea butonului. */
    private Gamestate state;
    /** Array de imagini pentru diferitele stări ale butonului (normal, mouse over, apăsat). */
    private BufferedImage[] imgs;
    private boolean mouseOver, mousePressed; // Flag-uri pentru starea interacțiunii cu mouse-ul
    /** Dreptunghiul care definește limitele butonului pentru detecția coliziunilor cu mouse-ul. */
    private Rectangle bounds;

    /**
     * Constructor pentru MenuButton.
     *
     * @param xPos Poziția x a centrului butonului.
     * @param yPos Poziția y a colțului de sus-stânga al butonului.
     * @param rowIndex Indexul rândului din spritesheet-ul de butoane (specific pentru tipul de buton de meniu).
     * @param state Starea de joc {@link Gamestate} asociată cu acest buton.
     */
    public MenuButton(int xPos, int yPos, int rowIndex, Gamestate state) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.rowIndex = rowIndex;
        this.state = state;
        loadImgs();
        initBounds();
    }

    /**
     * Inițializează limitele dreptunghiulare (hitbox) ale butonului.
     * Folosește constantele {@code B_WIDTH} și {@code B_HEIGHT} pentru dimensiuni.
     */
    private void initBounds() {
        bounds = new Rectangle(xPos - xOffsetCenter, yPos, B_WIDTH, B_HEIGHT);
    }

    /**
     * Încarcă imaginile pentru buton din spritesheet-ul global de butoane de meniu.
     * Folosește {@code rowIndex} pentru a selecta setul corect de imagini.
     */
    private void loadImgs() {
        imgs = new BufferedImage[3]; // 3 stări: normal, mouse over, apăsat
        BufferedImage temp = LoadSave.getSpriteAtlas(LoadSave.MENU_BUTTONS);
        if (temp != null) {
            for (int i = 0; i < imgs.length; i++)
                imgs[i] = temp.getSubimage(i * B_WIDTH_DEFAULT, rowIndex * B_HEIGHT_DEFAULT, B_WIDTH_DEFAULT, B_HEIGHT_DEFAULT);
        } else {
            System.err.println("Eroare: Spritesheet-ul pentru butoane de meniu nu a putut fi încărcat.");
            // Opțional: încarcă imagini placeholder
        }
    }

    /**
     * Desenează butonul pe ecran, folosind imaginea corespunzătoare stării curente.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    public void draw(Graphics g) {
        if (imgs != null && imgs[index] != null) { // Verifică dacă imaginile sunt încărcate
            g.drawImage(imgs[index], xPos - xOffsetCenter, yPos, B_WIDTH, B_HEIGHT, null);
        }
    }

    /**
     * Actualizează starea vizuală a butonului (indexul imaginii) pe baza interacțiunii cu mouse-ul.
     */
    public void update() {
        index = 0; // Starea normală
        if (mouseOver)
            index = 1; // Starea mouse over
        if (mousePressed)
            index = 2; // Starea apăsat
    }

    /**
     * Verifică dacă mouse-ul este deasupra butonului.
     * @return {@code true} dacă mouse-ul este deasupra, {@code false} altfel.
     */
    public boolean isMouseOver() {
        return mouseOver;
    }

    /**
     * Setează starea "mouse over" a butonului.
     * @param mouseOver Noua stare "mouse over".
     */
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    /**
     * Verifică dacă butonul este apăsat.
     * @return {@code true} dacă butonul este apăsat, {@code false} altfel.
     */
    public boolean isMousePressed() {
        return mousePressed;
    }

    /**
     * Setează starea de apăsare a butonului.
     * @param mousePressed Noua stare de apăsare.
     */
    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    /**
     * Returnează limitele dreptunghiulare (hitbox) ale butonului.
     * @return Obiectul {@link Rectangle} reprezentând limitele.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Aplică starea de joc asociată cu acest buton, schimbând starea globală a jocului.
     */
    public void applyGamestate() {
        Gamestate.state = state;
    }

    /**
     * Returnează starea de joc {@link Gamestate} asociată cu acest buton.
     * @return Starea de joc.
     */
    public Gamestate getState() {
        return state;
    }

    /**
     * Resetează flag-urile de interacțiune cu mouse-ul (mouseOver și mousePressed) la valorile implicite (false).
     */
    public void resetBools() {
        mouseOver = false;
        mousePressed = false;
    }
}
