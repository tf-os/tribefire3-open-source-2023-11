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
package com.braintribe.utils.lcd;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Date;
import java.util.List;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.utils.DateTools;

/**
 * <p>
 * A helper class to perform time measurements.
 * </p>
 * <b>Example:</b>
 *
 * <pre>
 * StopWatch stopWatch = new StopWatch();
 * // compute something
 * long elapsedTimeInMs = stopWatch.getElapsedTime();
 * System.out.println(&quot;The computation took &quot; + elapsedTimeInMs + &quot;ms&quot;);
 * </pre>
 *
 *
 * @author michael.lafite
 */
public class StopWatch {

	private final String name;

	private boolean automaticResetEnabled = false;

	private long startTime;

	private long lastElapsedTime;

	private long lastIntermediateResult;
	private final List<Pair<Long, String>> intermediateResults = newList();

	private long lastPauseStart = -1;
	private long allPauses;
	private long intermediatePauses;

	/** Equivalent to {@code new StopWatch(null)} */
	public StopWatch() {
		this(null);
	}

	/**
	 * Creates a new instance of this class and sets the start time. The name of the instance (which is printed by {@link #toString()}) is assigned to
	 * passed name. If the name is null, simple name of this class is used as default.
	 */
	public StopWatch(String name) {
		this.name = name;

		reset();
	}

	public StopWatch pause() {
		if (lastPauseStart < 0) {
			lastPauseStart = now();
		}
		return this;
	}

	public void resume() {
		if (lastPauseStart < 0) {
			return;
		}

		long pause = msSince(lastPauseStart);
		allPauses += pause;
		intermediatePauses += msSince(lastPauseStart);
		lastPauseStart = -1;
	}

	public void intermediate(String description) {
		checkpoint(description);
	}

	/** Just like {@link #intermediate(String)}, but returns the corresponding time in ms. */
	public long checkpoint(String description) {
		long now = now();
		long timeSinceStart = lastIntermediateResult != -1 ? now - lastIntermediateResult - intermediatePauses : now - startTime - allPauses;
		lastIntermediateResult = now;
		intermediatePauses = 0;

		intermediateResults.add(new Pair<>(timeSinceStart, description));
		return timeSinceStart;
	}

	@Override
	public String toString() {
		long elapsedMs = getElapsedTime();
		Date started = new Date(startTime);
		String startedString = DateTools.encode(started, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
		String elapsedString = StringTools.prettyPrintMilliseconds(elapsedMs, true);

		StringBuilder sb = new StringBuilder(name());
		sb.append("[Start:");
		sb.append(startedString);
		sb.append(",runs:");
		sb.append(elapsedString);
		sb.append('(');
		sb.append(elapsedMs);
		sb.append(" ms)");
		if (intermediateResults != null) {
			for (Pair<Long, String> i : intermediateResults) {
				sb.append(",");
				sb.append(i.second());
				sb.append(":");
				sb.append(StringTools.prettyPrintMilliseconds(i.first(), true));
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private String name() {
		return NullSafe.get(name, getClass().getSimpleName());
	}

	/**
	 * Prints only the overall time with the intermediates, e.g. <tt>1s [A: 100 ms, B: 900 ms]</tt> or if name was
	 * <tt>[GIVEN NAME] 1s [A: 100 ms, B: 900 ms]</tt>.
	 */
	public String getElapsedTimesReport() {
		long elapsedMs = msSince(startTime);
		String elapsedString = StringTools.prettyPrintMilliseconds(elapsedMs, true);

		StringBuilder sb = new StringBuilder();
		if (name != null) {
			sb.append('[');
			sb.append(name);
			sb.append("] ");
		}
		sb.append(elapsedString);
		if (!intermediateResults.isEmpty()) {
			sb.append(" [");
			for (Pair<Long, String> i : intermediateResults) {
				sb.append(i.second());
				sb.append(":");
				sb.append(StringTools.prettyPrintMilliseconds(i.first(), true));
				sb.append(",");
			}
			sb.setCharAt(sb.length() - 1, ']');
		}
		return sb.toString();
	}

	/**
	 * Returns the same result as method {@link #getElapsedTime()} returned when it was invoked the last time. If it hasn't been invoked yet, this
	 * method invokes it. A {@link #reset() reset} does not affect the result of this method.
	 */
	public long getLastElapsedTime() {
		if (lastElapsedTime == -1) {
			getElapsedTime();
		}

		return lastElapsedTime;
	}

	/** @see #getElapsedTime() */
	public long getElapsedTimeInMinutes() {
		return getElapsedTimeInSeconds() / Numbers.SECONDS_PER_MINUTE;
	}

	/** @see #getElapsedTime() */
	public long getElapsedTimeInSeconds() {
		return getElapsedTime() / Numbers.THOUSAND;
	}

	public String getElapsedTimePretty() {
		return StringTools.prettyPrintMilliseconds(getElapsedTime(), true);
	}

	/** Gets the elapsed time in milliseconds since the instance was created or reset. */
	public long getElapsedTime() {
		long end = lastPauseStart < 0 ? now() : lastPauseStart;
		long result = end - startTime - allPauses;

		if (automaticResetEnabled)
			reset();

		return lastElapsedTime = result;
	}

	/** Resets the start time. */
	public final void reset() {
		startTime = now();

		lastElapsedTime = -1;

		lastIntermediateResult = -1;
		intermediateResults.clear();

		lastPauseStart = -1;
		allPauses = 0;
		intermediatePauses = 0;
	}

	public boolean isAutomaticResetEnabled() {
		return automaticResetEnabled;
	}

	/** If enabled, the start time will be reset when getting the elapsed time. */
	public void setAutomaticResetEnabled(final boolean automaticResetEnabled) {
		this.automaticResetEnabled = automaticResetEnabled;
	}

	private long now() {
		return System.currentTimeMillis();
	}

	private long msSince(long startTime) {
		return now() - startTime;
	}

}
