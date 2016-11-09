package manager;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Central extends Agent {
	
	private int taxis = 0;
	
	// message template for requests
	private MessageTemplate request = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

	
	// initialize central
	protected void setup(){
		
		System.out.println("[CENTRAL] : CREATED");
		System.out.println("[CENTRAL] : THERE ARE NOW " + taxis + " TAXIS");
		
		// process requests
		addBehaviour(new CyclicBehaviour(){
			
			public void action(){
				
				// receive message from agent
				ACLMessage message = myAgent.receive(request);
				
				if(message != null) {
					
					String content = message.getContent(); // get request content
					
					if(content.contains("new_taxi")) {	// PROCESS 'NEW TAXI REQUEST'
						
						System.out.println("[CENTRAL] : RECEIVED 'NEW TAXI MSG' ->" + " THERE ARE NOW " + ++taxis + " TAXIS");
			
						// TODO: process taxi id and assign it to data structure
						
						// confirm joining with taxi
						ACLMessage reply = message.createReply();
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setContent("welcome");
						myAgent.send(reply);
						
					} else if(content.contains("ask_taxi")) {	// PROCESS 'ASK TAXI' REQUEST
						
						System.out.println("[CENTRAL] : RECEIVED 'ASK TAXI MSG'");
						
						// process request content
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
						
						// TODO: send message to every taxi 
						
						// TODO: process taxi responses and choose taxi
												
						// acknowledge passenger with id of chosen taxi
						int taxiId = 1; // sample id while data structures and selecting algorithm are not implemented
						
						ACLMessage reply = message.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("your_taxi;" + Integer.toString(taxiId));
						myAgent.send(reply);
						
						System.out.println("[CENTRAL] : SENT 'TAXI CONFIRMATION MSG' TO PASSENGER " + message.getSender().getLocalName());
					} 
				}
			}
		});
	}
	
	// terminate central
	protected void takeDown(){ System.out.println("[CENTRAL] : TERMINATED"); }
}
