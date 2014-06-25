package org.ow2.contrail.common.implementation.ovf;

import java.net.URI;

public class OVFVirtualDisk {

	private String id;
	private URI uri;
	private long size;
	
	public OVFVirtualDisk(String lid, URI luri, long lsize)
	{
		id = lid;
		uri = luri;
		size = lsize;
	}
	
	public String getId() {
		return id;
	}
	
	public URI getUri() {
		return uri;
	}
	
	public long getSize() {
		return size;
	}
}
