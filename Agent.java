// This class controls the agents.
// NOTE: Much of the spatial logic is inherited from class Item!

import java.util.Random;
public class Agent extends Item{
    int score;
    int num;
    Random R;
    double rotation;
    double speed;
    double heading;
    int NBNEUR, WSIZE;
    World myworld;
    double[][] w;
    double[] neury, neurx;
    double dtdivtau ;;
    Agent(World myw, int myn)
    {
        super(myw);
        myworld = myw;
        dtdivtau = 1.0 / myw.TAU;
        num = myn;
        R = myworld.R;
        score=0;
        NBNEUR = myworld.NBNEUR; 
        WSIZE = myworld.WSIZE;
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
    public void randomizeNet(){
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
                w[ii][jj] = (2.0 * R.nextDouble() - 1.0) ;
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
    }
    public void initialize()
    {
            randPos();
            resetNeurons();
            resetScore();
    }
    public void runNetwork()
    {
        double tempx;
        for (int row=0; row < NBNEUR; row++){
            tempx = 0;
            for (int col=0; col < NBNEUR; col++)
                tempx += w[row][col] * neury[col];
            neurx[row] += dtdivtau * (tempx - neurx[row]);
            neury[row] = Math.tanh(neurx[row]);
        }
    }
    public void mutate()
    {
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
            {
                if (R.nextDouble() < myworld.PROBAMUT){
                    w[ii][jj] += R.nextGaussian();
                    w[ii][jj] *= .99;
                    //w[ii][jj] = R.nextGaussian();
                    //double cauchy = Math.tan((R.nextDouble() - .5) * Math.PI);
                    //w[ii][jj] = cauchy;
                }
                if (w[ii][jj] > myworld.MAXW)
                    w[ii][jj] = myworld.MAXW;
                if (w[ii][jj] < -myworld.MAXW)
                    w[ii][jj] = -myworld.MAXW;
            }
    }

    // This function controls the agent's behavior.
    public void update()
    {
        double dist, angle;
        int sensorR, sensorL;
        neury[6] = 2.0 * R.nextDouble() - 1.0;
        neury[7] = 1.0;
        neury[8] = 0.0; //2.0 * (myworld.POISONFIRSTHALF - .5);
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
                    neury[8] = 3.0;
                }
                else {
                    decreaseScore();
                    neury[8] = -3.0;
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

}
