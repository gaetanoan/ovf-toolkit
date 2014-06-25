package org.ow2.contrail.common.implementation.ovf;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;

public class OVFVirtualNetwork {

	private String name;
	private String description;
	private Collection<String> ipAddress = new ArrayList<String>();
	private Collection<OVFVirtualHwEthernetAdapter> associatedNics = new ArrayList<OVFVirtualHwEthernetAdapter>();
    private String type;
	
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String value)
	{
		description = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String value)
	{
		name = value;
	}
	
	public OVFVirtualNetwork(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public Collection<String> getIpAddress()
	{
		return ipAddress;
	}
	
    public Collection<OVFVirtualHwEthernetAdapter> getAssociatedNics()
    {
        return associatedNics;
    }
    
  //FIXME addedPart for StAXParsing
  	public OVFVirtualNetwork(String name,XMLEvent e,XMLEventReader r ) throws XMLStreamException{
  		boolean end=false;
  		while(!end&&r.hasNext()){
  			e=r.nextEvent();
  			end=(e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("Network"));
  			if(!end)
  				switch(e.getEventType()){
  					case XMLEvent.START_ELEMENT:
  						if(e.asStartElement().getName().getLocalPart().contentEquals("Description")){
  							description=r.getElementText().trim();
  							this.name=name;
  						}
  						break;
  				}
  		}
  	}
}
