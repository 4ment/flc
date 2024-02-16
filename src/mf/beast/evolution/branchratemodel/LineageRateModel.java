package mf.beast.evolution.branchratemodel;

import java.util.HashSet;
import java.util.Set;

import beast.core.CalculationNode;
import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.tree.Node;

public interface LineageRateModel extends BranchRateModel {

    public void initializeNodeAssignment(Set<Node> nodes);

    public Set<Node> getNodes();

    @Description(value = "Base implementation of a clock model.", isInheritable = false)
    public abstract class Base extends CalculationNode implements LineageRateModel {

        public Input<RealParameter> meanRateInput = new Input<RealParameter>("clock.rate", "mean clock rate (defaults to 1.0)");

        protected Set<Node> nodes = new HashSet<Node>();

        public Set<Node> getNodes() {
            return nodes;
        }
    }
}
