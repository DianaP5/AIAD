package taxiManager;

import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;

public class Passenger extends Agent
{
	// PASSENGER FIELDS
	private int weight;			// how much 'capacity' does the passenger need
	private String srcPoint;	// where the passenger initially is
	private String dstPoint;	// where the passenger wants to go
	private double cost = 0;		// how much will the passenger pay
	private double tolerance;
	
	// Class constructor
	public Passenger(int weight, String srcPoint, String dstPoint)
	{
		this.weight = weight;
		this.srcPoint = srcPoint;
		this.dstPoint = dstPoint;
		this.tolerance = 0;
	}
	
	public Passenger(int weight, String srcPoint, String dstPoint, double tolerance)
	{
		this.weight = weight;
		this.srcPoint = srcPoint;
		this.dstPoint = dstPoint;
		this.tolerance = tolerance;
	}
	
	
	public String getSrcPoint() {
		return srcPoint;
	}

	public String getDstPoint() {
		return dstPoint;
	}

	public double getTolerance() {
		return tolerance;
	}
	
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
				System.out.println("[PASSENGER] : people are waiting on " + srcPoint + " to go to " + dstPoint + " with delta " + tolerance);
			else
				System.out.println("[PASSENGER] : I am waiting on " + srcPoint + " to go to " + dstPoint + " with delta " + tolerance);
			
			
			// Send a 'request for taxi'
			addBehaviour(new OneShotBehaviour(){
				public void action() {
					
					// Send a request to the central
					Utilities.sendMessage(ACLMessage.REQUEST, central, "ask_taxi;" +  srcPoint + ";" + dstPoint + ";" + weight + ";" + tolerance, myAgent);
	
				}		
			});
			
			// Process taxi messages
			addBehaviour(new CyclicBehaviour(){
				boolean received = false;
				public void action() {
					// Receive confirmation from taxi
					ACLMessage message = myAgent.receive();	
					if (message != null) {
						if(message.getPerformative() == ACLMessage.INFORM && !received){
							System.out.println("[PASSENGER " + myAgent.getLocalName() + "] Received : " + message.getContent());
							Context<Object> context = ContextUtils.getContext(myAgent);
							context.remove(myAgent);
							received = true;
						} else if(message.getPerformative() == ACLMessage.CONFIRM ){
							cost += Double.parseDouble(message.getContent());
							System.out.println("I paied a total of " + cost);
							myAgent.doDelete();
							received = false;
						} else if(message.getPerformative() == ACLMessage.SUBSCRIBE){
							cost += Double.parseDouble(message.getContent());
						}
					}
				}
			});
			
	}
	
	// take down passenger
	protected void takeDown() {}
}