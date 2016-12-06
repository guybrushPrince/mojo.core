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
package de.jena.uni.mojo.plan;


import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.annotations.MajorAnalysisPlan;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * The simplest plan that performs the classic control flow analysis.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
@MajorAnalysisPlan(id = 0, name = "Workflow Graph Analysis Major Plan", author = "Dipl.-Inf. Thomas M. Prinz", description = "The simplest plan that performs the classic control flow analysis")
public class WorkflowGraphMajorPlan extends MajorPlan {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6025334338242006815L;

	/**
	 * The store in which all elements are stored.
	 */
	private final ElementStore store;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            A node id to node map for a fast analysis.
	 * @param analysisInformation
	 *            An analysis information map.
	 * @param store
	 *            The element store.
	 */
	public WorkflowGraphMajorPlan(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation, ElementStore store) {
		super(graph, map, analysisInformation);
		this.store = store;
	}

	@Override
	protected void execute() {

		//
		// 1. Prepare the workflow graph
		//
		PreparationPlan preparationPlan = new PreparationPlan(graph, map,
				analysisInformation, store);
		this.errorList.addAll(preparationPlan.compute());
		this.map = preparationPlan.getMap();

		//
		// 2. Perform the control flow analysis plan
		//
		ControlFlowAnalysisPlan controlFlowPlan = new ControlFlowAnalysisPlan(
				graph, map, analysisInformation);
		errorList.addAll(controlFlowPlan.compute());
	}

}