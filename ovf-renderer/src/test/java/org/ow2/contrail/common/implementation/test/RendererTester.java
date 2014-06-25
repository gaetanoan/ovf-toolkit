package org.ow2.contrail.common.implementation.test;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.contrail.common.implementation.Renderer;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
import org.ow2.contrail.common.implementation.ovf.OVFProductSection;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFStartupSection;
import org.ow2.contrail.common.implementation.ovf.OVFStartupSection.Item;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;

public class RendererTester {

	
	private static ApplicationDescriptor[] appdescs={null,null};
	
	
	private static void disksValidityPrecondition(Object[] first,Object[] second){
		assertEquals(first.length,second.length);
		for(int i=0;i<first.length;i++){
			assertEquals(((Disk)first[i]).getId(),(((Disk)second[i]).getId()));
			assertEquals(((Disk)first[i]).getParent(),(((Disk)second[i]).getParent()));
			assertEquals(((Disk)first[i]).getFile().getId(),(((Disk)second[i]).getFile().getId()));
			assertEquals(((Disk)first[i]).getFile().getSize(),(((Disk)second[i]).getFile().getSize()));
			assertEquals(((Disk)first[i]).getFile().getUri(),(((Disk)second[i]).getFile().getUri()));
		}
	}
	
	private static void virtualNetworkValidityPrecondition(Object[] first,
			Object[] second) {
		assertTrue(first.length==second.length);
		for(int i=0;i<first.length;i++){
			assertEquals(((OVFVirtualNetwork)first[i]).getName(),(((OVFVirtualNetwork)second[i]).getName()));
			assertEquals(((OVFVirtualNetwork)first[i]).getDescription(),(((OVFVirtualNetwork)second[i]).getDescription()));
			
			ArrayList<String> afirst=new ArrayList<String>();
			ArrayList<String> asecond=new ArrayList<String>();
			afirst.addAll(((OVFVirtualNetwork)first[i]).getIpAddress());
			asecond.addAll(((OVFVirtualNetwork)second[i]).getIpAddress());
			assertEquals(afirst.size(),asecond.size());
			for(int j=0;j<afirst.size();j++){
				assertEquals(afirst.get(j),(asecond.get(j)));
			}
			
		}
	}
	
	private static void ovfStartupSectionValidityPrecondition(
			OVFStartupSection first,OVFStartupSection second){
		ArrayList<Item> ifirst=new ArrayList<Item>();
		ArrayList<Item> isecond=new ArrayList<Item>();
		if(first!=null&&second!=null){
			ifirst.addAll(first.getItems());
			isecond.addAll(second.getItems());
			assertEquals(ifirst.size(),isecond.size());
			for(int i=0;i<ifirst.size();i++){
				assertEquals(ifirst.get(i).getId(),(isecond.get(i).getId()));
				assertEquals(ifirst.get(i).getOrder(),isecond.get(i).getOrder());
			}
		}else 
			assertEquals(first,second);
		
	}
	
	private static void ovfProductSectionValidityPrecondition(
			ArrayList<OVFProductSection> first,ArrayList<OVFProductSection> second){
		assertEquals(first.size(),second.size());
		for(int i=0;i<first.size();i++){
			
			assertEquals(first.get(i).getCategory(),(second.get(i).getCategory()));
			assertEquals(first.get(i).getClassName(),(second.get(i).getClassName()));
			assertEquals(first.get(i).getFullVersion(),(second.get(i).getFullVersion()));
			//assertEquals(first.get(i).getInfo(),(second.get(i).getInfo()));
			assertEquals(first.get(i).getProduct(),(second.get(i).getProduct()));
			assertEquals(first.get(i).getInstance(),(second.get(i).getInstance()));
			assertEquals(first.get(i).getProductUrl(),(second.get(i).getProductUrl()));
			assertEquals(first.get(i).getVendor(),(second.get(i).getVendor()));
			assertEquals(first.get(i).getVendorUrl(),(second.get(i).getVendorUrl()));
			assertEquals(first.get(i).getVersion(),(second.get(i).getVersion()));
			
			ArrayList<OVFProperty> arrFirst=new ArrayList<OVFProperty>();
			ArrayList<OVFProperty> arrSecond=new ArrayList<OVFProperty>();
			arrFirst.addAll(first.get(i).getProperties());
			arrSecond.addAll(second.get(i).getProperties());
			assertEquals(arrFirst.size(),arrSecond.size());
			
			for(int j=0;j<arrFirst.size();j++){
				 if(arrFirst.get(j).getDescription()!=null&&arrSecond.get(j).getDescription()!=null){
					 assertEquals(arrFirst.get(j).getDescription().getMsgid(),arrSecond.get(j).getDescription().getMsgid());
					 assertEquals(arrFirst.get(j).getDescription().getDescription(),
							 (arrSecond.get(j).getDescription().getDescription()));
				 }
				 else 
					 assertEquals(arrFirst.get(j).getDescription(),arrSecond.get(j).getDescription());
				 
				 assertEquals(arrFirst.get(j).getKey(),(arrSecond.get(j).getKey()));
				 
				 if(arrFirst.get(j).getLabel()!=null&&arrSecond.get(j).getLabel()!=null){
					 assertEquals(arrFirst.get(j).getLabel().getMsgid(),(arrSecond.get(j).getLabel().getMsgid()));
					 assertEquals(arrFirst.get(j).getLabel().getDescription(),
							 (arrSecond.get(j).getLabel().getDescription()));
				 }
				 else 
					 assertEquals(arrFirst.get(j).getLabel(),arrSecond.get(j).getLabel());
				 
				 assertEquals(arrFirst.get(j).getQualifiers(),(arrSecond.get(j).getQualifiers()));
				 assertEquals(arrFirst.get(j).getType(),(arrSecond.get(j).getType()));
				 assertEquals(arrFirst.get(j).getValue(),(arrSecond.get(j).getValue()));
				
				 assertEquals(arrFirst.get(j).isPassword(),arrSecond.get(j).isPassword()); 
				 assertEquals(arrFirst.get(j).isUserConfigurable(),arrSecond.get(j).isUserConfigurable());
				 assertEquals(arrFirst.get(j).getPassword(),arrSecond.get(j).getPassword());
				 			
			}
						
		}
		
	}
	

