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
package com.braintribe.model.access.smood.collaboration.deployment;

import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.CsaStatePersistenceImpl;
import com.braintribe.model.access.collaboration.persistence.AbstractManipulationPersistence;
import com.braintribe.model.access.collaboration.persistence.BasicManipulationPersistence;
import com.braintribe.model.access.collaboration.persistence.CortexManipulationPersistence;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.dataio.FileBasedGmPathValueStore;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.StrictErrorHandler;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceAccessFactory;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeleteResourceResponse;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.testing.tools.gm.session.TestModelAccessory;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.FileStreamProviders;

/**
 * Base for a {@link CollaborativeSmoodAccess} builder.
 * 
 * NOTE that the builder can only be used to create a single instance.
 * 
 * @author peter.gazdik
 */
public abstract class AbstractCsaBuilder<//
		CSA extends CollaborativeSmoodAccess, //
		U extends AbstractCsaDeployedUnit<CSA>, //
		B extends AbstractCsaBuilder<CSA, U, B>> {

	private String accessId;

	protected CSA csa;
	protected U deployedUnit;

	private boolean skipPostConstruct;

	protected Supplier<CollaborativeSmoodConfiguration> configurationSupplier;
	protected CsaStatePersistence statePersistence;

	protected List<PersistenceInitializer> staticInitializers = emptyList();
	protected List<PersistenceInitializer> staticPostInitializers = emptyList();

	protected File baseFolder;
	protected File jsonFile;
	protected File resourcesBaseAbsoluteFile;
	protected Path resourcesBaseAbsolutePath;

	protected boolean cortex;
	protected boolean mergeModelAndData;

	protected GmMetaModel model;
	protected String selfModelName;
	protected ModelAccessory modelAccessory;

	protected CollaborativeSmoodConfiguration configuration;

	protected TestFileSystemBinaryProcessor binaryProcessor;

	protected GmmlManipulatorErrorHandler errorHandler = StrictErrorHandler.INSTANCE;

	protected final B self = (B) this;

	// #################################################
	// ## . . . . . . . Fluid methods . . . . . . . . ##
	// #################################################

	public B accessId(String accessId) {
		this.accessId = accessId;
		return self;
	}

	public B baseFolder(File baseFolder) {
		this.baseFolder = baseFolder;
		this.jsonFile = new File(baseFolder, "config.json");
		this.resourcesBaseAbsoluteFile = new File(baseFolder, AbstractCsaDeployedUnit.resourcesFolderName);
		this.resourcesBaseAbsolutePath = resourcesBaseAbsoluteFile.toPath();
		this.statePersistence = csaStatePersistence();

		FileTools.ensureFolderExists(resourcesBaseAbsoluteFile);

		return self;
	}

	public B configurationSupplier(Supplier<CollaborativeSmoodConfiguration> configurationSupplier) {
		this.configurationSupplier = configurationSupplier;
		return self;
	}

	public B staticInitializers(List<PersistenceInitializer> staticInitializers) {
		this.staticInitializers = staticInitializers;
		return self;
	}

	public B staticPostInitializers(List<PersistenceInitializer> staticPostInitializers) {
		this.staticPostInitializers = staticPostInitializers;
		return self;
	}

	public B model(GmMetaModel model) {
		this.model = model;
		return self;
	}

	public B selfModelName(String selfModelName) {
		this.selfModelName = selfModelName;
		return self;
	}

	public B modelAccessory(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;
		return self;
	}

	public B cortex(boolean cortex) {
		this.cortex = cortex;
		return self;
	}

	public B mergeModelAndData(boolean mergeModelAndData) {
		this.mergeModelAndData = mergeModelAndData;
		return self;
	}

	public B skipPostConstruct(boolean skipPostConstruct) {
		this.skipPostConstruct = skipPostConstruct;
		return self;
	}

	public B binaryProcessor(TestFileSystemBinaryProcessor binaryProcessor) {
		this.binaryProcessor = binaryProcessor;
		return self;
	}

	public B errorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return self;
	}

	// #################################################
	// ## . . . . . Final builder method . . . . . . .##
	// #################################################

	public U done() {
		if (deployedUnit != null)
			throw new IllegalStateException("Cannot build another instance with this builder as it supports only a single instance creation.");

		validate();

		if (modelAccessory == null)
			modelAccessory = TestModelAccessory.newModelAccessory(model);

		configuration = configuration();

		csa = newCsa();

		csa.setAccessId(accessId());
		csa.setReadWriteLock(new ReentrantReadWriteLock());
		csa.setManipulationPersistence(manipulationPersistence());
		csa.setMetaModel(model);
		csa.setSelfModelName(selfModelName);
		csa.setModelAccessory(modelAccessory);
		csa.setCollaborativeRequestProcessor(collaborativeAccessManager());

		deployedUnit = newUnit();
		deployedUnit.baseFolder = baseFolder;
		deployedUnit.jsonFile = jsonFile;
		deployedUnit.resourcesBaseAbsoluteFile = resourcesBaseAbsoluteFile;

		deployedUnit.statePersistence = statePersistence;
		deployedUnit.configurationSupplier = configurationSupplier;
		deployedUnit.configuration = configuration;

		deployedUnit.csa = csa;
		deployedUnit.sessionFactory = () -> newSessionFor();

		if (!skipPostConstruct)
			deployedUnit.postConstruct();

		return deployedUnit;
	}

	// #################################################
	// ## . . . . . Implementation providers . . . . .##
	// #################################################

	protected abstract CSA newCsa();

	protected abstract U newUnit();

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	protected String accessId() {
		return accessId != null ? accessId : baseFolder.getName();
	}

	private CsaStatePersistence csaStatePersistence() {
		CsaStatePersistenceImpl result = new CsaStatePersistenceImpl();
		result.setPathValueStore(fileBasedKeyValueStore());

		return result;
	}

	private FileBasedGmPathValueStore fileBasedKeyValueStore() {
		FileBasedGmPathValueStore bean = new FileBasedGmPathValueStore();
		bean.setRootDir(baseFolder);
		bean.setSerializationOptions(GmSerializationOptions.defaultOptions.derive().outputPrettiness(OutputPrettiness.high).build());
		bean.setMarshaller(new JsonStreamMarshaller());

		return bean;
	}

	protected CollaborativeSmoodConfiguration configuration() {
		if (jsonFile.exists())
			return statePersistence.readConfiguration();
		else
			return createJsonConfiguration();
	}

	private CollaborativeSmoodConfiguration createJsonConfiguration() {
		CollaborativeSmoodConfiguration configuration = configurationSupplier.get();
		statePersistence.overwriteOriginalConfiguration(configuration);

		return configuration;
	}

	protected CollaborativeAccessManager collaborativeAccessManager() {
		CollaborativeAccessManager bean = new CollaborativeAccessManager();
		bean.setAccess(csa);
		bean.setCsaStatePersistence(statePersistence);
		bean.setSourcePathResolver(resourcesBaseAbsolutePath::resolve);

		bean.postConstruct();

		return bean;
	}

	protected AbstractManipulationPersistence<?> manipulationPersistence() {
		AbstractManipulationPersistence<?> bean = cortex ? cortexManipulationPersistence() : new BasicManipulationPersistence();
		bean.setStaticInitializers(staticInitializers);
		bean.setStaticPostInitializers(staticPostInitializers);
		bean.setStorageBase(baseFolder);
		bean.setCsaStatePersistence(statePersistence);
		bean.setManipulationFilter(m -> true); // just to make
		bean.setGmmlErrorHandler(errorHandler);
		return bean;
	}

	private CortexManipulationPersistence cortexManipulationPersistence() {
		CortexManipulationPersistence bean = new CortexManipulationPersistence();
		bean.setMergeModelAndData(mergeModelAndData);

		return bean;
	}

	protected void validate() {
		checkConfigured(baseFolder, "baseFolder");
		checkConfigured(statePersistence, "statePersistence");

		if (!cortex)
			checkConfigured(model, "model");
		else if (model == null && isEmpty(selfModelName))
			throw new IllegalStateException("Model nor selfModelname is configured.");

		if (!jsonFile.exists())
			checkConfigured(configurationSupplier,
					"configurationSupplier. This is needed as the config.json file does not exist: " + jsonFile.getAbsolutePath());
	}

	protected void checkConfigured(Object o, String description) {
		if (o == null)
			throw new IllegalStateException("Value not configured: " + description);
	}

	// #################################################
	// #. . . . . . . . SessionFactory . . . . . . . .##
	// #################################################

	private PersistenceGmSession newSessionFor() {
		BasicResourceAccessFactory resourcesAccessFactory = new BasicResourceAccessFactory();
		resourcesAccessFactory.setShallowifyRequestResource(false);

		BasicPersistenceGmSession result = new BasicPersistenceGmSession(csa);
		result.setModelAccessory(modelAccessory);
		result.setResourcesAccessFactory(resourcesAccessFactory);
		result.setRequestEvaluator(new ResourceUploadRequestEvaluator(result));

		return result;
	}

	private class ResourceUploadRequestEvaluator implements Evaluator<ServiceRequest> {

		private final PersistenceGmSession session;

		public ResourceUploadRequestEvaluator(PersistenceGmSession session) {
			this.session = session;
		}

		@Override
		public <T> EvalContext<T> eval(ServiceRequest request) {
			return new EvalContext<T>() {
				@Override
				public T get() throws EvalException {
					try {
						if (request instanceof UploadResource)
							return (T) handleUploadResource((UploadResource) request);

						if (request instanceof DeleteResource)
							return (T) handleDeleteResource((DeleteResource) request);

						if (request instanceof GetResource)
							return (T) handleGetResource((GetResource) request);

						throw new UnsupportedOperationException("Unsupported evaluable '" + request + "'.");

					} catch (IOException e) {
						throw Exceptions.uncheckedAndContextualize(e, "Error while evaluating: " + request, GenericModelException::new);
					}
				}

				private UploadResourceResponse handleUploadResource(UploadResource request) throws IOException {
					Resource resource = request.getResource();
					File resourceFile = deployedUnit.resourceFile(resource.getName());

					try (InputStream in = resource.openStream()) {
						IOTools.inputToFile(in, resourceFile);
					}

					FileSystemSource source = session.create(FileSystemSource.T);
					source.setPath(resource.getName());
					// source.setPath(resourceFile.getAbsolutePath()); // Use this to test absolute path, if needed

					Resource resultResource = session.create(Resource.T);
					resultResource.setName(resource.getName());
					resultResource.setResourceSource(source);

					binaryProcessor.onStore(accessId, resultResource);

					UploadResourceResponse result = UploadResourceResponse.T.create();
					result.setResource(resultResource);

					return result;
				}

				private DeleteResourceResponse handleDeleteResource(DeleteResource request) throws IOException {
					Resource resource = session.findEntityByGlobalId(request.getResource().getGlobalId());

					File resourceFile = deployedUnit.resourceFile(resource.getName());
					if (!resourceFile.delete())
						throw new IOException("Unable to delete resource '" + resource.getName() + "'. File: " + resourceFile.getAbsolutePath());

					session.deleteEntity(resource);
					session.deleteEntity(resource.getResourceSource());

					binaryProcessor.onDelete(accessId, resource);

					return DeleteResourceResponse.T.create();
				}

				private GetBinaryResponse handleGetResource(GetResource request) {
					Resource resource = request.getResource();

					File resourceFile = deployedUnit.resourceFile(resource.getName());
					if (!resourceFile.exists())
						lazyLoad(resource);

					Resource callResource = Resource.createTransient(FileStreamProviders.from(resourceFile));

					GetBinaryResponse result = GetBinaryResponse.T.create();
					result.setResource(callResource);
					return result;
				}

				private void lazyLoad(Resource resource) {
					Resource persistentReource = session.getEntityByGlobalId(resource.getGlobalId());

					FileSystemSource source = (FileSystemSource) persistentReource.getResourceSource();
					if (!deployedUnit.csa.experimentalLazyLoad(source))
						throw new IllegalStateException("LazyLoading failed for resource: " + resource.getName());
				}

				@Override
				public void get(AsyncCallback<? super T> callback) {
					try {
						callback.onSuccess(get());
					} catch (Throwable e) {
						callback.onFailure(e);
					}
				}

				@Override
				public <V, A extends EvalContextAspect<? super V>> EvalContext<T> with(Class<A> aspect, V value) {
					return this;
				}

			};
		}

	}
}
