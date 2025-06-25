package gamestates;

/**
 * Enumerație ce definește diferitele stări posibile ale jocului.
 * Fiecare constantă reprezintă o secțiune distinctă a jocului, cum ar fi meniul principal, jocul propriu-zis, etc.
 */
public enum Gamestate {
    /** Starea în care jucătorul controlează personajul și interacționează cu nivelul. */
    PLAYING, 
    /** Starea meniului principal al jocului. */
    MENU, 
    /** Starea meniului de opțiuni/setări. */
    OPTIONS, 
    /** Starea care indică intenția de a părăsi jocul. */
    QUIT, 
    /** Starea pentru încărcarea unui joc salvat. */
    LOADGAME, 
    /** Starea pentru afișarea clasamentului (leaderboard). */
    LEADERBOARD,
    /** Starea inițială sau pentru introducerea numelui jucătorului. */
    ENTER_NAME;

    /** Starea curentă a jocului. Aceasta determină ce logică și randare sunt active. */
    public static Gamestate state = ENTER_NAME;
}
