package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platform.setup.api.tfruntime.Database;

public interface Spec extends GenericEntity {
	static EntityType<Spec> T = EntityTypes.T(Spec.class);

	String getDomain();
	void setDomain(String domain);

	String getDatabaseType();
	void setDatabaseType(String databaseType);

	List<Database> getDatabases();
	void setDatabases(List<Database> databases);

	Backend getBackend();
	void setBackend(Backend backend);

	List<Component> getComponents();
	void setComponents(List<Component> component);
	
	Database getDcsaConfig();
	void setDcsaConfig(Database dcsaConfig);
}