	private static void ovfVirtualSystemSectionValidityPrecondition(
			ArrayList<OVFVirtualSystem> first,
			ArrayList<OVFVirtualSystem> second) {
		// TODO Auto-generated method stub
		assertEquals(first.size(),second.size());
		for(int i=0;i<first.size();i++){
			assertEquals(first.get(i).getId(),(second.get(i).getId()));
			ArrayList<OVFVirtualHardware > arrFirst=new ArrayList<OVFVirtualHardware>();
			ArrayList<OVFVirtualHardware > arrSecond=new ArrayList<OVFVirtualHardware>();
			arrFirst.addAll(first.get(i).getRequiredHardware());
			arrSecond.addAll(second.get(i).getRequiredHardware());
			assertEquals(arrFirst.size(),arrSecond.size());
			
			for(int j=0;j<arrFirst.size();j++){
				assertEquals(arrFirst.get(j).toString(),arrSecond.get(j).toString());
			}
		}
		
		
	}
	
	private static void recursiveApplianceValidityPrecondition(
			Object[] first, Object[] second){
		assertEquals(first.length,second.length);
		for(int i=0;i<first.length;i++){
			assertEquals(((ApplianceDescriptor)first[i]).getID(),(
					((ApplianceDescriptor)second[i]).getID()));
			
			disksValidityPrecondition(((ApplianceDescriptor)first[i]).getDisks().toArray(),
					((ApplianceDescriptor)second[i]).getDisks().toArray());
			virtualNetworkValidityPrecondition(((ApplianceDescriptor)first[i]).getAssociatedVirtualNetworks().toArray(),
			((ApplianceDescriptor)second[i]).getAssociatedVirtualNetworks().toArray());
			ovfStartupSectionValidityPrecondition(
					(((ApplianceDescriptor)first[i]).getOVFStartupSection()), 
					(((ApplianceDescriptor)second[i]).getOVFStartupSection()));
			
		
			if(((ApplianceDescriptor)first[i]).getParent()!=null)
				assertEquals(((ApplianceDescriptor)first[i]).getParent().getID(),(
						((ApplianceDescriptor)second[i]).getParent().getID()));
			else 
				assertEquals(((ApplianceDescriptor)first[i]).getParent(),((ApplianceDescriptor)second[i]).getParent());
			
			ArrayList<OVFProductSection> arrFirst=new ArrayList<OVFProductSection>();
			ArrayList<OVFProductSection> arrSecond=new ArrayList<OVFProductSection>();
			arrFirst.addAll(((ApplianceDescriptor)first[i]).getProductSections());
			arrSecond.addAll(((ApplianceDescriptor)second[i]).getProductSections());
			ovfProductSectionValidityPrecondition(arrFirst,arrSecond);
			
			ArrayList<OVFVirtualSystem> arr1First=new ArrayList<OVFVirtualSystem>();
			ArrayList<OVFVirtualSystem> arr1Second=new ArrayList<OVFVirtualSystem>();
			arr1First.addAll(((ApplianceDescriptor)first[i]).getVirtualSystems());
			arr1Second.addAll(((ApplianceDescriptor)second[i]).getVirtualSystems());
			ovfVirtualSystemSectionValidityPrecondition(arr1First,arr1Second);
			
			
			recursiveApplianceValidityPrecondition(
					((ApplianceDescriptor)first[i]).getAppliancesDescriptors().toArray(),
					((ApplianceDescriptor)second[i]).getAppliancesDescriptors().toArray());
		}
		
	}
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		appdescs[0]=OVFParser.ParseOVF
				(URI.create("src/test/resources/contrail_petstore.xml"));
		appdescs[1]=OVFParser.previousOVFParser
				(URI.create("src/test/resources/contrail_petstore.xml"));
		/*check (OLD parser == StAXParser) == same data Structures */
		assertTrue(appdescs[0].getName().equals(appdescs[1].getName()));
		recursiveApplianceValidityPrecondition(
				(appdescs[0].getApplianceDescriptors().toArray()),
				(appdescs[1].getApplianceDescriptors().toArray()));
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		java.io.File file=new java.io.File("src/test/resources/renderer_result.xml");
		System.out.println("OVFRender test file elimination: "+file.delete());
	}


	@Test
	public void testRenderOVFApplicationDescriptorStringString() {
		try {
			Renderer.RenderOVF(appdescs[1], "src/test/resources", "renderer_result");
			ApplicationDescriptor test=OVFParser.ParseOVF(URI.create("src/test/resources/renderer_result.xml"));
			recursiveApplianceValidityPrecondition(test.getApplianceDescriptors().toArray(),
					appdescs[0].getApplianceDescriptors().toArray());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
		
	}

	

}
