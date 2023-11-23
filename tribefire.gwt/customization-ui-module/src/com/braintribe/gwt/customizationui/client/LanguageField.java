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
package com.braintribe.gwt.customizationui.client;

import com.braintribe.gwt.customizationui.client.NameAndCode.NameAndCodeProperties;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;

/**
 * This comboBox changes the application language.
 * @author michel.docouto
 *
 */
public class LanguageField extends ComboBox<NameAndCode> implements InitializableBean, DisposableBean {
	private static final String LOCALE = "locale=";
	
	private String defaultLanguageCode;
	private HandlerRegistration changeHandlerRegistration;
	private static NameAndCodeProperties props = GWT.create(NameAndCodeProperties.class);
	
	public LanguageField() {
		super(new ListStore<NameAndCode>(props.code()), props.name());
		setTriggerAction(TriggerAction.ALL);
		setAllowBlank(false);
		setForceSelection(true);
		setEditable(false);
	}
	
	@Override
	public void intializeBean() throws Exception {
		String currentLocale = getCurrentLocale();
		if (currentLocale == null)
			currentLocale = defaultLanguageCode;
		
		setValue(findModel(currentLocale));
		
		changeHandlerRegistration = addChangeHandler(event -> {
			String oldUrl = Window.Location.getHref();
			String oldQuery = Window.Location.getQueryString();
			String newLocale = LOCALE + LanguageField.this.getValue().getCode();
			String newUrl;
			if (oldQuery.contains(LOCALE)) {
				String partialQuery = oldQuery.substring(oldQuery.indexOf(LOCALE));
				String oldLocale;
				if (partialQuery.contains("&"))
					oldLocale = partialQuery.substring(0, partialQuery.indexOf("&"));
				else
					oldLocale = oldQuery.substring(oldQuery.indexOf(LOCALE));
				newUrl = oldUrl.replace(oldLocale, newLocale);
			} else {
				String newQuery;
				if (oldQuery.isEmpty()) {
					newQuery = "&" + newLocale;
					int hashPos = oldUrl.indexOf("#");
					if (hashPos != -1)
						newUrl = oldUrl.replace("#", newQuery + "#");
					else
						newUrl = oldUrl + "?" + newLocale;
				} else {
					newQuery = oldQuery + "&" + newLocale;
					newUrl = oldUrl.replace(oldQuery, newQuery);
				}
			}
			Window.Location.replace(newUrl);
		});
	}
	
	/**
	 * Configures the required default language to be used in the application.
	 */
	@Required
	public void setDefaultLanguageCode(String defaultLanguageCode) {
		this.defaultLanguageCode = defaultLanguageCode;
	}
	
	/**
	 * Configures the languages to be presented in the comboBox.
	 */
	@Configurable
	public void addLanguage(String name, String code) {
		NameAndCode model = new NameAndCode(name, code);
		getStore().add(model);
	}
	
	@Override
	public void disposeBean() throws Exception {
		changeHandlerRegistration.removeHandler();
	}
	
	private String getCurrentLocale() {
		String query = Window.Location.getQueryString();
		int localePos = query.indexOf(LOCALE);
		if (localePos == -1) {
			return null;
		}
		
		String partialQuery = query.substring(localePos + LOCALE.length());
		if (partialQuery.contains("&"))
			return partialQuery.substring(0, partialQuery.indexOf("&"));
		else
			return partialQuery;
	}
	
	private NameAndCode findModel(String localeCode) {
		for (NameAndCode model : getStore().getAll()) {
			if (model.getCode().equals(localeCode))
				return model;
		}
		
		return null;
	}
	
}
