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
package com.braintribe.gwt.logging.ui.gxt.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmMainDetailView;
import com.braintribe.gwt.gmview.client.GmTemplateMetadataViewSupport;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.OrangeFlatTabPanelAppearance;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.resources.LoggingResources;
import com.braintribe.gwt.logging.ui.gxt.client.resources.GxtErrorResources;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Dialog used for showing {@link ReasonException}s.
 * @author michel.docouto
 *
 */
public class GxtReasonErrorDialog extends ClosableWindow implements IgnoreKeyConfigurationDialog {
	
	interface ReasonErrorXTemplate extends XTemplates {
		@XTemplate(source = "com/braintribe/gwt/logging/ui/gxt/bt-resources/error-log/errorReasonPanel.html")
	    SafeHtml errorReason(ErrorReasonContext context);
	}
	
	private static Supplier<? extends GmContentView> gmContentViewSupplier;
	private GmContentView gmContentView;
	private static BorderLayoutContainer container;
	private TextButton maximizeButton;
	private TextButton restoreButton;
	private TextButton showAsTextButton;
	private static BorderLayoutContainer centerContainer;
	private TextArea textArea;
	private boolean showingAsText;
	private String message;
	private Throwable error;
	private boolean details;
	private PlainTabPanel tabPanel;
	private static GxtReasonErrorDialog dialog;
	private static ReasonErrorXTemplate template = GWT.create(ReasonErrorXTemplate.class);
	private static ImageResource defaultIcon = GxtErrorResources.INSTANCE.info();
	private static IconProvider iconProvider;
	private static Supplier<? extends IconProvider> iconSupplier;
	private static final String REASON_ERROR_USECASE = "reasonErrorPanel";
	private static PersistenceGmSession gmSession;
	private static HTMLPanel overviewPanel;
	private ToolBar toolBar;
	
	protected static class ErrorReasonContext {
		
		private SafeStyles iconStyle;
		private String hint;
		private String typeSignature;
		private String text;
		
		public ErrorReasonContext(SafeStyles iconStyle, String hint, String typeSignature, String text) {
			this.iconStyle = iconStyle;
			this.hint = hint;
			this.typeSignature = typeSignature;
			this.text = text;
		}
		
		public SafeStyles getIconStyle() {
			return iconStyle;
		}
		
		public String getHint() {
			return hint;
		}
		
		public String getTypeSignature() {
			return typeSignature;
		}
		
		public String getText() {
			return text;
		}
	}
	
	protected GxtReasonErrorDialog(String message, Throwable throwable, boolean details) {
		setModal(true);
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(false);
		setSize("1000px", "500px");
		
		container = new BorderLayoutContainer();
		container.setSouthWidget(getToolbar(), new BorderLayoutData(61));
		updateButtons();
		add(container);
		
		tabPanel = new PlainTabPanel(GWT.<OrangeFlatTabPanelAppearance>create(OrangeFlatTabPanelAppearance.class));
		tabPanel.setTabScroll(true);
		tabPanel.setBorders(false);
		tabPanel.setBodyBorder(false);
		tabPanel.addSelectionHandler(event -> {
			showAsTextButton.setVisible(event.getSelectedItem() == centerContainer);
			toolBar.forceLayout();
		});
		container.setCenterWidget(tabPanel);
		
		centerContainer = new BorderLayoutContainer();
		overviewPanel = new HTMLPanel("");
		tabPanel.add(overviewPanel, LocalizedText.INSTANCE.overview());
		tabPanel.add(centerContainer, LocalizedText.INSTANCE.advanced());
		container.setCenterWidget(tabPanel);
		
		prepareCenterWidget();
		this.message = message;
		this.error = throwable;
		this.details = details;
	}
	
	/**
	 * Configures the required supplier for the {@link GmContentView} used in the dialog.
	 */
	@Required
	public static void setGmContentViewSupplier(Supplier<? extends GmContentView> gmContentViewSupplier) {
		GxtReasonErrorDialog.gmContentViewSupplier = gmContentViewSupplier;
	}
	
	/**
	 * Configures the required provider which will provide icons.
	 */
	@Required
	public static void setIconProvider(Supplier<? extends IconProvider> supplier) {
		iconSupplier = supplier;
	}

	/**
	 * Configures the required session used for resolving metadata.
	 */
	@Required
	public static void setGmSession(PersistenceGmSession session) {
		gmSession = session;
	}

