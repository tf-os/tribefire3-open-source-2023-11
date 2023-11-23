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
package com.braintribe.web.servlet.about;

import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsString;
import static com.braintribe.web.servlet.about.ParameterTools.getTypeOfRequest;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.MathTools;
import com.braintribe.utils.StringTools;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;
import com.braintribe.web.servlet.TypedVelocityContext;
import com.braintribe.web.servlet.about.expert.DiagnosticMultinode;
import com.braintribe.web.servlet.about.expert.Heapdump;
import com.braintribe.web.servlet.about.expert.HotThreadsExpert;
import com.braintribe.web.servlet.about.expert.Json;
import com.braintribe.web.servlet.about.expert.PackagingExpert;
import com.braintribe.web.servlet.about.expert.ProcessesExpert;
import com.braintribe.web.servlet.about.expert.SystemInformation;
import com.braintribe.web.servlet.about.expert.Threaddump;
import com.braintribe.web.servlet.about.expert.TribefireInformation;

/**
 * This servlet provides the content of the tribefire-services "About" page. Its main objective is to provide information about the host, the servlet
 * container and tribefire itself. <br>
 * <br>
 * The About page is a portal to access all nodes in a cluster. (Almost) all actions can be applied on either one specific node or on all nodes. This
 * servlet does not have a fixed connection to any host, not even to the local one where this page has been serviced. Instead, it uses Multicast
 * messages to communicate with the host in the cluster. Hence, it does not make a difference which host actually provided the About page. <br>
 * <br>
 * The About page consists of a HTML page that contains a list of live nodes in the cluster and some buttons that can be used to invoke services on
 * remote hosts. The centre piece of the About page is empty to be filled with the result of the requested action. When the About page is opened for
 * the first time, a default request to get system information about the local server is issued. <br>
 * <br>
 * The following actions can be performed on the About page:
 * <ul>
 * <li>Get System Information: This will display the system information (host, servlet container, tribefire) of one or all nodes. (multiple nodes
 * supported)</li>
 * <li>Get System Information as JSON: The System Information will be provided as a downloadable package. (multiple nodes supported)</li>
 * <li>Get Version Information: Detailed version information of all artifacts of tribefire-services or a Cartridge will be displayed. (multiple nodes
 * supported)</li>
 * <li>Get a list of Hot Threads: Shows Hot Threads of one or all nodes. Hot Threads are threads that consume the most CPU time in a specific time
 * window. This helps to identify code that takes a lot of CPU cycles. (multiple nodes supported)</li>
 * <li>Get a list of all Processes: Shows all services that run on a specific node. (multiple nodes supported)</li>
 * <li>Get a Heapdump: Provides the heap dump of a specific node. Please note that taking a heap dump may take some time and pause the JVM. Hence, it
 * is advisable to not do this in a production environment. (multiple nodes not supported; due to limitations this is also only supported on the same
 * host as the About page was accessed at)</li>
 * <li>Get a Threaddump: Provides a thread dump of one or all nodes. (multiple nodes supported)</li>
 * <li>Download a Diagnostic Package: Provides a Diagnostic Package as download, which contains: log files, thread dump, processed, and system
 * information. (multiple nodes not supported; due to limitations this is also only supported on the same host as the About page was accessed at)</li>
 * <li>Download an Extended Diagnostic Package: Provides a Diagnostic Package that also includes a heap dump. (multiple nodes not supported; due to
 * limitations this is also only supported on the same host as the About page was accessed at)</li>
 * </ul>
 * 
 */
public class AboutServlet extends BasicTemplateBasedServlet implements InitializationAware {

	private static Logger logger = Logger.getLogger(AboutServlet.class);

	private static final long serialVersionUID = 4695919181704450507L;
	private static final String aboutPageTemplateLocation = "com/braintribe/web/servlet/about/templates/tfAbout.html.vm";
	private static final String systemInformationTemplateLocation = "com/braintribe/web/servlet/about/templates/tfSystemInformation.html.vm";
	private static final String deployablesPageTemplateLocation = "com/braintribe/web/servlet/about/templates/tfDeployables.html.vm";
	private static final String versionsPageTemplateLocation = "com/braintribe/web/servlet/about/templates/tfVersions.html.vm";
	private static final String hotthreadsPageTemplateLocation = "com/braintribe/web/servlet/about/templates/tfHotThreads.html.vm";
	private static final String processesPageTemplateLocation = "com/braintribe/web/servlet/about/templates/tfProcesses.html.vm";

	public static final String TYPE_SYSINFO = "systeminformation";
	public static final String TYPE_DEPLOYABLES = "deployables";
	public static final String TYPE_JSON = "json";
	public static final String TYPE_VERSIONS = "versions";
	public static final String TYPE_HOTTHREADS = "hotthreads";
	public static final String TYPE_PROCESSES = "processes";
	public static final String TYPE_HEAPDUMP = "heapdump";
	public static final String TYPE_THREADDUMP = "threaddump";
	public static final String TYPE_DIAGNOSTICPACKAGE = "diagnostic";
	public static final String TYPE_DIAGNOSTICPACKAGEEXTENDED = "diagnosticExtended";

