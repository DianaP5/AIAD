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
			
						// answer taxi with message
						ACLMessage reply = message.createReply();
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setContent("welcome");
						myAgent.send(reply);
						
					} else if(content.contains("ask_taxi")) {	// PROCESS 'ASK TAXI' REQUEST
						
						System.out.println("[CENTRAL] : RECEIVED 'ASK TAXI MSG'");
						
						// TODO process request content
						//String srcPoint;
						//String dstPoint;
						//int weigth;
						
						// TODO: send message to every taxi 
						
						// TODO: process requests
						
						// TODO: acknowledge passenger
						
					} 
				}
			}
		});
	}
	
	// terminate central
	protected void takeDown(){ System.out.println("[CENTRAL] : TERMINATED"); }
}
