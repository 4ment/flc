package mf.beast.evolution.branchratemodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.core.util.Log;
import beast.evolution.tree.Node;
import beast.util.Randomizer;

// Lets hope that the SwapOperator on rateCategories is constructed AFTER initializeNodeAssignment() is called
public class UCRelaxedLineageClockModel extends AbstractUCRelaxedClockModel implements LineageRateModel {

    private int[] map;
    private int assignedBranchCount;
    private Set<Node> nodes = new HashSet<Node>();

    @Override
    public void initAndValidate() {
    	assignedBranchCount = treeInput.get().getNodeCount()-1;
        map = new int[treeInput.get().getNodeCount()];
        super.initAndValidate();
    }

	////////////////////////////////////////////////////////////
	// Implement LineageRateModel interface
	////////////////////////////////////////////////////////////
    @Override
    public void initializeNodeAssignment(Set<Node> nodes) {
        
        if (tree.getNodeCount()-1 != nodes.size()) {
        	assignedBranchCount = nodes.size();// does not contain the root
        	this.nodes.clear();
        	this.nodes.addAll(nodes);
            
            if (!usingQuantiles) {
                LATTICE_SIZE_FOR_DISCRETIZED_RATES = numberOfDiscreteRates.get();
                if (LATTICE_SIZE_FOR_DISCRETIZED_RATES <= 0) {
                    LATTICE_SIZE_FOR_DISCRETIZED_RATES = assignedBranchCount;
                }
                Log.info.println("  UCRelaxedLineageClockModel: using " + LATTICE_SIZE_FOR_DISCRETIZED_RATES + " rate "
                        + "categories to approximate rate distribution across branches.");
            } else {
                if (numberOfDiscreteRates.get() != -1) {
                    throw new RuntimeException("Can't specify both numberOfDiscreteRates and rateQuantiles inputs.");
                }
                Log.info.println("  UCRelaxedLineageClockModel: using quantiles for rate distribution across branches.");
            }

            if (usingQuantiles) {
                quantiles.setDimension(assignedBranchCount);
                Double[] initialQuantiles = new Double[assignedBranchCount];
                for (int i = 0; i < assignedBranchCount; i++) {
                    initialQuantiles[i] = Randomizer.nextDouble();
                }
                RealParameter other = new RealParameter(initialQuantiles);
                quantiles.assignFromWithoutID(other);
                quantiles.setLower(0.0);
                quantiles.setUpper(1.0);
            } else {
                categories.setDimension(assignedBranchCount);
                Integer[] initialCategories = new Integer[assignedBranchCount];
                for (int i = 0; i < assignedBranchCount; i++) {
                    initialCategories[i] = Randomizer.nextInt(LATTICE_SIZE_FOR_DISCRETIZED_RATES);
                }
                // set initial values of rate categories
                IntegerParameter other = new IntegerParameter(initialCategories);
                categories.assignFromWithoutID(other);
                categories.setLower(0);
                categories.setUpper(LATTICE_SIZE_FOR_DISCRETIZED_RATES - 1);
            }

            if (!usingQuantiles) {
                // rates are initially zero and are computed by getRawRate(int i) as needed
                rates = new double[LATTICE_SIZE_FOR_DISCRETIZED_RATES];
                storedRates = new double[LATTICE_SIZE_FOR_DISCRETIZED_RATES];

                //System.arraycopy(rates, 0, storedRates, 0, rates.length);
            }
        }
        
        Arrays.fill(map, -1);
        int index = 0;
        for (Node node : nodes) {
            map[node.getNr()] = index;
            index++;
        }
    }
    
    @Override
	public Set<Node> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
    
	////////////////////////////////////////////////////////////
	// Implement abstract methods of AbstractUCRelaxedClockModel
	////////////////////////////////////////////////////////////
	
	protected int getCategoryIndex(int nodeNumber){
		return map[nodeNumber];
	}
	
	protected int getAssignedBranchCount(){
		return assignedBranchCount;
	}
}
