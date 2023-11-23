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
package com.braintribe.integration.etcd;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.DestructionAware;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.ArrayTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.Arguments;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.GetOption.SortOrder;
import io.etcd.jetcd.options.GetOption.SortTarget;
import io.etcd.jetcd.options.PutOption;

/**
 * Convenience tool that provides methods to interact with etcd via the coreos jetcd API. <br>
 * <br>
 * Users of this tool have to instantiate it, using a list of URLs where the etcd service can be reached. This tool is
 * thread-safe, but the underlying jetcd API might not be (tests did not show any problems, though).
 * 
 * @author Roman Kurmanowytsch
 *
 */
public class EtcdProcessing implements DestructionAware {

	private static final Logger logger = Logger.getLogger(EtcdProcessing.class);

	protected Client client;
	protected KV kvClient;

	private Supplier<Client> clientSupplier;

	@Override
	public void preDestroy() {
		IOTools.closeCloseable(kvClient, logger);
		IOTools.closeCloseable(client, logger);
		kvClient = null;
		client = null;
	}

	public EtcdProcessing(Supplier<Client> clientSupplier) {
		Arguments.notNullWithName("clientSupplier must not be null", clientSupplier);
		this.clientSupplier = clientSupplier;
	}

	/**
	 * Connects to the server using the endpoint URLs provided in the constructor. It is not obligatory to call this
	 * method as every method will automatically connect if it hasn't yet done so.
	 */
	public void connect() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					client = clientSupplier.get();
					kvClient = client.getKVClient();
				}
			}
		}
	}

	/**
	 * Puts the specified key/value pair into the etcd storage. If the ttl is greater than 0, it will also be applied.
	 * 
	 * @param key
	 *            The key of the key/value pair.
	 * @param value
	 *            The value of the key/value pair.
	 * @param ttl
	 *            The time-to-live of the new entry (in seconds).
	 * @return The {@link PutResponse} object returned by the jetcd API.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public PutResponse put(String key, String value, int ttl) throws Exception {
		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);

		return put(bsKey, bsValue, ttl);
	}

	/**
	 * Puts the specified key/value pair into the etcd storage. Note that the value in this case is a byte[], which is
	 * useful for storing binary data in etcd. If the ttl is greater than 0, it will also be applied.
	 * 
	 * @param key
	 *            The key of the key/value pair.
	 * @param value
	 *            The value of the key/value pair.
	 * @param ttl
	 *            The time-to-live of the new entry (in seconds).
	 * @return The {@link PutResponse} object returned by the jetcd API.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public PutResponse put(String key, byte[] value, int ttl) throws Exception {
		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value);

		return put(bsKey, bsValue, ttl);
	}

	/**
	 * Puts the specified key/value pair into the etcd storage. Note that the key and value in this case is of type
	 * {@link ByteSequence}. It is probably easier to use either {@link #put(String, String, int)} or
	 * {@link #put(String, byte[], int)} instead. If the ttl is greater than 0, it will also be applied.
	 * 
	 * @param bsKey
	 *            The key of the key/value pair.
	 * @param bsValue
	 *            The value of the key/value pair.
	 * @param ttl
	 *            The time-to-live of the new entry (in seconds).
	 * @return The {@link PutResponse} object returned by the jetcd API.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public PutResponse put(ByteSequence bsKey, ByteSequence bsValue, int ttl) throws Exception {
		connect();

		PutResponse putResponse = null;

		// put the key-value
		if (ttl > 0) {
			LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
			long leaseId = leaseGrantResponse.getID();
			PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
			putResponse = kvClient.put(bsKey, bsValue, putOption).get();
		} else {
			putResponse = kvClient.put(bsKey, bsValue).get();
		}

		return putResponse;
	}

	/**
	 * Returns the highest key of all that start with the specified prefix. This is done by doing a ranged search,
	 * sorting by keys descending and limiting the result to 1.
	 * 
	 * @param prefix
	 *            The key prefix.
	 * @return The actual key or null, if no such key could be found.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public String getHighestKey(String prefix) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(prefix, StandardCharsets.UTF_8);
		ByteSequence rangeEnd = getRangeEnd(prefix);

		GetOption getOption = GetOption.newBuilder().withRange(rangeEnd).withSortField(SortTarget.KEY).withSortOrder(SortOrder.DESCEND).withLimit(1)
				.withKeysOnly(true).build();

		kvClient.get(bsKey).get();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, getOption);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		List<KeyValue> kvs = response.getKvs();
		if (kvs == null || kvs.isEmpty()) {
			return null;
		}
		return kvs.get(0).getKey().toString(StandardCharsets.UTF_8);
	}

	/**
	 * Retrieves the KV response for a specific key. Instead of returning just the actual value, this method returns the
	 * {@link GetResponse} object so that the caller has access to the full information (e.g., the header with the
	 * modification counter). <br>
	 * <br>
	 * To get the actual value from this response, the methods {@link #getResponseValue(GetResponse)},
	 * {@link #getResponseValues(GetResponse)}, and {@link #getResponseValues(GetResponse, long)} can be used.
	 * 
	 * @param key
	 *            The key that should be used to retrieve the result.
	 * @return The {@link GetResponse} object provided by the jetcd API.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public GetResponse get(String key) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);

		kvClient.get(bsKey).get();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		return response;

	}

	/**
	 * Returns a map of all key/value pairs that are identified by the provided list of keys. Note that this method uses
	 * multi-threading to speed up the loading of the keys.
	 * 
	 * @param keys
	 *            The list of keys that should be loaded from etcd.
	 * @return The map of key/value pairs. The map might be smaller than the original list of keys as null values will
	 *         not be included.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public Map<String, String> getAllEntries(List<String> keys) throws Exception {
		connect();

		Map<String, String> result = new ConcurrentHashMap<>();
		keys.stream().parallel().forEach(key -> {
			try {
				GetResponse getResponse = get(key);
				String value = getResponseValue(getResponse);
				if (value != null) {
					result.put(key, value);
				}
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while trying to retrieve value for key " + key);
			}
		});
		return result;
	}

	/**
	 * Returns all key/value pairs that share the same prefix in the key. The method
	 * {@link #getResponseValues(GetResponse)} can be used to retrieve the actual values. <br>
	 * <br>
	 * Please use this method with care as the result might become too big to fit into a single response from the
	 * server. It is probably better to use {@link #getAllKeysWithPrefix(String)} instead to limit the response to the
	 * keys only.
	 * 
	 * @param prefix
	 *            The key prefix.
	 * @return A {@link GetResponse} object that contains all the results.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 * @deprecated Use {@link #getAllKeysWithPrefix(String)} and {@link #getAllEntries(List)} instead.
	 */
	@Deprecated
	public GetResponse getAllWithPrefix(String prefix) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(prefix, StandardCharsets.UTF_8);
		ByteSequence rangeEnd = getRangeEnd(prefix);

		GetOption getOption = GetOption.newBuilder().withRange(rangeEnd).withSortField(SortTarget.KEY).withSortOrder(SortOrder.ASCEND).build();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, getOption);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		return response;

	}

	/**
	 * Returns all keys from etcd that start with the provided prefix. Note that due to a limitation of jetcd, it is not
	 * possible to page results. Hence, it might cause an exception when the resulting set of keys is larger than the
	 * allowed maximum of the underlying gRPC protocol.
	 * 
	 * @param prefix
	 *            The prefix that all returned keys should start with.
	 * @return The list of keys, sorted by the creation timestamp (ascending).
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public List<String> getAllKeysWithPrefix(String prefix) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(prefix, StandardCharsets.UTF_8);
		ByteSequence rangeEnd = getRangeEnd(prefix);

		GetOption getOption = GetOption.newBuilder().withRange(rangeEnd).withKeysOnly(true).withSortField(SortTarget.CREATE)
				.withSortOrder(SortOrder.ASCEND).build();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, getOption);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		List<String> result = new ArrayList<>();

		List<KeyValue> kvs = response.getKvs();
		if (!kvs.isEmpty()) {
			for (KeyValue kv : kvs) {
				result.add(kv.getKey().toString(StandardCharsets.UTF_8));
			}
		}

		return result;

	}

	/**
	 * Returns all keys from etcd that start with the provided prefix, starting with the provided startWithId. Note that
	 * due to a limitation of jetcd, it is not possible to page results. Hence, it might cause an exception when the
	 * resulting set of keys is larger than the allowed maximum of the underlying gRPC protocol.
	 * 
	 * @param prefix
	 *            The prefix that all returned keys should start with.
	 * @param startWithId
	 *            The starting key so that all keys above this one can be returned.
	 * @return The list of keys, sorted by the creation timestamp (ascending).
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public List<String> getAllKeysWithPrefix(String prefix, String startWithId) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(startWithId, StandardCharsets.UTF_8);
		ByteSequence rangeEnd = getRangeEnd(prefix);

		GetOption getOption = GetOption.newBuilder().withRange(rangeEnd).withKeysOnly(true).withSortField(SortTarget.CREATE)
				.withSortOrder(SortOrder.ASCEND).build();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, getOption);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		List<String> result = new ArrayList<>();

		List<KeyValue> kvs = response.getKvs();
		if (!kvs.isEmpty()) {
			for (KeyValue kv : kvs) {
				result.add(kv.getKey().toString(StandardCharsets.UTF_8));
			}
		}

		return result;

	}

	/**
	 * Returns all key/value pairs that share the same prefix in the key, starting from a specific key on. The method
	 * {@link #getResponseValues(GetResponse)} can be used to retrieve the actual values. <br>
	 * <br>
	 * Please use this method with care as the result might become too big to fit into a single response from the
	 * server. It is probably better to use {@link #getAllKeysWithPrefix(String,String)} instead to limit the response
	 * to the keys only.
	 * 
	 * @param prefix
	 *            The key prefix.
	 * @param startWithId
	 *            The starting key.
	 * @return A {@link GetResponse} object that contains all the results.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 * @deprecated Use {@link #getAllKeysWithPrefix(String,String)} and {@link #getAllEntries(List)} instead.
	 */
	@Deprecated
	public GetResponse getAllWithPrefix(String prefix, String startWithId) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(startWithId, StandardCharsets.UTF_8);
		ByteSequence rangeEnd = getRangeEnd(prefix);

		GetOption getOption = GetOption.newBuilder().withRange(rangeEnd).withSortField(SortTarget.CREATE).withSortOrder(SortOrder.ASCEND).build();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, getOption);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		return response;

	}

	/**
	 * Returns (if available) the String value of the first KV entry in the provided response.
	 * 
	 * @param response
	 *            The {@link GetResponse} object that contains the key/value pair.
	 * @return The value (as a String) of the first key/value pair of the response. If no key/value pair is available,
	 *         null will returned.
	 */
	public static String getResponseValue(GetResponse response) {

		if (response.getKvs().isEmpty()) {
			// key does not exist
			return null;
		}
		String result = response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
		return result;

	}

	/**
	 * Returns (if available) the value of the first KV entry of the response object as a byte array.
	 * 
	 * @param response
	 *            The {@link GetResponse} object that contains the key/value pair.
	 * @return The value (as a byte array) of the first key/value pair of the response. If no key/value pair is
	 *         available, null will returned.
	 */
	public static byte[] getBinaryResponseValue(GetResponse response) {

		if (response.getKvs().isEmpty()) {
			// key does not exist
			return null;
		}
		byte[] result = response.getKvs().get(0).getValue().getBytes();
		return result;

	}

	/**
	 * Returns all values contained in the {@link GetResponse} object as a List of Strings.
	 * 
	 * @param response
	 *            The response that contains the key/value pairs.
	 * @return The values as a list of Strings, or null if no values are present.
	 */
	public static List<String> getResponseValues(GetResponse response) {

		if (response.getKvs().isEmpty()) {
			// key does not exist
			return null;
		}
		List<String> resultList = new ArrayList<>();
		for (KeyValue kv : response.getKvs()) {
			String result = kv.getValue().toString(StandardCharsets.UTF_8);
			resultList.add(result);
		}

		return resultList;

	}

	/**
	 * Returns all values contained in the {@link GetResponse} object as a List of Strings. All entries where the
	 * creation revision of the entry is lower than <code>startWithCreateRevision</code> will be omitted from the
	 * result.
	 * 
	 * @param response
	 *            The response that contains the key/value pairs.
	 * @return The values as a list of Strings, or null if no values are present.
	 */
	public static List<String> getResponseValues(GetResponse response, long startWithCreateRevision) {

		if (response.getKvs().isEmpty()) {
			// key does not exist
			return null;
		}
		List<String> resultList = new ArrayList<>();

		for (KeyValue kv : response.getKvs()) {
			if (kv.getCreateRevision() >= startWithCreateRevision) {
				String result = kv.getValue().toString(StandardCharsets.UTF_8);
				resultList.add(result);
			}
		}

		return resultList;

	}

	/**
	 * Returns the current modification revision of a specific key.
	 * 
	 * @param key
	 *            The key that should be retrieved from etcd.
	 * @return The modification revision of the specified key.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public long getModificationCount(String key) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		GetOption getOption = GetOption.newBuilder().withRange(getRangeEnd(key)).build();

		GetResponse getResponse = kvClient.get(bsKey, getOption).get();
		List<KeyValue> kvs = getResponse.getKvs();
		if (kvs != null && !kvs.isEmpty()) {
			return kvs.get(0).getModRevision();
		}

		return -1;
	}

	/**
	 * Creates a {@link ByteSequence} object that contains the provided key with the last character increased by 1. This
	 * can be used to get range when given a prefix.
	 * 
	 * @param key
	 *            The key that acts as a lower end of a range.
	 * @return The upper bound of the range.
	 */
	public static ByteSequence getRangeEnd(String key) {
		int max = key.length() - 1;
		if (max < 0) {
			return ByteSequence.from(new byte[] { 1 });
		}
		String excludeLast = key.substring(0, max);
		String rangeEndString = excludeLast + new String(new char[] { (char) (key.charAt(max) + 1) });
		ByteSequence endKey = ByteSequence.from(rangeEndString, StandardCharsets.UTF_8);
		return endKey;
	}

	/**
	 * Deletes the entry identified by <code>delKey</code> iff it is able to insert the
	 * <code>lockKey</code>/<code>workerId</code> pair (the insert fails when the key is already in use).
	 * 
	 * @param lockKey
	 *            The key that should be used for the lock.
	 * @param delKey
	 *            The key that should be deleted.
	 * @param workerId
	 *            The value that should be used for the lock.
	 * @param ttl
	 *            The ttl of the lock entry (in seconds).
	 * @return True, if the method was able to uniquely set the <code>lockKey</code>/<code>workerId</code> pair and
	 *         delete the <code>delKey</code>.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public boolean atomicLockAndDelete(String lockKey, String delKey, String workerId, int ttl) throws Exception {
		connect();

		ByteSequence bsLockKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
		ByteSequence bsDelKey = ByteSequence.from(delKey, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(workerId, StandardCharsets.UTF_8);

		LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
		long leaseId = leaseGrantResponse.getID();
		PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();

		GetOption getOption = GetOption.newBuilder().withSortField(SortTarget.MOD).withSortOrder(SortOrder.ASCEND).build();

		DeleteOption delOption = DeleteOption.newBuilder().build();

		Cmp condition = new Cmp(bsLockKey, Cmp.Op.EQUAL, CmpTarget.version(0));
		Op update = Op.put(bsLockKey, bsValue, putOption);
		Op get = Op.get(bsLockKey, getOption);
		Op del = Op.delete(bsDelKey, delOption);

		TxnResponse txnResponse = client.getKVClient().txn().If(condition).Then(update).Then(get).Then(del).commit().get();

		List<GetResponse> responses = txnResponse.getGetResponses();
		if (responses == null || responses.isEmpty()) {
			return false;
		}
		List<KeyValue> kvs = responses.get(0).getKvs();
		ByteSequence newValue = kvs.get(0).getValue();
		String newValueString = newValue.toString(StandardCharsets.UTF_8);

		return workerId.equals(newValueString);
	}

	/**
	 * Tries to set the <code>lockKey</code>/<code>value</code> pair if the key is not yet used.
	 * 
	 * @param lockKey
	 *            The key of the new entry.
	 * @param value
	 *            The value that should be set.
	 * @param ttl
	 *            The time-to-live of the new entry in seconds.
	 * @return True, if the put was successful, false otherwise.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public boolean atomicPutIfNonExistent(String lockKey, String value, int ttl) throws Exception {
		connect();

		logger.trace(() -> "Going to attempt to set "+value+" for key "+lockKey+" with TTL "+ttl+" seconds.");
		
		ByteSequence bsKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);

		LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
		long leaseId = leaseGrantResponse.getID();
		PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();

		GetOption getOption = GetOption.newBuilder().withSortField(SortTarget.MOD).withSortOrder(SortOrder.ASCEND).build();

		Cmp condition = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.version(0));
		Op update = Op.put(bsKey, bsValue, putOption);
		Op get = Op.get(bsKey, getOption);

		TxnResponse txnResponse = client.getKVClient().txn().If(condition).Then(update).Then(get).commit().get();

		List<GetResponse> responses = txnResponse.getGetResponses();
		if (responses == null || responses.isEmpty()) {
			String currentValue = getValue(lockKey, bsKey, getOption);
			logger.trace(() -> "Received no response at all while trying to set value "+value+" for key "+lockKey+" with TTL "+ttl+" seconds. Current value: "+currentValue);
			return false;
		}
		List<KeyValue> kvs = responses.get(0).getKvs();
		ByteSequence newValue = kvs.get(0).getValue();
		String newValueString = newValue.toString(StandardCharsets.UTF_8);

		boolean updatedSucceeded = value.equals(newValueString);

		if (updatedSucceeded) {
			
			//It has been seen during testing that this transaction is not as safe as it should be
			//Waiting a moment and then checking again if the value is still there
			Thread.sleep(10L);
			
			String checkValue = getValue(lockKey, bsKey, getOption);
			if (checkValue == null || !checkValue.equals(newValueString)) {
				logger.trace(() -> "Check value is different: key: "+lockKey+", should: "+newValueString+", actual: "+checkValue);
				return false;
			}
		}
		
		
		if (updatedSucceeded) {
			logger.trace(() -> "Successfully set value "+value+" for key "+lockKey+" with a TTL of "+ttl+" seconds");
		} else {
			logger.trace(() -> "Setting value "+value+" for key "+lockKey+" did not succeed as there is already value "+newValueString+" in place.");
		}
		
		return updatedSucceeded;
	}
	
	private String getValue(String key, ByteSequence bsKey, GetOption getOption) throws InterruptedException, ExecutionException {
		GetResponse checkResponse = kvClient.get(bsKey, getOption).get();
		if (checkResponse != null) {
			List<KeyValue> checkKvs = checkResponse.getKvs();
			if (checkKvs != null && !checkKvs.isEmpty()) {
				ByteSequence checkValueByteSequence = checkKvs.get(0).getValue();
				String value = checkValueByteSequence.toString(StandardCharsets.UTF_8);
				return value;
			} else {
				logger.trace(() -> "No value at all received for key "+key);
			}
		} else {
			logger.trace(() -> "No response received for key "+key);
		}
		return null;
	}

	/**
	 * Sets the <code>lockKey</code>/<code>value</code> pair if the existing value of this key is the same value.
	 * 
	 * @param lockKey
	 *            The key of the entry that should be updated.
	 * @param value
	 *            The value that should be set.
	 * @param ttl
	 *            The time-to-live of the new entry in seconds.
	 * @return True, if the update was successful, false otherwise.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public boolean atomicPutIfSameValue(String lockKey, String value, int ttl) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);

		LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
		long leaseId = leaseGrantResponse.getID();
		PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();

		GetOption getOption = GetOption.newBuilder().withSortField(SortTarget.MOD).withSortOrder(SortOrder.ASCEND).build();

		Cmp condition = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsValue));
		Op update = Op.put(bsKey, bsValue, putOption);
		Op get = Op.get(bsKey, getOption);

		TxnResponse txnResponse = client.getKVClient().txn().If(condition).Then(update).Then(get).commit().get();

		List<GetResponse> responses = txnResponse.getGetResponses();
		if (responses == null || responses.isEmpty()) {
			return false;
		}
		List<KeyValue> kvs = responses.get(0).getKvs();
		ByteSequence newValue = kvs.get(0).getValue();
		String newValueString = newValue.toString(StandardCharsets.UTF_8);

		return value.equals(newValueString);
	}

	/**
	 * Deletes the key <code>lockKey</code> iff the existing value matches <code>value</code>.
	 * 
	 * @param lockKey
	 *            The key that should be deleted.
	 * @param value
	 *            The match value that has to be present for the provided key.
	 * @return True, if the delete was successful, false otherwise.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public boolean deleteIfMatches(String lockKey, String value) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);

		DeleteOption delOption = DeleteOption.newBuilder().build();

		Cmp condition = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsValue));
		Op delete = Op.delete(bsKey, delOption);

		TxnResponse txnResponse = client.getKVClient().txn().If(condition).Then(delete).commit().get();

		List<DeleteResponse> responses = txnResponse.getDeleteResponses();
		if (responses == null || responses.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Deletes the entry with the specified key.
	 * 
	 * @param key
	 *            The key that should be deleted.
	 * @throws Exception
	 *             Thrown by the jetcd API in case of an error.
	 */
	public void delete(String key) throws Exception {
		connect();

		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);

		kvClient.delete(bsKey).get();
	}

	/**
	 * Returns the {@link Client} client object created by this tool.
	 * 
	 * @return The client object.
	 */
	public Client getClient() {
		connect();
		return client;
	}

	/**
	 * Returns the {@link KV} client object created by this tool.
	 * 
	 * @return The KV object.
	 */
	public KV getKvClient() {
		connect();
		return kvClient;
	}

	/**
	 * Returns the content of the binary data stored with the provided key prefix. If there are multiple chunks with the
	 * same prefix, they will be appended into a single byte array.
	 * 
	 * @param prefix
	 *            The key (or key prefix) of the content.
	 * @return The content of the entries specified by the key.
	 */
	public byte[] getChunkedBytes(String prefix) {
		byte[] responseValue = null;
		try {
			List<String> keys = getAllKeysWithPrefix(prefix);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (String k : keys) {
				GetResponse chunkResponse = get(k);
				byte[] chunkBytes = EtcdProcessing.getBinaryResponseValue(chunkResponse);
				baos.write(chunkBytes);
			}
			baos.close();
			responseValue = baos.toByteArray();
		} catch (Exception e1) {
			throw Exceptions.unchecked(e1, "Could not retrieve content for id " + prefix);
		}

		return responseValue;
	}

	/**
	 * Puts the provided content (as a byte array) into etcd, using the <code>keyProvider</code> to issue the key for
	 * each chunk. The keyProvider should return keys that share the same prefix and should (recommended, but not
	 * entirely necessary) also allow for sorting the content according to the keys.
	 * 
	 * @param keyProvider
	 *            The function that provides for a chunkId the resulting etcd key.
	 * @param sourceBytes
	 *            The content that should be stored.
	 * @param chunkSize
	 *            The maximum size of a chunk in a single etcd entry.
	 * @param ttlInSeconds
	 *            The time-to-live of the created entries (in seconds).
	 */
	public void putChunkedBytes(Function<String, String> keyProvider, byte[] sourceBytes, int chunkSize, int ttlInSeconds) {
		byte[][] chunks = ArrayTools.splitArray(sourceBytes, chunkSize);

		for (int i = 0; i < chunks.length; ++i) {
			try {
				String chunkId = StringTools.extendStringInFront("" + i, '0', 8);
				String chunkKey = keyProvider.apply(chunkId);
				put(chunkKey, chunks[i], ttlInSeconds);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not store content");
			}
		}
	}

}
