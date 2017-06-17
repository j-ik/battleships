import java.util.PriorityQueue;

public class Main {

    public static void main(String[] args) {


        PriorityQueue<Integer> pq = new PriorityQueue<>();

        /*
        pq.add(100);
        pq.add(5);
        pq.add(1);
        pq.add(10);
        */

        Integer x = 100;
        Integer y = 50;
        Integer z = 10;

        pq.add(x);
        pq.add(y);
        pq.add(z);


        Cell[] cells = new Cell[3];
        PriorityQueue<Cell> shotsQueue = new PriorityQueue<>();
        Cell a = new Cell(0,0);
        a.setPriority(10);
        Cell b = new Cell(0,1);
        b.setPriority(5);
        Cell c = new Cell(0,2);
        c.setPriority(1);
        shotsQueue.add(a);
        shotsQueue.add(b);
        shotsQueue.add(c);
        cells[0] = a;
        cells[1] = b;
        cells[2] = c;
        System.out.println("Heap head x: " + shotsQueue.peek().getX()); //0
        b.setPriority(1000);
        System.out.println("Heap head x: " + shotsQueue.peek().getX()); //1



        /*
        Game game = new Game();
        Cell shot = game.shoot();
        shot = game.processLastShot(Game.ShotResult.MISS, shot.getY(), shot.getX(), -1);
        for (int i = 0; i < 3; i++) {
            shot = game.processLastShot(Game.ShotResult.HIT, shot.getY(), shot.getX(), -1);
        }
        shot = game.processLastShot(Game.ShotResult.SUNK, shot.getY(), shot.getX(), -1);
        System.out.println("Checkpoint");
        */
    }
}