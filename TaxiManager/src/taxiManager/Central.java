package taxiManager;

import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;

public class Central extends Agent
{

	private ArrayList<String> companyTaxis = new ArrayList<String>();
	private int taxis = 0;

	// message template for requests
	private MessageTemplate request = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	
	// sends a message to an agent
	private void sendMessage(int performative, String receiver, String content){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		//message.addReceiver(getAID(receiver));
		send(message);
	}
	
	// reply to a received message
	private void replyMessage(ACLMessage message, int performative, String content){
		ACLMessage reply = message.createReply();
		reply.setPerformative(performative);
		reply.setContent(content);
		send(reply);
	}
	
	// initialize central
	protected void setup() {

		System.out.println("[CENTRAL] : CREATED");
		System.out.println("[CENTRAL] : THERE ARE NOW " + taxis + " TAXIS");

		// process requests
		addBehaviour(new CyclicBehaviour() {

			public void action() {

				// receive message from agent
				ACLMessage message = myAgent.receive(request);

				if (message != null) {

					String content = message.getContent(); // get request
															// content

					if (content.contains("new_taxi")) { // PROCESS 'NEW TAXI
														// REQUEST'

						// add taxi to data structure
						System.out.println("[CENTRAL] : RECEIVED 'NEW TAXI MSG' FROM TAXI");
						companyTaxis.add(message.getSender().getLocalName());

						// confirm joining with taxi
						replyMessage(message, ACLMessage.ACCEPT_PROPOSAL, "welcome");
						

					} else if (content.contains("ask_taxi")) { // PROCESS 'ASK
																// TAXI' REQUEST

						System.out.println("[CENTRAL] : RECEIVED 'ASK TAXI MSG'");

						// process request content
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);

						// block while the company has no taxis
						while (companyTaxis.size() == 0) {}

						// send message to every taxi

						for (int i = 0; i < companyTaxis.size(); i++) {
							sendMessage(ACLMessage.PROPOSE, companyTaxis.get(i), "taxi_proposal;" + srcPoint + ";" + dstPoint + ";" + weight);
							System.out.println("[CENTRAL] : SENT MESSAGE TO " + companyTaxis.get(i));
						}

						int answers = 0;
						ArrayList<Pair> accepted_taxis = new ArrayList<Pair>();
						while (answers < companyTaxis.size()) {
							ACLMessage taxi_reply = blockingReceive();
							if (taxi_reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
								Pair pair = new Pair(taxi_reply.getSender().getLocalName(),
										taxi_reply.getContent().substring(0, taxi_reply.getContent().indexOf(";")));
								accepted_taxis.add(pair);
								
							}
							System.out.println("[CENTRAL] : RECEIVED MESSAGE FROM TAXI");
							answers++;
						}

						// TODO: process responses and choose taxi

						// acknowledge passenger with id of chosen taxi
						int taxiId = 1; // sample id while data structures and
										// selecting algorithm are not
										// implemented
						
						// inform passenger
						replyMessage(message, ACLMessage.INFORM, "your_taxi;" + Integer.toString(taxiId));
						System.out.println("[CENTRAL] : SENT 'TAXI CONFIRMATION MSG' TO PASSENGER " + message.getSender().getLocalName());

						// inform taxi
						sendMessage(ACLMessage.INFORM, "t1", "get_passenger;" + srcPoint + ";" + dstPoint + ";" + weight);
						
					} else {
						System.out.println("[CENTRAL] : RECEIVED UNKNOWN MESSAGE TYPE");
					}
				}
			}
		});
	}

	// terminate central
	protected void takeDown() {
		System.out.println("[CENTRAL] : TERMINATED");
	}
}
