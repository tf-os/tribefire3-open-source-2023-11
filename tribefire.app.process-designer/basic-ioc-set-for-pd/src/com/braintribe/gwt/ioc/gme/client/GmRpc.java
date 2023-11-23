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
package com.braintribe.gwt.ioc.gme.client;

import java.util.function.Supplier;

import com.braintribe.gwt.gmrpc.web.client.GwtGmWebRpcEvaluator;
import com.braintribe.gwt.gmsession.client.CortexTypeEnsurer;
import com.braintribe.provider.SingletonBeanProvider;

public class GmRpc {
	
	protected static Supplier<CortexTypeEnsurer> typeEnsurer = new SingletonBeanProvider<CortexTypeEnsurer>() {
		@Override
		public CortexTypeEnsurer create() throws Exception {
			CortexTypeEnsurer bean = publish(new CortexTypeEnsurer());
			bean.setEvaluator(serviceRequestEvaluator.get());
			return bean;
		}
	};
	
	protected static Supplier<GwtGmWebRpcEvaluator> serviceRequestEvaluator = new SingletonBeanProvider<GwtGmWebRpcEvaluator>() {
		@Override
		public GwtGmWebRpcEvaluator create() throws Exception {
			GwtGmWebRpcEvaluator bean = publish(new GwtGmWebRpcEvaluator());
			bean.setServerUrl(serverUrl.get());
			bean.setTypeEnsurer(typeEnsurer.get());
			bean.setSessionIdProvider(Providers.sessionIdProvider.get());
			return bean;
		}
	};
	
	private static Supplier<String> serverUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return Runtime.tribefireServicesUrl.get() + "rpc";
		}
	};

}
