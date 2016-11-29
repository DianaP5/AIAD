package taxiManager;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import sajas.core.Agent;

public class Taxi extends Agent{
	

	private ContinuousSpace<Object > space ;
	private Network<Object> network;
	private String initialLocation;
	private String currentLocation;
	
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation) {
		this.space = space ;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
	}
	
	public void move(Location dst) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}
	
	public String getInitialLocation(){ return initialLocation;}
	
	@Override
	public void setup(){
		System.out.println("taxi");
	}
	
}