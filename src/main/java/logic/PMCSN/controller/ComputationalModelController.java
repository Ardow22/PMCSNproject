package logic.PMCSN.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;

import java.util.List;

import logic.PMCSN.libraries.Msq;
import logic.PMCSN.libraries.Rngs;
import logic.PMCSN.model.TimeSlot;
import static logic.PMCSN.model.Constants.*;
import static logic.PMCSN.model.Constants.PERCENTAGE;
import static logic.PMCSN.model.Events.*;

//classe per tener traccia del tempo
class MsqT {
    double current;  //tempo corrente                
    double next;     //tempo del prossimo evento               
}

//classe di supporto ad accumulare le statistiche di un singolo server
class MsqSum {                      
    double service;  //tempo di servizio totale impiegato                
    long served;    //numero clienti serviti in totale              
}

//classe per modellare un evento della simulazione
class MsqEvent {                    
    double t;   //tempo in cui avverrà l'evento
    int x;      //stato dell'evento: 1 è attivo, 0 è inattivo
}

/* TIPOLOGIE DI EVENTO IN BASE ALL'INDICE NELL'ARRAY EVENTS
 * CODA GIALLA
 * 0 arrivo
 * 1-10 servizio
 * 
 * INFOPOINT
 * 11 arrivo
 * 12-16 servizio
 * 
 * CODA ARANCIONE
 * 17 arrivo
 * 18-25 servizio
 * 26 abbandono
 */

/* (PROVA)
 * CODA GIALLA
 * 0 arrivo
 * 1-2 servizio
 * 
 * INFOPOINT
 * 3 arrivo
 * 4-5 servizio
 * 
 * CODA ARANCIONE
 * 6 arrivo
 * 7-8 servizio
 * 9 abbandono
 */

/* (PROVA)
 * LOGIN
 * 0 arrivo
 * 1-2 servizio
 * 3 abbandono
 * 
 * ULTIMATE TEAM
 * 4 arrivo
 * 5-6 servizio
 * 7 abbandono
 * 
 * STAGIONI
 * 8 arrivo
 * 9-10 servizio
 * 11 abbandono
 * 
 * PROCLUB
 * 12 arrivo
 * 13-14 servizio
 * 15 abbandono
 * 
 * MATCHMAKING(ULTIMATE TEAM)
 * 16 arrivo
 * 17-18 servizio
 * 19 abbandono
 * 
 * MATCHMAKING(STAGIONI)
 * 20 arrivo
 * 21-22 servizio
 * 23 abbandono
 * 
 * MATCHMAKING(PROCLUB)
 * 24 arrivo
 * 25-26 servizio
 * 27 abbandono
 * 
 */



