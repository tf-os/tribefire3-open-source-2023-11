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
package tribefire.extension.elastic.elasticsearch.wares.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.braintribe.cfg.Configurable;

/**
 * Sanitize characters to fix URIs/IRIs so they can consist of all US-ASCII characters and get handled gracefully by
 * broken clients who do not percent encode URIs/IRIs.
 *
 * As long as http://tools.ietf.org/html/draft-ietf-iri-3987bis-11 is not released, this filter will protect URI/IRI
 * sensitive services from clients who are not implementing http://tools.ietf.org/html/rfc2396#section-2.4.3
 *
 * More info: http://blog.jclark.com/2008/11/what-allowed-in-uri.html
 *
 */
public final class SanitizeCharacterFilter implements Filter {

	private String[] parameternames = new String[] { "q" };

	/**
	 * 
	 * These ASCII characters in HTTP parameters are an error in URI/IRI if not percent-encoded. Some clients are
	 * broken, so fix it here.
	 */
	private final static Map<Character, String> map = new HashMap<Character, String>() {

		private static final long serialVersionUID = 7563919467116052695L;

		{
			put('<', "%3C");
			put('>', "%3E");
			put('[', "%5B");
			put('\\', "%5C");
			put(']', "%5D");
			put('^', "%5E");
			put('`', "%60");
			put('{', "%7B");
			put('|', "%7C");
			put('}', "%7D");
			put('\u007f', "%7F");
		}
	};

	@Override
	public void init(FilterConfig filterConfig) {
		if (filterConfig != null && filterConfig.getInitParameter("parameternames") != null) {
			this.parameternames = filterConfig.getInitParameter("parameternames").split(",");
		}
	}

	private class FilteredRequest extends HttpServletRequestWrapper {

		public FilteredRequest(ServletRequest request) {
			super((HttpServletRequest) request);
		}

		@Override
		public String getQueryString() {
			return sanitize(super.getQueryString());
		}

		@Override
		public String getParameter(String paramName) {
			String value = super.getParameter(paramName);
			for (String p : parameternames) {
				if (p.equals(paramName)) {
					value = sanitize(value);
				}
			}
			return value;
		}

		@Override
		public String[] getParameterValues(String paramName) {
			String values[] = super.getParameterValues(paramName);
			for (String p : parameternames) {
				if (p.equals(paramName)) {
					for (int index = 0; index < values.length; index++) {
						values[index] = sanitize(values[index]);
					}
				}
			}
			return values;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			Map<String, String[]> values = super.getParameterMap();
			if (values != null) {
				for (String p : parameternames) {
					if (values.containsKey(p)) {
						values.put(p, sanitize(values.get(p)));
					}
				}
			}
			return values;
		}

		private String[] sanitize(String[] inputArray) {
			if (inputArray == null) {
				return null;
			}

			String[] target = new String[inputArray.length];

			for (int k = 0; k < inputArray.length; ++k) {
				target[k] = this.sanitize(inputArray[k]);
			}
			return target;
		}

		private String sanitize(String input) {
			if (input == null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < input.length(); i++) {
				Character ch = input.charAt(i);
				String mapped = map.get(ch);
				sb.append(mapped != null ? mapped : ch);
			}
			return sb.toString();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new FilteredRequest(request), response);
	}

	@Override
	public void destroy() {
		// Intentionally left blank
	}

	@Configurable
	public void setParameternames(String[] parameternames) {
		this.parameternames = parameternames;
	}

}
