/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;

/**
 * @author Romain Bioteau
 *
 */
public class BonitaHomeUtil {

	public static final String ENGINE_LOG_DESTINATION_FILE = "org.bonitasoft.studio.engine.log.destination.file";
	private static final String STUDIO_ENGINE_LOGGING_PROPERTIES = ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath()
			+ File.separator + "logback.xml";


	private static final String API_TYPE = "org.bonitasoft.engine.api-type";
	public static final String HTTP = "HTTP";
	private static final String SERVER_URL = "server.url";
	private static final String APPLICATION_NAME = "application.name";
	private static final String BONITA_APPLICATION = "bonita";
    private static final String BONITA_CLIENT_HOST_DEFAULT = "bonita.client.host.default";
    private static final String BONITA_CLIENT_PORT_DEFAULT = "bonita.client.port.default";

	public static File getBonitaHome() {
		return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString(), "bonita");
	}

	public static String getJaasPath() {
		return getBonitaHome().getAbsolutePath() + File.separator + "external"
				+ File.separator + "security" + File.separator
				+ "jaas-standard.cfg";
	}

	public static File getPortalI18NFolder() {
	    final File bonitaFolder =  new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator + "tomcat"+File.separator+"bonita");
        return new File(bonitaFolder, "client"
                + File.separator + "platform"
                + File.separator + "work"
                + File.separator + "i18n");
    }



	public static InputStream getLoggingFile() {
		return BonitaHomeUtil.class
				.getResourceAsStream("/resources/logging/logback.xml");
	}

	public static File getReferenceExternalFolder() {
		try {
			return new File(getReferenceBonitaHome(), "external");
		} catch (final Exception ex) {
			BonitaStudioLog.error(ex);
			return null;
		}
	}

	public static File getReferenceBonitaHome() throws IOException {
		return new File(FileLocator.toFileURL(
				ProjectUtil.getConsoleLibsBundle().getEntry("bonita-home"))
				.getFile());
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public synchronized static File initBonitaHome()  {
		File destBonitaHome = null;
		try{
			destBonitaHome = BonitaHomeUtil.getBonitaHome();
			if (!destBonitaHome.exists()) {
				BonitaStudioLog.debug("Initializing BONITA.HOME", Activator.PLUGIN_ID);
				destBonitaHome.mkdir();
				final File srcBonitaHome = BonitaHomeUtil.getReferenceBonitaHome();
				FileUtil.copyDir(srcBonitaHome, destBonitaHome);

                final String bonitaHostDefaultPortValue = System.getProperty(BONITA_CLIENT_HOST_DEFAULT, "localhost");
                final int bonitaClientDefaultPortValue = Integer.parseInt(System.getProperty(BONITA_CLIENT_PORT_DEFAULT, "8080"));
                configureBonitaClient(HTTP, bonitaHostDefaultPortValue, bonitaClientDefaultPortValue);
				BonitaStudioLog.debug("BONITA.HOME installed.", Activator.PLUGIN_ID);
			}
			if (!destBonitaHome.getAbsolutePath().equals(
					System.getProperty(BonitaConstants.HOME))) {
				System.setProperty(BonitaConstants.HOME,
						destBonitaHome.getAbsolutePath());
			}

		}catch (final Exception e) {
			BonitaStudioLog.error(e);
		}
		return destBonitaHome;
	}

	public static void configureBonitaClient(final String apiType,final String host,final int serverPort) {
		BonitaStudioLog.debug("Configuring bonita client on host "+host+":"+serverPort+" with API_TYPE="+apiType, Activator.PLUGIN_ID);
		final File clientWorkFolder = new File(BonitaHomeUtil.getBonitaHome(),"engine-client"+File.separatorChar+"work");
		final File clientConfFolder = new File(BonitaHomeUtil.getBonitaHome(),"engine-client"+File.separatorChar+"conf");
		final File defaultBonitaClientFile = new File(clientWorkFolder,"bonita-client-community.properties");
		final File customBonitaClientFile = new File(clientConfFolder,"bonita-client-custom.properties");
		if(!defaultBonitaClientFile.exists()){
            initBonitaHome();
            if (!defaultBonitaClientFile.exists()) {
                throw new RuntimeException("bonita-client.properties not found in the bonita home");
            }
		}
		final Properties p = new Properties();
		FileInputStream inStream = null;
		FileOutputStream out = null;
		try{
			inStream = new FileInputStream(defaultBonitaClientFile);
			p.load(inStream);
			p.setProperty(API_TYPE, apiType);
			if(HTTP.equals(apiType)){
				p.setProperty(SERVER_URL, "http://"+host+":"+serverPort);
				p.setProperty(APPLICATION_NAME, BONITA_APPLICATION);
			}
			out = new FileOutputStream(customBonitaClientFile);
			p.store(out, null);
			APITypeManager.refresh();
		}catch (final Exception e) {
			BonitaStudioLog.error(e);
		}finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (final IOException e) {

				}
			}
			if(out != null){
				try {
					out.close();
				} catch (final IOException e) {

				}
			}
		}
	}

	public static File getStudioLoggingFile() {
		return new File(STUDIO_ENGINE_LOGGING_PROPERTIES);
	}

	public static File getDefaultTenantSecurityConfigFile(final long tenantId) {
		final File bonitaFolder =  new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator + "tomcat"+File.separator+"bonita");
		return new File(bonitaFolder,"client"+File.separator+"tenants"+File.separator+String.valueOf(tenantId)+File.separator+"conf"+File.separator+"security-config.properties");
	}

	public static File getDefaultTenantSecurityConfigStudioFile() {
		final URL url = ProjectUtil.getConsoleLibsBundle().getEntry("bonita-home");
		File bonitaFolder = null;
		try {
			bonitaFolder = new File(FileLocator.toFileURL(url).getFile());
			return new File(bonitaFolder,"client"+File.separator+"platform"+File.separator+"tenant-template"+File.separator+"conf"+File.separator+"security-config.properties");
		} catch (final IOException e) {
			BonitaStudioLog.error(e);
		}

		return null ;
	}

	public static File getDefaultPlatformTenantConfigFile() {
		final URL url = ProjectUtil.getConsoleLibsBundle().getEntry("bonita-home");
		File bonitaFolder = null;
		try {
			bonitaFolder = new File(FileLocator.toFileURL(url).getFile());
			return new File(bonitaFolder,"client"+File.separator+"platform"+File.separator+"conf"+File.separator+"platform-tenant-config.properties");
		} catch (final IOException e) {
			BonitaStudioLog.error(e);
		}

		return null ;
	}
}
