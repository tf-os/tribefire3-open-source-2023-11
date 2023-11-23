package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platform.setup.api.tfruntime.LogLevel;
import com.braintribe.model.platform.setup.api.tfruntime.PersistentVolume;
import com.braintribe.model.platform.setup.api.tfruntime.Resources;

public interface Component extends GenericEntity {
	static EntityType<Component> T = EntityTypes.T(Component.class);

	String getName();
	void setName(String name);

	String getType();
	void setType(String type);

	String getImage();
	void setImage(String image);

	String getImageTag();
	void setImageTag(String imageTag);

	LogLevel getLogLevel();
	void setLogLevel(LogLevel logLevel);

	Integer getReplicas();
	void setReplicas(Integer replicas);	
	
	String getApiPath();
	void setApiPath(String apiPath);
	
	String getCustomHealthCheckPath();
	void setCustomHealthCheckPath(String customHealthCheckPath);
	
	// Boolean getLogJson();
	// void setLogJson(Boolean logJson);
	//
	// String getPublicUrl();
	// void setPublicUrl(String publicUrl);

	List<EnvironmentVariable> getEnv();
	void setEnv(List<EnvironmentVariable> env);

	Resources getResources();
	void setResources(Resources resources);

	List<PersistentVolume> getPersistentVolumes();
	void setPersistentVolumes(List<PersistentVolume> persistentVolumes);
}
