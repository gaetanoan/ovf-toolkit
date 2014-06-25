package org.ow2.contrail.common.implementation.graph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.contrail.common.ParserManager;
import org.ow2.contrail.common.exceptions.MalformedOVFException;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;
import org.ow2.contrail.common.implementation.application.ApplicationDescriptor;
import org.ow2.contrail.common.implementation.ovf.OVFParser;
import org.ow2.contrail.common.implementation.ovf.OVFVirtualNetwork;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class GraphFactory {

	public static GenericGraph<String, NetworkEdge> getApplicationGraph(URI ovfUri) throws Exception{
		ParserManager parser;
			try {
				parser = new ParserManager(ovfUri);
			}
			catch (Exception e){
				String string = "";
				for (StackTraceElement s : e.getStackTrace())
					string += s.toString()+ "\n";
				throw new Exception("Internal error while parsing the OVF document");
			}

			GenericGraph<String, NetworkEdge> appGraphDesc = GraphFactory.MakeGraph(-1, parser.getApplication());
			return appGraphDesc;
		}

	/**
	 * Create the physical graph from the <code>ApplicationDescriptor</code> retrieved by parsing the OVF related to the URI passed as argument
	 * 
	 * @param ovfURI
	 *            Uri of the ovf document
	 * @return An <code>ApplicationGraph</code>
	 * @throws MalformedOVFException
	 */
	private static GenericGraph<String, NetworkEdge> MakeGraph(int depth, URI ovfURI) throws NumberFormatException, FileNotFoundException,
			XPathExpressionException, DOMException, ParserConfigurationException, SAXException, IOException, URISyntaxException,
			MalformedOVFException, Exception
	{
		ApplicationDescriptor appDesc = OVFParser.ParseOVF(ovfURI);
		return MakeGraph(depth, appDesc);
	}

	/**
	 * Generate a partial physical view of the <code>ApplicationDescriptor</code> mapped on the <code>ApplicationGraphDescriptor</code> using the
	 * <code>Multigraph<String, DefaultEdge></code> of JGraphT
	 * 
	 * @param depth
	 *            Depth of visit for the logical view
	 * @param applicationDesc
	 *            An instance of the <code>ApplicationDescriptor</code>
	 * @return A <code>ApplicationgraphDescritor<String,ContrailEdge></code> representing the partial physical view requested
	 * @throws ApplicationOperationException 
	 */
	private static GenericGraph<String, NetworkEdge> MakeGraph(int depth, ApplicationDescriptor applicationDesc) throws Exception
	{
		int countDepth = 0;
		
		if (applicationDesc == null || applicationDesc.getApplianceDescriptors().size() == 0)
			throw new Exception("Impossibile to create application graph, error on input params");
		
		Collection<ApplianceDescriptor> logicalTreeAppliances = applicationDesc.getApplianceDescriptors();
		GenericGraph<String, NetworkEdge> lookupGraph = new GenericGraph<String, NetworkEdge>(NetworkEdge.class, applicationDesc.getName());

		// collection of leaf node to insert into the new graph
		Collection<ApplianceDescriptor> partialLogicalAppliancesTree = new ArrayList<ApplianceDescriptor>();
		for (ApplianceDescriptor appliance : logicalTreeAppliances)
		{
			getPartialGraph(countDepth, depth, appliance, partialLogicalAppliancesTree);
		}

		// Add the new vertex into the graph
		for (ApplianceDescriptor appliance : partialLogicalAppliancesTree)
		{
			lookupGraph.addVertex(appliance.getID());
			lookupGraph.addAppliance(appliance);
		}
		
		// check every appliance
		for (ApplianceDescriptor partialLogicalAppliance : partialLogicalAppliancesTree)
		{
			// we need to retrieve the network information
			// IMPORTANT:the vertex could be abstract so we need to retrieve
			// its network connection from its children

			// Temporary collection to save the partial ovf network
			Collection<OVFVirtualNetwork> partialNets = new ArrayList<OVFVirtualNetwork>();
			getPartialNetwork(partialLogicalAppliance, partialNets);
			for (ApplianceDescriptor otherPartialLogicalAppliance : partialLogicalAppliancesTree)
			{
				if (partialLogicalAppliance != otherPartialLogicalAppliance)
				{
					// Temporary collection to save the partial ovf network
					Collection<OVFVirtualNetwork> otherPartialNets = new ArrayList<OVFVirtualNetwork>();
					getPartialNetwork(otherPartialLogicalAppliance, otherPartialNets);
					for (OVFVirtualNetwork ovfNet : partialNets)
					{
						for (OVFVirtualNetwork otherOvfNet : otherPartialNets)
						{
							if (otherOvfNet.getName().equals(ovfNet.getName()))
							{
								if (!lookupGraph.containsEdge(partialLogicalAppliance.getID(), otherPartialLogicalAppliance.getID()))
								{
									lookupGraph.addEdge(partialLogicalAppliance.getID(), otherPartialLogicalAppliance.getID(), new NetworkEdge(1,
											ovfNet.getName()));									
								}
							}
						}
					}
				}
			}
		}
		return lookupGraph;
	}

	private static void getPartialNetwork(ApplianceDescriptor logicalAppliance, Collection<OVFVirtualNetwork> partialNets)
	{
		// Base case: if the appliance exist in physical graph it have some
		// network
		// TODO: Check if virtualSystemCollection can have network specification
		// We need a unique method to retrieve information on which machines and
		// which do not have associated networks
		// Maybe a check on "child virtualSystem" could work
		if (!logicalAppliance.isVirtualCollection())
		{
			for (OVFVirtualNetwork ovfNet : logicalAppliance.getAssociatedVirtualNetworks())
			{
				if (!partialNets.contains(ovfNet))
					partialNets.add(ovfNet);
			}
		}
		else
		{
			// Recursive case: check the appliance children and check for
			// existing networks
			for (ApplianceDescriptor applianceChild : logicalAppliance.getAppliancesDescriptors())
			{
				getPartialNetwork(applianceChild, partialNets);
			}
		}
	}
	
	// FIXME:improve the recursive method to retrieve the leaf node collection
		private static void getPartialGraph(int countDepth, int depth, ApplianceDescriptor appliance,
				Collection<ApplianceDescriptor> logicalAppliancesTree)
		{
			// Base case: reached max depth or no child
			if (((depth != -1) && (countDepth == depth)) || (appliance.getAppliancesDescriptors().size() == 0))
			{
				logicalAppliancesTree.add(appliance);
			}
			else
			{
				// Recursive case
				countDepth++;
				for (ApplianceDescriptor applianceChild : appliance.getAppliancesDescriptors())
				{
					getPartialGraph(countDepth, depth, applianceChild, logicalAppliancesTree);
				}
			}
		}
}
