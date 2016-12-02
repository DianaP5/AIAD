package taxiManager;

import sajas.core.Agent;
import sajas.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import repast.simphony.space.continuous.ContinuousSpace;

public class Passenger extends Agent
{
	// PASSENGER FIELDS
	private int weight;			// how much 'capacity' does the passenger need
	private String srcPoint;	// where the passenger initially is
	private String dstPoint;	// where the passenger wants to go
	private int cost = 0;		// how much will the passenger pay
	
	// Class constructor
	public Passenger(int weight, String srcPoint, String dstPoint)
	{
		this.weight = weight;
		this.srcPoint = srcPoint;
		this.dstPoint = dstPoint;
	}
	
	// Increments the cost by a given amount
	public void increaseCost(int cost){ this.cost += cost; }
	
	// Moves the passenger to a given location
	public void move(Location dst, ContinuousSpace<Object> space) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}
	
	// Initialize passenger
	@Override
	protected void setup() {
		
		// Prepare central AID for communication purposes
		AID central = new AID();
		central.setName(Utilities.CENTRAL_AID);
	
			if (weight > 1)
				System.out.println("[PASSENGER] : " + weight + " people are waiting on " + srcPoint + " to go to " + dstPoint);
			else
				System.out.println("[PASSENGER] : I am waiting on " + srcPoint + " to go to " + dstPoint);
			
			
			// Process messages
			addBehaviour(new OneShotBehaviour(){
				public void action() {
					
					// Send a request to the central
					Utilities.sendMessage(ACLMessage.REQUEST, central, "ask_taxi;" +  srcPoint + ";" + dstPoint + ";" + weight, myAgent);
					
				}		
			});
	}
	
	// take down passenger
	protected void takeDown() { System.out.println("[PASSENGER] : DESTINY LOCATION REACHED"); }
}