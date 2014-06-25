package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;

public class OVFVirtualHwSharedDiskDrive extends OVFVirtualHardware {
	
	private String hostResource;
	private int addressOnParent;
	private int parent;
	
	private SharedDisk disk;
	
	public void setSharedDisk(SharedDisk value)
	{
		disk = value;
	}

	public SharedDisk getDisk()
	{
		return disk;
	}
	
	public String getHostResource() {
		return hostResource;
	}
	
	public void setHostResource(String hostResource) {
		this.hostResource = hostResource;
	}
	
	public int getAddressOnParent() {
		return addressOnParent;
	}
	
	public void setAddressOnParent(int addressOnParent) {
		this.addressOnParent = addressOnParent;
	}
	
	public int getParent() {
		return parent;
	}
	
	public void setParent(int parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + 
			"hostResource = " + hostResource + "\n" +
			"addressOnParent = " + Integer.toString(addressOnParent) + "\n" + 
			"parent = " + Integer.toString(parent);
	}
}
