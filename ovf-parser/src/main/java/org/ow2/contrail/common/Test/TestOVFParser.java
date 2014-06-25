package org.ow2.contrail.common.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ow2.contrail.common.implementation.graph.GenericGraph;
import org.ow2.contrail.common.implementation.graph.GraphFactory;
import org.ow2.contrail.common.implementation.graph.NetworkEdge;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.OVFProductSection;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCdDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwFloppyDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwIdeController;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwParallelSCSI;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwSharedDiskDrive;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is intended for testing purpose only 
 * @author maestrelli, distefano, anastasi
 *
 */
public class TestOVFParser {
	
	public static void main(String[] args) throws Exception {	
		String filepath = "src/main/resources/Ovf5VS.xml";
		TestOVFParser.testDotFile(filepath);
	}

	/**
	 * Check if ApplicationDescriptor and DocumentObjectModel passed is consistent
	 * 
	 * @param app
	 * @param ovfD
	 * @return
	 * @throws XPathExpressionException
	 */
	public static boolean compareDOMwithDataStructure(ApplicationDescriptor app, Document ovfD) throws XPathExpressionException{
		XPath xpath = XPathFactory.newInstance().newXPath();
		Collection<ApplianceDescriptor> appliances = app.getAllAppliances();
		//check DiskSection
		Collection<Disk> appDisks = getApplicationDisks(app);
		NodeList allDisks =  ovfD.getElementsByTagName("Disk");
		HashMap<String, Boolean> checkAppDiskMap = new HashMap<String, Boolean>();
		if (allDisks.getLength()==appDisks.size()){
			boolean appDiskMatch = false;
			boolean appFindDisk = false;
			for (Disk appDisk : appDisks){
				for (int i=0; i < allDisks.getLength(); i++){
					Node currDisk = allDisks.item(i);
					if(currDisk.getAttributes().getNamedItem("ovf:diskId").getNodeValue().equals(appDisk.getId())){
						appFindDisk=true;
					}
					if(i==allDisks.getLength()-1 && appFindDisk==true){
						appDiskMatch=true;
					}
				}
				if(appDiskMatch)
					checkAppDiskMap.put(appDisk.getId(), true);
				else
					checkAppDiskMap.put(appDisk.getId(), false);
					
			}
		} else return false;
		//check NetworkSection
		Collection<OVFVirtualNetwork> appNets = getApplicationNetworks(app);
		NodeList allNets =  ovfD.getElementsByTagName("Network");
		HashMap<String, Boolean> checkAppNetMap = new HashMap<String, Boolean>();
		if (allNets.getLength()==appNets.size()){
			boolean appNetMatch = false;
			boolean appFindNet = false;
			for (OVFVirtualNetwork appNet : appNets){
				for (int i=0; i < allNets.getLength(); i++){
					Node currNet = allNets.item(i);
					if(currNet.getAttributes().getNamedItem("ovf:name").getNodeValue().equals(appNet.getName())){ 
						appFindNet=true;
					}
					if(i==allDisks.getLength()-1 && appFindNet==true){
						appNetMatch=true;
					}
				}
				if(appNetMatch)
					checkAppNetMap.put(appNet.getName(), true);
				else
					checkAppNetMap.put(appNet.getName(), false);
				}
		} else return false;
		NodeList nodes = ovfD.getElementsByTagName("VirtualSystem");
		HashMap<String, Boolean> checkVSNetMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> checkVSDiskMap = new HashMap<String, Boolean>();
		if (nodes.getLength()==appliances.size()){
			//Same number of virtual systems. Checking equivalence...
			for (ApplianceDescriptor appliance : appliances){
				for (int i=0; i < nodes.getLength(); i++){
					Node currNode = nodes.item(i);
					if(currNode.getAttributes().getNamedItem("ovf:id").getNodeValue().equals(appliance.getID())){ 
						//same virtual system. Checking networks.....
						XPathExpression expr = xpath.compile("ProductSection/Property[@value='dhcp@public']");
						NodeList children = (NodeList) expr.evaluate(currNode, XPathConstants.NODESET);
						
						if (children.getLength() == appliance.getAssociatedVirtualNetworks().size()){
							//property size of virtual system match. Checking singles networks...
							for (int j=0; j < children.getLength(); j++){
								Node currChild = children.item(j);
								boolean netsMatch = false;
								boolean findNet = false;
								for(OVFVirtualNetwork net: appliance.getAssociatedVirtualNetworks()){
									if (currChild.getAttributes().getNamedItem("ovf:key").getNodeValue().contains(net.getName())){
										findNet=true;
										break;
									}
								}
								if(j==children.getLength()-1 && findNet==true){
									netsMatch=true;
								}
								if(netsMatch)
									checkVSNetMap.put(appliance.getID(), true);
								else
									 checkVSNetMap.put(appliance.getID(), false);
							}
						} else {
							//System.out.println(children.getLength() +" == "+appliance.getAssociatedVirtualNetworks().size());
							checkVSNetMap.put(appliance.getID(), false);
						}
					//checking disks...
					XPathExpression exprDisk = xpath.compile("VirtualHardwareSection/Item/ResourceType[text()=\"17\"]");
					NodeList disks=(NodeList) exprDisk.evaluate(currNode, XPathConstants.NODESET);
					if(appliance.getDisks().size()==disks.getLength()){
						for (int j=0; j < disks.getLength(); j++){
							Node currDisk = disks.item(j);
							Node itemDiskNode = currDisk.getParentNode();
							XPathExpression exprHostResource = xpath.compile("HostResource");
							NodeList hostDisks=(NodeList) exprHostResource.evaluate(itemDiskNode, XPathConstants.NODESET);
							boolean diskMatch = false;
							boolean findDisk = false;
							for(Disk applianceDisk :appliance.getDisks()){
								if(hostDisks.item(0).getTextContent().split("/")[2].equals(applianceDisk.getId())){
									findDisk=true;
									break;
								}
							}
							if(j==disks.getLength()-1 && findDisk==true){
								diskMatch=true;
							}
							if(diskMatch)
								checkVSDiskMap.put(appliance.getID(), true);
							else
								checkVSDiskMap.put(appliance.getID(), false);
						}
					}else{
						System.out.println(appliance.getDisks().size()==disks.getLength());
						checkVSDiskMap.put(appliance.getID(), false);
					}
					/*
					//checking shared disks...
					XPathExpression exprSharedDisk = xpath.compile("VirtualHardwareSection/Item/ResourceType");
					NodeList sharedDisks=(NodeList) exprSharedDisk.evaluate(currNode, XPathConstants.NODESET);
					System.out.println(appliance.getSharedDisks().size()+" == "+sharedDisks.getLength());
					//if(appliance.getSharedDisks().size()==sharedDisks.getLength()){
						for (int j=0; j < sharedDisks.getLength(); j++){
							
							Node currDisk = sharedDisks.item(j);
							System.out.println(currDisk.getTextContent());
							boolean diskMatch = false;
							boolean findDisk = false;
							/*
							for(SharedDisk applianceSharedDisk :appliance.getSharedDisks()){
								if(currDisk.getTextContent().split("/")[2].equals(applianceSharedDisk.getId())){
									findDisk=true;
									break;
								}
							}
							if(j==sharedDisks.getLength()-1 && findDisk==true){
								diskMatch=true;
							}
							if(diskMatch)
								checkMap.put(appliance.getID(), true);
							else
								checkMap.put(appliance.getID(), false);
						
						}
					//}else{
						//System.out.println(appliance.getDisks().size()==disks.getLength());
					//	checkMap.put(appliance.getID(), false);
					//}
					*/
					
					}
				}
			}
		} else throw new Error("ERROR: number of virtual system doesn't match");
		boolean checkConsistency = true;
		Iterator it = checkAppDiskMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    checkConsistency = (checkConsistency & (Boolean) pairs.getValue());
		}
		it = checkAppNetMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    checkConsistency = (checkConsistency & (Boolean) pairs.getValue());
		}
		it = checkVSNetMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    checkConsistency = (checkConsistency & (Boolean) pairs.getValue());
		}
		it = checkVSDiskMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    checkConsistency = (checkConsistency & (Boolean) pairs.getValue());
		}
		return checkConsistency;
		
	}
	
	
	public static ArrayList<ApplianceDescriptor> getLinkedAppliance(GenericGraph<String, NetworkEdge> graph, String applName){
		ArrayList<ApplianceDescriptor> link = new ArrayList<ApplianceDescriptor>();
		for(ApplianceDescriptor appl:graph.getAppliances()){
			if (graph.containsEdge(applName, appl.getID()))
				link.add(appl);
		}
		return link;
		
		
	}
	
	public static boolean checkGraphCoherence(GenericGraph<String, NetworkEdge> graph1, GenericGraph<String, NetworkEdge> graph2){
		
//		Set<String> vertexSet = graph1.vertexSet();
//		for (String vert:vertexSet){
//			System.out.println("vertex: "+vert);
//		}
//		System.out.println("***************************************");
//		Set<String> vertexSet2 = graph2.vertexSet();
//		for (String vert:vertexSet){
//			System.out.println("vertex: "+vert);
//		}
		Set<NetworkEdge> edgeSet = graph1.edgeSet();
		NetworkEdge edge1=null;
		for (NetworkEdge edge:edgeSet){
			if(edge.getEdgeName().equals("VNet1")) edge1=edge;
			System.out.println("edge: "+edge.getEdgeName());
		}
		System.out.println("***************************************");
		Set<NetworkEdge> edgeSet2 = graph2.edgeSet();
		NetworkEdge edge2=null;
		for (NetworkEdge edge:edgeSet2){
			if(edge.getEdgeName().equals("VNet1")) edge2=edge;
			System.out.println("edge: "+edge.getEdgeName());
		}
		System.out.println(edge1.equals(edge2));
		
		
		return graph1.equals(graph2);
		//return graph1.checkGraphCoherence(graph2);
	}

	public static ApplicationDescriptor testDotFile(String filename){
		try {
			GenericGraph<String, NetworkEdge> graph = GraphFactory.getApplicationGraph(new URI(filename+".ovf"));
			Collection<ApplianceDescriptor> appls = graph.getAppliances();
			graph.export(filename+".dot");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * generate Document Object Model (DOM) from OVF file
	 * @param filePath
	 * @return
	 */
	public static Document getDOMfromOVF(String filePath){
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder builder = null;
		Document ovfD = null;
		try {
			builder = domFactory.newDocumentBuilder();
			ovfD = (Document) builder.parse(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ovfD;
	}
	
	private static void navigateOVF(ApplicationDescriptor applicationDesc){
		Collection<ApplianceDescriptor> applianceCollection = applicationDesc.getAllAppliances();
		System.out.println("Application name: "+applicationDesc.getName()+" ApplianceCollection size:"+ applianceCollection.size());
		for(ApplianceDescriptor appliance : applianceCollection){
			Collection<OVFVirtualSystem> vsCollection = appliance.getVirtualSystems();
			System.out.println("vsCollection Size: "+ vsCollection.size());
			for(OVFVirtualSystem vs : vsCollection){
				
				Collection<OVFVirtualHardware> vHardwareColl = vs.getRequiredHardware();
				//System.out.println(vHardwareColl.size());
				System.out.print("VsId: "+ vs.getId()+"  ->  ");
				for (OVFVirtualHardware vHardware: vHardwareColl){
					//System.out.println(vHardware);
					switch(vHardware.getResourceType()){
						case 3:
						{
							OVFVirtualHwCpu cpu = (OVFVirtualHwCpu) vHardware;
							System.out.print("CPU: " + cpu.getVirtualQuantity()+"   ");
							break;
						}
						case 4:
						{
							OVFVirtualHwMemory mem = (OVFVirtualHwMemory) vHardware;
							System.out.print("RAM: " + mem.getVirtualQuantity() + "   ");
							break;
						}
					}
				}
				System.out.println("");
				System.out.println("----------------------");
			}
		}
	}
	
	
	/**
	 * Get all virtual networks defined in application
	 * @param appDesc
	 * @return
	 */
	public static Collection<OVFVirtualNetwork> getApplicationNetworks(ApplicationDescriptor appDesc){
		Collection<OVFVirtualNetwork> virtNets = new ArrayList<OVFVirtualNetwork>();
		for(ApplianceDescriptor appliances: appDesc.getAllAppliances()){
			for (OVFVirtualNetwork net1 : appliances.getAssociatedVirtualNetworks()){
				boolean toAdd=true;
				for(OVFVirtualNetwork net2 : virtNets){
					if(net1.getName().equals(net2.getName()))
						toAdd=false;
				}
				if(toAdd) virtNets.add(net1);
			}
		}
		return virtNets;
	}
	
	/**
	 * Get all disks defined in application
	 * @param appDesc
	 * @return
	 */
	public static Collection<Disk> getApplicationDisks(ApplicationDescriptor appDesc){
		Collection<Disk> disks = new ArrayList<Disk>();
		for(ApplianceDescriptor appliances: appDesc.getAllAppliances()){
			for (Disk disk : appliances.getDisks()){
				if(!disks.contains(disk)){
					disks.add(disk);
				}
			}
		}
		return disks;
	}
	
	/**
	 * Get all shared disks defined in application
	 * @param appDesc
	 * @return
	 */
	public static Collection<SharedDisk> getApplicationSharedDisks(ApplicationDescriptor appDesc){
		Collection<SharedDisk> sharedDisks = new ArrayList<SharedDisk>();
		for(ApplianceDescriptor appliances: appDesc.getAllAppliances()){
			for (SharedDisk sharedDisk : appliances.getSharedDisks()){
				if(!sharedDisks.contains(sharedDisk)){
					sharedDisks.add(sharedDisk);
				}
			}
		}
		return sharedDisks;
	}
	/**
	 * generate ApplicationDescription create random virtual system. 
	 * The method takes an ApplicationDescriptor to copy network and disk in the new Application. 
	 * 
	 * @param appDesc
	 * @param numVs
	 */
	public static ApplicationDescriptor generateApplication(ApplicationDescriptor appDesc, int numVs){
		ApplicationDescriptor appGenerated = null;
		try {
			Collection<OVFVirtualNetwork> virtNets = new ArrayList<OVFVirtualNetwork>();
			Collection<SharedDisk> sharedDisks = new ArrayList<SharedDisk>();
			Collection<Disk> disks = new ArrayList<Disk>();
			for(ApplianceDescriptor appliances: appDesc.getAllAppliances()){
				for (OVFVirtualNetwork net1 : appliances.getAssociatedVirtualNetworks()){
					boolean toAdd=true;
					for(OVFVirtualNetwork net2 : virtNets){
						if(net1.getName().equals(net2.getName()))
							toAdd=false;
					}
					if(toAdd) virtNets.add(net1);
				}
				
				for (Disk disk : appliances.getDisks()){
					if(!disks.contains(disk)){
						disks.add(disk);
					}
				}
			}
//			for (Disk disk : disks) //System.out.println(disk.getId()); 			
//			for (OVFVirtualNetwork net : virtNets) System.out.println(net.getName()); 	
			
			appGenerated=  TestOVFParser.generateVirtualSystem(appDesc,disks,virtNets,numVs);
		} catch (Exception e) {
			System.out.println("\n***ERROR IN RENDERING OVF FILE***\n");
			e.printStackTrace();
		}
		return appGenerated;
	}
	
	/**
	 * generate virtualSystem with random network. Taking 
	 * 
	 * @param appDesc
	 * @param disks
	 * @param virtNets
	 * @param number
	 * @return
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public static ApplicationDescriptor generateVirtualSystem(ApplicationDescriptor appDesc,Collection<Disk> disks, Collection<OVFVirtualNetwork> virtNets, int number) throws URISyntaxException, Exception{
		ApplicationDescriptor app = new ApplicationDescriptor();
		Collection<ApplianceDescriptor> appls = appDesc.getAllAppliances();
		Collection<ApplianceDescriptor> newAppls = new ArrayList<ApplianceDescriptor>();
		ApplianceDescriptor firstAppliance = appls.iterator().next();

		int numberOfAppliance = newAppls.size();
		for (int i=numberOfAppliance; i<number; i++){
			
			Random random = new Random();
			int randomNet= random.nextInt(10) +1;
			int randomDisk= random.nextInt(5);
			String randomNetName= "VNet"+randomNet;
			Collection<OVFProperty> props= new ArrayList<OVFProperty>();
			Disk disk=(Disk) disks.toArray()[randomDisk];
			OVFProperty property = new OVFProperty();
			property.setKey("ip@"+randomNetName+"@3");
			property.setValue("dhcp@public");
			property.setType("string");
			props.add(property);
			OVFProductSection productSec = new OVFProductSection();
			productSec.setProperties(props);
			OVFVirtualSystem vs = cloneVSFromAppliance(firstAppliance, ++numberOfAppliance, productSec, disk);
			ApplianceDescriptor newAppliance = new ApplianceDescriptor("VirtualSystem"+numberOfAppliance, null , 1, false);
			for(OVFVirtualNetwork net: virtNets){
				if(net.getName().trim().equals(randomNetName)){
					//System.out.println("adding network...");
					newAppliance.addVirtualNetworkToAppliance(net); 
				}
			}
			newAppliance.addProductSection(productSec);
			newAppliance.getVirtualSystems().add(vs);
			newAppls.add(newAppliance);
		}
		app.addAppliances(newAppls);
		return app;
	}
	
	private static OVFVirtualSystem cloneVSFromAppliance(ApplianceDescriptor ad, int vsNum, OVFProductSection productSec, Disk disk){

		if(ad==null){
			System.out.println("ApplianceCollection is null!");
			return null;
		}
		OVFVirtualSystem newVs = null;
		Collection<OVFVirtualSystem> vsCollection = ad.getVirtualSystems();
		if(!vsCollection.isEmpty()){
			OVFVirtualSystem vsInAppliance = vsCollection.iterator().next();
			Collection<OVFVirtualHardware> vHardwareColl = vsInAppliance.getRequiredHardware();
			String vsId = vsInAppliance.getId();
			String vsIdprefix=vsId.substring(0, vsId.length()-1);
			//System.out.println("creating vs "+vsIdprefix+vsNum);
			Collection<OVFVirtualHardware> newVirtHardwareColl = cloneVirtualHardwareCollection(vHardwareColl, productSec, disk);
			newVs = new OVFVirtualSystem(vsIdprefix+vsNum,newVirtHardwareColl);
		}
		else {
			System.out.println("VSCollection is zero!");
		}
		return newVs;
	}
	
	private static Collection<OVFVirtualHardware> cloneVirtualHardwareCollection(Collection<OVFVirtualHardware> vHardwareColl, OVFProductSection productSec, Disk disk){
		
		Collection<OVFVirtualHardware> newVirtHwColl = new ArrayList<OVFVirtualHardware>();
		for (OVFVirtualHardware vHardware: vHardwareColl){
			OVFVirtualHardware newVirtHardware=null;
			switch(vHardware.getResourceType()){
			case 3:
				newVirtHardware= new OVFVirtualHwCpu();
				((OVFVirtualHwCpu) newVirtHardware).setResourceType(3);
				((OVFVirtualHwCpu) newVirtHardware).setVirtualQuantity(((OVFVirtualHwCpu)vHardware).getVirtualQuantity());
				((OVFVirtualHwCpu) newVirtHardware).setAllocationUnits(((OVFVirtualHwCpu)vHardware).getAllocationUnits());
				((OVFVirtualHwCpu) newVirtHardware).setElementName(((OVFVirtualHwCpu)vHardware).getElementName());
				((OVFVirtualHwCpu) newVirtHardware).setDescription(((OVFVirtualHwCpu)vHardware).getDescription());
				newVirtHwColl.add(newVirtHardware);
				break;
			case 4:
				newVirtHardware=new OVFVirtualHwMemory();
				((OVFVirtualHwMemory) newVirtHardware).setResourceType(4);
				((OVFVirtualHwMemory) newVirtHardware).setVirtualQuantity(((OVFVirtualHwMemory)vHardware).getVirtualQuantity());
				((OVFVirtualHwMemory) newVirtHardware).setAllocationUnits(((OVFVirtualHwMemory)vHardware).getAllocationUnits());
				((OVFVirtualHwMemory) newVirtHardware).setElementName(((OVFVirtualHwMemory)vHardware).getElementName());
				((OVFVirtualHwMemory) newVirtHardware).setDescription(((OVFVirtualHwMemory)vHardware).getDescription());
				newVirtHwColl.add(newVirtHardware);
				break;
			case 5:
				newVirtHardware= new OVFVirtualHwIdeController();
				((OVFVirtualHwIdeController) newVirtHardware).setAddress(((OVFVirtualHwIdeController)vHardware).getAddress());
				newVirtHwColl.add(newVirtHardware);
				break;
			case 6:
				newVirtHardware= new OVFVirtualHwParallelSCSI();
				((OVFVirtualHwParallelSCSI) newVirtHardware).setAddress(((OVFVirtualHwParallelSCSI)vHardware).getAddress());
				((OVFVirtualHwParallelSCSI) newVirtHardware).setResourceSubType(((OVFVirtualHwParallelSCSI)vHardware).getResourceSubType());
				newVirtHwColl.add(newVirtHardware);
				break;
			case 10:
				for(OVFProperty prop: productSec.getProperties()){
					if(prop.getKey().startsWith("ip")){
						String netId=prop.getKey().split("@")[1];
						newVirtHardware=new OVFVirtualHwEthernetAdapter();
						((OVFVirtualHwEthernetAdapter) newVirtHardware).setResourceType(10);
						if(((OVFVirtualHwEthernetAdapter)vHardware).getAddressOnParent() != null) 
							((OVFVirtualHwEthernetAdapter) newVirtHardware).setAddressOnParent(((OVFVirtualHwEthernetAdapter)vHardware).getAddressOnParent());
						if(Boolean.toString(((OVFVirtualHwEthernetAdapter)vHardware).getAutomaticAllocation()) != null)
							((OVFVirtualHwEthernetAdapter) newVirtHardware).setAutomaticAllocation(((OVFVirtualHwEthernetAdapter)vHardware).getAutomaticAllocation());	
						if(((OVFVirtualHwEthernetAdapter)vHardware).getResourceSubType() != null)
							((OVFVirtualHwEthernetAdapter) newVirtHardware).setResourceSubType(((OVFVirtualHwEthernetAdapter)vHardware).getResourceSubType());	
						if(((OVFVirtualHwEthernetAdapter)vHardware).getElementName() != null)
							((OVFVirtualHwEthernetAdapter) newVirtHardware).setElementName("Ethernet Adapter on \""+netId+"\"");	
						if(((OVFVirtualHwEthernetAdapter)vHardware).getConnection() != null)
							((OVFVirtualHwEthernetAdapter)newVirtHardware).setConnection(netId);
						newVirtHwColl.add(newVirtHardware);
					}
				}
				break;
			case 14:
				newVirtHardware= new OVFVirtualHwFloppyDrive();
				((OVFVirtualHwFloppyDrive) newVirtHardware).setResourceType(14);
				if((Integer)((OVFVirtualHwFloppyDrive)vHardware).getAddressOnParent() != null) 
					((OVFVirtualHwFloppyDrive) newVirtHardware).setAddressOnParent(((OVFVirtualHwFloppyDrive)vHardware).getAddressOnParent());
				if(Boolean.toString(((OVFVirtualHwFloppyDrive)vHardware).isAutomaticAllocation()) != null)
					((OVFVirtualHwFloppyDrive) newVirtHardware).setAutomaticAllocation(((OVFVirtualHwFloppyDrive)vHardware).isAutomaticAllocation());	
				newVirtHwColl.add(newVirtHardware);
				break;
			case 15:
				
				newVirtHardware=new OVFVirtualHwCdDrive(); 
				((OVFVirtualHwCdDrive) newVirtHardware).setResourceType(15);
				if((Integer)((OVFVirtualHwCdDrive)vHardware).getAddressOnParent() != null) 
					((OVFVirtualHwCdDrive) newVirtHardware).setAddressOnParent(((OVFVirtualHwCdDrive)vHardware).getAddressOnParent());
				if((Integer)((OVFVirtualHwCdDrive)vHardware).getParent() != null) 
					((OVFVirtualHwCdDrive) newVirtHardware).setParent(((OVFVirtualHwCdDrive)vHardware).getParent());
				if(Boolean.toString(((OVFVirtualHwCdDrive)vHardware).isAutomaticAllocation()) != null)
					((OVFVirtualHwCdDrive) newVirtHardware).setAutomaticAllocation(((OVFVirtualHwCdDrive)vHardware).isAutomaticAllocation());	
				newVirtHwColl.add(newVirtHardware);
				break;
			case 17:
				newVirtHardware=new OVFVirtualHwDiskDrive();
				((OVFVirtualHwDiskDrive) newVirtHardware).setResourceType(17);
				if((Integer)((OVFVirtualHwDiskDrive)vHardware).getAddressOnParent() != null) 
					((OVFVirtualHwDiskDrive) newVirtHardware).setAddressOnParent(((OVFVirtualHwDiskDrive)vHardware).getAddressOnParent());
				if((Integer)((OVFVirtualHwDiskDrive)vHardware).getParent() != null) 
					((OVFVirtualHwDiskDrive) newVirtHardware).setParent(((OVFVirtualHwDiskDrive)vHardware).getParent());
				if(((OVFVirtualHwDiskDrive)vHardware).getHostResource() != null) {
					((OVFVirtualHwDiskDrive) newVirtHardware).setHostResource("ovf:/disk/"+disk.getId());
					//((OVFVirtualHwDiskDrive) newVirtHardware).setDisk(((OVFVirtualHwDiskDrive)vHardware).getDisk());
					((OVFVirtualHwDiskDrive) newVirtHardware).setDisk(disk);
				}
				newVirtHwColl.add(newVirtHardware);
				break;
			case 32:
				newVirtHardware = new OVFVirtualHwSharedDiskDrive();
				((OVFVirtualHwSharedDiskDrive) newVirtHardware).setResourceType(32);
				if((Integer)((OVFVirtualHwSharedDiskDrive)vHardware).getAddressOnParent() != null) 
					((OVFVirtualHwSharedDiskDrive) newVirtHardware).setAddressOnParent(((OVFVirtualHwSharedDiskDrive)vHardware).getAddressOnParent());
				if((Integer)((OVFVirtualHwSharedDiskDrive)vHardware).getParent() != null) 
					((OVFVirtualHwSharedDiskDrive) newVirtHardware).setParent(((OVFVirtualHwSharedDiskDrive)vHardware).getParent());
				if(((OVFVirtualHwSharedDiskDrive)vHardware).getHostResource() != null) {
					((OVFVirtualHwSharedDiskDrive) newVirtHardware).setHostResource(((OVFVirtualHwSharedDiskDrive)vHardware).getHostResource());
					((OVFVirtualHwSharedDiskDrive)newVirtHardware).setSharedDisk(((OVFVirtualHwSharedDiskDrive)vHardware).getDisk());
				}
				newVirtHwColl.add(newVirtHardware);
				break;	
			}
			if(newVirtHardware!=null){
				newVirtHardware.setRequired((Boolean.toString(vHardware.getRequired())==null || vHardware.getRequired()==true));
				if(vHardware.getDescription()!=null & newVirtHardware.getDescription()==null)newVirtHardware.setDescription(vHardware.getDescription());
				if(vHardware.getElementName()!=null & newVirtHardware.getElementName()==null )newVirtHardware.setElementName(vHardware.getElementName());
				if(((Integer) vHardware.getInstanceId()!=null) & ((Integer) newVirtHardware.getInstanceId()==null))newVirtHardware.setInstanceId(vHardware.getInstanceId());
			}
		}
		return newVirtHwColl;	
	}
	
	private static void recursiveDesc(Collection<ApplianceDescriptor> cad,String indent){
		int appNumber = 0;
		for(ApplianceDescriptor ad:cad){
			System.out.println("ApplianceDescriptor num: " + appNumber);
			System.out.println(indent+ad.getID());
			System.out.println(indent+" Networks:");
			for(OVFVirtualNetwork vn:ad.getAssociatedVirtualNetworks()){
				for(OVFVirtualHwEthernetAdapter vEa: vn.getAssociatedNics()){
					System.out.println(indent+"   "+vEa.getConnection());
				}
				System.out.println(indent+"  "+vn.getName());
			}
			recursiveDesc(ad.getAppliancesDescriptors(),"\t"+indent);
			appNumber++;
		}
	}
	
//  OLD MAIN	
//	public static void main(String[] args) throws NumberFormatException, FileNotFoundException,
//	XPathExpressionException, DOMException, ParserConfigurationException, SAXException, IOException,
//	URISyntaxException 
//	{		
//		ParserManager p = null;
//		ApplicationDescriptor appDesc = null;
//		ApplicationDescriptor appDesc2 = null;
//		long finish=0,finish2=0,start=0;
//		try
//		{
//
//			start=System.currentTimeMillis();
//			//			for(int i=0;i<10;i++)
//			//			OVFParser.ParseOVF(URI.create("src/main/resources/contrail_petstore.xml"));
//			//			OVFParser.ParseOVF(URI.create("src/main/resources/official_source.xml"));
//			finish2=System.currentTimeMillis()-start;
//			//p = new org.ow2.contrail.common.ParserManager("src/main/resources/dsl-test-xlab.ovf");
//			start=System.currentTimeMillis();
//			//for(int i=0;i<10;i++)
//			new OVFStAXParser().parseOVF(URI.create("src/main/resources/contrail_petstore.xml"));
//			new OVFStAXParser().parseOVF(URI.create("src/main/resources/official_source.xml"));
//			finish = System.currentTimeMillis()-start;
//			if (false) throw new XMLStreamException("hi!");
//
//			/*	
//			start=System.currentTimeMillis();
//			appDesc2=OVFParser.ParseOVF(URI.create("src/main/resources/contrail_petstore.xml"));
//			finish2=System.currentTimeMillis()-start;
//			 */	
//		}
//		catch (MalformedOVFException e)
//		{
//			e.printStackTrace();
//		}
//		catch (XMLStreamException e) {
//			//			e.printStackTrace();
//		}
//		System.out.println("completamento time STAX: "+ finish+" completamento time dom: "+finish2);
//		//		recursiveDesc(appDesc.getApplianceDescriptors(),"");
//		//		recursiveDesc(appDesc2.getApplianceDescriptors(),"");
//		//org.ow2.contrail.common.ParserManager p = new org.ow2.contrail.common.ParserManager("src/main/resources/ovf.xml");
//
//		//for (String appId : p.getAppliances())
//		//{
//		//	System.out.print("Appliance Id = ");
//		//	System.out.println(appId);
//		//	
//		//	for (File f : p.getApplianceImages(appId))
//		//	{	
//		//		System.out.println("Image URI = " + f.getUri() + " " + "Image Size = " + f.getSize());
//		//	}
//		//	
//		//	Collection<String> networks = p.getApplianceNetworks(appId);
//		//	if (networks.size() > 0)
//		//	{
//		//		System.out.print("Associated Networks = ");
//		//		for (String n : networks)
//		//		{	
//		//			System.out.println(n);
//		//		}
//		//	}
//		//	System.out.println();
//		//}
//	}
}