	protected Evaluator<ServiceRequest> requestEvaluator;

	protected LiveInstances liveInstances;
	protected InstanceId localInstanceId;
	protected ServiceInstanceIdManagement serviceInstanceMgmt;

	private Supplier<String> userSessionIdProvider;

	protected ExecutorService executor;

	private DiagnosticMultinode diagnosticMultinode;
	private Threaddump threaddump;
	private Heapdump heapdump;
	private Json json;
	private PackagingExpert packagingExpert;
	private HotThreadsExpert hotThreadsExpert;
	private ProcessesExpert processesExpert;
	private SystemInformation systemInformation;
	private TribefireInformation tribefireInformation;

	public final static String KEY_ALL_NODES = "- All Nodes -";

	@Override
	public void postConstruct() {
		setTemplateLocation(aboutPageTemplateLocation);
		super.addTemplateLocation(TYPE_VERSIONS, versionsPageTemplateLocation);
		super.addTemplateLocation(TYPE_HOTTHREADS, hotthreadsPageTemplateLocation);
		super.addTemplateLocation(TYPE_PROCESSES, processesPageTemplateLocation);
		super.addTemplateLocation(TYPE_SYSINFO, systemInformationTemplateLocation);
		super.addTemplateLocation(TYPE_DEPLOYABLES, deployablesPageTemplateLocation);

		serviceInstanceMgmt = new ServiceInstanceIdManagement(localInstanceId, liveInstances);
	}

