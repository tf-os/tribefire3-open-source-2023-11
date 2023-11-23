// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.tribefire.jinni.support.request.alias;

import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.configurableSequence;
import static com.braintribe.console.ConsoleOutputs.cyan;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.logging.Logger;
import com.braintribe.model.jinni.api.ListAliases;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.utils.MapTools;
import com.braintribe.wire.api.util.Lists;

/**
 * The Class AliasProcessorExpert outputs defined aliases and manages registration of new aliases.
 * 
 */
public class AliasProcessor implements ServiceProcessor<ListAliases, ConsoleOutput> {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(AliasProcessor.class);
	private JinniAlias jinniAlias;

	@Override
	public ConsoleOutput process(ServiceRequestContext requestContext, ListAliases request) {
		return getAliasOutput();
	}

	private ConsoleOutput getAliasOutput() {
		ConfigurableConsoleOutputContainer aliasOut = configurableSequence();
		try {
			printAliases(jinniAlias.getAliases());

		} catch (RuntimeException ioex) {
			String msg = "Error while processing aliases: " + ioex.getMessage();
			log.error(msg, ioex);
			aliasOut.append(msg);
			return aliasOut;
		}

		return aliasOut;
	}

	// DirectOutput histOut = DirectOutput.T.create();
	// List<HistoryEntry> history;
	//
	// try {
	// history = jinniHistory.getHistory();
	// } catch (IOException ioex) {
	// String msg = "Error while retrieving jinni history: " + ioex.getMessage();
	// log.error(msg, ioex);
	// histOut.setText(msg);
	// return histOut;
	// }
	//
	// if (CollectionUtils.isEmpty(history)) {
	// histOut.setText("No history entries found. Is this the first request? Please verify the contents of "
	// + jinniHistory.getHistoryDirectory().toAbsolutePath() + " directory.");
	// return histOut;
	// }
	//
	// if (request != null && request.getRepeat() != null) {
	// try {
	// repeatHistory(requestContext, histOut, history, request.getRepeat());
	// } catch (IOException ioex) {
	// String msg = "Error while repeating jinni history: " + ioex.getMessage();
	// log.error(msg, ioex);
	// histOut.setText(msg);
	// return histOut;
	// } catch (ArrayIndexOutOfBoundsException | NumberFormatException aioobex) {
	// return invalidRepeat(request, histOut, aioobex);
	// }
	// } else {
	// printHistory(histOut, history);
	// }
	// return histOut;
	// }
	//
	// /**
	// * Returns information about invalid repeat parameter value.
	// *
	// * @param request the request
	// * @param histOut the hist out
	// * @param ex the Exception being reported
	// * @return the direct output
	// */
	// private DirectOutput invalidRepeat(History request, DirectOutput histOut, RuntimeException ex) {
	// String msg = "Attempted a history repetition, but did not receive a valid history entry: '"
	// + request.getRepeat()
	// + "'. Please check 'jinni history' for valid choices, or try 'jinni history repeat=last'.";
	// log.error(msg, ex);
	// histOut.setText(msg);
	// return histOut;
	// }
	//
	// /**
	// * Repeats a historized command.
	// *
	// * @param requestContext the request context
	// * @param histOut the hist out
	// * @param history the history
	// * @param repeat the repeat
	// * @return the object
	// * @throws IOException Signals that an I/O exception has occurred.
	// */
	// private Object repeatHistory(ServiceRequestContext requestContext, DirectOutput histOut, List<HistoryEntry> history,
	// String repeat) throws IOException {
	// List<ConsoleOutput> out = Lists.list();
	// HistoryEntry histEntry = history.get(getIndex(history, repeat));
	//
	// out.add(brightWhite("As usual, history repeats itself.\n\n"));
	// out.add(cyan("Executing entry number " + repeat + "\n\n"));
	// out.add(printServiceRequest(histEntry.getRequestString()));
	//
	// Object retObj = histEntry.getRequest().eval(requestContext).get();
	// jinniHistory.historize(histEntry.getRequest());
	// return retObj;
	// }
	//
	// /**
	// * Converts repeat to history index.
	// *
	// * @param history
	// *
	// * @param repeat the repeat
	// * @return the index
	// */
	// private int getIndex(List<HistoryEntry> history, String repeat) {
	// if ("last".equals(repeat)) {
	// return history.size() - 1;
	// }
	//
	// return Integer.parseInt(repeat) - 1;
	// }
	//

	private void printAliases(Map<String, Path> aliases) {
		List<ConsoleOutput> out = Lists.list();

		out.add(cyan("There are " + aliases.keySet().size() + " aliases defined.\n"));
		if (!MapTools.isEmpty(aliases)) {
			out.addAll(aliases.entrySet().stream().map(this::printAlias).collect(Collectors.toList()));
		}

		// black magic
		println(sequence(out.toArray(new ConsoleOutput[out.size()])));

	}

	private ConsoleOutput printAlias(Map.Entry<String, Path> alias) {
		ConfigurableConsoleOutputContainer sequence = configurableSequence();

		sequence.append(yellow("\n----------------------------------------------------------\n\n"));
		sequence.append(sequence(brightWhite("Alias "), cyan(alias.getKey() + ":\n\n")));
		try {
			sequence.append(sequence( //
					cyan("\nService request: "), //
					printServiceRequest(jinniAlias.getRequestStringFromFile(alias.getValue())) //
			));
		} catch (IOException e) {
			sequence.append(red("Error retrieving the alias from path " + alias.getValue().toAbsolutePath()));
		}

		return sequence;
	}

	private ConsoleOutput printServiceRequest(String request) {
		ConfigurableConsoleOutputContainer sequence = configurableSequence();
		Scanner scanner = new Scanner(request);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.contains(":")) {
				sequence.append(yellow(line + "\n"));
			} else {
				sequence.append(yellow(line.split(":")[0] + ":"));
				sequence.append(brightWhite(line.split(":", 2)[1] + "\n"));
			}
		}
		scanner.close();

		return sequence;
	}

	public void setJinniAlias(JinniAlias jinniAlias) {
		this.jinniAlias = jinniAlias;
	}

}
