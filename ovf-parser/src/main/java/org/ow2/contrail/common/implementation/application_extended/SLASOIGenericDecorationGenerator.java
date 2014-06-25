/**
 * 
 */
package org.ow2.contrail.common.implementation.application_extended;


import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;


/**
 * Realizes a transformation for a SLATemplate, in particular instances of this class transform a SLATemplate instance
 *  in null or in a ApplianceDescriptorDecorator object
 *  
 *  @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 */
public class SLASOIGenericDecorationGenerator<E> extends 
	ExtensionDataGenerator<E,ApplianceDescriptorDecorator,ApplianceDescriptor>{

	public SLASOIGenericDecorationGenerator(E data) {
		super(data);
	}

	/**
	 *  generate null or ApplianceDescriptorDecorator
	 * 
	 * @param typeOfSearch 
	 * @return <em>ApplianceDescriptorDecorator</em> instance if Input (SLATemplate instance) refers to the <em>ApplianceDescriptor</em>
	 * <em>null</em> otherwise 
	 */
	@Override
	protected ApplianceDescriptorDecorator generateData(ApplianceDescriptor typeOfSearch) {
		//TODO more sophisticated method to gather info regarding an ApplianceDescriptor may be required
		//FIXME ASSUMING: same reference for sla and slatemplate to OVF File
		E sl=super.data;
		// commenting for preventing bamboo errors
		/*
		if (sl instanceof SLA ){
			SLA sla=(SLA)sl;
			for(int i=0;i<sla.getInterfaceDeclrs().length;i++)
				for(int j=0;j<sla.getInterfaceDeclrs()[i].getEndpoints().length;j++)
					for(int k=0;k<sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyKeys().length;k++)
						if( typeOfSearch.getID().equals(
								sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyValue(
										sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyKeys()[k]
										)
								)
						){
						//TODO maybe required to fill the decorator with more structurated data
							return new SLASOIGenericDecoration<SLA>(typeOfSearch,sla);
						}
		}else if(sl instanceof SLATemplate){
			SLATemplate sla=(SLATemplate)sl;
			for(int i=0;i<sla.getInterfaceDeclrs().length;i++)
				for(int j=0;j<sla.getInterfaceDeclrs()[i].getEndpoints().length;j++)
					for(int k=0;k<sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyKeys().length;k++)
						if( typeOfSearch.getID().equals(
								sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyValue(
										sla.getInterfaceDeclrs()[i].getEndpoints()[j].getPropertyKeys()[k]
										)
								)
						){
						//TODO maybe required to fill the decorator with more structurated data
							return new SLASOIGenericDecoration<SLATemplate>(typeOfSearch,sla);
						}
			
		}
		*/
		return null;
	}
}
