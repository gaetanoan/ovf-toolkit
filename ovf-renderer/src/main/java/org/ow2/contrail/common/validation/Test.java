package org.ow2.contrail.common.validation;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.ow2.contrail.common.Test.TestOVFParser;
import org.ow2.contrail.common.implementation.graph.*;
import org.ow2.contrail.common.implementation.Renderer;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.apache.log4j.*;

public class Test {

	private static Logger logger = Logger.getLogger(Test.class);
	private static String path = "src/main/resources";
	private static String name = "test";
	private static String source = "contrail_petstore.xml";

	public static void testDotFile(){
		try {
			GenericGraph<String, NetworkEdge> graph = GraphFactory.getApplicationGraph(new URI("src/main/resources/ovf-3vms-vep2.0.ovf"));
			graph.addAppliance(new ApplianceDescriptor("vertex1", "" , 1, false));
			
			Collection<ApplianceDescriptor> appls = graph.getAppliances();
			
			ApplicationDescriptor app = new ApplicationDescriptor();
			app.addAppliances(appls);
			System.out.println(app.getAllAppliances().size());
			
			Renderer.RenderOVF(app, "/home/gae", "pippo");
			
			for (ApplianceDescriptor appl : appls){
				
			}
			ApplianceDescriptor ap = appls.iterator().next();
			Set<NetworkEdge> e = graph.edgeSet();
			System.out.println(e.size());
			
			graph.export(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws Exception {

		testDotFile();
		logger.info("Test Renderer - starting test");
		logger.info("Test Renderer - creating document \""+name+".xml\" from document \""+source+"\"\n");
		// Renderer.RenderOVF(path + "/contrail_petstore.xml", path, name);
		logger.info("Test Renderer - test completed, document \""+path+"/"+name+".xml\" created");
	}
}
