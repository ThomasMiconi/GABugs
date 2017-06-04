import java.awt.*;  
import java.awt.event.*;  
import java.util.Random;
class MyCanvas extends Canvas{
    World myWorld;
    public MyCanvas(World tt) {  
        myWorld = tt;
        setBackground (Color.blue);  
        setSize(tt.WSIZE, tt.WSIZE);  
    }  
    public void paint(Graphics G){
        G.setColor(Color.green);
        G.fillOval(10, 50, 10, 10);
        G.setColor(Color.black);
        for (int n=0; n < myWorld.food.length; n++)
            G.fillOval((int)(myWorld.food[n].x), (int)myWorld.food[n].y, 4, 4);
        G.setColor(Color.red);
        myWorld.pop.draw(G);
    }
}

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
           EATRADIUS = 3.0,
           EATRADIUSSQ = EATRADIUS * EATRADIUS,
           //MUTATIONSCALEFACTOR =  .1,
           PROBAMUT = .05,
           MAXW = 10.0;
    FoodBit[] food;
    Population pop, bestpop;
    int score, bestscore;
    int numgen;
    protected Thread thrd;
    World(){ 
        R = new Random(0);
        score = 0; bestscore=0;
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

    
    public void run()
    {
        numgen = 0;
        pop.randomizeNets();
        bestscore = -1;
        while (true)
        {
            if (numgen > 0)
                pop.copyFrom(bestpop);
            pop.mutate();
            pop.initialize();
            for (int numstep=0; numstep < NBSTEPSPERGEN; numstep++)
            {
                for (int n=0; n < food.length; n++)
                    food[n].update();
                pop.update();

                try{ Thread.sleep(delay); }
                catch ( InterruptedException e )
                {
                    System.out.println ( "Exception: " + e.getMessage() );
                }        
                cnv.repaint();
                scorelabel.setText("Score: "+pop.getTotalScore());
            }
            score = pop.getTotalScore();
            //Sys/tem.out.println("Gen done, score = "+score);
            System.out.println(score);
            if (score > bestscore)
            {
                //System.out.println("Best score!");
                bestpop.copyFrom(pop);
                bestscore = score;
            }
            numgen ++;
        }
    }

}  
