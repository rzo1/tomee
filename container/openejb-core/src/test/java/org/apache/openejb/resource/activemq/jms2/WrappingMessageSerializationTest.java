/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.activemq.jms2;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.openejb.activemq.JMS2AMQTest;
import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.proxy.SampleLocalBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.transaction.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class WrappingMessageSerializationTest {
    private static final String TEXT = "foo";

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()

                .p("amq", "new://Resource?type=ActiveMQResourceAdapter")
                .p("amq.DataSource", "")
                .p("amq.BrokerXmlConfig", "broker:(vm://localhost)")

                .p("target", "new://Resource?type=Queue")

                .p("mdbs", "new://Container?type=MESSAGE")
                .p("mdbs.ResourceAdapter", "amq")

                .p("cf", "new://Resource?type=" + ConnectionFactory.class.getName())
                .p("cf.ResourceAdapter", "amq")

                .p("xaCf", "new://Resource?class-name=" + ActiveMQXAConnectionFactory.class.getName())
                .p("xaCf.BrokerURL", "vm://localhost")

                .build();
    }


    @Module
    @Classes(cdi = true, value = { JustHereToCheckDeploymentIsOk.class, Listener.class})
    public MessageDrivenBean jar() {
        return new MessageDrivenBean(Listener.class);
    }

    @Resource(name = "cf")
    private ConnectionFactory cf;

    @Inject
    @JMSConnectionFactory("cf")
    private JMSContext context;

    @Inject // just there to ensure the injection works and we don't require @JMSConnectionFactory
    private JMSContext defaultContext;

    @Test
    public void serializeByteMessage() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            BytesMessage message = context.createBytesMessage();
            message.setStringProperty("text", TEXT);

            WrappingByteMessage wrappingByteMessage = new WrappingByteMessage(message);

            assertNotNull(wrappingByteMessage);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(wrappingByteMessage);

            final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);

            final WrappingByteMessage deserialized = (WrappingByteMessage) ois.readObject();

            assertNotNull(deserialized);
            assertEquals(wrappingByteMessage.getStringProperty("text"), deserialized.getStringProperty("text"));

        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void serializeObjectMessage() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            ObjectMessage message = context.createObjectMessage();
            message.setStringProperty("text", TEXT);

            WrappingObjectMessage wrappingObjectMessage = new WrappingObjectMessage(message);

            assertNotNull(wrappingObjectMessage);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(wrappingObjectMessage);

            final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);

            final WrappingByteMessage deserialized = (WrappingByteMessage) ois.readObject();

            assertNotNull(deserialized);
            assertEquals(wrappingObjectMessage.getStringProperty("text"), deserialized.getStringProperty("text"));

        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void serializeStreamMessage() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            StreamMessage message = context.createStreamMessage();
            message.setStringProperty("text", TEXT);

            WrappingStreamMessage wrappingStreamMessage = new WrappingStreamMessage(message);

            assertNotNull(wrappingStreamMessage);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(wrappingStreamMessage);

            final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);

            final WrappingByteMessage deserialized = (WrappingByteMessage) ois.readObject();

            assertNotNull(deserialized);
            assertEquals(wrappingStreamMessage.getStringProperty("text"), deserialized.getStringProperty("text"));

        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void serializeMapMessage() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            MapMessage message = context.createMapMessage();
            message.setStringProperty("text", TEXT);

            WrappingMapMessage wrappingMapMessage = new WrappingMapMessage(message);

            assertNotNull(wrappingMapMessage);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(wrappingMapMessage);

            final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);

            final WrappingByteMessage deserialized = (WrappingByteMessage) ois.readObject();

            assertNotNull(deserialized);
            assertEquals(wrappingMapMessage.getStringProperty("text"), deserialized.getStringProperty("text"));

        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }


    @Test
    public void serializeTextMessage() throws Exception {
        try (final JMSContext context = cf.createContext()) {
            TextMessage message = context.createTextMessage();
            message.setStringProperty("text", TEXT);

            WrappingTextMessage wrappingTextMessage = new WrappingTextMessage(message);

            assertNotNull(wrappingTextMessage);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(wrappingTextMessage);

            final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);

            final WrappingByteMessage deserialized = (WrappingByteMessage) ois.readObject();

            assertNotNull(deserialized);
            assertEquals(wrappingTextMessage.getStringProperty("text"), deserialized.getStringProperty("text"));

        } catch (final JMSRuntimeException ex) {
            fail(ex.getMessage());
        }
    }



    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "target")
    })
    public static class Listener implements MessageListener {
        public static volatile CountDownLatch latch;
        public static volatile boolean ok = false;

        @Override
        public void onMessage(final Message message) {
            try {
                try {
                    ok = (TextMessage.class.isInstance(message)
                            && TEXT.equals(TextMessage.class.cast(message).getText())
                            && TEXT.equals(message.getBody(String.class)))
                            || message.getStringProperty("text").equals(TEXT);
                } catch (final JMSException e) {
                    // no-op
                }
            } finally {
                latch.countDown();
            }
        }

        public static void reset() {
            latch = new CountDownLatch(1);
            ok = false;
        }

        public static boolean sync() throws InterruptedException {
            latch.await(1, TimeUnit.MINUTES);
            return ok;
        }
    }

    @TransactionScoped
    public static class JustHereToCheckDeploymentIsOk implements Serializable {
        @Inject
        private JMSContext context;

        public void ok() {
            assertNotNull(context);
        }
    }


}
