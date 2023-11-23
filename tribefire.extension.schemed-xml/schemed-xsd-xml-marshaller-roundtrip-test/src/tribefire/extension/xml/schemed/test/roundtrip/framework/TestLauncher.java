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
package tribefire.extension.xml.schemed.test.roundtrip.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

/**
 * forks a java process - used to separate class loaders 
 * 
 * @author pit
 *
 */
public class TestLauncher {
	
	private String commandClass;
	private List<LaunchParam> params;
	private File classpathDir;
	
	@Configurable @Required
	public void setCommandClass(String commandClass) {
		this.commandClass = commandClass;
	}
	
	@Configurable
	public void setParams(List<LaunchParam> params) {
		this.params = params;
	}
	
	@Configurable @Required
	public void setClasspathDir(File unpackDir) {
		this.classpathDir = unpackDir;
	}
	
	/**
	 * identifies if we're running on windows or something other .. (UNIX style)
	 * @return - true if the os name starts with "windows" 
	 */
	private  boolean isWindows() {
    	String os = System.getProperty("os.name");
    	if (os == null) {
    		throw new IllegalStateException("os.name");
    	}
    	os = os.toLowerCase();
    	return os.startsWith("windows");
    }

    /**
     * find the java executable (for the respective OS)
     * @return - the {@link File} that represents the java executable 
     * @throws FileNotFoundException
     */
    private  File getJreExecutable() throws FileNotFoundException {
    	String jreDirectory = System.getProperty("java.home");
    	if (jreDirectory == null) {
    		throw new IllegalStateException("java.home");
    	}
    	File exe;
    	if (isWindows()) {
    		exe = new File(jreDirectory, "bin/java.exe");
    	} else {
    		exe = new File(jreDirectory, "bin/java");
    	}
    	if (!exe.isFile()) {
    		throw new FileNotFoundException(exe.toString());
    	}
    	return exe;
    }

    /**
     * actually launch the java (or any other process)
     * @param cmdarray - an {@link Array} of {@link String} with command parameters 
     * @return - what the process returns 
     * @throws IOException
     * @throws InterruptedException
     */
    private int launch(List<String> cmdarray) throws IOException, InterruptedException {
    	byte[] buffer = new byte[1024];

    	ProcessBuilder processBuilder = new ProcessBuilder(cmdarray);
    	processBuilder.redirectErrorStream(true);
    	Process process = processBuilder.start();
    	InputStream in = process.getInputStream();
    	while (true) {
    		int r = in.read(buffer);
    		if (r <= 0) {
    			break;
    		}
    		System.out.write(buffer, 0, r);
    	}
    	return process.waitFor();
    }
    
    /**
     * @param json
     * @throws LaunchException
     */
    public  void run(File request)  {
    	try  {						
			List<String> cmds = new ArrayList<String>();
			// setup java call 
			cmds.add( getJreExecutable().getAbsolutePath());
			// class path 
			cmds.add( "-cp");
			String cp = String.format("%s/*", classpathDir.getAbsolutePath().replace("\\", "/"));
			cmds.add( cp);
			// parameters 
			if (params != null && params.size() > 0) {
				for (LaunchParam param: params) {
					String name = param.getName();
					if (name != null && name.length() > 0) {
						cmds.add(param.getName());
					}
					cmds.add( param.getValue());
				}
			}
			// main command class 
			cmds.add( commandClass);
			
			// produce file 
			
			cmds.add( request.getAbsolutePath());
			
			// launch it 
			int retval = launch( cmds);
			if (retval != 0) {
				String msg = String.format("java call failed with code [%d]", retval);
				throw new IllegalStateException( msg);
			}
		
		} catch (FileNotFoundException e) {
			String msg = String.format("cannot determine the location of the JRE");
			throw new IllegalStateException(msg);
		} catch (IOException e) {
			String msg="cannot launch JRE";
			throw new IllegalStateException(msg);
		} catch (InterruptedException e) {
			String msg="cannot launch JRE";
			throw new IllegalStateException(msg);
		}		
    }
}
