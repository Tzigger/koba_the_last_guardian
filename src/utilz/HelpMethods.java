package utilz;

import java.awt.geom.Rectangle2D;

import main.Game;

/**
 * Clasă utilitară ce conține metode statice ajutătoare, în principal pentru
 * detecția coliziunilor și poziționarea entităților în cadrul nivelului.
 */
public class HelpMethods {
    
    /**
     * Verifică dacă o entitate se poate deplasa la o anumită poziție (x, y)
     * fără a intra în coliziune cu tile-uri solide.
     * Verifică colțurile și puncte intermediare pe marginile hitbox-ului entității.
     *
     * @param x Poziția x dorită a colțului stânga-sus al hitbox-ului.
     * @param y Poziția y dorită a colțului stânga-sus al hitbox-ului.
     * @param width Lățimea hitbox-ului entității.
     * @param height Înălțimea hitbox-ului entității.
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @return {@code true} dacă entitatea se poate deplasa la noua poziție, {@code false} altfel.
     */
    public static boolean canMoveHere(float x, float y, float width, float height, int[][] lvlData){
        // Verifică cele 4 colțuri ale hitbox-ului
        if(!isSolid(lvlData, x, y)) // Stânga-sus
            if(!isSolid(lvlData, x + width, y + height)) // Dreapta-jos  
                if(!isSolid(lvlData, x + width, y)) // Dreapta-sus
                    if(!isSolid(lvlData, x, y + height)) // Stânga-jos
                        // Verifică puncte intermediare pe margini pentru o detecție mai fină
                        // Marginea de sus
                        if(!isSolid(lvlData, x + width/4, y))
                            if(!isSolid(lvlData, x + width/2, y))
                                if(!isSolid(lvlData, x + width*3/4, y))
                                    // Marginea de jos
                                    if(!isSolid(lvlData, x + width/4, y + height))
                                        if(!isSolid(lvlData, x + width/2, y + height))
                                            if(!isSolid(lvlData, x + width*3/4, y + height))
                                                // Marginea din stânga
                                                if(!isSolid(lvlData, x, y + height/4))
                                                    if(!isSolid(lvlData, x, y + height/2))
                                                        if(!isSolid(lvlData, x, y + height*3/4))
                                                            // Marginea din dreapta
                                                            if(!isSolid(lvlData, x + width, y + height/4))
                                                                if(!isSolid(lvlData, x + width, y + height/2))
                                                                    if(!isSolid(lvlData, x + width, y + height*3/4))
                                                                        return true; // Nicio coliziune detectată
        return false; // Coliziune detectată
    }

    /**
     * Verifică dacă un punct specific (x, y) din lume corespunde unui tile solid.
     * Un tile este considerat solid dacă valoarea sa în {@code lvlData} este între 0 și 95 (inclusiv).
     * De asemenea, verifică dacă punctul este în afara limitelor nivelului.
     *
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @param x Coordonata x a punctului de verificat (în pixeli).
     * @param y Coordonata y a punctului de verificat (în pixeli).
     * @return {@code true} dacă punctul este solid sau în afara limitelor, {@code false} altfel.
     */
    public static boolean isSolid(int[][] lvlData, float x, float y){
        if (lvlData == null || lvlData.length == 0 || lvlData[0].length == 0) return true; // Date invalide

        int maxWidth = lvlData[0].length * Game.TILES_SIZE;
        if( x < 0 || x >= maxWidth) // În afara limitelor orizontale
            return true;
        
        int maxHeight = lvlData.length * Game.TILES_SIZE; // Redenumit maxHeigth în maxHeight
        if( y < 0 || y >= maxHeight) // În afara limitelor verticale
            return true;

        float xIndex = x / Game.TILES_SIZE;
        float yIndex = y / Game.TILES_SIZE;

        // Asigură-te că indecșii sunt în limitele array-ului înainte de accesare
        int iY = (int)yIndex;
        int iX = (int)xIndex;
        if (iY < 0 || iY >= lvlData.length || iX < 0 || iX >= lvlData[iY].length) return true; // Index în afara limitelor

        int value = lvlData[iY][iX];

        // Un tile este considerat solid dacă ID-ul său este între 0 și 95 (exclusiv 96).
        // Tile-urile cu ID >= 96 sau < 0 (de ex., -1 pentru gol) nu sunt solide.
        return value >= 0 && value < 96; 
    }
    
