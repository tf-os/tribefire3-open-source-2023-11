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

import com.braintribe.gwt.gme.notification.client.resources.NotificationBarStyle;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.notification.client.resources.NotificationTemplates;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Dialog used for showing a message while executing a request.
 * @author michel.docouto
 *
 */
public class MessageDialog extends Window implements com.braintribe.gwt.gmview.ddsarequest.client.message.MessageDialog {
	
	private ImageResource currentIcon;
	private String currentMessage;
	private Label headerLabel;
	private NotificationBarStyle headerStyle;
	private ManagedGmSession resourceSession;
	private String defaultMessage = LocalizedText.INSTANCE.executingServiceRequest();
	private ImageResource defaultIcon = NotificationResources.INSTANCE.infoBigCircle();
	protected VerticalPanel mainPanel;
	
	public MessageDialog() {
		setModal(true);
		setClosable(false);
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
		setResizable(false);
		setMinHeight(85);
		setPixelSize(500, 85);
		addStyleName("tabbedDialog");
		addStyleName("messageDialog");
		addStyleName("noHeader");
		
		BorderLayoutContainer container = new BorderLayoutContainer();
		container.setCenterWidget(prepareMainPanel());
		add(container);
		
		headerLabel = new Label();
		headerStyle = NotificationResources.INSTANCE.notificationBarMessageBig();
		
		mainPanel.add(headerLabel);
		
		currentIcon = defaultIcon;
		currentMessage = defaultMessage;
		headerLabel.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderMessageBar(headerStyle, getIconStyle(currentIcon),
				SafeHtmlUtils.fromSafeConstant(currentMessage), null));
	}
	
	/**
	 * Configures the required session used for streaming the icon. This is only used if the icon belongs to a {@link TransientPersistenceGmSession}.
	 */
	@Required
	public void setResourceSession(ManagedGmSession resourceSession) {
		this.resourceSession = resourceSession;
	}
	
	@Override
	public void setMessage(String message) {
		if (message == null)
			message = defaultMessage;
		
		if (currentMessage != message) {
			currentMessage = message;
			headerLabel.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderMessageBar(headerStyle, getIconStyle(currentIcon),
					SafeHtmlUtils.fromSafeConstant(currentMessage), null));
		}
	}
	
	@Override
	public void setIcon(Resource icon) {
		ImageResource imageResource;
		if (icon != null)
			imageResource = GMEIconUtil.transform(icon, getResourceSession(icon));
		else
			imageResource = defaultIcon;

		if (currentIcon != imageResource) {
			currentIcon = imageResource;
			headerLabel.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderMessageBar(headerStyle, getIconStyle(imageResource),
					SafeHtmlUtils.fromSafeConstant(LocalizedText.INSTANCE.running()), null));
		}
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
	
	protected VerticalPanel prepareMainPanel() {
		mainPanel = new VerticalPanel();
		mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		return mainPanel;
	}

}