public class ComputationalModelController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double STOP = 3 * 3600; //dopo quanto tempo termina la simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo

    static List<TimeSlot> slotList = new ArrayList<>(); //lista con gli slot temporali da analizzare
	
	public void startAnalysis() {
		int streamIndex = 1; //per questo processo usiamo il flusso 1 del generatore di numeri casuali. Ogni processo avrà un flusso
		
		//inizializzazione degli utenti nelle varie code
		long queueLogin = 0;
		long queueUltimateTeam = 0;
        long queueStagioni = 0;
        long queueProClub = 0;
        long queueMatchmakingUT = 0;
        long queueMatchmakingS = 0;
        long queueMatchmakingPC = 0;
        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
        double firstCompletionLogin = 0;
        double firstCompletionUltimateTeam = 0;
        double firstCompletionStagioni = 0;
        double firstCompletionProClub = 0;
        double firstCompletionMatchmakingUT = 0;
        double firstCompletionMatchmakingS = 0;
        double firstCompletionMatchmakingPC = 0;
        
        //inizializzazione degli interi per memorizzare il totale dei completatamenti dei server in ogni coda
        int totalLoginCheck = 0;
        int totalUltimateTeamCheck = 0;
        int totalStagioniCheck = 0;
        int totalProClubCheck = 0;
        int totalMatchmakingUTcheck = 0;
        int totalMatchmakingScheck = 0;
        int totalMatchmakingPCcheck = 0;

		
		int e; //indice next event, cioè l'evento più imminente
		int s; //indice del server
		
		double areaLogin = 0.0;  /* time integrated number in the node */
		double areaUltimateTeam = 0.0;  /* time integrated number in the node */
		double areaStagioni = 0.0;  /* time integrated number in the node */
		double areaProClub = 0.0;  /* time integrated number in the node */
		double areaMatchmakingUT = 0.0;  /* time integrated number in the node */
		double areaMatchmakingS = 0.0;  /* time integrated number in the node */
		double areaMatchmakingPC = 0.0;  /* time integrated number in the node */
		
		double service; //tempo di servizio
		
		List<Double> dropoutsLoginQueue = new ArrayList<>();
		int dropoutsLogin = 0;
		List<Double> dropoutsUltimateTeamQueue = new ArrayList<>();
		int dropoutsUltimateTeam = 0;
		List<Double> dropoutsStagioniQueue = new ArrayList<>();
		int dropoutsStagioni = 0;
		List<Double> dropoutsProClubQueue = new ArrayList<>();
		int dropoutsProClub = 0;
		List<Double> dropoutsMatchmakingUTQueue = new ArrayList<>();
		int dropoutsMatchmakingUT = 0;
		List<Double> dropoutsMatchmakingSQueue = new ArrayList<>();
		int dropoutsMatchmakingS = 0;
		List<Double> dropoutsMatchmakingPCQueue = new ArrayList<>();
		int dropoutsMatchmakingPC = 0;
		
		
        //Setup generatore RNG
		Rngs rng = new Rngs();
		rng.plantSeeds(0); //chiedo all'utente di inserire un seme a runtime
		//rng.selectStream(streamIndex);
		
		//DEBUG
		//System.out.println("Seed inizializzato: " + rng.getSeed());
        /*System.out.println("Primi 5 numeri casuali:");
        for (int i = 0; i < 5; i++) {
            System.out.println("  " + rng.random());
        }*/
		
        //inizializzazione dei 3 time slot
		System.out.println("\n------------INIZIALIZZAZIONE DEI TIME SLOT--------------");
        for (int f = 0; f < 3; f++) {
            TimeSlot slot = new TimeSlot(PERCENTAGE[f], 12062, 3600 * f, 3600 * (f + 1) - 1);
            slotList.add(slot);
        }
        System.out.println("Elenco dei time slot: ");
        for (TimeSlot sl: slotList) {
        	System.out.println("Slot di indice: " + slotList.indexOf(sl) + " e percentuale: " + sl.getPercentage());
        }
        
        System.out.println("\n-----------INIZIALIZZAZIONE EVENTI NELLA SIMULAZIONE-------------");
        int sumDebug = ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB;
        System.out.println("Eventi totali previsti nella simulazione: " + sumDebug);
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB];
        
        System.out.println("Lista eventi: ");
        for (int i = 0; i < ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB; i++) {
            events[i] = new MsqEvent();
            sum[i] = new MsqSum();
        }
        for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        System.out.println("\n-----INIZIALIZZAZIONE DEL CLOCK------------");
        System.out.println("Siamo nell'istante: " + t.current); // DEBUG PRINT
        System.out.println("Il prossimo evento avverrà all'istante: " + t.next);
        
        //PRIMO ARRIVO NEL SISTEMA GENERICO
        System.out.println("\n-----------PRIMO ARRIVO AL SISTEMA--------");
        
        double generic_t;
        events[0].t = getArrival(rng, 0, t.current);
        events[0].x = 1;
        
        
        System.out.println("Nuova lista eventi: ");
        for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }
        
        for (int i = 0; i < ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB; i++) {
        	if ((events[i].t != 0) && (events[i].x != 1)) {
        		events[i].t = START;
                events[i].x = 0;
                sum[i].service = 0.0;
                sum[i].served = 0;
        	}
        } 
        
        System.out.println("ULTIMA LISTA DI EVENTI PRIMA DELL'ITERAZIONE: ");
        for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }
                
        System.out.println("Inizializzazione completata. Pronto per l’iterazione.");
        System.out.printf("\nSIMULAZIONE DURERà DA %.0f a %.0f secondi (%.2f ore)\n", START, STOP, STOP / 3600.0);
        
        /* === INIZIO ITERAZIONE === */
        System.out.println("\n\n\n----INIZIA LA SIMULAZIONE------");
        System.out.println("La simulazione andrà avanti fin tanto ci sarà un arrivo nella coda del login oppure fino a quando ci sarà ancora qualcuno in qualche coda da servire");
        
        int iter = 0;
        
        while ((events[0].x != 0) || (queueLogin + queueUltimateTeam + queueStagioni + queueProClub + queueMatchmakingUT + queueMatchmakingS + queueMatchmakingPC != 0)) {
        //while (iter != 5) {
        	iter++;
        	System.out.println("\n-------NUOVA ITERAZIONE, è LA NUMERO: " + iter);
        	System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI: ");
            for (int i = 0; i < events.length; i++) {
            	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
            	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
            	System.out.println(" ");
            }
            System.out.println("Events[0].x vale " + events[0].x);
            System.out.println("queueLogin vale " + queueLogin);
            System.out.println("queueUltimateTeam vale " + queueUltimateTeam);
            System.out.println("queueStagioni vale " + queueStagioni);
            System.out.println("queueProClub vale " + queueProClub);
            System.out.println("queueMatchmakingUT vale " + queueMatchmakingUT);
            System.out.println("queueMatchmakingS vale " + queueMatchmakingS);
            System.out.println("queueMatchmakingPC vale " + queueMatchmakingPC);
            
        	System.out.println("\n----SITUAZIONE ABBANDONI-----------");
        	System.out.println("ABBANDONI LOGIN: ");
        	for (double info: dropoutsLoginQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI ULTIMATE TEAM: ");
        	for (double info: dropoutsUltimateTeamQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI STAGIONI: ");
        	for (double info: dropoutsStagioniQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI PRO CLUB: ");
        	for (double info: dropoutsProClubQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI MATCHMAKING ULTIMATE TEAM: ");
        	for (double info: dropoutsMatchmakingUTQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI MATCHMAKING STAGIONI: ");
        	for (double info: dropoutsMatchmakingSQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI PRO CLUB: ");
        	for (double info: dropoutsMatchmakingPCQueue) {
        		System.out.println(info);
        	}
        	
        	       	
        	if(!dropoutsLoginQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni del Login non è vuota");
        		events[3].t = dropoutsLoginQueue.get(0);
        		System.out.println("L'evento di abbandono del Login avverrà all'istante " + events[3].t);
        		events[3].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL LOGIN: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni del Login è vuota, l'evento viene disattivato");
        		events[3].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsUltimateTeamQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda di Ultimate Team non è vuota");
        		events[7].t = dropoutsUltimateTeamQueue.get(0);
        		System.out.println("L'evento di abbandono della coda di Ultimate Team avverrà all'istante " + events[7].t);
        		events[7].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO Di ULTIMATE TEAM: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni della coda di Ultimate Team è vuota, l'evento viene disattivato");
        		events[7].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsStagioniQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda delle Stagioni non è vuota");
        		events[11].t = dropoutsStagioniQueue.get(0);
        		System.out.println("L'evento di abbandono avverrà della coda delle Stagioni all'istante " + events[11].t);
        		events[11].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DELLE STAGIONI: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni della coda delle Stagioni è vuota, l'evento viene disattivato");
        		events[11].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsProClubQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda del Pro Club non è vuota");
        		events[15].t = dropoutsProClubQueue.get(0);
        		System.out.println("L'evento di abbandono avverrà all'istante " + events[15].t);
        		events[15].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL PRO CLUB: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni della coda del Pro Club è vuota, l'evento viene disattivato");
        		events[15].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsMatchmakingUTQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda del matchmaking di Ultimate Team non è vuota");
        		events[19].t = dropoutsMatchmakingUTQueue.get(0);
        		System.out.println("L'evento di abbandono della coda del matchmaking di Ultimate Team avverrà all'istante " + events[19].t);
        		events[19].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL MATCHMAKING DI ULTIMATE TEAM: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni della coda del matchmaking di Ultimate Team è vuota, l'evento viene disattivato");
        		events[19].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsMatchmakingSQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda del matchmaking delle stagioni non è vuota");
        		events[23].t = dropoutsMatchmakingSQueue.get(0);
        		System.out.println("L'evento di abbandono avverrà all'istante " + events[23].t);
        		events[23].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL MATCHMAKING DELLE STAGIONI: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni del matchmaking delle stagioni è vuota, l'evento viene disattivato");
        		events[23].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsMatchmakingPCQueue.isEmpty()) {
        		System.out.println("La lista di abbandoni della coda del matchmaking di Pro Club non è vuota");
        		events[27].t = dropoutsMatchmakingPCQueue.get(0);
        		System.out.println("L'evento di abbandono della coda del matchmaking di Pro Club avverrà all'istante " + events[27].t);
        		events[27].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL MATCHMAKING DI PRO CLUB: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni della coda del matchmaking di Pro Club è vuota, l'evento viene disattivato");
        		events[27].x = 0; //disattivo l'evento di abbandono
        	}
        	


            // Trova evento più imminente
            e = nextEvent(events);
            System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.next = events[e].t;
            System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);
    		areaLogin += (t.next - t.current)*queueLogin;
    		areaUltimateTeam += (t.next - t.current)*queueUltimateTeam;
    		areaStagioni += (t.next - t.current)*queueStagioni;
    		areaProClub += (t.next - t.current)*queueProClub;
    		areaMatchmakingUT += (t.next - t.current)*queueMatchmakingUT;
    		areaMatchmakingS += (t.next - t.current)*queueMatchmakingS;
    		areaMatchmakingPC += (t.next - t.current)*queueMatchmakingPC;
    		System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento avverrà all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);

            if (e == 0) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL LOGIN-------------");
            	queueLogin++;
            	System.out.println("Elementi in coda Login: " + queueLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà");
                    events[0].x = 0;
        		}
            	
            	            	
            	if (queueLogin <= SERVERS_LOGIN) {
            		System.out.println("Ci sono meno utenti in coda di quanti server totali");
            		service = getService(rng, YQ_SR);
            		System.out.println("Si cerca un server libero");
            		s = findLoginServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == 4) { //e == 4, cioè l'arrivo ad Ultimate Team
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI ULTIMATE TEAM----------");
            	queueUltimateTeam++;
            	System.out.println("Elementi in coda ad Ultimate Team: " + queueUltimateTeam);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
            	}*/
            	
            	System.out.println("Numero di server della coda Ultimate Team: " + SERVERS_ULTIMATE_TEAM);
            	if (queueUltimateTeam <= SERVERS_ULTIMATE_TEAM) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_ULTIMATE_TEAM);
            		s = findUltimateTeamServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}	
            
            } else if (e == 8) { // e == 8 arrivo alla coda Stagioni
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI STAGIONI----------");
            	queueStagioni++;
            	System.out.println("Elementi in coda a Stagioni: " + queueStagioni);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
            	}*/
            	
            	System.out.println("Numero di server della coda Stagioni: " + SERVERS_STAGIONI);
            	if (queueStagioni <= SERVERS_STAGIONI) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_STAGIONI);
            		s = findStagioniServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == 12) { //e == 12 arrivo alla coda Pro Club
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI PRO CLUB----------");
            	queueProClub++;
            	System.out.println("Elementi in coda a Pro Club: " + queueProClub);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
            	}*/
            	
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_PRO_CLUB);
            	if (queueProClub <= SERVERS_PRO_CLUB) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_PRO_CLUB);
            		s = findProClubServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == 16) {//e == 16, arrivo al Matchmaking di Ultimate Team
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DEL MATCHMAKING DI ULTIMATE TEAM----------");
            	queueMatchmakingUT++;
            	System.out.println("Elementi in coda al matchmaking di Ultimate Team: " + queueMatchmakingUT);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
//            	}*/
            	
            	System.out.println("Numero di server della coda di matchmaking di Ultimate Team: " + SERVERS_MATCHMAKING_UT);
            	if (queueMatchmakingUT <= SERVERS_MATCHMAKING_UT) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_MATCHMAKING_UT);
            		s = findMatchmakingUtServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == 20) { //e == 20, arrivo alla coda matchmaking di Stagioni
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DEL MATCHMAKING DI STAGIONI----------");
            	queueMatchmakingS++;
            	System.out.println("Elementi in coda al matchmaking di Stagioni: " + queueMatchmakingS);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
            	}*/
            	
            	System.out.println("Numero di server della coda di matchmaking Stagioni: " + SERVERS_MATCHMAKING_STAGIONI);
            	if (queueMatchmakingS <= SERVERS_MATCHMAKING_STAGIONI) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_MATCHMAKING_STAGIONI);
            		s = findMatchmakingStagioniServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == 24) { //e == 24, arrivo alla coda matchmaking di Pro Club
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DEL MATCHMAKING DI PRO CLUB----------");
            	queueMatchmakingPC++;
            	System.out.println("Elementi in coda al matchmaking di Pro Club: " + queueMatchmakingPC);
            	
            	/*System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo al Login)");
            	events[0].t = getArrival(rng, 0, t.current);
            	System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                    events[0].x = 0;
            	}*/
            	
            	System.out.println("Numero di serventi della coda del matchmaking di Pro Club: " + SERVERS_MATCHMAKING_PRO_CLUB);
            	if (queueMatchmakingPC <= SERVERS_MATCHMAKING_PRO_CLUB) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_MATCHMAKING_PRO_CLUB);
            		s = findMatchmakingPCServer(events);
            		System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if ((e >= 1) && (e <= 2)) { //eventi dei server di Login
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL LOGIN------------------");
            	
            	if (firstCompletionLogin == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionLogin = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli del Login");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueLogin--;
            		s = e;
                	if (queueLogin >= SERVERS_LOGIN) {
                		System.out.println("Ci sono degli elementi in coda Login da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda Login, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalLoginCheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nel Login: " + totalLoginCheck);
                	queueLogin--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nel Login: " + queueLogin);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	
                	
                	if ((rng.random() <= 0.9) && (rng.random() >= 0.8)) {
                		System.out.println("L'utente andrà in coda Ultimate Team");            		
                	    events[4].t = t.current; //aggiunto un evento alla coda Ultimate Team
                		events[4].x = 1; //attivazione dell'evento
                	} else if ((rng.random() <= 0.7) && (rng.random() >= 0.5)) {
                		System.out.println("L'utente andrà in coda Stagioni");            		
                	    events[8].t = t.current; //aggiunto un evento alla coda Stagioni
                		events[8].x = 1; //attivazione dell'evento	
                	} else {
                		System.out.println("L'utente andrà in coda Pro Club");
                		events[12].t = t.current; //aggiunto un evento alla coda Pro Club
                		events[12].x = 1;//attivazione dell'evento
                	}
                	
                	s = e;
                	
                	if (queueLogin >= SERVERS_LOGIN) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda Login da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda Login, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
        	} else if ((e >= 5) && (e <= 6)) { //eventi dei server di Ultimate Team
        		System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AD ULTIMATE TEAM------------------");
            	if (firstCompletionUltimateTeam == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionUltimateTeam = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli di Ultimate Team");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsUltimateTeamQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueUltimateTeam--;
            		s = e;
                	if (queueUltimateTeam >= SERVERS_ULTIMATE_TEAM) {
                		System.out.println("Ci sono degli elementi in coda Ultimate Team da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalUltimateTeamCheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nella coda Ultimate Team: " + totalUltimateTeamCheck);
                	queueUltimateTeam--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nella coda UltimateTeam: " + queueUltimateTeam);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	
                	System.out.println("L'utente andrà in coda matchmaking UT");
                	events[16].t= t.current;
                	events[16].x= 1;
                	
                	s = e;
                	
                	if (queueUltimateTeam >= SERVERS_ULTIMATE_TEAM) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda Ultimate Teame da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 9) && (e <= 10)) { //eventi dei server di Stagioni
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER DI STAGIONI------------------");
            	if (firstCompletionStagioni == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionStagioni = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli di Stagioni");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsStagioniQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueStagioni--;
            		s = e;
                	if (queueStagioni >= SERVERS_STAGIONI) {
                		System.out.println("Ci sono degli elementi in coda Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda Stagioni, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalStagioniCheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nella coda di Stagioni: " + totalStagioniCheck);
                	queueStagioni--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nella coda di Stagioni: " + queueStagioni);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	
                	System.out.println("L'utente andrà in coda matchmaking Stagioni");
                	events[20].t= t.current;
                	events[20].x= 1;
                	
                	s = e;
                	
                	if (queueStagioni >= SERVERS_STAGIONI) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda Stagioni, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 13) && (e <= 14)) { //eventi dei server di Pro Club
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL PRO CLUB------------------");
            	if (firstCompletionProClub == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionProClub = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli del ProClub");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsProClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueProClub--;
            		s = e;
                	if (queueProClub >= SERVERS_PRO_CLUB) {
                		System.out.println("Ci sono degli elementi in coda ProClub da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalProClubCheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nel ProClub: " + totalProClubCheck);
                	queueProClub--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nel ProClub: " + queueProClub);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	
                	System.out.println("L'utente andrà in coda matchmaking Pro Club");
                	events[24].t= t.current;
                	events[24].x= 1;
                	
                	s = e;
                	
                	if (queueProClub >= SERVERS_PRO_CLUB) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda ProClub da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 17) && (e <= 18)) { //eventi dei server di matchmaking di Ultimate Team
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL MATCHMAKING UT------------------");
            	if (firstCompletionMatchmakingUT == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMatchmakingUT = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli del MatchmakingUT");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsMatchmakingUTQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueMatchmakingUT--;
            		s = e;
                	if (queueMatchmakingUT >= SERVERS_MATCHMAKING_UT) {
                		System.out.println("Ci sono degli elementi in coda MatchmakingUT da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda MatchmakingUT, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalMatchmakingUTcheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nel MatchmakingUT: " + totalMatchmakingUTcheck);
                	queueMatchmakingUT--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nel MatchmakingUT: " + queueMatchmakingUT);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	s = e;
                	
                	if (queueMatchmakingUT >= SERVERS_MATCHMAKING_UT) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda MatchmakingUT da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda MatchmakingUT, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 21) && (e <= 22)) { //eventi dei server di matchmaking di Stagioni
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL MATCHMAKING STAGIONI------------------");
            	if (firstCompletionMatchmakingS == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMatchmakingS = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli del Matchmaking Stagioni");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsMatchmakingSQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueMatchmakingS--;
            		s = e;
                	if (queueMatchmakingS >= SERVERS_MATCHMAKING_STAGIONI) {
                		System.out.println("Ci sono degli elementi in coda Matchmaking Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda Matchmaking Stagioni, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalMatchmakingScheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nel Matchmaking Stagioni: " + totalMatchmakingScheck);
                	queueMatchmakingS--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nel Matchmaking Stagioni: " + queueMatchmakingS);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	s = e;
                	
                	if (queueMatchmakingS >= SERVERS_MATCHMAKING_STAGIONI) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda Matchmaking Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda Matchmaking Stagioni, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 25) && (e <= 26)) { //eventi dei server di matchmaking di Pro Club
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL MATCHMAKING PRO CLUB------------------");
            	if (firstCompletionMatchmakingPC == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMatchmakingPC = t.current; 
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		System.out.println("L'utente non ha superato i controlli del Matchmaking Pro Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsMatchmakingPCQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il server " + e + " per abbandono");
            		queueMatchmakingPC--;
            		s = e;
                	if (queueMatchmakingPC >= SERVERS_MATCHMAKING_PRO_CLUB) {
                		System.out.println("Ci sono degli elementi in coda Matchmaking Pro Club da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda Matchmaking Pro Club, il server " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else {
            		totalMatchmakingPCcheck++;//aumento il numero di utenti serviti in questo centro
                	System.out.println("Utenti serviti nel Matchmaking Pro Club: " + totalMatchmakingPCcheck);
                	queueMatchmakingPC--;//diminuisco di 1 il numero di utenti in coda in questo centro
                	System.out.println("Utenti ancora nel Matchmaking Pro Club: " + queueMatchmakingPC);
                	//events[0].t = t.current; //genero un nuovo arrivo al Login
                	//events[0].x = 1;//attivo il nuovo evento al Login
                	//System.out.println("Generato e attivato un nuovo arrivo alla coda Login che avverrà al tempo: " + t.current);
                	s = e;
                	
                	if (queueMatchmakingPC >= SERVERS_MATCHMAKING_PRO_CLUB) {//ci sono ancora elementi in coda
                		System.out.println("Ci sono degli elementi in coda Matchmaking Pro Club da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, INFOQ_SR);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		System.out.println("Non ci sono altri elementi in coda Matchmaking Pro Club, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if (e == 3) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA LOGIN------------");
            	dropoutsLogin++;
            	dropoutsLoginQueue.remove(0);	
            } else if (e == 7) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA ULTIMATE TEAM------------");
            	dropoutsUltimateTeam++;
            	dropoutsUltimateTeamQueue.remove(0);
            	
            } else if (e == 11) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA STAGIONI------------");
            	dropoutsStagioni++;
            	dropoutsStagioniQueue.remove(0);
            	
            } else if (e == 15) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA PRO CLUB------------");
            	dropoutsProClub++;
            	dropoutsProClubQueue.remove(0);
            	
            } else if (e == 19) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA MATCHMAKING ULTIMATE TEAM------------");
            	dropoutsMatchmakingUT++;
            	dropoutsMatchmakingUTQueue.remove(0);
            	
            } else if (e == 23) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA MATCHMAKING STAGIONI------------");
            	dropoutsMatchmakingS++;
            	dropoutsMatchmakingSQueue.remove(0);
            	
            } else if (e == 27) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA MATCHMAKING PRO CLUB------------");
            	dropoutsMatchmakingPC++;
            	dropoutsMatchmakingPCQueue.remove(0);
            	
            }
        }
        System.out.println("Fine simulazione.");
        
        /*
        System.out.println("------------------RECUPERO STATISTICHE------------------------------");
        //formattiamo i numeri decimali in stringhe
        DecimalFormat f = new DecimalFormat("###0.00");
        DecimalFormat g = new DecimalFormat("###0.000");
        
        System.out.println("\n-------STATISTICHE CODA GIALLA---------");
        System.out.println("\nFor " + totalYellowcheck + " job the Yellow queue check statistics are:\n");//quante persone hanno completato i controlli gialli
        System.out.println("  avg interarrivals .. =   " + f.format(events[0].t / totalYellowcheck));//tempo medio di arrivo tra una pesona e l'altra
        System.out.println("  avg wait ........... =   " + f.format(areaYellow / totalYellowcheck));//tempo medio passato nel sistema (attesa + servizio)
        
        double yellowCheckFinalTime = 0; //tempo massimo di completamento tra i server
        for (s = 0; s <= SERVERS_YELLOW; s++) {
        	if (events[s].t > yellowCheckFinalTime) {
        		yellowCheckFinalTime = events[s].t;
        	}
        }
        
        double yellowCheckActualTime = yellowCheckFinalTime - firstCompletationYellowServer;//tempo totale durante cui i server sono stati effettivamente attivi
        System.out.println("  avg # in node ...... =   " + f.format(areaYellow/yellowCheckActualTime)); //formula di Little
        
        for (s = 1; s <= SERVERS_YELLOW; s++) {
        	areaYellow -= sum[s].service; //rimozione dei tempi di servizio per ottenere il tempo medio in coda (delay)
        }
        
        System.out.println("  avg delay: " + areaYellow / totalYellowcheck); //ritardo medio (solo coda, senza servizio)
        
        System.out.println("\nThe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");
        
        double allServices = 0;
        double allServed = 0;
        
        //utilizzo%, tempo medio di servizio, %carico gestito dal server
        for (s = 1; s <= SERVERS_YELLOW; s++) {
        	System.out.print("       " + (s) + "          " + g.format(sum[s].service / yellowCheckActualTime) + "            ");
        	System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double) totalYellowcheck));
        	allServices += sum[s].service;
        	allServed += sum[s].served;
        }
        
        System.out.println("  avg service time: " + g.format(allServices / allServed));
        
        System.out.println("");
        
        System.out.println("\n--------STATISTICHE INFOPOINT------------------");
        System.out.println("\nFor " + totalInfopointcheck + " job the Infopoint queue check statistics are:\n");//quante persone hanno completato i controlli gialli
        System.out.println("  avg interarrivals .. =   " + f.format(events[3].t / totalInfopointcheck));//tempo medio di arrivo tra una pesona e l'altra
        System.out.println("  avg wait ........... =   " + f.format(areaInfopoint / totalInfopointcheck));//tempo medio passato nel sistema (attesa + servizio)
        
        double infopointCheckFinalTime = 0; //tempo massimo di completamento tra i server
        for (s = 4; s <= 5; s++) {
        	if (events[s].t > infopointCheckFinalTime) {
        		infopointCheckFinalTime = events[s].t;
        	}
        }
        
        double infopointCheckActualTime = infopointCheckFinalTime - firstCompletationInfopoint;//tempo totale durante cui i server sono stati effettivamente attivi
        System.out.println("  avg # in node ...... =   " + f.format(areaInfopoint/infopointCheckActualTime)); //formula di Little
        
        for (s = 4; s <= 5; s++) {
        	areaInfopoint -= sum[s].service; //rimozione dei tempi di servizio per ottenere il tempo medio in coda (delay)
        }
        
        System.out.println("  avg delay: " + areaInfopoint / totalInfopointcheck); //ritardo medio (solo coda, senza servizio)
        
        System.out.println("\nThe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");
        
        allServices = 0;
        allServed = 0;
        
        //utilizzo%, tempo medio di servizio, %carico gestito dal server
        for (s = 4; s <= 5; s++) {
        	System.out.print("       " + (s-3) + "          " + g.format(sum[s].service / infopointCheckActualTime) + "            ");
        	System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double) totalInfopointcheck));
        	allServices += sum[s].service;
        	allServed += sum[s].served;
        }
        
        System.out.println("  avg service time: " + g.format(allServices / allServed));
        
        System.out.println("");
     
        System.out.println("\n--------STATISTICHE CODA ARANCIONE-------------");
        System.out.println("\nFor " + totalOrangecheck + " job the Orange Code queue statistics are:\n");//quante persone hanno completato i controlli gialli
        System.out.println("  avg interarrivals .. =   " + f.format(events[6].t / totalOrangecheck));//tempo medio di arrivo tra una pesona e l'altra
        System.out.println("  avg wait ........... =   " + f.format(areaOrange / totalOrangecheck));//tempo medio passato nel sistema (attesa + servizio)
        
        double orangeCheckFinalTime = 0; //tempo massimo di completamento tra i server
        for (s = 7; s <= 8; s++) {
        	if (events[s].t > orangeCheckFinalTime) {
        		orangeCheckFinalTime = events[s].t;
        	}
        }
        
        double orangeCheckActualTime = orangeCheckFinalTime - firstCompletationOrangeServer;//tempo totale durante cui i server sono stati effettivamente attivi
        System.out.println("  avg # in node ...... =   " + f.format(areaOrange/orangeCheckActualTime)); //formula di Little
        
        for (s = 7; s <= 8; s++) {
        	areaOrange -= sum[s].service; //rimozione dei tempi di servizio per ottenere il tempo medio in coda (delay)
        }
        
        System.out.println("  avg delay: " + areaOrange / totalOrangecheck); //ritardo medio (solo coda, senza servizio)
        
        System.out.println("\nThe server statistics are:\n");
        System.out.println("    server     utilization     avg service      share");
        
        allServices = 0;
        allServed = 0;
        
        //utilizzo%, tempo medio di servizio, %carico gestito dal server
        for (s = 7; s <= 8; s++) {
        	System.out.print("       " + (s-6) + "          " + g.format(sum[s].service / orangeCheckActualTime) + "            ");
        	System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double) totalOrangecheck));
        	allServices += sum[s].service;
        	allServed += sum[s].served;
        }
        
        System.out.println("  avg service time: " + g.format(allServices / allServed));
        
        System.out.println("");
	*/}
	
	static boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(1 + streamIndex);
        return rngs.random() <= percentage;
    }
	
		
	int findLoginServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 1; //i server Login iniziano dall'indice 1 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 2) { //i < 2, perché i server login sono da 1 a 2 ma si entra già facendo i++ quindi deve essere minore stretto di 2  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findUltimateTeamServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = 5; //i server di Ultimate Team iniziano dall'indice 5 in events

        while (event[i].x == 1)  
            i++;                  
        s = i;
        while (i < 6) { //i < 6, perché i server di Ultimate Team sono da 5 a 6 
        	i++;                                           
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findStagioniServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 9; //i server delle Stagioni iniziano dall'indice 9 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 10) { //i < 10, perché i server delle Stagioni sono da 9 a 10 ma si entra già facendo i++ quindi deve essere minore stretto di 10  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findProClubServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 13; //i server infopoint iniziano dall'indice 13 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 14) { //i < 14, perché i server del Pro Club sono da 13 a 14 ma si entra già facendo i++ quindi deve essere minore stretto di 14  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findMatchmakingUtServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 17; //i server del matchmaking Ultimate Team iniziano dall'indice 17 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 18) { //i < 18, perché i server del matchmaking Ultimate Team sono da 17 a 18 ma si entra già facendo i++ quindi deve essere minore stretto di 18  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findMatchmakingStagioniServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 21; //i server Matchmaking Stagioni iniziano dall'indice 21 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 22) { //i < 22, perché i server del Matchmaking Stagioni sono da 21 a 22 ma si entra già facendo i++ quindi deve essere minore stretto di 22  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findMatchmakingPCServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 25; //i server Matchmaking Pro Club iniziano dall'indice 25 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 26) { //i < 26, perché i server del Matchmaking Pro Club sono da 25 a 26 ma si entra già facendo i++ quindi deve essere minore stretto di 26  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
			
	double getService(Rngs r, double serviceTime) {
        r.selectStream(3);
        return (exponential(serviceTime, r));
    }
	
	//funzione per generare tempi esponenziali
	double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	//funzione per generare il prossimo arrivo in base allo slot orario
	double getArrival(Rngs r, int streamIndex, double currentTime) {
        System.out.println("----CALCOLO DELL'ARRIVO----");
        System.out.println("Ultimo istante in cui è stato generato un arrivo è: " + sarrival);
		r.selectStream(1 + streamIndex);
        int index = TimeSlotController.timeSlotSwitch(slotList, currentTime);
        System.out.println("Lo slot orario individuato è quello di indice: " + index);

        sarrival += exponential(1 / (slotList.get(index).getAveragePoisson() / 3600), r);
        System.out.println("Quindi ora l'ultimo istante in cui è stato generato un arrivo è: " + (sarrival));

        return (sarrival);
    }
	
	int nextEvent(MsqEvent[] event) {
		System.out.println("Ricerca in corso del prossimo evento da elaborare...");
	    int e;
	    int i = 0;
	    while (event[i].x == 0) 
	    	i++;
	    e = i;
	    while (i < ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_PRO_CLUB + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_UT + ALL_EVENTS_MATCHMAKING_STAGIONI + ALL_EVENTS_MATCHMAKING_PRO_CLUB -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    System.out.println("Evento trovato con indice " + (e));
	    return (e);   
	}

}
