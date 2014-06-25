package org.ow2.contrail.common.implementation.graph;

import org.jgrapht.graph.DefaultEdge;

@SuppressWarnings("serial")
public class NetworkEdge extends DefaultEdge{

	private String edgeName = "";
	private int weight = 1;

	public NetworkEdge(int w, String n) {
		super();
		this.weight = w;
		this.edgeName = n;		
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}

	public String getEdgeName() {
		return edgeName;
	}
	
	@Override
	public String toString(){
		return this.edgeName;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
	    }
	    if (obj == null) {
	        return false;
	    }
	    if (!(obj instanceof NetworkEdge)) {
	        return false;
	    }

	    NetworkEdge edge2 = (NetworkEdge) obj;
	    return this.edgeName.equals(edge2.edgeName);
	}

}
