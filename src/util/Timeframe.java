package util;

import java.util.Random;

public class Timeframe {
    public double Min;
    public double Max;

    private static Random rand = new Random();


    public double Random() {
        return rand.nextDouble() * Max + Min;
    }

    public Timeframe() {
        this(30, 300);
    }

    public Timeframe(double min, double max) {
        this.Min = min;
        this.Max = max;
    }
}