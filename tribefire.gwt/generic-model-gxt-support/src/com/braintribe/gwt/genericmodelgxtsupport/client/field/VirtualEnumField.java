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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport.ClickableComboBoxTemplates;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectListConfig;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ClickableTriggerField;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.extensiondeployment.meta.DynamicSelectList;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Icon;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TriggerField;

/**
 * Field to be used with properties having the {@link VirtualEnum} metaData.
 * @author michel.docouto
 */
public class VirtualEnumField<D> extends SimpleComboBox<D> implements ClickableTriggerField, NoBlurWhileEditingField {
		
	private static final String DISPLAY_FIELD = "displayField";
	
	private VirtualEnum virtualEnum;
	private boolean preparePossibleValues;
	private List<VirtualEnumConstant> possibleValues;
	private PropertyEditor<D> innerPropertyEditor;
	private int lastKeyCode;
	private String baseChars = "0123456789-";
	private List<Character> allowed;
	private D initialValue;
	private Function<SelectListConfig, Future<List<Object>>> dynamicEntriesLoader;
	private List<VirtualEnumConstant> constants;
	private VirtualEnumFieldContext<D> context;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> valueSelectionFutureSupplier;
	private Function<SelectionConfig, Future<InstanceSelectionData>> valueSelectionFuture;
	private List<Object> dynamicPossibleValues;
	private boolean editingField = false;
	
	public VirtualEnumField(VirtualEnumFieldContext<D> context, LabelProvider<? super D> labelProvider) {
		/*
		super(new ClickableComboBoxCell<D>(new ListStore<D>(new ModelKeyProvider<D>() {
			@Override
			public String getKey(D item) {
				return item.toString();
			}
		}), labelProvider, new LabelProviderSafeHtmlRenderer<D>(labelProvider)));
		*/
		super(new ClickableComboBoxCell<D>(new ListStore<D>(item -> item.toString()), labelProvider, new AbstractSafeHtmlRenderer<D>() {
				@Override
				public SafeHtml render(D item) {
					ClickableComboBoxTemplates templates = GWT.create(ClickableComboBoxTemplates.class);
					SafeUri imageUri = getIconUrl(item, context); 							
					String value = labelProvider.getLabel(item);
					
					if (imageUri == null || imageUri.toString().isEmpty())
						return templates.comboValue(value);
					else	
						return templates.comboImageWithValue(imageUri, value);
				}				
		}));
		
		DynamicSelectList dynamicSelectList = context.getDynamicSelectList();
		if (dynamicSelectList != null && dynamicSelectList.getOutlined()) {
			((ClickableComboBoxCell<D>) getCell()).setTriggerClickListener(() -> {
				handleValueSelectionFuture();
				editingField = true;
				return true;
			});
		}
		
		//setForceSelection(dynamicSelectList != null); //RVE commented out - rewrite later depending on virtualEnum.getForceSelection()
		//setEditable(dynamicSelectList == null);
		
		setTriggerAction(TriggerAction.ALL);
		
		allowed = new ArrayList<>();
		for (int i = 0; i < baseChars.length(); i++)
			allowed.add(baseChars.charAt(i));
		
		virtualEnum = context.getVirtualEnum();
		this.context = context;
		innerPropertyEditor = (PropertyEditor<D>) context.getInnerPropertyEditor();
		
		context.gmSession.listeners().add(new CommitListener() {
			@Override
			public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
				//NOP
			}
			
			@Override
			public void onAfterCommit(PersistenceGmSession session, Manipulation manipulation, Manipulation inducedManipluation) {
				if (virtualEnum != null) {
					preparePossibleValues = true;
					preparePossibleValues();
				}
			}
		});
		
		Boolean useForceSelection = virtualEnum == null ? true : virtualEnum.getForceSelection();		
		setForceSelection(useForceSelection);
		setEditable(true);  //RVE - enable to be able sort the list - togather with ForceSelection is not possible to add new value
		
		if (virtualEnum == null)
			initialValue = context.getInitialValue();
		else {
			preparePossibleValues = true;
			preparePossibleValues();
			preparePossibleValues = true;
			
			constants = virtualEnum.getConstants();
			setValue(context.getInitialValue());
		}
		
