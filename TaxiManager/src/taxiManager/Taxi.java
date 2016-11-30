package taxiManager;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class Taxi extends Agent {

	private ContinuousSpace<Object> space;
	private Network<Object> network;
	private String initialLocation;
	private String currentLocation;
	private AID resultsCollector;
	private final int CAPACITY = 4; // fixed maximum capacity
	private int passengers = 0;	// actual number of passenger inside the taxi
	
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
	}
	
	private MessageTemplate join = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
			MessageTemplate.MatchContent("welcome")
	);
	
	private void sendMessage(int performative, AID receiver, String content){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(receiver);
		send(message);
	}

	public void move(Location dst) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}

	public String getInitialLocation() {
		return initialLocation;
	}

	@Override
	public void setup() {
  		
		AID central = new AID();
		central.setName("Central@Taxi Manager");
  		
		System.out.println("Agent "+getName()+" found central:");

		sendMessage(ACLMessage.REQUEST, central,"new_taxi");
		
		// process job proposals
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				// receive message from agent
				ACLMessage message = myAgent.receive();
				if(message != null) {
					
					 // process job proposals
					if (message.getPerformative() == ACLMessage.PROPOSE) {
						
						// process request content
						String content = message.getContent();
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
						
						if (passengers + weight <= CAPACITY) { // if taxi has enough capacity to transport the passengers	
							sendMessage(ACLMessage.ACCEPT_PROPOSAL, central, "taxi_response;" + initialLocation);
						} else {	
							sendMessage(ACLMessage.REJECT_PROPOSAL, central, "taxi_response_no");
						}
					} else if (message.getPerformative() == ACLMessage.INFORM){
						
						// process request content
						String content = message.getContent();
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
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