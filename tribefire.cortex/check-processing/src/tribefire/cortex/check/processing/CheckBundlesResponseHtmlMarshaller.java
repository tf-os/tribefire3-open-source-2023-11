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
package tribefire.cortex.check.processing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregatable;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregation;

/**
 * This custom marshaller is mapped to custom mime type <code>text/html;spec=check-bundles-response</code> and reacts on
 * incoming {@link CheckBundlesResponse Check Bundle Responses}. Its responsibility is to marshall the incoming response
 * in a styled HTML site.
 * 
 * @author christina.wilpernig
 */
public class CheckBundlesResponseHtmlMarshaller implements CharacterMarshaller {

	private VelocityEngine veloEngine;
	private Template template;

	// MARSHALLING
	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, value, options);
			writer.flush();

		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling", e);
		}
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		if (!(value instanceof CheckBundlesResponse))
			throw new IllegalArgumentException("Unsupported value type. Supported type: " + CheckBundlesResponse.T.getShortName());

		this.veloEngine = com.braintribe.utils.velocity.VelocityTools.newResourceLoaderVelocityEngine(true);
		this.template = this.veloEngine.getTemplate("tribefire/cortex/check/processing/template/result.html.vm");

		CheckBundlesResponse response = (CheckBundlesResponse) value;

		VelocityContext context = new VelocityContext();
		context.put("response", response);
		context.put("overallStatus", response.getStatus());
		context.put("overallElapsedTime", response.getElapsedTimeInMs());

		List<CbrAggregatable> elements = response.getElements();

		StringBuilder aggregationListStringBuilder = new StringBuilder();
		int aggregationByCount = getAggregationList(aggregationListStringBuilder, elements);
		context.put("aggregationList", aggregationListStringBuilder.toString());

		context.put("aggregatedByCount", aggregationByCount);

		// Statistics
		ResponseStatistics statistic = new ResponseStatistics();
		statistic.createStatistic(elements);

		context.put("checkCount", statistic.getChecks());
		context.put("okCount", statistic.getOkCount());
		context.put("warnCount", statistic.getWarnCount());
		context.put("failCount", statistic.getFailCount());

		CheckBundlesVelocityTools tools = new CheckBundlesVelocityTools();
		context.put("tools", tools);

		template.merge(context, writer);
	}

	class ResponseStatistics {
		private int checks;
		private int okCount;
		private int warnCount;
		private int failCount;

		public void createStatistic(List<CbrAggregatable> elements) {
			for (CbrAggregatable a : elements) {
				if (a.isResult()) {
					checks++;
					switch (a.getStatus()) {
						case fail:
							failCount++;
							break;
						case ok:
							okCount++;
							break;
						case warn:
							warnCount++;
							break;
						default:
							break;
					}
				} else {
					createStatistic(((CbrAggregation) a).getElements());
				}
			}
		}

		public int getChecks() {
			return checks;
		}

		public int getOkCount() {
			return okCount;
		}

		public int getWarnCount() {
			return warnCount;
		}

		public int getFailCount() {
			return failCount;
		}
	}

	/**
	 * Returns the aggregation order of the elements in a human-readable format, like: <b>node / bundle / weight</b>
	 */
	private static int getAggregationList(StringBuilder res, List<CbrAggregatable> aggregatables) {
		int count = collectKinds(res, aggregatables, 0);
		if (count == 0)
			res.append("No aggregation defined");

		return count;
	}

	private static int collectKinds(StringBuilder res, List<CbrAggregatable> aggregatables, int count) {
		for (CbrAggregatable e : aggregatables) {
			if (e instanceof CbrAggregation) {
				if (res.length() > 0)
					res.append(" / ");

				CbrAggregation aggregation = (CbrAggregation) e;
				res.append(aggregation.getKind());
				List<CbrAggregatable> elements = aggregation.getElements();

				return collectKinds(res, elements, ++count);
			} else {
				break;
			}
		}
		return count;
	}

	// Unsupported
	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported.");
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported.");
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("Unmarshalling is not supported.");
	}
}
