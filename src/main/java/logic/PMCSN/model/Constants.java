package logic.PMCSN.model;

public class Constants {

    // ---- ABANDON PROB ----
    //probabilità di abbandonare il sistema dopo il login
    public static final double not_P1 = 0.2;
    
    //probabilità di abbandonare il sistema dopo i controlli Ultimate Team
    public static final double not_P5 = 0.03;
    
    //probabilità di abbandonare il sistema dopo i controlli Stagioni
    public static final double not_P6 = 0.01;
    
    //probabilità di abbandonare il sistema dopo i controlli Stagioni
    public static final double not_P7 = 0.02;
    


    // ---- TIME SLOT PERCENTAGE ----
    public static final double [] PERCENTAGE = { 0.20, 0.70, 0.10};

    // ---- ARRIVAL RATES [req/sec]----
    // first time window rate
    public static final double LAMBDA1 = 0.24044;

    // second time window rate
    public static final double LAMBDA2 = 0.84154;

    // third time window rate
    public static final double LAMBDA3 = 0.12022;
}
