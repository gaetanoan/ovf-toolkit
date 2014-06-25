package org.ow2.contrail.common.implementation.application.test;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;

public class ApplicationDescriptorTester {

	
	private static ApplicationDescriptor[] appDesc= {null,null};
	
	/*FIXME Any Changes in these files can lead to  misbehaviour in 
	 * testGetApplianceVirtualSystem() results*/
	private static String OVFtest[]={"src/test/resources/contrail_petstore.xml"
		,"src/test/resources/small-vm.ovf"};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		OVFStAXParser stax=new OVFStAXParser();
		appDesc[0]=stax.parseOVF(URI.create(OVFtest[0]));
		appDesc[1]=stax.parseOVF(URI.create(OVFtest[1]));
	}

	
	private void commonFactor( boolean str,int i){
		if(i<2&&i>=0){
			assertTrue(appDesc[i].getApplianceDescriptors().size()==1
					&&((ApplianceDescriptor)appDesc[i].getApplianceDescriptors().
							toArray()[0]).getID().equals( !str?(i==0?
									"ContrailPetStore":"Small VM"):appDesc[i].getName()));
			commonFactor(str,++i);
		}
		
	}
	
	@Test
	public void testGetApplianceDescriptors() {
		commonFactor(false,0);
	}

	@Test
	public void testGetName() {
		/*Application and first Appliance must have same name*/
		commonFactor(true,0);		
	}

	@Test
	public void testGetAllAppliances() {
		String[]res={"WebTieret1et2et3et4DB_frontendDB1DB2","Small VM"};
		for(int i=0;i<2;i++){
			StringBuffer sb=new StringBuffer();
			for(ApplianceDescriptor aa:appDesc[i].getAllAppliances())sb.append(aa.getID());
			assertEquals(res[i],sb.toString());
		}	
	}
}
