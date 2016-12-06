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

import java.util.ArrayList;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * A plan represents a simple workflow to a build complex analysis that is based
 * on other analysis.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class Plan extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6842408087300053935L;

	/**
	 * A list to store all errors
	 */
	protected final List<Annotation> errorList = new ArrayList<Annotation>();

	/**
	 * A map for analysis information.
	 */
	protected final AnalysisInformation analysisInformation;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            A node id to node map for a fast analysis.
	 * @param reporter
	 *            A time measurement reporter.
	 * @param analysisInformation
	 *            An analysis information map.
	 */
	public Plan(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation) {
		super(graph, map, analysisInformation);
		this.analysisInformation = analysisInformation;
	}

	@Override
	protected List<Annotation> analyze() {
		// Execute the plan
		executePlan();

		return errorList;
	}

	/**
	 * Executes the implemented plan. However, it may measure some information
	 * about the analysis.
	 */
	private void executePlan() {
		execute();
	}

	/**
	 * In this method, the plan is implemented.
	 */
	protected abstract void execute();

	/**
	 * Returns the name of the plan.
	 * 
	 * @return The name of the plan.
	 */
	public abstract String getName();

}
