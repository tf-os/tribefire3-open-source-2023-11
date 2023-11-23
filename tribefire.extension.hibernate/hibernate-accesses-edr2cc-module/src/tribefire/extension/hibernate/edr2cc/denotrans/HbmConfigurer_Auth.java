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
package tribefire.extension.hibernate.edr2cc.denotrans;

import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.SqlSource;
import com.braintribe.model.resource.source.TemplateSource;
import com.braintribe.model.resource.source.UrlUploadSource;
import com.braintribe.model.resource.specification.ImageSpecification;
import com.braintribe.model.resource.specification.OcrSpecification;
import com.braintribe.model.resource.specification.PhysicalDimensionSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Identity;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;

/**
 * @author peter.gazdik
 */
class HbmConfigurer_Auth extends AbstractHbmConfigurer {

	public static void run(HibernateAccessEdr2ccEnricher enricher) {
		new HbmConfigurer_Auth(enricher).run();
	}

	private HbmConfigurer_Auth(HibernateAccessEdr2ccEnricher enricher) {
		super(enricher, "hbm:edr2cc/auth/");
	}

	private void run() {
		mdEditor.onEntityType(GenericEntity.T) //
				.addMetaData(enricher.unmappedEntity()) //
				.addPropertyMetaData(screamingCamelCaseConversion());

		// All these were Not present in original hardcoded mappings
		mdEditor.onEntityType(Role.T) //
				.addPropertyMetaData(Role.localizedName, enricher.unmappedProperty());
		mdEditor.onEntityType(ImageSpecification.T) //
				.addPropertyMetaData(ImageSpecification.format, enricher.unmappedProperty());

		// custom mappings according to hardcoded mappings

		mdEditor.onEntityType(LocalizedString.T) //
				.addMetaData(entityMapping("TF_ID_LOCALIZED_STRINGS", LocalizedString.T)) //
				.addPropertyMetaData(LocalizedString.localizedValues, localizedStringValues());

		mdEditor.onEntityType(Icon.T) //
				.addMetaData(entityMapping("TF_ID_ICONS", "ICON_TYPE", Icon.T)) // TODO would be handled with a bug-fix for discriminator
				.addPropertyMetaData(GenericEntity.id, propMapping("id", "Icon/id")) // for whatever reason this was lower-case
				.addPropertyMetaData(Icon.name, propMapping("ICON_NAME", "Icon/name")) //
		;

		mdEditor.onEntityType(AdaptiveIcon.T) //
				.addPropertyMetaData(AdaptiveIcon.representations, adaptiveIconRepresentations()); //

		mdEditor.onEntityType(SimpleIcon.T) //
				.addPropertyMetaData(SimpleIcon.image, propMapping("IMAGE_RES_ID", "SimpleIcon/image"));

		mdEditor.onEntityType(Resource.T) //
				.addMetaData(entityMapping("TF_ID_RES", Resource.T)) //
				.addPropertyMetaData(Resource.name, propMapping("RES_NAME", "Resource/name")) //
				.addPropertyMetaData(Resource.resourceSource, propMapping("RES_SRC_ID", "Resource/resourceSource")) //
				.addPropertyMetaData(Resource.specification, propMapping("RES_SPEC_ID", "Resource/specification")) //
				.addPropertyMetaData(Resource.tags, resourceTags()) //
		;

		mdEditor.onEntityType(ResourceSource.T) //
				.addMetaData(entityMapping("TF_ID_RES_SRC", "RES_SRC_TYPE", ResourceSource.T)) //
				.addPropertyMetaData(GenericEntity.id, propMapping("id", "ResourceSource/id")) // for whatever reason this was lower-case
		;

		mdEditor.onEntityType(FileSystemSource.T) //
				.addPropertyMetaData(FileSystemSource.path, propMapping("RES_PATH", "FileSystemSource/path"));

		mdEditor.onEntityType(SqlSource.T) //
				.addPropertyMetaData(SqlSource.blobId, propMapping("blobId", "SqlSource/blobId")); // for whatever reason this was lower-case

		mdEditor.onEntityType(TemplateSource.T) //
				.addPropertyMetaData(TemplateSource.template, propMapping("TEMPLATE_ID", "TemplateSource/template"));

		mdEditor.onEntityType(ResourceSpecification.T) //
				.addMetaData(entityMapping("TF_ID_RES_SPEC", "RES_SPEC_TYPE", ResourceSpecification.T)) //
				.addPropertyMetaData(GenericEntity.id, propMapping("id", "ResourceSpecification/id")) // for whatever reason this was lower-case
		;

		mdEditor.onEntityType(PhysicalDimensionSpecification.T) //
				.addPropertyMetaData(PhysicalDimensionSpecification.widthInCm,
						propMapping("WIDHT_IN_CM", "PhysicalDimensionSpecification/widthInCm")); // this was misspelled in original
		mdEditor.onEntityType(OcrSpecification.T) //
				.addPropertyMetaData(OcrSpecification.widthInCm, propMapping("WIDTH_IN_CM", "OcrSpecification/widthInCm")); // this was somehow fixed
																															// for this sub-type of

		mdEditor.onEntityType(Identity.T) //
				.addPropertyMetaData(Identity.description, propMappingCascadeAllFetchJoin("DESCRIPTION_ID", "Identity/description")) //
		;

		mdEditor.onEntityType(Group.T) //
				.addMetaData(entityMapping("TF_ID_GROUPS", Group.T)) //
				.addPropertyMetaData(Group.name, unique(propMapping("GROUP_NAME", "Group/name"))) //
				.addPropertyMetaData(Group.picture, propMapping("GROUP_PICTURE_ID", "Group/picture")) //
				.addPropertyMetaData(Group.roles, cascadeAll(collectionProperty("TF_ID_GROUP_ROLES", "Group/roles"))) //
				.addPropertyMetaData(Group.users, cascadeAll(collectionProperty("TF_ID_GROUP_USERS", "Group/users"))) //
				.addPropertyMetaData(Group.localizedName, propMappingCascadeAllFetchJoin("LOCALIZED_NAME_ID", "Group/localizedName")) //
		;

		mdEditor.onEntityType(Role.T) //
				.addMetaData(entityMapping("TF_ID_ROLES", Role.T)) //
				.addPropertyMetaData(Role.description, propMappingCascadeAllFetchJoin("DESCRIPTION_ID", "Role/description")) //
				.addPropertyMetaData(Role.name, unique(propMapping("ROLE_NAME", "Role/name"))) //
		;

		mdEditor.onEntityType(User.T) //
				.addMetaData(entityMapping("TF_ID_USERS", User.T)) //
				.addPropertyMetaData(User.groups, cascadeAll(collectionProperty("TF_ID_USER_GROUPS", "User/groups"))) //
				.addPropertyMetaData(User.name, unique(propMapping("USER_NAME", "User/name"))) //
				.addPropertyMetaData(User.password, propMapping("USER_PASSWORD", "User/password")) //
				.addPropertyMetaData(User.picture, propMapping("USER_PICTURE_ID", "User/picture")) //
				.addPropertyMetaData(User.roles, cascadeAll(collectionProperty("TF_ID_USER_ROLES", "User/roles"))) //
		;

		mdEditor.onEntityType(UrlUploadSource.T) //
				.addPropertyMetaData(UrlUploadSource.url, propMapping("url", "UrlUploadSource/url")) // for whatever reason this was lower-case
		;
	}

