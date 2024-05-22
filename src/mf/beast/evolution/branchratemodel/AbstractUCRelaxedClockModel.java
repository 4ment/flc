package mf.beast.evolution.branchratemodel;

import java.util.Arrays;

import beast.base.core.*;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.inference.util.InputUtil;
import beast.base.util.Randomizer;

import org.apache.commons.math.MathException;

/**
 * @author Alexei Drummond
 */

@Description("Defines an uncorrelated relaxed molecular clock.")
@Citation(value = "Drummond AJ, Ho SYW, Phillips MJ, Rambaut A (2006) Relaxed Phylogenetics and\n"
        + "  Dating with Confidence. PLoS Biol 4(5): e88", DOI = "10.1371/journal.pbio.0040088", year = 2006, firstAuthorSurname = "drummond")
public abstract class AbstractUCRelaxedClockModel extends BranchRateModel.Base {

    public Input<ParametricDistribution> rateDistInput = new Input<ParametricDistribution>("distr",
            "the distribution governing the rates among branches. Must have mean of 1. The clock.rate parameter can be used to change the mean rate.",
            Input.Validate.REQUIRED);
    public Input<IntegerParameter> categoryInput = new Input<IntegerParameter>("rateCategories",
            "the rate categories associated with nodes in the tree for sampling of individual rates among branches.",
            Input.Validate.REQUIRED);

    public Input<Integer> numberOfDiscreteRates = new Input<Integer>("numberOfDiscreteRates",
            "the number of discrete rate categories to approximate the rate distribution by. A value <= 0 will cause the number of categories to be set equal to the number of branches in the tree. (default = -1)",
            -1);

    public Input<RealParameter> quantileInput = new Input<RealParameter>("rateQuantiles",
            "the rate quantiles associated with nodes in the tree for sampling of individual rates among branches.",
            Input.Validate.XOR, categoryInput);

    public Input<Tree> treeInput = new Input<Tree>("tree", "the tree this relaxed clock is associated with.",
            Input.Validate.REQUIRED);
    public Input<Boolean> normalizeInput = new Input<Boolean>("normalize",
            "Whether to normalize the average rate (default false).", false);

    private Function meanRate;

    int LATTICE_SIZE_FOR_DISCRETIZED_RATES = 100;

    // true if quantiles are used, false if discrete rate categories are used.
    boolean usingQuantiles;

    private int branchCount;

    protected abstract int getCategoryIndex(int nodeNumber);

    protected abstract int getAssignedBranchCount();

