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
package com.braintribe.web.servlet.logs;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.logging.Logger.LogLevel;

public class LogsServletContext {

	protected static final String tribefireRuntimeMBeanPrefix = "com.braintribe.tribefire:type=TribefireRuntime,name=";

	/***************************** URL METHODS ****************************/

	public static boolean requestHasParameter(Map<String, String[]> requestParamMap, String paramName) {
		if (requestParamMap != null && paramName != null) {
			for (String paramKey : requestParamMap.keySet()) {
				if (paramKey.equals(paramName)) {
					return true;
				}
			}
		}

		return false;
	}

	public static Map<String, String> getParameters(Map<String, String[]> requestParamMap) {
		Map<String, String> params = new HashMap<String, String>();

		if (requestParamMap != null) {
			for (Entry<String, String[]> paramEntry : requestParamMap.entrySet()) {
				params.put(paramEntry.getKey(), paramEntry.getValue()[0]);
			}
		}

		return params;
	}

	public static String appendParameter(String url, Map<String, String> params) {
		String queryString = buildQueryString(params);
		if (!queryString.isEmpty()) {
			return String.format("%s?%s", url, queryString);
		} else
			return url;
	}

	private static String buildQueryString(Map<String, String> params) {
		try {
			MapCodec<String, String> codec = new MapCodec<String, String>();
			codec.setEscapeCodec(new UrlEscapeCodec());
			codec.setDelimiter("&");

			String queryString = codec.encode(params);
			return queryString;
		} catch (CodecException e) {
			throw new RuntimeException("error while encoding query parameters", e);
		}
	}

	/*************************** ANCHOR METHODS ***************************/

	private static String anchor(String display, String url, String classes, boolean useButton) {
		StringBuilder builder = new StringBuilder();

		if (classes != null) {
			builder.append("<a class='");
			builder.append(classes);
			builder.append("' href='");
			builder.append(url);
			builder.append("'>");
		} else {
			builder.append("<a href='");
			builder.append(url);
			builder.append("'>");
		}

		if (useButton) {
			builder.append("<button type='button' class='_button'>");
			builder.append(display);
			builder.append("</button>");
		} else {
			builder.append(display);
		}

		builder.append("</a>");
		return builder.toString();
	}

	public static String downloadAnchor(String filename, String display, Integer topN, String from, String to, String classes) {
		return downloadAnchor(filename, display, topN, from, to, classes, false);
	}

	public static String downloadAnchor(String filename, String display, Integer topN, String from, String to, String classes, boolean useButton) {
		Map<String, String> params = new HashMap<String, String>();

		if (topN != null) {
			params.put("top", topN.toString());
		}
		if (from != null) {
			params.put("from", from);
		}
		if (from != null) {
			params.put("to", to);
		}

		String url = appendParameter(String.format("logs/%s", filename), params);
		return anchor(display, url, classes, useButton);
	}

	public static String setLogLevelAnchor(String pageName, Map<String, String[]> requestParamMap, String display, String cartridgeName, LogLevel logLevel) {
		return setLogLevelAnchor(pageName, requestParamMap, display, cartridgeName, logLevel, null, false);
	}

	public static String setLogLevelAnchor(String pageName, Map<String, String[]> requestParamMap, String display, String cartridgeName, LogLevel logLevel, String classes, boolean useButton) {
		Map<String, String> params = getParameters(requestParamMap);

		if (logLevel != null) {
			params.put("logLevel", logLevel.toString());
		} else {
			params.remove("logLevel");
		}
		if (cartridgeName != null) {
			params.put("cartridgeName", cartridgeName);
		} else {
			params.remove("cartridgeName");
		}

		String url = appendParameter(String.format("logs/%s", pageName), params);
		return anchor(display, url, classes, useButton);
	}

	/************************* LOGVIEWER METHODS **************************/

