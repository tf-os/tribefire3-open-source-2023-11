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
package com.braintribe.model.processing.elasticsearch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.info.PluginsAndModules;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsIndices;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsNodeResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsNodes;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.cluster.health.ClusterShardHealth;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.SizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.http.HttpInfo;
import org.elasticsearch.index.cache.query.QueryCacheStats;
import org.elasticsearch.index.cache.request.RequestCacheStats;
import org.elasticsearch.index.engine.SegmentsStats;
import org.elasticsearch.index.fielddata.FieldDataStats;
import org.elasticsearch.index.flush.FlushStats;
import org.elasticsearch.index.get.GetStats;
import org.elasticsearch.index.merge.MergeStats;
import org.elasticsearch.index.refresh.RefreshStats;
import org.elasticsearch.index.search.stats.SearchStats;
import org.elasticsearch.index.search.stats.SearchStats.Stats;
import org.elasticsearch.index.shard.DocsStats;
import org.elasticsearch.index.shard.IndexingStats;
import org.elasticsearch.index.store.StoreStats;
import org.elasticsearch.indices.NodeIndicesStats;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.jvm.JvmInfo.Mem;
import org.elasticsearch.monitor.os.OsInfo;
import org.elasticsearch.monitor.process.ProcessInfo;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.search.suggest.completion.CompletionStats;
import org.elasticsearch.threadpool.ThreadPool.Info;
import org.elasticsearch.threadpool.ThreadPool.ThreadPoolType;
import org.elasticsearch.threadpool.ThreadPoolInfo;
import org.elasticsearch.transport.TransportInfo;

import com.braintribe.model.elasticsearchreflection.ElasticsearchReflection;
import com.braintribe.model.elasticsearchreflection.ElasticsearchReflectionError;
import com.braintribe.model.elasticsearchreflection.ElasticsearchReflectionErrorAddress;
import com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealth;
import com.braintribe.model.elasticsearchreflection.nodeinformation.NodeInformation;
import com.braintribe.model.elasticsearchreflection.nodestats.NodeStatsInfo;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;

/**
 * Utility class to access cluster specific admin information from Elasticsearch
 *
 */
public class ElasticsearchReflectionUtil {

	public static TimeUnit STANDARD_TIME_UNIT = TimeUnit.SECONDS;
	public static long STANDARD_TIMEOUT = 10;

	public static ElasticsearchReflection getElasticsearchReflection(ElasticsearchClient client) {

		ClusterAdminClient clusterAdminClient = client.getClusterAdminClient();

		ElasticsearchReflection elasticsearchReflection = ElasticsearchReflection.T.create();
		try {

			addClusterHealth(clusterAdminClient, elasticsearchReflection);
			addNodeInfo(client.elastic(), elasticsearchReflection);

			addPendingClusterTasks(clusterAdminClient, elasticsearchReflection);
			addNodeStats(clusterAdminClient, elasticsearchReflection);
			addClusterStats(clusterAdminClient, elasticsearchReflection);

			// possibilities for future enhancements
			// ClusterHealthResponse healthResponse = client.elastic().execute(ClusterHealthAction.INSTANCE, new
			// ClusterHealthRequest()).actionGet();
			// ClusterStateResponse actionGet4 = client.elastic().execute(ClusterStateAction.INSTANCE, new
			// ClusterStateRequest()).actionGet();
			// ClusterHealthResponse actionGet5 = client.elastic().execute(ClusterHealthAction.INSTANCE, new
			// ClusterHealthRequest()).actionGet();
			// GetMappingsResponse actionGet6 = client.elastic().execute(GetMappingsAction.INSTANCE, new
			// GetMappingsRequest()).actionGet();
			// GetSettingsResponse actionGet7 = client.elastic().execute(GetSettingsAction.INSTANCE, new
			// GetSettingsRequest()).actionGet();
			// IndicesStatsResponse actionGet8 = client.elastic().execute(IndicesStatsAction.INSTANCE, new
			// IndicesStatsRequest()).actionGet();

		} catch (Exception e) {
			// Elasticsearch is not reachable
			ElasticsearchReflectionError ere = ElasticsearchReflectionError.T.create();
			List<ElasticsearchReflectionErrorAddress> list = new ArrayList<>();
			ere.setElasticsearchReflectionErrorAddresses(list);

			Client elasticClient = client.elastic();
			if (elasticClient instanceof TransportClient) {
				TransportClient transportClient = (TransportClient) elasticClient;
				List<TransportAddress> transportAddresses = transportClient.transportAddresses();
				for (TransportAddress transportAddress : transportAddresses) {
					ElasticsearchReflectionErrorAddress erea = ElasticsearchReflectionErrorAddress.T.create();
					list.add(erea);
					String address = transportAddress.getAddress();
					String host = transportAddress.getHost();
					int port = transportAddress.getPort();

					erea.setAddress(address);
					erea.setHost(host);
					erea.setPort(port);
				}
			}
			Settings settings = elasticClient.settings();
			String clusterName = settings.get("cluster.name");
			String nodeName = settings.get("node.name");

			String cause = null;
			if (e.getCause() != null) {
				cause = e.getCause().toString();
			}
			String message = e.getMessage();

			ere.setClusterName(clusterName);
			ere.setNodeName(nodeName);
			ere.setCause(cause);
			ere.setMessage(message);

			elasticsearchReflection.setElasticsearchReflectionError(ere);
		}

		return elasticsearchReflection;
	}

