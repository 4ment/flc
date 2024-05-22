package mf.beast.evolution.branchratemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.Node;

@Description("Defines an uncorrelated relaxed molecular clock for multiple monophyletic clades.")
public class UCRelaxedMultiCladeClockModel extends AbstractUCRelaxedClockModel implements CladeRateModel {

    public Input<List<Clade>> cladeInputs = new Input<List<Clade>>("clade", "list of clades", new ArrayList<>());

    private int branchCladeCount; // number of branches included in the clade list
    private List<Clade> clades;
    private int[] map;

    @Override
    public void initAndValidate() {
        tree = treeInput.get();
        map = new int[tree.getNodeCount()];
        Arrays.fill(map, -1);

        clades = cladeInputs.get();

        ArrayList<Node> mrcas = new ArrayList<>(clades.size());
        for (Clade clade : clades) {
            setUpMRCAs(clade.getTaxonSet(), tree.getRoot(), new HashSet<String>(), mrcas);
        }

        branchCladeCount = 0;
        Iterator<Clade> cladeInterator = clades.iterator();
        for (Node mrca : mrcas) {
            Clade clade = cladeInterator.next();
            if (!clade.includeStem())
                branchCladeCount--;
            setUpMap(mrca);
        }

        super.initAndValidate();
    }

    private void setUpMap(Node node) {
        map[node.getNr()] = branchCladeCount++;
        for (int i = 0; i < node.getChildCount(); i++) {
            setUpMap(node.getChild(i));
        }
    }

    private void setUpMRCAs(TaxonSet taxonSet, Node node, Set<String> descendants, List<Node> mrcas) {
        if (node.isLeaf()) {
            descendants.add(node.getID());

            // clade contains only one leaf
            if (taxonSet.getTaxaNames().equals(descendants)) {
                mrcas.add(node);
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                Set<String> set = new HashSet<String>();
                setUpMRCAs(taxonSet, node.getChild(i), set, mrcas);
                descendants.addAll(set);
            }
            // node is the MRCA of the current taxon set
            if (taxonSet.getTaxaNames().equals(descendants)) {
                mrcas.add(node);
            }
        }
    }

    ////////////////////////////////////////////////////////////
    // Implement abstract methods of AbstractUCRelaxedClockModel
    ////////////////////////////////////////////////////////////

    protected int getCategoryIndex(int nodeNumber) {
        return map[nodeNumber];
    }

    protected int getAssignedBranchCount() {
        return branchCladeCount;
    }

    //////////////////////////////////////
    // Implements CladeRateModel interface
    //////////////////////////////////////

    public int getTaxonSetCount() {
        return clades.size();
    }

    public TaxonSet getTaxonSet(int index) {
        return clades.get(index).getTaxonSet();
    }

    public boolean includeStem(int index) {
        return clades.get(index).includeStem();
    }
}
