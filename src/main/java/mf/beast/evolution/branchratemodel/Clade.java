package mf.beast.evolution.branchratemodel;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.alignment.TaxonSet;

@Description("Defines a simple clade object.")
public class Clade extends BEASTObject {
    public Input<TaxonSet> taxonSetInput = new Input<TaxonSet>("taxonset", "list of taxa.");
    public Input<Boolean> includeStemInput = new Input<Boolean>("includeStem",
            "true if the stem of the clade should be included.", false);

    @Override
    public void initAndValidate() {

    }

    public final TaxonSet getTaxonSet() {
        return taxonSetInput.get();
    }

    public final boolean includeStem() {
        return includeStemInput.get();
    }
}
