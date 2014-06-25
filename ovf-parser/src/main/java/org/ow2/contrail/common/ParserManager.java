package org.ow2.contrail.common;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing.OVFStAXParser;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * 
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 *
 */
public class ParserManager
{
	private ApplicationDescriptor application;

	public ApplicationDescriptor getApplication()
    {
    	return application;
    }
	
	public ParserManager(String path) throws IOException, NumberFormatException, XPathExpressionException, DOMException, ParserConfigurationException,
			SAXException, URISyntaxException, MalformedOVFException
	{
		// TODO: verify it is a path
		this(path, true);
	}

	/**
	 * 
	 * @param s
	 * @param isPath
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws URISyntaxException
	 * @throws MalformedOVFException
	 */
	public ParserManager(String s, boolean isPath) throws IOException, NumberFormatException, XPathExpressionException, DOMException,
			ParserConfigurationException, SAXException, URISyntaxException, MalformedOVFException
	{
		if (isPath)
		{
			parse(s);
		}
		else
		{
			application = org.ow2.contrail.common.implementation.ovf.OVFParser.ParseOVF(s);
		}
	}
	
	public ParserManager(URI uri) throws IOException, NumberFormatException, XPathExpressionException, DOMException,
	ParserConfigurationException, SAXException, URISyntaxException, MalformedOVFException
	{
		try {
			application = new OVFStAXParser().parseOVF(uri);
		} catch (XMLStreamException e) {
			throw new MalformedOVFException(e);
		}
	}

	private void parse(String path) throws IOException, NumberFormatException, XPathExpressionException, DOMException, ParserConfigurationException,
			SAXException, URISyntaxException, MalformedOVFException {
		try {
			application = new OVFStAXParser().parseOVF(path);
		} catch (XMLStreamException e) {
			throw new MalformedOVFException(e);
		}
	}

	/**
	 * Gets the IDs of all appliances in the application.
	 */
	public Collection<String> getAppliances()
	{
		Collection<String> result = new ArrayList<String>();

		for (ApplianceDescriptor app : application.getApplianceDescriptors())
		{
			internalGetAppliancesID(app, result);
		}

		return result;
	}

	private void internalGetAppliancesID(ApplianceDescriptor appliance, Collection<String> result)
	{
		result.add(appliance.getID());

		for (ApplianceDescriptor app : appliance.getAppliancesDescriptors())
		{
			internalGetAppliancesID(app, result);
		}
	}

	private Collection<File> internalGetApplianceImages(ApplianceDescriptor appliance, String applianceId, Collection<File> result)
	{
		if (appliance.getID().equals(applianceId))
		{
			for (Disk d : appliance.getDisks())
			{
				//changed
				Collection<Disk> resulta =new LinkedHashSet<Disk>();
				addFatherDisk(resulta,d);
				for(Disk k:resulta)result.add(k.getFile());
				//end change
				result.add(d.getFile());
			}
			return result;
		}
		else
		{
			for (ApplianceDescriptor app : appliance.getAppliancesDescriptors())
			{
				internalGetApplianceImages(app, applianceId, result);
			}
		}

		return null;
	}

	/**
	 * Gets the disks associated to an appliance.  Flattened disks return in correct 
	 * hierarchical order
	 */
	public Collection<Disk> getApplianceDisk(String applianceId)
	{
//FIXME flatdisk or disk?
		Collection<Disk> result =new LinkedHashSet<Disk>();
		//changed				
		Collection<ApplianceDescriptor> ad=new ArrayList<ApplianceDescriptor>(); 
		recursiveSearch(ad,application.getApplianceDescriptors());
		
		for (ApplianceDescriptor appliance : ad)
		{
			if (appliance.getID().equals(applianceId))
			{
				for(Disk d:appliance.getDisks()){
					addFatherDisk(result,d);
				}
			}
		}

		return result;
	}
	
	
	
