package org.ow2.contrail.common.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.contrail.common.ParserManager;
import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class ParserManagerTester {

	private static ArrayList<ParserManager> pp = null;
	
	/*FIXME Any Changes in these files can lead to  misbehaviour in 
	 * testGetApplianceVirtualSystem() results*/
	private static String OVFtest[]={"src/test/resources/contrail_petstore.xml"
		,"src/test/resources/small-vm.ovf"};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		try
		{
			pp=new ArrayList<ParserManager>();
			for(int i=0;i<OVFtest.length;i++)pp.add(new ParserManager(OVFtest[i]));
			
		}
		catch (MalformedOVFException e)
		{
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetApplication() {
		String[] adaa={"ContrailPetStore","Small VM"};
		int i=0;
		for(ParserManager p:pp){
//			System.out.println("File test No. "+pp.indexOf(p));
			ApplicationDescriptor aa=p.getApplication();
//			System.out.println("*APPLICATION DESCRIPTOR: ALL APPLIANCES*");
//			for(ApplianceDescriptor c: p.getApplication().getAllAppliances())System.out.println(c.getID());
//			System.out.println("*APPLICATION DESCRIPTOR: APPLIANCE DESCRIPTOR*");
//			for(ApplianceDescriptor c: p.getApplication().getApplianceDescriptors())System.out.println(c.getID());
//			System.out.println("*PARSER MANAGER: GET APPLIANCE*");
//			for(String c: p.getAppliances())System.out.println(c);
//			System.out.println("*************");
			
			assertEquals(aa.getName(),adaa[i++]);
		}
		//Semantic of appliance
		
	}
	
	/* all appliances ovf:id value recognized? */
	@Test
	public void testGetAppliances() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		for(ParserManager p:pp){
			FileReader fr = new FileReader(OVFtest[pp.indexOf(p)]);
			BufferedReader br = new BufferedReader(fr);
			StringBuffer sb = new StringBuffer();
			while (br.ready())sb.append(br.readLine());
			br.close();
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
		
			StringReader sr = new StringReader(sb.toString());
			InputSource is = new InputSource(sr);
			Document doc = builder.parse(is);
		
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//VirtualSystemCollection|//VirtualSystem");
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			Collection<String> ab=p.getAppliances();
		
		//Bijection
			assertEquals(nodes.getLength(),ab.size());
			if(nodes.getLength()!=ab.size())
				fail(pp.indexOf(p)+": Appliance Don't Map VirtualSystemCollection|VirtualSystem correctly");
			else {
				for(int i=0;i<nodes.getLength();i++)
					ab.remove(nodes.item(i).getAttributes().getNamedItem("ovf:id").getNodeValue());
				assertTrue(ab.isEmpty());
			}
		}
	}

	@Test
	public void testGetApplianceDisk() {
		for(ParserManager p:pp){
			Collection<String> appls=p.getAppliances();
			System.out.println("File test No. "+pp.indexOf(p));
			for(String app:appls){
				System.out.println("analysis DISK: "+app);
			/*app must be on top of ContentElement! */			
				Collection<Disk> dischi=p.getApplianceDisk(app);
			//the cause is "application.getApplianceDescriptors()" used by tested method
				for(Disk a:dischi)
					System.out.println("IdDisk: *"+a.getId()+"* file: "+a.getFile().getId()
					+" dimension: "+a.getFile().getSize()+" URI: "+a.getFile().getUri());
			/*repetitions, why? => LinkedHashSet instead of ArrayList */
				System.out.println();
			
			}
			System.out.println("*************");
			
		}
		String []dada={"base","web","engine_frontend","engine_worker","engine_backend","db_frontend","db_backend"};
		int i=0;
		for(Disk cada:pp.get(0).getApplianceDisk("ContrailPetstore")){
			assertEquals(cada.getId(),dada[i++]);
		}
		i=0;
		for(Disk cada:pp.get(0).getApplianceDisk("DB2")){
			assertEquals(cada.getId(),dada[i]);
			i=i+6;
		}
	}

	@Test
	public void testGetApplianceImages() {
		/*Return only first image an of appliance*/
		for(ParserManager p:pp){
			System.out.println("File test No. "+pp.indexOf(p));
			Collection<String> appls=p.getAppliances();
		/*suffers side effects ??*/	
			for(String app:appls){
				System.out.println("analysis IMAGE: "+app);		
				Collection<File> files=p.getApplianceImages(app);
				for(File a:files)
					System.out.println("File: "+a.getId()+" URI: "+a.getUri()+" Size: "+a.getSize());
				System.out.println();
			}
			System.out.println("*************"+pp.indexOf(p));
			
		}
		
		String []dada={"base","image0","image1","image2","image3","image4","image5"};
		int i=0;
		for(File cada:pp.get(0).getApplianceImages("ContrailPetstore")){
			assertEquals(cada.getId(),dada[i++]);
		}
		i=0;
		for(File cada:pp.get(0).getApplianceImages("DB2")){
			assertEquals(cada.getId(),dada[i]);
			i=i+6;
		}
		i=0;
		for(File cada:pp.get(0).getApplianceImages("EngineTier")){
			assertEquals(cada.getId(),dada[i]);
			i= (i==0?(i=i+2):i+1);
		}
	}

	@Test
	public void testGetNetworkDescription() {
	  assertEquals(pp.get(0).getNetworkDescription("VMNetwork0"),
			  "The network used to link the web server node and the engine frontend node.");
	  assertEquals(pp.get(0).getNetworkDescription("VMNetwork1"),
			  "The network used to link the engine nodes.");
	  assertEquals(pp.get(0).getNetworkDescription("VMNetwork2"),
			  "The network used to link the engine backend node and the db frontend node.");
	  assertEquals(pp.get(0).getNetworkDescription("VMNetwork3"),
			  "The network used to link the db nodes.");
	}

	
	
	private void internalNetTest(String applianceId, String[] aa,int i){
		Collection<String> papa=pp.get(0).getApplianceNetworks(applianceId);
		for(String ada:papa)
			assertEquals(aa[i++],ada);
	}
	
	@Test
	public void testGetApplianceNetworks() {
		/*Return only first network of an appliance: not all network associated to a Network are returned*/
		String []aa={"VMNetwork0","VMNetwork1","VMNetwork2","VMNetwork3"};
		internalNetTest("ContrailPetStore",aa,0);
		internalNetTest("WebTier",aa,0);
		internalNetTest("EngineTier",aa,0);
		internalNetTest("et1",aa,0);
		internalNetTest("et2",aa,1);
		internalNetTest("et3",aa,1);
		internalNetTest("et4",aa,1);
		internalNetTest("DBTier",aa,2);
		internalNetTest("DB_Frontend",aa,2);
		internalNetTest("DB1",aa,3);
		internalNetTest("DB2",aa,3);
		for(ParserManager p:pp){
			Collection<String> appls=p.getAppliances();
			System.out.println("File test No. "+pp.indexOf(p));
			for(String app:appls){
				System.out.println("analysis NETWORK: "+app);		
				Collection<String> netName=p.getApplianceNetworks(app);
				for(String a:netName)
					System.out.println(a+" Description:"+p.getNetworkDescription(a));
				System.out.println();
			}
			System.out.println("*************");
		}

	}

	@Test
	public void testGetApplianceVirtualSystem() {
		StringBuffer sb=new StringBuffer();
		for(ParserManager p:pp){
			Collection<String> appls=p.getAppliances();
			
			sb.append("File test No. "+pp.indexOf(p)+"\n");
			for(String app: appls){
				sb.append("\tanalysis VIRTUALSYSTEM: "+app+"\n");
				Collection<OVFVirtualSystem> vscol= p.getApplianceVirtualSystem(app);
				for(OVFVirtualSystem a:vscol){
					sb.append("\t\tvirtual system: "+a.getId()+"\n");
					for(OVFVirtualHardware vh:a.getRequiredHardware()){
						String str=vh.toString().replace("\n", "\n\t\t\t");
						sb.append("\t\t************************\n");
						sb.append("\t\t\t"+str+"\n");					
					}
					sb.append("\t\t******end virtualhardware of "+a.getId()+"*******\n");
				}
				sb.append("\t********end analysis virtualSystem of "+app+"*******\n");
			}
			sb.append("*************\n");
		}		
		oracleConsult(sb
				,"src/test/resources/ParserManager_ApplianceVirtualSystemValidityTest.txt");
	}

	@Test
	public void testGetChildren() {
		StringBuffer sb=new StringBuffer();
		for(ParserManager p:pp){
			sb.append("File test No. "+pp.indexOf(p)+"\n");
			Collection<String> appls=p.getAppliances();
			for(String app:appls){
				sb.append("Analysis children of: "+app+"\n");
				sb.append("\t"+p.getChildren(app)+"\n");
			}
			sb.append("*************\n");
		}
		oracleConsult(sb
				,"src/test/resources/ParserManager_ChildrenValidityTest.txt");
	}

	@Test
	public void testGetAllProperty() {
		/*TODO  see ApplianceDescriptor getAllProperties correctness*/
		StringBuffer sb=new StringBuffer();
		for(ParserManager p:pp){
			sb.append("File test No. "+pp.indexOf(p)+"\n");
			Collection<String> appls=p.getAppliances();
			for(String app:appls){
				sb.append("Analysis property of: "+app+"\n");
				Collection<OVFProperty> aa=p.getAllProperty(app);
				for(OVFProperty a:aa){
					sb.append("\tkey: "+a.getKey()+" type: "+a.getType()+" UC: "+a.isUserConfigurable()+
						" value: "+a.getValue()+"\n");
				}
			}
			sb.append("*************\n");
		}
		oracleConsult(sb
				,"src/test/resources/ParserManager_AllPropertyValidityTest.txt");

	}

	private void oracleConsult(StringBuffer sb, String fileTest_name) {
		java.util.Scanner sca;
		try {
			sca = new java.util.Scanner(new java.io.File(fileTest_name));
			String content = sca.useDelimiter("\\Z").next();
			sca.close();
			assertEquals(content,sb.toString());
			System.out.println(sb.toString());
		} catch (FileNotFoundException e) {
			
			fail("the Oracle is lost");
		}
	}

}
