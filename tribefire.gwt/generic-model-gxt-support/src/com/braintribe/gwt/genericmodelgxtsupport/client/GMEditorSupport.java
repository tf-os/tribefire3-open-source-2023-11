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
package com.braintribe.gwt.genericmodelgxtsupport.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableComboBox;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableDateField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableDateTimeField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableMonthDayField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableYearMonthField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ExtendedStringDialog;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.GmExtendedHtmlEditor;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.GmExtendedStringField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.GmExtendedTextArea;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.PasswordTextArea;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.VirtualEnumField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.VirtualEnumField.VirtualEnumFieldContext;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.htmleditor.HtmlEditorDialog;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.htmleditor.HtmlEditorField;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectListConfig;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.DateTimeField;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedPasswordField;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.extensiondeployment.meta.DynamicSelectList;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.meta.data.constraint.DateClipping;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.EditAsHtml;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.Field;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.BigDecimalPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.FloatPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.LongPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;

/**
 * This class provides static methods for preparing Fields based on the GenericModelType.
 * @author michel.docouto
 *
 */
public class GMEditorSupport {
	
    public interface ClickableComboBoxTemplates extends XTemplates {
		@XTemplate("<img class=\"clickableComboItemImage\" width=\"16\" height=\"16\" src=\"{imageUri}\"/><span class=\"gmeComboText\">{value}</span>")
		SafeHtml comboImageWithValue(SafeUri imageUri, String value);

		@XTemplate("<img class=\"clickableComboItemImage\" width=\"16\" height=\"16\" src=\"{imageUri}\"/>")
		SafeHtml comboImage(SafeUri imageUri);

		@XTemplate("<span class=\"gmeComboText\">{value}</span>")
		SafeHtml comboValue(String value);    	    	
    }
	
	private static Function<PropertyFieldContext, Supplier<? extends Field<?>>> additionalFieldsProviders;
	private boolean useExtendedStringField = true;
	private Function<SelectListConfig, Future<List<Object>>> dynamicEntriesLoader;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> valueSelectionFutureSupplier;
	private Supplier<? extends ExtendedStringDialog> extendedStringDialog;
	private Supplier<? extends HtmlEditorDialog> htmlEditorDialog;
	private Validation validation;
	
	/**
	 * Configures the required loader for dynamic entries.
	 */
	@Required
	public void setDynamicEntriesLoader(Function<SelectListConfig, Future<List<Object>>> dynamicEntriesLoader) {
		this.dynamicEntriesLoader = dynamicEntriesLoader;
	}
	
	/**
	 * Configures the required supplier for value selection.
	 */
	@Required
	public void setValueSelectionFutureSupplier(
			Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> valueSelectionFutureSupplier) {
		this.valueSelectionFutureSupplier = valueSelectionFutureSupplier;
	}
	
	/**
	 * Sets an Function for providing additional field providers.
	 */
	@Configurable
	public static void setAdditionalFieldsProviders(Function<PropertyFieldContext, Supplier<? extends Field<?>>> fieldsProviders) {
		additionalFieldsProviders = fieldsProviders;
	}
	
	/**
	 * Configures whether we should use the {@link ExtendedStringField} for Strings. Defaults to true.
	 * If false, a normal {@link TextField} is used.
	 */
	@Configurable
	public void setUseExtendedStringField(boolean useExtendedStringField) {
		this.useExtendedStringField = useExtendedStringField;
	}
	
	/**
	 * Configures the dialog used when extended string field is enabled.
	 * If {@link #setUseExtendedStringField(boolean)} is true, then this is required.
	 */
	@Configurable
	public void setExtendedStringFieldDialogProvider(Supplier<? extends ExtendedStringDialog> extendedStringDialog) {
		this.extendedStringDialog = extendedStringDialog;
	}
	
	/**
	 * Configures the dialog used for html editing a string. 
	 */
	@Required
	public void setHtmlEditorDialog(Supplier<? extends HtmlEditorDialog> htmlEditorDialog) {
		this.htmlEditorDialog = htmlEditorDialog;
	}
	
