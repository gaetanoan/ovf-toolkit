package org.ow2.contrail.common.implementation.ovf.item_processor;



import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwSharedDiskDrive;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFSharedDiskDriveProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {

		OVFVirtualHwSharedDiskDrive disk = new OVFVirtualHwSharedDiskDrive();
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:AddressOnParent"))
			{
				disk.setAddressOnParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:Parent"))
			{
				disk.setParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:HostResource"))
			{	
				disk.setHostResource(childs.item(i).getTextContent());
			}
			
		}
		
		return super.processCommonValues(disk, xmlNode);
	}

}
