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



public class ComputationalModelController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double STOP = 3 * 3600; //dopo quanto tempo termina la simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo

    static List<TimeSlot> slotList = new ArrayList<>(); //lista con gli slot temporali da analizzare
	
	public void startAnalysis() {
		int streamIndex = 1; //per questo processo usiamo il flusso 1 del generatore di numeri casuali. Ogni processo avrà un flusso
		
		//inizializzazione dei visitatori nelle varie code
		long queueInfopoint = 0;
        long queueYellowCheck = 0;
        long queueOrangeCheck = 0;
        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
        double firstCompletationYellowServer = 0;
        double firstCompletationInfopoint = 0;
        double firstCompletationOrangeServer = 0;
        
        //inizializzazione degli interi per memorizzare il totale dei controlli completati in ogni coda
        int totalYellowcheck = 0;
        int totalInfopointcheck = 0;
        int totalOrangecheck = 0;

		
		int e; //indice next event, cioè l'evento più imminente
		int s; //indice del server
		
		double areaInfopoint = 0.0;           /* time integrated number in the node */
        double areaYellow = 0.0;              /* time integrated number in the node */
		double areaOrange = 0.0;
		
		double service; //tempo di servizio
		
        List<Double> abandonsOrange = new ArrayList<>();
        int abandonsOrangeQueue = 0;
        
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
        int sumDebug = ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE;
        System.out.println("Eventi totali previsti nella simulazione: " + sumDebug);
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE];
        
        System.out.println("Lista eventi: ");
        for (int i = 0; i < ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE; i++) {
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
        System.out.println("\n-----------PRIMO ARRIVO AL SISTEMA GENERICO--------");
        
        double generic_t;
        generic_t = getArrival(rng, 0, t.current);
        if (rng.random() < 0.5) {
        	System.out.println("E' un visitatore con il QR Code, va nella coda gialla");
        	events[0].t = generic_t;
        	events[0].x = 1;
        }
        else {
        	System.out.println("E' un visitatore senza il QR Code, va all'infopoint");
        	events[3].t = generic_t;
        	events[3].x = 1;
        }
        
        System.out.println("Nuova lista eventi: ");
        for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }
        
        for (int i = 0; i < ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE; i++) {
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
        System.out.println("La simulazione andrà avanti fin tanto ci sarà un arrivo nella coda gialla, nella coda infopoint oppure fino a quando ci sarà ancora qualcuno in qualche coda da servire");
        
        int iter = 0;
        
        while ((events[0].x != 0) || (events[3].x != 0) || (queueInfopoint + queueYellowCheck + queueOrangeCheck != 0)) {
        //while (iter != 25) {
        	iter++;
        	System.out.println("\n-------NUOVA ITERAZIONE, è LA NUMERO: " + iter);
        	System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI: ");
            for (int i = 0; i < events.length; i++) {
            	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
            	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
            	System.out.println(" ");
            }
            System.out.println("Events[0].x vale " + events[0].x);
            System.out.println("Events[3].x vale " + events[3].x);
            System.out.println("queueInfopoint vale " + queueInfopoint);
            System.out.println("queueYellowCheck vale " + queueYellowCheck);
            System.out.println("queueOrangeCheck vale " + queueOrangeCheck);
        	
        	System.out.println("\n----SITUAZIONE ABBANDONI-----------");
        	System.out.println("ABBANDONI ARANCIONI: ");
        	for (double info: abandonsOrange) {
        		System.out.println(info);
        	}
        	       	
        	if(!abandonsOrange.isEmpty()) {
        		System.out.println("La lista di abbandoni non è vuota");
        		events[9].t = abandonsOrange.get(0);
        		System.out.println("L'evento di abbandono avverrà all'istante " + events[9].t);
        		events[9].x = 1; //attivo l'evento di abbandono
        		
        		System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }
        	}
        	else {
        		System.out.println("La lista di abbandoni è vuota, l'evento viene disattivato");
        		events[9].x = 0; //disattivo l'evento di abbandono
        	}

            // Trova evento più imminente
            e = nextEvent(events);
            System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.next = events[e].t;
            System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);
            areaInfopoint += (t.next - t.current)*queueInfopoint;       
            areaYellow += (t.next - t.current)*queueYellowCheck;          
    		areaOrange += (t.next - t.current)*queueOrangeCheck;
    		System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento avverrà all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);

            if (e == 0) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NELLA CODA GIALLA-------------");
            	queueYellowCheck++;
            	System.out.println("Elementi in coda gialla: " + queueYellowCheck);
            	System.out.println("Numero di serventi della coda gialla: " + SERVERS_YELLOW);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sia coda gialla o coda infopoint)");
            	generic_t = getArrival(rng, 0, t.current);
            	if (rng.random() < 0.5) {
            		events[0].t = generic_t;
            		System.out.println("--------(Sarà un arrivo in coda gialla, all'istante: " + events[0].t + ")");
            		if (events[0].t > STOP) {
            			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà");
                        events[0].x = 0;
            		}
            	}
            	else {
            		events[3].t = generic_t;
            		System.out.println("--------(Sarà un arrivo all'infopoint, all'istante: " + events[3].t + ")");
            		events[0].x = 0; //disattivo l'evento di arrivo alla coda gialla che altrimenti rimarrebbe attivo
            		if (events[3].t > STOP) {
            			System.out.println("------(però " + events[3].t + " è oltre " + STOP + "quindi non avverrà");
            			events[3].x = 0;
            		} else {
            			events[3].x = 1;
            		}
            	}            	
            	if (queueYellowCheck <= SERVERS_YELLOW) {
            		System.out.println("Ci sono meno visitatori in coda di quanti serventi totali");
            		service = getService(rng, YQ_SR);
            		System.out.println("Si cerca un server libero");
            		s = findYellowServer(events);
            		System.out.println("Abbiamo trovato il servente numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il servente " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == 3) { //e == 11, cioè l'arrivo all'infopoint
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALL'INFOPOINT----------");
            	queueInfopoint++;
            	System.out.println("Elementi in coda all'infopoint: " + queueInfopoint);
            	
            	System.out.println("-----------------(Intanto pianifico il nuovo evento di arrivo, che sia coda gialla o coda infopoint)");
            	generic_t = getArrival(rng, 0, t.current);
            	if (rng.random() < 0.5) {
            		events[0].t = generic_t;
            		System.out.println("--------(Sarà un arrivo in coda gialla, all'istante: " + events[0].t + ")");
            		events[3].x = 0;
            		if (events[0].t > STOP) {
            			System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà)");
                        events[0].x = 0;
            		} else {
            			events[0].x = 1;
            		}
            	}
            	else {
            		events[3].t = generic_t;
            		System.out.println("--------(Sarà un arrivo all'infopoint, all'istante: " + events[3].t + ")");
            		if (events[3].t > STOP) {
            			System.out.println("------(però " + events[3].t + " è oltre " + STOP + "quindi non avverrà)");
            			events[3].x = 0;
            		}
            	}
            	
            	System.out.println("Numero di serventi della coda infopoint: " + SERVERS_INFOPOINT);
            	if (queueInfopoint <= SERVERS_INFOPOINT) { //verifico se posso essere servito subito
            		service = getService(rng, INFOQ_SR);
            		System.out.println("Si cerca un server libero tra i " + SERVERS_INFOPOINT);
            		s = findInfoPointServer(events);
            		System.out.println("Abbiamo trovato il servente numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il servente " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}	
            
            } else if ((e >= 4) && (e <= 5)) { //sono gli eventi dei serventi in infopoint
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE ALL'INFOPOINT------------");
            	if (firstCompletationInfopoint == 0) { //salviamo il primo completamento per le statistiche
            		firstCompletationInfopoint = t.current;
            	}
            	totalInfopointcheck++; //aumento il numero di persone controllate in questo centro
            	System.out.println("Persone controllate all'infopoint: " + totalInfopointcheck);
            	queueInfopoint--; //diminuisco di 1 il numero di visitatori in coda in questo centro
            	System.out.println("Persone ancora nell'infopoint: " + queueInfopoint);
            	events[0].t = t.current; //genero un nuovo arrivo alla coda gialla
            	events[0].x = 1; //attivo il nuovo evento alla coda gialla
            	System.out.println("Generato e attivato un nuovo arrivo alla coda gialla che avverrà al tempo: " + t.current);
            	s = e;
            	if (queueInfopoint >= SERVERS_INFOPOINT) {//ci sono ancora elementi in coda
            		System.out.println("Ci sono degli elementi in coda infopoint da servire, ma ora il servente " + s + " si è liberato");
            		service = getService(rng, INFOQ_SR);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service; 
                    System.out.println("Il servente " + s + " concluderà all'istante " + events[s].t);
            	} else { //altrimenti, se non ci sono persone in coda
            		System.out.println("Non ci sono altri elementi in coda infopoint, il servente " + s + " diventa disponibile");
            		events[s].x = 0; //il server diventa libero
            		
            	}  	
            
            } else if ((e >= 1) && (e <= 2)) {//sono gli eventi dei serventi in coda gialla
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE ALLA CODA GIALLA------------");
            	if (firstCompletationYellowServer == 0) {
            		firstCompletationYellowServer = t.current;
            	}
            	totalYellowcheck++;
            	System.out.println("Persone controllate alla coda gialla: " + totalYellowcheck);
            	queueYellowCheck--;
            	System.out.println("Persone ancora nella coda gialla: " + queueYellowCheck);
            	
            	if (rng.random() < 0.5) {
            		System.out.println("Il visitatore andrà in coda arancione per ulteriori controlli");            		
            	    events[6].t = t.current; //aggiunto un evento alla coda arancione
            		events[6].x = 1; //attivazione dell'evento
            	}
            	
            	s = e;
            	if (queueYellowCheck >= SERVERS_YELLOW) {
            		System.out.println("Ci sono degli elementi in coda gialla da servire, ma ora il servente " + s + " si è liberato");
            		service = getService(rng, YQ_SR);
            		sum[s].service += service;
            		sum[s].served++;
            		events[s].t = t.current + service;
            		System.out.println("Il servente " + s + " concluderà all'istante " + events[s].t);
            	} else {
            		System.out.println("Non ci sono altri elementi in coda gialla, il servente " + s + " diventa disponibile");
            		events[s].x = 0;
            	}
            
            } else if (e == 6) { //arrivi nella coda arancione
            	System.out.println("\n--------L'EVENTO è UN NUOVO ARRIVO ALLA CODA ARANCIONE-----------");
            	events[6].x = 0; //disattivo l'arrivo alla coda arancione
            	queueOrangeCheck++;
            	System.out.println("Elementi in coda arancione: " + queueOrangeCheck);
            	if (queueOrangeCheck <= SERVERS_ORANGE) {
            		System.out.println("Ci sono meno visitatori di quanti serventi");
            		service = getService(rng, ORQ_SR);
            		System.out.println("Si cerca un servente libero");
            		s = findOrangeServer(events);
            		System.out.println("Trovato il servente di indice " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    System.out.println("Il servente terminerà all'istante di tempo " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if ((e >= 7) && (e <= 8)) { //eventi dei serventi nella coda arancione
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE ALLA CODA ARANCIONE------------");
            	if (firstCompletationOrangeServer == 0) {
            		firstCompletationOrangeServer = t.current;
            	}
            	boolean abandon = generateAbandon(rng, streamIndex, P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se il visitatore non supera i controlli
            		System.out.println("Il visitatore non ha superato i controlli arancioni");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		abandonsOrange.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni
            		System.out.println("Torna disponibile il servente " + e + " per abbandono");
            		queueOrangeCheck--;
            		s = e;
                	if (queueOrangeCheck >= SERVERS_ORANGE) {
                		System.out.println("Ci sono degli elementi in coda arancione da servire, ma ora il servente " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il servente " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda arancione, il servente " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }	
            	}
            	else { //il visitatore supera i controlli
            		System.out.println("Il visitatore ha superato i controlli arancioni");
            		totalOrangecheck++;
            		System.out.println("Persone controllate alla coda arancione: " + totalOrangecheck);
                	queueOrangeCheck--;
                	System.out.println("Persone ancora nella coda arancione: " + queueOrangeCheck);
                	s = e;
                	if (queueOrangeCheck >= SERVERS_ORANGE) {
                		System.out.println("Ci sono degli elementi in coda arancione da servire, ma ora il servente " + s + " si è liberato");
                		service = getService(rng, ORQ_SR);
                		sum[s].service += service;
                		sum[s].served++;
                		events[s].t = t.current + service;
                		System.out.println("Il servente " + s + " concluderà all'istante " + events[s].t);
            	     } else {
            	    	 System.out.println("Non ci sono altri elementi in coda arancione, il servente " + s + " diventa disponibile");
            	    	 events[s].x = 0;
            	     }
                }
            
            } else { //evento di abbandono della coda arancione, quindi events[26]
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA ARANCIONE------------");
            	abandonsOrangeQueue++;
            	abandonsOrange.remove(0);
            }
        }
        System.out.println("Fine simulazione.");
        
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
	}
	
	static boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(1 + streamIndex);
        return rngs.random() <= percentage;
    }
	
	int findYellowServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = 1; //i server gialli iniziano dall'indice 1 in events

        while (event[i].x == 1)  
            i++;                  
        s = i;
        while (i < 2) { //i < 10, perché i server gialli sono da 1 a 10 
        	i++;                                           
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findInfoPointServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 4; //i server infopoint iniziano dall'indice 12 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 5) { //i < 16, perché i server dell'infopoint sono da 12 a 16 ma si entra già facendo i++ quindi deve essere minore stretto di 16  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findOrangeServer(MsqEvent[] event) {
		/* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = 7; //i server arancioni iniziano dall'indice 18 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        while (i < 8) { //i < 25, perché i server arancioni sono da 18 a 25  
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
	    while (i < ALL_EVENTS_INFOPOINT + ALL_EVENTS_YELLOW + ALL_EVENTS_ORANGE -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    System.out.println("Evento trovato con indice " + (e));
	    return (e);   
	}

}