	/**
	 * Provides a Field for the given {@link PropertyFieldContext}.
	 */
	@SuppressWarnings("rawtypes")
	public IsField<?> providePropertyField(final PropertyFieldContext propertyFieldContext) {
		GenericModelType type = propertyFieldContext.getModelType();
		
		Class<?> javaType = type.getJavaType();
		DynamicSelectList dynamicSelectList = propertyFieldContext.getDynamicSelectList();
		if (propertyFieldContext.getVirtualEnum() != null || dynamicSelectList != null)
			return prepareVirtualEnumField(propertyFieldContext, type, javaType, dynamicSelectList);
		
		List<Enum<?>> possibleValues = null;
		if (type.getTypeCode() == TypeCode.enumType)
			possibleValues = preparePossibleValues((EnumType) type);
		
		if (possibleValues != null && !possibleValues.isEmpty())
			return prepareEnumComboBox(propertyFieldContext, possibleValues);
		
		IsField<?> field = null;
		Supplier<? extends Field<?>> fieldProvider = additionalFieldsProviders.apply(propertyFieldContext);
		if (fieldProvider != null)
			field = fieldProvider.get();
		
		Object minValue = propertyFieldContext.getMinValue();
		Object maxValue = propertyFieldContext.getMaxValue();
		
		if (field instanceof DateField) {
			DateField dateField = prepareDateField(true, propertyFieldContext, minValue, maxValue);
			if (dateField != null)
				field = dateField;
		}
		
		if (field == null) {
			if (propertyFieldContext.getPassword()) {
				if (propertyFieldContext.getUseAlternativeField())
					field = prepareTextArea(true);
				else
					field = new FixedPasswordField();
			} else if (javaType == String.class)
				field = prepareStringField(propertyFieldContext);
			else if (javaType == Integer.class)
				field = prepareIntegerField(propertyFieldContext, minValue, maxValue);
			else if (javaType == Date.class)
				field = prepareDateField(true, propertyFieldContext, minValue, maxValue);
			else if (javaType == Boolean.class) //We should not actually show an editor for the boolean type.
				return null;
			else if (javaType == Float.class)
				field = prepareFloatField(propertyFieldContext, minValue, maxValue);
			else if (javaType == Double.class)
				field = prepareDoubleField(propertyFieldContext, minValue, maxValue);
			else if (javaType == Long.class)
				field = prepareLongField(propertyFieldContext, minValue, maxValue);
			else if (javaType == BigDecimal.class)
				field = prepareBigDecimalField(propertyFieldContext, minValue, maxValue);
			else
				return null;
		}
				
		if (field instanceof Field) {
			((Field<?>) field).setReadOnly(propertyFieldContext.isReadOnly());
			Field<?> theField = (Field) field;
			if (propertyFieldContext.getParentEntity()!= null && propertyFieldContext.getParentEntity().session() instanceof PersistenceGmSession && validation != null)
				validation.getPredator().setGmSession((PersistenceGmSession) propertyFieldContext.getParentEntity().session());		
			
			if (javaType == String.class) {
				if (propertyFieldContext.getRegex() != null) {
					Pattern regex = propertyFieldContext.getRegex();
					GmRegExValidator validator = new GmRegExValidator(regex.getExpression(), LocalizedText.INSTANCE.invalidFormat());
					validator.setPropertyFieldContext(propertyFieldContext);
					validator.setValidation(validation);
					validator.setField(theField);
					theField.addValidator((Validator) validator);					
					theField.setAutoValidate(true);
				}
				
				if (propertyFieldContext.getMaxLenght() > 0) {
					GmMaxLengthValidator validator = new GmMaxLengthValidator(propertyFieldContext.getMaxLenght());
					validator.setPropertyFieldContext(propertyFieldContext);
					validator.setValidation(validation);
					validator.setField(theField);
					theField.addValidator((Validator) validator);					
				}
				
				if (propertyFieldContext.getMinLenght() > 0) {
					GmMinLengthValidator validator = new GmMinLengthValidator(propertyFieldContext.getMinLenght());
					validator.setPropertyFieldContext(propertyFieldContext);
					validator.setValidation(validation);
					validator.setField(theField);
					theField.addValidator((Validator) validator);					
				}
			}
			
			if (propertyFieldContext.isMandatory()) {
				GmEmptyValidator validator = new GmEmptyValidator();
				validator.setPropertyFieldContext(propertyFieldContext);
				validator.setValidation(validation);
				validator.setField(theField);
				theField.addValidator(validator);
			}
		}
		
		if (field instanceof GmSessionHandler)
			((GmSessionHandler) field).configureGmSession(propertyFieldContext.getGmSession());
		
		return field;
	}