	/**
	 * This method will process those request that result in a direct download of a resource. Everything else will be passed to the super class for
	 * processing, which eventually will result in a call to {@link #createContext(HttpServletRequest, HttpServletResponse)}.
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		logger.debug(() -> "Received a request with the parameters: " + ParameterTools.getParameterMapAsString(req));

		Collection<InstanceId> selectedServiceInstances = serviceInstanceMgmt.getSelectedServiceInstances(req);

		String userSessionId = this.userSessionIdProvider.get();

		try {
			String type = getTypeOfRequest(req);

			// The following types will return a downloadable file/resource. Hence, they will not use Velocity.

			if (type != null) {
				if (type.equalsIgnoreCase(TYPE_JSON)) {

					json.processJsonRequest(requestEvaluator, selectedServiceInstances, resp, userSessionId, executor);
					// Don't let the normal output proceed; request has been handled at this point
					return;

				} else if (type.equalsIgnoreCase(TYPE_HEAPDUMP)) {

					heapdump.processHeapdumpRequest(requestEvaluator, selectedServiceInstances, serviceInstanceMgmt, resp, userSessionId);
					return;

				} else if (type.equalsIgnoreCase(TYPE_THREADDUMP)) {

					threaddump.processThreaddumpRequest(requestEvaluator, selectedServiceInstances, resp, userSessionId, executor);
					return;

				} else if (type.equalsIgnoreCase(TYPE_DIAGNOSTICPACKAGE) || type.equalsIgnoreCase(TYPE_DIAGNOSTICPACKAGEEXTENDED)) {

					// diagnostic.processDiagnosticPackageRequest(selectedServiceInstances, serviceInstanceMgmt, resp, type, userSessionId);
					diagnosticMultinode.processDiagnosticPackageRequest(resp, type, userSessionId);
					return;
				}
			}
		} catch (Exception e) {
			throw new ServletException("Could not perform request.", e);
		} finally {
			logger.debug(() -> "Done with processing request types that might be handled directly as a download.");
		}

		logger.debug(() -> "Delegating the creation of the response to the super class.");

		super.service(req, resp);

		logger.debug(() -> "Done with processing the serlvet request.");

	}

	/**
	 * Creates a context based on the request. The result of this method is a HTTP snippet that will be included in the About page.
	 */
	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {

		logger.debug(() -> "Starting to create a Velocity context for a request.");

		TypedVelocityContext context = new TypedVelocityContext();

		Set<String> serviceInstances = serviceInstanceMgmt.getAllInstances();
		Collection<InstanceId> selectedServiceInstances = null;
		try {
			selectedServiceInstances = serviceInstanceMgmt.getSelectedServiceInstances(request);
		} catch (ServletException e) {
			logger.error("Error while determining selected service instance.", e);
		}

		String userSessionId = this.userSessionIdProvider.get();

		String selectedNodeId = KEY_ALL_NODES;
		boolean initialLoad = false;

		final String type = getTypeOfRequest(request);
		if (type != null && type.trim().length() > 0) {
			if (type.equalsIgnoreCase(TYPE_VERSIONS)) {

				context.setType(TYPE_VERSIONS);
				try {
					selectedServiceInstances = serviceInstanceMgmt.getSelectedApplicationInstanceIds(request);
				} catch (Exception e) {
					logger.error("Error while determining selected applications instances.", e);
				}
				packagingExpert.processPackagingRequest(requestEvaluator, selectedServiceInstances, context, userSessionId, executor);

			} else if (type.equalsIgnoreCase(TYPE_HOTTHREADS)) {

				context.setType(TYPE_HOTTHREADS);
				hotThreadsExpert.processHotThreadsRequest(requestEvaluator, selectedServiceInstances, request, context, userSessionId, executor);

			} else if (type.equalsIgnoreCase(TYPE_PROCESSES)) {

				context.setType(TYPE_PROCESSES);
				processesExpert.processProcessesRequest(requestEvaluator, selectedServiceInstances, context, userSessionId, executor);

			} else if (type.equalsIgnoreCase(TYPE_SYSINFO)) {

				context.setType(TYPE_SYSINFO);
				systemInformation.processSysInfoRequest(requestEvaluator, selectedServiceInstances, context, userSessionId, executor);

			} else if (type.equalsIgnoreCase(TYPE_DEPLOYABLES)) {

				context.setType(TYPE_DEPLOYABLES);
				tribefireInformation.processGetDeployablesInfo(requestEvaluator, selectedServiceInstances, context, userSessionId, executor);

			}
		} else {

			// Initial loading; getting local node

			selectedNodeId = serviceInstanceMgmt.getNodeFromInstanceId(localInstanceId);
			initialLoad = true;

		}

		if (StringTools.isBlank(selectedNodeId)) {
			String param = getSingleParameterAsString(request, "node");
			if (!StringTools.isBlank(param) && !param.equals(KEY_ALL_NODES)) {
				selectedNodeId = param;
			}
		}
		String hideNavString = getSingleParameterAsString(request, "hideNav");
		boolean hideNav = false;
		if (!StringTools.isBlank(hideNavString)) {
			hideNav = hideNavString.equalsIgnoreCase("true");
		}

		context.put("current_year", new GregorianCalendar().get(Calendar.YEAR));
		context.put("aboutRequestUrl", ""); // It's the safest way as we do not know which address the browser used to
											// access the about page.
		context.put("tribefireRuntime", TribefireRuntime.class);
		context.put("serviceInstances", serviceInstances);
		context.put("nodes", serviceInstanceMgmt.getNodes());
		context.put("selectedNodeId", selectedNodeId);
		context.put("initialLoad", initialLoad);
		context.put("hideNav", hideNav);
		context.put("allNodes", KEY_ALL_NODES);
		context.put("MathTools", MathTools.class);
		context.put("StringTools", StringTools.class);
		context.put("DateTools", DateTools.class);
		context.put("DateToolsISO8601", DateTools.ISO8601_DATE_FORMAT);
		context.put("ChronoUnit", ChronoUnit.class);
		context.put("ChronoUnitNanos", ChronoUnit.NANOS);

		logger.debug(() -> "Done with creating a Velocity context for a request.");

		return context;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	@Required
	public void setLiveInstances(LiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}
	@Configurable
	@Required
	public void setLocalInstanceId(InstanceId localInstanceId) {
		this.localInstanceId = localInstanceId;
	}
	@Configurable
	@Required
	public void setCurrentUserSessionIdProvider(Supplier<String> userSessionIdProvider) {
		this.userSessionIdProvider = userSessionIdProvider;
	}
	@Configurable
	@Required
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
	@Configurable
	@Required
	public void setDiagnosticMultinode(DiagnosticMultinode diagnosticMultinode) {
		this.diagnosticMultinode = diagnosticMultinode;
	}
	@Configurable
	@Required
	public void setThreaddump(Threaddump threaddump) {
		this.threaddump = threaddump;
	}
	@Configurable
	@Required
	public void setHeapdump(Heapdump heapdump) {
		this.heapdump = heapdump;
	}
	@Configurable
	@Required
	public void setJson(Json json) {
		this.json = json;
	}
	@Configurable
	@Required
	public void setPackagingExpert(PackagingExpert packagingExpert) {
		this.packagingExpert = packagingExpert;
	}
	@Configurable
	@Required
	public void setHotThreadsExpert(HotThreadsExpert hotThreadsExpert) {
		this.hotThreadsExpert = hotThreadsExpert;
	}
	@Configurable
	@Required
	public void setProcessesExpert(ProcessesExpert processesExpert) {
		this.processesExpert = processesExpert;
	}
	@Configurable
	@Required
	public void setSystemInformation(SystemInformation systemInformation) {
		this.systemInformation = systemInformation;
	}
	@Configurable
	@Required
	public void setTribefireInformation(TribefireInformation tribefireInformation) {
		this.tribefireInformation = tribefireInformation;
	}

}
