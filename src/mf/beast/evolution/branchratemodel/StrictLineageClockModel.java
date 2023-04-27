package mf.beast.evolution.branchratemodel;

import beast.base.evolution.branchratemodel.StrictClockModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import beast.base.evolution.tree.Node;

public class StrictLineageClockModel extends StrictClockModel implements LineageRateModel {
	
	private Set<Node> nodes = new HashSet<Node>();
	
    @Override
    public void initializeNodeAssignment(Set<Node> nodes) {
    	this.nodes.clear();
    	this.nodes.addAll(nodes);
    }

    @Override
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

}
