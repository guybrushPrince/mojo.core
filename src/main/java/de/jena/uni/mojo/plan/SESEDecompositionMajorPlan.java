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
import de.jena.uni.mojo.decomposition.FastFailureDetection;
import de.jena.uni.mojo.decomposition.GraphDecomposition;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * The SESE decomposition major plan performs a SESE decomposition and uses a
 * simple algorithm to get some failures of the process.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
@MajorAnalysisPlan(id = 2, name = "SESE Decomposition Major Plan", author = "Dipl.-Inf. Thomas M. Prinz", description = "The SESE decomposition major plan performs a SESE decomposition and uses a simple algorithm to get some failures of the process.")
public class SESEDecompositionMajorPlan extends MajorPlan {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3891461673573957791L;

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
	 * @param reporter
	 *            A time measurement reporter.
	 * @param analysisInformation
	 *            An analysis information map.
	 * @param store
	 *            The element store.
	 */
	public SESEDecompositionMajorPlan(WorkflowGraph graph, WGNode[] map,
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
		// 3: Decompose the workflow graph into a RPST (refined process
		// structure tree)
		GraphDecomposition graphDecomposition = new GraphDecomposition(graph,
				map, reporter);
		graphDecomposition.compute();

		//
		// 4: Construct the foldout graph from the rpst and the workflow graph.
		//
		FastFailureDetection fastFailureDetection = new FastFailureDetection(
				graph, map, reporter, graphDecomposition.getRPST());
		errorList.addAll(fastFailureDetection.compute());
	}

}