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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.keyconfiguration;

import java.text.ParseException;

import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gmview.codec.client.KeyConfigurationRendererCodec;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.KeyConfiguration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;

/**
* Extends the {@link TriggerField} by adding a {@link KeyConfiguration}
* Field converts pressed key combination into {@link KeyConfiguration} 
*
*/
public class KeyConfigurationField extends TriggerField<KeyConfiguration> implements TrackableChangesAction {
		
	private PersistenceGmSession gmSession;
	private KeyConfiguration fieldKeyConfiguration;
	private boolean gettingOldValue = false;
	private KeyConfiguration oldKeyConfiguration = null;
	private boolean hasChanges = false;
	
	public KeyConfigurationField() {
		super(new TriggerFieldCell<KeyConfiguration>());
		
		setPropertyEditor(new PropertyEditor<KeyConfiguration>() {
			@Override
			public KeyConfiguration parse(CharSequence text) throws ParseException {
				return fieldKeyConfiguration;
			}
			
			@Override
			public String render(KeyConfiguration keyConfiguration) {
				return keyConfiguration == null ? "" : KeyConfigurationRendererCodec.encodeKeyConfiguration(keyConfiguration);
			}
		});
		
		addKeyDownHandler(new KeyDownHandler() {			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				handleKeyDown(event);
				event.stopPropagation();
				event.preventDefault();
			}
		});
		setAutoValidate(true);
	}		

	private void handleKeyDown(KeyDownEvent event) {
		int code = event.getNativeKeyCode();
		if (KeyCodes.KEY_CTRL == code || KeyCodes.KEY_SHIFT == code || KeyCodes.KEY_ALT == code 
			|| KeyCodes.KEY_WIN_KEY_LEFT_META == code || KeyCodes.KEY_MAC_FF_META == code) {			
			return;
		}
		
		if ((KeyCodes.KEY_ENTER == code || KeyCodes.KEY_TAB == code)  
			 && !event.isControlKeyDown() && !event.isShiftKeyDown() && !event.isAltKeyDown() && !event.isMetaKeyDown()) {			
			return;
		}
		
		if (fieldKeyConfiguration == null) {
			hasChanges = true;
			fieldKeyConfiguration = gmSession.create(KeyConfiguration.T);
		}
		
		hasChanges = true;
		fieldKeyConfiguration.setCtrl(event.isControlKeyDown());
		fieldKeyConfiguration.setShift(event.isShiftKeyDown());
		fieldKeyConfiguration.setAlt(event.isAltKeyDown());
		fieldKeyConfiguration.setMeta(event.isMetaKeyDown());
		fieldKeyConfiguration.setKeyCode(event.getNativeKeyCode());

		Scheduler.get().scheduleDeferred(() -> {
			gettingOldValue = true;
			setValue(fieldKeyConfiguration, true);
			setText(KeyConfigurationRendererCodec.encodeKeyConfiguration(fieldKeyConfiguration));			
			gettingOldValue = false;
		});
	}	
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public void setValue(KeyConfiguration keyConfiguration) {
		if (keyConfiguration == null)
			oldKeyConfiguration = null;
		else
			oldKeyConfiguration = (KeyConfiguration) KeyConfiguration.T.clone(keyConfiguration, null, null);
		
		hasChanges = false;
		fieldKeyConfiguration = keyConfiguration;
		super.setValue(keyConfiguration);
	}
		
	@Override
	public KeyConfiguration getValue() {
	  if (gettingOldValue) {
			return oldKeyConfiguration;
      }
	  return super.getValue();
	}
	
	
	@Override
	public boolean hasChanges() {
		return hasChanges;
	}
}
