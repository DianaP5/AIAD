package taxiManager;

import java.util.ArrayList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import sajas.core.Agent;

public class Utilities {

	// Send a message to an agent
	public static void sendMessage(int performative, AID receiver, String content, Agent agent){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(receiver);
		agent.send(message);
	}
	
	// Process the contents of a job proposal / service request
	public static ArrayList<String> processServiceRequest(String content) {return processProposal(content);}
	public static ArrayList<String> processProposal(String content){
		ArrayList<String> data = new ArrayList<String>();
		String [] split = content.split(";");
		data.add(split[1]);
		data.add(split[2]);
		data.add(split[3]);
		data.add(split[4]);
		return data;
	}
	
	// GLOBAL VARIABLES
	public final static double MAXIMUM_DISTANCE = Double.POSITIVE_INFINITY;
	
	// SIMULATION VARIABLES
	public static int NUMBER_TAXIS = 2;
	public static int TAXI_CAPACITY = 4;
	public final static String CENTRAL_AID = "Central@Taxi Manager";	// central fixed AID
	public final static int FIRST_SERVED = 1;
	public final static int SHORTEST_TIME = 2;
	public final static int strategy = SHORTEST_TIME;
	
}