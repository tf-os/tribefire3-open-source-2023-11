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
package com.braintribe.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;

import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConfigurableMultiElementConsoleOutputContainer;
import com.braintribe.console.output.ConsoleDynamicText;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.console.output.ConsoleStaticText;
import com.braintribe.console.output.ConsoleText;
import com.braintribe.console.output.MultiElementConsoleOutputContainer;
import com.braintribe.console.output.SingleElementConsoleOutputContainer;

public interface ConsoleOutputs extends ConsoleStyles {

	static Console print(ConsoleOutput output) {
		return Console.get().print(output);
	}

	static Console print(String text) {
		return Console.get().print(text);
	}

	static Console println(ConsoleOutput output) {
		return Console.get().println(output);
	}

	static Console println(String text) {
		return Console.get().println(text);
	}

	static Console println() {
		return Console.get().println("");
	}

	static ConsoleText text(CharSequence s) {
		return new ConsoleStaticText(s);
	}

	static ConsoleText text(Consumer<Appendable> writer) {
		return new ConsoleDynamicText(writer);
	}

	static ConfigurableConsoleOutputContainer configurableSequence() {
		return new ConfigurableMultiElementConsoleOutputContainer();
	}

	static ConsoleOutputContainer sequence(ConsoleOutput... elements) {
		return new MultiElementConsoleOutputContainer(0, elements);
	}

	static ConsoleOutput underline(ConsoleOutput output) {
		return styled(ST_UNDERLINE, output);
	}

	static ConsoleOutput underline(String text) {
		return styled(ST_UNDERLINE, text(text));
	}

	static ConsoleOutput black(ConsoleOutput output) {
		return styled(FG_BLACK, output);
	}

	static ConsoleOutput black(String text) {
		return styled(FG_BLACK, text(text));
	}

	static ConsoleOutput red(ConsoleOutput output) {
		return styled(FG_RED, output);
	}

	static ConsoleOutput red(String text) {
		return styled(FG_RED, text(text));
	}

	static ConsoleOutput green(ConsoleOutput output) {
		return styled(FG_GREEN, output);
	}

	static ConsoleOutput green(String text) {
		return styled(FG_GREEN, text(text));
	}

	static ConsoleOutput yellow(ConsoleOutput output) {
		return styled(FG_YELLOW, output);
	}

	static ConsoleOutput yellow(String text) {
		return styled(FG_YELLOW, text(text));
	}

	static ConsoleOutput blue(ConsoleOutput output) {
		return styled(FG_BLUE, output);
	}

	static ConsoleOutput blue(String text) {
		return styled(FG_BLUE, text(text));
	}

	static ConsoleOutput magenta(ConsoleOutput output) {
		return styled(FG_MAGENTA, output);
	}

	static ConsoleOutput magenta(String text) {
		return styled(FG_MAGENTA, text(text));
	}

	static ConsoleOutput cyan(ConsoleOutput output) {
		return styled(FG_CYAN, output);
	}

	static ConsoleOutput cyan(String text) {
		return styled(FG_CYAN, text(text));
	}

	static ConsoleOutput white(ConsoleOutput output) {
		return styled(FG_WHITE, output);
	}

	static ConsoleOutput white(String text) {
		return styled(FG_WHITE, text(text));
	}
	static ConsoleOutput brightBlack(ConsoleOutput output) {
		return styled(FG_BRIGHT_BLACK, output);
	}

	static ConsoleOutput brightBlack(String text) {
		return styled(FG_BRIGHT_BLACK, text(text));
	}

	static ConsoleOutput brightRed(ConsoleOutput output) {
		return styled(FG_BRIGHT_RED, output);
	}

	static ConsoleOutput brightRed(String text) {
		return styled(FG_BRIGHT_RED, text(text));
	}

	static ConsoleOutput brightGreen(ConsoleOutput output) {
		return styled(FG_BRIGHT_GREEN, output);
	}

	static ConsoleOutput brightGreen(String text) {
		return styled(FG_BRIGHT_GREEN, text(text));
	}

	static ConsoleOutput brightYellow(ConsoleOutput output) {
		return styled(FG_BRIGHT_YELLOW, output);
	}

	static ConsoleOutput brightYellow(String text) {
		return styled(FG_BRIGHT_YELLOW, text(text));
	}

	static ConsoleOutput brightBlue(ConsoleOutput output) {
		return styled(FG_BRIGHT_BLUE, output);
	}

	static ConsoleOutput brightBlue(String text) {
		return styled(FG_BRIGHT_BLUE, text(text));
	}

	static ConsoleOutput brightMagenta(ConsoleOutput output) {
		return styled(FG_BRIGHT_MAGENTA, output);
	}

	static ConsoleOutput brightMagenta(String text) {
		return styled(FG_BRIGHT_MAGENTA, text(text));
	}

	static ConsoleOutput brightCyan(ConsoleOutput output) {
		return styled(FG_BRIGHT_CYAN, output);
	}

	static ConsoleOutput brightCyan(String text) {
		return styled(FG_BRIGHT_CYAN, text(text));
	}

	static ConsoleOutput brightWhite(ConsoleOutput output) {
		return styled(FG_BRIGHT_WHITE, output);
	}

	static ConsoleOutput brightWhite(String text) {
		return styled(FG_BRIGHT_WHITE, text(text));
	}

	static ConsoleOutput blackBg(ConsoleOutput output) {
		return styled(BG_BLACK, output);
	}

	static ConsoleOutput blackBg(String text) {
		return styled(BG_BLACK, text(text));
	}

	static ConsoleOutput redBg(ConsoleOutput output) {
		return styled(BG_RED, output);
	}

