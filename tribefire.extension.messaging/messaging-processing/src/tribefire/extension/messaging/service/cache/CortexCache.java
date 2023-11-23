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
package tribefire.extension.messaging.service.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

import tribefire.extension.messaging.connector.api.ConsumerMessagingConnector;
import tribefire.extension.messaging.connector.api.ProducerMessagingConnector;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;

public class CortexCache<K, V> implements LifecycleAware {

	private Cache<K, V> cache;
	private Function<V, K> keyExtractionFunction;

	private Duration expirationDuration;
	private boolean expireAfterWrite;
	private RemovalListener<K, V> removalListener;

	@Override
	public void postConstruct() {
		expireAfterWrite = false;
	}

	@Override
	public void preDestroy() {
		if (cache != null) {
			invalidateCache();
		}
	}

	public void initialize() {
		//@formatter:off
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler());
        //@formatter:on

		if (expireAfterWrite) {
			builder.expireAfterWrite(expirationDuration);
		} else {
			builder.expireAfterAccess(expirationDuration);
		}
		if (removalListener != null) {
			builder.removalListener(removalListener);
		}

		this.cache = builder.build();
	}

	public V get(K key) {
		return this.cache.getIfPresent(key);
	}

	public void put(V value) {
		//@formatter:off
        Function<V, K> keyFunc = Optional.ofNullable(keyExtractionFunction)
                .orElseThrow(()-> new UnsatisfiedMaybeTunneling(Reasons.build(ArgumentNotSatisfied.T)
                                                                    .text("Key extraction function was not supplied to the CortexCache, so putting value without a key is not possible!")
                                                                    .toMaybe()));
        //@formatter:on
		this.cache.put(keyFunc.apply(value), value);
	}

	public void put(K key, V value) {
		this.cache.put(key, value);
	}

	public Collection<V> getAll() {
		return this.cache.asMap().values();
	}

	public <C extends Collection<V>> C getAll(Function<Collection<V>, C> conversionFunction) {
		return conversionFunction.apply(getAll());
	}

	public void invalidateCache() {
		if (this.cache.estimatedSize() > 0) {
			this.cache.asMap().values().forEach(c -> {
				if (c instanceof ProducerMessagingConnector connector) {
					connector.destroy();
				} else if (c instanceof ConsumerMessagingConnector connector) {
					connector.finalizeConsume();
				}
			});
		}
		this.cache.invalidateAll();
		this.cache.cleanUp();
	}

	@Configurable
	@Required
	public void setKeyExtractionFunction(Function<V, K> keyExtractionFunction) {
		this.keyExtractionFunction = keyExtractionFunction;
	}

	@Configurable
	public void setExpirationDuration(Duration expirationDuration) {
		this.expirationDuration = expirationDuration;
	}

	@Configurable
	public void setExpireAfterWrite(boolean expireAfterWrite) {
		this.expireAfterWrite = expireAfterWrite;
	}

	@Configurable
	public void setRemovalListener(RemovalListener<K, V> removalListener) {
		this.removalListener = removalListener;
	}

}
