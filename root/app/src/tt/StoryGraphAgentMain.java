package tt;

import edu.uky.cs.nil.tt.Arguments;

/**
 * The main entry point for this Tandem Tales agent. This class includes the
 * {@link #main(String[]) main method} to launch the agent factory.
 */
public class StoryGraphAgentMain {
	
	/** The name of this agent */
	public static final String NAME = "Tandem Tales Story Graph Agent (Java)";
	
	/** The version number of this agent */
	public static final String VERSION = "0.9.0";
	
	/** The authors of this agent */
	public static final String AUTHORS = "Gage Birchmeier";
	
	/** The full title of this agent */
	public static final String TITLE = NAME + " v" + VERSION + " by " + AUTHORS;
	
	/** The key used to print the usage instructions */
	public static final String HELP_KEY = "help";
	
	/** The key used to specify the server to connect to */
	public static final String URL_KEY = "url";
	
	/** The key used to specify the network port to connect to the server on */
	public static final String PORT_KEY = "port";
	
	/** Instructions for how to use this agent */
	public static final String USAGE = 
		rpad("-" + HELP_KEY) + "Print usage information.\n" +
		rpad("-" + URL_KEY + " <string>") + "Specifies the URL of the server (defaults to \"" + StoryGraphAgent.DEFAULT_URL + "\").\n" +
		rpad("-" + PORT_KEY + " <number>") + "Specifies the network port of the server (defaults to " + StoryGraphAgent.DEFAULT_PORT + ").";
	
	private static final String rpad(String string) {
		return String.format("%-16s", string);
	}
	
	/**
	 * Configure and run the agent factory based on Java arguments passed from
	 * the terminal.
	 * 
	 * @param args the arguments passed to this executable
	 * @throws Exception if a problem occurs during the factory setup or while
	 * it is running
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(TITLE);
		// Help
		Arguments arguments = new Arguments(args);
		if(arguments.contains(HELP_KEY)) {
			System.out.println(USAGE);
			return;
		}
		// Network Settings
		String url = arguments.getValue(URL_KEY, StoryGraphAgent.DEFAULT_URL);
		if(url == null)
			url = StoryGraphAgent.DEFAULT_URL;
		int port;
		if(arguments.contains(PORT_KEY))
			port = Integer.parseInt(arguments.getValue(PORT_KEY, Integer.toString(StoryGraphAgent.DEFAULT_PORT)));
		else
			port = StoryGraphAgent.DEFAULT_PORT;
		// Check for unused arguments.
		arguments.checkUnused();
		// Create Agent Factory
		try(StoryGraphAgentFactory factory = new StoryGraphAgentFactory(url, port)) {
			factory.call();
		}
	}
	
	private StoryGraphAgentMain() {
		// This object cannot be instantiated.
	}
}