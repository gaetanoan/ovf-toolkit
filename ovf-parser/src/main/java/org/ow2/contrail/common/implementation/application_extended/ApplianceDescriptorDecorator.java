/**
 * 
 */
package org.ow2.contrail.common.implementation.application_extended;

import java.util.ArrayList;
import java.util.List;

import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;


/**
 * Decorator class (wrapper) for ApplianceDescriptor containing generic Information
 * 
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 */
public abstract class ApplianceDescriptorDecorator extends ApplianceDescriptor {
	
	protected ApplianceDescriptor appliance;
	
	public ApplianceDescriptorDecorator(ApplianceDescriptor appliance){
		super(appliance.getID(),appliance.getApplianceURI(),appliance.getSize(),appliance.isVirtualCollection());
		if(appliance.getParent()!=null)super.setParent(appliance.getParent());
		super.getAppliancesDescriptors().addAll(appliance.getAppliancesDescriptors());
		super.getVirtualSystems().addAll(appliance.getVirtualSystems());
		super.getAssociatedVirtualNetworks().addAll(appliance.getAssociatedVirtualNetworks());
		super.getProductSections().addAll(appliance.getProductSections());
		this.appliance=appliance;
	}
	
	public ApplianceDescriptor getAssociatedApplianceDescriptor(){
		if(appliance instanceof ApplianceDescriptorDecorator)
			return ((ApplianceDescriptorDecorator)appliance).getAssociatedApplianceDescriptor();
		else return appliance;
		
	}
	
	public List<ApplianceDescriptorDecorator> getAllDecorators(){
		List<ApplianceDescriptorDecorator> allDe=new ArrayList<ApplianceDescriptorDecorator>();
		allDe.add(this);
		if(appliance instanceof ApplianceDescriptorDecorator)allDe.addAll(((ApplianceDescriptorDecorator)appliance).getAllDecorators());
		return allDe;
	}
}
