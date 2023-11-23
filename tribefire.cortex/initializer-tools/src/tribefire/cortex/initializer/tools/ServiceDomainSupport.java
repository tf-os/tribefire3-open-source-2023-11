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
package tribefire.cortex.initializer.tools;

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.NullSafe.nonNull;
import static java.util.Objects.requireNonNull;
import static tribefire.cortex.initializer.tools.CommonModelsResolver.getModelOf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;

/**
 * Configurable {@link DataInitializer} that adds a one model as a dependency of another model.
 * 
 * @author peter.gazdik
 */
public class ServiceDomainSupport {

	/** @see SdCtx#SdCtx(String, String) */
	public static SdCtx domainInitializer(String callerIdentifier, String domainId) {
		return new SdCtx(callerIdentifier, domainId);
	}

	public static SmCtx serviceModelInitializer(String callerIdentifier, String modelName) {
		return new SmCtx(callerIdentifier, modelName, Model.modelGlobalId(modelName));
	}

	private static abstract class ServiceConfigurationCtx<ME extends ServiceConfigurationCtx<ME>> {

		protected final String callerIdentifier;
		protected final Set<Model> models = newLinkedSet();
		protected final Set<String> modelNames = newLinkedSet();
		protected final Map<Model, Set<EntityType<? extends ServiceRequest>>> modelToServiceRequests = newMap();
		protected final Map<EntityType<? extends ServiceRequest>, String> serviceProcessorsById = newMap();
		protected final Map<EntityType<? extends ServiceRequest>, ServiceProcessor> serviceProcessors = newMap();
		protected final List<BiConsumer<ModelMetaDataEditor, ManagedGmSession>> mdConfigurers = newList();

		/**
		 * @param callerIdentifier
		 *            identifier of the component that is doing the initialization. This is only used for error messages.
		 */
		public ServiceConfigurationCtx(String callerIdentifier) {
			this.callerIdentifier = requireNonNull(callerIdentifier);
		}

		public ME withServiceProcessor(EntityType<? extends ServiceRequest> requestType, ServiceProcessor serviceProcessor) {
			coverRequestType(requestType);
			serviceProcessors.put(requestType, serviceProcessor);
			return (ME) this;
		}

		public ME withServiceProcessor(EntityType<? extends ServiceRequest> requestType, String serviceProcessorGlobalId) {
			coverRequestType(requestType);
			serviceProcessorsById.put(requestType, serviceProcessorGlobalId);
			return (ME) this;
		}

		private void coverRequestType(EntityType<? extends ServiceRequest> requestType) {
			Model model = getModelOf(requestType);
			models.add(model);
			acquireSet(modelToServiceRequests, model).add(requestType);
		}

		public ME withServiceProcessors(Map<EntityType<? extends ServiceRequest>, ? extends ServiceProcessor> serviceProcessors) {
			this.serviceProcessors.putAll(serviceProcessors);
			return (ME) this;
		}

		public ME withMdConfigurer(BiConsumer<ModelMetaDataEditor, ManagedGmSession> mdConfigurer) {
			mdConfigurers.add(mdConfigurer);
			return (ME) this;
		}

		public ME withModelByName(String modelName) {
			Model model = GMF.getTypeReflection().findModel(modelName);
			if (model != null)
				withModels(model);
			else
				modelNames.add(modelName);
			return (ME) this;
		}

		public ME withModels(Model... models) {
			return withModels(asList(models));
		}

		public ME withModels(Collection<Model> models) {
			this.models.addAll(models);
			return (ME) this;
		}

	}

	public static class SdCtx extends ServiceConfigurationCtx<SdCtx> {
		protected final String domainId;

		public SdCtx(String callerIdentifier, String domainId) {
			super(callerIdentifier);
			this.domainId = requireNonNull(domainId);
		}

		public DataInitializer forExistingDomain() {
			return forExistingDomain(null);
		}

		public DataInitializer forExistingDomain(String domainModelGlobalId) {
			return new ServiceDomainExtendingInitializer(this, domainModelGlobalId);
		}

		public DataInitializer forNewDomain(String domainName) {
			return forMaybeNewDomain(domainName, false);
		}

		public DataInitializer forMaybeNewDomain(String domainName) {
			return forMaybeNewDomain(domainName, true);
		}

		public DataInitializer forMaybeNewDomain(String domainName, boolean canExist) {
			return new ServiceDomainCreatingInitializer(this, domainName, canExist);
		}
	}

	public static class SmCtx extends ServiceConfigurationCtx<SmCtx> {
		private final String modelName;
		private final String modelGlobalId;

