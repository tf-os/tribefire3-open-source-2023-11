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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.manipulation.marshaller.RemoteManipulationStringifier;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;

/**
 */
public class OfflineBufferedManipulationAppender implements AutoCloseable {

	private static final int LIMIT = 1000;

	private final BufferedWriter writer;
	private final RemoteManipulationStringifier stringifier;

	private final List<Manipulation> buffer = newList();

	public OfflineBufferedManipulationAppender(File destination) {
		validateDestinationFile(destination);

		this.stringifier = newRemoteStringifierWithTarget(destination);
		this.writer = newWriterTo(destination);
	}

	private void validateDestinationFile(File destination) {
		if (destination == null)
			throw new IllegalArgumentException("Cannot create appender. Destination file is null.");

		if (!destination.exists())
			throw new IllegalArgumentException("Cannot create appender for non-existent file: " + destination.getAbsolutePath());
	}

	private BufferedWriter newWriterTo(File destination) {
		try {
			return new BufferedWriter(new FileWriter(destination, true));

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while creating writer for: " + destination.getAbsolutePath());
		}
	}

	private RemoteManipulationStringifier newRemoteStringifierWithTarget(File destination) {
		Map<Object, String> vars = extractVariables(destination);

		RemoteManipulationStringifier result = new RemoteManipulationStringifier(extractTypeVars(vars), extractRefVars(vars), vars.values());
		result.setSingleBlock(true);

		return result;
	}

	private Map<Object, String> extractVariables(File gmmlFile) throws ManipulationPersistenceException {
		if (gmmlFile == null || !gmmlFile.exists())
			return newMap();

		ParseResponse response = CsaPersistenceTools.parseGmmlFile(gmmlFile, m -> { /* NOOP */ });
		return invertVariablesMap(response.variables);

	}

	private Map<Object, String> invertVariablesMap(Map<String, Object> variables) {
		Map<Object, String> result = newMap();

		for (Entry<String, Object> entry : variables.entrySet()) {
			String variable = entry.getKey();
			Object value = entry.getValue();

			String otherVariable = result.put(value, variable);
			if (otherVariable != null && !(value instanceof GenericEntity))
				throw new IllegalStateException("Cannot create value to variable map, as two different variables ('" + variable + "', '"
						+ otherVariable + "' map to the same non-entity value: " + value);
		}

		return result;
	}

	private static Map<String, String> extractTypeVars(Map<Object, String> dataVariables) {
		return dataVariables.entrySet().stream() //
				.filter(e -> (e.getKey() instanceof String)) //
				.collect(Collectors.toMap( //
						e -> (String) e.getKey(), //
						Map.Entry::getValue //
		));
	}

	private static Map<EntityReference, String> extractRefVars(Map<Object, String> dataVariables) {
		return dataVariables.entrySet().stream() //
				.filter(OfflineBufferedManipulationAppender::isEntityReferenceEntry) //
				.map(e -> (Map.Entry<EntityReference, String>) (Map.Entry<?, ?>) e) //
				.collect(Collectors.toMap( //
						Map.Entry::getKey, //
						Map.Entry::getValue //
		));
	}

	private static boolean isEntityReferenceEntry(Map.Entry<?, ?> e) {
		Object o = e.getKey();
		return o instanceof EntityReference ? ((EntityReference) o).session() == null : false;
	}

	public void append(Manipulation manipulation) {
		buffer.add(manipulation);

		if (buffer.size() >= LIMIT)
			flush();
	}

	public void flush() {
		try {
			tryFlush();

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while flushing manipulation buffer.");
		}
	}

	private void tryFlush() throws ManipulationPersistenceException, IOException {
		switch (buffer.size()) {
			case 0:
				return;
			case 1:
				_append(first(buffer));
				return;
			default:
				_append(compound(buffer));
		}

		buffer.clear();
	}

	private void _append(Manipulation manipulation) throws IOException {
		stringifier.stringify(writer, manipulation);
		writer.flush();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	
}
