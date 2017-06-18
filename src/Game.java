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
 * TODO
 * - Check whether safe to enter a duplicate object (same memory ref) to pq; if not -> refactor prio update logic so that 
 * create a new Cell from the udpate cell (Cell updated = new Cell(old, <priority))
 * - Update shooting logic to use Cell-based onGrid check (superfluent to use x and y coordinates) (cf. placement logic)
 * - Randomize placement probing direction
 * - Improved strategy for placement priority, now follows target priority, which is not feasible
 * - Randomize higher priorities (HIT NEIGHBOR, BLOCK END), e.g. 1995-2005, 995-1005, so that "from same priority class" truly random is chosen
 * - Smarter way to iterate up/down/left/right, ideally in a single loop, getNext etc.; could add here a direction randomizer
 * - Add ship ID, at least for own grid, for grid print
 * - Control logic: convert API-call output as data for methods, and call methods
 * - UI
 * - Server side
 */
public class Game {

    private int turnId;
    private PriorityQueue<Cell> shotsQueue;
    private PriorityQueue<Cell> placementQueue;
    private List<Cell> shots;
    private List<Cell> hits;
    private Map<Player, Set<Integer>> sunken;
    private Cell[][] targetGrid;
    private Cell[][] ownGrid;
    private static final int GRID_SIZE = 10;
    private static final int PRIORITY_HIT_NEIGHBOR = 1000;
    private static final int PRIORITY_BLOCK_END = 2000;
    private static final int SHIP_MAX_SIZE = 5;


    public enum Direction {VERTICAL, HORIZONTAL}

    public enum Player {OWN, ENEMY}

    public enum ShotResult {HIT, MISS, SUNK}

    public Game() {
        shotsQueue = new PriorityQueue<>();
        placementQueue = new PriorityQueue<>();
        shots = new ArrayList<>();
        hits = new ArrayList<>();
        sunken = new HashMap<>();
        sunken.put(Player.OWN, new HashSet<>());
        sunken.put(Player.ENEMY, new HashSet<>());
        targetGrid = new Cell[GRID_SIZE][GRID_SIZE];
        ownGrid = new Cell[GRID_SIZE][GRID_SIZE];
        initializeGrids();
    }

