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
package de.jena.uni.mojo.verifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.reflections.Reflections;

import de.jena.uni.mojo.Mojo;
import de.jena.uni.mojo.analysis.Analysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.annotations.MajorAnalysisPlan;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.general.MajorPlan;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.plan.WorkflowGraphMajorPlan;
import de.jena.uni.mojo.util.store.ElementStore;

/**
 * The verifier is the entry point for each analysis. It holds some information
 * about parallel processes, etc.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Verifier extends Analysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5747937418712967031L;

	/**
	 * A fork join pool to allow simple parallelism.
	 */
	private static ForkJoinPool fjPool = new ForkJoinPool();

	/**
	 * An element store where each element is stored.
	 */
	private final ElementStore store;

	/**
	 * Determined major plans.
	 */
	public static Set<Class<?>> majorPlans = null;

	/**
	 * Already determined constructors.
	 */
	private final static Map<Integer, Constructor<?>> constructors = 
			new HashMap<Integer, Constructor<?>>();

	/**
	 * When Mojo is used as terminal tool, it has not an element store in any
	 * case. If there is not such an element store, then the usage of this
	 * constructor should be preferred.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 */
	public Verifier(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter) {
		super(graph, map, reporter);
		this.store = null;

		if (Verifier.majorPlans == null) {
			Reflections reflections = new Reflections("org.mojo");
			Verifier.majorPlans = reflections
					.getTypesAnnotatedWith(MajorAnalysisPlan.class);
		}
	}

	/**
	 * When Mojo is used as library and there exists an element store, then this
	 * constructor should be used.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param map
	 *            The node array map.
	 * @param reporter
	 *            The analysis information.
	 * @param store
	 *            The element store.
	 */
	public Verifier(WorkflowGraph graph, WGNode[] map,
			AnalysisInformation reporter, ElementStore store) {
		super(graph, map, reporter);
		this.store = store;
	}

	@Override
	protected List<Annotation> analyze() {

		//
		// 1. Take the right major plan
		//
		// Get all major plans
		reporter.startIgnoreTimeMeasurement(graph, this.getClass().getName());

		// Determine the constructor of the right major plan.
		int planId = Mojo.getCommand("ANALYSIS_PLAN").asIntegerValue();
		Constructor<?> constr = Verifier.constructors.get(planId);
		
		// If the constructor was not found
		if (constr == null) {
			for (Class<?> mp : Verifier.majorPlans) {
				if (mp.getAnnotation(MajorAnalysisPlan.class).id() == planId) {
					// Determine the constructor
					try {
						constr = mp.getConstructor(
								WorkflowGraph.class, this.map.getClass(),
								AnalysisInformation.class, ElementStore.class);
						
						Verifier.constructors.put(planId, constr);
					} catch (NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// Instantiate the major plan
		MajorPlan plan = null;
		// Instantiate the plan
		if (constr != null) {
			try {
				plan = (MajorPlan) constr.newInstance(graph, map, reporter,
						store);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
		
		// Define the default plan
		if (plan == null) {
			plan = new WorkflowGraphMajorPlan(graph, map, reporter, store);
		}
		reporter.endIgnoreTimeMeasurement(graph, this.getClass().getName());

		// Invoke the plan
		fjPool.invoke(plan);

		// And join the information.
		return plan.join();
	}
}
