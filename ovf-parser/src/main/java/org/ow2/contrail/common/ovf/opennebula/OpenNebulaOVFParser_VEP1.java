package org.ow2.contrail.common.ovf.opennebula;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.security.auth.login.AppConfigurationEntry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.Disk;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
import org.ow2.contrail.common.implementation.ovf.OVFProperty;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualDisk;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualHardware;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualSystem;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * 
 * @author fdudouet
 */
public class OpenNebulaOVFParser_VEP1
{
	private HashMap<String, ApplianceDescriptor> idApp;
	private static final String COMMON_VM_TEMPLATE = "NAME = \"vmNAME\" \nCPU = vmCPUpct \nVCPU = vmVCPU \nMEMORY = vmMEM\n"
			+ "OS = [ boot=\"hd\", arch=\"x86_64\" ]\n" + "vmNETWORKS" + "GRAPHICS = [\n" + "type=\"vnc\",\n" + "listen=\"localhost\"\n" + "]\n";
	ApplicationDescriptor app;

	public OpenNebulaOVFParser_VEP1(String ovf) throws URISyntaxException, ParserConfigurationException, FileNotFoundException, SAXException, IOException,
			XPathExpressionException, NumberFormatException, DOMException, MalformedOVFException
	{
		try
		{
			app = OVFParser.ParseOVF(ovf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		idApp = new HashMap<String, ApplianceDescriptor>();
		for (ApplianceDescriptor a : app.getAllAppliances())
		{
			idApp.put(a.getID(), a);
		}

		org.ow2.contrail.common.ParserManager p = new org.ow2.contrail.common.ParserManager(ovf, false);
		for (String appId : p.getAppliances())
		{

		}
	}

	public String getNoDiskVMTemplate(String id)
	{
		String template = "";
                try {
                    ApplianceDescriptor a = idApp.get(id);

                    String vmNetwork = "";
                    Collection<OVFVirtualNetwork> ns = a.getAssociatedVirtualNetworks();
                    for (OVFVirtualNetwork v : ns)
                    {
                            if (v != null)
                            {
                                    vmNetwork += "NIC = [NETWORK=\"" + v.getName() + "\"]\n";
                            }
                    }

                    // String vmDisks = "";
                    // Collection<Disk> ds = a.getDisks();
                    // for(Disk d : ds){
                    // vmDisks += "DISK = [IMAGE=\""+d.getFile().getId()+"\"]\n";
                    // }

                    String vmName = a.getID();

                    String vmCPUpct = "1";

                    String vmVCPU = "";
                    String vmMEM = "";

                    Collection<OVFVirtualSystem> vss = a.getVirtualSystems();
                    OVFVirtualSystem vs = vss.iterator().next();
                    Collection<OVFVirtualHardware> vhs = vs.getRequiredHardware();
                    for (OVFVirtualHardware vh : vhs)
                    {
                            if (vh.getResourceType() == 3)
                            {
                                    vmVCPU = String.valueOf(((OVFVirtualHwCpu) vh).getVirtualQuantity());
                            }
                            else
                                    if (vh.getResourceType() == 4)
                                    {
                                            vmMEM = String.valueOf(((OVFVirtualHwMemory) vh).getVirtualQuantity());
                                    }
                    }
                    template = COMMON_VM_TEMPLATE.replaceAll("vmNAME", vmName).replaceAll("vmCPUpct", vmCPUpct).replaceAll("vmVCPU", vmVCPU)
                                    .replaceAll("vmMEM", vmMEM).replaceAll("vmNETWORKS", vmNetwork);

                } catch (Exception e) {
                    System.err.println("Parsing of the OVF failed");
                    e.printStackTrace();
                }
		return template;
	}

	public String getVMTemplate(String id)
	{
            String template = "";
            try {
                template = getNoDiskVMTemplate(id);
		ApplianceDescriptor a = idApp.get(id);

		String vmDisks = "";
		Collection<Disk> ds = a.getDisks();
		for (Disk d : ds)
		{
			vmDisks += "DISK = [IMAGE=\"" + d.getFile().getId() + "\"]\n";
		}

		template += vmDisks;
                
                //get context
                Collection<OVFProperty> pColl = a.getAllProperties();
                //pColl > 1 means there is some other property than ip that we should retrieve
                if (pColl.size()>1){
                    template += "CONTEXT=[\n";
                    //only a single prop is here
                    for (OVFProperty p : a.getAllProperties()){
                        if(!p.getKey().equals("ip"))
                            template += p.getKey() + "=\"" + p.getValue() + "\",\n";
                    }

                    template = template.substring(0, template.length()-2);
                    template += "\n]";
                }
            } 
            catch (Exception e) {
                System.err.println("Parsing of the OVF failed");
                e.printStackTrace();
            }
		return template;
	}

	public String getPropertiesTemplate(String id)
	{
		String template = "CONTEXT=[\n";
		ApplianceDescriptor a = idApp.get(id);

		// only a single prop is here
		for (OVFProperty p : a.getAllProperties())
		{
			if (!p.getKey().equals("ip"))
				template += p.getKey() + "=\"" + p.getValue() + "\",\n";
		}
		template += "]\n";
		return template;
	}

	public String getApplicationName()
	{
		return app.getApplianceDescriptors().iterator().next().getID();
		// or
		// ArrayList<ApplianceDescriptor> as = (ArrayList)app.getAppliancesDescriptors();
		// ApplianceDescriptor a = as.get(0);
		// return a.getID();
	}

	public int getCount()
	{
		return idApp.size();
	}

	public String[] getIDs()
	{
		return idApp.keySet().toArray(new String[idApp.keySet().size()]);
	}

	public String getImageTemplate(String vmId, String diskId)
	{
		ApplianceDescriptor a = idApp.get(vmId);
		Collection<Disk> ds = a.getDisks();
		for (Disk d : ds)
		{
			if (d.getFile().getId().equals(diskId))
			{
				String template = "NAME=\"" + diskId + "\"\nPATH=" + d.getFile().getUri();
				return template;
			}
		}
		return null;
	}

	public String getImageDiskPath(String vmId, String diskId)
	{
		ApplianceDescriptor a = idApp.get(vmId);
		Collection<Disk> ds = a.getDisks();
		for (Disk d : ds)
		{
			if (d.getFile().getId().equals(diskId))
			{
				String template = d.getFile().getUri();
				return template;
			}
		}
		return null;
	}

	public String[] getVMDisksId(String vmId)
	{
		ArrayList vmDisks = new ArrayList<String>();
		ApplianceDescriptor a = idApp.get(vmId);
		Collection<Disk> ds = a.getDisks();
		for (Disk d : ds)
		{
			vmDisks.add(d.getFile().getId());
		}
		return (String[]) vmDisks.toArray(new String[vmDisks.size()]);
	}

	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, XPathExpressionException, DOMException,
			ParserConfigurationException, SAXException, IOException, URISyntaxException, MalformedOVFException
	{
		FileReader fr = new FileReader("C:/repos/ubuntu-test.ovf");
		// FileReader fr = new FileReader("src/main/resources/contrail_petstore.xml");
		// FileReader fr = new FileReader("c:/repos/test2.ovf");
		BufferedReader br = new BufferedReader(fr);
		StringBuffer sb = new StringBuffer();
		while (br.ready())
		{
			sb.append(br.readLine());
		}
		String ovfv = sb.toString();
		OpenNebulaOVFParser_VEP1 onp = new OpenNebulaOVFParser_VEP1(sb.toString());
		// String ovfv = "";
		// OpenNebulaOVFParser_VEP1 onp = new OpenNebulaOVFParser_VEP1(ovfv);
		System.out.println(onp.getIDs());
		System.out.println(onp.getCount());
		System.out.println(onp.getApplicationName());
		System.out.println(onp.getNoDiskVMTemplate("Ubuntu MySQL DB"));
		System.out.println(onp.getVMDisksId("Ubuntu MySQL DB"));
		System.out.println(onp.getImageTemplate("Ubuntu MySQL DB", "ubuntu-1110"));
		System.out.println(onp.getImageDiskPath("Ubuntu MySQL DB", "ubuntu-1110"));
		System.out.println(onp.getPropertiesTemplate("Ubuntu MySQL DB"));

	}

}
