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
package com.braintribe.model.processing.deployment;

import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Required;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.ConfigurableDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.meta.AbstractModelArtifactBuilder;
import com.braintribe.model.processing.meta.AbstractModelArtifactBuilder.ModelArtifact;
import com.braintribe.model.processing.meta.FsBasedModelArtifactBuilder;
import com.braintribe.model.processing.meta.ZippingModelArtifactBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.service.cortex.publish.PublishModel;
import com.braintribe.model.service.cortex.publish.PublishModelRequest;
import com.braintribe.model.service.cortex.publish.PublishModels;
import com.braintribe.model.typescript.ModelEnsuringContext;
import com.braintribe.model.typescript.ModelEnsuringDTsWriter;
import com.braintribe.model.typescript.ModelEnsuringJsWriter;
import com.braintribe.model.typescript.TypeScriptWriterForModels;
import com.braintribe.model.typescript.TypeScriptWriterHelper;
import com.braintribe.utils.ZipTools;
import com.braintribe.utils.io.WriterBuilder;
import com.braintribe.utils.io.ZipEntryWriter;

public class PublishModelProcessor extends ConfigurableDispatchingAccessRequestProcessor<PublishModelRequest, Resource> {

	private ResourceBuilder resourceBuilder;
	private Supplier<String> userNameSupplier;

	public PublishModelProcessor() {
		super(PublishModelProcessor.class.getSimpleName());
	}

	@Required
	public void setUserNameSupplier(Supplier<String> userNameSupplier) {
		this.userNameSupplier = userNameSupplier;
	}

	@Required
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(PublishModel.T, this::publishModel);
		dispatching.register(PublishModels.T, this::publishModels);
	}

	private Resource publishModel(AccessRequestContext<PublishModel> context) {
		return publish(asList(context.getSystemRequest().getModel()));
	}

	private Resource publishModels(AccessRequestContext<PublishModels> context) {
		return publish(context.getSystemRequest().getModels());
	}

	public Resource publish(List<GmMetaModel> models) {
		models = prefetch(models);

		return new ModelPublishing(models).run();
	}

	private class ModelPublishing {

		private final List<GmMetaModel> models;

		private GmMetaModel model;
		private ModelArtifact modelArtifact;
		private String modelPathPrefix;

		public ModelPublishing(List<GmMetaModel> models) {
			this.models = models;
		}

		public Resource run() {
			return resourceBuilder.newResource() //
					.withName(resourceName()) //
					.withCreator(userNameSupplier.get()) //
					.usingOutputStream(os -> writeZip(os));
		}

		private String resourceName() {
			if (models.size() > 1)
				return "models.zip";

			GmMetaModel model = first(models);
			return model.getName() + "-" + model.getVersion() + ".zip";
		}

		private void writeZip(OutputStream os) throws IOException {
			ZipTools.writeZipTo(os, zew -> writeZipEntries(zew));
		}

		private void writeZipEntries(ZipEntryWriter zew) {
			for (GmMetaModel model : models) {
				setCurrentModel(model);
				writeCurrentModel(zew);
			}
		}

		private void setCurrentModel(GmMetaModel model) {
			this.model = model;
			this.modelArtifact = AbstractModelArtifactBuilder.modelArtifactFor(model);
			this.modelPathPrefix = modelPathPrefix(modelArtifact);
		}

		private String modelPathPrefix(ModelArtifact ma) {
			return ma.groupId + "/" + ma.artifactId + "/" + ma.version + "/";
		}

		private void writeCurrentModel(ZipEntryWriter zew) {
			// build parts
			ZippingModelArtifactBuilder builder = new ZippingModelArtifactBuilder();
			builder.setModel(model);
			builder.setUser(userNameSupplier.get());
			builder.setFilePrefix(modelPathPrefix);
			builder.setZos(zew.getZipOutputStream());
			builder.publish();

			// create TypeScript declaration zip
			// code is tricky as we are writing a zip inside a zip
			String jsZipPartName = modelArtifact.partFileName(".js.zip");
			zew.writeZipEntry(modelPathPrefix + jsZipPartName, wb -> writeJsZip(model, wb));

			// create asset.man part
			String assetPartName = modelArtifact.partFileName("-asset.man");
			zew.writeZipEntry(modelPathPrefix + assetPartName, wb -> wb.string("$nature = !com.braintribe.model.asset.natures.ModelPriming()"));
		}

		private void writeJsZip(GmMetaModel model, WriterBuilder<?> wb) {
			wb.usingOutputStream(os -> ZipTools.writeZipTo(os, zew -> writeTypeScriptZip(zew, model)));
		}

		private void writeTypeScriptZip(ZipEntryWriter zew, GmMetaModel model) {
			String tsFileName = TypeScriptWriterHelper.dtsFileName(modelArtifact.artifactId);
			zew.writeZipEntry(tsFileName, wb -> writeTypeScriptDeclarationsForModel(wb, model));

			ModelEnsuringContext meCtx = ModelEnsuringContext.create(model, FsBasedModelArtifactBuilder::rangifyModelVersion);
			zew.writeZipEntry(meCtx.dtsFileName(), wb -> wb.usingWriter(writer -> ModelEnsuringDTsWriter.writeDts(meCtx, writer)));
			zew.writeZipEntry(meCtx.jsFileName(), wb -> wb.usingWriter(writer -> ModelEnsuringJsWriter.writeJs(meCtx, writer)));
		}

		private void writeTypeScriptDeclarationsForModel(WriterBuilder<?> wb, GmMetaModel model) {
			ClassLoader myCl = getClass().getClassLoader();
			Function<Class<?>, String> jsNameResolver = TypeScriptWriterHelper.jsNameResolver(myCl);

			wb.usingWriter(w -> TypeScriptWriterForModels.write(model, FsBasedModelArtifactBuilder::rangifyModelVersion, jsNameResolver, w));
		}

	}

	private List<GmMetaModel> prefetch(List<GmMetaModel> models) {
		if (models.isEmpty())
			return models;

		GmSession session = first(models).session();
		if (!(session instanceof PersistenceGmSession))
			return models;

		Set<Object> ids = models.stream() //
				.map(m -> m.getId()) //
				.collect(Collectors.toSet());

		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.T) //
				.where().property(GmMetaModel.id).in(ids) //
				.tc(modelTc()) //
				.done();

		((PersistenceGmSession) session).query().entities(query).list();

		return models;
	}

	private TraversingCriterion modelTc() {
		// Cutting-off every complex property of any HasExternalId entity instance in the assembly (e.g.: Deployables)
		// @formatter:off
		return TC.create()
				.pattern()
					.typeCondition(TypeConditions.isAssignableTo(HasExternalId.T))
					.conjunction()
						.property()
						.typeCondition(orTc(
							TypeConditions.isKind(TypeKind.collectionType), 
							TypeConditions.isKind(TypeKind.entityType)
						))
					.close()
				.close()
			.done();
		// @formatter:on
	}

}
