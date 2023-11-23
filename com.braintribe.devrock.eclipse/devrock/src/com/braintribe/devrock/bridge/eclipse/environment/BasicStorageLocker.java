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
package com.braintribe.devrock.bridge.eclipse.environment;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static java.util.Collections.emptyMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.api.storagelocker.StorageLocker;
import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * simplest thing to be able to store some string values, but actually able to store any contents of the {@link StorageLockerPayload}
 * 
 * uses a GE to enable YAML marshalling
 * 
 * @author pit
 *
 */
public class BasicStorageLocker implements StorageLocker {

	private static final Logger log = Logger.getLogger(BasicStorageLocker.class);
	private static final String LOCKER_FILE = "locker-storage.yaml";

	private final LazyInitialized<Map<String, Object>> slotData = new LazyInitialized<>(this::initLockerStorage);
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}

	private Map<String, Object> initLockerStorage() {
		return new ConcurrentHashMap<>(readLockerStorage());
	}

	/**
	 * @return - an instantiated {@link StorageLockerPayload}, either prepped from the stored data or newly created
	 */
	private Map<String, Object> readLockerStorage() {
		File lockerFile = lockerFile();
		if (!lockerFile.exists())
			return emptyMap();

		try (InputStream in = new FileInputStream(lockerFile)) {
			StorageLockerPayload payload = (StorageLockerPayload) marshaller.unmarshall(in);
			return payload.getSlotData();

		} catch (Exception e) {
			String msg = "cannot load locker room data from [" + lockerFile.getAbsolutePath() + "]";
			log.error(msg, e);

			DevrockPluginStatus status = new DevrockPluginStatus(msg, e);
			DevrockPlugin.instance().log(status);

			return emptyMap();
		}
	}
	
	/**
	 * just overrides the settings (i.e. puts all into map)
	 * @param payload
	 */
	public void override( StorageLockerPayload payload) {
		slotData.get().putAll( payload.getSlotData());
	}

	@Override
	public <T> Optional<T> getValue(String slot) {
		return Optional.ofNullable(getRawValue(slot));
	}

	@Override
	public <T> T getValue(String slot, T value) {
		T rawValue = getRawValue(slot);
		return rawValue == null ? value : rawValue;
	}

	@SuppressWarnings("unchecked")
	private <T> T getRawValue(String slot) {
		return (T) slotData.get().get(slot);
	}

	@Override
	public <T> void setValue(String slot, T value) {
		if (value == null) {
			slotData.get().remove(slot);
		}
		else {
			slotData.get().put(slot, value);
		}
	}

	/**
	 * saves the storage locker's content to current workspace's storage, called at {@DevrockPlugin}'s stop
	 */
	public void save() {
		File lockerFile = lockerFile();

		Map<String, Object> rawData = slotData.get();

		// if nothing's to store, delete the last file if present
		if (isEmpty(rawData)) {
			if (lockerFile.exists()) {
				lockerFile.delete();
			}
			return;
		}

		try (OutputStream out = new FileOutputStream(lockerFile)) {
			marshaller.marshall(out, asStorageLockerPayload(rawData));

		} catch (Exception e) {
			String msg = "cannot write storage locker data to [" + lockerFile.getAbsolutePath() + "]";
			log.error(msg, e);

			DevrockPluginStatus status = new DevrockPluginStatus(msg, e);
			DevrockPlugin.instance().log(status);
		}
	}

	private StorageLockerPayload asStorageLockerPayload(Map<String, Object> rawData) {
		StorageLockerPayload payload = StorageLockerPayload.T.create();
		payload.getSlotData().putAll(rawData);
		return payload;
	}

	private File lockerFile() {
		File workspaceStorageLocation = DevrockPlugin.envBridge().workspaceSpecificStorageLocation();
		return new File(workspaceStorageLocation, LOCKER_FILE);
	}

	@Override
	public StorageLockerPayload content() {		
		return asStorageLockerPayload(slotData.get());
	}
	
	
}
