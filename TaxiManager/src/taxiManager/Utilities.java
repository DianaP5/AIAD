package taxiManager;

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
	public static String[] processServiceRequest(String content) {return processProposal(content);}
	public static String[] processProposal(String content){
		String[] data = new String[3];
		data[0] = content.substring(content.indexOf(';') + 1, content.length());
		data[1] = data[0].substring(data[0].indexOf(';') + 1, data[0].length());
		data[2] = data[1].substring(data[1].indexOf(';') + 1); 
		data[1] = data[1].substring(0, data[1].indexOf(';')); 
		data[0] = data[0].substring(0, data[0].indexOf(';')); 
		System.out.println(data[0]);
		System.out.println(data[1]);
		System.out.println(data[2]);
		return data;
	}
	
	// GLOBAL VARIABLES
	public final static double MAXIMUM_DISTANCE = Double.POSITIVE_INFINITY;
	
	// SIMULATION VARIABLES
	public final static int NUMBER_TAXIS = 1;	// initial number of taxis in the company
	public final static int TAXI_CAPACITY = 4;  // maximum capacity of taxis
	public final static String CENTRAL_AID = "Central@Taxi Manager";	// central fixed AID
	

}