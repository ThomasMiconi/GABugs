import java.util.Random;
public class FoodBit extends Item{
    double speedy;
    double speedx;
    World w;
    Random R;
    FoodBit(World myw)
    {
        super(myw);
        w = myw;
        R = w.R;
        speedx=0.0;
        speedy=0.0;
        x = R.nextDouble() * w.WSIZE;
        y = R.nextDouble() * w.WSIZE;
    }
    public void update()
    {
        speedx += w.FOODSPEED * (R.nextDouble() - .5);
        speedy += w.FOODSPEED * (R.nextDouble() - .5);
        speedx *= .98;
        speedy *= .98;
        x += speedx;
        y += speedy;
        if (y > w.WSIZE)
            y = 0;
        if (y < 0)
            y = w.WSIZE;
        if (x > w.WSIZE)
            x = 0;
        if (x < 0)
            x = w.WSIZE;
        
    }

}
