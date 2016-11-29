package taxiManager;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import sajas.core.Agent;
import sajas.domain.DFService;

public class Taxi extends Agent {

	private ContinuousSpace<Object> space;
	private Network<Object> network;
	private String initialLocation;
	private String currentLocation;
	private AID resultsCollector;
	private Codec codec;
	
	public Taxi(ContinuousSpace<Object> space, Network<Object> network, String initialLocation) {
		this.space = space;
		this.network = network;
		this.initialLocation = initialLocation;
		this.currentLocation = initialLocation;
	}

	public void move(Location dst) {
		space.moveTo(this, space.getLocation(dst).getX(), space.getLocation(dst).getY());
	}

	public String getInitialLocation() {
		return initialLocation;
	}

	@Override
	public void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName() + "-taxi");
		sd.setType("service-taxi");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println(e.getMessage());
		}
		
		//search central
		DFAgentDescription template = new DFAgentDescription();
  		ServiceDescription templateSd = new ServiceDescription();
  		templateSd.setType("service-central");
  		template.addServices(templateSd);
		SearchConstraints sc = new SearchConstraints();
  		// We want to receive 10 results at most
  		sc.setMaxResults(new Long(10));
  		
  		DFAgentDescription[] results = null;
		try {
			results = DFService.search(this, template, sc);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		if (results.length > 0) {
  			System.out.println("Agent "+getLocalName()+" found central:");
  			codec = new SLCodec();
  			getContentManager().registerLanguage(codec);
  			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
  			inform.addReceiver(dfd.getName());
  			inform.setLanguage(codec.getName());
  			inform.setContent("mandei");
  			send(inform);
  		}	
  		else {
  			System.out.println("Agent "+getLocalName()+" did not found central.");
  		}
		
		
	}

}