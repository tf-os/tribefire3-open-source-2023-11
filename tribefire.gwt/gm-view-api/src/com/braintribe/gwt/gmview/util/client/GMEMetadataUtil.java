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
package com.braintribe.gwt.gmview.util.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.utils.client.FastSet;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.PropertyPath;
import com.braintribe.model.meta.data.constraint.DateClipping;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.constraint.Referenceable;
import com.braintribe.model.meta.data.display.Group;
import com.braintribe.model.meta.data.display.GroupAssignment;
import com.braintribe.model.meta.data.prompt.AutoPagingSize;
import com.braintribe.model.meta.data.prompt.ColumnDisplay;
import com.braintribe.model.meta.data.prompt.ColumnInfo;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.meta.data.prompt.Condensed;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.DefaultView;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.DetailsViewMode;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Placeholder;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.JunctionSelector;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicTypeMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.utils.i18n.I18nTools;
import com.sencha.gxt.core.shared.FastMap;

/**
 * Utility class containing metadata resolution related code to be used throughout GME Components.
 *
 */
public class GMEMetadataUtil {
	
	private static GenericEntity singleEditableEntity;
	
	/**
	 * Configures the one and only entity where editions are allowed. When this is set, then
	 * {@link #isPropertyEditable(PropertyMdResolver, GenericEntity)} will always return false, unless checking this
	 * very same entity configured here.
	 */
	public static void configureReadOnlyExceptFor(GenericEntity entity) {
		singleEditableEntity = entity;
	}

	public static class CondensationBean {
	
	    private final String property;
	    private final CondensationMode mode;
	
	    public static CondensationBean createPair(String property, CondensationMode mode) {
	        return new CondensationBean(property, mode);
	    }
	
	    public CondensationBean(String property, CondensationMode mode) {
	        this.property = property;
	        this.mode = mode;
	    }
	
	    public String getProperty() {
			return property;
		}
	
	    public CondensationMode getMode() {
			return mode;
		}
	
	}

	/**
	 * Returns the entity {@link Name}, if available, or the entity short name.
	 */
	public static String getEntityNameMDOrShortName(EntityType<?> entityType, ModelMdResolver modelMdResolver, String useCase) {
		if (modelMdResolver != null) {
			Name nameMD = modelMdResolver.entityType(entityType).useCase(useCase).meta(Name.T).exclusive();
			if (nameMD != null) {
				LocalizedString name = nameMD.getName();
				if (name != null) {
					String resolvedName = I18nTools.getLocalized(name);
					if (resolvedName != null)
						return resolvedName;
				}
			}
		}
		
		return entityType.getShortName();
	}

	/**
	 * Returns the {@link Description}, if available, or the entity short name.
	 */
	public static String getEntityDescriptionMDOrShortName(EntityType<?> entityType, ModelMdResolver modelMdResolver, String useCase) {
		if (modelMdResolver != null) {
			Description descriptionMD = modelMdResolver.entityType(entityType).useCase(useCase).meta(Description.T).exclusive();
			if (descriptionMD != null) {
				LocalizedString description = descriptionMD.getDescription();
				if (description != null) {
					String resolvedDescription = I18nTools.getLocalized(description);
					if (resolvedDescription != null)
						return resolvedDescription;
				}
			}
		}
		
		return entityType.getShortName();
	}

	/**
	 * Returns the {@link Name} metadata for the given parameters.
	 */
	public static Name getName(GenericModelType genericModelType, Object instance, ModelMdResolver modelMdResolver, String useCase) {
		if (modelMdResolver == null)
			return null;
		
		if (genericModelType.isEntity())
			return modelMdResolver.entityType((EntityType<?>) genericModelType).useCase(useCase).meta(Name.T).exclusive();
		
		if (genericModelType.isEnum()) {
			if (instance instanceof Enum)
				return modelMdResolver.useCase(useCase).enumConstant((Enum<?>) instance).meta(Name.T).exclusive();
			return modelMdResolver.enumType((EnumType) genericModelType).useCase(useCase).meta(Name.T).exclusive();
		}
		
		return null;
	}

