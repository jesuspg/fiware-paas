package com.telefonica.euro_iaas.paasmanager.client;

import com.sun.jersey.api.client.Client;
import com.telefonica.euro_iaas.paasmanager.client.services.ApplicationInstanceService;
import com.telefonica.euro_iaas.paasmanager.client.services.EnvironmentInstanceService;
import com.telefonica.euro_iaas.paasmanager.client.services.TaskService;
import com.telefonica.euro_iaas.paasmanager.client.services.impl.ApplicationInstanceServiceImpl;
import com.telefonica.euro_iaas.paasmanager.client.services.impl.EnvironmentInstanceServiceImpl;
import com.telefonica.euro_iaas.paasmanager.client.services.impl.TaskServiceImpl;

/**
 * Contains the uris, and other constants related to paas-manager-client
 *
 * @author Jesus M. Movilla
 *
 */
public class PaasManagerClient {

	private static Client client = new Client();

	/**
	 * Get the service to work with Environment Instances.
	 * @param baseUrl the base url where the SDC is
	 * @param mediaType the media type (application/xml or application/json)
	 * @return the environment instance service.
	*/
	public EnvironmentInstanceService getEnvironmentInstanceService(
			String baseUrl, String mediaType) {
	        return new EnvironmentInstanceServiceImpl(client, baseUrl, mediaType);
	}

	/**
	 * Ge tthe service to work withApplication Instances
	 * @param baseUrl
	 * @param mediaType
	 * @return
	 */
	public ApplicationInstanceService getApplicationInstanceService(
			String baseUrl, String mediaType) {
	        return new ApplicationInstanceServiceImpl(client, baseUrl, mediaType);
	}
	
	/**
	 * Ge tthe service to work withA Tasks
	 * @param baseUrl
	 * @param mediaType
	 * @return
	 */
	public TaskService getTaskService(
			String baseUrl, String mediaType) {
	        return new TaskServiceImpl(client, baseUrl, mediaType);
	}
}