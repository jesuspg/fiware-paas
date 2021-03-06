/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U <br>
 * This file is part of FI-WARE project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * </p>
 * <p>
 * You may obtain a copy of the License at:<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * <p>
 * See the License for the specific language governing permissions and limitations under the License.
 * </p>
 * <p>
 * For those usages not covered by the Apache version 2.0 License please contact with opensource@tid.es
 * </p>
 */

package com.telefonica.euro_iaas.paasmanager.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.telefonica.euro_iaas.commons.dao.AbstractBaseDao;
import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.dao.NetworkDao;
import com.telefonica.euro_iaas.paasmanager.model.Network;

/**
 * @author Henar Munoz
 */
@Transactional(propagation = Propagation.REQUIRED)
public class NetworkDaoJpaImpl extends AbstractBaseDao<Network, String> implements NetworkDao {

    /*
     * find all networks.
     * @return network list
     */
    public List<Network> findAll() {
        return super.findAll(Network.class);
    }

    public Network load(String networkName, String vdc, String region) throws EntityNotFoundException {
        return findNetworkWithSubNet(networkName, vdc, region);
    }

    private Network findNetworkWithSubNet(String name, String vdc, String region ) throws EntityNotFoundException {
        Query query = getEntityManager().createQuery(
                "select p from Network p left join " + " fetch p.subNets where p.name = :name and p.vdc = :vdc and p.region =:region");
        query.setParameter("name", name);

        query.setParameter("region", region);
        if (vdc == null ){

            query.setParameter("vdc", "");
        } else {
            query.setParameter("vdc", vdc);
        }
        Network network = null;
        try {
            network = (Network) query.getSingleResult();
        } catch (NoResultException e) {
            String message = " No network found in the database with id: " + name + " and vdc " + vdc + " region " + region + " Exception: " + e.getMessage();
            throw new EntityNotFoundException(Network.class, "name", name);
        }
        return network;
    }

    @Override
    public Network load(String networkName) throws EntityNotFoundException {
        return this.loadByField(Network.class, "name", networkName);
    }

}
