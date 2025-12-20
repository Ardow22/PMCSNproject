package logic.PMCSN.controller;

import java.util.List;

import logic.PMCSN.model.Node;

public class StatisticsBatchController {
	private int batchsize;
	private int numBatches;
	
	public StatisticsBatchController(int batchsize, int numBatches) {
		this.batchsize = batchsize;
		this.numBatches = numBatches;
	}
	
	//funzione per verificare se la lista contiene numBatches valori
	public boolean checkBatchList(List newList) {
		if (newList.size() == numBatches) {
			return true;
		}
		return false;
	}
	
	//c1 contiene tutti i tempi di risposta, c2 contiene tutti i tempi di servizio.....
	//la funzione controlla se per ogni categoria ogni centro ha raggiunto la numBatches
    public boolean controlChecks(List<Boolean> c1, List<Boolean> c2, List<Boolean> c3, List<Boolean> c4, List<Boolean> c5, List<Boolean> c6, List<Boolean> c7) {
    	boolean control1 = false;
    	boolean control2 = false;
    	boolean control3 = false;
    	boolean control4 = false;
    	boolean control5 = false;
    	boolean control6 = false;
    	boolean control7 = false;
    	
    	control1 = helpControlChecks(c1);
    	control2 = helpControlChecks(c2);
    	control3 = helpControlChecks(c3);
    	control4 = helpControlChecks(c4);
    	control5 = helpControlChecks(c5);
    	control6 = helpControlChecks(c6);
    	control7 = helpControlChecks(c7);
    	
    	if (control1 && control2 && control3 && control4 && control5 && control6 && control7) {
    		return true;
    	}
    	else {
    		return false;
    	}
	}
    
    //funzione di support a controlChecks
    private boolean helpControlChecks(List<Boolean> newList) {
    	int error = 0;
    	for (int i = 0; i < newList.size(); i++) {
    		if ((!newList.get(i))) {
    			error += 1;
    		}
    	}
    	if (error == 0) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    public boolean verifyStats(int jobsServedPerBatch, int batchSize, Node node, MsqSum[] sum, double nodeArea, int indexFirstServer, int indexLastServer, double currentTime) {
    	System.out.println("Analisi del nodo con il primo server di indice: " + indexFirstServer);
    	System.out.println("Questo nodo ha servito un numero di job pari a " + jobsServedPerBatch);
    	
    	if ((jobsServedPerBatch != 0) && (jobsServedPerBatch % batchSize == 0)) {
    		double responseTime = nodeArea/jobsServedPerBatch;
    		double interarrivals;
    		double abandons;
    		double lambda = jobsServedPerBatch/ (node.getLastArrivalTime() - node.getCurrentStartTimeBatch());
    		double actualTime = currentTime - node.getCurrentStartTimeBatch();   
    		double avgPopulations = nodeArea/actualTime;
    		
    		double queueArea = nodeArea;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			queueArea -= sum[i].service;
    		}
    		double delaysTime = Math.max(0, queueArea/jobsServedPerBatch);
    		double avgQueuePopulations = queueArea/actualTime;
    		
    		double sumUtilizations = 0.0;
            double sumServices = 0.0;
            double sumServed = 0.0;
            for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			sumUtilizations += sum[i].service/actualTime;
    			sumServices += sum[i].service;
    			sumServed += sum[i].served;
    		}
            
            int numServers = 0;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			numServers++;
    		}
            double utilization = sumUtilizations/numServers;
            double serviceTime = sumServices/sumServed;
            
            node.getTempiMediDiRispostaBatch().add(responseTime);
    		node.getTempiMediInCodaBatch().add(delaysTime);
    		node.getTempiDiServizioBatch().add(serviceTime);
    		node.getPopolazioneDelSistemaBatch().add(avgPopulations);
    		node.getPopolazioneDellaCodaBatch().add(avgQueuePopulations);
    		node.getUtilizzazioneBatch().add(utilization);
    		node.getThroughputBatch().add(lambda);

    		
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /*public boolean verifyStats(int jobsServedPerBatch, int batchSize, Node node, MsqSum[] sum, double nodeArea, int indexFirstServer, int indexLastServer) {
    	//valutare la formula con MsqSum[] sum
    	System.out.println("Analisi del nodo con il primo server di indice: " + indexFirstServer);
    	System.out.println("Questo nodo ha servito un numero di job pari a " + jobsServedPerBatch);
    	System.out.println("Sum vale ");
    	for (int i = indexFirstServer; i <= indexLastServer; i++) {
			System.out.println(sum[i].served);
			System.out.println(sum[i].service);
		}
    	if ((jobsServedPerBatch != 0) && (jobsServedPerBatch == batchSize)) {
    		long numberOfJobsServed = 0;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			numberOfJobsServed += sum[i].served;
    		}
    		double queueArea = nodeArea;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			queueArea -= sum[i].service;
    		}
    		double serviceArea = 0.0;
    		double sumServed = 0.0;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			serviceArea += sum[i].service;
    			sumServed += sum[i].served;
    		}
    		int numServers = 0;
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			numServers++;
    		}
    		double sumOfMeans = 0.0;  // somma dei tempi medi dei server attivi
    		int activeServers = 0;     // contatore dei server che hanno servito almeno un job
    		for (int i = indexFirstServer; i <= indexLastServer; i++) {
    			if (sum[i].served > 0) {              // consideriamo solo server attivi
    				double serverMean = sum[i].service / sum[i].served;  // tempo medio di servizio del server i
    				sumOfMeans += serverMean;         // accumula la somma
    				activeServers++;                  // incrementa il numero di server attivi
    			}
    		}
    		if (activeServers == 0) {
    			throw new IllegalStateException("Nessun server ha servito job"); // equivalente a orElseThrow
    		}
    		double meanServiceTime = sumOfMeans / activeServers;  // media semplice

    	
    	    double tempoMedioDiRispostaBatch = nodeArea/numberOfJobsServed; 
    	    double tempoMedioInCodaBatch = queueArea/numberOfJobsServed;
    	    double tempoDiServizioBatch = serviceArea/sumServed;
    	    double popolazioneDelSistemaBatch = nodeArea/(node.getLastCompletionTime() - node.getCurrentStartTimeBatch());
    	    double popolazioneDellaCodaBatch = queueArea/(node.getLastCompletionTime() - node.getCurrentStartTimeBatch());
    	    double lambdaBatch = numberOfJobsServed/ (node.getLastArrivalTime() - node.getCurrentStartTimeBatch());
    	    double utilizzazioneBatch = (lambdaBatch*meanServiceTime)/numServers;
    	*/
    	/*if (jobsServedPerBatch == batchSize) {
    		node.getTempiMediDiRispostaBatch().add(tempoMedioDiRispostaBatch);
    		node.getTempiMediInCodaBatch().add(tempoMedioInCodaBatch);
    		node.getTempiDiServizioBatch().add(tempoDiServizioBatch);
    		node.getPopolazioneDelSistemaBatch().add(popolazioneDelSistemaBatch);
    		node.getPopolazioneDellaCodaBatch().add(popolazioneDellaCodaBatch);
    		node.getUtilizzazioneBatch().add(utilizzazioneBatch);
    		node.getThroughputBatch().add(lambdaBatch);
    		
    		return true;	
    	}*/
    	/*    return true;
        }
    	else {
    		System.out.println("PERO' questo nodo ancora non ha raggiunto un numero di job pari al numero di batch");
    		return false;
    	}
    }*/
	

}
