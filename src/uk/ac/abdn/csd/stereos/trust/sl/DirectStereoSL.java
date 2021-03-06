package uk.ac.abdn.csd.stereos.trust.sl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.agents.Experience;
import uk.ac.abdn.csd.stereos.exceptions.InvalidParametersException;

import uk.ac.abdn.csd.stereos.learning.Learner;
import uk.ac.abdn.csd.stereos.learning.M5PLearner;
import uk.ac.abdn.csd.stereos.learning.TwoStageLearner; //import uk.ac.abdn.csd.stereos.learning.ReFELearner;
//import uk.ac.abdn.csd.stereos.learning.ReducedModelLearner;

import uk.ac.abdn.csd.stereos.util.Utilities;

/**
 * This class implements a model with full direct, reputational and stereotyping
 * componants, using subjective logic as the underlying computational model.
 * 
 * This model uses direct trust only: as such it is mainly for testing purposes.
 * 
 * It extends the normal SL trust model, by using the learning mechanism to set
 * the base rate.
 * 
 * @author Chris Burnett
 * 
 */
public class DirectStereoSL extends DirectSL
{
	
	/*
	 * This trust model needs to encapsulate the clustering, labelling and
	 * learning algorithm. Not only that, but it must determine when to update
	 * its stereotype model (every interaction would be highly unfeasible).
	 * Finally, it must apply it in some way to come up with a better rating
	 * (e.g. calculate the base rate)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.abdn.csd.stereos.trust.TrustModel#evaluate(java.util.List,
	 * java.util.List, int)
	 */

	/**
	 * The number of experiences an agent will wait to obtain before
	 * recalculating stereotypes
	 */
	protected final int learningInterval;
	/**
	 * Number of experiences remaining before updating stereotypes
	 */
	protected int intervalRemaining;

	/**
	 * The stereotype learning model we will use
	 */
	protected Learner learner;

	/**
	 * Since the ratings produced by stereotypes aren't changing unless they are
	 * rebuilt, we can maintain a cache to store the outcomes of classifications
	 * for rapid re-use, and only rebuild the cache when the classifier is
	 * rebuilt.
	 */
	protected Map<Agent, Double> stereotypeRatingCache;

	/**
	 * Store the error of the trust model at each time step
	 */
	protected List<Double> errors;
	
	protected int clusters;

	/**
	 * Create a new model with the given parameters
	 * 
	 * @param temperature
	 * @param halfLife
	 * @param clusters
	 * @throws InvalidParametersException
	 */
	public DirectStereoSL(double temperature, int halfLife, int learningInterval, int clusters)
			throws InvalidParametersException
	{
		super(temperature, halfLife);
		// create the clusterer and classifier learner
		this.learner = new TwoStageLearner(clusters, true);
		//this.learner = new M5PLearner();
		this.clusters = clusters;
		this.learningInterval = learningInterval;
		intervalRemaining = learningInterval;
		stereotypeRatingCache = new HashMap<Agent, Double>();
		errors = new ArrayList<Double>();
	}

	/**
	 * We need to check when adding experiences whether the L limit has been
	 * reached. Every time an experience is added, we move on towards L because
	 * we know a delegation has been made.
	 */
	@Override
	public void addExperience(Experience e)
	{
		super.addExperience(e);
		// figure out if we should reproduce the clusters and classifier
		// if we should:
		if (intervalRemaining <= 0) {
			learner.train(this.opinions);
			// reset counter and base rate cache
			intervalRemaining = learningInterval;
		} else
			intervalRemaining--; // otherwise, decrement and continue
		// record the error at this point
		if (learner.isReady())
			errors.add(learner.getErrorRate());
		else
			errors.add(1.0); // if we haven't got the model ready, then add full
								// error
	}

	/**
	 * Evaluate a list of agents
	 */
	@Override
	public Map<Agent, Double> evaluate(List<Agent> agents, Map<Agent, List<Agent>> recommenders, int time)
	{
            Map<Agent, Double> results = new HashMap<Agent, Double>();

		// update the base rate cache as required, classifying any yet unseen
		// agents
		updateBaseRates(agents);

		for (Agent a : agents) {
			double thisRating = evaluate(a, recommenders, time);
			// update rating cache
			ratings.put(a, thisRating);

			// add to results
			results.put(a, thisRating);
		}

		// update the mean rating
		meanRating = Utilities.calculatePopulationMeanPerformance(ratings);
		// return the results
		return results;
	}

	@Override
	public double evaluate(Agent a, Map<Agent, List<Agent>> recommenders, int time)
	{
		// get an opinion if we already have one
		Opinion op;
		if (opinions.containsKey(a))
			op = opinions.get(a);
		else {
			// else create a new opinion - maybe we'll be able to set a base
			// rate for it
			op = new Opinion(0.0, 0.0);
			op.setBaseRate(defaultPrior);
			opinions.put(a, op);
		}

		// set the base rate from the stereotype, if we have one
		// otherwise, the ambiguity aversion parameter will take effect
		if (stereotypeRatingCache.containsKey(a)) {
			op.setBaseRate(this.stereotypeRatingCache.get(a));
		}

		// return the rating
		return op.getExpectationValue();
	}

	/**
	 * Use the stereotyping model to obtain the apriori base rate for these
	 * trustees, if required. We use this approach because Weka seems to prefer
	 * classifying sets of data rather than instances individually.
	 * 
	 * @param trustees
	 * @return a map mapping trustees to double base rate values
	 * @throws Exception
	 */
	protected void updateBaseRates(List<Agent> trustees)
	{
		// this is the list of unknown agents we will pass to the classifier
		List<Agent> classificationList = new ArrayList<Agent>();
		// classify this agent using the classifier we have built, if one exists
		if (learner.isReady()) {
			for (Agent trustee : trustees) {
				// the classifier is ready, so use our trained model
				// but first check to see if we have a cached value
				if (!this.stereotypeRatingCache.containsKey(trustee))
					classificationList.add(trustee);
			}
			// don't do anything if all the agents already have stereotypes
			if (!classificationList.isEmpty()) {
				// now classify the uncached agents
				Map<Agent, Double> newBaseRates = learner.getBaseRates(classificationList);
				// update the cache...
				this.stereotypeRatingCache.putAll(newBaseRates);
			}
		}
	}

	public Learner getLearner()
	{
		return this.learner;
	}
	
	public void forget()
	{
		super.forget();
		this.opinions.clear();
		this.stereotypeRatingCache.clear();
		this.ratings.clear();
		this.evidence.clear();
		
		this.learner = new TwoStageLearner(clusters, true);
	}

	/**
	 * Return the confidence value (in this case, computed from the underlying
	 * model)
	 */
	public double confidenceQuery()
	{
		return 1 - learner.getErrorRate();
	}

	/**
	 * Return the confidence value from the history
	 */
	public double confidenceQuery(int index)
	{
		return 1 - errors.get(index);
	}
}
