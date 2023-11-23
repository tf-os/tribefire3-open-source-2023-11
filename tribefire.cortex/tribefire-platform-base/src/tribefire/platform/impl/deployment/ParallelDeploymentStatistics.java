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
package tribefire.platform.impl.deployment;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;

public class ParallelDeploymentStatistics {

	private static AtomicInteger deploymentCounter = new AtomicInteger(0);

	protected StopWatch stopWatch = new StopWatch();
	protected int standardThreadCount = 0;
	protected int deployablesCount = 0;
	protected ConcurrentHashMap<String, PromiseStatistics> deployableStats = new ConcurrentHashMap<>();

	protected long creationTime = 0L;
	protected long deploymentStartTime = 0L;
	protected long waitForFinishedDeploymentStartTime = 0L;
	protected long waitForFinishedDeploymentEndTime = 0L;
	protected long deploymentEndTime = 0L;
	protected int deploymentId = deploymentCounter.incrementAndGet();

	protected TreeMap<Long, List<String>> deployablesByTotalTime;
	protected TreeMap<Long, List<String>> deployablesByExecutionTime;
	protected TreeMap<Long, List<String>> deployablesByStandardEnqueuedTime;
	protected TreeMap<Long, List<String>> deployablesByEagerEnqueuedTime;
	protected TreeMap<Long, List<String>> deployablesByMonitorAcquisitionTime;
	protected double promiseTotalMedianTime;
	protected double promiseExecutionMedianTime;
	protected double promiseStandardEnqueuedMedianTime;
	protected double promiseEagerEnqueuedMedianTime;
	protected double promiseMonitorAcquisitionMedianTime;

	public ParallelDeploymentStatistics() {
		this.creationTime = System.nanoTime();
	}

	public void deploymentStarts() {
		this.deploymentStartTime = System.nanoTime();
	}
	public void waitForFinishedDeploymentStart() {
		waitForFinishedDeploymentStartTime = System.nanoTime();
	}
	public void endOfWaitForFinishedDeploymentStart() {
		waitForFinishedDeploymentEndTime = System.nanoTime();
	}
	public void deploymentFinished() {
		deploymentEndTime = System.nanoTime();
	}

	public PromiseStatistics acquirePromiseStats(Deployable deployable) {
		String externalId = "unknown";
		if (deployable != null && deployable.getExternalId() != null) {
			externalId = deployable.getExternalId();
		}
		return deployableStats.computeIfAbsent(externalId, id -> new PromiseStatistics());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Deployment " + deploymentId + "\n");
		sb.append("Deployables: " + deployablesCount + "\n");
		sb.append("Total deployment time: " + getDuration(creationTime, waitForFinishedDeploymentEndTime) + "\n");
		sb.append("Preparation time: " + getDuration(creationTime, deploymentStartTime) + "\n");
		sb.append("Standard Threadcount: " + standardThreadCount + "\n");
		sb.append("Enqueuing time: " + getDuration(deploymentStartTime, waitForFinishedDeploymentStartTime) + "\n");
		sb.append(
				"Waiting for deployment finished time: " + getDuration(waitForFinishedDeploymentStartTime, waitForFinishedDeploymentEndTime) + "\n");
		sb.append("Teardown time: " + getDuration(waitForFinishedDeploymentEndTime, deploymentEndTime) + "\n");
		sb.append(stopWatch.toString() + "\n\n");

		if (deployablesByTotalTime != null) {
			sb.append("Median total time: " + getDuration(promiseTotalMedianTime) + "\n");
			sb.append("Median execution time: " + getDuration(promiseExecutionMedianTime) + "\n");
			sb.append("Median standard enqueud time: " + getDuration(promiseStandardEnqueuedMedianTime) + "\n");
			sb.append("Median eager enqueud time: " + getDuration(promiseEagerEnqueuedMedianTime) + "\n");
			sb.append("Median monitor acquisition time: " + getDuration(promiseMonitorAcquisitionMedianTime) + "\n");

			sb.append("Deployment total time (top 10): " + getTopTen(deployablesByTotalTime) + "\n");
			sb.append("Deployment execution time (top 10): " + getTopTen(deployablesByExecutionTime) + "\n");
			sb.append("Deployment standard enqueued time (top 10): " + getTopTen(deployablesByStandardEnqueuedTime) + "\n");
			sb.append("Deployment eager enqueued time (top 10): " + getTopTen(deployablesByEagerEnqueuedTime) + "\n");
			sb.append("Deployment monitor acquisition time (top 10): " + getTopTen(deployablesByMonitorAcquisitionTime) + "\n");
		}

		return StringTools.asciiBoxMessage(sb.toString(), -1);
	}

	private String getTopTen(TreeMap<Long, List<String>> deployablesByTime) {
		NavigableMap<Long, List<String>> descendingMap = deployablesByTime.descendingMap();
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (Map.Entry<Long, List<String>> entry : descendingMap.entrySet()) {
			Long timeInNanos = entry.getKey();
			List<String> ids = entry.getValue();
			String timeString = getDuration(timeInNanos.doubleValue());
			for (String id : ids) {
				count++;
				if (count > 10) {
					break;
				}
				sb.append("\n    " + id + " (" + timeString + ")");
			}
			if (count > 10) {
				break;
			}
		}
		return sb.toString();
	}

