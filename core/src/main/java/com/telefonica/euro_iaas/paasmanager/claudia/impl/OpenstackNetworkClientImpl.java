/**
 * (c) Copyright 2013 Telefonica, I+D. Printed in Spain (Europe). All Rights Reserved.<br>
 * The copyright to the software program(s) is property of Telefonica I+D. The program(s) may be used and or copied only
 * with the express written consent of Telefonica I+D or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.telefonica.euro_iaas.paasmanager.claudia.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.claudia.NetworkClient;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.Router;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.euro_iaas.paasmanager.util.OpenStackUtil;

/**
 * @author henar.munoz
 */
public class OpenstackNetworkClientImpl implements NetworkClient {

    private OpenStackUtil openStackUtil = null;
    private static Logger log = Logger.getLogger(OpenstackNetworkClientImpl.class);


    /**
     * It adds the network to the router.
     * 
     * @params claudiaData
     * @params router
     * @params network
     * @throws InfrastructureException
     */

    public void addNetworkToRouter(ClaudiaData claudiaData, String routerId, Network net) throws InfrastructureException {
        log.info("Add Interfact from net " + net.getNetworkName() + " to router " + routerId
                + " for user " + claudiaData.getUser().getTenantName());

        try {
            String response = openStackUtil.addInterface(routerId, net, claudiaData.getUser());
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + routerId+ ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }
    
    /**
     * It adds the network to the public router.
     * 
     * @params net
     * @throws InfrastructureException
     */

    public void addNetworkToPublicRouter(ClaudiaData claudiaData, Network net) throws InfrastructureException {
        log.info("Add Interfact from net " + net.getNetworkName() + " to public router ");

        try {
            String response = openStackUtil.addInterfaceToPublicRouter(claudiaData.getUser(), net);
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to add the network " + net.getNetworkName() + " to the public router :" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * It deletes the interface of the network in the router.
     */
    public void deleteNetworkFromRouter(ClaudiaData claudiaData, Router router, Network net)
        throws InfrastructureException {
        log.info("Delete Interfact net " + net.getNetworkName()+ " " + net.getIdNetRouter() +  " from router "
                + router.getName() + " for user " + claudiaData.getUser().getTenantName());

        try {

            String response = openStackUtil.removeInterface(router, net.getIdNetRouter(), claudiaData.getUser());
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * The deploy the network in Openstack.
     * 
     * @params claudiaData
     * @params network
     */
    public void deployNetwork(ClaudiaData claudiaData, Network network) throws InfrastructureException {
        log.info("Deploy network " + network.getNetworkName() + " for user " + claudiaData.getUser().getTenantName());
        log.debug("Payload " + network.toJson());
        String response;
        try {
            response = openStackUtil.createNetwork(network, claudiaData.getUser());
            log.debug(response);
            // "network-" + claudiaData.getUser().getTenantName()
            JSONObject networkString = new JSONObject(response);
            String id = networkString.getJSONObject("network").getString("id");
            log.debug("Network id " + id);
            network.setIdNetwork(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }
    /**
     * The deploy the network in Openstack.
     * 
     * @params claudiaData
     * @params network
     */
    public void deployRouter(ClaudiaData claudiaData, Router router) throws InfrastructureException {
        log.info("Deploy router " + router.getName() + " for user " + claudiaData.getUser().getTenantName());

        try {
            log.debug("Payload " + router.toJson());
            String response = openStackUtil.createRouter(router, claudiaData.getUser());


            JSONObject networkString = new JSONObject(response);
            String id = networkString.getJSONObject("router").getString("id");
            log.debug("Router id " + id);
            router.setIdRouter(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * The deploy the subnet in Openstack.
     * 
     * @params claudiaData
     * @params subNet
     */
    public void deploySubNetwork(ClaudiaData claudiaData, SubNetwork subNet) throws InfrastructureException {
        log.info("Deploy network " + subNet.getName() + " for user " + claudiaData.getUser().getTenantName());
        log.debug("Payload " + subNet.toJson());
        String response;
        try {
            response = openStackUtil.createSubNet(subNet, claudiaData.getUser());
            // "network-" + claudiaData.getUser().getTenantName()
            JSONObject networkString = new JSONObject(response);
            log.debug(response);
            /* {"subnet": {"name": "sub-net-uno-1", "enable_dhcp": true, "network_id": "70705cae-3078-43a9-babe-37d69bc16438",
             * "tenant_id": "67c979f51c5b4e89b85c1f876bdffe31", "dns_nameservers": [],
                "allocation_pools": [{"start": "10.100.1.2", "end": "10.100.1.254"}],
                    "host_routes": [], "ip_version": 4, "gateway_ip": "10.100.1.1", "cidr": "10.100.1.0/24", "id": "ec14e8d0-8612-4db4-a585-cdc15593ec8f"}}
             */
            String id = networkString.getJSONObject("subnet").getString("id");
            log.debug("Network id " + id);
            subNet.setIdSubNet(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + subNet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + subNet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It destroys the network.
     * 
     * @params claudiaData
     * @params network
     */
    public void destroyNetwork(ClaudiaData claudiaData, Network network) throws InfrastructureException {

        try {
            openStackUtil.deleteNetwork(network.getIdNetwork(), claudiaData.getUser());
        } catch (OpenStackException e) {
            String msm = "Error to delete the network " + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It delete the router in Openstack.
     */
    public void destroyRouter(ClaudiaData claudiaData, Router router) throws InfrastructureException {
        try {
            openStackUtil.deleteRouter(router.getIdRouter(), claudiaData.getUser());
        } catch (OpenStackException e) {
            String msm = "Error to delete the router " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * It destroys the network.
     * 
     * @params claudiaData
     * @params network
     */
    public void destroySubNetwork(ClaudiaData claudiaData, SubNetwork subnet) throws InfrastructureException {

        try {
            openStackUtil.deleteSubNetwork(subnet.getIdSubNet(), claudiaData.getUser());
        } catch (OpenStackException e) {
            String msm = "Error to delete the network " + subnet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It loads all networks.
     * 
     * @params claudiaData
     */
    public List<Network> loadAllNetwork(ClaudiaData claudiaData) throws OpenStackException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * It obtains information about the network.
     * 
     * @params claudiaData
     * @params network
     * @return network information
     */
    public String loadNetwork(ClaudiaData claudiaData, Network network) throws EntityNotFoundException {
        String response = "";
        try {
            response = openStackUtil.getNetworkDetails(network.getIdNetwork(), claudiaData.getUser());
        } catch (OpenStackException e) {
            String msm = "Error to obtain the network infromation " + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new EntityNotFoundException(Network.class, msm, e);
        }
        return response;
    }

    /**
     * Set the variable.
     * 
     * @params openStackUtil
     */
    public void setOpenStackUtil(OpenStackUtil openStackUtil) {
        this.openStackUtil = openStackUtil;
    }

}