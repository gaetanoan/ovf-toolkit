package org.ow2.contrail.common.implementation.ovf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCdDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwFloppyDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwIdeController;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwParallelSCSI;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwSharedDiskDrive;

public class OVFVirtualSystem {

	private String id;
	private Collection<OVFVirtualHardware> vh;
	
	public OVFVirtualSystem(String lid)
	{
		id = lid;
		vh = new ArrayList<OVFVirtualHardware>();
	}
	
	public OVFVirtualSystem(String lid, Collection<OVFVirtualHardware> vhard){
		id = lid;
		vh = new ArrayList<OVFVirtualHardware>(vhard);
	}
	
	public String getId() {
		return id;
	}
	
	public Collection<OVFVirtualHardware> getRequiredHardware()
	{
		return vh;
	}
	
	
	
	//FIXME StAXParser start
	public OVFVirtualSystem(String id2, HashMap<String, Disk> disk, HashMap<String, SharedDisk> shared_disk, XMLEvent e, XMLEventReader r) throws XMLStreamException {
		this(id2);
		boolean end=false;
		HashMap<String,String> itemAttr=new HashMap<String,String>();
		HashMap<String,String> rasdAttr=new HashMap<String,String>();
		ArrayList<String> connections=new ArrayList<String>();
		while(!end&&r.hasNext()){
			e=r.nextEvent();
			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("VirtualHardwareSection"));
			if(!end){
				switch(e.getEventType()){
					case XMLEvent.START_ELEMENT:
						if(e.asStartElement().getName().getLocalPart().contentEquals("Item")){
							Iterator<?> itra= e.asStartElement().getAttributes();
							while(itra.hasNext()){
								Attribute toIns=((Attribute)(itra.next()));
								itemAttr.put(toIns.getName().getLocalPart(), toIns.getValue());
							}
						}else if(e.asStartElement().getName().getNamespaceURI().contentEquals("http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData")
								&& !e.asStartElement().getName().getLocalPart().contentEquals("Connection")){
							String rasd=e.asStartElement().getName().getLocalPart();
							String value=r.getElementText().trim();
							rasdAttr.put(rasd, value);
						}else if(e.asStartElement().getName().getNamespaceURI().contentEquals("http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData")
								&& e.asStartElement().getName().getLocalPart().contentEquals("Connection")){
							connections.add(r.getElementText().trim());
						}
					;break;
					case XMLEvent.END_ELEMENT:
						if(e.asEndElement().getName().getLocalPart().contentEquals("Item")){
							
							OVFVirtualHardware cursvh=null;
							/*TODO conforming to StAX Parsing each OVFVirtualHardware component
							 * should parse its own attributes, instead to delegate  OVFVirtualSystem*/
							switch(Integer.parseInt(rasdAttr.get("ResourceType"))){
							case 3:
								cursvh= new OVFVirtualHwCpu();
								if(rasdAttr.get("VirtualQuantity")!=null)
									((OVFVirtualHwCpu)(cursvh))
									.setVirtualQuantity(Long.parseLong(rasdAttr.get("VirtualQuantity")));
								if(rasdAttr.get("AllocationUnits")!=null)
									((OVFVirtualHwCpu)(cursvh))
									.setAllocationUnits(rasdAttr.get("AllocationUnits"));
								break;
							case 4:
								cursvh=new OVFVirtualHwMemory();
								if(rasdAttr.get("VirtualQuantity")!=null)
									((OVFVirtualHwMemory)(cursvh))
									.setVirtualQuantity(Long.parseLong(rasdAttr.get("VirtualQuantity")));
								if(rasdAttr.get("AllocationUnits")!=null)
									((OVFVirtualHwMemory)(cursvh))
									.setAllocationUnits(rasdAttr.get("AllocationUnits"));
								break;
							case 5:
								cursvh= new OVFVirtualHwIdeController();
								if(rasdAttr.get("Address")!=null)
									((OVFVirtualHwIdeController)(cursvh))
									.setAddress(Long.parseLong(rasdAttr.get("Address")));
								break;
							case 6:
								cursvh= new OVFVirtualHwParallelSCSI();
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwParallelSCSI)(cursvh))
									.setAddress(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("ResourceSubType")!=null)
									((OVFVirtualHwParallelSCSI)(cursvh))
									.setResourceSubType(rasdAttr.get("ResourceSubType"));
								break;
							case 10:
								cursvh=new OVFVirtualHwEthernetAdapter();
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwEthernetAdapter)cursvh)
									.setAddressOnParent(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("AutomaticAllocation")!=null)
									((OVFVirtualHwEthernetAdapter)cursvh)
									.setAutomaticAllocation(Boolean.parseBoolean(rasdAttr.get("AutomaticAllocation")));
								if(rasdAttr.get("ResourceSubType")!=null)
									((OVFVirtualHwEthernetAdapter)cursvh)
									.setResourceSubType(rasdAttr.get("ResourceSubType"));
								String conn="";
								for(String c:connections)conn+=c+" ";
								if(conn.length()>0)((OVFVirtualHwEthernetAdapter)cursvh).setConnection(conn.trim());
								break;
							case 14:
								cursvh= new OVFVirtualHwFloppyDrive();
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwFloppyDrive)cursvh)
									.setAddressOnParent(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("AutomaticAllocation")!=null)
									((OVFVirtualHwFloppyDrive)cursvh)
									.setAutomaticAllocation(Boolean.parseBoolean(rasdAttr.get("AutomaticAllocation")));
								break;
							case 15:
								cursvh=new OVFVirtualHwCdDrive(); 
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwCdDrive)cursvh)
									.setAddressOnParent(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("Parent")!=null)
									((OVFVirtualHwCdDrive)cursvh)
									.setParent(Integer.parseInt(rasdAttr.get("Parent")));
								if(rasdAttr.get("AutomaticAllocation")!=null)
									((OVFVirtualHwCdDrive)cursvh)
									.setAutomaticAllocation(Boolean.parseBoolean(rasdAttr.get("AutomaticAllocation")));
								break;
							case 17:
								cursvh=new OVFVirtualHwDiskDrive();
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwDiskDrive)cursvh)
									.setAddressOnParent(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("Parent")!=null)
									((OVFVirtualHwDiskDrive)cursvh)
									.setParent(Integer.parseInt(rasdAttr.get("Parent")));
								if(rasdAttr.get("HostResource")!=null){
									((OVFVirtualHwDiskDrive)cursvh)
									.setHostResource(rasdAttr.get("HostResource"));
									((OVFVirtualHwDiskDrive)cursvh).setDisk(disk.get(rasdAttr.get("HostResource")));
								}
								break;
							case 32:
								cursvh = new OVFVirtualHwSharedDiskDrive();
								if(rasdAttr.get("AddressOnParent")!=null)
									((OVFVirtualHwSharedDiskDrive)cursvh)
									.setAddressOnParent(Integer.parseInt(rasdAttr.get("AddressOnParent")));
								if(rasdAttr.get("Parent")!=null)
									((OVFVirtualHwSharedDiskDrive)cursvh)
									.setParent(Integer.parseInt(rasdAttr.get("Parent")));
								if (rasdAttr.get("HostResource") != null){
									((OVFVirtualHwSharedDiskDrive)cursvh).setHostResource(rasdAttr.get("HostResource"));
									SharedDisk s = null;
									s = shared_disk.get(rasdAttr.get("HostResource"));
									if (s == null){
										String toSplit = rasdAttr.get("HostResource");
										String[] splitted = toSplit.split("\\/");
										System.out.println(splitted[2]);
										s = shared_disk.get(splitted[2]);
									}
									((OVFVirtualHwSharedDiskDrive)cursvh).setSharedDisk(s);
								}
							}
							
						if(cursvh!=null){
							//TODO parse generic data
							cursvh.setRequired((itemAttr.get("required")==null || itemAttr.get("required").contentEquals("true") ) );
							if(rasdAttr.get("Description")!=null)cursvh.setDescription(rasdAttr.get("Description"));
							if(rasdAttr.get("ElementName")!=null)cursvh.setElementName(rasdAttr.get("ElementName"));
							if(rasdAttr.get("InstanceID")!=null)cursvh.setInstanceId(Integer.parseInt(rasdAttr.get("InstanceID")));
							if(rasdAttr.get("ResourceType")!=null)cursvh.setResourceType(Integer.parseInt(rasdAttr.get("ResourceType")));
							
							vh.add(cursvh);
						}
						
						rasdAttr.clear();
						itemAttr.clear();
						connections.clear();
						
						}
						break;
				}
		// TODO Auto-generated constructor stub
			}
		}
	}
	//FIXME StAxParser end
	
}
