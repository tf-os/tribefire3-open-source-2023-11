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
package com.braintribe.gwt.gm.storage.impl.wb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.Storage;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gm.storage.api.StorageHandle;
import com.braintribe.gwt.gm.storage.expert.impl.wb.WbQueryStorageInput;
import com.braintribe.gwt.gm.storage.impl.wb.form.save.WbSaveQueryDialogConfig;
import com.braintribe.gwt.gm.storage.impl.wb.form.save.WbSaveQueryDialogResult;
import com.braintribe.gwt.gm.storage.impl.wb.form.setting.WbSettingQueryDialogConfig;
import com.braintribe.gwt.gm.storage.impl.wb.form.setting.WbSettingQueryDialogResult;
import com.braintribe.gwt.gm.storage.impl.wb.worker.WbTransactionWorker;
import com.braintribe.gwt.gm.storage.impl.wb.worker.WbTraversingWorker;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.data.prompt.ColumnInfo;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.Query;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.workbench.QueryAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.meta.AutoPagingSize;
import com.braintribe.model.workbench.meta.ColumnDisplay;
import com.braintribe.model.workbench.meta.DefaultView;
import com.braintribe.model.workbench.meta.QueryString;
import com.braintribe.utils.i18n.I18nTools;

public class WbStorage implements Storage {

	/********************************** Variables **********************************/

	private Workbench workbench = null;
	private PersistenceGmSession workbenchSession;
	private Consumer<WbSaveQueryDialogConfig> saveQueryDialog = null;
	private Consumer<WbSettingQueryDialogConfig> settingQueryDialog = null;
	private Supplier<? extends Consumer<WbSaveQueryDialogConfig>> saveQueryDialogProvider = null;
	private Supplier<? extends Consumer<WbSettingQueryDialogConfig>> settingQueryDialogProvider = null;

	/******************************** Storage Methods ********************************/

	/**
	 * Configures the required workbench session.
	 */
	@Required
	public void setWorkbenchSession(final PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}

	/**
	 * Configures the required {@link ExplorerConstellation} used for accessing the workbench.
	 */
	@Required
	public void setExplorerConstellation(final ExplorerConstellation explorerConstellation) {
		this.workbench = explorerConstellation.getWorkbench();
	}

	/**
	 * Configures the required dialog to be shown when saving queries.
	 */
	@Required
	public void setSaveQueryDialogProvider(final Supplier<? extends Consumer<WbSaveQueryDialogConfig>> saveQueryDialogProvider) {
		this.saveQueryDialogProvider = saveQueryDialogProvider;
	}

	/**
	 * Configures the required dialog to be shown when setting query properties.
	 */
	@Required
	public void setSettingQueryDialogProvider(final Supplier<? extends Consumer<WbSettingQueryDialogConfig>> settingQueryDialogProvider) {
		this.settingQueryDialogProvider = settingQueryDialogProvider;
	}

