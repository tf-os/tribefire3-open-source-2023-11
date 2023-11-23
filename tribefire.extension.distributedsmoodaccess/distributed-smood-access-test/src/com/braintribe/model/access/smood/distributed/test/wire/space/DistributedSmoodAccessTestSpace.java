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
package com.braintribe.model.access.smood.distributed.test.wire.space;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;

import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.hibernate.HibernateAccess;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.access.smood.distributed.DistributedSmoodAccess;
import com.braintribe.model.access.smood.distributed.test.concurrent.tester.AbstractSmoodDbAccessTest;
import com.braintribe.model.access.smood.distributed.test.concurrent.tester.MultiJvmSmoodDbAccessTest;
import com.braintribe.model.access.smood.distributed.test.concurrent.tester.SingleJvmSmoodDbAccessTest;
import com.braintribe.model.access.smood.distributed.test.concurrent.worker.WorkerFactory;
import com.braintribe.model.access.smood.distributed.test.utils.DerbyDialect;
import com.braintribe.model.access.smood.distributed.test.utils.TestUtilities;
import com.braintribe.model.access.smood.distributed.test.wire.contract.DistributedSmoodAccessTestContract;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.db.impl.DbLockManager;
import com.braintribe.persistence.hibernate.GmAwareHibernateSessionFactoryBean;
import com.braintribe.persistence.hibernate.HibernateSessionFactoryBean;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.zaxxer.hikari.HikariDataSource;

@Managed
public class DistributedSmoodAccessTestSpace implements DistributedSmoodAccessTestContract {

	@Override
	@Managed
	public List<AbstractSmoodDbAccessTest> concurrentTesters() {
		List<AbstractSmoodDbAccessTest> bean = new ArrayList<>();
		// bean.add(testerSingleVm());
		// bean.add(testerMultiVm());
		return bean;
	}

	@Managed
	private SingleJvmSmoodDbAccessTest testerSingleVm() {
		SingleJvmSmoodDbAccessTest bean = new SingleJvmSmoodDbAccessTest();
		bean.setFactory(workerFactory());
		bean.setSpace(this);
		bean.setWorkerCount(10);
		return bean;
	}

	@Managed
	private MultiJvmSmoodDbAccessTest testerMultiVm() {
		MultiJvmSmoodDbAccessTest bean = new MultiJvmSmoodDbAccessTest();
		bean.setFactory(workerFactory());
		bean.setSpace(this);
		bean.setWorkerCount(10);
		return bean;
	}

	@Override
	@Managed
	public WorkerFactory workerFactory() {
		WorkerFactory bean = new WorkerFactory();
		bean.setIterations(10);
		bean.setSpace(this);
		return bean;
	}

	@Override
	@Managed
	public TestUtilities utils() {
		TestUtilities bean = new TestUtilities();
		bean.setDataSource(dataSource());
		bean.setStorage(dbAccess());
		bean.setClassDataStorage(concurrentAccess());
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public DistributedSmoodAccess accessWithoutInitialData() {
		DistributedSmoodAccess bean = new DistributedSmoodAccess();
		configureAccess(bean);
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public DistributedSmoodAccess accessWithInitialData() {
		DistributedSmoodAccess bean = new DistributedSmoodAccess();
		configureAccess(bean);
		bean.setInitialStorage(initialAccess());
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public DistributedSmoodAccess concurrentAccess() {
		DistributedSmoodAccess bean = new DistributedSmoodAccess();
		configureAccess(bean);
		bean.setKeepNOldDumps(5);
		bean.setMaxManipulationBuffers(100);
		bean.setMaxManipulationBufferSize(500000);
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public DistributedSmoodAccess accessWithoutInitDataPrefix1() {
		DistributedSmoodAccess bean = new DistributedSmoodAccess();
		configureAccess(bean);
		bean.setIdentifierPrefix("1-");
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public DistributedSmoodAccess accessWithoutInitDataPrefix2() {
		DistributedSmoodAccess bean = new DistributedSmoodAccess();
		configureAccess(bean);
		bean.setIdentifierPrefix("2-");
		return bean;
	}

	@Managed
	private NonIncrementalAccess initialAccess() {
		XmlAccess bean = new XmlAccess();
		bean.setFilePath(new File("res/data/initial.xml"));
		bean.setModelProvider(this::userModel);
		return bean;
	}

	private void configureAccess(DistributedSmoodAccess bean) {
		bean.setAccessId("testAccess");
		bean.setDbLockManager(lockManager());
		bean.setKeepNOldDumps(1);
		bean.setMaxManipulationBuffers(3);
		bean.setModelProvider(this::userModel);
		bean.setDataSource(dataSource());
		bean.setHibernateSessionFactory(hibernateSessionFactory());
		bean.setReadWriteLock(readWriteLock());
	}

	@Managed
	private ReadWriteLock readWriteLock() {
		return new MutuallyExclusiveReadWriteLock();
	}

	@Managed
	private LockManager lockManager() {
		DbLockManager bean = new DbLockManager();
		bean.setDataSource(dataSource());
		try {
			bean.postConstruct();
		} catch (Exception e) {
			throw new RuntimeException("Error while initializing DbLockManager", e);
		}
		return bean;
	}

	@Managed
	private DataSource dataSource() {
		HikariDataSource bean = new HikariDataSource();
		bean.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
		bean.setJdbcUrl("jdbc:derby://localhost:1527/res/db/distributedsmoodtests;create=true");
		bean.setUsername("cortex");
		bean.setPassword("cortex");

		bean.setMaximumPoolSize(100);
		// bean.setMinPoolSize(1);
		// bean.setMaxStatements(100);
		return bean;
	}

	@Managed
	private GmMetaModel userModel() {
		GmMetaModel bean = GMF.getTypeReflection().getModel("com.braintribe.gm:user-model").getMetaModel();
		return bean;
	}

	@Managed
	private IncrementalAccess dbAccess() {
		HibernateAccess bean = new HibernateAccess();
		bean.setHibernateSessionFactory(hibernateSessionFactory());
		bean.setModelName("UserModel");
		bean.setModelSupplier(this::userModel);
		return bean;
	}

	@Managed
	private SessionFactory hibernateSessionFactory() {

		HibernateSessionFactoryBean factory = new GmAwareHibernateSessionFactoryBean();

		factory.setDataSource(dataSource());
		factory.setDialect(DerbyDialect.class);

		// @formatter:off
		try {
			ClassLoader cl = getClass().getClassLoader();
			factory.setMappingLocations(cl.getResource("com/braintribe/model/smoodstorage/com.braintribe.model.smoodstorage.SmoodStorage.hbm.xml"),
					cl.getResource("com/braintribe/model/smoodstorage/com.braintribe.model.smoodstorage.BufferedManipulation.hbm.xml"),
					cl.getResource("com/braintribe/model/smoodstorage/com.braintribe.model.smoodstorage.JavaClass.hbm.xml")
					);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to initialize session factory - mappings");
		}
		// @formatter:on
		factory.setDefaultBatchFetchSize(30);
		Map<String, String> props = new HashMap<>();
		props.put("hibernate.dialect", "com.braintribe.model.access.smood.distributed.test.utils.DerbyDialect");
		props.put("hibernate.show_sql", "false");
		props.put("hibernate.hbm2ddl.auto", "update");
		factory.setAdditionalProperties(props);

		InstanceConfiguration.currentInstance().onDestroy(factory::preDestroy);

		try {
			factory.afterPropertiesSet();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to initialize session factory");
		}

		SessionFactory bean = factory.getObject();
		return bean;

	}
}
