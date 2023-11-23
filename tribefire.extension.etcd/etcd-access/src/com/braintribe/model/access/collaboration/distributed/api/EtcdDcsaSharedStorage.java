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
package com.braintribe.model.access.collaboration.distributed.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.integration.etcd.EtcdConnection;
import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.etcd.resource.EtcdSource;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.etcd.EtcdLockManager;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;

import io.etcd.jetcd.Client;

/**
 * Implementation of the {@link DcsaSharedStorage} interface that is using etcd as a backend. The LockManager that it
 * uses can be configured. If no LockManager is set, an instance of the {@link EtcdLockManager} will be created. <br>
 * <br>
 * The Java API of etcd does not allow to use the revision number to filter for keys. Hence, this implementation had to
 * use locking to create unique keys in a sequential order. The keys are (apart from the configurable project name and a
 * static String) a 16-digit number, starting at 0000000000000001. Every new entry will increase the largest key by 1.
 * 
 * @author Roman Kurmanowytsch
 */
public class EtcdDcsaSharedStorage implements DcsaSharedStorage, DestructionAware {

	protected String project = "";

	protected HasStringCodec marshaller;

	protected EtcdProcessing processing;

	protected LockManager lockManager;

	protected Supplier<Client> clientSupplier;

	protected int ttlInSeconds = -1;

	protected int chunkSize = 1 * (int) Numbers.MEGABYTE;

	protected EtcdConnection connection;

	protected void connect() {
		if (processing == null) {
			synchronized (this) {
				if (processing == null) {
					processing = new EtcdProcessing(clientSupplier);
					processing.connect();
				}
			}
		}
	}

	protected String createKey(String accessId) {
		String randomId = RandomTools.getRandom32CharactersHexString(true);
		return project.concat("/dcsa/").concat(accessId).concat("/").concat(randomId);
	}
	protected String getKey(String accessId, String id) {
		return project.concat("/dcsa/").concat(accessId).concat("/").concat(id);
	}
	protected String getStreamingKey(String accessId, String id) {
		return project.concat("/dcsa-streaming/").concat(accessId).concat("/").concat(id);
	}

	@Override
	public Lock getLock(String accessId) {
		return getLockManager().forIdentifier(accessId).exclusive();
	}

	@Override
	public String storeOperation(String accessId, CsaOperation csaOperation) {

		connect();

		Lock lock = getLockManager().forIdentifier(accessId.concat("-write-lock")).exclusive();
		lock.lock();
		try {

			String key = getKey(accessId, "");

			String highestKey = processing.getHighestKey(key);
			String nextKeyValueString = getNextId(highestKey);

			String newKey = getKey(accessId, nextKeyValueString);
			String encoded = marshaller.getStringCodec().encode(csaOperation);

			if (csaOperation instanceof CsaResourceBasedOperation) {
				csaOperation = storeResource((CsaResourceBasedOperation) csaOperation, accessId, nextKeyValueString);
			}

			try {
				processing.put(newKey, encoded, ttlInSeconds);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not store csaOperation");
			}

			return nextKeyValueString;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not store " + csaOperation);
		} finally {
			lock.unlock();
		}

	}

	private CsaResourceBasedOperation storeResource(CsaResourceBasedOperation csaOperation, String accessId, String entryKey) {

		Resource resource = csaOperation.getPayload();
		if (resource == null) {
			return csaOperation;
		}
		byte[] resourceContent = null;
		try (InputStream inputStream = resource.openStream()) {
			resourceContent = IOTools.slurpBytes(inputStream);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not load resource of " + csaOperation);
		}

		final String keyPrefix = getStreamingKey(accessId, entryKey);
		processing.putChunkedBytes(chunkId -> {
			return keyPrefix.concat("/").concat(chunkId);
		}, resourceContent, chunkSize, ttlInSeconds);

		CsaResourceBasedOperation result = GmReflectionTools.makeShallowCopy(csaOperation);
		Resource clonedResource = GmReflectionTools.makeShallowCopy(resource);
		EtcdSource source = EtcdSource.T.create();
		source.setId(keyPrefix);
		clonedResource.setResourceSource(source);

		result.setPayload(clonedResource);

		return result;
	}

