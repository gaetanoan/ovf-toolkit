package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwFloppyDrive extends OVFVirtualHardware {
	
	private int addressOnParent;
	private boolean automaticAllocation;
	
	public int getAddressOnParent() {
		return addressOnParent;
	}
	
	public void setAddressOnParent(int addressOnParent) {
		this.addressOnParent = addressOnParent;
	}
	
	public boolean isAutomaticAllocation() {
		return automaticAllocation;
	}
	
	public void setAutomaticAllocation(boolean automaticAllocation) {
		this.automaticAllocation = automaticAllocation;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + 
			"addressOnParent = " + Integer.toString(addressOnParent) + "\n" +
			"automaticAllocation = " + Boolean.toString(automaticAllocation);
	}

}
