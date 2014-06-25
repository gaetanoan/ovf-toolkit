package org.ow2.contrail.common.implementation.ovf;

import java.util.Collection;
import java.util.LinkedHashSet;

public class Disk {

	private Disk parent;
	private Collection<Disk> child;
	private File file;
	private String id;
	
	public Disk(File file, String id)
	{
		this.file = file;
		this.id = id;
		this.parent= null;
		this.child=new LinkedHashSet<Disk>();	
	}
	
	public File getFile()
	{
		return file;
	}
	
	public String getId() {
		return id;
	}
	
	public void setParent(Disk parent){
		if(parent!=null)this.parent=parent;
		else throw new IllegalArgumentException("Cannot decide which Disk is root");
	}
	
	
	public Disk getParent(){
		return parent;
	}
	
	public boolean addChild(Disk child){
		return this.child.add(child);
	}
	
	public Collection<Disk> getchildren(){
		LinkedHashSet<Disk> toRet=new LinkedHashSet<Disk>();
		toRet.addAll(this.child);
		return toRet;
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof Disk && ((Disk)obj).id.equals(this.id);
	}
	
	@Override
	public int hashCode(){
		return this.id.hashCode();
	}
	
}
