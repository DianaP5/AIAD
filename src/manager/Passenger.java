package manager;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class Passenger extends Agent
{
	private int weigth;
	private int cost = 0;
	
	protected void setup()
	{
		System.out.println("Created Passenger");
	}
	
	protected void takeDown()
	{
		System.out.println("Removed Passenger");
	}
	
	private class CallTaxi extends Behaviour
	{

		@Override
		public void action()
		{
			ACLMessage msg = myAgent.receive();
			if(msg != null)
			{
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("take");
				send(reply);
			}
		}

		@Override
		public boolean done() 
		{
			return false;
		}
		
	}
}