	@Override
	public Future<StorageHandle> saveAs(final GenericEntity entity) {
		if (!(entity instanceof WbQueryStorageInput))
			throw new WbStorageRuntimeException("Could not save Query. Received no WbStorageInput.");
			
		// Create future result
		Future<StorageHandle> future = new Future<>();

		// Get query and queryString from entity
		WbQueryStorageInput storageInput = (WbQueryStorageInput) entity;
		String queryString = storageInput.getQueryString();
		Query query = storageInput.getQuery();

		// Open ShowDialog && check result
		Future<WbSaveQueryDialogResult> futureSaveQueryDialogResult = openSaveDialog();
		futureSaveQueryDialogResult.andThen(dialogResult -> {
			// Create instance of WbTransactionWorker
			WbTransactionWorker<StorageHandle> transactionWorker = new WbTransactionWorker<StorageHandle>() {
				@Override
				protected void transactionJob(final PersistenceGmSession transactionSession) {
					// Create SaveFolder and store folder in storage handle
					Folder queryFolder = transactionSession.create(Folder.T);
					getStorageHandle().setQueryFolder(queryFolder);

					// Set parent folder of created SaveFolder
					dialogResult.getParentFolder().getSubFolders().add(queryFolder);
					queryFolder.setParent(dialogResult.getParentFolder());

					// Set name & displayName
					queryFolder.setDisplayName(dialogResult.getFolderName());
					queryFolder.setName(I18nTools.getDefault(dialogResult.getFolderName(), ""));

					// Create Query-Template
					Template queryTemplate = transactionSession.create(Template.T);
					queryTemplate.setPrototypeTypeSignature(query != null ? query.entityType().getTypeSignature() : Query.T.getTypeSignature());
					queryTemplate.setPrototype(new WbTraversingWorker().createTemplateQuery(transactionSession, query));

					// Create the QueryString Meta-Data
					QueryString queryStringMetaData = transactionSession.create(QueryString.T);
					queryStringMetaData.setValue(queryString);

					// Set Meta-Data information to Query-Template
					Set<TemplateMetaData> metaData = queryTemplate.getMetaData();
					metaData.add(queryStringMetaData);

					// Create Template Query-Action and set QueryTemplate, then set Query-Action to Folder
					TemplateQueryAction templateQueryAction = transactionSession.create(TemplateQueryAction.T);
					templateQueryAction.setTemplate(queryTemplate);
					queryFolder.setContent(templateQueryAction);
					
					prepareColumnDisplay(storageInput, null, transactionSession, queryTemplate);
				}

				@Override
				protected StorageHandle getSuccessResult() {
					return getStorageHandle();
				}
			};

			// Set StorageHandle to worker and start working
			transactionWorker.setStorageHandle(new WbStorageHandle());
			transactionWorker.doTransaction(WbStorage.this.workbenchSession, WbStorage.this.workbench, future);
		}).onError(future::onFailure);

		return future;
	}

	@Override
	public Future<Void> save(GenericEntity entity, StorageHandle handle) {
		if (!(handle instanceof WbStorageHandle))
			throw new WbStorageRuntimeException("Could not save Query. Invalid handle received.");
		
		WbStorageHandle storageHandle = (WbStorageHandle) handle;
		Folder queryFolder = storageHandle.getQueryFolder();
		
		if (queryFolder == null)
			throw new WbStorageRuntimeException("Could not save Query. Received no queryFolder.");
		
		if (!(entity instanceof WbQueryStorageInput))
			throw new WbStorageRuntimeException("Could not save Query. Received no WbQueryStorageInput.");

		// Create future result
		Future<Void> future = new Future<>();

		// Get query and queryString from entity
		WbQueryStorageInput storageInput = (WbQueryStorageInput) entity;
		String queryString = storageInput.getQueryString();
		Query query = storageInput.getQuery();

		// Create instance of WbTransactionWorker
		WbTransactionWorker<Void> transactionWorker = new WbTransactionWorker<Void>() {
			@Override
			protected void transactionJob(PersistenceGmSession transactionSession) {
				// Check for Template Query-Action. If none create a TemplateQueryAction
				if ((queryFolder.getContent() instanceof TemplateQueryAction) == false)
					queryFolder.setContent(transactionSession.create(TemplateQueryAction.T));

				TemplateQueryAction templateQueryAction = (TemplateQueryAction) queryFolder.getContent();
				Template queryTemplate = templateQueryAction.getTemplate();

				// Check template for query
				if (queryTemplate == null) {
					// Create new template for Template Query-Action
					queryTemplate = transactionSession.create(Template.T);
					templateQueryAction.setTemplate(queryTemplate);
				}

				// Create Query-Template
				String prototypeTypeSignature = query != null ? query.entityType().getTypeSignature() : Query.T.getTypeSignature();
				if (!prototypeTypeSignature.equals(queryTemplate.getPrototypeTypeSignature()))
					queryTemplate.setPrototypeTypeSignature(query != null ? query.entityType().getTypeSignature() : Query.T.getTypeSignature());
				queryTemplate.setPrototype(new WbTraversingWorker().createTemplateQuery(transactionSession, query));

				// Try to find the QueryString and ColumnDisplay
				QueryString queryStringMetaData = null;
				ColumnDisplay columnDisplay = null;
				for (TemplateMetaData metadata : queryTemplate.getMetaData()) {
					if (metadata instanceof QueryString)
						queryStringMetaData = (QueryString) metadata;
					else if (metadata instanceof ColumnDisplay)
						columnDisplay = (ColumnDisplay) metadata;
				}

				// Check the QueryString
				if (queryStringMetaData == null) {
					// Create and add the QueryString to the MetaData
					queryStringMetaData = transactionSession.create(QueryString.T);
					queryTemplate.getMetaData().add(queryStringMetaData);
				}

				// Set new QueryString
				if (!queryString.equals(queryStringMetaData.getValue()))
					queryStringMetaData.setValue(queryString);
				
				prepareColumnDisplay(storageInput, columnDisplay, transactionSession, queryTemplate);
			}
		};

		// Start working
		transactionWorker.setStorageHandle(storageHandle);
		transactionWorker.doTransaction(workbenchSession, workbench, future);

		return future;
	}
	
