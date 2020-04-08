package biz.minecraft.launcher;

public class Version {

    private double last;

    public double getLast() {
        return last;
    }

    @Override
    public String toString() {
        return Double.toString(last);
    }
}
