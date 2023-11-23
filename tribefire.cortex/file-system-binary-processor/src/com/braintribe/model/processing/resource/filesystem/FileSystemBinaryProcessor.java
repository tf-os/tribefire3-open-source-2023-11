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
package com.braintribe.model.processing.resource.filesystem;

import static com.braintribe.exception.Exceptions.unchecked;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.NullSafe.nonNull;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.resource.filesystem.path.FsPathResolver;
import com.braintribe.model.processing.resource.filesystem.path.StaticFsPathResolver;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceEventSource;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceListener;
import com.braintribe.model.processing.resource.streaming.AbstractFsBasedBinaryRetriever;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.StaticSource;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

/**
 * A {@link ServiceProcessor } for {@link BinaryRequest} for {@link Resource}s backed by {@link FileSystemSource}s.
 */
public class FileSystemBinaryProcessor extends AbstractFsBasedBinaryRetriever implements BinaryPersistenceEventSource {

	// constants
	private static final Logger log = Logger.getLogger(FileSystemBinaryProcessor.class);

	// configurable
	protected FsPathResolver fsPathResolver;

	protected DateTimeFormatter timestampPathFormat = DateTimeFormatter.ofPattern("yyMM/ddHH/mmss").withLocale(Locale.US);
	protected Function<Resource, String> resourcePathSupplier = this::resolveUploadPath;
	private List<BinaryPersistenceListener> persistenceListeners = emptyList();

	/** Just a convenience shortcut for {@code setFsPathResolver(StaticFsPathResolver.newInstance(basePath))}. */
	public void setBasePath(Path basePath) {
		setFsPathResolver(StaticFsPathResolver.newInstance(basePath));
	}

	@Required
	public void setFsPathResolver(FsPathResolver fsPathResolver) {
		this.fsPathResolver = fsPathResolver;
	}

	@Configurable
	public void setTimestampPathFormat(DateTimeFormatter timestampPathFormat) {
		this.timestampPathFormat = timestampPathFormat;
	}

	@Configurable
	public void setResourcePathSupplier(Function<Resource, String> resourcePathSupplier) {
		this.resourcePathSupplier = nonNull(resourcePathSupplier, "resourcePathSupplier");
	}

	@Override
	public void addPersistenceListener(BinaryPersistenceListener listener) {
		nonNull(listener, "listener");

		List<BinaryPersistenceListener> newListeners = newList(persistenceListeners);
		newListeners.add(listener);

		persistenceListeners = newListeners;
	}

	@Override
	public void removePersistenceListener(BinaryPersistenceListener listener) {
		nonNull(listener, "listener");

		List<BinaryPersistenceListener> newListeners = newList(persistenceListeners);
		newListeners.remove(listener);

		persistenceListeners = newListeners;
	}

	@Override
	protected Path resolvePathForRetrieval(BinaryRetrievalRequest request) {
		Resource resource = request.getResource();

		FileSystemSource source = retrieveFileSystemSource(resource);

		return resolveSourcePath(request.getDomainId(), source, true);
	}

	@Override
	public StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {
		Resource resource = request.getCreateFrom();
		FileSystemSource resourceSource = createSource(resource);

		Path path = ensureNonExistingSourcePath(request, resourceSource);

		stream(resource, path);

		Resource managedResource = createResource(null, resource, resourceSource);

		notifyListenersOnStore(context, request, managedResource);

		StoreBinaryResponse response = StoreBinaryResponse.T.create();
		response.setResource(managedResource);

		return response;
	}

	private Path ensureNonExistingSourcePath(StoreBinary request, FileSystemSource resourceSource) {
		Path path = resolveSourcePath(request.getDomainId(), resourceSource, false);

		// Make sure there isn't already a file with that path for another resource source.
		// If the same resource gets updated multiple times in a second this might actually happen.
		while (!createNewFile(path)) {
			String newPath = prependTimeStampedPath(UUID.randomUUID().toString());
			resourceSource.setPath(newPath);
			path = resolveSourcePath(request.getDomainId(), resourceSource, false);
		}

		return path;
	}

