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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.LocalizedText;
import com.braintribe.gwt.gme.propertypanel.client.PropertyModel;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanelGrid;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.cell.core.client.form.TextInputCell.TextFieldAppearance;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;

/**
 * Field used for showing a Quick Access Dialog. Also, the text typed in here is the one used for filtering in there.
 * @author michel.docouto
 *
 */
public class QuickAccessTriggerField extends TextField implements NoBlurWhileEditingField {
	
	public interface QuickAccessTriggerFieldListener {
		void onQuickAccessResult(QuickAccessResult result);
	}
	
	private static final int DEFAULT_MIN_CHARS_FOR_FILTER = 1;
	private static int KEY_CODE_SPACE = 32;
	protected static final int TIMER_INTERVAL = 500;
	private static Logger logger = new Logger(QuickAccessTriggerField.class);
	
	private int minCharsForFilter = DEFAULT_MIN_CHARS_FOR_FILTER;
	private boolean editing = false;
	private Timer keyPressedTimer;
	private List<QuickAccessTriggerFieldListener> quickAccessTriggerFieldListeners;
	private QuickAccessDialog quickAccessDialog;
	private Supplier<SpotlightPanel> quickAccessPanelSupplier;
	private SpotlightPanel quickAccessPanel;
	private TypeCondition typeCondition;
	private String instantiationLabel;
	private boolean useNavigationButtons = true;
	private boolean useAsModal = false;
	private boolean addingToCollection = false;
	private boolean handleDetach;
	private Component textField = this;
	private HandlerRegistration focusHandlerRegistration;
	private HandlerRegistration keyPressHandlerRegistration;
	private HandlerRegistration keyDownHandlerRegistration;
	
	public QuickAccessTriggerField(TextFieldAppearance textFieldAppearance) {
		super(new TextInputCell(textFieldAppearance));
		init();
	}
	
	public QuickAccessTriggerField() {
		init();
		
		addAttachHandler(event -> {
			if (!event.isAttached() && quickAccessDialog != null && handleDetach) {
				handleDetach = false;
				quickAccessDialog.handleEscape();
			}
		});
	}
	
	/**
	 * Configures the required {@link SpotlightPanel} to be connected with this field.
	 */
	@Required
	public void setQuickAccessPanel(Supplier<SpotlightPanel> quickAccessPanelSupplier) {
		this.quickAccessPanelSupplier = quickAccessPanelSupplier;
	}
	