		public SmCtx(String callerIdentifier, String modelName, String modelGlobalId) {
			super(callerIdentifier);
			this.modelName = nonNull(modelName, "modelName");
			this.modelGlobalId = nonNull(modelGlobalId, "modelGlobalId");
		}

		public DataInitializer forModel(Boolean exists) {
			return new ServiceModelConfiguringInitializer(this, exists);
		}
	}

	// ###########################################################
	// ## . . . . . . . . . . Abstract Base . . . . . . . . . . ##
	// ###########################################################

	static abstract class ServiceConfiguringInitializer<CTX extends ServiceConfigurationCtx<CTX>> implements DataInitializer {

		protected final CTX ctx;

		protected ManagedGmSession session;
		protected GmMetaModel domainModel;

		public ServiceConfiguringInitializer(CTX ctx) {
			this.ctx = ctx;
		}

		protected void extendModel() {
			addModelDeps();

			ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(domainModel).withSession(session).done();

			addProcessWithMd(mdEditor);
			runCustomMdConfigurers(mdEditor);
		}

		private void addModelDeps() {
			ModelInitializingTools.extendModelToCoverModels(domainModel, ctx.models, ctx.modelNames, this::resolveGmMetaModel,
					this::resolveGmModelByName);
		}

		private GmMetaModel resolveGmMetaModel(Model m) {
			GmMetaModel model = session.findEntityByGlobalId(m.globalId());
			if (model != null)
				return model;

			throw modelNotInCortexException(m);
		}

		private IllegalStateException modelNotInCortexException(Model m) {
			StringBuilder msg = modelNotInCortexErrorMsg("globalId [" + m.globalId() + "]");

			Set<EntityType<? extends ServiceRequest>> requests = ctx.modelToServiceRequests.get(m);
			if (!isEmpty(requests)) {
				String requestsInfo = requests.stream() //
						.map(EntityType::getTypeSignature) //
						.collect(Collectors.joining(", ", " This model is required for the following request types: ", ""));

				msg.append(requestsInfo);
			}

			return new IllegalStateException(msg.toString());
		}

		private GmMetaModel resolveGmModelByName(String name) {
			GmMetaModel model = session.query().entities(EntityQueryBuilder.from(GmMetaModel.T).where().property("name").eq(name).done()).unique();
			if (model != null)
				return model;

			throw new IllegalStateException(modelNotInCortexErrorMsg("name [" + name + "]").toString());
		}

		private StringBuilder modelNotInCortexErrorMsg(String modelIdentification) {
			return new StringBuilder() //
					.append("No model found in cortex for ") //
					.append(modelIdentification) //
					.append(". It was expected there, as part of ") //
					.append(configuredThingie()) //
					.append(". Possible reason is that the model (or it's depender) wasn't depended as an Asset (using <?tag asset> in pom.xml).") //
					.append(" This Service Domain is configured by [") //
					.append(ctx.callerIdentifier) //
					.append("], so check the dependencies starting there.");
		}

		private void addProcessWithMd(ModelMetaDataEditor mdEditor) {
			for (Entry<EntityType<? extends ServiceRequest>, String> e : ctx.serviceProcessorsById.entrySet())
				mdEditor.onEntityType(e.getKey()) //
						.addMetaData(processWith(session.getEntityByGlobalId(e.getValue())));

			for (Entry<EntityType<? extends ServiceRequest>, ? extends ServiceProcessor> e : ctx.serviceProcessors.entrySet())
				mdEditor.onEntityType(e.getKey()) //
						.addMetaData(processWith(e.getValue()));
		}

		private MetaData processWith(ServiceProcessor sp) {
			ProcessWith result = session.create(ProcessWith.T, "processWith:" + globalIdThingiePart() + ":" + sp.getExternalId());
			result.setProcessor(sp);

			return result;
		}

		private void runCustomMdConfigurers(ModelMetaDataEditor mdEditor) {
			for (BiConsumer<ModelMetaDataEditor, ManagedGmSession> mdConfigurer : ctx.mdConfigurers)
				mdConfigurer.accept(mdEditor, session);
		}

		protected abstract String configuredThingie();
		protected abstract String globalIdThingiePart();

	}

	static abstract class ServiceDomainInitializer extends ServiceConfiguringInitializer<SdCtx> {

		protected ServiceDomainInitializer(SdCtx ctx) {
			super(ctx);
		}

		@Override
		protected String configuredThingie() {
			return "Service Domain [" + ctx.domainId + "]";
		}

		@Override
		protected String globalIdThingiePart() {
			return ctx.domainId;
		}

