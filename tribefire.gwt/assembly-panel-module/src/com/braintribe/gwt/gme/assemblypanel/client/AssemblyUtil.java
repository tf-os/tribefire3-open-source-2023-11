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
package com.braintribe.gwt.gme.assemblypanel.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CollectionTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CondensedEntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyAndValueTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyOrValueEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ObjectAndType;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntry;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryModelInterface;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ValueTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.PropertyBean;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.display.Bulleting;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.WindowManager;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 * Contains utility methods and classes
 * @author michel.docouto
 *
 */
public class AssemblyUtil {
	private static final Logger logger = new Logger(AssemblyUtil.class);
	
	private AbstractAssemblyPanel assemblyPanel;
	private String treeRendererSeparator = ":";
	private String listIndexSeparator = ".";
	private CodecRegistry<String> codecRegistry;
	public static final int MAX_SET_ENTRIES = 10;
	private static Timer replaceValueSelectionTimer;
	private static Set<AbstractGenericTreeModel> modelsWithUpdatePending = new HashSet<>();
	
	public void configureAssemblyPanel(AbstractAssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
	}
	
	/**
	 * Configures the required {@link CodecRegistry} used as renderers.
	 */
	@Required
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures the separator used in the tree node renderer.
	 * Defaults to ":"
	 */
	@Configurable
	public void setTreeRendererSeparator(String treeRendererSeparator) {
		this.treeRendererSeparator = treeRendererSeparator;
	}
	
	/**
	 * Configures the separator used in the tree node renderer for list elements.
	 * Defaults to "."
	 */
	@Configurable
	public void setListIndexSeparator(String listIndexSeparator) {
		this.listIndexSeparator = listIndexSeparator;
	}
	
	/**
	 * Checks if the given model is absent.
	 * @return the PropertyEntryModelInterface where the absence is. Null if the model is not absent.
	 */
	public static PropertyEntryModelInterface isModelAbsent(AbstractGenericTreeModel model) {
		PropertyEntryModelInterface absentProperty = getAbsentPropertyModel(model);
		if (absentProperty == null)
			absentProperty = getAbsentPropertyModel(model.getDelegate());
		
		return absentProperty;
	}
	
	private static PropertyEntryModelInterface getAbsentPropertyModel(AbstractGenericTreeModel model) {
		if (!(model instanceof PropertyEntryModelInterface))
			return null;
		
		PropertyEntry propertyEntry = ((PropertyEntryModelInterface) model).getPropertyEntry();
		return propertyEntry.isAbsent() ? (PropertyEntryModelInterface) model : null;
	}
	
	/**
	 * This method prepares the string representation of the given model, including the value and description representations.
	 * @param mapTopLevel - If true, {@link MapEntryTreeModel} models representations will be prepared with the key only.
	 * If false, both key and values will be taken.
	 * @param useValueAsDescription - if true, then if no description exists by default, then the value will be also used as description.
	 * @param showLink - if true, then display the linkStyle.
	 */
	public ValueDescriptionBean prepareLabel(AbstractGenericTreeModel model, boolean mapTopLevel, boolean useValueAsDescription, boolean showLink) {
		return prepareLabel(model, mapTopLevel, useValueAsDescription, showLink, true);
	}
	
	/**
	 * This method prepares the string representation of the given model, including the value and description representations.
	 * @param mapTopLevel - If true, {@link MapEntryTreeModel} models representations will be prepared with the key only.
	 * If false, both key and values will be taken.
	 * @param useValueAsDescription - if true, then if no description exists by default, then the value will be also used as description.
	 * @param showLink - if true, then display the linkStyle.
	 * @param prepareStyling - if false, no html styling is prepared.
	 */
	public ValueDescriptionBean prepareLabel(AbstractGenericTreeModel model, boolean mapTopLevel, boolean useValueAsDescription, boolean showLink, boolean prepareStyling) {
		String useCase = assemblyPanel.getUseCase();
		ModelMdResolver modelMdResolver = assemblyPanel.getGmSession().getModelAccessory().getMetaData();
		if (model instanceof ListEntryTreeModel)
			return prepareListEntryLabel(model, useValueAsDescription, modelMdResolver, useCase, prepareStyling);
		
		if (model instanceof MapEntryTreeModel) {
			//MapEntry mapEntry = ((MapEntryTreeModel) model).getMapEntry(); TODO: try if this works... otherwise, use the MapEntry
			GenericModelType elementType = model.getElementType();
			return prepareValueDescriptionBean(useCase, getModelObjectFromModel(model), modelMdResolver, elementType, getRendererCodec(elementType, null), useValueAsDescription);
		}
		
		if (model instanceof MapKeyAndValueTreeModel)
			return prepareMapKeyAndValueLabel(model/*, mapTopLevel*/, useValueAsDescription, modelMdResolver, useCase, prepareStyling);
		
		if (model instanceof MapKeyOrValueEntryTreeModel) { //TODO: verify how maps are behaving...
			GenericModelType elementType = model.getElementType();
			return prepareValueDescriptionBean(useCase, getModelObjectFromModel(model), modelMdResolver, elementType, getRendererCodec(elementType, null), useValueAsDescription);
		}
		
		if (model instanceof PropertyEntryModelInterface)
			return preparePropertyEntryLabel(model, useValueAsDescription, showLink, useCase, prepareStyling);
		
		if (model instanceof SetEntryTreeModel) {
			GenericModelType elementType = model.getElementType();
			return prepareValueDescriptionBean(useCase, getModelObjectFromModel(model), modelMdResolver, elementType, getRendererCodec(elementType, null), useValueAsDescription);
		}
		
		EntityTreeModel entityTreeModel = model.getEntityTreeModel();
		if (entityTreeModel != null) {
			GenericModelType elementType = entityTreeModel.getElementType();
			return prepareValueDescriptionBean(useCase, entityTreeModel.getModelObject(), modelMdResolver, elementType, getRendererCodec(elementType, null),
					useValueAsDescription);
		}
		
		if (model instanceof ValueTreeModel)
			return prepareValueDescriptionBean(useCase, getModelObjectFromModel(model), modelMdResolver, model.getElementType(), null, useValueAsDescription);
		
		return new ValueDescriptionBean("", "");
	}

	private ValueDescriptionBean prepareListEntryLabel(AbstractGenericTreeModel model, boolean useValueAsDescription, ModelMdResolver modelMdResolver, String useCase,
			boolean prepareStyling) {
		String index = Integer.toString(((ListEntryTreeModel) model).getListEntryIndex() + 1);
		Object modelObject = getModelObjectFromModel(model);
		
		GenericModelType elementType = model.getElementType();
		ValueDescriptionBean bean = prepareValueDescriptionBean(useCase, modelObject, modelMdResolver, elementType, getRendererCodec(elementType, null), useValueAsDescription);
		if (prepareStyling && checkPropertyBulleting(model))
			bean.setValue("<span class=\"" + AssemblyPanelResources.INSTANCE.css().collectionElementStyle() + "\">" + index + listIndexSeparator + "</span>"
					+ bean.getValue());
		return bean;
	}
	
	private ValueDescriptionBean prepareMapKeyAndValueLabel(AbstractGenericTreeModel model/* , boolean mapTopLevel */, boolean useValueAsDescription,
			ModelMdResolver modelMdResolver, String useCase, boolean prepareStyling) {
		Object keyObject = getModelObjectFromModel(((MapKeyAndValueTreeModel) model).getMapKeyEntryTreeModel());
		GenericModelType keyElementType = ((MapKeyAndValueTreeModel) model).getKeyElementType();
		/*if (mapTopLevel) {
			return prepareValueDescriptionBean(useCase, keyObject, modelMdResolver, keyElementType,
					getRendererCodec(keyElementType, null),
					useValueAsDescription);
		}*/
		Object valueObject = getModelObjectFromModel(((MapKeyAndValueTreeModel) model).getMapValueEntryTreeModel());
		ValueDescriptionBean keyBean = prepareValueDescriptionBean(useCase, keyObject, modelMdResolver, keyElementType,
				getRendererCodec(keyElementType, null), useValueAsDescription);
		GenericModelType elementType = model.getElementType();
		ValueDescriptionBean valueBean = prepareValueDescriptionBean(useCase, valueObject, modelMdResolver, elementType,
				getRendererCodec(elementType, null), useValueAsDescription);
		
		if (!prepareStyling)
			return new ValueDescriptionBean(keyBean.getValue() + ": " + valueBean.getValue(), null);
		
		return new ValueDescriptionBean(keyBean.getValue() + "<span class='" + AssemblyPanelResources.INSTANCE.css().mapKeyAndValueSeparator()
				+ "'>&nbsp;</span>" + valueBean.getValue(), null);
	}
	
