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
package de.jena.uni.mojo.general;


import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.annotations.MajorAnalysisPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * A major plan. The only difference from an ordinary plan is that the major
 * plan has a specific number. This number can be used to start mojo with a
 * different major plan.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class MajorPlan extends Plan {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2233587135381401491L;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            A node id to node map for a fast analysis.
	 * @param analysisInformation
	 *            An analysis information map.
	 */
	public MajorPlan(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation) {
		super(graph, map, analysisInformation);
	}

	/**
	 * Get the number of this major plan.
	 * 
	 * @return The number of this major plan.
	 */
	public int getNumber() {
		return this.getClass().getAnnotation(MajorAnalysisPlan.class).id();
	}

	@Override
	public String getName() {
		return this.getClass().getAnnotation(MajorAnalysisPlan.class).name();
	}

}
