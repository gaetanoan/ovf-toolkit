package org.ow2.contrail.common.implementation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;
import org.ow2.contrail.common.implementation.ovf.OVFProductSection;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFStartupSection;
import org.ow2.contrail.common.ParserManager;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCdDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwDiskDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwFloppyDrive;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwIdeController;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwParallelSCSI;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.log4j.*;

/**
 * Class to transform either instance of ApplicationDescriptor or ApplianceDescriptor in an OVF File Descriptor. 
 * Limitation: users are delegated to verify the significance of the generated OVF File.
 * 
 * Nevertheless, it is guaranteed the significance of the OVF file  if instances of ApplicationDescriptor or ApplianceDescriptor
 * were congruent. 
 * 
 * Either ApplicationDescriptor or ApplianceDescriptor instances are congruent if:
 * 
 *   they were generated from an OVF file and set operations were not performed over them
 *   
 *   
 */
public class Renderer {

	private static Logger logger = Logger.getLogger(Renderer.class);
	private static Collection<File> files = new ArrayList<File>();
	private static Collection<Disk> disks = new ArrayList<Disk>();
	private static Collection<String> appId = new ArrayList<String>();
	private static HashMap<String, Collection<String>> children = new HashMap<String, Collection<String>>();
	// private static Collection<OVFVirtualNetwork> networks = new
	// ArrayList<OVFVirtualNetwork>();
	private static Collection<String> netName = new ArrayList<String>();
	private static HashMap<String, Collection<OVFVirtualSystem>> vs = new HashMap<String, Collection<OVFVirtualSystem>>();
	private static ApplicationDescriptor globalDesc;
	
	/**
	 * ApplicationDescriptor to OVF File Descriptor rendering 
	 * 
	 * @param applDesc ApplicationDescriptor identifier
	 * @param dest path to file to write 
	 * @param nameFile name of the file to write
	 * @throws Exception
	 */
	public static void RenderOVF(ApplicationDescriptor applDesc, String dest, String nameFile) throws Exception{
		logger.info(" Ovf_Renderer - rendering ovf with ApplicationDescriptor source");	
		clearInternalData();
		globalDesc=applDesc;
		getInfo(applDesc);
		writeOVF(dest, nameFile);
	}

	private static void clearInternalData() {
		files.clear();
		disks.clear();
		appId.clear();
		children.clear();
		netName.clear();
		vs.clear();
	}
	
	/**
	 *  ApplianceDescriptor to OVF File Descriptor rendering 
	 *  
	 * @param appliance identifiers of the appliance to render
	 * @param dest path to OVF destination file 
	 * @param nameFile name of OVF destination file 
	 * @throws TransformerConfigurationException
	 * @throws IOException
	 * @throws TransformerFactoryConfigurationError
	 * @throws SAXException
	 * @throws Exception
	 */
	public static void RenderOVF(ApplianceDescriptor appliance,String dest,String nameFile) throws TransformerConfigurationException, IOException, TransformerFactoryConfigurationError, SAXException, Exception{
		clearInternalData();
		logger.info(" Ovf_Renderer - rendering ovf with ApplianceDescriptor source");
		ApplicationDescriptor newAppli=new ApplicationDescriptor();
		newAppli.getApplianceDescriptors().add(appliance);
		globalDesc=newAppli;
		getInfo(globalDesc);
		writeOVF(dest, nameFile);
	}
	
