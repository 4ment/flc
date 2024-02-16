package mf.beast.evolution.branchratemodel;

import beast.core.BEASTObject;
import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.TaxonSet;

@Description("Defines a simple clade object.")
public class Clade extends BEASTObject{
	public Input<TaxonSet> taxonSetInput = new Input<TaxonSet>("taxonset", "list of taxa.");
	public Input<Boolean> includeStemInput = new Input<Boolean>("includeStem", "true if the stem of the clade should be included.", false);
	
	@Override
	public void initAndValidate() {
		
	}
	
	public final TaxonSet getTaxonSet(){
		return taxonSetInput.get();
	}
	
	public final boolean includeStem(){
		return includeStemInput.get();
	}
}
