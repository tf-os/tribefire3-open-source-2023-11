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
package com.braintribe.gwt.gmview.action.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.filter.pattern.PatternMatcher;
import com.braintribe.filter.pattern.Range;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.CanceledException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.TwoStageLoader;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler.InputTriggerListener;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.gwt.quickaccess.api.QuickAccessHit;
import com.braintribe.gwt.quickaccess.api.QuickAccessHitReason;
import com.braintribe.gwt.quickaccess.continuation.QuickAccessContinuation;
import com.braintribe.gwt.quickaccess.continuation.codec.GmEntityAndEnumTypeCodec;
import com.braintribe.gwt.quickaccess.continuation.codec.GmEntityAndEnumTypeDisplayCodec;
import com.braintribe.gwt.quickaccess.continuation.codec.GmEnumConstantDisplayCodec;
import com.braintribe.gwt.quickaccess.continuation.filter.GmEnumConstantFilter;
import com.braintribe.gwt.quickaccess.continuation.filter.GmTypeFilter;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.logic.TypeConditionJunction;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.PrototypeQueryAction;
import com.braintribe.model.workbench.QueryAction;
import com.braintribe.model.workbench.SimpleInstantiationAction;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateInstantiationAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

public class SpotlightPanel extends ContentPanel implements SpotlightPanelTextFieldListener, InitializableBean {
	
	interface SpotlightDataProperties extends PropertyAccess<SpotlightData> {
		ModelKeyProvider<SpotlightData> id();
		ValueProvider<SpotlightData, Group> group();
	}
	
	public enum Group {
		values(LocalizedText.INSTANCE.values()),
		types(LocalizedText.INSTANCE.types()),
		actions(LocalizedText.INSTANCE.actions()),
		serviceRequests(LocalizedText.INSTANCE.serviceRequests());
		
		private String string;
		
