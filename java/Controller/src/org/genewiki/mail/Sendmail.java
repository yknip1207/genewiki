package org.genewiki.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A small class to handle sending alert emails. Defaults to smtp port of 587- make sure this is
 * open on the host machine. If Sendmail encounters an error, it is recommended that the program
 * fail safely rather than continuing (in the case of alerts being sent due to recurring failures).
 * @author eclarke
 *
 */
public class Sendmail {

	private final String from, pass;
	private final String host;

	
	/**
	 * Create a new Sendmail object that uses smtp.gmail.com as the
	 * smtp hostname.
	 * @param username
	 * @param password
	 */
	public Sendmail(String username, String password) {
		this(username, password, "smtp.gmail.com");
	}
	
	/**
	 * Create a new Sendmail object with the specified parameters.
	 * @param username
	 * @param password
	 * @param hostname smtp server
	 */
	public Sendmail(String username, String password, String hostname) {
		from = username;
		pass = password;
		host = hostname;
		Properties p = System.getProperties();
		p.setProperty("mail.smtp.starttls.enable", "true");
		p.setProperty("mail.smtp.host", host);
		p.setProperty("mail.smtp.user", from);
		p.setProperty("mail.smtp.password", pass);
		p.setProperty("mail.smtp.port", "465");
		p.setProperty("mail.smtp.auth", "true");
		p.setProperty("mail.smtp.socketFactory.port", "465");
		p.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		p.setProperty("mail.smtp.socketFactory.fallback", "false");
	}
	
	/**
	 * Sends an email message.
	 * @param to a valid email address
	 * @param subject the subject header
	 * @param content the message body
	 * @throws AddressException if email address (either the recipient or sender) is invalid
	 * @throws MessagingException on any other errors that may arise sending the email
	 */
	public void send(String[] to, String subject, String content) throws AddressException, MessagingException {
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props);
		
		// converting address strings to InternetAddresses
		InternetAddress[] recipients = new InternetAddress[to.length];
		for (int i=0; i<to.length; i++)
			recipients[i] = new InternetAddress(to[i]);
		
		// preparing message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		for (InternetAddress i : recipients) 
			message.addRecipient(Message.RecipientType.TO, i);				
		message.setSubject(subject);
		message.setText(content);
		
		// preparing transport
		Transport transport = session.getTransport("smtp");
		transport.connect(host, from, pass);
		transport.sendMessage(message, recipients);
		transport.close();
	}
	
}
