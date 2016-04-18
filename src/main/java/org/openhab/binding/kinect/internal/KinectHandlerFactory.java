/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kinect.internal;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.kinect.KinectBindingConstants;
import org.openhab.binding.kinect.handler.KinectConnector;
import org.openhab.binding.kinect.handler.KinectConnector.DeviceCallback;
import org.openhab.binding.kinect.handler.KinectConnector.KinectItem;
import org.openhab.binding.kinect.handler.KinectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KinectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mattias Markehed
 */
public class KinectHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(KinectHandler.class);

    private Map<String, KinectServer> kinectServers = new HashMap<>();

    public static class KinectServer {

        private String host;
        private int port;
        private Map<String, KinectHandler> devices = new HashMap<String, KinectHandler>();

        private KinectConnector connector;

        public KinectServer(String host, int port) {
            super();
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void addDevice(KinectHandler device) {
            devices.put(device.getName(), device);
        }

        public void removeDevice(KinectHandler device) {
            removeDevice(device.getName());
        }

        public void removeDevice(String deviceName) {
            devices.remove(deviceName);
        }

        public Collection<KinectHandler> getDevices() {
            return devices.values();
        }

        public void stop() {
            connector.stop();
        }

        public void start() {
            connector = new KinectConnector(host, port);
            connector.setDeviceListCallback(new DeviceCallback() {
                @Override
                public void onItemsChanged(List<KinectItem> updateddevices) {
                    for (KinectItem item : updateddevices) {
                        KinectHandler device = devices.get(item.getName());
                        if (device != null) {
                            device.updateActive(item.getState());
                        }
                    }
                }
            });
            connector.start();
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return KinectBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        for (KinectServer server : kinectServers.values()) {
            server.stop();
        }
        super.removeHandler(thingHandler);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(KinectBindingConstants.THING_TYPE_DEVICE)) {
            String name = (String) thing.getConfiguration().get("name");
            String hostname = (String) thing.getConfiguration().get("hostname");
            BigDecimal port = (BigDecimal) thing.getConfiguration().get("port");

            logger.info("Initializing Kinect createHandler. " + hostname + " " + port + " " + name);

            String serverId = hostname + ":" + port;
            KinectServer server = kinectServers.get(serverId);
            if (server == null) {
                server = new KinectServer(hostname, port.intValue());
                kinectServers.put(serverId, server);
                server.start();
            }

            KinectHandler device = new KinectHandler(thing);
            server.addDevice(device);
            return device;
        }

        return null;
    }
}
