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
package de.jena.uni.mojo.analysis.edge.deadlock;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.edge.dominance.PostDominatorEdgeAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.error.DeadlockAnnotation;
import de.jena.uni.mojo.error.DeadlockCycleAnnotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * This analysis finds the causes of potential deadlocks in a worklow graph.
 * Therefore, it makes an approximation of the execution edge relation. Then,
 * based on this relation, it performs a simple data flow analysis that finds
 * paths where no such execution edge lies on.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class DeadlockAnalysis extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6768536215720284136L;

	/**
	 * A constant which defines a variable for the analysis information.
	 */
	public final static String DEADLOCK_NUMBER_VISITED_EDGES = "DEADLOCK_NUMBER_VISITED_EDGES";

	/**
	 * The post dominance edge analysis.
	 */
	private final PostDominatorEdgeAnalysis edgeAnalysis;

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
	public DeadlockAnalysis(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, PostDominatorEdgeAnalysis edgeAnalysis) {
		super(graph, map, reporter);
		this.edgeAnalysis = edgeAnalysis;
		this.edges = edgeAnalysis.edges;
		this.incoming = edgeAnalysis.incoming;
		this.outgoing = edgeAnalysis.outgoing;
	}

	@Override
	protected List<Annotation> analyze() {
		// Determine execution edge approximation
		determineApprExecEdges();

		// Perform the dataflow analysis
		dataflowAnalysis();

		// Get the list of edges
		List<Edge> edges = edgeAnalysis.edges;

		// Create a list of errors.
		List<Annotation> errors = new ArrayList<Annotation>();

		// At first check the start edge
		WGNode start = graph.getStart();
		// Get the start edge
		Edge startEdge = edges.get(outgoing[start.getId()].nextSetBit(0));
		// Get its information
		BitSet information = startEdge.deadlockInformation;
		
		for (int i = information.nextSetBit(0); i >= 0; i = information
				.nextSetBit(i + 1)) {

			// An edge is visited
			edgesVisited++;
			
			reporter.startIgnoreTimeMeasurement(graph, this.getClass().getName());

			// There is a "normal" failure without a path from the
			// start node to the found incoming edge.
			Edge in = edges.get(i);
			// The join node
			WGNode join = in.tgt;
			// Get the outgoing edge of the join
			Edge out = edges.get(outgoing[join.getId()].nextSetBit(0));

			// There is a failure, so we have to add a failure annotation
			// to the workflow
			DeadlockAnnotation annotation = new DeadlockAnnotation(this);

			// Immediate dominator
			Edge iDom = out.dominatorList.getLast();

			// Perform a failure diagnostic
			failureDiagnostic(annotation, join, iDom, out, information);

			errors.add(annotation);
			reporter.add(graph, AnalysisInformation.NUMBER_DEADLOCKS_NORMAL, 1);
			reporter.endIgnoreTimeMeasurement(graph, this.getClass().getName());
		}

		// Now check the join nodes
		for (WGNode join : graph.getJoinList()) {
			// Get its outgoing edge
			Edge outEdge = edges.get(outgoing[join.getId()].nextSetBit(0));

			BitSet income = incoming[join.getId()];
			for (int in = income.nextSetBit(0); in >= 0; in = income
					.nextSetBit(in + 1)) {
				// An edge is visited
				edgesVisited++;

				if (outEdge.deadlockInformation.get(in)) {
					reporter.startIgnoreTimeMeasurement(graph, this.getClass().getName());
					// There is a failure, so we have to add a failure
					// annotation
					// to the workflow
					DeadlockCycleAnnotation annotation = new DeadlockCycleAnnotation(
							this);

					// Perform a failure diagnostic
					failureDiagnostic(annotation, join, outEdge, outEdge,
							outEdge.deadlockInformation);

					outEdge.deadlockInformation.andNot(income);

					errors.add(annotation);
					reporter.add(graph,
							AnalysisInformation.NUMBER_DEADLOCKS_LOOP, 1);
					reporter.endIgnoreTimeMeasurement(graph, this.getClass().getName());
				}
			}
		}

		// Add some information to the analysis reporter.
		reporter.put(graph, AnalysisInformation.NUMBER_DEADLOCKS, errors.size());
		reporter.put(graph, DEADLOCK_NUMBER_VISITED_EDGES, edgesVisited);
		return errors;
	}

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
		edges.set(0, edges.size());

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

					for (int s = allowed.nextSetBit(0); s >= 0; s = allowed
							.nextSetBit(s + 1)) {

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
	private void inverseDepthFirstSearch(int current, int last, BitSet allowed,
			BitSet visited) {
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
			for (int p = predCopy.nextSetBit(0); p >= 0; p = predCopy
					.nextSetBit(p + 1)) {
				inverseDepthFirstSearch(p, last, allowed, visited);
			}
		}
	}

	/**
	 * Determine the paths to the join from the edges which reaches the start or
	 * outgoing edge of the join without passing a appr. exec. edge
	 * 
	 * @param current
	 *            The current edge on path
	 * @param incoming
	 *            The incoming edge of the join node to reach.
	 * @param visited
	 *            The visited edges.
	 * @param splits
	 *            The split and or-splits reached.
	 */
	private void getPathsToJoin(int current, int incoming, BitSet visited,
			List<WGNode> splits) {

		// An edge is visited
		edgesVisited++;

		if (current != incoming) {

			// Get the current edge
			Edge curEdge = edges.get(current);

			// Get the successors of the current edge
			BitSet succCopy = (BitSet) outgoing[curEdge.tgt.getId()].clone();

			// Remove already visited edges
			succCopy.andNot(visited);

			for (int s = succCopy.nextSetBit(0); s >= 0; s = succCopy
					.nextSetBit(s + 1)) {
				// Get the successor edge
				Edge succ = edges.get(s);

				// Add the source if it is a (or-)split
				if (succ.src.getType() == Type.SPLIT
						|| succ.src.getType() == Type.OR_FORK) {

					splits.add(succ.src);
				}

				// If the successor has a path to the incoming edge,
				// then visit it.
				if (succ.deadlockInformation.get(incoming)) {
					// The successor edge is visited
					visited.set(s);

					getPathsToJoin(s, incoming, visited, splits);
				}
			}
		}
	}

	/**
	 * 
	 * @param annotation
	 * @param join
	 * @param from
	 * @param out
	 * @param information
	 */
	private void failureDiagnostic(DeadlockAnnotation annotation, WGNode join,
			Edge from, Edge out, BitSet information) {
		// Determine for each reached incoming edge of this
		// join node a path to the join.
		BitSet incomeJoin = (BitSet) incoming[join.getId()].clone();

		// Regard only the edges which arrive at the start edge.
		incomeJoin.and(information);
		// For each incoming edge calculate the paths
		for (int inEdge = incomeJoin.nextSetBit(0); inEdge >= 0; inEdge = incomeJoin
				.nextSetBit(inEdge + 1)) {

			// Create a new path/visited set
			BitSet vis = new BitSet(incomeJoin.size());
			// Determine split or or-splits
			List<WGNode> splits = new ArrayList<WGNode>();
			// Determine the path to the join
			getPathsToJoin(from.id, inEdge, vis, splits);
			// Add the path to the failure annotation
			annotation.addPathToFailure(vis);

			// Determine each (or-)split whose successors
			// are appr. exec. edges however not its incoming
			// edge
			for (WGNode split : splits) {
				// Get a copy of the outgoing edges
				BitSet outgo = (BitSet) outgoing[split.getId()].clone();
				outgo.and(out.isApproxExecutedBy);

				if (outgo.cardinality() < outgoing[split.getId()].cardinality()) {

					// It is a failure node
					annotation.addFailureNode(split);

				}
			}
		}

		annotation.addPrintableNode(join);
		annotation.addOpeningNode(from.tgt);

		// Delete the other incoming edge of this join from the
		// start edge information to avoid double failures
		information.andNot(incomeJoin);
	}

	/**
	 * Performs a data flow analysis which finds paths from the start edge to
	 * outgoing join edges and paths from the joins to themselves where no
	 * execution edge lies on.
	 */
	private void dataflowAnalysis() {
		// Get the list of edges
		List<Edge> edges = this.edges;

		// Set each set to empty set
		for (Edge e : edges) {
			// An edge is visited
			edgesVisited++;

			e.deadlockInformation.clear();
		}

		// Create a work list
		List<Edge> workingList = new ArrayList<Edge>();

		for (WGNode join : graph.getJoinList()) {
			// Get its incoming edges
			BitSet in = incoming[join.getId()];

			// Set the generate set for each incoming edge.
			for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
				// An edge is visited
				edgesVisited++;

				// Get the edge
				Edge inEdge = edges.get(i);
				// Set the generate
				inEdge.deadlockGen.set(inEdge.id);

				// Kill all incoming edges
				inEdge.deadlockKill.or(in);

				// Since only each incoming edge produces information,
				// we put each incoming edge in the working list
				workingList.add(inEdge);
			}

			// Get the outgoing edge of the join node
			Edge out = edges.get(outgoing[join.getId()].nextSetBit(0));

			// Set the kill set of each appr. execution edge of the
			// outgoing edge of the current join node.
			for (int e = out.isApproxExecutedBy.nextSetBit(0); e >= 0; e = out.isApproxExecutedBy
					.nextSetBit(e + 1)) {

				// An edge is visited
				edgesVisited++;

				edges.get(e).deadlockKill.or(in);
			}
		}

		BitSet in = new BitSet(edges.size());
		BitSet subset = new BitSet(edges.size());
		while (!workingList.isEmpty()) {
			// An edge is visited
			edgesVisited++;

			in.clear();
			subset.clear();

			// Get the first edge of the list
			Edge current = workingList.remove(0);

			// Get the outgoing edges
			BitSet outgoing = this.outgoing[current.tgt.getId()];

			// Build IN information
			for (int j = outgoing.nextSetBit(0); j >= 0; j = outgoing
					.nextSetBit(j + 1)) {
				// An edge is visited
				edgesVisited++;

				in.or(edges.get(j).deadlockInformation);
			}
			// Remove KILL information
			in.andNot(current.deadlockKill);
			// Add GEN information
			in.or(current.deadlockGen);

			subset.or(in);
			subset.andNot(current.deadlockInformation);
			if (!subset.isEmpty()) {
				// Some information has changed
				current.deadlockInformation.or(in);
				// Add outgoing edges
				// Get the outgoing edges
				BitSet incoming = this.incoming[current.src.getId()];
				for (int j = incoming.nextSetBit(0); j >= 0; j = incoming
						.nextSetBit(j + 1)) {
					// An edge is visited
					edgesVisited++;

					workingList.add(edges.get(j));
				}
			}
		}
	}
}
