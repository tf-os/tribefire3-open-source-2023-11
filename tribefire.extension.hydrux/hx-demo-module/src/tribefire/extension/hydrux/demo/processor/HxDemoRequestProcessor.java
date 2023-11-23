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
package tribefire.extension.hydrux.demo.processor;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.CallbackPushAddressing;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.hydrux.demo.model.api.HxDemoComputeTextLength;
import tribefire.extension.hydrux.demo.model.api.HxDemoRequest;
import tribefire.extension.hydrux.demo.model.api.HxDemoThrowException;
import tribefire.extension.hydrux.demo.model.api.push.HxDemoNotifyProgress;
import tribefire.extension.hydrux.demo.model.api.push.HxDemoSendProcessResult;
import tribefire.extension.hydrux.demo.model.api.push.HxDemoStartComplexProcessOnServer;

/**
 * @author peter.gazdik
 */
public class HxDemoRequestProcessor extends AbstractDispatchingServiceProcessor<HxDemoRequest, Object> {

	private Evaluator<ServiceRequest> evaluator;

	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<HxDemoRequest, Object> dispatching) {
		dispatching.register(HxDemoComputeTextLength.T, this::computeTextLength);
		dispatching.register(HxDemoThrowException.T, this::throwException);
		dispatching.register(HxDemoStartComplexProcessOnServer.T, this::callServer);
	}

	private Integer computeTextLength(@SuppressWarnings("unused") ServiceRequestContext ctx, HxDemoComputeTextLength request) {
		String text = request.getText();

		return text == null ? 0 : text.trim().length();
	}

	@SuppressWarnings("unused")
	private Boolean throwException(ServiceRequestContext ctx, HxDemoThrowException request) {
		throw new RuntimeException("This is the exception you wanted!");
	}

	@SuppressWarnings("unused")
	private Boolean callServer(ServiceRequestContext ctx, HxDemoStartComplexProcessOnServer request) {
		new Thread(new ComplexProcessRun(request)).start();

		return true;
	}

	private class ComplexProcessRun implements Runnable {

		private final CallbackPushAddressing pushAddress;

		public ComplexProcessRun(HxDemoStartComplexProcessOnServer request) {
			this.pushAddress = request.getPushAddress();
		}

		@Override
		public void run() {
			waitMs(700);
			notifyClient("Complex process on server started!");
			waitMs(1000);
			notifyClient("Sit straight!");
			waitMs(1500);
			notifyClient("Don't bite your fingers!");
			waitMs(1500);
			notifyClient("Done!");
			waitMs(1000);
			notifyProcessFinished();
		}

		private void notifyClient(String msg) {
			if (pushAddress == null)
				return;

			HxDemoNotifyProgress request = HxDemoNotifyProgress.T.create();
			request.setMessage(msg);

			send(request);
		}

		private void notifyProcessFinished() {
			HxDemoSendProcessResult request = HxDemoSendProcessResult.T.create();
			request.setResult("ALL DONE!");

			send(request);
		}

		private void send(ServiceRequest notify) {
			PushRequest pr = pushAddress.pushify(notify);
			pr.eval(evaluator).get(null);
		}

		private void waitMs(int millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				throw new RuntimeException("We got interrupted", e);
			}
		}

	}

}
