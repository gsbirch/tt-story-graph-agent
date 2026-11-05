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
    public StoryGraph decision;
    List<edu.uky.cs.nil.tt.world.Action> worldActions;
    
    public StoryGraphView(String filename, String decisionFilename, List<edu.uky.cs.nil.tt.world.Action> worldActions) {
        try {
            sg = StoryGraph.from(new File(filename));
            decision = StoryGraph.from(new File(decisionFilename));
            System.out.println("Successfully loaded the story graph!");
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
        System.out.print("The previous actions are: ");
        for (var act : history) {
            System.out.print(act.action + ", ");
        }
        System.out.println("");
        List<edu.uky.cs.nil.sg.Action> sgActions = history.stream().map(this::findAction).toList();
        System.out.print("The previous actions (Story graph edition) are: ");
        for (var act : sgActions) {
            System.out.print(act + ", ");
        }
        System.out.println();
        Node n = sg.nodes.get(0);
        return n.getAfter(sgActions);
    }

    public edu.uky.cs.nil.tt.world.Action recommendAction(List<Turn> history, Role role) {
        Node n = getToState(history);
        System.out.println("I travelled to node " + n.getID());
        System.out.print("The graph's next actions are: ");
        for (var edge : n.edges.temporal) {
            System.out.print(edge.tail.getID() + "->" + edge.label.name + "->" + edge.head.getID() +  ",   ");
        }
        System.out.println();
        Node decisionNode = decision.nodes.get(n.getID());
        for (var edge : decisionNode.edges.temporal) {
            if (edge.tail.equals(decisionNode) && (role == Role.PLAYER) == edge.isPlayer()) {
                System.out.println("I'm selecting edge: " + edge.label.name);
                return sgTottAction(edge.label);
            }
        }
        return null;
    }

    public int allowProposal(List<Turn> history, Role role, Turn proposal) {
        Node n = getToState(history);
        Node decisionNode = decision.nodes.get(n.getID());
        for (var edge : decisionNode.edges.temporal) {
            if (edge.tail.equals(decisionNode) && (role == Role.PLAYER) == edge.isPlayer() && edge.label.name.equals(proposal.action.name)) {
                return 0;
            }
        }
        return 1;
    }
}