	private void addFatherDisk(Collection<Disk> result,Disk disk){
		if(disk.getParent()!=null)addFatherDisk(result,disk.getParent());
		result.add(disk);
	}
	
	private void recursiveSearch(Collection<ApplianceDescriptor>toFill,Collection<ApplianceDescriptor> data){
		if(data!=null&&data.size()>0){
			toFill.addAll(data);
			for(ApplianceDescriptor ad:data){
				recursiveSearch(toFill,ad.getAppliancesDescriptors());
			}
		}
	}
	
	

	/**
	 * Gets all image files associated to an appliance. This method keep image files in correct hierarchical order 
	 */
	public Collection<File> getApplianceImages(String applianceId)
	{
		Collection<File> result = new LinkedHashSet<File>();//new ArrayList<File>();
		
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			internalGetApplianceImages(appliance, applianceId, result);
		}

		return result;
	}

	/**
	 * Gets the description associated to a virtual network
	 */
	public String getNetworkDescription(String networkId)
	{
		String networkDescription=null;
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			if(networkDescription==null){
				networkDescription=internalGetNetworkDescription(appliance, networkId);
			}
		}
		return networkDescription;
	}

	private String internalGetNetworkDescription(ApplianceDescriptor app, String networkId)
	{
		for (OVFVirtualNetwork net : app.getAssociatedVirtualNetworks())
		{
			if (net.getName().equals(networkId))
			{
				return net.getDescription();
			}
		}

		for (ApplianceDescriptor child : app.getAppliancesDescriptors())
		{
			String temp = internalGetNetworkDescription(child, networkId);
			if (temp != null)
				return temp;
		}
		return null;
	}

	/**
	 * Gets the names of the virtual networks associated to an appliance.
	 */
	public Collection<String> getApplianceNetworks(String applianceId)
	{
		Collection<String> result = new LinkedHashSet<String>();

		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			internalGetApplianceNetworks(appliance, applianceId, result);
		}

		return result;
	}

	private void internalGetApplianceNetworks(ApplianceDescriptor appliance, String applianceId, Collection<String> result)
	{
		//if(!appliance.getID().equals(applianceId)){
			
			if(appliance.getAppliancesDescriptors().size()>0){
				for(ApplianceDescriptor app:appliance.getAppliancesDescriptors()){
					internalGetApplianceNetworks(app,applianceId,result);
				}
			
			}else{
				for(OVFVirtualNetwork n : appliance.getAssociatedVirtualNetworks()){
					result.add(n.getName());
				}
				searchOtherNetApp(appliance,result);
			}
			
		//}
	}
	
	private void searchOtherNetApp(ApplianceDescriptor app,Collection<String> result){
		for(OVFVirtualNetwork n : app.getAssociatedVirtualNetworks()){
			result.add(n.getName());
		}
		for(ApplianceDescriptor ap:app.getAppliancesDescriptors()){
			searchOtherNetApp(ap,result);
		}
	}
	
