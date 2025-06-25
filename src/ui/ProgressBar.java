package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import utilz.LoadSave;

/**
 * Reprezintă o bară de progres personalizată pentru interfața utilizator.
 * Poate fi folosită pentru a afișa diverse tipuri de progres, cum ar fi încărcarea,
 * viața, experiența etc.
 */
public class ProgressBar {
    private int x, y, width, height; // Coordonatele și dimensiunile barei de progres
    /** Progresul curent al barei, o valoare între 0.0f (0%) și 1.0f (100%). */
    private float progress; 
    /** Imaginea de fundal ("șanțul") pentru bara de progres. */
    private BufferedImage grooverImg;
    /** Culoarea de umplere pentru partea de progres a barei. */
    private Color fillColor;
    
    /**
     * Constructor pentru ProgressBar.
     *
     * @param x Poziția x a colțului de sus-stânga al barei de progres.
     * @param y Poziția y a colțului de sus-stânga al barei de progres.
     * @param width Lățimea barei de progres.
     * @param height Înălțimea barei de progres.
     */
    public ProgressBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.progress = 0.0f; // Inițial, progresul este 0%
        this.fillColor = new Color(255, 255, 255, 200); // Culoare implicită: alb semi-transparent
        loadImages();
    }
    
    /**
     * Încarcă imaginea de fundal ("șanțul") pentru bara de progres.
     */
    private void loadImages() {
        grooverImg = LoadSave.getSpriteAtlas("groover.png");
    }
    
    /**
     * Desenează bara de progres pe ecran.
     * Include fundalul și partea de umplere corespunzătoare progresului.
     *
     * @param g Contextul grafic {@link Graphics} pe care se va desena.
     */
    public void draw(Graphics g) {
        // Desenează fundalul (șanțul)
        if (grooverImg != null) {
            g.drawImage(grooverImg, x, y, width, height, null);
        } else { // Fallback dacă imaginea nu s-a încărcat
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, width, height);
        }
        
        // Desenează umplerea progresului
        g.setColor(fillColor);
        int fillWidth = (int)(width * progress); // Calculează lățimea umplerii
        g.fillRect(x, y, fillWidth, height);
    }
    
    /**
     * Setează progresul curent al barei.
     * Valoarea este limitată între 0.0f și 1.0f.
     *
     * @param progress Noua valoare a progresului (între 0.0 și 1.0).
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    /**
     * Returnează progresul curent al barei.
     * @return Progresul (între 0.0 și 1.0).
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Setează culoarea de umplere pentru partea de progres a barei.
     * @param color Noua culoare de umplere.
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }
    
    /**
     * Setează poziția colțului de sus-stânga al barei de progres.
     * @param x Noua coordonată x.
     * @param y Noua coordonată y.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Setează dimensiunile barei de progres.
     * @param width Noua lățime.
     * @param height Noua înălțime.
     */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