	/**
	 * Configures the TypeCondition to be used within the Quick Access Dialog. If no type condition is set, then a default one is prepared,
	 * which gets all GenericEntity.
	 */
	@Configurable
	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}
	
	@Configurable
	public void setUseNavigationButtons(boolean useNavigationButtons) {
		this.useNavigationButtons = useNavigationButtons;
	}
	
	public void setInstantiationLabel(String instantiationLabel) {
		this.instantiationLabel = instantiationLabel;
	}
	
	/**
	 * Configures the minimum number of chars entered for the filter to take place. Defaults to 1.
	 */
	@Configurable
	public void setMinCharsForFilter(int minCharsForFilter) {
		this.minCharsForFilter = minCharsForFilter;
	}
	
	/**
	 * Configures whether to use the dialog as modal. Defaults to false.
	 */
	@Configurable
	public void setUseAsModal(boolean useAsModal) {
		this.useAsModal = useAsModal;
	}
	
	/**
	 * Configures whether this field is adding to a collection. Defaults to false.
	 * If true, then while editing, setValue does not fire events.
	 */
	@Configurable
	public void setAddingToCollection(boolean addingToCollection) {
		this.addingToCollection = addingToCollection;
	}
	
	private void init() {
		setHeight(22); //synchronize with BT logo
		setEmptyText(LocalizedText.INSTANCE.quickAccess());
		
		focusHandlerRegistration = addFocusHandler(event -> {
			if (QuickAccessTriggerField.this.getText().trim().length() >= minCharsForFilter)
				onTriggerClick();
		});
		
		keyPressHandlerRegistration = addKeyPressHandler(event -> Scheduler.get().scheduleDeferred(() -> {
			if (QuickAccessTriggerField.this.getText().trim().length() >= minCharsForFilter)
				getKeyPressedTimer().schedule(TIMER_INTERVAL);
		}));
		
		keyDownHandlerRegistration = this.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KEY_CODE_SPACE && event.isControlKeyDown()) {
				event.stopPropagation();
				event.preventDefault();
				onTriggerClick();
			}
		});
	}
	
	public void addQuickAccessTriggerFieldListener(QuickAccessTriggerFieldListener listener) {
		if (quickAccessTriggerFieldListeners == null)
			quickAccessTriggerFieldListeners = new ArrayList<>();
		
		quickAccessTriggerFieldListeners.add(listener);
	}
	
	public void removeQuickAccessTriggerFieldListener(QuickAccessTriggerFieldListener listener) {
		if (quickAccessTriggerFieldListeners != null)
			quickAccessTriggerFieldListeners.remove(listener);
	}
	
	@Override
	public boolean isEditingField() {
		return editing;
	}
	
	@Override
	public void setValue(String value, boolean fireEvents, boolean redraw) {
		if (editing && addingToCollection)
			fireEvents = false;
		
		super.setValue(value, fireEvents, redraw);
		
		if (textField != this && textField != null)
			((HasText) textField).setText(value);
	}
	
	/**
	 * Configures the external field. Notice that this {@link Component} must also be a {@link HasText} instance.
	 */
	public void configureExternalField(Component externalField) {
		if (externalField == textField)
			return;
		
		if (textField != null) {
			if (focusHandlerRegistration != null) {
				focusHandlerRegistration.removeHandler();
				focusHandlerRegistration = null;
			}
			
			if (keyDownHandlerRegistration != null) {
				keyDownHandlerRegistration.removeHandler();
				keyDownHandlerRegistration = null;
			}
			
			if (keyPressHandlerRegistration != null) {
				keyPressHandlerRegistration.removeHandler();
				keyPressHandlerRegistration = null;
			}
		}
		
		textField = externalField;
		
		if (externalField != null) {
			focusHandlerRegistration = externalField.addFocusHandler(event -> {
				if (((HasText) externalField).getText().trim().length() >= minCharsForFilter)
					onTriggerClick();
			});
			
			keyDownHandlerRegistration = externalField.addDomHandler(event -> {
				Scheduler.get().scheduleDeferred(() -> {
					if (((HasText) externalField).getText().trim().length() >= minCharsForFilter)
						getKeyPressedTimer().schedule(TIMER_INTERVAL);
				});
			}, KeyDownEvent.getType());
		}
		
		if (quickAccessPanel != null)
			quickAccessPanel.setTextField(externalField);
	}
	
	private Timer getKeyPressedTimer() {
		if (keyPressedTimer == null) {
			keyPressedTimer = new Timer() {
				@Override
				public void run() {
					onTriggerClick();
				}
			};
		}
		
		return keyPressedTimer;
	}
	
	protected void onTriggerClick() {
		if (editing)
			return;
		
		editing = true;
		if (typeCondition == null)
			typeCondition = getQuickAccessPanel().prepareTypeCondition(GenericEntity.T);
		handleDetach = true;
		
		Widget parentWidget = textField != null ? textField : this;
		
		getQuickAccessDialog().getQuickAccessResult(typeCondition, parentWidget, addingToCollection) //
				.andThen(result -> {
					handleDetach = false;
					quickAccessDialog.configureHandleKeyPress(false);
					fireQuickAccessResult(result);

					Widget parent = getParent();
					if (!addingToCollection || !(parent instanceof PropertyPanelGrid))
						return;

					PropertyPanelGrid grid = (PropertyPanelGrid) parent;

					boolean cancelPerformed = false;
					if (result != null) {
						GridCell gridCell = grid.getActiveCell();
						if (gridCell != null) {
							PropertyModel model = grid.getStore().get(gridCell.getRow());
							grid.cancelEditing();
							cancelPerformed = true;
							grid.handleCollectionEdition(result.getObject(), model, gridCell);
						}
					}

					if (!cancelPerformed)
						grid.cancelEditing();
				}).onError(e -> {
					editing = false;
					logger.error("Error while preparing quickAccess entries.", e);
				});
		
		quickAccessDialog.configureHandleKeyPress(true);
		if (textField instanceof InputFocusHandler)
			((InputFocusHandler) textField).focusInput();
		else
			getInputEl().focus();
	}
	
	private SpotlightPanel getQuickAccessPanel() {
		if (quickAccessPanel != null)
			return quickAccessPanel;
		
		quickAccessPanel = quickAccessPanelSupplier.get();
		return quickAccessPanel;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel quickAccessPanel = getQuickAccessPanel();
			quickAccessPanel.setMinCharsForFilter(minCharsForFilter);
			quickAccessPanel.setTextField(textField);
			return quickAccessPanel;
		};
	}
	
	private QuickAccessDialog getQuickAccessDialog() {
		if (quickAccessDialog != null)
			return quickAccessDialog;
		
		SpotlightPanel quickAccessPanel = getQuickAccessPanel();
		
		quickAccessDialog = new QuickAccessDialog();
		quickAccessDialog.setModal(useAsModal);
		quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
		quickAccessDialog.addStyleName(PropertyPanelResources.INSTANCE.css().border());
		if (quickAccessPanel.getUseCase() != null)
			quickAccessDialog.configureUseCase(quickAccessPanel.getUseCase());
		
		quickAccessDialog.setFocusWidget(this);
		quickAccessDialog.setUseApplyButton(quickAccessPanel.getUseApplyButton());
		quickAccessDialog.setUseNavigationButtons(useNavigationButtons);
		
		if (instantiationLabel != null)
			quickAccessDialog.setInstantiateButtonLabel(instantiationLabel);
		
		quickAccessDialog.addHideHandler(event -> {
			clear();
			new Timer() {
				@Override
				public void run() {
					Scheduler.get().scheduleDeferred(() -> {
						editing = false;
						blur();
					});
				}
			}.schedule(501);
		});
		
		try {
			quickAccessDialog.intializeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return quickAccessDialog;
	}
	
	private void fireQuickAccessResult(QuickAccessResult result) {
		if (quickAccessTriggerFieldListeners != null)
			quickAccessTriggerFieldListeners.forEach(listener -> listener.onQuickAccessResult(result));
	}

}