	private String getDuration(long startInNanos, long endInNanos) {
		long durationInNanos = endInNanos - startInNanos;
		double durationInMs = ((double) durationInNanos) / ((double) Numbers.NANOSECONDS_PER_MILLISECOND);
		return StringTools.prettyPrintMilliseconds(durationInMs, true, ChronoUnit.NANOS);
	}
	private String getDuration(double durationInNanos) {
		double durationInMs = durationInNanos / Numbers.NANOSECONDS_PER_MILLISECOND;
		return StringTools.prettyPrintMilliseconds(durationInMs, true, ChronoUnit.NANOS);
	}

	public void createStatistics() {

		deployablesByTotalTime = new TreeMap<>();
		deployablesByExecutionTime = new TreeMap<>();
		deployablesByStandardEnqueuedTime = new TreeMap<>();
		deployablesByEagerEnqueuedTime = new TreeMap<>();
		deployablesByMonitorAcquisitionTime = new TreeMap<>();

		int size = deployableStats.size();

		List<Long> totalTimes = new ArrayList<>(size);
		List<Long> executionTimes = new ArrayList<>(size);
		List<Long> standardEnqueueingTimes = new ArrayList<>(size);
		List<Long> eagerEnqueueingTimes = new ArrayList<>(size);
		List<Long> monitorTimes = new ArrayList<>(size);

		for (Map.Entry<String, PromiseStatistics> entry : deployableStats.entrySet()) {
			String deployableId = entry.getKey();
			PromiseStatistics promiseStats = entry.getValue();

			addTime(totalTimes, promiseStats.promiseCreationTime, promiseStats.executionEndTime);
			addTime(executionTimes, promiseStats.executionStartTime, promiseStats.executionEndTime);
			if (!promiseStats.eagerlyDeployed) {
				addTime(standardEnqueueingTimes, promiseStats.enqueueStandardTime, promiseStats.executionStartTime);
			} else {
				addTime(eagerEnqueueingTimes, promiseStats.enqueueEagerTime, promiseStats.executionStartTime);
			}
			addTime(monitorTimes, promiseStats.enqueueMonitorAcquisitionTime, promiseStats.enqueueMonitorAcquiredTime);

			addTime(deployablesByTotalTime, deployableId, promiseStats.promiseCreationTime, promiseStats.executionEndTime);
			addTime(deployablesByExecutionTime, deployableId, promiseStats.executionStartTime, promiseStats.executionEndTime);
			if (!promiseStats.eagerlyDeployed) {
				addTime(deployablesByStandardEnqueuedTime, deployableId, promiseStats.enqueueStandardTime, promiseStats.executionStartTime);
			} else {
				addTime(deployablesByEagerEnqueuedTime, deployableId, promiseStats.enqueueEagerTime, promiseStats.executionStartTime);
			}
			addTime(deployablesByMonitorAcquisitionTime, deployableId, promiseStats.enqueueMonitorAcquisitionTime,
					promiseStats.enqueueMonitorAcquiredTime);

		}

		promiseTotalMedianTime = createMedian(totalTimes);
		promiseExecutionMedianTime = createMedian(executionTimes);
		promiseStandardEnqueuedMedianTime = createMedian(standardEnqueueingTimes);
		promiseEagerEnqueuedMedianTime = createMedian(eagerEnqueueingTimes);
		promiseMonitorAcquisitionMedianTime = createMedian(monitorTimes);

	}

	private double createMedian(List<Long> times) {
		if (times.isEmpty())
			return 0d;

		int size = times.size();

		double[] array = new double[size];
		for (int i = 0; i < times.size(); ++i)
			array[i] = times.get(i).doubleValue();

		Arrays.sort(array);

		int halfSize = size / 2;
		if ((size & 1)  == 1)
			return array[halfSize];
		else
			return (array[halfSize-1] + array[halfSize]) / 2;
	}

	private void addTime(List<Long> times, long start, long end) {
		if (start != 0 && end != 0) {
			times.add(end - start);
		}
	}
	private void addTime(TreeMap<Long, List<String>> targetMap, String deployableId, long start, long end) {
		if (start != 0 && end != 0) {
			long duration = end - start;
			List<String> deployablesList = targetMap.computeIfAbsent(duration, d -> new ArrayList<>());
			deployablesList.add(deployableId);
		}
	}

	public class PromiseStatistics {
		private final long promiseCreationTime;
		private long enqueueStandardTime = 0L;
		private long enqueueEagerTime = 0L;
		private long enqueueMonitorAcquisitionTime = 0L;
		private long enqueueMonitorAcquiredTime = 0L;
		private long executionStartTime = 0L;
		private long executionEndTime = 0L;
		private boolean eagerlyDeployed = false;

		public PromiseStatistics() {
			this.promiseCreationTime = System.nanoTime();
		}
		public void executionStarts() {
			this.executionStartTime = System.nanoTime();
		}
		public void executionEnded() {
			this.executionEndTime = System.nanoTime();
		}
		public void enqueuedStandard() {
			enqueueStandardTime = System.nanoTime();
		}
		public void enqueuedEager() {
			enqueueEagerTime = System.nanoTime();
		}
		public void eagerMonitorAcquisition() {
			enqueueMonitorAcquisitionTime = System.nanoTime();
			eagerlyDeployed = true;
		}
		public void eagerMonitorAcquired() {
			enqueueMonitorAcquiredTime = System.nanoTime();
		}
	}

}
