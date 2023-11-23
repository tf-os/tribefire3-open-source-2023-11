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
package com.braintribe.model.platformreflection;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformreflection.check.cpu.Cpu;
import com.braintribe.model.platformreflection.check.io.IoMeasurements;
import com.braintribe.model.platformreflection.check.java.JavaEnvironment;
import com.braintribe.model.platformreflection.check.power.PowerSource;
import com.braintribe.model.platformreflection.db.DatabaseInformation;
import com.braintribe.model.platformreflection.disk.DiskInfo;
import com.braintribe.model.platformreflection.disk.FileSystemDetailInfo;
import com.braintribe.model.platformreflection.disk.FileSystemInfo;
import com.braintribe.model.platformreflection.hardware.ComputerSystem;
import com.braintribe.model.platformreflection.memory.Memory;
import com.braintribe.model.platformreflection.network.NetworkInterface;
import com.braintribe.model.platformreflection.network.NetworkParams;
import com.braintribe.model.platformreflection.os.OperatingSystem;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;
import com.braintribe.model.platformreflection.tf.Concurrency;
import com.braintribe.model.platformreflection.tf.Messaging;
import com.braintribe.model.platformreflection.threadpools.ThreadPools;

public interface SystemInfo extends PlatformReflectionResponse {

	EntityType<SystemInfo> T = EntityTypes.T(SystemInfo.class);

	List<DiskInfo> getDisks();
	void setDisks(List<DiskInfo> disks);

	ComputerSystem getComputerSystem();
	void setComputerSystem(ComputerSystem computerSystem);

	List<FileSystemInfo> getFileSystems();
	void setFileSystems(List<FileSystemInfo> fileSystems);

	FileSystemDetailInfo getFileSystemDetailInfo();
	void setFileSystemDetailInfo(FileSystemDetailInfo fileSystemDetailInfo);

	OperatingSystem getOperatingSystem();
	void setOperatingSystem(OperatingSystem operatingSystem);

	Memory getMemory();
	void setMemory(Memory memory);

	Cpu getCpu();
	void setCpu(Cpu cpu);

	List<NetworkInterface> getNetworkInterfaces();
	void setNetworkInterfaces(List<NetworkInterface> networkInterfaces);

	NetworkParams getNetworkParams();
	void setNetworkParams(NetworkParams networkPaar);

	List<PowerSource> getPowerSources();
	void setPowerSources(List<PowerSource> powerSources);

	List<com.braintribe.model.platformreflection.os.Process> getJavaProcesses();
	void setJavaProcesses(List<com.braintribe.model.platformreflection.os.Process> javaProcesses);

	JavaEnvironment getJavaEnvironment();
	void setJavaEnvironment(JavaEnvironment javaEnvironment);

	IoMeasurements getIoMeasurements();
	void setIoMeasurements(IoMeasurements ioMeasurements);

	DatabaseInformation getDatabaseInformation();
	void setDatabaseInformation(DatabaseInformation databaseInformation);

	ThreadPools getThreadPools();
	void setThreadPools(ThreadPools threadPools);

	List<String> getFontFamilies();
	void setFontFamilies(List<String> fontFamilies);

	Messaging getMessaging();
	void setMessaging(Messaging messaging);

	Concurrency getConcurrency();
	void setConcurrency(Concurrency concurrency);

}
