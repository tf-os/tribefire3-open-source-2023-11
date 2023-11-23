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
package com.braintribe.tribefire.jinni.support.request.history;

import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.configurableSequence;
import static com.braintribe.console.ConsoleOutputs.cyan;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.joda.time.format.DateTimeFormat;

import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.logging.Logger;
import com.braintribe.model.jinni.api.History;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.tribefire.jinni.support.request.alias.JinniAlias;
import com.braintribe.utils.CollectionTools;
import com.braintribe.wire.api.util.Lists;

/**
 * The Class HistoryProcessorExpert outputs history and manages repetition of recorded commands.
 * 
 */
public class HistoryProcessor implements ServiceProcessor<History, ConsoleOutput> {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HistoryProcessor.class);
	private JinniHistory jinniHistory;
	private JinniAlias jinniAlias;

	@Override
	public ConsoleOutput process(ServiceRequestContext requestContext, History request) {
		return getHistoryOutput(requestContext, request);
	}

	private ConsoleOutput getHistoryOutput(ServiceRequestContext requestContext, History request) {
		ConfigurableConsoleOutputContainer histOut = configurableSequence();

		List<HistoryEntry> history;

		try {
			history = jinniHistory.getHistory();
		} catch (IOException ioex) {
			String msg = "Error while retrieving jinni history: " + ioex.getMessage();
			log.error(msg, ioex);
			histOut.append(msg);
			return histOut;
		}

		if (CollectionTools.isEmpty(history)) {
			histOut.append("No history entries found. Is this the first request? Please verify the contents of "
					+ jinniHistory.getDirectory().toAbsolutePath() + " directory.");
			return histOut;
		}

		if (request != null && request.getRepeat() != null) {
			try {
				if (request.getAlias() != null) {
					historyToAlias(history, request.getAlias(), request.getRepeat());
				} else {
					repeatHistory(requestContext, history, request.getRepeat());
				}
			} catch (IOException ioex) {
				String msg = "Error while repeating jinni history: " + ioex.getMessage();
				log.error(msg, ioex);
				histOut.append(msg);
				return histOut;
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException aioobex) {
				return invalidRepeat(request, histOut, aioobex);
			}
		} else {
			printHistory(histOut, history);
		}

		return histOut;
	}

	private void historyToAlias(List<HistoryEntry> history, String alias, String repeat) {
		List<ConsoleOutput> out = Lists.list();
		HistoryEntry histEntry = history.get(getIndex(history, repeat));

		out.add(brightWhite("The history entry will be stored under alias " + alias + ".\n\n"));
		out.add(printServiceRequest(histEntry.getRequestString()));
		println(sequence(out.toArray(new ConsoleOutput[out.size()])));

		jinniAlias.createAlias(alias, histEntry.getRequest());
	}

	private ConsoleOutput invalidRepeat(History request, ConfigurableConsoleOutputContainer histOut, RuntimeException ex) {
		String msg = "Attempted a history repetition, but did not receive a valid history entry: '" + request.getRepeat()
				+ "'. Please check 'jinni history' for valid choices, or try 'jinni history repeat=last'.";
		log.error(msg, ex);
		histOut.append(msg);
		return histOut;
	}

	private Object repeatHistory(ServiceRequestContext requestContext, List<HistoryEntry> history, String repeat)
			throws IOException {
		List<ConsoleOutput> out = Lists.list();
		HistoryEntry histEntry = history.get(getIndex(history, repeat));

		out.add(brightWhite("As usual, history repeats itself.\n\n"));
		out.add(cyan("Executing entry number " + repeat + "\n\n"));
		out.add(printServiceRequest(histEntry.getRequestString()));
		println(sequence(out.toArray(new ConsoleOutput[out.size()])));

		Object retObj = histEntry.getRequest().eval(requestContext).get();
		jinniHistory.historize(histEntry.getRequest());
		return retObj;
	}

	private int getIndex(List<HistoryEntry> history, String repeat) {
		if ("last".equals(repeat)) {
			return history.size() - 1;
		}

		return Integer.parseInt(repeat) - 1;
	}

	private void printHistory(ConfigurableConsoleOutputContainer histOut, List<HistoryEntry> history) {
		List<ConsoleOutput> out = Lists.list();

		out.add(cyan("There are " + history.size() + " history entries stored out of " + JinniHistory.MAX_HISTORY + " maximum. ("
				+ history.size() * 100 / JinniHistory.MAX_HISTORY + "%)\n"));
		out.addAll(history.stream().map(he -> printHistoryEntry(he, history.indexOf(he))).collect(Collectors.toList()));

		println(sequence(out.toArray(new ConsoleOutput[out.size()])));

		histOut.append("History output successful.");
	}

	private ConsoleOutput printHistoryEntry(HistoryEntry entry, int index) {
		ConfigurableConsoleOutputContainer sequence = configurableSequence();

		sequence.append(yellow("\n----------------------------------------------------------\n\n"));
		sequence.append(yellow("History Entry #" + (index + 1) + ":\n\n"));
		sequence.append(sequence(cyan("Filename: "), brightWhite(entry.getFilename())));
		sequence.append(sequence(cyan("\nExecuted: "), brightWhite(entry.getDate().toString(DateTimeFormat.fullDateTime()))));
		sequence.append(sequence(cyan("\nContent: "), printServiceRequest(entry.getRequestString())));

		return sequence;
	}

	private ConsoleOutput printServiceRequest(String requestString) {
		ConfigurableConsoleOutputContainer sequence = configurableSequence();
		Scanner scanner = new Scanner(requestString);

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

	public void setJinniHistory(JinniHistory jinniHistory) {
		this.jinniHistory = jinniHistory;
	}

	public void setJinniAlias(JinniAlias jinniAlias) {
		this.jinniAlias = jinniAlias;
	}

}
