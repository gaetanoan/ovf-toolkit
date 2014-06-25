package org.ow2.contrail.common.implementation.ovf;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Collection;
import java.util.HashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwSharedDiskDrive;

/**
 * An OVF Parser.
 */
public class OVFParser
{
	// provide global information for the parser.
	private static HashMap<String, File> files = new HashMap<String, File>();
	private static HashMap<String, Disk> disks = new HashMap<String, Disk>();
	private static HashMap<String, SharedDisk> sharedDisks = new HashMap<String, SharedDisk>();
	private static HashMap<String, OVFVirtualNetwork> networks = new HashMap<String, OVFVirtualNetwork>();
	private static HashMap<String, OVFVirtualSystem> vs = new HashMap<String, OVFVirtualSystem>();
	//private static Collection<Entry<Integer, Collection<String>>> startup = new ArrayList<Entry<Integer, Collection<String>>>();

	/**
	 * Parses an OVF document.
	 * 
	 * @param ovfUri The path of the OVF document expressed as a <code>URI</code>.
	 * @return An <code>ApplicationDescriptor</code>
	 * @throws FileNotFoundException 
	 * @throws MalformedOVFException
	 */
	public static ApplicationDescriptor ParseOVF(URI ovfURI) throws FileNotFoundException, MalformedOVFException {
		try {
			InputStream input = new FileInputStream(ovfURI.getPath());
			return new org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser().parseOVF(input, false);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			throw new MalformedOVFException(e);
		}

	}


	/***
	 * previous version of OVF parser(DOM) maintained for testing purpose
	 * 
	 * 
	 * @param ovfURI
	 * @return An <code>ApplicationDescriptor</code>
	 * 
	 */
	public static ApplicationDescriptor previousOVFParser(URI ovfURI) throws ParserConfigurationException, FileNotFoundException,
	SAXException, IOException, NumberFormatException, XPathExpressionException, DOMException,
	URISyntaxException, MalformedOVFException{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(ovfURI.getPath()));

