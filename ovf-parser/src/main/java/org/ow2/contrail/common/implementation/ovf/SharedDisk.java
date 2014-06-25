package org.ow2.contrail.common.implementation.ovf;

import java.util.Collection;
import java.util.LinkedHashSet;

public class SharedDisk {

	private SharedDisk parent;
	private Collection<SharedDisk> child;
	private File file;
	private String id;
	private String capacity;
	private String format;
	
	public SharedDisk(String id, File f, String capacity, String format)
	{
		this.file = f;
		this.id = id;
		this.setCapacity(capacity);
		this.setFormat(format);
		this.parent= null;
		this.child=new LinkedHashSet<SharedDisk>();	
	}
	
	/*
	public SharedDisk(String id, String capacity, String format)
	{
		this.file = null;
		this.id = id;
		this.capacity = capacity;
		this.format = format;
		this.parent= null;
		this.child=new LinkedHashSet<SharedDisk>();	
	}
	*/
	
	public File getFile()
	{
		return file;
	}
	
	public String getId() {
		return id;
	}
	
	public void setParent(SharedDisk parent){
		if(parent!=null)this.parent=parent;
		else throw new IllegalArgumentException("Cannot decide which Disk is root");
	}
	
	
	public SharedDisk getParent(){
		return parent;
	}
	
	public boolean addChild(SharedDisk child){
		return this.child.add(child);
	}
	
	public Collection<SharedDisk> getchildren(){
		LinkedHashSet<SharedDisk> toRet=new LinkedHashSet<SharedDisk>();
		toRet.addAll(this.child);
		return toRet;
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof SharedDisk && ((SharedDisk)obj).id.equals(this.id);
	}
	
	@Override
	public int hashCode(){
		return this.id.hashCode();
	}
	
	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public boolean isPersistent(){
		String[] r = format.split("\\#");
		if (r[1].equals("persistent"))
			return true;
		else
			return false;
	}
	
	public boolean isTemporary(){
		String[] r = format.split("\\#");
		if (r[1].equals("temporary"))
			return true;
		else
			return false;
	}
	
	public boolean isVirtualNetworked(){
		String[] r = format.split("\\#");
		if (r[1].equals("virtualNetworked"))
			return true;
		else
			return false;
	}
	
}
