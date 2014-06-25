package org.ow2.contrail.common.implementation.ovf.item_processor;

import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwIdeController;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFIdeControllerProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {
		
		OVFVirtualHwIdeController ide = new OVFVirtualHwIdeController();
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0;i<childs.getLength();i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:Address"))
			{
				ide.setAddress(Long.parseLong(childs.item(i).getTextContent()));
			}
		}
		
		return super.processCommonValues(ide, xmlNode);
	}

}