	@SuppressWarnings("unused")
	private static void addClusterStats(ClusterAdminClient clusterAdminClient, ElasticsearchReflection elasticsearchReflection) {
		ClusterStatsResponse clusterStatsResponse = clusterAdminClient.prepareClusterStats().get();

		ClusterName clusterName = clusterStatsResponse.getClusterName();
		ClusterStatsIndices indicesStats = clusterStatsResponse.getIndicesStats(); // <<<----------------
		List<ClusterStatsNodeResponse> nodes = clusterStatsResponse.getNodes(); // <<<----------
		NodeStats nodeStats = nodes.get(0).nodeStats();// <<<----------
		Map<String, ClusterStatsNodeResponse> nodesMap = clusterStatsResponse.getNodesMap();
		ClusterStatsNodes nodesStats = clusterStatsResponse.getNodesStats(); // <<<??????
		ClusterHealthStatus status = clusterStatsResponse.getStatus();
		long timestamp = clusterStatsResponse.getTimestamp();

		// TODO: objects with <<< need to added

	}

	private static void addNodeStats(ClusterAdminClient clusterAdminClient, ElasticsearchReflection elasticsearchReflection) {
		NodesStatsResponse nodesStatsResponse = clusterAdminClient.prepareNodesStats().get();

		NodeStatsInfo _nodeStatsInfo = NodeStatsInfo.T.create();
		elasticsearchReflection.setNodeStatsInfo(_nodeStatsInfo);
		List<com.braintribe.model.elasticsearchreflection.nodestats.NodeStats> _nodeStatsList = new ArrayList<>();
		_nodeStatsInfo.setNodeStatsList(_nodeStatsList);

		for (NodeStats nodeStats : nodesStatsResponse.getNodes()) {

			// skip all the other - looks like duplicate information
			NodeIndicesStats indices = nodeStats.getIndices();

			StoreStats store = indices.getStore();
			DocsStats docs = indices.getDocs();
			IndexingStats indexing = indices.getIndexing();
			GetStats get = indices.getGet();
			SearchStats search = indices.getSearch();
			MergeStats merge = indices.getMerge();
			RefreshStats refresh = indices.getRefresh();
			FlushStats flush = indices.getFlush();
			FieldDataStats fieldData = indices.getFieldData();
			QueryCacheStats queryCache = indices.getQueryCache();
			RequestCacheStats requestCache = indices.getRequestCache();
			CompletionStats completion = indices.getCompletion();
			SegmentsStats segments = indices.getSegments();

			com.braintribe.model.elasticsearchreflection.nodestats.StoreStats _storeStats = com.braintribe.model.elasticsearchreflection.nodestats.StoreStats.T
					.create();
			_storeStats.setSizeInBytes(store.getSizeInBytes());
			_storeStats.setThrottleTimeInNanos(store.getThrottleTime().getNanos());

			com.braintribe.model.elasticsearchreflection.nodestats.DocsStats _docsStats = com.braintribe.model.elasticsearchreflection.nodestats.DocsStats.T
					.create();
			_docsStats.setCount(docs.getCount());
			_docsStats.setDeleted(docs.getDeleted());

			com.braintribe.model.elasticsearchreflection.nodestats.IndexingStats _indexingStats = com.braintribe.model.elasticsearchreflection.nodestats.IndexingStats.T
					.create();
			com.braintribe.model.elasticsearchreflection.nodestats.IndexingStatsEntry _indexingStatsTotal = com.braintribe.model.elasticsearchreflection.nodestats.IndexingStatsEntry.T
					.create();
			_indexingStats.setTotal(_indexingStatsTotal);
			org.elasticsearch.index.shard.IndexingStats.Stats total2 = indexing.getTotal();
			_indexingStatsTotal.setDeleteCount(total2.getDeleteCount());
			_indexingStatsTotal.setDeleteCurrent(total2.getDeleteCurrent());
			_indexingStatsTotal.setDeleteTimeInMillis(total2.getDeleteTime().getMillis());
			_indexingStatsTotal.setIndexCount(total2.getIndexCount());
			_indexingStatsTotal.setIndexCurrent(total2.getIndexCurrent());
			_indexingStatsTotal.setIndexFailedCount(total2.getIndexFailedCount());
			_indexingStatsTotal.setIsThrottled(total2.isThrottled());
			_indexingStatsTotal.setNoopUpdateCount(total2.getNoopUpdateCount());
			_indexingStatsTotal.setThrottleTimeInMillis(total2.getThrottleTime().getMillis());

			Map<String, com.braintribe.model.elasticsearchreflection.nodestats.IndexingStatsEntry> _typeStats = new HashMap<>();

			Map<String, org.elasticsearch.index.shard.IndexingStats.Stats> typeStats = indexing.getTypeStats();
			if (typeStats != null) {
				for (Map.Entry<String, org.elasticsearch.index.shard.IndexingStats.Stats> typeStat : typeStats.entrySet()) {
					com.braintribe.model.elasticsearchreflection.nodestats.IndexingStatsEntry _indexingStatsEntry = com.braintribe.model.elasticsearchreflection.nodestats.IndexingStatsEntry.T
							.create();
					String key = typeStat.getKey();
					org.elasticsearch.index.shard.IndexingStats.Stats value = typeStat.getValue();
					_indexingStatsEntry.setDeleteCount(value.getDeleteCount());
					_indexingStatsEntry.setDeleteCurrent(value.getDeleteCurrent());
					_indexingStatsEntry.setDeleteTimeInMillis(value.getDeleteTime().getMillis());
					_indexingStatsEntry.setIndexCount(value.getIndexCount());
					_indexingStatsEntry.setIndexCurrent(value.getIndexCurrent());
					_indexingStatsEntry.setIndexFailedCount(value.getIndexFailedCount());
					_indexingStatsEntry.setIsThrottled(value.isThrottled());
					_indexingStatsEntry.setNoopUpdateCount(value.getNoopUpdateCount());
					_indexingStatsEntry.setThrottleTimeInMillis(value.getThrottleTime().getMillis());
					_typeStats.put(key, _indexingStatsEntry);
				}
			}
			_indexingStats.setTypeStats(_typeStats);

			com.braintribe.model.elasticsearchreflection.nodestats.GetStats _getStats = com.braintribe.model.elasticsearchreflection.nodestats.GetStats.T
					.create();
			_getStats.setCurrent(get.current());
			_getStats.setExistsCount(get.getExistsCount());
			_getStats.setExistsTimeInMillis(get.getExistsTimeInMillis());
			_getStats.setMissingCount(get.getMissingCount());
			_getStats.setMissingTimeInMillis(get.getMissingTimeInMillis());

			com.braintribe.model.elasticsearchreflection.nodestats.SearchStats _searchStats = com.braintribe.model.elasticsearchreflection.nodestats.SearchStats.T
					.create();
			_searchStats.setOpenContexts(search.getOpenContexts());

			com.braintribe.model.elasticsearchreflection.nodestats.SearchStatsEntry _stats = com.braintribe.model.elasticsearchreflection.nodestats.SearchStatsEntry.T
					.create();
			Stats total = search.getTotal();
			_stats.setFetchCount(total.getFetchCount());
			_stats.setFetchCurrent(total.getFetchCurrent());
			_stats.setFetchTimeInMillis(total.getFetchTimeInMillis());
			_stats.setQueryCount(total.getQueryCount());
			_stats.setQueryCurrent(total.getQueryCurrent());
			_stats.setQueryTimeInMillis(total.getQueryTimeInMillis());
			_stats.setScrollCount(total.getScrollCount());
			_stats.setScrollCurrent(total.getScrollCurrent());
			_stats.setScrollCurrent(total.getScrollCurrent());
			_stats.setSuggestCount(total.getSuggestCount());
			_stats.setSuggestCurrent(total.getSuggestCurrent());
			_stats.setSuggestTimeInMillis(total.getSuggestTimeInMillis());
			_searchStats.setTotal(_stats);

			Map<String, com.braintribe.model.elasticsearchreflection.nodestats.SearchStatsEntry> _groupStats = new HashMap<>();
			Map<String, Stats> groupStats = search.getGroupStats();
			if (groupStats != null) {
				for (Map.Entry<String, Stats> groupStat : groupStats.entrySet()) {
					com.braintribe.model.elasticsearchreflection.nodestats.SearchStatsEntry _groupStatsValue = com.braintribe.model.elasticsearchreflection.nodestats.SearchStatsEntry.T
							.create();
					String key = groupStat.getKey();
					Stats value = groupStat.getValue();

					_groupStatsValue.setFetchCount(value.getFetchCount());
					_groupStatsValue.setFetchCurrent(value.getFetchCurrent());
					_groupStatsValue.setFetchTimeInMillis(value.getFetchTimeInMillis());
					_groupStatsValue.setQueryCount(value.getQueryCount());
					_groupStatsValue.setQueryCurrent(value.getQueryCurrent());
					_groupStatsValue.setQueryTimeInMillis(value.getQueryTimeInMillis());
					_groupStatsValue.setScrollCount(value.getScrollCount());
					_groupStatsValue.setScrollCurrent(value.getScrollCurrent());
					_groupStatsValue.setScrollCurrent(value.getScrollCurrent());
					_groupStatsValue.setSuggestCount(value.getSuggestCount());
					_groupStatsValue.setSuggestCurrent(value.getSuggestCurrent());
					_groupStatsValue.setSuggestTimeInMillis(value.getSuggestTimeInMillis());

					_groupStats.put(key, _groupStatsValue);
				}
			}
			_searchStats.setGroupStats(_groupStats);

			// ????????????

			com.braintribe.model.elasticsearchreflection.nodestats.MergeStats _mergeStats = com.braintribe.model.elasticsearchreflection.nodestats.MergeStats.T
					.create();
			_mergeStats.setTotal(merge.getTotal());
			_mergeStats.setTotalTimeInMillis(merge.getTotalTimeInMillis());
			_mergeStats.setTotalNumDocs(merge.getTotalNumDocs());
			_mergeStats.setTotalSizeInBytes(merge.getTotalSizeInBytes());
			_mergeStats.setCurrent(merge.getCurrent());
			_mergeStats.setCurrentNumDocs(merge.getCurrentNumDocs());
			_mergeStats.setCurrentSizeInBytes(merge.getCurrentSizeInBytes());
			_mergeStats.setTotalStoppedTimeInMillis(merge.getTotalStoppedTimeInMillis());
			_mergeStats.setTotalThrottledTimeInMillis(merge.getTotalThrottledTimeInMillis());
			_mergeStats.setTotalBytesPerSecAutoThrottle(merge.getTotalBytesPerSecAutoThrottle());

			com.braintribe.model.elasticsearchreflection.nodestats.RefreshStats _refreshStats = com.braintribe.model.elasticsearchreflection.nodestats.RefreshStats.T
					.create();
			_refreshStats.setTotal(refresh.getTotal());
			_refreshStats.setTotalTimeInMillis(refresh.getTotalTimeInMillis());
			_refreshStats.setListeners(refresh.getListeners());

			com.braintribe.model.elasticsearchreflection.nodestats.FlushStats _flushStats = com.braintribe.model.elasticsearchreflection.nodestats.FlushStats.T
					.create();
			_flushStats.setTotal(flush.getTotal());
			_flushStats.setTotalTimeInMillis(flush.getTotalTimeInMillis());

			com.braintribe.model.elasticsearchreflection.nodestats.FieldDataStats _fieldDataStats = com.braintribe.model.elasticsearchreflection.nodestats.FieldDataStats.T
					.create();
			_fieldDataStats.setMemorySizeInBytes(fieldData.getMemorySizeInBytes());
			_fieldDataStats.setEvictions(fieldData.getEvictions());

			com.braintribe.model.elasticsearchreflection.nodestats.QueryCacheStats _queryCacheStats = com.braintribe.model.elasticsearchreflection.nodestats.QueryCacheStats.T
					.create();
			_queryCacheStats.setMemorySizeInBytes(queryCache.getMemorySizeInBytes());
			_queryCacheStats.setCacheCount(queryCache.getCacheCount());
			_queryCacheStats.setCacheSize(queryCache.getCacheSize());
			_queryCacheStats.setHitCount(queryCache.getHitCount());
			_queryCacheStats.setMissCount(queryCache.getMissCount());

			com.braintribe.model.elasticsearchreflection.nodestats.RequestCacheStats _requestCacheStats = com.braintribe.model.elasticsearchreflection.nodestats.RequestCacheStats.T
					.create();
			_requestCacheStats.setMemorySizeInBytes(requestCache.getMemorySizeInBytes());
			_requestCacheStats.setEvictions(requestCache.getEvictions());
			_requestCacheStats.setHitCount(requestCache.getHitCount());
			_requestCacheStats.setMissCount(requestCache.getMissCount());

			com.braintribe.model.elasticsearchreflection.nodestats.CompletionStats _completionStats = com.braintribe.model.elasticsearchreflection.nodestats.CompletionStats.T
					.create();
			_completionStats.setSizeInBytes(completion.getSizeInBytes());

			com.braintribe.model.elasticsearchreflection.nodestats.SegmentsStats _segmentsStats = com.braintribe.model.elasticsearchreflection.nodestats.SegmentsStats.T
					.create();
			_segmentsStats.setBitsetMemoryInBytes(segments.getBitsetMemoryInBytes());
			_segmentsStats.setCount(segments.getCount());
			_segmentsStats.setDocValuesMemoryInBytes(segments.getDocValuesMemoryInBytes());
			Map<String, Long> map = new HashMap<>();
			segments.getFileSizes().forEach(item -> {
				map.put(item.key, item.value);
			});
			_segmentsStats.setFileSizes(map);
			_segmentsStats.setIndexWriterMemoryInBytes(segments.getIndexWriterMemoryInBytes());
			_segmentsStats.setMaxUnsafeAutoIdTimestamp(segments.getMaxUnsafeAutoIdTimestamp());
			_segmentsStats.setMemoryInBytes(segments.getMemoryInBytes());
			_segmentsStats.setNormsMemoryInBytes(segments.getNormsMemoryInBytes());
			_segmentsStats.setPointsMemoryInBytes(segments.getPointsMemoryInBytes());
			_segmentsStats.setStoredFieldsMemoryInBytes(segments.getStoredFieldsMemoryInBytes());
			_segmentsStats.setTermsMemoryInBytes(segments.getTermsMemoryInBytes());
			_segmentsStats.setTermVectorsMemoryInBytes(segments.getTermVectorsMemoryInBytes());
			_segmentsStats.setVersionMapMemoryInBytes(segments.getVersionMapMemoryInBytes());

			com.braintribe.model.elasticsearchreflection.nodestats.NodeStats _nodeStats = com.braintribe.model.elasticsearchreflection.nodestats.NodeStats.T
					.create();
			_nodeStatsList.add(_nodeStats);
			com.braintribe.model.elasticsearchreflection.nodestats.NodeIndicesStats _nodeIndicesStats = com.braintribe.model.elasticsearchreflection.nodestats.NodeIndicesStats.T
					.create();
			_nodeStats.setIndices(_nodeIndicesStats);

			_nodeIndicesStats.setStore(_storeStats);
			_nodeIndicesStats.setDocs(_docsStats);
			_nodeIndicesStats.setIndexingStats(_indexingStats);
			_nodeIndicesStats.setGet(_getStats);
			_nodeIndicesStats.setSearch(_searchStats);
			_nodeIndicesStats.setMerge(_mergeStats);
			_nodeIndicesStats.setRefresh(_refreshStats);
			_nodeIndicesStats.setFlush(_flushStats);
			_nodeIndicesStats.setFieldData(_fieldDataStats);
			_nodeIndicesStats.setQueryCache(_queryCacheStats);
			_nodeIndicesStats.setRequestCache(_requestCacheStats);
			_nodeIndicesStats.setCompletion(_completionStats);
			_nodeIndicesStats.setSegments(_segmentsStats);

		}

	}

