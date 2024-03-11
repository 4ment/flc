package mf.beast.evolution.branchratemodel;

import java.util.HashSet;
import java.util.Set;

import beast.base.inference.CalculationNode;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.tree.Node;

public interface LineageRateModel extends BranchRateModel {

    public void initializeNodeAssignment(Set<Node> nodes);

    public void updateNodeIndex(Set<Node> nodes);

    public Set<Node> getNodes();

    int[] getMap();

    @Description(value = "Base implementation of a clock model.", isInheritable = false)
    public abstract class Base extends CalculationNode implements LineageRateModel {

        public Input<RealParameter> meanRateInput = new Input<RealParameter>("clock.rate", "mean clock rate (defaults to 1.0)");

        protected Set<Node> nodes = new HashSet<Node>();

        public Set<Node> getNodes() {
            return nodes;
        }
    }
}
