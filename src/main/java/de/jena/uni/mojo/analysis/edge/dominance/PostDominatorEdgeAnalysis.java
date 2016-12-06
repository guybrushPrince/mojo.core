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
 * This analysis determines for each edge its post dominance frontier and
 * dominating edges. For this, it uses the algorithms of
 * 
 * Cooper, Keith D. and Harvey, Timothy J. and Kennedy, Ken: A Simple, Fast
 * Dominance Algorithm Rice Computer Science TR-06-33870
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class PostDominatorEdgeAnalysis extends Analysis {

	/**
	 * The serial version uid.
	 */
	private static final long serialVersionUID = 7598213822914028809L;

	/**
	 * A constant for the use in the analysis information reporter.
	 */
	public final static String POSTDOMEDGE_NUMBER_VISITED_EDGES = "POSTDOMEDGE_NUMBER_VISITED_EDGES";

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
	 * The edges in post reverse post order.
	 */
	private final LinkedList<Edge> postReversePostOrder;

	/**
	 * The number of edges visited.
	 */
	private int edgesVisited = 0;

	/**
	 * The constructor of the post dominator edge analysis.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node map array.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public PostDominatorEdgeAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.edges = graph.getEdges();
		this.incoming = graph.getIncomingEdges();
		this.outgoing = graph.getOutgoingEdges();
		this.postReversePostOrder = new LinkedList<Edge>();
	}

	@Override
	protected List<Annotation> analyze() {
		postReversePostorder();

		postDominanceAnalysis();
		allPostDominatorsAnalysis();

		postDominanceFrontierAnalysis();

		reporter.put(graph, POSTDOMEDGE_NUMBER_VISITED_EDGES, edgesVisited);

		return Collections.emptyList();
	}

	/**
	 * Performs the post dominance analysis (finds the post-dominance tree).
	 */
	private void postDominanceAnalysis() {
		// Some help sets
		BitSet defined = new BitSet(edges.size());
		BitSet out = new BitSet(edges.size());

		// Get the end edge
		Edge endEdge = edges
				.get(incoming[graph.getEnd().getId()].nextSetBit(0));
		endEdge.postDominatorList.add(endEdge);
		defined.set(endEdge.id);
		postReversePostOrder.removeFirst();

		boolean stable;
		do {
			stable = true;
			for (Edge e : postReversePostOrder) {
				// An edge is visited
				edgesVisited++;

				out.clear();
				out.or(outgoing[e.tgt.getId()]);
				out.and(defined);

				int j = out.nextSetBit(0);
				if (j >= 0) {
					Edge idom = edges.get(j);
					for (j = out.nextSetBit(j + 1); j >= 0; j = out
							.nextSetBit(j + 1)) {
						// An edge is visited
						edgesVisited++;

						idom = intersect(edges.get(j), idom);
					}
					if (!defined.get(e.id)) {
						defined.set(e.id);
						e.postDominatorList.addLast(idom);
						stable = false;
					} else {
						Edge idomOld = e.postDominatorList.getLast();
						if (idomOld.postPostOrderNumber != idom.postPostOrderNumber) {
							e.postDominatorList.addLast(idom);
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
	 * @return The intersection post dominator edge.
	 */
	private Edge intersect(Edge finger1, Edge finger2) {
		while (finger1.postPostOrderNumber != finger2.postPostOrderNumber) {
			// An edge is visited
			edgesVisited++;

			while (finger1.postPostOrderNumber < finger2.postPostOrderNumber) {
				// An edge is visited
				edgesVisited++;

				finger1 = finger1.postDominatorList.getLast();
			}
			while (finger2.postPostOrderNumber < finger1.postPostOrderNumber) {
				// An edge is visited
				edgesVisited++;

				finger2 = finger2.postDominatorList.getLast();
			}
		}
		return finger1;
	}

	/**
	 * Performs the post dominance frontier analysis.
	 */
	private void postDominanceFrontierAnalysis() {
		BitSet out = new BitSet(edges.size());
		for (Edge e : edges) {
			// An edge is visited
			edgesVisited++;

			out.clear();
			out.or(outgoing[e.tgt.getId()]);
			if (out.cardinality() >= 2) {
				for (int i = out.nextSetBit(0); i >= 0; i = out
						.nextSetBit(i + 1)) {
					// An edge is visited
					edgesVisited++;

					Edge runner = edges.get(i);
					while (runner.id != e.postDominatorList.getLast().id) {
						// An edge is visited
						edgesVisited++;

						runner.postDominanceFrontierSet.set(e.id);
						runner = runner.postDominatorList.getLast();
					}
				}
			}
		}
	}

	/**
	 * In post reverse post order, determine all post dominators of each edge.
	 */
	private void allPostDominatorsAnalysis() {
		BitSet visited = new BitSet(edges.size());

		// An edge is visited
		edgesVisited++;

		int endEdgeId = incoming[graph.getEnd().getId()].nextSetBit(0);
		visited.set(endEdgeId);
		edges.get(endEdgeId).postDominatorSet.set(endEdgeId);

		for (Edge e : postReversePostOrder) {
			if (!visited.get(e.id))
				getAllPostDominators(e, visited);
		}
	}

	/**
	 * Returns all post dominators for a single edge (with depth-first search).
	 * 
	 * @param e
	 *            The edge for which the post dominators should be determined.
	 * @param visited
	 *            The already visited edges.
	 * @return The post dominator set of this edge.
	 */
	private BitSet getAllPostDominators(Edge e, BitSet visited) {
		if (!visited.get(e.id)) {
			// An edge is visited
			edgesVisited++;

			visited.set(e.id);
			// Get the immediate post dominator
			Edge immediate = e.postDominatorList.getLast();
			e.postDominatorSet.or(getAllPostDominators(immediate, visited));
			e.postDominatorSet.set(e.id);
		}
		return e.postDominatorSet;
	}

	/**
	 * Produce a list of edges in post reverse post order.
	 */
	private void postReversePostorder() {
		BitSet visited = new BitSet(edges.size());

		// An edge is visited
		edgesVisited++;

		BitSet in = (BitSet) incoming[graph.getEnd().getId()];
		for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
			postDepthFirstSearch(edges.get(i), visited);
		}
	}

	/**
	 * Perform a simple depth first search to produce the post reverse post
	 * order.
	 * 
	 * @param e
	 *            The current edge of the search.
	 * @param visited
	 *            The visited edges.
	 */
	private void postDepthFirstSearch(Edge e, BitSet visited) {
		// An edge is visited
		edgesVisited++;

		visited.set(e.id);
		BitSet in = (BitSet) (incoming[e.src.getId()]).clone();
		in.andNot(visited);
		for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
			postDepthFirstSearch(edges.get(i), visited);
		}
		e.postPostOrderNumber = postReversePostOrder.size();
		postReversePostOrder.addFirst(e);
	}
}