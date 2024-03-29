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
package de.jena.uni.mojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.command.Command;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.interpreter.IdInterpreter;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.plan.WorkflowGraphPlanPlugin;
import de.jena.uni.mojo.plugin.PlanPlugin;
import de.jena.uni.mojo.plugin.SourcePlugin;
import de.jena.uni.mojo.processes.FileHandler;
import de.jena.uni.mojo.reader.Reader;
import de.jena.uni.mojo.util.export.WorkflowGraphExporter;
import de.jena.uni.mojo.verifier.Verifier;

/**
 * The Mojo class is the interface to use Mojo as terminal tool or as library.
 * All the analyses can be started from this class.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Mojo {

	/**
	 * A map that stores the commands with their specific names.
	 */
	private static Map<String, Command> commands = new HashMap<String, Command>();

	/**
	 * A file handler that searches and stores files.
	 */
	private static FileHandler fileHandler;

	/**
	 * Define a map which maps from file extension to the necessary source
	 * plugin.
	 */
	private final static Map<String, SourcePlugin> sourcePlugins = new HashMap<String, SourcePlugin>();

	/**
	 * Define a map which maps from an id to the necessary plan plugin.
	 */
	private final static Map<String, PlanPlugin> planPlugins = new HashMap<String, PlanPlugin>();

	/**
	 * Defines a list of available file extensions.
	 */
	public final static List<String> availableFileExtensions = new ArrayList<String>();

	/**
	 * Define a logger.
	 */
	public final static Logger logger = LogManager.getLogger(Mojo.class);

	/**
	 * The constructor of Mojo, which defines the commands.
	 */
	public Mojo() {
		loadPlugins();
		defineCommands();
	}

	/**
	 * The main entry method for terminal usage of Mojo. Via the args parameter,
	 * it can use some commands.
	 * 
	 * @param args
	 *            The program arguments
	 */
	public static void main(String[] args) {
		// Load possible plugins
		loadPlugins();

		// Define the commands
		defineCommands();

		// Define a file handler
		fileHandler = new FileHandler();

		// Read in the commands
		readCommands(args);

		// Should the help be displayed?
		if (getCommand("HELP").asBooleanValue()) {
			showHelp();
		} else {

			// Read in some files
			fileHandler.readFiles();

			// Perform the analysis for each graph
			for (int i = 0; i < commands.get("TIMES").asIntegerValue(); i++) {
				AnalysisInformation aInfo = analyze();
				if (commands.get("CSV").asBooleanValue()) {
					createCSV(aInfo);
				}
				System.gc();
			}
		}
	}

	/**
	 * Load extensions in form of plugins.
	 */
	private static void loadPlugins() {
		// Get the source plugins
		Iterator<SourcePlugin> iterator = ServiceLoader.load(SourcePlugin.class).iterator();
		while (iterator.hasNext()) {
			SourcePlugin plugin = iterator.next();
			// Add it to the library
			sourcePlugins.put(plugin.getFileExtension(), plugin);
			availableFileExtensions.add(plugin.getFileExtension());

			logger.info("Register source plugin: " + plugin.getName() + " " + plugin.getVersion());
			logger.info("\tHandles: " + plugin.getFileExtension());
		}

		// Put the workflow graph major plan plugin into
		// the plugins.
		WorkflowGraphPlanPlugin workflowPlugin = new WorkflowGraphPlanPlugin();
		planPlugins.put(workflowPlugin.getId(), workflowPlugin);

		logger.info("Register major plan: " + workflowPlugin.getName() + " " + workflowPlugin.getVersion());
		logger.info("\tDescription: " + workflowPlugin.getDescription());

		// Get the plan plugins
		Iterator<PlanPlugin> planIterator = ServiceLoader.load(PlanPlugin.class).iterator();
		while (planIterator.hasNext()) {
			PlanPlugin plugin = planIterator.next();
			// Add it to the library
			planPlugins.put(plugin.getId(), plugin);

			logger.info("Register major plan: " + plugin.getName() + " " + plugin.getVersion());
			logger.info("\tDescription: " + plugin.getDescription());
		}
	}

	/**
	 * Defines the commands which can be used as argument for Mojo.
	 */
	private static void defineCommands() {
		Command pathCommand = new Command("PATH", "path", "p", "Set the file path", false, String.class,
				"." + File.separatorChar);
		Command expoCommand = new Command("EXPORT_PATH", "export", "e",
				"Set the export path, i.e., the path where the output files are stored in.", false, String.class,
				"." + File.separatorChar);
		Command verbCommand = new Command("VERBOSE", "verbose", "v", "Makes Mojo more verbose", true, Boolean.class,
				false);
		Command timeCommand = new Command("TIMES", "times", "t", "Performs each analysis <times> often", false,
				Integer.class, 1);
		Command dotoCommand = new Command("DOT", "dot", "d", "Produces a dot graph for each file", true, Boolean.class,
				false);
		Command hideCommand = new Command("HIDE_STATISTICS", "hide", "no", "Hides the statistics about the processes",
				true, Boolean.class, false);
		Command helpCommand = new Command("HELP", "help", "h", "Show helpful information about the commands", true,
				Boolean.class, false);
		Command fileCommand = new Command("PROCESS_FILE", "file", "f", "Determine a process file that should be used",
				false, String.class, "");
		Command fromCommand = new Command("FROM_FILE", "from", "[", "Defines from which entry of the process sets, the analysis should start",
				false, Integer.class, 0);
		Command amouCommand = new Command("AMOUNT", "amount", "a", "Defines the maximum number of files to be investigated",
				false, Integer.class, Integer.MAX_VALUE);

		// Create a new stream where we can write in the information
		// about the major plans.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pStream = new PrintStream(baos);
		pStream.printf("%n%-20s %-5s %-50s %s%n", "", "Id", "Name", "Description");
		for (PlanPlugin plugin : planPlugins.values()) {
			pStream.printf("%-20s %-5s %-50s %s%n", "", plugin.getId(), plugin.getName(), plugin.getDescription());
		}

		Command anplCommand = new Command("ANALYSIS_PLAN", "analysisPlan", "ap",
				"Set the major analysis plan" + baos.toString(), false, String.class, "0");

		Command csvfCommand = new Command("CSV", "csv", "c", "Stores all analysis information in a csv file", true,
				Boolean.class, false);

		Command simpCommand = new Command("SIMPLE_END_PLACE", "simple", "s",
				"Produces a simple end node when the input is a PNML file.", true, Boolean.class, false);

		commands.put(pathCommand.getName(), pathCommand);
		commands.put(expoCommand.getName(), expoCommand);
		commands.put(verbCommand.getName(), verbCommand);
		commands.put(timeCommand.getName(), timeCommand);
		commands.put(dotoCommand.getName(), dotoCommand);
		commands.put(hideCommand.getName(), hideCommand);
		commands.put(helpCommand.getName(), helpCommand);
		commands.put(fileCommand.getName(), fileCommand);
		commands.put(fromCommand.getName(), fromCommand);
		commands.put(amouCommand.getName(), amouCommand);
		commands.put(anplCommand.getName(), anplCommand);
		commands.put(csvfCommand.getName(), csvfCommand);
		commands.put(simpCommand.getName(), simpCommand);
	}

	/**
	 * Get the command with the given name.
	 * 
	 * @param name
	 *            The name of the command.
	 * @return The command.
	 */
	public static Command getCommand(String name) {
		return commands.get(name);
	}

	/**
	 * Reads the commands from the terminal and sets them in their specific
	 * commands.
	 * 
	 * @param commands
	 *            The program arguments
	 */
	private static void readCommands(String[] commands) {
		// Iterate over each program argument
		for (int i = 0; i < commands.length; i++) {
			String arg = commands[i];

			// Iterate over each command
			boolean getCommand = false;
			for (Command com : Mojo.commands.values()) {
				if (arg.equals("-" + com.getCommand()) || arg.equals("-" + com.getShortCommand())) {

					getCommand = true;

					if (com.isFlag()) {
						// If the command is a flag that we can set it to true
						com.setValue(true);
					} else {
						// Else there is value for the command
						String val = commands[++i];

						if (com.getValueType() == Integer.class) {
							com.setValue(Integer.parseInt(val));
						} else if (com.getValueType() == Boolean.class) {
							com.setValue(Boolean.parseBoolean(val));
						} else {
							com.setValue(val);
						}
					}
				}
			}

			if (!getCommand) {
				// It is a file name
				fileHandler.addFileName(arg);
			}
		}
	}

	/**
	 * Show the help context.
	 */
	private static void showHelp() {
		System.out.println("Mojo - Forecast the control flows of business processes");

		System.out.printf("%-40s %-15s %-10s %s%n", "Argument", "Current value", "Data type", "Description");
		for (Command com : Mojo.commands.values()) {
			System.out.printf("%-40s %-15" + ((com.getValueType() == Integer.class) ? "d" : "s") + " %-10s %s%n",
					"-" + com.getCommand() + " " + (com.isFlag() ? "" : "<value> ") + "| -" + com.getShortCommand()
							+ " " + (com.isFlag() ? "" : "<value> "),
					com.getValue(), com.getValueType().getSimpleName(), com.getDescription());
		}
	}

	/**
	 * Set the export path where all output files should be stored in.
	 * 
	 * @param exportPath
	 *            The export path.
	 */
	public static void setExportPath(String exportPath) {
		if (!exportPath.endsWith(File.separator)) {
			exportPath += File.separator;
		}

		commands.get("EXPORT_PATH").setValue(exportPath);
	}

	/**
	 * Get the export path.
	 * 
	 * @return The export path.
	 */
	public static String getExportPath() {
		return commands.get("EXPORT_PATH").asStringValue();
	}

	/**
	 * Export all collected information within the analysis information to a csv
	 * file.
	 * 
	 * @param info
	 *            The analysis information.
	 */
	private static void createCSV(AnalysisInformation info) {
		// Get the path and export path
		String path = commands.get("PATH").asStringValue();
		String exportPath = commands.get("EXPORT_PATH").asStringValue();

		// Get the csv string from the analysis information.
		String i = info.export();

		// Set the right output path
		String p = (exportPath == "" ? path : exportPath) + File.separator;

		// Determine the next file to avoid overriding existing
		// information.
		int counter = -1;
		File f;
		do {
			counter++;
			f = new File(p + "results_" + counter + ".csv");
		} while (f.exists());

		// Export the csv file.
		try {
			System.out.println("Export csv file to " + f.getAbsolutePath());
			f.createNewFile();

			final PrintWriter writer = new PrintWriter(f);

			writer.println(i);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Analyze the processes within the current path. This method is only used
	 * if Mojo is used in a terminal as command prompt.
	 */
	private static AnalysisInformation analyze() {
		// Create a new analysis information container
		AnalysisInformation analysisInformation = new AnalysisInformation();

		// For each process file that was found
		int fileCounter = 0;
		for (File file : fileHandler.getFiles()) {

			List<Annotation> list = new ArrayList<Annotation>();

			// Get the right source plugin
			SourcePlugin plugin = Mojo.sourcePlugins.get(FilenameUtils.getExtension(file.getAbsolutePath()));
			
			if (plugin == null) {
				System.err.println("Cannot handle files with extension '" + 
						FilenameUtils.getExtension(file.getAbsolutePath()) + "'");
			} else {
				
				try {
					// Determine the right reader
					Reader reader = plugin.getReader(file.getName(), file, analysisInformation, Charset.defaultCharset());
					// Read in the files
					list.addAll(reader.compute());
	
					analyzeWorkflowGraphs(file, reader.getResult(), plugin.getIdInterpreter(), analysisInformation, list);
				} catch (IOException e) { }
			}

			fileCounter++;
			if (fileCounter > 0 && fileCounter % 1000 == 0) System.gc();
		}

		return analysisInformation;
	}

	/**
	 * Analyses all workflow graphs of a file.
	 * 
	 * @param file
	 *            The current file.
	 * @param graphs
	 *            The workflow graphs which were extracted from the file.
	 * @param interpreter
	 *            An id interpreter (depending on the input)
	 * @param analysisInformation
	 *            The analysis information.
	 * @param list
	 *            A list of annotations.
	 */
	private static void analyzeWorkflowGraphs(File file, List<WorkflowGraph> graphs, IdInterpreter interpreter,
			AnalysisInformation analysisInformation, List<Annotation> list) {

		// The graph cannot be transformed
		if (graphs != null) {

			// Print the file that will be verified.
			System.out.printf("%n%s", file);
			
			// For each workflow graph within the process
			int graphCounter = 1;
			for (WorkflowGraph g : graphs) {

				analysisInformation.put(g, "subgraph", graphCounter++);
				Mojo.analyzeWorkflowGraph(file.getName(), g, analysisInformation, list);

				// Show the information.
				if (!commands.get("HIDE_STATISTICS").asBooleanValue()) {

					// Print the time
					System.out.printf("%n\tTime spent: %15f [ms]%n", (double) ((long) analysisInformation.get(g,
							"Verifier" + AnalysisInformation.TIME_MEASUREMENT)) / (double) 1000000);

					// Print the errors
					for (Annotation error : list) {
						error.printInformation(interpreter);
					}
					if (list.isEmpty()) {
						System.out.printf("\t%s%n", "Everything well");
					}
				}
			}

			if (commands.get("DOT").asBooleanValue()) {
				// Export the workflow graph as dot file.
				String exportPath = commands.get("EXPORT_PATH").asStringValue();
				WorkflowGraphExporter.exportToDot(graphs,
						(exportPath == "" ? commands.get("PATH").asStringValue() : exportPath) + File.separator,
						file.getName());
			}
		}

	}

	/**
	 * Analyses a single workflow graph.
	 * 
	 * @param processName
	 *            The name of the process, e.g., the file name.
	 * @param graph
	 *            The workflow graph.
	 * @param analysisInformation
	 *            The analysis information
	 * @param list
	 *            A list of annotations.
	 */
	public static void analyzeWorkflowGraph(String processName, WorkflowGraph graph,
			AnalysisInformation analysisInformation, List<Annotation> list) {
		// Store the file name.
		analysisInformation.put(graph, AnalysisInformation.FILE_NAME, processName);

		// Start the time measurement
		analysisInformation.startTimeMeasurement(graph, "Verifier");

		// Create a new verifier.
		Verifier verifier = new Verifier(graph, createMap(graph, findMax(graph)), analysisInformation);
		// Verify the process.
		list.addAll(verifier.compute());

		// Stop the time measurement
		analysisInformation.endTimeMeasurement(graph, "Verifier");
	}

	/**
	 * When Mojo is used as library, this is the interface to verify a given
	 * workflow graph.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @param info
	 *            The analysis information which are collected by the analysis.
	 * @return A list of process annotations.
	 */
	public List<Annotation> verify(WorkflowGraph graph, AnalysisInformation info) {
		// Start the time measurement
		info.startTimeMeasurement(graph, "Verifier");

		System.out.println("Start to verify");

		// Define a new verifier.
		Verifier verifier = new Verifier(graph, createMap(graph, findMax(graph)), info);

		// Start the analysis
		List<Annotation> errors = verifier.compute();

		// Stop the time measurement
		info.endTimeMeasurement(graph, "Verifier");

		return errors;
	}

	/**
	 * When Mojo is used as a library it can read in a BPMN file and collect the
	 * information.
	 * 
	 * @param processName
	 *            The name of the process (optional)
	 * @param file
	 *            The file.
	 * @param info
	 *            The analysis information.
	 * @param encoding
	 *            The charset of the file.
	 * @return A list of errors within the process.
	 * @throws IOException
	 *             if a failure occurs during reading the file.
	 */
	public List<Annotation> verify(String processName, File file, AnalysisInformation info, Charset encoding)
			throws IOException {
		String stream = String.join("", Files.readAllLines(file.toPath(), encoding));
		return this.verify(processName, stream, FilenameUtils.getExtension(file.getAbsolutePath()), info, encoding);
	}

	/**
	 * If Mojo is used as a library, this interface can be used to verify a
	 * process string.
	 * 
	 * @param processName
	 *            The name of the process (optional)
	 * @param stream
	 *            The process stream string.
	 * @param info
	 *            The analysis information container.
	 * @param encoding
	 *            The charset of the file.
	 * @return A list of errors.
	 */
	public List<Annotation> verify(String processName, String stream, String extension, AnalysisInformation info,
			Charset encoding) {
		// Get the right source plugin
		SourcePlugin plugin = Mojo.sourcePlugins.get(extension);

		// Create a list to store the errors.
		List<Annotation> list = new ArrayList<Annotation>();

		// Create a reader for the string.
		Reader reader = plugin.getReader(processName, stream, info, encoding);

		list.addAll(reader.compute());

		List<WorkflowGraph> graphs = reader.getResult();

		// The graph cannot be transformed
		if (graphs != null) {
			// For each workflow graph within the process
			int graphCounter = 1;
			for (WorkflowGraph g : graphs) {
				info.put(g, "subgraph", graphCounter++);
				Mojo.analyzeWorkflowGraph(processName, g, info, list);
			}
		}

		return list;
	}

	/**
	 * Determine the maximum node id of the given workflow graph.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @return The maximum node id.
	 */
	public static int findMax(WorkflowGraph graph) {
		// The maximum is minimal the number of all nodes.
		int max = graph.getNodeListInclusive().size();

		// Determine the maximum.
		for (WGNode node : graph.getNodeListInclusive()) {
			if (node.getId() > max) {
				max = node.getId();
			}
		}
		return max + 1;
	}

	/**
	 * Create a node array map to guarantee fast access later during the
	 * analysis.
	 * 
	 * @param graph
	 *            The workflow graph.
	 * @return The node array map.
	 */
	public static WGNode[] createMap(WorkflowGraph graph, int max) {
		WGNode[] map = new WGNode[max];
		for (WGNode node : graph.getNodeListInclusive()) {
			map[node.getId()] = node;
		}
		return map;
	}

	/**
	 * Get the registered plan plugins.
	 * 
	 * @return The plan plugins.
	 */
	public static Map<String, PlanPlugin> getPlanPlugins() {
		return Mojo.planPlugins;
	}

}
