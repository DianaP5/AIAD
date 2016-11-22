package taxiManager;

import java.util.ArrayList;

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
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;


public class mapBuilder implements ContextBuilder<Object> {

	private void connectPlaces(Network<Object> network, Location src, Location dst, double weight){
		network.addEdge(src, dst).setWeight(weight);
		network.addEdge(dst, src).setWeight(weight);
	}
	
	// TODO: reference a location by its NAME
	private void addLocation(Context<Object> context, String locationName){}

	@Override
	public Context build(Context<Object> context) {
		
		context.setId("TaxiManager");
		
		// GENERATE SPACE
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory( null );
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace ("space", context ,
				new RandomCartesianAdder<Object>() ,
				new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);
		
		// GENERATE NETWORK
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("network", context, true);
		netBuilder.buildNetwork();
		@SuppressWarnings("unchecked")
		Network<Object> network = (Network<Object>) context.getProjection("network");
		
		
		// GENERATE LOCATIONS
		Location penafiel = new Location("Penafiel", space, network);
		Location paredes = new Location("Paredes", space, network);
		Location marco = new Location("Marco", space, network);
		context.add(penafiel);
		context.add(paredes);
		context.add(marco);
		
		// GENERATE ROADS
		connectPlaces(network, penafiel, paredes, 12.0);
		connectPlaces(network, penafiel, marco, 14.0);

		//GENERATE TAXIS
		Taxi taxi = new Taxi(space, network, penafiel); context.add(taxi); taxi.move(taxi.getInitialLocation());
		System.out.println(network.getAdjacent(penafiel));
		

		Iterable<Object> locations = network.getAdjacent(penafiel);
		Object closer = null;
		double minimumWeigth = 1000;
		for(Object lc : locations){
			if(network.getEdge(penafiel, lc).getWeight() <= minimumWeigth){
				System.out.println("New minimum: " + network.getEdge(penafiel, lc).getWeight());
				closer = lc;
				minimumWeigth = network.getEdge(penafiel, lc).getWeight();
			}
		}
		
		System.out.println(closer.toString());
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Moved");
		taxi.move((Location)closer);
		
		return context;
	}
}