	private static void addPendingClusterTasks(ClusterAdminClient clusterAdminClient, ElasticsearchReflection elasticsearchReflection) {
		PendingClusterTasksResponse pendingClusterTasksResponse = clusterAdminClient.preparePendingClusterTasks().get();

		List<PendingClusterTask> pendingTasks = pendingClusterTasksResponse.getPendingTasks();

		com.braintribe.model.elasticsearchreflection.tasks.PendingClusterTasks _pendingClusterTasks = com.braintribe.model.elasticsearchreflection.tasks.PendingClusterTasks.T
				.create();
		elasticsearchReflection.setPendingClusterTasks(_pendingClusterTasks);
		List<com.braintribe.model.elasticsearchreflection.tasks.PendingClusterTask> pct = new ArrayList<>();
		_pendingClusterTasks.setPendingClusterTasks(pct);

		if (pendingTasks != null) {
			for (PendingClusterTask pendingTask : pendingTasks) {
				com.braintribe.model.elasticsearchreflection.tasks.PendingClusterTask _pendingClusterTask = com.braintribe.model.elasticsearchreflection.tasks.PendingClusterTask.T
						.create();
				pct.add(_pendingClusterTask);

				_pendingClusterTask.setExecuting(pendingTask.isExecuting());
				_pendingClusterTask.setInsertOrder(pendingTask.getInsertOrder());

				Priority priority = pendingTask.getPriority();

				com.braintribe.model.elasticsearchreflection.tasks.Priority _priority = null;
				switch (priority) {
					case HIGH:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.HIGH;
						break;
					case IMMEDIATE:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.IMMEDIATE;
						break;
					case LANGUID:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.LANGUID;
						break;
					case LOW:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.LOW;
						break;
					case NORMAL:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.NORMAL;
						break;
					case URGENT:
						_priority = com.braintribe.model.elasticsearchreflection.tasks.Priority.URGENT;
						break;
					default:
						break;
				}

				_pendingClusterTask.setPriority(_priority);
				_pendingClusterTask.setSource(pendingTask.getSource().toString());
				_pendingClusterTask.setTimeInQueueInMillis(pendingTask.getTimeInQueueInMillis());
			}
		}

	}

