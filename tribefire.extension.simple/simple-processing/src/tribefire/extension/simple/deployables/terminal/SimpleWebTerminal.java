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
package tribefire.extension.simple.deployables.terminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;

/**
 * A simple {@link HttpServlet} which processes {@code GET} requests. It generates a basic HTML page with information about the incoming request. The
 * content of the response page to some extent depends on the terminal configuration, see {@link #setPrintRequestHeaders(boolean)} and
 * {@link #setPrintRequestParameters(boolean)}..
 *
 * @author michael.lafite
 */
public class SimpleWebTerminal extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** See {@link #setPrintRequestHeaders(boolean)}. */
	private boolean printRequestHeaders = false;
	/** See {@link #setPrintRequestParameters(boolean)}. */
	private boolean printRequestParameters = false;

	/** Specifies whether or not to print out the request headers. */
	public void setPrintRequestHeaders(boolean printRequestHeaders) {
		this.printRequestHeaders = printRequestHeaders;
	}

	/** Specifies whether or not to print out the request parameters. */
	@Configurable
	public void setPrintRequestParameters(boolean printRequestParameters) {
		this.printRequestParameters = printRequestParameters;
	}

	/**
	 * Processes the <code>request</code> adding a simple web page to the <code>response</code>.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

		PrintWriter writer = response.getWriter();

		writer.print("<h1>Simple Web Terminal!</h1>");
		writer.print("<p>");
		writer.print("<i>This is just a simple web terminal demo. Below you find information about the received HTTP request.</i>");
		writer.print("<h2>Request method: " + request.getMethod() + "</h2>");

		if (printRequestHeaders) {
			printHeaders(request, writer);
		}

		if (printRequestParameters) {
			printParams(request, writer);
		}
	}

	/**
	 * Prints information about the <code>request</code> parameters to the passed <code>writer</code>.
	 */
	private void printParams(HttpServletRequest request, PrintWriter writer) {
		writer.println("<h3>Parameters</h3>");
		Enumeration<String> paramNames = request.getParameterNames();
		writer.println("<table>");
		writer.println("<tr><th>Name</th><th>Value</th></tr>");
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			writer.print("<tr><td>");
			writer.print(paramName);
			writer.print("</td><td>");
			writer.print(request.getParameter(paramName));
			writer.println("</td></tr>");

		}
		writer.println("</table>");
	}

	/**
	 * Prints information about the <code>request</code> headers to the passed <code>writer</code>.
	 */
	private void printHeaders(HttpServletRequest request, PrintWriter writer) {
		writer.println("<h3>Headers</h3>");
		Enumeration<String> headerNames = request.getHeaderNames();
		writer.println("<table>");
		writer.println("<tr><th>Name</th><th>Value</th></tr>");
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			writer.print("<tr><td>");
			writer.print(headerName);
			writer.print("</td><td>");
			writer.print(request.getHeader(headerName));
			writer.println("</td></tr>");

		}
		writer.println("</table>");
	}
}
