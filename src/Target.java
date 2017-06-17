import java.util.List;
import java.util.Set;

/**
 * Created by jounikauremaa on 15/06/2017.
 */
public class Target {

    private int id;
    private int size;
    private List<Space> area;
    private boolean focused;
    private Space init;
    private Space currentLeft;
    private Space currentRight;
    private Space focus;
    private Set<Space> open;

    public Target(Space init) {
        this.currentLeft = this.currentRight = this.init = this.focus = init;
        this.focused = true;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocus(Space focus) {
        this.focus = focus;
    }

    public void removeFocus() {
        focused = false;
    }


}
