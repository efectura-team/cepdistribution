/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.distribution.editor.core.util.restclients.storequery;

import feign.Response;
import io.siddhi.distribution.editor.core.exception.SiddhiStoreQueryHelperException;
import org.apache.log4j.Logger;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.HashMap;

/**
 * Utility class to access the Siddhi Store API.
 */
public class StoreQueryAPIHelper {

    private static final Logger logger = Logger.getLogger(StoreQueryAPIHelper.class);
    private static final String STORE_API_CONFIG = "storeRESTAPI";
    private ConfigProvider configProvider;

    /**
     * Initialize the class with config provider.
     *
     * @param configProvider ConfigProvider object
     */
    public StoreQueryAPIHelper(ConfigProvider configProvider) {

        this.configProvider = configProvider;
    }

    /**
     * Execute store query API.
     *
     * @param query Query to be executed
     * @return HTTP Response
     * @throws SiddhiStoreQueryHelperException when executing store queries
     */
    public Response executeStoreQuery(String query) throws SiddhiStoreQueryHelperException {

        // TODO: 9/23/19 Temporary fix till  https://github.com/siddhi-io/distribution/issues/426 is solved
        try {
            HashMap<String, String> storeAPIConfig = (HashMap<String, String>)
                                                            configProvider.getConfigurationObject(STORE_API_CONFIG);
            String url;
            if (storeAPIConfig != null) {
                String host = storeAPIConfig.get("host") != null ? storeAPIConfig.get("host") : "0.0.0.0";
                String port = storeAPIConfig.get("port") != null ? storeAPIConfig.get("port") : "9390";
                url = host + ":" + port;
            } else {
                url = "0.0.0.0:9390";
            }
            return StoreQueryHTTPClient.executeStoreQuery(url, query);
        } catch (ConfigurationException e) {
            throw new SiddhiStoreQueryHelperException("Cannot read store query API configurations.", e);
        }
    }

    /**
     * Get store API host with the port from the deployment.yaml.
     *
     * @return Host with the port
     * @throws SiddhiStoreQueryHelperException when executing store queries
     */
    private String getStoreAPIHost() throws SiddhiStoreQueryHelperException {

        try {
            TransportsConfiguration transportsConfiguration = this.configProvider.getConfigurationObject
                    ("transports", TransportsConfiguration.class);
            for (ListenerConfiguration listenerConfiguration : transportsConfiguration.getListenerConfigurations()) {
                if ("default".equals(listenerConfiguration.getId())) {
                    logger.debug("Default configurations found in listener configurations.");
                    return listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
                }
            }
        } catch (ConfigurationException e) {
            throw new SiddhiStoreQueryHelperException("Cannot read store query API configurations.", e);
        }
        throw new SiddhiStoreQueryHelperException("Cannot find store query API configurations.");
    }
}
