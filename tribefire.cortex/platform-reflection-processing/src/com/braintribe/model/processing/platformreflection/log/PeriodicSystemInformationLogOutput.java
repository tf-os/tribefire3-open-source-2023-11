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
package com.braintribe.model.processing.platformreflection.log;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platformreflection.DeployableInfo;
import com.braintribe.model.platformreflection.HostInfo;
import com.braintribe.model.platformreflection.PlatformReflection;
import com.braintribe.model.platformreflection.SystemInfo;
import com.braintribe.model.platformreflection.TribefireInfo;
import com.braintribe.model.platformreflection.check.java.JavaEnvironment;
import com.braintribe.model.platformreflection.host.Webapp;
import com.braintribe.model.platformreflection.request.ReflectPlatform;
import com.braintribe.model.platformreflection.tf.TribefireServicesInfo;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.thread.api.DeferringThreadContextScoping;

public class PeriodicSystemInformationLogOutput implements Runnable, LifecycleAware {

	private static Logger logger = Logger.getLogger(PeriodicSystemInformationLogOutput.class);

	public static final String ENVIRONMENT_ABOUT_PERIODICLOGOUTPUT = "TRIBEFIRE_ABOUT_PERIODICLOGOUTPUT";
	public static final String ENVIRONMENT_ABOUT_PERIODICLOGOUTPUT_INTERVALMS = "TRIBEFIRE_ABOUT_PERIODICLOGOUTPUT_INTERVALMS";

	protected boolean keepRunning = true;
	protected long intervalInMs = Numbers.MILLISECONDS_PER_HOUR * 2L; // Every 2 hours
	protected LogLevel logLevel = LogLevel.INFO;
	protected int errorCount = 0;
	protected int maxErrorCount = 3;
	protected boolean singleLineOutput = false;

	protected Evaluator<ServiceRequest> requestEvaluator;
	protected DeferringThreadContextScoping threadScoping;
	protected Thread logThread;

	@Override
	public void postConstruct() {
		String logoutputString = TribefireRuntime.getProperty(ENVIRONMENT_ABOUT_PERIODICLOGOUTPUT);
		if (logoutputString != null && logoutputString.equalsIgnoreCase("true")) {

			keepRunning = true;

			Runnable boundRunnable = threadScoping.bindContext(this);
			logThread = Thread.ofVirtual().name("Periodic SysInfo Log Output").start(boundRunnable);

		} else {
			keepRunning = false;
		}
	}

	@Override
	public void preDestroy() {
		keepRunning = false;
		if (logThread != null) {
			try {
				logThread.interrupt();
			} catch (Exception e) {
				logger.error("Could not interrupt thread: " + logThread.getName(), e);
			}
			logThread = null;
		}
	}

	@Override
	public void run() {

		while (keepRunning) {

			this.doLogOutput();

			try {
				Thread.sleep(intervalInMs);
			} catch (InterruptedException e) {
				logger.debug(() -> "About-Logger got interrupted.");
				this.keepRunning = false;
				break;
			}
		}

	}

	protected void doLogOutput() {
		try {
			ReflectPlatform pr = ReflectPlatform.T.create();
			EvalContext<? extends PlatformReflection> eval = pr.eval(requestEvaluator);
			PlatformReflection platform = eval.get();

			if (platform == null) {
				return;
			}
			SystemInfo system = platform.getSystem();
			if (system != null) {
				JavaEnvironment javaEnvironment = system.getJavaEnvironment();
				if (javaEnvironment != null) {
					Map<String, String> systemProperties = javaEnvironment.getSystemProperties();
					if (systemProperties != null) {
						systemProperties.clear();
					}
				}
			}
			HostInfo host = platform.getHost();
			if (host != null) {
				List<Webapp> webapps = host.getWebapps();
				if (webapps != null) {
					webapps.clear();
				}
			}
			TribefireInfo tribefire = platform.getTribefire();
			if (tribefire != null) {
				TribefireServicesInfo servicesInfo = tribefire.getServicesInfo();
				if (servicesInfo != null) {
					List<DeployableInfo> deployables = servicesInfo.getDeployables();
					if (deployables != null) {
						deployables.clear();
					}
				}
			}

			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshall(baos, platform, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid).build());
			String output = baos.toString("UTF-8");

			if (output.trim().length() > 0) {
				if (singleLineOutput) {
					output = output.replace("\n", "").replace("\r", "");
				}
				logger.log(logLevel, output);
			}
			errorCount = 0;
		} catch (Exception e) {
			logger.debug(() -> "Error while trying to log system information.", e);
			errorCount++;
			if (errorCount > maxErrorCount) {
				keepRunning = false;
				logger.info(() -> "The maximum error count of " + maxErrorCount + " has been reached. Terminating the log output here.");
			}
		}
	}

	@Configurable
	public void setIntervalInMs(long intervalInMs) {
		if (intervalInMs > 0) {
			this.intervalInMs = intervalInMs;
		}
	}
	@Configurable
	public void setMaxErrorCount(int maxErrorCount) {
		this.maxErrorCount = maxErrorCount;
	}
	@Configurable
	public void setSingleLineOutput(boolean singleLineOutput) {
		this.singleLineOutput = singleLineOutput;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	@Required
	public void setThreadScoping(DeferringThreadContextScoping threadScoping) {
		this.threadScoping = threadScoping;
	}

}
