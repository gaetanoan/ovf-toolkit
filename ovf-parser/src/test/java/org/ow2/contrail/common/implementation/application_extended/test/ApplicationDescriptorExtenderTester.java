package org.ow2.contrail.common.implementation.application_extended.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application_extended.ApplianceDescriptorDecorator;
import org.ow2.contrail.common.implementation.application_extended.ApplicationDescriptorExtended;
import org.ow2.contrail.common.implementation.application_extended.ExtensionDataGenerator;
import org.ow2.contrail.common.implementation.application_extended.SLASOIGenericDecorationGenerator;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

public class ApplicationDescriptorExtenderTester {

	
	private static ApplicationDescriptorExtended appDescExt;
	
	private static Collection<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>> extraInfo;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		extraInfo=new ArrayList<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>>();
		appDescExt= ApplicationDescriptorExtended.newApplicationDescriptorExtended(
				new OVFStAXParser().parseOVF(URI.create("src/test/resources/contrail_petstore.xml")));
		SLASOITemplateParser slatParser=new SLASOITemplateParser();
		java.util.Scanner sca;
		try {
			sca = new java.util.Scanner(new java.io.File("src/test/resources/contrail_petstore_SLA.xml"));
			String content = sca.useDelimiter("\\Z").next();
			sca.close();
			SLATemplate slat=slatParser.parseTemplate(content);
			extraInfo.add(new SLASOIGenericDecorationGenerator<SLATemplate>(slat));
		} catch (FileNotFoundException e) {
			
			fail("the Oracle is lost");
		}
		
	}

	@Test
	public void testAddApplianceDataExtentions() {
		appDescExt.addApplianceDataExtentions(extraInfo);
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances()){
			if(ad.getID().equals("et2"))
				assertEquals(ad.getClass().getSimpleName(),"SLASOIGenericDecoration");
			else
				assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		}	
	}

	@Test
	public void testSetNewApplianceDataExtentions() {
		appDescExt.setNewApplianceDataExtentions(null);
		testAddApplianceDataExtentions();
		appDescExt.setNewApplianceDataExtentions(null);
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances())
			assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		appDescExt.setNewApplianceDataExtentions(extraInfo);
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances()){
			if(ad.getID().equals("et2"))
				assertEquals(ad.getClass().getSimpleName(),"SLASOIGenericDecoration");
			else
				assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		}
		appDescExt.setNewApplianceDataExtentions(null);
		for(ApplianceDescriptor ad:appDescExt.getAllAppliances())
			assertEquals(ad.getClass().getSimpleName(),"ApplianceDescriptor");
		
	}

}
