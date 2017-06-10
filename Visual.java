import java.awt.*;  
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;  
import java.util.Random;

// The canvas on which we will draw the world
class MyNewCanvas extends Canvas{
    World myWorld;
    public MyNewCanvas(World tt) {  
        myWorld = tt;
        setBackground (Color.gray);  
        setSize(tt.WSIZE, tt.WSIZE);  
    }  
    public void paint(Graphics G){
        // The green oval is there for no reason at all.
        G.setColor(Color.green); G.fillOval(10, 50, 20, 10);
        for (int n=0; n < myWorld.food.length; n++){

            if ( (n < myWorld.FOODSIZE /2  && myWorld.POISONFIRSTHALF == 1) || (n >= myWorld.FOODSIZE /2  && myWorld.POISONFIRSTHALF == 0) ) 
                G.setColor(Color.white);
            else
                G.setColor(Color.black);
            G.fillOval((int)(myWorld.food[n].x), (int)myWorld.food[n].y, 4, 4);
        }
        myWorld.pop.draw(G); // Population of agents can draw itself.
    }
}

public class Visual extends Frame implements ActionListener, Runnable {
    World myWorld;
    TextField tf1;
    Label scorelabel;
    Button b1,b2, b3; 
    Random R;
    PrintWriter outputfilewriter;
    MyNewCanvas cnv;
    protected Thread thrd;
    Population pop;
    FoodBit[] food;
    int POISONFIRSTHALF = 0;
    int delay=50;
    String FILENAME;

    Visual(World ww, String fname)
    {
        FILENAME = fname;
        myWorld = ww;
        int numarg = 0;
        food = new FoodBit[myWorld.FOODSIZE]; for (int nn=0; nn< food.length; nn++) food[nn] = new FoodBit(ww);
        pop = new Population(ww);
        tf1=new TextField();  
        tf1.setText(Integer.toString(delay));
        b1=new Button("+"); b2=new Button("-"); b3=new Button("Switch Poison"); 
        scorelabel.setText("Score: 0");
        b1.addActionListener(this); b2.addActionListener(this); b3.addActionListener(this); 
        add(tf1);add(b1);add(b2); add(scorelabel); add(b3); 
        cnv = new MyNewCanvas(this.myWorld);
        add(cnv);
        addWindowListener ( new WindowAdapter() {
            public void windowClosing ( WindowEvent evt ) {
                outputfilewriter.close();
                System.exit ( 0 );
            }
        } );
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
        }else if(e.getSource()==b3){  
            POISONFIRSTHALF = 1 - POISONFIRSTHALF;
        }            
        tf1.setText(Integer.toString(delay));
    }  
    public static void main(String[] args) {  
        String[] ss;
        World ww = new World(ss);
        if (args.length != 1){
            throw new RuntimeException("Visual takes exactly one argument - the filename of the population to load.");
        }
        Visual vv = new Visual(ww, args[0]);  
    }  

    
    public void run()
    {
        pop.readPop(FILENAME);
        while (true)
        {
            pop.initialize();
            POISONFIRSTHALF = R.nextInt(2);
            // Evaluation :
            for (int numstep=0; numstep < NBSTEPSPERGEN; numstep++)
            {
                if (numstep == (int)(NBSTEPSPERGEN / 2))
                    POISONFIRSTHALF = 1 - POISONFIRSTHALF;
                for (int n=0; n < food.length; n++)
                    food[n].update();
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
        }
        bestscore = pop.getTotalScore();
        //System.out.println("Gen "+numgen+": "+bestscore+" "+pops.get(1).getTotalScore());
        System.out.println(bestscore);
    }

}  


