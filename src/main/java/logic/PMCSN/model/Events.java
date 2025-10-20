package logic.PMCSN.model;

public class Events {

    // === UTENTI SENZA QR -> INFOPOINT ===
    public static int ARRIVAL_EVENT_INFOPOINT = 1;
    //public static int SERVERS_INFOPOINT = 5;
    public static int SERVERS_INFOPOINT = 2;

    // === UTENTI CON QR -> CONTROLLO PETTORINE GIALLE ===
    public static int ARRIVAL_EVENT_YELLOW = 1;
    //public static int SERVERS_YELLOW = 10;
    public static int SERVERS_YELLOW = 2;

    // === UTENTI CHE VANNO AL CONTROLLO ARANCIONI ===
    public static int ARRIVAL_EVENT_ORANGE = 1;
    //public static int SERVERS_ORANGE = 8;
    public static int SERVERS_ORANGE = 2;
    public static int ABANDON_EVENT_ORANGE = 1; //PERCHé SOLO DOPO I CONTROLLI ARANCIONI C'è LA POSSIBILITà DI ABBANDONARE L'INTERO SISTEMA

    // === TOTALE EVENTI PER BLOCCO ===
    public static int ALL_EVENTS_INFOPOINT = ARRIVAL_EVENT_INFOPOINT + SERVERS_INFOPOINT;
    public static int ALL_EVENTS_YELLOW = ARRIVAL_EVENT_YELLOW + SERVERS_YELLOW;
    public static int ALL_EVENTS_ORANGE = ARRIVAL_EVENT_ORANGE + SERVERS_ORANGE + ABANDON_EVENT_ORANGE;

 }
