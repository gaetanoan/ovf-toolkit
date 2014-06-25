package org.ow2.contrail.common.implementation.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;
import org.ow2.contrail.common.implementation.ovf.OVFProductSection;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFStartupSection;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwSharedDiskDrive;
// import mascoptLib.graphs.Vertex;

/**
 * Instances of this class represent an VirtualSystem or a VirtualSystemCollection of a OVF file.
 *  
 *
 */
public class ApplianceDescriptor
{
	private String applianceURI;
	private boolean isVirtualCollection;
	private String id;
	private long size;

	private int defaultVmNumber;

	private ApplianceDescriptor parent;

	private Collection<OVFVirtualSystem> virtualSystems;

	private Collection<OVFVirtualNetwork> virtualNetworks;

	private Collection<OVFProductSection> productSections;

	private Collection<ApplianceDescriptor> childAppliances;

	private Collection<SharedDisk> sharedDisks = null;

	private OVFStartupSection startupSection;

	public ApplianceDescriptor(String lid, String luri, long lsize, boolean vc)
	{
		id = lid;
		applianceURI = luri;
		size = lsize;
		isVirtualCollection = vc;

		virtualSystems = new ArrayList<OVFVirtualSystem>();
		virtualNetworks = new ArrayList<OVFVirtualNetwork>();

		childAppliances = new CopyOnWriteArrayList<ApplianceDescriptor>();
		productSections = new ArrayList<OVFProductSection>();
		defaultVmNumber = 10;
	}
	
	/**
	 * It returns the default number of VMs for the VirtualSystem
	 * representing this appliance
	 * @return integer
	 */
	public int getDefaultVmNumber() {
		return defaultVmNumber;
	}

	/**
	 * It sets the default number of VMs for the 
	 * VirtualSystem representing this appliance
	 * @return integer
	 */
	public void setDefaultVmNumber(int defaultVmNumber) {
		this.defaultVmNumber = defaultVmNumber;
	} 

	/**
	 * Gets the URI of the appliance.
	 * 
	 * @return A <code>String</code>.
	 */
	public String getApplianceURI()
	{
		return applianceURI;
	}

	/**
	 * Gets the appliance identifier.
	 * 
	 * @return An identifier expressed as a <code>String</code>.
	 */
	public String getID()
	{
		return id;
	}

	/**
	 * Gets the image size.
	 * 
	 * @return <code>long</code>
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * Gets the virtual systems associated with the OVF.
	 * 
	 * @return A collection of <code>OVFVirtualSystems</code>.
	 */
	public Collection<OVFVirtualSystem> getVirtualSystems()
	{
		return virtualSystems;
	}

	/**
	 * Get the virtual networks associated with the appliance.
	 * 
	 * @return A collection of <code>OVFVirtualNetwork</code>.
	 */
	public Collection<OVFVirtualNetwork> getAssociatedVirtualNetworks()
	{
		return virtualNetworks;
	}

	/**
	 * Gets the appliances this appliance is made of.
	 * 
	 * @return A <code>Collection</code> of <code>ApplianceDescriptor</code>.
	 */
	public Collection<ApplianceDescriptor> getAppliancesDescriptors()
	{
		return childAppliances;
	}

	/**
	 * Gets the product sections associated with this appliance.
	 * 
	 * @return A <code>Collection</code> of <code>OVFProductSection</code>.
	 */
	public Collection<OVFProductSection> getProductSections()
	{
		return this.productSections;
	}

	public void addProductSection(OVFProductSection section)
	{
		this.productSections.add(section);
	}
	
	/**
	 * Gets the disk images used by this appliance.
	 * 
	 * @return A <code>Collection</code> of <code>Disk</code>.
	 */
	public Collection<Disk> getDisks()
	{
		Collection<Disk> result = new ArrayList<Disk>();

		// this
		for (OVFVirtualSystem vs : virtualSystems)
		{
			for (OVFVirtualHardware vh : vs.getRequiredHardware())
			{
				if (vh.getResourceType() == 17)
				{
					result.add(((OVFVirtualHwDiskDrive) vh).getDisk());
				}
			}
		}

		// children
		for (ApplianceDescriptor child : childAppliances)
		{
			result.addAll(child.getDisks());
		}

		return result;
	}
	
