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
package com.braintribe.model.processing.resource.streaming;

import static com.braintribe.utils.lcd.NullSafe.nonNull;

import java.util.Date;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.logging.Logger;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.cache.CacheType;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceDefaults;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.base.BinaryResponse;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.ManipulateBinary;
import com.braintribe.model.resourceapi.persistence.ManipulateBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;

public abstract class AbstractBinaryProcessor extends AbstractDispatchingServiceProcessor<BinaryRequest, BinaryResponse>
		implements BinaryPersistenceDefaults {

	private static final Logger logger = Logger.getLogger(AbstractBinaryProcessor.class);

	protected CacheType cacheType = CacheType.privateCache;
	protected Integer cacheMaxAge = Numbers.SECONDS_PER_DAY; // In seconds. Default is 24h.
	protected boolean cacheMustRevalidate = true;

	@Override
	protected void configureDispatching(DispatchConfiguration<BinaryRequest, BinaryResponse> dispatching) {
		dispatching.register(StreamBinary.T, this::stream);
		dispatching.register(GetBinary.T, this::get);
		dispatching.register(StoreBinary.T, this::store);
		dispatching.register(DeleteBinary.T, this::delete);
		dispatching.registerReasoned(ManipulateBinary.T, this::manipulate);
	}

	@SuppressWarnings("unused")
	private Maybe<ManipulateBinaryResponse> manipulate(ServiceRequestContext context, ManipulateBinary request) {
		return Reasons.build(UnsupportedOperation.T).text(getClass().getName() + " does not support ManipulateBinary request").toMaybe();
	}

	public StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request) {
		Resource resource = request.getResource();
		CacheControl cacheControl = cacheControlFor(resource, request);

		StreamBinaryResponse response = StreamBinaryResponse.T.create();
		response.setCacheControl(cacheControl);

		if (matchesCondition(cacheControl, request.getCondition())) {
			return stream(context, request, response);
		} else {
			response.setNotStreamed(true);
			return response;
		}
	}
	public GetBinaryResponse get(ServiceRequestContext context, GetBinary request) {
		Resource resource = request.getResource();

		CacheControl cacheControl = cacheControlFor(resource, request);

		GetBinaryResponse response = GetBinaryResponse.T.create();
		response.setCacheControl(cacheControl);

		if (matchesCondition(cacheControl, request.getCondition())) {
			return get(context, request, response);
		} else {
			return response;
		}
	}
	protected abstract StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary originalRequest, StreamBinaryResponse response);
	protected abstract GetBinaryResponse get(ServiceRequestContext context, GetBinary originalRequest, GetBinaryResponse response);
	protected abstract DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary originalRequest);
	protected abstract StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request);

	protected CacheControl cacheControlFor(Resource resource, BinaryRetrievalRequest request) {
		CacheControl cacheControl = CacheControl.T.create();

		cacheControl.setType(cacheType);
		cacheControl.setMustRevalidate(cacheMustRevalidate);
		if (cacheMaxAge != null) {
			cacheControl.setMaxAge(cacheMaxAge);
		}

		cacheControl.setFingerprint(resource.getMd5());
		cacheControl.setLastModified(getLastModifiedDate(resource, request));

		return cacheControl;
	}

	protected Date getLastModifiedDate(Resource resource, @SuppressWarnings("unused") BinaryRetrievalRequest request) {
		return resource.getCreated();
	}

	private boolean matchesCondition(CacheControl cacheControl, StreamCondition streamCondition) {
		if (streamCondition == null)
			return true;

		if (streamCondition instanceof FingerprintMismatch)
			return matchesFingerprintCondition(cacheControl, (FingerprintMismatch) streamCondition);

		if (streamCondition instanceof ModifiedSince)
			return matchesModifiedSinceCondition(cacheControl, (ModifiedSince) streamCondition);

		return true;
	}

	private boolean matchesFingerprintCondition(CacheControl cacheControl, FingerprintMismatch streamCondition) {
		String conditionFingerprint = streamCondition.getFingerprint();
		if (conditionFingerprint == null) {
			logger.warn(() -> "A " + FingerprintMismatch.class.getName() + " condition with no fingerprint was provided and will be ignored");
			return true;
		}

		String currentFingerprint = cacheControl.getFingerprint();
		if (currentFingerprint == null) {
			logger.warn(() -> "The resource lacks a fingerprint");
			return true;
		}

		boolean matches = !currentFingerprint.equals(conditionFingerprint);
		logger.trace(() -> "Compared given condition fingerprint [" + conditionFingerprint + "] with resource's fingerprint [" + currentFingerprint
				+ "] . Condition for streaming matched: " + matches);

		return matches;
	}

	private boolean matchesModifiedSinceCondition(CacheControl cacheControl, ModifiedSince streamCondition) {
		Date modifiedSince = streamCondition.getDate();
		if (modifiedSince == null) {
			logger.warn(() -> "A " + ModifiedSince.class.getName() + " condition with no date was provided and will be ignored");
			return true;
		}

		Date lastModified = cacheControl.getLastModified();
		if (lastModified == null) {
			logger.debug(() -> "No last modified date/time available");
			return true;
		}

		boolean matches = lastModified.after(modifiedSince);
		logger.trace(() -> "Compared given modified since condition  [" + modifiedSince + "] with path's last modified time [" + lastModified
				+ "] . Condition for streaming matched: " + matches);

		return matches;
	}

	@Configurable
	public void setCacheType(CacheType cacheType) {
		this.cacheType =  nonNull(cacheType, "cacheType");
	}

	@Configurable
	public void setCacheMaxAge(Integer cacheMaxAge) {
		this.cacheMaxAge = cacheMaxAge;
	}

	@Configurable
	public void setCacheMustRevalidate(boolean cacheMustRevalidate) {
		this.cacheMustRevalidate = cacheMustRevalidate;
	}

}
