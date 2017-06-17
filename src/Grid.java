import java.util.Set;

/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Grid {

    Space[][] positions;
    Set<Space> unvisited;
    Set<Space> visited;
    Set<Target> targets;

    public Grid() {

    }

    public Space[][] getPositions() {
        return positions;
    }

    public Set<Space> getUnvisited() {
        return unvisited;
    }

    public Set<Space> getVisited() {
        return visited;
    }

    public Set<Target> getTargets() {
        return targets;
    }
}