	private static void addNodeInfo(Client client, ElasticsearchReflection elasticsearchReflection) {

		NodeInformation _nodeInformation = NodeInformation.T.create();

		elasticsearchReflection.setNodeInformation(_nodeInformation);

		NodesInfoResponse nodesInfoResponse = client.execute(NodesInfoAction.INSTANCE, new NodesInfoRequest()).actionGet();

		List<NodeInfo> nodeInfos = nodesInfoResponse.getNodes();

		List<com.braintribe.model.elasticsearchreflection.nodeinformation.NodeInfo> _nodeInfos = new ArrayList<>();

		_nodeInformation.setNodeInfo(_nodeInfos);

		if (nodeInfos != null) {
			for (NodeInfo nodeInfo : nodeInfos) {

				com.braintribe.model.elasticsearchreflection.nodeinformation.NodeInfo _nodeInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.NodeInfo.T
						.create();
				_nodeInfos.add(_nodeInfo);

				String hostname = nodeInfo.getHostname();

				_nodeInfo.setHostName(hostname);

				// skip HttpInfo
				// TODO: do not know what the following are:
				// nodeInfo.getHttp();
				// skip it ;)
				@SuppressWarnings("unused")
				HttpInfo http = nodeInfo.getHttp();

				com.braintribe.model.elasticsearchreflection.nodeinformation.JvmInfo _jvmInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.JvmInfo.T
						.create();

				_nodeInfo.setJvmInfo(_jvmInfo);

				JvmInfo jvmInfo = nodeInfo.getJvm();
				String bootClassPath = jvmInfo.getBootClassPath();
				String classPath = jvmInfo.getClassPath();
				String[] inputArguments = jvmInfo.getInputArguments();
				Mem mem = jvmInfo.getMem();
				ByteSizeValue directMemoryMax = mem.getDirectMemoryMax();
				ByteSizeValue heapInit = mem.getHeapInit();
				ByteSizeValue heapMax = mem.getHeapMax();
				ByteSizeValue nonHeapInit = mem.getNonHeapInit();
				ByteSizeValue nonHeapMax = mem.getNonHeapMax();
				long pid = jvmInfo.getPid();
				long startTime = jvmInfo.getStartTime();
				Map<String, String> systemProperties = jvmInfo.getSystemProperties();
				String version = jvmInfo.getVersion();
				String vmName = jvmInfo.getVmName();
				String vmVendor = jvmInfo.getVmVendor();
				String vmVersion = jvmInfo.getVmVersion();

				_jvmInfo.setBootClassPath(bootClassPath);
				_jvmInfo.setClassPath(classPath);
				_jvmInfo.setInputArguments(Arrays.asList(inputArguments));

				com.braintribe.model.elasticsearchreflection.nodeinformation.Mem _mem = com.braintribe.model.elasticsearchreflection.nodeinformation.Mem.T
						.create();
				_mem.setDirectMemoryMax(directMemoryMax.toString());
				_mem.setHeapInit(heapInit.toString());
				_mem.setHeapMax(heapMax.toString());
				_mem.setNonHeapInit(nonHeapInit.toString());
				_mem.setNonHeapMax(nonHeapMax.toString());
				_jvmInfo.setMem(_mem);
				_jvmInfo.setPid(pid);
				_jvmInfo.setStartTime(new Date(startTime));
				_jvmInfo.setSystemProperties(systemProperties);
				_jvmInfo.setVersion(version);
				_jvmInfo.setVmName(vmName);
				_jvmInfo.setVmVendor(vmVendor);
				_jvmInfo.setVmVersion(vmVersion);

				com.braintribe.model.elasticsearchreflection.nodeinformation.DiscoveryNode _discoveryNode = com.braintribe.model.elasticsearchreflection.nodeinformation.DiscoveryNode.T
						.create();
				_nodeInfo.setNode(_discoveryNode);

				com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress _transportAddress = com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress.T
						.create();
				_discoveryNode.setAddress(_transportAddress);

				DiscoveryNode discoveryNode = nodeInfo.getNode();
				TransportAddress address2 = discoveryNode.getAddress();

				String address3 = address2.getAddress();
				String host = address2.getHost();
				int port = address2.getPort();

				_transportAddress.setAddress(address3);
				_transportAddress.setHost(host);
				_transportAddress.setPort(port);

				Map<String, String> attributes = discoveryNode.getAttributes();
				String hostAddress = discoveryNode.getHostAddress();
				String hostName2 = discoveryNode.getHostName();
				String id = discoveryNode.getId();
				String name = discoveryNode.getName();
				Version version2 = discoveryNode.getVersion();

				_discoveryNode.setAttributes(attributes);
				_discoveryNode.setHostAddress(hostAddress);
				_discoveryNode.setHostName(hostName2);
				_discoveryNode.setDiscoveryNodeId(id);
				_discoveryNode.setName(name);
				_discoveryNode.setVersion(version2.toString());

				com.braintribe.model.elasticsearchreflection.nodeinformation.OsInfo _osInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.OsInfo.T
						.create();
				_nodeInfo.setOsInfo(_osInfo);

				OsInfo osInfo = nodeInfo.getOs();
				int allocatedProcessors = osInfo.getAllocatedProcessors();
				String arch = osInfo.getArch();
				int availableProcessors = osInfo.getAvailableProcessors();
				String name2 = osInfo.getName();
				long refreshInterval = osInfo.getRefreshInterval();
				String version3 = osInfo.getVersion();

				_osInfo.setAllocatedProcessors(allocatedProcessors);
				_osInfo.setArch(arch);
				_osInfo.setAvailableProcessors(availableProcessors);
				_osInfo.setName(name2);
				_osInfo.setRefreshInterval(refreshInterval);
				_osInfo.setVersion(version3);

				PluginsAndModules plugins = nodeInfo.getPlugins();
				List<PluginInfo> moduleInfos = plugins.getModuleInfos();

				com.braintribe.model.elasticsearchreflection.nodeinformation.PluginsAndModules _pluginsAndModules = com.braintribe.model.elasticsearchreflection.nodeinformation.PluginsAndModules.T
						.create();
				_nodeInfo.setPluginsAndModules(_pluginsAndModules);
				List<com.braintribe.model.elasticsearchreflection.nodeinformation.PluginInfo> _moduleInfos = new ArrayList<>();
				List<com.braintribe.model.elasticsearchreflection.nodeinformation.PluginInfo> _pluginInfos = new ArrayList<>();
				_pluginsAndModules.setModuleInfos(_moduleInfos);
				_pluginsAndModules.setPluginInfos(_pluginInfos);

				if (moduleInfos != null) {
					for (PluginInfo moduleInfo : moduleInfos) {
						addPluginInfo(_moduleInfos, moduleInfo);
					}
				}

				List<PluginInfo> pluginInfos = plugins.getPluginInfos();
				if (pluginInfos != null) {
					for (PluginInfo pluginInfo : pluginInfos) {
						addPluginInfo(_pluginInfos, pluginInfo);
					}
				}

				com.braintribe.model.elasticsearchreflection.nodeinformation.ProcessInfo _processInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.ProcessInfo.T
						.create();
				_nodeInfo.setProcessInfo(_processInfo);

				ProcessInfo processInfo = nodeInfo.getProcess();
				long id2 = processInfo.getId();
				long refreshInterval2 = processInfo.getRefreshInterval();
				_processInfo.setProcessInfoId(id2);
				_processInfo.setRefreshInterval(refreshInterval2);

				Settings settings = nodeInfo.getSettings();
				_nodeInfo.setSettings(settings.getAsMap());

				List<com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolInfo> _threadPoolInfos = new ArrayList<>();
				_nodeInfo.setThreadPoolInfos(_threadPoolInfos);

				ThreadPoolInfo threadPoolInfo = nodeInfo.getThreadPool();

				Iterator<Info> iterator = threadPoolInfo.iterator();
				while (iterator.hasNext()) {
					Info info = iterator.next();

					com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolInfo _threadPoolInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolInfo.T
							.create();
					_threadPoolInfos.add(_threadPoolInfo);

					TimeValue keepAlive = info.getKeepAlive();
					int max = info.getMax();
					int min = info.getMin();
					String name3 = info.getName();
					SizeValue queueSize = info.getQueueSize();
					ThreadPoolType threadPoolType = info.getThreadPoolType();

					_threadPoolInfo.setKeepAlive(keepAlive == null ? "" : keepAlive.toString());
					_threadPoolInfo.setMax(max);
					_threadPoolInfo.setMin(min);
					_threadPoolInfo.setName(name3);
					_threadPoolInfo.setQueueSize(queueSize == null ? "" : queueSize.toString());

					com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolType _threadPoolType;

					if (threadPoolType == ThreadPoolType.DIRECT) {
						_threadPoolType = com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolType.direct;

					} else if (threadPoolType == ThreadPoolType.FIXED) {
						_threadPoolType = com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolType.fixed;

					} else {
						// threadPoolType==ThreadPoolType.SCALING
						_threadPoolType = com.braintribe.model.elasticsearchreflection.nodeinformation.ThreadPoolType.scaling;

					}
					_threadPoolInfo.setThreadPoolType(_threadPoolType);

				}

				com.braintribe.model.elasticsearchreflection.nodeinformation.TransportInfo _transportInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.TransportInfo.T
						.create();
				_nodeInfo.setTransport(_transportInfo);

				com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress _boundTransportAddress = com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress.T
						.create();
				Map<String, com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress> _profileAddresses = new HashMap<>();
				_transportInfo.setAddress(_boundTransportAddress);
				_transportInfo.setProfileAddresses(_profileAddresses);

				TransportInfo transport = nodeInfo.getTransport();
				BoundTransportAddress boundTransportAddress = transport.getAddress();
				TransportAddress[] boundAddresses = boundTransportAddress.boundAddresses();
				List<com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress> _boundAddresses = new ArrayList<>();
				_boundTransportAddress.setBoundAddresses(_boundAddresses);
				if (boundAddresses != null) {
					for (TransportAddress boundAddress : boundAddresses) {
						addProfileAddressesBoundTransportAddress(_boundAddresses, boundAddress);
					}
				}

				com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress _publishAddress = com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress.T
						.create();
				_boundTransportAddress.setPublishAddress(_publishAddress);
				TransportAddress publishAddress = boundTransportAddress.publishAddress();
				String publishAddressAddress = publishAddress.getAddress();
				String publishAddressHost = publishAddress.getHost();
				int publishAddressPort = publishAddress.getPort();

				_publishAddress.setAddress(publishAddressAddress);
				_publishAddress.setHost(publishAddressHost);
				_publishAddress.setPort(publishAddressPort);

				Map<String, BoundTransportAddress> profileAddresses = transport.profileAddresses();
				if (profileAddresses != null) {

					for (Map.Entry<String, BoundTransportAddress> profileAddress : profileAddresses.entrySet()) {
						addProfileAddresses(_profileAddresses, profileAddress);
					}
				}

				Version version4 = nodeInfo.getVersion();
				_nodeInfo.setVersion(version4.toString());
			}
		}
	}

