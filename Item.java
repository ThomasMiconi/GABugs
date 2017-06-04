// Mostly to take care of redundant position code

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
    public void randPos(){
        x = R.nextDouble() * myw.WSIZE;
        y = R.nextDouble() * myw.WSIZE;
    }
    public double getDistanceFrom(Item ii){
            return Math.sqrt(torus(ii.x - x)*torus(ii.x - x) + torus(ii.y - y)*torus(ii.y - y));
            //return  Math.sqrt( (ii.x - x)*(ii.x - x) + (ii.y - y)*(ii.y - y) );
    }
    public double torus(double deltax)
    {
        if (deltax > myw.WSIZE / 2.0)
            return deltax - myw.WSIZE;
        if (deltax < -myw.WSIZE / 2.0)
            return myw.WSIZE + deltax;
        return deltax;
    }
    public double getAngleFrom(Item ii){
            //double angle = Math.atan2(torus(ii.y - y), torus(ii.x - x)); // -pi:pi range 
            double angle = Math.atan2(torus(y - ii.y), torus(ii.x - x)); // -pi:pi range; inverted in y to preserve counterclockwise (trigonometric) heading when y grows downwards.. 
            if (angle < 0) angle += 2*Math.PI;  // Now in the 0:2pi range, like heading
            //System.out.println(angle+" "+ii.y+" "+y+" "+ii.x+" "+x);
            return angle;
    }
}
