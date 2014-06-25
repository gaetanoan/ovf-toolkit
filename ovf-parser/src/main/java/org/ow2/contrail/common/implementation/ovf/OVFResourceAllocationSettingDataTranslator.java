package org.ow2.contrail.common.implementation.ovf;

import java.util.HashMap;
import java.util.Map;

import org.ow2.contrail.common.implementation.ovf.item_processor.OVFCdDriveProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFCpuProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFDiskDriveProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFEthernetAdapterProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFFloppyDriveProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFIdeControllerProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFMemoryProcessor;
import org.ow2.contrail.common.implementation.ovf.item_processor.OVFParallelSCSIProcessor;

public class OVFResourceAllocationSettingDataTranslator {
	
	private static final Map<Integer, String> resourceTypeToName = new HashMap<Integer, String>();
	private static final Map<Integer, OVFItemProcessor> resourceTypeToProcessor = new HashMap<Integer, OVFItemProcessor>();
	
	public static String getResourceName(int i) 
	{
		if ((i>33) && (i<32768)) 
			return "DMTF reserved";
		else if (i>=32768)
			return "Vendor Reserved";
			
		// POST: i <= 33
		return resourceTypeToName.get(i);	
	}
	
	public static OVFItemProcessor getItemProcessor(int id) throws NoClassDefFoundError 
	{	
		return resourceTypeToProcessor.get(id);	
	}
	
	static
	{	
		resourceTypeToName.put(1,"Other");
		resourceTypeToName.put(2,"Computer System");
		resourceTypeToName.put(3,"Processor");
		resourceTypeToName.put(4,"Memory");
		resourceTypeToName.put(5,"IDE Controller");
		resourceTypeToName.put(6,"Parallel SCSI HBA");
		resourceTypeToName.put(7,"FC HBA");
		resourceTypeToName.put(8,"iSCSI HBA");
		resourceTypeToName.put(9,"IB HCA");
		resourceTypeToName.put(10,"Ethernet Adapter");
		resourceTypeToName.put(11,"Other Network Adapter");
		resourceTypeToName.put(12,"I/O Slot");
		resourceTypeToName.put(13,"I/O Device");
		resourceTypeToName.put(14,"Floppy Drive");
		resourceTypeToName.put(15,"CD Drive");
		resourceTypeToName.put(16,"DVD drive");
		resourceTypeToName.put(17,"Disk Drive");
		resourceTypeToName.put(18,"Tape Drive");
		resourceTypeToName.put(19,"Storage Extent");
		resourceTypeToName.put(20,"Other storage device");
		resourceTypeToName.put(21,"Serial port");
		resourceTypeToName.put(22,"Parallel port");
		resourceTypeToName.put(23,"USB Controller");
		resourceTypeToName.put(24,"Graphics controller");
		resourceTypeToName.put(25,"IEEE 1394 Controller");
		resourceTypeToName.put(26,"Partitionable Unit");
		resourceTypeToName.put(27,"Base Partitionable Unit");
		resourceTypeToName.put(28,"Power");
		resourceTypeToName.put(29,"Cooling Capacity");
		resourceTypeToName.put(30,"Ethernet Switch Port");
		resourceTypeToName.put(31,"Logical Disk");
		resourceTypeToName.put(32,"Storage Volume");
		resourceTypeToName.put(33,"Ethernet Connection");
		//	resourceTypeToName.put(DMTF reserved (..)
		//	resourceTypeToName.put(Vendor Reserved (32768-65535)	
		
		// Initialize the hash map of the OVF processors. 
		resourceTypeToProcessor.put(3, new OVFCpuProcessor());
		resourceTypeToProcessor.put(4, new OVFMemoryProcessor());
		resourceTypeToProcessor.put(5, new OVFIdeControllerProcessor());
		resourceTypeToProcessor.put(6, new OVFParallelSCSIProcessor());
		resourceTypeToProcessor.put(10, new OVFEthernetAdapterProcessor());
		resourceTypeToProcessor.put(14, new OVFFloppyDriveProcessor());
		resourceTypeToProcessor.put(15, new OVFCdDriveProcessor());
		resourceTypeToProcessor.put(17, new OVFDiskDriveProcessor());
	}
}
