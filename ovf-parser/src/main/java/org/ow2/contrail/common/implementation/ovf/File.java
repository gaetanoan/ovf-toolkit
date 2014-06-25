package org.ow2.contrail.common.implementation.ovf;

/** Only one id per tree is allowed*/
public class File {
	 
	private String id;
	private String uri;
	
	private long size;
	
	public File(String id, String uri, long size)
	{
		this.id = id;
		this.uri = uri;
		this.size = size;
		
	}
	
	public String getId() {
		return id;
	}
	
	public String getUri() {
		return uri;
	}
	
	public long getSize() {
		return size;
	}
	
		
}
