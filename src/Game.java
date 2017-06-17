import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.istack.internal.Nullable;

/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Game {

    private int turnId;
    private PriorityQueue<Cell> shotsQueue;
    private List<Cell> shots;
    private List<Cell> hits;
    private Map<Player, Set<Integer>> sunken;
    private Cell[][] grid;
    private static final int GRID_SIZE = 10;
    private static final int PRIORITY_HIT_NEIGHBOR = 1000;
    private static final int PRIORITY_BLOCK_END = 2000;

    public enum Direction {VERTICAL, HORIZONTAL}

    public enum Player {OWN, ENEMY}

    public enum ShotResult {HIT, MISS, SUNK}

    public Game() {
        shotsQueue = new PriorityQueue<>();
        shots = new ArrayList<>();
        hits = new ArrayList<>();
        sunken = new HashMap<>();
        sunken.put(Player.OWN, new HashSet<>());
        sunken.put(Player.ENEMY, new HashSet<>());
        grid = new Cell[GRID_SIZE][GRID_SIZE];
        initializeTargetGrid();
    }

    //TODO
    public Cell[][] setOwnGrid() {
        return null;
    }

    public void initializeTargetGrid() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = new Cell(i, j);
                cell.setPriority(getPriorityForTarget(i, j));
                shotsQueue.add(cell);
                grid[i][j] = cell;
            }
        }
    }

    /**
     * Basic strategy for random shots (currently not trailing a HIT)
     * Conceptualize grid as a chequered board, with (0,0) as black and (0,1) & (1,0) as white, the rest following this pattern
     * Create priority for "blacks", and weight a diagonal line so that main diagonal tends to get highest priorities,
     * other diagonal lines the higher priorities the closer they are the main diagonal
     * @param y focal cell y coordinate
     * @param x focal cell x coordinate
     * @return priority
     */
    public int getPriorityForTarget(int y, int x) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if ((y % 2 == 0 && x % 2 == 1) || (y % 2 == 1 && x % 2 == 0))  return 0; //on "white"
        return (GRID_SIZE - (Math.abs(y - x))) * random.nextInt(GRID_SIZE); //the closer to diagonal, the higher value; add a random weight
    }

    /**
     * Process consequences of last round shot
     * - If hit, mark cell as containing a ship block update priorities of surrounding area of the hit as follows
     * 1) for each empty neighbor (not shot), set basic higher priority (i.e., mark neighbor as a likely container
     * of a block and thus a "hot zone" -> updates neighbor cell priority, which moves the cell (referred to in grid)
     * higher up in the heap of coming shots
     * 2) for each neighbor with a hit, follow the hits to that direction setting an empty "block end", i.e.
     * empty cell following a sequence of hits, with an even higher priority. So if we have say hit at (1,1)
     * and identify (through the follow(.) -recursion) that we have previously have hits on (1,2) and (1,3), and further,
     * (1,4) is empty (not tried yet) we should add even higher priority than a normal empty neighbor to (1,4), since
     * such "block end" empty-cell it is likely to contain a ship block
     * - If miss, fall through, no updates to shot priorities, as no new information obatained
     * - If sunk, mark cell as containing a ship and add ship id as sunken ship ("i.e. check the ship as completed")
     *
     * Finally, return the head of the heap with the untried cell having the highest priority (and mark the respective
     * cell as shot)
     *
     * @param shotResult Report of last shot
     * @param yLast y coordinate of last shot
     * @param xLast x coordinate of last shot
     * @param shipId
     * @return
     */
    public Cell processLastShot(ShotResult shotResult, int yLast, int xLast, int shipId) {
        switch (shotResult) {
            case MISS:
                break; //Fall through
            case HIT:
                grid[yLast][xLast].setHasShip();
                hits.add(grid[yLast][xLast]);

                //Up and down
                for (int deltaY = -1; deltaY <= 1; deltaY+=2) {
                    int yNew = yLast + deltaY;
                    int xNew = xLast;
                    if (!onGrid(yNew, xNew) || grid[yNew][xNew].isMiss()) continue;
                    updatePriority(yNew, xNew, Direction.VERTICAL, deltaY);
                }

                //Left and right
                for (int deltaX = -1; deltaX <= 1; deltaX+=2) {
                    int yNew = yLast;
                    int xNew = xLast + deltaX;
                    if (!onGrid(yNew, xNew) || grid[yNew][xNew].isMiss()) continue;
                    updatePriority(yNew, xNew, Direction.HORIZONTAL, deltaX);
                }

                break;
            case SUNK:
                grid[yLast][xLast].setHasShip();
                hits.add(grid[yLast][xLast]);
                sunken.get(Player.ENEMY).add(shipId);
                break;
            default:
                break;
        }
        return shoot();
    }

    public Cell shoot() {
        Cell nextShot = shotsQueue.poll();
        nextShot.setShot();
        shots.add(nextShot);
        return nextShot;
    }

    /**
     * Update neighbors and potential block ends.
     *
     * 1) For normal neighbors, add basic neighbor priority (neighbor of a HIT = "hot zone")
     * 2) For neighbor that is already a HIT, find out the "other end" of the block (see -> follow()), and if an empty
     * block end is found, set a higher level of priority
     * 3) For an empty neighbor, for which opposite cell is a HIT, set also a higher priority:
     * When opposite of an empty focal cell is HIT, the focal cell constitutes an end-of-block cell which is of high likelihood of hit
     * (higher than normal neighbor of a HIT cell. For example, we have a hit at (1,1), and are checking its neighbors,
     * and looking at (1,0). We are interested if (1,2) is hit. That would mean that (1,0) is an end of a block that contains
     * at least two hit cells, (1,1) and (1,2) to be precise. We need this kind of check, since our follow function is triggered only
     * from a neighbor that is HIT onwards; if we would follow also an empty cell, we would automatically update
     * all neighors with PRIORITY_BLOCK_END, instead of just a normal neighbor PRIORITY_HIT_NEIGHBOR (which should be the case)
     *
     * @param y y coordinate of the neighbor checked
     * @param x x coordinate of the neighbor checked
     * @param direction which way we are moving
     * @param delta increment, implicitly given by direction, explicate here; added either to x or y coordinate, depending on direction
     */
    public void updatePriority(int y, int x, Direction direction, int delta) {
        final Cell focal = grid[y][x];
        if (focal.isHit()) {
            Cell blockEnd = null;
            switch (direction) {
                case VERTICAL:
                    blockEnd = follow(focal, delta, 0);
                    break;
                case HORIZONTAL:
                    blockEnd = follow(focal, 0, delta);
                    break;
            }
            if (blockEnd != null) blockEnd.setPriority(PRIORITY_BLOCK_END);
        } else if (oppositeIsHit(focal, direction, delta)) {
            focal.setPriority(PRIORITY_BLOCK_END);
        } else {
            focal.setPriority(PRIORITY_HIT_NEIGHBOR);
        }
    }

    @Nullable
    public Cell follow (Cell parent, int deltaY, int deltaX) {
        final int childY = parent.getY();
        final int childX = parent.getX();
        if (!onGrid(childY, childX) || grid[childY][childX].isMiss()) return null; // block end out of grid or already tried to no avail
        if (grid[childY][childX].isEmpty()) return grid[childY][childX]; // reached a block end that has not been tried yet -> makes a high prio candidate
        follow(grid[childY][childX], deltaY, deltaX); // == cell is HIT -> continue following the sequence of hits to reach the block end
        return null;
    }

    public boolean onGrid(int x, int y) {
        return (x >= 0 && x <= 9 && y >= 0 && y <= 9);
    }

    /**
     * Determine whether a cell two steps back on the prevailing direction is a hit, making the queried cell a block end
     * containg at least its parent and the neighbor of the parent in the prevailing direction
     * @return Whether the opposite cell ("on the other side" of the parent) is HIT cell
     */
    public boolean oppositeIsHit(Cell focal, Direction direction, int delta) {
        //Go back two steps - one to parent, next to neighbor of the parent - in the direction the current cell was reached
        //E.g. cell (1,0) was reached by checking the neighbors of (1,1), when Direction = HORIZONTAL, and delta = -1
        //then we are interested in cell (1,2), for which y-coordinate = 1 and x-coordinate = 0 + (-1) * (-1) * 2 = 0 + 2 = 2
        final int offset = -delta * 2;
        int oppositeX = direction == Direction.VERTICAL ? focal.getX() : focal.getX() + offset;
        int oppositeY = direction == Direction.VERTICAL ? focal.getY() + offset : focal.getY();
        return onGrid(oppositeY, oppositeX) && grid[oppositeY][oppositeY].isHit();
    }

    /*
    public boolean onGrid(Cell cell) {
        return (cell.getX() >= 0 && cell.getX() <= 9 && cell.getY() >= 0 && cell.getY() <= 9);
    }
    */
}
