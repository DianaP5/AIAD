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
import sajas.core.Runtime;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;

class MoveAgentBehaviour extends TickerBehaviour {
	private ArrayList<String> data;
	private AID passengerAID;
	ShortestPath<Object> shortestPath;
	private double weightAux = 0;
	private boolean aux = true;

	public MoveAgentBehaviour(Agent a, long step, ArrayList<String> data) {
		super(a, step);
		this.data = data;
		this.passengerAID = new AID();
		this.passengerAID.setName(data.get(4));
		this.shortestPath = new ShortestPath<Object>(((Taxi) a).network);
	}

	@Override
	protected void onTick() {
		if (((Taxi) myAgent).currentLocation.equals(data.get(0))) {
			System.out.println("MANDEI MENSAGEM DE OLÁ");
			Utilities.sendMessage(ACLMessage.INFORM, passengerAID, "Hello", myAgent);
			((Taxi) myAgent).move((Location) ((Taxi) myAgent).getPath().get(0).getTarget());
			weightAux = ((Taxi) myAgent).getPath().get(0).getWeight();
			((Taxi) myAgent).getPath().remove(0);
		} else {
			if (((Taxi) myAgent).currentLocation.equals(((Taxi) myAgent).lessTolerantPassenger().getDstPoint())) {
				System.out.println("MANDEI MENSAGEM DE ADEUS");
				Utilities.sendMessage(ACLMessage.CONFIRM, ((Taxi) myAgent).lessTolerantPassenger().getAID(),
						Double.toString(weightAux / ((Taxi) myAgent).getPassengersList().size()), myAgent);
				((Taxi) myAgent).getPassengersList().remove(((Taxi) myAgent).lessTolerantPassenger());
				passengerAID.setName(((Taxi) myAgent).lessTolerantPassenger().getName());
				if (((Taxi) myAgent).getPassengersList().size() != 0) {
					((Taxi) myAgent).getPath().addAll(shortestPath.getPath(
							((Taxi) myAgent).getLocation(((Taxi) myAgent).currentLocation),
							((Taxi) myAgent).getLocation(((Taxi) myAgent).lessTolerantPassenger().getDstPoint())));
				} else {
					stop();
				}
			} else {
				for (int j = 0; j < ((Taxi) myAgent).getPassengersList().size(); j++) {
					Utilities.sendMessage(ACLMessage.SUBSCRIBE, ((Taxi) myAgent).getPassengersList().get(j).getAID(),
							Double.toString(weightAux / ((Taxi) myAgent).getPassengersList().size()), myAgent);
				}
				((Taxi) myAgent).move((Location) ((Taxi) myAgent).getPath().get(0).getTarget());
				weightAux = ((Taxi) myAgent).getPath().get(0).getWeight();
				((Taxi) myAgent).getPath().remove(0);
			}
		}
	}
	
	/*
	 * @Override protected void onTick() { if (!((Taxi)
	 * myAgent).getPath().isEmpty()) { if (((Taxi)
	 * myAgent).currentLocation.equals(data.get(0))) {
	 * System.out.println("MANDEI MENSAGEM DE OLÁ");
	 * Utilities.sendMessage(ACLMessage.INFORM, passengerAID, "Hello", myAgent);
	 * } else { for (int j = 0; j < ((Taxi) myAgent).getPassengersList().size();
	 * j++) { Utilities.sendMessage(ACLMessage.SUBSCRIBE, ((Taxi)
	 * myAgent).getPassengersList().get(j).getAID(), Double.toString(((Taxi)
	 * myAgent).getPath().get(0).getWeight() / ((Taxi)
	 * myAgent).getPassengersList().size()), myAgent); } } ((Taxi)
	 * myAgent).move((Location) ((Taxi) myAgent).getPath().get(0).getTarget());
	 * 
	 * if (!((Taxi) myAgent).getPath().isEmpty()) ((Taxi)
	 * myAgent).getPath().remove(0);
	 * 
	 * } else if (((Taxi) myAgent).currentLocation.equals(((Taxi)
	 * myAgent).lessTolerantPassenger().getDstPoint())) {
	 * Utilities.sendMessage(ACLMessage.CONFIRM, ((Taxi)
	 * myAgent).lessTolerantPassenger().getAID(), Double.toString(((Taxi)
	 * myAgent).getPath().get(0).getWeight() / ((Taxi)
	 * myAgent).getPassengersList().size()), myAgent);
	 * System.out.println("MANDEI MENSAGEM DE ADEUS"); ((Taxi)
	 * myAgent).getPath().clear(); ((Taxi)
	 * myAgent).getPassengersList().remove(((Taxi)
	 * myAgent).lessTolerantPassenger());
	 * 
	 * if (!((Taxi) myAgent).getPassengersList().isEmpty()) ((Taxi)
	 * myAgent).getPath() .addAll(shortestPath.getPath(((Taxi)
	 * myAgent).getLocation(((Taxi) myAgent).currentLocation), ((Taxi)
	 * myAgent).getLocation(((Taxi)
	 * myAgent).lessTolerantPassenger().getDstPoint()))); } }
	 */
}

public class Taxi extends Agent {

	// WORLD FIELDS
	private ContinuousSpace<Object> space;
	public Network<Object> network;

	// TAXI FIELDS
	private String initialLocation; // initial location
	public String currentLocation; // current location
	private int passengers = 0; // current number of passenger inside the taxi
	private ArrayList<Location> locations;
	private List<RepastEdge<Object>> path = new ArrayList<RepastEdge<Object>>();

