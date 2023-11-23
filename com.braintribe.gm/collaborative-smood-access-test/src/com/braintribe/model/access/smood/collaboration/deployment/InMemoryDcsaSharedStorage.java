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
package com.braintribe.model.access.smood.collaboration.deployment;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.collaboration.distributed.DistributedCollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.distributed.api.DcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 * This Storage is also testing that the {@link CsaOperation}s stored have the necessary information, like {@link Resource#getFileSize() fileSize} for
 * a GMML file resource - {@link #verifyResourceBasedOperation}.
 * 
 * @author peter.gazdik
 */
public class InMemoryDcsaSharedStorage implements DcsaSharedStorage {

	private final Map<String, Lock> accessToLock = new ConcurrentHashMap<>();
	private final Map<String, List<CsaOperation>> accessToOperations = newMap();
	private final Lock readLock;
	private final Lock writeLock;

	public static boolean TMP_ENABLE_LAZY_LOADING = true;

	public InMemoryDcsaSharedStorage() {
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	@Override
	public Lock getLock(String accessId) {
		return accessToLock.computeIfAbsent(accessId, a -> new ReentrantLock());
	}

	@Override
	public String storeOperation(String accessId, CsaOperation csaOperation) {
		writeLock.lock();

		try {
			return w_storeOperation(accessId, csaOperation);

		} finally {
			writeLock.unlock();
		}
	}

	private String w_storeOperation(String accessId, CsaOperation csaOperation) {
		List<CsaOperation> ops = accessToOperations.computeIfAbsent(accessId, k -> newList());
		String marker = "" + ops.size();
		ops.add(copyAndAdapt(csaOperation, marker));

		return marker;
	}

	private CsaOperation copyAndAdapt(CsaOperation csaOperation, String marker) {
		CsaOperation result = GmReflectionTools.makeShallowCopy(csaOperation);
		result.setId(marker);

		if (csaOperation instanceof CsaResourceBasedOperation)
			adaptResourceBasedOperation((CsaResourceBasedOperation) result);

		return result;
	}

	private void adaptResourceBasedOperation(CsaResourceBasedOperation resourceBasedOperation) {
		verifyResourceBasedOperation(resourceBasedOperation);
		resourceBasedOperation.setPayload(createLocalResource(resourceBasedOperation.getPayload()));
	}

	private void verifyResourceBasedOperation(CsaResourceBasedOperation resourceBasedOperation) {
		if (!(resourceBasedOperation instanceof CsaStoreResource)) {
			Resource payload = resourceBasedOperation.getPayload();

			assertThat(payload.getFileSize()).isNotNull();
			assertThat(payload.getMimeType()).isEqualTo(DistributedCollaborativeSmoodAccess.TEXT_PLAIN_MIME_TYPE);
		}
	}

	private Resource createLocalResource(Resource resource) {
		try {
			return Resource.createTransient(createLocalInputStreamProvider(resource));

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while localizing resource.");
		}
	}

	private InputStreamProvider createLocalInputStreamProvider(Resource resource) throws IOException {
		byte[] bytes = toByteArray(resource);
		return () -> new ByteArrayInputStream(bytes);
	}

	private byte[] toByteArray(Resource resource) throws IOException {
		try (InputStream inputStream = resource.openStream()) {
			return IOTools.slurpBytes(inputStream);
		}
	}

	@Override
	public DcsaIterable readOperations(String accessId, String lastReadMarker) {
		readLock.lock();

		try {
			return r_readOperations(accessId, lastReadMarker);

		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Map<String, Resource> readResource(String accessId, Collection<String> storedResourcesPaths) {
		readLock.lock();

		try {
			return r_readResources(accessId, storedResourcesPaths);

		} finally {
			readLock.unlock();
		}
	}

	private Map<String, Resource> r_readResources(String accessId, Collection<String> storedResourcesPaths) {
		List<CsaOperation> ops = accessToOperations.computeIfAbsent(accessId, k -> newList());

		return storedResourcesPaths.stream() //
				.collect(Collectors.toMap( //
						path -> path, path -> r_readResource(ops, path)));
	}

	private Resource r_readResource(List<CsaOperation> ops, String path) {
		requireNonNull(path, "Cannot retrieve resource for null path");

		return ops.stream() //
				.filter(CsaStoreResource.class::isInstance) //
				.map(CsaStoreResource.class::cast) //
				.filter(op -> path.equals(op.getResourceRelativePath())) //
				.map(CsaStoreResource::getPayload) //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException("No CsaStoreResource found for path : " + path));
	}

	private DcsaIterable r_readOperations(String accessId, String lastReadMarker) {
		int lastReadIndex = lastReadMarker == null ? -1 : Integer.parseInt(lastReadMarker);

		List<CsaOperation> ops = accessToOperations.computeIfAbsent(accessId, k -> newList());

		int lastPositionMarker = ops.size() - 1;

		if (lastReadIndex > lastPositionMarker)
			throw new IllegalArgumentException(
					"Invalid lastReadMarker. Value: " + lastReadMarker + ", current max possible value: " + lastPositionMarker);

		List<CsaOperation> newOperations = ops.subList(lastReadIndex + 1, ops.size());
		List<CsaOperation> resultOperations = removeResourcesFromStoreResourceOps(newOperations);

		String resultMarker = resultOperations.isEmpty() ? null : "" + lastPositionMarker;

		return new SimpleDcsaIterable(resultMarker, resultOperations);
	}

	/* Because resources are loaded lazily */
	private List<CsaOperation> removeResourcesFromStoreResourceOps(List<CsaOperation> ops) {
		return ops.stream() //
				.map(this::toLazyResourceOp) //
				.collect(Collectors.toList());
	}

	private CsaOperation toLazyResourceOp(CsaOperation original) {
		if (original instanceof CsaStoreResource)
			return removePayloadFrom((CsaStoreResource) original);
		else
			return original;
	}

	private CsaStoreResource removePayloadFrom(CsaStoreResource original) {
		CsaStoreResource result = GmReflectionTools.makeShallowCopy(original);
		if (TMP_ENABLE_LAZY_LOADING)
			result.setPayload(null);
		return result;
	}

	private static class SimpleDcsaIterable implements DcsaIterable {

		private final String lastReadMarker;
		private final List<CsaOperation> operations;

		public SimpleDcsaIterable(String lastReadMarker, List<CsaOperation> operations) {
			this.lastReadMarker = lastReadMarker;
			this.operations = operations;
		}

		@Override
		public Iterator<CsaOperation> iterator() {
			return operations.iterator();
		}

		@Override
		public String getLastReadMarker() {
			return lastReadMarker;
		}

	}

}
