package org.ow2.contrail.common.implementation.application_extended.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.net.URI;

import java.util.Arrays;


import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;

import org.ow2.contrail.common.implementation.application_extended.ApplicationDescriptorExtended;
import org.ow2.contrail.common.implementation.application_extended.ApplicationDescriptorExtendedFactory;

import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

public class ApplicationDescriptorExtendedFactoryTester {

	
	
	public static SLATemplate slat;
	public static ApplicationDescriptorExtendedFactory adef;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ApplicationDescriptor appDescExt= ApplicationDescriptorExtended.newApplicationDescriptorExtended(
				new OVFStAXParser().parseOVF(URI.create("src/test/resources/contrail_petstore.xml")));
		adef=new ApplicationDescriptorExtendedFactory(appDescExt);
		SLASOITemplateParser slatParser=new SLASOITemplateParser();
		java.util.Scanner sca;
		try {
			sca = new java.util.Scanner(new java.io.File("src/test/resources/contrail_petstore_SLA.xml"));
			String content = sca.useDelimiter("\\Z").next();
			sca.close();
			slat=slatParser.parseTemplate(content);
			
		} catch (FileNotFoundException e) {
			
			fail("the Oracle is lost");
		}
	}


	@Test
	public void testGetDataCollectionOfQ() {
		ApplicationDescriptorExtended appDescExt=adef.getData(null);
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances())
			assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		appDescExt = adef.getData(Arrays.asList(slat));
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances()){
			if(ad.getID().equals("et2"))
				assertEquals(ad.getClass().getSimpleName(),"SLASOIGenericDecoration");
			else
				assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		}	
	}

}