//	private Collection<String> internalGetApplianceNetworks(ApplianceDescriptor appliance, String applianceId, Collection<String> result)
//	{
//		if (appliance.getID().equals(applianceId))
//		{
//			for (OVFVirtualNetwork n : appliance.getAssociatedVirtualNetworks())
//			{
//				result.add(n.getName());
//			}
//			return result;
//		}
//		else
//		{
//			for (ApplianceDescriptor app : appliance.getAppliancesDescriptors())
//			{
//				internalGetApplianceNetworks(app, applianceId, result);
//			}
//		}
//
//		return null;
//	}
	
	/**
	 * Gets the Virtual System associated to an appliance, if  appliance isVirtualCollection()
	 * method returns <em>true</em> this method recursively returns all OVFVirtualSystem composing 
	 * this appliance   
	 */
	public Collection<OVFVirtualSystem> getApplianceVirtualSystem(String applianceId)
	{
		Collection<OVFVirtualSystem> result = new ArrayList<OVFVirtualSystem>();
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			internalGetApplianceVirtualSystem(appliance, applianceId, result);
		}
		return result;

	}

	private Collection<OVFVirtualSystem> internalGetApplianceVirtualSystem(ApplianceDescriptor appliance, String id,
			Collection<OVFVirtualSystem> result)
	{
		if (appliance.getID().equals(id))
		{
			for (OVFVirtualSystem vs : appliance.getVirtualSystems())
			{
				result.add(vs);
			}
			//recursively go down
			LinkedHashSet<OVFVirtualSystem>lhsvs=new LinkedHashSet<OVFVirtualSystem>();
			getSonVirtualSystem(appliance,lhsvs);
			result.addAll(lhsvs);
		}
		else
			for (ApplianceDescriptor child : appliance.getAppliancesDescriptors())
			{
				internalGetApplianceVirtualSystem(child, id, result);
			}
		return result;

	}

	private void getSonVirtualSystem(ApplianceDescriptor appliance,Collection<OVFVirtualSystem> vsToAdd){
		for(ApplianceDescriptor app:appliance.getAppliancesDescriptors()){
			for(OVFVirtualSystem vs: app.getVirtualSystems())vsToAdd.add(vs);
			getSonVirtualSystem(app,vsToAdd);
		}
		
	}
	
	/**
	 * Gets the id of the appliances associated to an appliance
	 */
	public Collection<String> getChildren(String applianceId)
	{
		Collection<String> result = new ArrayList<String>();
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			internalGetChildren(appliance, applianceId, result);
		}
		return result;
	}

	private void internalGetChildren(ApplianceDescriptor appliance, String id, Collection<String> result)
	{
		if (appliance.getID().equals(id))
		{
			for (ApplianceDescriptor child : appliance.getAppliancesDescriptors())
			{
				result.add(child.getID());
			}
		}
		else
		{
			for (ApplianceDescriptor subApp : appliance.getAppliancesDescriptors())
				internalGetChildren(subApp, id, result);
		}
		// return result;
	}

	// FIXME: to refactor. This method is temporary, it uses a lot of resources!!!
	/**
	 * Gets all the property of the specific appliance. If any inherited property is duplicated this method keep the child property
	 */
	public Collection<OVFProperty> getAllProperty(String applianceId)
	{
		Collection<OVFProperty> result = new ArrayList<OVFProperty>();
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors())
		{
			internalGetProperty(appliance, applianceId, result);
		}
		return result;
	}

	private void internalGetProperty(ApplianceDescriptor appliance, String applianceId, Collection<OVFProperty> result)
	{
		if (appliance.getID().equals(applianceId))
		{
			result.addAll(appliance.getAllProperties());
		}
		else
		{
			for (ApplianceDescriptor subApp : appliance.getAppliancesDescriptors())
			{
				internalGetProperty(subApp, applianceId, result);
			}
		}
	}
	
	//UTILITY FUNCTION TO RENDER AN APPLICATION DESCRIPTOR TO AN OVF 
	private ParserManager(ApplicationDescriptor applDesc){
		this.application=applDesc;
	}
	
	/**
	 * retrieve All Property of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * @see #getAllProperty
	 * 
	 * @param applianceId id of an ApplianceDescriptor in ApplicationDescriptor 
	 * @param applDesc an ApplicationDescriptor instance
	 * @return a <em>Collection&lt;OVFProperty></em>
	 */
	public static Collection<OVFProperty> getAllProperty(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getAllProperty(applianceId);
	}
	/***
	 * retrieve all children of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * @see #getChildren(String)
	 * @param applianceId identifier of an ApplianceDescriptor
	 * @param applDesc an ApplicationDescriptor instance
	 * @return <
	 */
	public static Collection<String> getChildren(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getChildren(applianceId);
	}
	
	/***
	 * retrieve all {@link org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem OVFVirtualSystem} of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * @see #getApplianceVirtualSystem(String)
	 * @param applianceId identifier of an ApplianceDescriptor
	 * @param applDesc an ApplicationDescriptor instance
	 * @return a <em>Collection&lt;OVFVirtualSystemy></em> associated to the applianceId
	 */
	public static Collection<OVFVirtualSystem> getApplianceVirtualSystem(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getApplianceVirtualSystem(applianceId);
	}
	
	/**
	 * retrieve all {@link org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork OVFVirtualNetwork} of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * @see #getApplianceNetworks(String) @see org.ow2.contrail.common.implementation.application.ApplianceDescriptor#getID
	 * @param applianceId identifier of an ApplianceDescriptor
	 * @param applDesc an ApplicationDescriptor instance
	 * @return a <em>Collection&lt;String></em> with the network id associated to the ApplianceDescriptor id
	 */
	public static Collection<String> getApplianceNetworks(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getApplianceNetworks(applianceId);
	}
	
	/***
	 * Given a Network identifiers and an ApplicationDescriptor instance returns a String containing the description of the 
	 * Network  
	 * 
	 * @see #getNetworkDescription(String)
	 * 
	 */
	public static String getNetworkDescription(String networkId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getNetworkDescription(networkId);
	}
	
	/***
	 * retrieve all {@link org.ow2.contrail.common.implementation.ovf.File File} of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * 
	 * @see #getApplianceImages(String)
	 * @param applianceId identifier of an ApplianceDescriptor
	 * @param applDesc an ApplicationDescriptor instance
	 */
	public static Collection<File> getApplianceImages(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getApplianceImages(applianceId);
	}
	
	/***
	 * retrieve all {@link org.ow2.contrail.common.implementation.ovf.Disk Disk} of an <em>ApplianceDescriptor</em> instance identified by its id
	 * in an <em>ApplicationDescriptor</em> instance
	 * @see #getApplianceDisk(String)
	 * @param applianceId identifier of an ApplianceDescriptor
	 * @param applDesc an ApplicationDescriptor instance
	 */
	public static Collection<Disk> getApplianceDisk(String applianceId,ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getApplianceDisk(applianceId);
	}
	
	/***
	 * Given an Application, gets the IDs of all appliances . 
	 * @see #getAppliances()
	 * @param applDesc an ApplicationDescriptor instance
	 * @return a <em>Collection&lt;String></em> with the appliance id associated to the given ApplicationDescriptor
	 */
	public static Collection<String> getAppliances(ApplicationDescriptor applDesc){
		return new ParserManager(applDesc).getAppliances();
	}
	
	public Collection<SharedDisk> getApplianceSharedDisks(String applianceId){
		Collection<SharedDisk> result = new LinkedHashSet<SharedDisk>();
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors()) {
			result = appliance.getSharedDisks();
			// internalGetApplianceSharedDisks(appliance, applianceId, result);
		}
		return result;
	}
	
	public Collection<SharedDisk> getAllSharedDisks() {
		Collection<SharedDisk> result = new LinkedHashSet<SharedDisk>();
		for (ApplianceDescriptor appliance : application.getApplianceDescriptors()) {
			result = appliance.getSharedDisks();
		}
		return result;
	}

	/*
	private void internalGetApplianceSharedDisks(ApplianceDescriptor appliance, String applianceId, Collection<SharedDisk> result) {
			if (appliance.getID().equals(applianceId)) {
				for (SharedDisk d : appliance.getDisks())
				{
					//changed
					Collection<Disk> resulta =new LinkedHashSet<Disk>();
					addFatherDisk(resulta,d);
					for(Disk k:resulta)result.add(k.getFile());
					//end change
					result.add(d.getFile());
				}
				return result;
			}
			else
			{
				for (ApplianceDescriptor app : appliance.getAppliancesDescriptors())
				{
					internalGetApplianceImages(app, applianceId, result);
				}
			}

			return null;
		}
		
	}
	*/
	
}
