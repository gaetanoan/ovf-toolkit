package org.ow2.contrail.common.implementation.ovf;

import org.w3c.dom.Node;

import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

public abstract class OVFItemProcessor {

	public abstract OVFVirtualHardware processItem(Node xmlNode) throws DOMException;
	
	protected OVFVirtualHardware processCommonValues(OVFVirtualHardware ovfVH, Node xmlNode)
	{
		// check if this virtual hardware requirement is mandatory.
		Node required = xmlNode.getAttributes().getNamedItem("ovf:required");
		if (required != null)
		{
			if (required.getNodeValue().equalsIgnoreCase("true")) 
				ovfVH.setRequired(true);
			else
				ovfVH.setRequired(false);
		}
		else
		{
			ovfVH.setRequired(true);
		}
		
		NodeList childs =  xmlNode.getChildNodes();
		
		for (int i=0;i<childs.getLength();i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:Description"))
			{
				ovfVH.setDescription(childs.item(i).getTextContent().trim());
			}
			else if (childs.item(i).getNodeName().equals("rasd:ElementName"))
			{
				ovfVH.setElementName(childs.item(i).getTextContent().trim());
			}
			else if (childs.item(i).getNodeName().equals("rasd:InstanceID"))
			{
				ovfVH.setInstanceId(Integer.parseInt(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:ResourceType"))
			{
				ovfVH.setResourceType(Integer.parseInt(childs.item(i).getTextContent()));
			}
		}
		
		return ovfVH;	
	}
}
