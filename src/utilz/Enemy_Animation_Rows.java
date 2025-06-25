package utilz;

/**
 * Enumerație ce definește rândurile de animație și numărul de cadre pentru inamici.
 * Fiecare constantă corespunde unui rând specific dintr-un spritesheet de animații pentru inamici.
 * Este utilizată pentru a gestiona și accesa secvențele de animație.
 */
public enum Enemy_Animation_Rows {
    /** Animația de moarte. */
    DYING(0, 15),
    /** Animația de cădere. */
    FALLING_DOWN(1, 6),
    /** Animația de rănire. */
    HURT(2, 12),
    /** Animația de repaus (idle), cu clipire. */
    IDLE(3, 18),
    /** Animația de repaus (idle), fără clipire. */
    IDLE_NO_BLINK(4, 18),
    /** Animația de buclă a săriturii (în aer). */
    JUMP_LOOP(5, 6),
    /** Animația de început a săriturii. */
    JUMP_START(6,6),
    /** Animația de lovitură cu piciorul. */
    KICKING(7, 12),
    /** Animația de atac cu tăietură în timpul alergării. */
    RUN_SLASING(8,12),
    /** Animația de aruncare în timpul alergării. */
    RUN_THROWING(9,12),
    /** Animația de alergare. */
    RUNNING(10,12),
    /** Animația de atac cu tăietură în aer. */
    SLASHING_IN_THE_AIR(11,12),
    /** Animația de atac cu tăietură (la sol). */
    SLASHING(12,12),
    /** Animația de alunecare. */
    SLIDING(13,6),
    /** Animația de aruncare în aer. */
    THROWING_IN_THE_AIR(14,6),
    /** Animația de aruncare (la sol). */
    THROWING(15,6),
    /** Animația de mers. */
    WALKING(16, 24);

    /**
     * Clasă internă statică ce definește constante pentru direcții.
     */
    public static class Directions{
        /** Constanta pentru direcția stânga. */
        public static final int LEFT = 0;
        /** Constanta pentru direcția dreapta. */
        public static final int RIGHT = 1;
    }

    /** Indexul rândului din spritesheet corespunzător acestei animații. */
    private final int rowIndex;
    /** Numărul de cadre (frame-uri) pentru această animație. */
    private final int frameCount;

    /**
     * Constructor pentru constantele enum care nu au un număr specific de cadre definit (implicit 0).
     * @param rowIndex Indexul rândului animației.
     */
    Enemy_Animation_Rows(int rowIndex) {
        this.rowIndex = rowIndex;
        this.frameCount = 0; // Sau un număr implicit de cadre dacă este cazul
    }

    /**
     * Constructor pentru constantele enum.
     * @param rowIndex Indexul rândului animației în spritesheet.
     * @param frameCount Numărul de cadre pentru animație.
     */
    Enemy_Animation_Rows(int rowIndex, int frameCount) {
        this.rowIndex = rowIndex;
        this.frameCount = frameCount;
    }

    /**
     * Returnează indexul rândului din spritesheet pentru această animație.
     * @return Indexul rândului.
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returnează numărul de cadre (frame-uri) pentru această animație.
     * @return Numărul de cadre.
     */
    public int getFrameCount() {
        return frameCount;
    }
}
