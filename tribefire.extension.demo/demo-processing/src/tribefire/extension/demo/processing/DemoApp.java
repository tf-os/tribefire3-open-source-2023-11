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
package tribefire.extension.demo.processing;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.genericmodel.SelectiveInformationResolver;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;

import tribefire.extension.demo.model.api.FindByText;
import tribefire.extension.demo.model.api.GetEmployeesByGender;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Gender;
import tribefire.extension.demo.model.data.Person;

/**
 * This {@link HttpServlet} implementation creates a session for the configured access, using the configured user and password in its {@link #doGet(HttpServletRequest, HttpServletResponse)}
 * method. From this session, it queries all {@link Person} entities and renders them in a simple HTML output. If an icon is set for the
 * User this image will also be streamed. 
 *
 */
public class DemoApp extends BasicTemplateBasedServlet implements InitializationAware {

	private static final long serialVersionUID = -5706028141561689402L;
	private static final String templateLocation = "tribefire/extension/demo/processing/templates/demo.html.vm";
	
	private String accessId;
	private String user;
	private String password;
	
	
	@Required @Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Required @Configurable
	public void setUser(String user) {
		this.user = user;
	}
	
	@Required @Configurable
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public void postConstruct() {
		setTemplateLocation(templateLocation);
	}
	
	/**
	 * Based on given parameter a list of GenericEntity will be determined and rendered based on a Velocity template. 
	 */
	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse repsonse) {
		
		VelocityContext context = new VelocityContext();
		
		try {
		
			List<GenericEntity> entities = new ArrayList<>(); 
			String parameter = request.getParameter("parameter");
			EntityType<? extends GenericEntity> entityType = null;
			boolean isTypeListing = false;
			
			/*
			 * Create new session for the configured access using configured user and password
			 */
			PersistenceGmSession session = newSession(parameter);
			
			
			if (parameter == null) {
				
				entities.addAll(listEntityTypes(session));
				entityType = GmEntityType.T;
				isTypeListing = true;
				
			} else if (parameter.equals("getByGender")) {

				entities.addAll(getEmployeeByGender(request, session));
				entityType = Person.T;
				
				
			} else if (parameter.equals("findByText")) {
				
				String type = request.getParameter("type");
				entities.addAll(findByText(request,session, type));
				entityType = GMF.getTypeReflection().getEntityType(type);
				
			} else {
				
				entityType = GMF.getTypeReflection().getEntityType(parameter);
				
				/*
				 * Query all entities via the session. 
				 */
				EntityQuery query = EntityQueryBuilder
						.from(entityType)
						.done();
				entities = session
						.query()
						.entities(query)
						.list();

			}
			
			
			context.put("entities", entities);
			context.put("entityTypeDisplayInfo", entityType.getShortName());
			context.put("entityType", entityType);
			context.put("tribefireRuntime", new TribefireRuntime());
			context.put("session", session);
			context.put("selectiveInfoResolver", new SelectiveInformationResolver());
			context.put("isTypeListing", isTypeListing);
			context.put("typeReflection", GMF.getTypeReflection());
			context.put("typeTool", this);
			context.put("isCompany", entityType.isAssignableFrom(Company.T));
			context.put("staticResUrl", TribefireRuntime.getPublicServicesUrl() + "/res");
			
			return context;
			
			
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
		
	}

	private List<GenericEntity> findByText(HttpServletRequest request, PersistenceGmSession session, String type) {
		
		String text = request.getParameter("text");
		if (text == null) {
			text = "*";
		}
		
		FindByText byTextRequest = FindByText.T.create();
		byTextRequest.setType(type);
		byTextRequest.setText(text);
		
		EvalContext<? extends List<GenericEntity>> result = byTextRequest.eval(session);
		return result.get();
	}

	private List<GenericEntity> getEmployeeByGender(HttpServletRequest request, PersistenceGmSession session) {
		long companyId = Long.parseLong(request.getParameter("company"));
		Gender gender = Gender.valueOf(request.getParameter("gender"));
		
		EntityQuery companyQuery = EntityQueryBuilder.from(Company.T).where().property("id").eq(companyId).done();
		Company company = session.query().entities(companyQuery).first();
		
		
		GetEmployeesByGender getEmployeesRequest = GetEmployeesByGender.T.create();
		getEmployeesRequest.setGender(gender);
		getEmployeesRequest.setCompany(company);
		
		EvalContext<? extends List<GenericEntity>> result = getEmployeesRequest.eval(session);
		List<GenericEntity> persons = result.get();
		return persons;
	}

	private List<GmEntityType> listEntityTypes(PersistenceGmSession session) {
		EntityQuery modelQuery = EntityQueryBuilder.from(GmEntityType.T).where().property("declaringModel.name").eq("tribefire.extension.demo:demo-model").orderBy().property("typeSignature").done();
		
		List<GmEntityType> entityTypes = session.query().entities(modelQuery).list();
		return entityTypes;
	}

	private PersistenceGmSession newSession(String entityTypeParameter) {
		PersistenceGmSession session = null;
		if (entityTypeParameter == null) {
			session = getSessionFactory().newSession("cortex");
		} else {
			session = getSessionFactory().newSession(accessId);
		}
		return session;
	}
	
	public static <T extends GenericModelType> T getTypeForValue(Object value) {
		return GMF.getTypeReflection().getType(value);
	}
	
	/**
	 * @return a SessionFactory using configured user credentials. 
	 */
	public PersistenceGmSessionFactory getSessionFactory()	{
		String servicesUrl = TribefireRuntime.getServicesUrl();
		
		PersistenceGmSessionFactory sessionFactory;
		try {
			sessionFactory = GmSessionFactories.remote(servicesUrl).authentication(user, password).done();
		} catch (GmSessionFactoryBuilderException e) {
			throw new RuntimeException(e);
		}

		return sessionFactory;
	}
	
}
