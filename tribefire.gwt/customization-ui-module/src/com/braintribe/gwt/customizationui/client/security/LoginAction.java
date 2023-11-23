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
package com.braintribe.gwt.customizationui.client.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.customizationui.client.resources.UiResources;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ExceptionUtil;
import com.braintribe.gwt.security.client.AuthenticationException;
import com.braintribe.gwt.security.client.SecurityService;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;

/**
 * This action is responsible for signing in the user.
 * 
 * @author michel.docouto
 *
 */
public class LoginAction extends Action {
	
	private SecurityService securityService;
	private Supplier<? extends ValueBaseField<String>> userNameTextFieldSupplier;
	private ValueBaseField<String> userNameTextField;
	private Supplier<? extends ValueBaseField<String>> passwordTextFieldSupplier;
	private ValueBaseField<String> passwordTextField;
	private Supplier<Label> errorMessageLabelSupplier;
	private Label errorMessageLabel;
	private Supplier<? extends Widget> parentPanelSupplier;
	private Widget parentPanel;
	private KeyDownHandler keyDownHandler = event -> {
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER)
			login(event.getRelativeElement());
	};
	private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();
	
	private Predicate<Throwable> failedLoginExceptionFilter = throwable -> throwable instanceof AuthenticationException;
	
	public LoginAction() {
		setName(LocalizedText.INSTANCE.signIn());
		setTooltip(LocalizedText.INSTANCE.signInDescription());
		setIcon(UiResources.INSTANCE.login());
	}
	
	private void login(Element element) {
		FormElement formElement = findForm(element);
		
		if (formElement != null) {
			prepareForm(formElement);
			formElement.submit();
		}
		
		if (userNameTextField.getValue() == null || passwordTextField.getValue() == null) {
			if (userNameTextField.getValue() == null ) {
	  			userNameTextField.focus();
	  		} else {
	  			passwordTextField.focus();
	  		}
			return;
		}
		
		getParentPanel().getElement().<XElement>cast().mask(LocalizedText.INSTANCE.signingIn());
		
		securityService.login(userNameTextField.getValue(), passwordTextField.getValue(), //
				AsyncCallbacks //
						.of(result -> {
							if (result == null)
								handleLoginFailure(new Exception(LocalizedText.INSTANCE.sessionNotCreated()));
							else {
								getParentPanel().getElement().<XElement> cast().unmask();
								getErrorMessageLabel().setText("");
								userNameTextField.setValue("");
								passwordTextField.setValue("");
							}
						}, this::handleLoginFailure));
	}
	
	private void handleLoginFailure(Throwable ex) {
		getParentPanel().getElement().<XElement> cast().unmask();
		if (ExceptionUtil.getSpecificCause(ex, failedLoginExceptionFilter) != null) {
			getErrorMessageLabel().setText(LocalizedText.INSTANCE.authenticationFailed());
			/* AlertMessageBox alertMessageBox = new AlertMessageBox(LocalizedText.INSTANCE.signIn(),
			 * LocalizedText.INSTANCE.authenticationFailed()); alertMessageBox.show(); */
		} else {
			getErrorMessageLabel().setText("");
			// ErrorDialog.show(LocalizedText.INSTANCE.loginError1() + "<br><br>" +
			// LocalizedText.INSTANCE.loginError2(), ex);
			ex.printStackTrace();
		}
	}
	
	@Configurable @Required
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	@Configurable @Required
	public void setUserNameTextField(Supplier<? extends ValueBaseField<String>> userNameTextFieldSupplier) {
		this.userNameTextFieldSupplier = userNameTextFieldSupplier;
	}
	
	@Configurable @Required
	public void setPasswordTextField(Supplier<? extends ValueBaseField<String>> passwordTextFieldSupplier) {
		this.passwordTextFieldSupplier = passwordTextFieldSupplier;
	}
	
	@Configurable @Required
	public void setErrorMessageLabel(Supplier<Label> errorMessageLabelSupplier) {
		this.errorMessageLabelSupplier = errorMessageLabelSupplier;
	}
	
	@Configurable @Required
	public void setParentPanel(Supplier<? extends Widget> parentPanelSupplier) {
		this.parentPanelSupplier = parentPanelSupplier;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		userNameTextField = userNameTextFieldSupplier.get();
		handlerRegistrations.add(userNameTextField.addKeyDownHandler(keyDownHandler));
		
		passwordTextField = passwordTextFieldSupplier.get();
		handlerRegistrations.add(passwordTextField.addKeyDownHandler(keyDownHandler));
		
		Element element = null;
		Widget widget = triggerInfo.getWidget();
		if (widget != null)
			element = widget.getElement();
		
		login(element);
	}
	
	/**
	 * this method prepares the login form to behave as normal submit form to store the input elements
	 */
	protected static void prepareForm(FormElement formElement) {
		formElement.setAction("servletpath/login");
		formElement.setMethod("GET");
		formElement.setTarget("loginSubmitProxyFrame");
		Element childElement = formElement.getFirstChildElement();
		if (childElement == null || !childElement.getTagName().equalsIgnoreCase("iframe")) {
			IFrameElement iFrameElement = Document.get().createIFrameElement();
			iFrameElement.setName("loginSubmitProxyFrame");
			iFrameElement.getStyle().setDisplay(Display.NONE);
			formElement.insertFirst(iFrameElement);
		}
	}
	
	protected static FormElement findForm(Element element) {
		while (element != null && !element.getTagName().equalsIgnoreCase("form"))
			element = element.getParentElement(); 
		
		return (FormElement)element;
	}
	
	private Label getErrorMessageLabel() {
		if (errorMessageLabel != null)
			return errorMessageLabel;
		
		errorMessageLabel = errorMessageLabelSupplier.get();
		return errorMessageLabel;
	}
	
	public Widget getParentPanel() {
		if (parentPanel != null)
			return parentPanel;
		
		parentPanel = parentPanelSupplier.get();
		return parentPanel;
	}
	
	@Override
	public void disposeBean() throws Exception {
		super.disposeBean();
		
		for (HandlerRegistration registration : handlerRegistrations)
			registration.removeHandler();
		
		handlerRegistrations.clear();
	}

}

