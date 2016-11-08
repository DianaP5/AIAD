package manager;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Taxi extends Agent
{
	private final int capacity = 4;
	private int passengers = 0;
	
	protected void setup()
	{
		System.out.println("Created taxi");
		addBehaviour(new CyclicBehaviour(this) 
		{

			@Override
			public void action() {
				ACLMessage msg = myAgent.receive();
				if(msg != null)
				{
					System.out.println("Recebi");
					if(msg.getContent().contains("take"))
					{
						if(passengers < capacity)
						{
							passengers++;
							System.out.println("Adicionou passageiro");
						}
						else
						{
							System.out.println("Taxi Cheio");
						}
					}
					
				}
				
			}
			
		});
	}
	
	protected void takeDown()
	{
		System.out.println("Removed taxi");
	}
	
	private class TakePassenger extends Behaviour
	{
		@Override
		public void action() 
		{
			ACLMessage msg = myAgent.receive();
			if(msg != null)
			{
				System.out.println("Recebi");
				if(msg.getContent().contains("take"))
				{
					if(passengers < capacity)
					{
						passengers++;
						System.out.println("Adicionou passageiro");
					}
				}
				
			}
			
		}

		@Override
		public boolean done() 
		{
			return true;
		}
	}
}
