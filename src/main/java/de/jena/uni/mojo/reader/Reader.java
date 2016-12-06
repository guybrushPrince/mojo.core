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
package de.jena.uni.mojo.reader;

import java.io.File;
import java.util.List;


import de.jena.uni.mojo.analysis.CoreAnalysis;
import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.util.store.ErrorAndWarningStore;

/**
 * A reader is an abstract class and core analysis which should be extended to
 * allow the parsing of a new file or input language.
 * 
 * The major task is to transform the incoming models directly into workflow
 * graphs.
 * 
 * @author Dipl.-Inf. Thomas Prinz
 * 
 */
public abstract class Reader extends CoreAnalysis {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1212080730178240467L;

	/**
	 * The file which is read.
	 */
	protected final File file;

	/**
	 * A list of resulting workflow graphs.
	 */
	protected List<WorkflowGraph> graphs;

	/**
	 * The constructor.
	 * 
	 * @param file
	 *            The file which contains the 'code'.
	 * @param analysisInformation
	 *            The analysis information.
	 */
	protected Reader(File file, AnalysisInformation analysisInformation) {
		super(file.getAbsolutePath(), analysisInformation);
		this.file = file;
	}

	@Override
	public abstract List<Annotation> analyze();

	/**
	 * Returns the error and warning store of the reader.
	 * 
	 * @return The error and warning store.
	 */
	public abstract ErrorAndWarningStore getStore();

	/**
	 * Returns the results of the reader --- the workflow graphs.
	 * 
	 * @return A list of workflow graphs.
	 */
	public List<WorkflowGraph> getResult() {
		return this.graphs;
	}

}
