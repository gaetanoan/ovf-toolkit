package org.ow2.contrail.common.implementation.application_extended;


import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;

/**
 * 
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 *
 * @param <E> generic decoration for a <em>ApplianceDescriptor</em>
 */
public class SLASOIGenericDecoration<E> extends ApplianceDescriptorDecorator {

	private E ext;
	
	
	public SLASOIGenericDecoration(ApplianceDescriptor appliance,E sla) {
		super(appliance);
		if(sla==null||appliance==null)throw new IllegalArgumentException("SLASOIGenericDecoration: NULL parameter");
		ext=sla;
		// TODO Auto-generated constructor stub
	}
	
	public E getSLASOIData(){
		return ext;
	}
	
	//TODO more complex object can simplify users life to retrieve wanted parameters ;) 
	
}
