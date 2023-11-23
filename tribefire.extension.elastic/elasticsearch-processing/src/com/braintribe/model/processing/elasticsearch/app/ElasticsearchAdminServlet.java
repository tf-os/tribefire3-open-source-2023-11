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
package com.braintribe.model.processing.elasticsearch.app;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.elasticsearch.service.ElasticReflection;
import com.braintribe.model.elasticsearch.service.ReflectElastic;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchreflection.ElasticsearchReflection;
import com.braintribe.model.elasticsearchreflection.ElasticsearchReflectionError;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;
import com.braintribe.web.servlet.TypedVelocityContext;

public class ElasticsearchAdminServlet extends BasicTemplateBasedServlet implements InitializationAware {

	private static Logger logger = Logger.getLogger(ElasticsearchAdminServlet.class);

	private static final long serialVersionUID = -74355987938567927L;

	private static final String adminServiceTemplateLocation = "com/braintribe/model/processing/elasticsearch/app/templates/elasticsearchAdmin.html.vm";
	private static final String adminServiceErrorTemplateType = "error";
	private static final String adminServiceErrorTemplateLocation = "com/braintribe/model/processing/elasticsearch/app/templates/error.html.vm";

	protected Evaluator<ServiceRequest> requestEvaluator;

	private Supplier<PersistenceGmSession> cortexSessionFactory;

	@Override
	public void postConstruct() {
		setTemplateLocation(adminServiceTemplateLocation);
		super.addTemplateLocation(adminServiceErrorTemplateType, adminServiceErrorTemplateLocation);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.service(req, resp);
	}

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
		TypedVelocityContext context = new TypedVelocityContext();

		String relativePath = "../..";
		String requestURI = request.getRequestURI();
		if (requestURI.endsWith("/")) {
			relativePath = "../..";
		} else {
			relativePath = "..";
		}
		context.put("tfsRelativePath", relativePath);

		try {
			ReflectElastic reflectRequest = ReflectElastic.T.create();

			PersistenceGmSession cortexSession = this.cortexSessionFactory.get();

			String conId = getSingleParameterAsString(request, "connector");
			if (conId != null) {
				ElasticsearchConnector con = queryConnector(cortexSession, conId);
				if (con != null) {
					reflectRequest.setElasticsearchConnector(con);
				}
			}

			EvalContext<? extends ElasticReflection> eval = reflectRequest.eval(cortexSession);
			ElasticReflection elasticReflection = eval.get();

			ElasticsearchReflection elasticsearch = elasticReflection.getElasticsearchReflection();

			ElasticsearchReflection elasticsearchReflection = elasticReflection.getElasticsearchReflection();
			ElasticsearchReflectionError elasticsearchReflectionError = elasticsearchReflection.getElasticsearchReflectionError();

			if (elasticsearchReflectionError != null) {
				context.setType(adminServiceErrorTemplateType);
				context.put("elasticsearchReflectionError", elasticsearchReflectionError);
				context.put("header_information", "Elasticsearch Admin Error");
			} else {
				context.put("elasticsearch", elasticsearch);
			}

		} catch (Exception e) {
			logger.error("Error while trying to collect Elasticsearch information.", e);
		}

		context.put("current_year", new GregorianCalendar().get(Calendar.YEAR));

		return context;
	}

	protected ElasticsearchConnector queryConnector(PersistenceGmSession cortexSession, String externalId) throws GmSessionException, Exception {

		EntityQuery query = EntityQueryBuilder.from(ElasticsearchConnector.T).where().property(Deployable.externalId).eq(externalId).done();

		ElasticsearchConnector con = cortexSession.query().entities(query).first();
		return con;
	}

	protected String getSingleParameterAsString(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> param) {
		this.cortexSessionFactory = param;
	}
}
