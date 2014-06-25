package org.ow2.contrail.common.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class TestRequiredMethod {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws DOMException 
	 * @throws XPathExpressionException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, XPathExpressionException, DOMException, IOException, ParserConfigurationException, SAXException, URISyntaxException {
		// TODO Auto-generated method stub
		org.ow2.contrail.common.ParserManager p = null;
		try
		{
			p = new org.ow2.contrail.common.ParserManager("src/main/resources/contrail_petstore.xml");
		}
		catch (MalformedOVFException e)
		{
			e.printStackTrace();
		}
		recursiveAppliance(p.getApplication().getApplianceDescriptors().iterator().next());
		
	}
	
	private static void recursiveAppliance(ApplianceDescriptor ad){
		System.out.println(ad.getID()+": #cpu "+ad.getRequiredCPU().getVirtualQuantity());
		System.out.println(ad.getID()+": mem(kb) "+ad.getRequiredMemory().getVirtualQuantity());
		for(ApplianceDescriptor ad1:ad.getAppliancesDescriptors()){
			recursiveAppliance(ad1);
		}
		
	}

}
