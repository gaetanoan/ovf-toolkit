package org.ow2.contrail.common.test;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.ow2.contrail.common.implementation.Renderer;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;




public class Test {


	static final String JAXP_SCHEMA_LANGUAGE =
		    "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
		static final String W3C_XML_SCHEMA =
		    "http://www.w3.org/2001/XMLSchema";
		static final String JAXP_SCHEMA_SOURCE =
			    "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	
	private static Logger logger = Logger.getLogger(Test.class);
	private static String path = "src/main/resources/";
	private static String test = "testLatest";
	private static String source = "contrail_petstore.xml";
	//private static String valSchema= "dsp8023.xsd";
	
	public static void main(String[] args) throws Exception {

		logger.info("Test Renderer - starting test");
		logger.info("Test Renderer - creating document \""+test+".xml\" from document \""+source+"\"\n");
		//Renderer.RenderOVF(path +source, path, test.substring(0, 5));
		
		//ApplicationDescriptor appDesc=OVFParser.ParseOVF(URI.create(path+source));
		//Renderer.RenderOVF(appDesc, path, test+"DOM");
		ApplicationDescriptor appDesc1=new OVFStAXParser().parseOVF(URI.create(path+source));
		Renderer.RenderOVF(appDesc1, path, test+"StAX");
		Renderer.generateDD(appDesc1.getAllAppliances(), path+"test_appliance.xsl");
		logger.info("Test Renderer - creation completed, document \""+path+test+" created");

		
		 try {

		        TransformerFactory tFactory = TransformerFactory.newInstance();            
		        
		        Transformer transformer =
		                tFactory.newTransformer( new StreamSource(path+"test_appliance.xsl" ) );
		        
		        StreamSource xmlSource = new StreamSource(path+source);
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        transformer.transform( xmlSource, new StreamResult( baos ) );
		
		        System.out.println(baos.toString());

		    } catch( Exception e ) {
		        e.printStackTrace();
		    }
		//		//validation 
//	    // parse an XML document into a DOM tree
//	    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
//	    dbf.setNamespaceAware(true);
//	    dbf.setValidating(true);
//	    dbf.setAttribute(JAXP_SCHEMA_LANGUAGE,W3C_XML_SCHEMA);
//	    dbf.setAttribute(JAXP_SCHEMA_SOURCE, new File(path+"/"+valSchema));

//	    DocumentBuilder parser;
//		try {
//			parser = dbf.newDocumentBuilder();
//			parser.setErrorHandler(new MyErrorHandler(System.err));
//			
//			try {
//				parser.parse(new File(path+"test_appliance.xml"));
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				System.err.printf(e.getMessage());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				System.err.printf(e.getMessage());
//			}
//			
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			System.err.printf(e.getMessage());
//		}
//	   
//	}
//	private static class MyErrorHandler implements ErrorHandler {
//	    private PrintStream out;
//
//	    MyErrorHandler(PrintStream out) {
//	        this.out = out;
//	    }
//
//	    private String getParseExceptionInfo(SAXParseException spe) {
//	        String systemId = spe.getSystemId();
//
//	        if (systemId == null) {
//	            systemId = "null";
//	        }
//
//	        String info = "URI=" + systemId + " Line=" 
//	            + spe.getLineNumber() + ": " + spe.getMessage();
//
//	        return info;
//	    }
//
//	    public void warning(SAXParseException spe) throws SAXException {
//	        out.println("Warning: " + getParseExceptionInfo(spe));
//	    }
//	        
//	    public void error(SAXParseException spe) throws SAXException {
//	        String message = "Error: " + getParseExceptionInfo(spe);
//	        throw new SAXException(message);
//	    }
//
//	    public void fatalError(SAXParseException spe) throws SAXException {
//	        String message = "Fatal Error: " + getParseExceptionInfo(spe);
//	        throw new SAXException(message);
//	    }
	}

}