	/**
	 * Returns a Pair with the {@link Name} and {@link Description} for the given type.
	 */
	public static Pair<Name, Description> getNameAndDescription(Object instance, GenericModelType genericModelType, ModelMdResolver modelMdResolver, String useCase) {
		if (genericModelType.isEntity()) {
			if (instance instanceof GenericEntity)
				modelMdResolver = improvedGetMetadata((GenericEntity) instance, modelMdResolver);
			EntityMdResolver resolver = modelMdResolver.entityType((EntityType<?>) genericModelType).useCase(useCase);
			return new Pair<>(resolver.meta(Name.T).exclusive(), resolver.meta(Description.T).exclusive());
		}
		
		if (genericModelType.isEnum()) {
			EnumMdResolver resolver = modelMdResolver.enumType((EnumType) genericModelType).useCase(useCase);
			return new Pair<>(resolver.meta(Name.T).exclusive(), resolver.meta(Description.T).exclusive());
		}
		
		return null;
	}
	
	private static ModelMdResolver improvedGetMetadata(GenericEntity entity, ModelMdResolver defaultResolver) {
		if (entity.session() == null && defaultResolver != null)
			return defaultResolver.lenient(true);
		
		return getMetaData(entity);
	}

	/**
	 * Returns a set of {@link Condensed} for the given entityType. It automatically creates optional {@link Condensed}
	 * for all collection properties not having an explicit {@link Condensed} md.
	 */
	public static List<Condensed> getEntityCondensations(GenericEntity entity, EntityType<?> entityType, ModelMdResolver modelMdResolver, String useCase) {
		List<Condensed> entityCondensations = new ArrayList<>();
		EntityMdResolver entityMdResolver;
		if (entity != null)
			entityMdResolver = improvedGetMetadata(entity, modelMdResolver).entity(entity);
		else
			entityMdResolver = modelMdResolver.lenient(true).entityType(entityType);
		
		List<Condensed> list = entityMdResolver.useCase(useCase).meta(Condensed.T).list();
		if (list != null)
			entityCondensations.addAll(list);
		
		if (entityCondensations.isEmpty()) {
			entityType.getProperties().stream().filter(p -> p.getType().isCollection()).forEach(p -> {
				GmProperty gmProperty = GmProperty.T.create();
				gmProperty.setName(p.getName());

				Condensed condensed = Condensed.T.create();
				condensed.setCondensationMode(CondensationMode.optional);
				condensed.setProperty(gmProperty);
				entityCondensations.add(condensed);
			});
			
			return entityCondensations;
		}
		
		return entityCondensations.stream().anyMatch(c -> c.getProperty() == null) ? null : entityCondensations;
	}

	/**
	 * Returns the condensed property and its condensation mode.
	 */
	public static GMEMetadataUtil.CondensationBean getEntityCondensationProperty(List<Condensed> condensations, boolean priorityReverse) {
		if (condensations == null)
			return null;
		
		String condensedProperty = null;
		CondensationMode condensationMode = null;
		Double currentConflictPriority = 0.0;
		
		for (Condensed entityCondensation : condensations) {
			CondensationMode mode = entityCondensation.getCondensationMode();
			if (CondensationMode.auto.equals(mode) || CondensationMode.forced.equals(mode)) {
				Double conflictPriority = entityCondensation.getConflictPriority();
				if (condensedProperty == null) {
					condensedProperty = entityCondensation.getProperty().getName();
					condensationMode = mode;
					currentConflictPriority = conflictPriority;
				} else {
					boolean checkPriority = priorityReverse ? currentConflictPriority > conflictPriority : currentConflictPriority < conflictPriority;
					if (checkPriority) {
						condensedProperty = entityCondensation.getProperty().getName();
						condensationMode = mode;
						currentConflictPriority = conflictPriority;
					}
				}
			}
		}
		
		if (condensedProperty != null)
			return GMEMetadataUtil.CondensationBean.createPair(condensedProperty, condensationMode);
		
		return null;
	}

	/**
	 * Checks if the given property has the {@link Confidential} metaData set and it is a password.
	 */
	public static boolean isPropertyPassword(PropertyMdResolver propertyMdResolver) {
		return propertyMdResolver != null && propertyMdResolver.is(Confidential.T);
	}

	/**
	 * Checks if the given property has the {@link Visible} metaData set and it is visible.
	 */
	public static boolean isPropertyVisible(PropertyMdResolver propertyMdResolver) {
		return propertyMdResolver.is(Visible.T);
	}

