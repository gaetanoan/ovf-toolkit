package org.ow2.contrail.common.implementation.ovf.item_processor;



import org.ow2.contrail.common.implementation.ovf.OVFItemProcessor;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwParallelSCSI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OVFParallelSCSIProcessor extends OVFItemProcessor {

	@Override
	public OVFVirtualHardware processItem(Node xmlNode) throws DOMException {

		OVFVirtualHwParallelSCSI scsi = new OVFVirtualHwParallelSCSI();
		
		NodeList childs = xmlNode.getChildNodes();
		
		for (int i=0; i<childs.getLength(); i++)
		{	
			if (childs.item(i).getNodeName().equals("rasd:AddressOnParent"))
			{
				scsi.setAddress(Long.parseLong(childs.item(i).getTextContent()));
			}
			else if (childs.item(i).getNodeName().equals("rasd:ResourceSubType"))
			{	
				scsi.setResourceSubType(childs.item(i).getTextContent().trim());
			}
		}
		
		return super.processCommonValues(scsi, xmlNode);
	}

}