		private Group(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	/**
	 * Prepares a comparator where the group set in the parameter will appear first when grouping.
	 */
	public static Comparator<Group> getGroupComparator(Group priorityGroup) {
		return new Comparator<Group>() {
			@Override
			public int compare(Group o1, Group o2) {
				if (o1.equals(o2))
					return 0;
				if (priorityGroup.equals(o1))
					return -1;
				if (priorityGroup.equals(o2))
					return 1;
				
				return o1.compareTo(o2);
			}
		};
	}
	
	public enum GmEnumTypeResult {
		type, constants, typeAndConstants
	}
	
	protected static final Logger logger = new Logger(SpotlightPanel.class);
	static int idCounter = 0;
	protected static final String MORE_CLASS = "moreButton";
	protected static final String LESS_CLASS = "lessButton";
	private static final int MAX_TYPES_TO_DISPLAY = 50;
	private static final int MAX_SERVICE_REQUESTS_TO_DISPLAY = 20;
	protected static final int TIMER_INTERVAL = 500;
	private static final int TEXT_COLUMN_INDEX = 1;
	private static final int TEXT_COLUMN_INITIAL_WIDTH = 300;
	
	static {
		GmViewActionResources.INSTANCE.css().ensureInjected();
	}
	
	private Object textField;
	private QuickAccessGrid grid;
	private List<SpotlightPanelListener> spotlightPanelListeners;
	protected List<QuickAccessValueSelectionListener> quickAccessValueSelectionListeners;
	private Timer keyPressedTimer;
	private CodecRegistry<String> codecRegistry;
	private TypeCondition typeCondition;
	private Supplier<? extends Function<ParserArgument, List<ParserResult>>> simpleTypesValuesProviderSupplier;
	private Supplier<? extends Function<ParserArgument, List<ParserResult>>> originalSimpleTypesValuesProviderSupplier;
	private Function<ParserArgument, Map<String, Action>> simpleActionsProvider;
	private Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider;
	private Supplier<Future<List<QueryAction>>> queryActionsProvider;
	private boolean queryActionsLoaded = false;
	private List<QueryAction> queryActions;
	private Map<String, Object> collectionTypes;
	private Set<GmEntityType> entityTypes;
	private PersistenceGmSession gmSession;
	private ModelMdResolver cmdResolver;
	private Set<GmEnumType> defaultEnumTypes;
	private Set<GmEnumType> currentEnumTypes;
	private Set<GmEnumConstant> defaultEnumConstants;
	private Set<GmEnumConstant> currentEnumConstants;
	private SpotlightData modelToSelect;
	private TwoStageLoader<EntitiesProviderResult> entitiesLoader;
	private AsyncCallback<EntitiesProviderResult> entitiesLoaderCallback;
	private boolean forceResetEntitiesLoader = false;
	private List<SpotlightData> existingEntitiesModels;
	private List<SpotlightData> existingTypeModels;
	private List<? extends PatternMatcher> patternMatchers;
	private boolean useApplyButton = true;
	private boolean displaySimpleQueryActions = false;
	private List<SpotlightData> queryActionModels;
	private Supplier<Future<List<InstantiationAction>>> instantiationActionsProvider;
	private List<InstantiationAction> instantiationActions;
	private List<SpotlightData> instantiationActionModels;
	private boolean instantiationActionsLoaded = false;
	private int minCharsForFilter = 1;
	protected Supplier<? extends IconProvider> iconProviderSupplier;
	private boolean loadExistingValues = true;
	private boolean loadTypes = true;
	private boolean lenient = true;
	private String useCase = KnownUseCase.quickAccessPanelUseCase.getDefaultValue();
	private HandlerRegistration keyDownHandlerRegistration;
	private KeyDownHandler keyDownHandler;
	private Codec<GmEnumConstant, String> enumConstantRenderer;
	private boolean waitingForQueryActions;
	private boolean waitingForInstantiationActions;
	private boolean displayValuesSection = true;
	private final BorderLayoutContainer borderLayoutContainer;
	private ImageResourceRenderer imageResourceRenderer;
	protected ColumnConfig<SpotlightData, Group> groupColumn;
	private Map<String, Supplier<? extends ExpertUI<?>>> expertUISupplierMap;
	private Map<String, ExpertUI<?>> expertUIMap;
	private boolean checkEntityInstantiationDisabledMetaData = false;
	private boolean handleKeyPress = true;
	private int maximumNumberOfExistingValuesToShow = 5;
	private SpotlightData existingValuesLoadData;
	private SpotlightData typesLoadData;
	private String lastInput;
	private boolean useTypeOnly;
	private QuickAccessContinuation<GmType> entityAndEnumQAContinuation;
	private GmTypeFilter<GmType> entityAndEnumTypeFilter;
	private QuickAccessContinuation<GmEnumConstant> enumConstantQAContinuation;
	private GmEnumConstantFilter enumConstantFilter;
	private GmEnumTypeResult enumTypeResult = GmEnumTypeResult.constants;
	private Set<GmEntityType> ambiguousEntityTypes;
	private final int minMovingDistance = 10;
	private int typesHitFound = 0;
	private int enumConstantHitFound = 0;
	private Set<GmType> entitiesAndEnums;
	private Set<GmEnumConstant> enumConstants;
	private boolean entityAndEnumsFinished = false;
	private boolean asyncAndActionsFinished = false;
	private GmEnumConstantDisplayCodec enumConstantDisplayCodec;
	private boolean showTemplates = true;
	private String keyPressText;
	protected boolean usingSessionGmMetaModel;
	protected GmMetaModel gmMetaModel;
	private ModelOracle modelOracle;
	protected TransientPersistenceGmSession transientGmSession;
	private int serviceRequestsHitFound = 0;
	private boolean serviceRequestsFinished = false;
	private QuickAccessContinuation<GmType> serviceRequestQAContinuation;
	private Set<GmType> serviceRequestTypes;
	private boolean ignoreMetadata = false;
	private boolean useQueryActions = true;
	private boolean enableInstantiation = true;
    private boolean showAbstractTypes = true;
    private int moreItemsPriority = 0;
    private boolean simplifiedAssignment;
    private boolean enableGroups = true;
    private Set<String> currentGroups = new FastSet();
    private boolean groupByPerformed;
    private int textFieldMaxWidth = -1;
    private boolean selectFirstEntry = false;
    private int loadLimit;
    private ColumnConfig<SpotlightData, SpotlightData> textColumn;
    private InputTriggerListener inputTriggerListener;
    private Timer widthResizeTimer;
    private boolean enableAutoWidth = false;
	
	public SpotlightPanel() {
		this.setBorders(false);
		this.setBodyBorder(false);
		this.setHeaderVisible(false);
		this.addStyleName("spotlightPanel");
		
		borderLayoutContainer = new BorderLayoutContainer() {
			@Override
			protected void applyLayout(Widget component, Rectangle box) {
				if (textFieldMaxWidth != -1 && component == textField && box.getWidth() > textFieldMaxWidth)
					box.setWidth(textFieldMaxWidth);
				
				super.applyLayout(component, box);
			}
		};
		borderLayoutContainer.setCenterWidget(getGrid());
		borderLayoutContainer.setBorders(false);
		borderLayoutContainer.addStyleName("spotlightPanelBorderLayout");
		this.add(borderLayoutContainer);
	}
	
	/**
	 * Configures the required provider for simple types object
	 */
	@Required
	public void setSimpleTypesValuesProvider(Supplier<? extends Function<ParserArgument, List<ParserResult>>> simpleTypesValuesProviderSupplier) {
		if (originalSimpleTypesValuesProviderSupplier == null)
			originalSimpleTypesValuesProviderSupplier = simpleTypesValuesProviderSupplier;
		
		this.simpleTypesValuesProviderSupplier = simpleTypesValuesProviderSupplier;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		if (usingSessionGmMetaModel && gmMetaModel != gmSession.getModelAccessory().getOracle().getGmMetaModel()) {
			resetTypes();
			gmMetaModel = gmSession.getModelAccessory().getOracle().getGmMetaModel();
			if (modelOracle != null)
				reconfigureModelOracle();
		}
	}
	
	/**
	 * Configures the required (at least one) matchers used for the filtering.
	 */
	@Required
	public void setPatternMatchers(List<? extends PatternMatcher> patternMatchers) {
		this.patternMatchers = patternMatchers;
	}
	
	/**
	 * Configures the required provider which will provide icons.
	 */
	@Required
	public void setIconProvider(Supplier<? extends IconProvider> iconProviderSupplier) {
		this.iconProviderSupplier = iconProviderSupplier;
	}
	
	/**
	 * Configures the required provider for future entities (coming from the server, for example).
	 */
	@Configurable
	public void setEntitiesFutureProvider(Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider) {
		this.entitiesFutureProvider = entitiesFutureProvider;
	}
	
	/**
	 * Configures the required {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures a provider for query actions.
	 */
	@Configurable
	public void setQueryActionsProvider(Supplier<Future<List<QueryAction>>> queryActionsProvider) {
		this.queryActionsProvider = queryActionsProvider;
	}
	
	/**
	 * Configures whether we should use Query Actions within the panel.
	 */
	@Configurable
	public void setUseApplyButton(boolean useApplyButton) {
		this.useApplyButton = useApplyButton;
	}
	
	/**
	 * Configures whether we should display {@link SimpleQueryAction}s.
	 * Defaults to false.
	 */
	@Configurable
	public void setDisplaySimpleQueryActions(boolean displaySimpleQueryActions) {
		this.displaySimpleQueryActions = displaySimpleQueryActions;
	}
	
	/**
	 * Configures a provider for instantiation actions.
	 */
	@Configurable
	public void setInstantiationActionsProvider(Supplier<Future<List<InstantiationAction>>> instantiationActionsProvider) {
		this.instantiationActionsProvider = instantiationActionsProvider;
	}
	
	/**
	 * Configures an external field to be used by the panel. It will NOT be added to the panel.
	 * This may be called after initialization as well.
	 * This may be either a Widget or an Element.
	 */
	@Configurable
	public void setTextField(Object textField) {
		if (this.textField == textField)
			return;
		
		if (this.textField != null) {
			if (this.textField instanceof Widget) {
				if (((Widget) this.textField).getParent() == borderLayoutContainer) {
					borderLayoutContainer.remove((Widget) this.textField);
					borderLayoutContainer.forceLayout();
				}
			}
			
			configureTextField((Widget) this.textField, true);
		}
		
		this.textField = textField;
		
		if (textField instanceof Widget)
			configureTextField((Widget) textField, false);
		else
			configureTextField((Element) textField, false);
	}
	
	/**
	 * Configures the minimum amount of chars to be typed prior to triggering the filter.
	 * Defaults to 1, but if the text field is cleared, then the filter is triggered as well.
	 */
	@Configurable
	public void setMinCharsForFilter(int minCharsForFilter) {
		this.minCharsForFilter = minCharsForFilter;
	}
	
	/**
	  * The place where this panel is being used on.
	  * Defaults to "quickAccess".
	  */
	 @Configurable
	 public void setUseCase(String useCase) {
		 if (useCase != null)
			 this.useCase = useCase;
	 }
	
	/**
	 * Configures whether existing values should be loaded.
	 * Defaults to true.
	 */
	@Configurable
	public void setLoadExistingValues(boolean loadExistingValues) {
		this.loadExistingValues = loadExistingValues;
	}
	
	/**
	 * Configures whether the Types section should be displayed.
	 * Defaults to true.
	 */
	@Configurable
	public void setLoadTypes(boolean loadTypes) {
		this.loadTypes = loadTypes;
	}
	
	/**
	 * Configures whether we should display the values section. Defaults to true.
	 */
	@Configurable
	public void setDisplayValuesSection(boolean displayValuesSection) {
		this.displayValuesSection = displayValuesSection;
	}
	
	/**
	 * Checks whether to check for {@link Instantiable} meta data when filtering entity types.
	 * Defaults to false.
	 * @see #setIgnoreMetadata(boolean)
	 */
	@Configurable
	public void setCheckEntityInstantiationDisabledMetaData(boolean checkEntityInstantiationDisabledMetaData) {
		this.checkEntityInstantiationDisabledMetaData = checkEntityInstantiationDisabledMetaData;
	}
	
	/**
	 * If true, we will ignore all metadata resolution within the {@link SpotlightPanel}.
	 * This will bypass any other metadata configuration.
	 * Defaults to false.
	 */
	@Configurable
	public void setIgnoreMetadata(boolean ignoreMetadata) {
		this.ignoreMetadata = ignoreMetadata;
	}
	
	/**
	 * Configures a map with experts responsible for returning new instances for a given type.
	 */
	@Configurable
	public void setExpertUIMap(Map<String, Supplier<? extends ExpertUI<?>>> expertUISupplierMap) {
		this.expertUISupplierMap = expertUISupplierMap;
	}
	
	@Configurable
	public void setSimpleActionsProvider(Function<ParserArgument, Map<String, Action>> simpleActionsProvider) {
		this.simpleActionsProvider = simpleActionsProvider;
	}
	
	@Configurable
	public void setEnumTypeResult(GmEnumTypeResult enumTypeResult) {
		this.enumTypeResult = enumTypeResult;
	}
	
	public GmEnumTypeResult getEnumTypeResult() {
		return enumTypeResult;
	}
	
	/**
	 * Configures the maximum number of existing values to show.
	 * Defaults to 5. See {@link #setDisplayValuesSection(boolean)}.
	 */
	@Configurable
	public void setMaximumNumberOfExistingValuesToShow(int maximumNumberOfExistingValuesToShow) {
		this.maximumNumberOfExistingValuesToShow = maximumNumberOfExistingValuesToShow;
	}
	
	public void configureHandleKeyPress(boolean handleKeyPress) {
		this.handleKeyPress = handleKeyPress;
	}

	/**
	 * Configures if we should show Templates in the list. Default is to show (true).
	 */
	@Configurable
	public void setShowTemplates(boolean showTemplates) {
		this.showTemplates = showTemplates;
	}
	
	/**
	 * Configures the session used for displaying data in the Service Requests section.
	 */
	@Configurable
	public void setTransientGmSession(TransientPersistenceGmSession transientGmSession) {
		this.transientGmSession = transientGmSession;
	}
	
	public void setForceResetEntitiesLoader(boolean forceResetEntitiesLoader) {
		this.forceResetEntitiesLoader = forceResetEntitiesLoader;
	}
	
	/**
	 * Configures if we should show Abstract Types in the list. Default is to show (true).
	 */
	@Configurable
	public void setShowAbstractTypes(boolean showAbstractTypes) {
		this.showAbstractTypes = showAbstractTypes;
	}
	
	/**
	 * Configures whether groups are enabled. Defaults to true.
	 */
	@Configurable
	public void setEnableGroups(boolean enableGroups) {
		this.enableGroups = enableGroups;
	}
	
	/**
	 * Configures whether the grid width must be automatically resized based on the contents. Defaults to false.
	 */
	@Configurable
	public void setEnableAutoWidth(boolean enableAutoWidth) {
		this.enableAutoWidth = enableAutoWidth;
	}
	
	@Override
	public void intializeBean() throws Exception {
		if (textField == null) {
			textField = new TextField();
			((TextField) textField).setEmptyText(LocalizedText.INSTANCE.selectTypeOrValue());
			configureTextField((Widget) textField, false);
			borderLayoutContainer.setNorthWidget((TextField) textField, new BorderLayoutData(30));
		}
	}
	
	public void addSpotlightPanelListener(SpotlightPanelListener listener) {
		if (spotlightPanelListeners == null)
			spotlightPanelListeners = new ArrayList<>();
		
		spotlightPanelListeners.add(listener);
	}
	
	public void removeSpotlightPanelListener(SpotlightPanelListener listener) {
		if (spotlightPanelListeners != null) {
			spotlightPanelListeners.remove(listener);
			if (spotlightPanelListeners.isEmpty())
				spotlightPanelListeners = null;
		}
	}
	
	public void addQuickAccessValueSelectionListener(QuickAccessValueSelectionListener listener) {
		if (quickAccessValueSelectionListeners == null)
			quickAccessValueSelectionListeners = new ArrayList<>();
		
		quickAccessValueSelectionListeners.add(listener);
	}
	
	public void removeQuickAccessValueSelectionListeners(QuickAccessValueSelectionListener listener) {
		if (quickAccessValueSelectionListeners != null) {
			quickAccessValueSelectionListeners.remove(listener);
			if (quickAccessValueSelectionListeners.isEmpty())
				quickAccessValueSelectionListeners = null;
		}
	}
	
	/**
	 * Configures the max width of the textField.
	 */
	public void configureTextFieldMaxWidth(int textFieldMaxWidth) {
		this.textFieldMaxWidth = textFieldMaxWidth;
	}
	
	public void configureUseTypeOnly(boolean useTypeOnly) {
		this.useTypeOnly = useTypeOnly;
	}
	
	/**
	 * Configures whether query actions should be shown (in case {@link #setQueryActionsProvider(Supplier)} was set).
	 */
	public void configureUseQueryActions(boolean useQueryActions) {
		this.useQueryActions = useQueryActions;
	}

	/**
	 * Configures whether instantiation actions (@see {@link #setInstantiationActionsProvider(Supplier)} and types should be shown.
	 */
	public void configureEnableInstantiation(boolean enableInstantiation) {
		this.enableInstantiation = enableInstantiation;
	}
	
	/**
	 * Configures parameter for the {@link EntitiesFutureProvider}.
	 */
	public void configureSimplifiedAssignment(boolean simplifiedAssignment) {
		this.simplifiedAssignment = simplifiedAssignment;
	}
	
	public Future<Void> configureTypeCondition(TypeCondition typeCondition) {
		return configureTypeCondition(typeCondition, true);
	}
	
	public Future<Void> configureTypeCondition(TypeCondition typeCondition, boolean clearTextField) {
		return configureTypeCondition(typeCondition, clearTextField, "");
	}
	
	public Future<Void> configureTypeCondition(TypeCondition typeCondition, boolean clearTextField, String initialText) {
		Future<Void> future = new Future<>();
		this.typeCondition = typeCondition;
		if (clearTextField && textField instanceof TextField)
			((TextField) textField).clear();
		
		String input = textField instanceof TextField ? ((TextField) textField).getCurrentValue() : initialText;
		if (input != null) {
			input = input.trim();
			if (input.isEmpty())
				input = null;
		}
		lastInput = input;
		
		loadLimit = maximumNumberOfExistingValuesToShow;
		if (!ignoreMetadata) {
			IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(typeCondition);
			if (entityTypeCondition != null) {
				Integer autoPagingSize = GMEMetadataUtil.getAutoPagingSize(null, entityTypeCondition.getTypeSignature(), gmSession, useCase);
				if (autoPagingSize != null)
					loadLimit = autoPagingSize;
			}
		}
			
		
		loadData(typeCondition, future, input);
		
		return future;
	}
	
	/**
	 * Empty text to be used by the field, if it is a {@link TextField}.
	 */
	public void configureEmptyText(String emptyText) {
		if (textField instanceof TextField)
			((TextField) textField).setEmptyText(emptyText);
	}

	private void loadData(TypeCondition typeCondition, Future<Void> future, String input) {
		ListStore<SpotlightData> store = grid.getStore();
		store.clear();
		selectFirstEntry = true;
		modelToSelect = null;
		entityAndEnumsFinished = false;
		serviceRequestsFinished = false;
		asyncAndActionsFinished = false;
		
		ParserArgument parserArgument = new ParserArgument(input, typeCondition);
		
		List<SpotlightData> models = new ArrayList<>();
		if (displayValuesSection) {
			List<ParserResult> simpleTypeResults = simpleTypesValuesProviderSupplier != null
					? simpleTypesValuesProviderSupplier.get().apply(parserArgument)
					: Collections.emptyList();
			
			if (!simpleTypeResults.isEmpty()) {
				for (ParserResult pr : simpleTypeResults)
					models.add(prepareSimpleData(pr, input));
				
				if (enableGroups) {
					currentGroups.add(Group.values.name());
					checkGroupBy();
				}
			}
		}
		
		Set<Entry<String, Object>> collectionTypes = getFilteredCollectionTypes(input).entrySet();
		if (!collectionTypes.isEmpty()) {
			for (Map.Entry<String, Object> entry : collectionTypes)
				models.add(prepareCollectionData(entry.getKey(), entry.getValue(), input));
			
			if (enableGroups) {
				currentGroups.add(Group.types.name());
				checkGroupBy();
			}
		}
		
		Map<String, Action> actionsMap = simpleActionsProvider != null ? simpleActionsProvider.apply(parserArgument) : Collections.emptyMap();
		if (actionsMap != null && !actionsMap.isEmpty()) {
			for (Map.Entry<String, Action> entry : actionsMap.entrySet())
				models.add(prepareActionModel(entry.getKey(), entry.getValue(), input));
			
			if (enableGroups) {
				currentGroups.add(Group.actions.name());
				checkGroupBy();
			}
		}
		
		if (!models.isEmpty())
			store.addAll(models);
		
		typesHitFound = 0;
		
		if (entitiesAndEnums == null)
			entitiesAndEnums = new HashSet<>();
		else
			entitiesAndEnums.clear();
		
		if (enableInstantiation) {
			entitiesAndEnums.addAll(getEntityTypesFromModel());
			if (GmEnumTypeResult.type.equals(enumTypeResult) || GmEnumTypeResult.typeAndConstants.equals(enumTypeResult))
				entitiesAndEnums.addAll(getEnumTypesFromModel());
		}
		
		entityAndEnumQAContinuation = getOrPrepareQAContinuation(entityAndEnumQAContinuation, entitiesAndEnums, typeCondition, getCMDResolver(),
				false);
		entityAndEnumQAContinuation.start(input) //
				.andThen(result -> handleEntityAndEnumQAContinuation(input, future, store)) //
				.onError(e -> {
					if (e instanceof CanceledException)
						handleEntityAndEnumQAContinuation(input, future, store);
					else
						ErrorDialog.show("Error while providing entity and enum types options.", e);
				});
		
		if (transientGmSession == null || transientGmSession.getModelAccessory() == null)
			serviceRequestsFinished = true;
		else {
			if (serviceRequestTypes == null) {
				serviceRequestTypes = new HashSet<>();
				serviceRequestTypes.addAll(getServiceRequestsFromModel());
			}
			
			if (serviceRequestTypes.isEmpty())
				serviceRequestsFinished = true;
			else {
				serviceRequestQAContinuation = getOrPrepareQAContinuation(serviceRequestQAContinuation, serviceRequestTypes, typeCondition,
						transientGmSession.getModelAccessory().getMetaData(), true);
				serviceRequestQAContinuation.start(input) //
						.andThen(v -> handleServiceRequestQAContinuation(future, store)) //
						.onError(e -> {
							if (e instanceof CanceledException)
								handleServiceRequestQAContinuation(future, store);
							else
								ErrorDialog.show("Error while providing ServiceRequests options.", e);
						});
			}
		}
		
		loadAsyncDataAndActions(typeCondition, future, input);
	}
	
	private void handleServiceRequestQAContinuation(Future<Void> future, ListStore<SpotlightData> store) {
		serviceRequestsFinished = true;
		handleContinuationFinished(future, store, entityAndEnumsFinished);
	}
	
	private void handleEntityAndEnumQAContinuation(String input, Future<Void> future, ListStore<SpotlightData> store) {
		enumConstantHitFound = 0;
		getEnumConstantQAContinuation(typeCondition).start(input) //
				.andThen(v -> handleEnumConstantQAContinuation(future, store)) //
				.onError(e -> {
					if (e instanceof CanceledException)
						handleEnumConstantQAContinuation(future, store);
					else
						ErrorDialog.show("Error while providing enum constants options.", e);
				});
	}
	
	private void handleEnumConstantQAContinuation(Future<Void> future, ListStore<SpotlightData> store) {
		entityAndEnumsFinished = true;
		handleContinuationFinished(future, store, serviceRequestsFinished);
	}
	
	/**
	 * Configures the enumConstant renderer. If used, then the display information for the enum constant will only be the result of this codec.
	 */
	public void configureEnumConstantRenderer(Codec<GmEnumConstant, String> enumConstantRenderer) {
		this.enumConstantRenderer = enumConstantRenderer;
		
		if (enumConstantDisplayCodec != null)
			enumConstantDisplayCodec.configureEnumConstantRenderer(enumConstantRenderer);
	}
	
	/**
	 * Configures a custom Comparator for comparing the groups, instead of the default natural order of the {@link Group} enum.
	 * A null can be set here so the natural order is used once again.
	 */
	public void configureGroupComparator(Comparator<Group> comparator) {
		groupColumn.setComparator(comparator);
	}
	
	public ObjectAndType prepareObjectAndType() {
		SpotlightData selectedModel = grid.getSelectionModel().getSelectedItem();
		return prepareObjectAndType(selectedModel);				
	}

	public ObjectAndType prepareObjectAndType(SpotlightData itemSpotlightModel) {
		if (itemSpotlightModel == null)
			return null;
		
		Object type = itemSpotlightModel.getType();
		
		if (!(type instanceof GmType))
			type = null;
		
		if (Group.serviceRequests.equals(itemSpotlightModel.group))
			return new ObjectAndType((GmType) type, itemSpotlightModel.getDisplay(), true);
		else
			return new ObjectAndType(itemSpotlightModel.getValue(), (GmType) type, itemSpotlightModel.getDisplay());
	}	
	
	public GmTypeOrAction prepareModelTypeOrAction() {
		SpotlightData selectedModel = grid.getSelectionModel().getSelectedItem();
		if (selectedModel == null)
			return null;
		
		GmType type = selectedModel.getType() instanceof GmType ? (GmType) selectedModel.getType() : null;
		WorkbenchAction action = selectedModel.getValue() instanceof WorkbenchAction ? (WorkbenchAction) selectedModel.getValue() : null;
		
		if (type == null && action == null)
			return null;
		
		return new GmTypeOrAction(type, action);
	}
	
	/**
	 * Returns the text used for filtering.
	 */
	public String getFilterText() {
		return lastInput;
	}
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 */
	public TypeCondition prepareTypeCondition(String typeSignature) {
		GenericModelType type = GMF.getTypeReflection().findType(typeSignature);
		return prepareTypeCondition(type);
	}
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 */
	public TypeCondition prepareTypeCondition(GenericModelType modelType) {
		return GMEUtil.prepareTypeCondition(modelType);
	}
	
	/*
	private TypeCondition getEntityTypeCondition(String typeSignature) {
		IsAssignableTo entityTypeCondition = IsAssignableTo.T.create();
		entityTypeCondition.setTypeSignature(typeSignature);

		if (showTemplates || !typeSignature.equals(GenericEntity.T.getTypeSignature()))		
		   return entityTypeCondition;
		
		TypeConditionNegation typeConditionNegation = TypeConditionNegation.T.create();
		TypeConditionJunction typeCondition = TypeConditionConjunction.T.create();
		
		//Template Entity
		IsAssignableTo templateCondition = IsAssignableTo.T.create();
		templateCondition.setTypeSignature(TemplateType.T.getTypeSignature());
		//negotiate Template Entity
		typeConditionNegation.setOperand(templateCondition);
		
		//Conjunction between Generic Entity and Negotiated Templates Entities -> find all NON Template Types
		List<TypeCondition> typeConditions = new ArrayList<>();
		typeConditions.add(entityTypeCondition);
		typeConditions.add(typeConditionNegation);
		typeCondition.setOperands(typeConditions);
		
		return typeCondition;					
	}
	*/
	
	/**
	 * Helper method for preparing a {@link TypeCondition}.
	 */
	public TypeCondition prepareTypeCondition(GmType modelType) {
		return GMEUtil.prepareTypeCondition(modelType, true);
	}
	
	/**
	 * To be called when the ModelEnvironment is changed.
	 */
	public void onModelEnvironmentChanged() {
		queryActionsLoaded = false;
		if (queryActions != null)
			queryActions = null;
		if (queryActionModels != null)
			queryActionModels = null;
		
		instantiationActionsLoaded = false;
		if (instantiationActions != null)
			instantiationActions = null;
		if (instantiationActionModels != null)
			instantiationActionModels = null;
		
		resetTypes();
		if (entityAndEnumQAContinuation != null)
			entityAndEnumQAContinuation.cancel();
		entityAndEnumQAContinuation = null;
		if (enumConstantQAContinuation != null)
			enumConstantQAContinuation.cancel();
		enumConstantQAContinuation = null;
		
		if (serviceRequestQAContinuation != null)
			serviceRequestQAContinuation.cancel();
		serviceRequestQAContinuation = null;
	}
	
	public Object getTextField() {
		return textField;
	}
	
	public String getUseCase() {
		return useCase;
	}
	
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	public Function<ParserArgument, Future<EntitiesProviderResult>> getEntitiesFutureProvider() {
		return entitiesFutureProvider;
	}
	
	public boolean getUseApplyButton() {
		return useApplyButton;
	}
	
	public void configureTextField(Widget field, boolean remove) {
		if (field == null)
			return;
		
		if (remove) {
			if (keyDownHandlerRegistration != null) {
				keyDownHandlerRegistration.removeHandler();
				keyDownHandlerRegistration = null;
			}
			
			if (field instanceof InputFocusHandler && inputTriggerListener != null) {
				((InputFocusHandler) field).removeInputTriggerListener(inputTriggerListener);
				inputTriggerListener = null;
			}
		} else {
			keyDownHandlerRegistration = field.addDomHandler(getKeyDownHandler(), KeyDownEvent.getType());
			
			if (field instanceof InputFocusHandler)
				((InputFocusHandler) field).addInputTriggerListener(getInputTriggerListener());
		}
	}
	
	public void configureTextField(Element fieldElement, boolean remove) {
		if (fieldElement == null)
			return;
		
		if (remove && keyDownHandlerRegistration != null) {
			keyDownHandlerRegistration.removeHandler();
			keyDownHandlerRegistration = null;
		}
	}
	
	/**
	 * Configures the simpleTypesValueProvider with the original one.
	 */
	public void configureOriginalSimpleTypesProvider() {
		setSimpleTypesValuesProvider(originalSimpleTypesValuesProviderSupplier);
	}
	
	@Override
	public void onKeyDown(Event event, String text) {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER)
			handleEnter(event.getCtrlKey(), null);
		else if (keyCode == KeyCodes.KEY_UP || keyCode == KeyCodes.KEY_DOWN)
			handleUpDown(keyCode == KeyCodes.KEY_UP);
		else
			handleKeyPress(keyCode, text);
	}
	
