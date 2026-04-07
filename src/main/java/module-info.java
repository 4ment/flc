open module flc {
    requires beast.pkgmgmt;
    requires beast.base;

    exports mf.beast.evolution.branchratemodel;

    provides beast.base.core.BEASTInterface with
        mf.beast.evolution.branchratemodel.Clade,
        mf.beast.evolution.branchratemodel.FLCRateStatistic,
        mf.beast.evolution.branchratemodel.FlexibleLocalClockModel,
        mf.beast.evolution.branchratemodel.StrictCladeModel,
        mf.beast.evolution.branchratemodel.StrictLineageClockModel,
        mf.beast.evolution.branchratemodel.UCRelaxedCladeClockModel,
        mf.beast.evolution.branchratemodel.UCRelaxedLineageClockModel,
        mf.beast.evolution.branchratemodel.UCRelaxedMultiCladeClockModel;
}
