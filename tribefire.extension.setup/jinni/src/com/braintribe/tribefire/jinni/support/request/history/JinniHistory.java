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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.tribefire.jinni.support.request.RequestPersistenceManipulator;

// TODO: turn one of the historized requests into an alias
// user would configure this with 'jinni alias'

/**
 * Deals with storing and retrieving request history.
 * 
 */
public class JinniHistory extends RequestPersistenceManipulator {

	public static final int MAX_HISTORY = 20;
	private static final Logger LOG = Logger.getLogger(JinniHistory.class);
	private static final String DIR_NAME = "history";
	private ModelAccessory modelAccessory;

	@Configurable
	@Required
	public void setModelAccessory(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;
	}

	public List<HistoryEntry> getHistory() throws IOException {
		return getHistoryEntries(getDirectory());
	}

	public void historize(ServiceRequest request) throws IOException {

		if (!modelAccessory.getCmdResolver().getMetaData().entity(request).useCase("history").is(Visible.T)) {
			LOG.debug(request.type().getTypeSignature() + " is not historized.");
			return;
		}

		// if (request instanceof History) {
		// LOG.debug("History requests are not historized..");
		// return;
		// }
		// if (request instanceof Help) {
		// LOG.debug("Help requests are not historized..");
		// return;
		// }
		// if (request instanceof Alias) {
		// LOG.debug("Alias requests are not historized..");
		// return;
		// }

		Path historyDirectory = getDirectory();

		if (requestsSimilar(request, getLastRequest(historyDirectory))) {
			LOG.info("Request similar to most recent one, historization disabled.");
			return;
		}

		pruneHistory(historyDirectory);

		EntityType<GenericEntity> jinniCall = request.entityType();

		Path newEntry = Paths.get(historyDirectory.toAbsolutePath() + SEPARATOR + getFilenameFromRequest(jinniCall));

		try (BufferedWriter writer = Files.newBufferedWriter(newEntry)) {

			new YamlMarshaller().marshall(writer, request, GmSerializationOptions.defaultOptions);

		} catch (MarshallException | IOException ex) {
			LOG.error("Error when writing history. ", ex);
			throw new IllegalStateException("History folder writing unsuccessful. Cause: " + ex.getMessage(), ex);
		}

	}

	private boolean requestsSimilar(ServiceRequest request, ServiceRequest lastRequest) {
		if (lastRequest == null) {
			return false;
		}

		EntityType<GenericEntity> entityType = request.entityType();
		EntityType<GenericEntity> lastType = lastRequest.entityType();

		if (!entityType.equals(lastType)) {
			return false;
		}

		return true;
	}

	private void pruneHistory(Path historyDirectory) throws IOException {

		List<Path> paths = getHistoryFiles(historyDirectory);

		if (paths.size() > MAX_HISTORY) {

			sortByModifiedAscending(paths);

			// delete all files after the cutoff
			for (int i = MAX_HISTORY - 1; i < paths.size(); i++) {
				try {
					Files.deleteIfExists(paths.get(i));
				} catch (IOException ioex) {
					LOG.error("Attempting to delete " + paths.get(i).toAbsolutePath() + " caused an error: " + ioex.getMessage(), ioex);
				}
			}
		}
	}

	private String getFilenameFromRequest(EntityType<GenericEntity> jinniCall) {
		// converts CamelCase to lower-dash-case
		return jinniCall.getShortName().replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase() + "." + new DateTime().toString("yyyyMMddHHmmss")
				+ ".yaml";
	}

	public List<ServiceRequest> getHistorizedServices(Path historyDirectory) throws IOException {
		return Files.walk(historyDirectory).filter(Files::isRegularFile).map(this::getServiceRequestFromFile).filter(r -> r != null)
				.collect(Collectors.toList());
	}

	private List<HistoryEntry> getHistoryEntries(Path historyDirectory) throws IOException {
		return Files.walk(historyDirectory).filter(Files::isRegularFile).map(this::getHistoryEntryFromFile)
				.sorted((h1, h2) -> h1.getDate().compareTo(h2.getDate())).collect(Collectors.toList());
	}

	private List<Path> getHistoryFiles(Path historyDirectory) throws IOException {
		return Files.walk(historyDirectory).filter(Files::isRegularFile).collect(Collectors.toList());
	}

	private ServiceRequest getLastRequest(Path historyDirectory) throws IOException {
		List<Path> historyEntries = sortByModifiedAscending(getHistoryFiles(historyDirectory));

		if (historyEntries.isEmpty()) {
			return null;
		}

		return getServiceRequestFromFile(historyEntries.get(0));
	}

	private HistoryEntry getHistoryEntryFromFile(Path path) {
		HistoryEntry entry = new HistoryEntry();

		try {
			entry.setDate(new DateTime(Files.getLastModifiedTime(path).toMillis()));
			entry.setFilename(path.getFileName().toString());
			entry.setRequestString(getRequestStringFromFile(path));
			entry.setRequest(getServiceRequestFromFile(path));
			return entry;
		} catch (MarshallException | IOException ex) {
			LOG.error("Error when reading history. ", ex);
			throw new IllegalStateException("History folder contains invalid entries. Purge is suggested. Cause: " + ex.getMessage(), ex);
		}
	}

	private List<Path> sortByModifiedAscending(List<Path> paths) {

		paths.sort((p1, p2) -> {
			try {
				return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
			} catch (IOException e) {
				LOG.error("Unable to compare paths " + p1.toAbsolutePath() + " and " + p2.toAbsolutePath());
				return 0;
			}
		});

		// returns modified collection for convenient chaining
		return paths;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.build.assets.RequestPersistenceManipulator#getDirName() */
	@Override
	protected String getDirName() {
		return DIR_NAME;
	}

}
