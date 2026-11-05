package tt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;

import edu.uky.cs.nil.sg.StoryGraph;
import edu.uky.cs.nil.tt.*;
import edu.uky.cs.nil.tt.io.*;
import edu.uky.cs.nil.tt.world.*;

/**
 * A Tandem Tales agent that makes random choices. This agent can play as either
 * role, in any story world, and with any partner.
 * <p>
 * This agent follows a simple process when making its choices. When choosing a
 * turn, the agent has a {@link #PASS_PROBABILITY} chance to pass control to its
 * partner. If it does not pass, it chooses a non-pass action uniformly at
 * random. There is one exception to this rule. When this agent has to decide on
 * a joint action proposed by its partner, it has a {@link SUCCEED_PROBABILITY}
 * chance of letting the action succeed.
 * <p>
 * This agent waits a random number of milliseconds up to {@link #delay} to give
 * the impression that it is thinking. If the delay is set to 0, the agent will
 * take turns with no delay.
 */
public class StoryGraphAgent extends Client {
	
	/** Probability that this agent will pass control to its partner */
	private static final double PASS_PROBABILITY = 0.3;
	
	/** Probability that this agent will agree to a proposed joint action */
	private static final double SUCCEED_PROBABILITY = 0.8;
	
	/** The default server URL used if none is provided */
	public static final String DEFAULT_URL = "localhost";
	
	/** The default network port used if none is provided */
	public static final int DEFAULT_PORT = Client.DEFAULT_PORT;
	
	/** The default delay if none is provided */
	private static final long DEFAULT_DELAY = 5000;
	
	/** The ID number of the next agent to be created */
	private static int nextID = 0;
	
	/** A unique ID number used to identify this agent */
	public final int id = nextID++;
	
	/** Max milliseconds this agent will wait before taking a turn */
	public final long delay;
	
	/** The random number generator used to make decisions */
	private final Random random;

	/** A helpful interface to the used story graph. */
	public StoryGraphView sgv;
	
	/**
	 * Creates a new random agent with the given settings.
	 * 
	 * @param url the URL of the server to connect to
	 * @param port the network port to connect to the server on
	 * @param random the pseudo-random number generator used to make decisions
	 * @param delay the max milliseconds to wait before taking a turn
	 */
	private StoryGraphAgent(String url, int port, Random random, long delay) {
		super("storygraph", null, null, null, url, port);
		this.random = random;
		this.delay = delay;
	}
	
	/**
	 * Creates a new random agent with the given settings.
	 * 
	 * @param url the URL of the server to connect to
	 * @param port the network port to connect to the server on
	 * @param seed the seed used by the pseudo-random number generator that
	 * makes decisions
	 * @param delay the max milliseconds to wait before taking a turn
	 */
	public StoryGraphAgent(String url, int port, long seed, long delay) {
		this(url, port, new Random(seed), delay);
	}
	
	/**
	 * Creates a new random agent with a random starting seed and the {@link
	 * #DEFAULT_DELAY default delay} time.
	 * 
	 * @param url the URL of the server to connect to
	 * @param port the network port to connect to the server on
	 */
	public StoryGraphAgent(String url, int port) {
		this(url, port, new Random(), DEFAULT_DELAY);
	}
	
	/**
	 * Creates a new random agent with a random starting seed and default
	 * settings.
	 */
	public StoryGraphAgent() {
		this(DEFAULT_URL, DEFAULT_PORT, new Random(), DEFAULT_DELAY);
	}
	
	@Override
	public String toString() {
		return "Story Graph Agent " + id;
	}
	
	@Override
	protected void onConnect(Connect connect) {
		System.out.println(this + " has connected to the server.");
	}
	
	@Override
	protected void onStart(World world, Role role, State state) {
		System.out.println(this + " has started its session as the " + role + " in world \"" + world.getName() + "\".");
		sgv = new StoryGraphView("tutorial.zip", "tutorial-player-decision.zip", world.getActions());
	}
	
	@Override
	protected int onChoice(Status status) {
		System.out.print("My possible actions are: ");
        for (var act : status.getChoices()) {
            System.out.print(act + ", ");
        }
        System.out.println("");
		if (isProposal(status))
			return sgv.allowProposal(cleanHistory(), getRole(), getHistory().get(getHistory().size() - 1));
		Action toTake = sgv.recommendAction(cleanHistory(), getRole());
		int i = 0;
		for (;i < getChoices().size(); i++) {
			Turn candidate = getChoices().get(i);
			if (candidate.action.equals(toTake)) break;
		}
		System.out.println();
		return i;
	}

	private List<Turn> cleanHistory() {
		List<Turn> clean = new ArrayList<Turn>();
		for (var t : getHistory()) {
			System.out.println(t);
			if (t.type.equals(Turn.Type.SUCCEED)) {
				clean.add(t);
			}
		}
		return clean;
	}
	
	/**
	 * Checks whether this agent has been asked to agree to (succeed) or disagree
	 * with (fail) a proposed joint action.
	 * 
	 * @param status the current status passed to this agent by the server
	 * @return true if the agent is currently responding to a proposal, or false
	 * if this is a normal turn
	 */
	private static final boolean isProposal(Status status) {
		List<Turn> choices = status.getChoices();
		return choices.size() == 2 &&
			choices.get(0).type == Turn.Type.SUCCEED &&
			choices.get(1).type == Turn.Type.FAIL;
	}
	
	@Override
	protected void onEnd(Ending ending) throws Exception {
		System.out.println(this + " has ended its session: \"" + ending.getDescription() + "\".");
	}
	
	@Override
	protected void onStop(String message) throws Exception {
		System.out.println(this + " has stopped" + (message == null ? "" : " because \"" + message + "\"") + ".");
	}
	
	@Override
	protected void onDisconnect() throws Exception {
		System.out.println(this + " has disconnected.");
	}
}