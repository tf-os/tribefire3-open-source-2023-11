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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

/**
 * This expert will listen for manipulations done in the PD session, and will forward them all to the parent explorer.
 * 
 * @author michel.docouto
 */
public class ManipulationListenerAndForwarder implements InitializableBean, DisposableBean, Consumer<Object> {
	
	private PersistenceGmSession session;
	private Object externalSession;
	private Timer manipulationListenerTimer;
	private ManipulationListener manipulationListener;
	private Set<Manipulation> manipulationsAlreadyForwarded = new HashSet<>();
	private JavaScriptObject jsUtilExplorer;
	private JavaScriptObject jsUtilPD;
	
	@Required
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	@Override
	public void intializeBean() throws Exception {
		manipulationListenerTimer = new Timer() {
			@Override
			public void run() {
				forwardNewManipulations();
			}
		};
		
		manipulationListener = manipulation -> manipulationListenerTimer.schedule(1000);
		session.listeners().add(manipulationListener);
	}
	
	@Override
	public void accept(Object session) {
		externalSession = session;
	}
	
	@Override
	public void disposeBean() throws Exception {
		session.listeners().remove(manipulationListener);
	}
	
	private void forwardNewManipulations() {
		List<Manipulation> manipulationsDone = new ArrayList<>(session.getTransaction().getManipulationsDone());
		if (manipulationsDone.isEmpty())
			return;
		
		manipulationsDone.removeAll(manipulationsAlreadyForwarded);
		manipulationsAlreadyForwarded.addAll(manipulationsDone);
		CompoundManipulation cm = (CompoundManipulation) ManipulationRemotifier.remotify(CompoundManipulation.create(manipulationsDone));
		Object preliminaryReferencesList = sendManipulationsToGME(getJsUtilExplorer(), getJsUtilPD(), cm.getCompoundManipulationList(), externalSession);

		List<InstantiationManipulation> instantiationManipulationsDone = fillInstantiationManipulations(manipulationsDone);
		for (int i = 0; i < getSize(preliminaryReferencesList); i++) {
			Object externalReference = getAtIndex(preliminaryReferencesList, i);
			String externalTypeSignature = getRefTypeSignature(externalReference);
			Object externalRefId = getRefId(externalReference);
			// We are filling the map references - preliminary ids here are different then the ones in the external
			// session. This is used when selecting a preliminary entity.
			for (InstantiationManipulation instantiationManipulation : new ArrayList<>(instantiationManipulationsDone)) {
				GenericEntity entity = instantiationManipulation.getEntity();
				if (entity.entityType().getTypeSignature().equals(externalTypeSignature)) {
					fillMapReferences(externalTypeSignature, entity.reference().getRefId(), externalRefId, getJsUtilPD());
					instantiationManipulationsDone.remove(instantiationManipulation);
					break;
				}
			}
		}
	}
	
	private List<InstantiationManipulation> fillInstantiationManipulations(List<Manipulation> manipulationsDone) {
		List<InstantiationManipulation> instantiationManipulations = new ArrayList<>();
		manipulationsDone.forEach(mani -> {
			List<InstantiationManipulation> list = fillInstantiationManipulations(mani);
			instantiationManipulations.addAll(list);
		});
		
		return instantiationManipulations;
	}
	
	private List<InstantiationManipulation> fillInstantiationManipulations(Manipulation mani) {
		if (mani instanceof InstantiationManipulation)
			return Arrays.asList((InstantiationManipulation) mani);
		else if (mani instanceof CompoundManipulation) {
			List<InstantiationManipulation> instantiationManipulations = new ArrayList<>();
			((CompoundManipulation) mani).getCompoundManipulationList().forEach(compoundMani -> {
				List<InstantiationManipulation> list = fillInstantiationManipulations(compoundMani);
				instantiationManipulations.addAll(list);
			});
			return instantiationManipulations;
		}
		
		return Collections.emptyList();
	}
	
	private JavaScriptObject getJsUtilExplorer() {
		if (jsUtilExplorer != null)
			return jsUtilExplorer;
		
		jsUtilExplorer = getNativeJsUtilExplorer();
		return jsUtilExplorer;
	}
	
	private JavaScriptObject getJsUtilPD() {
		if (jsUtilPD != null)
			return jsUtilPD;
		
		jsUtilPD = getNativeJsUtilPD();
		return jsUtilPD;
	}
	
	private native JavaScriptObject getNativeJsUtilExplorer() /*-{
		return new $wnd.$tf.util.JsUtil();
	}-*/;
	
	private native JavaScriptObject getNativeJsUtilPD() /*-{
		return new $wnd.$pd.util.JsUtil();
	}-*/;
	
	//Returns a List<PreliminaryEntityReference>
	private native Object sendManipulationsToGME(JavaScriptObject jsUtilExplorer, JavaScriptObject jsUtilPD, List<Manipulation> manipulations, Object externalSession) /*-{
		var encodedManis = jsUtilPD.encodeData(manipulations);
		return jsUtilExplorer.decodeAndApplyManipulations(encodedManis, externalSession);
	}-*/;
	
	private native int getSize(Object list) /*-{
		return list.size();
	}-*/;
	
	private native Object getAtIndex(Object list, int index) /*-{
		return list.getAtIndex(index);
	}-*/;
	
	private native String getRefTypeSignature(Object externalReference) /*-{
		return externalReference.typeSignature;
	}-*/;
	
	private native Object getRefId(Object externalReference) /*-{
		return externalReference.refId;
	}-*/;
	
	private native void fillMapReferences(String typeSignature, Object refId, Object externalRefId, JavaScriptObject jsUtilPD) /*-{
		jsUtilPD.putReferences(typeSignature, refId, externalRefId);
	}-*/;

}