	@Override
	public void onCancel() {
		fireOnCancel();
	}
	
	public KeyDownHandler getKeyDownHandler() {
		if (keyDownHandler != null)
			return keyDownHandler;
		
		keyDownHandler = event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
				handleEnter(event.isControlKeyDown(), null);
			else if (event.getNativeKeyCode() == KeyCodes.KEY_UP || event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
				event.preventDefault();
				handleUpDown(event.getNativeKeyCode() == KeyCodes.KEY_UP);
			} else
				handleKeyPress(event.getNativeKeyCode(), null);
		};
		
		return keyDownHandler;
	}
	
	/**
	 * Cancels any loading that may be currently in place
	 */
	public void cancelLoading() {
		if (entityAndEnumQAContinuation != null)
			entityAndEnumQAContinuation.cancel();
		if (enumConstantQAContinuation != null)
			enumConstantQAContinuation.cancel();
		if (serviceRequestQAContinuation != null)
			serviceRequestQAContinuation.cancel();
	}
	
	private void checkGroupBy() {
		if (!groupByPerformed && currentGroups.size() > 1) {
			grid.groupingView.groupBy(groupColumn);
			groupByPerformed = true;
		}
	}
	
	private void handleEnter(boolean isControlKey, ValueBaseField<?> field) {
		SpotlightData selectedItem = grid.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
			Object type = selectedItem.getType();
			if (!isControlKey || !(type instanceof GmEntityType))
				fireValueOrTypeSelected();
			else
				fireTypeSelected();
			
			if (field != null)
				field.clear();
		}
	}
	
	private void handleUpDown(boolean up) {
		SpotlightData selectedItem = grid.getSelectionModel().getSelectedItem();
		ListStore<SpotlightData> store = grid.getStore();
		boolean selected = false;
		
		if (selectedItem != null) {
			int index = store.indexOf(selectedItem);
			if (up) {
				if (index != 0) {
					SpotlightData model = store.get(index - 1);
					if (model.isActionData())
						grid.getSelectionModel().select(index - 2, false);
					else
						grid.getSelectionModel().select(model, false);
					
					selected = true;
				}
			} else {
				if (index < store.size() - 1) {
					SpotlightData model = store.get(index + 1);
					if (model.isActionData()) {
						if (index < store.size() - 2) {
							grid.getSelectionModel().select(index + 2, false);
							selected = true;
						}
					} else {
						grid.getSelectionModel().select(model, false);
						selected = true;
					}
				}
			}
		} else if (store.size() > 0) {
			grid.getSelectionModel().select(!up ? 0 : store.size() - 1, false);
			selected = true;
		}
		
		if (selected)
			grid.getView().ensureVisible(grid.getSelectionModel().getSelectedItem());
	}
	
	private void handleKeyPress(int keyCode, String text) {
		if (!handleKeyPress || (!(textField instanceof HasText) && !(textField instanceof TextAreaElement)) || keyCode == KeyCodes.KEY_SHIFT)
			return;
		
		keyPressText = text;
		Scheduler.get().scheduleDeferred(() -> {
			if (keyCode == KeyCodes.KEY_UP || keyCode == KeyCodes.KEY_DOWN || keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_ESCAPE)
				return;
			
			Scheduler.get().scheduleDeferred(() -> {
				if (textField instanceof HasText)
					keyPressText = ((HasText) textField).getText();
				int size = keyPressText == null ? 0 : keyPressText.trim().length();
				if (size >= minCharsForFilter)
					getKeyPressedTimer().schedule(TIMER_INTERVAL);
				else {
					lastInput = "";
					fireNotEnoughCharsTyped();
				}
				
				handleCancelLoading(keyCode);
			});
		});
	}
	
	/**
	 * Cancels the loading when handling a valid key.
	 */
	private void handleCancelLoading(int keyCode) {
		if (keyCode == KeyCodes.KEY_WIN_KEY_FF_LINUX || keyCode == KeyCodes.KEY_NUM_CENTER
				|| (keyCode >= KeyCodes.KEY_SHIFT && keyCode <= KeyCodes.KEY_CAPS_LOCK)
				|| (keyCode >= KeyCodes.KEY_PAGEUP && keyCode <= KeyCodes.KEY_DOWN) || keyCode == KeyCodes.KEY_PRINT_SCREEN
				|| keyCode == KeyCodes.KEY_INSERT || (keyCode >= KeyCodes.KEY_WIN_KEY_LEFT_META && keyCode <= KeyCodes.KEY_CONTEXT_MENU)
				|| (keyCode >= KeyCodes.KEY_F1 && keyCode <= KeyCodes.KEY_F12) || keyCode == KeyCodes.KEY_NUMLOCK
				|| keyCode == KeyCodes.KEY_SCROLL_LOCK || (keyCode >= KeyCodes.KEY_FIRST_MEDIA_KEY && keyCode <= KeyCodes.KEY_LAST_MEDIA_KEY)
				|| keyCode == KeyCodes.KEY_WIN_KEY || keyCode == KeyCodes.KEY_WIN_IME)
			return;
		
		cancelLoading();
	}
	
	private Timer getKeyPressedTimer() {
		if (keyPressedTimer != null)
			return keyPressedTimer;
		
		keyPressedTimer = new Timer() {
			@Override
			public void run() {
				String input = keyPressText;
				input = input == null ? "" : input.trim();
				int size = input.length();
				if (size >= minCharsForFilter && !input.equals(lastInput)) {
					cancelLoading();
					lastInput = input;
					loadData(typeCondition, null, input);
				}
			}
		};
		
		return keyPressedTimer;
	}
	
	protected ImageResourceRenderer getImageResourceRenderer() {
		if (imageResourceRenderer == null)
			imageResourceRenderer = new ImageResourceRenderer();
		
		return imageResourceRenderer;
	}
	
	public Grid<SpotlightData> getGrid() {
		if (grid != null)
			return grid;
		
		grid = new QuickAccessGrid(this);
		
		grid.getStore().addStoreAddHandler(event -> {
			if (enableAutoWidth)
				getWidthResizeTimer().schedule(100);
		});
		
		grid.getStore().addStoreClearHandler(event -> {
			if (enableAutoWidth)
				getWidthResizeTimer().schedule(100);
		});
		
		grid.groupingView.setAutoExpandColumn(textColumn);
		return grid;
	}
	
	private Timer getWidthResizeTimer() {
		if (widthResizeTimer != null)
			return widthResizeTimer;
		
		widthResizeTimer = new Timer() {
			@Override
			public void run() {
				resizeTextColumnToFit();
			}
		};
		
		return widthResizeTimer;
	}
	
	private void resizeTextColumnToFit() {
		List<SpotlightData> data = grid.getStore().getAll();
		if (data.isEmpty()) {
			grid.getColumnModel().setUserResized(true);
			grid.getColumnModel().setColumnWidth(TEXT_COLUMN_INDEX, TEXT_COLUMN_INITIAL_WIDTH);
			return;
		}

		TextMetrics textMetrics = TextMetrics.get();
		textMetrics.bind(grid.getView().getHeader().getAppearance().styles().head());
		int maxWidth = textMetrics.getWidth(textColumn.getHeader().asString()) + 20;
		
		textMetrics.bind(grid.getView().getCell(0, TEXT_COLUMN_INDEX));
		for (SpotlightData entry : data) {
			String text = entry.getDisplay();
			int width = textMetrics.getWidth(text) - 200;
			maxWidth = Math.max(maxWidth, width);
		}
		
		if (textColumn.getWidth() < maxWidth) {
			grid.getColumnModel().setUserResized(true);
			grid.getColumnModel().setColumnWidth(TEXT_COLUMN_INDEX, maxWidth);
		}
	}

	protected void fireValueOrTypeSelected() {
		cancelLoading();
		if (spotlightPanelListeners != null) {
			ObjectAndType objectAndType = prepareObjectAndType();
			for (SpotlightPanelListener listener : spotlightPanelListeners)
				listener.onValueOrTypeSelected(objectAndType);
		}
	}
	
	protected void fireTypeSelected() {
		cancelLoading();
		if (spotlightPanelListeners != null) {
			GmTypeOrAction type = prepareModelTypeOrAction();
			for (SpotlightPanelListener listener : spotlightPanelListeners)
				listener.onTypeSelected(type.getType());
		}
	}
	
	private void fireNotEnoughCharsTyped() {
		grid.getStore().clear();
		selectFirstEntry = true;
		if (spotlightPanelListeners != null) {
			for (SpotlightPanelListener listener : spotlightPanelListeners)
				listener.onNotEnoughCharsTyped();
		}
	}
	
	private void fireOnCancel() {
		cancelLoading();
		if (spotlightPanelListeners != null) {
			for (SpotlightPanelListener listener : spotlightPanelListeners)
				listener.onCancel();
		}
	}
	
	private Map<String, Object> getFilteredCollectionTypes(String input) {
		Map<String, Object> collectionTypes = getCollectionTypes();
		if ( input == null || input.isEmpty())
			return collectionTypes;

		Map<String, Object> filteredCollectionTypes = new FastMap<>();
		for (Map.Entry<String, Object> entry : collectionTypes.entrySet()) {
			String key = getFilteredKey(entry.getKey(), input);
			if (key != null)
				filteredCollectionTypes.put(key, entry.getValue());
		}
		
		return filteredCollectionTypes;
	}
	
	private Map<String, Object> getCollectionTypes() {
		Map<String, Object> availableCollectionTypes = new FastMap<>();
		
		if (collectionTypes == null) {
			collectionTypes = new FastMap<>();
			
			collectionTypes.put("List", new ArrayList<>());
			collectionTypes.put("Set", new HashSet<>());
			collectionTypes.put("Map", new HashMap<>());
		}
		
		if (typeCondition == null)
			availableCollectionTypes.putAll(collectionTypes);
		else {
			for (Map.Entry<String, Object> entry : collectionTypes.entrySet()) {
				if (typeCondition.matches(GMF.getTypeReflection().<GenericModelType>getType(entry.getValue())))
					availableCollectionTypes.put(entry.getKey(), entry.getValue());
			}
		}
		
		return availableCollectionTypes;
	}
	
	private Set<GmEntityType> getAmbiguousEntityTypes(Collection<GmEntityType> entityTypes) {
		if (!loadTypes || ambiguousEntityTypes != null)
			return ambiguousEntityTypes;
		
		Map<String, GmEntityType> entityTypesMap = new FastMap<>();
		ambiguousEntityTypes = new LinkedHashSet<>();
		for (GmEntityType entityType : entityTypes) {
			String shortName = GMEUtil.getShortName(entityType);
			
			GmEntityType ambiguousType = entityTypesMap.get(shortName);
			if (ambiguousType != null) {
				ambiguousEntityTypes.add(ambiguousType);
				ambiguousEntityTypes.add(entityType);
			}
			entityTypesMap.put(shortName, entityType);
		}
		
		return ambiguousEntityTypes;
	}
	
	private Set<GmEntityType> getEntityTypesFromModel() {
		if (loadTypes && entityTypes == null) {
			entityTypes = getModelOracle().getTypes().onlyEntities().<GmEntityType>asGmTypes().collect(Collectors.toSet());
		}
		
		Set<GmEntityType> availableEntityTypes = new TreeSet<>((o1, o2) -> {
			if (o1 == null || o2 == null)
				return -1;
			try {
				return o1.getTypeSignature().compareTo(o2.getTypeSignature());
			}catch(Exception ex) {
				return -1;
			}
		});

		if (entityTypes != null)
			availableEntityTypes.addAll(entityTypes);
				
		getAmbiguousEntityTypes(entityTypes);
		
		return availableEntityTypes;
	}
	
	private Set<GmEntityType> getServiceRequestsFromModel() {
		return transientGmSession.getModelAccessory().getOracle().getEntityTypeOracle(ServiceRequest.T).getSubTypes().transitive().asGmTypes();
	}
	
	public void configureCurrentEnumTypes(Set<GmEnumType> enumTypes) {
		this.currentEnumTypes = enumTypes;
	}
	
	public void configureDefaultEnumTypes() {
		if (defaultEnumTypes == null)
			defaultEnumTypes = getModelOracle().getTypes().onlyEnums().<GmEnumType>asGmTypes().collect(Collectors.toSet());
		
		this.currentEnumTypes = defaultEnumTypes;
	}
	
	/**
	 * Configures the {@link GmMetaModel} to be used for metaData and entity resolving. If none is set, then the one from the session is used.
	 */
	public void configureGmMetaModel(GmMetaModel gmMetaModel) {
		if (this.gmMetaModel != gmMetaModel) {
			onModelEnvironmentChanged();
		}
		
		this.gmMetaModel = gmMetaModel;
		usingSessionGmMetaModel = false;
		
		if (gmMetaModel != null && modelOracle != null)
			reconfigureModelOracle();
	}
	
	private void resetTypes() {
		entityTypes = null;
		defaultEnumTypes = null;
		ambiguousEntityTypes = null;
		serviceRequestTypes = null;
	}
	
	private Set<GmEnumType> getEnumTypesFromModel() {
		if (defaultEnumTypes == null)
			defaultEnumTypes = getModelOracle().getTypes().onlyEnums().<GmEnumType>asGmTypes().collect(Collectors.toSet());
					
		if (currentEnumTypes == null)
			currentEnumTypes = defaultEnumTypes;
		
		Set<GmEnumType> availableEnumTypes = new TreeSet<>((o1, o2) -> {
			if (o1 == null || o2 == null)
				return -1;
			
			return o1.getTypeSignature().compareTo(o2.getTypeSignature());
		});

		if (currentEnumTypes != null)
			availableEnumTypes.addAll(currentEnumTypes);

		return availableEnumTypes;
	}
	
	private Set<GmEnumConstant> getEnumConstantsFromModel() {
		if (defaultEnumConstants == null) {
			defaultEnumConstants = new HashSet<>();
			for (GmEnumType enumType : getEnumTypesFromModel())
				defaultEnumConstants.addAll(enumType.getConstants());
		}
		
		if (currentEnumConstants == null)
			currentEnumConstants = defaultEnumConstants;
		
		return currentEnumConstants != null ? new LinkedHashSet<>(currentEnumConstants) : Collections.emptySet();
	}
	
	private String getEnumTypeDisplay(GmEnumType enumType) {
		String enumTypeDisplay = enumType.getTypeSignature().substring(enumType.getTypeSignature().lastIndexOf(".") + 1);
		if (!ignoreMetadata) {
			Name enumTypeName = getCMDResolver().enumTypeSignature(enumType.getTypeSignature()).useCase(useCase).meta(Name.T).exclusive();
			if (enumTypeName != null && enumTypeName.getName() != null)
				enumTypeDisplay = I18nTools.getLocalized(enumTypeName.getName());
		}
		
		return enumTypeDisplay;
	}
	
	public ModelMdResolver getCMDResolver() {
		if (cmdResolver != null)
			return cmdResolver;
		
		if (usingSessionGmMetaModel || gmMetaModel == null) {
			usingSessionGmMetaModel = true;
			cmdResolver = gmSession.getModelAccessory().getMetaData();
		} else
			reconfigureCascadingMetaDataResolver();
		
		return cmdResolver;
	}
	
	private ModelOracle getModelOracle() {
		if (modelOracle != null)
			return modelOracle;
		
		if (usingSessionGmMetaModel || gmMetaModel == null) {
			usingSessionGmMetaModel = true;
			modelOracle = gmSession.getModelAccessory().getOracle();
		} else
			reconfigureModelOracle();
		
		return modelOracle;
	}
	
	private void reconfigureModelOracle() {
		modelOracle = new BasicModelOracle(gmMetaModel);
		if (cmdResolver != null)
			reconfigureCascadingMetaDataResolver();
	}
	
	private void reconfigureCascadingMetaDataResolver() {
		cmdResolver = new CmdResolverImpl(getModelOracle()) //
				.getMetaData() //
				.useCase(KnownUseCase.gmeGlobalUseCase.getDefaultValue());
	}
	
	/**
	 * Focus the configured field.
	 */
	public void focusField() {
		Object field = getTextField();
		if (field instanceof ValueBaseField)
			((ValueBaseField<?>) field).focus();
		else if (field instanceof FocusWidget)
			((FocusWidget) field).setFocus(true);
		else if (field instanceof Element)
			((Element) field).focus();
		else if (field instanceof InputFocusHandler)
			((InputFocusHandler) field).focusInput();
		else if (field instanceof Widget) {
			if (((Widget) field).getElement().getInnerText().isEmpty())
				((Widget) field).getElement().focus();
			else
				focusElement(((Widget) field).getElement());
		}
	}
	
	private static native void focusElement(Element editor) /*-{
		var range = $wnd.document.createRange();
		var lastElement = editor.childNodes[editor.childNodes.length - 1];
		range.setStart(lastElement, lastElement.textContent.length);
		range.setEnd(lastElement, lastElement.textContent.length);
		var sel = $wnd.getSelection();
		editor.focus();
		sel.removeAllRanges();
		sel.addRange(range);
	}-*/;
	
	private SpotlightData prepareActionModel(String key, Object value, String input) {
		SpotlightData model = new SpotlightData(key, key, Group.actions);
		model.priority = -1;
		model.value = value;
		model.simpleType = false;
		
		if (modelToSelect == null && key.equalsIgnoreCase(input))
			modelToSelect = model;
		
		return model;
	}
	
	private SpotlightData prepareSimpleData(ParserResult pr, String input) {
		SpotlightData model = new SpotlightData(pr.description, pr.description, Group.values);
		model.priority = -1;
		model.value = pr.value;
		model.simpleType = !(pr.value instanceof GenericEntity);
		model.setTypeSignature(pr.typeSignature);
		if (modelToSelect == null && pr.description.equalsIgnoreCase(input))
			modelToSelect = model;
		
		return model;
	}
	
	private SpotlightData prepareCollectionData(String key, Object value, String input) {
		SpotlightData model = new SpotlightData(key, key, Group.types);
		model.priority = -1;
		model.value = value;
		if (modelToSelect == null && key.equalsIgnoreCase(input))
			modelToSelect = model;
		
		return model;
	}
	
	private SpotlightData prepareEntityOrEnumData(QuickAccessEntry entry, String input, boolean forServiceRequests) {
		SpotlightData model = null;
		if (entry.type instanceof GmType) {
			if (forServiceRequests)
				model = new SpotlightData(entry.display, entry.sort.trim(), Group.serviceRequests);
			else
				model = new SpotlightData(entry.display, entry.sort.trim(), Group.types);
			
			model.type = entry.type;
			model.hint = prepareTypeHint(((GmType) entry.type));
			if (modelToSelect == null && entry.sort.equalsIgnoreCase(input))
				modelToSelect = model;
		} else if (entry.type instanceof Enum) {
			if (displayValuesSection) {
				model = new SpotlightData(entry.display, entry.sort.trim(), Group.values);
				model.value = entry.type;
				model.type = entry.type;
				if (modelToSelect == null && entry.sort.equalsIgnoreCase(input))
					modelToSelect = model;
			}
		} else if (entry.type instanceof GmEnumConstant) {
			if (displayValuesSection) {
				model = new SpotlightData(entry.display, entry.sort.trim(), Group.values);
				model.type = entry.type;
			}
		}

		if (model != null)
			assignPriority(model, entry.sort, input);
		
		return model;
	}
	
	private String prepareTypeHint(GmType type) {
		StringBuilder builder = new StringBuilder();
		GmMetaModel declaringModel = type.getDeclaringModel();
		if (declaringModel != null) {
			builder.append(declaringModel.getName()).append("#");
			builder.append(declaringModel.getVersion()).append(":");
		}
		builder.append(type.getTypeSignature());
		
		return builder.toString();
	}

	private void assignPriority(SpotlightData model, String dataString, String userInput) {
		if (userInput == null)
			model.priority = moreItemsPriority;
		else { //exact matches should appear first. Start with should appear second.
			model.priority = dataString.trim().equalsIgnoreCase(userInput) ? moreItemsPriority - 2 :
					(dataString.trim().toLowerCase().startsWith(userInput.toLowerCase()) ? moreItemsPriority - 1 : moreItemsPriority);
		}
	}
	
	private void loadAsyncData(String input) {
		if (entitiesFutureProvider == null)
			return;
		
		if (entitiesLoader == null || forceResetEntitiesLoader)
			entitiesLoader = new TwoStageLoader<EntitiesProviderResult>();
		
		ParserArgument config = new ParserArgument(input, typeCondition, loadLimit, 0, gmSession);
		config.setSimplifiedAssignment(simplifiedAssignment);
		entitiesLoader.loadFrom(
				entitiesFutureProvider.apply(config),
				getEntitiesLoaderCallback());
	}
	
	private AsyncCallback<EntitiesProviderResult> getEntitiesLoaderCallback() {
		if (entitiesLoaderCallback != null)
			return entitiesLoaderCallback;
		
		entitiesLoaderCallback = AsyncCallbacks.of( //
				result -> {
					displayExistingEntities(result);
					focusField();
				}, e -> {
					e.printStackTrace();
					ErrorDialog.show(LocalizedText.INSTANCE.errorLoadingEntities(), e);
				});
		
		return entitiesLoaderCallback;
	}
	
	private void loadQueryActions(final String input, final Future<Void> future) {
		final ProfilingHandle ph = Profiling.start(SpotlightPanel.class, "Loading query actions", false);
		queryActionsProvider.get() //
				.andThen(result -> {
					queryActionsLoaded = true;

					if (displaySimpleQueryActions)
						queryActions = result;
					else if (result != null) {
						queryActions = new ArrayList<>();
						for (QueryAction queryAction : result) {
							if (!(queryAction instanceof SimpleQueryAction))
								queryActions.add(queryAction);
						}
					}
					ph.stop();

					if (useQueryActions)
						displayWorkbenchActions(queryActions, true, input);
					focusField();

					waitingForQueryActions = false;
					if (future != null && !waitingForInstantiationActions)
						future.onSuccess(null);
				}).onError(e -> {
					ph.stop();
					e.printStackTrace();
					logger.error("Error while loading query actions.", e);
					waitingForQueryActions = false;
					if (future != null && !waitingForInstantiationActions)
						future.onFailure(e);
				});
	}
	
	private void loadInstantiationActions(final String input, final Future<Void> future) {
		final ProfilingHandle ph = Profiling.start(SpotlightPanel.class, "Loading instantiation actions", false);
		instantiationActionsProvider.get() //
				.andThen(result -> {
					// Using this to make sure this is async. After the first use, the instantiationActionsProvider
					// returns directly the result.
					Scheduler.get().scheduleDeferred(() -> {
						instantiationActionsLoaded = true;

						instantiationActions = result;
						ph.stop();

						if (enableInstantiation)
							displayWorkbenchActions(instantiationActions, false, input);
						focusField();

						waitingForInstantiationActions = false;
						if (future != null && !waitingForQueryActions)
							future.onSuccess(null);
					});
				}).onError(e -> {
					ph.stop();
					e.printStackTrace();
					logger.error("Error while loading instantiation actions.", e);
					waitingForInstantiationActions = false;
					if (future != null && !waitingForQueryActions)
						future.onFailure(e);
				});
	}
	
	private void displayExistingEntities(EntitiesProviderResult result) {
		ListStore<SpotlightData> store = grid.getStore();
		boolean loadingMore = result.getOffset() != 0;
		if (!loadingMore) {
			if (existingEntitiesModels != null) {
				for (SpotlightData model : existingEntitiesModels)
					store.remove(model);
				
				existingEntitiesModels.clear();
			}
			
			existingValuesLoadData = null;
			moreItemsPriority = 0;
		}
		
		if (result.getEntities() == null && result.getEntityAndDisplayList() == null)
			return;
		
		List<SpotlightData> models = new ArrayList<>();
		if (existingEntitiesModels == null)
			existingEntitiesModels = new ArrayList<>();
		
		if (result.getEntities() != null) {
			for (GenericEntity entity : result.getEntities())
				models.add(prepareEntityData(entity, null));
		} else {
			for (Pair<GenericEntity, String> entry : result.getEntityAndDisplayList())
				models.add(prepareEntityData(entry.first(), entry.second()));
		}
		
		if (result.isHasMore() || existingEntitiesModels.size() + models.size() > loadLimit) {
			if (existingValuesLoadData == null) {
				existingValuesLoadData = prepareLoadMoreOrLessData(result.isHasMore(),
						existingEntitiesModels.size() + models.size() > loadLimit, Group.values);
				existingValuesLoadData.priority = moreItemsPriority;
				models.add(existingValuesLoadData);
			} else {
				existingValuesLoadData.setDisplay(getLoadMoreOrLessDisplay(result.isHasMore(),
						existingEntitiesModels.size() + models.size() > loadLimit));
				existingValuesLoadData.priority = moreItemsPriority;
				store.update(existingValuesLoadData);
			}
		}
		
		if (!models.isEmpty()) {
			if (enableGroups) {
				currentGroups.add(Group.values.name());
				checkGroupBy();
			}
			
			store.addAll(models);
		}
		
		if (existingValuesLoadData != null)
			grid.getView().ensureVisible(typesLoadData);
		
		if (!models.isEmpty())
			existingEntitiesModels.addAll(models);
		
		if (grid.getSelectionModel().getSelectedItem() == null && grid.getStore().size() > 0)
			grid.getSelectionModel().select(0, false);
	}
	
	private void displayWorkbenchActions(List<? extends WorkbenchAction> workbenchActions, boolean queryActions, String input) {
		if (workbenchActions == null) {
			if (modelToSelect == null && grid.getStore().size() == 1)
				grid.getSelectionModel().select(0, false);
			return;
		}
		
		List<SpotlightData> models = queryActions ? queryActionModels : instantiationActionModels;
		if (models == null) {
			models = new ArrayList<>();
			for (WorkbenchAction action : workbenchActions)
				models.add(prepareWorkbenchActionData(action));
			
			if (queryActions)
				queryActionModels = models;
			else
				instantiationActionModels = models;
		}
		
		List<SpotlightData> filteredWorkbenchActionModels = new ArrayList<>();
		for (SpotlightData workbenchActionModel : models) {
			String filteredKey = getFilteredKey(workbenchActionModel.getSort(), input);
			if (filteredKey != null && actionFitsToType(workbenchActionModel)) {
				workbenchActionModel.setDisplay(filteredKey);
				filteredWorkbenchActionModels.add(workbenchActionModel);
			}
		}
		
		ListStore<SpotlightData> store = grid.getStore();
		if (!filteredWorkbenchActionModels.isEmpty()) {
			if (enableGroups) {
				currentGroups.add(Group.actions.name());
				checkGroupBy();
			}
			
			store.addAll(filteredWorkbenchActionModels);
		}
		
		if (modelToSelect == null && store.size() == 1)
			grid.getSelectionModel().select(0, false);
	}
	
	private boolean actionFitsToType(SpotlightData workbenchActionModel) {
		Object action = workbenchActionModel.getValue();
		
		IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(typeCondition);
		if (entityTypeCondition == null)
			return false;
		
		String typeSignature = null;
		if (action instanceof TemplateQueryAction) { //TODO: we probably need to rethink what is the best way to check the type conditions of actions
			TemplateQueryAction templateQueryAction = (TemplateQueryAction) action;
			if (templateQueryAction.getTemplate() != null && templateQueryAction.getTemplate().getPrototype() instanceof EntityQuery) 
				typeSignature = ((EntityQuery) templateQueryAction.getTemplate().getPrototype()).getEntityTypeSignature();
			else if (templateQueryAction.getTemplate() != null && templateQueryAction.getTemplate().getPrototype() instanceof SelectQuery)
				typeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) templateQueryAction.getTemplate().getPrototype());
		} else if (action instanceof SimpleQueryAction)
			typeSignature = ((SimpleQueryAction) action).getTypeSignature();
		else if (action instanceof PrototypeQueryAction) {
			PrototypeQueryAction prototypeQueryAction = (PrototypeQueryAction) action;
			if (prototypeQueryAction.getQuery() instanceof EntityQuery)
				typeSignature = ((EntityQuery) prototypeQueryAction.getQuery()).getEntityTypeSignature();
			else if (prototypeQueryAction.getQuery() instanceof SelectQuery)
				typeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) prototypeQueryAction.getQuery());
		} else if (action instanceof SimpleInstantiationAction)
			typeSignature = ((SimpleInstantiationAction) action).getTypeSignature();
		else if (action instanceof TemplateInstantiationAction) {
			if (((TemplateInstantiationAction) action).getTemplate() != null)
				typeSignature = ((TemplateInstantiationAction) action).getTemplate().getPrototypeTypeSignature();
		} else if (action instanceof InstantiationAction) {
			TraversingCriterion inplaceContextCriterion = ((InstantiationAction) action).getInplaceContextCriterion();
			if (inplaceContextCriterion instanceof TypeConditionCriterion) {
				GmType gmType = getModelOracle().<GmType> findGmType(entityTypeCondition.getTypeSignature());
				if (gmType != null)
					return ((TypeConditionCriterion) inplaceContextCriterion).getTypeCondition().matches(gmType);
			}
		}
		
		if (typeSignature != null) {
			GmType gmType = getModelOracle().<GmType>findGmType(typeSignature);
			if (gmType != null)
				return typeCondition.matches(gmType);
		}
		
		return false;
	}
	
	private SpotlightData prepareEntityData(GenericEntity entity, String display) {
		EntityType<?> entityType = entity.entityType();
		if (display == null)
			display = SelectiveInformationResolver.resolve(entityType, entity, ignoreMetadata ? null : getCMDResolver(), useCase, lenient);
		
		StringBuilder entityTypeRendering = new StringBuilder();
		entityTypeRendering.append(display);
		entityTypeRendering.append(" - ");
		entityTypeRendering.append("<span style='color: #808080'>");
		entityTypeRendering.append(entity instanceof GmEntityType ? ((GmEntityType) entity).getTypeSignature() : entityType.getShortName());
		entityTypeRendering.append("</span>");
		
		SpotlightData model = new SpotlightData(entityTypeRendering.toString(), display.trim(), Group.values);
		model.type = getModelOracle().findGmType(entityType.getTypeSignature());
		model.value = entity;
		if (entity.reference() instanceof PersistentEntityReference && entity.getId() != null)
			model.hint = entity.getId().toString();
		assignPriority(model, display, keyPressText);
		
		return model;
	}
	
	private SpotlightData prepareLoadMoreOrLessData(boolean prepareLoadMore, boolean prepareLoadLess, Group group) {
		SpotlightData model = new SpotlightData(getLoadMoreOrLessDisplay(prepareLoadMore, prepareLoadLess), String.valueOf(Character.MAX_VALUE), group);
		model.actionData = true;
		return model;
	}
	
	private String getLoadMoreOrLessDisplay(boolean prepareLoadMore, boolean prepareLoadLess) {
		StringBuilder render = new StringBuilder();
		if (prepareLoadMore)
			render.append("<span class='").append(MORE_CLASS).append("' style='color: #808080; cursor: pointer;'>").append(LocalizedText.INSTANCE.more()).append("</span>");
		if (prepareLoadLess) {
			render.append("<span class='").append(LESS_CLASS).append("' style='color: #808080; cursor: pointer;");
			if (prepareLoadMore)
				render.append(" margin-left: 40px;");
			render.append("'>").append(LocalizedText.INSTANCE.less()).append("</span>");
		}
		
		return render.toString();
	}
	
	private SpotlightData prepareWorkbenchActionData(WorkbenchAction action) {
		String displayName = action.getDisplayName() != null ? I18nTools.getLocalized(action.getDisplayName()) : "";
		SpotlightData model = new SpotlightData(displayName, displayName.trim(), Group.actions);
		model.value = action;
		return model;
	}
	
	private void displayExpertUIMapEntries(String input) {
		if (expertUISupplierMap == null)
			return;
		
		IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(typeCondition);
		if (entityTypeCondition != null) {
			String typeSignature = entityTypeCondition.getTypeSignature();
			Supplier<? extends ExpertUI<?>> expertUISupplier = expertUISupplierMap.get(typeSignature);
			if (expertUISupplier == null)
				return;
			
			if (expertUIMap == null)
				expertUIMap = new FastMap<>();
			
			ExpertUI<?> expertUI = expertUIMap.computeIfAbsent(typeSignature, v -> expertUISupplier.get());
			if (!expertUI.isValid())
				return;
			
			String i18nName = expertUI.getName() != null ? I18nTools.getLocalized(expertUI.getName()) : null;
			if (input != null && !input.trim().isEmpty()) {
				if (!(expertUI.getTechnicalName() != null && expertUI.getTechnicalName().contains(input)) && !(i18nName != null && i18nName.contains(input)))
					return;
			}
			
			String displayName = i18nName != null ? i18nName : expertUI.getTechnicalName();
			
			SpotlightData model = new SpotlightData(displayName, displayName.trim(), Group.actions);
			model.type = null;
			model.value = expertUI;
			
			if (enableGroups) {
				currentGroups.add(Group.actions.name());
				checkGroupBy();
			}
			
			grid.getStore().add(model);
			grid.getSelectionModel().select(model, false);
		}
	}
	
	private String getFilteredKey(String text, String input) {
		if (input == null)
			return text;
		
		for (PatternMatcher patternMatcher : patternMatchers) {
			List<Range> highlightRanges = patternMatcher.matches(input, text);
			if (highlightRanges == null || highlightRanges.isEmpty())
				continue;
			
			StringBuilder result = new StringBuilder();
			int start = highlightRanges.get(0).getStart();
			if (start > 0)
				result.append(text.substring(0, start));
			
			for (int i = 0; i < highlightRanges.size(); i++) {
				Range range = highlightRanges.get(i);
				
				int lastEnd = 0;
				if (range.getLenght() > 0) {
					lastEnd = range.getLenght() + range.getStart();
					result.append("<b>").append(text.substring(range.getStart(), lastEnd)).append("</b>");
				}
				
				if (i < highlightRanges.size() - 1)
					result.append(text.substring(lastEnd, highlightRanges.get(i + 1).getStart()));
			}
			
			Range lastRange = highlightRanges.get(highlightRanges.size() - 1);
			int end = lastRange.getLenght() + lastRange.getStart();
			if (end < text.length())
				result.append(text.substring(end));
			
			return result.toString();
		}
		
		return null;
	}
	
	public static IsAssignableTo getFirstEntityTypeCondition(TypeCondition typeCondition) {
		if (typeCondition instanceof IsAssignableTo)
			return (IsAssignableTo) typeCondition;
		
		if (typeCondition instanceof TypeConditionJunction) {
			TypeConditionJunction disjunction = (TypeConditionJunction) typeCondition;
			if (disjunction.getOperands() != null) {
				for (TypeCondition operandTypeCondition : disjunction.getOperands()) {
					IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(operandTypeCondition);
					if (entityTypeCondition != null)
						return entityTypeCondition;
				}
			}
		}
		
		return null;
	}
	
	private void loadMore(Group group) {
		switch (group) {
			case values:
				moreItemsPriority += 2;
				entitiesLoader.loadFrom(entitiesFutureProvider.apply(new ParserArgument(lastInput, typeCondition,
						loadLimit, existingEntitiesModels.size() - 1, gmSession)), getEntitiesLoaderCallback());
				break;
			case types:
			case actions:
			case serviceRequests:
				break;
		}
	}
	
	private void loadLess(Group group) {
		switch (group) {
			case values:
				int counter = 0;
				for (SpotlightData model : new ArrayList<SpotlightData>(existingEntitiesModels)) {
					if (counter == loadLimit && model != existingValuesLoadData) {
						existingEntitiesModels.remove(model);
						grid.getStore().remove(model);
					} else if (model != existingValuesLoadData)
						counter++;
				}
				existingValuesLoadData.setDisplay(getLoadMoreOrLessDisplay(true, false));
				existingValuesLoadData.priority = moreItemsPriority;
				grid.getStore().update(existingValuesLoadData);
				focusField();
				break;
			case types:
			case actions:
			case serviceRequests:
				break;
		}
	}
	
	private QuickAccessContinuation<GmType> getOrPrepareQAContinuation(QuickAccessContinuation<GmType> qaContinuation, Set<GmType> types,
			TypeCondition typeCondition, ModelMdResolver modelMdResolver, boolean forServiceRequests) {
		if (qaContinuation != null) {
			getEntityAndEnumTypeFilter().setTypeCondition(typeCondition);
			qaContinuation.resetPreFilter(types.iterator());
			return qaContinuation;
		}
		
		final GmEntityAndEnumTypeDisplayCodec displayCodec = new GmEntityAndEnumTypeDisplayCodec();
		displayCodec.configureMetaDataResolver(modelMdResolver);
		displayCodec.configureUseCase(useCase);
		
		qaContinuation = new QuickAccessContinuation<>(types.iterator(), getEntityAndEnumTypeFilter(),
				Arrays.asList(displayCodec, new GmEntityAndEnumTypeCodec()),
				hit -> handleQuickAccessContinuationHit(modelMdResolver, forServiceRequests, displayCodec, hit), patternMatchers);
		
		getEntityAndEnumTypeFilter().setTypeCondition(typeCondition);
		return qaContinuation;
	}

	private void handleQuickAccessContinuationHit(ModelMdResolver modelMdResolver, boolean forServiceRequests,
			final GmEntityAndEnumTypeDisplayCodec displayCodec, QuickAccessHit<GmType> hit) {
		GmType hitElement = hit.getElement();
		
		if (hitElement instanceof GmEntityType) {
			if (!isEntityVisibleAndInstantiable((GmEntityType) hitElement, modelMdResolver))							
				return;
		    if (((GmEntityType) hitElement).getIsAbstract() && !showAbstractTypes ) 
				return;
		}
		
		if (forServiceRequests)
			serviceRequestsHitFound++;
		else
			typesHitFound++;
		
		String display = null;
		String type = null;
		String sort = null;
		
		for (QuickAccessHitReason<GmType> reason : hit.getReasons()) {
			try {
				if (displayCodec == reason.getStringRepresentationProvider()) {
					sort = reason.getStringRepresentationProvider().apply(hitElement);
					display = getDisplayFromReason(sort, reason);
				} else
					type = getDisplayFromReason(reason.getStringRepresentationProvider().apply(hitElement), reason);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
			}
		}
		
		String userInput = forServiceRequests ? serviceRequestQAContinuation.getUserInput() : entityAndEnumQAContinuation.getUserInput();
		
		if (hitElement instanceof GmEntityType) {
			GmEntityType gmEntityType = (GmEntityType) hitElement;
			StringBuilder entityTypeRendering = new StringBuilder();
			if (Boolean.TRUE.equals(gmEntityType.getIsAbstract()))
				entityTypeRendering.append("<i>");
			entityTypeRendering.append(display != null ? display : type);
			if (Boolean.TRUE.equals(gmEntityType.getIsAbstract()))
				entityTypeRendering.append("</i>");
			
			String entityTypeSignature = gmEntityType.getTypeSignature();
			String typeString1 = GMEUtil.getShortName(gmEntityType);
			if ((ambiguousEntityTypes != null && ambiguousEntityTypes.contains(gmEntityType)) || display != null) {
				entityTypeRendering.append(" - ");
				entityTypeRendering.append("<span style='color: #808080'>");
				
				if (ambiguousEntityTypes == null || !ambiguousEntityTypes.contains(gmEntityType))
					entityTypeRendering.append(type != null ? type : typeString1);
				else {
					if (type != null)
						type = entityTypeSignature.substring(0, entityTypeSignature.indexOf(typeString1)) + type;
					else
						typeString1 = entityTypeSignature.substring(0, entityTypeSignature.indexOf(typeString1)) + typeString1;
					entityTypeRendering.append(type != null ? type : typeString1);
				}
				
				entityTypeRendering.append("</span>");
			}
			
			if (sort == null)
				sort = typeString1;
			
			QuickAccessEntry quickAccessEntry1 = new QuickAccessEntry(entityTypeRendering.toString(), sort, gmEntityType);
			SpotlightData model = prepareEntityOrEnumData(quickAccessEntry1, userInput, forServiceRequests);
			if (model != null) {
				if (enableGroups) {
					currentGroups.add(model.getGroup().name());
					checkGroupBy();
				}
				grid.getStore().add(model);
			}
		} else {
			StringBuilder enumTypeRendering = new StringBuilder();
			
			String typeString2 = hitElement.getTypeSignature();
			
			String displayString = null;
			try {
				displayString = displayCodec.apply(hitElement);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			enumTypeRendering.append(display != null ? display : displayString);
			enumTypeRendering.append(" - ");
			enumTypeRendering.append("<span style='color: #808080'>");
			enumTypeRendering.append(type != null ? type : typeString2);
			enumTypeRendering.append("</span>");
			
			QuickAccessEntry quickAccessEntry2 = new QuickAccessEntry(enumTypeRendering.toString(), displayString, hitElement);
			SpotlightData model = prepareEntityOrEnumData(quickAccessEntry2, userInput, forServiceRequests);
			if (model != null)
				grid.getStore().add(model);
		}
		
		focusField();
		
		if (forServiceRequests) {
			if (serviceRequestsHitFound == MAX_SERVICE_REQUESTS_TO_DISPLAY)
				serviceRequestQAContinuation.cancel();
		} else if (typesHitFound == MAX_TYPES_TO_DISPLAY)
			entityAndEnumQAContinuation.cancel();
	}
	
	private String getDisplayFromReason(String display, QuickAccessHitReason<?> reason) {
		List<Range> highlightRanges = reason.getRanges();
		
		StringBuilder result = new StringBuilder();
		int start = highlightRanges.get(0).getStart();
		if (start > 0)
			result.append(display.substring(0, start));
		
		for (int i = 0; i < highlightRanges.size(); i++) {
			Range range = highlightRanges.get(i);
			int lastEnd = 0;
			if (range.getLenght() > 0) {
				lastEnd = range.getLenght() + range.getStart();
				result.append("<b>").append(display.substring(range.getStart(), lastEnd)).append("</b>");
			}
			
			if (i < highlightRanges.size() - 1)
				result.append(display.substring(lastEnd, highlightRanges.get(i + 1).getStart()));
		}
		
		Range lastRange = highlightRanges.get(highlightRanges.size() - 1);
		int end = lastRange.getLenght() + lastRange.getStart();
		if (end < display.length())
			result.append(display.substring(end));
		
		return result.toString();
	}
	
	private GmTypeFilter<GmType> getEntityAndEnumTypeFilter() {
		if (entityAndEnumTypeFilter == null)
			entityAndEnumTypeFilter = new GmTypeFilter<>();
		
		return entityAndEnumTypeFilter;
	}
	
	private QuickAccessContinuation<GmEnumConstant> getEnumConstantQAContinuation(TypeCondition typeCondition) {
		if (enumConstantQAContinuation != null) {
			getEnumConstantFilter().setTypeCondition(typeCondition);
			enumConstantQAContinuation.resetPreFilter(enumConstants.iterator());
			return enumConstantQAContinuation;
		}
		
		enumConstants = new HashSet<>();
		if (GmEnumTypeResult.constants.equals(enumTypeResult) || GmEnumTypeResult.typeAndConstants.equals(enumTypeResult))
			enumConstants.addAll(getEnumConstantsFromModel());
		
		enumConstantQAContinuation = new QuickAccessContinuation<>(enumConstants.iterator(), getEnumConstantFilter(),
				Collections.singleton((Function<GmEnumConstant, String>) getEnumConstantDisplayCodec()), hit -> handleEnumContinuationHit(hit),
				patternMatchers);
		
		getEnumConstantFilter().setTypeCondition(typeCondition);
		return enumConstantQAContinuation;
	}

	private void handleEnumContinuationHit(QuickAccessHit<GmEnumConstant> hit) {
		for (QuickAccessHitReason<GmEnumConstant> reason : hit.getReasons()) {
			GmEnumConstant hitElement = hit.getElement();
			GmEnumType declaringType = hitElement.getDeclaringType();
			String displayString = getEnumTypeDisplay(declaringType);
			String userInput = enumConstantQAContinuation.getUserInput();
			String formatedDisplayString = getFilteredKey(displayString, userInput);
			
			try {
				String enumValueString = reason.getStringRepresentationProvider().apply(hitElement);
				//String enumValueString = getDisplayFromReason(reason.getStringRepresentationProvider().provide(hit.getElement()), reason);
				String formatedEnumValueString = getDisplayFromReason(enumValueString, reason);
				
				String typeString = declaringType.getTypeSignature();
				String formatedTypeString = getFilteredKey(typeString, userInput);
				
				StringBuilder result = new StringBuilder();
				if (enumConstantRenderer != null)
					result.append(formatedEnumValueString);
				else {
					result.append(formatedDisplayString != null ? formatedDisplayString : displayString).append(".");
					result.append(formatedEnumValueString);
					result.append(" - ");
					result.append("<span style='color: #808080'>");
					result.append(formatedTypeString != null ? formatedTypeString : typeString);
					result.append("</span>");
				}
				
				EnumType reflectedEnumType = GMF.getTypeReflection().findType(typeString);
				Object value = hitElement.getName();
				if (reflectedEnumType != null)
					value = reflectedEnumType.getEnumValue((String) value);
				
				QuickAccessEntry quickAccessEntry = new QuickAccessEntry(result.toString(), displayString + "." + enumValueString, value);
				SpotlightData entityOrEnumData = prepareEntityOrEnumData(quickAccessEntry, userInput, false);
				if (entityOrEnumData != null) {
					if (enableGroups) {
						currentGroups.add(entityOrEnumData.getGroup().name());
						checkGroupBy();
					}
					
					grid.getStore().add(entityOrEnumData);
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			
			enumConstantHitFound++;
			
			break;
		}
		
		focusField();
		
		if (enumConstantHitFound == MAX_TYPES_TO_DISPLAY)
			enumConstantQAContinuation.cancel();
	}
	
	private GmEnumConstantDisplayCodec getEnumConstantDisplayCodec() {
		if (enumConstantDisplayCodec == null) {
			enumConstantDisplayCodec = new GmEnumConstantDisplayCodec();
			enumConstantDisplayCodec.configureMetaDataResolver(getCMDResolver());
			enumConstantDisplayCodec.configureEnumConstantRenderer(enumConstantRenderer);
			enumConstantDisplayCodec.configureUseCase(useCase);
		}
		
		return enumConstantDisplayCodec;
	}
	
	private void loadAsyncDataAndActions(final TypeCondition typeCondition, final Future<Void> future, final String input) {
		IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(typeCondition);
		if (entityTypeCondition != null && loadExistingValues && displayValuesSection)
			loadAsyncData(input);
		
		waitingForQueryActions = false;
		if (useApplyButton && queryActionsProvider != null) {
			if (!queryActionsLoaded) {
				waitingForQueryActions = true;
				loadQueryActions(input, future);
			} else if (useQueryActions)
				displayWorkbenchActions(queryActions, true, input);
		}
		
		waitingForInstantiationActions = false;
		if (!instantiationActionsLoaded) {
			if (instantiationActionsProvider != null) {
				waitingForInstantiationActions = true;
				loadInstantiationActions(input, future);
			}
		} else if (enableInstantiation)
			displayWorkbenchActions(instantiationActions, false, input);
		
		if (!useTypeOnly)
			displayExpertUIMapEntries(input);
		
		if (!waitingForQueryActions && !waitingForInstantiationActions && future != null) {
			//Since we have to make sure the selection occurs before returning the future, we do the same as above
			Scheduler.get().scheduleDeferred(() -> new Timer() {
				@Override
				public void run() {
					asyncAndActionsFinished = true;
					if (entityAndEnumsFinished && serviceRequestsFinished)
						future.onSuccess(null);
					focusField();
				}
			}.schedule(TIMER_INTERVAL));
		}
	}
	
	public GmEnumConstantFilter getEnumConstantFilter() {
		if (enumConstantFilter == null)
			enumConstantFilter = new GmEnumConstantFilter();
		return enumConstantFilter;
	}
	
	private boolean isEntityVisibleAndInstantiable(GmEntityType entityType, ModelMdResolver modelMdResolver) {
		if (ignoreMetadata)
			return true;
		
		EntityMdResolver entityMdResolver = modelMdResolver.useCase(useCase).entityTypeSignature(entityType.getTypeSignature());
		boolean isVisible = entityMdResolver.is(Visible.T);
		
		boolean isInstantiable = true;
		if (checkEntityInstantiationDisabledMetaData)
			isInstantiable = entityMdResolver.is(Instantiable.T);
		
		return isVisible && isInstantiable;
	}
	
	private void handleContinuationFinished(Future<Void> future, ListStore<SpotlightData> store, boolean otherContinuationFinished) {
		if (modelToSelect == null && store.size() > 0) {
			if (grid.getSelectionModel().getSelectedItem() == null)
				grid.getSelectionModel().select(0, false);
			
			if (store.size() != 1)
				focusField();
		} else if (modelToSelect != null)
			grid.getSelectionModel().select(modelToSelect, false);
		else
			focusField();
		
		if (asyncAndActionsFinished && otherContinuationFinished && future != null)
			future.onSuccess(null);
	}
	
	protected ColumnModel<SpotlightData> prepareColumnModel() {
		IdentityValueProvider<SpotlightData> identityValueProvider = new IdentityValueProvider<>();
		
		ColumnConfig<SpotlightData, SpotlightData> iconColumn = new ColumnConfig<>(identityValueProvider, 30);
		iconColumn.setCellPadding(false);
		iconColumn.setCell(new AbstractCell<SpotlightData>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, SpotlightData model, SafeHtmlBuilder sb) {
				ImageResource icon = grid.getIcon(model);
				if (icon != null) {
					StringBuilder htmlConstant = new StringBuilder();
					htmlConstant.append("<img src='").append(icon.getSafeUri().asString()).append("' class='spotlightImage'>");
					sb.appendHtmlConstant(htmlConstant.toString());
				}
					//sb.append(getImageResourceRenderer().render(icon));
			}
		});
		
		textColumn = new ColumnConfig<>(identityValueProvider, TEXT_COLUMN_INITIAL_WIDTH);
		textColumn.setCellPadding(false);
		textColumn.setCell(new AbstractCell<SpotlightData>("click") {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, SpotlightData model, SafeHtmlBuilder sb) {
				String rendering = model.getDisplay();
				
				if (model.isSimpleType()) {
					Object value = model.getValue();
					if (value != null && codecRegistry != null) {
						Codec<Object, String> renderer = codecRegistry.getCodec(value.getClass());
						if (renderer == null)
							rendering = rendering + " - <b>" + value.toString() + "</b>";
						else {
							try {
								String valueRendered = renderer.encode(value);
								rendering = rendering + " - <b>" + valueRendered + "</b>";
							} catch (CodecException e) {
								SpotlightPanel.logger.error("Error while getting value renderer value.", e);
								e.printStackTrace();
							}
						}
					}
				}
				
				StringBuilder htmlConstant = new StringBuilder();
				htmlConstant.append("<div class='").append(GmViewActionResources.INSTANCE.css().quickAccessText()).append("'");
				if (model.hint != null)
					htmlConstant.append(" qtip='").append(model.hint).append("'");
				htmlConstant.append(">").append(rendering).append("</div>");
				
				sb.appendHtmlConstant(htmlConstant.toString());
			}
			
			@Override
			public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, SpotlightData model, NativeEvent event,
					ValueUpdater<SpotlightData> valueUpdater) {
				if (model == null || !model.isActionData()) {
					super.onBrowserEvent(context, parent, model, event, valueUpdater);
					return;
				}
				
				EventTarget eventTarget = event.getEventTarget();
				if (!Element.is(eventTarget))
					return;
				
				String cls = Element.as(eventTarget).getClassName();
				if (cls.contains(SpotlightPanel.MORE_CLASS))
					loadMore(model.getGroup());
				else if (cls.contains(SpotlightPanel.LESS_CLASS)) {
					//This must be deferred because we are deleting entries from the grid here before the clickEvent is handled.
					//Thus, we must assure the clickEvent is handled first, in order to avoid problems, then delete the entries from the grid
					Scheduler.get().scheduleDeferred(() -> loadLess(model.getGroup()));
				}
			}
		});
		
		groupColumn = new ColumnConfig<>(QuickAccessGrid.props.group());
		groupColumn.setCellPadding(false);
		groupColumn.setWidth(1);
		groupColumn.setHidden(true);
		
		return new ColumnModel<>(Arrays.asList(iconColumn, textColumn, groupColumn));
	}
	
	private InputTriggerListener getInputTriggerListener() {
		if (inputTriggerListener != null)
			return inputTriggerListener;
		
		inputTriggerListener = () -> fireValueOrTypeSelected();
		
		return inputTriggerListener;
	}
	
	public static class SpotlightData {
		private final int id;
		private Object value;
		private Object type;
		private Boolean simpleType;
		private String display;
		private final String sort;
		public int priority;
		private final Group group;
		private boolean actionData;
		private String typeSignature;
		protected String hint;
		
		public SpotlightData(String display, String sort, Group group) {
			id = idCounter++;
			this.display = display;
			this.sort = sort;
			this.group = group;
		}
		
		public int getId() {
			return id;
		}
		
		public Object getValue() {
			return value;
		}
		
		public Object getType() {
			return type;
		}
		
		public boolean isSimpleType() {
			return Boolean.TRUE.equals(simpleType);
		}
		
		public String getDisplay() {
			return display;
		}
		
		public Group getGroup() {
			return group;
		}
		
		public String getSort() {
			return sort;
		}
		
		public void setDisplay(String display) {
			this.display = display;
		}
		
		public boolean isActionData() {
			return actionData;
		}
		
		public void setActionData(boolean actionData) {
			this.actionData = actionData;
		}
		
		public String getTypeSignature() {
			return typeSignature;
		}
		
		public void setTypeSignature(String typeSignature) {
			this.typeSignature = typeSignature;
		}
	}
	
	private static class QuickAccessEntry {
		public String display;
		public String sort;
		public Object type;
		
		public QuickAccessEntry(String display, String sort, Object type) {
			this.display = display;
			this.sort = sort;
			this.type = type;
		}
	}

}
