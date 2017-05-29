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
package de.jena.uni.mojo.analysis;

import java.util.List;
import java.util.concurrent.RecursiveTask;


import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;

/**
 * The core analysis class is one of the cores of Mojo since each step during
 * the analysis inherits this abstract class. The abstract class itself is a
 * RecursiveTask, i.e., it can be run in parallel by a fork-join-pool.
 * Furthermore, the analysis class measures the time of each computation of the
 * implementing class.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public abstract class CoreAnalysis extends RecursiveTask<List<Annotation>> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 8490949638454974501L;

	/**
	 * The analysis information reporter.
	 */
	protected final AnalysisInformation reporter;
	
	/**
	 * The file or name of the process.
	 */
	protected final String processName;

	/**
	 * The constructor defines a core analysis consisting of an analysis
	 * information reporter that measures for example the time.
	 * 
	 * @param processName
	 *            The file or name of the process.
	 * @param reporter
	 *            An analysis information reporter
	 */
	public CoreAnalysis(String processName, AnalysisInformation reporter) {
		this.processName = processName;
		this.reporter = reporter;
	}

	/**
	 * An abstract method which defines the entry point of the analysis. As
	 * result it gives back a list of some error information.
	 * 
	 * @return A list of error information.
	 */
	protected abstract List<Annotation> analyze();

	/**
	 * This computation method must been used at runtime to guarantee a right
	 * behavior. Then, the method can be executed in parallel with other
	 * parallel tasks. Furthermore, this method automatically adds an entry of
	 * time measurement for this analysis.
	 * 
	 * @return A list of error information.
	 */
	public List<Annotation> compute() {
		reporter.startTimeMeasurement(processName, this.getClass().getName());
		List<Annotation> list = analyze();
		reporter.endTimeMeasurement(processName, this.getClass().getName());
		return list;
	}
}