	private void prepareColumnDisplay(WbQueryStorageInput storageInput, ColumnDisplay columnDisplay, PersistenceGmSession transactionSession,
			Template queryTemplate) {
		ColumnData columnData = storageInput.getColumnData();
		if (columnData == null)
			return;
		
		if (columnDisplay == null) {
			columnDisplay = transactionSession.create(ColumnDisplay.T);
			queryTemplate.getMetaData().add(columnDisplay);
		}
		
		if (columnDisplay.getDisplayNode() != columnData.getDisplayNode())
			columnDisplay.setDisplayNode(columnData.getDisplayNode());
		
		if (columnDisplay.getPreventSingleEntryExpand() != columnData.getPreventSingleEntryExpand())
			columnDisplay.setPreventSingleEntryExpand(columnData.getPreventSingleEntryExpand());
		
		if ((columnDisplay.getNodeWidth() == null && columnData.getNodeWidth() != null)
				|| (columnDisplay.getNodeWidth() != null && !columnDisplay.getNodeWidth().equals(columnData.getNodeWidth()))) {
			columnDisplay.setNodeWidth(columnData.getNodeWidth());
		}
		
		if (columnData.getNodeTitle() != null) {
			LocalizedString nodeTitle = columnDisplay.getNodeTitle();
			if (nodeTitle == null) {
				nodeTitle = transactionSession.create(LocalizedString.T);
				columnDisplay.setNodeTitle(nodeTitle);
			} else if (!nodeTitle.getLocalizedValues().isEmpty())
				nodeTitle.getLocalizedValues().clear();
			
			nodeTitle.getLocalizedValues().putAll(columnData.getNodeTitle().getLocalizedValues());
		}
		
		prepareDisplayPaths(columnDisplay, columnData.getDisplayPaths(), transactionSession);
	}

