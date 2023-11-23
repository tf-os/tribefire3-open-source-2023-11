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
package com.braintribe.model.access.smood.distributed.manipulation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smood.api.ManipulationStorage;
import com.braintribe.model.access.smood.api.ManipulationStorageException;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class DistributedSmoodLowLevelAccess implements ManipulationStorage, Supplier<File>, InitializationAware {

	protected static Logger logger = Logger.getLogger(DistributedSmoodLowLevelAccess.class);

	protected DataSource dataSource = null;
	protected String accessId = null;
	protected Codec<Object,String> entityCodec = null;
	protected SessionFactory hibernateSessionFactory = null;

	@Override
	public void postConstruct() {
		Session session = this.hibernateSessionFactory.openSession();
		session.close();
	}
	
	@Override
	public void appendManipulation(Manipulation manipulation) throws ManipulationStorageException {
		throw new UnsupportedOperationException("appendManipulation is not supported");
	}

	@Override
	public Manipulation getAccumulatedManipulation() throws ManipulationStorageException {

		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			connection.setAutoCommit(false);

			String smoodDumpId = this.getLatestSmoodStorageId(connection);
			List<Manipulation> manList = this.loadManipulationBuffers(connection, smoodDumpId);
			CompoundManipulation cm = CompoundManipulation.T.create();
			cm.setCompoundManipulationList(manList);

			return cm;

		} catch(Exception e) {
			throw new ManipulationStorageException("Could not get manipulation buffers for access "+this.accessId, e);
		} finally {
			IOTools.closeCloseable(connection, logger);
		}
	}

	protected List<Manipulation> loadManipulationBuffers(Connection connection, String smoodStorageId) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {

			ps = connection.prepareStatement("select bm.encodedManipulation from TF_BUFFEREDMANIPULATION bm join TF_SMOODSTORAGEBUFFEREDMANIP ss on ss.BufferedManipulationId = bm.id where ss.SmoodStorageId = ? order by bm.sequenceNumber asc");
			ps.setString(1, smoodStorageId);

			rs = ps.executeQuery();

			List<Manipulation> bmList = new LinkedList<Manipulation>();
			while (rs.next()) {
				Reader clobReader = rs.getClob(1).getCharacterStream();
				String encodedManipulation = null;
				try {
					encodedManipulation = IOTools.slurp(clobReader);
				} finally {
					IOTools.closeCloseable(clobReader, logger);
				}
				
				Manipulation manipulation = (Manipulation) this.entityCodec.decode(encodedManipulation);
				bmList.add(manipulation);
			}

			return bmList;
		} catch(Exception e) {
			throw new Exception("Could not load manipulation buffer of smood storage "+smoodStorageId, e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	@Override
	public void reset() throws ManipulationStorageException {
		throw new UnsupportedOperationException("reset is not supported");
	}

	@Override
	public long getSize() {
		throw new UnsupportedOperationException("getSize is not supported");
	}

	@Required @Configurable
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	protected String getLatestSmoodStorageId(Connection connection) throws ModelAccessException {

		boolean trace = logger.isTraceEnabled();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (trace) logger.trace("Getting latest smood storage for accessId "+this.accessId);

			ps = connection.prepareStatement("select id from TF_SMOODSTORAGE where accessId = ? order by sequenceNumber desc");
			ps.setString(1, this.accessId);

			rs = ps.executeQuery();

			if (rs.next()) {
				String id = rs.getString(1);
				return id;
			} else {
				return null;
			}

		} catch(Exception e) {
			throw new ModelAccessException("Could not get latest SmoodStorage from Access.", e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(ps, logger);
		}
	}

	@Configurable @Required
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	@Configurable @Required
	public void setEntityCodec(Codec<Object, String> entityCodec) {
		this.entityCodec = entityCodec;
	}

	@Override
	public File get() throws RuntimeException {

		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = null;
			ResultSet rs = null;
			try {

				ps = connection.prepareStatement("select ENCODEDDATA from TF_SMOODSTORAGE where accessId = ? order by sequenceNumber desc");
				ps.setString(1, this.accessId);

				rs = ps.executeQuery();

				File tempFile = File.createTempFile("dsmood-dump-"+FileTools.normalizeFilename(this.accessId, '_'), ".xml");
				tempFile.deleteOnExit();

				if (rs.next()) {

					Reader encodedDataReader = null;
					Writer writer = null;

					try {
						encodedDataReader = rs.getClob(1).getCharacterStream();
						writer = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
						IOTools.pump(encodedDataReader, writer);

					} finally {
						IOTools.closeCloseable(encodedDataReader, logger);
						IOTools.closeCloseable(writer, logger);
					}

				}
				return tempFile;

			} catch(Exception e) {
				throw new ModelAccessException("Could not get latest SmoodStorage from Access.", e);
			} finally {
				IOTools.closeCloseable(rs, logger);
				IOTools.closeCloseable(ps, logger);
			}

		} catch(Exception e) {
			throw new RuntimeException("Could not get manipulation buffers for access "+this.accessId, e);
		} finally {
			IOTools.closeCloseable(connection, logger);
		}
	}
	
	@Required @Configurable
	public void setHibernateSessionFactory(SessionFactory hibernateSessionFactory) {
		this.hibernateSessionFactory = hibernateSessionFactory;
	}

}
