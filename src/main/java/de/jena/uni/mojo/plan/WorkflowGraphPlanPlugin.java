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
package de.jena.uni.mojo.plan;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.annotations.MajorAnalysisPlan;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.plugin.PlanPlugin;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 *
 */
public class WorkflowGraphPlanPlugin implements PlanPlugin {

	@Override
	public String getId() {
		return WorkflowGraphMajorPlan.class.getAnnotation(MajorAnalysisPlan.class).id() + "";
	}

	@Override
	public String getName() {
		return WorkflowGraphMajorPlan.class.getAnnotation(MajorAnalysisPlan.class).name();
	}

	@Override
	public String getAuthor() {
		return WorkflowGraphMajorPlan.class.getAnnotation(MajorAnalysisPlan.class).author();
	}

	@Override
	public String getDescription() {
		return WorkflowGraphMajorPlan.class.getAnnotation(MajorAnalysisPlan.class).description();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public MajorPlan getInstance(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation analysisInformation, ElementStore store) {
		return new WorkflowGraphMajorPlan(graph, map, analysisInformation, store);
	}

}
