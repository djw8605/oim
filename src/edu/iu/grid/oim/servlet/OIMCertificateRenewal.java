package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.SideContentView;

public class OIMCertificateRenewal extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		//Authorization auth = context.getAuthorization();
		
		BootMenuView menuview = new BootMenuView(context, "home");
		BootPage page = new BootPage(context, menuview, new Content(context), createSideView(context));
		page.addExCSS("home.css");

		GenericView header = new GenericView();
		//header.add(new HtmlView("<h1>OSG Information Management System</h1>"));
		//	header.add(new HtmlView("<p class=\"lead\">Defines the topology used by various OSG services based on the <a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18\">OSG Blueprint Document</a></p>"));

		page.setPageHeader(header);
		
		page.render(response.getWriter());
	}
	
	class Content implements IView {
		UserContext context;
		public Content(UserContext context) {
			this.context = context;
		}
		
		@Override
		public void render(PrintWriter out) {
			//out.write("<div>");
			Authorization auth = context.getAuthorization();
		
			out.write("<ul>");
			out.write("<h1><a name=\"OSG_PKI_Certificate_Renewal\"></a>  <strong>OSG PKI Certificate Renewal</strong> </h1>");
			out.write("<p />");
			out.write("<div class=\"twikiToc\"> <ul>");
			out.write("<li> <a href=\"#Introduction\"> Introduction</a>");
			out.write("</li> <li> <a href=\"#Supported_Modes_of_Renewal\"> Supported Modes of Renewal</a>");
			out.write("</li> <li> <a href=\"#Certificate_Lifecycle\"> Certificate Lifecycle</a>");
			out.write("</li> <li> <a href=\"#Notification\"> Notification</a>");
			out.write("</li> <li> <a href=\"#OIM_Certificate_Renewal_Page\"> OIM Certificate Renewal Page</a>");
			out.write("</li> <li> <a href=\"#Relevant_Policies\"> Relevant Policies</a>");
			out.write("</li></ul> ");
			out.write("</div>");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Introduction\"></a> Introduction </span></h2>");
			out.write("<p />");
			out.write("This document explains the OSG PKI certificate renewal process.  This will provide a brief overview on how a user will obtain a new certificate for the same DN, but with a new expiration date and key pair."); 
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Supported_Modes_of_Renewal\"></a> Supported Modes of Renewal </span></h2>");
			out.write("<p /> <ul>");
			out.write("<li> Users may request renewal of their own user certificate via either a new CLI script or the web (OIM). <ul>");
			out.write("<li> RAs may not request renewal of a user's certificate.");
			out.write("</li></ul> ");
			out.write("</li> <li> User must have valid certificate and be registered with OIM. <ul>");
			out.write("<li> No guest/un-authenticated renewal.");
			out.write("</li></ul> ");
			out.write("</li> <li> If renewal cannot be accomplished for any reason, the user must re-request and go through the vetting process again.");
			out.write("</li> <li> Renewal can only be done if the vetting is less than 5-years old.");
			out.write("</li> <li> The user's current email address in OIM must match the email address in their certificate. <ul>");
			out.write("<li> To comply with policy on unchanged attributes.");
			out.write("</li></ul> ");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Certificate_Lifecycle\"></a> Certificate Lifecycle </span></h2>");
			out.write("<p /> <ul>");
			out.write("<li> 0-6 months: Certificate cannot be renewed. <ul>");
			out.write("<li> To prevent multiple accidental renewals.");
			out.write("</li> <li> Note that OIM-ITB will not enforce this restriction to allow for testing.");
			out.write("</li></ul> ");
			out.write("</li> <li> 7-12 months: Certificate can be renewed.");
			out.write("</li> <li> 12-13 months: <ul>");
			out.write("<li> Weekly email notifications are sent to certificate owner reminding them of renewal. <ul>");
			out.write("<li> Once renewed, notifications will stop.");
			out.write("</li></ul> ");
			out.write("</li> <li> A single email notification is sent to the original requestor of host certificates.");
			out.write("</li></ul> ");
			out.write("</li> <li> 13 months: Certificate expires, cannot be renewed.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("Note that, by policy, user vetting is required every 5 years and renewal is only possible if user has been vetting in past 5 years.");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Notification\"></a> Notification </span></h2>");
			out.write("<p /> <ul>");
			out.write("<li> Starting one month from expiration, weekly reminders are sent to the user. <ul>");
			out.write("<li> Or to the requestor of a user certificate.");       
			out.write("</li></ul>");
			out.write("</li> <li> On renewal, an email is sent to the user and relevant RAs/GAs notifying them of the renewal and asking them to contact the GOC if the renewal is unauthorized. <ul>");
			out.write("<li> Required by policy.");
			out.write("</li> <li> An example of an unauthorized renewal would be a user who has left a VO.");
			out.write("</li></ul>");
			out.write("</li> <li> The following is an example of a renewal email notification");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<p> <img alt=\"cert_renew_email.png\" src=\"https://twiki.grid.iu.edu/twiki/pub/Documentation/OSGPKICertificateRenewal/cert_renew_email.png\" /></p>");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"OIM_Certificate_Renewal_Page\"></a> OIM Certificate Renewal Page </span></h2>");
			out.write("<p /> <ul>");
			out.write("<li> User can access the certificate renewal page in OIM by clicking on the link provided at the bottom of the email notification.");
			out.write("</li> <li> The user will be taken to the OIM certificate renewal page.  The following is an example of the page.");
			out.write("</li></ul>"); 
			out.write("<p />");
			out.write("<p> <img alt=\"cert_renew_web.png\" src=\"https://twiki.grid.iu.edu/twiki/pub/Documentation/OSGPKICertificateRenewal/cert_renew_web.png\" /></p>");
			out.write("<p /> <ul>");
			out.write("<li> User will need to select the checkbox next to 'I Agree' button and click 'Next'");
			out.write("</li></ul>");
			out.write("<p /> <ul>");
			out.write("<li> The user will need to enter a new password twice before clicking on the green 'Renew' button in the 'Next Action' section at the bottom.");
			out.write("</li></ul>"); 
			out.write("<p />");
			out.write("<p> <img alt=\"cert_renew_passwd.png\" src=\"https://twiki.grid.iu.edu/twiki/pub/Documentation/OSGPKICertificateRenewal/cert_renew_passwd.png\" /></p>");
			out.write("<p /> <ul>");
			out.write("<li> The page will refresh and the user will then now be able to download their new certificate (pkcs12 file).");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<p> <img alt=\"cert_renew_download.png\" src=\"https://twiki.grid.iu.edu/twiki/pub/Documentation/OSGPKICertificateRenewal/cert_renew_download.png\" /></p>");
			out.write("<p /> <ul>");
			out.write("<li> Last step for the user is to click on the blue 'Download Certificate &amp; Private Key (PKCS12)' button and select a location to save the certificate.");
			out.write("</li></ul>");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Relevant_Policies\"></a> Relevant Policies </span></h2>");
			out.write("<p />");
			out.write("Policy with regards to renewal follows. In short, a certificate can be automatically renewed if not expired, nothing has changed, it hasn't been compromised and it has been less than 5 years since manual RA vetting.");
			out.write("<p />");
			out.write("Note the last bit, email should be sent to the user on renewal.");
			out.write("<p />");
			out.write("Relevant portions of Section 4.6 of the <a href=\"https://twiki.grid.iu.edu/twiki/pub/Operations/OSGPKIAgreements/OSG_RPS.pdf\" target=\"_top\">RPS</a>:");
			out.write("<p />");
			out.write("The OSG Operator may renew a certificate if:");
			out.write("<p /> <ol>");
			out.write("<li>    the associated public key has not reached the end of its validity period,");
			out.write("</li> <li>    the Subscriber name and attributes are unchanged,");
			out.write("</li> <li>    the associated private key remains un compromised, and");
			out.write("</li> <li>    re-verification of the Subscriber’s identity is not required under Section 3.3.1.");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("Section 3.3.1 says: \"OSG certificates have a validity period of 13 months. OSG may rekey/renew certificates prior to their expiration date for additional 13 month periods up to a maximum of five years. OSG or a Trusted Agent revalidates the certificate information at least once every five years.\"");
			out.write("<p />");
			out.write("No additional verification is required if the certificate subject information has not changed and less than five years have passed since the certificate’s information was verified. A Trusted Agent must represent that the renewal request is authorized.");
			out.write("<p />");
			out.write("The OSG Operator shall use contact information provided by the Subscriber to notify the Subscriber of the certificate’s issuance.");
			out.write("<p />");
			out.write("<p />");
		}
	}
    
	private SideContentView createSideView(UserContext context)
	{
		SideContentView contentview = new SideContentView();
		Authorization auth = context.getAuthorization();
		
	
		//contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures", "Operating Procedures", true));

		if(auth.isUser()) {
		    //	contentview.addContactLegend();
		}
		
		return contentview;
	}
	
	@SuppressWarnings("serial")
	class Confirmation extends DivRep
	{
		final ContactRecord crec;
		final ContactModel cmodel;
		final UserContext context;
		
		public Confirmation(Integer contact_id, UserContext _context) throws SQLException {
			super(_context.getPageRoot());
			
	    	cmodel = new ContactModel(_context);
	    	crec = (ContactRecord) cmodel.get(contact_id);//.clone();	    	
	    	context = _context;
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
					
		}	
	}
}