    @Override
    public void initAndValidate() {

        tree = treeInput.get();
        branchCount = tree.getNodeCount() - 1;

        categories = categoryInput.get();
        usingQuantiles = (categories == null);

        int assignedBranchCount = getAssignedBranchCount();

        if (!usingQuantiles) {
            LATTICE_SIZE_FOR_DISCRETIZED_RATES = numberOfDiscreteRates.get();
            if (LATTICE_SIZE_FOR_DISCRETIZED_RATES <= 0)
                LATTICE_SIZE_FOR_DISCRETIZED_RATES = assignedBranchCount;
            Log.info.println(
                    "  AbstractUCRelaxedClockModel: " + this.ID + " using " + LATTICE_SIZE_FOR_DISCRETIZED_RATES
                            + " rate " + "categories to approximate rate distribution across branches.");
        } else {
            if (numberOfDiscreteRates.get() != -1) {
                throw new RuntimeException("Can't specify both numberOfDiscreteRates and rateQuantiles inputs.");
            }
            Log.info.println("  AbstractUCRelaxedClockModel " + this.ID
                    + " using quantiles for rate distribution across branches.");
        }

        if (usingQuantiles) {
            quantiles = quantileInput.get();
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

        distribution = rateDistInput.get();

        if (!usingQuantiles) {
            // rates are initially zero and are computed by getRawRate(int i) as needed
            rates = new double[LATTICE_SIZE_FOR_DISCRETIZED_RATES];
            storedRates = new double[LATTICE_SIZE_FOR_DISCRETIZED_RATES];
        }
        normalize = normalizeInput.get();

        meanRate = meanRateInput.get();
        if (meanRate == null) {
            meanRate = new RealParameter("1.0");
        }

        try {
            double mean = rateDistInput.get().getMean();
            if (Math.abs(mean - 1.0) > 1e-6) {
                Log.warning.println("WARNING: mean of distribution for relaxed clock model is not 1.0.");
            }
        } catch (RuntimeException e) {
            // ignore
        }
    }

    public double getRateForBranch(Node node) {
        if (node.isRoot()) {
            // root has no rate
            return 1;
        }

        if (recompute) {
            // this must be synchronized to avoid being called simultaneously by
            // two different likelihood threads
            synchronized (this) {
                prepare();
                recompute = false;
            }
        }

        if (renormalize) {
            if (normalize) {
                synchronized (this) {
                    computeFactor();
                }
            }
            renormalize = false;
        }

        return getRawRate(node) * scaleFactor * meanRate.getArrayValue();
    }

    /**
     * Computes a scale factor for normalization. Only called if normalize=true.
     */
    private void computeFactor() {

        // scale mean rate to 1.0 or separate parameter

        double treeRate = 0.0;
        double treeTime = 0.0;

        if (!usingQuantiles) {
            for (int i = 0; i < tree.getNodeCount(); i++) {
                Node node = tree.getNode(i);
                if (!node.isRoot()) {
                    treeRate += getRawRateForCategory(node) * node.getLength();
                    treeTime += node.getLength();
                }
            }
        } else {
            for (int i = 0; i < tree.getNodeCount(); i++) {
                Node node = tree.getNode(i);
                if (!node.isRoot()) {
                    treeRate += getRawRateForQuantile(node) * node.getLength();
                    treeTime += node.getLength();
                }
            }
        }

        scaleFactor = 1.0 / (treeRate / treeTime);
    }

    private double getRawRate(Node node) {
        if (usingQuantiles) {
            return getRawRateForQuantile(node);
        }
        return getRawRateForCategory(node);
    }

    /**
     * @param node the node to get the rate of
     * @return the rate of the branch
     */
    private double getRawRateForCategory(Node node) {

        int nodeNumber = node.getNr();
        if (nodeNumber == branchCount) {
            // root node has nr less than #categories, so use that nr
            nodeNumber = node.getTree().getRoot().getNr();
        }

        int category = categories.getValue(getCategoryIndex(nodeNumber));

        if (rates[category] == 0.0) {
            try {
                rates[category] = distribution.inverseCumulativeProbability((category + 0.5) / rates.length);
            } catch (MathException e) {
                throw new RuntimeException("Failed to compute inverse cumulative probability!");
            }
        }
        return rates[category];
    }

    private double getRawRateForQuantile(Node node) {

        int nodeNumber = node.getNr();
        if (nodeNumber == branchCount) {
            // root node has nr less than #categories, so use that nr
            nodeNumber = node.getTree().getRoot().getNr();
        }

        try {
            return distribution.inverseCumulativeProbability(quantiles.getValue(getCategoryIndex(nodeNumber)));
        } catch (MathException e) {
            throw new RuntimeException("Failed to compute inverse cumulative probability!");
        }
    }

    private void prepare() {

        categories = categoryInput.get();

        usingQuantiles = (categories == null);

        distribution = rateDistInput.get();

        tree = treeInput.get();

        if (!usingQuantiles) {
            // rates array initialized to correct length in initAndValidate
            // here we just reset rates to zero and they are computed by getRawRate(int i)
            // as needed
            Arrays.fill(rates, 0.0);
        }
    }

    @Override
    protected boolean requiresRecalculation() {
        recompute = false;
        renormalize = true;

        if (rateDistInput.get().isDirtyCalculation()) {
            recompute = true;
            return true;
        }
        // NOT processed as trait on the tree, so DO mark as dirty
        if (categoryInput.get() != null && categoryInput.get().somethingIsDirty()) {
            // recompute = true;
            return true;
        }

        if (quantileInput.get() != null && quantileInput.get().somethingIsDirty()) {
            return true;
        }

        if (InputUtil.isDirty(meanRateInput)) {
            return true;
        }

        return recompute;
    }

    @Override
    public void store() {
        if (!usingQuantiles)
            System.arraycopy(rates, 0, storedRates, 0, rates.length);

        storedScaleFactor = scaleFactor;
        super.store();
    }

    @Override
    public void restore() {
        if (!usingQuantiles) {
            double[] tmp = rates;
            rates = storedRates;
            storedRates = tmp;
        }
        scaleFactor = storedScaleFactor;
        super.restore();
    }

    ParametricDistribution distribution;
    IntegerParameter categories;
    RealParameter quantiles;
    Tree tree;

    private boolean normalize = false;
    private boolean recompute = true;
    private boolean renormalize = true;

    protected double[] rates;
    protected double[] storedRates;
    private double scaleFactor = 1.0;
    private double storedScaleFactor = 1.0;

}