		ApplicationDescriptor appDesc = internalParseOVF(doc);
		return appDesc;

	}


	/**
	 * Parses an OVF document.
	 * 
	 * @param The content of the OVF document expressed as a <code>String</code> .
	 * @return An <code>ApplicationDescriptor</code>
	 * @throws MalformedOVFException
	 */
	public static ApplicationDescriptor ParseOVF(String ovf) throws MalformedOVFException {
		try {
			InputStream input = new ByteArrayInputStream(ovf.getBytes());
			return new org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser().parseOVF(input, false);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			throw new MalformedOVFException(e);
		}
	}

	/**
	 * Parses the VirtualSystemCollection XML element.
	 * 
	 * @param The
	 *            <code>Node</code> associated with the XML element to parse.
	 * @return An <code>ApplianceDescriptor</code> representing the appliance.
	 * @throws MalformedOVFException
	 */
	private static ApplianceDescriptor parseVSCollection(Node n, ApplianceDescriptor parent) throws XPathExpressionException, MalformedOVFException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();

		// id
		Node id = n.getAttributes().getNamedItem("ovf:id");
		if (id == null)
		{
			throw new RuntimeException("id was not specified for the appliance.");
		}
		String vsId = id.getNodeValue();

		ApplianceDescriptor result = new ApplianceDescriptor(vsId, null, 0, true);
		// appliance.getVirtualSystems().add(vs.get(vsId));
		result.setParent(parent);

		XPathExpression expr = xpath.compile("StartupSection");
		Node node = (Node) expr.evaluate(n, XPathConstants.NODE);

		// FIXME: we need to be sure that when startup-tag is missing the related
		// node is null!!!!
		if (node != null)
			result.setOVFStartupSection(parseStartupSection(xpath, node));

		expr = xpath.compile("ProductSection");
		NodeList productList = (NodeList) expr.evaluate(n, XPathConstants.NODESET);
		parseProductSection(result, productList, xpath);

		// VirtualSystem OR VirtualSystemCollection -> ApplianceDescriptor
		expr = xpath.compile("VirtualSystem | VirtualSystemCollection");

		NodeList nodes = (NodeList) expr.evaluate(n, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node current = nodes.item(i);

			ApplianceDescriptor child = null;
			if (current.getNodeName().equals("VirtualSystem"))
			{
				child = parseVS(current, result);
			}
			else
			{
				child = parseVSCollection(current, result);
			}

			result.getAppliancesDescriptors().add(child);
		}

		return result;
	}

	private static void parseProductSection(ApplianceDescriptor parent, NodeList sections, XPath xpath) throws XPathExpressionException,
	MalformedOVFException
	{
		for (int i = 0; i < sections.getLength(); i++)
		{
			OVFProductSection section = new OVFProductSection();
			Node sectionNode = sections.item(i);

			// reading attributes
			NamedNodeMap attributes = sectionNode.getAttributes();
			if (attributes.getNamedItem("ovf:class") != null)
			{
				section.setClassName(attributes.getNamedItem("ovf:class").getNodeValue());
			}
			if (attributes.getNamedItem("ovf:instance") != null)
			{
				section.setInstance(attributes.getNamedItem("ovf:instance").getNodeValue());
			}

			// reading children
			NodeList childList = sectionNode.getChildNodes();
			for (int k = 0; k < childList.getLength(); k++)
			{
				Node child = childList.item(k);
				if (child.getNodeType() != Node.TEXT_NODE)
				{
					if (child.getNodeName().equals("Info"))
					{
						section.setInfo(child.getFirstChild().getNodeValue());
					}
					else
						if (child.getNodeName().equals("Product"))
						{
							section.setProduct(child.getFirstChild().getNodeValue());
						}
						else
							if (child.getNodeName().equals("Vendor"))
							{
								section.setVendor(child.getFirstChild().getNodeValue());
							}
							else
								if (child.getNodeName().equals("Version"))
								{
									section.setVersion(child.getFirstChild().getNodeValue());
								}
								else
									if (child.getNodeName().equals("FullVersion"))
									{
										section.setFullVersion(child.getFirstChild().getNodeValue());
									}
									else
										if (child.getNodeName().equals("ProductUrl"))
										{
											section.setProductUrl(child.getFirstChild().getNodeValue());
										}
										else
											if (child.getNodeName().equals("VendorUrl"))
											{
												section.setVendorUrl(child.getFirstChild().getNodeValue());
											}
				}
			}
			XPathExpression expr = xpath.compile("Property");
			NodeList propertyList = (NodeList) expr.evaluate(sectionNode, XPathConstants.NODESET);
			parsePropertySection(parent, section, xpath, propertyList);
			parent.getProductSections().add(section);
		}
	}

	private static void parsePropertySection(ApplianceDescriptor parent, OVFProductSection section, XPath xpath, NodeList properties)
			throws XPathExpressionException, MalformedOVFException
			{

		for (int k = 0; k < properties.getLength(); k++)
		{
			NamedNodeMap attributes = properties.item(k).getAttributes();
			OVFProperty property = new OVFProperty();

			parsePropertyNode(xpath, parent, property, attributes, properties.item(k));

			section.getProperties().add(property);

		}
			}

	private static void parsePropertyNode(XPath xpath, ApplianceDescriptor parent, OVFProperty property, NamedNodeMap attributes, Node node)
			throws XPathExpressionException, MalformedOVFException
			{
		// ovf:key is mandatory
		Node nodeAtt = attributes.getNamedItem("ovf:key");
		if (nodeAtt != null)
		{
			String mainKey = nodeAtt.getNodeValue();
			property.setKey(nodeAtt.getNodeValue());

			if (nodeAtt.getNodeValue().equals("ip")){
				String[] splitKey = nodeAtt.getNodeValue().split("\\@");
				switch (splitKey.length){
				case 1: 
					//System.out.println("Assuming VEP 1.1");
					parseVirtualNetwork_VEP_1(parent, attributes);
					break;
				case 3:
					//System.out.println("Assuming VEP 2.0 ");
					parseVirtualNetwork_VEP_2(parent, attributes, nodeAtt);
					break;
				default:
					throw new MalformedOVFException("Unhandled ip property specification");
				}
			}
			/* code from Giacomo, substituted with the previous
				nodeAtt = attributes.getNamedItem("ovf:value");
				if (nodeAtt == null)
				{
					throw new NullPointerException("ovf:value is mandatory when ovf:key = ip");
				}

				String value = nodeAtt.getNodeValue();
				String[] splitted = value.split("\\@");
				// represent the complex string (ip_add@Vnet)
				if (splitted.length > 1)
				{
					// add ip to global networks
					OVFVirtualNetwork net = networks.get(splitted[1]);
					net.getIpAddress().add(splitted[0]);

					OVFVirtualNetwork n = parent.getVirtualNetworkByName(splitted[1]);

					if (n == null)
					{
						n = new OVFVirtualNetwork(splitted[1], net.getDescription());
						n.getIpAddress().add(splitted[0]);

						parent.getAssociatedVirtualNetworks().add(n);
					}
					else
					{
						n.getIpAddress().add(splitted[0]);
					}
				}
			}
			 */
			
			// check per optional attributes
			nodeAtt = attributes.getNamedItem("ovf:userConfigurable");
			if (nodeAtt != null)
			{
				property.setUserConfigurable(Boolean.parseBoolean(nodeAtt.getNodeValue()));
			}

			nodeAtt = attributes.getNamedItem("ovf:value");
			if (nodeAtt != null)
			{
				String value = nodeAtt.getNodeValue();
				if (value.startsWith("${"))
				{
					Collection<OVFProperty> indirectedProp = parent.getAllProperties();
					for (OVFProperty prop : indirectedProp)
					{
						if (prop.getKey().equals(value.substring(2, value.length() - 1)))
						{
							property.clone(prop, mainKey);
							return;
						}
					}
					throw new MalformedOVFException("OVF malformed; missing indirected reference for property: " + mainKey);
				}
				else
				{
					property.setValue(nodeAtt.getNodeValue());
				}
			}

			nodeAtt = attributes.getNamedItem("ovf:password");
			if (nodeAtt != null)
			{
				property.setPassword(Boolean.parseBoolean(nodeAtt.getNodeValue()));
			}

			nodeAtt = attributes.getNamedItem("ovf:qualifiers");
			if (nodeAtt != null)
			{
				property.setQualifiers(nodeAtt.getNodeValue());
			}

			nodeAtt = attributes.getNamedItem("ovf:type");
			if (nodeAtt != null)
			{
				property.setType(nodeAtt.getNodeValue());
			}

			XPathExpression expr = xpath.compile("Label");
			nodeAtt = (Node) expr.evaluate(node, XPathConstants.NODE);
			if (nodeAtt != null)
			{
				Node msgid = nodeAtt.getAttributes().getNamedItem("ovf:msgid");
				if (msgid != null)
				{
					//String h = msgid.getNodeValue();
					property.setLabel(nodeAtt.getAttributes().getNamedItem("ovf:msgid").getNodeValue(), nodeAtt.getFirstChild().getNodeValue());
				}
			}

			expr = xpath.compile("Description");
			nodeAtt = (Node) expr.evaluate(node, XPathConstants.NODE);
			if (nodeAtt != null)
			{
				Node msgid = nodeAtt.getAttributes().getNamedItem("ovf:msgid");

				if (msgid != null)
				{
					property.setDescription(msgid.getNodeValue(), nodeAtt.getFirstChild().getNodeValue());
				}

			}
		}
		else
			throw new NullPointerException("ovf:key is mandatory");
			}

	/**
	 * Parses the VirtualSystem XML element.
	 * 
	 * @param The
	 *            <code>Node</code> associated with the XML element to parse.
	 * @return An <code>ApplianceDescriptor</code> representing the appliance.
	 * @throws MalformedOVFException
	 */
	private static ApplianceDescriptor parseVS(Node n, ApplianceDescriptor parent) throws XPathExpressionException, MalformedOVFException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();

		// id
		Node id = n.getAttributes().getNamedItem("ovf:id");
		if (id == null)
		{
			throw new RuntimeException("id was not specified for the appliance.");
		}
		String vsId = id.getNodeValue();

		OVFVirtualSystem ovfVS = new OVFVirtualSystem(vsId);
		vs.put(vsId, ovfVS);

		// FIXME: remove NULL & 0 values;
		ApplianceDescriptor appliance = new ApplianceDescriptor(vsId, null, 0, false);
		appliance.getVirtualSystems().add(vs.get(vsId));
		appliance.setParent(parent);

		// reading product section
		XPathExpression expr = xpath.compile("ProductSection");
		NodeList productList = (NodeList) expr.evaluate(n, XPathConstants.NODESET);
		parseProductSection(appliance, productList, xpath);

		// parsing of the Item(s) of the VirtualHardwareSection.
		expr = xpath.compile("VirtualHardwareSection/Item");
		NodeList items = (NodeList) expr.evaluate(n, XPathConstants.NODESET);

		for (int k = 0; k < items.getLength(); k++)
		{
			Node item = items.item(k);
			OVFVirtualHardware vh = null;

			// for each child element "rasd:*" do
			for (int j = 0; j < item.getChildNodes().getLength(); j++)
			{
				Node child = item.getChildNodes().item(j);

				if (child.getNodeName().equals("rasd:ResourceType"))
				{
					int resourceType = Integer.parseInt(child.getTextContent());

					OVFItemProcessor p = OVFResourceAllocationSettingDataTranslator.getItemProcessor(resourceType);

					vh = p.processItem(item);
					ovfVS.getRequiredHardware().add(vh);

					switch (vh.getResourceType()){
					case 17: // if ResourceType == 17 => Link "Disk" from Disks hashmap
						OVFVirtualHwDiskDrive vDisk = (OVFVirtualHwDiskDrive) vh;
						String diskRef = vDisk.getHostResource();
						vDisk.setDisk(disks.get(diskRef));
						break;
					case 32: // if ResourceType == 17 => Link "Disk" from Disks hashmap
						OVFVirtualHwSharedDiskDrive vShDisk = (OVFVirtualHwSharedDiskDrive) vh;
						String shDiskRef = vShDisk.getHostResource();
						vShDisk.setSharedDisk(sharedDisks.get(shDiskRef));
						break;
					case 10:
						OVFVirtualHwEthernetAdapter veth = (OVFVirtualHwEthernetAdapter) vh;
						String nName = veth.getConnection();
						OVFVirtualNetwork vn = appliance.getVirtualNetworkByName(nName);
						if (vn == null)
						{
							vn = networks.get(nName);
							if (vn == null)
								throw new MalformedOVFException("Usage of a network not specified in NetworkSection");
							appliance.getAssociatedVirtualNetworks().add(new OVFVirtualNetwork(nName, vn.getDescription()));
						}
						appliance.linkEthAdapterToNetwork(veth, nName);
                        break;
					}
				}
			}
		}

		return appliance;
	}

	/**
	 * Internal method used to factorize common code.
	 * 
	 * @throws MalformedOVFException
	 */
	private static ApplicationDescriptor internalParseOVF(Document doc) throws ParserConfigurationException, FileNotFoundException, SAXException,
	IOException, XPathExpressionException, NumberFormatException, DOMException, URISyntaxException, MalformedOVFException
	{


		ApplicationDescriptor appDesc = new ApplicationDescriptor();

		XPath xpath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xpath.compile("//References/File");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			NamedNodeMap attributes = nodes.item(i).getAttributes();

			File f = new File(attributes.getNamedItem("ovf:id").getNodeValue(), attributes.getNamedItem("ovf:href").getNodeValue(),
					Long.parseLong(attributes.getNamedItem("ovf:size").getNodeValue()));


			files.put(f.getId(), f);
		}

		// Disks
		expr = xpath.compile("//DiskSection/Disk");
		nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			NamedNodeMap attributes = nodes.item(i).getAttributes();

			String fileRef = attributes.getNamedItem("ovf:fileRef").getNodeValue();
			File f = files.get(fileRef);
			String idd = attributes.getNamedItem("ovf:diskId").getNodeValue();
			disks.put("ovf:/disk/" + idd,new Disk(f, idd) );
		}
		//Create DiskTree structure supporting Delta Disks
		for(int i = 0; i<nodes.getLength();i++){
			NamedNodeMap attributes = nodes.item(i).getAttributes();
			Disk child=disks.get("ovf:/disk/"+attributes.getNamedItem("ovf:diskId").getNodeValue());
			if(attributes.getNamedItem("ovf:parentRef")!=null){
				Disk parent=disks.get("ovf:/disk/"+attributes.getNamedItem("ovf:parentRef").getNodeValue());
				child.setParent(parent);
				parent.addChild(child);
			}
		}
		
		/* new hook */
		expr = xpath.compile("//SharedDiskSection/SharedDisk");
		nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			NamedNodeMap attributes = nodes.item(i).getAttributes();

			String diskId = attributes.getNamedItem("ovf:diskId").getNodeValue();
			String format = attributes.getNamedItem("ovf:format").getNodeValue();
			String capacity = attributes.getNamedItem("ovf:capacity").getNodeValue();
			
			File f = null;
			Node n = attributes.getNamedItem("ovf:fileRef");
			if (n != null){
				String fileRef = n.getNodeValue();
				f = files.get(fileRef);
				if (f == null) 
					System.err.println("File reference not found for SharedDisk section");
				
			}
			sharedDisks.put("ovf:/disk/" + diskId, new SharedDisk(diskId, f, capacity, format));
		}

		// Virtual Networks
		expr = xpath.compile("//NetworkSection/Network");
		nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node current = nodes.item(i);

			// name
			String name = current.getAttributes().getNamedItem("ovf:name").getNodeValue();

			// description
			String description = null;
			for (int j = 0; j < current.getChildNodes().getLength(); j++)
			{
				Node n = current.getChildNodes().item(j);

				if (n.getNodeName().equals("Description"))
				{
					description = n.getTextContent();
					break;
				}
			}

			networks.put(name, new OVFVirtualNetwork(name, description));
		}

		// VirtualSystem OR VirtualSystemCollection -> ApplianceDescriptor
		expr = xpath.compile("Envelope/VirtualSystem | Envelope/VirtualSystemCollection");
		nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node current = nodes.item(i);

			ApplianceDescriptor appliance = null;
			if (current.getNodeName().equals("VirtualSystem"))
			{
				appliance = parseVS(current, null);
			}
			else
			{
				appliance = parseVSCollection(current, null);
			}

			appDesc.getApplianceDescriptors().add(appliance);
		}

		postProcessStartup(appDesc);

		return appDesc;
	}

	private static void postProcessStartup(ApplicationDescriptor appDesc){

	}

	/**
	 * Extracts information relevant for the appliances startup.
	 * 
	 * @param The
	 *            <code>XPath</code> used to query about the OVF structure.
	 * @param The
	 *            <code>ApplianceDescriptor</code> associated with the XML element to parse.
	 * @param The
	 *            <code>Node</code> associated with the XML element to parse.
	 * @throws XPathExpressionException
	 */
	private static OVFStartupSection parseStartupSection(XPath xpath, Node n) throws XPathExpressionException
	{
		OVFStartupSection result = new OVFStartupSection();

		// StartupSection -> StartupDescriptor
		XPathExpression expr = xpath.compile("Item");

		NodeList nodes = (NodeList) expr.evaluate(n, XPathConstants.NODESET);

		// For each Item extract infos..
		for (int i = 0; i < nodes.getLength(); i++)
		{
			NamedNodeMap attributes = nodes.item(i).getAttributes();

			String id = attributes.getNamedItem("ovf:id").getNodeValue();
			int order = Integer.parseInt(attributes.getNamedItem("ovf:order").getNodeValue());

			// FIXME: unused.
			String startDelay=null;
			if(attributes.getNamedItem("ovf:startDelay")!=null)
				startDelay = attributes.getNamedItem("ovf:startDelay").getNodeValue();
			String startAction=null;
			if(attributes.getNamedItem("ovf:startAction")!=null)
				startAction = attributes.getNamedItem("ovf:startAction").getNodeValue();
			String waitingForGuest = null;	
			if(attributes.getNamedItem("ovf:waitingForGuest")!=null)
				waitingForGuest = attributes.getNamedItem("ovf:waitingForGuest").getNodeValue();
			String stopDelay = null;
			if(attributes.getNamedItem("ovf:stopDelay")!=null)
				stopDelay = attributes.getNamedItem("ovf:stopDelay").getNodeValue();
			String stopAction = null;
			if(attributes.getNamedItem("ovf:stopAction")!=null)
				stopAction = attributes.getNamedItem("ovf:stopAction").getNodeValue();

			OVFStartupSection.Item startupItem = new OVFStartupSection.Item(id, order);
			result.getItems().add(startupItem);
		}

		return result;
	}

	private static void parseVirtualNetwork_VEP_1(ApplianceDescriptor parent, NamedNodeMap attributes){
		Node nodeAtt = attributes.getNamedItem("ovf:value");
		if (nodeAtt == null) {
			throw new NullPointerException("ovf:value is mandatory when ovf:key = ip");
		}

		String value = nodeAtt.getNodeValue();
		String[] splitted = value.split("\\@");
		// represent the complex string (ip_add@Vnet)
		if (splitted.length > 1)
		{
			// add ip to global networks
			OVFVirtualNetwork net = networks.get(splitted[1]);
			if (net != null){
				net.getIpAddress().add(splitted[0]);

				OVFVirtualNetwork n = parent.getVirtualNetworkByName(splitted[1]);

				if (n == null) {
					n = new OVFVirtualNetwork(splitted[1], net.getDescription());
					n.getIpAddress().add(splitted[0]);

					parent.getAssociatedVirtualNetworks().add(n);
				}
				else {
					n.getIpAddress().add(splitted[0]);
				}
			}
		}
	}
	
	private static void parseVirtualNetwork_VEP_2(ApplianceDescriptor parent, NamedNodeMap attributes, 
			Node nodeAtt)
					throws MalformedOVFException {

		String[] splitKey = nodeAtt.getNodeValue().split("\\@");
		if (splitKey.length != 3) 
			throw new MalformedOVFException("Format invalid: Ip key inside the ProductSection of a VirtualSystem");

		String vnName = splitKey[1];
		OVFVirtualNetwork net = networks.get(vnName);
		if (net != null){
			OVFVirtualNetwork n = parent.getVirtualNetworkByName(vnName);

			if (n == null) {
				n = new OVFVirtualNetwork(vnName, net.getDescription());
				parent.getAssociatedVirtualNetworks().add(n);
			}
			else {
				n.getIpAddress().add(splitKey[0]);
			}
		}
	}
}
