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
package com.braintribe.model.processing.aws.connect;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.logging.Logger;
import com.braintribe.model.aws.api.ConnectionStatistics;
import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.model.processing.aws.util.ErrorCodes;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.RangeInputStream;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.internal.impl.ConnectionManagerAwareHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.BucketLocationConstraint;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class S3ConnectionImpl implements S3Connector, DestructionAware {

	private static Logger logger = Logger.getLogger(S3ConnectionImpl.class);

	private com.braintribe.model.aws.deployment.S3Connector s3ConnectorDeployable;
	private S3Client masterClient;
	private Map<Region, S3Client> clientPerRegion = new ConcurrentHashMap<>();
	private Map<String, S3Client> clientPerBucket = new ConcurrentHashMap<>();
	private Map<String, Region> regionPerBucket = new ConcurrentHashMap<>();
	private Set<String> knownBucketNames = Collections.synchronizedSet(new HashSet<>());
	private Region defaultRegion;
	private ExtendedThreadPoolExecutor threadPool;
	private ReentrantLock threadPoolLock = new ReentrantLock();
	private Integer httpConnectionPoolSize;
	private Long connectionAcquisitionTimeout;
	private Long connectionTimeout;
	private Long socketTimeout;
	private Set<SdkHttpClient> httpClients = Collections.synchronizedSet(new HashSet<>());
	private Map<Region, PoolingHttpClientConnectionManager> poolStatsPerRegion = new ConcurrentHashMap<>();

	private static int uploadBufferSize = (int) (Numbers.MEBIBYTE * 6);

	public S3Client getMasterClient() {
		if (masterClient != null)
			return masterClient;

		masterClient = getClient(getDefaultRegion());
		return masterClient;
	}

	private Region getDefaultRegion() {
		if (defaultRegion == null) {
			S3Region region = s3ConnectorDeployable.getRegion();
			if (region == null) {
				region = S3Region.eu_west_1;
			}
			String regionString = region.name().replace('_', '-');
			defaultRegion = Region.of(regionString);
		}
		return defaultRegion;
	}

	private ExtendedThreadPoolExecutor getThreadPool() {
		if (threadPool != null) {
			return threadPool;
		}
		threadPoolLock.lock();
		try {
			if (threadPool != null) {
				return threadPool;
			}

			Integer poolSize = s3ConnectorDeployable.getStreamingPoolSize();
			if (poolSize == null || poolSize < 1) {
				poolSize = 10;
			}
			threadPool = new ExtendedThreadPoolExecutor(poolSize, poolSize, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
			threadPool.allowCoreThreadTimeOut(true);
			threadPool.setThreadNamePrefix("s3-streaming-");
		} finally {
			threadPoolLock.unlock();
		}
		return threadPool;
	}

	@Override
	public Set<String> getBucketsList() {

		S3Client client = getMasterClient();

		logger.debug(() -> "Listing all buckets on AWS account");
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		try {
			ListBucketsResponse listBucketsResponse = client.listBuckets(listBucketsRequest);

			Set<String> bucketList = listBucketsResponse.buckets().stream().map(x -> x.name()).collect(Collectors.toSet());

			return bucketList;
		} catch (S3Exception s3e) {

			final String context;
			AwsErrorDetails details = s3e.awsErrorDetails();
			if (details != null) {
				String errorCode = details.errorCode();
				context = errorCode + " (" + ErrorCodes.getErrorCodeDetails(errorCode) + ")";

				if (errorCode != null && ErrorCodes.isAccessDeniedByAcl(errorCode)) {
					logger.debug(() -> "We do not have access to the list of buckets with this user (" + context + ").");
					return Collections.EMPTY_SET;
				}

			} else {
				context = "n/a";
			}
			throw Exceptions.unchecked(s3e, context);

		}
	}

	@Override
	public List<String> getFilesList(String bucketName) {
		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
		ListObjectsV2Response response;
		List<String> bucketObjects = new ArrayList<>();

		S3Client client = ensureBucket(bucketName, getDefaultRegion());
		try {
			do {
				logger.debug(() -> "Creating list bucket objects request");
				response = client.listObjectsV2(request);

				for (S3Object s3Object : response.contents()) {
					bucketObjects.add(s3Object.key());
				}

				String token = response.continuationToken();
				logger.debug(() -> "Next Continuation Token: " + token);
				if (StringTools.isBlank(token)) {
					break;
				}
				request = ListObjectsV2Request.builder().bucket(bucketName).continuationToken(token).build();

			} while (response.isTruncated());
		} catch (SdkException e) {
			throw Exceptions.unchecked(e, "AWS S3 exception at getFilesList");
		}
		return bucketObjects;
	}

	@Override
	public void uploadFile(String bucketName, String key, InputStream in, Long fileSize, String contentType) {

		S3Client client = ensureBucket(bucketName, getDefaultRegion());

		if (fileSize != null && fileSize > 0) {
			software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest.Builder builder = CreateMultipartUploadRequest.builder().bucket(bucketName).key(key);
			if (!StringTools.isBlank(contentType)) {
				builder.contentType(contentType);
			}
			CreateMultipartUploadRequest createMultipartUploadRequest = builder.build();

			CreateMultipartUploadResponse response = client.createMultipartUpload(createMultipartUploadRequest);
			String uploadId = response.uploadId();
			logger.debug(() -> "Uploading " + key + " with upload Id: " + uploadId);

			doMultipartUpload(bucketName, key, in, client, uploadId);
		} else {
			software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder builder = PutObjectRequest.builder().bucket(bucketName).key(key);
			if (!StringTools.isBlank(contentType)) {
				builder.contentType(contentType);
			}
			PutObjectRequest putObjectRequest = builder.build();
			client.putObject(putObjectRequest, RequestBody.fromBytes(new byte[0]));
		}
	}

	private static class MultiUploadPart {
		int partNumber;
		long startInclusive;
		long endExclusive;
		String etag;
		CompletedPart completedPart;

		long getLength() {
			return endExclusive - startInclusive;
		}
		@Override
		public String toString() {
			return "Upload part " + partNumber + ", " + startInclusive + "-" + endExclusive + " (length: " + (endExclusive - startInclusive)
					+ "), etag: " + etag;
		}
	}

	private static List<MultiUploadPart> splitParts(long length, long blockSize) {
		int partCount = (int) (length / blockSize);
		if ((length % blockSize) > 0) {
			partCount++;
		}
		List<MultiUploadPart> parts = new ArrayList<>(partCount);
		for (int i = 0; i < partCount; ++i) {

			MultiUploadPart part = new MultiUploadPart();
			part.partNumber = (i + 1);
			part.startInclusive = i * blockSize;
			part.endExclusive = (i + 1) * blockSize;
			if (i == (partCount - 1)) {
				part.endExclusive = length;
			}
			parts.add(part);
		}
		return parts;
	}

	private MultiUploadPart uploadPart(File file, MultiUploadPart part, String key, String bucketName, String uploadId, S3Client client) {

		logger.debug(() -> "Uploading part " + part + " of file " + file.getAbsolutePath());
		Instant start = NanoClock.INSTANCE.instant();

		try (InputStream in = new RangeInputStream(new BufferedInputStream(new FileInputStream(file)), part.startInclusive, part.endExclusive)) {

			try {
				int expectedLength = (int) part.getLength();
				byte[] buffer = new byte[expectedLength];
				int read = IOTools.readFully(in, buffer);
				if (read != expectedLength) {
					throw new RuntimeException(
							"Expected to read " + expectedLength + " bytes from file " + file + ", but only got " + read + " bytes for part " + part);
				}
				UploadPartRequest uploadPartRequest = UploadPartRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
						.partNumber(part.partNumber).build();
				ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
				part.etag = client.uploadPart(uploadPartRequest, RequestBody.fromByteBuffer(byteBuffer)).eTag();
				part.completedPart = CompletedPart.builder().partNumber(part.partNumber).eTag(part.etag).build();

				logger.debug(() -> "Uploaded part " + part + " of file " + file.getAbsolutePath() + " took "
						+ StringTools.prettyPrintDuration(start, true, null));
			} catch (IOException ioe) {
				throw Exceptions.unchecked(ioe, "Error while trying to upload " + key + " to S3 bucket " + bucketName);
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not upload part " + part + " of file " + file);
		}
		return part;
	}

	private void doMultipartUpload(String bucketName, String key, InputStream inputStream, S3Client client, String uploadId) {

		List<CompletedPart> parts = new ArrayList<>();
		StopWatch stopWatch = new StopWatch();

		try (BufferedInputStream bufferedIn = new BufferedInputStream(inputStream)) {

			byte[] buffer = new byte[uploadBufferSize];
			bufferedIn.mark(uploadBufferSize + 1024);
			int read = IOTools.readFully(bufferedIn, buffer);

			stopWatch.intermediate("Read ahead");

			if (read == buffer.length) {

				logger.debug(() -> "Size exceeds single upload threshold. Splitting file into parts for separate upload");

				bufferedIn.reset();

				File tempFile = null;
				try {
					tempFile = File.createTempFile("s3-upload", ".bin");
					IOTools.inputToFile(bufferedIn, tempFile);
					long length = tempFile.length();

					stopWatch.intermediate("Write Temp File (" + length + " Bytes)");

					List<MultiUploadPart> multiUploadParts = splitParts(length, uploadBufferSize);

					List<Future<MultiUploadPart>> futures = new ArrayList<>();
					ExecutorService executor = getThreadPool();

					final File sourceFile = tempFile;
					for (MultiUploadPart part : multiUploadParts) {
						futures.add(executor.submit(() -> uploadPart(sourceFile, part, key, bucketName, uploadId, client)));
					}

					stopWatch.intermediate("Submitting " + multiUploadParts.size() + " parts");

					for (Future<MultiUploadPart> future : futures) {
						try {
							MultiUploadPart mup = future.get();
							parts.add(mup.completedPart);
						} catch (InterruptedException ie) {
							logger.debug("Got interrupted while waiting for upload of part to bucket " + bucketName);
							throw ie;
						} catch (ExecutionException ee) {
							throw Exceptions.unchecked(ee, "Error while uploading a chunk to bucket " + bucketName);
						}
					}

					stopWatch.intermediate("Upload");

				} catch (Exception e) {
					throw Exceptions.unchecked(e, "Could not upload the input stream via a temporary file.");
				} finally {
					if (tempFile != null) {
						FileTools.deleteFileSilently(tempFile);
					}
				}

			} else {

				logger.debug(() -> "Upload input stream directly");

				int partNumber = 1;

				try {
					do {
						UploadPartRequest uploadPartRequest = UploadPartRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
								.partNumber(partNumber).build();
						ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
						String etag1 = client.uploadPart(uploadPartRequest, RequestBody.fromByteBuffer(byteBuffer)).eTag();
						CompletedPart part = CompletedPart.builder().partNumber(partNumber).eTag(etag1).build();

						partNumber++;
						parts.add(part);

					} while ((read = IOTools.readFully(bufferedIn, buffer)) != -1);

				} catch (IOException ioe) {
					throw Exceptions.unchecked(ioe, "Error while trying to upload " + key + " to S3 bucket " + bucketName);
				}

				stopWatch.intermediate("Upload single part (" + read + " Bytes)");

			}
			// Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
			// parts and finish the multipart operation.

			CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(parts).build();
			CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key)
					.uploadId(uploadId).multipartUpload(completedMultipartUpload).build();
			client.completeMultipartUpload(completeMultipartUploadRequest);

			stopWatch.intermediate("Complete Upload");

			logger.debug(() -> "Upload of " + key + " to bucket " + bucketName + ": " + stopWatch);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not upload stream to S3");
		}
	}

	@Override
	public void downloadFile(String bucketName, String key, OutputStream os, Long start, Long end) {

		Builder builder = GetObjectRequest.builder().bucket(bucketName).key(key);
		if (start != null && end != null) {
			builder.range("bytes=" + start + "-" + end);
		}
		GetObjectRequest request = builder.build();

		S3Client client = getClient(bucketName);
		try {
			client.getObject(request, ResponseTransformer.toOutputStream(os));

		} catch (SdkException e) {
			throw Exceptions.unchecked(e, "AWS S3 exception at downloadFile");
		}
	}

	@Override
	public InputStream openStream(String bucketName, String key, Long start, Long end) {

		Builder builder = GetObjectRequest.builder().bucket(bucketName).key(key);
		if (start != null && end != null) {
			builder.range("bytes=" + start + "-" + end);
		}
		GetObjectRequest request = builder.build();

		S3Client client = getClient(bucketName);
		try {
			ResponseInputStream<GetObjectResponse> inputStream = client.getObject(request, ResponseTransformer.toInputStream());
			return inputStream;

		} catch (SdkException e) {
			throw Exceptions.unchecked(e, "AWS S3 exception at downloadFile");
		}
	}

	@Override
	public void deleteFile(String bucketName, String key) {
		DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
		DeleteObjectResponse response;

		S3Client client = getClient(bucketName);
		try {
			response = client.deleteObject(request);
			logger.debug(() -> "Deleting " + key + " in bucket " + bucketName + ": " + response.deleteMarker());
		} catch (SdkException e) {
			throw Exceptions.unchecked(e, "AWS S3 exception at deleteFile");
		}
	}

	@Override
	public void preDestroy() {
		poolStatsPerRegion.clear();
		for (S3Client regionalClient : clientPerRegion.values()) {
			try {
				regionalClient.close();
			} catch (Exception e) {
				logger.debug(() -> "Error while closing S3 client.", e);
			}
		}
		if (threadPool != null) {
			try {
				threadPool.shutdown();
			} catch (Exception e) {
				logger.debug(() -> "Error while closing S3 thread pool.", e);
			}
		}
		if (!httpClients.isEmpty()) {
			for (SdkHttpClient client : httpClients) {
				try {
					client.close();
				} catch (Exception e) {
					logger.debug(() -> "Error while trying to close http client.", e);
				}
			}
		}
	}

	@Required
	@Configurable
	public void setS3ConnectorDeployable(com.braintribe.model.aws.deployment.S3Connector s3ConnectionDeployable) {
		this.s3ConnectorDeployable = s3ConnectionDeployable;
	}

	private S3Client getClient(Region region) {
		return clientPerRegion.computeIfAbsent(region, r -> {
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(s3ConnectorDeployable.getAwsAccessKey(),
					s3ConnectorDeployable.getAwsSecretAccessKey());
			StaticCredentialsProvider awsCredsProvider = StaticCredentialsProvider.create(awsCreds);

			logger.debug(() -> "Creating S3 Client for region " + region + " with pool size: " + httpConnectionPoolSize);

			S3ClientBuilder builder = S3Client.builder().region(region).credentialsProvider(awsCredsProvider);

			String urlOverride = s3ConnectorDeployable.getUrlOverride();
			if (!StringTools.isBlank(urlOverride)) {
				try {
					builder.endpointOverride(new URI(urlOverride));
				} catch (URISyntaxException e) {
					throw new IllegalStateException("Could not parse " + urlOverride + " as a URI", e);
				}
			}

			software.amazon.awssdk.http.apache.ApacheHttpClient.Builder httpClientBuilder = ApacheHttpClient.builder();

			if (httpConnectionPoolSize != null && httpConnectionPoolSize > 0) {
				httpClientBuilder.maxConnections(httpConnectionPoolSize).build();
			}
			if (connectionAcquisitionTimeout != null && connectionAcquisitionTimeout > 0) {
				httpClientBuilder.connectionAcquisitionTimeout(Duration.ofMillis(connectionAcquisitionTimeout));
			}
			if (connectionTimeout != null && connectionTimeout > 0) {
				httpClientBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout));
			}
			if (socketTimeout != null && socketTimeout > 0) {
				httpClientBuilder.socketTimeout(Duration.ofMillis(socketTimeout));
			}

			SdkHttpClient httpClient = httpClientBuilder.build();

			if (httpClient instanceof ApacheHttpClient) {
				ApacheHttpClient ahc = (ApacheHttpClient) httpClient;
				try {
					Field f = ApacheHttpClient.class.getDeclaredField("httpClient");
					f.setAccessible(true);
					ConnectionManagerAwareHttpClient innerClient = (ConnectionManagerAwareHttpClient) f.get(ahc);
					HttpClientConnectionManager connectionManager = innerClient.getHttpClientConnectionManager();
					if (connectionManager instanceof PoolingHttpClientConnectionManager) {
						PoolingHttpClientConnectionManager pcm = (PoolingHttpClientConnectionManager) connectionManager;
						poolStatsPerRegion.put(r, pcm);
					}
				} catch (Throwable t) {
					logger.debug(() -> "Could not access inner HTTP client. Deep inspection will not be possible.", t);
				}

			}
			httpClients.add(httpClient);

			builder.httpClient(httpClient);
			S3Client s3Client = builder.build();

			logger.debug(() -> "S3 Client created succesfully for region " + region);
			return s3Client;
		});
	}

	private S3Client getClient(String bucketName) {
		return clientPerBucket.computeIfAbsent(bucketName, bn -> {

			S3Client mClient = getMasterClient();
			GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder().bucket(bucketName).build();
			GetBucketLocationResponse bucketLocation = mClient.getBucketLocation(locationRequest);
			BucketLocationConstraint locationConstraint = bucketLocation.locationConstraint();
			String regionName = locationConstraint.toString();

			if (BucketLocationConstraint.knownValues().contains(locationConstraint)) {
				Region bucketRegion = Region.of(regionName);

				S3Client regionalClient = getClient(bucketRegion);
				return regionalClient;

			} else {
				logger.debug(() -> "Region " + regionName + " is not directly supported byte the Amazon SDK. Using master client using "
						+ getDefaultRegion());
				return mClient;
			}

		});
	}

	private Region getRegion(String bucketName) {
		return regionPerBucket.computeIfAbsent(bucketName, bn -> {

			S3Client mClient = getMasterClient();
			GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder().bucket(bucketName).build();
			GetBucketLocationResponse bucketLocation = mClient.getBucketLocation(locationRequest);
			BucketLocationConstraint locationConstraint = bucketLocation.locationConstraint();
			String regionName = locationConstraint.toString();

			if (BucketLocationConstraint.knownValues().contains(locationConstraint)) {
				Region bucketRegion = Region.of(regionName);
				return bucketRegion;

			} else {
				logger.debug(() -> "Region " + regionName + " is not directly supported byte the Amazon SDK. Using master client using "
						+ getDefaultRegion());
				return getDefaultRegion();
			}

		});
	}

	private S3Client ensureBucket(String bucketName, Region region) {
		if (knownBucketNames.contains(bucketName)) {
			return getClient(bucketName);
		}

		return clientPerBucket.computeIfAbsent(bucketName, bn -> {

			S3Client regionalClient = getClient(region);
			S3Client mClient = getMasterClient();

			try {
				ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
				ListBucketsResponse listBucketsResponse = mClient.listBuckets(listBucketsRequest);
				Set<String> bucketNames = listBucketsResponse.buckets().stream().map(x -> x.name()).collect(Collectors.toSet());
				knownBucketNames = Collections.synchronizedSet(bucketNames);

				if (!bucketNames.contains(bucketName)) {
					CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
					CreateBucketResponse createBucketResponse = regionalClient.createBucket(createBucketRequest);
					String location = createBucketResponse.location();
					logger.info("Created bucket " + bucketName + " in location " + location);
				}
			} catch (S3Exception s3e) {
				if (s3e.statusCode() == 403) {
					logger.debug(() -> "We do not have access to the list of buckets with this user.");
				} else {
					logger.info(() -> "An error occurred while trying to ensure bucket " + bn, s3e);
				}
			}

			return regionalClient;
		});
	}

	@Override
	public String generatePresignedUrl(String bucketName, String key, long ttlInMs) {

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(s3ConnectorDeployable.getAwsAccessKey(),
				s3ConnectorDeployable.getAwsSecretAccessKey());
		StaticCredentialsProvider awsCredsProvider = StaticCredentialsProvider.create(awsCreds);
		Region region = getRegion(bucketName);

		S3Presigner s3Presigner = S3Presigner.builder().region(region).credentialsProvider(awsCredsProvider).build();

		// S3Presigner s3Presigner = S3Presigner.create(); //How does that even work without credentials? But it does...

		PresignedGetObjectRequest presignedRequest = s3Presigner
				.presignGetObject(r -> r.getObjectRequest(get -> get.bucket(bucketName).key(key)).signatureDuration(Duration.ofMillis(ttlInMs)));
		boolean browserExecutable = presignedRequest.isBrowserExecutable();
		if (!browserExecutable) {
			throw new IllegalStateException(
					"Could not create a browser-compatible presigned link for bucket " + bucketName + " and key " + key + ": " + presignedRequest);
		}
		URL URL = presignedRequest.url();
		return URL.toString();

	}

	@Configurable
	public void setHttpConnectionPoolSize(Integer httpConnectionPoolSize) {
		this.httpConnectionPoolSize = httpConnectionPoolSize;
	}
	@Configurable
	public void setConnectionAcquisitionTimeout(Long connectionAcquisitionTimeout) {
		this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
	}
	@Configurable
	public void setConnectionTimeout(Long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	@Configurable
	public void setSocketTimeout(Long socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	@Override
	public Map<String, ConnectionStatistics> getStatisticsPerRegion() {
		Map<String, ConnectionStatistics> result = new TreeMap<>();
		poolStatsPerRegion.entrySet().forEach(e -> {
			Region r = e.getKey();
			PoolingHttpClientConnectionManager pcm = e.getValue();
			PoolStats stats = pcm.getTotalStats();
			if (stats != null) {
				ConnectionStatistics statistics = ConnectionStatistics.T.create();
				statistics.setAvailable(stats.getAvailable());
				statistics.setLeased(stats.getLeased());
				statistics.setMax(stats.getMax());
				statistics.setPending(stats.getPending());
				result.put(r.id(), statistics);
			}
		});
		return result;
	}

}
