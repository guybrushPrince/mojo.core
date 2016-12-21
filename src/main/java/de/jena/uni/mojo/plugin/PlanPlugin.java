/**
 * Copyright 2016 mojo Friedrich Schiller University Jena
 * 
 * This file is part of mojo.
 * 
 * mojo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * mojo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with mojo. If not, see <http://www.gnu.org/licenses/>.
 */
package de.jena.uni.mojo.plugin;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * 
 * @author Dipl.-Inf. Thomas Prinz
 *
 */
public interface PlanPlugin {

	/**
	 * The id of the major analysis plan.
	 */
	public String getId();

	/**
	 * The name of the major analysis plan.
	 * @return The name of the plugin.
	 */
	public String getName();

	/**
	 * The author of the major analysis plan.
	 * @return The name of the author.
	 */
	public String getAuthor();

	/**
	 * A short description what the major analysis plan do.
	 * @return A short description.
	 */
	public String getDescription();
	
	/**
	 * Get the version.
	 * @return The version.
	 */
	public String getVersion();
	
	/**
	 * 
	 * @param graph
	 * @param map
	 * @param analysisInformation
	 * @param store
	 * @return
	 */
	public MajorPlan getInstance(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation, ElementStore store);
}
