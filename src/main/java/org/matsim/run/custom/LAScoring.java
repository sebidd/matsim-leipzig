package org.matsim.run.custom;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;

public class LAScoring implements ScoringFunctionFactory {

	private final Scenario scenario;

	private LAScoring(Scenario scenario){
		this.scenario = scenario;
	}

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sum = new SumScoringFunction();

		final ScoringParameters params = new ScoringParameters.Builder(scenario, person).build();
		sum.addScoringFunction(new CharyparNagelActivityScoring(params));
		sum.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork()));
		sum.addScoringFunction(new CharyparNagelMoneyScoring(params));
		sum.addScoringFunction(new CharyparNagelAgentStuckScoring(params));
		sum.addScoringFunction(new LAHandler((e) -> {return -Math.pow(e, 2);}, scenario));

		return sum;
	}
}
