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
package tribefire.module.wire.contract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.editor.MetaDataEditorBuilder;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.ConfigurationModelFactory;

/**
 * @author peter.gazdik
 */
public interface ModelApiContract extends WireSpace {

	/**
	 * Returns a {@link CmdResolverBuilder} for given model which is pre-configured by the platform.
	 * <p>
	 * For example custom {@link SelectorExpert}s bound via {@link HardwiredExpertsContract#bindMetaDataSelectorExpert(EntityType, SelectorExpert)}
	 * are passed to the returned builder.
	 */
	CmdResolverBuilder newCmdResolver(GmMetaModel model);

	CmdResolverBuilder newCmdResolver(ModelOracle modelOracle);

	/** Returns a {@link ModelOracle} for given {@link GmMetaModel model}. */
	ModelOracle newModelOracle(GmMetaModel model);

	/**
	 * Returns a {@link MetaDataEditorBuilder}. If given model is attached to a session, the builder's {@link MetaDataEditorBuilder#withSession} is
	 * called with that session (thus all the created overrides are created with the same session).
	 */
	MetaDataEditorBuilder newMetaDataEditor(GmMetaModel model);

	/**
	 * Returns a {@link ConfigurationModelFactory} which can be used to create new (configuration) models and configure it's dependencies using given
	 * {@link ManagedGmSession session}.
	 */
	ConfigurationModelFactory newConfigurationModelFactory(ManagedGmSession session);

}
