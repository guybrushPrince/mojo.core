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
	 * The transformed network nodes.
	 */
	private final NetworkNode[] nodes;

	/**
	 * The transformed network edges.
	 */
	private final NetworkEdge[] edges;

	/**
	 * The temporary network nodes.
	 */
	private NetworkNode[] tmpNodes;

	/**
	 * The temporary network edges.
	 */
	private NetworkEdge[] tmpEdges;

	/**
	 * Temporary incoming edges sets for network nodes.
	 */
	private BitSet[] incoming;

	/**
	 * Temporary outgoing edges sets for network nodes.
	 */
	private BitSet[] outgoing;

	/**
	 * The current network source.
	 */
	private NetworkNode flowSource;

	/**
	 * The current network sink.
	 */
	private NetworkNode flowSink;

	/**
	 * A list of paths of the last result;
	 */
	private List<BitSet> lastResult;

	/**
	 * A counter for visited network edges.
	 */
	private int visitedEdges = 0;

	/**
	 * Stores the sync edges which had been already checked.
	 */
	private BitSet checked;

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
	public NetworkGraph(WorkflowGraph graph, WGNode[] nodeMap,
			AnalysisInformation information) {
		super(graph, nodeMap, information);
		this.nodes = new NetworkNode[nodeMap.length];
		this.edges = new NetworkEdge[graph.getEdges().size()];
		initialize();
	}

	/**
	 * Initializes the network with the workflow graph.
	 */
	private void initialize() {
		// Copy the workflow graph nodes
		for (WGNode node : this.map) {
			visitedEdges++;
			// Create a new network node
			if (node != null) {
				nodes[node.getId()] = new NetworkNode(node.getId());
			}
		}
		List<Edge> wgEdges = graph.getEdges();
		// Copy the workflow graph edges
		for (Edge edge : wgEdges) {
			visitedEdges++;
			// Create a new network edge
			edges[edge.id] = new NetworkEdge(edge.id, nodes[edge.src.getId()],
					nodes[edge.tgt.getId()]);
		}
	}

	@Override
	protected List<Annotation> analyze() {
		lastResult = maxFlowAnalysis();
		for (BitSet path : lastResult) {
			for (int p = path.nextSetBit(0); p >= 0; p = path.nextSetBit(p + 1)) {
				NetworkEdge edge = tmpEdges[p];
				if (edge.origin != null) {
					path.clear(p);
					path.set(edge.origin.id);
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
		// Clear the lists
		tmpNodes = new NetworkNode[nodes.length + fork.getSuccessors().size()
				+ 1];
		tmpEdges = new NetworkEdge[edges.length + fork.getSuccessors().size()
				* 2];

		System.arraycopy(nodes, 0, tmpNodes, 0, nodes.length);
		System.arraycopy(edges, 0, tmpEdges, 0, edges.length);

		// Clear the already checked set
		checked = new BitSet(edges.length);

		//
		// STEP 1:
		//

		visitedEdges++;

		// Define two counters.
		int nodeCounter = nodes.length;
		int edgeCounter = edges.length;

		// Get the network node
		NetworkNode nFork = nodes[fork.getId()];
		// Copy the network fork
		NetworkNode cFork = new NetworkNode(nodeCounter);
		cFork.originNode = nFork;
		// Later, we have to remove this node
		tmpNodes[nodeCounter++] = cFork;

		// Create a node for each outgoing node of
		// the fork
		BitSet outgoing = graph.getOutgoingEdges()[fork.getId()];
		for (int o = outgoing.nextSetBit(0); o >= 0; o = outgoing
				.nextSetBit(o + 1)) {
			visitedEdges += 3;
			NetworkNode mNode = new NetworkNode(nodeCounter);
			mNode.originEdge = edges[o];
			tmpNodes[nodeCounter++] = mNode;

			//
			// STEP 3:
			//
			// Get the outgoing edge
			NetworkEdge out = edges[o];
			// Create a copy
			NetworkEdge cout = new NetworkEdge(o, mNode, out.tgt);
			cout.origin = out;
			// Replace
			tmpEdges[o] = cout;

			//
			// STEP 4:
			//
			// Create two new edges
			NetworkEdge oldForkMerge = new NetworkEdge(edgeCounter, nFork,
					mNode);
			oldForkMerge.origin = out;
			tmpEdges[edgeCounter++] = oldForkMerge;
			NetworkEdge newForkMerge = new NetworkEdge(edgeCounter, cFork,
					mNode);
			newForkMerge.origin = out;
			tmpEdges[edgeCounter++] = newForkMerge;
		}

		//
		// STEP 2:
		//
		// Get the incoming edge of the fork
		NetworkEdge inEdge = edges[graph.getIncomingEdges()[fork.getId()]
				.nextSetBit(0)];
		// Create a copy to the new fork copy
		NetworkEdge cinEdge = new NetworkEdge(inEdge.id, inEdge.src, cFork);
		cinEdge.origin = inEdge;
		// Replace
		// tmpEdges.add(cinEdge);
		tmpEdges[inEdge.id] = cinEdge;

		// Build incoming and outgoing sets
		this.incoming = new BitSet[tmpNodes.length];
		this.outgoing = new BitSet[tmpNodes.length];

		for (NetworkEdge edge : tmpEdges) {
			visitedEdges++;
			if (edge == null)
				continue;
			NetworkNode src = edge.src;
			NetworkNode tgt = edge.tgt;

			BitSet srcOut = this.outgoing[src.id];
			if (srcOut == null) {
				srcOut = new BitSet(tmpEdges.length);
				this.outgoing[src.id] = srcOut;
				this.incoming[src.id] = new BitSet(tmpEdges.length);
			}

			BitSet tgtIn = this.incoming[tgt.id];
			if (tgtIn == null) {
				tgtIn = new BitSet(tmpEdges.length);
				this.incoming[tgt.id] = tgtIn;
				this.outgoing[tgt.id] = new BitSet(tmpEdges.length);
			}

			srcOut.set(edge.id);
			tgtIn.set(edge.id);
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
		this.flowSource = tmpNodes[fork.getId()];

		// Set the source of the sync edge as flow sink
		this.flowSink = tmpEdges[sync.id].src;

		//
		// Step 6:
		//
		// Initialize
		for (NetworkEdge edge : tmpEdges) {
			visitedEdges++;
			if (edge != null) {
				edge.setCapacity(0);
				edge.setFlow(0);
			}
		}

		// Recursively set the capacity
		BitSet visited = new BitSet(tmpEdges.length);
		// Set the capacities not for the dependent outgoing edges of join nodes.
		BitSet not = (BitSet) sync.dependent.clone();
		not.clear(sync.id);
		for (NetworkEdge edge : tmpEdges) {
			visitedEdges++;
			if (edge.origin != null && not.get(edge.origin.id)) {
				not.set(edge.id);
			}
		}

		// Set the capacities
		setCapacities(tmpEdges[sync.id], not, visited);
		tmpEdges[sync.id].setCapacity(0);

		return true;
	}

	/**
	 * Sets the capacities recursively.
	 * 
	 * @param current
	 *            The current network edge.
	 * @param not
	 *            The edges which should not be visited.
	 * @param visited
	 *            A set of already visited edges.
	 */
	private void setCapacities(NetworkEdge current, BitSet not, BitSet visited) {
		// Mark as visited
		visited.set(current.id);

		visitedEdges++;

		// Set the capacity to 1
		current.setCapacity(1);

		// Visit the predecessor edges
		// Get the source node
		NetworkNode node = current.src;

		// Get the incoming bit sets
		BitSet incoming = (BitSet) this.incoming[node.id].clone();
		incoming.andNot(visited);
		incoming.andNot(not);
		for (int in = incoming.nextSetBit(0); in >= 0; in = incoming
				.nextSetBit(in + 1)) {
			// Each incoming edge of an old node is also an old
			// edge
			setCapacities(tmpEdges[in], not, visited);
		}
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
		ArrayList<NetworkNode> nodeList = new ArrayList<NetworkNode>();
		nodeList.add(flowSource);

		// Create an used set
		BitSet used = new BitSet(tmpEdges.length);

		// Create an active edges set
		BitSet label = new BitSet(tmpNodes.length);
		label.set(flowSource.id);

		NetworkEdge[] predEdge = new NetworkEdge[tmpNodes.length];

		boolean stop = false;
		while (!nodeList.isEmpty()) {
			// Get the first of the list
			NetworkNode current = nodeList.remove(0);

			BitSet inout = (BitSet) incoming[current.id].clone();
			inout.or(outgoing[current.id]);

			// For all in and outgoing edges
			for (int io = inout.nextSetBit(0); io >= 0; io = inout
					.nextSetBit(io + 1)) {

				// If it is not already used
				if (!used.get(io)) {

					// Set used
					used.set(io);

					NetworkEdge e = tmpEdges[io];

					// Is there a free capacity?
					if ((!label.get(e.tgt.id) && e.currentFlow < e.capacity)
							|| (!label.get(e.src.id) && e.currentFlow > 0)) {

						// Yes, then add it.

						NetworkNode n;
						if (!label.get(e.tgt.id)) {
							n = e.tgt;
						} else {
							n = e.src;
						}

						label.set(n.id);
						nodeList.add(n);

						// Remember the predecessor edge, which
						// we have visited.
						predEdge[n.id] = e;

						// If we have reached the sink, we have
						// find a path.
						if (n.id == this.flowSink.id) {
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
		if (predEdge[flowSink.id] != null) {
			NetworkNode current = flowSink;

			while (predEdge[current.id] != null) {
				NetworkEdge e = predEdge[current.id];
				path.set(e.id);
				if (e.tgt.id == current.id) {
					e.addFlow(e.capacity);
					current = e.src;
				} else {
					e.addFlow((-1) * e.capacity);
					current = e.tgt;
				}
			}

			return path;
		} else {
			return null;
		}
	}

	/**
	 * A node of the network.
	 * 
	 * @author Dipl.-Inf. Thomas M. Prinz
	 * 
	 */
	private class NetworkNode {

		/**
		 * The id of the node.
		 */
		protected final int id;

		/**
		 * The origin node (if there is one)
		 */
		protected NetworkNode originNode = null;

		/**
		 * The origin edge (if there is one)
		 */
		protected NetworkEdge originEdge = null;

		/**
		 * The constructor.
		 * 
		 * @param id
		 *            The id.
		 */
		public NetworkNode(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "N" + id
					+ (originNode == null ? "" : " oN: " + originNode.id)
					+ (originEdge == null ? "" : " oE: " + originEdge.id);
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
		protected final NetworkNode src;

		/**
		 * The target network node
		 */
		protected final NetworkNode tgt;

		/**
		 * The origin of the edge (if there is one)
		 */
		protected NetworkEdge origin = null;

		/**
		 * Information for the max flow algorithm: The current flow
		 */
		private int currentFlow = 0;

		/**
		 * Information for the max flow algorithm: The capacity of the edge
		 */
		private int capacity = 0;

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
		public NetworkEdge(int id, NetworkNode src, NetworkNode tgt) {
			this.id = id;
			this.src = src;
			this.tgt = tgt;
		}

		/**
		 * Sets the flow.
		 * 
		 * @param f
		 *            The current flow.
		 */
		public void setFlow(int f) {
			this.currentFlow = f;
		}

		/**
		 * Adds the flow to the current flow
		 * 
		 * @param f
		 *            The flow.
		 */
		public void addFlow(int f) {
			this.currentFlow += f;
		}

		/**
		 * Sets the capacity of the edge
		 * 
		 * @param c
		 *            The capacity.
		 */
		public void setCapacity(int c) {
			this.capacity = c;
		}

		@Override
		public String toString() {
			return "E" + id + "(" + src.id + " -> " + tgt.id + ",("
					+ this.currentFlow + "," + this.capacity + ")"
					+ (origin == null ? "" : "," + origin.id) + ")";
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Network graph (" + flowSource + ", " + flowSink + ")"
				+ "\n");
		for (NetworkNode node : tmpNodes) {
			builder.append("\t" + node + "\n");
		}
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
