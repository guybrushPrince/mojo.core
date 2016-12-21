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
package de.jena.uni.mojo.verifier;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import de.jena.uni.mojo.Mojo;
import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.plan.WorkflowGraphMajorPlan;
import de.jena.uni.mojo.plugin.PlanPlugin;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * The verifier is the entry point for each analysis. It holds some information
 * about parallel processes, etc.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Verifier extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5747937418712967031L;

	/**
	 * A fork join pool to allow simple parallelism.
	 */
	private static ForkJoinPool fjPool = new ForkJoinPool();

	/**
	 * An element store where each element is stored.
	 */
	private final ElementStore store;

	/**
	 * When Mojo is used as terminal tool, it has not an element store in any
	 * case. If there is not such an element store, then the usage of this
	 * constructor should be preferred.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 */
	public Verifier(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.store = null;
	}

	/**
	 * When Mojo is used as library and there exists an element store, then this
	 * constructor should be used.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 * @param store
	 *            The element store.
	 */
	public Verifier(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, ElementStore store) {
		super(graph, map, reporter);
		this.store = store;
	}

	@Override
	protected List<Annotation> analyze() {

		//
		// 1. Take the right major plan
		//
		// Get all major plans
		reporter.startIgnoreTimeMeasurement(graph, this.getClass().getName());

		// Determine the constructor of the right major plan.
		String planId = Mojo.getCommand("ANALYSIS_PLAN").asStringValue();
		
		// Instantiate the major plan
		PlanPlugin plugin = Mojo.getPlanPlugins().get(planId);
		MajorPlan plan = null;
		// Instantiate the plan
		if (plugin != null) {
			plan = plugin.getInstance(graph, map, reporter, store);
		} else {
			plan = new WorkflowGraphMajorPlan(graph, map, reporter, store);
		}
		
		reporter.endIgnoreTimeMeasurement(graph, this.getClass().getName());

		// Invoke the plan
		fjPool.invoke(plan);

		// And join the information.
		return plan.join();
	}
}
