package org.ow2.contrail.common.implementation.ovf;

/**
 * Defines a virtual hardware requirements.
 */
public class OVFVirtualHardware {

	private int instanceId;
	private int resourceType;
	
	private String elementName;
	private String description;
	
	private boolean required;
	
	// Getter methods
	
	public boolean isRequired() {
		return required;
	}
	
	public int getInstanceId() {
		return instanceId;
	}
	
	public int getResourceType() {
		return resourceType;
	}
	
	public String getElementName() {
		return elementName;
	}
	
	public String getDescription() {
		return description;
	}
	public boolean getRequired() {
		return required;
	}
	// Setter methods
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public void setElementName(String value) {
		elementName = value;
	}
	
	public void setDescription(String value) {
		description = value;
	}
	
	public void setInstanceId(int value) {
		instanceId = value;
	}
	
	public void setResourceType(int value) {
		resourceType = value;
	}
	
	@Override
	public String toString()
	{
		return "instanceId = " + Integer.toString(instanceId) + "\n" + 
		"resourceType = " + Integer.toString(resourceType) + "\n" + 
		"elementName = " + elementName + "\n" + "description = " + description + "\n" +  
		"required = " + Boolean.toString(required) + "\n";
	}
}
