package org.ow2.contrail.common.implementation.ovf.virtualhardware;

import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;

public class OVFVirtualHwEthernetAdapter extends OVFVirtualHardware {

	private Integer addressOnParent=null;
	private boolean automaticAllocation;
	private String connection="";
	private String resourceSubType;

	//Either "dhcp" or a fixed ip such as "10.1.1.1", does not come from the OVF spec but from a parsing of the ProductSection
	private String address;

	public String getAddress(){
		return address;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public Integer getAddressOnParent() {
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
	
	public boolean getAutomaticAllocation() {
		return automaticAllocation;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
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
				"addressOnParent = " + 
				addressOnParent + "\n" +
				"automaticAllocation = " + Boolean.toString(automaticAllocation) + "\n" +
				"connection = " + connection + "\n" +
				"resourceSubType = " + resourceSubType; 
	}
}