	private ValueDescriptionBean preparePropertyEntryLabel(AbstractGenericTreeModel model, boolean useValueAsDescription, boolean showLink,
			String useCase, boolean prepareStyling) {
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		PropertyEntry propertyEntry;
		if (delegateModel instanceof CondensedEntityTreeModel && ((CondensedEntityTreeModel) delegateModel).getPropertyEntryTreeModelInParent() != null)
			propertyEntry = ((CondensedEntityTreeModel) delegateModel).getPropertyEntryTreeModelInParent().getPropertyEntry();
		else
			propertyEntry = ((PropertyEntryModelInterface) model).getPropertyEntry();
		
		String propertyName = propertyEntry.getPropertyName();
		
		ModelMdResolver modelMdResolver;
		EntityMdResolver entityContextBuilder;
		if (propertyEntry.getEntity() != null) {
			modelMdResolver = GmSessions.getMetaData(propertyEntry.getEntity());
			entityContextBuilder = modelMdResolver.entity(propertyEntry.getEntity());
		} else {
			modelMdResolver = assemblyPanel.gmSession.getModelAccessory().getMetaData();
			entityContextBuilder = modelMdResolver.entityType(propertyEntry.getEntityType());
		}
		PropertyMdResolver propertyMetaDataContextBuilder = entityContextBuilder.property(propertyName).useCase(useCase);
		Pair<String, String> nameAndDescription = GMEMetadataUtil.getPropertyDisplayAndDescription(propertyName, propertyMetaDataContextBuilder);
		propertyName = nameAndDescription.getFirst();
		
		if (delegateModel instanceof CollectionTreeModel && !(delegateModel instanceof CondensedEntityTreeModel))
			return prepareCollectionPropertyLabel(useValueAsDescription, showLink, delegateModel, propertyEntry, propertyName, nameAndDescription.getSecond());
		
		Object modelObject = getModelObjectFromModel(model);
		boolean isPassword = GMEMetadataUtil.isPropertyPassword(propertyMetaDataContextBuilder);
		ValueDescriptionBean bean;
		if (isPassword)
			bean = new ValueDescriptionBean(GMEUtil.preparePasswordString((String) modelObject), null);
		else {
			PropertyBean propertyBean = new PropertyBean(propertyName, propertyEntry.getEntity(), propertyEntry.getEntityType());
			bean = prepareValueDescriptionBean(useCase, modelObject, modelMdResolver, model.getElementType(),
					getRendererCodec(model.getElementType(), propertyBean), useValueAsDescription);
		}
		
		if (nameAndDescription.getSecond() != null)
			bean.setDescription(nameAndDescription.getSecond());
		
		if (delegateModel.getParent() == null && bean.getValue() != null)
			return bean;
		
		if (useValueAsDescription && (bean.getDescription() == null || bean.getDescription().isEmpty()))
			bean.setDescription(bean.getValue());
		
		StringBuilder value = new StringBuilder();
		
		if (!prepareStyling) {
			value.append(propertyName).append(treeRendererSeparator)
					.append((bean.getValue() != null && !bean.getValue().isEmpty() ? " " + bean.getValue() : ""));
		} else {
			value.append("<span class=\"").append(AssemblyPanelResources.INSTANCE.css().propertyNameStyle()).append(" ")
					.append(GMEUtil.PROPERTY_NAME_CSS);
			if (showLink)
				value.append(" ").append(AssemblyPanelResources.INSTANCE.css().linkStyle());
			value.append("\">").append(propertyName).append(treeRendererSeparator).append("</span>");
			
			ImageResource icon = model.getIcon();
			if (icon != null) {
				if (icon == AssemblyPanelResources.INSTANCE.clear()) 
					value.append(AbstractImagePrototype.create(icon).getHTML().replace("<img", "<img class='clearImage'"));
				else	
					value.append(AbstractImagePrototype.create(icon).getHTML().replace("style='", "style='margin-left: 4px; "));
			}
			
			value.append("<span style='vertical-align: top;'>")
					.append(bean.getValue() != null && !bean.getValue().isEmpty() ? " " + bean.getValue() : "").append("</span>");
		}
		
		bean.setValue(value.toString());
		
		return bean;
	}

	private ValueDescriptionBean prepareCollectionPropertyLabel(boolean useValueAsDescription, boolean showLink,
			AbstractGenericTreeModel delegateModel, PropertyEntry propertyEntry, String propertyName, String description) {
		if (description == null)
			description = "";
		
		String value = prepareLabelForCollection(propertyName, (CollectionTreeModel) delegateModel, showLink, propertyEntry);
		if (useValueAsDescription && description.isEmpty())
			description = value;
		
		return new ValueDescriptionBean(value, description);
	}
	
	protected Codec<Object, String> getRendererCodec(GenericModelType modelType, PropertyBean propertyBean) {
		return GMEUtil.getRendererCodec(codecRegistry, modelType, assemblyPanel.getGmSession(), assemblyPanel.getUseCase(), propertyBean);
	}
	
	private boolean checkPropertyBulleting(AbstractGenericTreeModel model) {
		if (!(model instanceof ListEntryTreeModel) && !(model instanceof MapEntryTreeModel) && !(model instanceof MapKeyAndValueTreeModel)
				&& !(model instanceof MapKeyOrValueEntryTreeModel) && !(model instanceof SetEntryTreeModel))
			return false;
		
		EntityTreeModel entityTreeModel = AssemblyUtil.getParentEntityTreeModel(model);
		if (entityTreeModel == null)
			return false;
		
		String propertyName = model.getDelegate().getParent().getPropertyName();
		EntityMdResolver entityContextBuilder;
		if (entityTreeModel.getModelObject() instanceof GenericEntity) {
			GenericEntity entity = entityTreeModel.getModelObject();
			entityContextBuilder = getMetaData(entity).entity(entity);
		} else
			entityContextBuilder = assemblyPanel.getGmSession().getModelAccessory().getMetaData().entityType(entityTreeModel.getElementType());
		
		return  entityContextBuilder.property(propertyName).useCase(assemblyPanel.getUseCase()).is(Bulleting.T);
	}
	
	/**
	 * Prepares the value/description Entry based on the instance and the type (used only for Enums or Entities).
	 * @param rendererCodec - Codec for the simplified values.
	 * @param useValueAsDescription = if true, then if no description exists by default, then the value will be also used as description.
	 */
	private static ValueDescriptionBean prepareValueDescriptionBean(String useCase, Object instance, ModelMdResolver modelMdResolver,
			GenericModelType genericModelType, Codec<Object, String> rendererCodec, boolean useValueAsDescription) {
		String description = null;
		String value = null;
		
		if (rendererCodec != null) {
			try {
				value = SafeHtmlUtils.htmlEscape(rendererCodec.encode(instance));
				if (useValueAsDescription)
					description = value;
				else
					description = getDescription(instance, genericModelType, useCase, modelMdResolver);
				
				return new ValueDescriptionBean(value, description);
			} catch (CodecException e) {
				logger.error("Error while encoding renderer value.", e);
				e.printStackTrace();
			}
		}
		
		boolean isEntity = genericModelType.isEntity();
		if (!isEntity && !genericModelType.isEnum())
			value = instance != null ? instance.toString() : "";
		else {
			if (isEntity) {
				String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) genericModelType, (GenericEntity) instance, modelMdResolver,
						useCase/* , null */);
				if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
					value = selectiveInformation;
			} else
				value = (instance != null ? instance.toString() : "");
			