	private PropertyMapping adaptiveIconRepresentations() {
		PropertyMapping result = collectionProperty("TF_ID_ADAPT_ICONS", "AdaptiveIcon/representations");
		result.setCollectionKeyColumn("ADAPT_ICON_ID");
		result.setCollectionElementColumn("RES_ID");

		return result;
	}

	private PropertyMapping localizedStringValues() {
		PropertyMapping result = collectionProperty("TF_ID_LOCALIZED_STRING_VALUES", "LocalizedString/values");
		result.setCollectionKeyColumn("ID");
		result.setCollectionKeyColumnNotNull(true);
		result.setMapKeyColumn("LOCALE_NAME");
		result.setCollectionElementColumn("LOCALE_VALUE");
		return result;
	}

	private PropertyMapping resourceTags() {
		PropertyMapping result = collectionProperty("TF_ID_RES_TAGS", "Resource/tags");
		result.setCollectionKeyColumn("RES_ID");
		result.setCollectionElementColumn("RES_TAG");
		return result;
	}

	private PropertyMapping collectionProperty(String tableName, String gidSuffix) {
		PropertyMapping result = propMapping(gidSuffix);
		result.setCollectionTableName(tableName);

		return result;
	}

	private PropertyMapping unique(PropertyMapping result) {
		result.setUnique(true);
		return result;
	}

	private PropertyMapping cascadeAll(PropertyMapping pm) {
		pm.setCascade("all");
		return pm;
	}

	private PropertyMapping propMappingCascadeAllFetchJoin(String columnName, String gidSuffix) {
		PropertyMapping result = propMapping(columnName, gidSuffix);
		result.setCascade("all");
		result.setFetch("join");

		return result;
	}

}
