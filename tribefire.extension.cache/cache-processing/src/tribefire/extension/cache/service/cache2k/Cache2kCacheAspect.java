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
package tribefire.extension.cache.service.cache2k;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.core.InternalCache;
import org.cache2k.core.InternalCacheInfo;
import org.cache2k.event.CacheEntryCreatedListener;
import org.cache2k.event.CacheEntryExpiredListener;
import org.cache2k.event.CacheEntryRemovedListener;
import org.cache2k.event.CacheEntryUpdatedListener;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.utils.logging.LogLevels;

import tribefire.extension.cache.model.deployment.service.cache2k.Cache2kCacheAspectConfiguration;
import tribefire.extension.cache.model.deployment.service.cache2k.EntryLogging;
import tribefire.extension.cache.model.deployment.service.cache2k.Expiration;
import tribefire.extension.cache.model.deployment.service.cache2k.Mode;
import tribefire.extension.cache.model.deployment.service.cache2k.RefreshAheadConfiguration;
import tribefire.extension.cache.model.deployment.service.cache2k.SimpleConstantExpiration;
import tribefire.extension.cache.model.status.cache2k.Cache2kCacheAspectStatus;
import tribefire.extension.cache.service.CacheAspectInterface;
import tribefire.extension.cache.service.CacheValueHolder;
import tribefire.extension.cache.service.RefreshAheadCacheValueHolder;

public class Cache2kCacheAspect implements CacheAspectInterface<Cache2kCacheAspectStatus> {

	private final static Logger logger = Logger.getLogger(Cache2kCacheAspect.class);

	// -----------------
	// Configuration
	// -----------------

	private Supplier<String> cacheNameSupplier;
	private Cache2kCacheAspectConfiguration configuration;

	// -----------------
	// Local
	// -----------------

	private Cache<String, CacheValueHolder> cache;
	private ForkJoinPool refreshAheadPool;
	private int maxRefreshAheadPoolTerminationTime = 30;
	private long refreshAheadTime;

	// -----------------------------------------------------------------------
	// LIFECYCLE AWARE
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		RefreshAheadConfiguration refreshAheadConfiguration = configuration.getRefreshAheadConfiguration();
		if (refreshAheadConfiguration != null) {
			refreshAheadPool = new ForkJoinPool(refreshAheadConfiguration.getPoolSize());
			refreshAheadTime = refreshAheadConfiguration.getRefreshAheadTime().toDuration().toMillis();
		}

		String name = cacheNameSupplier.get();
		boolean highPerformanceMode = configuration.getHighPerformanceMode();
		boolean permitNullValues = configuration.getPermitNullValues();
		long maxCacheEntries = configuration.getMaxCacheEntries();
		boolean enableStatistics = configuration.getEnableStatistics();

		//@formatter:off
		Cache2kBuilder<String, CacheValueHolder> builder = Cache2kBuilder.of(String.class, CacheValueHolder.class)
			.name(name)
			.boostConcurrency(highPerformanceMode)
			.permitNullValues(permitNullValues)
			.entryCapacity(maxCacheEntries)
			.disableStatistics(!enableStatistics);
		//@formatter:on

		Expiration expiration = configuration.getExpiration();
		if (expiration instanceof SimpleConstantExpiration) {
			SimpleConstantExpiration simpleConstantExpiration = (SimpleConstantExpiration) expiration;
			TimeSpan timeSpan = simpleConstantExpiration.getExpiration();
			if (timeSpan == null) {
				builder.eternal(true);
			} else {
				long expireInMs = timeSpan.toDuration().toMillis();

				builder.expireAfterWrite(expireInMs, TimeUnit.MILLISECONDS);
			}
		} else {
			throw new IllegalArgumentException("Expiration: '" + expiration + "' not supported");
		}

		EntryLogging createEntryLogging = configuration.getCreateEntryLogging();
		if (createEntryLogging != null) {
			boolean async = createEntryLogging.getAsync();
			com.braintribe.logging.Logger.LogLevel logLevel = LogLevels.convert(createEntryLogging.getLogLevel());
			CacheEntryCreatedListener<String, CacheValueHolder> listener = new CacheEntryCreatedListener<String, CacheValueHolder>() {

				@Override
				public void onEntryCreated(Cache<String, CacheValueHolder> cache, CacheEntry<String, CacheValueHolder> entry) {
					logger.log(logLevel, () -> "[CREATED] " + cacheNameSupplier.get() + " key: '" + entry.getKey() + "' value: '"
							+ entry.getValue().getResult() + "'");
				}
			};
			if (async) {
				builder.addAsyncListener(listener);
			} else {
				builder.addListener(listener);
			}
		}

		LogLevel expireEntryLogging = configuration.getExpireEntryLogging();
		if (expireEntryLogging != null) {
			com.braintribe.logging.Logger.LogLevel logLevel = LogLevels.convert(expireEntryLogging);

			builder.addAsyncListener(new CacheEntryExpiredListener<String, CacheValueHolder>() {

				@Override
				public void onEntryExpired(Cache<String, CacheValueHolder> cache, CacheEntry<String, CacheValueHolder> entry) {
					logger.log(logLevel, () -> "[EXPIRED] " + cacheNameSupplier.get() + " key: '" + entry.getKey() + "' value: '"
							+ entry.getValue().getResult() + "'");
				}
			});
		}

