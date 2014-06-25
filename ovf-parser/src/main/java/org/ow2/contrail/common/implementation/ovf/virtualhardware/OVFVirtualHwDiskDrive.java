package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwDiskDrive extends OVFVirtualHardware {
	
	private String hostResource;
	private Integer addressOnParent;
	private int parent;
	
	private Disk disk;
	
	public void setDisk(Disk value)
	{
		disk = value;
	}

	public Disk getDisk()
	{
		return disk;
	}
	
	public String getHostResource() {
		return hostResource;
	}
	
	public void setHostResource(String hostResource) {
		this.hostResource = hostResource;
	}
	
	public Integer getAddressOnParent() {
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
