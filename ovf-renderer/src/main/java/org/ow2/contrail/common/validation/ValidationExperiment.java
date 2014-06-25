package org.ow2.contrail.common.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ow2.contrail.common.Test.TestOVFParser;
import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.graph.*;
import org.ow2.contrail.common.implementation.Renderer;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.w3c.dom.Document;

public class ValidationExperiment {


	public static void main(String[] args) throws Exception {	
		/**
		 * Test code to validate parsing and rendering
		 */
		String filetoParse="src/main/resources/Ovf1VS";
		int howManyFile = 100;
		ApplicationDescriptor appDesc = OVFParser.ParseOVF(URI.create(filetoParse+".ovf"));
		for(int i=0; i<howManyFile;i++){	
			System.out.println("-------------------------");
			String filename = generateOvfs(appDesc, i+1);
			ApplicationDescriptor application = OVFParser.ParseOVF(URI.create(filename+".ovf"));
			Renderer.RenderOVF(application, "", filename+"_re-rendered");
			Document ovfD  = TestOVFParser.getDOMfromOVF(filename+"_re-rendered.ovf");
			System.out.println("CHECK file: "+filename);
			boolean checkConsistency = TestOVFParser.compareDOMwithDataStructure(application,ovfD);
			if (checkConsistency) System.out.println("CHECK CONSISTENCY BETWEEN FILES: " + checkConsistency);
			else System.out.println("CHECK CONSISTENCY BETWEEN FILES: " + checkConsistency); 
		}
		System.out.println("-------------------------");
	}
	
	/**
	 * Generate OVF Files. This method take an ApplicationDescritor, to take a list of networks and disks 
	 * and a VirtualSystem definition to clone, and the number of VirtualSystems that have to defined in the ovf file.
	 * every VirtualSystem cloned is created with a random network and a random disk taken from the list retrieved in ApplicationDescriptor 
	 * @param appDesc
	 * @param vsNumber
	 * @throws Exception
	 */
	public static String generateOvfs(ApplicationDescriptor appDesc, int vsNumber) throws Exception{
		String filename="/tmp/Ovf"+vsNumber+"VS";
		File file = new File(filename+".ovf");
		if(!file.exists()){
			ApplicationDescriptor appGenerated = TestOVFParser.generateApplication(appDesc,vsNumber);
			Renderer.RenderOVF(appGenerated, "", filename);
			System.out.println(filename + " generated!");
		} else System.out.println(filename + " already exists");
		return filename;
	}

	
	

}