	/**
	 * Consumer implementation of this dialog. It handle {@link Reason}.
	 */
	public static void accept(ModelPath modelPath) {
		if (modelPath == null || !(modelPath.last().getValue() instanceof Reason))
			return;
		
		Reason reason = modelPath.last().getValue();
		ReasonException ex = new ReasonException(reason);
		show(reason.getText(), ex, false);
	}
	
	/**
	 * Shows a modal dialog with a {@link GmContentView} showing the exception.
	 */
	public static void show(String message, Throwable t, boolean details) {
		if (dialog == null)
			dialog = new GxtReasonErrorDialog(message, t, details);
		else {
			dialog.message = message;
			dialog.error = t;
			dialog.details = details;
		}
		
		prepareAndShowDialog(dialog, t);
	}
	
	/**
	 * Shows a modal dialog with a {@link GmContentView} showing the exception.
	 */
	public static void show(String message, Throwable t) {
		show(message, t, false);
	}
	
	private void prepareCenterWidget() {
		if (gmContentView != null)
			return;
		
		gmContentView = gmContentViewSupplier.get();
		centerContainer.setCenterWidget((Widget) gmContentView);
	}
	
	private static IconProvider getIconProvider() {
		if (iconProvider != null)
			return iconProvider;
		
		iconProvider = iconSupplier.get();
		return iconProvider;
	}
	
	private static void prepareOverviewPanel(ModelPath modelPath) {
		ImageResource icon = defaultIcon;
		
		IconAndType iconAndType = getIconProvider().apply(modelPath);
		if (iconAndType != null && iconAndType.getIcon() != null)
			icon = iconAndType.getIcon();
		
		ModelPathElement modelPathElement = modelPath.last();
		
		Image img = new Image(icon);
		SafeStyles iconStyle = new SafeStylesBuilder().backgroundImage(UriUtils.fromTrustedString(img.getUrl())).toSafeStyles();
		
		Pair<Name, Description> pair = GMEMetadataUtil.getNameAndDescription(modelPathElement.getValue(), modelPathElement.getType(),
				gmSession.getModelAccessory().getMetaData(), REASON_ERROR_USECASE);
		
		String typeSignature = ((EntityType<?>) modelPathElement.getType()).getShortName();
		Reason reason = modelPathElement.getValue();
		if (pair.first != null)
			typeSignature = I18nTools.getLocalized(pair.first.getName()) + " (" + typeSignature + ")";
		String hint = "";
		if (pair.second != null)
			hint = I18nTools.getLocalized(pair.second.getDescription());
		
		ErrorReasonContext context = new ErrorReasonContext(iconStyle, hint, typeSignature, reason.getText());
		
		overviewPanel.getElement().setInnerHTML(template.errorReason(context).asString());
	}
	
	private ToolBar getToolbar() {
		if (toolBar != null)
			return toolBar;
		
		toolBar = new ToolBar();
		toolBar.add(new FillToolItem());
		toolBar.add(prepareShowAsTextButton());
		toolBar.add(prepareAdvancedButton());
		toolBar.add(prepareMaximizeButton());
		toolBar.add(prepareRestoreButton());
		toolBar.add(prepareCloseButton());
		return toolBar;
	}
	
	private void updateButtons() {
		maximizeButton.setVisible(isMaximized() == false);
		restoreButton.setVisible(isMaximized() == true);
	}
	
	private TextButton prepareMaximizeButton() {
		maximizeButton = new TextButton(LocalizedText.INSTANCE.maximize());
		maximizeButton.setIconAlign(IconAlign.TOP);
		maximizeButton.setScale(ButtonScale.LARGE);
		maximizeButton.setIcon(LoggingResources.INSTANCE.maximize());
		maximizeButton.addSelectHandler(event -> {
			maximize();
			updateButtons();
		});
		return maximizeButton;
	}
	
	private TextButton prepareRestoreButton() {
		restoreButton = new TextButton(LocalizedText.INSTANCE.restore());
		restoreButton.setIconAlign(IconAlign.TOP);
		restoreButton.setScale(ButtonScale.LARGE);
		restoreButton.setIcon(LoggingResources.INSTANCE.restore());
		restoreButton.addSelectHandler(event -> {
			restore();
			updateButtons();
		});
		return restoreButton;
	}
	
	private TextButton prepareShowAsTextButton() {
		showAsTextButton = new TextButton(LocalizedText.INSTANCE.showAsText());
		showAsTextButton.setIconAlign(IconAlign.TOP);
		showAsTextButton.setScale(ButtonScale.LARGE);
		showAsTextButton.setIcon(GMGxtSupportResources.INSTANCE.show());
		showAsTextButton.addSelectHandler(event -> handleShowAsText());
		
		return showAsTextButton;
	}

