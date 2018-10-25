package mf.beast.evolution.branchratemodel;

import beast.core.Citation;
import beast.core.Description;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beast.core.Input;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

// TreeLikelihood contains a reference to a abstract BranchRateModel.Base object as input, not a BranchModel interface
@Description("Defines a flexible local clock model.")
@Citation(value = "Fourment M and Darling AE (2018) Local and relaxed clocks: the best of both worlds\n" +
                "  PeerJ 6:e5140", DOI = "10.7717/peerj.5140", year = 2018, firstAuthorSurname = "fourment")
public class FlexibleLocalClockModel extends BranchRateModel.Base {//CalculationNode implements BranchRateModel {

    public Input<LineageRateModel> rootRateModelInput = new Input<LineageRateModel>("rootClockModel", "the branch rate model for branchs that do not belong to a local clock.", Input.Validate.REQUIRED);
    public Input<List<CladeRateModel>> cladeRateModelInputs = new Input<List<CladeRateModel>>("cladeClockModel", "List of clades containing a rate (strict or UC relaxed clock).", new ArrayList<CladeRateModel>());
    
    public Input<Tree> treeInput = new Input<Tree>("tree", "the tree this local clock is associated with.", Input.Validate.REQUIRED);

    private LineageRateModel rootRateModel;
    private List<CladeRateModel> cladeRateModels;
    Tree tree;

    private Map<Integer, BranchRateModel> nodeClockMap = new HashMap<Integer, BranchRateModel>();

    @Override
    public void initAndValidate() {
        tree = treeInput.get();
        cladeRateModels = cladeRateModelInputs.get();
        rootRateModel = rootRateModelInput.get();

        nodeClockMap.put(tree.getRoot().getNr(), rootRateModel);
        postorderTraverse(tree.getRoot());
        preorderTraverse(tree.getRoot());

        Set<Node> nodes = new HashSet<Node>();
        for (Integer nodeNr : nodeClockMap.keySet()) {
            if (nodeClockMap.get(nodeNr).equals(rootRateModel) && nodeNr != tree.getRoot().getNr()) {
                Node node = tree.getNode(nodeNr);
                nodes.add(node);
            }
        }

        rootRateModel.initializeNodeAssignment(nodes);
    }

    @Override
    public double getRateForBranch(Node node) {
        if (node.isRoot()) {
            return 1;
        }

        BranchRateModel clock = nodeClockMap.get(node.getNr());
        return clock.getRateForBranch(node);
    }

    // Set up nodeClockMap for every MRCA node of each cladeRateModel
    // Other nodes are set up in preorderTraverse
    private Set<String> postorderTraverse(Node node) {
        Set<String> descendants = new HashSet<String>();

        if (node.isLeaf()) {
            descendants.add(node.getID());

            // Check if a cladeRateModel is assigned to this leaf only
            // includeStem is not checked since it is assumed to be true (otherwise the cladeRateModel is useless)
            for (CladeRateModel rateModel : cladeRateModels) {
            	int i = 0;
            	for( ; i < rateModel.getTaxonSetCount(); i++){
	                if (rateModel.getTaxonSet(i).getTaxaNames().equals(descendants)) {
	                    nodeClockMap.put(node.getNr(), rateModel);
	                    break;
	                }
            	}
            	if(i != rateModel.getTaxonSetCount()){
            		break;
            	}
            }
        }
        else {

            for (int i = 0; i < node.getChildCount(); i++) {
                Set<String> childSet = postorderTraverse(node.getChild(i));
                descendants.addAll(childSet);
            }

            if (!node.isRoot()) {
                for (CladeRateModel rateModel : cladeRateModels) {
                	int i = 0;
                	for( ; i < rateModel.getTaxonSetCount(); i++){
	                    if (rateModel.getTaxonSet(i).getTaxaNames().equals(descendants)) {
	                        // Here starts a new clock
	                        if (rateModel.includeStem(i)) {
	                            nodeClockMap.put(node.getNr(), rateModel);
	                        }
	                        // node will inherits its clock AND its children get the same new clock
	                        else {
	                            for (int j = 0; j < node.getChildCount(); j++) {
	                                nodeClockMap.put(node.getChild(j).getNr(), rateModel);
	                            }
	                        }
	                        break;
	                    }
                	}
                	if(i != rateModel.getTaxonSetCount()){
                		break;
                	}
                }
            }
        }
        return descendants;
    }

    // Set up nodeClockMap for other nodes by inheriting the parent rate model
    private void preorderTraverse(Node node) {

        if (!nodeClockMap.containsKey(node.getNr())) {
            nodeClockMap.put(node.getNr(), nodeClockMap.get(node.getParent().getNr()));
        }
        if (!node.isLeaf()) {
            for (int i = 0; i < node.getChildCount(); i++) {
                preorderTraverse(node.getChild(i));
            }
        }
    }

    public Map<Integer, BranchRateModel> getClockMap(){
        return nodeClockMap;
    }

    // Number of local clock + ancestral clock
    public int getNumberOfClocks(){
        return cladeRateModels.size()+1;
    }

    public List<CladeRateModel> getCladeRateModels(){
        return cladeRateModels;
    }

    public LineageRateModel getrootRateModel(){
        return rootRateModel;
    }
}
