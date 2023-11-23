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
package tribefire.extension.cache.model.status.cache2k;

import java.util.Date;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.cache.model.status.InMemoryCacheAspectStatus;

public interface Cache2kCacheAspectStatus extends InMemoryCacheAspectStatus {

	EntityType<Cache2kCacheAspectStatus> T = EntityTypes.T(Cache2kCacheAspectStatus.class);

	String name = "name";
	String startTime = "startTime";
	String size = "size";
	String capacity = "capacity";
	String hitRate = "hitRate";
	String getCount = "getCount";
	String missCount = "missCount";
	String putCount = "putCount";
	String cacheCount = "cacheCount";
	String newEntryCount = "newEntryCount";
	String expiredCount = "expiredCount";
	String removedCount = "removedCount";
	String clearCount = "clearCount";
	String removeByClearCount = "removeByClearCount";
	String evictedCount = "evictedCount";
	String evictionRunningCount = "evictionRunningCount";
	String goneSpinCount = "goneSpinCount";
	String hashCollisionCount = "hashCollisionCount";
	String hashCollisionSlotCount = "hashCollisionSlotCount";
	String hashQuality = "hashQuality";
	String internalExceptions = "internalExceptions";

	@Name("Name")
	@Description("Name of the cache.")
	@Priority(1.0d)
	String getName();
	void setName(String name);

	@Name("Start Time")
	@Priority(0.99d)
	Date getStartTime();
	void setStartTime(Date startTime);

	@Name("Size")
	@Description("Current number of entries in the cache. This may include entries with expired values.")
	@Priority(0.98d)
	long getSize();
	void setSize(long size);

	@Name("Capacity")
	@Description("Configured limit of the total cache entry capacity.")
	@Priority(0.97d)
	long getCapacity();
	void setCapacity(long capacity);

	@Name("Hit Rate")
	@Description("Hit rate of the cache.")
	@Priority(0.96d)
	double getHitRate();
	void setHitRate(double hitRate);

	@Name("'Get' Count")
	@Description("Number of cache operations, only access.")
	@Priority(0.95d)
	long getGetCount();
	void setGetCount(long getCount);

	@Name("'Miss' Count")
	@Description("A value was requested, either the entry is not present or the data was expired.")
	@Priority(0.94d)
	long getMissCount();
	void setMissCount(long missCount);

	@Name("'Put' Count")
	@Description("Entry was inserted in the cache.")
	@Priority(0.93d)
	long getPutCount();
	void setPutCount(long putCount);

	@Name("'Cache' Count")
	@Description("Total counted hits on the heap cache data. The counter is increased when an entry is present in the cache, regardless whether the value is valid or not.")
	@Priority(0.92d)
	long getCacheCount();
	void setCacheCount(long cacheCount);

	@Name("'New Entry' Count")
	@Description("Counts entries that expired. This counter includes removed entries from the cache and entries that are kept in the cache but expired.")
	@Priority(0.91d)
	long getNewEntryCount();
	void setNewEntryCount(long newEntryCount);

	@Name("'Expired' count")
	@Description("Number of created cache entries. Counter is increased for a load operation, put, etc. when the entry is not yet in the cache. A load operation always creates a new cache entry, even if the the expiry is immediately to block multiple loads. This counter is provided by the eviction implementation.")
	@Priority(0.90d)
	long getExpiredCount();
	void setExpiredCount(long expiredCount);

	@Name("'Removed' Count")
	@Description("Removed entries, because of programmatic removal. Removal of entries by clear is counted separately. Provided by the eviction implementation.")
	@Priority(0.89d)
	long getRemovedCount();
	void setRemovedCount(long removedCount);

	@Name("'Clear' Count")
	@Description("Number of calls to clear this cache has received.")
	@Priority(0.88d)
	long getClearCount();
	void setClearCount(long clearCount);

	@Name("'RemoveByClear' Count")
	@Description("Number of calls to clear this cache has received.")
	@Priority(0.87d)
	long getRemoveByClearCount();
	void setRemoveByClearCount(long removeByClearCount);

	@Name("'Evicted' Count")
	@Description("Entry was evicted.")
	@Priority(0.86d)
	long getEvictedCount();
	void setEvictedCount(long evictedCount);

	@Name("'Eviction Running' Count")
	@Description("Number of entries currently being evicted.")
	@Priority(0.85d)
	int getEvictionRunningCount();
	void setEvictionRunningCount(int evictionRunningCount);

	@Name("'Gone Spin' Count")
	@Description("Entry was removed while waiting to get the mutation lock.")
	@Priority(0.84d)
	long getGoneSpinCount();
	void setGoneSpinCount(long goneSpinCount);

	@Name("'Hash Collision' Count")
	@Description("Number of hashcode collisions within the cache. E.g. the hashCode: 2, 3, 3, 4, 4, 4 will mean three collisions.")
	@Priority(0.83d)
	int getHashCollisionCount();
	void setHashCollisionCount(int hashCollisionCount);

	@Name("'Hash Collision Slot' Count")
	@Description("Number of collision slots within the cache. E.g. the hashCode: 2, 3, 3, 4, 4, 4 will mean two collision slots.")
	@Priority(0.82d)
	int getHashCollisionSlotCount();
	void setHashCollisionSlotCount(int hashCollisionSlotCount);

	@Name("Hash Quality")
	@Description("Value between 0 and 100 to help evaluate the quality of the hashing function. 100 means perfect, there are no collisions. This metric takes into account the collision to size ratio, the longest collision size and the collisions to slot ratio. The value reads 0 if the longest collision size gets more then 20.")
	@Priority(0.81d)
	int getHashQuality();
	void setHashQuality(int hashQuality);

	@Name("Internal Exceptions")
	@Description("The cache produced an exception by itself that should have been prevented.")
	@Priority(0.80d)
	long getInternalExceptions();
	void setInternalExceptions(long internalExceptions);

}
