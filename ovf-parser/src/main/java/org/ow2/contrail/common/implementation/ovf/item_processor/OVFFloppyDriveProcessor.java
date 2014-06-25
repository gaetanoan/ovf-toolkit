package org.ow2.contrail.common.implementation.ovf.item_processor;



import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwFloppyDrive;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFFloppyDriveProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {
		
		OVFVirtualHwFloppyDrive floppy = new OVFVirtualHwFloppyDrive();
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:AddressOnParent"))
			{
				floppy.setAddressOnParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:AutomaticAllocation"))
			{	
				floppy.setAutomaticAllocation(Boolean.parseBoolean(childs.item(i).getTextContent()));
			}		
		}
		
		return super.processCommonValues(floppy, xmlNode);
		
	}

}
