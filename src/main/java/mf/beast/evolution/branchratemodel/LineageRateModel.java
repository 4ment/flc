package mf.beast.evolution.branchratemodel;

import java.util.HashSet;
import java.util.Set;

import beast.base.core.Description;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.tree.Node;

public interface LineageRateModel extends BranchRateModel {

    public void initializeNodeAssignment(Set<Node> nodes);

    public Set<Node> getNodes();

    @Description(value = "Base implementation of a clock model.", isInheritable = false)
    public abstract class Base extends beast.base.spec.evolution.branchratemodel.Base implements LineageRateModel {

        protected Set<Node> nodes = new HashSet<Node>();

        public Set<Node> getNodes() {
            return nodes;
        }
    }
}
