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
package com.braintribe.gwt.gme.propertypanel.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.braintribe.gwt.async.client.CanceledException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.codec.date.client.ZonelessDateCodec;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.PropertyFieldContext;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableInsideTriggerField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ClickableTriggerField;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringField;
import com.braintribe.gwt.qc.api.client.EntityFieldSource;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.display.formatting.CodeFormatting;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.meta.data.prompt.TimeZoneless;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.sencha.gxt.data.shared.Converter;
import com.sencha.gxt.widget.core.client.event.FocusEvent;
import com.sencha.gxt.widget.core.client.form.Field;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;

public class PropertyPanelEditorPreparationContinuation implements RepeatingCommand {

	private final int maxProcessTimeInMs = 20;
	private boolean cancel;
	private Future<Void> future;
	private Iterator<PropertyModel> it;
	private final PropertyPanelGrid propertyPanelGrid;
	private int rowIndex;
	private final String useCase;
	private final PersistenceGmSession gmSession;
	private final GMEditorSupport gmEditorSupport;
	private final ColumnConfig<PropertyModel, ?> columnConfig;
	//private final boolean hasActionManager;
	private final PropertyPanel propertyPanel;
	private Converter<?,?> converter;
	private Converter<Date, Date> zonelessDateConverter;
	private Converter<String, String> extendedStringFieldConverter;

