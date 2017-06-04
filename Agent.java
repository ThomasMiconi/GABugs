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
    double dtdivtau = 1.0 / 30.0;
    Agent(World myw, int myn)
    {
        super(myw);
        myworld = myw;
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
                w[ii][jj] = 2.0 * R.nextDouble() - 1.0;
    }
    public void resetNeurons() { 
        for (int n=0; n<NBNEUR; n++){
            neurx[n] = 0;
            neury[n] = 0;
        }
    }
    public void resetScore() { score = 0; } 
    public void increaseScore() { score ++; } 
    public void copyFrom(Agent A){
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
    public void fillSensors()
    {
        neury[2] = 0.0;
        for (int n=0; n < myworld.FOODSIZE; n++)
        {
            if (n == num) 
                continue;
            double angle = getAngleFrom(myworld.food[n]);
            if ((angle-heading < .15) && (angle-heading > -.15))
                neury[2] += 1.0 / (1.0 + .1 * getDistanceFrom(myworld.food[n]));
        }
        neury[3] = 2.0 * R.nextDouble() - 1.0;
        neury[4] = 1.0;
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
                //double cauchy = myworld.MUTATIONSCALEFACTOR * Math.tan((R.nextDouble() - .5) * Math.PI);
                //w[ii][jj] += cauchy;
                if (R.nextDouble() < myworld.PROBAMUT)
                    w[ii][jj] += R.nextGaussian();
                if (w[ii][jj] > myworld.MAXW)
                    w[ii][jj] = myworld.MAXW;
                if (w[ii][jj] < -myworld.MAXW)
                    w[ii][jj] = -myworld.MAXW;
            }
    }
    public void update()
    {
        double dist, angle;
        neury[4] = 2.0 * R.nextDouble() - 1.0;
        neury[5] = 1.0;
        neury[2] = 0.0; neury[3] = 0.0;
        for (int n=0; n < myworld.FOODSIZE; n++)
        {
            dist = getDistanceFrom(myworld.food[n]);
            //System.out.println(dist);
            if (dist < 10.0) //  myworld.EATRADIUS) //Eat !
            {
                    increaseScore();
                    myworld.food[n].randPos(); 
            }
            else
            {
                angle = getAngleFrom(myworld.food[n]);
                if ((angle-heading < 3.0) && (angle-heading > 0))
                    neury[2] += 1.0 / (1.0 + .1 * dist);
                if ((angle-heading > -3.0) && (angle-heading < 0))
                    neury[3] += 1.0 / (1.0 + .1 * dist);
                //System.out.println(angle - heading);
            }

        }
        /*if (neury[2] > .1)
            System.out.println("Left");
        if (neury[3] > .1)
            System.out.println("Right");*/
        runNetwork();
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
