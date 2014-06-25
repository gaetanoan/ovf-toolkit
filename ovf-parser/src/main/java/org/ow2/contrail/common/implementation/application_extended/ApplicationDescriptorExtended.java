package org.ow2.contrail.common.implementation.application_extended;

import java.util.Collection;


import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;

/**
 * Extends ApplicationDescriptor with additional information associated with
 * ApplianceDescriptor composing this ApplicationDescriptor.
 * <br><br>
 * To get data Information this class uses
 * {@link org.ow2.contrail.common.implementation.application_extended.ExtensionDataGenerator ExtensionDataGenerator}
 * subclasses in the form <em>ExtentionDataGenerator&lt;?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor></em>
 * (in other words only ApplianceDescriptor, this ApplicationDescriptor is made of, can be extended with further information)
 * <br><br>
 * To provide new data extension for ApplianceDescriptor, users must implement<br>  
 * {@link org.ow2.contrail.common.implementation.application_extended.ExtensionDataGenerator#generateData(Object) <b>ExtensionDataGenerator</b>.searchContextualData(<b>ApplianceDescriptor</b> <em>ada</em>)}
 * method<br> 
 * with this agreement:
 * <br>
 * if <em>ApplianceDescriptor</em> parameter can be associated to the data extension, the method must return a 
 * {@link org.ow2.contrail.common.implementation.application_extended.ApplianceDescriptorDecorator ApplianceDescriptorDecorator} <em>Object</em>
 * <b>null</b> otherwise  
 *  
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 * */
public class ApplicationDescriptorExtended extends ApplicationDescriptor {

	
	/***
	 * Initialize a ApplicationDescriptorExtended with no extended data.
	 */
	public ApplicationDescriptorExtended(){
		super();
	}
	
	private void mapping(Collection<ApplianceDescriptor> adC,
			Collection<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>> extraInfo){
		for(ApplianceDescriptor ada:adC){
			
			ApplianceDescriptor last=ada;
			for(ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor> ped: extraInfo){
				ApplianceDescriptorDecorator tdc=ped.getData(last);
				if(tdc != null){
//					//substitutes ApplianceDescriptor node of an ApplicationDescriptor with new decorated node. 
					ApplianceDescriptor father=tdc.getParent();
					if(father==null){
						this.getApplianceDescriptors().remove(last);
						this.getApplianceDescriptors().add(tdc);
					}else if(father!=null&&father.getAppliancesDescriptors().remove(last))
						father.getAppliancesDescriptors().add(tdc);
					for(ApplianceDescriptor ad:tdc.getAppliancesDescriptors())ad.setParent(tdc);
					last=tdc;
				}
			}
			mapping(ada.getAppliancesDescriptors(),extraInfo);
		}
		
	}
	 
	/***
	 * Append more information to this Appliance, if a not null collection of
	 * decorator generator is provided
	 * 
	 * @param extraInfo 
	 */
	public void addApplianceDataExtentions(Collection<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>> extraInfo){
		if(extraInfo!=null)mapping(super.getApplianceDescriptors(),extraInfo);	
	}
	
	/**
	 * Initialize data structure with fresh extended data. All previous decorations will be discarded
	 * 
	 * 
	 * @param extraInfo if not null, a new set of decoration is provided.
	 */
	public void setNewApplianceDataExtentions(Collection<ExtensionDataGenerator<?,? extends ApplianceDescriptorDecorator,? super ApplianceDescriptor>> extraInfo){
		internalSetNewApplianceDecoration(this.getApplianceDescriptors());
		if(extraInfo!=null)mapping(super.getApplianceDescriptors(),extraInfo);	
		
	}

	//internal method to switch an ApplianceDescriptorDecorator to ApplianceDescriptor
	private void internalSetNewApplianceDecoration(Collection<ApplianceDescriptor> adC) {
		for(ApplianceDescriptor app:adC){
			if(app instanceof ApplianceDescriptorDecorator){
				ApplianceDescriptor father=app.getParent();
				if(father==null){
					if(this.getApplianceDescriptors().remove(app))
						this.getApplianceDescriptors()
							.add(((ApplianceDescriptorDecorator)app).getAssociatedApplianceDescriptor());
				}else if(father!=null&&father.getAppliancesDescriptors().remove(app)){
					father.getAppliancesDescriptors()
						.add(((ApplianceDescriptorDecorator)app).getAssociatedApplianceDescriptor());		
				}
				for(ApplianceDescriptor son:app.getAppliancesDescriptors())
					son.setParent(((ApplianceDescriptorDecorator)app).getAssociatedApplianceDescriptor());
			}	
			internalSetNewApplianceDecoration(app.getAppliancesDescriptors());
		}
	}
	
	/***
	 * Converts an <em>ApplicationDescriptor</em> in to ApplicationDescriptorExtended
	 * to support 
	 * {@link org.ow2.contrail.common.implementation.application_extended
	 * .ApplianceDescriptorDecorator ApplianceDescriptorDecorator} decoration.
	 * 
	 *  
	 * @param ad ApplicationDescriptor to convert
	 * @return new instance of ApplicationDescriptorExtended class containing references
	 * to underlying data structures of the ApplicationDescriptor to convert.  
	 */
	public static ApplicationDescriptorExtended newApplicationDescriptorExtended(ApplicationDescriptor ad){
		ApplicationDescriptorExtended ade=new ApplicationDescriptorExtended();
		ade.getApplianceDescriptors().addAll(ad.getApplianceDescriptors());
		return ade;
	}
	
}
