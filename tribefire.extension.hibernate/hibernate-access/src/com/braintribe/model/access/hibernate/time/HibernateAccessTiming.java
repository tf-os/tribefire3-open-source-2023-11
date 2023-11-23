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
package com.braintribe.model.access.hibernate.time;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.query.internal.QueryImpl;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.access.hibernate.HibernateAccess;
import com.braintribe.model.access.hibernate.HibernateApplyStatistics;
import com.braintribe.model.accessdeployment.hibernate.HibernateLogging;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.logging.LogLevels;

/**
 * This class is used by the HibernateAccess to print timing information into the log. If a certain time threshold is
 * exceeded, the log output will be printed with WARN level. Otherwise, a short DEBUG message or, if activated, a
 * verbose TRACE message will be printed.
 */
public class HibernateAccessTiming {

	private static int MAX_MANIPULATIONS_RECORDED = 25;

	// This is not a copy/paste error. We want to log in the name of the HibernateAccess
	protected static Logger logger = Logger.getLogger(HibernateAccess.class);
	protected static Logger myLogger = Logger.getLogger(HibernateAccessTiming.class);

	protected HibernateLogging logging;
	protected ActionType type;
	protected org.hibernate.query.Query<?> hqlQuery;
	protected com.braintribe.model.query.Query gmQuery;
	protected long initialized = System.currentTimeMillis();
	protected long sessionAvailable;
	protected long startProcessing;
	protected long stopProcessing;
	protected long resultAvailable;
	protected long warnThreshold;
	protected long debugThreshold;
	protected SessionFactory hibernateSessionFactory;

	protected List<HibernateTimingEvent> manipulationEvents = new ArrayList<>(MAX_MANIPULATIONS_RECORDED + 1);
	int manipulationEventsCounter = 0;

	private HibernateApplyStatistics statistics;

	public enum ActionType {
		SelectQuery,
		EntityQuery,
		PropertyQuery,
		NativeHqlQuery,
		ApplyManipulation;

		public boolean isReadOnly() {
			return this != ApplyManipulation;
		}
	}

	public HibernateAccessTiming(ActionType _type, long _warnThreshold, long _debugThreshold, HibernateLogging _logging) {
		this.warnThreshold = _warnThreshold;
		this.debugThreshold = _debugThreshold;
		this.type = _type;
		this.logging = _logging;
	}

	public void setQueryInformation(SessionFactory _hibernateSessionFactory, com.braintribe.model.query.Query _gmQuery,
			org.hibernate.query.Query<?> _hqlQuery) {
		this.hibernateSessionFactory = _hibernateSessionFactory;
		this.gmQuery = _gmQuery;
		this.hqlQuery = _hqlQuery;
	}

	public void acquiredSession() {
		this.sessionAvailable = System.currentTimeMillis();
	}

	public void processingStarts() {
		this.startProcessing = System.currentTimeMillis();
	}

	public void processingStopped() {
		this.stopProcessing = System.currentTimeMillis();
	}

	public void resultAvailable() {
		this.resultAvailable = System.currentTimeMillis();
	}

