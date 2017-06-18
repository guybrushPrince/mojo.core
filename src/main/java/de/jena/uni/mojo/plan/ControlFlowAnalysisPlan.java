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


import de.jena.uni.mojo.analysis.edge.EdgeAnalysis;
import de.jena.uni.mojo.analysis.edge.StrongComponentsAnalysis;
import de.jena.uni.mojo.analysis.edge.abundance.AbundanceAnalysis;
import de.jena.uni.mojo.analysis.edge.deadlock.DeadlockAnalysis;
import de.jena.uni.mojo.analysis.edge.dominance.DominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.edge.dominance.PostDominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.analysis.or.WaitingAreaAnalysis;
import de.jena.uni.mojo.general.Plan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * This plan performs the heart of mojo: the control flow analysis. It detects
 * deadlocks and lack of synchronization.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class ControlFlowAnalysisPlan extends Plan {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4642670985350618575L;

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
	public ControlFlowAnalysisPlan(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation) {
		super(graph, map, analysisInformation);
	}

	@Override
	protected void execute() {
		//
		// 0. Determine the edges in the workflow graph
		//
		EdgeAnalysis edgeAnalysis = new EdgeAnalysis(graph, map, reporter);
		edgeAnalysis.compute();

		//
		// 1. Perform a dominator edge analysis
		//
		DominatorEdgeAnalysis domEdgeAnalysis = new DominatorEdgeAnalysis(
				graph, map, reporter);
		domEdgeAnalysis.fork();

		//
		// 2. Perform a post-dominator edge analysis
		//
		PostDominatorEdgeAnalysis postDomEdgeAnalysis = new PostDominatorEdgeAnalysis(
				graph, map, reporter);
		postDomEdgeAnalysis.fork();
		
		// 
		// 2.1 Perform a strong connected components analysis
		// 
		StrongComponentsAnalysis strongComponentsAnalysis = new StrongComponentsAnalysis(
				graph, map, reporter);
		strongComponentsAnalysis.fork();

		domEdgeAnalysis.join();
		postDomEdgeAnalysis.join();
		strongComponentsAnalysis.join();

		//
		// 3. Determine the waiting areas
		//
		if (!graph.getOrJoinList().isEmpty()) {
			WaitingAreaAnalysis waitingAreaAnalysis = new WaitingAreaAnalysis(
					graph, map, reporter);
			errorList.addAll(waitingAreaAnalysis.compute());
		}

		//
		// 4. Perform the deadlock analysis
		//
		DeadlockAnalysis deadlockAnalysis = new DeadlockAnalysis(graph, map,
				reporter, postDomEdgeAnalysis);
		deadlockAnalysis.fork();

		//
		// 5. Determine abundances
		//
		AbundanceAnalysis abuAnalysis = new AbundanceAnalysis(graph, map,
				reporter, domEdgeAnalysis, strongComponentsAnalysis);
		abuAnalysis.fork();

		//
		// 6. Perform the OR join deadlock analysis (already done with
		// abundance)
		//
		errorList.addAll(deadlockAnalysis.join());
		errorList.addAll(abuAnalysis.join());
	}

	@Override
	public String getName() {
		return "Workflow Graph Control Flow Analysis Plan";
	}

}
