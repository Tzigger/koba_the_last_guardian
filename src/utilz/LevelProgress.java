package utilz;

/**
 * Clasă simplă de date (POJO - Plain Old Java Object) pentru a stoca
 * informațiile despre progresul jucătorului într-un anumit nivel.
 * Utilizată pentru salvarea și încărcarea stării jocului.
 */
public class LevelProgress {
    /** Scorul obținut de jucător în nivel. */
    public int score;
    /** Viața rămasă a jucătorului. */
    public int health;
    /** Numărul de nuci de cocos colectate de jucător. */
    public int coconuts;
    /** Poziția X a jucătorului în nivel. */
    public int posX;
    /** Poziția Y a jucătorului în nivel. */
    public int posY;

    /**
     * Constructor implicit. Inițializează progresul cu valori default
     * (scor 0, viață 100, nuci de cocos 0, poziție 0,0).
     */
    public LevelProgress() {
        this.score = 0;
        this.health = 100; // Valoare implicită pentru viață
        this.coconuts = 0;
        this.posX = 0; // Poziție implicită X
        this.posY = 0; // Poziție implicită Y
    }

    /**
     * Constructor cu parametri pentru a inițializa progresul cu valori specifice.
     *
     * @param score Scorul jucătorului.
     * @param health Viața jucătorului.
     * @param coconuts Numărul de nuci de cocos.
     * @param posX Poziția X a jucătorului.
     * @param posY Poziția Y a jucătorului.
     */
    public LevelProgress(int score, int health, int coconuts, int posX, int posY) {
        this.score = score;
        this.health = health;
        this.coconuts = coconuts;
        this.posX = posX;
        this.posY = posY;
    }
}
