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
	
	// template for job proposal
	private MessageTemplate proposal = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
	
	// initialize taxi
	protected void setup()
	{
		
		System.out.println("[TAXI_" + ++id + "] : CREATED");

		// join the company
		addBehaviour(new OneShotBehaviour(this){
			public void action() {
				
				// send a request message to the central
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.setContent("new_taxi;" + id);
				message.addReceiver(getAID("Central"));
				send(message);
				
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
				ACLMessage message = myAgent.receive(proposal);
				if(message != null) {
					System.out.println("[TAXI_" + id + "] : RECEIVED JOB PROPOSAL FROM CENTRAL WITH CONTENT " + message.getContent());
					
					// process request content
					String content = message.getContent();
					String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
					String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
					int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
					dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
					srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
					
					if (passengers + weight <= CAPACITY) { // if taxi has enough capacity to transport the passengers
						ACLMessage taxi_proposal= new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						taxi_proposal.setContent("taxi_response;" + location);
						taxi_proposal.addReceiver(getAID("Central"));
						send(taxi_proposal);
						System.out.println("[TAXI_" + id + "] : ACCEPT PROPOSAL FROM CENTRAL");
					} else {
						ACLMessage taxi_proposal= new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						taxi_proposal.setContent("taxi_response_no");
						taxi_proposal.addReceiver(getAID("Central"));
						send(taxi_proposal);
						System.out.println("[TAXI_" + id + "] : REJECTED PROPOSAL FROM CENTRAL");
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
