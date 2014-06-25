package org.ow2.contrail.common.implementation.ovf.item_processor;



import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFEthernetAdapterProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {
		
		OVFVirtualHwEthernetAdapter eth = new OVFVirtualHwEthernetAdapter();
		
		NodeList childs = xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:AddressOnParent"))
			{
				eth.setAddressOnParent(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:AutomaticAllocation"))
			{	
				eth.setAutomaticAllocation(Boolean.parseBoolean(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:Connection"))
			{	
				eth.setConnection(childs.item(i).getTextContent().trim());
			}
			else if (childs.item(i).getNodeName().equals("rasd:ResourceSubType"))
			{	
				eth.setResourceSubType(childs.item(i).getTextContent().trim());
			}
		}
		
		return super.processCommonValues(eth, xmlNode);
	}
}
