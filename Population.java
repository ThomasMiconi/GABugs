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
    Population(World ww){
        NBNEUR = ww.NBNEUR;
        POPSIZE = ww.POPSIZE;
        pop = new Agent[POPSIZE]; for (int nn=0; nn< pop.length; nn++) pop[nn] = new Agent(ww, nn);
    }
    public void update(World ww){
            for (int n=0; n < pop.length; n++)
                pop[n].update(ww);
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
    
    public void readPopOld(String fname)
    {   
        try{
             BufferedReader in
                    = new BufferedReader(new FileReader(fname));
             
                for (int row=0; row < NBNEUR; row++)
                {   
                    String strs[] = in.readLine().split(" ");
                    if (strs.length != NBNEUR)
                        throw new RuntimeException("Data file has wrong number of neurons!");
                    for (int col=0; col < NBNEUR; col++)
                        pop[0].w[row][col] = Double.parseDouble(strs[col]);
                }
                in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (int numagent=1; numagent < POPSIZE; numagent++){
            pop[numagent].copyFrom(pop[0]);
        }


    }
    public void readPop(String fname)
    {   

        try{
            Population other;

            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fname));
            other=(Population)in.readObject();
            copyFrom(other);

        }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    public void savePop(String fname)
    {   
        try{

            ObjectOutputStream oos = new ObjectOutputStream( 
                    new FileOutputStream(fname));

            oos.writeObject( this );
            oos.close();
        }
        catch (IOException e) { e.printStackTrace(); }

    }
    
    public void savePopOld(String fname)
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
            e.printStackTrace();
        }

    }

}
