package taxiManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
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
	public void addLocation(String locationName){
		if(!locationExists(locationName)){
			Location location = new Location(locationName, space, network);
			locations.add(location);
			context.add(location);
		} else {
			System.out.println("WARNING: A location names \"" + locationName + "\" already exists. New location will not be created.");
		}
	}
	
	// adds a location to the map (specified position)
	public void addLocation(String locationName, double x, double y){
		if(!locationExists(locationName)){
			addLocation(locationName);
			space.moveTo(getLocation(locationName), x, y);
		} else {
			System.out.println("WARNING: A location names \"" + locationName + "\" already exists. New location will not be created.");
		}
	}
	
	// adds a road between two locations
	public void connectPlaces(String srcName, String dstName, double weight){
		Location src = getLocation(srcName);
		Location dst = getLocation(dstName);
		network.addEdge(src, dst).setWeight(weight);
		network.addEdge(dst, src).setWeight(weight);
	}
	
	// adds a taxi to the map
	public void addTaxi(String initialPosition){
		Taxi taxi = new Taxi(space, network, initialPosition, locations);
		context.add(taxi); 
		taxi.move(getLocation(initialPosition));
	}
	
	// returns a location given its name
	private Location getLocation(String locationName){
		
		for(int i = 0; i < locations.size(); i++) {
			if(locations.get(i).getLocationName().equals(locationName))
				return locations.get(i);
		}
		return null;
	}
	
	// verifies if a location already exists
	public boolean locationExists(String locationName){
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
			/*
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
		addLocation("Santarem", 5.1, 20.0);
		addLocation("Portalegre", 17.0, 20.0);
		addLocation("Lisboa", 3.0, 15.0);
		addLocation("Setubal", 4.0, 12.0);
		addLocation("Evora", 13.0, 12.0);
		addLocation("Beja", 12.5, 7.0);
		addLocation("Faro", 14.0, 3.0);

		// GENERATE ROADS
		//ROAD_1
		connectPlaces("Viana Do Castelo", "Porto", 5.0);
		connectPlaces("Porto", "Viseu", 6.0);
		connectPlaces("Viseu", "Castelo Branco", 9.0);
		connectPlaces("Castelo Branco", "Santarem", 9.0);
		connectPlaces("Santarem", "Lisboa", 7.0);
		connectPlaces("Lisboa", "Evora", 7.0);
		connectPlaces("Evora", "Faro", 14.0);
		connectPlaces("Porto", "Aveiro", 1.0);
		
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
		connectPlaces("Porto", "Coimbra", 2.0);

		*/
		// TEST MAP
		addLocation("a", 10.0, 30.0);
		addLocation("b", 10.0, 10.0);
		addLocation("c", 30.0, 30.0);
		addLocation("d", 30.0, 10.0);
		addLocation("e", 45.0, 15.0);
		addLocation("f", 45.0, 45.0);
		connectPlaces("a","b",4.0);
		connectPlaces("a","c",3.0);
		connectPlaces("a","d",5.0);
		connectPlaces("b","d",2.0);
		connectPlaces("d","c",3.0);
		connectPlaces("e","d",3.0);
		connectPlaces("e","c",2.0);
		connectPlaces("e","f",2.0);
		connectPlaces("f","c",3.0);
		
		
		// Clean everything before the simulation ends
		Schedule scheduler = new Schedule();
		ScheduleParameters stop = ScheduleParameters.createAtEnd(ScheduleParameters.END);
		scheduler.schedule(stop, this, "endSimulation");
		
		return super.build(context);
	}

	public void endSimulation(){
		System.out.println("END OF SIMULATION");
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
			
			
			Central central = new Central(locations,space,network);
			agentContainer.acceptNewAgent("Central", central).start();
			
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	//Not Tested
		public void txtToCode(String locationsFile, String connectFile) throws IOException
		{
			//Linha exemplo no locationsFile .txt
			//Localidade;x;y
			
			FileReader locFile = new FileReader(locationsFile);
			BufferedReader locReader = new BufferedReader(locFile);
			String loc_line;
			
			while((loc_line = locReader.readLine())!=null)
			{
				String [] splitter = loc_line.split(";");
				addLocation(splitter[0], Double.parseDouble(splitter[1]),Double.parseDouble(splitter[2]));
			}
			
			locReader.close();
			
			//Linha exemplo no connectFile .txt
			//src;dst;weigth
			
			FileReader conFile = new FileReader(connectFile);
			BufferedReader conReader = new BufferedReader(conFile);
			String con_line;
			
			while((con_line = conReader.readLine())!=null)
			{
				String [] splitter = con_line.split(";");
				connectPlaces(splitter[0], splitter[1], Double.parseDouble(splitter[2]));
			}
			
			conReader.close();
		}
	
}