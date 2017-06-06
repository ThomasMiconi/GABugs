import java.awt.*;  
public class Population implements Comparable<Population>{
    Agent[] pop;
    int POPSIZE, NBNEUR;
    World myworld;
    Population(World ww){
        myworld = ww;
        POPSIZE = ww.POPSIZE;
        pop = new Agent[POPSIZE]; for (int nn=0; nn< pop.length; nn++) pop[nn] = new Agent(myworld, nn);
    }
    public void update(){
            for (int n=0; n < pop.length; n++)
                pop[n].update();
    }
    public int compareTo(Population o)
    {
        return Integer.signum(getTotalScore() - o.getTotalScore());
    }
    public int getTotalScore()
    {
        int s=0;
        for (int n=0; n < pop.length; n++)
            s += pop[n].score;
        return s;
    }
    public void resetScores()
    {
        for (int n=0; n < pop.length; n++)
            pop[n].resetScore();
    }
    public void draw(Graphics G){
        G.setColor(Color.red);
        for (int n=0; n < pop.length; n++){
            //G.fillArc((int)(myWorld.pop[n].x), (int)myWorld.pop[n].y, 20, 20, (int)Math.toDegrees(myWorld.pop[n].heading), 40); // Doesn't really work
            G.fillOval((int)(pop[n].x)-3, (int)pop[n].y-3, 7, 7);
            G.fillOval((int)(pop[n].x + 6 * Math.cos(pop[n].heading))-1, 
                       (int)(pop[n].y - 6 * Math.sin(pop[n].heading))-1, // minus to preserve counterclockwise (trigonometric) heading when y grows downwards..
                        2,2);

        }
    }
    public void copyFrom(Population p){
        for (int n=0; n < pop.length; n++)
            pop[n].copyFrom(p.pop[n]);
    }
    public void randomizeNets(){
        // Clonal!
        pop[0].randomizeNet();
        for (int n=1; n < pop.length; n++)
            pop[n].copyFrom(pop[0]);
        //for (int n=0; n < pop.length; n++)
        //    pop[n].randomizeNet();
    }
    public void randomizePositions(){
        for (int n=0; n < pop.length; n++)
            pop[n].randPos();
    }
    public void initialize(){
        for (int n=0; n < pop.length; n++)
            pop[n].initialize();
    }
    public void mutate(){
        // Clonal!
        pop[0].mutate();
        for (int n=1; n < pop.length; n++)
            pop[n].copyFrom(pop[0]);
    }

}
