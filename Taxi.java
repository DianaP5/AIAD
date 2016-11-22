package taxiManager;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Taxi {
	

	private ContinuousSpace<Object > space ;
	private Network<Object> network;
	private Location initialLocation;
	private Location currentLocation;
	
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, Location initialLocation) {
		this.space = space ;
		this.network = network;
		this.initialLocation = initialLocation;
	}
	
	public void move(Location dst) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}
	
	public Location getInitialLocation(){return initialLocation;}
	
}