		EntryLogging removeEntryLogging = configuration.getRemoveEntryLogging();
		if (removeEntryLogging != null) {
			boolean async = removeEntryLogging.getAsync();
			com.braintribe.logging.Logger.LogLevel logLevel = LogLevels.convert(removeEntryLogging.getLogLevel());
			CacheEntryRemovedListener<String, CacheValueHolder> listener = new CacheEntryRemovedListener<String, CacheValueHolder>() {

				@Override
				public void onEntryRemoved(Cache<String, CacheValueHolder> cache, CacheEntry<String, CacheValueHolder> entry) {
					logger.log(logLevel, () -> "[REMOVED] " + cacheNameSupplier.get() + " key: '" + entry.getKey() + "' value: '"
							+ entry.getValue().getResult() + "'");
				}
			};
			if (async) {
				builder.addAsyncListener(listener);
			} else {
				builder.addListener(listener);
			}
		}

		EntryLogging updateEntryLogging = configuration.getUpdateEntryLogging();
		if (updateEntryLogging != null) {
			boolean async = updateEntryLogging.getAsync();
			com.braintribe.logging.Logger.LogLevel logLevel = LogLevels.convert(updateEntryLogging.getLogLevel());
			CacheEntryUpdatedListener<String, CacheValueHolder> listener = new CacheEntryUpdatedListener<String, CacheValueHolder>() {

				@Override
				public void onEntryUpdated(Cache<String, CacheValueHolder> cache, CacheEntry<String, CacheValueHolder> currentEntry,
						CacheEntry<String, CacheValueHolder> entryWithNewData) {
					logger.log(logLevel, () -> "[UPDATED] " + cacheNameSupplier.get() + " key: '" + currentEntry.getKey() + "' currentEntry: '"
							+ currentEntry.getValue().getResult() + "' newEntry: '" + entryWithNewData.getValue().getResult());
				}
			};
			if (async) {
				builder.addAsyncListener(listener);
			} else {
				builder.addListener(listener);
			}
		}

		cache = builder.build();

	}

	@Override
	public void preDestroy() {
		if (cache != null) {
			cache.clearAndClose();
		}
		if (refreshAheadPool != null) {
			refreshAheadPool.shutdown();

			try {
				boolean shutdown = refreshAheadPool.awaitTermination(maxRefreshAheadPoolTerminationTime, TimeUnit.SECONDS);
				if (!shutdown) {
					refreshAheadPool.shutdownNow();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	// -----------------------------------------------------------------------
	// IMPLEMENTATION
	// -----------------------------------------------------------------------

	@Override
	public Cache2kCacheAspectStatus retriveCacheStatus() {

		Cache2kCacheAspectStatus status = Cache2kCacheAspectStatus.T.create();

		InternalCacheInfo info = ((InternalCache<String, CacheValueHolder>) cache).getInfo();

		status.setName(info.getName());
		status.setSize(info.getSize());
		status.setCapacity(info.getHeapCapacity());
		status.setHitRate(info.getHitRate());
		status.setGetCount(info.getGetCount());
		status.setMissCount(info.getMissCount());
		status.setPutCount(info.getPutCount());
		status.setCacheCount(info.getHeapHitCount());
		status.setNewEntryCount(info.getNewEntryCount());
		status.setExpiredCount(info.getExpiredCount());
		status.setRemovedCount(info.getRemoveCount());
		status.setClearCount(info.getClearCount());
		status.setRemoveByClearCount(info.getClearedEntriesCount());
		status.setEvictedCount(info.getEvictedCount());
		status.setEvictionRunningCount(info.getEvictionRunningCount());
		status.setGoneSpinCount(info.getGoneSpinCount());
		status.setHashCollisionCount(info.getHashCollisionCount());
		status.setHashCollisionSlotCount(info.getHashCollisionSlotCount());
		status.setHashQuality(info.getHashQuality());
		status.setStartTime(new Date(info.getStartedTime()));
		status.setInternalExceptions(info.getInternalExceptionCount());

		return status;
	}

	@Override
	public Object retrieveCacheResult(Supplier<Object> resultProvider, ServiceRequest request, String hash) {
		Mode mode = configuration.getMode();

		CacheValueHolder holder = cache.computeIfAbsent(hash, () -> {
			Object result = resultProvider.get();
			if (result == null && !configuration.getPermitNullValues()) {
				throw new IllegalStateException("'null' values are not allowed - change configuration to allow them");
			}
			if (refreshAheadTime == 0) {
				return CacheValueHolder.create(result, mode, resultProvider, request);
			}
			return RefreshAheadCacheValueHolder.create(result, mode, resultProvider, request);
		});

		if (refreshAheadTime > 0) {
			boolean updateResult = holder.updateResult(refreshAheadTime);
			if (updateResult) {
				refreshAheadPool.submit(() -> {
					Object result = resultProvider.get();
					cache.put(hash, RefreshAheadCacheValueHolder.create(result, mode, resultProvider, request));
				});
			}
		}

		Object result = holder.getResult();
		return result;
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

	@Override
	public void removeEntry(String hash) {
		cache.remove(hash);
	}

	@Override
	public boolean containsEntry(String hash) {
		return cache.containsKey(hash);
	}

	@Override
	public CacheValueHolder getEntry(String hash) {
		CacheValueHolder holder = cache.get(hash);
		return holder;
	}

	@Override
	public Map<String, CacheValueHolder> getAllEntries() {
		return cache.asMap();
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setCacheNameSupplier(Supplier<String> cacheNameSupplier) {
		this.cacheNameSupplier = cacheNameSupplier;
	}

	@Configurable
	@Required
	public void setConfiguration(Cache2kCacheAspectConfiguration configuration) {
		this.configuration = configuration;
	}

}