	public Collection<SharedDisk> getSharedDisks()
	{
		Collection<SharedDisk> result = new ArrayList<SharedDisk>();

		// this
		for (OVFVirtualSystem vs : virtualSystems)
		{
			for (OVFVirtualHardware vh : vs.getRequiredHardware())
			{
				if (vh.getResourceType() == 32)
				{
					result.add(((OVFVirtualHwSharedDiskDrive) vh).getDisk());
				}
			}
		}

		// children
		for (ApplianceDescriptor child : childAppliances)
		{
			result.addAll(child.getSharedDisks());
		}

		return result;
	}

	/**
	 * Check if this element represent a virtual collection.
	 * 
	 * @return A <code>boolean</code> value, <code>true</code> if the appliance is a virtual collection, <code>false</code> otherwise.
	 */
	public boolean isVirtualCollection()
	{
		return this.isVirtualCollection;
	}

	/**
	 * Gets the startup information associated with this appliance.
	 * 
	 * @return A <code>OVFStartupSection</code>
	 */
	public OVFStartupSection getOVFStartupSection()
	{
		return startupSection;
	}

	/**
	 * Sets the startup information associated with this appliance.
	 * 
	 * @param value
	 *            The <code>OVFStartupSection</code> that has to be set.
	 */
	public void setOVFStartupSection(OVFStartupSection value)
	{
		startupSection = value;
	}

	/**
	 * Gets all the properties of this appliance including those inherited.
	 * 
	 * @return A <code>Collection</code> of <code>OVFProperty</code>.
	 */
	public Collection<OVFProperty> getAllProperties()
	{
		Collection<OVFProperty> allProperty = new ArrayList<OVFProperty>();
		allProperty.addAll(this.getProperties());
		if (parent != null)
		{
			Collection<OVFProperty> tempProp = parent.getAllProperties();
			boolean contain = false;
			for (OVFProperty prop : tempProp)
			{
				for (OVFProperty allProp : allProperty)
				{
					if (allProp.getKey().equals(prop.getKey()))
						contain = true;
				}
				if (!contain)
				{
					allProperty.add(prop);
				}
				contain = false;
			}
		}
		return allProperty;
	}

	/**
	 * Gets only the property of this specific Appliance
	 * 
	 * @return A <code>Collection</code> of <code>OVFProperty</code>
	 */
	public Collection<OVFProperty> getProperties()
	{
		Collection<OVFProperty> result = new ArrayList<OVFProperty>();
		for (OVFProductSection section : this.productSections)
		{
			// FIXME: if properties are duplicated, what to do ?
			result.addAll(section.getProperties());
		}
		return result;
	}

	/**
	 * Sets the parent of the specific appliance.
	 * 
	 * @param parent
	 *            An <code>ApplianceDescriptor</code> representing the enclosing appliance.
	 */
	public void setParent(ApplianceDescriptor parent)
	{
		/*TODO fine-grained control ?? */ 
		this.parent = parent;
	}

	/**
	 * Gets the parent appliance.
	 * 
	 * @return An <code>ApplianceDescriptor</code> for the parent appliance.
	 */
	public ApplianceDescriptor getParent()
	{
		return parent;
	}

	/**
	 * Get the virtual network given its name.
	 * 
	 * @param networkName
	 *            The name of the network expressed as a <code>String</code>.
	 * @return A <code>OVFVirtualNetwork</code>.
	 */

	public OVFVirtualNetwork getVirtualNetworkByName(String networkName)
	{
		for (OVFVirtualNetwork n : virtualNetworks)
		{
			if (n.getName().equals(networkName))
			{
				return n;
			}
		}

		return null;
	}
	
	@Deprecated 
	public void addVirtualNetworkToAppliance(OVFVirtualNetwork net){
		virtualNetworks.add(net);
		
	}
	public void linkEthAdapterToNetwork(OVFVirtualHwEthernetAdapter eth, String networkName) throws MalformedOVFException
    {
        OVFVirtualNetwork n = null;
        n = getVirtualNetworkByName(networkName);
        if (n == null) 
        	throw new MalformedOVFException("Cannot find a Network corresponding to the one specified in the Hardware Section");
        n.getAssociatedNics().add(eth);
    }
	
