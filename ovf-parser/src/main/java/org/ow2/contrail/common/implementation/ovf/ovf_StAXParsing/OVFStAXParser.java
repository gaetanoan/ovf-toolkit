package org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
/**
 * Creates an application descriptor from an OVF file using StAX to parse the OVF file.
 * 
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola   
 */
public class OVFStAXParser {

	public static String ovfNames="{http://schemas.dmtf.org/ovf/envelope/1}";
	public static final String ovfNames2="{http://schemas.dmtf.org/ovf/envelope/2}";
	
	public static final String ovf1="http://schemas.dmtf.org/ovf/envelope/1";
	public static final String ovf2="http://schemas.dmtf.org/ovf/envelope/2";
	
	private static Logger logger = Logger.getLogger(OVFStAXParser.class);
	
	/**
	 * Parses an OVF file
	 *  
	 * @param file file to be parsed 
	 * @return an ApplicationDescriptor representing the OVF file parsed 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws MalformedOVFException
	 */
	public ApplicationDescriptor parseOVF(URI file) throws FileNotFoundException,XMLStreamException, MalformedOVFException{
		return parseOVF(new FileInputStream(file.getPath()),false);
	}
	
	public ApplicationDescriptor parseOVF(String file) throws FileNotFoundException,XMLStreamException, MalformedOVFException{
		InputStream i = this.getClass().getResourceAsStream("/" + file);
		return parseOVF(i, false);
	}
	
	/**
	 * Parses an <em>InputStream</em> representing an OVF
	 * 
	 * @param str an input stream generating XML events
	 * @param isValidating if <em>true</em> turns on  validation for input stream  
	 * @return an ApplicationDescriptor representing an OVF stream or null if input cannot be parsed as a valid OVF  
	 * @throws XMLStreamException
	 * @throws MalformedOVFException
	 */
	public ApplicationDescriptor parseOVF(InputStream str, boolean isValidating) throws XMLStreamException, MalformedOVFException{
		
		ApplicationDescriptor appDesc = null;
		
		XMLEventReader r = null;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
			XMLEventReader event_reader = factory.createXMLEventReader(str);
			r = factory.createFilteredReader(event_reader, new FilterOVFElement());
		}
		catch (Exception e){
			throw new XMLStreamException(e.getMessage());
		}
		
