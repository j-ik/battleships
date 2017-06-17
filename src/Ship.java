/**
 * Created by jounikauremaa on 16/06/2017.
 */
public class Ship {

    private int id;
    private int size;
    private Space leftEnd;
    private Space rightEnd;

    public Ship (int id, int size, Space leftEnd, Space rightEnd) {
        this.id = id;
        this.size = size;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
    }
}
