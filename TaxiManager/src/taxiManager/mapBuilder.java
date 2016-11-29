package taxiManager;

import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import sajas.core.Agent;
import sajas.core.Runtime;

public class mapBuilder extends RepastSLauncher implements ContextBuilder<Object> {

	////////////////////////
	//						
	//	MEMBER VARIABLES  
	//						
	////////////////////////
	public static final boolean SEPARATE_CONTAINERS = false;
	private ArrayList<Location> locations = new ArrayList<Location>();	
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private Network<Object> network;
	private ContainerController mainContainer;
	private ContainerController agentContainer;
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
		addLocation("Viana Do Castelo", 5.0, 47.0);
		addLocation("Braga", 10.0, 43.0);
		addLocation("Vila Real", 14.0, 40.0);
		addLocation("Braganca", 20.0, 47.0);
		addLocation("Porto", 7.0, 38.0);
		addLocation("Aveiro", 5.5, 33.0);
		addLocation("Viseu", 13.0, 33.0);
		addLocation("Guarda", 18.0, 31.0);
		addLocation("Coimbra", 10.5, 29.0);
		addLocation("Leiria", 5.0, 25.0);
		addLocation("Castelo Branco", 15.0, 25.0);
		addLocation("Santarém", 5.1, 20.0);
		addLocation("Portalegre", 17.0, 20.0);
		addLocation("Lisboa", 3.0, 15.0);
		addLocation("Setubal", 4.0, 12.0);
		addLocation("Évora", 13.0, 12.0);
		addLocation("Beja", 12.5, 7.0);
		addLocation("Faro", 14.0, 0.0);

		// GENERATE ROADS
		//ROAD_1
		connectPlaces("Viana Do Castelo", "Porto", 5.0);
		connectPlaces("Porto", "Viseu", 6.0);
		connectPlaces("Viseu", "Castelo Branco", 9.0);
		connectPlaces("Castelo Branco", "Santarém", 9.0);
		connectPlaces("Santarém", "Lisboa", 7.0);
		connectPlaces("Lisboa", "Évora", 7.0);
		connectPlaces("Évora", "Faro", 14.0);
		
		//ROAD_2
		connectPlaces("Braga", "Vila Real", 3.0);
		connectPlaces("Vila Real", "Guarda", 8.0);
		connectPlaces("Guarda", "Braganca", 12.0);
		connectPlaces("Braganca", "Aveiro", 20.0);
		connectPlaces("Aveiro", "Leiria", 10.0);
		connectPlaces("Leiria", "Portalegre", 12.0);
		connectPlaces("Portalegre", "Coimbra", 10.0);
		connectPlaces("Coimbra", "Setubal", 18.0);
		connectPlaces("Setubal", "Beja", 8.0);		
		
		return super.build(context);
	}

	@Override
	public String getName() {
		return "Taxi Manager";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentContainer = rt.createAgentContainer(p2);
		} else {
			agentContainer = mainContainer;
		}
		launchAgents();
	}
	
	private void launchAgents() {
		try {
			AID resultsCollectorAID = null;
			Random r = new Random();
			String moveTo = "";
			for (int i = 0; i < 5; i++) {
				moveTo = locations.get(r.nextInt(locations.size()-1)).getLocationName();
				Taxi taxi = new Taxi(space,network,moveTo);
				agentContainer.acceptNewAgent("Taxi" + i, taxi).start();
				taxi.move(getLocation(moveTo));
			}
			
			Central central = new Central();
			agentContainer.acceptNewAgent("Central", central).start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