		protected ServiceDomain queryServiceDomain(boolean required) {
			ServiceDomain sd = session.query().select(domainByExternalId()).unique();
			if (sd == null && required)
				throw new IllegalArgumentException(
						"Wrong configuration in " + ctx.callerIdentifier + ". No service domain found with external id: " + ctx.domainId);

			return sd;
		}

		private SelectQuery domainByExternalId() {
			return new SelectQueryBuilder().from(ServiceDomain.T, "d").where().property(HasExternalId.externalId).eq(ctx.domainId).done();
		}

	}

	// ###########################################################
	// ## . . . . . . . . Extend Service Domain . . . . . . . . ##
	// ###########################################################

	static class ServiceDomainExtendingInitializer extends ServiceDomainInitializer {
		private final String domainModelGlobalId;

		public ServiceDomainExtendingInitializer(SdCtx sdInit, String domainModelGlobalId) {
			super(sdInit);

			this.domainModelGlobalId = domainModelGlobalId;
		}

		@Override
		public void initialize(PersistenceInitializationContext context) {
			this.session = context.getSession();
			this.domainModel = lookupModel();

			extendModel();
		}

		private GmMetaModel lookupModel() {
			if (domainModelGlobalId != null)
				return session.findEntityByGlobalId(domainModelGlobalId);
			else
				return queryServiceDomain(true).getServiceModel();
		}

	}

	// ###########################################################
	// ## . . . . . . . . New Service Domain . . . . . . . . . .##
	// ###########################################################

	static class ServiceDomainCreatingInitializer extends ServiceDomainInitializer {

		private final String domainName;
		private final boolean canExist;

		public ServiceDomainCreatingInitializer(SdCtx sdInit, String domainName, boolean canExist) {
			super(sdInit);
			this.domainName = domainName;
			this.canExist = canExist;
		}

		@Override
		public void initialize(PersistenceInitializationContext context) {
			this.session = context.getSession();
			this.domainModel = acquireServiceModel();

			extendModel();

			ServiceDomain domain = session.create(ServiceDomain.T, "hardwired:" + ctx.domainId);
			domain.setExternalId(ctx.domainId);
			domain.setName(domainName);
			domain.setServiceModel(domainModel);
		}

		private GmMetaModel acquireServiceModel() {
			ServiceDomain existingDomain = queryServiceDomain(false);
			if (existingDomain == null)
				return newServiceModel();

			if (canExist)
				return existingDomain.getServiceModel();

			throw new IllegalArgumentException(
					"Wrong configuration in " + ctx.callerIdentifier + ". ServiceDomain exists, which was not expected: " + ctx.domainId);
		}

		private GmMetaModel newServiceModel() {
			String modelName = "serviceDomain.model.synthetic:" + ctx.domainId;

			GmMetaModel result = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
			result.setName(modelName);

			return result;
		}
	}

	// ###########################################################
	// ## . . . . . . . Maybe New Service Model . . . . . . . . ##
	// ###########################################################

	static class ServiceModelConfiguringInitializer extends ServiceConfiguringInitializer<SmCtx> {

		private final Boolean exists;

		public ServiceModelConfiguringInitializer(SmCtx ctx, Boolean exists) {
			super(ctx);
			this.exists = exists;
		}

		@Override
		protected String configuredThingie() {
			return "Model [" + ctx.modelName + "]";
		}

		@Override
		protected String globalIdThingiePart() {
			return ctx.modelName;
		}

		@Override
		public void initialize(PersistenceInitializationContext context) {
			this.session = context.getSession();
			this.domainModel = acquireServiceModel();

			extendModel();
		}

		private GmMetaModel acquireServiceModel() {
			GmMetaModel existingModel = session.findEntityByGlobalId(ctx.modelGlobalId);
			if (existingModel == null) {
				if (Boolean.TRUE.equals(exists))
					throw new IllegalArgumentException("Wrong configuration in module: " + ctx.callerIdentifier + ". Model with name ["
							+ ctx.modelName + "] was expected to exist but it does not. Maybe it never did, we don't know. But we need it.");

				return newServiceModel();
			}

			if (Boolean.FALSE.equals(exists))
				throw new IllegalArgumentException("Wrong configuration in module: " + ctx.callerIdentifier + ". Model with name [" + ctx.modelName
						+ "] exists, but that was not expected.");

			return existingModel;
		}

		private GmMetaModel newServiceModel() {
			GmMetaModel result = session.create(GmMetaModel.T, ctx.modelGlobalId);
			result.setName(ctx.modelName);

			return result;
		}

	}

}
