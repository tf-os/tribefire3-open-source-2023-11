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
package com.braintribe.gwt.metadataeditor.client.action;

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorUtil;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredPropertyOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorBaseExpert;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorProvider;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;


/**
* This action provides removing of MetaData directly from declared owner or as Overriden
*
*/

public class RemoveMetaDataAction extends ModelAction {
	private MetaDataEditorBaseExpert metaDataExpert;
	private ModelPath modelPath;
	private MetaData metaData;
	private ModelMetaDataEditor modelMetaDataEditor = null;

	public RemoveMetaDataAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.removeMetaData());
		setIcon(GmViewActionResources.INSTANCE.remove());
		setHoverIcon(GmViewActionResources.INSTANCE.removeBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	protected void updateVisibility() {
		Boolean useHidden = true;
		this.metaData = null;
		
		//get MetaData Collection
		if 	(this.gmContentView instanceof MetaDataEditorPanel) {
			MetaDataEditorProvider metaDataEditorProvider = ((MetaDataEditorPanel) this.gmContentView).getEditorProvider();
			if (metaDataEditorProvider != null) {
				this.modelPath = metaDataEditorProvider.getFirstSelectedItem();
				this.metaDataExpert = metaDataEditorProvider.getModelExpert();
				
					
				if (this.modelPath != null && (this.metaDataExpert instanceof DeclaredOverviewExpert || this.metaDataExpert instanceof DeclaredPropertyOverviewExpert)) {
					ModelPathElement modelPathElement =  modelPath.last();
					if (modelPathElement !=null && modelPathElement.getValue() instanceof MetaData) {
						this.metaData = (MetaData) modelPathElement.getValue();						
						useHidden = !MetaDataEditorUtil.canEditMetaData(this.modelPath, this.metaData, metaDataExpert instanceof DeclaredPropertyOverviewExpert);
					}
				}
			}
		}
		setHidden(useHidden);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		final NestedTransaction nestedTransaction = getGmSession().getTransaction().beginNestedTransaction();
		try {
											
			GmMetaModel editingMetaModel = null;
			GmEntityTypeInfo editingEntityType = null;
			GmEnumTypeInfo editingEnumType = null;
			GmPropertyInfo editingProperty = null;
			GmEnumConstantInfo editingEnumConstant = null;
			//for (ModelPath modelPath : modelPaths1) {
				if (modelPath != null) {
					for (ModelPathElement modelPathElement : modelPath) {
						if (modelPathElement.getValue() instanceof GmMetaModel)
							editingMetaModel = modelPathElement.getValue();
						if (modelPathElement.getValue() instanceof GmEntityTypeInfo)
							editingEntityType = modelPathElement.getValue();
						if (modelPathElement.getValue() instanceof GmEnumTypeInfo)
							editingEnumType = modelPathElement.getValue();
						if (modelPathElement.getValue() instanceof GmPropertyInfo)
							editingProperty = modelPathElement.getValue();
						if (modelPathElement.getValue() instanceof GmEnumConstantInfo)
							editingEnumConstant = modelPathElement.getValue();
					}
				}
			//}
						
			if (editingEnumConstant != null) {
				if (editingEnumType == null)
					editingEnumType = editingEnumConstant.declaringTypeInfo();
				if (editingMetaModel == null)
					editingMetaModel = editingEnumType.getDeclaringModel();
				ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
				editorMetaModel.onEnumType(editingEnumType).removeConstantMetaData(editingEnumConstant, md-> md == this.metaData);
			} else if (editingProperty != null) {
				if (editingEntityType == null)
					editingEntityType = editingProperty.declaringTypeInfo();
				if (editingMetaModel == null)
					editingMetaModel = editingEntityType.getDeclaringModel();
				ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
				editorMetaModel.onEntityType(editingEntityType).removePropertyMetaData(editingProperty, md-> md == this.metaData);
			} else if (editingEnumType != null) {
				if (editingMetaModel == null)
					editingMetaModel = editingEnumType.getDeclaringModel();
				ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
				editorMetaModel.onEnumType(editingEnumType).removeMetaData(md-> md == this.metaData);
			} else if (editingEntityType != null) {
				if (editingMetaModel == null)
					editingMetaModel = editingEntityType.getDeclaringModel();
				ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
				if (metaDataExpert instanceof DeclaredPropertyOverviewExpert) 
					editorMetaModel.onEntityType(editingEntityType).removePropertyMetaData(md-> md == this.metaData);					
				else	
					editorMetaModel.onEntityType(editingEntityType).removeMetaData(md-> md == this.metaData);								
			} else if (editingMetaModel != null) {
				ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
				editorMetaModel.removeModelMetaData(md-> md == this.metaData);
			}
														
			nestedTransaction.commit();
		} catch (RuntimeException e) {
			handleFailure(nestedTransaction, e);
		}
	}

	private static void handleFailure(NestedTransaction nestedTransaction, Throwable e) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e1) {
			e1.printStackTrace();
		}
		
		ErrorDialog.show(LocalizedText.INSTANCE.errorAddingEntries(), e);
		e.printStackTrace();
		//window.getGenericModelEditorPanel().getPerformEntityAndPropertyQueryAction().configureEntitiesToIgnore(null); TODO
	}
	
	/*
	private static void performRollback(NestedTransaction nestedTransaction) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e) {
			e.printStackTrace();
			ErrorDialog.show(LocalizedText.INSTANCE.errorRollingBack(), e);
		}
	}
	*/
	
	private ModelMetaDataEditor getModelMetaDataEditor(GmMetaModel editingMetaModel) {
		if (this.modelMetaDataEditor == null || this.modelMetaDataEditor.getMetaModel() != editingMetaModel) {
			//this.modelMetaDataEditor = new BasicModelMetaDataEditor(editingMetaModel, getGmSession()::create, GlobalIdFactory.noGlobalId);
			//this.modelMetaDataEditor = new BasicModelMetaDataEditor(editingMetaModel, getGmSession()::create); Deprecated
			this.modelMetaDataEditor = BasicModelMetaDataEditor.create(editingMetaModel).withSession(getGmSession()).done();
		}
		return this.modelMetaDataEditor;			
	}
	
	protected PersistenceGmSession getGmSession() {
		return gmContentView.getGmSession();
	}	
}
