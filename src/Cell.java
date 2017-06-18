/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Cell implements Comparable<Cell> {

    private int y;
    private int x;
    private int priority;
    private boolean hasShip;
    private boolean isShot;
    private int shipID;
    private static final char SYMBOL_EMPTY = '-';
    private static final char SYMBOL_HIT_SHIP = 'X';
    private static final char SYMBOL_MISS = 'o';
    private char symbol;

    public Cell(int y, int x, int priority) {
        this.y = y;
        this.x = x;
        this.priority = priority;
    }

    public Cell(int y, int x) {
        this(y, x, 0);
    }

    public Cell(Cell reference, int priority) {
        this.y = reference.getY();
        this.x = reference.getX();
        this.hasShip = reference.hasShip;
        this.isShot = reference.isShot;
        this.priority = priority;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setHasShip() {
        this.hasShip = true;
    }

    public void setShot() {
        isShot = true;
    }

    public boolean isMiss() {
        return isShot && !hasShip;
    }

    public boolean isHit() {
        return isShot && hasShip;
    }

    public boolean isEmpty() {
        return !hasShip && !isShot;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isShot() {
        return isShot;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasShip() {
        return hasShip;
    }

    public void setShipID(int shipID) {
        this.shipID = shipID;
    }

    @Override
    public int compareTo(Cell o) {
        return -Integer.compare(this.priority, o.priority);
    }


    public char getSymbol() {
        if (isEmpty()) return SYMBOL_EMPTY;
        if (isMiss()) return SYMBOL_MISS;
        if (isHit()) return SYMBOL_HIT_SHIP;
        else return Character.forDigit(shipID, 10);
    }
}
