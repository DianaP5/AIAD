package taxiManager;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import sajas.core.Agent;

public class Utilities {

	// sends a message to an agent
	public static void sendMessage(int performative, AID central, String content, Agent agent){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(agent.getAID());
		agent.send(message);
	}

}
