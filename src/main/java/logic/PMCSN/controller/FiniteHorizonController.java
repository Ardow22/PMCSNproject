package logic.PMCSN.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import logic.PMCSN.libraries.Rngs;
import logic.PMCSN.model.ClubNode;
import logic.PMCSN.model.LoginNode;
import logic.PMCSN.model.Node;
import logic.PMCSN.model.StagioniNode;
import logic.PMCSN.model.TransientStats;
import logic.PMCSN.model.UltimateTeamNode;

public class FiniteHorizonController {
	
	public void runAnalysis() {
		long[] seeds = new long[1024];
		seeds[0] = 123456789;
		Rngs r = new Rngs();
		
		for (int i = 0; i < 150; i++) {
			LoginNode ln = new LoginNode();
			UltimateTeamNode utN = new UltimateTeamNode();
			ClubNode cn = new ClubNode();
			StagioniNode sn = new StagioniNode();
			seeds[i+1] = finiteHorizonSimulation(seeds[i], r, ln, utN, cn, sn);
			//writeCsvLogin(ts, seeds[i]);
			//writeCsvUT(ts, seed);
			//writeCsvClub(ts, seed);
			//writeCsvStagioni(ts, seed);
			//write_file(ts, seed) delle statistiche calcolate nella singola run
			//appendStats salvo tutto: statistiche run singola, statistiche check point
		}
	}
	
	private long finiteHorizonSimulation(long seed, Rngs r, LoginNode loginNode, UltimateTeamNode utNode, ClubNode clubNode, StagioniNode stagioniNode) {
		r.plantSeeds(seed);
		System.out.println("Il seme è: " + seed);
		
		r.selectStream(255);
		return r.getSeed();
	}
	
	private void writeCsvLogin(TransientStats ts, long seed) {
		String filepath = "loginTransiente.csv";
		File file = new File(filepath);
		boolean fileExists = file.exists();
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			if (!fileExists) {
				writer.write("seed, valore_1, valore_2, valore_3");
				writer.newLine();
			}
			
			StringBuilder row = new StringBuilder();
			row.append(seed);
			for (double d: ts.getTransientStatsLogin()) {
				row.append(",").append(d);
			}
			
			writer.write(row.toString());
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