	/***
	 * gets total number of CPU, recursively goes down on children appliances
	 * @return  a <em>OVFVirtualHwCpu<em> instance containing required number of CPU
	 * 	and allocationUnits, other values are not expressive.
	 * 
	 * @throws InvalidArgumentException if allocation allocation unit info is not the same in visited appliance
	 */
	public OVFVirtualHwCpu getRequiredCPU(){
		OVFVirtualHwCpu vc=new OVFVirtualHwCpu();
		vc.setResourceType(3);
		vc.setDescription("Total Amount of cpu required by "+this.getID());
		vc.setElementName("Total Number of cpu");
		fillOVFVS(vc,getVirtualSystems());
		fillAd(vc,getAppliancesDescriptors());
		return vc;
	}
	
	/***
	 * gets total amount of memory, recursively goes down on children appliances
	 * @return  a <em>OVFVirtualHwMemory<em> instance containing required total amount of memory
	 * 	 in kilobytes other values are not expressive.
	 * 
	 * @throws numberFormatException 
	 * @throw IllegalArgumentException  if in appliance descriptor hierarchies exists at least one
	 *    appliance that is not in programmatic unit form "byte * 2^exp" as defined in CIM
	 */
	public OVFVirtualHwMemory getRequiredMemory(){
		OVFVirtualHwMemory vm=new OVFVirtualHwMemory();
		vm.setAllocationUnits("byte * 2^10");
		vm.setResourceType(4);
		vm.setDescription("Total Amount of memory required by: "+this.getID());
		vm.setElementName("Total Amount of memory");
		fillOVFVS(vm,getVirtualSystems());
		fillAd(vm,getAppliancesDescriptors());
		return vm;
		
	}
	
	private static void fillAd(OVFVirtualHardware vm,Collection<ApplianceDescriptor> cad){
		for(ApplianceDescriptor ad:cad){
			fillOVFVS(vm,ad.getVirtualSystems());
			fillAd(vm,ad.getAppliancesDescriptors());
		}
	}
	
	private static<E extends OVFVirtualHardware> void fillOVFVS(E vm,Collection<OVFVirtualSystem> cvs  ){
		if(cvs.iterator().hasNext()){
		//for(OVFVirtualSystem vs:cvs){
			OVFVirtualSystem vs=cvs.iterator().next();
			if(vm.getResourceType()==4)
				fillReqHwMemory((OVFVirtualHwMemory)vm,vs.getRequiredHardware());
			if(vm.getResourceType()==3)
				fillReqHwCpu((OVFVirtualHwCpu)vm,vs.getRequiredHardware());
		}
	}
	
	private static void fillReqHwCpu(OVFVirtualHwCpu vm,Collection<OVFVirtualHardware> cvh){
		for(OVFVirtualHardware vs:cvh){
			if(vs.getResourceType()==3){
				vm.setVirtualQuantity(vm.getVirtualQuantity()
						+((OVFVirtualHwCpu)vs).getVirtualQuantity());
				//FIXME allowed only uniform measure unit over all appliance descriptor and its children
				if(vm.getAllocationUnits()==null)vm.setAllocationUnits(((OVFVirtualHwCpu)vs).getAllocationUnits());
				else if(!vm.getAllocationUnits().equals(((OVFVirtualHwCpu)vs).getAllocationUnits()))
					throw new IllegalArgumentException("cpu unit not uniform");
			}
		}
	}
	
	private static void fillReqHwMemory(OVFVirtualHwMemory vm,Collection<OVFVirtualHardware> cvh){
		for(OVFVirtualHardware vs:cvh){
			if(vs.getResourceType()==4){
				String[] toSplit=((OVFVirtualHwMemory)vs).getAllocationUnits().split(" ");
				if(toSplit.length>=3){
					//FIXME retrieve information only on programmatic unit as defined in CIM
					//in format: byte * 2^exp other format will throw an exception
					String[] expNote=toSplit[2].split("\\^");
					if(expNote.length>=2){
						long qmem=(long)(Math.floor( 
							 ((OVFVirtualHwMemory)vs).getVirtualQuantity() * 
							  	Math.pow(2, ((Long.parseLong(expNote[1])-10)))));
						vm.setVirtualQuantity(vm.getVirtualQuantity()+qmem);
					}else throw new IllegalArgumentException("programmatic unit not recongized");
				}else throw new IllegalArgumentException("separation format not recognized");
			}
		}
	}
	
