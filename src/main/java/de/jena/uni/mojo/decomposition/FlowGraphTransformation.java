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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpt.pm.AndGateway;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.pm.bpmn.Task;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.WGNode.Type;

/**
 * Transforms the workflow graph model into the process model of the jBPT
 * library (https://www.openhub.net/p/jbpt).
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class FlowGraphTransformation extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -403018791829318331L;

	/**
	 * The transformation map. It stores for each node its transformation.
	 */
	private Map<Integer, FlowNode> transformMap = new HashMap<Integer, FlowNode>();

	/**
	 * The produces process model.
	 */
	private ProcessModel model = null;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph to transform into a process model.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public FlowGraphTransformation(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
	}

	@Override
	protected List<Annotation> analyze() {
		ProcessModel process = new ProcessModel();
		// We iterate over each node and create a corresponding FlowNode

		getOrCreate(graph.getStart(), process);

		reporter.put(graph, AnalysisInformation.NUMBER_NODES_FLOW_GRAPH,
				process.getFlowNodes().size());
		reporter.put(graph, AnalysisInformation.NUMBER_EDGES_FLOW_GRAPH,
				process.getControlFlow().size());

		this.model = process;

		return Collections.emptyList();
	}

	/**
	 * Produces a flow node for each workflow graph node.
	 * 
	 * @param node
	 *            The current workflow graph node.
	 * @param process
	 *            The current process model.
	 * @return The produced flow node.
	 */
	private FlowNode getOrCreate(WGNode node, ProcessModel process) {
		FlowNode n = transformMap.get(node.getId());
		if (n == null) {
			if (node.getType() == Type.ACTIVITY || node.getType() == Type.START
					|| node.getType() == Type.END) {
				n = new Task(node.getId() + "");
			} else if (node.getType() == Type.SPLIT
					|| node.getType() == Type.MERGE) {
				n = new XorGateway(node.getId() + "");
			} else if (node.getType() == Type.FORK
					|| node.getType() == Type.JOIN) {
				n = new AndGateway(node.getId() + "");
			} else {
				n = new OrGateway(node.getId() + "");
			}

			transformMap.put(node.getId(), n);
			process.addFlowNode(n);

			for (WGNode succ : node.getSuccessors()) {
				FlowNode s = getOrCreate(succ, process);
				process.addControlFlow(n, s);
			}
		}

		return n;
	}

	/**
	 * Get the produced process model.
	 * 
	 * @return The process model.
	 */
	public ProcessModel getProcessModel() {
		return model;
	}

}
