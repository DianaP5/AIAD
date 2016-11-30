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

import java.util.ArrayList;
import java.util.Random;

public class Central extends Agent
{

	private ArrayList<AID> companyTaxis = new ArrayList<AID>();
	private final int NUMBER_TAXIS = 5;
	private int taxis = NUMBER_TAXIS;
	private ArrayList<Location> locations = new ArrayList<Location>();	
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	
	private int passNum = 0;	
	// message template for requests
	private MessageTemplate request = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	
	// reply to a received message
	private void replyMessage(ACLMessage message, int performative, String content){
		ACLMessage reply = message.createReply();
		reply.setPerformative(performative);
		reply.setContent(content);
		send(reply);
	}
	
	// returns a location given its name
	private Location getLocation(String locationName){
		Location location = null;
		for(int i = 0; i < locations.size(); i++) {
			location = locations.get(i);
			if(location.getLocationName() == locationName)
				 break;
		}
		return location;
	}
	
	public Central(ArrayList<Location> locations,ContinuousSpace<Object> space, Network<Object> network)
	{
		this.locations=locations;
		this.network = network;
		this.space = space;
	}
	
	@ScheduledMethod(start = 100, interval = 100)
	public void addPassenger()
	{
		Random r = new Random();
		String moveTo = locations.get(r.nextInt(locations.size())).getLocationName();
		Passenger pass = new Passenger(2,moveTo,"Aveiro");
		try {
			this.getContainerController().acceptNewAgent("Passenger" + passNum++, pass).start();
			pass.move(getLocation(moveTo), space);
			System.out.println("NASCI");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	
	// initialize central
	protected void setup() {
		
		System.out.println("[CENTRAL] : CREATED");
		Random r = new Random();
		String moveTo = "";
		
		for (int i = 0; i < NUMBER_TAXIS; i++) {
			moveTo = locations.get(r.nextInt(locations.size())).getLocationName();
			Taxi taxi = new Taxi(space,network,moveTo);
			AID taxiAID = new AID();
			taxiAID.setName("Taxi"+i+"@Taxi Manager");
			System.out.println("TAXI AID: " + taxiAID);
			companyTaxis.add(taxiAID);
			try {
				this.getContainerController().acceptNewAgent("Taxi" + i, taxi).start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			taxi.move(getLocation(moveTo));
		}
		System.out.println("PRE"+companyTaxis);
		// process requests
		addBehaviour(new CyclicBehaviour() {
			
			public void action() {
				// receive message from agent
				ACLMessage message = myAgent.receive(request);

				if (message != null) {

					String content = message.getContent(); // get request
															// content
					
					 if (content.contains("ask_taxi")) { // PROCESS 'ASK
																// TAXI' REQUEST

						System.out.println("[CENTRAL] : RECEIVED 'ASK TAXI MSG'");

						// process request content
						String srcPoint = content.substring(content.indexOf(';') + 1, content.length());
						String dstPoint = srcPoint.substring(srcPoint.indexOf(';') + 1, srcPoint.length());
						int weight = Integer.parseInt(dstPoint.substring(dstPoint.indexOf(';') + 1)); // System.out.println(weight);
						dstPoint = dstPoint.substring(0, dstPoint.indexOf(';')); // System.out.println(dstPoint);
						srcPoint = srcPoint.substring(0, srcPoint.indexOf(';')); // System.out.println(srcPoint);
						// send message to every taxi
						
						for (int i = 0; i < companyTaxis.size(); i++) {
							Utilities.sendMessage(ACLMessage.PROPOSE, companyTaxis.get(i), "taxi_proposal;" + srcPoint + ";" + dstPoint + ";" + weight, myAgent);
							System.out.println("[CENTRAL] : SENT MESSAGE TO " + companyTaxis.get(i).getName());
							
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

						// TODO: inform selected taxi
						
					} else {
						System.out.println("[CENTRAL] : "+content);
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
