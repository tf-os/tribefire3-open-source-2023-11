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
package tribefire.extension.cache.model.deployment.service.cache2k;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;

import tribefire.extension.cache.model.deployment.service.InMemoryCacheAspectConfiguration;

public interface Cache2kCacheAspectConfiguration extends InMemoryCacheAspectConfiguration {

	final EntityType<? extends Cache2kCacheAspectConfiguration> T = EntityTypes.T(Cache2kCacheAspectConfiguration.class);

	String maxCacheEntries = "maxCacheEntries";
	String enableStatistics = "enableStatistics";
	String expiration = "expiration";
	String permitNullValues = "permitNullValues";
	String highPerformanceMode = "highPerformanceMode";
	String createEntryLogging = "createEntryLogging";
	String expireEntryLogging = "expireEntryLogging";
	String removeEntryLogging = "removeEntryLogging";
	String updateEntryLogging = "updateEntryLogging";
	String mode = "mode";
	String refreshAheadConfiguration = "refreshAheadConfiguration";

	@Initializer("100000l")
	@Mandatory
	long getMaxCacheEntries();
	void setMaxCacheEntries(long maxCacheEntries);

	@Initializer("true")
	@Mandatory
	boolean getEnableStatistics();
	void setEnableStatistics(boolean enableStatistics);

	@Mandatory
	Expiration getExpiration();
	void setExpiration(Expiration expiration);

	@Initializer("true")
	@Mandatory
	boolean getPermitNullValues();
	void setPermitNullValues(boolean permitNullValues);

	@Initializer("true")
	@Mandatory
	boolean getHighPerformanceMode();
	void setHighPerformanceMode(boolean highPerformanceMode);

	EntryLogging getCreateEntryLogging();
	void setCreateEntryLogging(EntryLogging createEntryLogging);

	LogLevel getExpireEntryLogging();
	void setExpireEntryLogging(LogLevel expireEntryLogging);

	EntryLogging getRemoveEntryLogging();
	void setRemoveEntryLogging(EntryLogging removeEntryLogging);

	EntryLogging getUpdateEntryLogging();
	void setUpdateEntryLogging(EntryLogging updateEntryLogging);

	@Mandatory
	@Initializer("enum(tribefire.extension.cache.model.deployment.service.cache2k.Mode,PRODUCTION)")
	Mode getMode();
	void setMode(Mode mode);

	RefreshAheadConfiguration getRefreshAheadConfiguration();
	void setRefreshAheadConfiguration(RefreshAheadConfiguration refreshAheadConfiguration);

}
