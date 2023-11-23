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
package com.braintribe.model.ftp;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.transport.ftp.enums.SourcePostProcessing;
import com.braintribe.transport.ftp.enums.TargetConflictHandling;

public interface BatchFtpJob extends StandardIdentifiable {
	
	final EntityType<BatchFtpJob> T = EntityTypes.T(BatchFtpJob.class);
	
	 String getName();
	 void setName(String name);
	
	 FtpConnection getConnection();
	 void setConnection(FtpConnection connection);

	 String getRemotePath();
	 void setRemotePath(String arg);

	 String getLocalPath();
	 void setLocalPath(String arg);

	 SourcePostProcessing getSourcePostProcessing();
	 void setSourcePostProcessing(SourcePostProcessing arg);

	 TargetConflictHandling getTargetConflictHandling();
	 void setTargetConflictHandling(TargetConflictHandling arg);
	
	 String getSourceArchivePath();
	 void setSourceArchivePath(String arg);
	
	 boolean getContinueOnError();
	 void setContinueOnError(boolean arg);

	 void setFilenameInclusionFilter(String arg);
	 String getFilenameInclusionFilter();
}
