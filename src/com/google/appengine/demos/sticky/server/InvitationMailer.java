package com.google.appengine.demos.sticky.server;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvitationMailer {
	private static Logger log = LoggerFactory.getLogger(InvitationMailer.class);
	
	private String SITE_SUBJECT = "Join our Sticky-Site at ";
	private String SURFACE_SUBJECT = "Join Surface invitation";
	
	public InvitationMailer() {}
	
	public void sendJoinSiteInvitation(Store.Author sender, String receiverEmail, String siteUrl) {	
		try {
			Message msg = createMessage(sender, receiverEmail);
			msg.setSubject(SITE_SUBJECT + siteUrl);
			String body = sender.getName() + " invites you to " + 
				"join our Sticky-Site at\n" + 
				siteUrl + "\n\n" + 
				"After your first visit you can be added to surfaces!";
			
			msg.setText(body);
			
			sendMessage(msg);
		} catch (AddressException ex) {
			log.error(ex.getMessage(), ex);
		} catch (MessagingException ex) {
			log.error(ex.getMessage(), ex);
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void sendJoinSurfaceInvitation(Store.Author sender, String receiverEmail, Store.Surface surface, String siteUrl) {
		try {
			Message msg = createMessage(sender, receiverEmail);
			msg.setSubject(SURFACE_SUBJECT);
			String body = sender.getName() + " wants to share her/his " + 
				"surface '" + surface.getTitle() + "' with you!\n" +
				"The surface is already shared with the following users:\n";
			for(String author : surface.getAuthorNames()) {
				body += author + "\n";
			}
			body +="\nYou can reach us here: " + siteUrl;
			
			msg.setText(body);
			
			sendMessage(msg);
		} catch (AddressException ex) {
			log.error(ex.getMessage(), ex);
		} catch (MessagingException ex) {
			log.error(ex.getMessage(), ex);
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	private Message createMessage(Store.Author sender, String receiverEmail) throws
									UnsupportedEncodingException, MessagingException {
		
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties);
		
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(sender.getEmail(), sender.getName()));
		msg.addRecipient(RecipientType.TO, new InternetAddress(receiverEmail));
		
		return msg;
	}
	
	private boolean sendMessage(Message message) {
		try {
			Transport.send(message);
		} catch (MessagingException ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
		return true;
	}
}
