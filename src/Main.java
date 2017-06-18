public class Main {

    public static class Number implements Comparable<Number> {
        int value;
        
        public Number(int value) {
            this.value = value;
        }
        
        public void setValue(int newValue) {
            value = newValue;
        }

        @Override
        public int compareTo(Number o) {
            return -Integer.compare(this.value, o.value);
        }
    }
    
    
    public static void main(String[] args) {



        /*
        Number a = new Number(1);
        Number b = new Number(5);
        Number c = new Number(10);
        PriorityQueue<Number> pq = new PriorityQueue<>();
        pq.add(a);
        pq.add(b);
        pq.add(c);
        b.setValue(100);
        pq.add(b);
        b.setValue(1000);
        pq.add(b);
        b.setValue(10000);
        pq.add(b);
        while(!pq.isEmpty()) System.out.println(pq.poll().value);
        */


        Game game = new Game();
        System.out.println("Grids at start:");
        System.out.println("\n===================");
        System.out.println("Own:");
        game.printGrid(Game.Player.OWN);
        System.out.println("\n===================");
        System.out.println("Enemy:");
        game.printGrid(Game.Player.ENEMY);
        Cell shot = game.shoot();
        shot = game.processLastShot(Game.ShotResult.MISS, shot.getY(), shot.getX(), -1);
        for (int i = 0; i < 3; i++) {
            shot = game.processLastShot(Game.ShotResult.HIT, shot.getY(), shot.getX(), -1);
        }
        shot = game.processLastShot(Game.ShotResult.SUNK, shot.getY(), shot.getX(), 2);
        System.out.println("\n===================");
        System.out.println("\n===================");
        System.out.println("Grids after game:");
        System.out.println("\n===================");
        System.out.println("Own:");
        game.printGrid(Game.Player.OWN);
        System.out.println("\n===================");
        System.out.println("Enemy:");
        game.printGrid(Game.Player.ENEMY);
    }
}