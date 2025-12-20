package logic.PMCSN;

import java.util.Scanner;

import logic.PMCSN.controller.ComputationalModelController;
import logic.PMCSN.controller.VerificaController;

public class App 
{
    public static void main( String[] args ) {
        //ComputationalModelController cmc = new ComputationalModelController();
        Scanner input = new Scanner(System.in);
        System.out.println("Benvenuto nel simulatore PMCSN!");
        System.out.println("Puoi scegliere tra le seguenti opzioni: ");
        System.out.println("1 - Verifica");
        System.out.println("2 - Simulazione ad orizzonte infinito");
        System.out.println("3 - Simulazione ad orizzonte finito");
        
        System.out.println("Digita un numero: ");
        String choice = input.nextLine();
        
        switch(choice) {
        case "1":
        	System.out.println("Hai scelto la verifica");
        	break;
        
        case "2":
        	System.out.println("Hai scelto la simulazione ad orizzonte infinito");
        	VerificaController vc = new VerificaController();
        	vc.startAnalysis();
        	break;
        
        case "3":
        	System.out.println("Hai scelto la simulazione ad orizzonte finito");
        	break;
        	
        default:
        	System.out.println("Non hai scelto nulla, chiusura del programma");
        	System.exit(0);
        }
        //cmc.startAnalysis();
    }
}
