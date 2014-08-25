package com.lombardrisk.activemq.queue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * queue: Producer
 */
public class Producer {

	public static void main(String[] args) throws IOException {
		InputStream is = System.in;
		Scanner scan = new Scanner(is);

		// System.out.println("Enter the target queue name: ");
		String rquestQueueName = "requestQueueXbrl";
		String replyQueueName = "replyQueueXbrl";

		System.out
				.println("Enter the XML message file path and name:\n(Use default file: {current path}\\xbrlExpRequest.xml press \"y\")");
		String path = scan.next();

		System.out.println("Enter destination IP. (use default localhost, input \"1\")");
		String ip = scan.next();

		scan.close();

		if (StringUtils.isBlank(path) || path.equals("y")) {
			path = "xbrlExpRequest.xml";
		}

		File curFile = new File(".");
		System.out.println("Request message file: " + curFile.getCanonicalPath() + File.separator + path);

		// Create a ConnectionFactory
		String user = ActiveMQConnection.DEFAULT_USER;
		String password = ActiveMQConnection.DEFAULT_PASSWORD;

		String url = "";
		ConnectionFactory contectionFactory = null;

		if (ip.equals("1")) {
			url = ActiveMQConnection.DEFAULT_BROKER_URL;
			contectionFactory = new ActiveMQConnectionFactory(user, password, url);
		} else {
			url = "tcp://" + ip + ":61616";
			contectionFactory = new ActiveMQConnectionFactory(url);
		}

		try {
			// Create a Connection
			Connection connection = contectionFactory.createConnection();
			connection.start();

			System.out.println("Connect to OCELOT server successfully.");

			// Create a Session
			Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination rquestDestination = session.createQueue(rquestQueueName);
			Destination replyDestination = session.createQueue(replyQueueName);

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(rquestDestination);
			System.out.println("Set DeliveryMode to NON_PERSISTENT");
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a messages
			StringBuilder sb = new StringBuilder();

			File file = new File(path);

			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			TextMessage message = session.createTextMessage(sb.toString());
			message.setJMSReplyTo(replyDestination);
			message.setJMSCorrelationID("xbrlRequestCorrelationId");

			producer.send(message);
			System.out.println("Send request to destination: " + rquestQueueName);
			System.out.println("Message content: " + sb.toString());
			System.out.println("Reply destination: " + replyQueueName);

			session.commit();
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
