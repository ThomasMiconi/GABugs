import java.awt.*;  
import java.awt.event.*;  
import java.util.Random;

// The canvas on which we will draw the world
class MyCanvas extends Canvas{
    World myWorld;
    public MyCanvas(World tt) {  
        myWorld = tt;
        setBackground (Color.blue);  
        setSize(tt.WSIZE, tt.WSIZE);  
    }  
    public void paint(Graphics G){
        // This green oval is there for no reason at all.
        G.setColor(Color.green); G.fillOval(10, 50, 20, 10);
        G.setColor(Color.black);
        for (int n=0; n < myWorld.food.length; n++)
            G.fillOval((int)(myWorld.food[n].x), (int)myWorld.food[n].y, 4, 4);
        myWorld.pop.draw(G); // Population of agents can draw itself.
    }
}

// The World class is the overall controller and also implements the View (w/ MyCanvas).
// While it defines the overall algorithm, much of the actual logic occurs in
// classes Agent and FoodItem.
public class World extends Frame implements ActionListener, Runnable{
    TextField tf1;
    Label scorelabel;
    Button b1,b2, b3; 
    Random R;
    MyCanvas cnv;
    int delay, FOODSIZE = 10, 
        POPSIZE = 5, 
        WSIZE = 350,  NBSTEPSPERGEN = 10000,
        NBNEUR = 20;
    double FOODSPEED = 1.0, 
           AGENTSPEED = 5.0, 
           AGENTANGULARSPEED = .25, 
           EATRADIUS = 10.0,
           PROBAMUT = .05,
           MAXW = 10.0;
    FoodBit[] food;
    Population pop, bestpop;
    int bestscore;
    int numgen;
    protected Thread thrd;
    World(){ 
        // Initializations and graphics setup...
        R = new Random(0);
        pop = new Population(this);
        bestpop = new Population(this);
        scorelabel = new Label(); scorelabel.setPreferredSize(new Dimension(100, 25));
        delay = 100;
        food = new FoodBit[FOODSIZE]; for (int nn=0; nn< food.length; nn++) food[nn] = new FoodBit(this);
        tf1=new TextField();  
        tf1.setText(Integer.toString(delay));
        b1=new Button("+"); b2=new Button("-"); b3=new Button("O");
        scorelabel.setText("Score: "+score);
        b1.addActionListener(this); b2.addActionListener(this); b3.addActionListener(this); 
        add(tf1);add(b1);add(b2); add(scorelabel); // add(b3);
        cnv = new MyCanvas(this);
        add(cnv);
        addWindowListener ( new WindowAdapter() {
            public void windowClosing ( WindowEvent evt ) {
                System.exit ( 0 );
            }
        } );
        //setSize(1000,1000);  
        setLayout(new FlowLayout());  
        pack();
        setVisible(true);  
        thrd = new Thread(this);
        thrd.start();
    }         

    public void actionPerformed(ActionEvent e) {
        // The buttons control the waiting delay between refreshes.
        if(e.getSource()==b1){  
            delay+=50;  
        }else if(e.getSource()==b2){  
            if (delay >= 50)
                delay-=50;  
        }
        tf1.setText(Integer.toString(delay));
    }  
    public static void main(String[] args) {  
        World tf = new World();  
    }  

    
    // The actual evolutionary algorithm!
    public void run()
    {
        numgen = 0;
        pop.randomizeNets();
        bestscore = -1;
        while (true)
        {
            // Every new candidate is a mutated copy of the current best-ever population.
            if (numgen > 0)
                pop.copyFrom(bestpop);  
            pop.mutate();
            pop.initialize();
            // Evaluation :
            for (int numstep=0; numstep < NBSTEPSPERGEN; numstep++)
            {
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
                cnv.repaint();
                scorelabel.setText("Score: "+pop.getTotalScore());
            }
            score = pop.getTotalScore();
            System.out.println(score);
            if (score > bestscore)
            {
                // We have a new champion!
                bestpop.copyFrom(pop);
                bestscore = score;
            }
            numgen ++;
        }
    }

}  
