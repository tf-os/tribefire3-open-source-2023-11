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
package com.braintribe.model.processing.elasticsearch.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.ThrowableTools;

public abstract class ReportContext {

	protected Exception exception;
	protected String message = null;
	protected String detailedMessage = null;
	protected PersistenceGmSession session;

	public ReportContext(PersistenceGmSession session) {
		this.session = session;
	}

	/**
	 * If an exception is thrown, it is added to the report.
	 *
	 * @param e
	 *            - The exception thrown
	 */
	public void registerException(Exception e) {
		this.exception = e;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");

		if (exception != null) {

			sb.append(lineSeparator);
			sb.append("An exception was thrown during the migration process.");
			sb.append(lineSeparator);
			sb.append("---- Start of Exception Trace ----");
			sb.append(lineSeparator);
			sb.append(ThrowableTools.getStackTraceString(exception));
			sb.append(lineSeparator);
			sb.append("---- End of Exception Trace ----");

		} else {

			if (this.message != null) {
				sb.append(lineSeparator);
				sb.append(lineSeparator);
				sb.append("---- Report Summary ----");
				sb.append(lineSeparator);
				sb.append(this.message);

				sb.append(lineSeparator);
			}

			sb.append(lineSeparator);
			sb.append(lineSeparator);
			sb.append("The migration process took: ");

		}

		if (this.detailedMessage != null) {
			sb.append(lineSeparator);
			sb.append(lineSeparator);
			sb.append("---- Start of detailed Report----");
			sb.append(lineSeparator);
			sb.append(this.detailedMessage);
			sb.append(lineSeparator);
			sb.append("---- End of detailed Report ----");
			sb.append(lineSeparator);
		}

		return sb.toString();
	}

	/**
	 * Creates a well-formed report file with information about the migration process:
	 * <ul>
	 * <li>The id of the access the data is migrated from (source access)</li>
	 * <li>The id of the access the data is migrated to (destination access)</li>
	 * <li>The duration of the migration process (in milliseconds)</li>
	 * <li>Number of entities clonable</li>
	 * <li>Number of entities cloned</li>
	 * <li>In case of an exception the exception trace</li>
	 * </ul>
	 *
	 * @return - A {@link com.braintribe.model.resource.Resource} containing all the relevant information
	 *
	 */
	public Resource createReport() {

		Date now = new Date();
		String fileName = "report_" + now.getTime() + ".txt";
		String content = this.toString();

		Resource resource = session.resources().create().name(fileName).mimeType("text/plain")
				.store(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

		resource.setCreated(new Date());

		return resource;
	}

	/**
	 * If there is no exception thrown, it is still possible that the migration was stopped. This could happen due to
	 * wrong configuration.
	 *
	 * Also if the migration was successful, a message is created.
	 */
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * The whole log output of the migration process
	 */
	public String getDetailedMessage() {
		return detailedMessage;
	}

	public Exception getException() {
		return exception;
	}

	public void append(String m) {
		Date now = new Date();
		m = "[" + now.toString() + "] " + m;

		if (detailedMessage == null) {
			detailedMessage = m;

		} else {
			StringBuilder b = new StringBuilder(detailedMessage);
			b.append(System.getProperty("line.separator"));
			b.append(m);
			detailedMessage = b.toString();
		}
	}
}
