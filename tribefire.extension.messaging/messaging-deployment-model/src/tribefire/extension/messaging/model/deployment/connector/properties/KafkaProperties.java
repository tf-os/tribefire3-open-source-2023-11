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
package tribefire.extension.messaging.model.deployment.connector.properties;

import java.util.Optional;
import java.util.Properties;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface KafkaProperties extends CommonMessagingProperties {
	EntityType<KafkaProperties> T = EntityTypes.T(KafkaProperties.class);

	String aks = "aks";
	String retries = "retries";
	String batchSize = "batchSize";
	String lingerMs = "lingerMs";
	String bufferMemory = "bufferMemory";
	String kafkaGroupId = "kafkaGroupId";
	String reconnectTimeout = "reconnectTimeout";
	String groupInstanceId = "groupInstanceId";
	String maxPollIntervalMs = "maxPollIntervalMs";
	String pollDuration = "pollDuration";
	String sessionTimout = "sessionTimout";
	String sslKeyPassword = "sslKeyPassword";
	String sslKeystoreCertificateChain = "sslKeystoreCertificateChain";
	String sslKeystoreKey = "sslKeystoreKey";
	String sslKeystoreLocation = "sslKeystoreLocation";
	String sslKeystorePassword = "sslKeystorePassword";
	String sslTruststoreCertificates = "sslTruststoreCertificates";
	String sslTruststoreLocation = "sslTruststoreLocation";
	String sslTrustStorePassword = "sslTrustStorePassword";

	@Initializer("'all'")
	String getAks();
	void setAks(String aks);

	@Initializer("100")
	Integer getRetries();
	void setRetries(Integer retries);

	@Initializer("16384")
	Integer getBatchSize();
	void setBatchSize(Integer batchSize);

	@Initializer("1")
	Integer getLingerMs();
	void setLingerMs(Integer lingerMs);

	@Initializer("33554432")
	Integer getBufferMemory();
	void setBufferMemory(Integer bufferMemory);

	@Initializer("'groupId'")
	String getKafkaGroupId();
	void setKafkaGroupId(String groupId);

	@Initializer("100")
	Integer getReconnectTimeout();
	void setReconnectTimeout(Integer reconnectionTimeout);

	String getGroupInstanceId();
	void setGroupInstanceId(String groupInstanceId);

	Integer getMaxPollIntervalMs();
	void setMaxPollIntervalMs(Integer maxPollIntervalMs);

	@Initializer("100000")
	Integer getSessionTimout();
	void setSessionTimout(Integer sessionTimout);

	@Initializer("10")
	Integer getPollDuration();
	void setPollDuration(Integer pollDuration);

	String getSslKeyPassword();
	void setSslKeyPassword(String sslKeyPassword);

	String getSslKeystoreCertificateChain();
	void setSslKeystoreCertificateChain(String sslKeystoreCertificateChain);

	String getSslKeystoreKey();
	void setSslKeystoreKey(String sslKeystoreKey);

	String getSslKeystoreLocation();
	void setSslKeystoreLocation(String sslKeystoreLocation);

	String getSslKeystorePassword();
	void setSslKeystorePassword(String sslKeystorePassword);

	String getSslTruststoreCertificates();
	void setSslTruststoreCertificates(String sslTruststoreCertificates);

	String getSslTruststoreLocation();
	void setSslTruststoreLocation(String sslTruststoreLocation);

	String getSslTrustStorePassword();
	void setSslTrustStorePassword(String sslTrustStorePassword);

	default Properties getKafkaProperties(String externalId) {
		Properties props = new Properties();
		props.put("bootstrap.servers", String.join(",", this.getServiceUrls()));
		props.put("acks", this.getAks());

		props.put("client.id", externalId);
		props.put("group.id", this.getKafkaGroupId());
		Optional.ofNullable(this.getGroupInstanceId()).ifPresent(id -> props.put("group.instance.id", id));

		props.put("reconnect.backoff.ms", this.getReconnectTimeout());
		props.put("retries", this.getRetries());
		props.put("batch.size", this.getBatchSize());
		props.put("linger.ms", this.getLingerMs());
		props.put("buffer.memory", this.getBufferMemory());
		props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

		Optional.ofNullable(this.getSessionTimout()).ifPresent(t -> props.put("session.timeout.ms", t));

		// SSL settings
		Optional.ofNullable(this.getSslKeyPassword()).ifPresent(p -> props.put("ssl.key.password", p)); // NA
		Optional.ofNullable(this.getSslKeystoreCertificateChain()).ifPresent(p -> props.put("ssl.keystore.certificate.chain", p)); // NA
		Optional.ofNullable(this.getSslKeystoreKey()).ifPresent(p -> props.put("ssl.keystore.key", p)); // NA
		Optional.ofNullable(this.getSslKeystoreLocation()).ifPresent(p -> props.put("ssl.keystore.location", p)); // NA
		Optional.ofNullable(this.getSslKeystorePassword()).ifPresent(p -> props.put("ssl.keystore.password", p)); // NA
		Optional.ofNullable(this.getSslTruststoreCertificates()).ifPresent(p -> props.put("ssl.truststore.certificates", p)); // NA
		Optional.ofNullable(this.getSslTruststoreLocation()).ifPresent(p -> props.put("ssl.truststore.location", p)); // NA
		Optional.ofNullable(this.getSslTrustStorePassword()).ifPresent(p -> props.put("ssl.truststore.password", p)); // NA

		return props;
	}

}
