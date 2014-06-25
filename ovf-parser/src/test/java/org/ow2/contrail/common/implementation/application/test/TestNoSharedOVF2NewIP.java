package org.ow2.contrail.common.implementation.application.test;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.contrail.common.ParserManager;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.File;

public class TestNoSharedOVF2NewIP 
{

	Collection<String> appliances;
	ParserManager pm;
	final String ovfINRIA_O = "sampleWithXtreemFS_original.ovf";
	final String ovfINRIA = "sampleWithXtreemFS.ovf";
	final String ovfINRIA_S = "new/sampleWithXtreemFSandSharedDisk.ovf";
	//final String ovfINRIA_S = "src/test/resources/new/ovf-2vms-vep2.0_shared.ovf";
	
	@Before
	public void setUp() throws Exception 
	{
		pm = new ParserManager(ovfINRIA_S);
		// ApplicationDescriptor ad = pm.getApplication();
		appliances = pm.getAppliances();
	}

	@Test
	public void test() 
	{
		final int expectedAppliances = 3;
		int size = appliances.size();
		assertEquals(size,expectedAppliances);
	}
	
	@Test
	public void testDisk()
	{
		Iterator<String> i = appliances.iterator();
		
		//virtualSystemColection
		Collection<Disk> disks = pm.getApplianceDisk(i.next());
		Iterator<Disk> id = disks.iterator();
		assertEquals(id.next().getId(), "test-disk1");
		assertEquals(id.next().getId(), "test-disk2");
	
		//virtualSystem1
		disks = pm.getApplianceDisk(i.next());
		id = disks.iterator();
		assertEquals(id.next().getId(), "test-disk1");
		
		//virtualSystem2
		disks = pm.getApplianceDisk(i.next());
		id = disks.iterator();
		assertEquals(id.next().getId(), "test-disk2");
	}
	
	@Test
	public void testImage() 
	{
		Iterator<String> i = appliances.iterator();
		
		//virtualSystemColection
		Collection<File> files = pm.getApplianceImages(i.next());
		Iterator<File> id = files.iterator();
		assertEquals(id.next().getId(), "ttylinux-1");
		assertEquals(id.next().getId(), "ttylinux-2");
		
		files = pm.getApplianceImages(i.next());
		id = files.iterator();
		assertEquals(id.next().getId(), "ttylinux-1");
		
		files = pm.getApplianceImages(i.next());
		id = files.iterator();
		assertEquals(id.next().getId(), "ttylinux-2");
	}
	

	// @Test
	public void testNet() {
		Iterator<String> i = appliances.iterator();
		
		//virtualSystemColection
		Collection<String> nets = pm.getApplianceNetworks(i.next());
		
		/*
		System.out.println(nets.size());
		for (String s: nets)
			System.out.println(s);
		*/
		
		Iterator<String> id = nets.iterator();

		String net = id.next();
		// System.out.println("id"+net);
		assertTrue(net.equals("VEP private network"));
		
		//virtualSystem1
		nets = pm.getApplianceNetworks(i.next());
		id = nets.iterator();
		assertTrue(id.next().equals("VEP private network"));
		
	}

}
