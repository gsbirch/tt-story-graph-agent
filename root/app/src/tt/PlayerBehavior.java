package tt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.uky.cs.nil.sg.Node;
import edu.uky.cs.nil.sg.StoryGraph;
import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.world.State;
import edu.uky.cs.nil.tt.world.Turn;
import edu.uky.cs.nil.tt.world.Value;
import edu.uky.cs.nil.tt.world.Variable;
import edu.uky.cs.nil.tt.world.World;

/**
 * This class provides a relevant interface to the story graph.
 * The main purpose is to query and locate the correct state,
 * then suggest an action.
 */
public class PlayerBehavior {
    public StoryGraph sg;
    World world;
    List<edu.uky.cs.nil.tt.world.Action> worldActions;
    String playerOrder = "";
    /** The player will ALWAYS prefer to trade their money for tea if they can */
    static final String TRADE_ACTION = "trade(Player, Money, Barista, Tea)";
    static final String BREW_TEA = "brew(Barista, Tea)";
    static final String BREW_COFFEE = "brew(Barista, Coffee)";
    static final String COFFEE_LOCATION = "has(Coffee)";
    static final String TEA_LOCATION = "has(Tea)";
    static final String BARISTA_VALUE = "Barista";

    Variable coffeeLoc;
    Variable teaLoc;
    Value baristaVal;
    
    public PlayerBehavior(String filename, List<edu.uky.cs.nil.tt.world.Action> worldActions, World world) {
        try {
            sg = StoryGraph.from(new File(filename));
            System.out.println("Successfully loaded the story graph!");
        }
        catch (Exception e) {
            System.err.println("Error opening the story graphs!");
        }
        this.worldActions = worldActions;
        this.world = world;
        // for (var v : world.getVariables()) System.out.println(v);
        coffeeLoc = world.getVariable(COFFEE_LOCATION);
        teaLoc = world.getVariable(TEA_LOCATION);
        baristaVal = world.getEntity(BARISTA_VALUE);
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

    public edu.uky.cs.nil.sg.Action findActionSG(String s) {
        for (var a : sg.actions) {
            if (a.name.equals(s))
                return a;
        }
        return null;
    }

    public edu.uky.cs.nil.tt.world.Action findActionTT(String s) {
        for (var a : worldActions) {
            if (a.name.equals(s))
                return a;
        }
        return null;
    }

    public Node getToState(List<Turn> history, State state) {
        // System.out.print("The previous actions are: ");
        // for (var act : history) {
        //     System.out.print(act.action + ", ");
        // }
        // System.out.println("");
        List<edu.uky.cs.nil.sg.Action> sgActions = history.stream().map(this::findAction).collect(Collectors.toCollection(ArrayList::new));
        // Fix the history:
        // If the barista brewed anything while the player was outside,
        // They "don't know the barista brewed anything"
        // So those actions needed to added manually
        // System.out.println(coffeeLoc);
        // System.out.println(state.get(coffeeLoc));
        if (!state.get(coffeeLoc).equals(null) && !sgActions.contains(findActionSG(BREW_COFFEE))) {
            sgActions.add(0, findActionSG(BREW_COFFEE));
        }
        if (!state.get(teaLoc).equals(null) && !sgActions.contains(findActionSG(BREW_TEA))) {
            sgActions.add(0, findActionSG(BREW_TEA));
        }

        // System.out.print("The previous actions (Story graph edition) are: ");
        // for (var act : sgActions) {
        //     System.out.print(act + ", ");
        // }
        // System.out.println();
        Node n = sg.nodes.get(0);
        return n.getAfter(sgActions);
    }

    public int recommendAction(List<Turn> history, Role role, List<Turn> choices, State state) {
        Node n = getToState(history, state);
        
        // Printys
        // System.out.println("I travelled to node " + n.getID());
        // System.out.print("The graph's next actions are: ");
        // for (var edge : n.edges.temporal) {
        //     System.out.print(edge.tail.getID() + "->" + edge.label.name + "->" + edge.head.getID() +  ",   ");
        // }
        // System.out.println();
        
        // Look at outgoing edges to find the action to take
        // There should only be one that the GM will take
        edu.uky.cs.nil.tt.world.Action act = null;
        for (var edge : n.edges.temporal) {
            // When it's available, the player will ALWAYS ask to trade
            if (edge.tail.equals(n) && edge.label.name.equals(TRADE_ACTION)) {
                // System.out.println("I found the trade action! I will ask to do this.");
                act = sgTottAction(edge.label);
                break;
            }
            if (act == null && edge.tail.equals(n) && edge.isPlayer()) {
                // System.out.println("I'm selecting edge: " + edge.label.name);
                act = sgTottAction(edge.label);
                // We need to keep searching for the trade action
                // break;
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

    public int allowProposal(List<Turn> history, Role role, Turn proposal, State state) {
        Node n = getToState(history, state);
        // We only accept actions that are in the state graph
        for (var edge : n.edges.temporal) {
            if (edge.tail.equals(n) && edge.label.name.equals(proposal.action.name)) {
                return 0;
            }
        }
        // If we could not find the action, reject it
        return 1;
    }

}