    /**
     * Verifică dacă o entitate se află pe sol.
     * Verifică punctele de sub colțurile stânga-jos și dreapta-jos ale hitbox-ului.
     *
     * @param hitbox Dreptunghiul de coliziune al entității.
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @return {@code true} dacă entitatea este pe sol, {@code false} altfel.
     */
    public static boolean isEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData ){
        // Verifică puțin sub colțul stânga-jos și dreapta-jos al hitbox-ului
        if(!isSolid(lvlData, hitbox.x, hitbox.y + hitbox.height + 1))
            if(!isSolid(lvlData, hitbox.x + hitbox.width, hitbox.y + hitbox.height +1))
                return false; // Ambele puncte sunt în aer
        return true; // Cel puțin un punct este pe sol (sau în interiorul unui tile solid)
    }

    /**
     * Verifică dacă o entitate se află lângă un perete (la dreapta sa).
     * Această metodă pare incompletă sau specifică unui anumit context,
     * deoarece verifică doar un singur tile la dreapta coordonatelor date.
     *
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @param x Coordonata x a tile-ului curent al entității (în unități de tile-uri).
     * @param y Coordonata y a tile-ului curent al entității (în unități de tile-uri).
     * @return {@code true} dacă tile-ul din dreapta nu este gol (ID != 0), {@code false} altfel.
     */
    public static boolean isEntityOnWall(int[][] lvlData, int x, int y){
        // Verifică dacă tile-ul din dreapta este solid (ID != 0, presupunând că 0 este gol)
        // Această logică poate necesita ajustare în funcție de cum sunt definite tile-urile goale/solide.
        if (y >= 0 && y < lvlData.length && x + 1 >= 0 && x + 1 < lvlData[y].length) {
            return lvlData[y][x + 1] != 0; // Presupune că 0 este un tile gol/nesolid
        }
        return false; // În afara limitelor sau tile-ul din dreapta este gol
    }

    /**
     * Verifică dacă o entitate se află sub un tavan.
     * Această metodă pare incompletă sau specifică unui anumit context,
     * deoarece verifică doar un singur tile deasupra coordonatelor date.
     *
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @param x Coordonata x a tile-ului curent al entității (în unități de tile-uri).
     * @param y Coordonata y a tile-ului curent al entității (în unități de tile-uri).
     * @return {@code true} dacă tile-ul deasupra nu este gol (ID != 0), {@code false} altfel.
     */
    public static boolean isEntityOnCeiling(int[][] lvlData, int x, int y){
        // Verifică dacă tile-ul deasupra este solid (ID != 0)
        if (y - 1 >= 0 && y - 1 < lvlData.length && x >= 0 && x < lvlData[y-1].length) {
            return lvlData[y - 1][x] != 0; // Presupune că 0 este un tile gol/nesolid
        }
        return false; // În afara limitelor sau tile-ul deasupra este gol
    }

    /**
     * Calculează poziția X a unei entități astfel încât să fie exact lângă un perete,
     * fără a-l penetra, în funcție de direcția de mișcare.
     *
     * @param hitbox Dreptunghiul de coliziune al entității.
     * @param xSpeed Viteza orizontală a entității (pozitivă pentru dreapta, negativă pentru stânga).
     * @return Noua coordonată X a hitbox-ului.
     */
    public static float getEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed){
        int currentTile = (int)(hitbox.x / Game.TILES_SIZE); // Tile-ul curent în care se află marginea stângă a hitbox-ului
        if(xSpeed > 0){ // Se deplasează la dreapta
            // Calculează poziția astfel încât marginea dreaptă a hitbox-ului să fie la marginea stângă a următorului tile solid
            int tileXPos = (currentTile + 1) * Game.TILES_SIZE; // Marginea stângă a tile-ului din dreapta
            return tileXPos - hitbox.width -1; // -1 pentru a evita suprapunerea exactă
        } else { // Se deplasează la stânga
            // Calculează poziția astfel încât marginea stângă a hitbox-ului să fie la marginea dreaptă a tile-ului solid din stânga
            return currentTile * Game.TILES_SIZE; // Marginea dreaptă a tile-ului din stânga (care este și marginea stângă a tile-ului curent)
        }
    }

    /**
     * Calculează poziția Y a unei entități astfel încât să fie exact sub un tavan
     * sau deasupra unei podele, fără a le penetra, în funcție de direcția de mișcare verticală.
     *
     * @param hitbox Dreptunghiul de coliziune al entității.
     * @param airSpeed Viteza verticală a entității (pozitivă pentru cădere, negativă pentru săritură).
     * @return Noua coordonată Y a hitbox-ului.
     */
    public static float getEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed){
        int currentTile = (int)(hitbox.y / Game.TILES_SIZE); // Tile-ul curent în care se află marginea de sus a hitbox-ului
        if(airSpeed > 0){ // Entitatea cade (atinge podeaua)
            // Poziționează hitbox.y astfel încât hitbox.y + hitbox.height să fie la marginea de sus a următorului tile.
            // Entitatea aterizează pe rândul de tile-uri de sub currentTile, adică (currentTile + 1).
            // Marginea de sus a acestui tile de aterizare este (currentTile + 1) * Game.TILES_SIZE.
            // Dorim ca hitbox.y + hitbox.height = (currentTile + 1) * Game.TILES_SIZE.
            // Deci, hitbox.y = (currentTile + 1) * Game.TILES_SIZE - hitbox.height.
            // Se adaugă -1 pentru a evita suprapunerea exactă și posibile probleme de coliziune ulterioare.
            return (float)((currentTile + 1) * Game.TILES_SIZE - hitbox.height -1);
        } else { // Entitatea sare (atinge tavanul)
            // Poziționează hitbox.y astfel încât să fie la marginea de jos a tile-ului pe care l-a lovit.
            // currentTile este rândul de tile-uri în care se află (și a lovit) marginea de sus a entității.
            // Marginea de jos a acestui tile este currentTile * Game.TILES_SIZE + Game.TILES_SIZE.
            // Deci, hitbox.y = currentTile * Game.TILES_SIZE + Game.TILES_SIZE.
            return (float)(currentTile * Game.TILES_SIZE + Game.TILES_SIZE);
        }
    }

    /**
     * O versiune mai puțin strictă a metodei {@link #canMoveHere(float, float, float, float, int[][])},
     * potențial pentru entități mai mari precum boșii.
     * Verifică mai puține puncte: cele 4 colțuri și centrul marginii frontale (în direcția de mișcare).
     *
     * @param x Poziția x dorită a colțului stânga-sus al hitbox-ului.
     * @param y Poziția y dorită a colțului stânga-sus al hitbox-ului.
     * @param width Lățimea hitbox-ului entității.
     * @param height Înălțimea hitbox-ului entității.
     * @param lvlData Matricea 2D cu datele tile-urilor nivelului.
     * @param direction Direcția de mișcare a entității (de ex., 1 pentru dreapta, -1 sau 0 pentru stânga).
     * @return {@code true} dacă entitatea se poate deplasa la noua poziție, {@code false} altfel.
     */
    public static boolean canBossMoveHere(float x, float y, float width, float height, int[][] lvlData, int direction) {
        // Verifică cele 4 colțuri
        if (isSolid(lvlData, x, y)) return false;                 // Stânga-sus
        if (isSolid(lvlData, x + width, y)) return false;          // Dreapta-sus
        if (isSolid(lvlData, x, y + height)) return false;         // Stânga-jos
        if (isSolid(lvlData, x + width, y + height)) return false; // Dreapta-jos

        // Verifică centrul marginii frontale în funcție de direcție
        // (Presupunând că direction > 0 este dreapta, direction <= 0 este stânga, similar cu clasa Enemy)
        if (direction > 0) { // Se deplasează la Dreapta
            if (isSolid(lvlData, x + width, y + height / 2)) return false; // Centrul marginii din dreapta
        } else { // Se deplasează la Stânga
            if (isSolid(lvlData, x, y + height / 2)) return false; // Centrul marginii din stânga
        }
        
        // Opțional: S-ar putea verifica și centrul marginilor de sus/jos dacă mișcarea verticală ar fi implicată.
        // Momentan, este folosită în principal pentru mișcarea orizontală a boșilor.

        return true; // Dacă toate punctele verificate nu sunt solide
    }
}