	private void handleShowAsText() {
		showingAsText = !showingAsText;
		showAsTextButton.setText(showingAsText ? LocalizedText.INSTANCE.hideText() : LocalizedText.INSTANCE.showAsText());
		showAsTextButton.setIcon(showingAsText ? GMGxtSupportResources.INSTANCE.hide() : GMGxtSupportResources.INSTANCE.show());
		TextArea textArea = getTextArea();
		if (showingAsText) {
			textArea.setValue(prepareTextFromError());
			BorderLayoutData layoutData = new BorderLayoutData(61);
			layoutData.setMargins(new Margins(0, 4, 0, 4));
			centerContainer.setSouthWidget(textArea, layoutData);
		} else
			centerContainer.remove(textArea);
		container.forceLayout();
	}
	
	private TextButton prepareAdvancedButton() {
		TextButton advancedButton = new TextButton(LocalizedText.INSTANCE.showError());
		advancedButton.setIconAlign(IconAlign.TOP);
		advancedButton.setScale(ButtonScale.LARGE);
		advancedButton.setIcon(GxtErrorResources.INSTANCE.info());
		advancedButton.addSelectHandler(event -> GxtErrorDialog.show(message, error, details));
		
		return advancedButton;
	}
	
	private TextButton prepareCloseButton() {
		TextButton closeButton = new TextButton(LocalizedText.INSTANCE.close());
		closeButton.setIconAlign(IconAlign.TOP);
		closeButton.setScale(ButtonScale.LARGE);
		closeButton.setIcon(LoggingResources.INSTANCE.delete());
		closeButton.addSelectHandler(event -> hide());
		return closeButton;
	}
	
	private TextArea getTextArea() {
		if (textArea != null)
			return textArea;
		
		textArea = new TextArea();
		textArea.setReadOnly(true);
		return textArea;
	}
	
	private static void prepareAndShowDialog(GxtReasonErrorDialog dialog, Throwable error) {
		ModelPath modelPath = new ModelPath();
		ReasonException reasonException = (ReasonException) error;
		Reason reason = reasonException.getReason();
		modelPath.add(new RootPathElement(Reason.T, reason));
		boolean hasSubReasons = reason.getReasons() != null && !reason.getReasons().isEmpty();
		
		prepareOverviewPanel(modelPath);
		dialog.tabPanel.setActiveWidget(overviewPanel);
		
		if (dialog.gmContentView instanceof GmMainDetailView)
			((GmMainDetailView) dialog.gmContentView).setMainViewVisibility(hasSubReasons);
		if (hasSubReasons) {
			if (dialog.gmContentView instanceof GmTemplateMetadataViewSupport) {
				AutoExpand autoExpand = AutoExpand.T.create();
				autoExpand.setDepth(Integer.toString(Integer.MAX_VALUE));
				((GmTemplateMetadataViewSupport) dialog.gmContentView).setAutoExpand(autoExpand);
			}
		}
		dialog.gmContentView.setContent(modelPath);
		
		if (hasSubReasons && dialog.gmContentView instanceof GmCondensationView) {
			Scheduler.get().scheduleDeferred(() -> {
				((GmCondensationView) dialog.gmContentView).condense(Reason.reasons, CondensationMode.forced, Reason.T);
				new Timer() {
					@Override
					public void run() {
						dialog.gmContentView.select(0, false);
					}
				}.schedule(500);
			});
		}
		
		if (dialog.showingAsText)
			dialog.handleShowAsText();
		
		dialog.show();
	}
	
	private String prepareTextFromError() {
		ReasonException reasonException = (ReasonException) error;
		Reason reason = reasonException.getReason();
		
		StringBuilder builder = new StringBuilder();
		prepareForReason(reason, builder, 0);
		
		return builder.toString();
	}
	
	private void prepareForReason(Reason reason, StringBuilder builder, int ammountOfTabs) {
		for (int i = 0; i < ammountOfTabs; i++)
			builder.append("\t");
		builder.append(reason.entityType().getTypeSignature());
		builder.append(": ").append(reason.getText());
		List<Reason> reasons = reason.getReasons();
		int counter = 0;
		for (Reason childReason : reasons) {
			if (counter > 0)
				builder.append("\n");
			prepareForReason(childReason, builder, ammountOfTabs + 1);
			counter++;
		}
	}

}
