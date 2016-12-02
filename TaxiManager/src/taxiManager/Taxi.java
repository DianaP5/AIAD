package taxiManager;

import java.util.List;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class Taxi extends Agent {

	// WORLD FIELDS
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	
	// TAXI FIELDS
	private String initialLocation;	// initial location
	private String currentLocation;	// current location
	private int passengers = 0;	// current number of passenger inside the taxi
	private List<RepastEdge<Object> > path; // the path to follow
	
	// Class constructor
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
	}
	
	// Move to specific location 
	public void move(Location dst) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
		currentLocation = dst.getLocationName();
	}

	// Get current location
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	// Initialize agent
	@Override
	public void setup() {
		
		System.out.println(getName() + " starting service in " + initialLocation);
		
		// Prepare central AID for communication purposes
		AID central = new AID(); 
		central.setName(Utilities.CENTRAL_AID);
		
		// Process job proposals
		addBehaviour(new CyclicBehaviour(this) {

			public void action() {

				ACLMessage message = myAgent.receive(); // Receive messages from central
				if(message != null) {	
					
					// Process job proposals
					if (message.getPerformative() == ACLMessage.PROPOSE) {
						
						// Process request content
						String [] data = Utilities.processProposal(message.getContent());
						// TODO: USE THE OTHER VARIABLES INTO SOMETHING USEFUL
						int weight = Integer.parseInt(data[2]);
						
						// If there is still capacity to transport the passengers...	
						if (passengers + weight <= Utilities.TAXI_CAPACITY) 
							Utilities.sendMessage(ACLMessage.ACCEPT_PROPOSAL, central, "taxi_response;" + currentLocation, myAgent);
						else	
							Utilities.sendMessage(ACLMessage.REJECT_PROPOSAL, central, "taxi_response_no", myAgent);
						
					// Acknowledge jobs
					} else if (message.getPerformative() == ACLMessage.INFORM){
	
						// Process request content
						String content = message.getContent();
						String destination = content.substring(content.indexOf(';') + 1, content.length());
						System.out.println("I'm the choosen one! I must go to " + destination);
						
						// Calculate shortest path to passenger location
						ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
						
						System.out.println(space.getLocation(currentLocation) + " " + space.getLocation(destination));
						//path = shortestPath.getPath(space.getLocation(currentLocation), space.getLocation(destination));
						//System.out.println("I will follow the path : " + path.toString());
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