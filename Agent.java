// This class controls the agents.
// NOTE: Much of the spatial logic is contained in class Item, which this clas inherits from.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.awt.*;  
import java.util.Random;
import java.io.IOException;

public class Agent extends Item implements Comparable<Agent>{
    int score;
    Random R;
    World myworld;
    double rotation;
    double speed;
    double heading;
    int NBNEUR, WSIZE;
    double MUTATIONSIZE, MAXW;
    double[][] w;
    double[] neury, neurx;
    double dtdivtau , meannormw;
    
    Agent(World ww)
    {
        super(ww);
        myworld = ww;
        dtdivtau = 1.0 / myw.TAU;
        R = myworld.R;
        score=0;
        NBNEUR = myworld.NBNEUR; 
        WSIZE = myworld.WSIZE;
        MUTATIONSIZE = myworld.MUTATIONSIZE; MAXW = myworld.MAXW;
        neurx = new double[NBNEUR];
        neury = new double[NBNEUR];
        for (int ii=0; ii < NBNEUR; ii++){ 
            neury[ii] = 0.0; neurx[ii] = 0.0; 
        }
        w = new double[NBNEUR][NBNEUR];
        speed=0.0;
        rotation=0.0;
        heading = R.nextDouble() * 2 * Math.PI;
    }
    public int compareTo(Agent a)
    {
        return Integer.signum(getScore() - a.getScore());
    }
    public void randomizeNet(){
        meannormw = 0;
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++){
                w[ii][jj] = (2.0 * R.nextDouble() - 1.0) ;
                //w[ii][jj] = (2.0 * R.nextDouble() - 1.0) / Math.sqrt((double)NBNEUR);
                meannormw += Math.abs(w[ii][jj]);
            }
        meannormw /= (NBNEUR * NBNEUR);
    }
    public void resetNeurons() { 
        for (int n=0; n<NBNEUR; n++){
            neurx[n] = 0;
            neury[n] = 0;
        }
    }
    public void resetScore() { score = 0; } 
    public void increaseScore() { score ++; } 
    public void decreaseScore() { score --; } 
    public void copyFrom(Agent A){
        score = A.score;
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
                w[ii][jj] = A.w[ii][jj];
        meannormw = A.meannormw;
    }
    public void initialize()
    {
            randPos();
            resetNeurons();
            resetScore();
    }
    public int getScore() { return score /*- meannormw * 15.0 */ ; }
    public void runNetwork()
    {
        double tempx;
            int startcol;
        for (int row=0; row < NBNEUR; row++){
            tempx = 0;
            for (int col=0; col < NBNEUR; col++)
                if ((row > 2) || (col < 2 || col > 9)) // The actuators can't receive direct sensor input!
                //if ((row > 2) || (col > 9)) // The actuators can't receive direct sensor or actuator input!
                    tempx += w[row][col] * neury[col];
            neurx[row] += dtdivtau * (tempx - neurx[row]);
        }
        for (int row=0; row < NBNEUR; row++)
            neury[row] = Math.tanh(neurx[row]);
    }
    public void mutate()
    {
        meannormw = 0;
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
            {
                ///w[ii][jj] += MUTATIONSIZE * R.nextGaussian();
                //w[ii][jj] *= .99;
                if (R.nextDouble() < myworld.PROBAMUT){
                    double cauchy = Math.tan((R.nextDouble() - .5) * Math.PI);
                    w[ii][jj] += MUTATIONSIZE * cauchy;
                    w[ii][jj] *= .99;
                //    w[ii][jj] += myworld.MUTATIONSIZE * R.nextGaussian();
                //    w[ii][jj] *= .99;
                }
                if (w[ii][jj] > MAXW)
                    w[ii][jj] = MAXW;
                if (w[ii][jj] < -MAXW)
                    w[ii][jj] = -MAXW;
                meannormw += w[ii][jj];
            }
        meannormw /= (NBNEUR * NBNEUR);
    }

    // This function controls the agent's behavior.
    public void update()
    {
        double dist, angle;
        int sensorR, sensorL;
        neury[6] = 2.0 * R.nextDouble() - 1.0;
        neury[7] = 1.0;
        neury[8] = 0.0; neury[9] = 0.0; // 2.0 * (myworld.POISONFIRSTHALF - .5);
        //neury[8] = (double)score / 200.0; neury[9] = (double)score / 20.0;
        neury[2] = 0.0; neury[3] = 0.0; neury[4] = 0.0; neury[5] = 0.0;
        // Check where the food bits (and poison bits!) are, whether we have eaten one, and fill
        // the sensors with appropriate values:
        for (int n=0; n < myworld.FOODSIZE; n++)
        {
            // The sensors only detect 1st half vs. 2nd half, independently of which is poison. The agent must figure that out!
            // It can only do this by using a sensor for score / pain vs. pleasure
            // A sensor for score only could work *in theory*, but that sounds like a tall order...
            // A "pain" vs "pleasure" sensor could be more helpful.
            //if ( (n < myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 1) || (n >= myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 0))  { sensorL = 2; sensorR = 3; } else{ sensorL = 4; sensorR = 5;}
            if ( n < myworld.FOODSIZE /2)  { sensorL = 2; sensorR = 3; } else{ sensorL = 4; sensorR = 5;}
            dist = getDistanceFrom(myworld.food[n]); // getDistanceFrom and getAngleFrom are from ancestor class Item
            if (dist < myworld.EATRADIUS)  // Eaten!
            {
                if ( (n < myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 1) || (n >= myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 0)) {
                    increaseScore(); 
                    neury[8] = 10.0;
                }
                else {
                    decreaseScore();
                    neury[9] = -10.0;
                }
                myworld.food[n].randPos(); 
            }
            else
            {
                angle = getAngleFrom(myworld.food[n]);
                if ((angle-heading < 3.0) && (angle-heading > 0))
                    neury[sensorL] += 1.0 / (1.0 + .1 * dist);
                if ((angle-heading > -3.0) && (angle-heading < 0))
                    neury[sensorR] += 1.0 / (1.0 + .1 * dist);
            }

        }
        runNetwork(); // Runs the neural network.
        // Determine motion based on neural network outputs:
        speed = myworld.AGENTSPEED * (1.0 + neury[0]) / 2.0;
        rotation = myworld.AGENTANGULARSPEED * neury[1];
        heading += rotation;
        if (heading < 0) heading += 2 * Math.PI;
        if (heading > 2 * Math.PI) heading -= 2 * Math.PI;
        x += speed * Math.cos(heading);
        y -= speed * Math.sin(heading);
        if (y > myworld.WSIZE)
            y -= myworld.WSIZE;
        if (y < 0)
            y += myworld.WSIZE;
        if (x > myworld.WSIZE)
            x -= myworld.WSIZE;
        if (x < 0)
            x += myworld.WSIZE;
    }
    
    public void readAgent(String fname)
    {   
        try{
            System.out.println("Reading from file "+fname);
            BufferedReader in
                = new BufferedReader(new FileReader(fname));

            for (int row=0; row < NBNEUR; row++)
            {   
                String strs[] = in.readLine().split(" ");
                if (strs.length != NBNEUR)
                    throw new RuntimeException("Data file has wrong number of neurons! (strs.length is "+strs.length+", strs[0] is "+strs[0]+", row "+row+")");
                for (int col=0; col < NBNEUR; col++)
                    this.w[row][col] = Double.parseDouble(strs[col]);
            }
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't read agent from file "+fname); System.exit(0);            
        }
    }
    
    public void saveAgent(String fname)
    {   
        try{
            PrintWriter writer = new PrintWriter(fname);
            for (int row=0; row < NBNEUR; row++)
            {   
                for (int col=0; col < NBNEUR; col++)
                    writer.print(Double.toString(this.w[row][col])+" "); 
                writer.print("\n");
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't save agent to file "+fname); System.exit(0);            
        }

    }
    public void draw(Graphics G){
        G.setColor(Color.red);
            G.fillOval((int)(x)-3, (int)y-3, 7, 7);
            G.fillOval((int)(x + 6 * Math.cos(heading))-1, 
                       (int)(y - 6 * Math.sin(heading))-1, // minus to preserve counterclockwise (trigonometric) heading when y grows downwards..
                        2,2);
    }


}
