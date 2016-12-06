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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.graph.abs.IDirectedEdge;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.ProcessModel;

import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * Performs a graph decomposition into SESE fragments. It uses the library jBpt:
 * https://www.openhub.net/p/jbpt
 * 
 * This uses algorithms from:
 * 
 * Vanhatalo, J. and Voelzer, H. and Leymann: Faster and More Focused
 * Control-Flow Analysis for Business Process Models Through SESE Decomposition.
 * Service-Oriented Computing - ICSOC 2007, Fifth International Conference,
 * Vienna, Austria, September 17-20, 2007, Proceedings
 * 
 * Vanhatalo, J. and Voelzer, H. and Koehler, Jana: The Refined Process
 * Structure Tree. Data & Knowledge Engineering 68 I9
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class GraphDecomposition extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6670654397586137623L;

	/**
	 * The refined process structure tree (RPST).
	 */
	private RPST<ControlFlow<FlowNode>, FlowNode> rpst;

	/**
	 * The constructor.
	 * 
	 * @param graph
	 *            The workflow graph to decompose.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information reporter.
	 */
	public GraphDecomposition(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
	}

	@Override
	protected List<Annotation> analyze() {

		// Transform the workflow graph into a flow graph
		FlowGraphTransformation transform = new FlowGraphTransformation(graph,
				map, reporter);
		transform.compute();

		ProcessModel model = transform.getProcessModel();

		// Create the RPST
		rpst = new RPST<ControlFlow<FlowNode>, FlowNode>(model);

		return Collections.emptyList();
	}

	/**
	 * Get the RPST.
	 * 
	 * @return The RPST.
	 */
	public RPST<ControlFlow<FlowNode>, FlowNode> getRPST() {
		return rpst;
	}

	/**
	 * Produces a dot output from the RPST.
	 * 
	 * @return A string as dot output for the RPST.
	 */
	public String getDotOutput() {
		Collection<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> vertices = rpst
				.getVertices();
		Collection<IDirectedEdge<IRPSTNode<ControlFlow<FlowNode>, FlowNode>>> edges = rpst
				.getEdges();

		StringBuilder builder = new StringBuilder();
		builder.append("digraph EasyGraph {\n");

		for (IRPSTNode<ControlFlow<FlowNode>, FlowNode> v : vertices) {
			String name = v.getName();
			if (name.contains("->")) {
				name = name.replace("->", "t");
				name = name.replace("[", "");
				name = name.replace("]", "");
			}
			name = "n" + name;
			builder.append(name + "[label=\"" + name);
			builder.append("\", shape=circle];\n");
		}

		for (IDirectedEdge<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> e : edges) {
			String sname = e.getSource().getName();
			if (sname.contains("->")) {
				sname = sname.replace("->", "t");
				sname = sname.replace("[", "");
				sname = sname.replace("]", "");
			}
			sname = "n" + sname;

			String tname = e.getTarget().getName();
			if (tname.contains("->")) {
				tname = tname.replace("->", "t");
				tname = tname.replace("[", "");
				tname = tname.replace("]", "");
			}
			tname = "n" + tname;

			builder.append(sname + "->" + tname + "[label=\"\"];\n");
		}

		builder.append("}");

		return builder.toString();
	}

}