	private boolean warnThresholdExceeded(long totalDurationMs) {
		if ((this.warnThreshold >= 0) && (totalDurationMs > this.warnThreshold)) {
			// Ok we are over the warning theshold. However, there might have been just many manipulations
			if (manipulationEventsCounter == 0) {
				return true;
			} else {
				double durationPerManipulation = ((double) totalDurationMs) / ((double) manipulationEventsCounter);
				if (durationPerManipulation > 1d) {
					// One manipulation took more than a millisecond (on the average)
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	public void logTimingInformation() {

		long now = System.currentTimeMillis();
		boolean verbose = false;
		LogLevel verboseLogLevel = LogLevel.TRACE;
		boolean debugOutput = true;

		long totalDuration = now - initialized;

		if (logging != null) {
			StringBuilder sb = new StringBuilder();
			if (logging.getLogTimings()) {
				enrichLogTimings(now, totalDuration, sb);
			}
			if (logging.getLogGMStatements()) {
				enrichGMStatements(sb);
			}
			if (logging.getLogHQLStatements()) {
				enrichHQLStatements(sb);
			}
			if (logging.getLogSQLStatements()) {
				enrichSQLStatments(sb, logging.getEnrichSQLParameters());
			}
			if (logging.getLogStatistics()) {
				enrichStatistics(sb);
			}
			logger.log(LogLevels.convert(logging.getLogLevel()), sb.toString().trim());
		}

		if (warnThresholdExceeded(totalDuration)) {
			verboseLogLevel = LogLevel.WARN;
			verbose = true;
			debugOutput = false;
		} else if (!logger.isDebugEnabled()) {
			// No warning necessary. If Debug is not enabled we can stop at this point
			return;
		} else if (logger.isTraceEnabled()) {
			verbose = true;
			debugOutput = false;
		}

		if (verbose) {
			StringBuilder sb = new StringBuilder();
			enrichLogTimings(now, totalDuration, sb);
			enrichGMStatements(sb);
			enrichHQLStatements(sb);
			enrichSQLStatments(sb, false);
			enrichStatistics(sb);
			if (!manipulationEvents.isEmpty()) {
				String events = manipulationEvents.stream() //
						.map(HibernateTimingEvent::toString) //
						.collect(Collectors.joining("\n"));
				if (manipulationEvents.size() < manipulationEventsCounter) {
					events += "\n+ " + (manipulationEventsCounter - manipulationEvents.size()) + " more";
				}

				sb.append("Manipulations:\n");
				sb.append(StringTools.asciiBoxMessage(events));
				sb.append('\n');
			}
			logger.log(verboseLogLevel, sb.toString());
		}

		if (debugOutput) {
			if (totalDuration > debugThreshold || logger.isTraceEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Processing (");
				sb.append(this.type);
				sb.append("): Total time: ");
				sb.append(totalDuration);
				sb.append(" ms, Session acquisition: ");
				if (this.sessionAvailable > 0) {
					sb.append((this.sessionAvailable - this.initialized));
					sb.append(" ms");
				} else {
					sb.append("n/a");
				}
				sb.append(" ms, Processing time: ");
				if (this.startProcessing > 0 && this.stopProcessing > 0) {
					sb.append((this.stopProcessing - this.startProcessing));
					sb.append(" ms");
				} else {
					sb.append("n/a");
				}
				if (this.gmQuery != null) {
					try {
						String stringified = BasicQueryStringifier.create().stringify(this.gmQuery);
						sb.append(", GM Query: ");
						sb.append(stringified);
					} catch (Exception e) {
						myLogger.debug("Could not stringify query.", e);
					}
				}
				if (this.hqlQuery != null) {
					try {
						String hqlQueryString = this.hqlQuery.getQueryString();
						sb.append(", HQL Query: ");
						sb.append(hqlQueryString);
					} catch (Exception e) {
						myLogger.debug("Could not stringify HQL query.", e);
					}
				}
				// This output spams the log, so if the timing is below a threshold, we resort to trace putput
				if (totalDuration > debugThreshold) {
					logger.debug(sb.toString());
				} else {
					logger.trace(sb.toString());
				}
			}
		}

	}

	// **************************************************************************
	// Helpers
	// **************************************************************************

	private void enrichStatistics(StringBuilder sb) {
		if (statistics != null) {
			sb.append("Statistics:          ");
			sb.append(statistics.toString());
			sb.append('\n');
		}
	}

	private void enrichGMStatements(StringBuilder sb) {
		if (this.gmQuery != null) {
			try {
				String stringified = BasicQueryStringifier.create().stringify(this.gmQuery);
				sb.append("GM Query:            ");
				sb.append(stringified);
				sb.append('\n');
			} catch (Exception e) {
				myLogger.debug("Could not stringify query.", e);
			}
		}
	}

	private void enrichHQLStatements(StringBuilder sb) {
		if (this.hqlQuery != null) {
			try {
				String hqlQueryString = this.hqlQuery.getQueryString();
				sb.append("HQL Query:           ");
				sb.append(hqlQueryString);
				sb.append('\n');
			} catch (Exception e) {
				myLogger.debug("Could not stringify HQL query.", e);
			}
		}
	}

	private void enrichSQLStatments(StringBuilder sb, boolean enrichSQLParameters) {
		if (this.hqlQuery != null) {
			try {
				String sqlQuery = this.getSqlFromHqlQuery(enrichSQLParameters);
				sb.append("SQL Query:           ");
				sb.append(sqlQuery);
				sb.append('\n');
			} catch (Exception e) {
				myLogger.debug("Could not convert HQL query to SQL.", e);
			}
		}
	}

	private void enrichLogTimings(long now, long totalDuration, StringBuilder sb) {
		sb.append("Processing (");
		sb.append(this.type);
		sb.append("):\n");
		sb.append("Total time:          ");
		String totalDurationString = "" + totalDuration;
		int dataLength = totalDurationString.length();
		sb.append(totalDurationString);
		sb.append(" ms\n");
		sb.append("Preparation time:    ");
		if (this.startProcessing > 0) {
			sb.append(StringTools.extendStringInFront("" + (this.startProcessing - this.initialized), ' ', dataLength));
			sb.append(" ms\n");
		} else {
			sb.append(StringTools.extendStringInFront("n/a\n", ' ', dataLength));
		}
		sb.append("Session acquistion:  ");
		if (this.sessionAvailable > 0) {
			sb.append(StringTools.extendStringInFront("" + (this.sessionAvailable - this.initialized), ' ', dataLength));
			sb.append(" ms\n");
		} else {
			sb.append(StringTools.extendStringInFront("n/a\n", ' ', dataLength));
		}
		sb.append("Processing time:     ");
		if (this.startProcessing > 0 && this.stopProcessing > 0) {
			sb.append(StringTools.extendStringInFront("" + (this.stopProcessing - this.startProcessing), ' ', dataLength));
			sb.append(" ms\n");
		} else {
			sb.append(StringTools.extendStringInFront("n/a\n", ' ', dataLength));
		}
		sb.append("Postprocessing time: ");
		if (this.stopProcessing > 0) {
			sb.append(StringTools.extendStringInFront("" + (now - this.stopProcessing), ' ', dataLength));
			sb.append(" ms\n");
		} else {
			sb.append(StringTools.extendStringInFront("n/a\n", ' ', dataLength));
		}
	}

	private static final HqlLogSilencer hqlLogSilencer = new HqlLogSilencer();

	private String getSqlFromHqlQuery(boolean enrichSQLParameters) {
		if (hqlQuery == null || hibernateSessionFactory == null)
			return "n/a";

		hqlLogSilencer.mute();
		try {
			return tryGetSqlFromHqlQuery(enrichSQLParameters);

		} finally {
			hqlLogSilencer.unmute();
		}
	}

	// Sometimes Hibernate tends to log errors internally, even 10 stack-traces for a single query.
	/* E.g.. org.hibernate.hql.internal.ast.ErrorTracker ' Unknown entity: com.braintribe.model.process.Process
	 * [cause=org.hibernate.MappingException: Unknown entity: com.braintribe.model.process.Process]' */
	private static class HqlLogSilencer {
		private final java.util.logging.Logger hibLog = java.util.logging.Logger.getLogger("org.hibernate.hql.internal");
		private final Level configuredLevel = hibLog.getLevel();
		private ReentrantLock lock = new ReentrantLock();

		private int count = 0;

		public void mute() {
			lock.lock();
			try {
				if (count++ == 0)
					hibLog.setLevel(Level.OFF);
			} finally {
				lock.unlock();
			}
		}

		public void unmute() {
			lock.lock();
			try {
				if (--count == 0)
					hibLog.setLevel(configuredLevel);
			} finally {
				lock.unlock();
			}
		}
	}

	private String tryGetSqlFromHqlQuery(boolean enrichSQLParameters) {
		try {
			final QueryTranslatorFactory ast = new ASTQueryTranslatorFactory();
			final QueryTranslatorImpl newQueryTranslator = (QueryTranslatorImpl) ast.createQueryTranslator("", this.hqlQuery.getQueryString(),
					Collections.EMPTY_MAP, (SessionFactoryImplementor) this.hibernateSessionFactory, null);
			newQueryTranslator.compile(null, false);
			String sql = newQueryTranslator.getSQLString();

			Map<String, List<String>> parameters = new HashMap<>();
			if (enrichSQLParameters) {
				Dialect dialect = null;
				if (hibernateSessionFactory instanceof SessionFactoryImplementor) {
					SessionFactoryImplementor hibernateSessionFactoryImpl = (SessionFactoryImplementor) hibernateSessionFactory;
					dialect = hibernateSessionFactoryImpl.getJdbcServices().getDialect();
				}

				TreeMap<ParameterKey, String> params = new TreeMap<>();
				if (this.hqlQuery instanceof QueryImpl) {
					QueryImpl<?> queryImpl = (QueryImpl<?>) hqlQuery;
					QueryParameters queryParameters = queryImpl.getQueryParameters();
					Map<String, TypedValue> namedParameters = queryParameters.getNamedParameters();

					for (Map.Entry<String, TypedValue> namedParameter : namedParameters.entrySet()) {
						TypedValue typedValue = namedParameter.getValue();
						Type type = typedValue.getType();

						String objectToSQLString = null;
						if (type instanceof LiteralType) {
							LiteralType<Object> literalType = (LiteralType<Object>) type;
							try {
								objectToSQLString = literalType.objectToSQLString(typedValue.getValue(), dialect);
							} catch (Exception e) {
								throw Exceptions.unchecked(e,
										"Could not get SQL string from '" + typedValue.getValue() + "' using dialect: '" + dialect + "'");
							}
						}

						ParameterKey parameterKey = new ParameterKey(namedParameter.getKey());
						params.put(parameterKey, objectToSQLString);
					}
				}
				params.forEach((k, v) -> {
					if (k.parameterIndex == -1) {
						parameters.put(k.parameterName, asList(v));
					} else {
						if (parameters.containsKey(k.parameterName)) {
							parameters.get(k.parameterName).add(v);
						} else {
							parameters.put(k.parameterName, asList(v));
						}
					}
				});
			}

			final List<ParameterSpecification> parameterSpecifications = newQueryTranslator.getCollectedParameterSpecifications();
			if (!CollectionTools.isEmpty(parameterSpecifications)) {
				for (ParameterSpecification parameter : parameterSpecifications) {
					if (parameter instanceof NamedParameterSpecification) {
						NamedParameterSpecification namedParameter = (NamedParameterSpecification) parameter;
						if (enrichSQLParameters) {
							String parameterName = namedParameter.getName();
							String parameterValue = StringTools.createStringFromCollection(parameters.get(parameterName), ",");
							sql = StringTools.replaceOnce(sql, "?", parameterValue);
						} else {
							sql = StringTools.replaceOnce(sql, "?", ":" + namedParameter.getName());
						}

					}
				}
			}
			return sql;
		} catch (Exception e) {
			myLogger.debug("Could not get SQL query from HQL query " + this.hqlQuery.getQueryString(), e);
			return "n/a - '" + e.getMessage() + "'";
		}
	}

	public void addManipulationEvent(long startNanos, String context) {
		if (manipulationEventsCounter < MAX_MANIPULATIONS_RECORDED) {
			long now = System.nanoTime();
			Duration d = Duration.of((now - startNanos), ChronoUnit.NANOS);
			if (context != null) {
				context = context.trim();
			}
			String trimmedContext = StringTools.getFirstNCharacters(context, 255);
			HibernateTimingEvent ev = new HibernateTimingEvent(d, trimmedContext);
			manipulationEvents.add(ev);
		}
		manipulationEventsCounter++;
	}

	public void setStatistics(HibernateApplyStatistics statistics) {
		this.statistics = statistics;
	}

	// **************************************************************************
	// Helper Class
	// **************************************************************************

	private class ParameterKey implements Comparable<ParameterKey> {
		private String parameterName;
		private int parameterIndex;

		private ParameterKey(String key) {
			if (key.contains("_")) {
				String[] split = key.split("_");
				parameterName = split[0];
				parameterIndex = Integer.parseInt(split[1]);
			} else {
				parameterName = key;
				parameterIndex = -1;
			}
		}

		@Override
		public int compareTo(ParameterKey o) {
			if (parameterName.equals(o.parameterName)) {
				if (parameterIndex < o.parameterIndex) {
					return -1;
				} else if (parameterIndex > o.parameterIndex) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return parameterName.compareTo(o.parameterName);
			}
		}
	}
}
