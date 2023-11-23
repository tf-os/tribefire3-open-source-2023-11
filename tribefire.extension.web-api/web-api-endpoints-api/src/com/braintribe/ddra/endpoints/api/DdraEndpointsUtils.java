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
package com.braintribe.ddra.endpoints.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.codec.marshaller.api.EntityRecurrenceDepth;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.IdentityManagementModeOption;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.ddra.endpoints.api.api.v1.ApiV1EndpointContext;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.mimetype.MimeTypeParser;
import com.braintribe.mimetype.ParsedMimeType;
import com.braintribe.model.DdraEndpoint;
import com.braintribe.model.DdraEndpointDepth;
import com.braintribe.model.DdraEndpointDepthKind;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.lcd.StringTools;

/**
 * TODO this should be split up and wired into where it's needed...
 */
public class DdraEndpointsUtils {

	public static final String APPLICATION_JSON = "application/json";

	public static <T> T evaluateServiceRequest(Evaluator<ServiceRequest> evaluator, ServiceRequest service) {
		try {
			EvalContext<?> context = service.eval(evaluator);
			return (T) context.get();
		} catch (RuntimeException e) {
			// TODO handle other exceptions, check the root cause instead??
			if (e instanceof AuthorizationException || e instanceof SecurityServiceException) {
				HttpExceptions.unauthotized(e.getMessage());
			}

			throw e;
		}
	}

	public static void writeResponse(DdraTraversingCriteriaMap criteriasMap, DdraEndpointContext<?> context, Object result, boolean full)
			throws IOException {
		writeResponse(criteriasMap, context, result, BaseType.INSTANCE, full);
	}

	public static void writeResponse(DdraTraversingCriteriaMap criteriasMap, DdraEndpointContext<?> context, Object result,
			GenericModelType expectedResponseType, boolean full) throws IOException {

		if (context.getForceResponseCode() == null && result == Neutral.NEUTRAL) {
			HttpServletResponse response = context.getResponse();
			response.setStatus(204); // No Content
			response.getOutputStream().close(); // Commit response
			return;
		}

		DdraEndpoint endpoint = context.getEndpoint();
		com.braintribe.model.ddra.endpoints.OutputPrettiness endpointPrettiness = endpoint.getPrettiness();
		com.braintribe.model.ddra.endpoints.TypeExplicitness endpointTypeExplicitness = endpoint.getTypeExplicitness();
		OutputPrettiness prettyness = (endpointPrettiness == null) ? null : OutputPrettiness.valueOf(endpointPrettiness.name());
		TypeExplicitness typeExplicitness = (endpointTypeExplicitness == null) ? null : TypeExplicitness.valueOf(endpointTypeExplicitness.name());

		// TODO .useDirectPropertyAccess(true) crashes everything.
		GmSerializationOptions options = GmSerializationOptions.defaultOptions.derive() //
				.outputPrettiness(prettyness) //
				.stabilizeOrder(context.getEndpoint().getStabilizeOrder()) //
				.writeEmptyProperties(endpoint.getWriteEmptyProperties()) //
				.writeAbsenceInformation(endpoint.getWriteAbsenceInformation()) //
				.set(TypeExplicitnessOption.class, typeExplicitness) //
				.set(EntityRecurrenceDepth.class, endpoint.getEntityRecurrenceDepth()) //
				.set(IdentityManagementModeOption.class, IdentityManagementMode.valueOf(endpoint.getIdentityManagementMode().name())) //
				.set(EntityVisitorOption.class, context.getMarshallingVisitor()).inferredRootType(expectedResponseType) //
				.build();

		Object toWrite = full ? result : getResultWithDepth(context.getEndpoint().getComputedDepth(), criteriasMap, result);

		try (OutputStream out = context.openResponseOutputStream()) {
			context.getMarshaller().marshall(out, toWrite, options);
		}
	}

