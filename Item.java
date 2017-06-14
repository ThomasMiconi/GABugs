// Mostly to take care of redundant position code between FoodBit and Agent
import java.util.Random;
public class Item  
{
    double x;
    double y;
    Random R;
    World myw;
    public Item(World w)
    {
        myw = w;
        R = myw.R;
    }
    
    // The torus function transforms distances to take into account the fact
    // that the world is toroidal (wrap-around both vertically and
    // horizontally). If a distance is larger than WSIZE/2, the wraparound
    // distance is smaller and is therefore the correct one!
    public double torus(double deltax)
    {
        if (deltax > myw.WSIZE / 2.0)
            return deltax - myw.WSIZE;
        if (deltax < -myw.WSIZE / 2.0)
            return myw.WSIZE + deltax;
        return deltax;
    }
    
    public void randPos(){
        x = R.nextDouble() * myw.WSIZE;
        y = R.nextDouble() * myw.WSIZE;
    }
    public double getDistanceFrom(Item ii){
            return Math.sqrt(torus(ii.x - x)*torus(ii.x - x) + torus(ii.y - y)*torus(ii.y - y));
    }

    // Computes the angle between us and another Item. This should be a
    // trigonometric-circle angle: 0 for "full right", pi/2 for "just above",
    // all the way to 2*pi for "just below full right".
    // There is almost certainly a computationally faster way to do this, but this works.
    public double getAngleFrom(Item ii){
            double angle = Math.atan2(torus(y - ii.y), torus(ii.x - x)); // -pi:pi range; inverted in y to preserve counterclockwise (trigonometric) heading as y grows downwards.. 
            if (angle < 0) angle += 2*Math.PI;  // Now in the 0:2pi range
            return angle;
    }
}
