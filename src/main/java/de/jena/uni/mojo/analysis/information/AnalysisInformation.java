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
package de.jena.uni.mojo.analysis.information;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jena.uni.mojo.model.WorkflowGraph;

/**
 * Measure and store some information about the analysis.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 *
 */
public class AnalysisInformation {
	
	/**
	 * Some constants
	 */
	public final static String TIME_MEASUREMENT 						= "_TIME_MEASUREMENT";
	public final static String IGNORE_LAST 								= "_IGNORE_LAST";
	public final static String IGNORE_COMPLETE							= "_IGNORE_COMPLETE";
	public final static String TIME_MEASUREMENT_PLAN 					= "TIME_MEASUREMENT_PLAN";
	public final static String NODES_BEFORE_MEASUREMENT_PLAN 			= "NODES_BEFORE_MEASUREMENT_PLAN";
	public final static String NODES_AFTER_MEASUREMENT_PLAN 			= "NODES_AFTER_MEASUREMENT_PLAN";
	public final static String NUMBER_DEADLOCKS							= "NUMBER_DEADLOCKS";
	public final static String NUMBER_DEADLOCKS_NORMAL					= "NUMBER_DEADLOCKS_NORMAL";
	public final static String NUMBER_DEADLOCKS_LOOP					= "NUMBER_DEADLOCKS_LOOP";
	public final static String NUMBER_LACK_OF_SYNCHRONIZATION			= "NUMBER_LACK_OF_SYNCHRONIZATION";
	public final static String NUMBER_LACK_OF_SYNCHRONIZATION_NORMAL	= "NUMBER_LACK_OF_SYNCHRONIZATION_NORMAL";
	public final static String NUMBER_VICIOUS_CIRCLES					= "NUMBER_VICIOUS_CIRCLES";
	public final static String NUMBER_LACK_OF_SYNCHRONIZATION_LOOP		= "NUMBER_LACK_OF_SYNCHRONIZATION_LOOP";
	public final static String NUMBER_RIGIDS							= "NUMBER_RIGIDS";
	public final static String NUMBER_BONDS								= "NUMBER_BONDS";
	public final static String NUMBER_POLYGONS							= "NUMBER_POLYGONS";
	public final static String NUMBER_TRIVIALS							= "NUMBER_TRIVIALS";
	public final static String RIGID_SIZE								= "RIGID_SIZE_";
	public final static String NUMBER_NODES_FLOW_GRAPH					= "NUMBER_NODES_FLOW_GRAPH";
	public final static String NUMBER_EDGES_FLOW_GRAPH					= "NUMBER_EDGES_FLOW_GRAPH";
	public final static String NUMBER_EDGES								= "NUMBER_EDGES";
	public final static String NUMBER_VISITED_EDGES_EXECUTION_EDGE		= "NUMBER_VISITED_EDGES_EXECUTION_EDGE";
	public final static String FILE_NAME								= "FILE_NAME";

	/**
	 * Generate a map for all the information.
	 */
	private final Map<WorkflowGraph, Map<String, Object>> information =
			Collections.synchronizedMap(new HashMap<WorkflowGraph, Map<String, Object>>());
	
	/**
	 * A map for all the information until the workflow graph is not produced
	 */
	private final Map<String, Map<String, Object>> informationFiles =
			Collections.synchronizedMap(new HashMap<String, Map<String, Object>>());
	
	/**
	 * Store a special information for the given graph. 
	 * @param graph The workflow graph.
	 * @param key The key of the information
	 * @param value The information itself.
	 */
	public void put(WorkflowGraph graph, String key, Object value) {
		Map<String, Object> m = information.get(graph);
		if (m == null) {
			m = Collections.synchronizedMap(new HashMap<String, Object>());
			information.put(graph, m);
		}
		m.put(key, value);
	}
	
	/**
	 * Store a special information for the given file. 
	 * @param file The file of the workflow graph.
	 * @param key The key of the information
	 * @param value The information itself.
	 */
	public void put(String file, String key, Object value) {
		Map<String, Object> m = informationFiles.get(file);
		if (m == null) {
			m = Collections.synchronizedMap(new HashMap<String, Object>());
			informationFiles.put(file, m);
		}
		m.put(key, value);
	}
	
	/**
	 * Get the information for a given graph and key.
	 * @param graph The workflow graph.
	 * @param key The key of the information.
	 * @return The information
	 */
	public Object get(WorkflowGraph graph, String key) {
		Map<String, Object> map = information.get(graph);
		return (map == null ? null : map.get(key));
	}
	
	/**
	 * Get the information for a given file and key.
	 * @param file The file of the workflow graph.
	 * @param key The key of the information.
	 * @return The information
	 */
	public Object get(String file, String key) {
		Map<String, Object> map = informationFiles.get(file);
		return (map == null ? null : map.get(key));
	}
	
