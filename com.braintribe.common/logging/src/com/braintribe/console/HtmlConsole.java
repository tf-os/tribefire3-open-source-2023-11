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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;

import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.console.output.ConsoleText;

public class HtmlConsole implements Console, ConsoleStyles, AutoCloseable {
	
	private static final String PLACEHOLDER_CONTENT = "${content}";
	private Appendable out;
	private String postText;
	
	public HtmlConsole(Appendable out) {
		this.out = out;
		
		URL url = getClass().getResource("pre-framing.html");
		
		String preFrameingHtml = null;
		
		char buffer[] = new char[64000];
		
		try (StringWriter writer = new StringWriter(); BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
			int bytesRead;
			
			while ((bytesRead = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, bytesRead);
			}
			
			preFrameingHtml = writer.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		int index = preFrameingHtml.lastIndexOf(PLACEHOLDER_CONTENT);
		
		printUnescaped(preFrameingHtml.substring(0, index));
		postText = preFrameingHtml.substring(index + PLACEHOLDER_CONTENT.length());
	}
	
	public HtmlConsole(Appendable out, String preText, String postText) {
		super();
		this.out = out;
		this.postText = postText;
		
		if (preText != null)
			printUnescaped(preText);
	}
	
	private Console printUnescaped(String s) {
		try {
			out.append(s);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return this;
	}

	@Override
	public Console print(ConsoleOutput output) {
		switch (output.kind()) {
		case container: return print((ConsoleOutputContainer)output);
		case text: return print(((ConsoleText)output).getText().toString());
		default: return this;
		}
	}

	private String getClasses(int style) {
		switch (style) {
		case ST_BRIGHT: return "ST_BRIGHT";
		case ST_UNDERLINE: return "ST_UNDERLINE";
			
		case FG_BLACK: return "FG_BLACK";
		case FG_RED: return "FG_RED";
		case FG_GREEN: return "FG_GREEN";
		case FG_YELLOW: return "FG_YELLOW";
		case FG_BLUE: return "FG_BLUE";
		case FG_MAGENTA: return "FG_MAGENTA";
		case FG_CYAN: return "FG_CYAN";
		case FG_WHITE: return "FG_WHITE";
		case FG_DEFAULT: return "FG_DEFAULT";
		case FG_BRIGHT_BLACK: return "FG_BRIGHT_BLACK";
		case FG_BRIGHT_RED: return "FG_BRIGHT_RED";
		case FG_BRIGHT_GREEN: return "FG_BRIGHT_GREEN";
		case FG_BRIGHT_YELLOW: return "FG_BRIGHT_YELLOW";
		case FG_BRIGHT_BLUE: return "FG_BRIGHT_BLUE";
		case FG_BRIGHT_MAGENTA: return "FG_BRIGHT_MAGENTA";
		case FG_BRIGHT_CYAN: return "FG_BRIGHT_CYAN";
		case FG_BRIGHT_WHITE:return "FG_BRIGHT_WHITE";
			
		case BG_BLACK: return "BG_BLACK";
		case BG_RED: return "BG_RED";
		case BG_GREEN: return "BG_GREEN";
		case BG_YELLOW: return "BG_YELLOW";
		case BG_BLUE: return "BG_BLUE";
		case BG_MAGENTA: return "BG_MAGENTA";
		case BG_CYAN: return "BG_CYAN";
		case BG_WHITE: return "BG_WHITE";
		case BG_DEFAULT: return "BG_DEFAULT";
		case BG_BRIGHT_BLACK: return "BG_BRIGHT_BLACK";
		case BG_BRIGHT_RED: return "BG_BRIGHT_RED";
		case BG_BRIGHT_GREEN: return "BG_BRIGHT_GREEN";
		case BG_BRIGHT_YELLOW: return "BG_BRIGHT_YELLOW";
		case BG_BRIGHT_BLUE: return "BG_BRIGHT_BLUE";
		case BG_BRIGHT_MAGENTA: return "BG_BRIGHT_MAGENTA";
		case BG_BRIGHT_CYAN: return "BG_BRIGHT_CYAN";
		case BG_BRIGHT_WHITE:return "BG_BRIGHT_WHITE";
		case 0: return null;
		default: return null;
		}
	}
	
	private Console print(ConsoleOutputContainer container) {
		String classes = getClasses(container.getStyle());

		if (classes != null) {
			printUnescaped("<span class='");
			printUnescaped(classes);
			printUnescaped("'>");
			printElements(container);
			printUnescaped("</span>");
		}
		else {
			printElements(container);
		}
		
		return this;
	}
	
	private void printElements(ConsoleOutputContainer container) {
		int size = container.size();
		
		for (int i = 0; i < size; i++) {
			ConsoleOutput consoleOutput = container.get(i);
			print(consoleOutput);
		}
	}
	
	@Override
	public Console print(String text) {
		return printUnescaped(escape(text));
	}

	@Override
	public Console println(ConsoleOutput output) {
		print(output);
		return printUnescaped("\n");
	}

	@Override
	public Console println(String text) {
		printUnescaped(escape(text));
		return printUnescaped("\n");
	}
	
	private static String escape(String s) {
		int c = s.length();
		
		if (c == 0)
			return s;

		char[] chars = new char[c];
		s.getChars(0, c, chars, 0);

		StringBuilder b = new StringBuilder(c * 2);

		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];

			switch (ch) {
			case '&':
				b.append("&amp;");
				break;
			case '<':
				b.append("&lt;");
				break;
			case '>':
				b.append("&gt;");
				break;
			case '"':
				b.append("&quot;");
				break;
			case '\'':
				b.append("&apos;");
				break;
			default:
				if (ch > 0x7F) {
					b.append("&#");
					b.append(Integer.toString(ch, 10));
					b.append(';');
				} else {
					b.append(ch);
				}
			}
		}
		
		return b.toString();
	}

	@Override
	public void close() {
		if (postText != null)
			printUnescaped(postText);
	}
}
