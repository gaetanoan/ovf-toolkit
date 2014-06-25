package org.ow2.contrail.common.implementation.application_extended.test;

import java.io.File;

import java.util.Scanner;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application_extended.ApplianceDescriptorDecorator;
import org.ow2.contrail.common.implementation.application_extended.SLASOIGenericDecorationGenerator;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;


public class SLASOIDecorationTesting {

	public static SLASOIGenericDecorationGenerator<?> test=null; 
	

	@Test
	public void testGenerateDataApplianceDescriptor() throws Exception {
		String sourceSLA="contrail_petstore_SLA.xml";
		String path="src/main/resources/";
		Scanner a;
		
		String stw = (a=new Scanner( new File(path+sourceSLA) )).useDelimiter("\\A").next();
		a.close();
		/*Correct decoration testing*/
		test=new SLASOIGenericDecorationGenerator<Object>(new Object());
		/*Test1: a null decoration returns null */
		Assert.assertNull(test.getData(new ApplianceDescriptor("et2", null, 0, false)));
		/*Test2: a not null decoration returns not null decoration*/
		test=new SLASOIGenericDecorationGenerator<SLATemplate>(new SLASOITemplateParser().parseTemplate(stw.toString()));
		ApplianceDescriptorDecorator result=test.getData(new ApplianceDescriptor("et2", null, 0, false));
		Assert.assertNotNull("Decorator List: "+result.getAllDecorators().size()
				+"\nInstance of: "+result.getClass().getSimpleName()+"\nId: "+result.getID(),result);
		/*Test3: significance*/
		Assert.assertEquals("1SLASOIGenericDecorationet2", +result.getAllDecorators().size()
				+result.getClass().getSimpleName()+result.getID());
		
	}


}
