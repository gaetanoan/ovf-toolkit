package org.ow2.contrail.common.implementation.ovf;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;


/**
 * Represents an OVF startup section block.
 */
public class OVFStartupSection 
{
	private Collection<Item> items;
	
	public OVFStartupSection(){
		items=new ArrayList<Item>();
		
	}
	
	public Collection<Item> getItems()
	{
		return items;
	}
	
	public static class Item {
		
		private String id;
		private int order;
		
		private ApplianceDescriptor appliance;
		
		public Item(String id, int order)
		{
			this.id =  id;
			this.order = order;
		}
		
		public String getId()
		{
			return id;
		}
		
		public int getOrder() 
		{
			return order;
		}
		
		public ApplianceDescriptor getApplianceDescriptor()
		{
			return appliance;
		}
		
		public void setApplianceDescriptor(ApplianceDescriptor appliance)
		{
			this.appliance = appliance;
		}
	}
	
	//FIXME added to StAx
	public OVFStartupSection(XMLEvent e, XMLEventReader r) throws XMLStreamException {
		// TODO Auto-generated constructor stub
		this();
		boolean end=false;
		while(!end&&r.hasNext()){
			e=r.nextEvent();
			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("StartupSection"));
			if(!end){
				switch(e.getEventType()){
				case XMLEvent.START_ELEMENT:
					String tagName=e.asStartElement().getName().getLocalPart();
					if(tagName.contentEquals("Item")){
						items.add(new Item(e.asStartElement().getAttributeByName(
								QName.valueOf("{http://schemas.dmtf.org/ovf/envelope/1}id")).getValue().trim(),
								Integer.parseInt(e.asStartElement().getAttributeByName(
										QName.valueOf("{http://schemas.dmtf.org/ovf/envelope/1}order")).getValue())));
					}
					break;
				}
			}
		}
	}
	//FIXME END added to StAx
}