	public PropertyPanelEditorPreparationContinuation(PropertyPanel propertyPanel) {
		this.propertyPanelGrid = propertyPanel.propertyPanelGrid;
		this.gmSession = propertyPanel.gmSession;
		this.useCase = propertyPanel.useCase;
		this.gmEditorSupport = propertyPanel.gmEditorSupport;
		this.propertyPanel = propertyPanel;

		this.columnConfig = this.propertyPanelGrid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX);
		//this.hasActionManager = propertyPanel.actionManager != null;
	}

	@Override
	public boolean execute() {
		if (cancel) {
			future.onFailure(new CanceledException());
			return false;
		}

		long s = System.currentTimeMillis();
		while (true) {
			if (doStep()) {
				long d = System.currentTimeMillis() - s;

				if (d > maxProcessTimeInMs) {
					return true;
				}
			} else {
				future.onSuccess(null);
				return false;
			}
		}
	}

	public Future<Void> start(List<PropertyModel> propertyModels) {
		this.it = propertyModels.iterator();
		this.cancel = false;
		this.future = new Future<Void>();
		this.rowIndex = 0;
		propertyPanelGrid.gridInlineEditing.clearEditors();
		Scheduler.get().scheduleIncremental(this);
		return this.future;
	}
	
	public void cancel() {
		this.cancel = true;
	}

	@SuppressWarnings("rawtypes")
	private boolean doStep() {
		if (!it.hasNext())
			return false;

		PropertyModel propertyModel = it.next();
		boolean isCollection = propertyModel.getValueElementType().isCollection();
		GenericModelType propertyType = isCollection ? ((CollectionType) propertyModel.getValueElementType()).getCollectionElementType() : propertyModel.getValueElementType();		
		if (!propertyModel.isEditable() && (!propertyType.getJavaType().equals(String.class) || isVirtualEnum(propertyModel))) {
			rowIndex++;
			return true;
		}
		
		propertyPanelGrid.gridInlineEditing.setCurrentRow(rowIndex);

		PropertyMdResolver propertyMdResolver = null;
		EntityMdResolver entityMdResolver = null;
		int minLenght = -1;
		int maxLenght = -1;
		GenericEntity parentEntity = propertyModel.getParentEntity();
		ModelMdResolver metaDataResolver = parentEntity == null ? propertyPanel.getMetaData() : GmSessions.getMetaData(parentEntity);
		String propertyName = propertyModel.getPropertyName();
			
		if (metaDataResolver != null) {
			if (parentEntity != null)
				entityMdResolver = metaDataResolver.entity(parentEntity);
			else
				entityMdResolver = metaDataResolver.lenient(propertyPanel.lenient).entityType(propertyModel.getParentEntityType());
			
			propertyMdResolver = entityMdResolver.property(propertyName);
			
			MinLength minLengthMeta = propertyMdResolver.meta(MinLength.T).exclusive();
			if (minLengthMeta != null)
				minLenght = ((Long) minLengthMeta.getLength()).intValue();
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, MinLength.T);
	
			MaxLength maxLengthMeta = propertyMdResolver.meta(MaxLength.T).exclusive();
			if (maxLengthMeta != null)
				maxLenght = ((Long) maxLengthMeta.getLength()).intValue();
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, MaxLength.T);
		}
		
		Object minValue = null;
		Min min = propertyMdResolver == null ? null : propertyMdResolver.meta(Min.T).exclusive();
		if (min != null)
			minValue = min.getLimit();
		propertyPanel.handleMetadataReevaluation(propertyMdResolver, Min.T);
		
		Object maxValue = null;
		Max max = propertyMdResolver == null ? null : propertyMdResolver.meta(Max.T).exclusive();
		if (max != null)
			maxValue = max.getLimit();
		propertyPanel.handleMetadataReevaluation(propertyMdResolver, Max.T);

		PropertyFieldContext context = new PropertyFieldContext();
		if (propertyPanel.enableMandatoryFieldConfiguration)
			context.setMandatory(propertyModel.getMandatory());
		context.setPassword(propertyModel.getPassword());
		context.setModelType(propertyModel.getValueElementType());
		context.setRegex(propertyMdResolver == null ? null : (Pattern) propertyMdResolver.meta(Pattern.T).exclusive());
		propertyPanel.handleMetadataReevaluation(propertyMdResolver, Pattern.T);
		
		context.setUseAlternativeField(isCollection ? false : propertyModel.getFlow());
		context.setUseCase(useCase);
		context.setGmSession(gmSession);
		context.setVirtualEnum(propertyModel.getVirtualEnum());
		context.setMinLenght(minLenght);
		context.setMaxLenght(maxLenght);
		context.setParentEntity(parentEntity);
		context.setParentEntityType(propertyModel.getParentEntityType());
		context.setPropertyName(propertyName);
		context.setMinValue(minValue);
		context.setMaxValue(maxValue);
		context.setDynamicSelectList(propertyModel.getDynamicSelectList());
		context.setIconProvider(propertyPanel.iconProvider);
		context.setReadOnly(!propertyModel.isEditable());
		
		boolean isCollectionInline = false;
		if (isCollection && propertyMdResolver != null && propertyMdResolver.is(Inline.T)) {
			isCollectionInline = true;
			context.setHandlingCollection(true);
			context.setModelType(propertyType);
		}
		propertyPanel.handleMetadataReevaluation(propertyMdResolver, Inline.T);
		
		IsField<?> field = gmEditorSupport.providePropertyField(context);
		
		if (field == null) {
			rowIndex++;
			return true;
		}
		
		final int fieldRowIndex = rowIndex;
		
		//RVE - set field Name as "$property of $instance" (used as a Dialog caption in special cases)
		Name name = propertyMdResolver == null ? null : propertyMdResolver.meta(Name.T).exclusive();
		if (name != null && name.getName() != null)
			propertyName = I18nTools.getLocalized(name.getName());
		String parentEntityName = propertyModel.getParentEntityType().getShortName();
		String selectiveInformation = SelectiveInformationResolver.resolve(propertyModel.getParentEntityType(), propertyModel.getParentEntity(),
				metaDataResolver != null ? metaDataResolver : this.gmSession.getModelAccessory().getMetaData(), this.useCase);		
		if (selectiveInformation != null)
			parentEntityName = selectiveInformation;
		if (field instanceof Field) {
			((Field<?>) field).setData("dialogName", propertyName + " " + LocalizedText.INSTANCE.of() + " " + parentEntityName);
		
			CodeFormatting codeFormatting = propertyMdResolver == null ? null : propertyMdResolver.meta(CodeFormatting.T).exclusive();
			if (codeFormatting != null) 
				((Field<?>) field).setData("codeFormatting", codeFormatting.getCodeFormat().toString());		
		}
		
		if (field instanceof TriggerFieldAction) {
			if (field instanceof TriggerField<?>)
				((TriggerField<?>) field).setHideTrigger(true);
			((TriggerFieldAction) field).setGridInfo(propertyPanelGrid.gridInlineEditing,
					new GridCell(rowIndex, propertyPanelGrid.getColumnModel().getColumns().indexOf(columnConfig)));

			if (/*!hasActionManager || */!(field instanceof SimplifiedEntityField)) {
				if (propertyPanel.triggerFieldActionModelMap == null)
					propertyPanel.triggerFieldActionModelMap = new HashMap<>();
				propertyPanel.triggerFieldActionModelMap.put(propertyModel, (TriggerFieldAction) field);
			}
		}
		
		boolean sessionConfigured = false;
		if (propertyPanel.alternativeGmSession != null && field instanceof EntityFieldSource) {
			((EntityFieldSource) field).configureGmSession(propertyPanel.alternativeGmSession);
			sessionConfigured = true;
		}
		
		boolean instantiable = true;
		if (propertyMdResolver != null)
			instantiable = GMEMetadataUtil.isInstantiable(propertyMdResolver, metaDataResolver);
		
		if (field instanceof SimplifiedEntityField) {
			boolean simplified = false;
			boolean useDetail = true;
			
			SimplifiedAssignment sa = propertyModel.getSimplifiedAssignment();
			if (sa != null) {
				useDetail = sa.getShowDetails();
				simplified = true;
			}
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, SimplifiedAssignment.T);
			
			if (!sessionConfigured)
				((SimplifiedEntityField) field).configureGmSession(gmSession);
			((SimplifiedEntityField) field).configureUseCase(useCase);
			((SimplifiedEntityField) field).configurePropertyModel(propertyModel, instantiable, simplified, useDetail);
			((SimplifiedEntityField) field).configureGmContentView(this.propertyPanel);
		}
		
		context.setInstantiable(instantiable);
		context.setReferenceable(propertyModel.isReferenceable());

		// if (field instanceof CheckBox) TODO
		// editor.setAutoSizeMode(AutoSizeMode.HEIGHT);
		// else if (field instanceof TextArea)
		// editor.setAutoSizeMode(AutoSizeMode.WIDTH);

		// editor.removeStyleName("x-small-editor x-grid-editor"); //Removing the x-small-editor, which limits the height of the editor within GXT
		// editor.addStyleName("x-grid-editor");

		if (isCollectionInline)
			propertyPanelGrid.gridInlineEditing.addEditor(columnConfig, getCollectionInlineConverter(), (IsField) field, false);
		else
			propertyPanelGrid.gridInlineEditing.addEditor(columnConfig, getConverter(propertyMdResolver, field), (IsField) field, propertyModel.getFlow());
		
		if (field instanceof ClickableTriggerField || field instanceof ClickableInsideTriggerField
				|| isSettingGmPropertyInitializer(propertyName, parentEntity)) {
			propertyPanel.refreshRow(propertyPanelGrid.getView(), rowIndex);
			
			if (propertyModel.getFlow() && propertyPanelGrid.expander != null)
				propertyPanelGrid.expander.refreshRow(rowIndex);
		}

		field.asWidget().addHandler(event -> propertyPanel.updateValidateToolTip(fieldRowIndex), FocusEvent.getType());
		
		rowIndex++;

		return true;
	}
	
	private boolean isVirtualEnum(PropertyModel model) {
		return model.getVirtualEnum() != null || model.getDynamicSelectList() != null;
	}
	
	private boolean isSettingGmPropertyInitializer(String propertyName, GenericEntity parentEntity) {
		return "initializer".equals(propertyName) && parentEntity instanceof GmProperty;
	}
	
	@SuppressWarnings("rawtypes")
	private Converter getCollectionInlineConverter() {
		if (converter != null)
			return converter;
		
		converter = new Converter<Object, Object>() {
			@Override
			public Object convertFieldValue(Object object) {
				return object;
			}

			@Override
			public Object convertModelValue(Object object) {
				return null;
			}
		};
		
		return converter;
	}
	
	@SuppressWarnings("rawtypes")
	private Converter getConverter(PropertyMdResolver propertyMdResolver, IsField<?> field) {
		if (propertyMdResolver == null || !propertyMdResolver.is(TimeZoneless.T)) {
			if (!(field instanceof ExtendedStringField))
				return null;
			
			if (extendedStringFieldConverter != null)
				return extendedStringFieldConverter;
			
			extendedStringFieldConverter = new Converter<String, String>() {
				@Override
				public String convertModelValue(String value) {
					return value;
				}
				
				@Override
				public String convertFieldValue(String value) {
					return ((ExtendedStringField) field).getTextWithEncodedLineBreaks(value);
				}
			};
			
			return extendedStringFieldConverter;
		}
		
		if (zonelessDateConverter != null)
			return zonelessDateConverter;
		
		zonelessDateConverter = new Converter<Date, Date>() {
			@Override
			public Date convertModelValue(Date zonedDate) {
				return ZonelessDateCodec.INSTANCE.decode(zonedDate);
			}
			
			@Override
			public Date convertFieldValue(Date zonelessDate) {
				return ZonelessDateCodec.INSTANCE.encode(zonelessDate);
			}
		};
		
		return zonelessDateConverter;
	}

}