	private static void getInfo(ApplicationDescriptor appDesc) {
		appId = ParserManager.getAppliances(appDesc);
		for (String id : appId) {
			Collection<Disk> disk = ParserManager.getApplianceDisk(id,appDesc);
			for (Disk d : disk) {
				if (!disks.contains(d))
					disks.add(d);
			}
			Collection<String> name = ParserManager.getApplianceNetworks(id,appDesc);
			for (String net : name) {
				if (!netName.contains(net)) {
					netName.add(net);
				}
			}
			vs.put(id, ParserManager.getApplianceVirtualSystem(id,appDesc));
			children.put(id, ParserManager.getChildren(id,appDesc));
			
		}
		for (Disk d : disks) {
			if (!files.contains(d.getFile()))
				files.add(d.getFile());
		}
	}
	
	
	//MODIFING STOP
	public static void RenderOVF(String path, String dest, String nameFile) throws Exception {

		logger.info(" Ovf_Renderer - loading parser");
		logger.info(" Ovf_Renderer - rendering ovf with ApplicationDescription source"); 
		globalDesc = new ParserManager(path).getApplication();
		getInfo(globalDesc);
		writeOVF(dest, nameFile);
	}

	private static void writeOVF(String dest, String nameFile)
			throws IOException, TransformerFactoryConfigurationError,
			TransformerConfigurationException, SAXException, Exception {
		logger.info(" Ovf_Renderer - info loaded\n");
		logger.info(" Ovf_Renderer - loading transformer");
		FileWriter fw=new FileWriter(dest + "/" + nameFile + ".ovf");
		StreamResult streamResult = new StreamResult(new PrintWriter(fw));
		SAXTransformerFactory trasformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler handler = trasformerFactory.newTransformerHandler();
		Transformer serializer = handler.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty(OutputKeys.METHOD, "xml");
		handler.setResult(streamResult);
		logger.info(" Ovf_Renderer - transformer ready\n");

		createDocument(handler);
		createReference(handler);
		createDiskSection(handler);
		createNetworkSection(handler);
		createVirtualSystem(handler);
		closeDocument(handler);
		
		streamResult.getWriter().flush();
		streamResult.getWriter().close();
		
	}

	private static void createVirtualSystem(TransformerHandler handler) throws Exception {
		logger.info(" Ovf_Renderer - creating virtual system section");
		Collection<String> tempId = new ArrayList<String>();
		for (String id : appId) {
			if (!tempId.contains(id)) {
				if (children.get(id).size() != 0) {
					createVSCollection(handler, id, tempId);
				} else {
					createVS(handler, id, tempId);
				}
			}
		}
		logger.info(" Ovf_Renderer - virtual system section done\n");
	}

