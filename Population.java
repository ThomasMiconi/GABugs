import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.*;  
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
public class Population implements Comparable<Population>{
    Agent[] pop;
    int POPSIZE, NBNEUR;
    World myworld;
    Population(World ww){
        myworld = ww;
        NBNEUR = ww.NBNEUR;
        POPSIZE = ww.POPSIZE;
        pop = new Agent[POPSIZE]; for (int nn=0; nn< pop.length; nn++) pop[nn] = new Agent(ww, nn);
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
    
    public void readPop(String fname)
    {   
        try{
            System.out.println("Reading from file "+fname);
            BufferedReader in
                = new BufferedReader(new FileReader(fname));

            for (int numagent=0; numagent < POPSIZE; numagent++)
            {
                for (int row=0; row < NBNEUR; row++)
                {   
                    String strs[] = in.readLine().split(" ");
                    if (strs.length != NBNEUR)
                        throw new RuntimeException("Data file has wrong number of neurons! (strs.length is "+strs.length+", strs[0] is "+strs[0]+", numagent "+numagent+", row "+row+")");
                    for (int col=0; col < NBNEUR; col++)
                        pop[numagent].w[row][col] = Double.parseDouble(strs[col]);
                }
                in.readLine(); in.readLine(); in.readLine();
            }
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't read population from file "+fname); System.exit(0);            
        }
        for (int numagent=1; numagent < POPSIZE; numagent++){
            pop[numagent].copyFrom(pop[0]);
        }
    }


    
    public void savePop(String fname)
    {   
        try{
            PrintWriter writer = new PrintWriter(fname);
            for (int n=0; n < pop.length; n++)
            {
                for (int row=0; row < NBNEUR; row++)
                {   
                    for (int col=0; col < NBNEUR; col++)
                        writer.print(Double.toString(pop[n].w[row][col])+" "); 
                    writer.print("\n");
                }
                writer.println("\n\n");
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't save population to file "+fname); System.exit(0);            
        }

    }

}
