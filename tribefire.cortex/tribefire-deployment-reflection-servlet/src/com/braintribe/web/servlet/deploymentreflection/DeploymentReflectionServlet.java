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
package com.braintribe.web.servlet.deploymentreflection;

import static com.braintribe.web.servlet.deploymentreflection.DeploymentReflectionServletUtils.getParameterMapAsString;
import static com.braintribe.web.servlet.deploymentreflection.DeploymentReflectionServletUtils.getSingleParameterAsBoolean;
import static com.braintribe.web.servlet.deploymentreflection.DeploymentReflectionServletUtils.getSingleParameterAsString;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.EXTERNALID;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.HIDE_NAV;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.IS_ASSIGNABLE_TO;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.NODE_ID;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.TYPE_SIGNATURE;
import static com.braintribe.web.servlet.deploymentreflection.UrlFilters.WIRE_KIND;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentreflection.DeploymentSummary;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnits;
import com.braintribe.model.deploymentreflection.request.GetDeploymentSummaryPlain;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;
import com.braintribe.web.servlet.TypedVelocityContext;

/**
 * <p>
 * TODO JAVADOC
 * 
 * @author christina.wilpernig
 */
public class DeploymentReflectionServlet extends BasicTemplateBasedServlet implements InitializationAware {

	private static final long serialVersionUID = 6667756826152502463L;
	private static Logger logger = Logger.getLogger(DeploymentReflectionServlet.class);

	protected Supplier<PersistenceGmSession> cortexSessionFactory;
	protected LiveInstances liveInstances;
	protected Evaluator<ServiceRequest> requestEvaluator;

	protected DeploymentReflectionServletUtils servletUtils;

	private static final String templateLocation = "com/braintribe/web/servlet/deploymentreflection/templates/tfDeploymentReflection.html.vm";

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

	@Configurable
	@Required
	public void setNodeFactory(LiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}

	@Override
	public void postConstruct() {
		setRefreshFileBasedTemplates(true);
		setTemplateLocation(templateLocation);

		servletUtils = new DeploymentReflectionServletUtils(liveInstances);
	}

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
		logger.debug(() -> "Received DeploymentSummary request with parameters: " + getParameterMapAsString(request));

		TypedVelocityContext context = new TypedVelocityContext();

		EventCartridge cartridge = new EventCartridge();
		cartridge.addReferenceInsertionEventHandler((s, r, v) -> {
			return DOMTools.encode(v.toString());
		});
		context.attachEventCartridge(cartridge);

		try {
			Set<String> nodeIds = servletUtils.getNodeIds();
			Set<String> cartridgeIds = servletUtils.getCartridgeIds();

			String type = getSingleParameterAsString(request, TYPE_SIGNATURE);
			String typeSignature = determineType(type);
			context.put("typeSignatureFilter", type);

			GetDeploymentSummaryPlain summaryPlain = createPlainRequest(request, typeSignature);
			DeploymentSummary summary = summaryPlain.eval(requestEvaluator).get();

			DeploymentReflectionVelocityTools tools = new DeploymentReflectionVelocityTools(liveInstances);
			tools.setNodeIds(nodeIds);
			tools.setCartridgeIds(cartridgeIds);
			context.put("tools", tools);

			long unitsComplete = summary.getSourceUnits().stream().filter(i -> i.getIsComplete()).count();
			context.put("qualifiedUnitsComplete", unitsComplete);

			// information that is needed for the navigation filters
			context.put("nodeIds", nodeIds);
			context.put("cartridgeIds", cartridgeIds);
			context.put("wireKinds", servletUtils.getWireKinds());

			context.put("wireKindFilter", summaryPlain.getWireKind());
			context.put("isAssignableToFilter", summaryPlain.getIsAssignableTo());
			context.put("nodeIdFilter", summaryPlain.getNodeId());

			context.put("externalIdFilter", summaryPlain.getExternalIdPattern());

			Map<Deployable, QualifiedDeployedUnits> unitsByDeployable = summary.getUnitsByDeployable();

			Map<Deployable, QualifiedDeployedUnits> sorted = sort(unitsByDeployable);

			context.put("summary", summary);
			context.put("unitsByDeployable", sorted);
			context.put("hideNav", getSingleParameterAsBoolean(request, HIDE_NAV));
			context.put("requestUrl", request.getRequestURL());

		} catch (Exception e) {
			throw new RuntimeException("Could not create DeploymentSummary context", e);
		} finally {
			logger.debug(() -> "Finished request handling for DeploymentSummary creation.");
		}

		return context;
	}

	/**
	 * HELPER METHODS
	 */

	private Map<Deployable, QualifiedDeployedUnits> sort(Map<Deployable, QualifiedDeployedUnits> unitsByDeployable) {
		Comparator<Deployable> externalIdComparator = new Comparator<Deployable>() {
			@Override
			public int compare(Deployable d1, Deployable d2) {
				return d1.getExternalId().toLowerCase().compareTo(d2.getExternalId().toLowerCase());
			}
		};

		SortedMap<Deployable, QualifiedDeployedUnits> map = new TreeMap<Deployable, QualifiedDeployedUnits>(externalIdComparator);
		map.putAll(unitsByDeployable);

		return map;
	}

	private GetDeploymentSummaryPlain createPlainRequest(HttpServletRequest request, String typeSignature) {
		GetDeploymentSummaryPlain summaryPlain = GetDeploymentSummaryPlain.T.create();
		summaryPlain.setNodeId(getSingleParameterAsString(request, NODE_ID));
		summaryPlain.setExternalIdPattern(getSingleParameterAsString(request, EXTERNALID));

		String wireKind = getSingleParameterAsString(request, WIRE_KIND);
		summaryPlain.setWireKind(wireKind);

		summaryPlain.setTypeSignature(typeSignature);
		Boolean pIsAssignableTo = getSingleParameterAsBoolean(request, IS_ASSIGNABLE_TO);
		summaryPlain.setIsAssignableTo(CommonTools.getValueOrDefault(pIsAssignableTo, Boolean.FALSE));

		return summaryPlain;
	}

	private String determineType(String type) {
		// by definition a type containing '.' is seen as a fully qualified type signature
		if (type == null || type.contains(".")) {
			return type;
		}
		PersistenceGmSession session = cortexSessionFactory.get();
		EntityQuery query = EntityQueryBuilder.from(GmEntityType.T).where().property("typeSignature").like("*\\." + type).done();
		GmEntityType t = session.query().entities(query).first();

		return (t != null) ? t.getTypeSignature() : null;
	}

}
