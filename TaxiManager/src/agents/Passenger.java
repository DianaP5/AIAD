package agents;

import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.OneShotBehaviour;
import taxiManager.Location;
import utilities.Utilities;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
	
	private MessageTemplate confirm = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
	private MessageTemplate inform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	private MessageTemplate subscribe = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
	
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
			/*addBehaviour(new CyclicBehaviour(){
				public void action() {
					// Receive confirmation from taxi
					ACLMessage message = myAgent.receive(inform);	
					if (message != null) {
							System.out.println("[PASSENGER " + myAgent.getLocalName() + "] Received : " + message.getContent());
							Context<Object> context = ContextUtils.getContext(myAgent);
							context.remove(myAgent);
					} else {
						block();
					}
				}
			});*/
			
			addBehaviour(new CyclicBehaviour(){
				public void action() {
					ACLMessage message = myAgent.receive();
					if(message != null) {
						
						if(message.getPerformative() == ACLMessage.CONFIRM)
						{
							cost += Double.parseDouble(message.getContent());
							System.out.println(myAgent.getAID()+" paied a total of " + cost);
							Utilities.sendMessage(ACLMessage.AGREE,central, String.valueOf(cost), myAgent);
							myAgent.doDelete();
						}
						
						if(message.getPerformative() == ACLMessage.SUBSCRIBE)
						{
							cost += Double.parseDouble(message.getContent());
						}
						
						if(message.getPerformative() == ACLMessage.INFORM)
						{
							System.out.println("[PASSENGER " + myAgent.getLocalName() + "] Received : " + message.getContent());
							Context<Object> context = ContextUtils.getContext(myAgent);
							context.remove(myAgent);
						}
					}
					else 
					{
						block();
					}
				}
			});
	}
	
	// take down passenger
	protected void takeDown() {System.out.println(getName() + " reached his destination");}
}