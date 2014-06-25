package org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing;

import javax.xml.stream.events.XMLEvent;

/**
 * Filter for StAX event 
 * 
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 *
 */
public class FilterOVFElement implements javax.xml.stream.EventFilter {


	public boolean accept(XMLEvent event) {
		// TODO Auto-generated method stub
		if (!event.isStartElement() && !event.isEndElement() && !event.isCharacters())return false;
		else return true;
		
	}

}