	private static Object getResultWithDepth(DdraEndpointDepth depth, DdraTraversingCriteriaMap criteriasMap, Object result) {
		if (depth.getKind() == DdraEndpointDepthKind.reachable) {
			return result;
		}
		return cloneWithTraversingCriterion(criteriasMap.getCriterion(depth), result);
	}

	private static Object cloneWithTraversingCriterion(TraversingCriterion traversingCriterion, Object result) {
		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(traversingCriterion);

		StandardCloningContext cloningContext = new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity entity) {
				GenericEntity clonedEntity = super.supplyRawClone(entityType, entity);

				if (entity.hasTransientData()) {
					if (entity instanceof TransientSource) {
						TransientSource transientSource = (TransientSource) entity;
						TransientSource clonedTransientSource = (TransientSource) clonedEntity;
						clonedTransientSource.setInputStreamProvider(transientSource.getInputStreamProvider());
					} else if (entity instanceof CallStreamCapture) {
						CallStreamCapture callStreamCapture = (CallStreamCapture) entity;
						CallStreamCapture clonedCallStreamCapture = (CallStreamCapture) clonedEntity;
						clonedCallStreamCapture.setOutputStreamProvider(callStreamCapture.getOutputStreamProvider());
					}
				}

				return clonedEntity;
			}
		};

		cloningContext.setMatcher(matcher);
		cloningContext.setAbsenceResolvable(true);

		return BaseType.INSTANCE.clone(cloningContext, result, StrategyOnCriterionMatch.partialize);
	}

	public static void computeDepth(DdraEndpoint endpoint) {
		DdraEndpointDepth depth = DdraEndpointDepth.T.create();
		endpoint.setComputedDepth(depth);
		if (DdraEndpointDepthKind.shallow.name().equals(endpoint.getDepth())) {
			depth.setKind(DdraEndpointDepthKind.shallow);
		} else if (DdraEndpointDepthKind.reachable.name().equals(endpoint.getDepth())) {
			depth.setKind(DdraEndpointDepthKind.reachable);
		} else {
			try {
				depth.setKind(DdraEndpointDepthKind.custom);
				depth.setCustomDepth(Integer.valueOf(endpoint.getDepth()));
			} catch (NumberFormatException e) {
				HttpExceptions.badRequest("Invalid depth parameter, expected \"shallow\", \"reachable\" or a number > 0 but got %s",
						endpoint.getDepth());
			}

			if (depth.getCustomDepth() < 0) {
				HttpExceptions.badRequest("Invalid depth parameter, expected \"shallow\", \"reachable\" or a number > 0 but got %s",
						endpoint.getDepth());
			}
		}
	}

	public static Marshaller getInMarshallerFor(MarshallerRegistry registry, DdraEndpoint endpoint) {
		return getInMarshallerFor(registry, endpoint.getContentType());
	}

	public static Marshaller getInMarshallerFor(MarshallerRegistry registry, String contentTypeParameter) {
		final String contentType;
		if (StringTools.isBlank(contentTypeParameter)) {
			// In some cases it's not possible to set the content type
			// - for example in a part of a form-data request when it comes from a native HTML form
			// Therefore we need a default
			// TODO: Maybe this is not the correct place to specify a default
			// If one is specified before we can throw an exception:
			// HttpExceptions.notAcceptable("Please specify a Content-Type header to unmarshall the body.");
			contentType = APPLICATION_JSON;
		} else {
			contentType = contentTypeParameter;
		}

		Marshaller marshaller = registry.getMarshaller(getMimeType(contentType));

		if (marshaller == null && contentTypeParameter != null) {
			String mimeType = getMimeType(contentTypeParameter);
			if ("text/plain".equalsIgnoreCase(mimeType)) {
				// This is a sensible fallback as a JSON part can also be sent as a text/plain part.
				marshaller = registry.getMarshaller(getMimeType(APPLICATION_JSON));
			}
		}

		if (marshaller != null) {
			return marshaller;
		}

		HttpExceptions.notAcceptable("Unsupported Content-Type: %s.", contentTypeParameter);
		return null;
	}

	public static <E extends DdraEndpoint> void computeOutMarshallerFor(MarshallerRegistry registry, DdraEndpointContext<E> context,
			String defaultMimeType) {
		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver(registry);
		acceptHeaderResolver.setInternalMimeTypeBias(APPLICATION_JSON);
		String mimeType = acceptHeaderResolver.resolveMimeType(context.getEndpoint().getAccept(), defaultMimeType);
		Marshaller marshaller = registry.getMarshaller(mimeType);

		context.setMimeType(mimeType);
		context.setMarshaller(marshaller);
	}

	private static String selectMimeType(List<MarshallerRegistryEntry> entries, List<String> preferedMimeTypes) {
		int firstFoundMimeTypeIndex = -1;

		for (MarshallerRegistryEntry entry : entries) {
			String mimeType = entry.getMimeType();
			int indexOfFound = preferedMimeTypes.indexOf(mimeType);

			if (indexOfFound >= 0 && (firstFoundMimeTypeIndex == -1 || indexOfFound < firstFoundMimeTypeIndex)) {
				firstFoundMimeTypeIndex = indexOfFound;
			}
		}

		if (firstFoundMimeTypeIndex >= 0) {
			return preferedMimeTypes.get(firstFoundMimeTypeIndex);
		}

		return entries.get(0).getMimeType();
	}

	// TODO UNUSED as of 20.7.2021. Delete if not needed!!!
	private static String getOutMimeTypeFor(MarshallerRegistry registry, DdraEndpoint endpoint, String defaultMimeType) {
		List<String> preferedMimeTypes = new ArrayList<>(); // in case of wildcard matches these mime types have
															// priority
		if (defaultMimeType != null) {
			preferedMimeTypes.add(defaultMimeType);
		}
		preferedMimeTypes.add(APPLICATION_JSON);

		String firstMatch = null;

		for (String accept : endpoint.getAccept()) {
			String mimeType = getMimeType(accept);

			List<MarshallerRegistryEntry> entries = registry.getMarshallerRegistryEntries(mimeType);
			if (entries.isEmpty()) {
				continue;
			}

			if (firstMatch == null) {
				firstMatch = selectMimeType(entries, preferedMimeTypes);
			}

			if (defaultMimeType == null) {
				return firstMatch;
			}

			for (MarshallerRegistryEntry entry : entries) {
				if (entry.getMimeType().equals(defaultMimeType)) {
					return defaultMimeType;
				}
			}
		}

		return firstMatch != null ? firstMatch : APPLICATION_JSON;
	}

	/**
	 * Extract the mime type part from the content type header, since the given content type may include a character
	 * encoding specification, for example, {@code text/html;charset=UTF-8}.
	 * <p>
	 * In case the request content type contains further specialization, for example,
	 * {@code test/html;spec=module-checks-response}, the <code>spec</code> attribute is the only mime type attribute being
	 * retained.
	 */
	private static String getMimeType(String requestContentType) {

		if (requestContentType == null)
			return requestContentType;

		ParsedMimeType parsedType = MimeTypeParser.getParsedMimeType(requestContentType);
		parsedType.getParams().keySet().retainAll(Collections.singleton("spec"));

		return parsedType.toString();
	}

	public static String getPathInfo(ApiV1EndpointContext context) {
		String pathInfo = context.getRequest().getPathInfo() != null ? context.getRequest().getPathInfo() : "";

		if (pathInfo.startsWith("/")) {
			return pathInfo;
		}

		return "/" + pathInfo;
	}

	public static void setAllowHeader(DdraEndpointContext<?> context, Collection<String> allowedMethods) {
		String allowHeader = String.join(", ", allowedMethods);
		context.getResponse().addHeader("Allow", allowHeader);
	}
}