			Pair<Name, Description> pair = GMEMetadataUtil.getNameAndDescription(instance, genericModelType, modelMdResolver, useCase);
			if (pair != null) {
				LocalizedString displayInfoName = pair.first() != null ? pair.first().getName() : null;
				LocalizedString displayInfoDescription = pair.second() != null ? pair.second().getDescription() : null;
				if (displayInfoDescription != null || displayInfoName != null)
					description = displayInfoDescription != null ? I18nTools.getLocalized(displayInfoDescription) : I18nTools.getLocalized(displayInfoName);
				else {
					if (isEntity)
						description = ((EntityType<?>) genericModelType).getShortName();
					else
						description = genericModelType.getTypeName();
				}
				
				if (value == null || value.isEmpty()) {
					if (displayInfoName != null)
						value = I18nTools.getLocalized(displayInfoName);
					else
						value = description;
				}
			} else {
				if (isEntity)
					description = ((EntityType<?>) genericModelType).getShortName();
				else
					description = genericModelType.getTypeName();
				
				if (value == null || value.isEmpty())
					value = description;
			}
		}
		
		value = SafeHtmlUtils.htmlEscape(value);
		if (useValueAsDescription && (description == null || description.isEmpty()))
			description = value;
		
		return new ValueDescriptionBean(value, description);
	}
	
	private static String getDescription(Object instance, GenericModelType genericModelType, String useCase, ModelMdResolver modelMdResolver) {
		Pair<Name, Description> pair = GMEMetadataUtil.getNameAndDescription(instance, genericModelType, modelMdResolver, useCase);
		if (pair != null && ((pair.getFirst() != null && pair.getFirst().getName() != null) || (pair.getSecond() != null && pair.getSecond().getDescription() != null))) {
			LocalizedString description = pair.getSecond() != null ? pair.getSecond().getDescription() : null;
			return description != null ? I18nTools.getLocalized(description) : I18nTools.getLocalized(pair.getFirst().getName());
		}

		return genericModelType.isEntity() ? ((EntityType<?>) genericModelType).getShortName() : genericModelType.getTypeName();
	}
	
	private static String prepareLabelForCollection(String propertyName, CollectionTreeModel collectionModel, boolean showLink, PropertyEntry propertyEntry) {
		int size = getCollectionModelSize(collectionModel, propertyEntry);
		StringBuilder builder = new StringBuilder();
		builder.append("<span class=\"").append(AssemblyPanelResources.INSTANCE.css().propertyNameStyle()).append(" ").append(GMEUtil.PROPERTY_NAME_CSS);
		if (showLink)
			builder.append(" ").append(AssemblyPanelResources.INSTANCE.css().linkStyle());
		builder.append("\">").append(propertyName).append("</span>");
		if (size != -1) {
			builder.append(" <span class=\"").append(AssemblyPanelResources.INSTANCE.css().propertyNameStyle()).append("\">(").append(size);
			if (collectionModel instanceof SetTreeModel) {
				int maxSize = ((SetTreeModel) collectionModel).getMaxSize();
				if (collectionModel.getElementType().isEntity() && ((size >= maxSize && maxSize > 0) || isIncomplete(collectionModel)))
					builder.append("<span class=\"").append(AssemblyPanelResources.INSTANCE.css().moreItemsInSetStyle()).append("\">...</span>");
			}
			
			builder.append(")</span>");
		}
		
		return builder.toString();
	}
	
	private static boolean isIncomplete(CollectionTreeModel collectionModel) {
		Object collection = collectionModel.getCollectionObject();
		return collection instanceof EnhancedCollection ? ((EnhancedCollection) collection).isIncomplete() : false;
	}
	
	/**
	 * Returns the CollectionTreeModel collection element size.
	 * @return - 1 if the collection is null.
	 */
	private static int getCollectionModelSize(CollectionTreeModel collectionModel, PropertyEntry propertyEntry) {
		Object collection = collectionModel.getCollectionObject();
		if (collection == null || GMEUtil.isPropertyAbsent(propertyEntry.getEntity(), propertyEntry.getProperty()))
			return -1;

		if (collection instanceof Map)
			return ((Map<?,?>) collection).size();
		
		int size = ((Collection<?>) collection).size();
		if (collectionModel instanceof SetTreeModel) {
			SetTreeModel setTreeModel = (SetTreeModel) collectionModel;
			int maxSize = setTreeModel.getMaxSize();
			if (size > maxSize && maxSize > 0)
				return maxSize;
		}
		return size;
	}
	
	private static Object getModelObjectFromModel(AbstractGenericTreeModel model) {
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		return delegateModel instanceof CondensedEntityTreeModel ? ((CondensedEntityTreeModel) delegateModel).getEntityTreeModel().getModelObject() : model.getModelObject();
	}
	
	/**
	 * Returns the icon for the given model.
	 */
	public ImageResource prepareIcon(AbstractGenericTreeModel model, boolean useEntityIcon, boolean usePropertyIcon) {
		if (model != null) {
			ImageResource icon = getIcon(model, useEntityIcon, usePropertyIcon);
			if (icon != null)
				return icon;
		}
		return AssemblyPanelResources.INSTANCE.clear();
	}
	
	protected ImageResource getIcon(AbstractGenericTreeModel model, boolean useEntityIcon, boolean usePropertyIcon) {
		ModelPath modelPath = new ModelPath();
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		GenericModelType type;
		if (model instanceof PropertyEntryTreeModel && ((PropertyEntryTreeModel) model).getPropertyEntry() != null) {
			PropertyEntry propertyEntry = ((PropertyEntryTreeModel) model).getPropertyEntry();
			Property property = propertyEntry.getProperty();
			modelPath.add(new PropertyPathElement(propertyEntry.getEntity(), property, property.get(propertyEntry.getEntity())));
		} else {
			if (delegateModel instanceof CollectionTreeModel && !(delegateModel instanceof CondensedEntityTreeModel))
				type = ((CollectionTreeModel) delegateModel).getCollectionType();
			else	
				type = model.getElementType();
			modelPath.add(new RootPathElement(type, model.getModelObject()));
		}
		IconAndType iconAndType = assemblyPanel.getIconProvider().apply(modelPath);
		if (iconAndType != null && ((useEntityIcon && iconAndType.isEntityIcon()) || (usePropertyIcon && !iconAndType.isEntityIcon()))) {
			return new GmImageResource(null, iconAndType.getIcon().getSafeUri().asString()) {
				@Override
				public int getHeight() {
					return 16;
				}
				
				@Override
				public int getWidth() {
					return 16;
				}
			};
			//return iconAndType.getIcon();
		}
		
		return null;
	}
	
	/**
	 * Called when a {@link ChangeValueManipulation} or an {@link AbsentingManipulation} is received.
	 */
	public static void onChangeManipulation(PropertyManipulation manipulation, Collection<AbstractGenericTreeModel> parentModels, AssemblyPanel assemblyPanel) {
		Object changedValue;
		
		LocalEntityProperty localEntityProperty = (LocalEntityProperty) manipulation.getOwner();
		String propertyName = localEntityProperty.getPropertyName();
		GenericEntity entity = localEntityProperty.getEntity();
		
		if (manipulation instanceof ChangeValueManipulation) {
			if (entity == null)
				changedValue = ((ChangeValueManipulation) manipulation).getNewValue();
			else
				changedValue = entity.entityType().getProperty(propertyName).get(entity);
		} else {
			changedValue = ((AbsentingManipulation) manipulation).getAbsenceInformation();
			propertyName = ((AbsentingManipulation) manipulation).getOwner().getPropertyName();
		}
		
		if (parentModels == null || parentModels.isEmpty())
			parentModels = assemblyPanel.getTreeGrid().getTreeStore().getRootItems();
		
		for (AbstractGenericTreeModel parentModel : parentModels)
			handleReplaceValue(assemblyPanel, parentModel, changedValue, propertyName, entity);
	}
	
	/**
	 * Called when an {@link AddManipulation} is received.
	 */
	public static void onInsertToCollectionManipulation(PropertyManipulation manipulation, Collection<AbstractGenericTreeModel> parentModels,
			AssemblyPanel assemblyPanel/*, boolean addToRootInCaseParentNotInTree*/) {
		List<Pair<Object, Object>> itemsToAdd = null;
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		if (manipulation instanceof AddManipulation)
			itemsToAdd = preparePairs(((AddManipulation)manipulation).getItemsToAdd());
		
		if (parentModels == null || parentModels.isEmpty()) {
			boolean addRootItems = true;
			if (!treeGrid.getTreeStore().getRootItems().isEmpty()) {
				for (AbstractGenericTreeModel model : treeGrid.getTreeStore().getRootItems()) {
					EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
					if (entityTreeModel != null) {
						addRootItems = false;
						break;
					}
				}
			}
			if (addRootItems)
				parentModels = treeGrid.getTreeStore().getRootItems();
		}
		
		for (AbstractGenericTreeModel parentModel : parentModels)
			handleInsertToCollectionManipulation(assemblyPanel, parentModel, itemsToAdd, true);
		/*if (addToRootInCaseParentNotInTree) {
			if (collection instanceof List) {
				ListTreeModel.insertNewItems(collectionType.getCollectionElementType(), itemsToAdd, modelFactory, treeGrid);
			} else if (collection instanceof Set) {
				SetTreeModel.insertNewItems(collectionType.getCollectionElementType(), itemsToAdd, modelFactory, treeGrid, ((Set<?>) collection).size());
			} else if (collection instanceof Map) {
				MapTreeModel.insertNewItems(collectionType.getParameterization(), itemsToAdd, modelFactory, treeGrid, ((Map<?,?>) collection).size());
			}
		}*/
	}
	
	private static List<Pair<Object, Object>> preparePairs(Map<Object, Object> itemsToAdd) {
		List<Pair<Object, Object>> pairs = null;
		if (itemsToAdd != null) {
			pairs = new ArrayList<>();
			for (Map.Entry<Object, Object> entry : itemsToAdd.entrySet())
				pairs.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		
		return pairs;
	}

	/**
	 * Called when a {@link RemoveManipulation} is received.
	 */
	public static void onRemoveFromCollectionManipulation(Manipulation manipulation, Set<AbstractGenericTreeModel> parentModels,
			TreeGrid<AbstractGenericTreeModel> treeGrid/*, boolean removeFromRoot, Object collection*/) {
		Set<Object> keysForRemove = null;
		if (manipulation instanceof RemoveManipulation && ((RemoveManipulation) manipulation).getItemsToRemove() != null)
			keysForRemove = ((RemoveManipulation)manipulation).getItemsToRemove().keySet();
		
		if (parentModels == null || parentModels.isEmpty()) {
			boolean addRootItems = true;
			if (!treeGrid.getTreeStore().getRootItems().isEmpty()) {
				for (AbstractGenericTreeModel model : treeGrid.getTreeStore().getRootItems()) {
					EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
					if (entityTreeModel != null) {
						addRootItems = false;
						break;
					}
				}
			}
			if (addRootItems)
				parentModels = new LinkedHashSet<AbstractGenericTreeModel>(treeGrid.getTreeStore().getRootItems());
		}
		for (AbstractGenericTreeModel parentModel : parentModels)
			handleRemoveFromCollectionManipulation(treeGrid, parentModel, keysForRemove);
		/*if (removeFromRoot) {
			if (collection instanceof List) {
				ListTreeModel.removeItems(keysForRemove, treeGrid);
			} else if (collection instanceof Set) {
				SetTreeModel.removeItems(keysForRemove, treeGrid);
			} else if (collection instanceof Map) {
				MapTreeModel.removeItems(keysForRemove, treeGrid);
			}
		}*/
	}
	
	/**
	 * Called when a {@link ClearCollectionManipulation} is received.
	 */
	public static void onClearCollectionManipulation(Object parentObject, Set<AbstractGenericTreeModel> parentModels,
			TreeGrid<AbstractGenericTreeModel> treeGrid) {
		if (parentModels == null || parentModels.isEmpty())
			parentModels = new LinkedHashSet<>(treeGrid.getTreeStore().getRootItems());
		
		parentModels.stream().filter(p -> p.refersTo(parentObject)).forEach(p -> handleClearCollectionManipulation(treeGrid, p));
	}
	
	/**
	 * Handle value replacement.
	 */
	private static void handleReplaceValue(final AssemblyPanel assemblyPanel, final AbstractGenericTreeModel parentModel, Object changedValue, String propertyName, Object ownerObject) {
		final TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		final AbstractGenericTreeModel selectedModel = treeGrid.getSelectionModel().getSelectedItem();
		Object newValue = changedValue;
		if (changedValue instanceof AbsenceInformation) {
			newValue = null;
			if (ownerObject instanceof GenericEntity) { 
				GenericEntity entity = (GenericEntity) ownerObject;
				entity.entityType().getProperty(propertyName).setAbsenceInformation(entity, (AbsenceInformation) changedValue);
			}
		}
		
		boolean refresh = false;
		if (propertyName == null) {
			/*treeGrid.getTreeStore().remove(parentModel);
			rootModels.remove(parentModel);
			models.remove(parentModel.getModelObject());
			setModel((GenericEntity)newValue);*/
		} else {
			AbstractGenericTreeModel parentDelegateModel = parentModel.getDelegate();
			if (parentDelegateModel instanceof CondensedEntityTreeModel && propertyName.equals(((CondensedEntityTreeModel) parentDelegateModel).getPropertyName())) {
				updatePropertyEntryTreeModel(assemblyPanel, parentModel, ((CondensedEntityTreeModel) parentDelegateModel).getPropertyDelegate(),
						newValue, changedValue instanceof AbsenceInformation, false, true);
				if (selectedModel == parentModel)
					refresh = true;
			} else {
				List<AbstractGenericTreeModel> children = parentModel.getChildren();
				EntityTreeModel entityTreeModel = parentModel.getEntityTreeModel();
				if (entityTreeModel != null)
					children = entityTreeModel.getChildren();
				else if (parentDelegateModel instanceof CondensedEntityTreeModel)
					children = ((CondensedEntityTreeModel) parentDelegateModel).getEntityTreeModel().getChildren();
				
				if (children != null && !children.isEmpty() && children.get(0) instanceof PropertyEntryModelInterface) {
					for (AbstractGenericTreeModel model : children) {
						PropertyEntryModelInterface propertyModel = (PropertyEntryModelInterface) model;
						AbstractGenericTreeModel delegateModel = propertyModel.getPropertyDelegate();
						String key = propertyModel.getPropertyEntry().getPropertyName();
						if (key.equals(propertyName)) {
							updatePropertyEntryTreeModel(assemblyPanel, (AbstractGenericTreeModel) propertyModel, delegateModel, newValue,
									changedValue instanceof AbsenceInformation, false, true);
							if (selectedModel == propertyModel)
								refresh = true;
							break;
						}
					}
				}
				
				entityTreeModel = parentDelegateModel.getEntityTreeModel();
				if (entityTreeModel != null) {
					boolean found = false;
					TreePropertyModel tpm = entityTreeModel.getTreePropertyModel(propertyName);
					if (tpm != null) {
						found = true;
						updateTreePropertyModel(assemblyPanel, parentModel, tpm, newValue, changedValue instanceof AbsenceInformation, true);
					}
					
					if (!found) {
						tpm = entityTreeModel.getHiddenTreePropertyModel(propertyName);
						if (tpm != null)
							updateTreePropertyModel(assemblyPanel, parentModel, tpm, newValue, changedValue instanceof AbsenceInformation, false);
					}
				}
			}
		}
		
		final boolean finalRefresh = refresh;
		final boolean updateModel = modelsWithUpdatePending.add(parentModel);
		Scheduler.get().scheduleDeferred(() -> {
			if (updateModel && treeGrid.getTreeStore().findModel(parentModel) != null) {
				treeGrid.getTreeStore().update(parentModel);
				//Do not focus the grid in case we have some active window
				if (treeGrid.getSelectionModel().isSelected(parentModel) && WindowManager.get().getActive() == null)
					treeGrid.getTreeView().focusRow(treeGrid.getStore().indexOf(parentModel));
				modelsWithUpdatePending.remove(parentModel);
			}
			
			if (finalRefresh) { //Since many operations depend on the selection (loading of absent properties, visibility, etc), we reselect if needed.
				if (replaceValueSelectionTimer == null) {
					replaceValueSelectionTimer = new Timer() {
						@Override
						public void run() {
							assemblyPanel.editorTreeGrid.selectionModel.handleSelectionChanged(Collections.singletonList(selectedModel), true);
						}
					};
				}
				
				replaceValueSelectionTimer.schedule(100);
			}
		});
	}
	
	private static void handleInsertToCollectionManipulation(AssemblyPanel ap, AbstractGenericTreeModel parentModel,
			List<Pair<Object, Object>> items, boolean addToRootInCaseParentNotInTree) {
		AbstractGenericTreeModel parentDelegate = parentModel.getDelegate();
		TreeGrid<AbstractGenericTreeModel> treeGrid = ap.getTreeGrid();
		if (!(parentDelegate instanceof CollectionTreeModel))
			return;
		
		if (((CollectionTreeModel) parentDelegate).getNotCompleted())
			treeGrid.setExpanded(parentModel, true);
		else {
			((CollectionTreeModel) parentDelegate).insertNewItems(items, parentModel, ap, treeGrid.getSelectionModel().getSelectedItem(),
					addToRootInCaseParentNotInTree);
		}
		
		refreshRow(treeGrid, parentModel);
	}
	
	private static void handleRemoveFromCollectionManipulation(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel parentModel, Set<Object> itemsKeyToRemove) {
		AbstractGenericTreeModel parentDelegate = parentModel.getDelegate();
		if (parentDelegate instanceof CollectionTreeModel) {
			((CollectionTreeModel) parentDelegate).removeItems(itemsKeyToRemove, parentModel, treeGrid, treeGrid.getSelectionModel().getSelectedItem());
			refreshRow(treeGrid, parentModel);
		}
	}
	
	private static void handleClearCollectionManipulation(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel parentModel) {
		AbstractGenericTreeModel parentDelegate = parentModel.getDelegate();
		if (parentModel instanceof PropertyEntryModelInterface)
			parentDelegate = ((PropertyEntryModelInterface) parentModel).getPropertyDelegate();
		else
			parentDelegate = parentModel.getDelegate();
		
		if (parentDelegate instanceof CondensedEntityTreeModel)
			parentDelegate = ((CondensedEntityTreeModel) parentDelegate).getPropertyDelegate();
		if (parentDelegate.getChildren() != null) {
			TreeStore<AbstractGenericTreeModel> treeStore = treeGrid.getTreeStore();
			parentDelegate.getChildren().forEach(m -> treeStore.remove(m));
		}
		parentDelegate.clear();
		
		if (parentDelegate instanceof CollectionTreeModel)
			((CollectionTreeModel) parentDelegate).clearItems();
		refreshRow(treeGrid, parentModel);
	}
	
	/**
	 * Refreshes the row in the {@link TreeGrid} containing the given model, by calling onUpdate on the grid selection model.
	 * Notice that this will NOT repaint the row.
	 */
	public static void refreshNode(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel model) {
		Scheduler.get().scheduleFinally(() -> updateRow(treeGrid.getSelectionModel(), model));
	}
	
	private native static void updateRow(GridSelectionModel<AbstractGenericTreeModel> selectionModel, AbstractGenericTreeModel model) /*-{
		selectionModel.@com.sencha.gxt.widget.core.client.grid.GridSelectionModel::onUpdate(*)(model);
	}-*/;
	
	/**
	 * Refreshes the row in the {@link TreeGrid} containing the given model, by calling refreshRow on the grid view.
	 * Notice that this will repaint the row.
	 */
	public static void refreshRow(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel model) {
		refreshRow(treeGrid.getView(), treeGrid.getStore().indexOf(model));
	}
	
	private native static void refreshRow(GridView<AbstractGenericTreeModel> view, int row) /*-{
		view.@com.sencha.gxt.widget.core.client.grid.GridView::refreshRow(I)(row);
	}-*/;
	
	/**
	 * Inserts a new model to the given {@link TreeGrid}.
	 * @param parentModel - parent model where the model will be added to.
	 * @param model - model to be added.
	 * @param index - index where the model will be inserted.
	 * @param select - true for selecting and expanding the parentModel.
	 * @param addToRootInCaseParentNotInTree - true for adding the model to the root in case the parent is not in the tree.
	 */
	public static void insertToTreeStore(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parentModel, AbstractGenericTreeModel model, int index,
			boolean select, boolean addToRootInCaseParentNotInTree) {
		insertToTreeStore(assemblyPanel, parentModel, model, index, select, addToRootInCaseParentNotInTree, true);
	}
	
	/**
	 * Inserts a new model to the given {@link TreeGrid}.
	 * @param parentModel - parent model where the model will be added to.
	 * @param model - model to be added.
	 * @param index - index where the model will be inserted.
	 * @param select - true for selecting and expanding the parentModel.
	 * @param addToRootInCaseParentNotInTree - true for adding the model to the root in case the parent is not in the tree.
	 * @param updateChildrenAndFocus - true for updating the children entry Number and focus the row in the grid.
	 */
	public static void insertToTreeStore(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parentModel, AbstractGenericTreeModel model, int index,
			boolean select, boolean addToRootInCaseParentNotInTree, boolean updateChildrenAndFocus) {
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		TreeStore<AbstractGenericTreeModel> treeStore = treeGrid.getTreeStore();
		boolean parentModelIsInTree = model != null && isParentModelInTree(parentModel, treeStore);
		if (index != -1) {
			if (parentModelIsInTree)
				insertToTreeWithChildren(assemblyPanel, parentModel, index, model);
			else if (addToRootInCaseParentNotInTree)
				insertToTreeWithChildren(assemblyPanel, null, index, model);
			
			if (updateChildrenAndFocus)
				updateChildren(treeStore, parentModel, index + 1);
		} else {
			if (parentModelIsInTree)
				addToTreeWithChildren(assemblyPanel, parentModel, model);
			else if (addToRootInCaseParentNotInTree)
				addToTreeWithChildren(assemblyPanel, null, model);
		}
		
		if (select)
			expandAndSelect(treeGrid, parentModel, model, parentModelIsInTree);
		
		//Do not focus the grid in case we have some active window
		if (updateChildrenAndFocus  && WindowManager.get().getActive() == null)
			treeGrid.getView().focusRow(treeGrid.getStore().indexOf(model));
	}
	
	private static void insertToTreeWithChildren(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parent,
			int index, AbstractGenericTreeModel model) {
		TreeStore<AbstractGenericTreeModel> treeStore = assemblyPanel.getTreeGrid().getTreeStore();
		
		if (parent != null)
			treeStore.insert(parent, index, model);
		else
			treeStore.insert(index, model);
		
		addChildrenToTree(assemblyPanel, model);
	}
	
	private static void addChildrenToTree(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parent) {
		TreeStore<AbstractGenericTreeModel> treeStore = assemblyPanel.getTreeGrid().getTreeStore();
		
		List<AbstractGenericTreeModel> children = parent.getChildren();
		if (children != null) {
			treeStore.add(parent, children);
			
			for (AbstractGenericTreeModel child : children) {
				addChildrenToTree(assemblyPanel, child);
				addDragAndDropListenerToElementModel(child, assemblyPanel);
			}
		}
	}
	
	public static void addToTreeWithChildren(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parent, AbstractGenericTreeModel model) {
		TreeStore<AbstractGenericTreeModel> treeStore = assemblyPanel.getTreeGrid().getTreeStore();
		
		if (parent != null)
			treeStore.add(parent, model);
		else
			treeStore.add(model);
		
		addChildrenToTree(assemblyPanel, model);
		
		addDragAndDropListenerToElementModel(model, assemblyPanel);
	}
	
	public static void addToTreeWithChildren(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parent,
			List<AbstractGenericTreeModel> children) {
		TreeStore<AbstractGenericTreeModel> treeStore = assemblyPanel.getTreeGrid().getTreeStore();
		
		if (parent != null) {
			treeStore.add(parent, children);
			addDragAndDropListenerToElementModel(parent, assemblyPanel);
		} else
			treeStore.add(children);

		for (AbstractGenericTreeModel child : children) {
			addChildrenToTree(assemblyPanel, child);
			addDragAndDropListenerToElementModel(child, assemblyPanel);
		}
	}
	
	private static boolean isParentModelInTree(AbstractGenericTreeModel parentModel, TreeStore<AbstractGenericTreeModel> treeStore) {
		return parentModel != null && treeStore.getAll().contains(parentModel);
	}
	
	/**
	 * Removes a model from the given {@link TreeGrid}.
	 * @param treeGrid - {@link TreeGrid} used for performing the operation.
	 * @param parentModel - parent model where the model will be removed.
	 * @param index - index of the model to be removed.
	 * @param updateChildren - true for updating the view of the other children of the parentModel.
	 */
	public static void removeFromTreeStore(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel parentModel, int index,
			boolean updateChildren) {
		TreeStore<AbstractGenericTreeModel> treeStore = treeGrid.getTreeStore();
		boolean parentModelIsInTree = isParentModelInTree(parentModel, treeStore);
		if (parentModelIsInTree)
			treeStore.remove(treeStore.getChildren(parentModel).get(index));
		else
			treeStore.remove(treeStore.getChild(index));
		
		if (updateChildren)
			updateChildren(treeStore, parentModel, index);
	}
	
	/**
	 * Replaces a model for another one in the given parent, for the given GMEditorTreeGrid.
	 * @param parentModel - parent model where the model will be replaced.
	 * @param oldModel - model to be removed.
	 * @param newModel - model to be added.
	 * @param index - index of the model to be replaced.
	 * @param select - true for selecting and expanding the parentModel.
	 */
	public static void replaceInTreeStore(AssemblyPanel assemblyPanel, AbstractGenericTreeModel parentModel, AbstractGenericTreeModel oldModel,
			AbstractGenericTreeModel newModel, int index, boolean select) {
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		TreeStore<AbstractGenericTreeModel> treeStore = treeGrid.getTreeStore();
		if (index != -1) {
			treeStore.remove(treeStore.getChildren(parentModel).get(index));
			insertToTreeWithChildren(assemblyPanel, parentModel, index, newModel);
		} else {
			treeStore.remove(oldModel);
			//treeStore.remove(parentModel, oldModel);
			addToTreeWithChildren(assemblyPanel, parentModel, newModel);
		}
		
		if (select) {
			if (index == -1)
				updateChildren(treeStore, parentModel, 0);
			expandAndSelect(treeGrid, parentModel, newModel, true);
		}
	}
	
	private static void updateChildren(TreeStore<AbstractGenericTreeModel> treeStore, AbstractGenericTreeModel model, int index) {
		List<AbstractGenericTreeModel> children = null;
		if (model != null)
			children = model.getChildren();
		else
			children = treeStore.getRootItems();
		
		if (children == null || children.size() <= index)
			return;
		
		for (int i = index; i < children.size(); i++) {
			AbstractGenericTreeModel child = children.get(i);
			if (child instanceof MapKeyAndValueTreeModel)
				((MapKeyAndValueTreeModel) child).setEntryNumber(i);
			else if (child instanceof ListEntryTreeModel)
				((ListEntryTreeModel) child).setListEntryIndex(i);
			else if (child instanceof SetEntryTreeModel)
				((SetEntryTreeModel) child).getSetEntry().setIndex(i);
			
			treeStore.update(child);
		}
	}
	
	private static void expandAndSelect(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel parentModel,
			AbstractGenericTreeModel model, boolean parentModelIsInTree) {
		if (parentModelIsInTree && !treeGrid.isExpanded(parentModel))
			treeGrid.setExpanded(parentModel, true);
		treeGrid.getSelectionModel().select(model, false);
	}
	
	/**
	 * Updates the given property model.
	 */
	public static void updatePropertyEntryTreeModel(AssemblyPanel assemblyPanel, AbstractGenericTreeModel model, AbstractGenericTreeModel delegateModel,
			Object newValue, boolean setAsAbsent, boolean updatesNodesOnlyForSet, boolean handleBaseType) {
		updatePropertyEntryTreeModel(assemblyPanel, model, delegateModel, newValue, setAsAbsent, updatesNodesOnlyForSet, handleBaseType, false);
	}
	
	/**
	 * Updates the given property model.
	 * @param restoreDepth - true for restoring the depth in order to complete building the models.
	 */
	public static void updatePropertyEntryTreeModel(AssemblyPanel assemblyPanel, AbstractGenericTreeModel model, AbstractGenericTreeModel delegateModel,
			Object newValue, boolean setAsAbsent, boolean updatesNodesOnlyForSet, boolean handleBaseType, boolean restoreDepth) {
		model.setLabel(null);
		AssemblyPanelTreeGrid treeGrid = assemblyPanel.getTreeGrid();
		boolean isExpanded = treeGrid.isExpanded(model);
		boolean depthChecker = isExpanded || (delegateModel instanceof CondensedEntityTreeModel && (isNotComplete(model)));
		TreeStore<AbstractGenericTreeModel> treeStore = treeGrid.getTreeStore();
		boolean presentInStore = treeStore.findModel(model) != null;
		if (presentInStore)
			treeStore.removeChildren(model);
		
		if (model instanceof PropertyEntryModelInterface)
			((PropertyEntryModelInterface) model).getPropertyEntry().setAbsent(setAsAbsent);
		
		if (updatesNodesOnlyForSet)
			((SetTreeModel) delegateModel).updateNodesOnly((Set<?>) newValue);
		else {
			if (model instanceof PropertyEntryModelInterface && handleBaseType && ((PropertyEntryModelInterface) model).getPropertyEntry().isBasedType()) {
				GenericModelType propertyType = GMF.getTypeReflection().getBaseType();
				if (newValue != null) {
					propertyType = ((BaseType) propertyType).getActualType(newValue);
					if (propertyType == null)
						propertyType = GMF.getTypeReflection().getBaseType();
				}
				
				if (model instanceof PropertyEntryTreeModel) {
					ObjectAndType objectAndType = new ObjectAndType();
					objectAndType.setObject(newValue);
					objectAndType.setType(propertyType);
					objectAndType.setDepth(((PropertyEntryModelInterface) model).getPropertyEntry().getDepth());
					objectAndType.setMapAsList(((PropertyEntryModelInterface) model).getPropertyEntry().getMapAsList());
					delegateModel = assemblyPanel.getModelFactory().apply(objectAndType);
					((PropertyEntryTreeModel) model).setDelegate(delegateModel);
					AbstractGenericTreeModel parentInStore = assemblyPanel.getTreeGrid().getTreeStore().getParent(model);
					delegateModel.setParent(parentInStore);
				}
			} else {
				delegateModel.setModelObject(newValue, depthChecker || restoreDepth? 0 : ModelFactory.MAX_DEPTH);
				if (delegateModel instanceof EntityTreeModel) {
					String condensedPropertyName = ((EntityTreeModel) delegateModel).getCondensedPropertyAfterUpdate();
					if (condensedPropertyName != null)
						Scheduler.get().scheduleDeferred(() -> assemblyPanel.condenseSingleEntity(model, condensedPropertyName));
				}
			}
		}
		
		if (presentInStore && delegateModel.getChildren() != null)
			delegateModel.getChildren().forEach(child -> addToTreeWithChildren(assemblyPanel, model, child));
		
		if (presentInStore && modelsWithUpdatePending.add(model)) {
			new Timer() {
				@Override
				public void run() {
					if (treeGrid.getTreeStore().findModel(model) != null) {
						treeStore.update(model);
						//Do not focus the grid in case we have some active window
						if (treeGrid.getSelectionModel().isSelected(model) && WindowManager.get().getActive() == null)
							treeGrid.getTreeView().focusRow(treeGrid.getStore().indexOf(model));
					}
					modelsWithUpdatePending.remove(model);
					
					if (isExpanded && !treeGrid.isExpanded(model) && treeGrid.getAssemblyPanelTreeStore().isWrapperExisting(model)) 
						treeGrid.setExpanded(model, true);
				}
			}.schedule(100);
		}
		assemblyPanel.maybeShowContextMenuOnLoadingAbsent();
	}
	
	protected static void addDragAndDropListenerToElementIndex(int rowIndex, AssemblyPanel assemblyPanel) {
		addDragAndDropListenerToElement(null, rowIndex, assemblyPanel);
	}
	
	protected static void addDragAndDropListenerToElementModel(AbstractGenericTreeModel model, AssemblyPanel assemblyPanel) {
		addDragAndDropListenerToElement(model, -1, assemblyPanel);
	}
	
	private static void addDragAndDropListenerToElement(AbstractGenericTreeModel model, int index, AssemblyPanel assemblyPanel) {
		if (assemblyPanel.gmeDragAndDropSupport == null)
			return;
		
		Scheduler.get().scheduleDeferred(() -> {
			AbstractGenericTreeModel theModel;
			int rowIndex;
			if (model == null) {
				rowIndex = index;
				theModel = assemblyPanel.getTreeGrid().getStore().get(rowIndex);
			} else {
				theModel = model;
				rowIndex = assemblyPanel.getTreeGrid().getStore().indexOf(model);
			}
			
			if (rowIndex < 0) {
				assemblyPanel.addModelToWaitingDropListener(theModel);
				return;
			}
			
			Element rowElement = assemblyPanel.getTreeGrid().getTreeView().getRow(rowIndex);
			assemblyPanel.prepareDropTarget(rowElement, -1);
		});
	}
	
	protected static boolean isNotComplete(AbstractGenericTreeModel model) {
		if (model == null)
			return false;
		
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (entityTreeModel != null) {
			if (entityTreeModel.getNotCompleted())
				return true;
			if (delegateModel instanceof CondensedEntityTreeModel && ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate() != null &&
					((CondensedEntityTreeModel) delegateModel).getPropertyDelegate().getNotCompleted()) {
				return true;
			}
		} else if (delegateModel.getNotCompleted())
			return true;
		
		return false;
	}
	
	private static void updateTreePropertyModel(AssemblyPanel assemblyPanel, AbstractGenericTreeModel model, TreePropertyModel treePropertyModel,
			Object newValue, boolean setAsAbsent, boolean isVisible) {
		treePropertyModel.setAbsent(setAsAbsent);
		treePropertyModel.setValue(newValue);
		if (isVisible)
			refreshNode(assemblyPanel.getTreeGrid(), model);
	}
	
	/**
	 * Returns the first EntityTreeModel parent of the given model, or null.
	 */
	public static EntityTreeModel getParentEntityTreeModel(AbstractGenericTreeModel model) {
		EntityTreeModel entityTreeModel = model != null ? model.getEntityTreeModel() : null;
		if (entityTreeModel != null)
			return entityTreeModel;
		
		return model == null ? null : getParentEntityTreeModel(model.getParent());
	}
	
	/**
	 * Returns the DisplayInfo or short name of the entityType which is element of the given collection
	 * @return the entityInfo, or null, if the given collection does not contains entityTypes.
	 */
	public static String getCollectionEntityInfo(AbstractGenericTreeModel collectionTreeModel, ModelMdResolver modelMdResolver, String useCase) {
		GenericModelType modelType = (collectionTreeModel instanceof CondensedEntityTreeModel ?
				((CondensedEntityTreeModel) collectionTreeModel).getPropertyDelegate().getElementType() : collectionTreeModel.getElementType());
		if (modelType.isEntity())
			return GMEMetadataUtil.getEntityNameMDOrShortName((EntityType<?>) modelType, modelMdResolver, useCase);
		return null;
	}
	
	/**
	 * Returns the property name of the given model in its parent EntityModelInterface, or null if the given model has no parent.
	 */
	public static String getPropertyName(AbstractGenericTreeModel model, ModelPath modelPath) {
		AbstractGenericTreeModel parent = model != null ? (AbstractGenericTreeModel) model.getParent() : null;
		if (parent == null) {
			if (model instanceof CondensedEntityTreeModel)
				return model.getPropertyName();
			
			if (modelPath != null) {
				for (int i = modelPath.size() - 1; i >= 0; i--) {
					ModelPathElement pathElement = modelPath.get(i);
					if (model == null || pathElement.getValue() == model.getModelObject() || (model instanceof SetTreeModel && ((SetTreeModel) model).isRoot()))
						return ((PropertyPathElement) pathElement).getProperty().getName();
				}
			}
			
			return null;
		}
		
		EntityTreeModel entityTreeModel = parent.getEntityTreeModel();
		if (entityTreeModel != null)
			return model.getPropertyName();
		
		return getPropertyName(model.getParent().getDelegate(), modelPath);
	}
	
	protected static ModelPath getModelPath(AbstractGenericTreeModel model, ModelPath rootPath) {
		if (rootPath == null) {
			ModelPath modelPath = new ModelPath();
			prepareModelPathElement(0, model, modelPath);
			
			/*GenericModelType type;
			if (model.getDelegate() instanceof CollectionTreeModel)
				type = ((CollectionTreeModel) model.getDelegate()).getCollectionType();
			else
				type = model.getElementType();
			modelPath.add(new RootPathElement(type, model.getModelObject()));*/
			return modelPath;
		}
		
		if (model != null && rootPath.last().getValue() == model.getModelObject())
			return rootPath;
		
		ModelPath modelPath = new ModelPath();
		rootPath.forEach(element -> modelPath.add(element.copy()));
		if (model != null)
			prepareModelPathElement(modelPath.size(), model, modelPath);
		return modelPath;
	}
	
	protected static List<ModelPath> getAmbiguousModelPath(AbstractGenericTreeModel model, ModelPath rootPath) {
		List<ModelPath> modelPaths = new ArrayList<>();
		if (model == null)
			return modelPaths;
		
		ModelPath modelPath = getModelPath(model, rootPath);
		modelPaths.add(modelPath);
		
		if (model.getDelegate() instanceof CondensedEntityTreeModel) {
			AbstractGenericTreeModel ambiguousModel = ((CondensedEntityTreeModel) model.getDelegate()).getPropertyDelegate();
			if (ambiguousModel != null) {
				ModelPath ambiguousModelPath = getModelPath(ambiguousModel, rootPath);
				modelPaths.add(ambiguousModelPath);
			}
		}
		
		return modelPaths;
	}
	
	private static void prepareModelPathElement(int index, AbstractGenericTreeModel model, ModelPath path) {
		if (!path.isEmpty() && index != 0 && path.get(index - 1).getValue() == model.getModelObject())
			return;
		
		AbstractGenericTreeModel delegateModel = model == null ? null : model.getDelegate();
		if (model instanceof ListEntryTreeModel) {
			ModelPathElement modelPathElement = prepareListItemPathElement((ListEntryTreeModel) model, model.getParent(), path);
			if (modelPathElement != null)
				path.add(index, modelPathElement);
			else
				prepareModelPathElement(index, delegateModel, path);
		} else if (model instanceof SetEntryTreeModel) {
			SetItemPathElement setItemPathElement = prepareSetItemPathElement((SetEntryTreeModel) model, model.getParent(), path);
			if (setItemPathElement != null)
				path.add(index, setItemPathElement);
			else
				prepareModelPathElement(index, delegateModel, path);
		} else if (model instanceof MapKeyOrValueEntryTreeModel) {
			MapKeyOrValueEntryTreeModel mapKeyOrValueEntryTreeModel = (MapKeyOrValueEntryTreeModel) model;
			if (mapKeyOrValueEntryTreeModel.isKey())
				path.add(index, prepareMapKeyPathElement((MapKeyOrValueEntryTreeModel) model, model.getParent(), path));
			else
				path.add(index, prepareMapValuePathElement((MapKeyAndValueTreeModel) model.getParent(), path));
		} else if (model instanceof MapKeyAndValueTreeModel) {
			MapKeyAndValueTreeModel mapKeyAndValueTreeModel = (MapKeyAndValueTreeModel) model;
			path.add(index, prepareMapValuePathElement(mapKeyAndValueTreeModel, path));
		} else if (model != null && delegateModel instanceof CondensedEntityTreeModel) {
			/*if (delegateModel.getModelObject() != null && model instanceof PropertyEntryModelInterface &&
					(path.isEmpty() || path.get(index).getValue() != ((CondensedEntityTreeModel) delegateModel).getCollectionObject()) &&
					((CondensedEntityTreeModel) delegateModel).getCollectionObject() != null) {
				path.add(index, preparePropertyPathElement((PropertyEntryModelInterface) delegateModel));
				if (model instanceof CondensedEntityTreeModel)
					prepareModelPathElement(index, ((CondensedEntityTreeModel) model).getEntityTreeModel(), path);
				else if (model instanceof PropertyEntryModelInterface)
					path.add(index, preparePropertyPathElement((PropertyEntryModelInterface) model));
			} else {*/
			if (model instanceof CondensedEntityTreeModel)
				prepareModelPathElement(index, ((CondensedEntityTreeModel) model).getEntityTreeModel(), path);
			else if (model instanceof PropertyEntryModelInterface)
				path.add(index, preparePropertyPathElement((PropertyEntryModelInterface) model));
			//}
				//path.add(index, new RootPathElement(model.getElementType(), model.getModelObject()));
			
			//if (model.getModelObject() != null) {
				//prepareModelPathElement(index + 1, ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate(), path);
			//}
		} else if (model != null && delegateModel instanceof CollectionTreeModel) {
			PropertyPathElement collectionPathElement = prepareCollectionPathElement((CollectionTreeModel) delegateModel, path);
			if (collectionPathElement != null)
				path.add(index, collectionPathElement);
			/*else if (delegateModel instanceof CondensedEntityTreeModel) {//absent condensed
				if (model instanceof PropertyEntryModelInterface && (path.isEmpty() ||
						path.get(index).getValue() != ((CondensedEntityTreeModel) delegateModel).getCollectionObject()))
					path.add(index, preparePropertyPathElement((PropertyEntryModelInterface) model));
				else
					path.add(index, new RootPathElement(model.getElementType(), model.getModelObject()));
			}*/
		} else if (model instanceof PropertyEntryModelInterface)
			path.add(index, preparePropertyPathElement((PropertyEntryModelInterface) model));
		else if (model instanceof EntityTreeModel)
			path.add(index, new RootPathElement(model.getElementType(), model.getModelObject()));
		else if (model instanceof ValueTreeModel)
			path.add(index, new RootPathElement(model.getElementType(), model.getModelObject()));
		
		if (model != null && model.getParent() != null && !(index < path.size() && path.get(index) instanceof RootPathElement))
			prepareModelPathElement(index, model.getParent(), path);
	}
	
	/**
	 * Prepares a {@link PropertyPathElement} for the given {@link CollectionTreeModel}.
	 */
	public static PropertyPathElement prepareCollectionPathElement(final CollectionTreeModel collectionTreeModel, ModelPath modelPath) {
		GenericEntity parentEntity = getParentEntity(collectionTreeModel, modelPath);
		
		if (parentEntity != null) { //This may be null in case of absent condensed
			String propertyName = AssemblyUtil.getPropertyName(collectionTreeModel, modelPath);
			if (propertyName != null) {
				return new PropertyPathElement(parentEntity, parentEntity.entityType().getProperty(propertyName),
						collectionTreeModel.getCollectionObject());
			}
		}
		return null;
	}
	
	/**
	 * Prepares a {@link PropertyPathElement} from the given {@link PropertyEntryModelInterface}.
	 */
	public static PropertyPathElement preparePropertyPathElement(final PropertyEntryModelInterface propertyEntryModelInteface) {
		return new PropertyPathElement(propertyEntryModelInteface.getPropertyEntry().getEntity(),
				propertyEntryModelInteface.getPropertyEntry().getEntityType().getProperty(propertyEntryModelInteface.getPropertyEntry().getPropertyName()),
				propertyEntryModelInteface.getPropertyDelegate() != null ? propertyEntryModelInteface.getPropertyDelegate().getModelObject() : null);
	}
	
	private static GenericEntity getParentEntity(AbstractGenericTreeModel model, ModelPath modelPath) {
		EntityTreeModel entityTreeModel = AssemblyUtil.getParentEntityTreeModel(model);
		if (entityTreeModel != null)
			return entityTreeModel.getModelObject();
		
		for (int i = modelPath.size() - 1; i >= 0; i--) {
			ModelPathElement pathElement = modelPath.get(i);
			if (model == null || pathElement.getValue() == model.getModelObject() || (model instanceof SetTreeModel && ((SetTreeModel) model).isRoot())
					|| (model instanceof MapKeyAndValueTreeModel && checkMapModel((MapKeyAndValueTreeModel) model, pathElement.getValue()))) {
				return pathElement instanceof PropertyPathElement ? ((PropertyPathElement) pathElement).getEntity() : null;
			}
		}
		
		return null;
	}
	
	private static boolean checkMapModel(MapKeyAndValueTreeModel mapKeyAndValueModel, Object value) {
		if (mapKeyAndValueModel.getParent() instanceof MapTreeModel)
			return value == ((MapTreeModel) mapKeyAndValueModel.getParent()).getModelObject();
		
		return false;
	}
	
	/**
	 * Prepares a {@link ListItemPathElement} (or {@link SetItemPathElement}) from the given {@link ListEntryTreeModel} and its parent tree model.
	 */
	public static ModelPathElement prepareListItemPathElement(ListEntryTreeModel listEntryTreeModel, AbstractGenericTreeModel parentTreeModel,
			ModelPath modelPath) {
		GenericEntity parentEntity = getParentEntity(parentTreeModel, modelPath);
		
		if (parentEntity != null) {
			return new ListItemPathElement(parentEntity, parentEntity.entityType().getProperty(AssemblyUtil.getPropertyName(parentTreeModel, modelPath)),
					listEntryTreeModel.getListEntryIndex(), listEntryTreeModel.getElementType(), listEntryTreeModel.getModelObject());
		}
		
		if (modelPath != null && !modelPath.isEmpty() && ((CollectionType) modelPath.last().getType()).getCollectionKind().equals(CollectionKind.set)) {
			 //This is a "fake" list item (query results transformed into list)
			parentEntity = ((PropertyPathElement) modelPath.last()).getEntity();
			
			return new SetItemPathElement(parentEntity, ((PropertyPathElement) modelPath.last()).getProperty(), listEntryTreeModel.getElementType(),
					listEntryTreeModel.getModelObject());
		}
		
		return null;
	}
	
	/**
	 * Prepares a {@link SetItemPathElement} from the given {@link SetEntryTreeModel} and its parent tree model.
	 */
	public static SetItemPathElement prepareSetItemPathElement(final SetEntryTreeModel setEntryTreeModel, AbstractGenericTreeModel parentTreeModel,
			ModelPath modelPath) {
		GenericEntity parentEntity = getParentEntity(parentTreeModel, modelPath);
		if (parentEntity == null)
			return null;
		
		return new SetItemPathElement(parentEntity, parentEntity.entityType().
				getProperty(AssemblyUtil.getPropertyName(parentTreeModel, modelPath)),
				setEntryTreeModel.getElementType(), setEntryTreeModel.getModelObject());
	}
	
	/**
	 * Prepares a {@link MapKeyPathElement} from the given {@link MapKeyOrValueEntryTreeModel} and its parent tree model.
	 */
	public static MapKeyPathElement prepareMapKeyPathElement(final MapKeyOrValueEntryTreeModel mapKeyValueTreeModel, AbstractGenericTreeModel parentTreeModel,
			ModelPath modelPath) {
		GenericEntity parentEntity = getParentEntity(parentTreeModel, modelPath);
		
		MapKeyAndValueTreeModel mapKeyAndValueTreeModel = (MapKeyAndValueTreeModel) mapKeyValueTreeModel.getParent();
		
		return new MapKeyPathElement(parentEntity, parentEntity.entityType().
				getProperty(AssemblyUtil.getPropertyName(parentTreeModel, modelPath)),
				mapKeyValueTreeModel.getElementType(), mapKeyValueTreeModel.getModelObject(),
				mapKeyAndValueTreeModel.getElementType(), mapKeyAndValueTreeModel.getMapValueEntryTreeModel().getModelObject());
	}
	
	/**
	 * Prepares a {@link MapValuePathElement} from the given {@link MapKeyAndValueTreeModel} and its parent tree model.
	 */
	public static MapValuePathElement prepareMapValuePathElement(MapKeyAndValueTreeModel mapKeyAndValueTreeModel, ModelPath modelPath) {
		GenericEntity parentEntity = getParentEntity(mapKeyAndValueTreeModel, modelPath);
		
		return new MapValuePathElement(parentEntity, parentEntity.entityType().
				getProperty(AssemblyUtil.getPropertyName(mapKeyAndValueTreeModel, modelPath)),
				mapKeyAndValueTreeModel.getMapKeyEntryTreeModel().getElementType(), mapKeyAndValueTreeModel.getMapKeyEntryTreeModel().getModelObject(),
				mapKeyAndValueTreeModel.getMapValueEntryTreeModel().getElementType(), mapKeyAndValueTreeModel.getMapValueEntryTreeModel().getModelObject(),
				prepareMapKeyPathElement(mapKeyAndValueTreeModel.getMapKeyEntryTreeModel(), mapKeyAndValueTreeModel, modelPath));
	}
	
	private static boolean isInline(EntityType<?> propertyEntityType, ModelMdResolver metaDataResolver, ModelFactory modelFactory) {
		boolean isSimpleOrSimplified = false;
		Set<Class<?>> simplifiedEntityTypes = modelFactory.getSimplifiedEntityTypes();
		if (simplifiedEntityTypes != null && simplifiedEntityTypes.contains(propertyEntityType.getJavaType()))
			isSimpleOrSimplified = true;

		if (!modelFactory.isIgnoreInlineMetadata())
			isSimpleOrSimplified = metaDataResolver.entityType(propertyEntityType).is(Inline.T);
		
		return isSimpleOrSimplified;
	}
	
	public static boolean isInline(Property property, GenericEntity parentEntity, EntityType<?> parentEntityType, ModelMdResolver modelMdResolver, ModelFactory modelFactory) {
		if (modelFactory.isIgnoreInlineMetadata())
			return false;
		
		boolean isSimpleOrSimplified = false;
		GenericModelType propertyType = property.getType();
		
		if (parentEntity != null && propertyType.isBase()) {
			BaseType baseType = (BaseType) propertyType;
			Object propertyValue = property.get(parentEntity);
			if (propertyValue != null)
				propertyType = baseType.getActualType(propertyValue);
			if (propertyType == null)
				propertyType = property.getType();
		}
		
		if (propertyType.isEntity())
			isSimpleOrSimplified = isInline((EntityType<?>) propertyType, modelMdResolver, modelFactory);
		
		if (isSimpleOrSimplified)
			return isSimpleOrSimplified;
		
		EntityMdResolver entityMdResolver;
		if (parentEntity != null)
			entityMdResolver = modelMdResolver.entity(parentEntity);
		else
			entityMdResolver = modelMdResolver.entityType(parentEntityType);
		
		isSimpleOrSimplified = entityMdResolver.property(property).is(Inline.T);
		
		return isSimpleOrSimplified;
	}
	
	/**
	 * Checks for the existence of the {@link Outline} metadata.
	 * There are useCases where this is required.
	 */
	public static boolean hasOutline(Property property, GenericEntity parentEntity, EntityType<?> parentEntityType, ModelMdResolver modelMdResolver, ModelFactory modelFactory) {
		if (modelFactory.isIgnoreInlineMetadata())
			return false;
		
		GenericModelType propertyType = property.getType();
		
		if (parentEntity != null && propertyType.isBase()) {
			BaseType baseType = (BaseType) propertyType;
			Object propertyValue = property.get(parentEntity);
			if (propertyValue != null)
				propertyType = baseType.getActualType(propertyValue);
			if (propertyType == null)
				propertyType = property.getType();
		}
		
		boolean hasOutline = false;
		
		if (propertyType.isEntity()) {
			Outline outline = modelMdResolver.entityType((EntityType<?>) propertyType).meta(Outline.T).exclusive();
			if (outline != null)
				hasOutline = !outline.isTrue();
		}
		
		if (hasOutline)
			return hasOutline;
		
		EntityMdResolver entityMdResolver;
		if (parentEntity != null)
			entityMdResolver = modelMdResolver.entity(parentEntity);
		else
			entityMdResolver = modelMdResolver.entityType(parentEntityType);
		
		Outline outline = entityMdResolver.property(property).meta(Outline.T).exclusive();
		if (outline != null)
			return !outline.isTrue();
		
		return false;
	}

	public static native TreeNode<AbstractGenericTreeModel> findNode(TreeGrid<AbstractGenericTreeModel> treeGrid, AbstractGenericTreeModel model) /*-{
		return treeGrid.@com.sencha.gxt.widget.core.client.treegrid.TreeGrid::findNode(Ljava/lang/Object;)(model);
	}-*/;
	
	public static class ValueDescriptionBean {
		private String value;
		private String description;
		
		public ValueDescriptionBean(String value, String description) {
			setValue(value);
			setDescription(description);
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
	}
	
}
