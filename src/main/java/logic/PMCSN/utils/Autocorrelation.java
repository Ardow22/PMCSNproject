package logic.PMCSN.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class Autocorrelation {
	static int K = 50;            // lag massimo
    static int SIZE = K + 1;       // dimensione array circolare
	
	public void startCalculate() throws IOException {
		// Lettura tutti i file .dat nella cartella
        File folder = new File("batch_reports");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                calcolaAcs(file);
            }
        } else {
            System.out.println("Nessun file .dat trovato nella cartella batch_reports");
        }
	}
	
	private void calcolaAcs(File file) throws IOException {
		int i = 0;                     // indice dati
        int j;
        int p = 0;                     // testa array circolare
        double x;
        double sum = 0.0;
        long n;
        double mean;
        double hold[] = new double[SIZE];
        double cosum[] = new double[SIZE];

        for (j = 0; j < SIZE; j++) cosum[j] = 0.0;

        BufferedReader ReadThis = new BufferedReader(new FileReader(file));
        String line;

        try {
            // Lettura dei primi K+1 dati
            while (i < SIZE && (line = ReadThis.readLine()) != null) {
                x = Double.parseDouble(line);
                sum += x;
                hold[i] = x;
                i++;
            }

            // Lettura del resto dei dati
            while ((line = ReadThis.readLine()) != null) {
                for (j = 0; j < SIZE; j++)
                    cosum[j] += hold[p] * hold[(p + j) % SIZE];

                x = Double.parseDouble(line);
                sum += x;
                hold[p] = x;
                p = (p + 1) % SIZE;
                i++;
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Formato non valido in " + file.getName() + ": " + nfe);
        }

        n = i;

        // Svuotamento array circolare
        while (i < n + SIZE) {
            for (j = 0; j < SIZE; j++)
                cosum[j] += hold[p] * hold[(p + j) % SIZE];
            hold[p] = 0.0;
            p = (p + 1) % SIZE;
            i++;
        }

        // Calcolo media e autocovarianze
        mean = sum / n;
        for (j = 0; j <= K; j++)
            cosum[j] = (cosum[j] / (n - j)) - (mean * mean);

        DecimalFormat f = new DecimalFormat("###0.00");
        DecimalFormat g = new DecimalFormat("###0.000");

        /*Stampa risultati
        System.out.println("\nFile: " + file.getName());
        System.out.println("Per " + n + " dati, media = " + f.format(mean) +
                ", stdev = " + f.format(Math.sqrt(cosum[0])) + "\n");
        System.out.println("  j (lag)   r[j] (autocorrelazione)");
        for (j = 1; j < SIZE; j++)
            System.out.println("  " + j + "          " + g.format(cosum[j] / cosum[0]));*/
        
     // Stampa solo lag j = 1
        System.out.println("\nFile: " + file.getName());
        System.out.println("Per " + n + " dati, media = " + f.format(mean) +
                ", stdev = " + f.format(Math.sqrt(cosum[0])) + "\n");

        int jj = 1; // lag desiderato
        System.out.println("Lag j = " + jj + ", autocorrelazione r[j] = " +
                g.format(cosum[jj] / cosum[0]));

        ReadThis.close();
		
	}

}
