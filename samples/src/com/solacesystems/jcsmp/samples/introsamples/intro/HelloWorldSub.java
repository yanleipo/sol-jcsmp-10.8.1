/**
 *  Copyright 2012-2020 Solace Corporation. All rights reserved.
 *
 *  http://www.solace.com
 *
 *  This source is distributed under the terms and conditions
 *  of any contract or contracts between Solace and you or
 *  your company. If there are no contracts in place use of
 *  this source is not authorized. No support is provided and
 *  no distribution, sharing with others or re-use of this
 *  source is authorized unless specifically stated in the
 *  contracts referred to above.
 *
 * HelloWorldSub
 *
 * This sample shows the basics of creating session, connecting a session,
 * subscribing to a topic, and receiving a message. This is meant to be a
 * very basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import java.util.concurrent.CountDownLatch;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;

public class HelloWorldSub {

    public static void main(String... args) throws JCSMPException {
        if (args.length < 4) {
            System.out
                    .println("Usage: HelloWorldSub <msg_backbone_ip:port> <vpn> <client-username> <topic>");
            System.out
                    .println("        - to use wildcard for topic, that argument must be enclosed by quotes");
            System.exit(-1);
        }
        System.out.println("HelloWorldSub initializing...");
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);  // msg-backbone-ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]); // message-vpn
        // client-username (assumes no password)
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(args[3]);
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);

        final CountDownLatch latch = new CountDownLatch(1); // used for
                                                            // synchronizing b/w threads
        /** Anonymous inner-class for MessageListener 
         *  This demonstrates the async threaded message callback */
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            public void onReceive(BytesXMLMessage msg) {
                if (msg instanceof TextMessage) {
                    System.out.printf("TextMessage received: '%s'%n",
                            ((TextMessage)msg).getText());
                } else {
                    System.out.println("Message received.");
                }
                System.out.printf("Message Dump:%n%s%n",msg.dump());
                latch.countDown();  // unblock main thread
            }
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
                latch.countDown();  // unblock main thread
            }
        });
        session.addSubscription(topic);
        System.out.println("Connected. Awaiting message...");
        cons.start();
        // Consume-only session is now hooked up and running!

        try {
            latch.await(); // block here until message received, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }
        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();
    }
}
