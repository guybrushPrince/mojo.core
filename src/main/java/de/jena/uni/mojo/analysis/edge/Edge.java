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
package de.jena.uni.mojo.analysis.edge;

import java.util.BitSet;
import java.util.LinkedList;

import de.jena.uni.mojo.model.WGNode;

/**
 * This class presents a simple edge between one workflow graph node and another
 * one. For performance reasons, most information are public.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Edge {

	/**
	 * The id of the edge.
	 */
	public final int id;

	/**
	 * The source of the edge, i.e., where the edge starts.
	 */
	public final WGNode src;

	/**
	 * The target of the edge, i.e., where the edge ends.
	 */
	public final WGNode tgt;
	
	/**
	 * The index for the strong components analysis.
	 */
	public int index = -1;
	
	/**
	 * The lowlink for the strong components analysis.
	 */
	public int lowlink = -1;
	
	/**
	 * Whether this edge is within a cycle or not.
	 */
	public boolean inCycle = false;
	
	/**
	 * The number of the component of the edge.
	 */
	public int component = -1;

	/**
	 * A bit set that later contains the deadlock information.
	 */
	public final BitSet deadlockInformation;

	/**
	 * The information this edge generates during the deadlock analysis.
	 */
	public final BitSet deadlockGen;

	/**
	 * The information this edge kills during the deadlock analysis.
	 */
	public final BitSet deadlockKill;

	/**
	 * The synchronization edges of all direct successor edges.
	 */
	public final BitSet syncEdges;
	
	/**
	 * A set of all dependent edges from this edge.
	 */
	public final BitSet dependent;

	/**
	 * A list of dominators.
	 */
	public final LinkedList<Edge> dominatorList = new LinkedList<Edge>();

	/**
	 * The dominance frontier of this edge as a set.
	 */
	public final BitSet dominanceFrontierSet;

	/**
	 * A set of edges where each edge dominates this edge.
	 */
	public final BitSet dominatorSet;

	/**
	 * A number for a post order.
	 */
	public int postOrderNumber = -1;

	/**
	 * A list of post dominators.
	 */
	public final LinkedList<Edge> postDominatorList = new LinkedList<Edge>();

	/**
	 * A set of edges where each edge post dominates this edge.
	 */
	public final BitSet postDominatorSet;

	/**
	 * The post dominance frontier set.
	 */
	public final BitSet postDominanceFrontierSet;

	/**
	 * A number for the post post order.
	 */
	public int postPostOrderNumber = -1;

	/**
	 * Stores from which edge the current is approximated executed.
	 */
	public final BitSet isApproxExecutedBy;

	/**
	 * The constructor that generates a new edge.
	 * 
	 * @param id
	 *            The id of the edge.
	 * @param src
	 *            The source node of this edge.
	 * @param tgt
	 *            The target node of this edge.
	 * @param edges
	 *            The current number of edges.
	 */
	protected Edge(int id, WGNode src, WGNode tgt, int edges) {
		this.id = id;
		this.src = src;
		this.tgt = tgt;
		this.deadlockInformation = new BitSet(edges);
		this.deadlockGen = new BitSet(edges);
		this.deadlockKill = new BitSet(edges);
		this.syncEdges = new BitSet(edges);
		this.dependent = new BitSet(edges);

		this.dominatorSet = new BitSet(edges);
		this.postDominatorSet = new BitSet(edges);
		this.isApproxExecutedBy = new BitSet(edges);
		this.dominanceFrontierSet = new BitSet(edges);
		this.postDominanceFrontierSet = new BitSet(edges);
	}

	@Override
	public String toString() {
		return id + " (" + src.getId() + "|" + tgt.getId() + ")";
	}
}
