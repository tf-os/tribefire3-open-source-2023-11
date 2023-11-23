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

import java.util.Date;
import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.codec.date.client.ZonelessDateCodec;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.PropertyFieldContext;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CollectionTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyOrValueEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ValueTreeModel;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.MultiEditorGridInlineEditing;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.prompt.TimeZoneless;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.data.shared.Converter;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class ExtendGridInlineEditing extends MultiEditorGridInlineEditing<AbstractGenericTreeModel> {
	
	private final AssemblyPanel assemblyPanel;
	private ValueTreeModel valueTreeModel;
	private boolean useAlternativeField;
	private VirtualEnum virtualEnum;
	private Pattern regex;
	private final GMEditorSupport editorSupport;
	private int maxLenght = -1;
	private NestedTransaction editionNestedTransaction;
	private Converter<Date, Date> timeZonelessConverter;

	public ExtendGridInlineEditing(TreeGrid<AbstractGenericTreeModel> editableGrid, AssemblyPanel assemblyPanel, GMEditorSupport editorSupport) {
		super(editableGrid);
		this.assemblyPanel = assemblyPanel;
		this.editorSupport = editorSupport;
		setUseMultiEditor(false);
		
		addBeforeStartEditHandler(getBeforeStartEditHandler(editableGrid, assemblyPanel));
		
		addCancelEditHandler(event -> rollbackEditionTransaction());
		
		addCompleteEditHandler(getCompleteEditHandler(editableGrid, assemblyPanel));
	}

	@Override
	public <N, O> Converter<N, O> getConverter(ColumnConfig<AbstractGenericTreeModel, N> columnConfig) {
		Converter<N, O> converter = super.getConverter(columnConfig, 0);
		if (converter != null)
			return converter;
		
		ModelPath modelPath = assemblyPanel.getFirstSelectedItem();
		if (modelPath == null || !(modelPath.last() instanceof PropertyRelatedModelPathElement))
			return null;
		
		GenericEntity parentEntity = ((PropertyRelatedModelPathElement) modelPath.last()).getEntity();
		Property property = ((PropertyRelatedModelPathElement) modelPath.last()).getProperty();
		
		if (parentEntity == null || property == null)
			return null;
		
		ModelMdResolver modelMdResolver = getMetaData(parentEntity);
		PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity).useCase(assemblyPanel.getUseCase())
				.property(property.getName());
		
		if (!propertyMdResolver.is(TimeZoneless.T))
			return null;
		
		if (timeZonelessConverter != null)
			return (Converter<N, O>) timeZonelessConverter;
		
		timeZonelessConverter = new Converter<Date, Date>() {
			@Override
			public Date convertModelValue(Date zonedDate) {
				return ZonelessDateCodec.INSTANCE.decode(zonedDate);
			}
			
			@Override
			public Date convertFieldValue(Date zonelessDate) {
				return ZonelessDateCodec.INSTANCE.encode(zonelessDate);
			}
		};
		
		return (Converter<N, O>) timeZonelessConverter;
	}
	
	@Override
	public <O> IsField<O> getEditor(ColumnConfig<AbstractGenericTreeModel, ?> columnConfig) {
		IsField<O> editor = super.getEditor(columnConfig, 0);
		
		GenericEntity parentEntity = null;
		String propertyName = null;
		valueTreeModel = null;
		if (editor != null) {
			AbstractGenericTreeModel selectedItem = assemblyPanel.getTreeGrid().getSelectionModel().getSelectedItem();
			if (selectedItem == null || !(selectedItem.getDelegate() instanceof ValueTreeModel)) {
				valueTreeModel = null;
				return editor;
			}
			
			//RVE TODO - activeCell is NULL -> what to in that case, because activeCell is not used inside the if statement
			if ((activeCell == null || activeCell.getCol() != 0) && (multiEditorActiveCell == null || multiEditorActiveCell.getCol() != 0)) {
				valueTreeModel = null;
				return null;
			}
			
			valueTreeModel = (ValueTreeModel) selectedItem.getDelegate();
			if (!(valueTreeModel.getParent() instanceof CollectionTreeModel) || !valueTreeModel.getElementType().isSimple()) {
				valueTreeModel = null;
				return null;
			}
			
			ModelPath modelPath = assemblyPanel.getFirstSelectedItem();
			Property property = null;
			if (modelPath != null && modelPath.last() instanceof PropertyRelatedModelPathElement) {
				parentEntity = ((PropertyRelatedModelPathElement) modelPath.last()).getEntity();
				property = ((PropertyRelatedModelPathElement) modelPath.last()).getProperty();
				propertyName = property.getName();
			}
			useAlternativeField = false;
			virtualEnum = null;
			regex = null;
			if (parentEntity != null && property != null) {
				ModelMdResolver modelMdResolver = getMetaData(parentEntity);
				PropertyMdResolver propertyMetaDataContextBuilder = modelMdResolver.entity(parentEntity).useCase(assemblyPanel.getUseCase())
						.property(propertyName);
				virtualEnum = propertyMetaDataContextBuilder.meta(VirtualEnum.T).exclusive();
				regex = propertyMetaDataContextBuilder.meta(Pattern.T).exclusive();
				useAlternativeField = AssemblyUtil.hasOutline(property, parentEntity, null, modelMdResolver, assemblyPanel.modelFactory);
				MaxLength maxLengthMeta = propertyMetaDataContextBuilder.meta(MaxLength.T).exclusive();
				maxLenght = maxLengthMeta == null ? -1 : ((Long) maxLengthMeta.getLength()).intValue();
			}
			
			return editor;
		}
		
		if (valueTreeModel == null)
			return null;
		
		PropertyFieldContext context = new PropertyFieldContext();
		context.setModelType(valueTreeModel.getElementType());
		context.setInitialValue(valueTreeModel.getModelObject());
		context.setUseAlternativeField(useAlternativeField);
		context.setUseCase(assemblyPanel.getUseCase());
		context.setGmSession(assemblyPanel.getGmSession());
		context.setVirtualEnum(virtualEnum);
		context.setRegex(regex);
		context.setMaxLenght(maxLenght);
		context.setParentEntity(parentEntity);
		context.setPropertyName(propertyName);
		
		IsField<?> field = editorSupport.providePropertyField(context);
		//field.setAllowBlur(true);
		return (IsField<O>) field;
	}
	
	@Override
	protected void onClick(ClickEvent event) {
		Element target = Element.as(event.getNativeEvent().getEventTarget());
		
		int rowIndex = assemblyPanel.getTreeGrid().getView().findRowIndex(target);
		if (rowIndex != -1) {
			int colIndex = assemblyPanel.getTreeGrid().getView().findCellIndex(target, null);
			AbstractGenericTreeModel model = assemblyPanel.getTreeGrid().getStore().get(rowIndex);
			EntityTreeModel entityTreeModel = model != null ? model.getDelegate().getEntityTreeModel() : null;
			if (entityTreeModel != null) {
				TreePropertyModel propertyModel = getTreePropertyModelByIndex(colIndex, entityTreeModel);
				if (propertyModel != null && propertyModel.getElementType().getJavaType().equals(Boolean.class))
					return;
			}
		}
		
		super.onClick(event);
	}
	
	private BeforeStartEditHandler<AbstractGenericTreeModel> getBeforeStartEditHandler(TreeGrid<AbstractGenericTreeModel> editableGrid, AssemblyPanel assemblyPanel) {
		return event -> {
			AbstractGenericTreeModel delegateModel = editableGrid.getStore().get(event.getEditCell().getRow()).getDelegate();
			EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
			if (entityTreeModel != null) {
				String propertyName = editableGrid.getColumnModel().getColumn(event.getEditCell().getCol()).getPath();
				TreePropertyModel handledModel = entityTreeModel.getTreePropertyModel(propertyName);
				
				rollbackEditionTransaction();
				if (handledModel == null || handledModel.isReadOnly())
					event.setCancelled(true);
				else {
					assemblyPanel.gmEditionViewControllerSupplier.get().registerAsCurrentEditionView(assemblyPanel);
					editionNestedTransaction = assemblyPanel.gmSession.getTransaction().beginNestedTransaction();
				}
			} else if (!(delegateModel instanceof ValueTreeModel))
				event.setCancelled(true);
		};
	}
	
	private CompleteEditHandler<AbstractGenericTreeModel> getCompleteEditHandler(TreeGrid<AbstractGenericTreeModel> editableGrid, AssemblyPanel assemblyPanel) {
		return event -> {
			AbstractGenericTreeModel model = editableGrid.getStore().get(event.getEditCell().getRow());
			AbstractGenericTreeModel delegateModel = model.getDelegate();
			IsField<?> editor = event.getSource().getEditor(editableGrid.getColumnModel().getColumn(event.getEditCell().getCol()));
			Object value;
			if (editor instanceof ValueBaseField)
				value = ((ValueBaseField<?>) editor).getCurrentValue();
			else
				value = editor.getValue();
			
			Converter<Object, Object> converter = event.getSource().getConverter(editableGrid.getColumnModel().getColumn(event.getEditCell().getCol()));
			if (converter != null)
				value = converter.convertFieldValue(value);
			
			EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
			
			new Timer() {
				@Override
				public void run() {
					editableGrid.getTreeStore().rejectChanges();
				}
			}.schedule(500);
			
			if (entityTreeModel != null) {
				if (editionNestedTransaction == null)
					return;
				
				String propertyName = editableGrid.getColumnModel().getColumn(event.getEditCell().getCol()).getPath();
				TreePropertyModel handledModel = entityTreeModel.getTreePropertyModel(propertyName);
				if (handledModel == null)
					return;
				
				if (!GMEUtil.isEditionValid(value, handledModel.getValue(), editor)) {
					rollbackEditionTransaction();
					return;
				}
				
				GenericEntity parentEntity = handledModel.getParentEntity();
				EntityType<GenericEntity> entityType = parentEntity.entityType();
				entityType.getProperty(handledModel.getPropertyName()).set(parentEntity, value);
				
				List<Manipulation> manipulationsDone = editionNestedTransaction.getManipulationsDone();
				Manipulation triggerManipulation = manipulationsDone.stream().filter(m -> m.manipulationType().equals(ManipulationType.CHANGE_VALUE))
						.findFirst().orElse(null);
				
				editionNestedTransaction.commit();
				editionNestedTransaction = null;
				assemblyPanel.gmEditionViewControllerSupplier.get().unregisterAsCurrentEditionView(assemblyPanel);
				
				ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity);
				EntityMdResolver entityMdResolver = modelMdResolver.useCase(assemblyPanel.getUseCase()).entity(parentEntity);
				PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyName);
				
				Future<Void> future = GMEUtil.fireOnEditRequest(parentEntity, triggerManipulation, propertyMdResolver, assemblyPanel.getGmSession(),
						assemblyPanel.transientSession, null, assemblyPanel.transientSessionSupplier, assemblyPanel.notificationFactorySupplier);
				if (future ==  null)
					handleAutoCommit();
				else {
					assemblyPanel.mask();
					future //
							.andThen(result -> {
								assemblyPanel.unmask();
								handleAutoCommit();
							}).onError(e -> {
								assemblyPanel.unmask();
								ErrorDialog.show(LocalizedText.INSTANCE.errorRunningOnEditRequest(), e);
								e.printStackTrace();
							});
				}
			} else if (delegateModel instanceof ValueTreeModel) {
				if (!GMEUtil.isEditionValid(value, delegateModel.getModelObject(), editor)) {
					rollbackEditionTransaction();
					return;
				}
				
				if (model instanceof ListEntryTreeModel) {
					List<Object> list = (model.getParent()).getModelObject();
					list.set(((ListEntryTreeModel) model).getListEntryIndex(),  value);
				} else if (model instanceof SetEntryTreeModel) {
					assemblyPanel.manipulationHandler.replaceInSet((SetEntryTreeModel) model, value);
				} else if (model instanceof MapKeyOrValueEntryTreeModel)
					assemblyPanel.manipulationHandler.replaceInMap((MapKeyOrValueEntryTreeModel) model, value);
			}
		};
	}
	
	private TreePropertyModel getTreePropertyModelByIndex(int index, EntityTreeModel entityTreeModel) {
		if (index == -1)
			return null;
		
		String propertyName = assemblyPanel.getTreeGrid().getColumnModel().getColumn(index).getPath();
		return entityTreeModel.getTreePropertyModel(propertyName);
	}
	
	private void rollbackEditionTransaction() {
		try {
			if (editionNestedTransaction != null) {
				editionNestedTransaction.rollback();
				editionNestedTransaction = null;
				assemblyPanel.gmEditionViewControllerSupplier.get().unregisterAsCurrentEditionView(assemblyPanel);
			}
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		}
	}
	
	private void handleAutoCommit() {
		if (assemblyPanel.autoCommit && assemblyPanel.commitAction != null && assemblyPanel.commitAction.getEnabled()) {
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.put("AutoCommit", true);
			assemblyPanel.commitAction.perform(triggerInfo);
		}
	}

}
