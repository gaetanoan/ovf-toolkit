package org.ow2.contrail.common.implementation.application_extended;


import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;

/**
 * Factory to create ApplicationDescriptorExtended from an ApplicationDescriptor.
 *
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 */
public class ApplicationDescriptorExtendedFactory extends
		ExtensionDataGenerator<ApplicationDescriptor,ApplicationDescriptorExtended,Collection<?>>{

	
	private static enum decoration{SLA,SLATEMPLATE,NO_DECORATION}
	
	public ApplicationDescriptorExtendedFactory(ApplicationDescriptor data) {
		super(data);
		// TODO Auto-generated constructor stub
	}

	private static decoration getDec(Object bo){
		// commenting for preventing bamboo errors
		/*
		if (bo instanceof SLATemplate )
			return decoration.SLATEMPLATE;
		else if(bo instanceof SLA)
			return decoration.SLA;
		else 
		*/
			return decoration.NO_DECORATION;
	}
	
	
	
	/**
	 * for each object in Collection,this method first creates a <em>Decorator Generator</em> (if exists),
	 * then modifies ApplicationDescriptor with the provided decorations.
	 * 
	 * If an object in collection can not be associated to a Decorator,it will be ignored    
	 * 
	 * @param typeOfSearch collection of object used as Input Source by a Decorator Generator (subclass of ExtensionDataGenerator)
	 * @return an ApplicationDescriptorExtended instance with all decoration provided
	 */
	@Override
	protected ApplicationDescriptorExtended generateData(Collection<?> typeOfSearch) {
		// TODO Auto-generated method stub
		Collection<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>> pedC=
				new LinkedHashSet<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>>();
		if(typeOfSearch!=null) 
		for(Object bo:typeOfSearch){
			ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator ,? super ApplianceDescriptor> muu=null;
			switch(getDec(bo)){
				/*
				case SLATEMPLATE:
					muu = new SLASOIGenericDecorationGenerator<SLATemplate>((SLATemplate)bo);
					pedC.add(muu);
					break;
				case SLA:
					muu = new SLASOIGenericDecorationGenerator<SLA>((SLA)bo);
					pedC.add(muu);
					break;
				*/
				case NO_DECORATION:		
				//NOT EXIST a DECORATOR GENERATOR for BO:
				default: Logger.getLogger(getClass()).info("Need a DecorationGenerator for class "+bo.getClass()+" - ignoring");
					break;
			}
		}
		ApplicationDescriptorExtended ade=new ApplicationDescriptorExtended();
		ade.getApplianceDescriptors().addAll(super.data.getApplianceDescriptors());
		ade.setNewApplianceDataExtentions(pedC);
		return ade;
	}
}
