package taxiManager;

import java.util.ArrayList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
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
		return data;
	}
	
	// GLOBAL VARIABLES
	public final static double MAXIMUM_DISTANCE = 100000.0;
	
	// SIMULATION VARIABLES
	public final static int NUMBER_TAXIS = 5;	// initial number of taxis in the company
	public final static int TAXI_CAPACITY = 4;  // maximum capacity of taxis
	public final static String CENTRAL_AID = "Central@Taxi Manager";	// central fixed AID
	

}