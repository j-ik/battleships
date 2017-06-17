public class Main {

    public static void main(String[] args) {

        Game game = new Game();
        Cell shot = game.shoot();
        shot = game.processLastShot(Game.ShotResult.MISS, shot.getY(), shot.getX(), -1);
        for (int i = 0; i < 3; i++) {
            shot = game.processLastShot(Game.ShotResult.HIT, shot.getY(), shot.getX(), -1);
        }
        shot = game.processLastShot(Game.ShotResult.SUNK, shot.getY(), shot.getX(), -1);
        System.out.println("Checkpoint");
    }
}