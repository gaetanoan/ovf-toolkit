package org.ow2.contrail.common.validation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ow2.contrail.common.Test.TestOVFParser;
import org.ow2.contrail.common.implementation.Renderer;
import org.ow2.contrail.common.implementation.graph.GenericGraph;
import org.ow2.contrail.common.implementation.graph.NetworkEdge;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
import org.w3c.dom.Document;

public class SplitRemergeValidation {
	/**
	 * Test code to validate splitting and re-merging application
	 */
	
	public static void main(String[] args) throws Exception {	
		String filetoParse="src/main/resources/Ovf1VS";
		int howManyFile = 100;
		ApplicationDescriptor appDesc = OVFParser.ParseOVF(URI.create(filetoParse+".ovf"));
		for(int i=1; i<howManyFile;i++){	
			System.out.println("-------------------------");
			String filename = ValidationExperiment.generateOvfs(appDesc, i+1);
			splitRemergeAndCompare(filename);
		}
		
	}
	/**
	 * Method that executes split and remerge of an application described in filename parameter 
	 * using the methods splitAndRemerge and splittingApplication.
	 * It compares the remerged file with the original with methoid comapreDOMwithDataStructure
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void splitRemergeAndCompare(String filename) throws Exception{
		ApplicationDescriptor application = OVFParser.ParseOVF(URI.create(filename+".ovf"));
		splitAndRemerge(filename, application);
		Document ovfD  = TestOVFParser.getDOMfromOVF(filename+"Remerged.ovf");
		boolean checkConsistency = TestOVFParser.compareDOMwithDataStructure(application,ovfD);
		if (checkConsistency) System.out.println("CHECK CONSISTENCY BETWEEN FILES: " + checkConsistency);
		else System.out.println("CHECK CONSISTENCY BETWEEN FILES: " + checkConsistency); 
	}
	
	/**
	 * Methods that execute split and remerge of an application described in application
	 * This method write 3 OVF file during execution:
	 * <filename>Splitted1.ovf (first part of application after splitting)
	 * <filename>Splitted1.ovf (second part of application after splitting)
	 * filenameRemerged.ovf (application remerged)
	 * @param filename used for the renderer to write files
	 * @application ApplicationDescriptor to split
	 * @throws Exception
	 */
	public static void splitAndRemerge(String filename, ApplicationDescriptor application) throws Exception{

		ApplicationDescriptor splitted1 = new ApplicationDescriptor();
		ApplicationDescriptor splitted2 = new ApplicationDescriptor();
		splittingApplication(filename,application,splitted1,splitted2);
		ApplicationDescriptor applicationRemerged = new ApplicationDescriptor();
		applicationRemerged.addAppliances(splitted1.getAllAppliances());
		applicationRemerged.addAppliances(splitted2.getAllAppliances());
		System.out.println("Re-Rendering Remerged Application on "+filename+"Remerged.ovf");
		Renderer.RenderOVF(applicationRemerged, "", filename+"Remerged");

	}
	/**
	 * Methods that execute split of an application described in applicationDescritor given.
	 * This method write 2 OVF file during execution:
	 * <filename>Splitted1.ovf (first part of application after splitting)
	 * <filename>Splitted1.ovf (second part of application after splitting)
	 * @param filename
	 * @application ApplicationDescritor described the application to be splitted
	 * @splitted1 an empty ApplicationDescriptor that will be filled with first part of application
	 * @splitted2 an empty ApplicationDescriptor that will be filled with second part of application
	 * @throws Exception
	 */
	public static void splittingApplication(String filename,ApplicationDescriptor application, ApplicationDescriptor splitted1, ApplicationDescriptor splitted2 ) throws Exception{
		System.out.println("Round Robin split of Application.....");
		roundRobinSplit(application,splitted1,splitted2);
		System.out.println("Split DONE!!");
		
		System.out.println("Rendering first part of Application on "+filename+"Splitted1.ovf");
		Renderer.RenderOVF(splitted1, "", filename+"Splitted1");
		
		System.out.println("Rendering second part of Application on"+filename+"Splitted2.ovf");
		Renderer.RenderOVF(splitted2, "", filename+"Splitted2");
	}
	
	
	public static void roundRobinSplit(ApplicationDescriptor toSplit, ApplicationDescriptor splitted1, ApplicationDescriptor splitted2){
		Collection<ApplianceDescriptor> appliance1st = new ArrayList<ApplianceDescriptor>();
		Collection<ApplianceDescriptor> appliance2nd = new ArrayList<ApplianceDescriptor>();
		Collection<ApplianceDescriptor> applianceCollection = toSplit.getAllAppliances();
		int iteration = 1;
		for(ApplianceDescriptor appliance : applianceCollection){
			if ((iteration%2)!=0){
				appliance1st.add(appliance);
			} else {
				appliance2nd.add(appliance);
			}
			iteration++;
		}
		splitted1.addAppliances(appliance1st);
		splitted2.addAppliances(appliance2nd);
	}
	
	public static void printLinkedAppliances(GenericGraph<String, NetworkEdge> graph, GenericGraph<String, NetworkEdge> graphSplit1, GenericGraph<String, NetworkEdge> graphSplit2){
		HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>> fullApplicationMap = new HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>>();
		Collection<ApplianceDescriptor> applianceCollection = graph.getAppliances();
		for(ApplianceDescriptor appl:applianceCollection){
			ArrayList<ApplianceDescriptor> link = TestOVFParser.getLinkedAppliance(graph, appl.getID());
			fullApplicationMap.put(appl, link);
		}
		Iterator it = fullApplicationMap.entrySet().iterator();
		
		while(it.hasNext()){
			 Map.Entry pairs = (Map.Entry)it.next();
		        System.out.println(((ApplianceDescriptor) pairs.getKey()).getID() + " = " + pairs.getValue());
		        it.remove();
		}
		System.out.println("******************************************************************************");
		HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>> firstApplicationMap = new HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>>();
		Collection<ApplianceDescriptor> firstApplianceCollection = graphSplit1.getAppliances();
		for(ApplianceDescriptor appl:firstApplianceCollection){
			ArrayList<ApplianceDescriptor> link = TestOVFParser.getLinkedAppliance(graphSplit1, appl.getID());
			firstApplicationMap.put(appl, link);
		}
		Iterator it2 = firstApplicationMap.entrySet().iterator();
		
		while(it2.hasNext()){
			 Map.Entry pairs = (Map.Entry)it2.next();
		        System.out.println(((ApplianceDescriptor) pairs.getKey()).getID() + " = " + pairs.getValue());
		        it2.remove(); 
		}
		System.out.println("******************************************************************************");
		HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>> sndApplicationMap = new HashMap<ApplianceDescriptor, Collection<ApplianceDescriptor>>();
		Collection<ApplianceDescriptor> sndApplianceCollection = graphSplit2.getAppliances();
		for(ApplianceDescriptor appl:sndApplianceCollection){
			ArrayList<ApplianceDescriptor> link = TestOVFParser.getLinkedAppliance(graphSplit2, appl.getID());
			sndApplicationMap.put(appl, link);
		}
		Iterator it3 = sndApplicationMap.entrySet().iterator();
		
		while(it3.hasNext()){
			 Map.Entry pairs = (Map.Entry)it3.next();
		        System.out.println(((ApplianceDescriptor) pairs.getKey()).getID() + " = " + pairs.getValue());
		        it3.remove(); 
		}
		System.out.println("******************************************************************************");
	}
}
