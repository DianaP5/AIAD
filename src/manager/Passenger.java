package manager;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
	private void sendMessage(int performative, String receiver, String content){
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		message.addReceiver(getAID(receiver));
		send(message);
	}
	
	// initialize passenger
	protected void setup()
	{
		// try to parse passenger arguments
		try {
			Object[] args = getArguments();
			this.srcPoint = args[0].toString(); // obtain initial position
			this.dstPoint = args[1].toString(); // obtain destiny position
			this.weight = Integer.parseInt(args[2].toString());	// obtain needed 'capacity'
			
			System.out.println("[PASSENGER] : CREATED " + weight + " AND WAITING ON " + srcPoint + " TO GO TO " + dstPoint);
			
			// ask central for taxi
			addBehaviour(new OneShotBehaviour(){
				public void action() {
					
					// send a request message to the central
					sendMessage(ACLMessage.REQUEST, "Central", "ask_taxi;" +  srcPoint + ";" + dstPoint + ";" + weight);
					
					// block until message from central is not received
					ACLMessage reply = blockingReceive(centralReply);
					
					// process reply content
					String content = reply.getContent();
					setTaxiID(Integer.parseInt(content.substring(content.indexOf(';') + 1)));
					System.out.println("[PASSENGER] : WAITING FOR TAXI Nr " + taxiID);
				}		
			});
		} 
		catch(ArrayIndexOutOfBoundsException e) { System.out.println("[ERROR] : INVALID NUMBER OF ARGUMENTS WHILE CREATING 'PASSENGER'");  System.exit(1); }
		catch (NumberFormatException e) { System.out.println("[ERROR] : INVALID CAPACITY WHILE CREATING PASSENGER");  System.exit(1); }
		
	}
	
	// take down passenger
	protected void takeDown() { System.out.println("[PASSENGER] : DESTINY LOCATION REACHED"); }
}
