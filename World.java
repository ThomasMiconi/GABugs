import java.awt.*;  
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;  
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

// The World class is the overall controller.
// While it defines the overall algorithm, much of the actual logic occurs in
// classes Agent, Item, Population and FoodItem.
public class World extends Frame {
    // First, various definitions....
    String FILESUFFIX;
    String FILENAME = "";
    Random R;
    PrintWriter outputfilewriter;
    MyFrame mf; // MyFrame defines the graphical View/Controller.
    int delay=0, 
        FOODSIZE = 10, // Number of food items

        // NOTE: What we are evolving here is not single agents, but *populations*. In the current version, each population has size 1, or if it has more than one agent, all agents are clones and share a common genome. 
        // For this task it is unnecessary, but this allows us to evolve collective behaviors in the future

        POPSIZE = 1,   // Nuber of agents used in each population (all sharing the same unique genome, in the current version). You can set this to a higher number, but for now we evolve populations of size 1.
        WSIZE = 250,  
        NBSTEPSPEREVAL = 10000,  // Duration of an evaluation
        NBPOPS = 100,           // Number of 'populations' (each coresponding to one genome) in the algorithm
        NBBEST = 20,            // How many of the best populations are kept from each generation to the next
        SEED = 0, // Random seed
        POISONFIRSTHALF = 1, // Half of the foodbits are poison; is it 1st or 2nd half?
        NBNEUR = 25; // Number of neurons
    double FOODSPEED = 1.0,  // The speed of the food/poison items.
           AGENTSPEED = 5.0,  // MAximum agent speed
           AGENTANGULARSPEED = .3,  // Maximum agent angular speed
           EATRADIUS = 10.0,  // How close must we be to be deemed 'eaten'?
           PROBAMUT = .05,      // Probability of mutation for each gene
           MUTATIONSIZE= 1.0,   // Size parameter of the Cauchy distribution used for the mutations
           TAU = 5.0,       // Time constant of the recurrent neural network
           MAXW = 10.0;     // Maximum weight 
    FoodBit[] food;
    int VISUAL = 0;  // Using graphics or not?
    Population pop, bestpop;  // 'pop' is the population being currently evaluated. 
    ArrayList<Population>  pops;  // The list of populations on which the genetic algorithm is performed
    int bestscore;
    int bestscoreever;
    int numgen;
    protected Thread thrd;
    World(String args[]){ 
        // Reading the command line arguments...
        int numarg = 0;
        if (args.length % 2 != 0) { throw new RuntimeException("Each argument must be provided with its value"); }
        while (numarg < args.length) {
            if (args[numarg].equals( "FILENAME")) { VISUAL = 1 ; FILENAME  = args[numarg+1]; delay=50; }
            if (args[numarg].equals( "POPSIZE")) POPSIZE = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBBEST")) NBBEST = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "VISUAL")) { VISUAL  = Integer.parseInt(args[numarg+1]); if ((VISUAL !=0) && (VISUAL != 1)) throw new RuntimeException("VISUAL must be 0 or 1!");}
            if (args[numarg].equals( "NBSTEPSPEREVAL")) NBSTEPSPEREVAL = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBPOPS")) NBPOPS = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "SEED")) SEED = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBNEUR")) NBNEUR = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "TAU")) TAU  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MAXW")) MAXW  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "PROBAMUT")) PROBAMUT  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MUTATIONSIZE")) MUTATIONSIZE  = Double.parseDouble(args[numarg+1]);
            numarg += 2;
        }
        // suffix for the output files (results and bestpop).
        FILESUFFIX = "_fullsens_nodirectio_noglobalmut_cauchy_NBPOPS"+NBPOPS+"_NBBEST"+NBBEST+"_MUTATIONSIZE"+MUTATIONSIZE+"_NBNEUR"+NBNEUR+"_MAXW"+MAXW+"_SEED"+SEED;
        if (VISUAL == 0) {
            try { outputfilewriter = new PrintWriter("results"+FILESUFFIX+".txt"); } catch(IOException e) {}
        }
        // Initializations and graphics setup...
        R = new Random(SEED);
        pop = new Population(this);
        pops = new ArrayList<Population>();
        for (int i=0; i<NBPOPS; i++) 
            pops.add(new Population(this));
        food = new FoodBit[FOODSIZE]; for (int nn=0; nn< food.length; nn++) food[nn] = new FoodBit(this);
        if (VISUAL == 1)
            mf = new MyFrame(this);
    }         

    public static void main(String[] args) {  
        World tf = new World(args);  
        tf.run();

    }  



    // The actual evolutionary algorithm!
    public void run()
    {
        numgen = 0; bestscoreever = 0;
        for (int i=0; i<NBPOPS; i++) pops.get(i).randomizeNets();
        //while (numgen < 10)
        while (true)
        {
            // Note that we re-evaluate the champion populations at each generation, even though we already know their score! OTOH, score evaluation is quite noisy, so it's probably worth it.
            for (int numpop=0; numpop < NBPOPS; numpop++)
            {
                // Each population is evaluated in turn by putting it into the 'active' pop.
                pop.copyFrom(pops.get(numpop));
                // If we are currently visualizing a population saved in a 'bestpop' file (provided as command line argument), we only ever show this one.
                if (FILENAME.length() > 0) 
                    pop.readPop(FILENAME);
                pop.initialize();
                // Which is food, which is poison? Randomly set.
                POISONFIRSTHALF = R.nextInt(2);

                // Evaluation :
                for (int numstep=0; numstep < NBSTEPSPEREVAL; numstep++)
                {
                    // Food and poison switch at mid-trial !
                    if (numstep == (int)(NBSTEPSPEREVAL / 2))
                        POISONFIRSTHALF = 1 - POISONFIRSTHALF;
                    for (int n=0; n < food.length; n++)
                        food[n].update();
                    pop.update(); // Takes care of sensors, network update, score update, motion, etc.

                    // If using graphics, 
                    // we need a delay between refreshes if we want to see what's going on...
                    // But we can set it to 0 (with the buttons) if we just want
                    // the algorithm to proceed fast.
                    try{ Thread.sleep(delay); }
                    catch ( InterruptedException e )   {
                        System.out.println ( "Exception: " + e.getMessage() );
                    }        
                    if (VISUAL > 0){
                        mf.cnv.repaint();
                        mf.scorelabel.setText("Score: "+pop.getTotalScore());
                    }
                }
                pops.get(numpop).copyFrom(pop); // Mostly to get back the total score.
                if (FILENAME.length() > 0) System.out.println(pop.getTotalScore());
            }
            Collections.sort(pops); // This will sort pops by ascending order of the scores, because Population implements Comparable.
            Collections.reverse(pops); // We want descending order.
            bestscore = pops.get(0).getTotalScore();
            //System.out.println("Gen "+numgen+": "+bestscore+" "+pops.get(1).getTotalScore());
            System.out.println(bestscore);
            if (FILENAME.length() == 0){
                outputfilewriter.println(bestscore); outputfilewriter.flush();
                pops.get(0).savePop("bestpop"+FILESUFFIX+".txt");
                if (pops.get(0).getTotalScore() > bestscoreever)
                {
                    bestscoreever = pops.get(0).getTotalScore();
                    pops.get(0).savePop("besteverpop"+FILESUFFIX+".txt");
                }
            }

            for (int n=NBBEST; n<NBPOPS; n++)
            {
                pops.get(n).copyFrom(pops.get(R.nextInt(NBBEST)));
                pops.get(n).mutate();
            }
            numgen ++;
        }
        //System.exit(0);
    }

}  
