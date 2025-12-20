package logic.PMCSN.model;

public class Events {

    // === UTENTI -> LOGIN ===
    public static int ARRIVAL_EVENT_LOGIN = 1;
    public static int SERVERS_LOGIN = 12;
    public static int DROPOUT_EVENT_LOGIN = 1;

    // === UTENTI CHE GIOCANO AD ULTIMATE TEAM -> SERVER DI ACCESSO AD ULTIMATE TEAM ===
    public static int ARRIVAL_EVENT_ULTIMATE_TEAM = 1;
    public static int SERVERS_ULTIMATE_TEAM = 13;
    public static int DROPOUT_EVENT_ULTIMATE_TEAM = 1;
    
    
    // === UTENTI CHE GIOCANO A STAGIONI -> SERVER DI ACCESSO A STAGIONI ===
    public static int ARRIVAL_EVENT_STAGIONI = 1;
    public static int SERVERS_STAGIONI = 2;
    public static int DROPOUT_EVENT_STAGIONI = 1;
    
    // === UTENTI CHE GIOCANO A CLUB -> SERVER DI ACCESSO A CLUB ===
    public static int ARRIVAL_EVENT_CLUB = 1;
    public static int SERVERS_CLUB = 5;
    public static int DROPOUT_EVENT_CLUB = 1;
    
    // === TOTALE EVENTI PER BLOCCO ===
    public static int ALL_EVENTS_LOGIN = ARRIVAL_EVENT_LOGIN + SERVERS_LOGIN + DROPOUT_EVENT_LOGIN;
    public static int ALL_EVENTS_ULTIMATE_TEAM = ARRIVAL_EVENT_ULTIMATE_TEAM + SERVERS_ULTIMATE_TEAM + DROPOUT_EVENT_ULTIMATE_TEAM;
    public static int ALL_EVENTS_STAGIONI = ARRIVAL_EVENT_STAGIONI + SERVERS_STAGIONI + DROPOUT_EVENT_STAGIONI;
    public static int ALL_EVENTS_CLUB = ARRIVAL_EVENT_CLUB + SERVERS_CLUB + DROPOUT_EVENT_CLUB;
    
    public static int ALL_EVENTS = ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_CLUB;
    public static int ALL_EVENTS_WITH_SAVE_STAT = ALL_EVENTS + 1; //l'ultimo evento è l'evento periodico SAVE_STAT per salvare le statisiche
    public static int EVENT_SERVER_LOGIN;
    public static int EVENT_SERVER_ULTIMATE_TEAM;
    public static int EVENT_SERVER_STAGIONI;
    public static int EVENT_SERVER_PRO_CLUB;
    		

 }
