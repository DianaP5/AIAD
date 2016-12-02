package taxiManager;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Location {
	
	// WORLD VARIABLES
	private ContinuousSpace <Object> space;
	private Network<Object> network;
	private String name;
	
	// Class constructor
	public Location(String name, ContinuousSpace<Object> space, Network<Object> network) {
		this.space = space ;
		this.name = name;
		this.network = network;
	}
	
	// Get the location name
	public String getLocationName() {return name;}
}