	/**
	 * Checks if the given property is editable by checking for the presence of the {@link Modifiable} metadata, and
	 * also checking the {@link AclOperation#WRITE}. @see #configureReadOnlyExceptFor(GenericEntity).
	 */
	public static boolean isPropertyEditable(PropertyMdResolver propertyMdResolver, GenericEntity entity) {
		if (singleEditableEntity != null && singleEditableEntity != entity)
			return false;
		
		if (entity != null) {
			GmSession session = entity.session();
			if (session instanceof PersistenceGmSession) {
				boolean preliminary = ((PersistenceGmSession) session).getTransaction().created(entity);
				propertyMdResolver = propertyMdResolver.preliminary(preliminary);
			}
		}
		
		return propertyMdResolver.is(Modifiable.T) && GMEUtil.isOperationGranted(entity, AclOperation.WRITE);
	}
	
	/**
	 * Prepares the given {@link PropertyMdResolver} as preliminary in case the entity is preliminary.
	 */
	public static PropertyMdResolver getPropertyMdResolverForEntity(PropertyMdResolver propertyMdResolver, GenericEntity entity) {
		if (entity == null)
			return propertyMdResolver;
		
		GmSession session = entity.session();
		if (!(session instanceof PersistenceGmSession))
			return propertyMdResolver;
		
		boolean preliminary = ((PersistenceGmSession) session).getTransaction().created(entity);
		propertyMdResolver = propertyMdResolver.preliminary(preliminary);
		return propertyMdResolver;
	}
	
	/**
	 * Checks whether the details panel is configured to be hidden for the given type.
	 * Returns that, and also the hard hide configuration.
	 */
	public static DetailsViewMode getDetailsViewMode(Object queryOrTemplate, String entityTypeSignature, PersistenceGmSession gmSession) {
		DetailsViewMode detailsViewMode = null;
		if (queryOrTemplate instanceof Template) {
			Template template = (Template) queryOrTemplate;
			detailsViewMode = getTemplateMetaData(template, DetailsViewMode.T, null);
		}
		
		if (detailsViewMode == null && entityTypeSignature != null) {
			detailsViewMode = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).lenient(true).meta(DetailsViewMode.T)
					.exclusive();
		}
		
