# -*- coding: utf-8 -*-
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of FI-WARE project.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
# You may obtain a copy of the License at:
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.
#
# For those usages not covered by the Apache version 2.0 License please
# contact with opensource@tid.es

from tools import http

__author__ = 'henar'

import sys
from xml.etree.ElementTree import tostring
import json
from tools.environment_instance import EnvironmentInstance

from tools.environment import Environment
from tools.tier import Tier
from tools.productrelease import ProductRelease
from tools.productrequest import ProductRequest


class EnvironmentInstanceRequest:
    def __init__(self, keystone_url, paas_manager_url, tenant, user, password, vdc, sdc_url=''):
        self.paasmanager_url = paas_manager_url
        self.sdc_url = sdc_url
        self.vdc = vdc
        self.environment = []
        self.keystone_url = keystone_url

        self.user = user
        self.password = password
        self.tenant = tenant

        self.token = self.__get__token()
        print self.token
        self.environments = []

    def __get__token(self):
        return http.get_token(self.keystone_url + '/tokens', self.tenant, self.user, self.password)

    def __process_env_inst(self, data):
        envIns = EnvironmentInstance(data['blueprintName'], data['description'], None, data['status'])
        return envIns


    def add_blueprint_instance(self, environment_instance):
        url = "%s/%s/%s/%s" % (self.paasmanager_url, "envInst/org/FIWARE/vdc", self.vdc, "environmentInstance")
        headers = {'X-Auth-Token': self.token, 'Tenant-Id': self.vdc, 'Content-Type': "application/xml",
                   'Accept': "application/json"}
        print url
        print headers
        payload = tostring(environment_instance.to_xml())
        response = http.post(url, headers, payload)

        ## Si la respuesta es la adecuada, creo el diccionario de los datos en JSON.
        if response.status != 200 and response.status != 204:
            data = response.read()
            print 'error to deploy the environment ' + str(response.status) + " " + data
            sys.exit(1)
        else:
            http.processTask(headers, json.loads(response.read()))

    def delete_blueprint_instance(self, environment_instance):
        url = "%s/%s/%s/%s/%s" % (
            self.paasmanager_url, "envInst/org/FIWARE/vdc", self.vdc, "environmentInstance", environment_instance)

        headers = {'X-Auth-Token': self.token, 'Tenant-Id': self.vdc, 'Content-Type': "application/xml",
                   'Accept': "application/json"}

        response = http.delete(url, headers)

        ## Si la respuesta es la adecuada, creo el diccionario de los datos en JSON.
        if response.status != 200 and response.status != 204:
            data = response.read()
            print 'error to delete the environment ' + str(response.status) + " " + data
            sys.exit(1)
        else:
            http.processTask(headers, json.loads(response.read()))

    def get_blueprint_instance(self, environment_instance_name):
        url = "%s/%s/%s/%s/%s" % (
            self.paasmanager_url, "envInst/org/FIWARE/vdc", self.vdc, "environmentInstance", environment_instance_name)

        print url
        headers = {'X-Auth-Token': self.token, 'Tenant-Id': self.vdc, 'Content-Type': "application/xml",
                   'Accept': "application/json"}

        response = http.get(url, headers)

        ## Si la respuesta es la adecuada, creo el diccionario de los datos en JSON.
        if response.status != 200 and response.status != 204:
            data = response.read()
            print 'error to deploy the environment ' + str(response.status) + " " + data
            sys.exit(1)
        else:
            envInstance = self.__process_env_inst(json.loads(response.read()))
            envInstance.to_string()

    def add_application_instance(self, environment_instance_name, application_instance):
        url = "%s/%s/%s/%s/%s/%s" % (
            self.paasmanager_url, "envInst/org/FIWARE/vdc", self.vdc, "environmentInstance", environment_instance_name,
            "applicationInstance")
        headers = {'X-Auth-Token': self.token, 'Tenant-Id': self.vdc, 'Content-Type': "application/xml",
                   'Accept': "application/json"}
        print url
        print headers
        payload = tostring(application_instance.to_xml())
        response = http.post(url, headers, payload)

        ## Si la respuesta es la adecuada, creo el diccionario de los datos en JSON.
        if response.status != 200 and response.status != 204:
            print 'error to deploy the application instance ' + str(response.status)
            sys.exit(1)
        else:
            http.processTask(headers, json.loads(response.read()))

