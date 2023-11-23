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
package com.braintribe.gwt.gme.constellation.client.expert;

import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.google.gwt.user.client.Timer;
import com.google.gwt.xhr.client.XMLHttpRequest;

/**
 * Connection Controller. Check the server connection. Listening server events on Session close and on before Session Timeout
 *
 */
public class ConnectionController implements InitializableBean, DisposableBean  {
	private boolean isServiceUnavailable = false;
	private String servicesUrl;
	private Timer timer = null;
	private boolean doCheck = false;
	/*private Action sessionNotFoundExceptionMessageAction = null;
	private ValidateUserSession validateUserSession = null;
	private PersistenceGmSession gmSession = null;*/
	

	@Configurable
	public void setTribefireServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}

	/*@Required
	public void setPersistenceSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}*/	
	
	@Override
	public void intializeBean() throws Exception {
		setCheck(true);		
	}

	@Override
	public void disposeBean() throws Exception {
		setCheck(false);				
	}
	
	public void setCheck(boolean doCheck) {
		this.doCheck = doCheck;
		prepareServiceChecker();
	}
	
	private void prepareServiceChecker() {
		if (timer == null) {
			if (!doCheck)
				return;
			
		    timer = new Timer() {			
				@Override
				public void run() {
					XMLHttpRequest xhttp = XMLHttpRequest.create();
	
					xhttp.setOnReadyStateChange(xhr -> {
					    if (xhr.getReadyState() == XMLHttpRequest.DONE) {
					    	prepareServicesCheckPanel(xhr.getStatus() == 200);
					    }
					});
									  
					xhttp.open("POST", servicesUrl);
					xhttp.send();												
				}
			};		
		    timer.scheduleRepeating(10000);
		}
		
		if (doCheck) 
			timer.scheduleRepeating(10000);
		else
			timer.cancel();
	}
	
	private void prepareServicesCheckPanel(boolean servicesRunning) {
		if (servicesRunning && isServiceUnavailable) {
			GlobalState.clearState();
			GlobalState.unmask();
			GlobalState.showSuccess(LocalizedText.INSTANCE.servicesAvailable());
			isServiceUnavailable = false;
		} else if (!servicesRunning && !isServiceUnavailable) {
			GlobalState.showWarning(LocalizedText.INSTANCE.servicesNotAvailable(), true, true, false, false, false);
			GlobalState.mask(LocalizedText.INSTANCE.servicesNotAvailable());
			isServiceUnavailable = true;
		}
	}	
	
	/*private void onBeforeSessionTimeout() {
		String message = LocalizedText.INSTANCE.sessionBeforeExpired();
		ConfirmationDialog dialog = NotificationView.showConfirmationDialog(Level.WARNING, message);
		dialog.setCancelButtonVisible(false);
		dialog.setOkButtonVisible(true);
		
		dialog.getConfirmation().andThen(result -> {
			GlobalState.unmask();
			if (validateUserSession == null) {
				validateUserSession = ValidateUserSession.T.create();
				validateUserSession.setSessionId(gmSession.getSessionAuthorization().getSessionId());				
			}
			gmSession.eval(validateUserSession);			
		});		
	}
		
	private void onSessionClose() {
		if (sessionNotFoundExceptionMessageAction != null) {
			sessionNotFoundExceptionMessageAction.perform(null);
			return;
		}		
	}

	public void setSessionNotFoundExceptionMessageAction(Action sessionNotFoundExceptionMessageAction) {
		this.sessionNotFoundExceptionMessageAction = sessionNotFoundExceptionMessageAction;
	}*/
}
