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
package com.braintribe.model.processing.meta.cmd.resolvers;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.extended.ConstantMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.PropertyMdDescriptor;

/**
 * @author peter.gazdik
 */
public class MetaDataWrapper {

	static MdDescriptor forDefault(EntityType<? extends MdDescriptor> descriptorType, MetaData md) {
		MdDescriptor result = descriptorType.create();
		result.setResolvedValue(md);
		result.setResolvedAsDefault(true);

		return result;
	}

	static PropertyMdDescriptor forProperty(MetaData md, GmEntityType resolvedForType, GmMetaModel metaModel, GmEntityTypeInfo ownerEntityTypeInfo,
			GmPropertyInfo ownerPropertyInfo) {

		PropertyMdDescriptor result = forEntityRelated(PropertyMdDescriptor.T, md, resolvedForType, metaModel, ownerEntityTypeInfo);
		result.setOwnerPropertyInfo(ownerPropertyInfo);

		return result;
	}

	static EntityMdDescriptor forEntityType(MetaData md, GmEntityType resolvedForType, GmMetaModel metaModel, GmEntityTypeInfo ownerTypeInfo) {
		return forEntityRelated(EntityMdDescriptor.T, md, resolvedForType, metaModel, ownerTypeInfo);
	}

	private static <D extends EntityRelatedMdDescriptor> D forEntityRelated(EntityType<D> descriptorType, MetaData md, GmEntityType resolvedForType,
			GmMetaModel metaModel, GmEntityTypeInfo ownerTypeInfo) {

		D result = newInstance(descriptorType, md, metaModel);
		result.setResolvedForType(resolvedForType);
		result.setOwnerTypeInfo(ownerTypeInfo);
		result.setInherited(md.getInherited());

		return result;
	}

	static ConstantMdDescriptor forConstant(MetaData md, GmMetaModel ownerModel, GmEnumTypeInfo ownerTypeInfo, GmEnumConstantInfo ownerConstantInfo) {
		ConstantMdDescriptor result = forEnumRelated(ConstantMdDescriptor.T, md, ownerModel, ownerTypeInfo);
		result.setOwnerConstantInfo(ownerConstantInfo);
		return result;
	}

	static EnumMdDescriptor forEnumType(MetaData md, GmMetaModel ownerModel, GmEnumTypeInfo ownerTypeInfo) {
		return forEnumRelated(EnumMdDescriptor.T, md, ownerModel, ownerTypeInfo);
	}

	private static <D extends EnumRelatedMdDescriptor> D forEnumRelated(EntityType<D> descriptorType, MetaData md, GmMetaModel ownerModel,
			GmEnumTypeInfo ownerTypeInfo) {
		D result = newInstance(descriptorType, md, ownerModel);
		result.setOwnerTypeInfo(ownerTypeInfo);
		result.setInherited(md.getInherited());

		return result;
	}

	static ModelMdDescriptor forModel(MetaData md, GmMetaModel ownerModel) {
		return newInstance(ModelMdDescriptor.T, md, ownerModel);
	}

	private static <D extends MdDescriptor> D newInstance(EntityType<D> descriptorType, MetaData md, GmMetaModel ownerModel) {
		D result = descriptorType.create();
		result.setResolvedValue(md);
		result.setResolvedAsDefault(false);
		result.setOwnerModel(ownerModel);

		return result;
	}

}
