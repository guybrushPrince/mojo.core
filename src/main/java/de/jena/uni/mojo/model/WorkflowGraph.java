/**
 * Copyright 2013 mojo Friedrich Schiller University Jena
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
package de.jena.uni.mojo.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


import de.jena.uni.mojo.analysis.edge.Edge;
import de.jena.uni.mojo.info.IPluginStrings;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * Class representing one single workflow graph that can be analyzed.
 * 
 * @author Norbert Spiess
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class WorkflowGraph {

	/**
	 * The start node of this workflow graph.
	 */
	private WGNode start;

	/**
	 * The end node of this workflow graph.
	 */
	private WGNode end;

	/**
	 * A list of all the nodes of the graph.
	 */
	private final List<WGNode> nodeList = new ArrayList<>();

	/**
	 * A bitset of all the nodes within the graph. For performance issues.
	 */
	private final BitSet nodeSet = new BitSet();

	/**
	 * A list of the fork nodes of the graph.
	 */
	private final List<WGNode> forkList = new ArrayList<>();

	/**
	 * A bitset of the fork nodes within the graph. For performance issues.
	 */
	private final BitSet forkSet = new BitSet();

	/**
	 * A list of the join nodes of the graph.
	 */
	private final List<WGNode> joinList = new ArrayList<>();

	/**
	 * A bitset of the join nodes within the graph. For performance issues.
	 */
	private final BitSet joinSet = new BitSet();

	/**
	 * A list of the split nodes of the graph.
	 */
	private final List<WGNode> splitList = new ArrayList<>();

	/**
	 * A bitset of the split nodes within the graph. For performance issues.
	 */
	private final BitSet splitSet = new BitSet();

	/**
	 * A list of the merge nodes of the graph.
	 */
	private final List<WGNode> mergeList = new ArrayList<>();

	/**
	 * A bitset of the merge nodes within the graph. For performance issues.
	 */
	private final BitSet mergeSet = new BitSet();

	/**
	 * A list of the or-fork nodes of the graph.
	 */
	private final List<WGNode> orForkList = new ArrayList<>();

	/**
	 * A bitset of the or-fork nodes within the graph. For performance issues.
	 */
	private final BitSet orForkSet = new BitSet();

	/**
	 * A list of the or-join nodes of the graph.
	 */
	private final List<WGNode> orJoinList = new ArrayList<>();

	/**
	 * A bitset of the or-join nodes within the graph. For performance issues.
	 */
	private final BitSet orJoinSet = new BitSet();

	/**
	 * A list of the task/activity nodes of the graph.
	 */
	private final List<WGNode> activityList = new ArrayList<>();

	/**
	 * A bitset of the task/activity nodes within the graph. For performance
	 * issues.
	 */
	private final BitSet activitySet = new BitSet();

	/**
	 * A list of all edges within the workflow graph.
	 */
	private List<Edge> edges;

	/**
	 * An array of bit sets where each bit set contains incoming edges of the
	 * node with the id of the position in the array.
	 */
	private BitSet[] incoming;

	/**
	 * An array of bit sets where each bit set contains outgoing edges of the
	 * node with the id of the position in the array.
	 */
	private BitSet[] outgoing;

	/**
	 * Whether this workflow graph is closed (final) or can be modified.
	 */
	private boolean closed = false;

	/**
	 * Get a list holding references to all nodes of type {@link Type#Fork} of
	 * this graph.
	 * 
	 * @return A list of all fork nodes.
	 */
	public List<WGNode> getForkList() {
		return forkList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#FORK} stored in this graph.
	 * 
	 * @return A bitset of all fork nodes.
	 */
	public BitSet getForkSet() {
		return forkSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#JOIN} of
	 * this graph.
	 * 
	 * @return A list of all join nodes.
	 */
	public List<WGNode> getJoinList() {
		return joinList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#JOIN} stored in this graph.
	 * 
	 * @return A bitset of all join nodes.
	 */
	public BitSet getJoinSet() {
		return joinSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#SPLIT} of
	 * this graph.
	 * 
	 * @return A list of all split nodes.
	 */
	public List<WGNode> getSplitList() {
		return splitList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#SPLIT} stored in this graph.
	 * 
	 * @return A bitset of all split nodes.
	 */
	public BitSet getSplitSet() {
		return splitSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#MERGE} of
	 * this graph.
	 * 
	 * @return A list of all merge nodes.
	 */
	public List<WGNode> getMergeList() {
		return mergeList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#MERGE} stored in this graph.
	 * 
	 * @return A bitset of all merge nodes.
	 */
	public BitSet getMergeSet() {
		return mergeSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#OR_FORK}
	 * of this graph.
	 * 
	 * @return A list of all or-fork nodes.
	 */
	public List<WGNode> getOrForkList() {
		return orForkList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#OR_FORK} stored in this graph.
	 * 
	 * @return A bitset of all or-fork nodes.
	 */
	public BitSet getOrForkSet() {
		return orForkSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#OR_JOIN}
	 * of this graph.
	 * 
	 * @return A list of all or-join nodes.
	 */
	public List<WGNode> getOrJoinList() {
		return orJoinList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#OR_JOIN} stored in this graph.
	 * 
	 * @return A bitset of all or-join nodes.
	 */
	public BitSet getOrJoinSet() {
		return orJoinSet;
	}

	/**
	 * Get a list holding references to all nodes of type {@link Type#ACTIVITY}
	 * of this graph.
	 * 
	 * @return A list of all task/activity nodes.
	 */
	public List<WGNode> getActivityList() {
		return activityList;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes of type
	 * {@link Type#ACTIVITY} stored in this graph.
	 * 
	 * @return A bitset of all task/activity nodes.
	 */
	public BitSet getActivitySet() {
		return activitySet;
	}

	/**
	 * Set the start node of this graph.
	 * 
	 * @param start
	 *            The start node.
	 */
	public void setStart(final WGNode start) {
		this.start = start;
	}

	/**
	 * Set the end node of this graph.
	 * 
	 * @param end
	 *            The end node.
	 */
	public void setEnd(final WGNode end) {
		this.end = end;
	}

	/**
	 * Add a node to the graph. The type is not allowed to be
	 * {@link Type#UNDEFINED}, {@link Type#START} or {@link Type#END}.
	 * 
	 * @param node
	 *            The node that should be added to the workflow graph.
	 * @throws IllegalArgumentException
	 *             if type of the node is of {@link Type#UNDEFINED},
	 *             {@link Type#START} or {@link Type#END}.
	 * @throws RuntimeException
	 *             if there is already a node with the same id within the
	 *             workflow graph.
	 */
	public void addNode(final WGNode node) throws IllegalArgumentException,
			RuntimeException {
		final int nodeId = node.getId();

		if (nodeSet.get(nodeId) || closed)
			throw new RuntimeException();

		addToFittingListAndSet(node, node.getType(), nodeId);
		nodeList.add(node);
		nodeSet.set(nodeId);
	}

	/**
	 * Remove a node from the graph. The type is not allowed to be
	 * {@link Type#UNDEFINED}, {@link Type#START} or {@link Type#END}.
	 * 
	 * @param node
	 *            The node which should be removed from the workflow graph.
	 * @throws IllegalArgumentException
	 *             if type of the node is of {@link Type#UNDEFINED},
	 *             {@link Type#START} or {@link Type#END}.
	 */
	public void removeNode(final WGNode node) throws IllegalArgumentException,
			RuntimeException {
		if (closed) {
			throw new RuntimeException();
		}

		final int nodeId = node.getId();
		removeFromFittingListAndSet(node, node.getType(), nodeId);
		nodeList.remove(node);
		nodeSet.clear(nodeId);
	}

	/**
	 * Sorts the given node into the right type list.
	 * 
	 * @param node
	 *            The node.
	 * @param type
	 *            The type of the node.
	 * @param nodeId
	 *            The id of the node.
	 * @throws IllegalArgumentException
	 *             If the type of the workflow graph node is unknown.
	 */
	private void addToFittingListAndSet(final WGNode node, final Type type,
			final int nodeId) throws IllegalArgumentException {

		switch (type) {
		case FORK:
			forkList.add(node);
			forkSet.set(nodeId);
			break;
		case JOIN:
			joinList.add(node);
			joinSet.set(nodeId);
			break;
		case SPLIT:
			splitList.add(node);
			splitSet.set(nodeId);
			break;
		case MERGE:
			mergeList.add(node);
			mergeSet.set(nodeId);
			break;
		case OR_FORK:
			orForkList.add(node);
			orForkSet.set(nodeId);
			break;
		case OR_JOIN:
			orJoinList.add(node);
			orJoinSet.set(nodeId);
			break;
		case ACTIVITY:
			activityList.add(node);
			activitySet.set(nodeId);
			break;
		default:
			throw new IllegalArgumentException(IPluginStrings.UNEXPECTED_TYPE
					+ type);
		}
	}

	/**
	 * Removes the given node from its type-specific list.
	 * 
	 * @param node
	 *            The node that should be removed.
	 * @param type
	 *            The type of the node.
	 * @param nodeId
	 *            The id of the node.
	 * @throws IllegalArgumentException
	 *             if the type of the node is unknown.
	 */
	private void removeFromFittingListAndSet(final WGNode node,
			final Type type, final int nodeId) throws IllegalArgumentException {

		switch (type) {
		case FORK:
			forkList.remove(node);
			forkSet.clear(nodeId);
			break;
		case JOIN:
			joinList.remove(node);
			joinSet.clear(nodeId);
			break;
		case SPLIT:
			splitList.remove(node);
			splitSet.clear(nodeId);
			break;
		case MERGE:
			mergeList.remove(node);
			mergeSet.clear(nodeId);
			break;
		case OR_FORK:
			orForkList.remove(node);
			orForkSet.clear(nodeId);
			break;
		case OR_JOIN:
			orJoinList.remove(node);
			orJoinSet.clear(nodeId);
			break;
		case ACTIVITY:
			activityList.remove(node);
			activitySet.clear(nodeId);
			break;
		default:
			throw new IllegalArgumentException(IPluginStrings.UNEXPECTED_TYPE
					+ type);

		}
	}

	/**
	 * Get the start node.
	 * 
	 * @return The start node.
	 */
	public WGNode getStart() {
		return start;
	}

	/**
	 * Get the end node.
	 * 
	 * @return The end node.
	 */
	public WGNode getEnd() {
		return end;
	}

	/**
	 * Get a list holding references to all nodes exclusive start and end node
	 * of this graph.
	 * 
	 * @return A list with all nodes exclusive the start and end node.
	 */
	public List<WGNode> getNodeList() {
		return nodeList;
	}

	/**
	 * Get a list holding references to all nodes inclusive start and end node
	 * of this graph.
	 * 
	 * @return A list with all nodes inclusive the start and the end node.
	 */
	public List<WGNode> getNodeListInclusive() {
		List<WGNode> nodes = new ArrayList<WGNode>(nodeList);
		nodes.add(start);
		nodes.add(end);
		return nodes;
	}

	/**
	 * Get a BitSet holding bits representing the ids of all the nodes stored in
	 * this graph (without the start and end node).
	 * 
	 * @return A bit set of all nodes of this graph.
	 */
	public BitSet getNodeSet() {
		return nodeSet;
	}

	/**
	 * Closes the workflow graph so that further changes are not allowed.
	 * 
	 * @param edges
	 *            A list of edges.
	 * @param in
	 *            An array with incoming edges for each node as bit set.
	 * @param out
	 *            An array with outgoing edges for each node as bit set.
	 * 
	 * @throws RuntimeException
	 */
	public void close(List<Edge> edges, BitSet[] in, BitSet[] out)
			throws RuntimeException {

		if (this.closed)
			throw new RuntimeException();

		this.edges = edges;
		this.incoming = in;
		this.outgoing = out;
		this.closed = true;
	}

	/**
	 * Determines whether this workflow graph is closed or not.
	 * 
	 * @return True or false.
	 */
	public boolean isClosed() {
		return this.closed;
	}

	/**
	 * @return the edges of the graph
	 */
	public List<Edge> getEdges() {
		return edges;
	}

	/**
	 * @return the incoming edges of each node
	 */
	public BitSet[] getIncomingEdges() {
		return incoming;
	}

	/**
	 * @return the outgoing edges of each node
	 */
	public BitSet[] getOutgoingEdges() {
		return outgoing;
	}

}