		return detailsViewMode;
	}
	
	/**
	 * Returns the metadata instance if the type is found within the template.
	 */
	public static <T> T getTemplateMetaData(Template template, EntityType<?> metadataEntityType, EntityType<?> templateMetadataEntityType) {
		for (TemplateMetaData tmd : template.getMetaData()) {
			if (tmd == null)
				continue;
			//String typeSignature = tmd.type().getTypeSignature();
			EntityType<?> tmdType = tmd.entityType();
			if ((metadataEntityType != null && metadataEntityType.isAssignableFrom(tmdType))
					|| (templateMetadataEntityType != null && templateMetadataEntityType.isAssignableFrom(tmdType))) {
				return (T) tmd;
			} else if (tmd instanceof DynamicTypeMetaDataAssignment) {
				for (MetaData md : ((DynamicTypeMetaDataAssignment) tmd).getMetaData()) {
					if (metadataEntityType != null && metadataEntityType.isAssignableFrom(md.entityType()))
						return (T) md;
				}
			}
			/*if (typeSignature.equals(metadataEntityTypeSignature) || typeSignature.equals(templateMetadataEntityTypeSignature))
				return (T) tmd;
			else if (tmd instanceof DynamicTypeMetaDataAssignment) {
				for (MetaData md : ((DynamicTypeMetaDataAssignment) tmd).getMetaData()) {
					if (md.type().getTypeSignature().equals(metadataEntityTypeSignature))
						return (T) md;
				}
			}*/
		}
		
		return null;
	}

	/**
	 * Returns the max count for the given property, if the {@link MaxLength} exists. Null, otherwise.
	 */
	public static Integer getPropertyCollectionMaxCount(PropertyMdResolver propertyMdResolver) {
		MaxLength ml = propertyMdResolver.meta(MaxLength.T).exclusive();
		return ml == null ? null : (int) ml.getLength();
	}

	/**
	 * Returns the localized property name. It first checks the {@link Name} metadata. If that is null, then the property name is returned
	 */
	public static String getPropertyDisplay(Name name, String propertyName) {
		if (name == null || name.getName() == null)
			return propertyName;
		
		String localizedName = I18nTools.getLocalized(name.getName());
		return localizedName != null ? localizedName : propertyName;
	}

	/**
	 * Returns the localized property name. It first checks the {@link Name} metadata. If that does not exists, then the property name is
	 * returned.
	 */
	public static String getPropertyDisplay(String propertyName, PropertyMdResolver propertyMdResolver) {
		return getPropertyDisplay(GMEMetadataUtil.getName(propertyMdResolver), propertyName);
	}

	/**
	 * Returns the {@link Name} for the given property.
	 */
	public static Name getName(PropertyMdResolver propertyMdResolver) {
		return propertyMdResolver.meta(Name.T).exclusive();
	}

	/**
	 * Returns the localized property name and description. It first checks the {@link Name} metadata. If that does not exists, then the property
	 * name is returned. For the description, if the {@link Description} metadata does not exist, null is returned.
	 */
	public static Pair<String, String> getPropertyDisplayAndDescription(String propertyName, PropertyMdResolver propertyMdResolver) {
		Name displayInfo = propertyMdResolver.meta(Name.T).exclusive();
		Description descriptionMD = propertyMdResolver.meta(Description.T).exclusive();
		String name = propertyName;
		String description = null;
		if (displayInfo != null && displayInfo.getName() != null)
			name = I18nTools.getLocalized(displayInfo.getName());
		if (name == null)
			name = propertyName;
		if (descriptionMD != null && descriptionMD.getDescription() != null)
			description = I18nTools.getLocalized(descriptionMD.getDescription());
		
		return new Pair<>(name, description);
	}
	
	/**
	 * Returns the localized placeholder for the given property.
	 */
	public static String getPlaceholder(PropertyMdResolver propertyMdResolver) {
		if (propertyMdResolver == null)
			return null;
		
		Placeholder placeHolder = propertyMdResolver.meta(Placeholder.T).exclusive();
		return placeHolder != null ? I18nTools.getLocalized(placeHolder.getDescription()) : null;
	}

	/**
	 * Returns the priority for the given property, based on the {@link Priority} metadata.
	 * Returns null if the property doesn't have this metadata.
	 */
	public static Double getPropertyPriority(PropertyMdResolver propertyMdResolver) {
		if (propertyMdResolver == null)
			return null;
		
		Priority propertyPriority = propertyMdResolver.meta(Priority.T).exclusive();
		Double priority = propertyPriority == null ? null : propertyPriority.getPriority();
		return priority;
	}

	/**
	 * Returns the type display of the given entity, based on the {@link Name}. Returns null if there is no metadata.
	 */
	public static String getEntityTypeDisplay(EntityMdResolver entityMdResolver) {
		if (entityMdResolver != null) {
			Name entityTypeDisplayInfo = entityMdResolver.meta(Name.T).exclusive();
			if (entityTypeDisplayInfo != null)
				return I18nTools.getLocalized(entityTypeDisplayInfo.getName());
		}
		
		return null;
	}

	/**
	 * Returns the name of the group of the given property, based on the {@link GroupAssignment}.
	 * Returns null if there is no such metadata.
	 */
	public static String getGroupName(PropertyMdResolver propertyMdResolver) {
		GroupAssignment groupAssignment = propertyMdResolver.meta(GroupAssignment.T).exclusive();
		if (groupAssignment != null) {
			Group propertyGroup = groupAssignment.getGroup();
			if (propertyGroup != null)
				return I18nTools.getLocalized(propertyGroup.getLocalizedName());
		}
		
		return null;
	}

	/**
	 * Prepares a map which key is the first property in the path of the the {@link EntityCompoundViewing}, and the value is the {@link EntityCompoundViewing}
	 * itself.
	 */
	public static Map<String, EntityCompoundViewing> getEntityCompoundViewingsMap(EntityMdResolver entityMdResolver) {
		List<EntityCompoundViewing> entityCompoundViewings = entityMdResolver.meta(EntityCompoundViewing.T).list();
		
		if (entityCompoundViewings.isEmpty())
			return null;
		
		Map<String, EntityCompoundViewing> map = null;
		for (EntityCompoundViewing entityCompoundViewing : entityCompoundViewings) {
			PropertyPath propertyPath = entityCompoundViewing.getPropertyPath();
			if (propertyPath != null && !propertyPath.getProperties().isEmpty()) {
				if (map == null)
					map = new FastMap<>();
				map.put(propertyPath.getProperties().get(0).getName(), entityCompoundViewing);
			}
		}
		
		return map;
	}
	
	/**
	 * Checks whether the given property is {@link Instantiable}. It also checks the type of the property.
	 */
	public static boolean isInstantiable(PropertyMdResolver propertyMdResolver, ModelMdResolver modelMdResolver) {
		if (!propertyMdResolver.is(Instantiable.T))
			return false;
		
		GmProperty property = null;
		try {
			property = propertyMdResolver.getGmProperty();
		} catch (Exception ex) {
			//The EmptyPropertyMdResolver doesn't have this method defined, thus we need to catch this exception.
		}
		if (property == null || !property.getType().isGmEntity())
			return true;
		
		return modelMdResolver.entityTypeSignature(property.getType().getTypeSignature()).is(Instantiable.T);
	}
	
	/**
	 * Checks whether the given property is {@link Referenceable}. It also checks the type of the property.
	 */
	public static boolean isReferenceable(PropertyMdResolver propertyMdResolver, ModelMdResolver modelMdResolver) {
		if (propertyMdResolver == null)
			return true;
		
		if (!propertyMdResolver.is(Referenceable.T))
			return false;
		
		GmProperty property = null;
		try {
			property = propertyMdResolver.getGmProperty();
		} catch (Exception ex) {
			//The EmptyPropertyMdResolver doesn't have this method defined, thus we need to catch this exception.
		}
		
		if (property == null || !property.getType().isGmEntity())
			return true;
		
		if (modelMdResolver == null)
			return true;
		
		return modelMdResolver.entityTypeSignature(property.getType().getTypeSignature()).is(Referenceable.T);
	}
	
	/**
	 * Returns the default view identification in case it is special (not thumbnail nor assembly) or if the currentViewUseCase is null.
	 */
	public static String getSpecialDefaultView(String currentViewUseCase, GenericEntity entity) {
		return getSpecialDefaultView(getMetaData(entity).entity(entity).meta(DefaultView.T).exclusive(), currentViewUseCase);
	}
	
	/**
	 * Returns the default view identification in case it is special (not thumbnail nor assembly) or if the currentViewUseCase is null.
	 */
	public static String getSpecialDefaultView(String currentViewUseCase, Object queryOrTemplate, String entityTypeSignature,
			PersistenceGmSession gmSession) {
		DefaultView defaultView = null;
		if (queryOrTemplate instanceof Template) {
			Template template = (Template) queryOrTemplate;
			defaultView = GMEMetadataUtil.getTemplateMetaData(template, DefaultView.T, com.braintribe.model.workbench.meta.DefaultView.T);
		}
		
		if (defaultView == null && entityTypeSignature != null) {
			defaultView = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).lenient(true).meta(DefaultView.T)
					.exclusive();
		}
		
		return getSpecialDefaultView(defaultView, currentViewUseCase);
	}
	
	/**
	 * Returns the configured auto paging size. There are 3 possibilities for that. It comes from the {@link Template},
	 * from the {@link AutoPagingSize} or from the {@link WorkbenchConfiguration}. It is checked in that order. The one
	 * existing first will be returned. If none exist, null is returned.
	 */
	public static Integer getAutoPagingSize(Template template, String entityTypeSignature, PersistenceGmSession gmSession, String useCase) {
		if (template != null) {
			AutoPagingSize autoPagingSize = getTemplateMetaData(template, AutoPagingSize.T, com.braintribe.model.workbench.meta.AutoPagingSize.T);
			if (autoPagingSize != null)
				return autoPagingSize.getSize();
		}
		
		if (entityTypeSignature != null) {
			AutoPagingSize autoPagingSize = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).lenient(true)
					.useCase(useCase).meta(AutoPagingSize.T).exclusive();
			if (autoPagingSize != null)
				return autoPagingSize.getSize();
		}
		
		if (gmSession instanceof ModelEnvironmentDrivenGmSession) {
			WorkbenchConfiguration config = ((ModelEnvironmentDrivenGmSession) gmSession).getModelEnvironment().getWorkbenchConfiguration();
			if (config != null)
				return config.getAutoPagingSize();
		}
		
		return null;
	}
	
	/**
	 * Returns the limit according to the configured {@link Max} metadata (if existent).
	 * If it doesn't exist, the defaultLimit is returned.
	 */
	public static int getMaxLimit(EntityMdResolver entityMdResolver, Property property, int defaultLimit) {
		Max max = entityMdResolver.property(property).meta(Max.T).exclusive();
		
		int maxSize = defaultLimit;
		if (max == null)
			return maxSize;
		
		if (max.getLimit() == null)
			maxSize = -1;
		else {
			try {
				if (max.getLimit() instanceof String)
					maxSize = Integer.parseInt((String) max.getLimit()); 	
				else if (max.getLimit() instanceof Number)	
					maxSize = ((Number) max.getLimit()).intValue();
			} catch (RuntimeException e) {
				//Ignore value
			}
			
			if (maxSize < -1)
			  maxSize = -1;
		}
		
		return maxSize;
	}
	
	/**
	 * Prepares a {@link ColumnData} from the given {@link ColumnDisplay}, if any.
	 */
	public static ColumnData prepareColumnData(ColumnDisplay columnDisplay) {
		if (columnDisplay == null)
			return null;
		
		ColumnData columnData = ColumnData.T.create();
		columnData.setDisplayNode(columnDisplay.getDisplayNode());
		columnData.setNodeWidth(columnDisplay.getNodeWidth());
		columnData.setPreventSingleEntryExpand(columnDisplay.getPreventSingleEntryExpand());
		columnData.setDisableExpansion(columnDisplay.getDisableExpansion());
		
		if (columnDisplay.getNodeTitle() != null) {
			LocalizedString ls = LocalizedString.T.create();
			ls.getLocalizedValues().putAll(columnDisplay.getNodeTitle().getLocalizedValues());
			columnData.setNodeTitle(ls);
		}
		
		List<StorageColumnInfo> list = new ArrayList<>();
		for (ColumnInfo ci : columnDisplay.getDisplayPaths()) {
			StorageColumnInfo sci = StorageColumnInfo.T.create();
			sci.setPath(ci.getPath());
			sci.setDeclaringTypeSignature(ci.getDeclaringTypeSignature());
			sci.setWidth(ci.getWidth());
			if (ci.getTitle() != null) {
				LocalizedString ls = LocalizedString.T.create();
				ls.getLocalizedValues().putAll(ci.getTitle().getLocalizedValues());
				sci.setTitle(ls);
			}
			sci.setAutoExpand(ci.getAutoExpand());
			list.add(sci);
		}
		columnData.setDisplayPaths(list);
		
		return columnData;
	}
	
	/**
	 * Returns the date pattern for the given {@link PropertyMdResolver} context, based on the {@link DateClipping} metadata.
	 * If no {@link DateClipping} is configured, then the defaultPattern is returned.
	 */
	public static String getDatePattern(PropertyMdResolver propertyMdResolver, String defaultPattern) {
		DateClipping dateClipping = propertyMdResolver.meta(DateClipping.T).exclusive();
		return getDatePattern(dateClipping, defaultPattern);
	}
	
	/**
	 * Returns the date pattern for the given {@link DateClipping} metadata.
	 * If no {@link DateClipping} is set, then the defaultPattern is returned.
	 */
	public static String getDatePattern(DateClipping dateClipping, String defaultPattern) {
		if (dateClipping == null)
			return defaultPattern;
		
		DateOffsetUnit lower = dateClipping.getLower();
		DateOffsetUnit upper = dateClipping.getUpper();
		
		String dateSeparator = dateClipping.getDateSeparator() != null ? dateClipping.getDateSeparator() : "/";
		String timeSeparator = dateClipping.getTimeSeparator() != null ? dateClipping.getTimeSeparator() : ":";
		LocaleUtil.configureDateSeparator(dateSeparator);
		LocaleUtil.configureTimeSeparator(timeSeparator);
		
		String datePattern;
		if (DateOffsetUnit.month.equals(lower)) {
			if (upper == null || upper.equals(DateOffsetUnit.year))
				datePattern = LocaleUtil.getMonthYearFormat();
			else
				datePattern = "MM";
		} else if (DateOffsetUnit.day.equals(lower)) {
			if ((upper == null || upper.equals(DateOffsetUnit.year)))
				datePattern = LocaleUtil.getDateFormat();
			else if (DateOffsetUnit.month.equals(upper))
				datePattern = LocaleUtil.getDayMonthFormat();
			else
				datePattern = "dd";
		} else if (DateOffsetUnit.year.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year)))
			datePattern = "yyyy";
		else if (DateOffsetUnit.second.equals(lower)) {
			if (upper == null || upper.equals(DateOffsetUnit.year))
				datePattern = LocaleUtil.getDateTimeSecondFormat();
			else if (DateOffsetUnit.hour.equals(upper))
				datePattern = "HH" + timeSeparator + "mm" + timeSeparator + "ss";
			else if (DateOffsetUnit.minute.equals(upper))
				datePattern = "mm" + timeSeparator + "ss";
			else
				datePattern = "ss";
		}
		else if (lower == null || DateOffsetUnit.millisecond.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year)))
			datePattern = LocaleUtil.getDateTimeSecondMilisecondFormat();
		else if (DateOffsetUnit.minute.equals(lower)) {
			if (DateOffsetUnit.hour.equals(upper))
				datePattern = "HH" + timeSeparator + "mm";
			else
				datePattern = LocaleUtil.getDateTimeFormat();
		} else if (DateOffsetUnit.hour.equals(lower) && DateOffsetUnit.hour.equals(upper))
			datePattern = "HH";
		else
			datePattern = defaultPattern;
		
		LocaleUtil.configureDateSeparator("/");
		LocaleUtil.configureTimeSeparator(":");
		
		return datePattern;
	}
	
	private static String getSpecialDefaultView(DefaultView defaultView, String currentViewUseCase) {
		String defaultViewIdentification = null;
		if (defaultView != null)
			defaultViewIdentification = defaultView.getViewIdentification();
		
		//We will always use the view which was passed here (the previousView), unless the defaultView is special
		if (currentViewUseCase == null || isSpecialView(defaultViewIdentification))
			currentViewUseCase = defaultViewIdentification;
		
		return currentViewUseCase;
	}
	
	private static boolean isSpecialView(String viewIdentification) {
		if (viewIdentification != null && !viewIdentification.equals(KnownUseCase.assemblyPanelUseCase.getDefaultValue())
				&& !viewIdentification.equals(KnownUseCase.thumbnailPanelUseCase.getDefaultValue()))
			return true;
		
		return false;
	}
	
	/**
	 * Returns the properties for the {@link PropertyValueComparator} if the given metadata has any of those as selector.
	 */
	public static Set<String> getPropertyPathsForMetadataWithPropertyValueComparator(MetaData metadata) {
		if (metadata == null)
			return null;
		
		return getSelectorPropertyValueComparatorPropertyPaths(metadata.getSelector());
	}
	
	private static Set<String> getSelectorPropertyValueComparatorPropertyPaths(MetaDataSelector selector) {
		if (selector == null)
			return null;
		
		if (selector instanceof PropertyValueComparator)
			return new FastSet(Arrays.asList(((PropertyValueComparator) selector).getPropertyPath()));
		
		if (selector instanceof NegationSelector)
			return getSelectorPropertyValueComparatorPropertyPaths(((NegationSelector) selector).getOperand());
		
		if (selector instanceof JunctionSelector) {
			List<MetaDataSelector> operands = ((JunctionSelector) selector).getOperands();
			Set<String> allPropertyPaths = null;
			for (MetaDataSelector operand : operands) {
				Set<String> propertyPaths = getSelectorPropertyValueComparatorPropertyPaths(operand);
				if (propertyPaths != null) {
					if (allPropertyPaths == null)
						allPropertyPaths = new FastSet();
					allPropertyPaths.addAll(propertyPaths);
				}
			}
			
			return allPropertyPaths;
		}
		
		return null;
	}

	/*
	 * Returns the list of {@link Embedded} properties of the given {@link GenericEntity}.
	 *
	public static List<String> getEmbeddedProperties(GenericEntity entity, EntityType<?> entityType, ModelMdResolver mdResolver, String useCase) {
		List<String> embeddedProperties = new ArrayList<>();
		if (entity != null) {
			entityType = entity.entityType();
			mdResolver = getMetaData(entity);
		}
		
		if (mdResolver != null) {
			ModelMdResolver modelMdResolver = mdResolver.useCase(useCase);
			
			EntityMdResolver parentEntityMdResolver;
			if (entity != null)
				parentEntityMdResolver = modelMdResolver.entity(entity);
			else
				parentEntityMdResolver = modelMdResolver.entityType(entityType);
			
			entityType.getProperties().stream().forEach(property -> {
				GenericModelType propertyType = property.getType();
				if (propertyType.isEntity()) {
					Embedded embedded = parentEntityMdResolver.property(property).meta(Embedded.T).exclusive();
					if (embedded == null)
						embedded = modelMdResolver.entityType((EntityType<?>) propertyType).meta(Embedded.T).exclusive();
					
					if (embedded != null)
						embeddedProperties.add(property.getName());
				}
			});
		}
		
		return embeddedProperties;
	}*/

}
