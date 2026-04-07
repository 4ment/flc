package mf.beast.evolution.branchratemodel;

import beast.base.evolution.tree.Node;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.type.RealScalar;

public class StrictCladeModel extends CladeRateModel.Base {

    RealScalar<PositiveReal> muParameter;

    @Override
    public void initAndValidate() {
        muParameter = meanRateInput.get();
        if (muParameter != null) {
            mu = muParameter.get();
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
        mu = muParameter.get();
        return true;
    }

    @Override
    protected void restore() {
        mu = muParameter.get();
        super.restore();
    }

    @Override
    protected void store() {
        mu = muParameter.get();
        super.store();
    }

    private double mu = 1.0;
}