	private static void addPluginInfo(List<com.braintribe.model.elasticsearchreflection.nodeinformation.PluginInfo> _moduleInfos,
			PluginInfo moduleInfo) {
		com.braintribe.model.elasticsearchreflection.nodeinformation.PluginInfo _moduleInfo = com.braintribe.model.elasticsearchreflection.nodeinformation.PluginInfo.T
				.create();
		_moduleInfos.add(_moduleInfo);

		String classname = moduleInfo.getClassname();
		String description = moduleInfo.getDescription();
		String name3 = moduleInfo.getName();
		String version4 = moduleInfo.getVersion();

		_moduleInfo.setClassname(classname);
		_moduleInfo.setDescription(description);
		_moduleInfo.setName(name3);
		_moduleInfo.setVersion(version4);
	}

	private static void addProfileAddresses(
			Map<String, com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress> _profileAddresses,
			Map.Entry<String, BoundTransportAddress> profileAddress) {
		String key = profileAddress.getKey();
		BoundTransportAddress value = profileAddress.getValue();
		TransportAddress[] boundAddresses2 = value.boundAddresses();

		com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress _value = com.braintribe.model.elasticsearchreflection.nodeinformation.BoundTransportAddress.T
				.create();

		List<com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress> _boundAddresses2 = new ArrayList<>();

		_value.setBoundAddresses(_boundAddresses2);

		if (boundAddresses2 != null) {
			for (TransportAddress boundAddress : boundAddresses2) {
				addProfileAddressesBoundTransportAddress(_boundAddresses2, boundAddress);

			}
		}
		TransportAddress publishAddress2 = value.publishAddress();
		String address4 = publishAddress2.getAddress();
		String host3 = publishAddress2.getHost();
		int port3 = publishAddress2.getPort();

		com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress _transportAddress2 = com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress.T
				.create();
		_value.setPublishAddress(_transportAddress2);

		_transportAddress2.setAddress(address4);
		_transportAddress2.setHost(host3);
		_transportAddress2.setPort(port3);

		_profileAddresses.put(key, _value);
	}

