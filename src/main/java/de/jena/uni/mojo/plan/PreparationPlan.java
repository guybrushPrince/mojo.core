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
import de.jena.uni.mojo.analysis.transformation.SimplenessTransformation;
import de.jena.uni.mojo.general.Plan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.sub.Data;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * This plan prepares the workflow graph for the analysis.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class PreparationPlan extends Plan {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6922867438861392744L;

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
	public PreparationPlan(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation, ElementStore store) {
		super(graph, map, analysisInformation);
		this.store = store;
	}

	@Override
	protected void execute() {
		//
		// 1. Make each workflow graph simple, i.e., we add (if necessary)
		// an additional task node on each edge with a source and sink
		// which are not task nodes.
		//
		SimplenessTransformation transform = new SimplenessTransformation(
				graph, map, reporter, store);

		transform.compute();
		map = transform.getExtendedMap();

		//
		// 2. For each node of the graph, create some extra information that
		// can be used for a fast and efficient analysis.
		//
		for (WGNode node : graph.getNodeListInclusive()) {
			node.setExtraInformation(new Data(map.length));
		}
	}

	/**
	 * Returns the changed map of workflow graph nodes.
	 * 
	 * @return The node map.
	 */
	protected WGNode[] getMap() {
		return this.map;
	}

	@Override
	public String getName() {
		return "Workflow Graph Preparation Plan";
	}

}
