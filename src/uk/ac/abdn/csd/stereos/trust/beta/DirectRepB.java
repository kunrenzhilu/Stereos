package uk.ac.abdn.csd.stereos.trust.beta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.exceptions.InvalidParametersException;
import uk.ac.abdn.csd.stereos.util.Pair;

/**
 * A trust model using the beta reputaion model, and considering both direct
 * trust and reputation.
 * 
 * @author Chris Burnett
 * 
 */
public class DirectRepB extends DirectB
{

	public DirectRepB(double temperature, int halfLife) throws InvalidParametersException
	{
		super(temperature, halfLife);
	}

	@Override
	public Map<Agent, Double> evaluate(List<Agent> agents, Map<Agent, List<Agent>> recommenders, int time)
	{
		Map<Agent, Double> results = new HashMap<Agent, Double>();
		for (Agent a : agents)
			results.put(a, evaluate(a, recommenders, time));
		return results;
	}

	public double evaluate(Agent a, Map<Agent, List<Agent>> recommenders, int time)
	{
		// double totalPositives = 0, totalNegatives = 0;
		// calculate a reputation value for this agent by quering available
		// recommendation providers for their combined evidence tuples
		Pair<Double, Double> repEvidence = aggregateReputation(recommenders.get(a), a, time);
		double totalPositives = repEvidence.a;
		double totalNegatives = repEvidence.b;

		// if we have a direct rating for this agent include it
		if (evidence.containsKey(a)) {
			// get the rating, taking into account reputation
			Pair<Double, Double> dirEvidence = evidence.get(a);
			// add it to our total evidence tuple
			totalPositives += dirEvidence.a;
			totalNegatives += dirEvidence.b;
			// rating += ratings.get(a);
		}

		// calculate a reputation rating from probability expectation
		return this.calculateProbabilityExpectation(totalPositives, totalNegatives);
	}

	/**
	 * When calculating reputation in a probabilistic model, if we assume all
	 * agents are reliable, then we can just incorporate their experiences as
	 * evidence and re-calculate the probability rather than taking the mean or
	 * average or whatever. Of course, this is a big assumption and needs to be
	 * noted.
	 * 
	 * @param recommenders
	 *            - set of recommenders
	 * @param a
	 *            - target agent
	 * @param time
	 *            - timepoint of recommendation
	 * @return a recommendation
	 */
	private Pair<Double, Double> aggregateReputation(List<Agent> recommenders, Agent a, int time)
	{

		// totals for this target agent
		double positives = 0, negatives = 0;
		// query all our recommenders and accumulate the evidence
		for (Agent r : recommenders) {
			// don't ask the target for its own opinion :)
			if (!r.equals(a)) {
				Pair<Double, Double> rEvidence = r.evidenceQuery(a);
				positives += rEvidence.a;
				negatives += rEvidence.b;
			}
		}
		return new Pair<Double, Double>(positives, negatives);
	}

	@Override
	public Pair<Double, Double> evidenceQuery(Agent a)
	{
		if (evidence.containsKey(a))
			return evidence.get(a);
		else
			return new Pair<Double, Double>(0.0, 0.0);
	}

}
