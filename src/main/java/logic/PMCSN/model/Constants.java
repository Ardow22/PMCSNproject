package logic.PMCSN.model;

public class Constants {

    // ---- VIP USER PROB ----
    public static final  double q = 0.076438;

    // ---- ABANDON PROB ----
    //probabilità di abandonare il sistema dopo i controlli arancioni
    public static final double P1 = 0.08;
    
    // probability of abandon after vip single queue
    public static final double P6 = 0.002; //First probability combined with second probability


    // ---- TIME SLOT PERCENTAGE ----
    public static final double [] PERCENTAGE = { 0.20, 0.70, 0.10};

    // ---- ARRIVAL RATES [req/sec]----
    // first time window rate
    public static final double LAMBDA1 = 0.24044;

    // second time window rate
    public static final double LAMBDA2 = 0.84154;

    // third time window rate
    public static final double LAMBDA3 = 0.12022;

    // ---- SERVICE RATES  [sec] ----
    // controlli coda gialla
    public static final double YQ_SR = 7;

    // infopoint
    public static final double INFOQ_SR = 20;

    // controlli coda arancione
    public static final double ORQ_SR = 5;

    // VIP single queue service time
    public static final double VIP_MEAN_SERVICE_TIME = 27;

}