	public static String createLogViewerScript() {
		StringBuilder builder = new StringBuilder();

		builder.append("<script>").append("\r\n");
		builder.append("  var logViewerEl = null;").append("\r\n");
		builder.append("  function getLogViewer() {").append("\r\n");
		builder.append("    if (logViewerEl === undefined || logViewerEl == null) {").append("\r\n");
		builder.append("      logViewerEl = document.getElementById('logViewer');").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    return logViewerEl;").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function getParameter(siteUrl, paramName) {").append("\r\n");
		builder.append("    var parameterArea = siteUrl.split('?')[1];").append("\r\n");
		builder.append("\r\n");
		builder.append("    if (parameterArea !== undefined && parameterArea != null) {").append("\r\n");
		builder.append("      var parameters = parameterArea.split('&');").append("\r\n");
		builder.append("\r\n");
		builder.append("      for (var i = 0, l = parameters.length; i < l; i++) {").append("\r\n");
		builder.append("        var keyValue = parameters[i].split('=');").append("\r\n");
		builder.append("        if (keyValue[0] == paramName) {").append("\r\n");
		builder.append("          return (keyValue[1] ? keyValue[1] : '');").append("\r\n");
		builder.append("        }").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    return null;").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  var logViewerFileSet = false;").append("\r\n");
		builder.append("  function startLogViewer() {").append("\r\n");
		builder.append("    var logViewer = getLogViewer();").append("\r\n");
		builder.append("    if(logViewer === undefined || logViewer == null) {").append("\r\n");
		builder.append("      setTimeout(startLogViewer, 100);").append("\r\n");
		builder.append("      return;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    if(sessionStorage.tfLogViewer !== undefined) {").append("\r\n");
		builder.append("      var tfLogViewer = JSON.parse(sessionStorage.tfLogViewer);").append("\r\n");
		builder.append("\r\n");
		builder.append("      var siteUrl = window.location.href;").append("\r\n");
		builder.append("      var logFile = getParameter(siteUrl, 'logFile');").append("\r\n");
		builder.append("      var followTail = getParameter(siteUrl, 'followTail');").append("\r\n");
		builder.append("\r\n");
		builder.append("      logViewerFileSet = (logFile !== undefined && logFile != null && logFile != '');").append("\r\n");
		builder.append("      if (tfLogViewer.logFile == logFile) {").append("\r\n");
		builder.append("        fillLogViewer(logViewer, tfLogViewer.logContent);").append("\r\n");
		builder.append("        if (followTail !== undefined && followTail != null) {").append("\r\n");
		builder.append("          logViewer.scrollTop = logViewer.scrollHeight;").append("\r\n");
		builder.append("        } else {").append("\r\n");
		builder.append("          logViewer.scrollTop = tfLogViewer.scrollPos;").append("\r\n");
		builder.append("        }").append("\r\n");
		builder.append("      } else {").append("\r\n");
		builder.append("        clearLogViewer(true);").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    logViewerWorker();").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function logViewerWorker() {").append("\r\n");
		builder.append("    var siteUrl = window.location.href;").append("\r\n");
		builder.append("    var logFile = getParameter(siteUrl, 'logFile');").append("\r\n");
		builder.append("    var logLines = getParameter(siteUrl, 'logLines');").append("\r\n");
		builder.append("    var pauseLog = getParameter(siteUrl, 'pauseLog');").append("\r\n");
		builder.append("    var followTail = getParameter(siteUrl, 'followTail');").append("\r\n");
		builder.append("\r\n");
		builder.append("    var tfLogViewer = undefined;").append("\r\n");
		builder.append("    if (sessionStorage.tfLogViewer !== undefined) {").append("\r\n");
		builder.append("      tfLogViewer = JSON.parse(sessionStorage.tfLogViewer);").append("\r\n");
		builder.append("    } else {").append("\r\n");
		builder.append("      tfLogViewer = {").append("\r\n");
		builder.append("        logFile: logFile,").append("\r\n");
		builder.append("        logMark: undefined,").append("\r\n");
		builder.append("        scrollPos: undefined,").append("\r\n");
		builder.append("        logContent: undefined,").append("\r\n");
		builder.append("        creationDate: undefined").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("\r\n");
		builder.append("      sessionStorage.tfLogViewer = JSON.stringify(tfLogViewer);").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    if ((logFile !== undefined && logLines !== undefined) && (logFile != null && logFile != '' && logLines > 0)) {").append("\r\n");
		builder.append("      var loadLogLines = logLines;").append("\r\n");
		builder.append("      if (pauseLog !== undefined && pauseLog != null) {").append("\r\n");
		builder.append("        var logContentSize = (tfLogViewer.logContent instanceof Array ? tfLogViewer.logContent.length : 0);").append("\r\n");
		builder.append("        if (logContentSize < logLines) {").append("\r\n");
		builder.append("          loadLogLines = logLines - logContentSize;").append("\r\n");
		builder.append("        } else {").append("\r\n");
		builder.append("          loadLogLines = 0;").append("\r\n");
		builder.append("        }").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("\r\n");
		builder.append("      if (loadLogLines != 0) {").append("\r\n");
		builder.append("        callService(logFile, logLines, loadLogLines, followTail, logViewerHandler, tfLogViewer.logMark, tfLogViewer.creationDate);").append("\r\n");
		builder.append("      } else {").append("\r\n");
		builder.append("        tfLogViewer.scrollPos = getLogViewer().scrollTop;").append("\r\n");
		builder.append("        sessionStorage.tfLogViewer = JSON.stringify(tfLogViewer);").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    setTimeout(logViewerWorker, 1000);").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function callService(logFile, logLines, loadLogLines, followTail, callback, logMark, creationDate) {").append("\r\n");
		builder.append("    var url = 'logs/logContent?logFile=' + logFile + '&logLines=' + loadLogLines;").append("\r\n");
		builder.append("    if (logMark !== undefined && logMark != null) {").append("\r\n");
		builder.append("      url = url + '&logMark=' + logMark;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("    if (creationDate !== undefined && creationDate != null) {").append("\r\n");
		builder.append("      url = url + '&creationDate=' + creationDate;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    try {").append("\r\n");
		builder.append("      var xmlHttp;").append("\r\n");
		builder.append("      if (window.XMLHttpRequest !== undefined) {").append("\r\n");
		builder.append("        xmlHttp = new XMLHttpRequest();").append("\r\n");
		builder.append("      } else {").append("\r\n");
		builder.append("        xmlHttp = new ActiveXObject('Microsoft.XMLHTTP');").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("\r\n");
		builder.append("      xmlHttp.onreadystatechange = function() {").append("\r\n");
		builder.append("        if (callback !== undefined && callback instanceof Function && this.readyState == 4) {").append("\r\n");
		builder.append("          if (this.status == 200) {").append("\r\n");
		builder.append("            var data = xmlHttp.responseText;").append("\r\n");
		builder.append("            var serviceResult = JSON.parse(data);").append("\r\n");
		builder.append("\r\n");
		builder.append("            // Return result").append("\r\n");
		builder.append("            callback(logFile, logLines, followTail, serviceResult);").append("\r\n");
		builder.append("          } else {").append("\r\n");
		builder.append("            // Service-Error").append("\r\n");
		builder.append("            callback(logFile, logLines, followTail, null);").append("\r\n");
		builder.append("          }").append("\r\n");
		builder.append("        }").append("\r\n");
		builder.append("      };").append("\r\n");
		builder.append("\r\n");
		builder.append("      xmlHttp.open('GET', url, false);").append("\r\n");
		builder.append("      xmlHttp.send();").append("\r\n");
		builder.append("    } catch (e) {").append("\r\n");
		builder.append("      // XMLHttpRequest-Error").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function logViewerHandler(logFile, logLines, followTail, serviceResult) {").append("\r\n");
		builder.append("    var tfLogViewer = JSON.parse(sessionStorage.tfLogViewer);").append("\r\n");
		builder.append("\r\n");
		builder.append("    var logContent = tfLogViewer.logContent;").append("\r\n");
		builder.append("    if ((logContent instanceof Array) == false) {").append("\r\n");
		builder.append("      logContent = [];").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    while (logContent.length > logLines) {").append("\r\n");
		builder.append("      logContent.shift();").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    var logViewer = getLogViewer();").append("\r\n");
		builder.append("    if ((serviceResult !== undefined && serviceResult != null) && (serviceResult.content instanceof Array)) {").append("\r\n");
		builder.append("      serviceResult.content.forEach(function(item, index) {").append("\r\n");
		builder.append("        if (logContent.length >= logLines) {").append("\r\n");
		builder.append("          logContent.shift();").append("\r\n");
		builder.append("        }").append("\r\n");
		builder.append("\r\n");
		builder.append("        logContent.push(item);").append("\r\n");
		builder.append("      });").append("\r\n");
		builder.append("\r\n");
		builder.append("      fillLogViewer(logViewer, logContent);").append("\r\n");
		builder.append("\r\n");
		builder.append("      tfLogViewer.logMark = serviceResult.mark;").append("\r\n");
		builder.append("      tfLogViewer.creationDate = serviceResult.creationDate;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    if (followTail !== undefined && followTail != null) {").append("\r\n");
		builder.append("      logViewer.scrollTop = logViewer.scrollHeight;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    tfLogViewer.logFile = logFile;").append("\r\n");
		builder.append("    tfLogViewer.logContent = logContent;").append("\r\n");
		builder.append("    tfLogViewer.scrollPos = logViewer.scrollTop;").append("\r\n");
		builder.append("    sessionStorage.tfLogViewer = JSON.stringify(tfLogViewer);").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function fillLogViewer(logViewer, logContent) {").append("\r\n");
		builder.append("    if (logViewerFileSet == true) {").append("\r\n");
		builder.append("      var logViewerHTML = '';").append("\r\n");
		builder.append("      if (logContent instanceof Array) {").append("\r\n");
		builder.append("        logContent.forEach(function(item, index) {").append("\r\n");
		builder.append("          logViewerHTML = logViewerHTML + item + '<br />';").append("\r\n");
		builder.append("        });").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("\r\n");
		builder.append("      logViewer.innerHTML = logViewerHTML;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function clearLogViewer(clearLogMark) {").append("\r\n");
		builder.append("    if (clearLogMark !== undefined && clearLogMark == true) {").append("\r\n");
		builder.append("      sessionStorage.removeItem('tfLogViewer');").append("\r\n");
		builder.append("    } else if(sessionStorage.tfLogViewer !== undefined) {").append("\r\n");
		builder.append("      var tfLogViewer = JSON.parse(sessionStorage.tfLogViewer);").append("\r\n");
		builder.append("\r\n");
		builder.append("      tfLogViewer.scrollPos = undefined;").append("\r\n");
		builder.append("      tfLogViewer.logContent = undefined;").append("\r\n");
		builder.append("      sessionStorage.tfLogViewer = JSON.stringify(tfLogViewer);").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    fillLogViewer(getLogViewer());").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function clearSelectedLogFile(sender) {").append("\r\n");
		builder.append("    var inputEl = sender.form.getElementsByTagName(\"input\");").append("\r\n");
		builder.append("    for (var i = 0, l = inputEl.length; i < l; i++) {").append("\r\n");
		builder.append("      if (inputEl[i].name == 'logFile' && inputEl[i].checked == true) {").append("\r\n");
		builder.append("        inputEl[i].checked = false;").append("\r\n");
		builder.append("      }").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    sender.form.submit();").append("\r\n").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function submitLogLines(sender, event) {").append("\r\n");
		builder.append("    if (event !== undefined && event.keyCode != 13) {").append("\r\n");
		builder.append("      return; //not enter").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    if (checkNumberValue(sender, 100, 3000) == true) {").append("\r\n");
		builder.append("      sender.form.submit();").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function submitShowLogLines(sender, event) {").append("\r\n");
		builder.append("    if (event !== undefined && event.keyCode != 13) {").append("\r\n");
		builder.append("      return; //not enter").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    if (checkNumberValue(sender, 10, 43) == true) {").append("\r\n");
		builder.append("      sender.form.submit();").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("  }").append("\r\n").append("\r\n");

		builder.append("  function checkNumberValue(sender, defaultMin, defaultMax) {").append("\r\n");
		builder.append("    var min = (sender.min !== undefined && sender.min != '' ? parseInt(sender.min) : defaultMin);").append("\r\n");
		builder.append("    var max = (sender.max !== undefined && sender.max != '' ? parseInt(sender.max) : defaultMax);").append("\r\n");
		builder.append("\r\n");
		builder.append("    var value = parseInt(sender.value);").append("\r\n");
		builder.append("    if (isNaN(value) == true) {").append("\r\n");
		builder.append("      return false;").append("\r\n");
		builder.append("    } else if (value < min) {").append("\r\n");
		builder.append("      sender.value = min;").append("\r\n");
		builder.append("    } else if (value > max) {").append("\r\n");
		builder.append("      sender.value = max;").append("\r\n");
		builder.append("    }").append("\r\n");
		builder.append("\r\n");
		builder.append("    return true;").append("\r\n");
		builder.append("  }").append("\r\n");
		builder.append("</script>");

		return builder.toString();
	}

	/*************************** HELPER CLASSES ***************************/

	private static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			int res = new Date(file2.lastModified()).compareTo(new Date(file1.lastModified()));
			if (res == 0) {
				return file1.compareTo(file2);
			}

			return res;
		}
	}

	private static class DateFileFilter implements FileFilter {
		private final String pattern;
		private final Date from;
		private final Date to;

		public DateFileFilter(Date from, Date to, String pattern) {
			super();

			this.pattern = pattern;
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean accept(File checkFile) {
			if (checkFile.isFile() == false) {
				return false;
			}
			if (pattern != null && checkFile.getName().matches(pattern) == false) {
				return false;
			}

			if (from != null || to != null) {
				Date date = new Date(checkFile.lastModified());

				if (from != null && from.compareTo(date) > 0) {
					return false;
				}
				if (to != null && to.compareTo(date) < 0) {
					return false;
				}
			}

			return true;
		}
	}
}
