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
package com.braintribe.model.io.metamodel.render;

import com.braintribe.model.io.metamodel.MetaModelSourceDescriptor;
import com.braintribe.model.io.metamodel.render.context.EntityTypeContext;
import com.braintribe.model.io.metamodel.render.context.EntityTypeContextBuilder;
import com.braintribe.model.io.metamodel.render.context.EnumTypeContext;
import com.braintribe.model.io.metamodel.render.context.EnumTypeContextBuilder;
import com.braintribe.model.io.metamodel.render.context.TypeSignatureResolver;
import com.braintribe.model.io.metamodel.render.info.MetaModelInfo;
import com.braintribe.model.io.metamodel.render.info.TypeInfo;
import com.braintribe.model.io.metamodel.render.render.EntityRenderer;
import com.braintribe.model.io.metamodel.render.render.EnumRenderer;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;

/**
 * For given GmType (Entity/Enum) creates source code and information about the name of the file which should be created as source file (This
 * information is encapsulated in a Class MetaModelSourceDescriptor).
 */
public class MetaModelRenderer {

	private final SourceWriterContext context;
	private final MetaModelInfo metaModelInfo;
	private final TypeSignatureResolver typeSignatureResolver;

	public MetaModelRenderer(SourceWriterContext context) {
		this.context = context;
		this.metaModelInfo = new MetaModelInfo(context.modelOracle.getGmMetaModel());
		this.typeSignatureResolver = new TypeSignatureResolver();
	}

	public MetaModelSourceDescriptor renderEntityType(GmEntityType gmEntityType) {
		if (!shouldRenderType(gmEntityType))
			return null;

		MetaModelSourceDescriptor result = createNewDescriptorForType(gmEntityType);

		EntityTypeContext context = provideEntityTypeContext(gmEntityType, metaModelInfo);
		result.sourceCode = new EntityRenderer(context).render();

		return result;
	}

	private EntityTypeContext provideEntityTypeContext(GmEntityType gmEntityType, MetaModelInfo metaModelInfo) {
		return new EntityTypeContextBuilder(context, typeSignatureResolver, gmEntityType, metaModelInfo).build();
	}

	public MetaModelSourceDescriptor renderEnumType(GmEnumType gmEnumType) {
		MetaModelSourceDescriptor result = createNewDescriptorForType(gmEnumType);

		EnumTypeContext context = provideEnumTypeContext(gmEnumType, metaModelInfo);
		result.sourceCode = new EnumRenderer(context).render();

		return result;
	}

	private EnumTypeContext provideEnumTypeContext(GmEnumType gmEnumType, MetaModelInfo metaModelInfo) {
		return new EnumTypeContextBuilder(gmEnumType, metaModelInfo).build();
	}

	private boolean shouldRenderType(GmType gmType) {
		return context.shouldWriteSourcesForExistingClasses || !existsClass(gmType.getTypeSignature());
	}

	private boolean existsClass(String className) {
		try {
			Class.forName(className);
			return true;

		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/** Should only be called with GmEnumType or GmEntityType, which are retrievable through metaModelInfo.getInfoForType(); */
	private MetaModelSourceDescriptor createNewDescriptorForType(GmType gmType) {
		MetaModelSourceDescriptor result = new MetaModelSourceDescriptor();

		TypeInfo entityInfo = metaModelInfo.getInfoForType(gmType);
		result.sourceRelativePath = getSourceRelativePath(entityInfo);

		return result;
	}

	private String getSourceRelativePath(TypeInfo entityInfo) {
		String pathToPackageFolder = entityInfo.packageName.replaceAll("\\.", "/");

		return pathToPackageFolder + "/" + entityInfo.simpleName + ".java";
	}

}
