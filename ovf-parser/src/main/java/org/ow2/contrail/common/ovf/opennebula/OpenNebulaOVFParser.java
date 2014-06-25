package org.ow2.contrail.common.ovf.opennebula;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import org.ow2.contrail.common.implementation.ovf.SharedDisk;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwCpu;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwEthernetAdapter;
import org.ow2.contrail.common.implementation.ovf.virtualhardware.OVFVirtualHwMemory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * 
 * @author fdudouet
 */
public class OpenNebulaOVFParser
{
	private HashMap<String, ApplianceDescriptor> idApp;
	private static final String COMMON_VM_TEMPLATE = "NAME = \"vmNAME\" \nCPU = vmCPUpct \nVCPU = vmVCPU \nMEMORY = vmMEM\n"
			+ "OS = [ boot=\"hd\", arch=\"x86_64\" ]\n" + "vmNETWORKS" + "GRAPHICS = [\n" + "type=\"vnc\",\n" + "listen=\"localhost\"\n" + "]\n";
	ApplicationDescriptor app;
	String version;
	String adminname;
	Collection<SharedDisk> sd;
	public OpenNebulaOVFParser(String ovf, String version, String adminname) throws URISyntaxException, ParserConfigurationException, FileNotFoundException, SAXException, IOException,
			XPathExpressionException, NumberFormatException, DOMException, MalformedOVFException
	{
		this.version = version;
		this.adminname = adminname;
		try
		{
			app = OVFParser.ParseOVF(ovf);
		}
		catch (Exception e)
		{
                    throw new MalformedOVFException(e.getMessage());
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
		sd = p.getAllSharedDisks();
	}
        
        /**
         * Returns a networks array for a specific VS
         * @param id the VS id
         * @return ArrayList of HashMap, each HM describing a network, having k:v name:NetworkName, type:public|private, nics:ArrayList of String(each being either dhcp or an ip)
         */
        public ArrayList<HashMap> getNetworks(String id)
        {
            ArrayList<HashMap> nw = new ArrayList<HashMap>();
            ApplianceDescriptor a = idApp.get(id);
            Collection<OVFVirtualNetwork> ns = a.getAssociatedVirtualNetworks();
            for (OVFVirtualNetwork v : ns)
            {
                if (v != null)
                {
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    hm.put("name", v.getName());
                    hm.put("type", v.getType());
                    ArrayList<String> nics = new ArrayList<String>();
                    for(OVFVirtualHwEthernetAdapter veth : v.getAssociatedNics())
                    {
                        nics.add(veth.getAddress());
                    }
                    if(!nics.isEmpty())
                    {
                        hm.put("nics", nics);
                        //only add network to final returned collection if there are any registered nics on it
                        nw.add(hm);
                    }
                }
            }
            return nw;
        }
        
        
        
        public String getNetworksString(String id)
        {
            String s = "";
            ArrayList<HashMap> a = getNetworks(id);
            for(HashMap m : a)
            {
                s+="name:"+m.get("name")+"\n";
                s+="type:"+m.get("type")+"\n";
                s+="addresses:";
                for(String l : (ArrayList<String>)m.get("nics"))
                {
                    s+=l+", ";
                }
                s+="\n";
                s+="-------------------\n";
            }
            return s;
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
				if (this.version.startsWith("2.2"))
					vmNetwork += "NIC = [NETWORK=\"" + v.getName() + "\"]\n";
				else if (this.version.startsWith("3.4")) 
					vmNetwork += "NIC = [NETWORK=\"" + v.getName() + "\",\nNETWORK_UNAME=\"" + adminname + "\"]\n";
                            }
                    }

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
                                    vmVCPU = String.valueOf(((OVFVirtualHwCpu) vh).getVirtualQuantity());
                            else if (vh.getResourceType() == 4)
                                    vmMEM = String.valueOf(((OVFVirtualHwMemory) vh).getVirtualQuantity());
                    }
                    template = COMMON_VM_TEMPLATE.replaceAll("vmNAME", vmName).replaceAll("vmCPUpct", vmCPUpct).replaceAll("vmVCPU", vmVCPU)
                                    .replaceAll("vmMEM", vmMEM).replaceAll("vmNETWORKS", vmNetwork);

                } catch (Exception e) {
                    System.err.println("Parsing of the OVF failed at NoDiskVMTemplate");
                   
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
			if (this.version.startsWith("3.4"))
				vmDisks += "DISK = [IMAGE=\"" + d.getFile().getId() + "\",\nIMAGE_UNAME=\"" + this.adminname + "\"]\n";
			else if (this.version.startsWith("2.2")) 
				vmDisks += "DISK = [IMAGE=\"" + d.getFile().getId() + "\"]\n";
		}
		template += vmDisks;
                
                //retrieve network names 
                 Collection<OVFVirtualSystem> vss = a.getVirtualSystems();
                OVFVirtualSystem vs = vss.iterator().next();
                Collection<OVFVirtualHardware> vhs = vs.getRequiredHardware();
                ArrayList<String> networksList = new ArrayList<String>();
                for (OVFVirtualHardware vh : vhs)
                {
                        if (vh.getResourceType() == 10)
                                networksList.add(String.valueOf(((OVFVirtualHwEthernetAdapter) vh).getConnection()));
                }
                
                //get context
                Collection<OVFProperty> pColl = a.getAllProperties();
                boolean emptyContext = true;
		String contextTemplate = "CONTEXT=[\n";
		for (OVFProperty p : pColl) {
			if (p.getKey().equalsIgnoreCase("VNCAllowedIp"))
				template = template.replaceAll("vmVNCAllowedIp", p.getValue());
			else if (!p.getKey().equalsIgnoreCase("ip"))
			{
				contextTemplate = contextTemplate + p.getKey() + "=\"" + p.getValue() + "\",\n";
				emptyContext = false;
			}
		}
		contextTemplate = contextTemplate.substring(0, contextTemplate.length() - 2);
		contextTemplate = contextTemplate + "\n]";
		if (!emptyContext)
			template = template + contextTemplate;
		if (template.contains("vmVNCAllowedIp"))
			template = template.replaceAll("vmVNCAllowedIp", "localhost");
            } 
            catch (Exception e) {
                System.err.println("Parsing of the OVF failed at VMTemplate");
                
            }
	return template;
	}
        
        /**
         * Get the hardware specs of a VirtualSystem (cpu/ram/cores)
         * @param id a Virtual System id
         * @return Map("rcores":valCores,"ram":valRam,"vcores":valCores)
         */
        public HashMap<String, Integer> getVSSpecs(String id)
        {
            HashMap<String, Integer> result = new HashMap<String, Integer>();
            ApplianceDescriptor a = idApp.get(id);
            OVFVirtualSystem vs = a.getVirtualSystems().iterator().next();
            Collection<OVFVirtualHardware> vhs = vs.getRequiredHardware();
            int vmVCPU = 0;
            int vmMEM = 0;
            for (OVFVirtualHardware vh : vhs)
            {
                if (vh.getResourceType() == 3)
                    vmVCPU = (int)((OVFVirtualHwCpu) vh).getVirtualQuantity();
                else if (vh.getResourceType() == 4)
                    vmMEM = (int)((OVFVirtualHwMemory) vh).getVirtualQuantity();
            }
            
            result.put("ram", vmMEM);
            result.put("vcores", vmVCPU);
            result.put("rcores", vmVCPU);
            return result;
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

        /**
         * Gets the context data of a VirtualSystem
         * @param id the id of the Virtual System
         * @return A String containing the properties as a list of key="value" separated by commas
         */
        public String getContext(String id)
	{
                String context = "";
		ApplianceDescriptor a = idApp.get(id);

		// only a single prop is here
		for (OVFProperty p : a.getAllProperties())
		{
			if (!p.getKey().startsWith("ip@") && !p.getKey().equals("VNCAllowedIp") && !p.getKey().equals("files"))
				context += p.getKey() + "=\"" + p.getValue() + "\",\n";
		}
		return context;
	}
        
        /**
         * Gets the files' list from a Virtual System
         * @param id the id of the Virtual System
         * @return A String[] containing the path of the files to be uploaded to the VM
         */
        public String[] getFiles(String id)
	{
                String[] files = null;
		ApplianceDescriptor a = idApp.get(id);

		// only a single prop is here
		for (OVFProperty p : a.getAllProperties())
		{
			if (p.getKey().equals("files"))
                        {
                            files = p.getValue().split(" "); 
                            // Will result in a wrong return if one file has a space in its path but ONE template seems to only use spaces as separators
                        }
		}
		return files;
	}
        
    /**
     * get all the shared disks objects where VEP needs to create the volume 
     * retrieves all disks at the same time, returns only those where a CREATE action is needed
     */
//    public ArrayList<SharedDisk> getToCreateSharedDisks()
//    {
//    	
//    	ArrayList<SharedDisk> toCreate = new ArrayList<SharedDisk>();
//    	
//    	for(SharedDisk s : sd)
//    	{
//    		if(s.getFile() == null)//we need to create a volume
//    		{
//    			toCreate.add(s);
//    		}
//    	}
//    	return toCreate;
//    }
        
    /**
     * Get all shared disks linked to a specific VS, even those referring to an already existent volume. 
     * @param id the id of the appliance
     */
    public Collection<SharedDisk> getSharedDisks(String id)
    {
    	ApplianceDescriptor a = idApp.get(id);
    	return a.getSharedDisks();
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

	public String getImageTemplate(String vmId, String diskId, String name)
	{
		ApplianceDescriptor a = idApp.get(vmId);
		Collection<Disk> ds = a.getDisks();
		for (Disk d : ds)
		{
			if (d.getFile().getId().equals(diskId))
			{
				String template = "NAME=\"" + name + "@" + diskId + "\"\nPATH=" + d.getFile().getUri();
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
		ArrayList<String> vmDisks = new ArrayList<String>();
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
		FileReader fr = new FileReader("/home/fdudouet/Documents/FilipposOVF.xml");

		BufferedReader br = new BufferedReader(fr);
		StringBuffer sb = new StringBuffer();
		while (br.ready())
		{
			sb.append(br.readLine());
		}
		String ovfv = sb.toString();
		OpenNebulaOVFParser onp = new OpenNebulaOVFParser(sb.toString(), "3.6", "oneadmin");
//		 String ovfv = "";
//		System.out.println(onp.getIDs());
//		System.out.println(onp.getCount());
//		System.out.println(onp.getApplicationName());
//		System.out.println(onp.getVMTemplate("ubu1"));
//        System.out.println(onp.getVSSpecs("ubu1").toString());
//        Collection<SharedDisk> a = onp.getSharedDisks("ubu1");
//        for(SharedDisk s : a){
//        	System.out.println((s.getFile() == null ? "new volume --- " : "existing volume --- ")+"name: "+s.getId()+" --- temporary: "+s.isTemporary() +" --- persistent: "+s.isPersistent());
//        }
//                System.out.println(onp.getNetworksString("ubu1"));
//                System.out.println(onp.getNetworksString("ubu2"));
//		System.out.println(onp.getVMDisksId("ubu1"));
//		System.out.println(onp.getImageTemplate("ubu1", "ubuServer","user1"));
		//System.out.println(onp.getImageDiskPath("ubu1", "dsl-1a"));
//		System.out.println(onp.getPropertiesTemplate("ubu1"));

	}

}

