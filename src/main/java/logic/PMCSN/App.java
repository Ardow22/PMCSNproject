package logic.PMCSN;

import logic.PMCSN.controller.ComputationalModelController;

public class App 
{
    public static void main( String[] args ) {
        ComputationalModelController cmc = new ComputationalModelController();
        cmc.startAnalysis();
    }
}
