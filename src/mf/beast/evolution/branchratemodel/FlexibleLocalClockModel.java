package mf.beast.evolution.branchratemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beast.base.core.Citation;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;

@Description("Defines a flexible local clock model.")
@Citation(value = "Fourment M and Darling AE (2018) Local and relaxed clocks: the best of both worlds\n"
        + "  PeerJ 6:e5140", DOI = "10.7717/peerj.5140", year = 2018, firstAuthorSurname = "fourment")
public class FlexibleLocalClockModel extends BranchRateModel.Base {

    public Input<LineageRateModel> rootRateModelInput = new Input<LineageRateModel>("rootClockModel",
            "the branch rate model for branches that do not belong to a local clock.", Input.Validate.REQUIRED);
    public Input<List<CladeRateModel>> cladeRateModelInputs = new Input<List<CladeRateModel>>("cladeClockModel",
            "List of clades containing a rate (strict or UC relaxed clock).", new ArrayList<CladeRateModel>());

    public Input<Tree> treeInput = new Input<Tree>("tree", "the tree this local clock is associated with.",
            Input.Validate.REQUIRED);

    private LineageRateModel rootRateModel;
    private List<CladeRateModel> cladeRateModels;
    Tree tree;

    private Map<Integer, BranchRateModel> nodeClockMap = new HashMap<Integer, BranchRateModel>();

    private Map<Integer, BranchRateModel> nodeClockMapStored = new HashMap<Integer, BranchRateModel>();

    @Override
    protected boolean requiresRecalculation() {
        if (isStrictClockLineage(rootRateModel) && isStrictClockClade(cladeRateModels)) {
            // update node indices for strict clock models only
            updateNodeIndex();
        }
        return true;
    }

    @Override
    protected void store() {
        // store nodeClockMap
        nodeClockMapStored = nodeClockMap;
        super.store();
    }

    @Override
    protected void restore() {
        // restore nodeClockMap
        nodeClockMap = nodeClockMapStored;
        super.restore();
    }

    public void updateNodeIndex() {
        cladeRateModels = cladeRateModelInputs.get();
        rootRateModel = rootRateModelInput.get();

        nodeClockMap = new HashMap<Integer, BranchRateModel>();
        nodeClockMap.put(tree.getRoot().getNr(), rootRateModel);
        postorderTraverse(tree.getRoot());
        preorderTraverse(tree.getRoot());
        // node assignments
        Set<Node> nodes = new HashSet<Node>();
        for (Integer nodeNr : nodeClockMap.keySet()) {
            if (nodeClockMap.get(nodeNr).equals(rootRateModel) && nodeNr != tree.getRoot().getNr()) {
                Node node = tree.getNode(nodeNr);
                nodes.add(node);
            }
        }
        rootRateModel.initializeNodeAssignment(nodes);
    }

    private boolean isStrictClockLineage(LineageRateModel model) {
        return model instanceof StrictLineageClockModel;
    }

    private boolean isStrictClockClade(List<CladeRateModel> cladeList) {
        for (CladeRateModel model : cladeList) {
            if (!(model instanceof StrictCladeModel)) {
                return false;
            }
        }
        return true;
    }

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
            // includeStem is not checked since it is assumed to be true (otherwise the
            // cladeRateModel is useless)
            for (CladeRateModel rateModel : cladeRateModels) {
                int i = 0;
                for (; i < rateModel.getTaxonSetCount(); i++) {
                    if (rateModel.getTaxonSet(i).getTaxaNames().equals(descendants)) {
                        nodeClockMap.put(node.getNr(), rateModel);
                        break;
                    }
                }
                if (i != rateModel.getTaxonSetCount()) {
                    break;
                }
            }
        } else {

            for (int i = 0; i < node.getChildCount(); i++) {
                Set<String> childSet = postorderTraverse(node.getChild(i));
                descendants.addAll(childSet);
            }

            if (!node.isRoot()) {
                for (CladeRateModel rateModel : cladeRateModels) {
                    int i = 0;
                    for (; i < rateModel.getTaxonSetCount(); i++) {
                        if (rateModel.getTaxonSet(i).getTaxaNames().equals(descendants)) {
                            // Here starts a new clock
                            if (rateModel.includeStem(i)) {
                                nodeClockMap.put(node.getNr(), rateModel);
                            }
                            // node will inherit its clock AND its children get the same new clock
                            else {
                                for (int j = 0; j < node.getChildCount(); j++) {
                                    nodeClockMap.put(node.getChild(j).getNr(), rateModel);
                                }
                            }
                            break;
                        }
                    }
                    if (i != rateModel.getTaxonSetCount()) {
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

    public Map<Integer, BranchRateModel> getClockMap() {
        return nodeClockMap;
    }

    // Number of local clock + ancestral clock
    public int getNumberOfClocks() {
        return cladeRateModels.size() + 1;
    }

    public List<CladeRateModel> getCladeRateModels() {
        return cladeRateModels;
    }

    public LineageRateModel getRootRateModel() {
        return rootRateModel;
    }
}
