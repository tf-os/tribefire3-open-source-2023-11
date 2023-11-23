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
package com.braintribe.eclipse.launching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;

public class FileClassPathJUnitLauncher extends JUnitLaunchConfigurationDelegate
{
	public FileClassPathJUnitLauncher()
	{
	}

    public String getVMArguments(ILaunchConfiguration configuration)
            throws CoreException
        {
            StringBuilder builder = new StringBuilder(super.getVMArguments(configuration));
            builder.append(" -Djava.system.class.loader=com.braintribe.utils.classloader.FileClassPathClassLoader");
            builder.append(" -Dcom.braintribe.classpath.file=\"");
            builder.append(getClasspathFileLocation(configuration).getPath());
            builder.append("\"");
            String extendedVMArguments = builder.toString();
            return extendedVMArguments;
        }


    protected File getClasspathFileLocation(ILaunchConfiguration configuration)
    {
        String name = configuration.getName();
        File directory = Activator.getDefault().getStateLocation().toFile();
        File classpathFile = new File(directory, (new StringBuilder(String.valueOf(name))).append(".classpath").toString());
        return classpathFile;
    }

    public String[] getClasspath(ILaunchConfiguration configuration)
        throws CoreException
    {
        String originalClasspath[];
        File classpathFile;
        BufferedWriter writer;
        originalClasspath = super.getClasspath(configuration);
        classpathFile = getClasspathFileLocation(configuration);
        writer = null;
        try {
	        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(classpathFile), "UTF-8"));
	        int i = 0;
	        String as[];
	        int k = (as = originalClasspath).length;
	        for(int j = 0; j < k; j++)
	        {
	            String line = as[j];
	            if(i++ > 0)
	                writer.newLine();
	            writer.append(line);
	        }
	
	        writer.flush();
	        
	        return (new String[] {
	        		ClassloaderJarFile.getJarFile().toString()
	        });
        }
        catch (IOException e) { 
	        Status status = new Status(4, "Launcher", "error while relocating classpath from commandline to file", e);
	        throw new CoreException(status);
        }
        finally {
        	try
        	{
        		if(writer != null)
        			writer.close();
        	}
        	catch(IOException ex)
        	{
        		ex.printStackTrace();
        	}
        }
        

    }        

}