	/**
	 * Get all information in a map for a given graph.
	 * @param graph The workflow graph.
	 * @return A key-value map of all information.
	 */
	public Map<String, Object> getInformation(WorkflowGraph graph) {
		return information.get(graph);
	}
	
	/**
	 * A method that starts a time measurement. It is a specialized
	 * method as it is often used.
	 * @param graph The workflow graph.
	 * @param key The key.
	 */
	public void startTimeMeasurement(WorkflowGraph graph, String key) {
		put(graph, key + TIME_MEASUREMENT, System.nanoTime());
	}
	
	/**
	 * A method that starts a time measurement. It is a specialized
	 * method as it is often used.
	 * @param file The file of the workflow graph.
	 * @param key The key.
	 */
	public void startTimeMeasurement(String file, String key) {
		put(file, key + TIME_MEASUREMENT, System.nanoTime());
	}

	/**
	 * A method that stops a time measurement. It is a specialized
	 * method as it is often used.
	 * @param graph The workflow graph.
	 * @param key The key.
	 */
	public void endTimeMeasurement(WorkflowGraph graph, String key) {
		long time = System.nanoTime() - (Long) get(graph, key + TIME_MEASUREMENT);
		put(graph, key + TIME_MEASUREMENT, time);
	}
	
	/**
	 * A method that stops a time measurement. It is a specialized
	 * method as it is often used.
	 * @param file The file of the workflow graph.
	 * @param key The key.
	 */
	public void endTimeMeasurement(String file, String key) {
		long time = System.nanoTime() - (Long) get(file, key + TIME_MEASUREMENT);
		put(file, key + TIME_MEASUREMENT, time);
	}
	
	/**
	 * A method that starts a time ignore measurement. It is a specialized
	 * method as it is often used.
	 * @param graph The workflow graph.
	 * @param key The key.
	 */
	public void startIgnoreTimeMeasurement(WorkflowGraph graph, String key) {
		put(graph, key + IGNORE_LAST, System.nanoTime());
	}
	
	/**
	 * A method that starts a time ignore measurement. It is a specialized
	 * method as it is often used.
	 * @param file The file of the workflow graph.
	 * @param key The key.
	 */
	public void startIgnoreTimeMeasurement(String file, String key) {
		put(file, key + IGNORE_LAST, System.nanoTime());
	}

	/**
	 * A method that stops a time ignore measurement. It is a specialized
	 * method as it is often used.
	 * @param graph The workflow graph.
	 * @param key The key.
	 */
	public void endIgnoreTimeMeasurement(WorkflowGraph graph, String key) {
		long time = System.nanoTime() - (Long) get(graph, key + IGNORE_LAST);
		put(graph, key + IGNORE_LAST, time);
		Object complete = get(graph, key + IGNORE_COMPLETE);
		put(graph, key + IGNORE_COMPLETE, (complete != null ? (Long) complete : 0) + time);
	}
	
	/**
	 * A method that stops a time ignore measurement. It is a specialized
	 * method as it is often used.
	 * @param file The file of the workflow graph.
	 * @param key The key.
	 */
	public void endIgnoreTimeMeasurement(String file, String key) {
		long time = System.nanoTime() - (Long) get(file, key + IGNORE_LAST);
		put(file, key + IGNORE_LAST, time);
		Object complete = get(file, key + IGNORE_COMPLETE);
		put(file, key + IGNORE_COMPLETE, (complete != null ? (Long) complete : 0) + time);
	}
	
	/**
	 * A special method that adds a value to an entry.
	 * @param graph The workflow graph.
	 * @param key The key.
	 * @param add The value to add.
	 */
	public void add(WorkflowGraph graph, String key, int add) {
		Object val = get(graph,key);
		int oldVal;
		if (val == null) {
			oldVal = 0;
		} else {
			oldVal = (Integer) val;
		}
		put(graph,key,oldVal + add);
	}
	
	/**
	 * Returns all information within a string CVS like format.
	 * @return All information within a string CVS like format.
	 */
	public String export() {
		// Determine all important keys
		Set<String> keys = new HashSet<String>();
		for (WorkflowGraph g: information.keySet()) {
			keys.addAll(information.get(g).keySet());
		}
		
		StringBuilder builder = new StringBuilder();
		
		List<String> keyList = new ArrayList<String>(keys);
		
		// Generate the header
		for (String k: keyList) {
			builder.append(k + ";");
		}
		builder.append("\n");
		
		// Put the information into the map
		for (WorkflowGraph g: information.keySet()) {
			Map<String, Object> map = information.get(g);
			
			for (String k: keyList) {
				builder.append((map.get(k) == null ? "" : map.get(k)) + ";");
			}
			builder.append("\n");
		}
		
		return builder.toString();		
	}
	
}