	protected String getNextId(String numericalKey) {
		Long highestKeyValue = 0L;
		if (!StringTools.isBlank(numericalKey)) {
			int index = numericalKey.lastIndexOf('/');
			if (index != -1) {
				highestKeyValue = Long.parseLong(numericalKey.substring(index + 1));
			} else {
				highestKeyValue = Long.parseLong(numericalKey);
			}
		}
		long nextKeyValue = highestKeyValue + 1;
		String nextKeyValueString = StringTools.extendStringInFront("" + nextKeyValue, '0', 16);
		return nextKeyValueString;
	}

	@Override
	public DcsaIterable readOperations(String accessId, String lastReadMarker) {

		connect();

		String startWithKey = null;
		if (!StringTools.isBlank(lastReadMarker)) {
			startWithKey = getNextId(lastReadMarker);
		}

		String key = getKey(accessId, "");

		List<String> keys = null;
		Map<String, String> map = null;
		try {
			if (startWithKey == null) {
				keys = processing.getAllKeysWithPrefix(key);
			} else {
				keys = processing.getAllKeysWithPrefix(key, getKey(accessId, startWithKey));
			}
			map = processing.getAllEntries(keys);
		} catch (Exception e) {
			throw new ModelAccessException("Error while trying to load the population with prefix " + project, e);
		}

		List<CsaOperation> result = new ArrayList<>();

		if (map != null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {

				String entryKey = entry.getKey();
				String value = entry.getValue();

				CsaOperation ge = (CsaOperation) marshaller.getStringCodec().decode(value);
				if (ge instanceof CsaResourceBasedOperation) {

					final String streamingKey = entryKey.replace("/dcsa/", "/dcsa-streaming/");

					Resource resource = Resource.createTransient(() -> {
						byte[] content = processing.getChunkedBytes(streamingKey);
						return new ByteArrayInputStream(content);
					});
					((CsaResourceBasedOperation) ge).setPayload(resource);
				}

				result.add(ge);
			}
		}

		String newLastReadMarker = null;
		if (keys != null && !keys.isEmpty()) {
			// Because of ordering of keys, the last one is the new marker
			String lastKey = keys.get(keys.size() - 1);
			int index = lastKey.lastIndexOf('/');

			newLastReadMarker = lastKey.substring(index + 1);
		}

		return new EtcdDcsaIterable(newLastReadMarker, result);

	}

	protected long getNumericalPartOfKey(String key) {
		int index = key.lastIndexOf('/');
		return Long.parseLong(key.substring(index + 1));
	}

	private static class EtcdDcsaIterable implements DcsaIterable {

		private final String lastReadMarker;
		private final List<CsaOperation> operations;

		public EtcdDcsaIterable(String lastReadMarker, List<CsaOperation> operations) {
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

	@Configurable
	@Required
	public void setProject(String project) {
		this.project = project;
	}
	@Configurable
	@Required
	public void setMarshaller(HasStringCodec marshaller) {
		this.marshaller = marshaller;
	}

	public LockManager getLockManager() {
		if (lockManager == null) {
			synchronized (this) {
				if (lockManager == null) {
					EtcdLockManager lm = new EtcdLockManager();
					lm.setClientSupplier(clientSupplier);
					lm.setIdentifierPrefix(project);
					lm.postConstruct();
					lockManager = lm;
				}
			}
		}
		return lockManager;
	}
	@Configurable
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	@Configurable
	public void setTtlInSeconds(int ttlInSeconds) {
		this.ttlInSeconds = ttlInSeconds;
	}

	@Override
	public void preDestroy() {
		if (processing != null) {
			processing.preDestroy();
		}
	}

	@Configurable
	@Required
	public void setConnection(EtcdConnection connection) {
		this.connection = connection;
	}
	@Configurable
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Configurable
	@Required
	public void setClientSupplier(Supplier<Client> clientSupplier) {
		this.clientSupplier = clientSupplier;
	}

}
