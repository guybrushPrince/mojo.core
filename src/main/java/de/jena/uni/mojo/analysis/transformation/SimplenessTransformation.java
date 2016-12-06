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
package de.jena.uni.mojo.analysis.transformation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;


import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.WGNode.Type;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * Places additional activity nodes between gateways to make incoming and
 * outgoing edges more explicit.
 * 
 * @author Dipl.-Inf. Thomas Prinz
 * 
 */
public class SimplenessTransformation extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2382270947488315924L;

	/**
	 * Counts the nodes so that we have a unique ongoing id for each node.
	 */
	private int nodeCounter;

	/**
	 * The (new) list of workflow graph nodes.
	 */
	private final List<WGNode> list = new ArrayList<WGNode>();

	/**
	 * The element store.
	 */
	private final ElementStore store;

	/**
	 * The simpleness transformation constructor.
	 * 
	 * @param graph
	 *            The workflow graph to transform.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public SimplenessTransformation(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.nodeCounter = map.length;
		for (WGNode n : map) {
			if (n != null)
				list.add(n);
		}
		this.store = null;
	}

	/**
	 * A second simpleness transformation constructor that should be used when
	 * this analysis is started by an extern tool that uses the element store.
	 * 
	 * @param graph
	 *            The workflow graph to transform.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public SimplenessTransformation(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, ElementStore elementStore) {
		super(graph, map, reporter);
		this.nodeCounter = map.length;
		for (WGNode n : map) {
			if (n != null)
				list.add(n);
		}
		this.store = elementStore;
	}

	/**
	 * Transform the workflow graph by adding additional nodes.
	 */
	private void transform() {
		for (WGNode join : graph.getJoinList()) {
			BitSet pres = (BitSet) join.getPredecessorsBitSet().clone();
			// Eliminate the activities
			pres.andNot(graph.getActivitySet());
			for (int i = pres.nextSetBit(0); i >= 0; i = pres.nextSetBit(i + 1)) {
				insertActivity(map[i], join);
			}
		}
		for (WGNode orJoin : graph.getOrJoinList()) {
			BitSet pres = (BitSet) orJoin.getPredecessorsBitSet().clone();
			// Eliminate the activities
			pres.andNot(graph.getActivitySet());
			for (int i = pres.nextSetBit(0); i >= 0; i = pres.nextSetBit(i + 1)) {
				insertActivity(map[i], orJoin);
			}
		}
		for (WGNode merge : graph.getMergeList()) {
			BitSet pres = (BitSet) merge.getPredecessorsBitSet().clone();
			// Eliminate the activities
			pres.andNot(graph.getActivitySet());
			for (int i = pres.nextSetBit(0); i >= 0; i = pres.nextSetBit(i + 1)) {
				insertActivity(map[i], merge);
			}
		}
		for (WGNode fork : graph.getForkList()) {
			BitSet sucs = (BitSet) fork.getSuccessorsBitSet().clone();
			// Eliminate the activities
			sucs.andNot(graph.getActivitySet());
			for (int i = sucs.nextSetBit(0); i >= 0; i = sucs.nextSetBit(i + 1)) {
				insertActivity(fork, map[i]);
			}
		}
		for (WGNode split : graph.getSplitList()) {
			BitSet sucs = (BitSet) split.getSuccessorsBitSet().clone();
			// Eliminate the activities
			sucs.andNot(graph.getActivitySet());
			for (int i = sucs.nextSetBit(0); i >= 0; i = sucs.nextSetBit(i + 1)) {
				insertActivity(split, map[i]);
			}
		}
		for (WGNode orfork : graph.getOrForkList()) {
			BitSet sucs = (BitSet) orfork.getSuccessorsBitSet().clone();
			// Eliminate the activities
			sucs.andNot(graph.getActivitySet());
			for (int i = sucs.nextSetBit(0); i >= 0; i = sucs.nextSetBit(i + 1)) {
				insertActivity(orfork, map[i]);
			}
		}
		WGNode end = graph.getEnd();
		WGNode pre = end.getPredecessors().get(0);
		if (pre.getType() != Type.ACTIVITY) {
			insertActivity(pre, end);
		}

		WGNode start = graph.getStart();
		WGNode succ = start.getSuccessors().get(0);
		if (succ.getType() != Type.ACTIVITY) {
			insertActivity(start, succ);
		}
	}

	/**
	 * Inserts an activity between two nodes.
	 * 
	 * @param from
	 *            The node where the edge starts.
	 * @param to
	 *            The node where the edge ends.
	 */
	private void insertActivity(WGNode from, WGNode to) {
		do {
			// Create the new activity
			WGNode activity;
			if (store == null) {
				activity = createNode(Type.ACTIVITY);
			} else {
				activity = store.createNode(Type.ACTIVITY);
				list.add(activity);
			}
			// Eliminate the old connections
			from.removeSuccessor(to);
			to.removePredecessor(from);

			// Add the new connections
			from.addSuccessor(activity);
			activity.addPredecessor(from);
			to.addPredecessor(activity);
			activity.addSuccessor(to);
			
			// Add the process elements of both
			activity.addProcessElements(from.getProcessElements());
			activity.addProcessElements(to.getProcessElements());

			// Add the node to the workflow graph
			graph.addNode(activity);
		} while (from.getSuccessors().contains(to)
				|| to.getPredecessors().contains(from));
	}

	/**
	 * Creates an extended node array map.
	 * 
	 * @return The extended node array map.
	 */
	public WGNode[] getExtendedMap() {
		WGNode[] map;
		if (store == null) {
			map = new WGNode[nodeCounter];
		} else {
			map = new WGNode[store.getNodeList().size()];
		}
		for (WGNode node : list) {
			map[node.getId()] = node;
		}
		return map;
	}

	/**
	 * Create a node with a specific type.
	 * 
	 * @param type
	 *            The type that should be created.
	 * @return The created node.
	 */
	private WGNode createNode(final Type type) {
		final WGNode node = new WGNode(nodeCounter++, type);
		list.add(node);
		return node;
	}

	@Override
	protected List<Annotation> analyze() {

		// Measure the nodes before executing the plan
		int nodesBefore = graph.getNodeList().size();

		transform();

		// Measure the nodes after executing the plan
		int nodesAfter = graph.getNodeList().size();

		reporter.put(graph, AnalysisInformation.NODES_BEFORE_MEASUREMENT_PLAN,
				nodesBefore);
		reporter.put(graph, AnalysisInformation.NODES_AFTER_MEASUREMENT_PLAN,
				nodesAfter);

		reporter.put(graph, "NUMBER_FORKS", graph.getForkList().size());
		reporter.put(graph, "NUMBER_JOINS", graph.getJoinList().size());
		reporter.put(graph, "NUMBER_TASKS", graph.getActivityList().size());
		reporter.put(graph, "NUMBER_SPLITS", graph.getSplitList().size());
		reporter.put(graph, "NUMBER_MERGES", graph.getMergeList().size());
		reporter.put(graph, "NUMBER_ORJOINS", graph.getOrJoinList().size());
		reporter.put(graph, "NUMBER_ORSPLITS", graph.getOrForkList().size());

		return Collections.emptyList();
	}
}
