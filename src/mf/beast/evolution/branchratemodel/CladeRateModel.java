package mf.beast.evolution.branchratemodel;

import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.branchratemodel.BranchRateModel;

public interface CladeRateModel extends BranchRateModel {

	public int getTaxonSetCount();
	
    public TaxonSet getTaxonSet(int index);

    public boolean includeStem(int index);

    public void updateNodeIndex();

    public abstract class Base extends BranchRateModel.Base implements CladeRateModel {

    	public Input<TaxonSet> taxonSetInput = new Input<TaxonSet>("taxonset","list of taxa",Validate.REQUIRED);
    	public Input<Boolean> includeStemInput = new Input<Boolean>("includeStem","include stem",false);
    	
        public int getTaxonSetCount(){
        	return 1;
        }
        
        public TaxonSet getTaxonSet(int index) {
        	return taxonSetInput.get();
        }

        public boolean includeStem(int index) {
        	return includeStemInput.get();
        }

    }
}
