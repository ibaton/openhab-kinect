package org.openhab.binding.kinect.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KinectConnector {
    private final Logger logger = LoggerFactory.getLogger(KinectConnector.class);

    private Gson gson = new GsonBuilder().create();

    private String host;
    private int port;
    private DeviceCallback callback;
    private boolean running = true;
    private Socket client;

    /**
     * Setup kinect server communication.
     *
     * @param host the kinect server host.
     * @param port the kinect server port.
     */
    public KinectConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Add callback listening for virtual devices state.
     *
     * @param callback
     */
    public void setDeviceListCallback(DeviceCallback callback) {
        this.callback = callback;
    }

    /**
     * Start the communication between client and openhab server.
     */
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setup();
            }
        }).start();
    }

    /**
     * Stop communication with server
     */
    public void stop() {
        logger.info("Closing connection to Kinect Server " + host);
        running = false;
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the server.
     */
    private void setup() {
        while (running) {
            try {
                logger.info("Connecting to Kinect Server " + host + ":" + port);
                client = new Socket(host, port);
                logger.info("Connected to Kinect Server " + host + ":" + port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (running && client.isConnected()) {
                    String line = reader.readLine();
                    KinectMessage message = gson.fromJson(line, KinectMessage.class);
                    callback.onItemsChanged(message.getItems());
                }
            } catch (IOException e) {
                logger.error("Kinect Socket Error ", e);
                e.printStackTrace();
            }

            if (running) {
                try {
                    logger.info("Connection to kinect server lost reconnecting in 60 seconds");
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface DeviceCallback {
        void onItemsChanged(List<KinectItem> devices);
    }

    /**
     * A virtual device on kinect server.
     */
    public static class KinectItem {
        private String name;
        private boolean state;

        /**
         * Get the name of virtual device
         *
         * @return name of device.
         */
        public String getName() {
            return name;
        }

        /**
         * Get the active state of virtual device
         *
         * @return true if device is pointed at, else false.
         */
        public boolean getState() {
            return state;
        }
    }

    /**
     * Message sent from kinect server.
     */
    public static class KinectMessage {
        private int mtype;
        private int size;
        private List<KinectItem> items;

        /**
         * Get all items on kinect server.
         *
         * @return message containing virtual kinect items.
         */
        public List<KinectItem> getItems() {
            return new ArrayList<KinectItem>(items);
        }

        /**
         * Get the type of message
         *
         * @return message type
         */
        public int getMessageType() {
            return mtype;
        }

        /**
         * Get number of items on server.
         *
         * @return number of virtual devices.
         */
        public int getSize() {
            return size;
        }
    }
}
