package manager;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Taxi extends Agent
{
	private final int CAPACITY = 4; // fixed maximum capacity
	private int passengers = 0;	// actual number of passenger inside the taxi
	public static int id = 0;	// the taxi ID
	private String location = "Penafiel";
	
	// join the company reply template
	private MessageTemplate join = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			MessageTemplate.MatchContent("welcome")
	);
	
	// sends a message to an agent
	private void sendMessage(int performative, String receiver, String content){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(getAID(receiver));
		send(message);
	}
	
	// initialize taxi
	protected void setup()
	{
		System.out.println("[TAXI_" + ++id + "] : CREATED");

		// join the company
		addBehaviour(new OneShotBehaviour(this){
			public void action() {
				
				// send a request message to the central
				sendMessage(ACLMessage.REQUEST, "Central", "new_taxi;" + id);
				System.out.println("[TAXI_" + id + "] : SENT 'JOINING MESSAGE' TO CENTRAL");
				
				// block until confirmation from central is not received
				ACLMessage reply = blockingReceive(join);
				System.out.println("[TAXI_" + id + "] : JOINED THE COMPANY");
			}
		});

		// process job proposals
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				// receive message from agent
				ACLMessage message = myAgent.receive();
				if(message != null) {
					
					 // process job proposals
					if (message.getPerformative() == ACLMessage.PROPOSE) {
					
						System.out.println("[TAXI_" + id + "] : RECEIVED JOB PROPOSAL FROM CENTRAL WITH CONTENT " + message.getContent());
						
						// process request content
						String content = message.getContent();
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
						
						if (passengers + weight <= CAPACITY) { // if taxi has enough capacity to transport the passengers	
							sendMessage(ACLMessage.ACCEPT_PROPOSAL, "Central", "taxi_response;" + location);
							System.out.println("[TAXI_" + id + "] : ACCEPT PROPOSAL FROM CENTRAL");	
						} else {	
							sendMessage(ACLMessage.REJECT_PROPOSAL, "Central", "taxi_response_no");
							System.out.println("[TAXI_" + id + "] : REJECTED PROPOSAL FROM CENTRAL");
						}
					} else if (message.getPerformative() == ACLMessage.INFORM){
						
						// process request content
						String content = message.getContent();
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
					
						System.out.println("[TAXI_" + id + "] : GOING ON MY WAY NOW " + message.getContent());
						
					}
				} 
			}
		});
	}
	
	protected void takeDown()
	{
		System.out.println("Removed taxi");
	}
	
}
