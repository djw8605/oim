package edu.iu.grid.oim.lib;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	private String from;
	private String to;
	private String subject;
	private String text;
	
	public SendMail(String from, String to, String subject, String text){
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
	}
	
	public void send() throws MessagingException{
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", "25");
		//props.put("mail.debug", true);
		
		Session mailSession = Session.getDefaultInstance(props);
		Message simpleMessage = new MimeMessage(mailSession);
		
		InternetAddress fromAddress = null;
		InternetAddress toAddress = null;

		fromAddress = new InternetAddress(from);
		toAddress = new InternetAddress(to);

		simpleMessage.setFrom(fromAddress);
		simpleMessage.setRecipient(RecipientType.TO, toAddress);
		simpleMessage.setSubject(subject);
		simpleMessage.setText(text);
			
		Transport.send(simpleMessage);				
	}
	
	static public void sendErrorEmail(String content) throws MessagingException {
		String from = "goc@opensciencegrid.org";
		String to = "hayashis@indiana.edu";
		String subject = "OIM Error";
		String message = "OIM has detected an error\r\n" + content;

		SendMail sendMail = new SendMail(from, to, subject, message);
		sendMail.send();
	}
}