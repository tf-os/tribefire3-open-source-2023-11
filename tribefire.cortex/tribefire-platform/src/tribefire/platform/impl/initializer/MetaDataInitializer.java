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
package tribefire.platform.impl.initializer;

import java.util.function.BiConsumer;

import com.braintribe.cfg.Required;
import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.query.tools.PreparedQueries;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * @author peter.gazdik
 */
public class MetaDataInitializer extends SimplePersistenceInitializer {

	private String modelName;
	private BiConsumer<ModelMetaDataEditor, ManagedGmSession> metaDataConfigurer;

	private final String stageName;

	public MetaDataInitializer() {
		this(null);
	}

	public MetaDataInitializer(String stageName) {
		this.stageName = stageName;
	}

	public void setModelAr(ArtifactReflection modelAr) {
		setModelName(modelAr.name());
	}

	@Required
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	@Override
	protected String stageName() {
		return stageName != null ? stageName : "MetaData-of-" + modelName;
	}
	
	public void setMetaDataConfigurer(BiConsumer<ModelMetaDataEditor, ManagedGmSession> metaDataConfigurer) {
		this.metaDataConfigurer = metaDataConfigurer;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		GmMetaModel gmModel = queryModel(context);

		ManagedGmSession session = context.getSession();
		ModelMetaDataEditor editor = BasicModelMetaDataEditor.create(gmModel).withSession(session).done();
		metaDataConfigurer.accept(editor, session);
	}

	private GmMetaModel queryModel(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		GmMetaModel result = context.getSession().query().select(PreparedQueries.modelByName(modelName)).unique();
		if (result == null)
			throw new ManipulationPersistenceException("Model not found in the context session: " + modelName);

		return result;
	}

}
