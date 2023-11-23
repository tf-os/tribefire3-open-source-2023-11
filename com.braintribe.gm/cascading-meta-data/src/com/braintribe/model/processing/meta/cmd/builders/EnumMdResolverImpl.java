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
package com.braintribe.model.processing.meta.cmd.builders;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.empty.EmptyConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.resolvers.ConstantMdAggregator;
import com.braintribe.model.processing.meta.cmd.resolvers.EnumMdAggregator;
import com.braintribe.model.processing.meta.cmd.result.EnumMdResult;

/**
 * 
 */
public class EnumMdResolverImpl extends MdResolverImpl<EnumMdResolver> implements EnumMdResolver {

	protected EnumMdAggregator enumMdAggregator;

	protected EnumMdResolverImpl(EnumMdAggregator enumMdAggregator, MutableSelectorContext selectorContext) {
		super(EnumMdResolverImpl.class, selectorContext, enumMdAggregator);

		this.enumMdAggregator = enumMdAggregator;
	}

	@Override
	public ConstantMdResolver constant(Enum<?> constant) {
		return constant(constant.name());
	}

	@Override
	public ConstantMdResolver constant(GmEnumConstant constant) {
		return constant(constant.getName());
	}

	@Override
	public ConstantMdResolver constant(String constant) {
		if (enumMdAggregator.getEnumOracle().findConstant(constant) == null) {
			return lenientOrThrowException(() -> EmptyConstantMdResolver.INSTANCE, () -> "Constant not found: " + constant);
		}

		ConstantMdAggregator constantMdAggregator = enumMdAggregator.acquireConstantMdAggregator(constant);
		constantMdAggregator.addLocalContextTo(selectorContext);

		return getEnumConstantMetaDataContextBuilder(constantMdAggregator);
	}

	@Override
	public GmEnumType getEnumType() {
		return enumMdAggregator.getGmEnumType();
	}

	protected ConstantMdResolver getEnumConstantMetaDataContextBuilder(ConstantMdAggregator constantMdAggregator) {
		return new ConstantMdResolverImpl(constantMdAggregator, selectorContext);
	}

	@Override
	public EnumMdResolver fork() {
		return new EnumMdResolverImpl(enumMdAggregator, selectorContext.copy());
	}

	@Override
	public <M extends MetaData> EnumMdResult<M> meta(EntityType<M> metaDataType) {
		return new MdResultImpl.EnumMdResultImpl<>(metaDataType, enumMdAggregator, selectorContext);
	}

}
