package utilz;

/**
 * Enumerație ce definește rândurile de animație și numărul de cadre pentru personajul jucător (Gorila).
 * Fiecare constantă corespunde unui rând specific dintr-un spritesheet de animații pentru jucător.
 * Este utilizată pentru a gestiona și accesa secvențele de animație ale jucătorului.
 */
public enum Gorilla_Animation_rows {
    /** Animația de pumn în poziție ghemuită. */
    PUNCH_CROUCHED(0,10),
    /** Animația de combo în picioare. */
    COMBO_STANDING(1,12),
    /** Animația de tranziție de la ghemuit la în picioare. */
    CROUCH_TO_STAND(2,5),
    /** Animația de blocare în picioare. */
    STANDING_BLOCK(3,5),
    /** Animația de moarte în poziție ghemuită. */
    DIE_CROUCHED(4,5),
    /** Animația de rănire în poziție ghemuită. */
    HURT_CROUCHED(5,5),
    /** Animația de rănire în picioare. */
    HURT_STANDING(6,5),
    /** Animația de repaus (idle) în poziție ghemuită. */
    IDLE_CROUCHED(7,20),
    /** Animația de atac "Jump Slam" în picioare. */
    STANDING_JUMP_SLAM(8,12),
    /** Animația de săritură în picioare. */
    JUMP_STANDING(9,12),
    /** Animația de pumn în picioare. */
    PUNCH_STANDING(10, 10),
    /** Animația de alergare în poziție ghemuită. */
    CROUCH_RUN(11,12),
    /** Animația de alergare în picioare. */
    STANDING_RUN(12, 12),
    /** Animația de moarte în picioare. */
    DIE_STANDING(13,5),
    /** Animația de repaus (idle) în picioare. */
    IDLE_STANDING(14, 20),
    /** Animația de tranziție de la în picioare la ghemuit. */
    STAND_TO_CROUCH(15,5),
    /** Animația de aruncare în poziție ghemuită. */
    CROUCH_THROW(16,10),
    /** Animația de aruncare în picioare. */
    STAND_THROW(17,10),

    // Rânduri de animație pentru liană (VINE) - acestea nu au un număr specific de cadre definit aici
    /** Rândul 18 pentru animația de liană. */
    VINE_18(18),
    /** Rândul 19 pentru animația de liană. */
    VINE_19(19),
    /** Rândul 20 pentru animația de liană. */
    VINE_20(20),
    /** Rândul 21 pentru animația de liană. */
    VINE_21(21),
    /** Rândul 22 pentru animația de liană. */
    VINE_22(22),
    /** Rândul 23 pentru animația de liană. */
    VINE_23(23),

    /** Animația de mers în poziție ghemuită. */
    CROUCH_WALK(24,12),
    /** Animația de mers în picioare. */
    STAND_WALK(25, 16),
    /** Animația de atac "Slam" în poziție ghemuită. */
    CROUCH_SLAM(26,10),
    /** Animația de atac "Slam" în picioare. */
    STAND_SLAM(27,10);
    
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
     * Constructor pentru constantele enum care nu au un număr specific de cadre definit
     * (de ex., animațiile de liană). Implicit, numărul de cadre este 0.
     * @param rowIndex Indexul rândului animației.
     */
    Gorilla_Animation_rows(int rowIndex) {
        this.rowIndex = rowIndex;
        this.frameCount = 0; // Animațiile de liană pot avea o logică diferită pentru cadre
    }

    /**
     * Constructor pentru constantele enum.
     * @param rowIndex Indexul rândului animației în spritesheet.
     * @param frameCount Numărul de cadre pentru animație.
     */
    Gorilla_Animation_rows(int rowIndex, int frameCount) {
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
