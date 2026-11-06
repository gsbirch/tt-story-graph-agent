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
public class GameMasterBehavior {
    public StoryGraph sg;
    List<edu.uky.cs.nil.tt.world.Action> worldActions;
    String playerOrder = "";
    static final String BREW_COFFEE = "brew(Barista, Coffee)";
    static final String BREW_TEA = "brew(Barista, Tea)";
    static final String ORDER_COFFEE = "order(Player, Coffee)";
    static final String ORDER_TEA = "order(Player, Tea)";
    static final String COFFEE = "Coffee";
    static final String TEA = "Tea";
    
    public GameMasterBehavior(String filename, List<edu.uky.cs.nil.tt.world.Action> worldActions) {
        try {
            sg = StoryGraph.from(new File(filename));
        }
        catch (Exception e) {
            System.err.println("Error opening the story graphs!");
        }
        this.worldActions = worldActions;
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

    public int recommendAction(List<Turn> history, Role role, List<Turn> choices) {
        // Update by what the player has most recently ordered
        setPlayerOrder(history);

        Node n = getToState(history);
        // Special behavior for state 1
        // This is because ordering is idempotent
        if (n.getID() == 1) return state1Action(history, role, choices);
        
        // Look at outgoing edges to find the action to take
        // There should only be one that the GM will take
        edu.uky.cs.nil.tt.world.Action act = null;
        for (var edge : n.edges.temporal) {
            if (edge.tail.equals(n) && !edge.isPlayerOnly()) {
                act = sgTottAction(edge.label);
                break;
            }
        }
        // Use the found action to find the choice to make
        // Return the index
        if (act != null)
            for (int i = 0; i < choices.size(); i++) {
                if (choices.get(i).action.equals(act))
                    return i;
            }
        return choices.size() - 1;
    }

    private int state1Action(List<Turn> history, Role role, List<Turn> choices) {
        // If player's order is coffee, brew coffee
        if (playerOrder == COFFEE) {
            for (int i = 0; i < choices.size(); i++) {
                if (choices.get(i).action.name.equals(BREW_COFFEE))
                    return i;
            }
        }
        // If player's order is tea, brew tea
        if (playerOrder == TEA) {
            for (int i = 0; i < choices.size(); i++) {
                if (choices.get(i).action.name.equals(BREW_TEA))
                    return i;
            }
        }
        // If the player hasn't ordered yet, wait for them to do so
        return choices.size() - 1;
    }

    public int allowProposal(List<Turn> history, Role role, Turn proposal) {
        // Update by what the player has most recently ordered
        setPlayerOrder(history);

        Node n = getToState(history);
        // We only accept actions that are in the state graph
        for (var edge : n.edges.temporal) {
            if (edge.tail.equals(n) && edge.label.name.equals(proposal.action.name)) {
                return 0;
            }
        }
        // If we could not find the action, reject it
        return 1;
    }

    private void setPlayerOrder(List<Turn> history) {
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).action.name.equals(ORDER_COFFEE)) {
                playerOrder = COFFEE;
                return;
            }
            if (history.get(i).action.name.equals(ORDER_TEA)) {
                playerOrder = TEA;
                return;
            }
        }
    }
}
