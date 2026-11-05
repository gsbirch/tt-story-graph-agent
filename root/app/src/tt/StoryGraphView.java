package tt;

import java.io.File;
import java.util.List;

import edu.uky.cs.nil.sg.Node;
import edu.uky.cs.nil.sg.StoryGraph;
import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.world.Turn;

/**
 * This class provides a relevant interface to the story graph.
 * The main purpose is to query and locate the correct state,
 * then suggest an action.
 */
public class StoryGraphView {
    public StoryGraph sg;
    List<edu.uky.cs.nil.tt.world.Action> worldActions;
    
    public StoryGraphView(String filename) {
        try {
            sg = StoryGraph.from(new File(filename));
        }
        catch (Exception e) {
            System.err.println("Error opening the story graph at \"" + filename + "\"");
        }
    }

    public edu.uky.cs.nil.sg.Action ttTosgAction(edu.uky.cs.nil.tt.world.Action ttAction) {
        for (var sga : sg.actions) {
            if (ttAction.name.equals(sga.name)) {
                return sga;
            }
        }
        return null;
    }

    public edu.uky.cs.nil.tt.world.Action sgTottAction(edu.uky.cs.nil.sg.Action sgAction) {
        for (var tta : worldActions) {
            if (sgAction.name.equals(tta.name)) {
                return tta;
            }
        }
        return null;
    }

    public edu.uky.cs.nil.sg.Action findAction(Turn t) {
        return ttTosgAction(t.action);
    }

    public Node getToState(List<Turn> history) {
        List<edu.uky.cs.nil.sg.Action> sgActions = history.stream().map(this::findAction).toList();
        Node n = sg.nodes.get(0);
        return n.getAfter(sgActions);
    }

    public edu.uky.cs.nil.tt.world.Action recommendAction(List<Turn> history, Role role) {
        Node n = getToState(history);
        for (var edge : n.edges.temporal) {
            if ((role == Role.PLAYER) == edge.isPlayer()) {
                return sgTottAction(edge.label);
            }
        }
        return null;
    }
}