	static ConsoleOutput redBg(String text) {
		return styled(BG_RED, text(text));
	}

	static ConsoleOutput greenBg(ConsoleOutput output) {
		return styled(BG_GREEN, output);
	}

	static ConsoleOutput greenBg(String text) {
		return styled(BG_GREEN, text(text));
	}

	static ConsoleOutput yellowBg(ConsoleOutput output) {
		return styled(BG_YELLOW, output);
	}

	static ConsoleOutput yellowBg(String text) {
		return styled(BG_YELLOW, text(text));
	}

	static ConsoleOutput blueBg(ConsoleOutput output) {
		return styled(BG_BLUE, output);
	}

	static ConsoleOutput blueBg(String text) {
		return styled(BG_BLUE, text(text));
	}

	static ConsoleOutput magentaBg(ConsoleOutput output) {
		return styled(BG_MAGENTA, output);
	}

	static ConsoleOutput magentaBg(String text) {
		return styled(BG_MAGENTA, text(text));
	}

	static ConsoleOutput cyanBg(ConsoleOutput output) {
		return styled(BG_CYAN, output);
	}

	static ConsoleOutput cyanBg(String text) {
		return styled(BG_CYAN, text(text));
	}

	static ConsoleOutput whiteBg(ConsoleOutput output) {
		return styled(BG_WHITE, output);
	}

	static ConsoleOutput whiteBg(String text) {
		return styled(BG_WHITE, text(text));
	}

	static ConsoleOutput brightBlackBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_BLACK, output);
	}

	static ConsoleOutput brightBlackBg(String text) {
		return styled(BG_BRIGHT_BLACK, text(text));
	}

	static ConsoleOutput brightRedBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_RED, output);
	}

	static ConsoleOutput brightRedBg(String text) {
		return styled(BG_BRIGHT_RED, text(text));
	}

	static ConsoleOutput brightGreenBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_GREEN, output);
	}

	static ConsoleOutput brightGreenBg(String text) {
		return styled(BG_BRIGHT_GREEN, text(text));
	}

	static ConsoleOutput brightYellowBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_YELLOW, output);
	}

	static ConsoleOutput brightYellowBg(String text) {
		return styled(BG_BRIGHT_YELLOW, text(text));
	}

	static ConsoleOutput brightBlueBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_BLUE, output);
	}

	static ConsoleOutput brightBlueBg(String text) {
		return styled(BG_BRIGHT_BLUE, text(text));
	}

	static ConsoleOutput brightMagentaBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_MAGENTA, output);
	}

	static ConsoleOutput brightMagentaBg(String text) {
		return styled(BG_BRIGHT_MAGENTA, text(text));
	}

	static ConsoleOutput brightCyanBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_CYAN, output);
	}

	static ConsoleOutput brightCyanBg(String text) {
		return styled(BG_BRIGHT_CYAN, text(text));
	}

	static ConsoleOutput brightWhiteBg(ConsoleOutput output) {
		return styled(BG_BRIGHT_WHITE, output);
	}

	static ConsoleOutput brightWhiteBg(String text) {
		return styled(BG_BRIGHT_WHITE, text(text));
	}

	static ConsoleOutput styled(int style, ConsoleOutput output) {
		return new SingleElementConsoleOutputContainer(style, output);
	}

	static ConsoleOutput styled(int style, ConsoleOutput... outputs) {
		return new MultiElementConsoleOutputContainer(style, outputs);
	}

	static ConsoleOutput styled(int style, String text) {
		return new SingleElementConsoleOutputContainer(style, text(text));
	}

	static ConsoleOutput spaces(int len) {
		// Java 11+
		// return ConsoleOutputs.text(" ".repeat(len));

		StringBuilder builder = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			builder.append(" ");
		return ConsoleOutputs.text(builder.toString());
	}

	static Collector<ConsoleOutput, ?, ConsoleOutput> joiningCollector(ConsoleOutput delimiter) {
		return joiningCollector(delimiter, null, null);
	}

	/** Both prefix and suffix can be <tt>null</tt>, meaning those values are empty. */
	static Collector<ConsoleOutput, ?, ConsoleOutput> joiningCollector(ConsoleOutput delimiter, ConsoleOutput prefix, ConsoleOutput suffix) {
		Objects.requireNonNull(delimiter);

		return Collector.<ConsoleOutput, OutputJoiner, ConsoleOutput> of( //
				() -> new OutputJoiner(delimiter, prefix, suffix), //
				OutputJoiner::append, //
				OutputJoiner::merge, //
				OutputJoiner::finish);
	}

}

class OutputJoiner {
	private final ConsoleOutput delimiter;
	private final ConsoleOutput prefix;
	private final ConsoleOutput suffix;

	private final List<ConsoleOutput> buffer = new ArrayList<>();

	public OutputJoiner(ConsoleOutput delimiter, ConsoleOutput prefix, ConsoleOutput suffix) {
		this.delimiter = delimiter;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public OutputJoiner append(ConsoleOutput output) {
		if (output != null)
			buffer.add(output);
		return this;
	}

	public OutputJoiner merge(OutputJoiner other) {
		buffer.addAll(other.buffer);
		return this;
	}

	public ConsoleOutput finish() {
		ConfigurableConsoleOutputContainer sequence = ConsoleOutputs.configurableSequence();
		if (prefix != null)
			sequence.append(prefix);

		boolean first = true;
		for (ConsoleOutput output : buffer) {
			if (first)
				first = false;
			else
				sequence.append(delimiter);

			sequence.append(output);
		}

		if (suffix != null)
			sequence.append(suffix);

		return sequence;
	}

}
