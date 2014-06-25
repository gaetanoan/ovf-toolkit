package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwMemory extends OVFVirtualHardware {
	
	private long virtualQuantity;
	private String allocationUnits;
	
	public long getVirtualQuantity() {
		return virtualQuantity;
	}
	
	public void setVirtualQuantity(long virtualQuantity) {
		this.virtualQuantity = virtualQuantity;
	}
	
	public String getAllocationUnits() {
		return allocationUnits;
	}
	
	public void setAllocationUnits(String allocationUnits) {
		this.allocationUnits = allocationUnits;
	}

	@Override
	public String toString()
	{
		return super.toString() + 
			"virtualQuantity = " + Long.toString(virtualQuantity) + "\n" +
			"allocationUnits = " + allocationUnits;
	}
}
