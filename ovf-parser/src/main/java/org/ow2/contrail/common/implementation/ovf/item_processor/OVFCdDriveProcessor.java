package org.ow2.contrail.common.implementation.ovf.item_processor;


import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCdDrive;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFCdDriveProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {

		OVFVirtualHwCdDrive cd = new OVFVirtualHwCdDrive();
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++) 
		{	
			if (childs.item(i).getNodeName().equals("rasd:AddressOnParent"))
			{
				cd.setAddressOnParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:Parent"))
			{
				cd.setParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:AutomaticAllocation"))
			{	
				cd.setAutomaticAllocation(Boolean.parseBoolean(childs.item(i).getTextContent()));
			}		
		}
		
		return super.processCommonValues(cd, xmlNode);
	}
}
