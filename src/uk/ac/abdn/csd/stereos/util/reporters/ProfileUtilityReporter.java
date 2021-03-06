package uk.ac.abdn.csd.stereos.util.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.abdn.csd.stereos.Experiment;
import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.agents.Profile;

/**
 * Note: this reporter ignores multiple conditions - only prints out a result
 * file for the first condition Note: may only work when the join/leave prob is
 * set to 0 specified in a list.
 * 
 * @author cburnett
 * 
 */
public class ProfileUtilityReporter implements Reporter
{

	/**
	 * String that will be appended to the output filename
	 */
	public static final String id = "pu";

	// directory to write to
	private File dir;

	public ProfileUtilityReporter(File expsDir)
	{
		dir = expsDir;
	}

	public void writeReport(Experiment[] e) throws IOException
	{
		System.out.print("Writing profile utility results...");
		// write the results to a file
		String profile = e[0].getProfileName();
		PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(new File(dir, profile + "-" + id + ".csv"))));

		// print the header line
		out.append("time,");

		// for each experiment, create a column
		// WITHIN THIS LOOP GOES THE CODE TO GENERATE THE RESULTS FOR EACH
		// EXPERIMENT

		// fix j to 0 because we only look at the first experiment in a batch
		// here - we need the data columns
		// for comparing the different profiles to each other.
		int j = 0;
		// create the header and datacolumn
		// out.append("condition"+e[j].getCondition()+",");

		// for each time step
		// for each profile in the experiment
		// average the utilities of agents belonging to that profile
		// each profile plotted as a column

		List<Profile> profiles = e[j].getAgentProfiles();
		// create out colums for data to go in, one for each experiment
		List<List<Double>> profileAverages = new ArrayList<List<Double>>(profiles.size());

		for (int k = 0; k < profiles.size(); k++) {
			// initialise each column
			out.append(profiles.get(k).getId() + ",");
			profileAverages.add(new ArrayList<Double>());
		}
		out.append("\n");

		// for each time step...
		for (int i = 0; i < e[j].getTimeSteps() - 1; i++) {
			// come up with an average of each profile
			for (int l = 0; l < profiles.size(); l++) {
				// for each profile
				double sum = 0, count = 0;
				// for each agent of that profile
				for (Agent a : e[j].getAgents()) {
					// if the agent is of the current profile
					if (a.getProfile().equals(profiles.get(l))) {
						// get the utility of the agent at the current time step
						sum += a.getUtilityHistory().get(i);
						count++;
					}
				}
				double avg = sum / count;
				// record the result to the end of the column corresponding to
				// this profile
				profileAverages.get(l).add(avg);
			}
		}

		// writeout
		// start at i=1
		int i = 1;
		// while none of the columns are empty
		while (dataRemaining(profileAverages)) {
			// write the interaction step
			out.append(i + ",");
			// put the columns together
			for (List<Double> profileColumn : profileAverages) {
				out.append(profileColumn.remove(0) + ",");
				// munch off this item
			}
			i++;
			out.append("\n");
		}
		out.close();
		System.out.print("...completed.\n");
	}

	@SuppressWarnings("unused")
	private String getDescriptionString(Experiment e)
	{
		StringBuffer output = new StringBuffer();
		output.append("PARAMETERS\n");
		output.append("Profile," + e.getProfileName() + "\n");
		output.append("Runs," + e.getTimeSteps() + "\n");
		output.append("Agent count," + e.getAgentCount() + "\n");
		output.append("# teams," + e.getTeamCount() + "\n");
		output.append("# trustor per team," + e.getTrustorCount() + "\n");
		output.append("Team size," + e.getTeamSize() + "\n");
		output.append("Team lifetime," + e.getTeamLifeTime() + "\n");
		output.append("Noise feature count," + e.getNoiseFeatureCount() + "\n");
		output.append("Recency half-life," + e.getHalfLife() + "\n");
		output.append("Temperature," + e.getTemp() + "\n");
		output.append("Maximum rep. queries," + e.getMaxQueries() + "\n\n");

		output.append("AGENT PROFILES\n");
		output.append("name,mean,st.dev,count,features\n");
		List<Profile> profiles = e.getAgentProfiles();
		for (Profile profile : profiles) {
			output.append(profile.getId() + "," + profile.getDefaultMeanPerformance() + ","
					+ profile.getDefaultVariance() + "," + profile.getTrusteeCount() + ",");
			for (Entry<String, Double> feature : profile.getFeatures().entrySet()) {
				output.append(feature.getKey() + ":" + feature.getValue() + " ");
			}
			output.append("\n");
		}
		output.append("\n");
		return output.toString();
	}

	/**
	 * Return false if any of the data columns are empty
	 * 
	 * @param data
	 * @return
	 */
	private boolean dataRemaining(List<List<Double>> data)
	{
		boolean dataRemaining = true;
		for (List<Double> column : data) {
			if (column.isEmpty())
				dataRemaining = false;
		}
		return dataRemaining;
	}

}
