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
package com.braintribe.gwt.aceeditor.client;

import java.text.ParseException;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableInsideTriggerField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gmview.util.client.TextResourceManager;
import com.braintribe.gwt.gxt.gxtresources.css.GxtResources;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.StringSource;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

import tribefire.extension.scripting.model.deployment.Script;

public class GmScriptEditor extends TriggerField<Script> implements ClickableInsideTriggerField, TriggerFieldAction {
	
	private Action triggerAction;
	private Script script;
	private GmScriptEditorDialog dialog;
	private HandlerRegistration hideHandlerRegistration;
	private HideHandler hideHandler;
	private AbstractGridEditing<?> gridEditing;
	private GridCell gridCell;
	
	public GmScriptEditor() {
		super(new PropertyEditor<Script>() {
			@Override
			public Script parse(CharSequence text) throws ParseException {
				return null;
			}
			
			@Override
			public String render(Script object) {
				return null;
			}
		});
		
		addTriggerClickHandler(event -> handleTriggerClick());
	}
	
	public void setScriptEditorDialog(GmScriptEditorDialog dialog) {
		this.dialog = dialog;
	}
	
	@Override
	public void setValue(Script value) {
		this.script = value;
		super.setValue(value);
	}
	
	public void handleTriggerClick() {
		new Timer() {
			@Override
			public void run() {		
				gridEditing.cancelEditing();		
				new Timer() {
					@Override
					public void run() {
						GmScriptEditorDialog scriptDialog = getDialog();
						scriptDialog.setEntityValue(script);
						scriptDialog.setReadOnly(isReadOnly());
						scriptDialog.show();
						if (isRendered())
							getInputEl().focus();
					}					
				}.schedule(100);
			}
		}.schedule(100);						
	}
	
	private GmScriptEditorDialog getDialog() {
		if (dialog != null) {
			if (hideHandlerRegistration == null)
				hideHandlerRegistration = dialog.addHideHandler(getHideHandler());
			
			return dialog;
		}
		
		return null;
	}
	
	private HideHandler getHideHandler() {
		if (hideHandler != null)
			return hideHandler;
		
		hideHandler = (HideHandler) event -> {
			boolean hasChanges = dialog.hasChanges();
			new Timer() {
				@Override
				public void run() {
					if (hasChanges) {
						if (script.getSource() == null) {
							Resource resource = Resource.T.create();
							resource.setResourceSource(StringSource.T.create());
							script.setSource(resource);
						}
						
						Property prop = script.entityType().getProperty("source");
						TextResourceManager.saveResourceContent(dialog.getScriptText(), script.getSource(), prop, script).get(AsyncCallbacks.of(v -> {
							GmScriptEditor.super.setValue(script);
						}, e -> {
							ErrorDialog.show(GmScriptEditorLocalizedText.INSTANCE.saveError(), e);
						}));
					}
					if (!hasChanges)
						blur();
					
					hideHandlerRegistration.removeHandler();
					hideHandlerRegistration = null;
				}
						
			}.schedule(150);
		};
		
		return hideHandler;
	}

	@Override
	public TriggerField<?> getTriggerField() {
		return this;
	}

	@Override
	public void fireTriggerClick(NativeEvent event) {
		handleTriggerClick();
	}

	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				gridEditing.startEditing(gridCell);
				Scheduler.get().scheduleDeferred(() -> new Timer() {
					@Override
					public void run() {
						handleTriggerClick();
						if (isReadOnly())
							gridEditing.cancelEditing();
					}
				}.schedule(100));
			}
		};
		
		triggerAction.setIcon(GxtResources.INSTANCE.multiLine());
		triggerAction.setName(LocalizedText.INSTANCE.multiLineEditor());
		triggerAction.setTooltip(LocalizedText.INSTANCE.multiLineEditor());
		triggerAction.setHidden(true);
		
		return triggerAction;
	}

	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
		
		if (dialog != null)
			dialog.configureGridEditing(gridEditing);			
	}

	@Override
	public boolean canFireTrigger() {
		if (dialog == null)
			return false;
		
		return dialog.canFireDialog();
	}	
}
