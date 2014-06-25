package org.ow2.contrail.common.implementation.ovf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class OVFProductSection
{
	// FIXME: we need to handle the Icon object
	private String info;
	private String product;
	private String vendor;
	private String version;
	private String fullVersion;
	private String productUrl;
	private String vendorUrl;
	private String category;
	private String className;
	private String instance;

	private Collection<OVFProperty> properties;

	public OVFProductSection(){
		properties = new ArrayList<OVFProperty>();
	}
	
	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public String getProduct()
	{
		return product;
	}

	public void setProduct(String product)
	{
		this.product = product;
	}

	public String getVendor()
	{
		return vendor;
	}

	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getProductUrl()
	{
		return productUrl;
	}

	public void setProductUrl(String productUrl)
	{
		this.productUrl = productUrl;
	}

	public String getFullVersion()
	{
		return fullVersion;
	}

	public void setFullVersion(String fullVersion)
	{
		this.fullVersion = fullVersion;
	}

	public String getVendorUrl()
	{
		return vendorUrl;
	}

	public void setVendorUrl(String vendorUrl)
	{
		this.vendorUrl = vendorUrl;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public Collection<OVFProperty> getProperties()
	{
		return properties;
	}
	
	public void setProperties(Collection<OVFProperty> props)
	{
		this.properties=props;
	}

	public String getInstance()
	{
		return instance;
	}

	public void setInstance(String instance)
	{
		this.instance = instance;
	}
	
	//FIXME StAX creator Start
	public OVFProductSection(XMLEvent e,XMLEventReader r,HashMap<String,OVFProperty> vars,
			Attribute className, Attribute instance,HashMap<String,OVFVirtualNetwork> net,ApplianceDescriptor app, OVFStAXParser sta) 
					throws XMLStreamException, MalformedOVFException{
		this();
		HashMap<String,OVFProperty> localProp=new HashMap<String,OVFProperty>(); 
		this.instance=(instance!=null?instance.getValue():null);
		this.className=(className!=null?className.getValue():null);
		boolean end=false;
		while(!end&&r.hasNext()){
			e=r.nextEvent();
			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("ProductSection"));
			if(!end){
				switch(e.getEventType()){
				case XMLEvent.START_ELEMENT:
					String tagName=e.asStartElement().getName().getLocalPart();
					
					if(tagName.contentEquals("Info"))this.info=r.getElementText().trim();
					else if(tagName.contentEquals("Product"))this.product=r.getElementText().trim();
					else if(tagName.contentEquals("Vendor"))this.vendor=r.getElementText().trim();
					else if(tagName.contentEquals("Version"))this.version=r.getElementText().trim();
					else if(tagName.contentEquals("FullVersion"))this.fullVersion=r.getElementText().trim();
					else if(tagName.contentEquals("ProductUrl"))this.productUrl=r.getElementText().trim();
					else if(tagName.contentEquals("FullVersion"))this.fullVersion=r.getElementText().trim();
					else if(tagName.contentEquals("VendorUrl"))this.vendorUrl=r.getElementText().trim();
					//TODO parsing property node.
					else if(tagName.contentEquals("Property")){
						String key = e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames + "key")).getValue().trim();
						
						String value=(e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"value"))!=null)
								?e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"value")).getValue().trim():null;
						
						String userConfig=(e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"userConfigurable"))!=null)?
								e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"userConfigurable")).getValue().trim():null;
						
						String qualifiers=(e.asStartElement().getAttributeByName(QName.valueOf("{http://schemas.dmtf.org/ovf/envelope/1}qualifiers"))!=null)?
								e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"qualifiers")).getValue().trim():null;
						
						String password=(e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"password"))!=null)?
								e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"password")).getValue().trim():null;
						
						String type=(e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"type"))!=null)?
								e.asStartElement().getAttributeByName(QName.valueOf(OVFStAXParser.ovfNames+"type")).getValue().trim():null;
						
						if (key.contains("ip")) {
							
							if (key == null) throw new IllegalArgumentException("value node is mandatory for ip key");
							String[] splitted = key.split("\\@");
							switch (splitted.length){
							case 1: 
								//System.out.println("Assuming VEP 1.1");
								splitVirtualNetwork_VEP_1(app, net, splitted);
								break;
							case 3:
								//System.out.println("Assuming VEP 2.0 ");
								splitVirtualNetwork_VEP_2(app, net, splitted);
								break;
							default:
								throw new MalformedOVFException("Unhandled ip property specification");
							}
							
							/* old method
							if(splitted[0].length()>1){
								OVFVirtualNetwork network = net.get(splitted[1]);
								network.getIpAddress().add(splitted[0]);
								OVFVirtualNetwork n = app.getVirtualNetworkByName(splitted[1]);
								if (n == null){
									n = new OVFVirtualNetwork(splitted[1], network.getDescription());
									n.getIpAddress().add(splitted[0]);

									app.getAssociatedVirtualNetworks().add(n);
								}
								else{
									n.getIpAddress().add(splitted[0]);
								}
							}
							*/
						}
						
						OVFProperty prop=new OVFProperty();
						prop.setKey(key);
						prop.setType(type);
						if (value!=null){
							if(value.startsWith("${"))prop.clone(vars.get(value),key);
							else prop.setValue(value);
						}
						if(userConfig!=null)prop.setUserConfigurable(Boolean.parseBoolean(userConfig));
						prop.setQualifiers(qualifiers);
						if(password!=null)prop.setPassword(Boolean.parseBoolean(password));
						
						sta.parseProductSection(prop, e, r);
						localProp.put("${"+key+"}", prop);
						properties.add(prop);
					}
					break;
				}
			}
		}
		vars.putAll(localProp);
	}
	//FIXME StAX creator end
	
	
	
	private static void splitVirtualNetwork_VEP_1(ApplianceDescriptor app, HashMap<String, OVFVirtualNetwork> net, String[] splitted){
		// represent the complex string (ip_add@Vnet)
		
		if (splitted.length > 1)
		{
			// add ip to global networks
			OVFVirtualNetwork network = net.get(splitted[1]);
			if (net != null){
				network.getIpAddress().add(splitted[0]);

				OVFVirtualNetwork n = app.getVirtualNetworkByName(splitted[1]);

				if (n == null) {
					n = new OVFVirtualNetwork(splitted[1], network.getDescription());
					n.getIpAddress().add(splitted[0]);

					app.getAssociatedVirtualNetworks().add(n);
				}
				else {
					n.getIpAddress().add(splitted[0]);
				}
			}
		}
	}
	private static void splitVirtualNetwork_VEP_2(ApplianceDescriptor app, HashMap<String, OVFVirtualNetwork> net, String[] splitKey)
					throws MalformedOVFException {

		if (splitKey.length != 3) 
			throw new MalformedOVFException("Format invalid: Ip key inside the ProductSection of a VirtualSystem");

		String vnName = splitKey[1];
		OVFVirtualNetwork network = net.get(vnName);
		if (net != null){
			OVFVirtualNetwork n = app.getVirtualNetworkByName(vnName);

			if (n == null) {
				n = new OVFVirtualNetwork(vnName, network.getDescription());
				app.getAssociatedVirtualNetworks().add(n);
			}
			else {
				n.getIpAddress().add(splitKey[0]);
			}
		}
	}
}
