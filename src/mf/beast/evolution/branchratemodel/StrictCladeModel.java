package mf.beast.evolution.branchratemodel;

import beast.base.core.Function;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.tree.Node;

public class StrictCladeModel extends CladeRateModel.Base {

    Function muParameter;

    @Override
    public void initAndValidate() {
        muParameter = meanRateInput.get();
        if (muParameter != null) {
//            muParameter.setBounds(Math.max(0.0, muParameter.getLower()), muParameter.getUpper());
            mu = muParameter.getArrayValue();
            if (mu <= 0)
                throw new IllegalArgumentException("mu parameter must be > 0 !");
        }
    }

    @Override
    public double getRateForBranch(Node node) {
        return mu;
    }

    @Override
    public boolean requiresRecalculation() {
        mu = muParameter.getArrayValue();
        return true;
    }

    @Override
    protected void restore() {
        mu = muParameter.getArrayValue();
        super.restore();
    }

    @Override
    protected void store() {
        mu = muParameter.getArrayValue();
        super.store();
    }

    private double mu = 1.0;
}
