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
 *  HelloWorldPub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  and publishing a direct message to a topic. This is meant to be a very
 *  basic example for demonstration purposes.
 */

package com.solacesystems.jcsmp.samples.introsamples.intro;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class HelloWorldPub {
    
    public static void main(String... args) throws JCSMPException {
    	// Check command line arguments
        if (args.length < 4) {
            System.out.println("Usage: HelloWorldPub <msg_backbone_ip:port> <vpn> <client-username> <topic>");
            System.exit(-1);
        }
        System.out.println("HelloWorldPub initializing...");

    	// Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);      // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  // client-username (assumes no password)
        final JCSMPSession session =  JCSMPFactory.onlyInstance().createSession(properties);
        
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(args[3]);
        
        session.connect();
        /** Anonymous inner-class for handling publishing events */
        XMLMessageProducer prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e);
            }
        });

        // Publish-only session is now hooked up and running!
        
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        final String text = "Hello world!";
        msg.setText(text);
        System.out.printf("Connected. About to send message '%s' to topic '%s'...%n",text,topic.getName());
        prod.send(msg,topic);
        System.out.println("Message sent. Exiting.");
        session.closeSession();
    }
}