	@Override
	public Future<Void> settings(final StorageHandle handle) {
		if (!(handle instanceof WbStorageHandle))
			throw new WbStorageRuntimeException("Could not save Query. Invalid handle received.");
		
		WbStorageHandle storageHandle = (WbStorageHandle) handle;
		Folder queryFolder = storageHandle.getQueryFolder();
		
		if (queryFolder == null)
			throw new WbStorageRuntimeException("Could not save Query. Received no queryFolder.");

		// Create future result
		Future<Void> future = new Future<>();

		// Open ShowDialog && check result
		Future<WbSettingQueryDialogResult> futureSettingQueryDialogResult = openSettingDialog(queryFolder);
		futureSettingQueryDialogResult.andThen(dialogResult -> {
			// Create instance of WbTransactionWorker
			WbTransactionWorker<Void> transactionWorker = new WbTransactionWorker<Void>() {
				@Override
				protected void transactionJob(final PersistenceGmSession transactionSession) {
					// Set new icon
					if (queryFolder.getIcon() != dialogResult.getIcon())
						queryFolder.setIcon(dialogResult.getIcon());

					// Set new parent folder when new parent is not the query folder
					if (queryFolder.getParent() != dialogResult.getParentFolder() && dialogResult.getParentFolder() != queryFolder) {
						queryFolder.getParent().getSubFolders().remove(queryFolder);
						dialogResult.getParentFolder().getSubFolders().add(queryFolder);
						queryFolder.setParent(dialogResult.getParentFolder());
					}

					// Set new folder name
					if (localizedStringValuesAreEqual(queryFolder.getDisplayName(), dialogResult.getFolderName()) == false) {
						// Get and check if current LocalizedString is null
						LocalizedString currentFolderName = queryFolder.getDisplayName();
						if (currentFolderName == null) {
							// Create a new LocalizedString using the session
							currentFolderName = transactionSession.create(LocalizedString.T);
						}

						// Modify current LocalizedString values
						currentFolderName.getLocalizedValues().clear();
						currentFolderName.getLocalizedValues().putAll(dialogResult.getFolderName().getLocalizedValues());

						// Set modified LocalizedString
						queryFolder.setDisplayName(currentFolderName);
						queryFolder.setName(I18nTools.getDefault(currentFolderName, ""));
					}

					setWorkbenchAction(transactionSession);
				}

				private void setWorkbenchAction(final PersistenceGmSession transactionSession) {
					// Check the Folder-Content type for QueryAction
					if (queryFolder.getContent() instanceof QueryAction) {
						QueryAction queryAction = (QueryAction) queryFolder.getContent();

						// Set new context
						if (queryAction.getInplaceContextCriterion() != dialogResult.getContext())
							queryAction.setInplaceContextCriterion(dialogResult.getContext());

						// Set new multi selection
						if (queryAction.getMultiSelectionSupport() != dialogResult.getMultiSelection())
							queryAction.setMultiSelectionSupport(dialogResult.getMultiSelection());
					}

					// Check the Folder-Content type for TemplateQueryAction
					if (!(queryFolder.getContent() instanceof TemplateQueryAction))
						return;
					
					TemplateQueryAction templateQueryAction = (TemplateQueryAction) queryFolder.getContent();
					Template queryTemplate = templateQueryAction.getTemplate();

					// Check template for query
					if (queryTemplate == null) {
						// Create new template for Template Query-Action
						queryTemplate = transactionSession.create(Template.T);
						templateQueryAction.setTemplate(queryTemplate);
					}
					
					if (templateQueryAction.getForceFormular() != dialogResult.getForceForm())
						templateQueryAction.setForceFormular(dialogResult.getForceForm());
					
					Integer newSize = dialogResult.getAutoPagingSize();
					boolean autoPagingSizeFound = false;
					String newDefaultView = dialogResult.getDefaultView();
					boolean defaultViewFound = false;
					for (TemplateMetaData metadata : queryTemplate.getMetaData()) {
						if (metadata instanceof AutoPagingSize) {
							AutoPagingSize autoPagingSize = (AutoPagingSize) metadata;
							if (newSize == null)
								queryTemplate.getMetaData().remove(metadata);
							else if (newSize != autoPagingSize.getSize())
								autoPagingSize.setSize(newSize);
							
							autoPagingSizeFound = true;
						} else if (metadata instanceof DefaultView) {
							DefaultView defaultView = (DefaultView) metadata;
							if (newDefaultView == null || newDefaultView.isEmpty())
								queryTemplate.getMetaData().remove(metadata);
							else if (!newDefaultView.equals(defaultView.getViewIdentification()))
								defaultView.setViewIdentification(newDefaultView);
							
							defaultViewFound = true;
						}
					}
					
					if (!autoPagingSizeFound && newSize != null) {
						AutoPagingSize autoPagingSize = transactionSession.create(AutoPagingSize.T);
						autoPagingSize.setSize(newSize);
						queryTemplate.getMetaData().add(autoPagingSize);
					}
					
					if (!defaultViewFound && newDefaultView != null && newDefaultView.isEmpty()) {
						DefaultView defaultView = transactionSession.create(DefaultView.T);
						defaultView.setViewIdentification(newDefaultView);
						queryTemplate.getMetaData().add(defaultView);
					}
				}

				/*************************** LocalizedString Methods ***************************/

				private boolean localizedStringValuesAreEqual(LocalizedString object1, LocalizedString object2) {
					// Check both LocalizedStrings
					if (object1 == null && object2 == null)
						return true;
					else if (object1 == null || object2 == null)
						return false;

					// Check size of both LocalizedString-Set
					if (object1.getLocalizedValues().size() != object2.getLocalizedValues().size())
						return false;

					// Compare values of both LocalizedStrings
					for (Entry<String, String> objectValue : object1.getLocalizedValues().entrySet()) {
						if (!object2.getLocalizedValues().containsKey(objectValue.getKey())
								|| !object2.getLocalizedValues().get(objectValue.getKey()).equals(objectValue.getValue())) {
							return false;
						}
					}

					return true;
				}
			};

			// Start working
			transactionWorker.setStorageHandle(storageHandle);
			transactionWorker.doTransaction(workbenchSession, workbench, future);
		}).onError(future::onFailure);

		return future;
	}

