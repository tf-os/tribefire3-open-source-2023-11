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
package com.braintribe.model.processing.check.hw;

import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.check.service.hw.MemoryCheck;
import com.braintribe.model.check.service.hw.MemoryThresholdValues;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.utils.StringTools;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryCheckProcessor implements ParameterizedAccessCheckProcessor<MemoryCheck> {

	private static Logger logger = Logger.getLogger(MemoryCheckProcessor.class);

	protected String globalMemoryAvailableWarnThreshold = "10%";
	protected String globalMemoryAvailableFailThreshold = "5%";
	protected String swapAvailableWarnThreshold = "10%";
	protected String swapAvailableFailThreshold = "5%";
	protected String javaMemoryAvailableWarnThreshold = "10%";
	protected String javaMemoryAvailableFailThreshold = "5%";

	@Override
	public CheckResult check(AccessRequestContext<MemoryCheck> requestContext) {

		MemoryCheck request = requestContext.getRequest();
		MemoryThresholdValues globalMemoryThresholds = request.getGlobalMemoryThresholds();
		MemoryThresholdValues swapMemoryThresholds = request.getSwapMemoryThresholds();
		MemoryThresholdValues javaMemoryThresholds = request.getJavaMemoryThresholds();

		CheckResult result = CheckResult.T.create();
		List<CheckResultEntry> entries = result.getEntries();

		collectGlobalMemoryResults(entries, globalMemoryThresholds, swapMemoryThresholds);
		collectJavaMemoryResults(entries, javaMemoryThresholds);

		return result;
	}

	protected void collectJavaMemoryResults(List<CheckResultEntry> entries, MemoryThresholdValues javaMemoryThresholds) {

		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory();
		long maxMemory = runtime.maxMemory();
		long usedMemory = totalMemory - runtime.freeMemory();
		long freeMemory = maxMemory - usedMemory;

		checkMemoryLimits(entries, maxMemory, freeMemory, javaMemoryAvailableWarnThreshold, javaMemoryAvailableFailThreshold, javaMemoryThresholds, "Java");
	}
	
	

	protected void collectGlobalMemoryResults(List<CheckResultEntry> entries, MemoryThresholdValues globalMemoryThresholds, MemoryThresholdValues swapMemoryThresholds) {

		SystemInfo si = new SystemInfo();
		GlobalMemory memory = si.getHardware().getMemory();

		long available = memory.getAvailable();
		long total = memory.getTotal();
		VirtualMemory virtualMemory = memory.getVirtualMemory();
		long swapTotal = virtualMemory.getSwapTotal();
		long swapUsed = virtualMemory.getSwapUsed();
		long swapAvailable = swapTotal-swapUsed;

		checkMemoryLimits(entries, total, available, globalMemoryAvailableWarnThreshold, globalMemoryAvailableFailThreshold, globalMemoryThresholds, "Global");
		if (swapTotal > 0) {
			checkMemoryLimits(entries, swapTotal, swapAvailable, swapAvailableWarnThreshold, swapAvailableFailThreshold, swapMemoryThresholds, "Swap");
		}
		
	}
	
	protected static void checkMemoryLimits(List<CheckResultEntry> entries, 
			long total, long available, 
			String configuredWarnThreshold, String configuredFailThreshold,
			MemoryThresholdValues memoryThresholds,
			String descriptionPrefix) {
		
		CheckStatus jvmStatus = CheckStatus.ok;
		if (total <= 0) {
			jvmStatus = CheckStatus.fail;
		}
		
		String warnThresholdString = choose(configuredWarnThreshold, memoryThresholds != null ? memoryThresholds.getAvailableWarnThreshold() : null, Numbers.MEGABYTE*50);
		String failThresholdString = choose(configuredFailThreshold, memoryThresholds != null ? memoryThresholds.getAvailableFailThreshold() : null, Numbers.MEGABYTE*5);
		
		long warnThreshold =  getMemoryValue(configuredWarnThreshold, total, Numbers.MEGABYTE*50);
		long errorThreshold = getMemoryValue(configuredFailThreshold, total, Numbers.MEGABYTE*5);

		entries.add(createEntry(descriptionPrefix.toLowerCase()+"MemoryTotal", descriptionPrefix+" Memory Total", ""+total, jvmStatus));

		if (memoryThresholds != null) {
			warnThreshold = getMemoryValue(memoryThresholds.getAvailableWarnThreshold(), total, warnThreshold);
			errorThreshold = getMemoryValue(memoryThresholds.getAvailableFailThreshold(), total, errorThreshold);
		}

		CheckStatus availableStatus = CheckStatus.ok;
		if (available < warnThreshold) {
			availableStatus = CheckStatus.warn;
		}
		if (available < errorThreshold) {
			availableStatus = CheckStatus.fail;
		}
		entries.add(createEntry(descriptionPrefix.toLowerCase()+"MemoryAvailable", descriptionPrefix+" Memory Available (warning threshold: "+warnThresholdString+", fail threshold: "+failThresholdString+")", ""+available, availableStatus));
		
	}

	protected static String choose(String configuredThreshold, String parameterThreshold, long defaultValue) {
		if (!StringTools.isEmpty(parameterThreshold)) {
			return parameterThreshold;
		}
		if (!StringTools.isEmpty(configuredThreshold)) {
			return configuredThreshold;
		}
		return StringTools.prettyPrintBytes(defaultValue);
	}

	protected static long getMemoryValue(String valueString, long total, long defaultValue) {
		if (valueString == null) {
			return defaultValue;
		}
		valueString = valueString.trim();
		if (valueString.length() == 0) {
			return defaultValue;
		}
		if (valueString.endsWith("%")) {
			String percentageString = valueString.substring(0,  valueString.length()-1);
			try {
				double percentage = Double.parseDouble(percentageString);
				percentage = percentage / 100;
				return (long) (total * percentage);
			} catch(Exception e) {
				logger.error("Could not parse percentage "+percentageString, e);
				return defaultValue;
			}
		} else {
			try {
				long result = StringTools.parseBytesString(valueString);
				return result;
			} catch(Exception e) {
				logger.error("Could not parse value "+valueString, e);
				return defaultValue;
			}
		}
	}

	protected static CheckResultEntry createEntry(String name, String details, String msg, CheckStatus cs) {
		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName(name);
		entry.setDetails(details);
		entry.setMessage(msg);
		entry.setCheckStatus(cs);
		return entry;
	}

	@Configurable
	public void setGlobalMemoryAvailableWarnThreshold(String globalMemoryAvailableWarnThreshold) {
		if (globalMemoryAvailableWarnThreshold != null) {
			this.globalMemoryAvailableWarnThreshold = globalMemoryAvailableWarnThreshold;
		}
	}
	@Configurable
	public void setGlobalMemoryAvailableFailThreshold(String globalMemoryAvailableFailThreshold) {
		if (globalMemoryAvailableFailThreshold != null) {
			this.globalMemoryAvailableFailThreshold = globalMemoryAvailableFailThreshold;
		}
	}
	@Configurable
	public void setSwapAvailableWarnThreshold(String swapAvailableWarnThreshold) {
		if (swapAvailableWarnThreshold != null) {
			this.swapAvailableWarnThreshold = swapAvailableWarnThreshold;
		}
	}
	@Configurable
	public void setSwapAvailableFailThreshold(String swapAvailableFailThreshold) {
		if (swapAvailableFailThreshold != null) {
			this.swapAvailableFailThreshold = swapAvailableFailThreshold;
		}
	}
	@Configurable
	public void setJavaMemoryAvailableWarnThreshold(String javaMemoryAvailableWarnThreshold) {
		if (javaMemoryAvailableWarnThreshold != null) {
			this.javaMemoryAvailableWarnThreshold = javaMemoryAvailableWarnThreshold;
		}
	}
	@Configurable
	public void setJavaMemoryAvailableFailThreshold(String javaMemoryAvailableFailThreshold) {
		if (javaMemoryAvailableFailThreshold != null) {
			this.javaMemoryAvailableFailThreshold = javaMemoryAvailableFailThreshold;
		}
	}

}
