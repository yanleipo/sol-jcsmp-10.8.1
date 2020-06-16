/**
 * BlockingSubscriber.java
 * 
 * This blocking subscriber sample illustrates how to open a subscriber
 * channel, start reading from it, and retrieve a message from the
 * application thread.
 * 
 * Copyright 2006-2020 Solace Corporation. All rights reserved.
 */

package com.solacesystems.jcsmp.samples.introsamples;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPTransportException;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.samples.introsamples.common.ArgParser;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleApp;
import com.solacesystems.jcsmp.samples.introsamples.common.SampleUtils;
import com.solacesystems.jcsmp.samples.introsamples.common.SessionConfiguration;

public class BlockingSubscriber extends SampleApp {
	XMLMessageConsumer cons = null;
	SessionConfiguration conf = null;

	void createSession(String[] args) {
		// Parse command-line arguments.
		ArgParser parser = new ArgParser();
		if (parser.parse(args) == 0)
			conf = parser.getConfig();
		else
			printUsage(parser.isSecure());
		
		session = SampleUtils.newSession(conf, new PrintingSessionEventHandler(),null);
	}
	
	void printUsage(boolean secure) {
		String strusage = ArgParser.getCommonUsage(secure);
		System.out.println(strusage);
		finish(1);
	}
	
	public BlockingSubscriber() {
	}

	public static void main(String[] args) {
		BlockingSubscriber bsub = new BlockingSubscriber();
		bsub.run(args);
	}

	void run(String[] args) {
		createSession(args);

		try {
			// Acquire a blocking message consumer and open the data channel to
			// the appliance.
			System.out.println("About to connect to appliance.");
			cons = session.getMessageConsumer((XMLMessageListener) null);
			printRouterInfo();
			
			// Use a Topic subscription.
			Topic topic = JCSMPFactory.onlyInstance().createTopic(SampleUtils.SAMPLE_TOPIC);
			System.out.printf("Setting topic subscription '%s'...\n", topic.getName());
			session.addSubscription(topic);
			System.out.println("Connected!");

			// Receive a message (wait up to 60 seconds).
			cons.start();
			System.out.println("About to attempt to receive message (timeout 60s).");
			BytesXMLMessage msg = cons.receive(60000);
			if (msg != null) {
				printRxMessage(msg);
			} else {
				System.out.println("No message received.");
			}
			cons.stop();			
			finish(0);
		} catch (JCSMPTransportException ex) {
			System.err.println("Encountered a JCSMPTransportException, closing consumer channel... " + ex.getMessage());
			if (cons != null) {
				cons.close();
				// At this point the consumer handle is unusable; a new one should be created 
				// by calling cons = session.getMessageConsumer(...) if the application 
				// logic requires the consumer channel to remain open.
			}
			finish(1);
		} catch (JCSMPException ex) {
			System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
			// Possible causes: 
			// - Authentication error: invalid username/password 
			// - Provisioning error: unable to add subscriptions from CSMP
			// - Invalid or unsupported properties specified
			if (cons != null) {
				cons.close();
				// At this point the consumer handle is unusable; a new one should be created 
				// by calling cons = session.getMessageConsumer(...) if the application 
				// logic requires the consumer channel to remain open.
			}
			finish(1);
		} catch (Exception ex) {
			System.err.println("Encountered an Exception... " + ex.getMessage());
			finish(1);
		}
	}

}
