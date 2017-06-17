/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Space implements Comparable<Space> {

    private int y;
    private int x;
    private int priority;


    public int getY() {

        return y;
    }

    public int getX() {
        return x;
    }

    public Space(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setShot() {
        priority = -1;
    }

    @Override
    public int compareTo(Space o) {
        return -Integer.compare(this.priority, o.priority);
    }
}
