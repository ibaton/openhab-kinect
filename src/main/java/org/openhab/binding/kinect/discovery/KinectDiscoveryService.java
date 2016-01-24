/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kinect.discovery;

import static org.openhab.binding.kinect.KinectBindingConstants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.kinect.handler.KinectConnector;
import org.openhab.binding.kinect.handler.KinectConnector.DeviceCallback;
import org.openhab.binding.kinect.handler.KinectConnector.KinectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KinectDiscoveryService} is responsible for finding kinect servers on network.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class KinectDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(KinectDiscoveryService.class);

    private JmDNS dnsService;

    public KinectDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 900, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {

        // Start looking for kinect servers
        startAutomaticRefresh();
    }

    /**
     * Create a virtual device.
     *
     * @param host server host
     * @param port the server port
     * @param name name of virtual device
     */
    private void createDevice(String host, int port, String name) {
        ThingUID uid = new ThingUID(THING_TYPE_DEVICE, name);

        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put("hostname", host);
            properties.put("port", port);
            properties.put("name", name);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Kinect Device").build();
            thingDiscovered(result);
        }
    }

    private ServiceListener mdnsServiceListener = new ServiceListener() {
        @Override
        public void serviceAdded(ServiceEvent serviceEvent) {
            logger.debug("Service found: " + serviceEvent.toString());
            dnsService.requestServiceInfo("_openhab-kinect._tcp", serviceEvent.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent serviceEvent) {
        }

        /**
         * Called when kinect service is found.
         */
        @Override
        public void serviceResolved(ServiceEvent serviceEvent) {
            // Test service info is resolved.
            String[] serviceUrls = serviceEvent.getInfo().getURLs();

            logger.info("Found kinect service " + serviceEvent.getInfo().getURLs()[0].toString());

            if (serviceUrls.length > 0) {
                logger.info("Connecting to kinect service 2");

                try {
                    final URL url = new URL(serviceUrls[0]);
                    logger.info("Connecting to kinect service " + url.getHost() + ":" + url.getPort());
                    final KinectConnector connector = new KinectConnector(url.getHost(), url.getPort());
                    connector.setDeviceListCallback(new DeviceCallback() {
                        @Override
                        public void onItemsChanged(List<KinectItem> devices) {
                            logger.info("Found kinect objects");
                            for (KinectItem item : devices) {
                                createDevice(url.getHost(), url.getPort(), item.getName().replace(" ", ""));
                            }
                            connector.stop();
                        }
                    });
                    connector.start();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    /**
     * Stop looking for kinect servers.
     */
    public void stopAutomaticRefresh() {
        try {
            dnsService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start looking for kinect servers.
     */
    public void startAutomaticRefresh() {
        logger.info("startAutomaticRefresh");
        try {
            String serviceType = "_openhab-kinect._tcp.local.";
            dnsService = JmDNS.create();
            dnsService.addServiceListener(serviceType, mdnsServiceListener);
            logger.info("startAutomaticRefresh 2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
