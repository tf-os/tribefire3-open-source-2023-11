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
package com.braintribe.model.processing.meta.cmd;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.ResolutionContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextImpl;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.ModelMdIndex;
import com.braintribe.model.processing.meta.cmd.resolvers.ModelMdAggregator;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * Standard implementation of {@link CmdResolver}.
 */
public class CmdResolverImpl implements CmdResolver {

	protected ModelMdAggregator modelMdAggregator;
	private final ResolutionContext resolutionContext;
	private final ResolutionContextInfo contextInfo;

	public static CmdResolverBuilder create(ModelOracle modelOracle) {
		return new ResolutionContextBuilder(modelOracle);
	}

	public CmdResolverImpl(ModelOracle modelOracle) {
		this(new ResolutionContextBuilder(modelOracle).build());
	}

	public CmdResolverImpl(ResolutionContextInfo rci) {
		this.contextInfo = rci;
		this.resolutionContext = new ResolutionContext(rci);
		this.modelMdAggregator = newMmdAggregator(resolutionContext);
		setDefaultValues();
	}

	private void setDefaultValues() {
		setSuppressInconsistencies(false);
	}

	/**
	 * Determines what to do in case the meta data configuration is not consistent with the model (e.g. there is a selector for a non-existent
	 * property, or expects the property to have different type). If set to {@code true} the problem is logged and the selector is evaluated as false,
	 * otherwise an exception is thrown.
	 */
	public void setSuppressInconsistencies(boolean shouldSuppress) {
		resolutionContext.setSuppressInconsistencies(shouldSuppress);
	}

	/**
	 * If set to {@code true} the resolver handles session scope as if it was static (since there is only one session). Typical usage is the client
	 * side (GWT).
	 */
	public void setSingleSession(boolean singleSession) {
		resolutionContext.setSigleSession(singleSession);
	}

	public void setDefaultMetaData(Set<? extends MetaData> defaultValues) {
		resolutionContext.setDefaultMetaData(defaultValues);
	}

	@Override
	public ModelOracle getModelOracle() {
		return resolutionContext.modelOracle;
	}

	@Override
	public MdSelectorResolver getMdSelectorResolver() {
		return resolutionContext.mdSelectorResolver;
	}

	/**
	 * Method that starts new resolution process for this resolver. This method returns a {@link ModelMdResolverImpl}, which is a first of a cascade
	 * of builders that together provide a convenient fluent interface to specify the context of resolution (i.e. where to look for meta data
	 * (entity/property..), what type (visibility, mandatory) and any other {@link SelectorContextAspect}s needed for resolution).
	 * <p>
	 * One may then find the desired meta data with a call like this:
	 * {@code getMetaData().entity(person).property("name").meta(PropertyVisibility.T).exclusive()}.
	 * 
	 * @return new instance of {@linkplain ModelMdResolverImpl}.
	 */
	@Override
	public ModelMdResolver getMetaData() {
		return new ModelMdResolverImpl(modelMdAggregator, newContext());
	}

	/** Resolves the type of {@link GenericEntity#id id} property based on {@link TypeSpecification} meta data. */
	@Override
	public <T extends ScalarType> T getIdType(String typeSignature) {
		TypeSpecification ts = getMetaData().entityTypeSignature(typeSignature).property(GenericEntity.id).meta(TypeSpecification.T).exclusive();

		return ts != null ? (T) ts.getType().reflectionType() : (T) SimpleTypes.TYPE_LONG;
	}

	private ModelMdAggregator newMmdAggregator(ResolutionContext resolutionContext) {
		ModelMdIndex modelMdIndex = MetaDataIndexStructure.newModelMdIndex(resolutionContext);
		return new ModelMdAggregator(modelMdIndex, resolutionContext);
	}

	protected MutableSelectorContext newContext() {
		SelectorContextImpl context = new SelectorContextImpl(resolutionContext.modelOracle, resolutionContext.getSessionProvider());

		context.putAll(contextInfo.getStaticAspects());
		context.setDynamicAspectValueProviders(contextInfo.getDynamicAspectValueProviders());

		return context;
	}

}
