package org.ow2.contrail.common.implementation.graph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.jgrapht.Graph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.util.TypeUtil;
import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;


@SuppressWarnings("serial")
public class GenericGraph<V, E> extends Multigraph<V, E> {
	public String name;

	public GenericGraph(Class<? extends E> arg0, String name) {
		super(arg0);
		this.name = name;
	}

	private Collection<ApplianceDescriptor> appliances;

	public Collection<ApplianceDescriptor> getAppliances() {
		return this.appliances;
	}

	public void setAppliances(Collection<ApplianceDescriptor> appliances) {
		this.appliances = appliances;
	}

	public void addAppliance(ApplianceDescriptor appliance) {
		if (this.appliances == null) {
			this.appliances = new ArrayList<ApplianceDescriptor>();
		}
		this.appliances.add(appliance);
	}

	public String getName() {
		return this.name;
	}
	
	public void export(String filename){
		VertexNameProvider<String> vertID = new StringNameProvider<String>();
		VertexNameProvider<String> vertName = new StringNameProvider<String>();
		EdgeNameProvider<NetworkEdge> edgeName = new StringEdgeNameProvider<NetworkEdge>();
		DOTExporter t = new DOTExporter(vertID,vertName,edgeName);
		try {
			t.export(new FileWriter(filename), this );

		} catch (IOException e) {
			System.out.println("Eccezione");
			e.printStackTrace();
		}
	}
	
	public String toString(){
		return this.name;
	}
}
