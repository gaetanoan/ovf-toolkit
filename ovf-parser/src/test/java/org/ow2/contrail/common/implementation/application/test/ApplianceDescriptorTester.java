package org.ow2.contrail.common.implementation.application.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.ovf.OVFProductSection;
import org.ow2.contrail.common.implementation.ovf.OVFStartupSection;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;


/**
 * Basic test for appliance descriptor fields
 */
public class ApplianceDescriptorTester 
{

	private static ApplianceDescriptor  ad = null;
	
	@Before
	public void setUp() throws Exception 
	{
		ad = new ApplianceDescriptor("id","uri",251,true);
	}

	@Test
	public void testGetApplianceURI() 
	{
		assertEquals("uri",ad.getApplianceURI());
	}

	@Test
	public void testGetID() 
	{
		assertEquals("id",ad.getID());
	}

	@Test
	public void testGetSize() 
	{
		assertEquals(251,ad.getSize());
	}

	@Test
	public void testGetVirtualSystems() 
	{
		assertEquals(0,ad.getVirtualSystems().size());
	}

	@Test
	public void testGetAssociatedVirtualNetworks() 
	{
		assertEquals(0,ad.getAssociatedVirtualNetworks().size());
	}

	@Test
	public void testGetAppliancesDescriptors() 
	{
		assertEquals(0,ad.getAppliancesDescriptors().size());
	}

	@Test
	public void testGetProductSections() 
	{
		assertEquals(0,ad.getProductSections().size());
	}

	@Test
	public void testAddProductSection() 
	{
		OVFProductSection ovfps=new OVFProductSection();
		ad.addProductSection(ovfps);
		assertTrue(ad.getProductSections().contains(ovfps));
	}

	@Test
	public void testGetDisks() 
	{
		assertEquals(0,ad.getDisks().size());
	}

	@Test
	public void testIsVirtualCollection() 
	{
		assertTrue(ad.isVirtualCollection());
	}

	@Test
	public void testGetOVFStartupSection() 
	{
		assertEquals(null,ad.getOVFStartupSection());
	}

	@Test
	public void testSetOVFStartupSection() 
	{
		OVFStartupSection ovfss=new OVFStartupSection();
		ad.setOVFStartupSection(ovfss);
		assertEquals(ovfss,ad.getOVFStartupSection());
	}

	@Test
	public void testGetAllProperties() 
	{
		assertEquals(0,ad.getAllProperties().size());
	}

	@Test
	public void testGetProperties() 
	{
		assertEquals(0,ad.getProperties().size());
	}

	@Test
	public void testSetParent() 
	{
		ApplianceDescriptor nad=new ApplianceDescriptor("ciccio","bucci",0,false);
		ad.setParent(nad);
		assertSame(nad,ad.getParent());
	}


	@Test
	public void testGetVirtualNetworkByName() 
	{
		ad.getAssociatedVirtualNetworks();
		assertTrue(ad.getAssociatedVirtualNetworks().isEmpty());
		OVFVirtualNetwork e=new OVFVirtualNetwork("Pinco","Pallo");
		ad.getAssociatedVirtualNetworks().add(e);
		assertSame(e, ad.getVirtualNetworkByName("Pinco"));
	}

}
