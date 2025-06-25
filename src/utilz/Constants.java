package utilz;

import main.Game;

/**
 * Clasă ce conține constante globale utilizate în diverse părți ale jocului.
 * Constantele sunt grupate în clase interne statice pentru o mai bună organizare.
 */
public class Constants {

    /**
     * Constante legate de interfața utilizator (UI).
     */
    public static class UI {
        /**
         * Constante pentru dimensiunile butoanelor.
         */
        public static class Buttons {
            /** Lățimea implicită (nescalată) a butoanelor standard de meniu. */
            public static final int B_WIDTH_DEFAULT = 320;
            /** Înălțimea implicită (nescalată) a butoanelor standard de meniu. */
            public static final int B_HEIGHT_DEFAULT = 97;
            /** Lățimea scalată a butoanelor standard de meniu. */
            public static final int B_WIDTH = (int) (B_WIDTH_DEFAULT * Game.SCALE);
            /** Înălțimea scalată a butoanelor standard de meniu. */
            public static final int B_HEIGHT = (int) (B_HEIGHT_DEFAULT * Game.SCALE);

            /** Lățimea implicită (nescalată) a butoanelor de nivel. */
            public static final int L_WIDTH_DEFAULT = 128;
            /** Înălțimea implicită (nescalată) a butoanelor de nivel. */
            public static final int L_HEIGHT_DEFAULT = 155;
            /** Lățimea scalată a butoanelor de nivel. */
            public static final int L_WIDTH = (int) (L_WIDTH_DEFAULT * Game.SCALE);
            /** Înălțimea scalată a butoanelor de nivel. */
            public static final int L_HEIGHT = (int) (L_HEIGHT_DEFAULT * Game.SCALE);
        }
    }

    /**
     * Constante pentru direcțiile de mișcare sau orientare.
     */
    public static class Directions {
        /** Direcția stânga. */
        public static final int LEFT = 0;
        /** Direcția sus. */
        public static final int UP = 1;
        /** Direcția dreapta. */
        public static final int RIGHT = 2;
        /** Direcția jos. */
        public static final int DOWN = 3;
    }

    /**
     * Constante legate de inamici.
     */
    public static class EnemyConstants{
        /** Dimensiunea generică pentru inamici (de ex., Nanite). Poate necesita ajustări per tip de inamic. */
        public static final int ENEMY_SIZE = 100; 

        // Constante pentru Goblin
        /** ID-ul tipului de inamic pentru Goblin. */
        public static final int GOBLIN = 1; 

        /** Lățimea sursă a sprite-ului de Goblin (dimensiunea celulei în spritesheet). */
        public static final int GOBLIN_SPRITE_SOURCE_WIDTH_DEFAULT = 100;
        /** Înălțimea sursă a sprite-ului de Goblin (dimensiunea celulei în spritesheet). */
        public static final int GOBLIN_SPRITE_SOURCE_HEIGHT_DEFAULT = 100;

        /** Lățimea de desenare implicită (nescalată) pentru Goblin. */
        public static final int GOBLIN_DRAW_WIDTH_DEFAULT = 120; 
        /** Înălțimea de desenare implicită (nescalată) pentru Goblin. */
        public static final int GOBLIN_DRAW_HEIGHT_DEFAULT = 120; 
        /** Lățimea de desenare scalată pentru Goblin. */
        public static final int GOBLIN_WIDTH = (int) (GOBLIN_DRAW_WIDTH_DEFAULT * Game.SCALE); 
        /** Înălțimea de desenare scalată pentru Goblin. */
        public static final int GOBLIN_HEIGHT = (int) (GOBLIN_DRAW_HEIGHT_DEFAULT * Game.SCALE); 

        /** Offset-ul X pentru desenarea sprite-ului de Goblin relativ la hitbox. */
        public static final int GOBLIN_DRAW_OFFSET_X = (int) (35 * Game.SCALE); 
        /** Offset-ul Y pentru desenarea sprite-ului de Goblin relativ la hitbox. */
        public static final int GOBLIN_DRAW_OFFSET_Y = (int) (15 * Game.SCALE); 

        // Constante pentru Goblin Boss (dimensiunile sursă ale sprite-ului)
        // Dimensiunile de desenare și offset-urile sunt calculate în clasa GoblinBoss.
        /** Lățimea sursă a sprite-ului de Goblin Boss (identică cu Goblin normal). */
        public static final int GOBLIN_BOSS_SPRITE_SOURCE_WIDTH_DEFAULT = 100; 
        /** Înălțimea sursă a sprite-ului de Goblin Boss (identică cu Goblin normal). */
        public static final int GOBLIN_BOSS_SPRITE_SOURCE_HEIGHT_DEFAULT = 100; 

        // Stări de animație generice pentru inamici (pot fi partajate sau specifice)
        /** Starea de animație "Idle" (Repaus). */
        public static final int IDLE = 0;
        /** Starea de animație "Running" (Alergare). */
        public static final int RUNNING = 1;
        /** Starea de animație "Attack" (Atac). */
        public static final int ATTACK = 2;
        /** Starea de animație "Hit" sau "Hurt" (Lovit/Rănit). */
        public static final int HIT = 3; 
        /** Starea de animație "Dead" sau "Dying" (Mort/Murind). */
        public static final int DEAD = 4; 
    }

    /**
     * Constante legate de obiectele colectabile.
     */
    public static class Collectibles {
        /** Bonusul la viața maximă oferit de o banană. */
        public static final int BANANA_HP_BONUS = 10;
        /** Bonusul la daunele de atac oferit de o banană. */
        public static final int BANANA_ATTACK_BONUS = 5;
    }

    /**
     * Constante legate de tile-urile din nivel.
     */
    public static class Tiles {
        /** ID-ul tile-ului pentru o banană (exemplu, poate fi folosit în editorul de niveluri). */
        public static final int BANANA_TILE_ID = 99; 
        /** ID-ul tile-ului gol (spațiu liber). */
        public static final int EMPTY_TILE_ID = -1; 
    }
}
