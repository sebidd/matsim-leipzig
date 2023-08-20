package org.matsim.run.custom;

import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;

/**
 * Local area scoring factory as subclass of the MATSim internal scoring function factory.
 * @author Sebastian
 *
 */
public class LocalAreaScoringFactory implements ScoringFunctionFactory {

	@Inject
	private Network network;

	@Inject
	private ScoringParametersForPerson pparams;

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		final ScoringParameters params = pparams.getScoringParameters(person);

		SumScoringFunction ssf = new SumScoringFunction();
		/*
		 * Default scoring terms of MATSim.
		 */
		ssf.addScoringFunction(new CharyparNagelLegScoring(params, network));
		ssf.addScoringFunction(new CharyparNagelActivityScoring(params));
		ssf.addScoringFunction(new CharyparNagelMoneyScoring(params));
		ssf.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

		/*
		 * Custom scoring term for local areas.
		 */
		ssf.addScoringFunction(new LocalAreaScoring(network));
		
		return ssf;
	}

}
