package agents;

import jade.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import taxiManager.Location;
import utilities.Pair;
import utilities.Utilities;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Central extends Agent {

	// WORLD FIELDS
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	private int passNum = 0; // passenger number
	private double money = 0;
	// CENTRAL FIELDS
	private ArrayList<Taxi> companyTaxis = new ArrayList<Taxi>(); // company
																	// taxis
	private ArrayList<Location> locations = new ArrayList<Location>(); // company
																		// locations
	private int taxis = Utilities.NUMBER_TAXIS; // current number of taxis in
												// the company;

	// Class constructor
	public Central(ArrayList<Location> locations, ContinuousSpace<Object> space, Network<Object> network) {
		this.locations = locations;
		this.network = network;
		this.space = space;
	}

	// Returns a location given its name
	private Location getLocation(String locationName) {

		for (int i = 0; i < locations.size(); i++) {
			if (locations.get(i).getLocationName().equals(locationName))
				return locations.get(i);
		}
		return null;
	}

	// Creates passengers in random places
	@ScheduledMethod(start = 5, interval = 10)
	public void addPassenger() {
		Random r = new Random();
		String src, dst;
		src = locations.get(r.nextInt(locations.size())).getLocationName();
		do {
			dst = locations.get(r.nextInt(locations.size())).getLocationName();
		} while (src.equals(dst));
		Passenger pass = new Passenger(2, src, dst, r.nextDouble() * 10);
		try {
			this.getContainerController().acceptNewAgent("Passenger" + passNum++, pass).start();
			pass.move(getLocation(src), space);
			System.out.println("Passenger" + passNum + " created");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	// Initializes central
	@Override
	protected void setup() {

		System.out.print("Central Initialized | ");
		// Initialize the company taxis
		Random r = new Random();
		String moveTo = "";
		for (int i = 0; i < taxis; i++) {
			moveTo = locations.get(r.nextInt(locations.size())).getLocationName();
			Taxi taxi = new Taxi(space, network, moveTo, locations);
			AID taxiAID = new AID();
			taxiAID.setName("Taxi" + i + "@Taxi Manager");
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
			ArrayList<String> data = new ArrayList<String>();
			String clientName = new String();

			public void action() {

				ACLMessage message = myAgent.receive(); // Receives requests
				if (message != null) {

					String content = message.getContent();

					if (message.getPerformative() == ACLMessage.AGREE) {
						String moneyString = message.getContent();
						double moneyAux = Double.parseDouble(moneyString);
						money += moneyAux;
						System.out.println("The company earned " + money + "€");
					}

					else if (content.contains("ask_taxi")) { // Process request
																// for taxi

						clientName = message.getSender().getName();

						// Process request content and send job proposal to
						// every company taxi
						data = Utilities.processServiceRequest(content);
						for (int i = 0; i < companyTaxis.size(); i++) {
							Utilities.sendMessage(ACLMessage.PROPOSE, companyTaxis.get(i).getAID(), "taxi_proposal;"
									+ data.get(0) + ";" + data.get(1) + ";" + data.get(2) + ";" + data.get(3), myAgent);
						}

					} else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
							&& answers < companyTaxis.size()) {
						Pair pair = new Pair(message.getSender().getLocalName(),
								message.getContent().substring(0, message.getContent().indexOf(";")));
						accepted_taxis.add(pair);
						++answers;
					} else if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL
							&& answers < companyTaxis.size()) {
						++answers;
					} else {
						System.out.println("[CENTRAL] : " + content);
					}

				} else if (answers >= companyTaxis.size()) {

					if (Utilities.strategy == Utilities.FIRST_SERVED) {

						System.out.println("\n:: FIRST-COME - FIRST-SERVED ::");
						ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
						double minimumDistance = Utilities.MAXIMUM_DISTANCE;
						double totalDistance, distanceNeeded;
						Taxi choosenTaxi = null;

						// for each company taxi...
						for (int i = 0; i < taxis; i++) {

							// calculate minimum distance needed to serve next
							// passenger
							if (companyTaxis.get(i).isEmpty()) {
								distanceNeeded = shortestPath.getPathLength(
										getLocation(companyTaxis.get(i).getCurrentLocation()),
										getLocation(data.get(0)));
							} else {
								distanceNeeded = shortestPath.getPathLength(companyTaxis.get(i).lastStop(),
										getLocation(data.get(0)));
							}
							distanceNeeded += shortestPath.getPathLength(getLocation(data.get(0)),
									getLocation(data.get(1)));
							// calculate minimum total distance needed
							totalDistance = companyTaxis.get(i).distanceStillToTravel() + distanceNeeded;

							if (totalDistance < minimumDistance) {
								minimumDistance = totalDistance;
								choosenTaxi = companyTaxis.get(i);
							}
						}

						System.out.println("THE TAXI TO TAKE YOU WILL BE : " + choosenTaxi.getLocalName());

						// inform selected taxi and passenger
						Utilities.sendMessage(ACLMessage.INFORM, choosenTaxi.getAID(), clientName, myAgent);
						answers = 0; // restart variable for future requests
					} else if (Utilities.strategy == Utilities.SHORTEST_TIME) {
						Taxi choosenTaxi = bestChoiceBetweenEmpty(data.get(0));
						if (choosenTaxi == null)
							choosenTaxi = bestChoiceBetweenPartial(data.get(0));
						if (choosenTaxi != null) {
							Utilities.sendMessage(ACLMessage.INFORM, choosenTaxi.getAID(), clientName, myAgent);
							answers = 0; // restart variable for future requests
						} else {
							System.out.println("Taxi not choosen");
							Random t = new Random();
							choosenTaxi = companyTaxis.get(t.nextInt(companyTaxis.size()));
							Utilities.sendMessage(ACLMessage.INFORM, choosenTaxi.getAID(), clientName, myAgent);
							answers = 0; // restart variable for future requests
						}
					}
				}
			}
		});
	}

	public Taxi bestChoiceBetweenEmpty(String src) {
		Taxi t = null;
		ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < companyTaxis.size(); i++) {
			if (companyTaxis.get(i).isEmpty()) {
				double distance = shortestPath.getPathLength(getLocation(companyTaxis.get(i).getCurrentLocation()),
						getLocation(src));
				if (distance <= min) {
					min = distance;
					t = companyTaxis.get(i);
				}
			}
		}
		return t;
	}

	public Taxi bestChoiceBetweenPartial(String src) {
		Taxi t = null;
		ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
		double max = 0;
		for (int i = 0; i < companyTaxis.size(); i++) {
			Passenger lessTolerant = companyTaxis.get(i).lessTolerantPassenger();
			double detour = shortestPath.getPathLength(getLocation(companyTaxis.get(i).getCurrentLocation()),
					getLocation(src))
					+ shortestPath.getPathLength(getLocation(src), getLocation(lessTolerant.getDstPoint()));
			double direct = shortestPath.getPathLength(getLocation(companyTaxis.get(i).getCurrentLocation()),
					getLocation(lessTolerant.getDstPoint()));
			if (Math.abs(detour - direct) <= lessTolerant.getTolerance()) {
				double difference = (detour - direct) - lessTolerant.getTolerance();
				if (difference >= max) {
					max = difference;
					t = companyTaxis.get(i);
				}
			}
		}
		return t;
	}

	public void txtToAgent(String agentsFile) throws IOException {
		FileReader agentFile = new FileReader(agentsFile);
		BufferedReader agentReader = new BufferedReader(agentFile);
		String loc_line;

		while ((loc_line = agentReader.readLine()) != null) {
			String[] splitter = loc_line.split(";");
			String weight = splitter[0];
			String src = splitter[1];
			String dst = splitter[2];
			Passenger pass = new Passenger(Integer.parseInt(weight), src, dst);
			try {
				this.getContainerController().acceptNewAgent("Passenger" + passNum++, pass).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

			pass.move(getLocation(src), space);
		}

		agentReader.close();
	}

	// terminate central
	protected void takeDown() {
		System.out.println("[CENTRAL] : TERMINATED");
	}
}