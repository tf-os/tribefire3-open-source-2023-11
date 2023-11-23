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
package tribefire.extension.scripting.common;

import java.util.function.Function;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnhancableCustomType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.utils.collection.impl.AttributeContexts;

import tribefire.extension.scripting.model.deployment.Script;

/**
 * The ScriptTools are passed to any ScriptedServiceProcessor (via {@link CommonScriptedProcessor}) as an extra facility. It can be used for various
 * helpful tasks.
 * <ul>
 * <li>{@link ScriptTools#abortWithReason(Reason)}</li>
 * <li>{@link ScriptTools#abortWithMaybe(Maybe)}</li>
 * <li>{@link ScriptTools#getLogger()}</li>
 * <li>{@link ScriptTools#getTypeReflection()}</li>
 * <li>{@link ScriptTools#getRuntimeProperty(String)}</li>
 * <li>{@link ScriptTools#create(String)}</li>
 * </ul>
 * 
 * @author Dirk Scheffler
 *
 */
public class ScriptTools { 

	private final Logger logger = Logger.getLogger(ScriptTools.class);
	private String loggerContext;

	private final Function<String, Object> runtimePropertyResolver;
	private PersistenceGmSessionFactory requestSessionFactory;
	private PersistenceGmSessionFactory systemSessionFactory;
	private Script script;
	private Deployable deployable;

	/**
	 * Constructor for the ScriptTools. For each script it can store a specific logger context String. In addition, the runtimePropertyResolver
	 * function can be overwritten here. The default is
	 * 
	 * @param loggerContext
	 *            The logger context for the script logger.
	 * @param runtimePropertyResolver
	 *            Is a `Function<String, Object>` to resolve runtime properties (a good default is `System::getProperty`)
	 * @param requestSessionFactory
	 * @param systemSessionFactory
	 * @param script
	 * @param deployable
	 */
	public ScriptTools(String loggerContext, Function<String, Object> runtimePropertyResolver, PersistenceGmSessionFactory requestSessionFactory,
			PersistenceGmSessionFactory systemSessionFactory, Script script, Deployable deployable) {
		this.loggerContext = loggerContext;
		this.runtimePropertyResolver = runtimePropertyResolver;
		this.requestSessionFactory = requestSessionFactory;
		this.systemSessionFactory = systemSessionFactory;
		this.script = script;
		this.deployable = deployable;
	}

	public PersistenceGmSessionFactory getSessionFactory() {
		return requestSessionFactory;
	}

	public PersistenceGmSessionFactory getSystemSessionFactory() {
		return systemSessionFactory;
	}

	public Script getScript() {
		return script;
	}

	public Deployable getDeployable() {
		return deployable;
	}

	// TODO docs
	public boolean isAuthenticated() {
		return getAttributeContext().findOrNull(RequestorSessionIdAspect.class) != null;
	}

	private AttributeContext getAttributeContext() {
		return AttributeContexts.peek();
	}

	/**
	 * To abort a script due to a failure and return a specific {@link Maybe} object. The {@link Maybe} may contain partial data.
	 * 
	 * @param maybe
	 *            The {@link Maybe} that is attached in the {@link UnsatisfiedMaybeTunneling} exception produced here.
	 */
	void abortWithMaybe(Maybe<?> maybe) {
		throw new UnsatisfiedMaybeTunneling(maybe);
	}

	/**
	 * To abort a script due to an internal failure and make this transparent via a Reason.
	 * 
	 * @param reason
	 *            The Reason for the script failure. The reason will be put into a Maybe and returned via a {@link UnsatisfiedMaybeTunneling}
	 *            exception.
	 */
	void abortWithReason(Reason reason) {
		abortWithMaybe(Maybe.empty(reason));
	}

	/**
	 * The logger object that can/should be used from within the script.
	 * 
	 * @return {@link Logger}
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * The {@link GenericModelTypeReflection} to be used from within the script.
	 * 
	 * @return {@link GenericModelTypeReflection}
	 */
	public GenericModelTypeReflection getTypeReflection() {
		return GMF.getTypeReflection();
	}

	/**
	 * Method to obtain runtime properties from within the script.
	 * 
	 * @param propertyName
	 *            The name of the property to return.
	 * @return Object
	 */
	public Object getRuntimeProperty(String propertyName) {
		return runtimePropertyResolver.apply(propertyName);
	}

	/**
	 * Can be used from within script to create new {@link GenericModelType}s.
	 * 
	 * @param typeSignature
	 *            The type signature of the {@link GenericModelType} to be produced.
	 * @return Object
	 */
	public Object create(String typeSignature) {
		GenericModelType type = getTypeReflection().getType(typeSignature);

		switch (type.getTypeCode()) {
			case entityType:
			case setType:
			case mapType:
			case listType:
				return ((EnhancableCustomType) type).create();
			default:
				/* PGA: Sorry if this caused problems to anyone, but I think this method only makes sense for entities and collections. If not, let's
				 * discuss this, cause we don't really have a real solution for this case right now. */
				throw new RuntimeException("Cannot create new instance of '" + typeSignature + "'. Resolved type: " + type);
		}
	}

	public void before() {
		if (loggerContext != null)
			logger.pushContext(loggerContext);
	}

	public void after() {
		if (loggerContext != null) {
			loggerContext = null;
			logger.popContext();
		}
	}

}