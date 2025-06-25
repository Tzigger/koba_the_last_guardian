package main;

/**
 * Clasa principală a aplicației, conținând punctul de intrare (metoda main).
 * Responsabilitatea sa este de a inițializa și porni jocul.
 */
public class Main
{
    /**
     * Punctul de intrare principal pentru aplicația jocului.
     * Creează o nouă instanță a clasei {@link Game}, care la rândul său
     * inițializează fereastra jocului, panoul și începe bucla principală a jocului.
     *
     * @param args Argumentele liniei de comandă (neutilizate în acest joc).
     */
    public static void main(String[] args)
    {
        Game g = new Game(); // Creează și pornește jocul
    }
}
