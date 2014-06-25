package org.ow2.contrail.common.implementation.ovf.item_processor;



import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFMemoryProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {

		OVFVirtualHwMemory mem = new OVFVirtualHwMemory();
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:VirtualQuantity"))
			{
				mem.setVirtualQuantity(Long.parseLong(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:AllocationUnits"))
			{	
				// TODO: do NOT user String, "allocation units" needs to be transformed.
				mem.setAllocationUnits(childs.item(i).getTextContent().trim());
				
			}
			else if (childs.item(i).getNodeName().equals("rasd:ElementName"))
			{
				mem.setElementName(childs.item(i).getTextContent().trim());
			}
		}
		
		return super.processCommonValues(mem, xmlNode);
	}

}
