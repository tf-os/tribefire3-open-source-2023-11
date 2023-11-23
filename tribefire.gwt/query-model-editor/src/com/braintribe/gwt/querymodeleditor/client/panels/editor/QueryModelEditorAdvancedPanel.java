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
package com.braintribe.gwt.querymodeleditor.client.panels.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.Storage;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gm.storage.api.StorageHandle;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageExpert;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageInput;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanelTextFieldListener;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.qc.api.client.QueryProviderActions;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderStorageHandle;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.gwt.querymodeleditor.client.panels.autocompletion.AutoCompletionPanel;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.controls.PaginationControl;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.controls.SaveActionControl;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormDialog;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormTemplate;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorResources;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorTemplates;
import com.braintribe.gwt.querymodeleditor.client.resources.TemplateConfigurationBean;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.query.api.shortening.QueryShorteningRuntimeException;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.autocompletion.QueryAutoCompletion;
import com.braintribe.model.processing.query.autocompletion.api.QueryAutoCompletionResult;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.QueryLexer;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;
import com.braintribe.model.processing.query.expander.QueryTypeSignatureExpanderBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.shortening.SmartShortening;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.template.Template;
import com.braintribe.utils.lcd.StringTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class QueryModelEditorAdvancedPanel extends ContentPanel implements InitializableBean, QueryProviderView<GenericEntity>,
		QueryProviderStorageHandle, QueryProviderActions, ClickHandler, ResizeHandler, HasText, InputFocusHandler, DisposableBean {

	/********************************** Constants **********************************/

	private static final Logger logger = new Logger(QueryModelEditorAdvancedPanel.class);

	private final String queryInputPlaceholderId = this.hashCode() + "_queryInputPlaceholder";
	private final String queryInputHolderId = this.hashCode() + "_queryInputHolder";
	private final String queryInputTextboxId = this.hashCode() + "_queryInputTextbox";
	private final String measurementDivId = this.hashCode() + "_measurementDiv";
	private final String layoutDivId = this.hashCode() + "_layoutDiv";
	private final String openFormButtonCellId = this.hashCode() + "_openFormButtonCell";
	private final String openFormButtonId = this.hashCode() + "_openFormButton";
	private final String queryFeedbackIconId = this.hashCode() + "_queryFeedbackIcon";
	private final String queryContextCellId = this.hashCode() + "_queryContextCell";
	private final String querySearchButtonId = this.hashCode() + "_querySearchButton";
	private final String querySearchButtonImageId = this.hashCode() + "_querySearchButtonImage";
	private final String pagenationControlCellId = this.hashCode() + "_pagenationControlCell";
	private final String saveActionDropDownCellId = this.hashCode() + "_saveActionDropDownCell";
	private final String switchModeAnchorId = this.hashCode() + "_switchModeAnchor";
	private final String switchPagingModeAnchorId = this.hashCode() + "_switchPagingModeAnchor";

	private final String selectQueryInputPlaceholder = "div[@id='" + this.queryInputPlaceholderId + "']";
	private final String selectQueryInputHolder = "div[@id='" + this.queryInputHolderId + "']";
	private final String selectQueryInputTextbox = "textarea[@id='" + this.queryInputTextboxId + "']";
	private final String selectMeasurementDiv = "div[@id='" + this.measurementDivId + "']";
	private final String selectLayoutDiv = "div[@id='" + this.layoutDivId + "']";
	private final String selectOpenFormButtonCell = "div[@id='" + this.openFormButtonCellId + "']";
	private final String selectOpenFormButton = "img[@id='" + this.openFormButtonId + "']";
	private final String selectQueryFeedbackIcon = "img[@id='" + this.queryFeedbackIconId + "']";
	private final String selectQueryContextCell = "div[@id='" + this.queryContextCellId + "']";
	private final String selectQuerySearchButton = "div[@id='" + this.querySearchButtonId + "']";
	private final String selectQuerySearchButtonImage = "img[@id='" + this.querySearchButtonImageId + "']";
	private final String selectPagenationControlCell = "div[@id='" + this.pagenationControlCellId + "']";
	private final String selectSaveActionDropDownCell = "div[@id='" + this.saveActionDropDownCellId + "']";
	private final String selectSwitchModeAnchor = "a[@id='" + this.switchModeAnchorId + "']";
	private final String selectSwitchPagingModeAnchor = "a[@id='" + this.switchPagingModeAnchorId + "']";
	
	private final String selectCountElement = "div[@class='queryModelEditorCountElement']";

	private static final String queryModelEditorClickedSearchElement = "queryModelEditorClickedSearchElement";
	private static final String queryModelEditorExpandedTextBoxClass = "queryModelEditorExpandedTextBox";
	private static final String queryModelEditorScrollTextBoxClass = "queryModelEditorScrollTextBox";

	private static int autoCompletionDialogMaxWidth = 300;
	private static int autoCompletionDialogHeight = 300;
	private static int queryFormDialogHeight = 150;

	/********************************** Variables **********************************/

	private boolean usePaging = true;
	private boolean inputHolderInitialVisible = false;
	private int currentPageSize = PaginationControl.pageSizeValues.get(4);

	private DivElement inputPlaceholder = null;
	private DivElement inputHolder = null;

	private TextAreaElement inputTextArea = null;
	private DivElement measurementDiv = null;
	private DivElement layoutDiv = null;

	private DivElement openFormButtonCell = null;
	private ImageElement openFormButton = null;

	private ImageElement validationImage = null;
	private DivElement queryContextCell;
	private DivElement inputSearchButton = null;
	private ImageElement inputSearchButtonImage = null;

	private DivElement pagenationControlCell = null;
	private PaginationControl paginationControl = null;
	private AnchorElement inputSwitchPagingModeAnchor = null;

	private DivElement saveActionControlCell = null;
	private SaveActionControl saveActionControl = null;
	private AnchorElement inputSwitchAnchor = null;

	private SignatureExpert shorteningMode = null;
	private ModelEnvironmentDrivenGmSession gmSession = null;

	private Storage storage = null;
	private Dialog autoCompletionDialog = null;
	private QueryFormDialog queryFormDialog = null;

	private AutoCompletionPanel autoCompletionPanel = null;
	private Supplier<AutoCompletionPanel> autoCompletionPanelProvider = null;
	private boolean showingAutoCompletionPanel = false;

	private int defaultTextBoxHeight = 0;
	private int defaultHolderHeight = 0;
	private int holderOffsetHeight = 0;

	private Query parsedQuery = null;
	private String originalQueryString = null;

	private StorageHandle storageHandle = null;
	private QueryStorageExpert queryStorageExpert = null;

	private boolean isQueryValid = false;
	private boolean getOriginalQuery = false;
	private boolean textAreaCollapsed = true;
	private boolean queryContainsPaging = false;

	private boolean isSearchButtonEnabled = false;
	private boolean isOpenFormButtonVisible = false;
	private boolean isPagenationControlEnabled = false;

	private QueryProviderView<GenericEntity> otherQueryProviderView = null;
	private final List<QueryProviderViewListener> queryProviderViewListeners = new ArrayList<>();
	
	private boolean disposed;
	
	private QuickAccessDialog quickAccessDialog;
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private SpotlightPanelTextFieldListener spotlightPanelTextFieldListener;
	private String previousText;
	private String currentQuickAccessTypeSignature;
	private QueryAutoCompletionResult autoCompletionResult;
	private IconProvider iconProvider;
	private final List<Pair<String, String>> entityRepresentationList = new ArrayList<>();
	private final List<Pair<String, SpanElement>> entityElementsList = new ArrayList<>();
	private final List<Integer> entityElementsPositionList = new ArrayList<>();
	private boolean hideDelete;
	private Timer keyUpTimer;
	private final List<Pair<String, String>> enumRepresentationList = new ArrayList<>();
	private final List<Pair<String, SpanElement>> enumElementsList = new ArrayList<>();
	private final List<Integer> enumElementsPositionList = new ArrayList<>();
	private String lastPreparedText;
	
	private Template template;
	
	private boolean displayNode;
	private Integer nodeWidth;
	private List<StorageColumnInfo> displayPaths;
	private Menu queryContextMenu;
	private CheckMenuItem globalItem = new CheckMenuItem(LocalizedText.INSTANCE.global());
	private CheckMenuItem contextMenuItem = new CheckMenuItem();
	
	private DivElement countElement;

	/************************ QueryModelEditorAdvancedPanel ************************/

	public QueryModelEditorAdvancedPanel() {
		// Define style of Query-Editor
		setId("AdvancedQueryModelEditor");
		setHeaderVisible(false);
		setDeferHeight(false);
		setBodyBorder(false);
		setBorders(false);

		// Needed to enable events - but only when using it stand-alone
		//RootPanel.get().add(this);
	}

	/**
	 * Configures the required dialog to be shown when saving queries.
	 */
	@Required
	public void setStorage(final Storage storage) {
		this.storage = storage;
	}

	/**
	 * Configures the required dialog to be shown for auto completion.
	 */
	@Required
	public void setAutoCompletionPanelProvider(final Supplier<AutoCompletionPanel> autoCompletionPanelProvider) {
		this.autoCompletionPanelProvider = autoCompletionPanelProvider;
	}
	
	/**
	 * Configures the required provider for the {@link SpotlightPanel} used for the auto completion dialog.
	 */
	@Required
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}

	@Configurable
	public void setQueryFormDialog(final QueryFormDialog queryFormDialog) {
		this.queryFormDialog = queryFormDialog;

		queryFormDialog.setQueryModelEditorActions(this);
		queryFormDialog.configureFocusObject(inputTextArea);
	}

	/**
	 * Configures the initial page size. Defaults to 25.
	 *
	 * @see QueryModelEditorAdvancedPanel#setUsePaging(boolean)
	 */
	@Configurable
	public void setPageSize(final int pageSize) {
		currentPageSize = pageSize;

		if (paginationControl != null)
			paginationControl.setPageSize(currentPageSize);
	}

	/**
	 * Configures whether to use paging buttons. Defaults to true.
	 */
	@Configurable
	public void setUsePaging(final boolean usePaging) {
		this.usePaging = usePaging;
	}

	@Configurable
	public void setInputHolderInitialVisible(final boolean inputHolderInitialVisible) {
		this.inputHolderInitialVisible = inputHolderInitialVisible;
	}
	
	/**
	 * Configures the provider which will provide icons.
	 */
	@Configurable
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	/**
	 * @return input placeholder
	 */
	public DivElement getInputPlaceholder() {
		return inputPlaceholder;
	}

	/**
	 * @return input holder
	 */
	public DivElement getInputHolder() {
		return inputHolder;
	}

	/**
	 * @return input text area
	 */
	public TextAreaElement getInputTextArea() {
		return inputTextArea;
	}

	/**
	 * @return measurement div
	 */
	public DivElement getMeasurementDiv() {
		return measurementDiv;
	}

	/**
	 * @return open form table cell element
	 */
	public DivElement getOpenFormButtonCell() {
		return openFormButtonCell;
	}

	/**
	 * @return open form image element
	 */
	public ImageElement getOpenFormButton() {
		return openFormButton;
	}

	/**
	 * @return validation image element
	 */
	public ImageElement getValidationImage() {
		return validationImage;
	}

	/**
	 * @return search image element
	 */
	public DivElement getInputSearchButton() {
		return inputSearchButton;
	}

	/**
	 * @return search image element
	 */
	public ImageElement getInputSearchButtonImage() {
		return inputSearchButtonImage;
	}

	/**
	 * @return pagenation control cell
	 */
	public DivElement getPagenationControlCell() {
		return pagenationControlCell;
	}

	/**
	 * @return save query anchor
	 */
	public DivElement getSaveActionDropDownCell() {
		return saveActionControlCell;
	}
	
	/**
	 * @return input switch paging mode anchor
	 */
	public AnchorElement getInputSwitchPagingModeAnchor() {
		return inputSwitchPagingModeAnchor;
	}

	/**
	 * @return input switch anchor
	 */
	public AnchorElement getInputSwitchAnchor() {
		return inputSwitchAnchor;
	}

	/**
	 * Return this widget instead of null!
	 */
	@Override
	public Widget getWidget() {
		return this;
	}

	public boolean isUsePaging() {
		return usePaging;
	}

	/******************************* InitializableBean *****************************/

	@Override
	public void intializeBean() throws Exception {
		// Get and initialize template Elements
		TemplateConfigurationBean bean = new TemplateConfigurationBean();
		bean.setQueryInputPlaceholderId(queryInputPlaceholderId);
		bean.setQueryInputHolderId(queryInputHolderId);
		bean.setQueryInputTextboxId(queryInputTextboxId);
		bean.setMeasurementDivId(measurementDivId);
		bean.setLayoutDivId(layoutDivId);
		bean.setOpenFormButtonCellId(openFormButtonCellId);
		bean.setOpenFormButtonId(openFormButtonId);
		bean.setQueryFeedbackIconId(queryFeedbackIconId);
		bean.setQueryContextCellId(queryContextCellId);
		bean.setQuerySearchButtonId(querySearchButtonId);
		bean.setQuerySearchButtonImageId(querySearchButtonImageId);
		bean.setPaginationControlCellId(pagenationControlCellId);
		bean.setSwitchPagingModeAnchorId(switchPagingModeAnchorId);
		bean.setSaveActionDropDownCellId(saveActionDropDownCellId);
		bean.setSwitchModeAnchorId(switchModeAnchorId);
		add(new HTML(QueryModelEditorTemplates.INSTANCE.qmeAdvancedPanel(bean)));

		XElement element = getElement();
		inputPlaceholder = DomQuery.selectNode(selectQueryInputPlaceholder, element).cast();
		inputHolder = DomQuery.selectNode(selectQueryInputHolder, element).cast();

		// Hide absolute positioned input holder
		final XElement xInputHolder = XElement.as(inputHolder);
		xInputHolder.setVisible(inputHolderInitialVisible);

		inputTextArea = DomQuery.selectNode(selectQueryInputTextbox, element).cast();
		measurementDiv = DomQuery.selectNode(selectMeasurementDiv, element).cast();
		layoutDiv = DomQuery.selectNode(selectLayoutDiv, element).cast();
		
		if (GXT.isGecko()) {
			layoutDiv.getStyle().setLeft(6, Unit.PX);
			layoutDiv.getStyle().setTop(9, Unit.PX);
		} else {
			layoutDiv.getStyle().setLeft(5, Unit.PX);
			layoutDiv.getStyle().setTop(8, Unit.PX);
		}

		openFormButtonCell = DomQuery.selectNode(selectOpenFormButtonCell, element).cast();
		setOpenFormButtonCellVisible(false);

		openFormButton = DomQuery.selectNode(selectOpenFormButton, element).cast();
		openFormButton.setSrc(QueryModelEditorResources.INSTANCE.dropDown().getSafeUri().asString());

		validationImage = DomQuery.selectNode(selectQueryFeedbackIcon, element).cast();
		validationImage.setSrc(QueryModelEditorResources.INSTANCE.invalid().getSafeUri().asString());
		
		queryContextCell = DomQuery.selectNode(selectQueryContextCell, element).cast();
		globalItem.setGroup("queryContextGroup");
		globalItem.setChecked(true, true);
		contextMenuItem.setGroup("queryContextGroup");
		contextMenuItem.setVisible(false);

		inputSearchButton = DomQuery.selectNode(selectQuerySearchButton, element).cast();
		inputSearchButtonImage = DomQuery.selectNode(selectQuerySearchButtonImage, element).cast();
		inputSearchButtonImage.setSrc(QueryModelEditorResources.INSTANCE.query().getSafeUri().asString());
		
		inputSwitchPagingModeAnchor = DomQuery.selectNode(selectSwitchPagingModeAnchor, element).cast();

		saveActionControlCell = DomQuery.selectNode(selectSaveActionDropDownCell, element).cast();
		saveActionControlCell.appendChild(getSaveActionControl().getElement());

		inputSwitchAnchor = DomQuery.selectNode(selectSwitchModeAnchor, element).cast();
		inputSwitchAnchor.setName(LocalizedText.INSTANCE.switchBasic());
		inputSwitchAnchor.setInnerText(LocalizedText.INSTANCE.switchBasic());
		
		countElement = DomQuery.selectNode(selectCountElement, element).cast();
		
		preparePagingElements();

		// Draw layout
		forceLayout();

		// Add Events to InputTextArea
		DOM.sinkEvents(inputTextArea, Event.ONFOCUS | Event.ONBLUR | Event.ONKEYDOWN | Event.ONKEYUP | Event.ONSCROLL | Event.ONPASTE);
		DOM.setEventListener(inputTextArea, this);
		
		DOM.sinkEvents(layoutDiv, Event.ONMOUSEDOWN);
		DOM.setEventListener(layoutDiv, this);

		// Add Events to InputSearchButton
		DOM.sinkEvents(inputSearchButton, Event.ONMOUSEDOWN | Event.ONMOUSEOUT | Event.ONMOUSEUP);
		DOM.setEventListener(inputSearchButton, this);

		// Add Events to Element
		addDomHandler(this, ClickEvent.getType());
		addHandler(this, ResizeEvent.getType());

		// Disable search & save button
		validateCondition(getCurrentText());

		// Positioning element
		positioningTextarea(false);
		expandTextarea(true);
	}
	
	private void preparePagingElements() {
		if (queryFormDialog != null)
			queryFormDialog.setUsePaging(usePaging);
		pagenationControlCell = DomQuery.selectNode(selectPagenationControlCell, getElement()).cast();
		pagenationControlCell.appendChild(getPaginationControl().getElement());
		setPagenationControlCellVisible(usePaging);
		inputSwitchPagingModeAnchor.setName(usePaging ? LocalizedText.INSTANCE.autoPaging() : LocalizedText.INSTANCE.paging());
		inputSwitchPagingModeAnchor.setInnerText(usePaging ? LocalizedText.INSTANCE.autoPaging() : LocalizedText.INSTANCE.paging());
	}

	public PaginationControl getPaginationControl() {
		if (paginationControl != null)
			return paginationControl;
		
		// Create pagination control
		paginationControl = new PaginationControl();
		paginationControl.setPageSize(currentPageSize);
		paginationControl.setEnabled(isPagenationControlEnabled);

		// Define pagination control events
		SelectionHandler<Item> pageSizeSelectedHandler = event -> {
			paginationControl.enableIncreasePageButton(false);
			paginationControl.enableDecreasePageButton(false);
			currentPageSize = getPaginationControl().getPageSize();

			positioningTextarea(false);
			fireQueryPerform(false);
		};
		
		final ClickHandler paginationClickHandler = event -> {
			paginationControl.enableIncreasePageButton(false);
			paginationControl.enableDecreasePageButton(false);

			positioningTextarea(false);
			fireQueryPerform(false);
		};

		// Set event handler of pagination control
		paginationControl.setPageSizeSelected(pageSizeSelectedHandler);
		paginationControl.setIncreasePageClicked(paginationClickHandler);
		paginationControl.setDecreasePageClicked(paginationClickHandler);

		return paginationControl;
	}

	public SaveActionControl getSaveActionControl() {
		if (saveActionControl != null)
			return saveActionControl;
		
		// Create save action control
		saveActionControl = new SaveActionControl();

		// Define save action control events
		ClickHandler saveActionClickedHandler = event -> {
			try {
				// Set getQuery to NoPaging-Mode
				getOriginalQuery = true;

				// Always get query from query provider to ensure you get always the same query!
				QueryStorageInput input = queryStorageExpert.prepareStorageInput(getQueryProviderContext().getQuery(true),
						resolveEntitiesAndEnums(getCurrentText()), prepareColumnData());

				// Check for a StorageHandle
				if (storageHandle != null) {
					// StorageHandle found, so default is save -> so save it
					storage.save(input, storageHandle).andThen(v -> {
						// Save current text as original query string & disable save button
						originalQueryString = getCurrentText();
						enableSaveButton(false);

						// Show Success
						GlobalState.showSuccess(LocalizedText.INSTANCE.successSave());
					}).onError(e -> {
						// User abort?
						if (e != null)
							GlobalState.showError(e.getMessage(), e);
					});
				} else {
					// StorageHandle not found, so default is saveAs -> so create new
					storage.saveAs(input).andThen(result -> {
						// Save new StorageHandle
						storageHandle = result;
						saveActionControl.setDefaultSaveAs(false);

						// Save current text as original query string & disable save button
						originalQueryString = getCurrentText();
						enableSaveButton(false);

						// Show Success
						GlobalState.showSuccess(LocalizedText.INSTANCE.successSave());
					}).onError(e -> {
						// User abort?
						if (e != null)
							GlobalState.showError(e.getMessage(), e);
					});
				}
			} finally {
				// Set getQuery to normal Mode
				getOriginalQuery = false;
				if (displayPaths != null) {
					displayPaths.clear();
					displayPaths = null;
				}
			}
		};
		
		SelectionHandler<Item> saveActionSelectedHandler = event -> {
			// Check for a StorageHandle
			if (storageHandle == null)
				return;
			
			try {
				// Set getQuery to NoPaging-Mode
				getOriginalQuery = true;

				// Always get query from query provider to ensure you get always the same query!
				QueryStorageInput input = queryStorageExpert.prepareStorageInput(
						getQueryProviderContext().getQuery(true), resolveEntitiesAndEnums(getCurrentText()), prepareColumnData());

				// SaveAs clicked, so create new
				storage.saveAs(input).andThen(result -> {
					// Save new StorageHandle
					storageHandle = result;
					saveActionControl.setDefaultSaveAs(false);

					// Save current text as original query string & disable save button
					originalQueryString = getCurrentText();
					enableSaveButton(false);

					// Show Success
					GlobalState.showSuccess(LocalizedText.INSTANCE.successSave());
				}).onError(e -> {
					// User abort?
					if (e != null)
						GlobalState.showError(e.getMessage(), e);
				});
			} finally {
				// Set getQuery to normal Mode
				getOriginalQuery = false;
				if (displayPaths != null) {
					displayPaths.clear();
					displayPaths = null;
				}
			}
		};
		
		SelectionHandler<Item> settingsActionSelectedHandler = event -> {
			try {
				// Set getQuery to NoPaging-Mode
				getOriginalQuery = true;

				// Check for a StorageHandle
				if (storageHandle == null)
					return;
				
				// Settings clicked -> so call settings
				storage.settings(QueryModelEditorAdvancedPanel.this.storageHandle)
						.andThen(v -> GlobalState.showSuccess(LocalizedText.INSTANCE.successSave())).onError(e -> {
							// User abort?
							if (e != null)
								GlobalState.showError(e.getMessage(), e);
						});
			} finally {
				// Set getQuery to normal Mode
				QueryModelEditorAdvancedPanel.this.getOriginalQuery = false;
			}
		};

		// Set event handler of save action control
		saveActionControl.setSaveActionClicked(saveActionClickedHandler);
		saveActionControl.setSaveActionSelected(saveActionSelectedHandler);
		saveActionControl.setSettingsActionSelected(settingsActionSelectedHandler);

		// Enable/Disable edit mode
		saveActionControl.setDefaultSaveAs(storageHandle == null);

		return saveActionControl;
	}

	private QueryFormDialog getQueryFormDialog() {
		if (queryFormDialog == null)
			setQueryFormDialog(new QueryFormDialog());

		return this.queryFormDialog;
	}
	
	@Override
	public String getText() {
		return getCurrentText();
	}
	
	@Override
	public void setText(String text) {
		setCurrentText(text);
	}
	
	@Override
	public void focusInput() {
		inputTextArea.focus();
	}
	
	@Override
	public Element getInputField() {
		return inputTextArea;
	}
	
	@Override
	public Element getInputTrigger() {
		return inputSearchButton;
	}

	/******************************* QueryProviderView *****************************/

	@Override
	public QueryProviderContext getQueryProviderContext() {
		QueryProviderContext context = new QueryProviderContext() {
			@Override
			public boolean isAutoPageEnabled() {
				return usePaging ? false : !queryContainsPaging;
			}

			@Override
			public int getAutoPageSize() {
				return paginationControl != null ? paginationControl.getPageSize() : currentPageSize;
			}
			
			@Override
			public Template getTemplate() {
				return template;
			}

			@Override
			public Query getQuery(final boolean initialQuery) {
				Query query = null;

				if (initialQuery) // Parse query && expand type signatures if not parsed so far
					QueryModelEditorAdvancedPanel.this.parsedQuery = getTypeSignatureExpandedParsedQuery();

				query = parsedQuery;
				try {
					// Set parsed query as result query
					if (!getOriginalQuery && template != null) {
						// Set parsed query to dialog
						setQueryToQueryFormDialog(query);

						// Evaluate parsed query with defined variable values
						QueryFormTemplate queryTemplate = getQueryFormDialog().getQueryTemplate();
						if (queryTemplate.getTemplatehasValueDescriptors())
							query = queryTemplate.evaluateQuery(query, template);
					}
				} catch (final TemplateEvaluationException e) {
					// Set parsed query as result query
					query = QueryModelEditorAdvancedPanel.this.parsedQuery;
				}
				
				if (query == null || getOriginalQuery)
					return query;

				// Determine paging
				if (!isAutoPageEnabled() && isPagenationControlEnabled && paginationControl != null) {
					// Create restriction of query is missing
					Restriction restriction = query.getRestriction();
					if (restriction == null) {
						restriction = Restriction.T.create();
						query.setRestriction(restriction);
					}

					// Create paging of query is missing
					Paging paging = restriction.getPaging();
					if (paging == null) {
						paging = Paging.T.create();
						restriction.setPaging(paging);
					}

					// Set defined page size and index
					paging.setPageSize(QueryModelEditorAdvancedPanel.this.paginationControl.getPageSize());
					paging.setStartIndex(QueryModelEditorAdvancedPanel.this.paginationControl.getPageIndex());
				}

				return query;
			}
		};

		return context;
	}

	@Override
	public void notifyQueryPerformed(final QueryResult queryResult, final QueryProviderContext queryProviderContext) {
		// Enable/disable pagination buttons
		if (paginationControl != null) {
			paginationControl.enableIncreasePageButton(queryResult.getHasMore());
			paginationControl.enableDecreasePageButton(paginationControl.getPageIndex() > 0);
		}
	}

	@Override
	public void setEntityContent(final GenericEntity entityContent) {
		Query externalQuery = null;

		// Setting current entity type
		if (entityContent instanceof Template) {
			template = (Template) entityContent;
			externalQuery = (Query) template.getPrototype();
		} else {
			template = null;
			externalQuery = (Query) entityContent;
		}
		
		originalQueryString = getStringfiedQuery(externalQuery);

		// Set and expand TextArea
		setCurrentText(originalQueryString);
		expandTextarea(true);

		// Validate query string
		validateCondition(this.originalQueryString);
	}

	private String getStringfiedQuery(Query externalQuery) {
		if (externalQuery == null)
			return "";
		
		String stringfiedQuery = "";
		try {
			// Stringify received query with defined shortening mode and set query string.
			stringfiedQuery = BasicQueryStringifier.create().shorteningMode().custom(this.shorteningMode).stringify(externalQuery);
		} catch (final Exception e) {
			if (e instanceof QueryShorteningRuntimeException) {
				try {
					// Stringify received query with no shortening mode and set query string.
					stringfiedQuery = BasicQueryStringifier.create().stringify(externalQuery);
					this.shorteningMode = null; // Disable shortening

					// Warn: Disabled shortening Mode
					GlobalState.showWarning("Disabled shortening of Query because:\r\n" + e.getMessage());
				} catch (final Exception ex) {
					// Show other Exception
					GlobalState.showError(ex.getMessage(), ex);
					stringfiedQuery = "";
				}
			} else {
				// Show other Exception
				GlobalState.showError(e.getMessage(), e);
				stringfiedQuery = "";
			}
		}
		
		return stringfiedQuery;
	}

	@Override
	public void addQueryProviderViewListener(final QueryProviderViewListener listener) {
		if (!queryProviderViewListeners.contains(listener))
			queryProviderViewListeners.add(listener);
	}

	@Override
	public void removeQueryProviderViewListener(final QueryProviderViewListener listener) {
		queryProviderViewListeners.remove(listener);
	}

	@Override
	public void configureGmSession(final PersistenceGmSession persistenceGmSession) {
		gmSession = (ModelEnvironmentDrivenGmSession) persistenceGmSession;
		shorteningMode = new SmartShortening(gmSession.getModelAccessory().getOracle());
	}

	@Override
	public void focusEditor() {
		inputTextArea.focus();
	}

	/**
	 * Configures the QueryProviderView used when in a different mode (simple or advanced).
	 */
	@Override
	@Configurable
	public void setOtherModeQueryProviderView(Supplier<? extends QueryProviderView<GenericEntity>> otherModelQueryProviderView) {
		if (otherQueryProviderView == null) {
			otherQueryProviderView = otherModelQueryProviderView.get();
			otherQueryProviderView.setOtherModeQueryProviderView(() -> QueryModelEditorAdvancedPanel.this);
		}
	}

	@Override
	public void modeQueryProviderViewChanged() {
		// Show absolute positioned input holder
		XElement xInputHolder = XElement.as(inputHolder);
		xInputHolder.setVisible(true);

		Scheduler.get().scheduleDeferred(() -> onResize(null));
	}
	
	@Override
	public boolean isFormAvailable() {
		return isOpenFormButtonVisible;
	}
	
	@Override
	public void showForm() {
		handleShowForm();
	}
	
	@Override
	public void hideForm() {
		if (queryFormDialog != null)
			queryFormDialog.hide();
	}
	
	@Override
	public void onViewChange(boolean displayNode, Integer nodeWidth, List<StorageColumnInfo> columnsVisible) {
		enableSaveButton(true);
		this.displayNode = displayNode;
		if (nodeWidth != null)
			this.nodeWidth = nodeWidth;
		this.displayPaths = columnsVisible;
	}
	
	@Override
	public void setDisplayCountText(String countText) {
		if (countText != null)
			countElement.setInnerSafeHtml(SafeHtmlUtils.fromSafeConstant(countText));
		else
			countElement.setInnerHTML("");
	}
	
	private void switchPagingMode() {
		usePaging = !usePaging;
		preparePagingElements();
	}

	/************************** QueryProviderStorageHandle *************************/

	@Override
	public void setQueryStorageExpert(QueryStorageExpert queryStorageExpert) {
		this.queryStorageExpert = queryStorageExpert;
	}

	@Override
	public QueryStorageExpert getQueryStorageExpert() {
		return this.queryStorageExpert;
	}

	@Override
	public void setStorageHandle(final StorageHandle handle) {
		storageHandle = handle;
		if (storageHandle != null && queryStorageExpert != null) {
			// Get query string from storage handle using the received expert
			String queryString = queryStorageExpert.getQueryString(storageHandle);
			if (queryString != null) {
				// Set query string and expand TextArea
				originalQueryString = queryString;
				setCurrentText(queryString);
				expandTextarea(true);

				// Validate query string
				validateCondition(queryString);
			}
		}

		// Enable/Disable default is saveAs
		if (saveActionControl != null) {
			saveActionControl.setDefaultSaveAs(storageHandle == null);
			handleSaveButtonVisibility();
		}
	}

	@Override
	public StorageHandle getStorageHandle() {
		return storageHandle;
	}

	/***************************** QueryProviderActions ****************************/

	@Override
	public void fireModeChange() {
		if (queryProviderViewListeners == null || otherQueryProviderView == null)
			return;
		
		try {
			// Set getQuery to NoPaging-Mode
			getOriginalQuery = true;

			// Hide QueryFormDialog
			getQueryFormDialog().hide();

			// Hide absolute positioned input holder
			XElement xInputHolder = XElement.as(inputHolder);
			xInputHolder.setVisible(false);

			// Copy the list because the listener list will get changed internally
			for (final QueryProviderViewListener listener : new ArrayList<>(queryProviderViewListeners))
				listener.onModeChanged(otherQueryProviderView, false);
		} finally {
			// Set getQuery to normal Mode
			getOriginalQuery = false;
		}
	}

	@Override
	public void fireQueryPerform(final boolean resetCurrentStartIndex) {
		if (resetCurrentStartIndex) {
			// Reset page index
			if (paginationControl != null)
				paginationControl.setPageIndex(0);
		}

		// Notify listeners
		if (queryProviderViewListeners != null) {
			// Always get query from query provider to ensure you get always the same query!
			for (final QueryProviderViewListener listener : queryProviderViewListeners)
				listener.onQueryPerform(getQueryProviderContext());
		}

		getQueryFormDialog().hide();
		hideAutoCompletionDialog();
		focusEditor();
	}

	/************************** Listener & Event Methods ***************************/

	public void setAutoCompletionValue(String possibleHint) {
		String filterString = autoCompletionResult.getFilterString();

		// Check the received possible hint
		if (!StringTools.isEmpty(possibleHint)) {
			int cursorPosition = getCursorPosition();

			// Split current element value
			String inputAfter = getCurrentText().substring(cursorPosition);
			String inputBefore = getCurrentText().substring(0, cursorPosition - filterString.length());

			// Paste pasteData in middle of the input text
			setCurrentText(inputBefore + possibleHint + inputAfter);
			setCursorPosition(cursorPosition - filterString.length() + possibleHint.length());

			// Validate & run AutoResize-Script
			validateCondition(getCurrentText()); //TODO: this stuff should be called also from QA results.
			expandTextarea(false);
		}

		hideAutoCompletionDialog();
	}

	@Override
	public void onBrowserEvent(final Event event) {
		switch (event.getTypeInt()) {
			case Event.ONFOCUS:
				onFocusEvent(event);
				break;
			case Event.ONBLUR:
				onBlurEvent(event);
				break;
			case Event.ONKEYDOWN:
				onKeyDownEvent(event);
				break;
			case Event.ONKEYUP:
				onKeyUpEvent(event);
				break;
			case Event.ONSCROLL:
				onScrollEvent(event);
				break;
			case Event.ONPASTE:
				onPasteEvent(event);
				break;
			case Event.ONMOUSEDOWN:
				onMouseDown(event);
				break;
			case Event.ONMOUSEOUT:
				onMouseOut(event);
				break;
			case Event.ONMOUSEUP:
				onMouseUp(event);
				break;
			default:
				break;
		}

		super.onBrowserEvent(event);
	}

	public void onFocusEvent(final Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputTextArea.isOrHasChild(targetElement)) // Run AutoResize-Script
			expandTextarea(false);
	}

	public void onBlurEvent(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (!inputTextArea.isOrHasChild(targetElement))
			return;
		
		Node currentElement = QueryModelEditorScripts.getCurrentElement();
		if (currentElement != null && inputHolder.isOrHasChild(currentElement)) {
			// Focus again!
			event.preventDefault();
			focusEditor();
		} else // Run AutoResize-Script
			expandTextarea(true);
	}

	public void onKeyDownEvent(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (!inputTextArea.isOrHasChild(targetElement))
			return;
		
		layoutDiv.getStyle().setDisplay(Display.NONE);
		String currentText = getCurrentText();
		// Disable/Enable search & save button
		//validateCondition(currentText);

		switch (event.getKeyCode()) {
			case KeyCodes.KEY_ENTER:
				if (event.getShiftKey())
					break;
				
				event.preventDefault();
				event.stopPropagation();
				
				if (spotlightPanelTextFieldListener != null)
					break;

				if (isQueryValid && (autoCompletionPanel == null || !showingAutoCompletionPanel))
					fireQueryPerform(usePaging == false);
	
				break;
			case KeyCodes.KEY_SPACE:
				// Check auto completion dialog
				if (!event.getCtrlKey())
					break;
				
				event.preventDefault();
				event.stopPropagation();
	
				if (spotlightPanelTextFieldListener != null) //quickAccess already shown
					return;
				
				// Check if the cursor position is valid
				int cursorPosition = getFixedCursorPosition();
				// Prepare AutoCompletionDialog hints
				autoCompletionResult = null;
				try {
					// Load hints for the AutoCompletionDialog with defined shortening mode
					autoCompletionResult = new QueryAutoCompletion(gmSession).getPossibleHints(currentText, cursorPosition, shorteningMode);
				} catch (final Exception e) {
					// Show Exception as Notification
					GlobalState.showError(e.getMessage(), e);
					return;
				}
				
				String typeSignature = autoCompletionResult.getTypeSignature();
				if (typeSignature != null)
					displayQuickAccess(typeSignature);
				else
					displayAutoCompletion();
	
				break;
			case KeyCodes.KEY_ESCAPE:
				event.preventDefault();
				event.stopPropagation();
	
				getQueryFormDialog().hide();
				hideAutoCompletionDialog();
				
				if (spotlightPanelTextFieldListener != null)
					getQuickAccessDialog().hide();
	
				break;
			case KeyCodes.KEY_DELETE :
			case KeyCodes.KEY_BACKSPACE:
			case KeyCodes.KEY_LEFT:
			case KeyCodes.KEY_RIGHT:
				int keyCode = event.getKeyCode();
				boolean handlingEntity = true;
				int index = isCloseToEntityOrEnum(keyCode == KeyCodes.KEY_DELETE || keyCode == KeyCodes.KEY_RIGHT, true);
				if (index == - 1) {
					index = isCloseToEntityOrEnum(keyCode == KeyCodes.KEY_DELETE || keyCode == KeyCodes.KEY_RIGHT, false);
					handlingEntity = false;
				} else {
					List<Pair<String, SpanElement>> elementsList = handlingEntity ? entityElementsList : enumElementsList;
					if (keyCode == KeyCodes.KEY_DELETE || keyCode == KeyCodes.KEY_BACKSPACE)
						removeEntityOrEnumElement(elementsList.get(index).getSecond(), handlingEntity);
					else
						walkEntityOrEnumElement(index, elementsList.get(index).first(), keyCode == KeyCodes.KEY_RIGHT, handlingEntity);
					event.preventDefault();
					event.stopPropagation();
				}
				break;
			default:
				break;
		}
		
		if ((autoCompletionPanel != null && showingAutoCompletionPanel)
				|| spotlightPanelTextFieldListener != null && event.getKeyCode() == KeyCodes.KEY_UP || event.getKeyCode() == KeyCodes.KEY_DOWN) {
			event.preventDefault();
			event.stopPropagation();
		}
		
		if (autoCompletionPanel != null && showingAutoCompletionPanel) {
			if (event.getKeyCode() == KeyCodes.KEY_UP)
				autoCompletionPanel.selectPrevious();
			else if (event.getKeyCode() == KeyCodes.KEY_DOWN)
				autoCompletionPanel.selectNext();
			else if (event.getKeyCode() == KeyCodes.KEY_ENTER)
				autoCompletionPanel.chooseSelected();
			else {
				Scheduler.get().scheduleDeferred(() -> {
					autoCompletionResult = new QueryAutoCompletion(gmSession).getPossibleHints(getCurrentText(), getFixedCursorPosition(), shorteningMode);
					autoCompletionPanel.setPossibleHints(preparePossibleHints(autoCompletionResult));
				});
			}
		} else if (spotlightPanelTextFieldListener != null) {
			Scheduler.get().scheduleDeferred(() -> {
				if (spotlightPanelTextFieldListener == null)
					return;
				
				int cursorPosition = getFixedCursorPosition();
				String textAreaValue = getCurrentText();
				QueryAutoCompletionResult result = new QueryAutoCompletion(this.gmSession).getPossibleHints(textAreaValue, cursorPosition, this.shorteningMode);
				if (!signaturesMatch(currentQuickAccessTypeSignature, result.getTypeSignature()) && !isTypingBoolean(result)
						&& !isTypingEntity(result) && !isTypingDate(result)) {
					//TODO There are some exceptions, like typing dot when entering for a number
					//or in case you type a bad stuff in a number (like a string)... then I do not hide it, but simply filter the values to empty
					spotlightPanelTextFieldListener.onCancel();
					return;
				}
				
				String text = result.getFilterString();
				if (text == null) {
					if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE || event.getKeyCode() == KeyCodes.KEY_LEFT) {
						text = previousText.substring(0, previousText.length() - 1);
					//} else if (event.getKeyCode() == KeyCodes.KEY_DELETE) {
						
					} else
						text = previousText + textAreaValue.charAt(cursorPosition - 1);
				}
				previousText = text;
				spotlightPanelTextFieldListener.onKeyDown(event, text);
			});
		}
	}

	private void displayAutoCompletion() {
		XElement textArea = XElement.as(inputTextArea);

		// Calculate location and position and display auto completion dialog
		int dialogWidth = textArea.getOffsetWidth() > autoCompletionDialogMaxWidth ? autoCompletionDialogMaxWidth : textArea.getOffsetWidth();
		displayAutoCompletionDialog(preparePossibleHints(autoCompletionResult), textArea.getX(), textArea.getY() + textArea.getOffsetHeight(), dialogWidth,
				autoCompletionDialogHeight);
		showingAutoCompletionPanel = true;
		
		new Timer() {
			@Override
			public void run() {
				focusEditor();
			}
		}.schedule(200);
	}
	
	private Map<String, String> preparePossibleHints(QueryAutoCompletionResult autoCompletionResult) {
		Map<String, String> possibleHintsMap = new LinkedHashMap<>();
		boolean useDisplayInfo = autoCompletionResult.getAliasType() != null;
		List<String> displayNames = new ArrayList<>();
		Map<String, String> auxMap = new FastMap<>();
		EntityMdResolver entityMdResolver = !useDisplayInfo ? null
				: gmSession.getModelAccessory().getMetaData().entityTypeSignature(autoCompletionResult.getAliasType().getTypeSignature())
						.lenient(true).useCase(KnownUseCase.queryEditorUseCase.getDefaultValue());
		for (String possibleHint : autoCompletionResult.getPossibleHints()) {
			String value = possibleHint;
			if (useDisplayInfo)
				value = GMEMetadataUtil.getPropertyDisplay(possibleHint, entityMdResolver.property(possibleHint));
			displayNames.add(value);
			auxMap.put(value, possibleHint);
		}
		
		if (useDisplayInfo) {
			String filterString = autoCompletionResult.getFilterString();
			for (String possibleHintFilteredOut : autoCompletionResult.getPossibleHintsFilteredOut()) {
				String value = GMEMetadataUtil.getPropertyDisplay(possibleHintFilteredOut, entityMdResolver.property(possibleHintFilteredOut));
				
				if (value.startsWith(filterString)) {
					displayNames.add(value);
					auxMap.put(value, possibleHintFilteredOut);
				}
			}
		}
		
		Collections.sort(displayNames);
		for (String displayName : displayNames)
			possibleHintsMap.put(auxMap.get(displayName), displayName);
		
		return possibleHintsMap;
	}

	private void displayQuickAccess(String typeSignature) {
		getQuickAccessDialog().configureDefaultEntitiesFutureProvider();
		getQuickAccessDialog().configureEnumConstantRenderer(null);
		getQuickAccessDialog().configureDefaultEnumTypes();
		try {
			SpotlightPanel quickAccessPanel = getQuickAccessDialog().getQuickAccessPanel();
			spotlightPanelTextFieldListener = quickAccessPanel;
			previousText = autoCompletionResult.getFilterString();
			currentQuickAccessTypeSignature = typeSignature;
			getQuickAccessDialog().getQuickAccessResult( //
					quickAccessPanel.prepareTypeCondition(typeSignature), inputTextArea, autoCompletionResult.getFilterString()) //
					.andThen(result -> {
						spotlightPanelTextFieldListener = null;
						if (result == null)
							return;

						boolean resultHandled = handleMissingDelimiter(result.getObject());
						if (resultHandled)
							return;

						Object resultObject = result.getObject();
						if (resultObject instanceof GenericEntity)
							handleQuickAccessEntity((GenericEntity) resultObject, result.getFilterText());
						else if (resultObject instanceof Enum<?>)
							handleQuickAccessEnum(result.getObjectAndType(), result.getFilterText());
						else if (resultObject instanceof Date)
							handleQuickAccessDate(result);
						// }// else TODO
						// handleQuickAccessResult(getQuickAccessResultString(resultObject, propertyType));

						// Validate & run AutoResize-Script
						validateCondition(getCurrentText());
						expandTextarea(false);
					}).onError(e -> {
						GlobalState.showError(e.getMessage(), e);
						e.printStackTrace();
					});
			
			new Timer() {
				@Override
				public void run() {
					focusEditor();
				}
			}.schedule(200);
			
		} catch (RuntimeException e) {
			GlobalState.showError(e.getMessage(), e);
			e.printStackTrace();
		}
	}
		
	private boolean signaturesMatch(String expectedSignature, String currentSignature) {
		if (expectedSignature.equals(currentSignature))
			return true;
		
		if (SimpleTypes.TYPE_DOUBLE.getTypeSignature().equals(expectedSignature)
				|| SimpleTypes.TYPE_DECIMAL.getTypeSignature().equals(expectedSignature)
				|| SimpleTypes.TYPE_FLOAT.getTypeSignature().equals(expectedSignature)
				|| SimpleTypes.TYPE_LONG.getTypeSignature().equals(expectedSignature)) {
			return SimpleTypes.TYPE_INTEGER.getTypeSignature().equals(currentSignature);
		}
		
		return false;
	}
	
	private boolean isTypingBoolean(QueryAutoCompletionResult result) {
		if (SimpleTypes.TYPE_BOOLEAN.getTypeSignature().equals(currentQuickAccessTypeSignature) && result.getTypeSignature() == null
				&& result.getFilterString() != null) {
			String filterString = result.getFilterString().toUpperCase();
			return "TRUE".contains(filterString) || "FALSE".contains(filterString);
		}
		
		return false;
	}
	
	private boolean isTypingEntity(QueryAutoCompletionResult result) {
		if (currentQuickAccessTypeSignature == null)
			return false;
		
		GenericModelType type = GMF.getTypeReflection().getType(currentQuickAccessTypeSignature);
		if (!type.isEntity())
			return false;
		
		return result.getPossibleHints().isEmpty() && !result.getFilterString().isEmpty();
	}
	
	private boolean isTypingDate(QueryAutoCompletionResult result) {
		return SimpleTypes.TYPE_DATE.getTypeSignature().equals(currentQuickAccessTypeSignature) && result.getTypeSignature() == null
				&& result.getFilterString() != null;
	}
	
	private boolean handleMissingDelimiter(Object result) {
		if (!SimpleTypes.TYPE_DOUBLE.getTypeSignature().equals(currentQuickAccessTypeSignature)
				&& !SimpleTypes.TYPE_DECIMAL.getTypeSignature().equals(currentQuickAccessTypeSignature)
				&& !SimpleTypes.TYPE_FLOAT.getTypeSignature().equals(currentQuickAccessTypeSignature)
				&& !SimpleTypes.TYPE_LONG.getTypeSignature().equals(currentQuickAccessTypeSignature)
				&& !SimpleTypes.TYPE_STRING.getTypeSignature().equals(currentQuickAccessTypeSignature)) {
			return false;
		}
		
		int cursorPosition = getFixedCursorPosition();
		if (cursorPosition != 0)
			cursorPosition--;
		String value = getCurrentText();
		
		int lastPosition = QueryLexer.getLastValuePosition(cursorPosition, value);
		if (SimpleTypes.TYPE_STRING.getTypeSignature().equals(currentQuickAccessTypeSignature)) {
			String resultString = (String) result;
			boolean changed = false;
			if (value.charAt(lastPosition - resultString.length()) != '\'') {
				value = value.substring(0, lastPosition - resultString.length() + 1) + '\'' + value.substring(lastPosition - resultString.length() + 1);
				lastPosition++;
				changed = true;
			}
			
			if (value.charAt(lastPosition) != '\'') {
				value = value.substring(0, lastPosition + 1) + '\'' + value.substring(lastPosition + 1);
				changed = true;
			}
			
			if (changed) {
				setCurrentText(value);
				validateCondition(getCurrentText());
				expandTextarea(false);
			}
			
			return true;
		}
		
		if (Character.isDigit(value.charAt(lastPosition))) {
			String characterToAdd;
			if (SimpleTypes.TYPE_DOUBLE.getTypeSignature().equals(currentQuickAccessTypeSignature))
				characterToAdd = QueryLexerToken.DoubleDelimiter.getKeywords()[0];
			else if (SimpleTypes.TYPE_DECIMAL.getTypeSignature().equals(currentQuickAccessTypeSignature))
				characterToAdd = QueryLexerToken.BigDecimalDelimiter.getKeywords()[0];
			else if (SimpleTypes.TYPE_FLOAT.getTypeSignature().equals(currentQuickAccessTypeSignature))
				characterToAdd = QueryLexerToken.FloatDelimiter.getKeywords()[0];
			else
				characterToAdd = QueryLexerToken.LongDelimiter.getKeywords()[0];
			
			value = value.substring(0, lastPosition + 1) + characterToAdd + value.substring(lastPosition + 1);
			
			setCurrentText(value);
			
			validateCondition(getCurrentText());
			expandTextarea(false);
		}
		
		return true;
	}
	
	public void onKeyUpEvent(Event event) {
		int key = event.getKeyCode();
		Element targetElement = event.getEventTarget().cast();
		if (inputTextArea.isOrHasChild(targetElement) && key != KeyCodes.KEY_LEFT && key != KeyCodes.KEY_RIGHT && key != KeyCodes.KEY_UP
				&& key != KeyCodes.KEY_DOWN && key != KeyCodes.KEY_ENTER) {
			String text = getCurrentText();
			//prepareLayoutDivText(text);
			// Disable/Enable search & save button
			validateCondition(text);
			expandTextarea(false);
			
			getKeyUpTimer().schedule(200);
		} else if (!entityElementsList.isEmpty() || !enumElementsList.isEmpty())
			layoutDiv.getStyle().clearDisplay();
	}
	
	private Timer getKeyUpTimer() {
		if (keyUpTimer != null)
			return keyUpTimer;
		
		keyUpTimer = new Timer() {
			@Override
			public void run() {
				prepareLayoutDivText(getCurrentText());
			}
		};
		
		return keyUpTimer;
	}

	public void onScrollEvent(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (!inputTextArea.isOrHasChild(targetElement))
			return;
		
		event.preventDefault();
		event.stopPropagation();

		// Get defined max height from TextArea
		int currentHeight = inputTextArea.getOffsetHeight();
		int maxHeight = getPxStyle(inputTextArea, "max-height");

		// Check: Max height is reached?
		if (currentHeight < maxHeight)
			inputTextArea.setScrollTop(0); // Scroll back to top (Flickering)

		// Run AutoResize-Script
		expandTextarea(false);
	}

	public void onPasteEvent(Event event) {
		//TODO: also handle in the layout stuff
		Element targetElement = event.getEventTarget().cast();
		if (inputTextArea.isOrHasChild(targetElement)) {
			// Wait until browser is done!
			Scheduler.get().scheduleDeferred(() -> expandTextarea(false));
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();
		Element targetElement = nativeEvent.getEventTarget().cast();

		if (inputTextArea.isOrHasChild(targetElement))
			focusEditor();
		else if (openFormButtonCell.isOrHasChild(targetElement))
			handleShowForm();
		else if (inputSearchButton.isOrHasChild(targetElement)) {
			if (isSearchButtonEnabled)
				fireQueryPerform(!usePaging);
		} else if (inputSwitchPagingModeAnchor.isOrHasChild(targetElement))
			switchPagingMode();
		else if (inputSwitchAnchor.isOrHasChild(targetElement))
			fireModeChange(); // Change mode
		else if (queryContextCell.isOrHasChild(targetElement))
			getQueryContextMenu().show(queryContextCell, new AnchorAlignment(Anchor.TOP_LEFT));
	}

	private void handleShowForm() {
		if (!isOpenFormButtonVisible)
			return;
		
		// Set query to dialog and run AutoResize-Script
		setQueryToQueryFormDialog(getTypeSignatureExpandedParsedQuery());
		expandTextarea(false);
		focusEditor();

		XElement xInputHolder = XElement.as(inputHolder);
		QueryFormDialog dialog = getQueryFormDialog();
		dialog.setUsePaging(isUsePaging());
		dialog.setSize(xInputHolder.getOffsetWidth() + "px", queryFormDialogHeight + "px");
		dialog.setShowingPosition(xInputHolder.getX(), xInputHolder.getY() + xInputHolder.getOffsetHeight() - 16);
		dialog.show();
	}

	@Override
	public void onResize(ResizeEvent event) {
		// Draw layout
		forceLayout();

		// Positioning element
		positioningTextarea(false);
		expandTextarea(true);
	}

	public void onMouseDown(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement)) {
			if (isSearchButtonEnabled)
				inputSearchButton.addClassName(queryModelEditorClickedSearchElement); // Add clicked search element class name (click style)
		} else if (layoutDiv.isOrHasChild(targetElement)) {
			Scheduler.get().scheduleDeferred(
					() -> QueryModelEditorScripts.setInputCursorPosition(inputTextArea, QueryModelEditorScripts.getElementCursorPosition(layoutDiv)));
		}
	}
	
	public void onMouseOut(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement) && isSearchButtonEnabled)
			inputSearchButton.removeClassName(queryModelEditorClickedSearchElement); // Remove clicked search element class name (click style)
	}

	public void onMouseUp(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement) && isSearchButtonEnabled)
			inputSearchButton.removeClassName(queryModelEditorClickedSearchElement); // Remove clicked search element class name (click style)
	}
	
	private void handleQuickAccessDate(QuickAccessResult result) {
		String dateDisplay = prepareDateDisplay((Date) result.getObject());
		//resultRepresentationList.add(new Pair<String, String>(dateDisplay, getDateRepresentation(date)));
		
		setCurrentText(getCurrentText().replaceAll(result.getFilterText(), dateDisplay));
	}
	
	private void handleQuickAccessEntity(GenericEntity entity, String filterText) {
		String entityDisplay = getEntityAsString(entity);
		ImageResource icon = getIcon(entity);
		
		String text = getCurrentText();
		int cursorPosition = getFixedCursorPosition();
		if (!filterText.isEmpty()) {
			text = text.substring(0, cursorPosition - filterText.length()) + text.substring(cursorPosition);
			cursorPosition -= filterText.length();
		}
		setCurrentText(text);
		
		entityRepresentationList.add(new Pair<>(entityDisplay, getEntityRepresentation(entity)));
		
		String firstPart = text.substring(0, cursorPosition);
		String lastPart = text.substring(cursorPosition);
		text = firstPart + entityDisplay + lastPart;
		//setCurrentText(value);
		inputTextArea.setValue(text);
		layoutDiv.setInnerText(firstPart);
		
		SpanElement valueSpan = Document.get().createSpanElement();
		valueSpan.addClassName("queryModelEntityClass");
		valueSpan.setInnerText(entityDisplay);
		layoutDiv.appendChild(valueSpan);
		
		layoutDiv.appendChild(Document.get().createTextNode(lastPart));
		
		layoutDiv.getStyle().clearDisplay();
		
		entityElementsList.add(new Pair<>(entityDisplay, valueSpan));
		entityElementsPositionList.add(cursorPosition);
		
		ImagePrototypeElement iconElement = icon == null ? null : AbstractImagePrototype.create(icon).createElement();
		if (iconElement != null) {
			iconElement.getStyle().setBackgroundColor("white");
			iconElement.getStyle().setPosition(Position.ABSOLUTE);
			iconElement.getStyle().setMarginLeft(-16, Unit.PX);
		}
		
		ImagePrototypeElement deleteIcon = AbstractImagePrototype.create(QueryModelEditorResources.INSTANCE.delete()).createElement();
		deleteIcon.getStyle().setBottom(2, Unit.PX);
		deleteIcon.getStyle().setPaddingRight(4, Unit.PX);
		deleteIcon.getStyle().setPosition(Position.ABSOLUTE);
		deleteIcon.getStyle().setBackgroundColor("white");
		deleteIcon.getStyle().setCursor(Cursor.POINTER);
		deleteIcon.setClassName("queryModelEntityCloseClass");
		Event.sinkEvents(deleteIcon, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		DOM.sinkEvents(valueSpan, Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		
		EventListener eventListener = event -> {
			switch (event.getTypeInt()) {
				case Event.ONMOUSEOVER:
					valueSpan.appendChild(deleteIcon);
					if (iconElement != null)
						valueSpan.insertFirst(iconElement);
					hideDelete = false;
					break;
				case Event.ONMOUSEOUT:
					hideDelete = true;
					Scheduler.get().scheduleDeferred(() -> {
						if (hideDelete) {
							deleteIcon.removeFromParent();
							if (iconElement != null)
								iconElement.removeFromParent();
						}
					});
					break;
				case Event.ONCLICK:
					Element targetElement = event.getEventTarget().cast();
					if (deleteIcon.isOrHasChild(targetElement))
						removeEntityOrEnumElement(valueSpan, true);
					break;
			}
		};
		
		DOM.setEventListener(valueSpan, eventListener);
		DOM.setEventListener(deleteIcon, eventListener);
	}
	
	private void handleQuickAccessEnum(ObjectAndType objectAndType, String filterText) {
		String enumDisplay = objectAndType.getDescription();
		int index = enumDisplay.indexOf(" -");
		if (index != -1)
			enumDisplay = enumDisplay.substring(0, index);
		
		String text = getCurrentText();
		int cursorPosition = getFixedCursorPosition();
		if (!filterText.isEmpty()) {
			text = text.substring(0, cursorPosition - filterText.length()) + text.substring(cursorPosition);
			cursorPosition -= filterText.length();
		}
		setCurrentText(text);
		
		enumRepresentationList.add(new Pair<>(enumDisplay, getEnumRepresentation(currentQuickAccessTypeSignature, (Enum<?>) objectAndType.getObject())));
		
		String firstPart = text.substring(0, cursorPosition);
		String lastPart = text.substring(cursorPosition);
		text = firstPart + enumDisplay + lastPart;
		//setCurrentText(value);
		inputTextArea.setValue(text);
		layoutDiv.setInnerText(firstPart);
		
		SpanElement valueSpan = Document.get().createSpanElement();
		valueSpan.addClassName("queryModelEntityClass");
		valueSpan.setInnerText(enumDisplay);
		layoutDiv.appendChild(valueSpan);
		
		layoutDiv.appendChild(Document.get().createTextNode(lastPart));
		
		layoutDiv.getStyle().clearDisplay();
		
		enumElementsList.add(new Pair<>(enumDisplay, valueSpan));
		enumElementsPositionList.add(cursorPosition);
		
		ImagePrototypeElement deleteIcon = AbstractImagePrototype.create(QueryModelEditorResources.INSTANCE.delete()).createElement();
		deleteIcon.getStyle().setBottom(2, Unit.PX);
		deleteIcon.getStyle().setPaddingRight(4, Unit.PX);
		deleteIcon.getStyle().setPosition(Position.ABSOLUTE);
		deleteIcon.getStyle().setBackgroundColor("white");
		deleteIcon.getStyle().setCursor(Cursor.POINTER);
		deleteIcon.setClassName("queryModelEntityCloseClass");
		Event.sinkEvents(deleteIcon, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		DOM.sinkEvents(valueSpan, Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		
		EventListener eventListener = event -> {
			switch (event.getTypeInt()) {
				case Event.ONMOUSEOVER:
					valueSpan.appendChild(deleteIcon);
					hideDelete = false;
					break;
				case Event.ONMOUSEOUT:
					hideDelete = true;
					Scheduler.get().scheduleDeferred(() -> {
						if (hideDelete)
							deleteIcon.removeFromParent();
					});
					break;
				case Event.ONCLICK:
					Element targetElement = event.getEventTarget().cast();
					if (deleteIcon.isOrHasChild(targetElement))
						removeEntityOrEnumElement(valueSpan, false);
					break;
			}
		};
		
		DOM.setEventListener(valueSpan, eventListener);
		DOM.setEventListener(deleteIcon, eventListener);
	}

	/******************************** Helper Methods *******************************/

	private String getCurrentText() {
		return inputTextArea.getValue();
	}

	private void setCurrentText(final String newText) {
		inputTextArea.setValue(newText);
		prepareLayoutDivText(newText);
	}
	
	private void prepareLayoutDivText(String text) {
		if (text == null || text.equals(lastPreparedText)) {
			if (!entityElementsList.isEmpty() || !enumElementsList.isEmpty())
				layoutDiv.getStyle().clearDisplay();
			return;
		}
		
		lastPreparedText = text;
		layoutDiv.removeAllChildren();
		
		if (entityElementsList.isEmpty() && enumElementsList.isEmpty()) {
			layoutDiv.setInnerText(text);
			return;
		}
		
		text = SafeHtmlUtils.htmlEscape(text);
		
		boolean firstSet = false;
		String textPart = text;
		List<Pair<Pair<String, SpanElement>, Boolean>> entries = new ArrayList<>();
		
		int entitySize = entityElementsList.size();
		int enumSize = enumElementsList.size();
		int lastEntityIndex = 0;
		int lastEnumIndex = 0;
		for (int i = 0; i < entitySize + enumSize; i++) {
			int entityIndex = Integer.MAX_VALUE;
			int enumIndex = Integer.MAX_VALUE;
			if (entitySize > lastEntityIndex)
				entityIndex = entityElementsPositionList.get(lastEntityIndex);
			if (enumSize > lastEnumIndex)
				enumIndex = enumElementsPositionList.get(lastEnumIndex);
			
			if (entityIndex < enumIndex)
				entries.add(new Pair<>(entityElementsList.get(lastEntityIndex++), true));
			else
				entries.add(new Pair<>(enumElementsList.get(lastEnumIndex++), false));
		}
		
		int entityCounter = 0;
		int enumCounter = 0;
		for (Pair<Pair<String, SpanElement>, Boolean> entry : entries) {
			String display = entry.getFirst().getFirst();
			int index = textPart.indexOf(display);
			if (index == -1) {
				removeEntityOrEnumElement(entry.getFirst().getSecond(), entry.getSecond(), false);
				continue;
			}
			
			if (!firstSet) {
				firstSet = true;
				//layoutDiv.setInnerText(textPart.substring(0, index));
				layoutDiv.setInnerHTML(textPart.substring(0, index).replaceAll(" ", "&nbsp;"));
			} else
				layoutDiv.appendChild(Document.get().createTextNode(textPart.substring(0, index)));
			textPart = textPart.replaceFirst(display, "");
			
			layoutDiv.appendChild(entry.getFirst().getSecond());
			textPart = textPart.substring(index);
			if (entry.getSecond())
				entityElementsPositionList.set(entityCounter++, text.indexOf(display));
			else
				enumElementsPositionList.set(enumCounter++, text.indexOf(display));
		}
		
		if (entityElementsList.isEmpty() && enumElementsList.isEmpty())
			return;
		
		if (!textPart.isEmpty()) {
			SpanElement textSpan = Document.get().createSpanElement();
			textSpan.setInnerHTML(textPart.replaceAll(" ", "&nbsp;"));
			layoutDiv.appendChild(textSpan);
			//layoutDiv.appendChild(Document.get().createTextNode(textPart));
		}
		
		layoutDiv.getStyle().clearDisplay();
	}
	
	private void removeEntityOrEnumElement(SpanElement element, boolean removingEntity) {
		removeEntityOrEnumElement(element, removingEntity, true);
	}
	
	private void removeEntityOrEnumElement(SpanElement element, boolean removingEntity, boolean prepareLayout) {
		int indexToRemove = 0;
		String text = null;
		List<Pair<String, SpanElement>> elementsList = removingEntity ? entityElementsList : enumElementsList;
		List<?> representationList = removingEntity ? entityRepresentationList : enumRepresentationList;
		List<Integer> positionList = removingEntity ? entityElementsPositionList : enumElementsPositionList;
		for (Pair<String, SpanElement> entry : elementsList) {
			if (entry.getSecond().equals(element)) {
				text = entry.getFirst();
				break;
			}
			indexToRemove++;
		}
		
		elementsList.remove(indexToRemove);
		representationList.remove(indexToRemove);
		int entityElementPosition = positionList.remove(indexToRemove);
		
		element.removeFromParent();
		
		if (prepareLayout) {
			String currentText = inputTextArea.getValue();
			String firstPart = currentText.substring(0, entityElementPosition);
			String lastPart = currentText.substring(entityElementPosition + text.length());
			currentText = firstPart + lastPart;
			inputTextArea.setValue(currentText);
			
			if (entityElementsList.isEmpty() && enumElementsList.isEmpty())
				layoutDiv.getStyle().setDisplay(Display.NONE);
		}
	}
	
	private void walkEntityOrEnumElement(int index, String text, boolean right, boolean walkingEntity) {
		int position = walkingEntity ? entityElementsPositionList.get(index) : enumElementsPositionList.get(index);
		if (right)
			position = position + text.length();
		setCursorPosition(position);
	}
	
	private int isCloseToEntityOrEnum(boolean checkToTheRight, boolean checkEntity) {
		int index = getFixedCursorPosition();
		List<Integer> elementsPositionList = checkEntity ? entityElementsPositionList : enumElementsPositionList;
		if (checkToTheRight)
			return elementsPositionList.indexOf(index);
		
		List<Pair<String, String>> representationList = checkEntity ? entityRepresentationList : enumRepresentationList;
		for (int i = 0; i < elementsPositionList.size(); i++) {
			int position = elementsPositionList.get(i);
			String text = representationList.get(i).first();
			if (position + text.length() == index)
				return i;
		}
		
		return -1;
	}

	private void enableSearchButton(boolean value) {
		if (inputSearchButtonImage != null) {
			inputSearchButtonImage.getStyle().setOpacity(value == true ? 1d : 0.5d);
			isSearchButtonEnabled = value;
		}
	}

	private void enableSaveButton(boolean value) {
		if (saveActionControl != null)
			saveActionControl.enableSaveButton(value);
	}

	private void setQueryToQueryFormDialog(Query query) {
		// Set Query to QueryFormDialog
		QueryFormDialog dialog = getQueryFormDialog();
		dialog.configureEntity(query);
	}

	private void setOpenFormButtonCellVisible(boolean value) {
		isOpenFormButtonVisible = value;

		if (openFormButtonCell != null) {
			XElement xOpenFormButtonCell = XElement.as(openFormButtonCell);
			xOpenFormButtonCell.setVisible(value);
		}
	}

	private void setPagenationControlCellVisible(boolean value) {
		boolean oldValue = isPagenationControlEnabled;
		isPagenationControlEnabled = value;

		if (pagenationControlCell != null) {
			XElement xPagenationControlCell = XElement.as(pagenationControlCell);
			xPagenationControlCell.setVisible(value);
		}
		
		if (paginationControl != null) {
			paginationControl.setEnabled(isPagenationControlEnabled);
			paginationControl.setPageSize(currentPageSize);
		}

		if (oldValue != value) {
			// Fix TextArea
			positioningTextarea(true);
			expandTextarea(textAreaCollapsed);
		}
	}

	private Query getTypeSignatureExpandedParsedQuery() {
		// Parse the query (The query itself could be modified)
		ParsedQuery parseResult = QueryParser.parse(resolveEntitiesAndEnums(getCurrentText()));
		if (parseResult.getIsValidQuery() && parseResult.getErrorList().isEmpty()) {
			try {
				// Expand type signatures of parsed query with defined shortening mode
				return QueryTypeSignatureExpanderBuilder.create(parseResult.getQuery(), QueryModelEditorAdvancedPanel.this.shorteningMode).done();
			} catch (final Exception e) {
				// Show Exception as Notification
				GlobalState.showError(e.getMessage(), e);
			}
		}

		// Invalid
		return null;
	}

	private void positioningTextarea(boolean checkValidation) {
		// Get elements as XElement
		XElement xInputPlaceholder = XElement.as(inputPlaceholder);
		XElement xInputHolder = XElement.as(inputHolder);

		// Get bounds of placeholder and set them to holder
		Rectangle bounds = xInputPlaceholder.getBounds();
		int width = bounds.getWidth();
		if (checkValidation) {
			if (isPagenationControlEnabled)
				width -= validationImage.getParentElement().getClientWidth();
			else
				width += validationImage.getParentElement().getClientWidth() + 2;
		}
		
		xInputHolder.setBounds(bounds.getX() - 1, bounds.getY() - 1, width, bounds.getHeight());

		// Get default holder height and offset if control is visible
		if (defaultHolderHeight == 0 && xInputHolder.isVisible(true)) {
			defaultHolderHeight = xInputHolder.getClientHeight();
			holderOffsetHeight = xInputHolder.getOffsetHeight() - defaultHolderHeight;
		}

		// Get default TextBox height
		if (defaultTextBoxHeight == 0) {
			// get measurement div and check if control is visible
			XElement xMeasurementDiv = XElement.as(measurementDiv);
			if (xMeasurementDiv.isVisible(true)) {
				// Set default value and get height!
				xMeasurementDiv.setInnerHTML("<br>");
				defaultTextBoxHeight = xMeasurementDiv.getOffsetHeight();
			}
		}
	}

	private void expandTextarea(boolean collapse) {
		textAreaCollapsed = collapse;

		// Set convert textArea value and set TextBox width to measurement div
		measurementDiv.setInnerHTML(getCurrentText().replace("\n", "<br>") + "<br>");
		measurementDiv.getStyle().setWidth(inputTextArea.getOffsetWidth(), Unit.PX);

		// Get and check height of measurement div for InputHolder
		int newHeight = measurementDiv.getOffsetHeight();
		if (!collapse && newHeight + holderOffsetHeight > defaultHolderHeight) {
			// Set new height of inputHolder and add expanded TextBox class
			inputHolder.getStyle().setHeight(newHeight + holderOffsetHeight, Unit.PX);
			inputHolder.addClassName(queryModelEditorExpandedTextBoxClass);
		} else {
			// Reset height of inputHolder and remove expanded TextBox class
			inputHolder.getStyle().setHeight(defaultHolderHeight, Unit.PX);
			inputHolder.removeClassName(queryModelEditorExpandedTextBoxClass);
		}

		// Check height of measurement div
		if (newHeight > defaultTextBoxHeight) {
			// Is TextBox height higher then the holder height?
			if (!collapse || (newHeight < defaultHolderHeight - holderOffsetHeight)) {
				// Set new height of TextBox
				inputTextArea.getStyle().setHeight(newHeight, Unit.PX);
				layoutDiv.getStyle().setHeight(newHeight, Unit.PX);
			} else {
				// Set new height of TextBox
				inputTextArea.getStyle().setHeight(defaultHolderHeight - holderOffsetHeight, Unit.PX);
				layoutDiv.getStyle().setHeight(defaultHolderHeight - holderOffsetHeight, Unit.PX);
			}
		} else {
			// Reset height of TextBox
			inputTextArea.getStyle().clearHeight();
			layoutDiv.getStyle().clearHeight();
		}

		// Get defined max height from TextArea and check max reached?
		if (!collapse && newHeight >= getPxStyle(inputTextArea, "max-height")) {
			// Add scroll TextBox class (vertical scroll bar)
			inputTextArea.addClassName(queryModelEditorScrollTextBoxClass);
			measurementDiv.addClassName(queryModelEditorScrollTextBoxClass);
			layoutDiv.addClassName(queryModelEditorScrollTextBoxClass);
		} else {
			// Remove scroll TextBox class (vertical scroll bar)
			inputTextArea.removeClassName(queryModelEditorScrollTextBoxClass);
			measurementDiv.removeClassName(queryModelEditorScrollTextBoxClass);
			layoutDiv.removeClassName(queryModelEditorScrollTextBoxClass);
		}
	}

	private void validateCondition(String queryString) {
		queryString = resolveEntitiesAndEnums(queryString);
		
		// Clear ToolTip of validation image
		validationImage.setTitle(null);
		parsedQuery = null;

		try {
			// Parse the query
			ParsedQuery parseResult = QueryParser.parse(queryString);
			if (!parseResult.getIsValidQuery() || !parseResult.getErrorList().isEmpty()) {
				StringBuilder errorString = new StringBuilder();

				// Get all error strings of parse results error list
				for (final GmqlParsingError parsingError : parseResult.getErrorList()) {
					errorString.append(parsingError.getMessage());
					errorString.append("\n");
				}

				// Set errors as ToolTip of validation image
				validationImage.setTitle(errorString.toString());
				throw new RuntimeException("Error while parsing occured.");
			}

			// Check paging of parsed query
			Query query = parseResult.getQuery();
			queryContainsPaging = (query.getRestriction() != null && query.getRestriction().getPaging() != null);
			setPagenationControlCellVisible(usePaging && !queryContainsPaging);

			// Enable/Disable buttons
			enableSearchButton(true);
			setOpenFormButtonCellVisible(!parseResult.getVariablesMap().isEmpty());

			// Set validation image to valid
			validationImage.setSrc(QueryModelEditorResources.INSTANCE.valid().getSafeUri().asString());
			isQueryValid = true;
		} catch (final Exception e) {
			// Disable buttons
			enableSearchButton(false);
			setOpenFormButtonCellVisible(false);

			// Set validation image to invalid
			validationImage.setSrc(QueryModelEditorResources.INSTANCE.invalid().getSafeUri().asString());
			isQueryValid = false;
		}
		
		handleSaveButtonVisibility();
	}
	
	private void handleSaveButtonVisibility() {
		boolean enableSave = saveActionControl.isDefaultSaveAs()
				&& (gmSession == null || gmSession.getModelEnvironment().getWorkbenchModel() != null);

		// Enable save button when query string changed && set width of TextBox
		enableSaveButton(
				enableSave || displayPaths != null || (originalQueryString != null && originalQueryString.equals(getCurrentText()) ? false : true));
	}

	private String resolveEntitiesAndEnums(String queryString) {
		for (Pair<String, String> entry : entityRepresentationList)
			queryString = queryString.replaceFirst(entry.getFirst(), entry.getSecond());
		
		for (Pair<String, String> entry : enumRepresentationList)
			queryString = queryString.replaceFirst(entry.getFirst(), entry.getSecond());
		
		return queryString;
	}

	private void displayAutoCompletionDialog(Map<String, String> possibleHints, int dialogLeft, int dialogTop, int dialogWidth, int dialogHeight) {
		if (autoCompletionDialog == null) {
			// Create border less dialog
			autoCompletionDialog = new AutoCompletionDialog(inputTextArea);
			autoCompletionDialog.setBorders(false);
			autoCompletionDialog.setBodyBorder(false);
			autoCompletionDialog.setHeaderVisible(false);

			// Set AutoCompletionDialog Buttons and hide informations
			autoCompletionDialog.setHideOnButtonClick(true);
			autoCompletionDialog.setPredefinedButtons();
			autoCompletionDialog.setAutoHide(true);

			// Set dialog to ModalDialog
			autoCompletionDialog.setMaximizable(false);
			autoCompletionDialog.setDraggable(true);
			autoCompletionDialog.setResizable(true);
			autoCompletionDialog.setClosable(false);
			autoCompletionDialog.setModal(false);
			autoCompletionDialog.setOnEsc(false);

			try {
				// Create content panel of AutoCompletionDialog
				autoCompletionPanel = autoCompletionPanelProvider.get();
				autoCompletionPanel.setAdvancedQueryModelEditorPanel(this);

				// Set content panel to AutoCompletionDialog
				autoCompletionDialog.setWidget(autoCompletionPanel);
			} catch (final RuntimeException e) {
				GlobalState.showError("Error while providing the AutoCompletionPanel.", e);
				return;
			}
		}

		// Hide auto completion dialog
		autoCompletionDialog.hide();
		showingAutoCompletionPanel = false;
		autoCompletionPanel.setPossibleHints(possibleHints);

		// Show dialog, then set bounds
		autoCompletionDialog.show(); // Set bounds of dialog after showing!
		showingAutoCompletionPanel = true;
		autoCompletionDialog.setBounds(dialogLeft, dialogTop, dialogWidth, dialogHeight);
	}

	private void hideAutoCompletionDialog() {
		// Hide query auto completion dialog
		if (this.autoCompletionDialog != null && showingAutoCompletionPanel) {
			this.autoCompletionDialog.hide();
			showingAutoCompletionPanel = false;
		}
	}
	
	private QuickAccessDialog getQuickAccessDialog() {
		if (quickAccessDialog != null)
			return quickAccessDialog;
		
		quickAccessDialog = new QuickAccessDialog();
		quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
		quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
		quickAccessDialog.setUseInstantiateButton(false);
		//if (useCase != null) //TODO?
			//quickAccessDialog.configureUseCase(useCase);
		//quickAccessDialog.setBorders(true);
		//quickAccessDialog.setBodyBorder(true);
		
		/*quickAccessDialog.addListener(Events.Hide, new Listener<ComponentEvent>() {
			public void handleEvent(ComponentEvent be) {
				quickAccessTextField.editing = false; //TODO?
				quickAccessTextField.blur();
			}
		});*/
		try {
			quickAccessDialog.intializeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel quickAccessPanel = quickAccessPanelProvider.get();
			quickAccessPanel.setTextField(inputTextArea);
			quickAccessPanel.setMinCharsForFilter(0); //TODO Needed to be 0 (before it was 1)?
			//quickAccessPanel.setUseQueryActions(false);
			//quickAccessPanel.setUseCase(useCase); //TODO?
			return quickAccessPanel;
		};
	}
	
	private int getFixedCursorPosition() {
		int cursorPosition = getCursorPosition();
		if (cursorPosition >= 0) {
			// Fix cursor position if cursor is at the end
			String textAreaValue = getCurrentText();
			if (cursorPosition > textAreaValue.length())
				cursorPosition = textAreaValue.length();
		}
		
		return cursorPosition;
	}
	
	private String getEntityAsString(GenericEntity entity) {
		EntityType<GenericEntity> entityType = entity.entityType();
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, entity, (ModelMdResolver) null, null/*useCase*//*, null*/);
		if (selectiveInformation != null)
			return selectiveInformation;
		
		return "";
	}
	
	private ImageResource getIcon(GenericEntity entity) {
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(entity.entityType(), entity));
		ImageResource icon = null;
		try {
			//iconProvider.configureUseCase(useCase);
			iconProvider.configureGmSession(gmSession);
			IconAndType iconAndType = iconProvider.apply(modelPath);
			if (iconAndType != null)
				icon = iconAndType.getIcon();
		} catch (RuntimeException e) {
			logger.error("Error while preparing the icon.", e);
		}
		
		return icon;
	}
	
	private String getEntityRepresentation(GenericEntity entity) {
		EntityType<GenericEntity> entityType = entity.entityType();
		Object id = entity.getId();
		
		StringJoiner joiner = new StringJoiner(",", "reference(", ")");
		joiner.add(entityType.getTypeSignature());
		if (id != null) {
			//joiner.add(idType.instanceToGmString(id));
			String stringifiedId = BasicQueryStringifier.create().stringify(id);
			joiner.add(stringifiedId);
			joiner.add("true");
		} else {
			//joiner.add(SimpleTypes.TYPE_LONG.instanceToGmString(entity.runtimeId()));
			String stringifiedId = BasicQueryStringifier.create().stringify(entity.runtimeId());
			joiner.add(stringifiedId);
			joiner.add("false");
		}
		
		return joiner.toString();
	}
	
	private String getEnumRepresentation(String typeSignature, Enum<?> theEnum) {
		StringJoiner joiner = new StringJoiner(",", "enum(", ")");
		joiner.add(typeSignature);
		joiner.add(theEnum.name());
		return joiner.toString();
	}
	
	@SuppressWarnings("deprecation")
	private String prepareDateDisplay(Date date) {
		StringBuilder builder = new StringBuilder("date(");
		builder.append(date.getYear() + 1900).append("Y,");
		builder.append(date.getMonth() + 1).append("M,");
		builder.append(date.getDate()).append("D)");
		
		return builder.toString();
	}
	
	private static class AutoCompletionDialog extends Dialog {
		
		public AutoCompletionDialog(Element inputTextArea) {
			BaseEventPreview eventPreview = new BaseEventPreview() {
				@Override
				protected boolean onAutoHide(NativePreviewEvent pe) {
					if (isResizing(AutoCompletionDialog.this))
						return false;

					Element target = Element.as(pe.getNativeEvent().getEventTarget());
					if (target == inputTextArea)
						return false;

					if (pe.getTypeInt() == Event.ONMOUSEUP)
						return false;

					hide();
					return true;
				}
			};
					
			eventPreview.getIgnoreList().add(getElement());
			setEventPreview(eventPreview, this);
		}
		
	}
	
	private ColumnData prepareColumnData() {
		ColumnData columnData = null;
		
		if (displayPaths != null) {
			columnData = ColumnData.T.create();
			columnData.setDisplayNode(displayNode);
			columnData.setNodeWidth(nodeWidth);
			columnData.setDisplayPaths(new ArrayList<>(displayPaths));
		}
		
		return columnData;
	}
	
	/**
	 * Sets the current query context name.
	 * @param global - true when using a global context. Should be false when a context is given.
	 * @param contextName - name of the context. Should be null when set as global.
	 */
	@Override
	public void setCurrentContext(boolean global, String contextName) {
		globalItem.setChecked(global, true);
		contextMenuItem.setChecked(!global, true);
		
		queryContextCell.setInnerText(global ? LocalizedText.INSTANCE.global() : contextName);
		
		if (!global) {
			contextMenuItem.setText(contextName);
			contextMenuItem.setVisible(true);
		}
		
		if (global) {
			fireModeChange();
			otherQueryProviderView.setCurrentContext(true, null);
			if (otherQueryProviderView instanceof HasText)
				((HasText) otherQueryProviderView).setText(null);
		}
		
		/*if (global)
			disableFullMode();
		else
			enableFullMode();*/
	}
	
	/**
	 * Removes the query context.
	 */
	@Override
	public void removeQueryContext() {
		globalItem.setChecked(true, true);
		contextMenuItem.setVisible(false);
	}
	
	/**
	 * Adds a new query context.
	 * @param contextName - name of the context
	 */
	@Override
	public void addQueryContext(String contextName) {
		contextMenuItem.setText(contextName);
		contextMenuItem.setVisible(true);
	}
	
	private Menu getQueryContextMenu() {
		if (queryContextMenu != null)
			return queryContextMenu;
		
		queryContextMenu = new Menu();
		queryContextMenu.setMinWidth(180);
		queryContextMenu.add(globalItem);
		queryContextMenu.add(contextMenuItem);
		
		globalItem.addSelectionHandler(event -> setCurrentContext(true, null));
		contextMenuItem.addSelectionHandler(event -> setCurrentContext(false, contextMenuItem.getText()));
		
		return queryContextMenu;
	}
	
	/**************************** Native Helper-Methods ****************************/

	private int getCursorPosition() {
		return QueryModelEditorScripts.getInputCursorPosition(this.inputTextArea);
	}

	private void setCursorPosition(final int position) {
		QueryModelEditorScripts.setInputCursorPosition(this.inputTextArea, position);
	}

	private static int getPxStyle(final Element element, final String styleProperty) {
		// Get defined max height from TextArea
		final String maxHeightString = QueryModelEditorScripts.getStyle(element, styleProperty);
		return Integer.parseInt(maxHeightString.replaceAll("[^\\d.]", ""), 10);
	}
	
	private static native boolean isResizing(Dialog dialog) /*-{
		return dialog.@com.sencha.gxt.widget.core.client.Window::resizing;
	}-*/;
	
	private static native void setEventPreview(BaseEventPreview eventPrev, Dialog dialog) /*-{
		dialog.@com.sencha.gxt.widget.core.client.Window::eventPreview = eventPrev;
	}-*/;
	
	@Override
	public void disposeBean() throws Exception {
		if (disposed)
			return;

		//RootPanel.get().remove(this);
		
		if (queryProviderViewListeners != null)
			queryProviderViewListeners.clear();
		
		if (this.queryFormDialog != null)
			this.queryFormDialog.disposeBean();
		
		if (this.autoCompletionPanel != null)
			this.autoCompletionPanel.disposeBean();
		
		disposed = true;
		
		if (otherQueryProviderView instanceof DisposableBean)
			((DisposableBean) otherQueryProviderView).disposeBean();
	}
	
}
