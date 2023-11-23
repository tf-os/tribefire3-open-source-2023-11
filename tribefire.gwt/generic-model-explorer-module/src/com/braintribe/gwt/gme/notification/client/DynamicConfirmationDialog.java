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
package com.braintribe.gwt.gme.notification.client;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.notification.client.resources.NotificationBarStyle;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.notification.client.resources.NotificationTemplates;
import com.braintribe.gwt.gmview.ddsarequest.client.confirmation.ConfirmationExpert.DynamicConfirmationData;
import com.braintribe.gwt.gmview.ddsarequest.client.confirmation.ResourceSessionConfig;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.uiservice.ConfirmationData;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 * Extension of the {@link ConfirmationDialog} prepared for {@link ConfirmationData}.
 * @author michel.docouto
 *
 */
public class DynamicConfirmationDialog extends ConfirmationDialog implements ResourceSessionConfig, Function<DynamicConfirmationData, Future<Boolean>> {
	
	private TextArea textArea;
	private Label headerLabel;
	private NotificationBarStyle headerStyle;
	private ManagedGmSession resourceSession;
	
	public DynamicConfirmationDialog() {
		headerLabel = new Label();
		headerStyle = NotificationResources.LEVEL_STYLES_BIG.get(Level.INFO);
		
		textArea = new TextArea();
		textArea.setReadOnly(true);
		textArea.setBorders(false);
		textArea.setHeight("200px");
		textArea.setWidth("500px");
		
		mainPanel.add(headerLabel);
		mainPanel.add(textArea);
	}
	
	/**
	 * Configures the required session used for streaming the icon. This is only used if the icon belongs to a {@link TransientPersistenceGmSession}.
	 */
	@Override
	public void setResourceSession(ManagedGmSession resourceSession) {
		this.resourceSession = resourceSession;
	}
	
	@Override
	public Future<Boolean> apply(DynamicConfirmationData confirmation) {
		LocalizedString ok = confirmation.dynamicConfirmation.getOKDisplay();
		if (ok != null)
			okButton.setText(I18nTools.getLocalized(ok));
		
		LocalizedString cancel = confirmation.dynamicConfirmation.getCancelDisplay();
		if (cancel != null)
			cancelButton.setText(I18nTools.getLocalized(cancel));
		
		ImageResource imageResource;
		Resource resource = confirmation.confirmationData.getIcon();
		if (resource != null) {
			imageResource = GMEIconUtil.transform(resource, getResourceSession(resource));
		} else
			imageResource = NotificationResources.INSTANCE.infoBigCircle();
		
		String message = confirmation.confirmationData.getMessage();
		configureTextAreaLayoutAndMessage(textArea, message);
		
		headerLabel.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderConfirmationBar(headerStyle, getIconStyle(imageResource),
				SafeHtmlUtils.fromSafeConstant(LocalizedText.INSTANCE.confirmation()), null));
		
		setConfirmationMouseClick(confirmation.dynamicConfirmation.getMouseClick());
		
		return getConfirmation();
	}
	
	private ManagedGmSession getResourceSession(Resource resource) {
		if (resource.session() instanceof TransientPersistenceGmSession || !(resource.session() instanceof ManagedGmSession))
			return resourceSession;
		
		return (ManagedGmSession) resource.session();
	}

	private SafeStyles getIconStyle(ImageResource icon) {
		return icon == null ? new SafeStylesBuilder().toSafeStyles()
				: new SafeStylesBuilder().backgroundImage(icon.getSafeUri())
						.append(SafeStylesUtils.fromTrustedNameAndValue("backgroundRepeat", "no-repeat")).toSafeStyles();
	}

}