	private Field<?> prepareBigDecimalField(final PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		NumberField<BigDecimal> numberField = new NumberField<>(new BigDecimalPropertyEditor() {
			@Override
			public String render(Number value) {
				String render = super.render(value);
				if (render != null)
					render = render.replace(LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator(),
							LocaleUtil.getDecimalSeparator());
				return render;
			}
			
			@Override
			public BigDecimal parse(CharSequence text) throws java.text.ParseException {
				if (text != null) {
					String textString = text.toString();
					textString = textString.replace(LocaleUtil.getDecimalSeparator(),
							LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator());
					text = textString;
				}
				
				return super.parse(text);
			}
		});
		numberField.setAllowDecimals(false);
		numberField.setDecimalSeparator(LocaleUtil.getDecimalSeparator());
		numberField.setAllowDecimals(true);
		numberField.setValue((BigDecimal) propertyFieldContext.getInitialValue());
		if (minValue instanceof BigDecimal) {
			GmMinNumberValidator<BigDecimal> validator = new GmMinNumberValidator<>((BigDecimal) minValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		if (maxValue instanceof BigDecimal) {
			GmMaxNumberValidator<BigDecimal> validator = new GmMaxNumberValidator<>((BigDecimal) maxValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		
		return numberField;
	}

	private Field<?> prepareLongField(final PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		NumberField<Long> numberField = new NumberField<>(new LongPropertyEditor());
		numberField.setValue((Long) propertyFieldContext.getInitialValue());
		if (minValue instanceof Long) {
			GmMinNumberValidator<Long> validator = new GmMinNumberValidator<>((Long) minValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		if (maxValue instanceof Long) {
			GmMaxNumberValidator<Long> validator = new GmMaxNumberValidator<>((Long) maxValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		
		return numberField;
	}

	private Field<?> prepareDoubleField(final PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		NumberField<Double> numberField = new NumberField<>(new DoublePropertyEditor() {
			@Override
			public String render(Number value) {
				String render = super.render(value);
				if (render != null)
					render = render.replace(LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator(),
							LocaleUtil.getDecimalSeparator());
				return render;
			}
			
			@Override
			public Double parse(CharSequence text) throws java.text.ParseException {
				if (text != null) {
					String textString = text.toString();
					textString = textString.replace(LocaleUtil.getDecimalSeparator(),
							LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator());
					text = textString;
				}
				
				return super.parse(text);
			}
		});
		numberField.setAllowDecimals(false);
		numberField.setDecimalSeparator(LocaleUtil.getDecimalSeparator());
		numberField.setAllowDecimals(true);
		numberField.setValue((Double) propertyFieldContext.getInitialValue());
		if (minValue instanceof Double) {
			GmMinNumberValidator<Double> validator = new GmMinNumberValidator<>((Double) minValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		if (maxValue instanceof Double) {
			GmMaxNumberValidator<Double> validator = new GmMaxNumberValidator<>((Double) maxValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);
			numberField.addValidator(validator);
		}
		
		return numberField;
	}

	private Field<?> prepareFloatField(final PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		NumberField<Float> numberField = new NumberField<>(new FloatPropertyEditor() {
			@Override
			public String render(Number value) {
				String render = super.render(value);
				if (render != null)
					render = render.replace(LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator(),
							LocaleUtil.getDecimalSeparator());
				return render;
			}
			
			@Override
			public Float parse(CharSequence text) throws java.text.ParseException {
				if (text != null) {
					String textString = text.toString();
					textString = textString.replace(LocaleUtil.getDecimalSeparator(),
							LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator());
					text = textString;
				}
				
				return super.parse(text);
			}
		});
		numberField.setAllowDecimals(false);
		numberField.setDecimalSeparator(LocaleUtil.getDecimalSeparator());
		numberField.setAllowDecimals(true);
		numberField.setValue((Float) propertyFieldContext.getInitialValue());
		if (minValue instanceof Float) {
			GmMinNumberValidator<Float> validator = new GmMinNumberValidator<>((Float) minValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		if (maxValue instanceof Float) {
			GmMaxNumberValidator<Float> validator = new GmMaxNumberValidator<>((Float) maxValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);
			numberField.addValidator(validator);
		}
		
		return numberField;
	}

	private Field<?> prepareIntegerField(final PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		NumberField<Integer> numberField = new NumberField<>(new IntegerPropertyEditor());
		numberField.setAllowDecimals(false);
		numberField.setValue((Integer) propertyFieldContext.getInitialValue());
		if (minValue instanceof Integer) {
			GmMinNumberValidator<Integer> validator = new GmMinNumberValidator<>((Integer) minValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);			
			numberField.addValidator(validator);
		}
		if (maxValue instanceof Integer) {
			GmMaxNumberValidator<Integer> validator = new GmMaxNumberValidator<>((Integer) maxValue);
			validator.setPropertyFieldContext(propertyFieldContext);
			validator.setValidation(validation);
			validator.setField(numberField);
			numberField.addValidator(validator);
		}
		
		return numberField;
	}

	private IsField<?> prepareStringField(PropertyFieldContext propertyFieldContext) {
		IsField<?> field;
		
		EntityMdResolver entityMdResolver;
		if (propertyFieldContext.getParentEntity() != null)
			entityMdResolver = GmSessions.getMetaData(propertyFieldContext.getParentEntity()).lenient(true).entity(propertyFieldContext.getParentEntity());
		else
			entityMdResolver = propertyFieldContext.getGmSession().getModelAccessory().getMetaData().lenient(true).entityType(propertyFieldContext.getParentEntityType());
		
		entityMdResolver = entityMdResolver.useCase(propertyFieldContext.getUseCase());
		EditAsHtml editAsHtml = entityMdResolver.property(propertyFieldContext.getPropertyName()).meta(EditAsHtml.T).exclusive();
		if (editAsHtml != null) {
			if (!propertyFieldContext.getUseAlternativeField()) {
				HtmlEditorField htmlEditor = new HtmlEditorField();
				htmlEditor.setHtmlEditorDialog(htmlEditorDialog);
				htmlEditor.setUsePTag(editAsHtml.getUsePTag());
				htmlEditor.setUseStrongTag(editAsHtml.getUseStrongTag());
				htmlEditor.setUseEmTag(editAsHtml.getUseEmTag());
				field = htmlEditor;
			} else {
				GmExtendedHtmlEditor htmlEditor = new GmExtendedHtmlEditor(htmlEditorDialog);
				htmlEditor.setUsePTag(editAsHtml.getUsePTag());
				htmlEditor.setUseStrongTag(editAsHtml.getUseStrongTag());
				htmlEditor.setUseEmTag(editAsHtml.getUseEmTag());
				field = htmlEditor;
				field.asWidget().setHeight("90px");
				
				//The HtmlEditor is NOT a HasKeyPressHandlers instance
				/*if (field instanceof HasKeyPressHandlers) {
					final IsField<?> finalField = field;
					((HasKeyPressHandlers) field).addKeyPressHandler(event -> {
						if (event.getCharCode() == KeyCodes.KEY_BACKSPACE || event.getCharCode() == KeyCodes.KEY_DELETE) {
							while (!isFieldScrollableY(finalField) && finalField.asWidget().getOffsetHeight() >= 104)
								finalField.asWidget().setHeight(finalField.asWidget().getOffsetHeight() - 14 + "px");
							
							while (isFieldScrollableY(finalField) && finalField.asWidget().getOffsetHeight() <= 150)
								finalField.asWidget().setHeight(finalField.asWidget().getElement().getOffsetHeight() + 14 + "px");
							
							return;
						}
						
						while (isFieldScrollableY(finalField) && finalField.asWidget().getOffsetHeight() <= 150)
							finalField.asWidget().setHeight(finalField.asWidget().getOffsetHeight() + 14 + "px");
					});
				}*/
			}
		} else if (!propertyFieldContext.getUseAlternativeField())
			field = useExtendedStringField ? new GmExtendedStringField(extendedStringDialog) : new TextField();
		else
			field = prepareTextArea(false);
		
		((TakesValue<String>) field).setValue((String) propertyFieldContext.getInitialValue());
		//if (field instanceof Field)
		//	((Field<?>) field).setReadOnly(propertyFieldContext.isReadOnly());
		return field;
	}
	
	private Field<?> prepareTextArea(boolean usePassword) {
		Field<?> field;
		if (usePassword)
			field = new PasswordTextArea();
		else
			field = new GmExtendedTextArea(extendedStringDialog);
		field.setHeight(90);
		
		if (field instanceof HasKeyPressHandlers) {
			final Field<?> finalField = field;
			((HasKeyPressHandlers) field).addKeyPressHandler(event -> {
				if (event.getCharCode() == KeyCodes.KEY_BACKSPACE || event.getCharCode() == KeyCodes.KEY_DELETE) {
					while (!isFieldScrollableY(finalField) && finalField.getElement().getHeight(false) >= 104)
						finalField.getElement().setHeight(finalField.getElement().getHeight(false) - 14);
					
					while (isFieldScrollableY(finalField) && finalField.getElement().getHeight(false) <= 150)
						finalField.getElement().setHeight(finalField.getElement().getHeight(false) + 14);
					
					return;
				}
				
				while (isFieldScrollableY(finalField) && finalField.getElement().getHeight(false) <= 150)
					finalField.getElement().setHeight(finalField.getElement().getHeight(false) + 14);
			});
		}
		
		return field;
	}

	private Field<?> prepareEnumComboBox(PropertyFieldContext propertyFieldContext, List<Enum<?>> possibleValues) {
		ModelKeyProvider<Enum<?>> modelKeyProvider = item -> item.name();
		
		LabelProvider<Enum<?>> labelProvider = item -> {
			String stringValue = item.toString();
			Name name = propertyFieldContext.getGmSession().getModelAccessory().getMetaData().lenient(true).enumConstant(item).
					useCase(propertyFieldContext.getUseCase()).meta(Name.T).exclusive();
			return name == null || name.getName() == null ? stringValue : I18nTools.getLocalized(name.getName());
		};
		
		//ComboBox<Enum<?>> comboBox = new ClickableComboBox<Enum<?>>(new ListStore<Enum<?>>(modelKeyProvider), labelProvider);
		ComboBox<Enum<?>> comboBox = new ClickableComboBox<>(new ListStore<Enum<?>>(modelKeyProvider), labelProvider, new AbstractSafeHtmlRenderer<Enum<?>>() {
			@Override
			public SafeHtml render(Enum<?> item) {
				ClickableComboBoxTemplates comboboxTemplates = GWT.create(ClickableComboBoxTemplates.class);
				SafeUri imageUri = getIconUrl(item, propertyFieldContext); 							
				String value = labelProvider.getLabel(item);
				
				if (imageUri == null || imageUri.toString().isEmpty())
					return comboboxTemplates.comboValue(value);
				
				return comboboxTemplates.comboImageWithValue(imageUri, value);
			}				
		});
		
		comboBox.getStore().addAll(possibleValues);
		comboBox.setEditable(true);   //RVE - enable to be able sort the list - together with ForceSelection is not possible to add new value
		comboBox.setForceSelection(true);
		comboBox.setTriggerAction(TriggerAction.ALL);
		comboBox.setReadOnly(propertyFieldContext.isReadOnly());
				
		return comboBox;
	}

	@SuppressWarnings("rawtypes")
	private Field<?> prepareVirtualEnumField(final PropertyFieldContext propertyFieldContext, GenericModelType type, Class<?> javaType,
			DynamicSelectList dynamicSelectList) {
		VirtualEnumFieldContext context = new VirtualEnumFieldContext();
		context.setGmSession(propertyFieldContext.getGmSession());
		context.setInitialValue(propertyFieldContext.getInitialValue());
		context.setVirtualEnum(propertyFieldContext.getVirtualEnum());
		context.setDynamicSelectList(dynamicSelectList);
		context.setPropertyName(propertyFieldContext.getPropertyName());
		context.setParentEntity(propertyFieldContext.getParentEntity());
		context.setGmType(type);
		context.setInstantiable(propertyFieldContext.isInstantiable());
		context.setReferenceable(propertyFieldContext.isReferenceable());
		
		VirtualEnumField<?> field;
		if (javaType == Integer.class) {
			context.setInnerPropertyEditor(new IntegerPropertyEditor());
			context.setAllowDecimal(false);
			field = new VirtualEnumField<Integer>(context, VirtualEnumField.getLabelProvider(context));
		} else if (javaType == Float.class) {
			context.setInnerPropertyEditor(new FloatPropertyEditor());
			context.setAllowDecimal(true);
			field = new VirtualEnumField<Float>(context, VirtualEnumField.getLabelProvider(context));
		} else if (javaType == Long.class) {
			context.setInnerPropertyEditor(new LongPropertyEditor());
			context.setAllowDecimal(true);
			field = new VirtualEnumField<Long>(context, VirtualEnumField.getLabelProvider(context));
		} else if (javaType == Double.class) {
			context.setInnerPropertyEditor(new DoublePropertyEditor());
			context.setAllowDecimal(true);
			field = new VirtualEnumField<Double>(context, VirtualEnumField.getLabelProvider(context));
		} else if (javaType == BigDecimal.class) {
			context.setInnerPropertyEditor(new BigDecimalPropertyEditor());
			context.setAllowDecimal(true);
			field = new VirtualEnumField<BigDecimal>(context, VirtualEnumField.getLabelProvider(context));
		} else
			field = new VirtualEnumField<String>(context, VirtualEnumField.getLabelProvider(context));
		field.setDynamicEntriesLoader(dynamicEntriesLoader);
		field.setValueSelectionFutureSupplier(valueSelectionFutureSupplier);
		field.setReadOnly(propertyFieldContext.isReadOnly());
		
		return field;
	}
	
	protected SafeUri getIconUrl(Object item, PropertyFieldContext propertyFieldContext) {
		if (item == null || propertyFieldContext == null || propertyFieldContext.getIconProvider() == null)
			return null;
		
		GenericModelType type = propertyFieldContext.getModelType();
		
		ModelPath modelPath = null;		
		if (type.isEntity()) {
			modelPath = new ModelPath();
			modelPath.add(new RootPathElement(type, item));			
		} else if (type.isEnum()) {
			modelPath = new ModelPath();
			modelPath.add(new RootPathElement(type, item));			
		} else if (propertyFieldContext.getVirtualEnum() != null) {
			VirtualEnum ve = propertyFieldContext.getVirtualEnum();
			modelPath = new ModelPath();
			modelPath.add(new PropertyPathElement(ve, ve.entityType().getProperty("constants"), item.toString()));
		}
		
		if (modelPath == null)
			return null;
		
		IconAndType iconAndType = propertyFieldContext.getIconProvider().apply(modelPath);
		if (iconAndType != null && iconAndType.getIcon() != null)
			return iconAndType.getIcon().getSafeUri();
		
		//2nd choice for EnumTypes (VirtualEnums)
		if (type.isEnum()) {
			Icon icon = propertyFieldContext.getGmSession().getModelAccessory().getMetaData().enumConstant((Enum<?>) item).
				useCase(propertyFieldContext.getUseCase()).meta(Icon.T).exclusive();
			if (icon != null && icon.getIcon() != null)				
				return UriUtils.fromString(propertyFieldContext.getGmSession().getModelAccessory().getModelSession().resources().url(GMEIconUtil.getLargestImageFromIcon(icon.getIcon())).asString());				
		}
		
		return null;
	}

	private List<Enum<?>> preparePossibleValues(EnumType enumType) {
		Enum<? extends Enum<?>>[] enumValues = enumType.getEnumValues();
		List<Enum<?>> possibleValues = new ArrayList<>();
		for (Enum<? extends Enum<?>> enumValue : enumValues)
			possibleValues.add(enumValue);
		
		return possibleValues;
	}
	
	private static boolean isFieldScrollableY(IsWidget field) {
		return field.asWidget().getElement().getFirstChildElement().getScrollHeight() > field.asWidget().getElement().getClientHeight();
	}
	
	private DateField prepareDateField(boolean forcePrepareField, PropertyFieldContext propertyFieldContext, Object minValue, Object maxValue) {
		EntityMdResolver entityBuilder;
		GenericEntity parentEntity = propertyFieldContext.getParentEntity();
		if (parentEntity != null)
			entityBuilder = GmSessions.getMetaData(parentEntity).entity(parentEntity).useCase(propertyFieldContext.getUseCase());
		else {
			entityBuilder = propertyFieldContext.getGmSession().getModelAccessory().getMetaData().useCase(propertyFieldContext.getUseCase())
					.entityType(propertyFieldContext.getParentEntityType());
		}
		
		DateClipping dateClipping = entityBuilder.property(propertyFieldContext.getPropertyName()).meta(DateClipping.T).exclusive();
		
		DateField dateField = null;
		String datePattern;
		if (dateClipping == null) {
			datePattern = LocaleUtil.getDateTimeFormat();
			if (forcePrepareField)
				dateField = new ClickableDateTimeField();
		} else {
			datePattern = GMEMetadataUtil.getDatePattern(dateClipping, LocaleUtil.getDateTimeFormat());
			DateOffsetUnit lower = dateClipping.getLower();
			DateOffsetUnit upper = dateClipping.getUpper();
			
			if (DateOffsetUnit.month.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year)))
				dateField = new ClickableYearMonthField();
			else if (DateOffsetUnit.month.equals(lower) && DateOffsetUnit.month.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.day.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year)))
				dateField = new ClickableDateField();
			else if (DateOffsetUnit.year.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year))) {
				dateField = new ClickableDateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.second.equals(lower) && (upper == null || upper.equals(DateOffsetUnit.year))) {
				DateTimeField field = new ClickableDateTimeField();
				String regex = LocalizedText.INSTANCE.hourMinuteSecondRegex();
				if (dateClipping.getTimeSeparator() != null && !dateClipping.getTimeSeparator().equals(":"))
					regex = regex.replaceAll(":", dateClipping.getTimeSeparator());
				field.setTimeRegex(regex, datePattern);
				field.setUseSeconds(true);
				dateField = field;
			} else if (DateOffsetUnit.second.equals(lower) && DateOffsetUnit.hour.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if ((lower == null || DateOffsetUnit.millisecond.equals(lower)) && (upper == null || upper.equals(DateOffsetUnit.year))) {
				DateTimeField field = new ClickableDateTimeField();
				String regex = LocalizedText.INSTANCE.hourMinuteSecondMillisecondRegex();
				if (dateClipping.getTimeSeparator() != null && !dateClipping.getTimeSeparator().equals(":"))
					regex = regex.replaceAll(":", dateClipping.getTimeSeparator());
				field.setTimeRegex(regex, datePattern);
				field.setUseMilliseconds(true);
				dateField = field;
			} else if (DateOffsetUnit.day.equals(lower) && DateOffsetUnit.month.equals(upper))
				dateField = new ClickableMonthDayField();
			else if (DateOffsetUnit.day.equals(lower) && DateOffsetUnit.day.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.minute.equals(lower) && DateOffsetUnit.hour.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.hour.equals(lower) && DateOffsetUnit.hour.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.second.equals(lower) && DateOffsetUnit.minute.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else if (DateOffsetUnit.second.equals(lower) && DateOffsetUnit.second.equals(upper)) {
				dateField = new DateField();
				dateField.setHideTrigger(true);
			} else
				dateField = new ClickableDateTimeField();
		}
		
		if (dateField != null) {
			dateField.setPropertyEditor(new DateTimePropertyEditor(datePattern));
			dateField.setValue((Date) propertyFieldContext.getInitialValue());
			
			if (minValue instanceof Date)
				dateField.setMinValue((Date) minValue);
			if (maxValue instanceof Date)
				dateField.setMaxValue((Date) maxValue);
		}
		
		return dateField;
	}

	public Validation getValidation() {
		return validation;
	}

	public void setValidation(Validation validation) {
		this.validation = validation;
	}

}
