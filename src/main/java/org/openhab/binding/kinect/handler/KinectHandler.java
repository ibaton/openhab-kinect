/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kinect.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.kinect.KinectBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KinectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class KinectHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(KinectHandler.class);

    private String name;
    private String hostname;
    private BigDecimal port;

    public KinectHandler(Thing thing) {
        super(thing);

        name = (String) thing.getConfiguration().get("name");
        hostname = (String) thing.getConfiguration().get("hostname");
        port = (BigDecimal) thing.getConfiguration().get("port");
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Update state of virtual device
     *
     * @param state true if device is active
     */
    public void updateActive(boolean state) {
        updateState(KinectBindingConstants.CHANNEL_ACTIVE, state ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void initialize() {
        logger.info("Initializing Kinect handler.");
        super.initialize();
    }

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public BigDecimal getPort() {
        return port;
    }

    public void setPort(BigDecimal port) {
        this.port = port;
    }
}
