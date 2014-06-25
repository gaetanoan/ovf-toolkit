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
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
/**
 * Instances of this class represent  all information of a OVF file.
 * 
 */
public class ApplicationDescriptor
{
	private Collection<ApplianceDescriptor> appliances;
	
	//private Collection<ApplianceStartupDescriptor> startupDescriptors;

	/**
	 * Creates a new instance of the <code>ApplicationDescriptor</code> type.
	 */
	public ApplicationDescriptor() 
	{
		appliances = new CopyOnWriteArrayList<ApplianceDescriptor>();
	//	startupDescriptors = new ArrayList<ApplianceStartupDescriptor>();
	}
	
	/**
	 * Gets root appliances (collection of ApplianceDescriptors mapping VirtualSystem 
	 * or VirtualSystemCollection direct children of Envelope node in OVF file).
	 * <br><br>
	 * Modifies to collection cause side effects to this object, leading to possible misbehaviour
	 * <br><br>
	 * e.g. an appliance added to collection with same unique_ovf_id of appliance already 
	 * 	present in collection. 
	 * <br>
	 * @return Collection<ApplianceDescriptor> root appliances
	 */
	public Collection<ApplianceDescriptor> getApplianceDescriptors() 
	{
		return appliances;
	}
	
	/**
	 * Add appliances to Appliances Collection of the Application
	 * 
	 * @param newAppliances
	 */
	public void addAppliances(Collection<ApplianceDescriptor> newAppliances) 
	{
		for(ApplianceDescriptor newAppl: newAppliances){
			appliances.add(newAppl);
		}
	}
	
	//FIXME testingPart (ovfPull creation)
	public ApplicationDescriptor(XMLEvent e, XMLEventReader r, OVFStAXParser sta) throws XMLStreamException, MalformedOVFException{
		this();
		boolean end = false;
		HashMap<String,File> file = null;
		HashMap<String,Disk> disk = null;
		HashMap<String,SharedDisk> sharedDisk = null;
		HashMap<String, OVFVirtualNetwork> network = null;
		/*HashMap<String, OVFVirtualSystem> vs= null;*/
		while(!end && r.hasNext()){
			e = r.nextEvent();
			end = e.isEndElement() && e.asEndElement().getName().getLocalPart().contentEquals("Envelope");
			if (!end){
				switch (e.getEventType()){
					case XMLEvent.START_ELEMENT:
						String tagName = e.asStartElement().getName().getLocalPart();
						if (tagName.contentEquals("References")){
							file = new HashMap<String,File>();
							sta.parseReferencesTag(file, e, r);
						}
						else if (tagName.contentEquals("DiskSection")){
							disk = new HashMap<String,Disk>();
							if (file==null) throw new MalformedOVFException("DiskSection must follow References in OVF");
							sta.parseDiskSectionTag(disk, file,e, r); 
						}
						else if (tagName.contentEquals("SharedDiskSection")){
							sharedDisk = new HashMap<String,SharedDisk>();
							if (file == null) throw new MalformedOVFException("DiskSection must follow References in OVF");
							sta.parseSharedDiskSectionTag(sharedDisk, file, e, r); 
						}
						else if (tagName.contentEquals("NetworkSection")){
							network=new HashMap<String, OVFVirtualNetwork>();
							sta.parseNetworkSectionTag(network,e,r);
						}
						
						else if (tagName.contentEquals("VirtualSystem")||tagName.contentEquals("VirtualSystemCollection")){
							Attribute curs = e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames + "id"));
							
							//System.out.println("Adding networks: "+ network.size());
							
							ApplianceDescriptor ad = new ApplianceDescriptor(curs.getValue().trim(), 
									null, 0, tagName.contentEquals("VirtualSystemCollection"), e, r,
									file, disk, network, null, sta, sharedDisk);
							
							appliances.add(ad);
						}
						break;
				}
			}
		}
	}
	//FIXME end ovfPull creation

	//	/**
//	 * Gets information about the startup ordering for this application.
//	 */
//	public Collection<ApplianceStartupDescriptor> getStartupDescriptor() 
//	{
//		return startupDescriptors;
//	}
//    
	
	/**
	 * Gets the name of the application. The name is the Id of the root appliance.
	 * 
	 * @return The name of the application expressed as a <code>String</code>.
	 */
	public String getName()
	{
		ApplianceDescriptor[] app = appliances.toArray(new ApplianceDescriptor[appliances.size()]);
		return app[0].getID();
		
		
	}
	
	/**
	 * Gets all Appliance Descriptors that mapping VirtualSystem this application is made of
	 * (in other words all Appliances of this Application whom <em>isVirtualCollection()</em>
	 *  method returns false ).
	 * 
	 * @return A <code>Collection<ApplianceDescriptor></code>
	 */
    public Collection<ApplianceDescriptor> getAllAppliances()
    {
		// add leaf nodes.
		Collection<ApplianceDescriptor> allAppliances = new ArrayList<ApplianceDescriptor>();
		for (ApplianceDescriptor app : appliances)
		{	
			addLeafNodes(app, allAppliances);
		}
        
		return allAppliances;
    }
		
    private void addLeafNodes(ApplianceDescriptor appliance, Collection<ApplianceDescriptor> allAppliances)
	{
		// base case: leaf node.
		if (appliance.getAppliancesDescriptors().size() == 0)
		{
			allAppliances.add(appliance);
		}
		else
		{
			// inductive case: search inside children nodes.
			for (ApplianceDescriptor app : appliance.getAppliancesDescriptors())
			{
				addLeafNodes(app, allAppliances);
			}	
		}
	}
}
