package taxiManager;

import jade.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Central extends Agent
{
	
	// WORLD FIELDS
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	private int passNum = 0; // passenger number
	
	// CENTRAL FIELDS
	private ArrayList<Taxi> companyTaxis = new ArrayList<Taxi>();	// company taxis
	private ArrayList<Location> locations = new ArrayList<Location>();	// company locations
	private int taxis = Utilities.NUMBER_TAXIS;	// current number of taxis in the company;
	
	// Class constructor
	public Central(ArrayList<Location> locations,ContinuousSpace<Object> space, Network<Object> network)
	{
		this.locations = locations;
		this.network = network;
		this.space = space;
	}
		
	// Returns a location given its name
	private Location getLocation(String locationName){
		Location location = null;
		for(int i = 0; i < locations.size(); i++) {
			location = locations.get(i);
			if(location.getLocationName() == locationName)
				 break;
		}
		return location;
	}
	
	// Creates passengers in random places
	@ScheduledMethod(start = 10, interval = 10)
	public void addPassenger()
	{
		Random r = new Random();
		String src, dst; 
		src = locations.get(r.nextInt(locations.size())).getLocationName();
		do { dst = locations.get(r.nextInt(locations.size())).getLocationName(); } while(src.equals(dst));
		Passenger pass = new Passenger(2,"a",dst);
		try {
			this.getContainerController().acceptNewAgent("Passenger" + passNum++, pass).start();
			pass.move(getLocation("a"), space);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	
	// Prints a network path [DEBUG]
	private void printPath(List<RepastEdge<Object> > path){
		int i = 0;
		for(; i < path.size(); i++){
			System.out.print(path.get(i).getSource().toString() + " (" + path.get(i).getWeight() + ") ");
		}
		System.out.print("\n");
	}
	
	// Initializes central
	@Override
	protected void setup() {
		
		System.out.print("Central Initialized | ");
		
		// Initialize the company taxis
		Random r = new Random();
		String moveTo = "";
		for (int i = 0; i < taxis; i++) {
			moveTo = "f"; // locations.get(r.nextInt(locations.size())).getLocationName(); 
			Taxi taxi = new Taxi(space,network,moveTo);
			AID taxiAID = new AID();
			taxiAID.setName("Taxi"+i+"@Taxi Manager");
			taxi.setAID(taxiAID);
			companyTaxis.add(taxi);
			try {
				this.getContainerController().acceptNewAgent("Taxi" + i, taxi).start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			taxi.move(getLocation(moveTo));
		}

		System.out.println("Taxis Initialized");
		
		// Process requests
		addBehaviour(new CyclicBehaviour() {
			
			int answers = 0;
			ArrayList<Pair> accepted_taxis = new ArrayList<Pair>();
			String data[];
			
			public void action() {

				ACLMessage message = myAgent.receive();	// Receives requests
				
				if (message != null) {

					String content = message.getContent();
					
					 if (content.contains("ask_taxi")) { // Process request for taxi

						System.out.println("[CENTRAL] : RECEIVED 'ASK TAXI MSG'");

						// Process request content and send job proposal to every company taxi
						data = Utilities.processServiceRequest(content);
						for (int i = 0; i < companyTaxis.size(); i++) {
							Utilities.sendMessage(ACLMessage.PROPOSE, companyTaxis.get(i).getAID(), "taxi_proposal;" + data[0] + ";" + data[1] + ";" + data[2], myAgent);
							System.out.println("[CENTRAL] : SENT MESSAGE TO " + companyTaxis.get(i).getName());
						}
						
					 } else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && answers < companyTaxis.size()) {
						 Pair pair = new Pair(message.getSender().getLocalName(), message.getContent().substring(0, message.getContent().indexOf(";")));
						 accepted_taxis.add(pair);
						 ++answers;
					 } else if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL && answers < companyTaxis.size()) {
						 ++answers;
					 } else {
						System.out.println("[CENTRAL] : "+content);
					}
					 
				} else if (answers >= companyTaxis.size()) {
					
					// TODO: check which of the taxis is closer and choose that taxi
					ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
					double minimumDistance = Utilities.MAXIMUM_DISTANCE;
					double distance;
					Taxi choosenTaxi = null;
					
					for (int i = 0; i < taxis; i++) {
						distance = shortestPath.getPathLength(getLocation(companyTaxis.get(i).getCurrentLocation()), getLocation(data[0]));
						System.out.println("Path length of " + companyTaxis.get(i).getLocalName() + " is " + distance);
						System.out.print("Path is "); printPath(shortestPath.getPath(getLocation(companyTaxis.get(i).getCurrentLocation()), getLocation(data[0])));
						if (distance < minimumDistance){
							minimumDistance = distance;
							choosenTaxi = companyTaxis.get(i);
						}
					}
					
					System.out.println("THE TAXI TO TAKE YOU WILL BE : " + choosenTaxi.getLocalName());						
					answers = 0;
					
					// inform selected taxi and passenger
					Utilities.sendMessage(ACLMessage.INFORM, choosenTaxi.getAID(), "ack;" + data[0], myAgent);
					
				}
			}
		});
	}

	// terminate central
	protected void takeDown() {
		System.out.println("[CENTRAL] : TERMINATED");
	}
}