package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwIdeController extends OVFVirtualHardware {

	private long address;

	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + 
			"address = " + Long.toString(address);
	}
	
}
