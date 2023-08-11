package org.matsim.run.custom;

import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;

public class LocalAreaScoringFactory implements ScoringFunctionFactory {

	@Inject
	private Network network;

	@Inject
	private ScoringParametersForPerson pparams;

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		final ScoringParameters params = pparams.getScoringParameters(person);

		SumScoringFunction ssf = new SumScoringFunction();
		ssf.addScoringFunction(new CharyparNagelLegScoring(params, network));
		ssf.addScoringFunction(new CharyparNagelActivityScoring(params));
		ssf.addScoringFunction(new CharyparNagelMoneyScoring(params));
		ssf.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

		ssf.addScoringFunction(new LocalAreaScoring(network));
		
		return ssf;
	}

}
