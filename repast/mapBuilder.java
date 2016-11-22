package taxiManager;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.core.Runtime;

import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;


public class mapBuilder implements ContextBuilder<Object> {

	////////////////////////
	//						
	//	MEMBER VARIABLES  
	//						
	////////////////////////
	private ArrayList<Location> locations = new ArrayList<Location>();	
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	
	
	////////////////////////
	//						
	//	MEMBER METHODS
	//						
	////////////////////////
	
	// adds a location to the map
	
	private void addLocation(String locationName){
		addLocation(context, space, network, locationName);
	}
	
	// creates a new location at a random place
	private void addLocation(Context<Object> context,  ContinuousSpace<Object> space, Network<Object> network, String locationName){
		Location location = new Location(locationName, space, network);
		context.add(location);
		locations.add(location);
	}
	// creates a new location at a specific place
	private void addLocation(String locationName, double x, double y){
		addLocation(context, space, network, locationName);
		space.moveTo(getLocation(locationName), x, y);
	}
	
	
	// returns a location given its name
	private Location getLocation(String locationName){
		Location location = null;
		for(int i = 0; i < locations.size(); i++) {
			location = locations.get(i);
			if(location.getLocationName() == locationName)
				 break;
		}
		return location;
	}
	
	// connects two locations
	private void connectPlaces(String srcName, String dstName, double weight){
		Location src = getLocation(srcName);
		Location dst = getLocation(dstName);
		network.addEdge(src, dst).setWeight(weight);
		network.addEdge(dst, src).setWeight(weight);
	}

	@Override
	public Context build(Context<Object> context) {
		this.context = context;
		this.context.setId("TaxiManager");
		
		// GENERATE SPACE
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory( null );
		space = spaceFactory.createContinuousSpace (
				"space", 
				this.context ,
				new RandomCartesianAdder<Object>() ,
				new repast.simphony.space.continuous.WrapAroundBorders(),
				50, 50);
		
		
		// GENERATE NETWORK
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("network", this.context, true);
		netBuilder.buildNetwork();
		network = (Network<Object>) this.context.getProjection("network");
				
		// GENERATE LOCATIONS
		addLocation("Penafiel", 20.0, 20.0);
		addLocation("Paredes", 30.0, 30.0);
		addLocation("Marco", 40.0, 40.0);

		// GENERATE ROADS
		connectPlaces("Penafiel", "Paredes", 12.0);
		connectPlaces("Penafiel", "Marco", 14.0);

		//GENERATE TAXIS
		Taxi taxi = new Taxi(space, network, "Penafiel");
		context.add(taxi); taxi.move(getLocation(taxi.getInitialLocation()));
		
		return context;
	}
}
