package taxiManager;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Location {
	
	private ContinuousSpace <Object> space;
	private Network<Object> network;
	private String name;
	
	public Location(String name, ContinuousSpace<Object> space, Network<Object> network) {
		this.space = space ;
		this.name = name;
		this.network = network;
	}
	
	public String getLocationName() {return name;}
}