	private static void createVSCollection(TransformerHandler handler, String appId, Collection<String> tempId)
			throws Exception {
		//  we are assuming:
		// - appliance without virtualsystem -> virtual  system
		// - appliance with 1 or more virtualsystem -> virtual system collection
		logger.info("");
		logger.info(" Ovf_Renderer - creating virtual system collection: " + appId);
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "ovf:id", "CDATA", appId);
		handler.startElement("", "", "VirtualSystemCollection", atts);
		tempId.add(appId);
		//FIXME miss the infoSection
		writeTextTag("Info", "No info for " + appId, null, handler);
		writeTextTag("Name", "Should be a better name: " + appId, null, handler);
		createProductSection(handler,appId);
		// FIXME: miss the resourcesallocation section
		createStartupSection(handler,appId);
		Collection<String> child = children.get(appId);
		for (String childId : child) {
			if (children.get(childId).size() != 0) {
				createVSCollection(handler, childId, tempId);
			} else {
				createVS(handler, childId, tempId);
			}
		}
		handler.endElement("", "", "VirtualSystemCollection");
		logger.info(" OVF_Renderer - virtual system collection done\n");
	}

	private static void createStartupSection(TransformerHandler handler,String appli) throws SAXException {
		//FIXME lack of information
		logger.info(" Ovf_Renderer - creating startup section");
		
		Collection<ApplianceDescriptor> appls=globalDesc.getApplianceDescriptors();
		ApplianceDescriptor found=searchingForAppliance(appls,appli);
		if(found!=null&&found.getOVFStartupSection()!=null){
			handler.startElement("", "", "StartupSection", null);
			writeTextTag("Info", "Information LACK ", null, handler);
			for(OVFStartupSection.Item item: found.getOVFStartupSection().getItems()){	
				AttributesImpl impl=new AttributesImpl();
				impl.addAttribute("", "", "ovf:id","CDATA", item.getId());
				impl.addAttribute("", "", "ovf:order","CDATA", item.getOrder()+"");
				handler.startElement("", "", "Item", impl);
				handler.endElement("", "", "Item");
			}
			handler.endElement("", "", "StartupSection");
		}
		logger.info(" Ovf_Renderer - startup section done");
	}

	private static ApplianceDescriptor searchingForAppliance(Collection<ApplianceDescriptor> appls,String app){
		ApplianceDescriptor temp=null;
		for(ApplianceDescriptor applia: appls){
			if(applia.getID().equals(app))return applia ;
			temp=searchingForAppliance(applia.getAppliancesDescriptors(),app);
			if(temp!=null)return temp;
		}
		
		return null;
	}
	
	private static void createProductSection(TransformerHandler handler,String appi) throws SAXException {
		logger.info(" Ovf_Renderer - creating product section");
		ApplianceDescriptor found=searchingForAppliance(globalDesc.getApplianceDescriptors(),appi);
		if(found!=null){
			
			for(OVFProductSection ovfpd:found.getProductSections()){
				AttributesImpl atts0 = new AttributesImpl();
				boolean isAttr0=false,isAttr1=false;
				if(ovfpd.getClassName()!=null&&(isAttr0=!isAttr0))
					atts0.addAttribute("", "", "ovf:class", "CDATA",ovfpd.getClassName());
				//assignament to boolean if first test is true
				if(ovfpd.getInstance()!=null&&(isAttr1=!isAttr1))
					atts0.addAttribute("", "", "ovf:class", "CDATA",ovfpd.getInstance());
				handler.startElement("", "", "ProductSection", (isAttr0||isAttr1?atts0:null));
				
				if(ovfpd.getInfo()!=null)writeTextTag("Info",ovfpd.getInfo(),null,handler);
				else writeTextTag("Info", "No info for " + appi, null, handler);
				if(ovfpd.getProduct()!=null)writeTextTag("Product",ovfpd.getProduct(),null,handler);
				if(ovfpd.getVendor()!=null)writeTextTag("Vendor",ovfpd.getVendor(),null,handler);
				if(ovfpd.getVersion()!=null)writeTextTag("Version",ovfpd.getVersion(),null,handler);
				if(ovfpd.getFullVersion()!=null)writeTextTag("FullVersion",ovfpd.getFullVersion(),null,handler);
				if(ovfpd.getProductUrl()!=null)writeTextTag("ProductUrl",ovfpd.getProductUrl(),null,handler);
				if(ovfpd.getVendorUrl()!=null)writeTextTag("VendorUrl",ovfpd.getVendorUrl(),null,handler);
				if(ovfpd.getCategory()!=null)//FIXME where is ovf:msgid in category?
					writeTextTag("Category",ovfpd.getCategory(),null,handler);
				if(ovfpd.getProperties()!=null&&ovfpd.getProperties().size()>0){
					for(OVFProperty prop:ovfpd.getProperties()){
						AttributesImpl atts1=new AttributesImpl();
						if(prop.getKey()!=null)
							atts1.addAttribute("", "","ovf:key" ,"CDATA",prop.getKey());
						if(prop.getValue()!=null)
							atts1.addAttribute("", "","ovf:value" ,"CDATA",prop.getValue());
						if(prop.getType()!=null)
							atts1.addAttribute("", "","ovf:type" ,"CDATA",prop.getType());
						if(prop.getQualifiers()!=null)
							atts1.addAttribute("", "","ovf:qualifiers" ,"CDATA",prop.getQualifiers());
						if(prop.getPassword())
							atts1.addAttribute("", "","ovf:password" ,"CDATA",prop.getPassword()+"");
						if(prop.isUserConfigurable())
							atts1.addAttribute("", "","ovf:userConfigurable","CDATA",prop.isUserConfigurable()+"");
						handler.startElement("", "", "Property", atts1);
						if(prop.getLabel()!=null){
							AttributesImpl atts2=new AttributesImpl();
							atts2.addAttribute("","","ovf:msgid","CDATA",prop.getLabel().getMsgid());
							writeTextTag("Label",prop.getLabel().getDescription(),atts2,handler);
						}
						if(prop.getDescription()!=null){
							AttributesImpl atts2=new AttributesImpl();
							atts2.addAttribute("","","ovf:msgid","CDATA",prop.getDescription().getMsgid());
							writeTextTag("Description",prop.getDescription().getDescription(),atts2,handler);
						}
						
						handler.endElement("", "", "Property");
					}
				}
					
				handler.endElement("", "", "ProductSection");
			}	
		}
		
		logger.info(" Ovf_Renderer - product section done");
	}
	
	private static void createVS(TransformerHandler handler, String appId, Collection<String> tempId) throws Exception {
		logger.info("");
		logger.info(" Ovf_Renderer - creating virtual system: " + appId);
		Collection<String> child = children.get(appId);
		if (child.size() != 0) {
			throw new Exception("error while trying to create virtualsystem node");
		} else {
			
					AttributesImpl atts = new AttributesImpl();
					atts.addAttribute("", "", "ovf:id", "CDATA", appId);
					handler.startElement("", "", "VirtualSystem", atts);
					// TODO: fancy info tag 
					writeTextTag("Info", "No info for " + appId, null, handler);
					createProductSection(handler,appId);
					createHardwareSection(handler,vs.get(appId));
					handler.endElement("", "", "VirtualSystem");
					if(!tempId.contains(appId))tempId.add(appId);
				
			
		}
		logger.info(" Ovf_Renderer - virtual system done");
	}

	private static void createHardwareSection(TransformerHandler handler, Collection<OVFVirtualSystem> systems) throws SAXException {
		logger.info(" Ovf_Renderer - creating hardware section");
		for(OVFVirtualSystem system:systems){
			handler.startElement("", "", "VirtualHardwareSection", null);
			writeTextTag("Info", "No info for "+system.getId(), null, handler);
			handler.startElement("", "", "System", null);
			writeTextTag("vssd:ElementName", "Virtual Hardware Family", null, handler);
			writeTextTag("vssd:InstanceID", "0", null, handler);
			writeTextTag("vssd:VirtualSystemType", "vmx-04", null, handler);
			Collection<OVFVirtualHardware> hardware = system.getRequiredHardware();
			handler.endElement("", "", "System");
			createItemHW(hardware, handler);
			handler.endElement("", "", "VirtualHardwareSection");
		}
		logger.info(" Ovf_Renderer - hardware section done");
	}

	private static void createItemHW(Collection<OVFVirtualHardware> hw, TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - creating item section");
		for (OVFVirtualHardware hardware : hw) {
			String name = hardware.getClass().getSimpleName();
			AttributesImpl atts = new AttributesImpl();
			atts.addAttribute("", "", "ovf:required","CDATA", hardware.isRequired()?"true":"false");
			//FIXME miss bound information
			handler.startElement("", "", "Item", atts);
			if ("OVFVirtualHwCdDrive".equals(name)) {
				OVFVirtualHwCdDrive cd = (OVFVirtualHwCdDrive) hardware;
				if (cd.getDescription() != null)
					writeTextTag("rasd:Description", cd.getDescription(), null, handler);
				if (cd.getElementName() != null)
					writeTextTag("rasd:ElementName", cd.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(cd.getInstanceId()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(cd.getResourceType()), null, handler);
				writeTextTag("rasd:Parent", Integer.toString(cd.getParent()), null, handler);
				writeTextTag("rasd:AutomaticAllocation", Boolean.toString(cd.isAutomaticAllocation()), null, handler);
				writeTextTag("rasd:AddressOnParent", Integer.toString(cd.getAddressOnParent()), null, handler);
				
			}
			if ("OVFVirtualHwCpu".equals(name)) {
				OVFVirtualHwCpu cpu = (OVFVirtualHwCpu) hardware;
				
				if (cpu.getAllocationUnits() != null)
					writeTextTag("rasd:AllocationUnits", cpu.getAllocationUnits(), null, handler);
				if (cpu.getDescription() != null)
					writeTextTag("rasd:Description", cpu.getDescription(), null, handler);
				if (cpu.getElementName() != null)
					writeTextTag("rasd:ElementName", cpu.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(cpu.getInstanceId()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(cpu.getResourceType()), null, handler);
				writeTextTag("rasd:VirtualQuantity", Long.toString(cpu.getVirtualQuantity()), null, handler);
				
				
			}

			if ("OVFVirtualHwDiskDrive".equals(name)) {
				OVFVirtualHwDiskDrive ddrive = (OVFVirtualHwDiskDrive) hardware;
				
				if(ddrive.getAddressOnParent()!=null)
					writeTextTag("rasd:AddressOnParent", Integer.toString(ddrive.getAddressOnParent()), null, handler);
				if (ddrive.getDescription() != null)
					writeTextTag("rasd:Description", ddrive.getDescription(), null, handler);
				if (ddrive.getElementName() != null)
					writeTextTag("rasd:ElementName", ddrive.getElementName(), null, handler);
				if (ddrive.getHostResource() != null)
					writeTextTag("rasd:HostResource", (ddrive.getHostResource()), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(ddrive.getInstanceId()), null, handler);
				writeTextTag("rasd:Parent", Integer.toString(ddrive.getParent()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(ddrive.getResourceType()), null, handler);
				
			}
			if ("OVFVirtualHwEthernetAdapter".equals(name)) {
				OVFVirtualHwEthernetAdapter net = (OVFVirtualHwEthernetAdapter) hardware;
				
				if(net.getAddressOnParent()!= null)
					writeTextTag("rasd:AddressOnParent", Integer.toString(net.getAddressOnParent()), null, handler);
				writeTextTag("rasd:AutomaticAllocation", Boolean.toString(net.isAutomaticAllocation()), null, handler);
				if (net.getConnection() != null)
					writeTextTag("rasd:Connection", net.getConnection(), null, handler);
				if (net.getDescription() != null)
					writeTextTag("rasd:Description", net.getDescription(), null, handler);
				if (net.getElementName() != null)
					writeTextTag("rasd:ElementName", net.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(net.getInstanceId()), null, handler);
				if (net.getResourceSubType() != null)
					writeTextTag("rasd:ResourceSubType", net.getResourceSubType(), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(net.getResourceType()), null, handler);	
				
			}
			if ("OVFVirtualHwFloppyDrive".equals(name)) {
				OVFVirtualHwFloppyDrive floppy = (OVFVirtualHwFloppyDrive) hardware;
				
				if (floppy.getDescription() != null)
					writeTextTag("rasd:Description", floppy.getDescription(), null, handler);
				if (floppy.getElementName() != null)
					writeTextTag("rasd:ElementName", floppy.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(floppy.getInstanceId()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(floppy.getResourceType()), null, handler);
				writeTextTag("rasd:AutomaticAllocation", Boolean.toString(floppy.isAutomaticAllocation()), null,
						handler);
				writeTextTag("rasd:AddressOnParent", Integer.toString(floppy.getAddressOnParent()), null, handler);
				
			}
			if ("OVFVirtualHwIdeController".equals(name)) {
				OVFVirtualHwIdeController ide = (OVFVirtualHwIdeController) hardware;
				
				if (ide.getDescription() != null)
					writeTextTag("rasd:Description", ide.getDescription(), null, handler);
				if (ide.getElementName() != null)
					writeTextTag("rasd:ElementName", ide.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(ide.getInstanceId()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(ide.getResourceType()), null, handler);
				writeTextTag("rasd:Address", Long.toString(ide.getAddress()), null, handler);
				
			}
			if ("OVFVirtualHwMemory".equals(name)) {
				OVFVirtualHwMemory mem = (OVFVirtualHwMemory) hardware;
				
				if (mem.getAllocationUnits() != null)
					writeTextTag("rasd:AllocationUnits", mem.getAllocationUnits(), null, handler);
				if (mem.getDescription() != null)
					writeTextTag("rasd:Description", mem.getDescription(), null, handler);
				if (mem.getElementName() != null)
					writeTextTag("rasd:ElementName", mem.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(mem.getInstanceId()), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(mem.getResourceType()), null, handler);
				writeTextTag("rasd:VirtualQuantity", Long.toString(mem.getVirtualQuantity()), null, handler);
				
			}
			if ("OVFVirtualHwParallelSCSI".equals(name)) {
				OVFVirtualHwParallelSCSI scsi = (OVFVirtualHwParallelSCSI) hardware;
				
				writeTextTag("rasd:AddressOnParent", Long.toString(scsi.getAddress()), null, handler);
				if (scsi.getDescription() != null)
					writeTextTag("rasd:Description", scsi.getDescription(), null, handler);
				if (scsi.getElementName() != null)
					writeTextTag("rasd:ElementName", scsi.getElementName(), null, handler);
				writeTextTag("rasd:InstanceID", Integer.toString(scsi.getInstanceId()), null, handler);
				if (scsi.getResourceSubType() != null)
					writeTextTag("rasd:ResourceSubType", scsi.getResourceSubType(), null, handler);
				writeTextTag("rasd:ResourceType", Integer.toString(scsi.getResourceType()), null, handler);	
				
			}
			handler.endElement("", "", "Item");
		}
		logger.info(" Ovf_Renderer - item section done");
	}

	private static void createNetworkSection(TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - creating network section");
		handler.startElement("", "", "NetworkSection", null);
		writeTextTag("Info", "List of logical networks used in the package", null, handler);	
		for (String name : netName) {
			
			AttributesImpl atts = new AttributesImpl();
			atts.addAttribute("", "", "ovf:name", "CDATA", name);// vn.getName());
			handler.startElement("", "", "Network", atts);
			atts.clear();
			atts.addAttribute("", "", "ovf:msgid", "CDATA", "network.description");
			handler.startElement("", "", "Description", atts);
			char[] desc;
			if(ParserManager.getNetworkDescription(name,globalDesc)!=null){
				desc = (ParserManager.getNetworkDescription(name,globalDesc)).toCharArray();// vn.getDescription()).toCharArray();
			}else{				
				desc = ("No description for "+name).toCharArray();
			}
			handler.characters(desc, 0, desc.length);
			handler.endElement("", "", "Description");
			handler.endElement("", "", "Network");
		}
		handler.endElement("", "", "NetworkSection");
		logger.info(" Ovf_Renderer - network secton done\n");
	}

	private static void writeTextTag(String tag, String text, AttributesImpl atts, TransformerHandler handler)
			throws SAXException {
		handler.startElement("", "", tag, atts);
		char[] info = new String(text).toCharArray();
		handler.characters(info, 0, info.length);
		handler.endElement("", "", tag);
	}

	private static void createDiskSection(TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - creating disk section");
		handler.startElement("", "", "DiskSection", null);
		writeTextTag("Info", "Describes the set of virtual disks", null, handler);
		for (Disk d : disks) {

			AttributesImpl atts = new AttributesImpl();
			if(d.getId()!=null)
				atts.addAttribute("", "", "ovf:diskId", "CDATA", d.getId());
			if(d.getFile()!=null&&d.getFile().getId()!=null)
				atts.addAttribute("", "", "ovf:fileRef", "CDATA", d.getFile().getId());
			// FIXME: check the capacity<- parser don't look at
			atts.addAttribute("", "", "ovf:capacity", "CDATA", "4294967296");
			if(d.getParent()!=null)
				atts.addAttribute("", "", "ovf:parentRef", "CDATA", d.getParent().getId());
			//FIXME: parser don't track ovf:format parameter that's  default one
			atts.addAttribute("", "", "ovf:format", "CDATA",
					"http://www.vmware.com/specifications/vmdk.html#streamOptimized");
			handler.startElement("", "", "Disk", atts);
			handler.endElement("", "", "Disk");
		}
		handler.endElement("", "", "DiskSection");
		logger.info(" Ovf_Renderer - disk section done\n");
	}

	private static void createReference(TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - creating reference section");
		handler.startElement("", "", "References", null);
		for (File f : files) {
			AttributesImpl atts = new AttributesImpl();
			if(f.getId()!=null)
				atts.addAttribute("", "", "ovf:id", "CDATA", f.getId());
			if(f.getUri()!=null)	
				atts.addAttribute("", "", "ovf:href", "CDATA", f.getUri());
			atts.addAttribute("", "", "ovf:size", "CDATA", Long.toString(f.getSize()));
			handler.startElement("", "", "File", atts);
			handler.endElement("", "", "File");
		}
		handler.endElement("", "", "References");
		logger.info(" Ovf_Renderer - reference section done\n");
	}

//	private static void getInfo(ParserManager parser) {
//		appId = parser.getAppliances();
//		for (String id : appId) {
//			Collection<Disk> disk = parser.getApplianceDisk(id);
//			for (Disk d : disk) {
//				if (!disks.contains(d))
//					disks.add(d);
//			}
//			Collection<String> name = parser.getApplianceNetworks(id);
//			for (String net : name) {
//				if (!netName.contains(net)) {
//					netName.add(net);
//				}
//			}
//			vs.put(id, parser.getApplianceVirtualSystem(id));
//			children.put(id, parser.getChildren(id));
//		}
//		for (Disk d : disks) {
//			if (!files.contains(d.getFile()))
//				files.add(d.getFile());
//		}
//	}

	private static void createDocument(TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - creating document");
		handler.startDocument();
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "xmlns:xsi", "CDATA", "http://www.w3.org/2001/XMLSchema-instance");
		atts.addAttribute("", "", "xmlns", "CDATA", "http://schemas.dmtf.org/ovf/envelope/1");
		atts.addAttribute("", "", "xmlns:ovf", "CDATA", "http://schemas.dmtf.org/ovf/envelope/1");
		atts.addAttribute("", "", "xmlns:vssd", "CDATA",
				"http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData");
		atts.addAttribute("", "", "xmlns:rasd", "CDATA",
				"http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData");
		handler.startElement("", "", "Envelope", atts);
		logger.info(" Ovf_Renderer - document ready\n");
	}

	private static void closeDocument(TransformerHandler handler) throws SAXException {
		logger.info(" Ovf_Renderer - closing document");
		handler.endElement("", "", "Envelope");
		handler.endDocument();
		logger.info(" Ovf_Renderer - document closed\n");
	}
	
	/***
	 * Generate a Deploy Document (a xsl file) containing information for deployment of <em>Collection&lt;ApplianceDescriptor></em>
	 * this method assumes that every <em>ApplicanceDescriptor</em> have an unique id  overall <em>ApplicationDescriptor</em> this collection belongs to.
	 *  
	 * 
	 * @param ad ApplianceDescritptor to deploy
	 * @param output name(and extension) of file to generate
	 * @throws XMLStreamException
	 * @throws IOException 
	 */
	public static void generateDD(Collection<ApplianceDescriptor> cad, String output) throws XMLStreamException, IOException{
		//TODO Add deploy information 
		String xslNames="http://www.w3.org/1999/XSL/Transform";
		String ovfNames="http://schemas.dmtf.org/ovf/envelope/1";
		
		XMLOutputFactory xof =  XMLOutputFactory.newInstance();
		XMLStreamWriter xtw = xof.createXMLStreamWriter(new FileWriter(output)); 
		xtw.writeStartDocument("UTF-8","1.0");
			
		xtw.writeStartElement("xsl","transform",xslNames);
		xtw.writeAttribute("version","1.0");
		xtw.writeNamespace("xsl", xslNames);
		xtw.writeNamespace("ovf",ovfNames);
		xtw.writeEmptyElement(xslNames, "output");
		xtw.writeAttribute("indent", "yes");
		xtw.writeAttribute("method", "xml");
			xtw.writeStartElement(xslNames, "template");
			xtw.writeAttribute("match","/");
			xtw.writeStartElement("Deployment");	
				
		for(ApplianceDescriptor ad:cad){
			ApplicationDescriptor newAppli=new ApplicationDescriptor();
			newAppli.getApplianceDescriptors().add(ad);
			
			Collection<String> netName=ParserManager.getApplianceNetworks(ad.getID(),newAppli);
			Collection<Disk> colD=ParserManager.getApplianceDisk(ad.getID(),newAppli);
			
			
			String selectFile="", selectDisk="" , selectNetwork="";
			Disk first=null;
			if(colD.iterator().hasNext())first=colD.iterator().next();
			for(Disk d:colD){
				selectFile+=(d!=first?" or ":"")+"@ovf:id='"+d.getFile().getId()+"'";
				selectDisk+=(d!=first?" or ":"")+"@ovf:diskId='"+d.getId()+"'";
			}
			String firstStr="";
			if(netName.iterator().hasNext())
				firstStr=netName.iterator().next();
			
			for(String name:netName)selectNetwork+=(name!=firstStr?" or ":"")+"@ovf:name='"+name+"'";
			xtw.writeStartElement("DeployData");
			xtw.writeAttribute("ovfRef", ad.getID());
			xtw.writeEmptyElement(xslNames, "copy-of");
			xtw.writeAttribute("select","descendant::ovf:"+(ad.isVirtualCollection()?"VirtualSystemCollection":"VirtualSystem")+"[@ovf:id='"+ad.getID()+"']");
			
			if(selectFile.length()>0){
				xtw.writeStartElement("References");
				conditionalXSLSubtree(xslNames, selectFile,"References",xtw);
				xtw.writeEndElement();
				xtw.writeStartElement("DiskSection");
				conditionalXSLSubtree(xslNames, selectDisk,"DiskSection" ,xtw);
				xtw.writeEndElement();
			}
			if(selectNetwork.length()>0){
				xtw.writeStartElement("NetworkSection");
				conditionalXSLSubtree(xslNames, selectNetwork,"NetworkSection" ,xtw);
				xtw.writeEndElement();
			}
			xtw.writeStartElement("StartupInformation");
			//FIXME get information from ovf file (information maybe miss)
			xtw.writeEmptyElement(xslNames, "copy-of");
			xtw.writeAttribute("select", "//ovf:"+(ad.isVirtualCollection()?"VirtualSystemCollection":"VirtualSystem")+"[@ovf:id='"+ad.getID()
					+"']/../ovf:StartupSection/ovf:Item[@ovf:id='"+ad.getID()+"']");
			xtw.writeEndElement();
			xtw.writeEndElement();
		}
			xtw.writeEndElement();
			xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndDocument();
		xtw.flush();
		xtw.close();
	}

	private static void conditionalXSLSubtree(String xslNames,
			String selectFile,String tagName, XMLStreamWriter xtw) throws XMLStreamException {
		xtw.writeEmptyElement(xslNames, "copy-of");
		xtw.writeAttribute("select","/ovf:Envelope/ovf:"+tagName+"/node()"+"["+selectFile+"]" );
	}
}
