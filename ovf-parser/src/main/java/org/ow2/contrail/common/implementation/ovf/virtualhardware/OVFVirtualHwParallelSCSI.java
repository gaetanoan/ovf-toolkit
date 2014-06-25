package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwParallelSCSI extends OVFVirtualHardware {

	private long address;
	private String resourceSubType;
	
	
	public long getAddress() {
		return address;
	}
	public void setAddress(long address) {
		this.address = address;
	}
	public String getResourceSubType() {
		return resourceSubType;
	}
	public void setResourceSubType(String resourceSubType) {
		this.resourceSubType = resourceSubType;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + 
			"address = " + Long.toString(address) + "\n" + 
			"resourceSubType = " + resourceSubType;
	}
	
}
