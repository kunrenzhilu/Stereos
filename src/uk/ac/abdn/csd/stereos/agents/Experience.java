package uk.ac.abdn.csd.stereos.agents;

import uk.ac.abdn.csd.stereos.agents.evaluators.PerformanceEvaluator;

/**
 * This class encodes a single instance of one agent's experience delegating to
 * another.
 * 
 * @author Chris Burnett
 * 
 */
public class Experience implements Comparable<Experience>
{

	private Agent trustor;
	private Agent trustee;
	private String effort;
	private double observation;
	private double evaluation;
	private PerformanceEvaluator peFunction;
	private int timeStep;

	/**
	 * Create a new experience with the given parameters.
	 * 
	 * @param trustor
	 *            the trustor agent
	 * @param trustee
	 *            the trustee agent
	 * @param effort
	 *            the effort level ID observed, or null if it wasn't monitored
	 * @param observation
	 *            the real, <i>observed</i> (objective) outcome of the
	 *            interaction
	 * @param evaluation
	 *            the evaluated (subjective) outcome of this interaction
	 * @param peFunction
	 *            the performance evaluation function used to produce the
	 *            evaluation
	 * @param timeStep
	 *            the time step at which this experience was created
	 */
	public Experience(Agent trustor, Agent trustee, String effort, double observation, double evaluation,
			PerformanceEvaluator peFunction, int timeStep)
	{
		super();
		this.trustor = trustor;
		this.trustee = trustee;
		this.effort = effort;
		this.observation = observation;
		this.evaluation = evaluation;
		this.peFunction = peFunction;
		this.timeStep = timeStep;
	}

	/*
	 * Getters and Setters
	 */

	public Agent getTrustor()
	{
		return trustor;
	}

	public void setTrustor(Agent trustor)
	{
		this.trustor = trustor;
	}

	public Agent getTrustee()
	{
		return trustee;
	}

	public void setTrustee(Agent trustee)
	{
		this.trustee = trustee;
	}

	public double getObservation()
	{
		return observation;
	}

	public void setObservation(double observation)
	{
		this.observation = observation;
	}

	public double getEvaluation()
	{
		return evaluation;
	}

	public void setEvaluation(double evaluation)
	{
		this.evaluation = evaluation;
	}

	public PerformanceEvaluator getPeFunction()
	{
		return peFunction;
	}

	public void setPeFunction(PerformanceEvaluator peFunction)
	{
		this.peFunction = peFunction;
	}

	public int getTimeStep()
	{
		return timeStep;
	}

	public void setTimeStep(int timeStep)
	{
		this.timeStep = timeStep;
	}

	/**
	 * Compare two experiences with respect to time
	 * 
	 * @param e
	 * @return
	 */
	public int compareTo(Experience e)
	{
		if (this.timeStep == e.getTimeStep())
			return 0;
		if (this.timeStep < e.getTimeStep())
			return -1;
		else
			return 1;
	}

	/**
	 * @param effort
	 *            the effort to set
	 */
	public void setEffort(String effort)
	{
		this.effort = effort;
	}

	/**
	 * @return the effort, null if effort wasn't monitored
	 */
	public String getEffort()
	{
		return effort;
	}
}
