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
package com.braintribe.gwt.gme.constellation.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.bapi.AvailableAccesses;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class AccessChoiceDialog extends Window implements InitializableBean {
	
	interface DisplayValueProperties extends PropertyAccess<DisplayValue> {
		ModelKeyProvider<DisplayValue> value();
		ValueProvider<DisplayValue, String> display();
	}
	private static DisplayValueProperties props = GWT.create(DisplayValueProperties.class);
	
	private ListView<DisplayValue, String> accessList;
	private ListStore<DisplayValue> accessListStore;
	private TextButton chooseButton;
	private DisplayValue currentModel;
	//private HTML emptyPanel;
	private Future<String> accessId;
	private Supplier<Future<AvailableAccesses>> availableAccessesProvider;
	
	public AccessChoiceDialog() {
		setBorders(false);
		setBodyBorder(false);
		setHeading(LocalizedText.INSTANCE.chooseAccess());
		setClosable(false);
		setSize("300px", "300px");
	}
	
	/**
	 * Configures the required provider for providing the available accesses.
	 */
	@Required
	public void setAvailableAccessesProvider(Supplier<Future<AvailableAccesses>> availableAccessesProvider) {
		this.availableAccessesProvider = availableAccessesProvider;
	}
	
	@Override
	public void intializeBean() throws Exception {			
		addButton(getChooseButton());
	}
	
	public Future<String> getAccessId() {
		accessId = new Future<>();
		
		availableAccessesProvider.get() //
				.andThen(result -> {
					List<IncrementalAccess> accesses = result.getAccesses();
					if (accesses.isEmpty() || accesses.size() == 1) {
						accessId.onSuccess(accesses.isEmpty() ? null : accesses.get(0).getExternalId());
						return;
					}

					add(getAccessList());
					for (IncrementalAccess access : accesses) {
						String externalId = access.getExternalId();
						DisplayValue model = new DisplayValue(access.getName() + " (" + externalId + ")", externalId);
						getAccessListStore().add(model);
					}

					show();
				}).onError(accessId::onFailure);
		
		return accessId;
	}
	
	@SuppressWarnings("unused")
	public ListView<DisplayValue, String> getAccessList() {
		if (accessList != null)
			return accessList;
		
		accessList = new ListView<>(getAccessListStore(), props.display());
		accessList.setBorders(false);
		
		accessList.getSelectionModel().addSelectionChangedHandler(event -> {
			List<DisplayValue> selection = event.getSelection();
			currentModel = selection == null || selection.isEmpty() ? null : selection.get(0);
			getChooseButton().setEnabled(currentModel != null);
		});

		accessList.addDomHandler(event -> handleDoubleClick(), DoubleClickEvent.getType());
		
		new KeyNav(accessList) {
			@Override
			public void onEnter(NativeEvent evt) {
				handleDoubleClick();
			}
		};
		
		return accessList;
	}
	
	private void handleDoubleClick() {
		currentModel = accessList.getSelectionModel().getSelectedItem();
		if (currentModel != null) {
			accessId.onSuccess(currentModel.getValue().toString());
			hide();
		}
	}
	
	public ListStore<DisplayValue> getAccessListStore() {
		if (accessListStore == null)
			accessListStore = new ListStore<>(props.value());
		
		return accessListStore;
	}
	
	public TextButton getChooseButton() {
		if (chooseButton == null) {
			chooseButton = new TextButton(LocalizedText.INSTANCE.choose());
			chooseButton.setEnabled(false);
			chooseButton.addSelectHandler(event -> {
				accessId.onSuccess(currentModel.getValue().toString());
				hide();
			});
		}
		return chooseButton;
	}
	
	/*private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noItemsToDisplay()).append("</div></div>");
			
			emptyPanel = new HTML(html.toString());
		}
		
		return emptyPanel;
	}*/
	
	public class DisplayValue {
		private String display;
		private String value;
		
		public DisplayValue(String display, String value) {
			setDisplay(display);
			setValue(value);
		}
		
		public String getDisplay() {
			return display;
		}
		
		public void setDisplay(String display) {
			this.display = display;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
