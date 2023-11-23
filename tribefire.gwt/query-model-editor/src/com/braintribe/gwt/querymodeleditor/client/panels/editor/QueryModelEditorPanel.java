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
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.MenuWithSearch;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.qc.api.client.QueryProviderActions;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.controls.DropDownControl;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.controls.PaginationControl;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormDialog;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormTemplate;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorResources;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorTemplates;
import com.braintribe.gwt.querymodeleditor.client.resources.TemplateConfigurationBean;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.display.DefaultSort;
import com.braintribe.model.meta.data.display.SortDirection;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;
import com.braintribe.model.processing.query.tools.QueryTools;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.template.Template;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class QueryModelEditorPanel extends ContentPanel implements InitializableBean, QueryProviderView<GenericEntity>, QueryProviderActions,
		ClickHandler, HasText, InputFocusHandler, DisposableBean {

	/********************************** Constants **********************************/

	// TODO: Add Logging

	private final String queryInputTableId = hashCode() + "_queryInputTable";
	private final String queryInputTextboxId = hashCode() + "_queryInputTextbox";
	private final String openFormButtonCellId = hashCode() + "_openFormButtonCell";
	private final String openFormButtonId = hashCode() + "_openFormButton";
	private final String queryContextCellId = hashCode() + "_queryContextCell";
	private final String querySearchButtonId = hashCode() + "_querySearchButton";
	private final String querySearchButtonImageId = hashCode() + "_querySearchButtonImage";
	private final String pagenationControlCellId = hashCode() + "_pagenationControlCell";
	private final String orderByDropDownCellId = hashCode() + "_orderByDropDownCell";
	private final String addOrderByDropDownCellId = hashCode() + "_addOrderByDropDownCell";
	private final String removeOrderByDropDownCellId = hashCode() + "_removeOrderByDropDownCell";
	//private final String orderingDirectionButtonId = hashCode() + "_orderingDirectionButton";
	private final String addOrderingButtonId = hashCode() + "_addOrderingButton";
	private final String removeOrderingButtonId = hashCode() + "_removeOrderingButton";
	private final String switchModeAnchorId = hashCode() + "_switchModeAnchor";
	private final String switchPagingModeAnchorId = hashCode() + "_switchPagingModeAnchor";

	private final String selectQueryInputTable = "div[@id='" + queryInputTableId + "']";
	private final String selectQueryInputTextbox = "input[@id='" + queryInputTextboxId + "']";
	private final String selectOpenFormButtonCell = "div[@id='" + openFormButtonCellId + "']";
	private final String selectOpenFormButton = "img[@id='" + openFormButtonId + "']";
	private final String selectQueryContextCell = "div[@id='" + queryContextCellId + "']";
	private final String selectQuerySearchButton = "div[@id='" + querySearchButtonId + "']";
	private final String selectQuerySearchButtonImage = "img[@id='" + querySearchButtonImageId + "']";
	private final String selectPagenationControlCell = "div[@id='" + pagenationControlCellId + "']";
	private final String selectOrderByDropDownCell = "div[@id='" + orderByDropDownCellId + "']";
	private final String selectAddOrderByDropDownCell = "div[@id='" + addOrderByDropDownCellId + "']";
	private final String selectRemoveOrderByDropDownCell = "div[@id='" + removeOrderByDropDownCellId + "']";	
	//private final String selectOrderingDirectionButton = "img[@id='" + orderingDirectionButtonId + "']";
	private final String selectAddOrderingButton = "img[@id='" + addOrderingButtonId + "']";	
	private final String selectRemoveOrderingButton = "img[@id='" + removeOrderingButtonId + "']";		
	private final String selectSwitchModeAnchor = "a[@id='" + switchModeAnchorId + "']";
	private final String selectSwitchPagingModeAnchor = "a[@id='" + switchPagingModeAnchorId + "']";
	
	private final String selectCountElement = "div[@class='queryModelEditorCountElement']";

	private static final String queryModelEditorClickedSearchElement = "queryModelEditorClickedSearchElement";
	private static int queryFormDialogHeight = 150;

	/********************************** Variables **********************************/

	private boolean usePaging = true;
	private int currentPageSize = PaginationControl.pageSizeValues.get(4);

	private DivElement queryInputTable = null;
	private InputElement inputTextArea = null;

	private DivElement openFormButtonCell = null;
	private ImageElement openFormButton = null;
	
	private DivElement queryContextCell;

	private DivElement inputSearchButton = null;
	private ImageElement inputSearchButtonImage = null;

	private DivElement paginationControlCell = null;
	private PaginationControl paginationControl = null;
	private AnchorElement inputSwitchPagingModeAnchor = null;

	private DivElement orderByDropDownCell = null;
	private DropDownControl orderByDropDown = null;
	private Menu orderByPropertiesMenu = null;
	private DivElement addOrderByDropDownCell = null;
	private Menu addOrderByPropertiesMenu = null;
	private DivElement removeOrderByDropDownCell = null;
	private Menu removeOrderByPropertiesMenu = null;

	//private Map<Pair<String,String>, ImageElement> mapOrderingDirectionButton = new HashMap<>();
	//private ImageElement orderingDirectionButton = null;
	private ImageElement addOrderingButton = null;
	private ImageElement removeOrderingButton = null;
	private AnchorElement inputSwitchAnchor = null;

	private PersistenceGmSession gmSession = null;
	private QueryFormDialog queryFormDialog = null;

	private Query externalQuery = null;
	private Query providerContextQuery = null;

	private boolean getOriginalQuery = false;
	private boolean queryContainsPaging = false;

	private int maxNumberOfLevelsInOrderMenu = 4;

	private boolean isOpenFormButtonVisible = false;
	private boolean isPagenationControlEnabled = false;

	private String currentOrderByPropertyPath = null;
	//private OrderingDirection currentOrderingDirection = OrderingDirection.ascending;
	private final FulltextComparison basicFulltextComparison = FulltextComparison.T.create();

	private Supplier<? extends QueryProviderView<GenericEntity>> otherQueryProviderViewSupplier;
	private QueryProviderView<GenericEntity> otherQueryProviderView = null;
	private final List<QueryProviderViewListener> queryProviderViewListeners = new ArrayList<>();
	private List<QueryOrderItem> orderDirectionList = new ArrayList<>();
	
	private boolean disposed;
	
	private Template template;
	
	private BasicQuerySelectionResolver basicQuerySelectionResolver;
	private Comparator<PropertyPriority> priorityComparator;
	private Menu queryContextMenu;
	private CheckMenuItem globalItem = new CheckMenuItem(LocalizedText.INSTANCE.global());
	private CheckMenuItem contextMenuItem = new CheckMenuItem();
	private List<InputTriggerListener> inputTriggerListeners;
	private DivElement countElement;
	private EntityType<?> parentEntityType = null;


	/**************************** QueryModelEditorPanel ****************************/

	public QueryModelEditorPanel() {
		super();

		// Define style of Query-Editor
		setId("QueryModelEditor");
		setHeaderVisible(false);
		setDeferHeight(false);
		setBodyBorder(false);
		setBorders(false);

		// Needed to enable events - but only when using it stand-alone
		//addWidgetToRootPanel(this);
	}

	@Configurable
	public void setQueryFormDialog(final QueryFormDialog queryFormDialog) {
		this.queryFormDialog = queryFormDialog;

		this.queryFormDialog.setQueryModelEditorActions(this);
		this.queryFormDialog.configureFocusObject(this.inputTextArea);
		this.queryFormDialog.addHideHandler(event -> {
			Variable searchTextVariable = queryFormDialog.getQueryTemplate().getSearchTextVariable();
			if (searchTextVariable != null)
				setCurrentText(queryFormDialog.getSearchTextValue());
		});
		//this.queryFormDialog.configureUseEntityReferences(true);
	}

	/**
	 * Configures the initial page size. Defaults to 10.
	 *
	 * @see QueryModelEditorPanel#setUsePaging(boolean)
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

	/**
	 * Configures the maximum number of levels for preparing the order by menu. Defaults to 4.
	 */
	@Configurable
	public void setMaxNumberOfLevelsInOrderMenu(final int maxNumberOfLevelsInOrderMenu) {
		this.maxNumberOfLevelsInOrderMenu = maxNumberOfLevelsInOrderMenu;
	}

	/**
	 * @return input text area
	 */
	public InputElement getInputTextArea() {
		return inputTextArea;
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
	 * @return search image element
	 */
	/*public DivElement getInputSearchButton() {
		return inputSearchButton;
	}*/

	/**
	 * @return search image element
	 */
	public ImageElement getInputSearchButtonImage() {
		return inputSearchButtonImage;
	}

	/**
	 * @return pagination control cell
	 */
	public DivElement getPaginationControlCell() {
		return paginationControlCell;
	}

	/**
	 * @return order by properties menu cell
	 */
	/*
	public DivElement getOrderByDropDownCell() {
		return orderByDropDownCell;
	}
	*/

	/**
	 * @return ordering direction image element
	 */
	/*public ImageElement getOrderingDirectionButton() {
		return orderingDirectionButton;
	}
	*/
	
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
		bean.setQueryInputTableId(queryInputTableId);
		bean.setQueryInputTextboxId(queryInputTextboxId);
		bean.setOpenFormButtonCellId(openFormButtonCellId);
		bean.setOpenFormButtonId(openFormButtonId);
		bean.setQueryContextCellId(queryContextCellId);
		bean.setQuerySearchButtonId(querySearchButtonId);
		bean.setQuerySearchButtonImageId(querySearchButtonImageId);
		bean.setPaginationControlCellId(pagenationControlCellId);
		bean.setSwitchPagingModeAnchorId(switchPagingModeAnchorId);
		bean.setOrderByDropDownCellId(orderByDropDownCellId);
		bean.setAddOrderByDropDownCellId(addOrderByDropDownCellId);
		//bean.setOrderingDirectionButtonId(orderingDirectionButtonId);
		bean.setAddOrderingButtonId(addOrderingButtonId);
		bean.setRemoveOrderingButtonId(removeOrderingButtonId);
		bean.setRemoveOrderByDropDownCellId(removeOrderByDropDownCellId);
		bean.setSwitchModeAnchorId(switchModeAnchorId);
		add(new HTML(QueryModelEditorTemplates.INSTANCE.qmePanel(bean)));

		XElement element = getElement();
		queryInputTable = DomQuery.selectNode(selectQueryInputTable, element).cast();
		inputTextArea = DomQuery.selectNode(selectQueryInputTextbox, element).cast();

		openFormButtonCell = DomQuery.selectNode(selectOpenFormButtonCell, element).cast();
		setOpenFormButtonCellVisible(false);

		openFormButton = DomQuery.selectNode(selectOpenFormButton, element).cast();
		openFormButton.setSrc(QueryModelEditorResources.INSTANCE.dropDown().getSafeUri().asString());
		
		queryContextCell = DomQuery.selectNode(selectQueryContextCell, element).cast();
		globalItem.setGroup("queryContextGroup");
		globalItem.setChecked(true, true);
		contextMenuItem.setGroup("queryContextGroup");
		contextMenuItem.setVisible(false);

		inputSearchButton = DomQuery.selectNode(selectQuerySearchButton, element).cast();
		inputSearchButtonImage = DomQuery.selectNode(selectQuerySearchButtonImage, element).cast();
		inputSearchButtonImage.setSrc(QueryModelEditorResources.INSTANCE.query().getSafeUri().asString());

		inputSwitchPagingModeAnchor = DomQuery.selectNode(selectSwitchPagingModeAnchor, element).cast();
		
		orderByDropDownCell = DomQuery.selectNode(selectOrderByDropDownCell, element).cast();
		orderByDropDownCell.appendChild(getOrderByDropDown().getElement());

		/*
		orderingDirectionButton = DomQuery.selectNode(selectOrderingDirectionButton, element).cast();
		orderingDirectionButton.setSrc(getOrderingDirectionIcon());
		orderingDirectionButton.setTitle(getOrderingDirectionIconTitle());
		*/
		
		addOrderingButton = DomQuery.selectNode(selectAddOrderingButton, element).cast();
		addOrderingButton.setSrc(getAddOrderingIcon());
		addOrderingButton.setTitle(LocalizedText.INSTANCE.addOrder());

		removeOrderingButton = DomQuery.selectNode(selectRemoveOrderingButton, element).cast();
		removeOrderingButton.setSrc(getRemoveOrderingIcon());
		removeOrderingButton.setTitle(LocalizedText.INSTANCE.removeOrder());		
		
		addOrderByDropDownCell = DomQuery.selectNode(selectAddOrderByDropDownCell, element).cast();
		removeOrderByDropDownCell = DomQuery.selectNode(selectRemoveOrderByDropDownCell, element).cast();
		
		//addOrderByDropDownCell.appendChild(getAddOrderByDropDown().getElement());
		
		inputSwitchAnchor = DomQuery.selectNode(selectSwitchModeAnchor, element).cast();
		inputSwitchAnchor.setName(LocalizedText.INSTANCE.switchAdvanced());
		inputSwitchAnchor.setInnerText(LocalizedText.INSTANCE.switchAdvanced());
		
		countElement = DomQuery.selectNode(selectCountElement, element).cast();
		
		preparePagingElements();

		// Draw layout
		forceLayout();

		// Add Events to InputTextArea
		DOM.sinkEvents(inputTextArea, Event.ONKEYDOWN);
		DOM.setEventListener(inputTextArea, this);

		// Add Events to InputSearchButton
		DOM.sinkEvents(inputSearchButton, Event.ONMOUSEDOWN | Event.ONMOUSEOUT | Event.ONMOUSEUP);
		DOM.setEventListener(inputSearchButton, this);

		// Add Event to Element
		addDomHandler(this, ClickEvent.getType());
	}

	private void preparePagingElements() {
		queryFormDialog.setUsePaging(usePaging);
		paginationControlCell = DomQuery.selectNode(selectPagenationControlCell, getElement()).cast();
		paginationControlCell.appendChild(getPaginationControl().getElement());
		inputSwitchPagingModeAnchor.setName(usePaging ? LocalizedText.INSTANCE.autoPaging() : LocalizedText.INSTANCE.paging());
		inputSwitchPagingModeAnchor.setInnerText(usePaging ? LocalizedText.INSTANCE.autoPaging() : LocalizedText.INSTANCE.paging());
		setPaginationControlCellVisible(usePaging);
	}

	/*private static void addWidgetToRootPanel(final Widget widget) {
		RootPanel.get().add(widget);
	}
	
	private static void removeWidgetFromRootPanel(final Widget widget) {
		RootPanel.get().remove(widget);
	}*/
	
	/**
	 * Hides the additional UI elements such as the order by, advanced and paging elements.
	 */
	private void disableFullMode() {
		Element paginationControlCell = DomQuery.selectNode(selectPagenationControlCell, getElement());
		paginationControlCell.getNextSiblingElement().getStyle().setDisplay(Display.NONE);
	}
	
	/**
	 * Shows the additional UI elements such as the order by, advanced and paging elements.
	 */
	private void enableFullMode() {
		Element paginationControlCell = DomQuery.selectNode(selectPagenationControlCell, getElement());
		paginationControlCell.getNextSiblingElement().getStyle().clearDisplay();
	}

	public PaginationControl getPaginationControl() {
		if (paginationControl != null)
			return paginationControl;
		
		// Create pagination control
		paginationControl = new PaginationControl();
		paginationControl.setPageSize(currentPageSize);
		paginationControl.setEnabled(isPagenationControlEnabled);

		// Define pagination control
		SelectionHandler<Item> selectPageSizeHandler = event -> {
			paginationControl.enableIncreasePageButton(false);
			paginationControl.enableDecreasePageButton(false);
			currentPageSize = getPaginationControl().getPageSize();
			fireQueryPerform(false);
		};
		
		ClickHandler paginationClickEvents = event -> {
			paginationControl.enableIncreasePageButton(false);
			paginationControl.enableDecreasePageButton(false);
			fireQueryPerform(false);
		};

		// Set event handler of pagination control
		paginationControl.setPageSizeSelected(selectPageSizeHandler);
		paginationControl.setIncreasePageClicked(paginationClickEvents);
		paginationControl.setDecreasePageClicked(paginationClickEvents);

		return paginationControl;
	}

	public DropDownControl getOrderByDropDown() {
		if (orderByDropDown != null)
			return orderByDropDown;
		
		// Set style to overwrite default!
		orderByDropDown = new DropDownControl();
		orderByDropDown.setMenu(getOrderByPropertiesMenu());
		orderDirectionList.clear();
		if (currentOrderByPropertyPath != null) {
			QueryOrderItem queryOrderItem = new QueryOrderItem();
			queryOrderItem.setDisplayName(currentOrderByPropertyPath);
			queryOrderItem.setPropertyName(currentOrderByPropertyPath);
			orderDirectionList.add(queryOrderItem);
		}
		orderByDropDown.setHTML(getOrderByHTML(orderDirectionList));
		updateOrderingButtonList(orderDirectionList);
		//orderByDropDown.setHTML(getOrderByHTML(currentOrderByPropertyPath));

		// Needed to enable events - but only when using it stand-alone
		//addWidgetToRootPanel(orderByDropDown);

		return orderByDropDown;
	}

	private Menu getOrderByPropertiesMenu() {
		if (orderByPropertiesMenu == null)
			orderByPropertiesMenu = new MenuWithSearch();

		return orderByPropertiesMenu;
	}
	
	private Menu getAddOrderByPropertiesMenu() {
		if (addOrderByPropertiesMenu == null)
			addOrderByPropertiesMenu = new MenuWithSearch();

		return addOrderByPropertiesMenu;
	}
	
	private Menu getRemoveOrderByPropertiesMenu() {
		if (removeOrderByPropertiesMenu == null)
			removeOrderByPropertiesMenu = new Menu();

		return removeOrderByPropertiesMenu;
	}		

	private QueryFormDialog getQueryFormDialog() {
		if (queryFormDialog == null)
			setQueryFormDialog(new QueryFormDialog());

		return queryFormDialog;
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
	
	@Override
	public void addInputTriggerListener(InputTriggerListener listener) {
		if (inputTriggerListeners == null)
			inputTriggerListeners = new ArrayList<>();
		
		inputTriggerListeners.add(listener);
	}
	
	@Override
	public void removeInputTriggerListener(InputTriggerListener listener) {
		if (inputTriggerListeners != null) {
			inputTriggerListeners.remove(listener);
			if (inputTriggerListeners.isEmpty())
				inputTriggerListeners = null;
		}
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
				if (paginationControl != null)
					return paginationControl.getPageSize();
				
				return currentPageSize;
			}
			
			@Override
			public Template getTemplate() {
				return template;
			}

			@Override
			public Query getQuery(boolean initialQuery) {
				Query query = null;

				// Copy the received external query so the original will not be changed
				if (initialQuery)
					providerContextQuery = copyQuery(externalQuery);
				
				boolean handledTemplateSearchText = false;
				query = providerContextQuery;
				if (!getOriginalQuery && template != null) { //Handle template only if there is really a template
					QueryFormTemplate queryTemplate = getQueryFormDialog().getQueryTemplate();
					handledTemplateSearchText = handleTemplateSearchText(queryTemplate);
	
					try {
						// Replace variables with values
						if (queryTemplate.getTemplatehasValueDescriptors())
							query = queryTemplate.evaluateQuery(query, template);
					} catch (final TemplateEvaluationException e) {
						query = providerContextQuery;
					}
				}
				
				if (query == null || getOriginalQuery)
					return query;

				if (!handledTemplateSearchText)
					handleFulltext(query);
				Restriction restriction = query.getRestriction();

				// Determine paging
				if (!isAutoPageEnabled() && isPagenationControlEnabled && paginationControl != null) {
					// Create restriction of query is missing
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
					paging.setPageSize(paginationControl.getPageSize());
					paging.setStartIndex(paginationControl.getPageIndex());
				}

				// Determine ordering
				if (orderDirectionList.size() > 0) {
					Source orderingSource = null;

					if (!(query instanceof SelectQuery) || (orderingSource = QueryTools.getSingleSource((SelectQuery) query)) != null) {
						//RVE 
						if (orderDirectionList.size() == 1) {
							//just one order
							QueryOrderItem queryOrderItem = orderDirectionList.get(0);
							
							SimpleOrdering simpleOrdering = acquireQueryOrdering(query);							
							simpleOrdering.setDirection(queryOrderItem.getOrderingDirection());
							simpleOrdering.setOrderBy(propertyOperand(orderingSource, queryOrderItem.getPropertyName()));							
						} else {												
							//more order
							CascadedOrdering cascadedOrdering = CascadedOrdering.T.create();
							//for (String key : orderDirectionMap.keySet()) {
							for (QueryOrderItem queryOrderItem : orderDirectionList) {
								SimpleOrdering simpleOrdering = SimpleOrdering.T.create();
								simpleOrdering.setDirection(queryOrderItem.getOrderingDirection());
								simpleOrdering.setOrderBy(propertyOperand(orderingSource, queryOrderItem.getPropertyName()));
								cascadedOrdering.getOrderings().add(simpleOrdering);
							}						
							query.setOrdering(cascadedOrdering);
						}
					}					
				}
					
				return query;
			}

			private void handleFulltext(Query query) {
				// Prepare text for full text search
				String currentText = getCurrentText();
				basicFulltextComparison.setText(currentText);

				// Determine full text search
				Restriction restriction = query.getRestriction();
				Condition queryCondition = restriction != null ? restriction.getCondition() : null;
				if (currentText != null && !currentText.isEmpty()) {
					if (restriction == null) {
						restriction = Restriction.T.create();
						queryCondition = basicFulltextComparison;
						restriction.setCondition(queryCondition);
						query.setRestriction(restriction);
					} else if (!restrictionContainsFulltextComparison(restriction)) {
						if (queryCondition != null) {
							List<Condition> operands;

							if (queryCondition instanceof Conjunction) {
								Conjunction conjunction = ((Conjunction) queryCondition);

								operands = conjunction.getOperands();
								if (conjunction.getOperands() == null) {
									operands = new ArrayList<>();
									conjunction.setOperands(operands);
								}
							} else {
								Conjunction conjunction = Conjunction.T.create();

								operands = new ArrayList<>();
								operands.add(queryCondition);

								conjunction.setOperands(operands);
								restriction.setCondition(conjunction);
							}

							operands.add(basicFulltextComparison);
						} else {
							queryCondition = basicFulltextComparison;
							restriction.setCondition(queryCondition);
						}
					}
				} else if (restrictionContainsFulltextComparison(restriction)) {
					if (queryCondition == basicFulltextComparison) {
						queryCondition = null;
						restriction.setCondition(null);
					} else if (queryCondition instanceof Conjunction) {
						final Conjunction conjunction = (Conjunction) queryCondition;

						if (conjunction.getOperands() != null) {
							for (Condition condition : conjunction.getOperands()) {
								if (condition == basicFulltextComparison) {
									conjunction.getOperands().remove(condition);
									break;
								}
							}
						}
					}
				}
			}

			private SimpleOrdering acquireQueryOrdering(Query query) {
				Ordering qo = query.getOrdering();
				if (qo instanceof SimpleOrdering)
					return (SimpleOrdering) qo;

				SimpleOrdering so = SimpleOrdering.T.create();
				query.setOrdering(so);
				return so;
			}

			private Query copyQuery(final Query query) {
				try {
					// Clone the query
					return GMT.clone(query);
				} catch (GmTraversingException ex) {
					// Show Exception
					GlobalState.showError(ex.getMessage(), ex);
					return null;
				}
			}
		};

		return context;
	}
	
	private boolean handleTemplateSearchText(QueryFormTemplate queryTemplate) {
		Variable variable = queryTemplate.getSearchTextVariable();
		if (variable == null)
			return false;
		
		queryTemplate.updateVariableValue(variable.getName(), getCurrentText());
		return true;
	}
	
	private boolean restrictionContainsFulltextComparison(Restriction restriction) {
		if (restriction == null)
			return false;
		
		Condition condition = restriction.getCondition();
		if (condition == basicFulltextComparison)
			return true;
		
		if (condition instanceof Conjunction) {
			Conjunction conjunction = (Conjunction) condition;

			List<Condition> operands = conjunction.getOperands();
			if (operands != null && !operands.isEmpty()) {
				for (Condition operand : operands) {
					if (operand == basicFulltextComparison)
						return true;
				}
			}
		}
		
		return false;
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
		// Setting current entity type
		if (entityContent instanceof Template) {
			template = (Template) entityContent;
			externalQuery = (Query) template.getPrototype();
		} else {
			template = null;
			if (entityContent != null)
				externalQuery = (Query) entityContent;
		}

		// Set Query to QueryFormDialog
		QueryFormDialog dialog = getQueryFormDialog();
		dialog.configureEntity(entityContent);

		// Check properties of received external query
		queryContainsPaging = (externalQuery != null && externalQuery.getRestriction() != null
				&& externalQuery.getRestriction().getPaging() != null);
		
		if (!usePaging && queryContainsPaging) {
			usePaging = true;
			preparePagingElements();
			setPageSize(externalQuery.getRestriction().getPaging().getPageSize());
		} else if (!queryContainsPaging && usePaging) {
			usePaging = false;
			preparePagingElements();
			setPageSize(PaginationControl.pageSizeValues.get(4));
		}
		determinOrdering();
		determineAutoPagingSize();

		// Check if query has variables using the QueryFormTemplate of the QueryFormDialog
		boolean openFormButtonVisible = dialog.getQueryTemplate().getTemplateHasVariables();
		setOpenFormButtonCellVisible(openFormButtonVisible);
		
		Variable searchTextVariable = dialog.getQueryTemplate().getSearchTextVariable();
		if (searchTextVariable != null) {
			Object defaultValue = searchTextVariable.getDefaultValue();
			if (defaultValue instanceof String)
				setCurrentText((String) defaultValue);
		}
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
		gmSession = persistenceGmSession;
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
	public void setOtherModeQueryProviderView(Supplier<? extends QueryProviderView<GenericEntity>> otherModelQueryProviderViewSuplier) {
		otherQueryProviderViewSupplier = otherModelQueryProviderViewSuplier;
	}

	@Override
	public void modeQueryProviderViewChanged() {
		// Draw layout
		forceLayout();
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
		if (otherQueryProviderView != null)
			otherQueryProviderView.onViewChange(displayNode, nodeWidth, columnsVisible);
	}
	
	private void switchPagingMode() {
		usePaging = !usePaging;
		preparePagingElements();
	}
	
	@Override
	public String getCurrentQueryText() {
		return getCurrentText();
	}
	
	@Override
	public void setDisplayCountText(String countText) {
		if (countText != null)
			countElement.setInnerSafeHtml(SafeHtmlUtils.fromSafeConstant(countText));
		else
			countElement.setInnerHTML("");
	}

	/***************************** QueryProviderActions ****************************/

	@Override
	public void fireModeChange() {
		// Enable other mode
		QueryProviderView<GenericEntity> otherView = getOtherQueryProviderView();
		if (queryProviderViewListeners == null || otherView == null)
			return;
		
		try {
			// Set getQuery to NoModify-Mode
			getOriginalQuery = true;

			// Hide QueryFormDialog
			getQueryFormDialog().hide();

			// Copy the list because the listener list will get changed internally
			for (QueryProviderViewListener listener : new ArrayList<>(queryProviderViewListeners))
				listener.onModeChanged(otherView, true);
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

		if (queryProviderViewListeners != null) {
			// Always get query from query provider to ensure you get always the same query!
			for (final QueryProviderViewListener listener : queryProviderViewListeners)
				listener.onQueryPerform(getQueryProviderContext());
		}

		getQueryFormDialog().hide();
		focusEditor();
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onBrowserEvent(final Event event) {
		switch (event.getTypeInt()) {
			case Event.ONKEYDOWN:
				onKeyDownEvent(event);
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

	public void onKeyDownEvent(final Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (!inputTextArea.isOrHasChild(targetElement))
			return;
		
		switch (event.getKeyCode()) {
			case KeyCodes.KEY_ENTER: {
				if (!event.getShiftKey()) {
					event.preventDefault();
					event.stopPropagation();
	
					fireQueryPerform(!usePaging);
				}
	
				break;
			}
			case KeyCodes.KEY_ESCAPE: {
				event.preventDefault();
				event.stopPropagation();
	
				getQueryFormDialog().hide();
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onClick(final ClickEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();
		Element targetElement = nativeEvent.getEventTarget().cast();

		if (inputSearchButton.isOrHasChild(targetElement)) {
			fireQueryPerform(!usePaging);
			fireInputTriggerListeners();
		} else if (inputSwitchPagingModeAnchor.isOrHasChild(targetElement))
			switchPagingMode();
		else if (inputSwitchAnchor.isOrHasChild(targetElement))
			fireModeChange();
		else if (openFormButtonCell.isOrHasChild(targetElement))
			handleShowForm();
		/* else if (orderingDirectionButton.isOrHasChild(targetElement)) {
			currentOrderingDirection = currentOrderingDirection == OrderingDirection.ascending ? OrderingDirection.descending
					: OrderingDirection.ascending;
			orderingDirectionButton.setSrc(getOrderingDirectionIcon());
			orderingDirectionButton.setTitle(getOrderingDirectionIconTitle());

			fireQueryPerform(!usePaging);
		}*/ else if (addOrderByDropDownCell.isOrHasChild(targetElement)) {
			Menu menu = getAddOrderByPropertiesMenu();
			//menu.show(addOrderByDropDownCell, new AnchorAlignment(Anchor.BOTTOM_LEFT));
			if (menu.getWidgetCount() > 0)
				menu.show(addOrderByDropDownCell, new AnchorAlignment(Anchor.BOTTOM_LEFT));
		} else if (removeOrderByDropDownCell.isOrHasChild(targetElement)) {
			Menu menu = getRemoveOrderByPropertiesMenu();
			if (menu.getWidgetCount() > 0)
				menu.show(removeOrderByDropDownCell, new AnchorAlignment(Anchor.BOTTOM_LEFT));			
		} else if (queryContextCell.isOrHasChild(targetElement)) {
			getQueryContextMenu().show(queryContextCell, new AnchorAlignment(Anchor.TOP_LEFT));
		} else {
			//for (Pair<String, String> pair : mapOrderingDirectionButton.keySet()) {
			for (QueryOrderItem queryOrderItem : orderDirectionList) {
				ImageElement imageOrderingDirectionElement = queryOrderItem.getButtonImageElement();
				if (imageOrderingDirectionElement == null)
					continue;
				
				if (imageOrderingDirectionElement.isOrHasChild(targetElement)) {
					String orderTitleAsc = getOrderingDirectionIconTitle(OrderingDirection.ascending);
					OrderingDirection orderingDirection = OrderingDirection.ascending;
					if (orderTitleAsc.equals(targetElement.getTitle())) 
						orderingDirection = OrderingDirection.descending; 

					queryOrderItem.setOrderingDirection(orderingDirection);
					imageOrderingDirectionElement.setSrc(getOrderingDirectionIcon(orderingDirection));
					imageOrderingDirectionElement.setTitle(getOrderingDirectionIconTitle(orderingDirection));
					fireQueryPerform(!usePaging);
					break;
				}
			}
				
		}
		
	}
	
	private void fireInputTriggerListeners() {
		if (inputTriggerListeners == null)
			return;
		
		new ArrayList<>(inputTriggerListeners).forEach(listener -> listener.onTriggerClick());
	}

	private void handleShowForm() {
		if (!isOpenFormButtonVisible)
			return;
		
		QueryFormDialog dialog = getQueryFormDialog();
		QueryFormTemplate queryTemplate = dialog.getQueryTemplate();
		if (queryTemplate.getSearchTextVariable() == null)
			setCurrentText("");
		focusEditor();

		XElement xQueryInputTable = XElement.as(queryInputTable);
		handleTemplateSearchText(queryTemplate);
		dialog.setUsePaging(isUsePaging());
		dialog.setDefaultHeight(queryFormDialogHeight);
		dialog.setWidth(xQueryInputTable.getOffsetWidth());
		dialog.setShowingPosition(xQueryInputTable.getX(), xQueryInputTable.getY() + xQueryInputTable.getOffsetHeight());
		dialog.show();
	}

	public void onMouseDown(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement)) {
			// Add clicked search element class name (click style)
			inputSearchButton.addClassName(queryModelEditorClickedSearchElement);
		}
	}

	public void onMouseOut(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement)) {
			// Remove clicked search element class name (click style)
			inputSearchButton.removeClassName(queryModelEditorClickedSearchElement);
		}
	}

	public void onMouseUp(Event event) {
		Element targetElement = event.getEventTarget().cast();
		if (inputSearchButton.isOrHasChild(targetElement)) {
			// Remove clicked search element class name (click style)
			inputSearchButton.removeClassName(queryModelEditorClickedSearchElement);
		}
	}

	/******************************** Helper Methods *******************************/
	
	//private Map<String, OrderingDirection> getCascadedOrderingMap(CascadedOrdering cascadedOrdering, EntityType<?> entityType) {
	private List<QueryOrderItem> getCascadedOrderingList(CascadedOrdering cascadedOrdering, EntityType<?> entityType) {
		List<QueryOrderItem> cascadedOrderingList = new ArrayList<>();
		
		EntityMdResolver entityMdResolver = null;
		if (entityType != null)
			entityMdResolver = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType)
					.useCase(KnownUseCase.queryEditorUseCase.getDefaultValue());
		
		for (SimpleOrdering simpleOrdering : cascadedOrdering.getOrderings()) {
			Object orderBy = simpleOrdering.getOrderBy();
			if (orderBy instanceof EntitySignature) {
				QueryOrderItem queryOrderItem = new QueryOrderItem();
				queryOrderItem.setDisplayName(LocalizedText.INSTANCE.type());
				queryOrderItem.setPropertyName(LocalizedText.INSTANCE.type());
				queryOrderItem.setOrderingDirection(simpleOrdering.getDirection());
				cascadedOrderingList.add(queryOrderItem);
			} else if (orderBy instanceof PropertyOperand) {
				String propertyName = ((PropertyOperand) orderBy).getPropertyName();
				String displayName = propertyName;
				
				if (entityMdResolver != null) {
					Name name = entityMdResolver.property(propertyName).meta(Name.T).exclusive();
					if (name != null)
						displayName = I18nTools.getLocalized(name.getName());
				}
				
				QueryOrderItem queryOrderItem = new QueryOrderItem();
				queryOrderItem.setDisplayName(displayName);
				queryOrderItem.setPropertyName(propertyName);
				queryOrderItem.setOrderingDirection(simpleOrdering.getDirection());
				cascadedOrderingList.add(queryOrderItem);
			}
		}
		
		return cascadedOrderingList;
	}

	private void determinOrdering() {
		// initialize order by properties menu
		orderByPropertiesMenu.clear();
		getAddOrderByPropertiesMenu().clear();
		getRemoveOrderByPropertiesMenu().clear();

		//currentOrderingDirection = OrderingDirection.ascending;
		currentOrderByPropertyPath = null;
		
		if (externalQuery == null)
			return;

		parentEntityType = getEntityTypeOfQuery(externalQuery);
		Ordering externalQueryOrdering = externalQuery.getOrdering();
		if (externalQueryOrdering instanceof CascadedOrdering) {
			mountOrderByPropertiesMenu(parentEntityType, orderByPropertiesMenu, 0, null, OrderingDirection.ascending, null, false);
			mountOrderByPropertiesMenu(parentEntityType, addOrderByPropertiesMenu, 0, null, OrderingDirection.ascending, null, true);
			prepareUIForCascadedOrdering((CascadedOrdering) externalQueryOrdering, parentEntityType);
			mountRemoveOrderingMenu(removeOrderByPropertiesMenu, orderDirectionList);
			return;
		}
		/*
		orderingDirectionButton.getStyle().clearVisibility();
		if (orderByDropDown != null)
			orderByDropDown.setEnabled(true);
		*/
		
		if (parentEntityType == null)
			return;
		
		SimpleOrdering ordering = externalQueryOrdering instanceof SimpleOrdering ? (SimpleOrdering) externalQueryOrdering : null;

		DefaultSort defaultSort = null;
		if (externalQueryOrdering == null) {
			EntityMdResolver entityMdResolver = gmSession.getModelAccessory().getMetaData().entityType(parentEntityType)
					.useCase(KnownUseCase.queryEditorUseCase.getDefaultValue());
			defaultSort = entityMdResolver.meta(DefaultSort.T).exclusive();
		}
		if (defaultSort != null) {
			final SimpleOrdering simpleOrdering = SimpleOrdering.T.create();
			simpleOrdering.setDirection(defaultSort.getDirection() == SortDirection.ascending ? OrderingDirection.ascending : OrderingDirection.descending);
			String name = defaultSort.getProperty().getName();
			Source source = getExternalQuerySingleSource();
			simpleOrdering.setOrderBy(propertyOperand(source, name));
			ordering = simpleOrdering;
		} else if (externalQueryOrdering instanceof SimpleOrdering) {
			//currentOrderingDirection = ((SimpleOrdering) externalQueryOrdering).getDirection();
			//orderingDirectionButton.setSrc(getOrderingDirectionIcon());
			//orderingDirectionButton.setTitle(getOrderingDirectionIconTitle());
		}
		String propertyName = ordering != null && ordering.getOrderBy() != null && ordering.getOrderBy() instanceof PropertyOperand
				? ((PropertyOperand) ordering.getOrderBy()).getPropertyName()
				: null;
		
		OrderingDirection orderingDirection = ordering != null ? ordering.getDirection() : OrderingDirection.ascending;
		mountOrderByPropertiesMenu(parentEntityType, orderByPropertiesMenu, 0, propertyName, orderingDirection, null, false);
		mountOrderByPropertiesMenu(parentEntityType, addOrderByPropertiesMenu, 0, propertyName, orderingDirection, null, true);
		mountRemoveOrderingMenu(removeOrderByPropertiesMenu, orderDirectionList);
	}
	
	private void mountRemoveOrderingMenu(Menu menu, List<QueryOrderItem> orderItemList) {
		menu.clear();
		for (QueryOrderItem queryOrderItem : orderItemList) {	
			if (queryOrderItem.getDisplayName() == null)
				continue;
			
			MenuItem menuItem = new MenuItem(queryOrderItem.getDisplayName());
			menuItem.addSelectionHandler(event -> {
				Scheduler.get().scheduleDeferred(() -> {				
					orderItemList.remove(queryOrderItem);
					getOrderByDropDown().setHTML(getOrderByHTML(orderItemList));
					updateOrderingButtonList(orderItemList);										
					fireQueryPerform(true);
					getAddOrderByPropertiesMenu().clear();
					mountOrderByPropertiesMenu(parentEntityType, addOrderByPropertiesMenu, 0, queryOrderItem.getPropertyName(), queryOrderItem.getOrderingDirection(), null, true);
					mountRemoveOrderingMenu(removeOrderByPropertiesMenu, orderItemList);
				});
			});
			menu.add(menuItem);
		}
		
		if (menu.getWidgetCount() == 0)
			removeOrderingButton.addClassName("queryModelEditorRemoveIconDisabled");
		else
			removeOrderingButton.removeClassName("queryModelEditorRemoveIconDisabled");
			
	}

	private void prepareUIForCascadedOrdering(CascadedOrdering cascadedOrdering, EntityType<?> entityType) {
		//orderingDirectionButton.getStyle().setVisibility(Visibility.HIDDEN);
		//getOrderByDropDown().setEnabled(false);
		
		//Map<String, OrderingDirection> cascadedOrderingMap = getCascadedOrderingMap(cascadedOrdering, entityType);
		orderDirectionList.clear();
		orderDirectionList.addAll(getCascadedOrderingList(cascadedOrdering, entityType));
		getOrderByDropDown().setHTML(getOrderByHTML(orderDirectionList));
		updateOrderingButtonList(orderDirectionList);
		//getAddOrderByDropDown().setHTML(getOrderByHTML(cascadedOrderingMap));
	}
	
	private void determineAutoPagingSize() {
		String entityTypeSignature = null;
		if (externalQuery instanceof EntityQuery)
			entityTypeSignature = ((EntityQuery) externalQuery).getEntityTypeSignature();
		else if (externalQuery instanceof SelectQuery) {
			List<QuerySelection> selections = getBasicQuerySelectionResolver().resolve((SelectQuery) externalQuery);
			entityTypeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery(selections);
		}
		
		Integer autoPagingSize = GMEMetadataUtil.getAutoPagingSize(template, entityTypeSignature, gmSession,
				KnownUseCase.queryEditorUseCase.getDefaultValue());
		
		if (autoPagingSize != null)
			setPageSize(autoPagingSize);
	}

	/**
	 * Note that if the externalQuery is a SelectQuery, this method is only reachable iff the singleSource exists
	 * (otherwise the entityType inside {@link #determinOrdering()} would be null and the whole ordering processing is
	 * skipped).
	 */
	private Source getExternalQuerySingleSource() {
		return externalQuery instanceof SelectQuery ? QueryTools.getSingleSource((SelectQuery) externalQuery) : null;
	}

	private static PropertyOperand propertyOperand(Source source, String name) {
		PropertyOperand result = PropertyOperand.T.create();
		result.setSource(source);
		result.setPropertyName(name);

		return result;
	}

	/*
	private String getOrderingDirectionIcon() {
		return getOrderingDirectionIcon(currentOrderingDirection);
	}
	*/
	
	private static String getOrderingDirectionIcon(OrderingDirection direction) {
		ImageResource icon = direction == OrderingDirection.ascending ? QueryModelEditorResources.INSTANCE.ascending()
				: QueryModelEditorResources.INSTANCE.descending();
		return icon.getSafeUri().asString();
	}
	
	/*
	private String getOrderingDirectionIconTitle() {
		return getOrderingDirectionIconTitle(currentOrderingDirection);
	}
	*/
	
	private static String getOrderingDirectionIconTitle(OrderingDirection direction) {
		return direction == OrderingDirection.ascending ? LocalizedText.INSTANCE.ascending()
				: LocalizedText.INSTANCE.descending();		
	}

	private static String getAddOrderingIcon() {
		ImageResource icon = QueryModelEditorResources.INSTANCE.addBig();
		return icon.getSafeUri().asString();
	}	

	private static String getRemoveOrderingIcon() {
		ImageResource icon = QueryModelEditorResources.INSTANCE.removeBig();
		return icon.getSafeUri().asString();
	}	
	
	private String getCurrentText() {
		return inputTextArea.getValue();
	}

	private void setCurrentText(final String newText) {
		inputTextArea.setValue(newText);
	}

	private void setOpenFormButtonCellVisible(final boolean value) {
		isOpenFormButtonVisible = value;

		if (openFormButtonCell != null) {
			final XElement xOpenFormButtonCell = XElement.as(openFormButtonCell);
			xOpenFormButtonCell.setVisible(value);
		}
	}

	private void setPaginationControlCellVisible(final boolean value) {
		isPagenationControlEnabled = value;

		if (paginationControlCell != null) {
			final XElement xPagenationControlCell = XElement.as(paginationControlCell);
			xPagenationControlCell.setVisible(value);
		}
		if (paginationControl != null) {
			paginationControl.setEnabled(isPagenationControlEnabled);
			paginationControl.setPageSize(currentPageSize);
		}
	}

	/*
	private static String getOrderByHTML(final String orderByPropertyPath) {
		StringBuilder orderByHTML = new StringBuilder();
		orderByHTML.append("<div class='queryModelEditorOrderByLabel'>");
		orderByHTML.append("<span style='color:silver;'>").append(LocalizedText.INSTANCE.orderedBy()).append("</span>");
		if (orderByPropertyPath != null)
			orderByHTML.append(" " + orderByPropertyPath);
		orderByHTML.append("</div>");
		
		return orderByHTML.toString();
	}
	*/
	
	private String getOrderByHTML(List<QueryOrderItem> orderList) {
		StringBuilder orderByHTML = new StringBuilder();
		orderByHTML.append("<div class='queryModelEditorOrderByLabel'>");
		orderByHTML.append("<span style='color:silver;'>").append(LocalizedText.INSTANCE.orderedBy()).append("&nbsp;</span>");

		//for (Map.Entry<String, OrderingDirection> entry : orderMap.entrySet()) {			
		for (QueryOrderItem queryOrderItem : orderList) {			
			String orderingDirectionButtonId = queryOrderItem.hashCode() + "_orderingDirectionButton";
			orderByHTML.append("<span class='queryModelEditorOrderByLabel'>").append(queryOrderItem.getDisplayName()).append("<span>");
			//orderByHTML.append("<img class='queryModelEditorOrderingIconDisabled' src='").append(getOrderingDirectionIcon(entry.getValue())).append("'/>");
			orderByHTML.append("<img id='").append(orderingDirectionButtonId).append("' class='queryModelEditorOrderingIcon' src='");
			orderByHTML.append(getOrderingDirectionIcon(queryOrderItem.getOrderingDirection()));
			orderByHTML.append("' title='").append(getOrderingDirectionIconTitle(queryOrderItem.getOrderingDirection())).append("'/>");
			queryOrderItem.setOrderButtonId(orderingDirectionButtonId);
		}
		
		orderByHTML.append("</div>");
		
		return orderByHTML.toString();
	}

	private void updateOrderingButtonList(List<QueryOrderItem> orderList) {
		XElement element = getElement();
		//for (Pair<String, String> pair  : mapOrderingDirectionButton.keySet()) {
		for (QueryOrderItem queryOrderItem : orderList) {			
			String keyId = queryOrderItem.getOrderButtonId();
			String selectorOrderingDirectionButton = "img[@id='" + keyId + "']";
			ImageElement orderingDirectionButton = DomQuery.selectNode(selectorOrderingDirectionButton, element).cast();
			if (orderingDirectionButton != null) {
				orderingDirectionButton.setSrc(getOrderingDirectionIcon(queryOrderItem.getOrderingDirection()));
				orderingDirectionButton.setTitle(getOrderingDirectionIconTitle(queryOrderItem.getOrderingDirection()));	
				queryOrderItem.setButtonImageElement(orderingDirectionButton);
			}
		}
		
		orderByDropDown.setEnabled(orderList.size() == 1);
	}
	
	private static EntityType<?> getEntityTypeOfQuery(Query query) {
		if (query instanceof EntityQuery)
			return GMF.getTypeReflection().getEntityType(((EntityQuery) query).getEntityTypeSignature());

		if (query instanceof PropertyQuery) {
			// not sure about this decision
			PropertyQuery propertyQuery = (PropertyQuery) query;
			String properyName = propertyQuery.getPropertyName();
			EntityType<?> entityType = GMF.getTypeReflection().getEntityType(((PropertyQuery) query).getEntityReference().getTypeSignature());
			GenericModelType propertyType = entityType.getProperty(properyName).getType();
			if (propertyType.isCollection())
				propertyType = ((CollectionType) propertyType).getCollectionElementType();

			return propertyType.isEntity() ? (EntityType<?>) propertyType : null;
		}
		
		if (query instanceof SelectQuery) {
			From from = QueryTools.getSingleSource((SelectQuery) query);
			if (from != null)
				return GMF.getTypeReflection().getEntityType(from.getEntityTypeSignature());
			
			return null;
		}

		throw new IllegalArgumentException("Unsupported Query type: " + query);
	}

	private void mountOrderByPropertiesMenu(EntityType<?> entityType, Menu parentMenu, int level, String propertyName, OrderingDirection orderingDirection,
			String propertyPath, boolean addNew) {
		List<PropertyPriority> menuItems = new ArrayList<>();
		
		EntityMdResolver entityMdResolver = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType)
				.useCase(KnownUseCase.queryEditorUseCase.getDefaultValue());
		
		for (Property property : entityType.getProperties()) {
			GenericModelType propertyType = property.getType();
			if (propertyType.isCollection() || (propertyType.isEntity()) && level >= maxNumberOfLevelsInOrderMenu)
				continue;
			
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(property.getName());
			if (!GMEMetadataUtil.isPropertyVisible(propertyMdResolver))
				continue;
			
			Name nameMD = propertyMdResolver.meta(Name.T).exclusive();
			String displayInfo = (nameMD != null && nameMD.getName() != null) ? I18nTools.getLocalized(nameMD.getName()) : property.getName();

			String orderByPropertyPath;
			if (propertyPath == null)
				orderByPropertyPath = property.getName();
			else
				orderByPropertyPath = propertyPath + "." + property.getName();
			boolean skip = false;
			for (QueryOrderItem queryOrderItem : orderDirectionList) {
				if (orderByPropertyPath.equals(queryOrderItem.getPropertyName())) {
					//RVE - do not allow add same property twice
					skip = true;
					continue;
				}
			}					
			if (skip)
				continue;
			
			MenuItem menuItem = new MenuItem(displayInfo);
			if (!propertyType.isEntity()) {
				menuItem.addSelectionHandler(event -> {
					Scheduler.get().scheduleDeferred(() -> {				

						if (propertyPath == null)
							currentOrderByPropertyPath = property.getName();
						else
							currentOrderByPropertyPath = propertyPath + "." + property.getName();
	
						String orderByPropertyPath2 = currentOrderByPropertyPath;
						
						OrderingDirection newOrderingDirection = OrderingDirection.ascending;
						if (!addNew) {
							if (orderDirectionList.size() == 1) {
								QueryOrderItem oueryOrderItem = orderDirectionList.get(0);
								newOrderingDirection = oueryOrderItem.getOrderingDirection();
							}
							orderDirectionList.clear();
						}
						
						for (QueryOrderItem queryOrderItem : orderDirectionList) {
							if (orderByPropertyPath2.equals(queryOrderItem.getPropertyName())) {
								//RVE - do not allow add same property twice
								return;
							}
						}					
						
						QueryOrderItem queryOrderItem = new QueryOrderItem();
						queryOrderItem.setDisplayName(displayInfo);
						queryOrderItem.setPropertyName(orderByPropertyPath2);
						queryOrderItem.setOrderingDirection(newOrderingDirection);
						orderDirectionList.add(queryOrderItem);
						//orderDirectionList.put(displayInfo, currentOrderingDirection);
						getOrderByDropDown().setHTML(getOrderByHTML(orderDirectionList));
						updateOrderingButtonList(orderDirectionList);
						fireQueryPerform(true);
						getAddOrderByPropertiesMenu().clear();
						mountOrderByPropertiesMenu(parentEntityType, addOrderByPropertiesMenu, 0, propertyName, orderingDirection, null, true);
						mountRemoveOrderingMenu(removeOrderByPropertiesMenu, orderDirectionList);
					});
				});
			} else {
				Menu menu = new Menu();

				String currentPropertyPath;
				if (propertyPath == null)
					currentPropertyPath = property.getName();
				else
					currentPropertyPath = propertyPath + "." + property.getName();

				mountOrderByPropertiesMenu((EntityType<?>) propertyType, menu, level + 1, null, orderingDirection, currentPropertyPath, addNew);
				menuItem.setSubMenu(menu);
			}
			
			if (currentOrderByPropertyPath == null) {
				if (propertyName != null) {
					if (propertyName.equals(property.getName())) {
						currentOrderByPropertyPath = property.getName();
						orderDirectionList.clear();
						QueryOrderItem queryOrderItem = new QueryOrderItem();
						queryOrderItem.setDisplayName(displayInfo);
						queryOrderItem.setPropertyName(property.getName());
						queryOrderItem.setOrderingDirection(orderingDirection);
						orderDirectionList.add(queryOrderItem);					
						getOrderByDropDown().setHTML(getOrderByHTML(orderDirectionList));						
						updateOrderingButtonList(orderDirectionList);
					}
				} else if (property.isIdentifier() && level == 0) {
					currentOrderByPropertyPath = property.getName();
					orderDirectionList.clear();
					QueryOrderItem queryOrderItem = new QueryOrderItem();
					queryOrderItem.setDisplayName(displayInfo);
					queryOrderItem.setPropertyName(property.getName());
					queryOrderItem.setOrderingDirection(orderingDirection);
					orderDirectionList.add(queryOrderItem);					
					getOrderByDropDown().setHTML(getOrderByHTML(orderDirectionList));
					updateOrderingButtonList(orderDirectionList);
				}
			}

			/*
			if (currentOrderingDirection == null) {
				//currentOrderingDirection = ordering != null ? ordering.getDirection() : OrderingDirection.ascending;
				currentOrderingDirection = orderingDirection;
				//orderingDirectionButton.setSrc(getOrderingDirectionIcon());
				//orderingDirectionButton.setTitle(getOrderingDirectionIconTitle());
			}
			*/
			
			Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
			if (priority == null)
				priority = Double.NEGATIVE_INFINITY;
			
			menuItems.add(new PropertyPriority(menuItem, priority));
		}
		
		Collections.sort(menuItems, getPriorityComparator());
		for (PropertyPriority propertyPriority : menuItems)
			parentMenu.add(propertyPriority.menuItem);
		
		if (parentMenu.getWidgetCount() == 0)
			addOrderingButton.addClassName("queryModelEditorAddIconDisabled");
		else
			addOrderingButton.removeClassName("queryModelEditorAddIconDisabled");		
	}
	
	private BasicQuerySelectionResolver getBasicQuerySelectionResolver() {
		if (basicQuerySelectionResolver != null)
			return basicQuerySelectionResolver;
		
		basicQuerySelectionResolver = BasicQuerySelectionResolver.create()
				.aliasMode()
				.simple()
				.shorteningMode()
				.simplified();
		
		return basicQuerySelectionResolver;
	}
	
	private QueryProviderView<GenericEntity> getOtherQueryProviderView() {
		if (otherQueryProviderView != null)
			return otherQueryProviderView;

		if (otherQueryProviderViewSupplier == null)
			return null;
		
		otherQueryProviderView = otherQueryProviderViewSupplier.get();
		// Set this QueryProviderView in the other defined QueryProviderView
		otherQueryProviderView.setOtherModeQueryProviderView(() -> QueryModelEditorPanel.this);
		
		return otherQueryProviderView;
	}
	
	private Comparator<PropertyPriority> getPriorityComparator() {
		if (priorityComparator != null)
			return priorityComparator;
		
		priorityComparator = (o1, o2) -> {
			int priorityComparison = o2.priority.compareTo(o1.priority);
			if (priorityComparison == 0)
				return o1.menuItem.getText().compareToIgnoreCase(o2.menuItem.getText());
			
			return priorityComparison;
		};
		return priorityComparator;
	}

	/**
	 * Sets the current query context name.
	 * @param global - true when using a global context. Should be false when a context is given.
	 * @param contextName - name of the context. Should be null when set as global.
	 */
	@Override
	public void setCurrentContext(boolean global, String contextName) {
		setCurrentContext(global, contextName, false);
	}
	
	private void setCurrentContext(boolean global, String contextName, boolean fireEvent) {
		globalItem.setChecked(global, true);
		contextMenuItem.setChecked(!global, true);
		
		queryContextCell.setInnerText(global ? LocalizedText.INSTANCE.global() : contextName);
		
		if (!global) {
			contextMenuItem.setText(contextName);
			contextMenuItem.setVisible(true);
		}
		
		if (global)
			disableFullMode();
		else
			enableFullMode();
		
		if (fireEvent)
			fireContextChanged(global);
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
		
		globalItem.addSelectionHandler(event -> setCurrentContext(true, null, true));
		contextMenuItem.addSelectionHandler(event -> setCurrentContext(false, contextMenuItem.getText(), true));
		
		return queryContextMenu;
	}
	
	private void fireContextChanged(boolean global) {
		if (queryProviderViewListeners == null)
			return;
		
		new ArrayList<>(queryProviderViewListeners).forEach(listener -> listener.onContextChanged(global));
	}
	
	private class PropertyPriority {
		private MenuItem menuItem;
		private Double priority;
		
		public PropertyPriority(MenuItem menuItem, Double priority) {
			this.menuItem = menuItem;
			this.priority = priority;
		}
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (disposed)
			return;
		
		//removeWidgetFromRootPanel(this);
		//if (orderByDropDown != null)
			//removeWidgetFromRootPanel(orderByDropDown);
		
		if (queryProviderViewListeners != null)
			queryProviderViewListeners.clear();
		
		if (queryFormDialog != null)
			queryFormDialog.disposeBean();
		
		disposed = true;
		
		if (otherQueryProviderView instanceof DisposableBean)
			((DisposableBean) otherQueryProviderView).disposeBean();
	}
}
