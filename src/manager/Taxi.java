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
	
	// join the company reply template
	private MessageTemplate join = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			MessageTemplate.MatchContent("welcome")
	);
	
	// initialize taxi
	protected void setup()
	{
		
		System.out.println("[TAXI_" + ++id + "] : CREATED");

		// join the company
		addBehaviour(new OneShotBehaviour(this){
			public void action() {
				
				// send a request message to the central
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.setContent("new_taxi");
				message.addReceiver(getAID("Central"));
				send(message);
				
				System.out.println("[TAXI_" + id + "] : SENT 'JOINING MESSAGE' TO CENTRAL");
				
				// block until confirmation from central is not received
				ACLMessage reply = blockingReceive(join);
				System.out.println("[TAXI_" + id + "] : JOINED THE COMPANY");
			}
		});

	}
	
	protected void takeDown()
	{
		System.out.println("Removed taxi");
	}
	
}
