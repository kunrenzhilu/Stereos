package uk.ac.abdn.csd.stereos.learning;

import java.util.Map;

import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.trust.sl.Opinion;

public interface Clusterer
{

	/**
	 * Run the clusterer on the provided data.
	 */
	public void createClusters();

	/**
	 * Return a mapping of agents to cluster IDs produced by this clusterer.
	 * 
	 * @return
	 */
	public Map<Agent, Integer> getLabelledAgents();

	public void visualise();

	/**
	 * Return a mapping of agents to cluster-based PE values, given a list of
	 * agents and the cluster to which they belong (or to which they are
	 * predicted to belong) to.
	 * 
	 * @param agents
	 * @return a map mapping agents to base-rate PE values derived from clusters
	 */
	public Map<Agent, Double> getClassPEValues(Map<Agent, Integer> agents);

	/**
	 * Get a set of found centroids
	 * 
	 * @return
	 */
	public int getNumClusters();

	public void addOpinions(Map<Agent, Opinion> examples);

	public boolean isReady();

}