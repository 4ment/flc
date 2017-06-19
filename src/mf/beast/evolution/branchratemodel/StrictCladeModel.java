package mf.beast.evolution.branchratemodel;

import beast.core.parameter.RealParameter;
import beast.evolution.tree.Node;

public class StrictCladeModel extends CladeRateModel.Base {

    RealParameter muParameter;

    @Override
    public void initAndValidate() {
        muParameter = meanRateInput.get();
        if (muParameter != null) {
            muParameter.setBounds(Math.max(0.0, muParameter.getLower()), muParameter.getUpper());
            mu = muParameter.getValue();
        }
    }

    @Override
    public double getRateForBranch(Node node) {
        return mu;
    }

    @Override
    public boolean requiresRecalculation() {
        mu = muParameter.getValue();
        return true;
    }

    @Override
    protected void restore() {
        mu = muParameter.getValue();
        super.restore();
    }

    @Override
    protected void store() {
        mu = muParameter.getValue();
        super.store();
    }

    private double mu = 1.0;
}