	@Override
	public String toString(){
		return this.id;
	}
	//FIXME constructor for StAX
	//TODO there is a better way to retrieve OVFVirtualSystem Informations: using shittyFix and a counter to recognize
	// many VirtualHardwareSection configurations of a VirtualSystem  but an underling problem remains: 
	// there's no standard way to reference a particular
	// VirtualHardwareSection configuration through an OVF file, furthermore id attribute of VirtualHardwareSection is not mandatory. 
	
	//private HashMap<String,OVFVirtualSystem> shittyFix;
	//private int intvh=0;
	/***
	 * Builds an ApplianceDescriptor object parsing either VirtualSystem or 
	 * VirtualSystemCollection section of an OVF file
	 * using a StAX parser. This constructor is intended to be invoked only through ApplicationDescriptor StAX constructor    
	 * 
	 * @param value
	 * @param luri
	 * @param i
	 * @param b
	 * @param e
	 * @param r
	 * @param file
	 * @param disk
	 * @param net
	 * @param vars
	 * @param sta
	 * @param sd
	 * @throws XMLStreamException
	 */
	public ApplianceDescriptor(String value, String luri, int i,boolean b,
			XMLEvent e, XMLEventReader r, HashMap<String, File> file, HashMap<String, Disk> disk, 
			/*HashMap<String, OVFVirtualSystem> vs,*/HashMap<String,OVFVirtualNetwork> net, HashMap<String, OVFProperty> vars, 
			OVFStAXParser sta, HashMap<String, SharedDisk> sd) throws XMLStreamException, MalformedOVFException {
		this(value, luri, i, b);
		
		/*
		if (sd != null)
			this.sharedDisks = sd.values();
		*/
		
		//shittyFix=new HashMap<String,OVFVirtualSystem>();
		HashMap<String,OVFProperty>curVar=new HashMap<String,OVFProperty>();
		
		if (vars!=null)
			curVar.putAll(vars);
		
		boolean end = false;
		while(!end && r.hasNext()){
			e=r.nextEvent();
			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("VirtualSystemCollection"))
					|| (e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("VirtualSystem"));
			if(!end)
			switch (e.getEventType()) {
			case XMLEvent.START_ELEMENT:
				
				String tagName=e.asStartElement().getName().getLocalPart();
				if (tagName.contentEquals("VirtualSystem")||tagName.contentEquals("VirtualSystemCollection")){
					Attribute curs=
							e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames + "id"));
					ApplianceDescriptor neo = new ApplianceDescriptor(curs.getValue().trim(),null,0,
							tagName.contentEquals("VirtualSystemCollection"),e,r,file, disk, net, curVar, sta, sd);
					neo.setParent(this);
					this.getAppliancesDescriptors().add(neo);
				}
				else if (tagName.contentEquals("ProductSection")) {
					OVFProductSection section = new OVFProductSection(e,r,curVar,
							e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames + "class")),
							e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames + "instance")),net,this,sta);
					this.getProductSections().add(section);
				}
				else if(tagName.contentEquals("StartupSection")){
					this.startupSection=new OVFStartupSection(e,r);
				}
				else if(tagName.contentEquals("VirtualHardwareSection")){
					//String ideVH=(e.asStartElement().getAttributeByName(
					//	QName.valueOf(OVFStAXParser.ovfNames+"id"))==null?""+intvh:
					//		e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"id")).getValue()); 
					//++intvh;
					OVFVirtualSystem ovfvsys = new OVFVirtualSystem(this.id, disk, sd, e, r);
					virtualSystems.add(ovfvsys);
					//shittyFix.put(ideVH, ovfvsys); 
				}
				break;
			}
		}
		
	}
	//FIXME end constructor for StAX	
}

