import java.awt.*;  
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;  
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

// The World class is the overall controller and also implements the View (w/ MyCanvas).
// While it defines the overall algorithm, much of the actual logic occurs in
// classes Agent, Item, Population and FoodItem.
public class World extends Frame {
    TextField tf1;
    Button b1,b2, b3; 
    Checkbox cbOnly0;
    String FILESUFFIX;
    String FILENAME = "";
    Random R;
    PrintWriter outputfilewriter;
    MyFrame mf;
    int delay=0, FOODSIZE = 10, 
        POPSIZE = 1, 
        WSIZE = 250,  NBSTEPSPERGEN = 10000,
           NBPOPS = 100,
           VISUAL = 1,
           NBBEST = 20, 
           SEED = 4,
           POISONFIRSTHALF = 1, // Half of the foodbits are poison; is it 1st or 2nd half?
        NBNEUR = 25; // 20;
    double FOODSPEED = 1.0, 
           AGENTSPEED = 5.0, 
           AGENTANGULARSPEED = .3, 
           EATRADIUS = 10.0,
           PROBAMUT = .05,
           MUTATIONSIZE= 1.0,
           TAU = 5.0,
           MAXW = 10.0;
    //boolean ONLYSHOW0 = false;
    FoodBit[] food;
    boolean VISUALONLY = false;
    Population pop, bestpop;
    ArrayList<Population>  pops;
    int bestscore;
    int numgen;
    protected Thread thrd;
    World(String args[]){ 
        int numarg = 0;
        if (args.length % 2 != 0) { throw new RuntimeException("Each argument must be provided with its value"); }
        while (numarg < args.length) {
            if (args[numarg].equals( "FILENAME")) { VISUALONLY = true; FILENAME  = args[numarg+1]; }
            if (args[numarg].equals( "POPSIZE")) POPSIZE = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "VISUAL")) VISUAL = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBBEST")) NBBEST = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBSTEPSPERGEN")) NBSTEPSPERGEN = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBPOPS")) NBPOPS = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "SEED")) SEED = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBNEUR")) NBNEUR = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "TAU")) TAU  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MAXW")) MAXW  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "PROBAMUT")) PROBAMUT  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MUTATIONSIZE")) MUTATIONSIZE  = Double.parseDouble(args[numarg+1]);
            numarg += 2;
        }
        FILESUFFIX = "_fullsens_nodirectio_noglobalmut_NBPOPS"+NBPOPS+"_MUTATIONSIZE"+MUTATIONSIZE+"_NBNEUR"+NBNEUR+"_MAXW"+MAXW+"_SEED"+SEED;
        if (VISUALONLY == false) {
            try { outputfilewriter = new PrintWriter("results"+FILESUFFIX+".txt"); } catch(IOException e) {}
        }
        else
            VISUAL = 1;
        // Initializations and graphics setup...
        R = new Random(SEED);
        pop = new Population(this);
        pops = new ArrayList<Population>();
        for (int i=0; i<NBPOPS; i++) 
            pops.add(new Population(this));
        bestpop = new Population(this);
        food = new FoodBit[FOODSIZE]; for (int nn=0; nn< food.length; nn++) food[nn] = new FoodBit(this);
        if (VISUAL != 0)
            mf = new MyFrame(this);
        /*add(tf1);add(b1);add(b2); add(scorelabel); add(b3); // add(cbOnly0);  
        cnv = new MyCanvas(this);
        add(cnv);
        addWindowListener ( new WindowAdapter() {
            public void windowClosing ( WindowEvent evt ) {
                outputfilewriter.close();
                System.exit ( 0 );
            }
        } );
        setLayout(new FlowLayout());  
        pack();
        setVisible(true);  */
    }         

    public static void main(String[] args) {  
        World tf = new World(args);  
        tf.run();
        
    }  

    
    // The actual evolutionary algorithm!
    public void run()
    {
        numgen = 0; boolean currentlyOnlyShowing0=false;
        for (int i=0; i<NBPOPS; i++) pops.get(i).randomizeNets();
        //while (numgen < 10)
        while (true)
        {
            // This re-evaluates the champion populations, even though we already know their score! OTOH, score evaluation is quite noisy...
            for (int numpop=0; numpop < NBPOPS; numpop++)
            {
                pop.copyFrom(pops.get(numpop));
                if (VISUALONLY) pop.readPop(FILENAME);
                pop.initialize();
                POISONFIRSTHALF = R.nextInt(2);
                // Evaluation :
                for (int numstep=0; numstep < NBSTEPSPERGEN; numstep++)
                {
                    if (numstep == (int)(NBSTEPSPERGEN / 2))
                        POISONFIRSTHALF = 1 - POISONFIRSTHALF;
                    for (int n=0; n < food.length; n++)
                        food[n].update();
                    pop.update(); // Takes care of sensors, network update, score update, motion, etc.

                    // We need a delay between refreshes if we want to see what's going on...
                    // But we can set it to 0 (with the buttons) if we just want
                    // the algorithm to proceed fast.
                    try{ Thread.sleep(delay); }
                    catch ( InterruptedException e )
                    {
                        System.out.println ( "Exception: " + e.getMessage() );
                    }        
                    if (VISUAL > 0){
                        mf.cnv.repaint();
                        mf.scorelabel.setText("Score: "+pop.getTotalScore());
                    }
                }
                pops.get(numpop).copyFrom(pop); // Mostly to get back the total score.
            }
            Collections.sort(pops); // This will sort pops by ascending order of the scores, because Population implements Comparable.
            Collections.reverse(pops); // We want descending order.
            bestscore = pops.get(0).getTotalScore();
            //System.out.println("Gen "+numgen+": "+bestscore+" "+pops.get(1).getTotalScore());
            System.out.println(bestscore);
            if (VISUALONLY == false){
                outputfilewriter.println(bestscore); outputfilewriter.flush();
                pops.get(0).savePop("bestpop"+FILESUFFIX+".txt");
            }
            for (int n=NBBEST; n<NBPOPS; n++)
            {
                /*int z1 = R.nextInt(NBBEST);
                int z2 = R.nextInt(NBBEST);
                if (pops.get(z2).getTotalScore() > pops.get(z1).getTotalScore())
                    z1 = z2;
                pops.get(n).copyFrom(pops.get(z1));
                pops.get(n).mutate();*/
                pops.get(n).copyFrom(pops.get(R.nextInt(NBBEST)));
                pops.get(n).mutate();
            }
            numgen ++;
        }
        //System.exit(0);
    }

}  
