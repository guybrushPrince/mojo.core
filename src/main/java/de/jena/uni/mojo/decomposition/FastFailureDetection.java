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
package de.jena.uni.mojo.decomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.error.StructureMismatchAnnotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * Determines failures in the process by analyzing its refined process structure
 * tree (RPST). Therefore, it uses no heuristics. Only the facts.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class FastFailureDetection extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6300622255223553482L;

	/**
	 * The refined process structure tree (RPST).
	 */
	private final RPST<ControlFlow<FlowNode>, FlowNode> rpst;

	/**
	 * A list of failures.
	 */
	private final List<Annotation> errors = new ArrayList<Annotation>();

	/**
	 * The counter for analyzed rigids.
	 */
	private int rigidCounter = 0;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 * @param rpst
	 *            The refined process structure tree.
	 */
	public FastFailureDetection(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter,
			RPST<ControlFlow<FlowNode>, FlowNode> rpst) {
		super(graph, map, reporter);
		this.rpst = rpst;
	}

	@Override
	protected List<Annotation> analyze() {

		// Determine the root of the rpst
		IRPSTNode<ControlFlow<FlowNode>, FlowNode> root = rpst.getRoot();

		check(root, false);

		return errors;
	}

	/**
	 * Since the RPST is a tree, it goes from the root to its leaves.
	 * 
	 * @param current
	 *            The current tree node.
	 * @param rigid
	 *            A flag whether this node belongs to a rigid or not.
	 */
	private void check(IRPSTNode<ControlFlow<FlowNode>, FlowNode> current,
			boolean rigid) {

		switch (current.getType()) {
		case BOND: {
			checkBond(current, false);
		}
			break;
		case POLYGON: {
			checkPolygon(current, false);
		}
			break;
		case RIGID: {
			checkRigid(current, rigid);
		}
			break;
		case TRIVIAL: {
			checkTrivial(current, rigid);
		}
			break;
		default:
			break;
		}
	}

	/**
	 * Checks a trivial node. It is a leave.
	 * 
	 * @param current
	 *            The current node.
	 * @param rigid
	 *            Whether it is in a rigid or not.
	 */
	private void checkTrivial(
			IRPSTNode<ControlFlow<FlowNode>, FlowNode> current, boolean rigid) {
		reporter.add(graph, AnalysisInformation.NUMBER_TRIVIALS, 1);
	}

	/**
	 * Checks a bond node.
	 * 
	 * @param current
	 *            The current node.
	 * @param rigid
	 *            Whether it is in a rigid or not.
	 */
	private void checkBond(IRPSTNode<ControlFlow<FlowNode>, FlowNode> current,
			boolean rigid) {

		reporter.add(graph, AnalysisInformation.NUMBER_BONDS, 1);

		// Get the entry and exit flow nodes
		FlowNode entry = current.getEntry();
		FlowNode exit = current.getExit();

		// Determine the failures in the structure
		WGNode wgEntry = map[Integer.parseInt(entry.getName())];
		WGNode wgExit = map[Integer.parseInt(exit.getName())];
		determineBondFailures(wgEntry, wgExit);

		// Get the children
		Set<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> children = rpst
				.getChildren(current);

		// For each of its children create either a loop forward
		// or a loop backward fowo node
		for (IRPSTNode<ControlFlow<FlowNode>, FlowNode> child : children) {
			// Transform the child
			check(child, false);
		}
	}

	/**
	 * Checks a polygon.
	 * 
	 * @param current
	 *            The current node.
	 * @param rigid
	 *            Whether it is in a rigid or not.
	 */
	private void checkPolygon(
			IRPSTNode<ControlFlow<FlowNode>, FlowNode> current, boolean rigid) {

		reporter.add(graph, AnalysisInformation.NUMBER_POLYGONS, 1);

		// Get the children
		Set<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> children = rpst
				.getChildren(current);

		// We transform each of its children
		for (IRPSTNode<ControlFlow<FlowNode>, FlowNode> child : children) {
			// Transform the node, warning: we put the current root into it.
			check(child, false);
		}
	}

	/**
	 * Checks a rigid.
	 * 
	 * @param current
	 *            The current node.
	 * @param rigid
	 *            Whether it is in a rigid or not.
	 */
	private void checkRigid(IRPSTNode<ControlFlow<FlowNode>, FlowNode> current,
			boolean rigid) {

		if (!rigid) {
			reporter.add(graph, AnalysisInformation.NUMBER_RIGIDS, 1);
			reporter.put(graph,
					AnalysisInformation.RIGID_SIZE + rigidCounter++, current
							.getFragment().size());
			rigid = true;
		}

		// Get the children
		Set<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> children = rpst
				.getChildren(current);

		// We handle it a little bit similar to a polygon
		for (IRPSTNode<ControlFlow<FlowNode>, FlowNode> child : children) {
			check(child, rigid);
		}
	}

	/**
	 * Determine failures in a bond by comparing the entry and exit node of this
	 * bond.
	 * 
	 * @param wgEntry
	 *            The entry workflow graph node of this bond.
	 * @param wgExit
	 *            The exit workflow graph node of this bond.
	 */
	private void determineBondFailures(WGNode wgEntry, WGNode wgExit) {
		switch (wgEntry.getType()) {
		case FORK:
			if (wgExit.getType() == Type.MERGE) {
				StructureMismatchAnnotation annotation = new StructureMismatchAnnotation(
						"Fork is closed with merge, that lead to a lack "
								+ "of synchronisation, and should not be done! We "
								+ "handle it as a OR-join.", this);
				annotation.addPrintableNode(wgExit);
				annotation.addOpeningNode(wgEntry);

				// Add it to the failure list
				errors.add(annotation);

				reporter.add(
						graph,
						AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION_NORMAL,
						1);
				reporter.add(graph,
						AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION, 1);
			}
			break;
		case OR_FORK:
			if (wgExit.getType() != Type.OR_JOIN) {
				StructureMismatchAnnotation annotation = new StructureMismatchAnnotation(
						"Or-fork should be closed with an or-join. We handle "
								+ "it as an OR-join", this);
				annotation.addPrintableNode(wgExit);
				annotation.addOpeningNode(wgEntry);

				// Add it to the failure list
				errors.add(annotation);

				if (wgExit.getType() == Type.MERGE) {
					reporter.add(
							graph,
							AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION_NORMAL,
							1);
					reporter.add(graph,
							AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION,
							1);
				} else {
					reporter.add(graph,
							AnalysisInformation.NUMBER_DEADLOCKS_NORMAL, 1);
					reporter.add(graph, AnalysisInformation.NUMBER_DEADLOCKS, 1);
				}
			}
			break;
		case SPLIT:
			if (wgExit.getType() == Type.JOIN) {
				StructureMismatchAnnotation annotation = new StructureMismatchAnnotation(
						"Split is closed with join. We handle it as an OR-join",
						this);
				annotation.addPrintableNode(wgExit);
				annotation.addOpeningNode(wgEntry);

				// Add it to the failure list
				errors.add(annotation);

				reporter.add(graph,
						AnalysisInformation.NUMBER_DEADLOCKS_NORMAL, 1);
				reporter.add(graph, AnalysisInformation.NUMBER_DEADLOCKS, 1);
			}
			break;
		case JOIN: {
			StructureMismatchAnnotation annotation = new StructureMismatchAnnotation(
					"A loop starts with a join. That cannot be "
							+ "profitable. We handle it as a merge", this);
			annotation.addPrintableNode(wgEntry);
			annotation.addOpeningNode(wgEntry);

			// Add it to the failure list
			errors.add(annotation);

			reporter.add(graph, AnalysisInformation.NUMBER_DEADLOCKS_LOOP, 1);
			reporter.add(graph, AnalysisInformation.NUMBER_DEADLOCKS, 1);
		}
			break;
		default:
			break;
		}
		if (wgExit.getType() == Type.FORK || wgExit.getType() == Type.OR_FORK) {
			StructureMismatchAnnotation annotation = new StructureMismatchAnnotation(
					"A loop with a (OR-)fork as branch leads to "
							+ "a lack of synchronization. Handled as split.",
					this);
			annotation.addPrintableNode(wgExit);
			annotation.addOpeningNode(wgEntry);

			// Add it to the failure list
			errors.add(annotation);

			reporter.add(graph,
					AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION_LOOP, 1);
			reporter.add(graph,
					AnalysisInformation.NUMBER_LACK_OF_SYNCHRONIZATION, 1);
		}
	}
}