	private static void addProfileAddressesBoundTransportAddress(
			List<com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress> _boundAddresses2, TransportAddress boundAddress) {
		com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress _createEntity = com.braintribe.model.elasticsearchreflection.nodeinformation.TransportAddress.T
				.create();
		_boundAddresses2.add(_createEntity);

		String address4 = boundAddress.getAddress();
		String host3 = boundAddress.getHost();
		int port3 = boundAddress.getPort();

		_createEntity.setAddress(address4);
		_createEntity.setHost(host3);
		_createEntity.setPort(port3);
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static void addClusterHealth(ClusterAdminClient clusterAdminClient, ElasticsearchReflection elasticsearchReflection) {
		ClusterHealthResponse healths = clusterAdminClient.prepareHealth().get();

		// TODO: do not know what the following are:
		// healths.getContext();
		// healths.getHeaders();
		// skip it ;)

		int activePrimaryShards = healths.getActivePrimaryShards();
		int activeShards = healths.getActiveShards();
		double activeShardsPercent = healths.getActiveShardsPercent();
		String clusterName = healths.getClusterName();
		int delayedUnassignedShards = healths.getDelayedUnassignedShards();
		Map<String, ClusterIndexHealth> indices = healths.getIndices();
		int initializingShards = healths.getInitializingShards();
		int numberOfDataNodes = healths.getNumberOfDataNodes();
		int numberOfNodes = healths.getNumberOfNodes();
		int numberOfPendingTasks = healths.getNumberOfPendingTasks();
		int relocatingShards = healths.getRelocatingShards();
		ClusterHealthStatus status = healths.getStatus();
		TimeValue taskMaxWaitingTime = healths.getTaskMaxWaitingTime();
		int unassignedShards = healths.getUnassignedShards();

		ClusterHealth clusterHealth = ClusterHealth.T.create();
		elasticsearchReflection.setClusterHealth(clusterHealth);

		clusterHealth.setActivePrimaryShards(activePrimaryShards);
		clusterHealth.setActiveShards(activeShards);
		clusterHealth.setActiveShardsPercent(activeShardsPercent);
		clusterHealth.setClusterName(clusterName);
		clusterHealth.setDelayedUnassignedShards(delayedUnassignedShards);

		Map<String, com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterIndexHealth> _indices = new HashMap<>();

		if (indices != null) {
			for (Map.Entry<String, ClusterIndexHealth> index : indices.entrySet()) {
				addClusterIndexHealth(_indices, index);
			}
		}

		clusterHealth.setIndices(_indices);
		clusterHealth.setInitializingShards(initializingShards);
		clusterHealth.setNumberOfDataNodes(numberOfDataNodes);
		clusterHealth.setNumberOfNodes(numberOfNodes);
		clusterHealth.setNumberOfPendingTasks(numberOfPendingTasks);
		clusterHealth.setRelocatingShards(relocatingShards);

		com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus _status;
		if (status == ClusterHealthStatus.GREEN) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.GREEN;
		} else if (status == ClusterHealthStatus.YELLOW) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.YELLOW;
		} else {
			// must be ClusterHealthStatus.RED
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.RED;
		}
		clusterHealth.setStatus(_status);
		clusterHealth.setTaskMaxWaitingTime(taskMaxWaitingTime.toString());
		clusterHealth.setUnassignedShards(unassignedShards);
	}

	private static void addClusterIndexHealth(Map<String, com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterIndexHealth> _indices,
			Map.Entry<String, ClusterIndexHealth> indexEntry) {
		String key = indexEntry.getKey();
		ClusterIndexHealth value = indexEntry.getValue();
		int activePrimaryShards = value.getActivePrimaryShards();
		int activeShards = value.getActiveShards();
		String index = value.getIndex();
		int initializingShards = value.getInitializingShards();
		int numberOfReplicas = value.getNumberOfReplicas();
		int numberOfShards = value.getNumberOfShards();
		int relocatingShards = value.getRelocatingShards();
		Map<Integer, ClusterShardHealth> shards = value.getShards();
		ClusterHealthStatus status = value.getStatus();
		int unassignedShards = value.getUnassignedShards();

		com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterIndexHealth clusterIndexHealth = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterIndexHealth.T
				.create();

		clusterIndexHealth.setActivePrimaryShards(activePrimaryShards);
		clusterIndexHealth.setActiveShards(activeShards);
		clusterIndexHealth.setIndex(index);
		clusterIndexHealth.setInitializingShards(initializingShards);
		clusterIndexHealth.setNumberOfReplicas(numberOfReplicas);
		clusterIndexHealth.setNumberOfShards(numberOfShards);
		clusterIndexHealth.setRelocatingShards(relocatingShards);

		Map<Integer, com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterShardHealth> _shards = new HashMap<>();
		if (shards != null) {
			for (Map.Entry<Integer, ClusterShardHealth> shard : shards.entrySet()) {

				addClusterShardHealth(_shards, shard);
			}
		}

		clusterIndexHealth.setShards(_shards);

		com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus _status;
		if (status == ClusterHealthStatus.GREEN) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.GREEN;
		} else if (status == ClusterHealthStatus.YELLOW) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.YELLOW;
		} else {
			// must be ClusterHealthStatus.RED
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.RED;
		}
		clusterIndexHealth.setStatus(_status);
		clusterIndexHealth.setUnassignedShards(unassignedShards);

		_indices.put(key, clusterIndexHealth);
	}

	private static void addClusterShardHealth(Map<Integer, com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterShardHealth> _shards,
			Map.Entry<Integer, ClusterShardHealth> shardEntry) {
		Integer key = shardEntry.getKey();
		ClusterShardHealth value = shardEntry.getValue();

		int activeShards = value.getActiveShards();
		int id = value.getId();
		boolean primaryActive = value.isPrimaryActive();
		int initializingShards = value.getInitializingShards();
		int relocatingShards = value.getRelocatingShards();
		ClusterHealthStatus status = value.getStatus();
		int unassignedShards = value.getUnassignedShards();

		com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterShardHealth _clusterShardHealth = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterShardHealth.T
				.create();

		_clusterShardHealth.setActiveShards(activeShards);
		_clusterShardHealth.setShardId(id);
		_clusterShardHealth.setPrimaryActive(primaryActive);
		_clusterShardHealth.setInitializingShards(initializingShards);
		_clusterShardHealth.setRelocatingShards(relocatingShards);

		com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus _status;
		if (status == ClusterHealthStatus.GREEN) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.GREEN;
		} else if (status == ClusterHealthStatus.YELLOW) {
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.YELLOW;
		} else {
			// must be ClusterHealthStatus.RED
			_status = com.braintribe.model.elasticsearchreflection.clusterhealth.ClusterHealthStatus.RED;
		}
		_clusterShardHealth.setStatus(_status);
		_clusterShardHealth.setUnassignedShards(unassignedShards);

		_shards.put(key, _clusterShardHealth);
	}
}
