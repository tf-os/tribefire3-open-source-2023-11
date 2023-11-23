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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gxt.gxtresources.components.client.GmeToolTip;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.widget.core.client.form.Field;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

public class GmRegExValidator extends RegExValidator {

	private PropertyFieldContext propertyFieldContext;
	private Validation validation;	
	private Field<?> field;
	private GmeToolTip tip;
	
	public GmRegExValidator(String regex) {
		super(regex);
	}

	public GmRegExValidator(String regex, final String message) {
		super(regex, message);		
	}
	
	public PropertyFieldContext getPropertyFieldContext() {
		return propertyFieldContext;
	}

	public void setPropertyFieldContext(PropertyFieldContext propertyFieldContext) {
		this.propertyFieldContext = propertyFieldContext;
	}	
	
	@Override
	public List<EditorError> validate(Editor<String> field, String value) {
	    if (value == null)
	    	return null;

	    if (!value.isEmpty() && (!getCompiledRegExp().test(value))) {
			if (validation != null) {
				Future<ValidationLog> valRes = validation.validatePropertyValue(propertyFieldContext.getParentEntity(), propertyFieldContext.getPropertyName(), value);
				valRes.andThen(result -> {
					String validationRes = 	doValidation(result);
					if (validationRes != null && !validationRes.isEmpty()) {
						tip = prepareToolTip(validationRes);
						tip.showAtHoverElement();
					} else {
						if (tip != null && tip.isVisible())
							tip.hide();
					}
				});	
				return null;
			}
	    	
			tip = prepareToolTip(getMessages().regExMessage());
			tip.showAtHoverElement();			
			return null;
	    } else {
			if (tip != null && tip.isVisible())
				tip.hide();	    	
	    }
	    
	    return null;
	}
	
	public Validation getValidation() {
		return validation;
	}

	public void setValidation(Validation validation) {
		this.validation = validation;
	}
	
	private String doValidation(ValidationLog validationLog) {
		if (propertyFieldContext.getPropertyName() == null)
			return null;
		
		for (Entry<GenericEntity, ArrayList<ValidatorResult>> validationEntry : validationLog.entrySet()) {
			GenericEntity parentEntity = validationEntry.getKey();
			if (!parentEntity.equals(propertyFieldContext.getParentEntity()))
				continue;
			
			for ( ValidatorResult validatorResult : validationEntry.getValue()) {
				String failPropertyName = validatorResult.getPropertyName();
				if (failPropertyName == null)
					continue;
				
				if (propertyFieldContext.getPropertyName().equals(failPropertyName)) {
					return validatorResult.getResultMessage();
				}
				
			}
		}		
		return null;
	}

	private GmeToolTip prepareToolTip(String toolTipText) {
		if (tip == null) {
			ToolTipConfig toolTipConfig = new ToolTipConfig(toolTipText);
			toolTipConfig.setAutoHide(false);						
			toolTipConfig.setAnchorArrow(true);
			toolTipConfig.setAnchor(Side.BOTTOM);
			toolTipConfig.setAnchorToTarget(true);
			
			tip = new GmeToolTip(field, field.getElement(), toolTipConfig);
			tip.setShowOnHover(false);
			tip.addStyleName("failValidationPropertyToolTip");			
		} else {
			tip.getToolTipConfig().setBody(toolTipText);
		}
		
		return tip;
	}
	
	public Field<?> getField() {
		return field;
	}

	public void setField(Field<?> field) {
		this.field = field;
	}
	
}