		while (r.hasNext()){
			XMLEvent e = r.nextEvent();
			/*EVENT DISPATCHER*/
			switch (e.getEventType()) {
			case XMLEvent.START_ELEMENT:
				// if(e.asStartElement().getName().getLocalPart().contentEquals("Envelope") && e.getSchemaType()!=null && e.getSchemaType().getNamespaceURI().contentEquals(ovfNames))
				if (e.asStartElement().getName().getLocalPart().contentEquals("Envelope")){	
					String namespace = e.asStartElement().getName().getNamespaceURI();
					if (!namespace.equals(ovf1)){
						ovfNames = ovfNames2;
						logger.info("The namespace is " + ovfNames);
					}
					appDesc = new ApplicationDescriptor(e,r,this);
				}
				else
					throw new MalformedOVFException();
				break;
			}
		}
		return appDesc;
	}
	
	/**
	 * parses References Section of an OVF file(seen as a stream of XMLEvent) and maps <em>id</em> attribute (as string ) of ovf <em>File</em> tag
	 * to  ovf <em>File</em> tag <em>href</em> attribute (as string) 
	 * 
	 * @param files HashMap for mapping
	 * @param e current event
	 * @param r XMLEventReader instance setted on OVF file to parse
	 * @throws XMLStreamException
	 */
	public void parseReferencesTag(HashMap<String,File> files,XMLEvent e,XMLEventReader r) throws XMLStreamException{
		boolean end = false;
		while (!end&&r.hasNext()){
			e=r.nextEvent();
			end=e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("References");
			if (!end)
				switch (e.getEventType()){
					case XMLEvent.START_ELEMENT:
						if (e.asStartElement().getName().getLocalPart().contentEquals("File")){
							Attribute attrId=e.asStartElement().
									getAttributeByName(QName.valueOf(ovfNames+"id"));
							Attribute attrHref=e.asStartElement().
									getAttributeByName(QName.valueOf(ovfNames+"href"));
							Attribute attrSize=e.asStartElement().
									getAttributeByName(QName.valueOf(ovfNames+"size"));
							long size;
							try {
								size = Long.parseLong(attrSize.getValue().trim());
								files.put(attrId.getValue(),new File(attrId.getValue(),attrHref.getValue(),size));
							}
							catch (java.lang.NullPointerException eNull){
								
							}
						}
						break;
				}
		}
	}
	
	/**
	 * parses DiskSection of an OVF file(seen as a stream of XMLEvent) and sets relation between Files and Disks
	 * 
	 * @param disks
	 * @param files
	 * @param e
	 * @param r
	 * @throws XMLStreamException
	 */
	public void parseDiskSectionTag(HashMap<String,Disk> disks, HashMap<String,File> files, XMLEvent e, XMLEventReader r) throws XMLStreamException{
		boolean end=false;
		while(!end&&r.hasNext()){
			e=r.nextEvent();
			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("DiskSection"));
			if(!end)
				switch(e.getEventType()){
					case XMLEvent.START_ELEMENT:
						if(e.asStartElement().getName().getLocalPart().contentEquals("Disk")){
							String id= e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"diskId")).
									getValue().trim();
							
							//FIXME disk in ovf file can be empty
							String fileRef = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"fileRef"))
									.getValue().trim();
							
							disks.put("ovf:/disk/" + id, new Disk(files.get(fileRef),id) );
							Attribute attParRef = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"parentRef"));
							
							if (attParRef!=null){
								Disk parent=disks.get("ovf:/disk/" +attParRef.getValue());
								Disk child=disks.get("ovf:/disk/" +id); 
								child.setParent(parent);
								parent.addChild(child);
							}
						}
						break;
				}
		}	
	}
	
	public void parseSharedDiskSectionTag(HashMap<String, SharedDisk> sdisks, HashMap<String,File> files, XMLEvent e, XMLEventReader r) throws XMLStreamException{
		boolean end = false;
		while (!end && r.hasNext()){
			e = r.nextEvent();
			end = (e.isEndElement() && e.asEndElement().getName().getLocalPart().contentEquals("SharedDiskSection"));
			if (!end)
				switch (e.getEventType()){
					case XMLEvent.START_ELEMENT:
						if (e.asStartElement().getName().getLocalPart().contentEquals("SharedDisk")){
							String id = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames + "diskId")).
									getValue().trim();
							//FIXME disk in ovf file can be empty
							String capacity = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames + "capacity"))
									.getValue().trim();
							String format = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames + "format"))
									.getValue().trim();
							
							String fileRef = null;
							try {
								fileRef = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames + "fileRef"))
									.getValue().trim();
							}
							catch (NullPointerException ex){
								fileRef = "";
							}
							
							sdisks.put("ovf:/disk/" + id, new SharedDisk(id, files.get(fileRef), capacity, format) );
							
							Attribute attParRef = e.asStartElement().getAttributeByName(QName.valueOf(ovfNames + "parentRef"));
							if (attParRef!=null){
								SharedDisk parent = sdisks.get("ovf:/disk/" + attParRef.getValue());
								SharedDisk child = sdisks.get("ovf:/disk/" + id); 
								child.setParent(parent);
								parent.addChild(child);
							}
						}
						break;
				}
		}	
	}
	
	/**
	 * parses NetworkSection of an OVF file(seen as a stream of XMLEvent)
	 * 
	 * @param network
	 * @param e
	 * @param r
	 * @throws XMLStreamException
	 */
	public void parseNetworkSectionTag(HashMap<String, OVFVirtualNetwork> network, XMLEvent e, XMLEventReader r) throws XMLStreamException{
		boolean end=false;
		while(!end && r.hasNext()){
			e = r.nextEvent();
			end=(e.isEndElement() && e.asEndElement().getName().getLocalPart().contentEquals("NetworkSection"));
			if(!end){
				switch(e.getEventType()){
					case XMLEvent.START_ELEMENT:
						if(e.asStartElement().getName().getLocalPart().contentEquals("Network"))
						{
							String name = 
									e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"name")).getValue().trim();
							network.put(name,new OVFVirtualNetwork(name,e,r));		
						}
						break;
				}
			}
		}
	}
	
	/**
	 * parses Property section of an OVF file(seen as a stream of XMLEvent)
	 *  
	 * @param prop
	 * @param e
	 * @param r
	 * @throws XMLStreamException
	 */
	public void parseProductSection(OVFProperty prop,XMLEvent e,XMLEventReader r) throws XMLStreamException{
		boolean end = false;
		while (!end && r.hasNext()){
			e = r.nextEvent();
			end = (e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("Property"));
			if (!end){
				switch(e.getEventType()){
				case XMLEvent.START_ELEMENT:
					if(e.asStartElement().getName().getLocalPart().contentEquals("Label")){
						String msg=e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"msgid"))!=null?
								e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"msgid")).getValue():null;
								prop.setLabel(msg,r.getElementText());
					}
					else if(e.asStartElement().getName().getLocalPart().contentEquals("Description")){
						String msg=e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"msgid"))!=null?
								e.asStartElement().getAttributeByName(QName.valueOf(ovfNames+"msgid")).getValue():null;
								
						prop.setDescription(msg,r.getElementText());
					}
					break;
				}
			}
		}
	}
}