	/********************************* Helper Methods ********************************/

	private Future<WbSaveQueryDialogResult> openSaveDialog() {
		if (saveQueryDialog == null)
			saveQueryDialog = saveQueryDialogProvider.get();

		try {
			WbSaveQueryDialogConfig dialogConfig = new WbSaveQueryDialogConfig();
			dialogConfig.setDialogResult(new Future<>());
			dialogConfig.setWorkbenchSession(workbenchSession);
			dialogConfig.setWorkbench(workbench);

			saveQueryDialog.accept(dialogConfig);
			return dialogConfig.getDialogResult();
		} catch (final Exception e) {
			throw new WbStorageRuntimeException("Error while providing the Query.", e);
		}
	}

	private Future<WbSettingQueryDialogResult> openSettingDialog(final Folder queryFolder) {
		if (settingQueryDialog == null)
			settingQueryDialog = settingQueryDialogProvider.get();

		try {
			WbSettingQueryDialogConfig dialogConfig = new WbSettingQueryDialogConfig();
			dialogConfig.setDialogResult(new Future<>());
			dialogConfig.setWorkbenchSession(workbenchSession);
			dialogConfig.setWorkbench(workbench);
			dialogConfig.setQueryFolder(queryFolder);

			settingQueryDialog.accept(dialogConfig);
			return dialogConfig.getDialogResult();
		} catch (final Exception e) {
			throw new WbStorageRuntimeException("Error while providing the Query.", e);
		}
	}
	
	private void prepareDisplayPaths(ColumnDisplay columnDisplay, List<StorageColumnInfo> displayPath, PersistenceGmSession transactionSession) {
		int counter = 0;
		List<ColumnInfo> mdDisplayPath = columnDisplay.getDisplayPaths();
		List<ColumnInfo> pathsToRemove = new ArrayList<>(mdDisplayPath);
		for (StorageColumnInfo storageColumnInfo : displayPath) {
			ColumnInfo mdColumnInfo;
			if (mdDisplayPath.size() > counter)
				mdColumnInfo = mdDisplayPath.get(counter);
			else {
				mdColumnInfo = transactionSession.create(ColumnInfo.T);
				mdDisplayPath.add(mdColumnInfo);
			}
			
			if (!storageColumnInfo.getPath().equals(mdColumnInfo.getPath()))
				mdColumnInfo.setPath(storageColumnInfo.getPath());
			
			if (storageColumnInfo.getWidth() != mdColumnInfo.getWidth())
				mdColumnInfo.setWidth(storageColumnInfo.getWidth());
			
			if (storageColumnInfo.getTitle() != null) {
				LocalizedString title = mdColumnInfo.getTitle();
				if (title == null) {
					title = transactionSession.create(LocalizedString.T);
					mdColumnInfo.setTitle(title);
				} else if (!title.getLocalizedValues().isEmpty())
					title.getLocalizedValues().clear();
				
				title.getLocalizedValues().putAll(storageColumnInfo.getTitle().getLocalizedValues());
			}
			
			if (!pathsToRemove.isEmpty())
				pathsToRemove.remove(0);
			counter++;
		}
		
		if (!pathsToRemove.isEmpty())
			mdDisplayPath.removeAll(pathsToRemove);
	}
	
}
