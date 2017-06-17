import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Game {

    private int turnId;
    private Set<Target> wipTargets;
    private PriorityQueue<Space> shots;
    private Space[][] grid;
    private static final int GRID_SIZE = 10;
    //private ArrayDeque<Target> targets;
    //private Map<Integer, Ship> fleet;

    public Game() {
        //target = new Target();
        //fleet = new HashMap();
        //targets = new ArrayDeque();
        this.shots = new PriorityQueue<>();
        grid = new Space[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Space space = new Space(i, j);
                space.setPriority(getPriority(i, j));
                shots.add(space);
                grid[i][j] = space;
            }
        }
    }

    //TODO
    public Space[][] setOwnGrid() {
        return null;
    }

    public int getPriority(int y, int x) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if ((y % 2 == 0 && x % 2 == 1) || (y % 2 == 1 && x % 2 == 0))  return 0; //on "white"
        return (GRID_SIZE - (Math.abs(y - x))) * random.nextInt(GRID_SIZE); //the closer to diagonal, the higher value, random weight
    }

    public Space shoot(String lastResult, int xLast, int yLast) {
        Space lastShot = grid[xLast][yLast];
        lastShot.setShot(); //set to bottom of the heap
        //Target target = getTarget(lastShot);
        switch (lastResult) {
            case "MISS": "SUNK":
                break; //fall through
            case "HIT":
                // set high priority for unvisited neighbors of a hit cell
                for (int i = -1; i <= 1; i+=2) {
                    int newY = yLast;
                    int newX = xLast + i;
                    if (newY < 0 || newY > 9 || newX < 0 || newY > 9 || grid[newY][newY].getPriority() == -1) continue;
                    grid[newY][newY].setPriority(1000);
                }
                for (int i = -1; i <= 1; i+=2) {
                    int newY = yLast + 1;
                    int newX = xLast;
                    if (newY < 0 || newY > 9 || newX < 0 || newY > 9 || grid[newY][newY].getPriority() == -1) continue;
                    grid[newY][newY].setPriority(1000);
                }
            default:
                break;
        }
        return shots.poll();
    }


    /*
    public Target getTarget(Space reference) {
        Target target;
        if (targets.isEmpty()) {
            target = new Target(reference);
            targets.addLast(target);
        } else {
            target = targets.peekFirst();
        }
        return target;
    }
    public static Grid createFleet() {
        Map<Integer, Target> fleet = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            fleet.put(i, new Target(i));
        }
        //return new Grid();

    }


    public Space shootRandom() {
        return shots.poll();
    }

    public static void shoot(Grid target) {
        //List<Space> empty = target.
    }

    public Space shootFocused(Space focal) {
        for (int i = -1; i <= 1; i+= 2) {

        }
    }
    */
}
