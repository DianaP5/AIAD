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
	private List<RepastEdge<Object> > path = new ArrayList<RepastEdge<Object> >(); // the path to follow
	
	// Class constructor
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation, ArrayList<Location> locations) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
		this.locations = locations;
	}
	
	// Prints the path to follow
	public void dumpPath(){
		int i = 0;
		for(; i < path.size(); i++){
			System.out.print(path.get(i).getSource() + " -> ");
		}
		System.out.println(path.get(--i).getTarget());
	}
	
	// Return the last stop
	public Location lastStop() {
		if(!path.isEmpty())
			return (Location) path.get(path.size()).getTarget();
		return null;
			
	}
	
	// Return the sum of the edges still to be traveled
	public double distanceStillToTravel() {
		double sum = 0.0;
		for(int i = 0; i < path.size(); i++){
			sum += path.get(i).getWeight();
		}
		return sum;
	}
	
	// Tells if the taxi is empty
	public boolean isEmpty() { return (passengers == 0); }
	
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

			ArrayList<String> data = new ArrayList<String>();
			
			public void action() {
				
				ACLMessage message = myAgent.receive(); // Receive messages from central
				if(message != null) {	
					
					// Process job proposals
					if (message.getPerformative() == ACLMessage.PROPOSE) {
						
						// Process request content
						data = Utilities.processProposal(message.getContent());
						int weight = Integer.parseInt(data.get(2));
						
						// If there is still capacity to transport the passengers...	
						if (passengers + weight <= Utilities.TAXI_CAPACITY) 
							Utilities.sendMessage(ACLMessage.ACCEPT_PROPOSAL, central, "taxi_response;" + currentLocation, myAgent);
						else	
							Utilities.sendMessage(ACLMessage.REJECT_PROPOSAL, central, "taxi_response_no", myAgent);
						
					// Acknowledge jobs
					} else if (message.getPerformative() == ACLMessage.INFORM) {
						
						// Calculate shortest path needed to fulfill service
						ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);	
						
						if(isEmpty()) {
							path.addAll(shortestPath.getPath(getLocation(currentLocation), getLocation(data.get(0))));
						} else {
							path.addAll(shortestPath.getPath(lastStop(), getLocation(data.get(0))));
						}
						
						path.addAll(shortestPath.getPath(getLocation(data.get(0)), getLocation(data.get(1))));
						
						// Move taxi accordingly
						for(int i = 0 ; i < path.size(); i++) {
							move((Location) path.get(i).getTarget());
							// When passenger source location is reached...
							AID passengerAID = new AID();
							passengerAID.setName(message.getContent());
							if(currentLocation.equals(data.get(0))) {
								Utilities.sendMessage(ACLMessage.INFORM, passengerAID, "Hello", myAgent);
							} else if(currentLocation.equals(data.get(1))) {
								Utilities.sendMessage(ACLMessage.CONFIRM, passengerAID, Double.toString(path.get(i).getWeight()), myAgent);
							} else {
								Utilities.sendMessage(ACLMessage.SUBSCRIBE, passengerAID, Double.toString(path.get(i).getWeight()), myAgent);
							}
						}
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