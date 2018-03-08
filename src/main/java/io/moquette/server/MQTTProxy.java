/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2017
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
// Created By : Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package io.moquette.server;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.server.config.ClasspathResourceLoader;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.IResourceLoader;
import io.moquette.server.config.ResourceLoaderConfig;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.fusesource.hawtbuf.Buffer.utf8;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MQTTEvent;
/**
 * Launch a configured version of the server.
 */
public class MQTTProxy {

    /**
     * The MQTT target broker.
     */
    private int proxyPort = 1884;
    private MQTTListen worker1;
    private BlockingConnection brokerConnection;


    /**
     * The thread that listens for events from the broker so they can be
     * notified on the proxy.
     */
    private class MQTTListen extends Thread {

        @Override
        public void run() {
            // Loop for ten iterations.

            while(true){
                try {
                    Message message = brokerConnection.receive(1000, TimeUnit.MILLISECONDS);
                    if(message != null) {
                        MQTT mqtt = new MQTT();
                        mqtt.setHost("localhost", proxyPort);
                        BlockingConnection workerConnection = mqtt.blockingConnection();
                        workerConnection.connect();

                        String topicName = message.getTopic();
                        workerConnection.publish(topicName, message.getPayload(), QoS.EXACTLY_ONCE, true);
                    }
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // break out of the loop to close the thread
                    break;
                } catch (Exception ex) {
                    Logger.getLogger(MQTTProxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * The message handler that redirects MQTT messages from the proxy to the
     * external broker.
     */
    private class PublisherListener extends AbstractInterceptHandler {
        private EventCapture stm = null;

        public PublisherListener(final EventCapture stmIn) {
            stm = stmIn;
        }

        /**
         * Get the proxy id
         * @return The proxy identifier.
         */
        @Override
        public String getID() {
            return "EmbeddedLauncherPublishListener";
        }

        /**
         * A client publishes an event; the broker intercepts this publish
         * message and then connects to the remote broker and republishes the
         * message via an MQTT message.
         * @param msg The message that will be forwarded
         */
        @Override
        public void onPublish(InterceptPublishMessage msg) {
             System.out.println(
                    "Received on topic: " + msg.getTopicName());
            try {
                ByteBuf buf = msg.getPayload();
                byte[] bytes = new byte[buf.readableBytes()];
                int readerIndex = buf.readerIndex();
                buf.getBytes(readerIndex, bytes);

                String topicName = msg.getTopicName();
                MqttQoS qos = msg.getQos();

                final MQTTEvent rResp = new MQTTEvent();

                /*
                * Create a REST event about the Service Response i.e. capture and
                * uniform the data to be understood by the state machine rule checker
                */
                rResp.addParameter(new Parameter("mqtt.msg", "publish"));
                rResp.addParameter(new Parameter("mqtt.topic", topicName));
                rResp.addParameter(new Parameter("mqtt.qos", "" + qos.value()));
                rResp.addParameter(new Parameter("mqtt.clientid", msg.getClientID()));
                rResp.addParameter(new Parameter("mqtt.retain-flag", ""+ msg.isRetainFlag()));
                rResp.addParameter(new Parameter("mqtt.dupFlag", ""+msg.isDupFlag()));

                final ByteBuf msgContent = msg.getPayload();
                rResp.addContent("application/octet-stream", new String(bytes));


//                Topic[] topics = {new Topic(utf8(topicName), valQoS)};
//                byte[] qoses = brokerConnection.subscribe(topics);
//
//                brokerConnection.publish(topicName, bytes, valQoS, false);
//                Message message = brokerConnection.receive();

                // To let the server know that it has been processed.
//                message.ack();
                stm.pushEvent(rResp);
                System.out.println(
                        "Re-published on topic: " + msg.getTopicName() + " content: " + new String(bytes));
            } catch (Exception ex) {
                Logger.getLogger(MQTTProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            System.out.println(
                    "Received on topic: " + msg.getTopicFilter());
            try {
                String topicName = msg.getTopicFilter();
                MqttQoS qos = msg.getRequestedQos();

                QoS valQoS = null;
                switch (qos) {
                    case AT_LEAST_ONCE:
                        valQoS = QoS.AT_LEAST_ONCE;
                        break;
                    case AT_MOST_ONCE:
                        valQoS = QoS.AT_MOST_ONCE;
                        break;
                    case EXACTLY_ONCE:
                        valQoS = QoS.EXACTLY_ONCE;
                        break;
                    default:
                        break;
                }

                final MQTTEvent rResp = new MQTTEvent();

                /*
                * Create a REST event about the Service Response i.e. capture and
                * uniform the data to be understood by the state machine rule checker
                */
                rResp.addParameter(new Parameter("mqtt.msg", "subscribe"));
                rResp.addParameter(new Parameter("mqtt.topic", topicName));
                rResp.addParameter(new Parameter("mqtt.qos", "" + qos.value()));
                rResp.addParameter(new Parameter("mqtt.clientid", msg.getClientID()));

                Topic[] topics = {new Topic(utf8(topicName), valQoS)};
                byte[] qoses = brokerConnection.subscribe(topics);
                stm.pushEvent(rResp);
            } catch (Exception ex) {
                Logger.getLogger(MQTTProxy.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            try {
                String topicName = msg.getTopicFilter();

                String[] topics = {topicName};
                brokerConnection.unsubscribe(topics);

                 final MQTTEvent rResp = new MQTTEvent();

                /*
                * Create a REST event about the Service Response i.e. capture and
                * uniform the data to be understood by the state machine rule checker
                */
                rResp.addParameter(new Parameter("mqtt.msg", "unsubscribe"));
                rResp.addParameter(new Parameter("mqtt.topic", topicName));
                rResp.addParameter(new Parameter("mqtt.clientid", msg.getClientID()));

            } catch (Exception ex) {
                Logger.getLogger(MQTTProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
    * The reference to the state machine push event interface, i.e.
    * the redirector creates events and passes them to the state machine.
    */
    private transient EventCapture stateMachine;

    private final Server server;

    public MQTTProxy(String address, int port, final EventCapture stm){
        stateMachine = stm;
        MQTT mqtt = new MQTT();
        try {
            mqtt.setHost(address, port);
            brokerConnection = mqtt.blockingConnection();
            brokerConnection.connect();

        } catch (URISyntaxException ex) {
            System.err.println("Invalid MQTT address:  " + ex.getReason());
        } catch (Exception ex) {
            System.err.println("Unable to connect to broker:  " + ex.getMessage());
        }

        server = new Server();

    }

    /**
     * Starts the MQTT inteceptor
     *
     * @throws IOException
     *             in case of any IO error.
     */
    public void startServer(EventCapture stm) throws IOException {
        IResourceLoader classpathLoader = new ClasspathResourceLoader();
        final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);

        List<? extends InterceptHandler> userHandlers = Collections.singletonList(new PublisherListener(stm));
        server.startServer(classPathConfig, userHandlers);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MQTTProxy.class.getName()).log(Level.SEVERE, null, ex);
        }

//        server.startServer();
//        server.addInterceptHandler(new PublisherListener());

        //Create a thread that listens for events and then sends them back to this proxy
        worker1 = new MQTTListen();
        worker1.start();
    }

    /**
     * Stop the MQTT interceptor
     *
     */
    public void stopServer() {
       server.stopServer();
       worker1.interrupt();
    }

}
