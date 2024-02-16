package mf.beast.evolution.branchratemodel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.Node;


public class UCRelaxedCladeClockModel extends AbstractUCRelaxedClockModel implements CladeRateModel {

	public Input<TaxonSet> taxonSetInput = new Input<TaxonSet>("taxonset","list of taxa",Validate.REQUIRED);
	public Input<Boolean> includeStemInput = new Input<Boolean>("includeStem","include stem",false);
    
	private int branchCladeCount; // number of branches included in the clade 
	private TaxonSet taxonSet;
	private int[] map;

	@Override
	public void initAndValidate() {
		
		map = new int[treeInput.get().getNodeCount()];
		Arrays.fill(map, -1);
		
		taxonSet = taxonSetInput.get();
		Node mrca = findMRCA(taxonSet, treeInput.get().getRoot(), new HashSet<String>());
		branchCladeCount = 0;
		setUpMap(mrca);
		if(!includeStemInput.get()) branchCladeCount--;
		
		super.initAndValidate();
	}

	private void setUpMap(Node node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			setUpMap(node.getChild(i));
		}
		map[node.getNr()] = branchCladeCount;
		branchCladeCount++;
	}
	
	private Node findMRCA(TaxonSet taxonSet, Node node, Set<String> descendants) {
		if (node.isLeaf()) {
			descendants.add(node.getID());
			
			if (taxonSet.getTaxaNames().equals(descendants)) {
				return node;
			}
		}
		else {
			for (int i = 0; i < node.getChildCount(); i++) {
				Set<String> set = new HashSet<String>();
				Node mrca =  findMRCA(taxonSet, node.getChild(i), set);
				if(mrca!=null) return mrca;
				descendants.addAll(set);
			}
			
			if (taxonSet.getTaxaNames().equals(descendants)) {
				return node;
			}
		}
		return null;
	}
	
	////////////////////////////////////////////////////////////
	// Implement abstract methods of AbstractUCRelaxedClockModel
	////////////////////////////////////////////////////////////
	
	protected int getCategoryIndex(int nodeNumber){
		return map[nodeNumber];
	}
	
    protected int getAssignedBranchCount(){
    	return branchCladeCount;
    }
    
	//////////////////////////////////////
    // Implement CladeRateModel interface
	//////////////////////////////////////
    
    public int getTaxonSetCount(){
    	return 1;
    }
    
    public TaxonSet getTaxonSet(int index) {
        return taxonSet;
    }

    public boolean includeStem(int index) {
        return includeStemInput.get();
    }
}
