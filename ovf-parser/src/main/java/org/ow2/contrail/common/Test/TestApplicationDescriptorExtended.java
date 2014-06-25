package org.ow2.contrail.common.Test;


import java.io.*;
import java.net.URI;
import java.util.Arrays;


import org.ow2.contrail.common.implementation.application_extended.ApplicationDescriptorExtended;
import org.ow2.contrail.common.implementation.application_extended.ApplicationDescriptorExtendedFactory;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
//import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
// import org.slasoi.slamodel.sla.SLATemplate;

public class TestApplicationDescriptorExtended {

	//testing section
	private static String sourceSLA="contrail_petstore_SLA.xml";
	private static String sourceOVF="contrail_petstore.xml";
	private static String path="src/main/resources/";

	public static void main(String arg[]) throws Exception{
//		
//		File file = new File(path+sourceSLA);
//		System.out.println("loading SLA from: "+file.getAbsolutePath());
//		
//		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = domFactory.newDocumentBuilder();
//		Document doc =builder.parse(file);
//		/*snippet*/
//		StringWriter stw = new StringWriter();
//		Transformer serializer = TransformerFactory.newInstance().newTransformer();
//		serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
//		/*end snippet*/
		// SLASOITemplateParser slatParser=new SLASOITemplateParser();
		StringBuffer sb=new StringBuffer();
		java.util.Scanner sca;
		try {
			sca = new java.util.Scanner(new java.io.File("src/test/resources/contrail_petstore_SLA.xml"));
			String content = sca.useDelimiter("\\Z").next();
			sca.close();
			/*assertEquals(content,sb.toString());*/
			/*System.out.println(sb.toString());*/
			// SLATemplate ada=slatParser.parseTemplate(content);
			
			// ApplicationDescriptorExtended ad= (new ApplicationDescriptorExtendedFactory(OVFParser.ParseOVF(URI.create(path+sourceOVF)))).getData(Arrays.asList(ada));
			// System.out.println(ad.getName());
			
		} catch (FileNotFoundException e) {
			System.out.println("bv");
			/*fail("the Oracle is lost");*/
		}
		
		
		
			
	}
}
