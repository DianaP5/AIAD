package taxiManager;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
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
	private ArrayList<Location> locations;
	private List<RepastEdge<Object> > path; // the path to follow
	
	// Class constructor
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation, ArrayList<Location> locations) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
		this.locations = locations;
	}
	
	// Returns a location given its name
	private Location getLocation(String locationName){
		
		for(int i = 0; i < locations.size(); i++) {
			if(locations.get(i).getLocationName().equals(locationName))
				return locations.get(i);
		}
		return null;
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
						ArrayList<String> data = Utilities.processProposal(message.getContent());
						// TODO: USE THE OTHER VARIABLES INTO SOMETHING USEFUL
						int weight = Integer.parseInt(data.get(2));
						
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
						
						path = shortestPath.getPath(getLocation(currentLocation),getLocation(destination));
						System.out.println("I will follow the path : " + path.toString());
						for(int i = 0 ; i < path.size(); i++){
							move((Location) path.get(i).getTarget());
						}
					} 
				} 
			}
		});
	}
	
	public void moveTowards (RepastEdge<Object> edge) {
		Location loc = (Location) edge.getTarget();
		NdPoint myPoint, nextStop;
		double w,g = 0;
		w=edge.getWeight();
		System.out.println(loc);
		while(g<w){
			myPoint = space.getLocation(this);
			nextStop = new NdPoint(loc.x, loc.y);
			double angle = SpatialMath.calcAngleFor2DMovement(space, nextStop, myPoint);
			System.out.println(angle);
			space.moveByVector(this, edge.getWeight(), 0.785398163, 0);
			g++;
		}
	}

	
	protected void takeDown()
	{
		System.out.println("Removed taxi");
	}

}