		if (context.allowDecimal) {
			String decimalSeparator = LocaleUtil.getDecimalSeparator();
			for (int i = 0; i < decimalSeparator.length(); i++)
				allowed.add(decimalSeparator.charAt(i));
		}
	}
	
	protected static SafeUri getIconUrl(Object item, VirtualEnumFieldContext<?> context) {
		if (context.getVirtualEnum() == null)
			return null;
		
		List<VirtualEnumConstant> virtualEnumConstantList = getVirtualEnumValidItems(item, context.getVirtualEnum().getConstants());		
		for (VirtualEnumConstant enumValue : virtualEnumConstantList) {
			if (!enumValue.getValue().equals(item) && !enumValue.getDisplayValue().equals(item))
				continue;
			
			Icon icon = enumValue.getIcon();
			if (icon != null)				
				return UriUtils.fromString(context.getGmSession().getModelAccessory().getModelSession().resources().url(GMEIconUtil.getLargestImageFromIcon(icon)).asString());				
		}		
			
		return null;
	}

	private static List<VirtualEnumConstant> getVirtualEnumValidItems(Object item, List<VirtualEnumConstant> constants) {
		List<VirtualEnumConstant> VirtualEnumConstantList = null;
		for (VirtualEnumConstant virtualEnumConstant : constants) {
			if (item != virtualEnumConstant.getValue())
				continue;
			
			VirtualEnumConstantList = virtualEnumConstant.getValidSuccessors();
			if (VirtualEnumConstantList != null && !VirtualEnumConstantList.isEmpty() && !VirtualEnumConstantList.contains(virtualEnumConstant))
				VirtualEnumConstantList.add(virtualEnumConstant);
			break;
		}
		
		if (VirtualEnumConstantList == null || VirtualEnumConstantList.isEmpty())
			VirtualEnumConstantList = constants;
		
		return VirtualEnumConstantList;
	}

	/**
	 * Configures the required Loader for Dynamic entries.
	 */
	@Required
	public void setDynamicEntriesLoader(Function<SelectListConfig, Future<List<Object>>> dynamicEntriesLoader) {
		this.dynamicEntriesLoader = dynamicEntriesLoader;
		
		if (virtualEnum == null)
			loadDynamicEntries(context);
	}
	
	/**
	 * Configure an required supplier which provides an instance selection future.
	 */
	@Required
	public void setValueSelectionFutureSupplier(
			Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> valueSelectionFutureSupplier) {
		this.valueSelectionFutureSupplier = valueSelectionFutureSupplier;
	}
	
	public static <T> LabelProvider<T> getLabelProvider(VirtualEnumFieldContext<?> context) {
		return item -> getStringValue(item, context);
	}
	
	@Override
	public ImageResource getImageResource() {
		return GMGxtSupportResources.INSTANCE.dropDown();
	}

	@Override
	public TriggerField<?> getTriggerField() {
		return this;
	}
	
	@Override
	public void fireTriggerClick(NativeEvent event) {
		if (!isReadOnly())
			((ClickableComboBoxCell<D>) getCell()).onTriggerClick(createContext(), getElement(), event, getValue(), valueUpdater);
	}
	
	@Override
	protected void onKeyDown(Event event) {
		super.onKeyDown(event);
		lastKeyCode = event.getKeyCode();
	}
	
	@Override
	protected void onKeyPress(Event event) {
		super.onKeyPress(event);
		
		if (isForceSelection() || innerPropertyEditor == null)
			return;
		
		char key = (char) event.getCharCode();
		XEvent xEvent = event.cast();
		if (xEvent.isSpecialKey(lastKeyCode) || event.getCtrlKey())
			return;
		
		if (!allowed.contains(key))
			event.stopPropagation();
	}
	
	@Override
	public void setValue(D value) {
		super.setValue(value);
		preparePossibleValues();		
	}
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}
	
	private void preparePossibleValues() {
		if (!preparePossibleValues)
			return;
		
		preparePossibleValues = false;
		getStore().clear();
		
		D value = getValue();		
		possibleValues = getVirtualEnumValidItems(value, virtualEnum.getConstants());
		
		List<D> comboValues = new ArrayList<D>();
		possibleValues.forEach(possibleValue -> comboValues.add((D) possibleValue.getValue()));
		add(comboValues);
	}
	
	private static String getStringValue(Object value, VirtualEnumFieldContext<?> context) {
		if (value == null)
			return "";
		
		List<VirtualEnumConstant> possibleValues = null;
		VirtualEnum virtualEnum = context.getVirtualEnum();
		if (virtualEnum != null) {
			List<VirtualEnumConstant> constants = context.getVirtualEnum().getConstants();
			possibleValues = getVirtualEnumValidItems(value, constants);
		
			for (VirtualEnumConstant virtualEnumConstant : possibleValues) {
				if (virtualEnumConstant.getValue() == value || virtualEnumConstant.getValue().equals(value))
					return I18nTools.getLocalized(virtualEnumConstant.getDisplayValue());
			}
		}
		
		if (context.getInnerPropertyEditor() == null)
			return value.toString();
		
		PropertyEditor<Object> propertyEditor = (PropertyEditor<Object>) context.getInnerPropertyEditor();
		return propertyEditor.render(value);
	}
	
	private native int getKeyCode(NativeEvent e) /*-{
		return e.keyCode || 0;
	}-*/;
	
	private native char getChar(NativeEvent e) /*-{
		return e.which || e.charCode || e.keyCode || 0;
	}-*/;
	
	private void loadDynamicEntries(VirtualEnumFieldContext<D> context) {
		DynamicSelectList dynamicSelectList = context.getDynamicSelectList();
		SelectListConfig config = new SelectListConfig(dynamicSelectList.getRequestProcessing(), context.getParentEntity(), context.getPropertyName(),
				dynamicSelectList.getDisableCache());
		dynamicEntriesLoader.apply(config) //
				.andThen(possibleValues -> {
					dynamicPossibleValues = possibleValues;
					if (possibleValues == null)
						return;

					List<D> comboValues = new ArrayList<>();
					for (Object possibleValue : possibleValues)
						comboValues.add((D) possibleValue);
					add(comboValues);

					if (initialValue != null)
						setValue(initialValue);
				}).onError(e -> ErrorDialog.show("Error while loading dynamic entries.", e));
	}
	
	private void handleValueSelectionFuture() {
		if (valueSelectionFuture == null)
			valueSelectionFuture = valueSelectionFutureSupplier.get();
		
		SelectionConfig config = new SelectionConfig(context.getGmType(), null, 1, null, context.getGmSession(), null, false,
				context.isInstantiable(), context.isReferenceable(), true, false, null);
		config.setPossibleValues(dynamicPossibleValues);
		valueSelectionFuture.apply(config) //
				.andThen(instanceSelectionData -> {
					List<GMTypeInstanceBean> result = instanceSelectionData == null ? null : instanceSelectionData.getSelections();
					if (result == null || result.isEmpty())
						return;

					setValue((D) result.get(0).getInstance(), true);
					editingField = false;
				}).onError(e -> ErrorDialog.show("Error while getting selected value.", e));
	}
	
	public static class VirtualEnumFieldContext<D> {
		
		private VirtualEnum virtualEnum;
		private PersistenceGmSession gmSession;
		private D initialValue;
		private PropertyEditor<?> innerPropertyEditor;
		private boolean allowDecimal;
		private DynamicSelectList dynamicSelectList;
		private GenericModelType gmType;
		private boolean instantiable;
		private boolean referenceable;
		private String propertyName;
		private GenericEntity parentEntity;
		
		public void setVirtualEnum(VirtualEnum virtualEnum) {
			this.virtualEnum = virtualEnum;
		}
		
		public VirtualEnum getVirtualEnum() {
			return virtualEnum;
		}
		
		public void setGmType(GenericModelType gmType) {
			this.gmType = gmType;
		}
		
		public GenericModelType getGmType() {
			return gmType;
		}
		
		public void setDynamicSelectList(DynamicSelectList dynamicSelectList) {
			this.dynamicSelectList = dynamicSelectList;
		}
		
		public DynamicSelectList getDynamicSelectList() {
			return dynamicSelectList;
		}
		
		public void setGmSession(PersistenceGmSession gmSession) {
			this.gmSession = gmSession;
		}
		
		public PersistenceGmSession getGmSession() {
			return gmSession;
		}
		
		public void setInitialValue(D initialValue) {
			this.initialValue = initialValue;
		}
		
		public D getInitialValue() {
			return initialValue;
		}
		
		public PropertyEditor<?> getInnerPropertyEditor() {
			return innerPropertyEditor;
		}
		
		public void setInnerPropertyEditor(PropertyEditor<?> innerPropertyEditor) {
			this.innerPropertyEditor = innerPropertyEditor;
		}
		
		public void setAllowDecimal(boolean allowDecimal) {
			this.allowDecimal = allowDecimal;
		}
		
		public boolean isAllowDecimal() {
			return allowDecimal;
		}
		
		public void setInstantiable(boolean instantiable) {
			this.instantiable = instantiable;
		}
		
		public boolean isInstantiable() {
			return instantiable;
		}
		
		public void setReferenceable(boolean referenceable) {
			this.referenceable = referenceable;
		}
		
		public boolean isReferenceable() {
			return referenceable;
		}
		
		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
		
		public void setParentEntity(GenericEntity parentEntity) {
			this.parentEntity = parentEntity;
		}
		
		public GenericEntity getParentEntity() {
			return parentEntity;
		}
	}

}
