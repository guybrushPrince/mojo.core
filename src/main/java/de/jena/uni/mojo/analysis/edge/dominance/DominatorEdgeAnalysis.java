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
package de.jena.uni.mojo.analysis.edge.dominance;

import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * This analysis determines for each edge its dominance frontier and dominating
 * edges. For this, it uses the algorithms of
 * 
 * Cooper, Keith D. and Harvey, Timothy J. and Kennedy, Ken: A Simple, Fast
 * Dominance Algorithm Rice Computer Science TR-06-33870
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class DominatorEdgeAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7598213822914028809L;

	/**
	 * A constant for the analysis reporter.
	 */
	public final static String DOMEDGE_NUMBER_VISITED_EDGES = "DOMEDGE_NUMBER_VISITED_EDGES";

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
	 * A list of the edges in reverse post order.
	 */
	private final LinkedList<Edge> reversePostOrder;

	/**
	 * The number of edges which are visited by this algorithm.
	 */
	private int edgesVisited = 0;

	/**
	 * The dominator edge analysis constructor.
	 * 
	 * @param graph
	 *            The workflow graph for which the dominators are determined.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public DominatorEdgeAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.edges = graph.getEdges();
		this.incoming = graph.getIncomingEdges();
		this.outgoing = graph.getOutgoingEdges();
		this.reversePostOrder = new LinkedList<Edge>();
	}

	@Override
	protected List<Annotation> analyze() {
		//
		// Step 1: Determine the reverse post order
		//
		reversePostorder();

		//
		// Step 2: Determine the dominance tree
		//
		dominanceAnalysis();

		//
		// Step 3: Determine all dominators of each edge (instead of
		// only the immediate dominator).
		//
		allDominatorsAnalysis();

		//
		// Step 4: Determine the dominace frontier of each edge.
		//
		dominanceFrontierAnalysis();

		// Put some information into the reporter about the number of
		// visited edges.
		reporter.put(graph, DOMEDGE_NUMBER_VISITED_EDGES, edgesVisited);

		// Return an empty collection since this analysis do not find
		// failures.
		return Collections.emptyList();
	}

	/**
	 * Perform the dominance analysis.
	 */
	private void dominanceAnalysis() {
		// Some help sets
		BitSet defined = new BitSet(edges.size());
		BitSet in = new BitSet(edges.size());

		// Get the start edge
		Edge startEdge = edges.get(outgoing[graph.getStart().getId()]
				.nextSetBit(0));
		startEdge.dominatorList.add(startEdge);
		defined.set(startEdge.id);
		reversePostOrder.removeFirst();

		boolean stable;
		do {
			stable = true;
			for (Edge e : reversePostOrder) {
				in.clear();
				in.or(incoming[e.src.getId()]);
				in.and(defined);

				// A edge is visited
				edgesVisited++;

				int j = in.nextSetBit(0);
				if (j >= 0) {
					Edge idom = edges.get(j);
					for (j = in.nextSetBit(j + 1); j >= 0; j = in
							.nextSetBit(j + 1)) {
						// A edge is visited
						edgesVisited++;
						idom = intersect(edges.get(j), idom);
					}
					if (!defined.get(e.id)) {
						defined.set(e.id);
						e.dominatorList.addLast(idom);
						stable = false;
					} else {
						Edge idomOld = e.dominatorList.getLast();
						if (idomOld.postOrderNumber != idom.postOrderNumber) {
							e.dominatorList.addLast(idom);
							stable = false;
						}
					}
				}
			}
		} while (!stable);
	}

	/**
	 * Intersects some information. Look at the paper of Cooper et al. for more
	 * information.
	 * 
	 * @param finger1
	 *            The first edge and
	 * @param finger2
	 *            the second edge to intersect.
	 * @return The intersection dominator edge.
	 */
	private Edge intersect(Edge finger1, Edge finger2) {
		while (finger1.postOrderNumber != finger2.postOrderNumber) {
			while (finger1.postOrderNumber < finger2.postOrderNumber) {
				// A edge is visited
				edgesVisited++;

				finger1 = finger1.dominatorList.getLast();
			}
			while (finger2.postOrderNumber < finger1.postOrderNumber) {
				// A edge is visited
				edgesVisited++;

				finger2 = finger2.dominatorList.getLast();
			}
		}
		return finger1;
	}

	/**
	 * Perform the dominance frontier analysis.
	 */
	private void dominanceFrontierAnalysis() {
		BitSet in = new BitSet(edges.size());
		for (Edge e : edges) {
			// A edge is visited
			edgesVisited++;

			in.clear();
			in.or(incoming[e.src.getId()]);
			if (in.cardinality() >= 2) {
				for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
					// A edge is visited
					edgesVisited++;

					Edge runner = edges.get(i);
					while (runner.id != e.dominatorList.getLast().id) {
						// A edge is visited
						edgesVisited++;

						runner.dominanceFrontierSet.set(e.id);
						runner = runner.dominatorList.getLast();
					}
				}
			}
		}
	}

	/**
	 * In reverse post order, determine all dominators of each edge.
	 */
	private void allDominatorsAnalysis() {
		BitSet visited = new BitSet(edges.size());

		// A edge is visited
		edgesVisited++;

		int startEdgeId = outgoing[graph.getStart().getId()].nextSetBit(0);
		visited.set(startEdgeId);
		edges.get(startEdgeId).dominatorSet.set(startEdgeId);

		for (Edge e : reversePostOrder) {
			if (!visited.get(e.id))
				getAllDominators(e, visited);
		}
	}

	/**
	 * Returns all dominators for a single edge (with depth-first search).
	 * 
	 * @param e
	 *            The edge for which the dominators should be determined.
	 * @param visited
	 *            The already visited edges.
	 * @return The dominator set of this edge.
	 */
	private BitSet getAllDominators(Edge e, BitSet visited) {
		if (!visited.get(e.id)) {
			// A edge is visited
			edgesVisited++;

			visited.set(e.id);
			// Get the immediate dominator
			Edge immediate = e.dominatorList.getLast();
			e.dominatorSet.or(getAllDominators(immediate, visited));
			e.dominatorSet.set(e.id);
		}
		return e.dominatorSet;
	}

	/**
	 * Produce a list of edges in reverse post order.
	 */
	private void reversePostorder() {
		BitSet visited = new BitSet(edges.size());

		// A edge is visited
		edgesVisited++;

		BitSet out = (BitSet) outgoing[graph.getStart().getId()].clone();
		for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
			depthFirstSearch(edges.get(i), visited);
		}
	}

	/**
	 * Perform a simple depth first search to produce the reverse post order.
	 * 
	 * @param e
	 *            The current edge of the search.
	 * @param visited
	 *            The visited edges.
	 */
	private void depthFirstSearch(Edge e, BitSet visited) {
		// A edge is visited
		edgesVisited++;

		visited.set(e.id);
		BitSet out = (BitSet) (outgoing[e.tgt.getId()]).clone();
		out.andNot(visited);
		for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
			depthFirstSearch(edges.get(i), visited);
		}
		e.postOrderNumber = reversePostOrder.size();
		reversePostOrder.addFirst(e);
	}
}