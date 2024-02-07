package beast.base.evolution.branchratemodel;

import beast.base.evolution.RateStatistic;
import beast.base.evolution.tree.Node;
import beast.base.util.DiscreteStatistics;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by mathieu on 20/07/2017.
 */
public class FLCRateStatistic extends RateStatistic {

    private FlexibleLocalClockModel branchModel;
    private List<List<Double>> rates;
    private List<List<Double>> branchLengths;

    private Map<BranchRateModel,Integer> map = new HashMap<BranchRateModel,Integer>();

    @Override
    public void initAndValidate() {
        super.initAndValidate();
        branchModel = (FlexibleLocalClockModel)branchRateModelInput.get();
        int clockCount = branchModel.getNumberOfClocks();
        rates = new ArrayList<List<Double>>(clockCount);
        branchLengths = new ArrayList<List<Double>>(clockCount);
        for(int i = 0; i < clockCount; i++){
            rates.add(new ArrayList<>());
            branchLengths.add(new ArrayList<>());
        }

        int index = 0;
        for(BranchRateModel brm : branchModel.getClockMap().values()){
            if(!map.containsKey(brm)){
                map.put(brm, index);
                index++;
            }
        }
    }
    /**
     * Loggable implementation *
     */

    @Override
    public void init(final PrintStream out) {
        super.init(out);
        for(BranchRateModel brm : map.keySet()){
            BranchRateModel.Base brm2 = (BranchRateModel.Base)brm;
            String id = brm2.getID();
            if (id == null) {
                id = "";
            }
            out.print(id + ".mean\t" + id + ".variance\t" + id + ".coefficientOfVariation\t");
        }
    }


    @Override
    public void log(final long sample, final PrintStream out) {
        super.log(sample, out);

        for (int i = 0; i < rates.size(); i++){
            rates.get(i).clear();
            branchLengths.get(i).clear();
        }
        final Node[] nodes = treeInput.get().getNodesAsArray();
        for (Node node : nodes ) {
            if (!node.isRoot()) {
                final Node parent = node.getParent();
                BranchRateModel brm = branchModel.getClockMap().get(node.getNr());
                rates.get(map.get(brm)).add(brm.getRateForBranch(node));
                branchLengths.get(map.get(brm)).add(parent.getHeight() - node.getHeight());
            }
        }

        for(BranchRateModel brm : map.keySet()) {
            double totalWeightedRate = 0.0;
            double totalTreeLength = 0.0;
            int index = map.get(brm);
            final double[] ratesArray = new double[rates.get(index).size()];
            for (int i = 0; i < ratesArray.length; i++) {
                final double bl =  branchLengths.get(index).get(i);
                ratesArray[i] = rates.get(index).get(i);
                totalWeightedRate += ratesArray[i] * bl;
                totalTreeLength += bl;
            }
            double m = totalWeightedRate / totalTreeLength;

            double variance = DiscreteStatistics.variance(ratesArray);
            final double mean = DiscreteStatistics.mean(ratesArray);
            double cv = Math.sqrt(DiscreteStatistics.variance(ratesArray, mean)) / mean;
            out.print(m + "\t" + variance + "\t" + cv + "\t");
        }
    }

}