	public List<RepastEdge<Object>> getPath() {
		return path;
	}

	private ArrayList<Passenger> passengersList = new ArrayList<Passenger>();

	// Class constructor
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation,
			ArrayList<Location> locations) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
		this.locations = locations;
	}

	public Passenger lessTolerantPassenger() {
		Passenger p = null;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < passengersList.size(); i++) {
			if (passengersList.get(i).getTolerance() <= min) {
				min = passengersList.get(i).getTolerance();
				p = passengersList.get(i);
			}
		}
		return p;
	}

	public ArrayList<Passenger> getPassengersList() {
		return passengersList;
	}

	// Prints the path to follow
	public void dumpPath() {
		int i = 0;
		for (; i < path.size(); i++) {
			System.out.print(path.get(i).getSource() + " -> ");
		}
		System.out.println(path.get(--i).getTarget());
	}

	// Return the last stop
	public Location lastStop() {
		if (!path.isEmpty())
			return (Location) path.get(path.size()).getTarget();
		return null;

	}

	// Return the sum of the edges still to be traveled
	public double distanceStillToTravel() {
		double sum = 0.0;
		for (int i = 0; i < path.size(); i++) {
			sum += path.get(i).getWeight();
		}
		return sum;
	}

	// Tells if the taxi is empty
	public boolean isEmpty() {
		return (passengersList.size() == 0);
	}

	// Returns a location given its name
	public Location getLocation(String locationName) {

		for (int i = 0; i < locations.size(); i++) {
			if (locations.get(i).getLocationName().equals(locationName))
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

				ACLMessage message = myAgent.receive(); // Receive messages from
														// central
				if (message != null) {

					// Process job proposals
					if (message.getPerformative() == ACLMessage.PROPOSE) {

						// Process request content
						data = Utilities.processProposal(message.getContent());
						int weight = Integer.parseInt(data.get(2));

						// If there is still capacity to transport the
						// passengers...
						if (passengers + weight <= Utilities.TAXI_CAPACITY)
							Utilities.sendMessage(ACLMessage.ACCEPT_PROPOSAL, central,
									"taxi_response;" + currentLocation, myAgent);
						else
							Utilities.sendMessage(ACLMessage.REJECT_PROPOSAL, central, "taxi_response_no", myAgent);

						// Acknowledge jobs
					} else if (message.getPerformative() == ACLMessage.INFORM) {
						if (Utilities.strategy == Utilities.FIRST_SERVED) {
							// Calculate shortest path needed to fulfill service
							ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);

							if (isEmpty()) {
								path.addAll(
										shortestPath.getPath(getLocation(currentLocation), getLocation(data.get(0))));
							} else {
								path.addAll(shortestPath.getPath(lastStop(), getLocation(data.get(0))));
							}

							path.addAll(shortestPath.getPath(getLocation(data.get(0)), getLocation(data.get(1))));

							// Move taxi accordingly
							for (int i = 0; i < path.size(); i++) {
								// TODO : ADD MOVE HERE
								// When passenger source location is reached...
								AID passengerAID = new AID();
								passengerAID.setName(message.getContent());
								if (currentLocation.equals(data.get(0))) {
									Utilities.sendMessage(ACLMessage.INFORM, passengerAID, "Hello", myAgent);
								} else if (currentLocation.equals(data.get(1))) {
									Utilities.sendMessage(ACLMessage.CONFIRM, passengerAID,
											Double.toString(path.get(i).getWeight()), myAgent);
								} else {
									Utilities.sendMessage(ACLMessage.SUBSCRIBE, passengerAID,
											Double.toString(path.get(i).getWeight()), myAgent);
								}
							}
						} else if (Utilities.strategy == Utilities.SHORTEST_TIME) {

							AID passengerAID = new AID();
							passengerAID.setName(message.getContent());
							ShortestPath<Object> shortestPath = new ShortestPath<Object>(network);
							data.add(message.getContent());
							if (isEmpty()) {
								passengersList.add(new Passenger(Integer.parseInt(data.get(2)), data.get(0),
										data.get(1), Double.parseDouble(data.get(3))));
								System.out.println(myAgent.getLocalName() + " : I WAS EMPTY AND ADDED A PASSENGER");
								path.addAll(
										shortestPath.getPath(getLocation(currentLocation), getLocation(data.get(0))));
								path.addAll(shortestPath.getPath(getLocation(data.get(0)), getLocation(data.get(1))));
							} else {
								passengersList.add(new Passenger(Integer.parseInt(data.get(2)), data.get(0),
										data.get(1), Double.parseDouble(data.get(3))));
								System.out.println(
										myAgent.getLocalName() + " : I WAS PARCIAL AND ADDED A PASSENGER AND HAVE "
												+ passengersList.size() + " PASSENGERS");
								path.clear();
								path.addAll(
										shortestPath.getPath(getLocation(currentLocation), getLocation(data.get(0))));
								path.addAll(shortestPath.getPath(getLocation(data.get(0)),
										getLocation(lessTolerantPassenger().getDstPoint())));
							}

							myAgent.addBehaviour(new MoveAgentBehaviour(myAgent, 5, data));

						}
					}
				}
			}
		});
	}

	protected void takeDown() {
		System.out.println("Removed taxi");
	}

}