    private void initializeGrids() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell targetCell = new Cell(i, j);
                targetCell.setPriority(getPriorityForTarget(i, j));
                shotsQueue.add(targetCell);
                targetGrid[i][j] = targetCell;
                Cell ownCell = new Cell(i, j);
                ownCell.setPriority(getPriorityForPlacement(i, j));
                placementQueue.add(ownCell);
                ownGrid[i][j] = ownCell;
            }
        }
        placeShips();
    }

    private void placeShips() {
        for (int size = 1; size <= SHIP_MAX_SIZE; size++) {
            boolean placed = false;
            while (!placed) {
                Cell candidate = placementQueue.poll();
                if (!placed) {
                    //Up and down
                    for (int deltaY = -1; deltaY <= 1; deltaY+=2) {
                        if (enoughSpace(candidate, size, deltaY, 0)) {
                            place(candidate, size , deltaY, 0, size);
                            placed = true;
                            break;
                        }
                    }
                }
                if (!placed) {
                    //Left and right
                    for (int deltaX = -1; deltaX <= 1; deltaX += 2) {
                        if (enoughSpace(candidate, size, 0, deltaX)) {
                            place(candidate, size, 0, deltaX, size);
                            placed = true;
                            break;
                        }
                    }
                }
            }

        }

    }

    private boolean enoughSpace(Cell focal, int size, int deltaY, int deltaX) {
        if (size == 0) return true;
        if (!onGrid(focal) || focal.hasShip()) return false;
        return enoughSpace(ownGrid[focal.getY() + deltaY][focal.getX() + deltaX], size - 1, deltaY, deltaX);
    }

    private void place(Cell parent, int size, int deltaY, int deltaX, int shipID) {
        if (size == 0) return;
        parent.setHasShip();
        parent.setShipID(shipID);
        place (ownGrid[parent.getY() + deltaY][parent.getX() + deltaX], size - 1, deltaY, deltaX, shipID);
    }

    /**
     * Basic strategy for random shots (currently not trailing a HIT)
     * Conceptualize targetGrid as a chequered board, with (0,0) as black and (0,1) & (1,0) as white, the rest following this pattern
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
     * Version 1: use same priority logic as for shooting (quite stupid, but done to get "something" done;
     * a better version would need adhering to principles, e.g. using border cells etc.
     * @param y
     * @param x
     * @return
     */
    public int getPriorityForPlacement(int y, int x) {
        return y + x;
    }

    /**
     * Process consequences of last round shot
     * - If hit, mark cell as containing a ship block update priorities of surrounding area of the hit as follows
     * 1) for each empty neighbor (not shot), set basic higher priority (i.e., mark neighbor as a likely container
     * of a block and thus a "hot zone" -> updates neighbor cell priority, which moves the cell (referred to in targetGrid)
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
                targetGrid[yLast][xLast].setHasShip();
                hits.add(targetGrid[yLast][xLast]);

                //Up and down
                for (int deltaY = -1; deltaY <= 1; deltaY+=2) {
                    int yNew = yLast + deltaY;
                    int xNew = xLast;
                    if (!onGrid(yNew, xNew) || targetGrid[yNew][xNew].isMiss()) continue;
                    updatePriority(yNew, xNew, Direction.VERTICAL, deltaY);
                }

                //Left and right
                for (int deltaX = -1; deltaX <= 1; deltaX+=2) {
                    int yNew = yLast;
                    int xNew = xLast + deltaX;
                    if (!onGrid(yNew, xNew) || targetGrid[yNew][xNew].isMiss()) continue;
                    updatePriority(yNew, xNew, Direction.HORIZONTAL, deltaX);
                }

                break;
            case SUNK:
                targetGrid[yLast][xLast].setHasShip();
                hits.add(targetGrid[yLast][xLast]);
                sunken.get(Player.ENEMY).add(shipId);
                break;
            default:
                break;
        }
        return shoot();
    }

    public Cell shoot() {
        while(!shotsQueue.isEmpty() && shotsQueue.peek().isShot()) shotsQueue.poll(); //remove duplicates, i.e. cells that have had priority updated
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
        Cell focal = targetGrid[y][x];
        if (focal.isHit()) {
            //find end of block, set as focal
            focal = direction == Direction.VERTICAL ? follow(focal, delta, 0) : follow(focal, 0, delta);
            if (focal != null) {
                focal.setPriority(PRIORITY_BLOCK_END);
            } else return; //focal (end of block) is null, i.e. out of targetGrid or a miss
        } else {
            focal.setPriority(oppositeIsHit(focal, direction, delta) ? PRIORITY_BLOCK_END : PRIORITY_HIT_NEIGHBOR);
        }
        shotsQueue.add(focal);
    }

    @Nullable
    public Cell follow (Cell parent, int deltaY, int deltaX) {
        final int childY = parent.getY() + deltaY;
        final int childX = parent.getX() + deltaX;
        if (!onGrid(childY, childX) || targetGrid[childY][childX].isMiss()) { // block end out of targetGrid or a miss
            return null;
        } else if (targetGrid[childY][childX].isEmpty()) { // reached a block end that has not been tried yet -> makes a high prio candidate
            return targetGrid[childY][childX].getPriority() < PRIORITY_BLOCK_END ? targetGrid[childY][childX] : null; //return a cell to update only if lower than block end prio
        } else {
            return follow(targetGrid[childY][childX], deltaY, deltaX); // == cell is HIT -> continue following the sequence of hits to reach the block end
        }
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
        return onGrid(oppositeY, oppositeX) && targetGrid[oppositeY][oppositeX].isHit();
    }

    public boolean onGrid(Cell cell) {
        return (cell.getX() >= 0 && cell.getX() <= 9 && cell.getY() >= 0 && cell.getY() <= 9);
    }

    public void printGrid(Player player) {
        Cell[][] focal = player == Player.OWN ? ownGrid : targetGrid;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            sb.append("[");
            for (int j = 0; j < GRID_SIZE; j++) {
                sb.append(focal[i][j].getSymbol());
            }
            sb.append("]\n");
        }
        System.out.println(sb.toString());
    }
}