	private static boolean createNewFile(Path path) {
		File parentFile = path.toFile().getParentFile();

		if (parentFile != null)
			parentFile.mkdirs();

		try {
			// java.io.File.createNewFile() is atomic
			return path.toFile().createNewFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void notifyListenersOnStore(ServiceRequestContext context, StoreBinary request, Resource managedResource) {
		for (BinaryPersistenceListener listener : persistenceListeners) {
			try {
				listener.onStore(context, request, managedResource);

			} catch (Exception e) {
				log.warn("BinaryPersistenceListener threw an exception on store event.", e);
			}
		}
	}

	@Override
	public DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary request) {

		FileSystemSource source = retrieveFileSystemSource(request.getResource());

		Path path = resolveSourcePath(request.getDomainId(), source, true);

		try {
			log.trace(() -> "Deleting file: " + path);
			Files.deleteIfExists(path);

			String currentRelativePath = DateTools.encode(new Date(), timestampPathFormat);
			List<Path> unusedFolders = collectUnusedAndEmptyParentFolders(request.getDomainId(), path, currentRelativePath);
			for (Path unusedFolder : unusedFolders) {
				try {
					log.trace(() -> "Deleting unused folder: " + path);
					Files.deleteIfExists(unusedFolder);
				} catch (Exception e) {
					log.debug(() -> "Could not delete presumably empty folder: " + unusedFolder, e);
				}
			}

		} catch (IOException e) {
			throw unchecked(e, "Failed to delete " + path);
		}

		notifyListenersOnDelete(context, request);

		return DeleteBinaryResponse.T.create();
	}

	protected List<Path> collectUnusedAndEmptyParentFolders(String accessId, Path filePath, String currentRelativePath) {
		List<Path> result = new ArrayList<>();
		if (accessId != null && filePath != null) {
			Path basePath = fsPathResolver.resolveDomainPath(accessId);
			Path fileFolder = filePath.getParent();

			if (!fileFolder.startsWith(basePath)) {
				log.debug(() -> "The file path " + fileFolder + " does apparently not start with the base path " + basePath);
				return Collections.EMPTY_LIST;
			}

			List<File> emptySuperFolders = null;
			try {
				emptySuperFolders = FileTools.collectEmptySuperFolders(fileFolder.toFile(), basePath.toFile());
			} catch (Exception e) {
				log.debug(() -> "Error while trying to collect empty super folders of " + fileFolder, e);
				return Collections.EMPTY_LIST;
			}
			if (log.isTraceEnabled()) {
				log.trace("Got empty super folders: " + emptySuperFolders);
			}

			if (emptySuperFolders == null || emptySuperFolders.isEmpty()) {
				log.trace(() -> "No empty super folders of " + filePath + " found.");
				return Collections.EMPTY_LIST;
			}

			log.trace(() -> "The current relative path is: " + currentRelativePath);

			String[] checkFolderParts = currentRelativePath.split("/");
			Path checkPath = basePath.resolve(currentRelativePath);

			for (int i = 0; i < checkFolderParts.length && i < emptySuperFolders.size(); ++i) {
				Path emptyPath = emptySuperFolders.get(i).toPath();

				if (log.isTraceEnabled()) {
					log.trace("The current empty path is: " + emptyPath + ", check path is: " + checkPath);
				}

				if (emptyPath.equals(checkPath)) {
					if (log.isTraceEnabled()) {
						log.trace("The current empty path " + emptyPath + " equals the check path " + checkPath + ". Stopping here.");
					}
					break;
				} else {
					log.trace(() -> "Adding empty path " + emptyPath);
					result.add(emptyPath);
				}
				checkPath = checkPath.getParent();
			}
		}
		return result;
	}

	private void notifyListenersOnDelete(ServiceRequestContext context, DeleteBinary request) {
		for (BinaryPersistenceListener listener : persistenceListeners) {
			try {
				listener.onDelete(context, request);

			} catch (Exception e) {
				log.warn("BinaryPersistenceListener threw an exception on delete event.", e);
			}
		}
	}

	protected void stream(Resource callResource, Path path) {
		// @formatter:off
		try (
			OutputStream out = newOutputStream(path);
			InputStream in = callResource.openStream()
		) {
			IOTools.pump(in, out);
		} catch (IOException e) {
			throw unchecked(e, "Failed to stream to: " + path);
		}
		// @formatter:on
	}

	protected OutputStream newOutputStream(Path path) {
		try {
			Files.createDirectories(path.getParent());

			// @formatter:off
			return Files.newOutputStream(path, 
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE);
			// @formatter:on

		} catch (IOException e) {
			throw unchecked(e, "Failed to create target for streaming: " + path);
		}
	}

	protected Path resolveSourcePath(String accessId, FileSystemSource source, boolean forReading) {
		Path path = fsPathResolver.resolveSourcePathForDomain(accessId, source);

		if (forReading && !Files.exists(path))
			throw new NotFoundException("File does not exist: " + path);

		return path;
	}

	protected String resolveUploadPath(Resource resource) {
		nonNull(resource, "resource");

		String fileName = resource.getGlobalId() == null ? UUID.randomUUID().toString() : resource.getGlobalId();

		return prependTimeStampedPath(fileName);

	}

	private String prependTimeStampedPath(String fileName) {
		if (timestampPathFormat == null)
			return fileName;

		Date now = new Date();

		String timeStampPart = DateTools.encode(now, timestampPathFormat);

		return timeStampPart.concat("/").concat(fileName);
	}

	protected FileSystemSource createSource(Resource resource) {
		String sourcePath = resourcePathSupplier.apply(resource);
		nonNull(sourcePath, "sourcePath");

		FileSystemSource source = FileSystemSource.T.create();
		source.setPath(sourcePath);

		return source;
	}

	@Override
	protected Date getLastModifiedDate(Resource resource, BinaryRetrievalRequest request) {
		FileSystemSource source = retrieveFileSystemSource(resource);
		Path path = resolveSourcePath(request.getDomainId(), source, true);
		try {
			FileTime date = Files.getLastModifiedTime(path);
			return date == null ? null : Date.from(date.toInstant());

		} catch (IOException e) {
			throw unchecked(e, "Failed to obtain the last modified time for " + path);
		}
	}

	protected static FileSystemSource retrieveFileSystemSource(Resource resource) {
		nonNull(resource, "resource");

		ResourceSource source = requireNonNull(resource.getResourceSource(), () -> "resource source is null for resource: " + resource);

		if (source instanceof FileSystemSource)
			return (FileSystemSource) source;

		// Enables this processor to stream the deprecated StaticSource as well:
		if (source instanceof StaticSource) {
			log.trace(() -> "Deprecated " + StaticSource.T.getTypeSignature() + " found. " + "Please consider converting it to "
					+ FileSystemSource.T.getTypeSignature());

			FileSystemSource fileSystemSource = FileSystemSource.T.create();
			fileSystemSource.setPath(((StaticSource) source).getResolverURI());
			return fileSystemSource;
		}

		throw new IllegalStateException(FileSystemBinaryProcessor.class.getName() + " instances cannot handle " + source);

	}

}
