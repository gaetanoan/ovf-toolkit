package org.ow2.contrail.common.implementation.ovf.item_processor;


import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFCpuProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {
		
		OVFVirtualHwCpu cpu = new OVFVirtualHwCpu();
		
		NodeList childs = xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:VirtualQuantity"))
			{
				cpu.setVirtualQuantity(Long.parseLong(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:AllocationUnits"))
			{	
				// TODO: do NOT use String, "allocation units" needs to be transformed
				cpu.setAllocationUnits(childs.item(i).getFirstChild().getTextContent().trim());
			}
			else if (childs.item(i).getNodeName().equals("rasd:ElementName"))
			{
				cpu.setElementName(childs.item(i).getTextContent().trim());
			}
		}
		
		return super.processCommonValues(cpu, xmlNode);
	}

}
