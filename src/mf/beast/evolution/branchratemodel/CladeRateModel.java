package mf.beast.evolution.branchratemodel;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.branchratemodel.BranchRateModel;

public interface CladeRateModel extends BranchRateModel {

	public int getTaxonSetCount();
	
    public TaxonSet getTaxonSet(int index);

    public boolean includeStem(int index);

    public abstract class Base extends BranchRateModel.Base implements CladeRateModel {

    	//public Input<List<Clade>> cladeInputs = new Input<List<Clade>>("clade","list of clades",new ArrayList<Clade>());
    	public Input<TaxonSet> taxonSetInput = new Input<TaxonSet>("taxonset","list of taxa",Validate.REQUIRED);
    	public Input<Boolean> includeStemInput = new Input<Boolean>("includeStem","include stem",false);
    	
        public int getTaxonSetCount(){
        	//return cladeInputs.get().size();
        	return 1;
        }
        
        public TaxonSet getTaxonSet(int index) {
            //return cladeInputs.get().get(index).getTaxonSet();
        	return taxonSetInput.get();
        }

        public boolean includeStem(int index) {
            //return cladeInputs.get().get(index).includeStem();
        	return includeStemInput.get();
        }
    }
}
