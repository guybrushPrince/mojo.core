/**
 * Copyright 2019 mojo Friedrich Schiller University Jena
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
package de.jena.uni.mojo.analysis.edge;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.dominance.PostDominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * This analysis detects the (approx.) execution edges for each 
 * join node.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class ExecutionEdgeAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 992480107651825462L;	

	/**
	 * A constant which defines a variable for the analysis information.
	 */
	public final static String EXEC_EDGE_NUMBER_VISITED_EDGES = "EXEC_EDGE_NUMBER_VISITED_EDGES";
	
	/**
	 * A list of all edges within the workflow graph.
	 */
	public final List<Edge> edges;

	/**
	 * An array of bit sets where each bit set contains incoming edges of the
	 * node with the id of the position in the array.
	 */
	public final BitSet[] incoming;

	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	public final BitSet[] outgoing;

	/**
	 * The number of edges visited during this analysis.
	 */
	private int edgesVisited = 0;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 * @param edgeAnalysis
	 *            The post dominance edge analysis.
	 */
	public ExecutionEdgeAnalysis(WorkflowGraph graph, WGNode[] map, AnalysisInformation reporter,
			PostDominatorEdgeAnalysis edgeAnalysis) {
		super(graph, map, reporter);
		this.edges = edgeAnalysis.edges;
		this.incoming = edgeAnalysis.incoming;
		this.outgoing = edgeAnalysis.outgoing;
	}

	@Override
	protected List<Annotation> analyze() {
		// Determine execution edge approximation
		determineApprExecEdges();

		// Add some information to the analysis reporter.
		reporter.put(graph, EXEC_EDGE_NUMBER_VISITED_EDGES, edgesVisited);
		return Collections.emptyList();
	}

	/**
	 * Determine the approximated activation edges.
	 */
	private void determineApprExecEdges() {
		// Build a bit set of all outgoing edges
		// of all split nodes
		BitSet outSplits = new BitSet(this.edges.size());
		for (WGNode split : graph.getSplitList()) {
			outSplits.or(outgoing[split.getId()]);
		}
		for (WGNode orsplit : graph.getOrForkList()) {
			outSplits.or(outgoing[orsplit.getId()]);
		}

		// Build an edge bit set
		BitSet edges = new BitSet(this.edges.size());
		edges.set(0, this.edges.size());

		// We determine the appr. exec. edges for each
		// join node
		for (WGNode join : graph.getJoinList()) {
			// Get the join's incoming edges
			BitSet in = incoming[join.getId()];

			// Get the join's outgoing edge
			int out = outgoing[join.getId()].nextSetBit(0);

			// Set the outgoing's edge appr. exec.
			// edges to the whole set of edges.
			Edge outEdge = this.edges.get(out);
			outEdge.isApproxExecutedBy.or(edges);

			// For each incoming edge of the join, we determine
			// its appr. exec. edges.
			for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {

				// Create a copy of the bit set of edges
				BitSet allowed = (BitSet) edges.clone();
				BitSet splitCopy = (BitSet) outSplits.clone();

				boolean stable;
				do {
					// Set it to stable
					stable = true;

					// Create a bit set for visited edges
					BitSet visited = new BitSet(allowed.size());
					// Perform a inverse depth first search
					inverseDepthFirstSearch(i, out, allowed, visited);

					// Determine removed edges
					allowed.andNot(visited);
					// Determine all edges which went out of a split
					// and were removed. Its split can be removed.
					allowed.and(splitCopy);

					// An edge is visited
					edgesVisited++;

					for (int s = allowed.nextSetBit(0); s >= 0; s = allowed.nextSetBit(s + 1)) {

						// An edge is visited
						edgesVisited++;

						// Determine the split
						WGNode split = this.edges.get(s).src;

						// We can remove its incoming edge.
						visited.andNot(incoming[split.getId()]);

						// We do not have to visited this split twice, so
						// we remove its outgoing edges from both, the removed
						// edges and the outgoing split edges set
						splitCopy.andNot(outgoing[split.getId()]);
						allowed.andNot(outgoing[split.getId()]);

						stable = false;
					}

					allowed = visited;

				} while (!stable);

				// The allowed nodes are those, who are appr. exec. edges
				// of the current incoming edge.
				Edge curIn = this.edges.get(i);
				curIn.isApproxExecutedBy.or(allowed);
				// Set for each edge that it approx. executes the incoming edge.
				for (int s = allowed.nextSetBit(0); s >= 0; s = allowed.nextSetBit(s + 1)) {
					// An edge is visited
					edgesVisited++;
					
					this.edges.get(s).approxExecutes.set(i);
				}

				outEdge = this.edges.get(out);
				outEdge.isApproxExecutedBy.and(allowed);
			}
		}
	}

	/**
	 * Searches all edges with currently a path to the edge.
	 * 
	 * @param current
	 *            The current edge id.
	 * @param last
	 *            The last edge id (the outgoing edge of join).
	 * @param notAllowed
	 *            Removed edges.
	 * @param visited
	 *            The already visited edges.
	 */
	private void inverseDepthFirstSearch(int current, int last, BitSet allowed, BitSet visited) {
		// The current edge is visited
		visited.set(current);

		// An edge is visited
		edgesVisited++;

		// If we reach the outgoing edge (last) of the join
		// node, then we not visited the other edges since
		// we are in a deliver graph.
		if (current != last) {

			// Determine the edge
			Edge curEdge = edges.get(current);

			// Get the predecessors of the current edge
			BitSet predCopy = (BitSet) incoming[curEdge.src.getId()].clone();

			// Eliminate visited and not allowed edges
			predCopy.and(allowed);
			predCopy.andNot(visited);

			// Visited the rest predecessors
			for (int p = predCopy.nextSetBit(0); p >= 0; p = predCopy.nextSetBit(p + 1)) {
				inverseDepthFirstSearch(p, last, allowed, visited);
			}
		}
	}
}
