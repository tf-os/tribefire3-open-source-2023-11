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
package com.braintribe.ddra.endpoints.api.api.v1;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.ddra.endpoints.api.DdraEndpointsUtils;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * This class acts as a Map that can gets filled in from the DdraConfigurationStateChangeProcessor and can retrieve ddra
 * mapping from a HttpServletRequest.
 * 
 * This class is thread-safe to ensure that the ApiV1RestServlet is thread-safe.
 * 
 * A single instance of this class is shared between the instance of DdraConfigurationStateChangeProcessor and
 * ApiV1RestServlet.
 */
public class DdraMappings {

	private final Map<Pair<String, DdraUrlMethod>, SingleDdraMapping> mappings = new HashMap<>();

	private CloningContext cloningContext;

	private boolean ddraMappingsInitialized = false;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public DdraMappings() {
		this.cloningContext = createDefaultCloningContext();
	}

	private CloningContext createDefaultCloningContext() {
		TraversingCriterion tc = TC.create().negation().joker().done();

		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);

		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setMatcher(matcher);

		return cloningContext;
	}

	public void setMappings(Collection<DdraMapping> mappings) {
		lock.writeLock().lock();
		try {
			this.mappings.clear();

			for (DdraMapping mapping : mappings) {
				add(mapping);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public SingleDdraMapping get(String pathInfo, DdraUrlMethod method) {
		lock.readLock().lock();
		try {
			return mappings.get(getKey(pathInfo, method));
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> getMethods(ApiV1EndpointContext context) {
		lock.readLock().lock();
		try {
			Collection<String> mappedMethods = getMethods(DdraEndpointsUtils.getPathInfo(context));

			// The mapping exists explicitly
			if (!mappedMethods.isEmpty())
				return mappedMethods;

			// Implicit mapping generated from request type
			if (context.getServiceRequestType() != null)
				return Arrays.asList("GET", "POST");

			// No request type determined
			return Collections.EMPTY_LIST;
		} finally {
			lock.readLock().unlock();
		}
	}
	public Collection<String> getMethods(String pathInfo) {
		lock.readLock().lock();
		try {
			return mappings.keySet().stream().filter(k -> k.first.equals(pathInfo)).map(k -> k.second.name()).sorted().collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}

	/* public synchronized SingleDdraMapping get(HttpServletRequest request) { DdraUrlMethod method =
	 * DdraUrlMethod.valueOf(request.getMethod().toUpperCase()); return mappings.get(getKey(request.getPathInfo(), method));
	 * } */

	public Collection<SingleDdraMapping> getAll() {
		lock.readLock().lock();
		try {
			return mappings.values();
		} finally {
			lock.readLock().unlock();
		}
	}

	private void add(DdraMapping mapping) {
		SingleDdraMapping singleMapping = getSingleMappingFor(mapping);

		if (mapping.getMethod() == DdraUrlMethod.GET_POST) {
			mappings.put(getKey(mapping.getPath(), DdraUrlMethod.GET), singleMapping);
			mappings.put(getKey(mapping.getPath(), DdraUrlMethod.POST), singleMapping);
		} else {
			mappings.put(getKey(mapping.getPath(), mapping.getMethod()), singleMapping);
		}
	}

	private SingleDdraMapping getSingleMappingFor(DdraMapping mapping) {

		// get the requestTypeSignature
		String requestTypeSignature = mapping.getRequestType() != null ? mapping.getRequestType().getTypeSignature() : null;

		// get the serviceRequest
		ServiceRequest transformRequest = mapping.getTransformRequest() != null ? mapping.getTransformRequest().clone(cloningContext) : null;

		return new SingleDdraMapping(requestTypeSignature, transformRequest, mapping);
		/* return new SingleDdraMapping(mapping.getPath(), mapping.getMethod(), requestType, transformRequest,
		 * mapping.getDefaultProjection(), mapping.getDefaultMimeType(), mapping.getDefaultServiceDomain()); */
	}

	private Pair<String, DdraUrlMethod> getKey(String path, DdraUrlMethod method) {
		switch (method) {
			case DELETE:
			case GET:
			case POST:
			case PUT:
			case PATCH:
				return new Pair<>(path, method);
			case GET_POST:
			default:
				// TODO which exception type here? This is a code issue, should never happen
				throw new RuntimeException("Unexpected mapping method: " + method);
		}
	}

	public boolean isDdraMappingsInitialized() {
		return ddraMappingsInitialized;
	}
	public void setDdraMappingsInitialized(boolean ddraMappingsInitialized) {
		this.ddraMappingsInitialized = ddraMappingsInitialized;
	}
	@Configurable
	public void setCloningContext(CloningContext cloningContext) {
		this.cloningContext = cloningContext;
	}
}
