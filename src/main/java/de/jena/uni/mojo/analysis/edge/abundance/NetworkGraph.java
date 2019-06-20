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
package de.jena.uni.mojo.analysis.edge.abundance;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * The network graph class is both - a graph model for networks as well as an
 * analysis. This class allows multiple analyses of one workflow graph and
 * multiple fork nodes.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class NetworkGraph extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6776142263615374573L;

	/**
	 * The transformed network edges.
	 */
	private final NetworkEdge[] edges;

	/**
	 * The temporary network edges.
	 */
	private final NetworkEdge[] tmpEdges;

	/**
	 * Temporary incoming edges sets for network nodes.
	 */
	private final BitSet[] incoming;

	/**
	 * Temporary outgoing edges sets for network nodes.
	 */
	private final BitSet[] outgoing;

	/**
	 * The current network source.
	 */
	private int flowSource;

	/**
	 * The current network sink.
	 */
	private int flowSink;

	/**
	 * A list of paths of the last result;
	 */
	private List<BitSet> lastResult;

	/**
	 * A counter for visited network edges.
	 */
	private int visitedEdges = 0;

	/**
	 * Stores the meeting points which had been already checked.
	 */
	private BitSet checked;

	/**
	 * The maximum number of temporary edges
	 */
	private final int maxAdditionalEdges;

	/**
	 * The current number of virtual edges.
	 */
	private int virtualNumberEdges;

	/**
	 * The current number of virtual nodes.
	 */
	private int virtualNumberNodes;

	/**
	 * A bit set representing the capacities of the edges (false is 0, true is
	 * 1)
	 */
	private final BitSet capacities;

	/**
	 * A bit set representing the current flow (false is 0, true is 1)
	 */
	private final BitSet currentFlow;

	/**
	 * A bit set storing the edges, which were replaced.
	 */
	private final BitSet replaced;

	/**
	 * Mapping between virtual and original edges.
	 */
	private final HashMap<Integer, NetworkEdge> virtualEdgeOrigin = new HashMap<>();
	
	/**
	 * Incoming edges sets for all join nodes.
	 */
	private final BitSet incomingJoinNodes;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param nodeMap
	 *            The map of nodes.
	 * @param information
	 *            The information for analysis.
	 */
	public NetworkGraph(WorkflowGraph graph, WGNode[] nodeMap, AnalysisInformation information) {
		super(graph, nodeMap, information);
		this.edges = new NetworkEdge[graph.getEdges().size()];
		this.replaced = new BitSet(this.edges.length);

		int max = 0;
		BitSet forkSet = (BitSet) graph.getForkSet().clone();
		forkSet.or(graph.getOrForkSet());
		BitSet[] outgoing = graph.getOutgoingEdges();
		// Determine the maximum number of outgoing edges of forks
		for (int f = forkSet.nextSetBit(0); f >= 0; f = forkSet.nextSetBit(f + 1)) {
			max = Math.max(outgoing[f].cardinality(), max);
		}
		this.maxAdditionalEdges = edges.length + max * 2;
		this.tmpEdges = new NetworkEdge[maxAdditionalEdges];
		int maxAdditionalNodes = nodeMap.length + max + 1;
		this.incoming = new BitSet[maxAdditionalNodes];
		this.outgoing = new BitSet[maxAdditionalNodes];
		this.incomingJoinNodes = new BitSet(maxAdditionalNodes);

		this.capacities = new BitSet(maxAdditionalEdges);
		this.currentFlow = new BitSet(maxAdditionalEdges);

		initialize();
	}

	/**
	 * Initializes the network with the workflow graph.
	 */
	private void initialize() {
		List<Edge> wgEdges = graph.getEdges();
		// Copy the workflow graph edges
		for (Edge edge : wgEdges) {
			visitedEdges++;
			// Create a new network edge
			NetworkEdge tmp = new NetworkEdge(edge.id, edge.src.getId(), edge.tgt.getId());
			edges[edge.id] = tmp;
			tmpEdges[edge.id] = tmp;
		}
	}

	@Override
	protected List<Annotation> analyze() {
		lastResult = maxFlowAnalysis();
		for (BitSet path : lastResult) {
			for (int p = path.nextSetBit(0); p >= 0; p = path.nextSetBit(p + 1)) {
				NetworkEdge edge = tmpEdges[p];
				NetworkEdge origin = this.virtualEdgeOrigin.get(edge);
				if (origin != null) {
					path.clear(p);
					path.set(origin.id);
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Transforms the network with regard to the given fork node.
	 * 
	 * @param fork
	 *            The fork node.
	 */
	public void transformFor(WGNode fork) {
		// Clear the virtual edges.
		for (int e = edges.length; e < this.virtualNumberEdges - 1; e++) {
			tmpEdges[e] = null;
		}
		// Reset the replaced edges.
		for (int r = replaced.nextSetBit(0); r >= 0; r = replaced.nextSetBit(r + 1)) {
			tmpEdges[r] = edges[r];
		}
		// Clear the replaced set
		replaced.clear();
		// Clear the already checked set
		checked = new BitSet(edges.length);
		// Clear the virtual edges mapping
		this.virtualEdgeOrigin.clear();

		//
		// STEP 1:
		//
		visitedEdges++;

		// Define two counters.
		int nodeCounter = this.map.length;
		int edgeCounter = edges.length;

		// Get the network node
		int nFork = fork.getId();
		// Copy the network fork
		int cFork = nodeCounter++;

		// Create a node for each outgoing node of
		// the fork
		BitSet outgoing = graph.getOutgoingEdges()[fork.getId()];
		for (int o = outgoing.nextSetBit(0); o >= 0; o = outgoing.nextSetBit(o + 1)) {
			visitedEdges += 3;
			int mNode = nodeCounter++;

			//
			// STEP 3:
			//
			// Get the outgoing edge
			NetworkEdge out = edges[o];
			// Create a copy
			NetworkEdge cout = new NetworkEdge(o, mNode, out.tgt);
			// cout.origin = out;
			this.virtualEdgeOrigin.put(cout.id, out);
			// Replace
			tmpEdges[o] = cout;
			this.replaced.set(o);

			//
			// STEP 4:
			//
			// Create two new edges
			NetworkEdge oldForkMerge = new NetworkEdge(edgeCounter, nFork, mNode);
			// oldForkMerge.origin = out;
			this.virtualEdgeOrigin.put(oldForkMerge.id, out);
			tmpEdges[edgeCounter++] = oldForkMerge;
			NetworkEdge newForkMerge = new NetworkEdge(edgeCounter, cFork, mNode);
			// newForkMerge.origin = out;
			this.virtualEdgeOrigin.put(newForkMerge.id, out);
			tmpEdges[edgeCounter++] = newForkMerge;
		}

		this.virtualNumberNodes = nodeCounter + 1;
		this.virtualNumberEdges = edgeCounter + 1;

		//
		// STEP 2:
		//
		// Get the incoming edge of the fork
		NetworkEdge inEdge = edges[graph.getIncomingEdges()[fork.getId()].nextSetBit(0)];
		// Create a copy to the new fork copy
		NetworkEdge cinEdge = new NetworkEdge(inEdge.id, inEdge.src, cFork);
		this.virtualEdgeOrigin.put(cinEdge.id, inEdge);
		// Replace
		// tmpEdges.add(cinEdge);
		tmpEdges[inEdge.id] = cinEdge;
		replaced.set(inEdge.id);

		for (int i = 0; i < this.incoming.length; i++) {
			if (this.incoming[i] != null)
				this.incoming[i].clear();
			if (this.outgoing[i] != null)
				this.outgoing[i].clear();
		}
		
		this.incomingJoinNodes.clear();

		// Build incoming and outgoing sets
		for (NetworkEdge edge : tmpEdges) {
			visitedEdges++;
			if (edge == null)
				continue;
			int src = edge.src;
			int tgt = edge.tgt;

			BitSet srcOut = this.outgoing[src];
			if (srcOut == null) {
				srcOut = new BitSet(this.virtualNumberEdges);
				this.outgoing[src] = srcOut;
				this.incoming[src] = new BitSet(this.virtualNumberEdges);
			}

			BitSet tgtIn = this.incoming[tgt];
			if (tgtIn == null) {
				tgtIn = new BitSet(this.virtualNumberEdges);
				this.incoming[tgt] = tgtIn;
				this.outgoing[tgt] = new BitSet(this.virtualNumberEdges);
			}

			srcOut.set(edge.id);
			tgtIn.set(edge.id);
			
			if (graph.getJoinSet().get(tgt)) this.incomingJoinNodes.set(edge.id);
		}
	}

	/**
	 * Calculates the capacities of the edges with regard to the source (fork)
	 * and sink (sync).
	 * 
	 * @param fork
	 *            The source of the network.
	 * @param sync
	 *            The sink of the network.
	 * @return Whether the sync edge was already checked or not
	 */
	public boolean setCapacities(WGNode fork, Edge sync) {
		if (checked.get(sync.id)) {
			return false;
		}
		checked.set(sync.id);

		//
		// Step 5:
		//
		// Set the fork as flow source
		this.flowSource = fork.getId();
		Edge inFork = graph.getEdges().get(graph.getIncomingEdges()[fork.getId()].nextSetBit(0));

		// Set the source of the sync edge as flow sink
		this.flowSink = tmpEdges[sync.id].src;

		//
		// Step 6:
		//
		// Initialize
		this.capacities.clear();
		this.currentFlow.clear();

		// Set the capacities not for the outgoing edges of join
		// nodes for which sync is execution edge.
		BitSet not = (BitSet) sync.approxExecutes.clone();
		not.and(incomingJoinNodes);		
		not.clear(sync.id);
		for (NetworkEdge edge : tmpEdges) {
			visitedEdges++;
			NetworkEdge origin = this.virtualEdgeOrigin.get(edge);
			if (edge != null && origin != null && not.get(origin.id)) {
				not.set(edge.id);
			}
		}

		// Set the capacities
		this.capacities.set(0, this.virtualNumberEdges);
		this.capacities.andNot(not);
		this.capacities.clear(sync.id);
		
		this.capacities.and(inFork.bond);
		this.capacities.set(inFork.id);
		// Clear the virtual edges.
		for (int e = edges.length; e < this.virtualNumberEdges - 1; e++) {
			this.capacities.set(tmpEdges[e].id);
		}

		return true;
	}

	/**
	 * Determine the max flow and therefore the paths.
	 * 
	 * @return a list of paths
	 */
	public List<BitSet> maxFlowAnalysis() {
		// Create a list of paths.
		List<BitSet> paths = new ArrayList<BitSet>();
		BitSet path;
		do {
			// Determine a new path.
			path = determinePath();
			if (path != null) {
				// Take a look if there is an intersection
				// with another path. Then we have to delete
				// the old one.
				for (int i = 0; i < paths.size(); i++) {
					BitSet oldPath = (BitSet) paths.get(i).clone();
					oldPath.and(path);
					if (!oldPath.isEmpty()) {
						// Two paths are not disjoint
						// Eliminate the old one
						paths.remove(i);
						i--;
					}
				}
				paths.add(path);
			}
		} while (path != null);

		return paths;
	}

	/**
	 * Determines a path from source to target with capacities.
	 * 
	 * @return The path.
	 */
	private BitSet determinePath() {
		// Create a working list
		ArrayList<Integer> nodeList = new ArrayList<Integer>();
		nodeList.add(flowSource);

		// Create an used set
		BitSet used = new BitSet(this.virtualNumberEdges);

		// Create an active edges set
		BitSet label = new BitSet(this.virtualNumberNodes);
		label.set(flowSource);

		NetworkEdge[] predEdge = new NetworkEdge[this.virtualNumberNodes];

		boolean stop = false;
		while (!nodeList.isEmpty()) {
			// Get the first of the list
			int current = nodeList.remove(0);

			BitSet inout = (BitSet) incoming[current].clone();
			inout.or(outgoing[current]);

			// For all in and outgoing edges
			for (int io = inout.nextSetBit(0); io >= 0; io = inout.nextSetBit(io + 1)) {

				// If it is not already used
				if (!used.get(io)) {

					// Set used
					used.set(io);

					NetworkEdge e = tmpEdges[io];

					// Is there a free capacity?
					if ((!label.get(e.tgt) && (currentFlow.get(e.id) ? 1 : 0) < (capacities.get(e.id) ? 1 : 0))
							|| (!label.get(e.src) && currentFlow.get(e.id))) {

						// Yes, then add it.
						int n;
						if (!label.get(e.tgt)) {
							n = e.tgt;
						} else {
							n = e.src;
						}

						label.set(n);
						nodeList.add(n);

						// Remember the predecessor edge, which
						// we have visited.
						predEdge[n] = e;

						// If we have reached the sink, we have
						// find a path.
						if (n == this.flowSink) {
							stop = true;
						}
					}
				}

				if (stop)
					break;

			}

			if (stop)
				break;
		}

		// The path as a bit set.
		BitSet path = new BitSet(edges.length);

		// As long as we find a predecessor
		// of the current node (at start, the sink),
		// we add the edge to the path.
		if (predEdge[flowSink] != null) {
			int current = flowSink;

			while (predEdge[current] != null) {
				NetworkEdge e = predEdge[current];
				path.set(e.id);
				if (e.tgt == current) {
					if (this.capacities.get(e.id))
						currentFlow.set(e.id);
					current = e.src;
				} else {
					if (this.capacities.get(e.id))
						currentFlow.clear(e.id);
					current = e.tgt;
				}
			}

			return path;
		} else {
			return null;
		}
	}

	/**
	 * An edge of the network.
	 * 
	 * @author Dipl.-Inf. Thomas M. Prinz
	 * 
	 */
	private class NetworkEdge {

		/**
		 * The id.
		 */
		protected final int id;

		/**
		 * The source network node
		 */
		protected final int src;

		/**
		 * The target network node
		 */
		protected final int tgt;

		/**
		 * The constructor.
		 * 
		 * @param id
		 *            The id of the edge.
		 * @param src
		 *            The source of the edge.
		 * @param tgt
		 *            The target of the edge.
		 */
		public NetworkEdge(int id, int src, int tgt) {
			this.id = id;
			this.src = src;
			this.tgt = tgt;
		}

		@Override
		public String toString() {
			return "E" + id + "(" + src + " -> " + tgt + ")";
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Network graph (" + flowSource + ", " + flowSink + ")" + "\n");
		for (NetworkEdge edge : tmpEdges) {
			builder.append("\t" + edge + "\n");
		}
		return builder.toString();
	}

	/**
	 * @return the lastResult
	 */
	public List<BitSet> getLastResult() {
		return lastResult;
	}

	/**
	 * @return the visited edges
	 */
	public int getVisitedEdges() {
		return visitedEdges;
	}
}
