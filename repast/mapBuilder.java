package taxiManager;

import java.util.ArrayList;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;

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
	
	
	// adds a location to the map (random position)
	private void addLocation(String locationName){
		if(!locationExists(locationName)){
			Location location = new Location(locationName, space, network);
			context.add(location);
			locations.add(location);
		} else {
			System.out.println("WARNING: A location names \"" + locationName + "\" already exists. New location will not be created.");
		}
	}
	
	// adds a location to the map (specified position)
	private void addLocation(String locationName, double x, double y){
		if(!locationExists(locationName)){
			addLocation(locationName);
			space.moveTo(getLocation(locationName), x, y);
		} else {
			System.out.println("WARNING: A location names \"" + locationName + "\" already exists. New location will not be created.");
		}
	}
	
	// adds a road between two locations
	private void connectPlaces(String srcName, String dstName, double weight){
		Location src = getLocation(srcName);
		Location dst = getLocation(dstName);
		network.addEdge(src, dst).setWeight(weight);
		network.addEdge(dst, src).setWeight(weight);
	}
	
	// adds a taxi to the map
	private void addTaxi(String initialPosition){
		Taxi taxi = new Taxi(space, network, initialPosition);
		context.add(taxi); 
		taxi.move(getLocation(initialPosition));
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
	
	// verifies if a location already exists
	private boolean locationExists(String locationName){
		for(int i = 0; i < locations.size(); i++){
			if(locations.get(i).getLocationName() == locationName)
				return true;
		}
		return false;
	}
	
	@Override
	public Context build(Context<Object> context) {
		this.context = context;
		this.context.setId("TaxiManager");
		
		// GENERATE CONTINUOUS SPACE
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
		addLocation("Penafiel", 25.4, 19.0);
		addLocation("Paredes", 32.6, 32.7);
		addLocation("Penafiel", 0.0, 0.0);
		addLocation("Marco", 47.7, 49.2);

		// GENERATE ROADS
		connectPlaces("Penafiel", "Paredes", 12.0);
		connectPlaces("Penafiel", "Marco", 14.0);

		//GENERATE TAXIS
		addTaxi("Penafiel");
		
		return context;
	}
}
