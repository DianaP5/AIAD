package taxiManager;

import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;

public class Passenger extends Agent
{
	private int weight;		// how much 'capacity' does the passenger need
	private String srcPoint;	// where the passenger initially is
	private String dstPoint;	// where the passenger wants to go
	private int cost = 0;		// how much will the passenger pay
	private int taxiID;			// the taxi that the passenger must take
	
	// message template for central replies
	private MessageTemplate centralReply = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	
	// sets the taxi ID
	public void setTaxiID(int taxiID){ this.taxiID = taxiID; }
	
	// increments the cost by a given amount
	public void increaseCost(int cost){ this.cost += cost; }
	
	// sends a message to an agent
	private void sendMessage(int performative, AID receiver, String content){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(receiver);
		send(message);
	}
	
	public Passenger(int weight, String srcPoint, String dstPoint)
	{
		this.weight = weight;
		this.srcPoint = srcPoint;
		this.dstPoint = dstPoint;
	}
	
	public void move(Location dst, ContinuousSpace<Object> space) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}
	
	// initialize passenger
	protected void setup()
	{
		AID central = new AID();
		central.setName("Central@Taxi Manager");
		// try to parse passenger arguments
		try {
			
			System.out.println("[PASSENGER] : CREATED " + weight + " AND WAITING ON " + srcPoint + " TO GO TO " + dstPoint);
			
			// ask central for taxi
			addBehaviour(new OneShotBehaviour(){
				public void action() {
					
					// send a request message to the central
					sendMessage(ACLMessage.REQUEST, central, "ask_taxi;" +  srcPoint + ";" + dstPoint + ";" + weight);
					
					//ACLMessage reply = myAgent.receive();

					/*while(reply == null)
					{
					}*/
				
	
						// process reply content
						/*String content = reply.getContent();
						setTaxiID(Integer.parseInt(content.substring(content.indexOf(';') + 1)));
						System.out.println("[PASSENGER] : WAITING FOR TAXI Nr " + taxiID);*/

				}		
			});
		} 
		catch(ArrayIndexOutOfBoundsException e) { System.out.println("[ERROR] : INVALID NUMBER OF ARGUMENTS WHILE CREATING 'PASSENGER'");  System.exit(1); }
		catch (NumberFormatException e) { System.out.println("[ERROR] : INVALID CAPACITY WHILE CREATING PASSENGER");  System.exit(1); }
		
	}
	
	// take down passenger
	protected void takeDown() { System.out.println("[PASSENGER] : DESTINY LOCATION REACHED"); }
}
