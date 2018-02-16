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

public class OIMWebInterfaceServlet extends ServletBase  {
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
			out.write("<h1><a name=\"Getting_a_User_Certificate_via_W\"></a>  Getting a User Certificate via Web interface </h1>");
			out.write("<p />");
			out.write("<div class=\"twikiToc\"> <ul>");
			out.write("<li> <a href=\"/oim/oimwebinterface#About_This_Document\"> About This Document</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Requirements\"> Requirements</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Setting_the_Master_Password_Opti\"> Setting the Master Password (Optional)</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Downloading_Certifying_Authority\"> Downloading Certifying Authority certificate files for OSG CILogon-based certificates</a> <ul>");
			out.write("<li> <a href=\"/oim/oimwebinterface#Installing_Certifying_Authority\"> Installing Certifying Authority certificates into a web browser</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Requesting_a_New_User_Certificat\"> Requesting a New User Certificate</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#VO_Registration\"> VO Registration</a> <ul>");
			out.write("<li> <a href=\"/oim/oimwebinterface#Registration_Using_a_VOMS_Admin\"> Registration Using a VOMS-Admin Server</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Exporting_the_Certificate_to_Dis\"> Exporting the Certificate to Disk</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Transferring_the_Certificate_to\"> Transferring the Certificate to the Submit Host</a>");
			out.write("</li> <li> <a href=\"/oim/oimwebinterface#Next_Steps\"> Next Steps</a>");
			out.write("</li></ul> ");
			out.write("</div>");
			out.write("<p />");
			out.write("Please send feedback on the documentation or ask questions about the process to the <a href=\"mailto&#58;goc&#64;opensciencegrid&#46;org\">Grid Operations Center</a>.");
			out.write("<p />");
			out.write("<h1><a name=\"About_This_Document\"></a> About This Document </h1>");
			out.write("<p />");
			out.write("This document contains some general instructions on how a scientist can obtain an electronic credentials that allow");
			out.write("the use of the <a href=\"/bin/view/Documentation/WhatIsOSG\" class=\"twikiLink\">Open Science Grid</a> (OSG). Getting these credentials");
			out.write("is part of the process of <a href=\"/bin/view/Documentation/UsingTheGrid\" class=\"twikiLink\">becoming a new grid user</a>.");
			out.write("The steps are: <ol>");
			out.write("<li> Getting what's called a <a href=\"/bin/view/Documentation/CertificateWhatIs\" class=\"twikiLink\">certificate</a> as described below in this document. ");
			out.write("</li> <li> Registering this certificate with a type of organization known as a <a href=\"https://twiki.grid.iu.edu/bin/view/Documentation/WhatIsOSG#Virtual_Organizations\" target=\"_top\">VO</a> that is authorized to use computers on the grid. This is explained <a href=\"/bin/view/Documentation/CertificateGetWeb#VO_Registration\" class=\"twikiCurrentTopicLink twikiAnchorLink\">further down</a> in this document.");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("These instructions are mainly for users who do not have credentials.");
			out.write("If you already have a certificate, you probably want to");
			out.write("instead <a href=\"/bin/view/Documentation/MiscellaneousCertificateTasks\" class=\"twikiLink\">renew or replace it</a>.");
			out.write("<p />");
			out.write("<p />");
			out.write("<h1><a name=\"Requirements\"></a> Requirements </h1>");
			out.write("<p />");
			out.write("You first need to know the VO you should join. If you're not sure,");
			out.write("<a href=\"https://my.opensciencegrid.org/vosummary?all_vos=on&amp;active=on&amp;active_value=1&amp;datasource=summary\" target=\"_top\">this list</a> may help, or please email the <a href=\"mailto&#58;goc&#64;opensciencegrid&#46;org\">Grid Operations Center</a>.");
			out.write("<p />");
			out.write("From your VO you should find out <ol>");
			out.write("<li> Whether to use these instructions or some      VO specific ones instead;");
			out.write("</li> <li> Who to use as your sponsor for the certificate;");
			out.write("</li> <li> The URL for applying for VO membership, the      sponsor to use, and what group, if any, to request; and");
			out.write("</li> <li> What machine you can use to submit jobs to      OSG. The VO may need to make you an account on that machine.");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("These instructions were written for Firefox although");
			out.write("the basic steps are similar for other browsers. ");
			out.write("<p />");
			out.write("<h1><a name=\"Setting_the_Master_Password_Opti\"></a> Setting the Master Password (Optional) </h1>");
			out.write("<p />");
			out.write("To help protect the certificate, it's best to set");
			out.write("Firefox's <a href=\"http://kb.mozillazine.org/Master_password\" target=\"_top\">master password</a>. First, go to the Security");
			out.write("submenu under Options/Preferences, which <a href=\"http://kb.mozillazine.org/Menu_differences_in_Windows,_Linux,_and_Mac\" target=\"_top\">is reachable under</a> ");
			out.write("<strong>Tools -&gt; Options</strong> for Windows, ");
			out.write("<strong>Firefox -&gt; Preferences</strong> for Macs, and ");
			out.write("<strong>Edit -&gt; Preferences</strong> for Linux. ");
			out.write("Then click on the \"Use a master password\" button");
			out.write("and set a password when prompted.");
			out.write("<span class=\"twistyPlugin twikiMakeVisibleInline\">  <span id=\"twistyIdDocumentationCertificateGetWeb1show\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleopen-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Screenshot</span></a> </span> <span id=\"twistyIdDocumentationCertificateGetWeb1hide\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleclose-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Hide</span></a> </span>  </span><!--/twistyPlugin twikiMakeVisibleInline--> <span class=\"twistyPlugin\"><span id=\"twistyIdDocumentationCertificateGetWeb1toggle\" class=\"twistyContent twikiMakeHidden twistyInited\">");
			out.write("<br />    ");
			out.write("<img alt=\"set_master_password1.png\" src=\"/oim/images/set_master_password1.png\" />");
			out.write("</span></span> <!--/twistyPlugin-->");
			out.write("The browser will later ask you for this password");
			out.write("when you use your certificate.");
			out.write("<p />");
			out.write("There is more information about protecting grid");
			out.write("credentials <a href=\"/bin/view/Documentation/SecurityUserResponsibilities\" class=\"twikiLink\">here</a>.");
			out.write("<p />");
			out.write("<a name=\"DownloadingCaCert\"></a>");
			out.write("<p />");
			out.write("<p />");
			out.write("<h1><a name=\"Downloading_Certifying_Authority\"></a> Downloading Certifying Authority certificate files for OSG CILogon-based certificates </h1>");
			out.write("<a href=\"http://ca.cilogon.org/downloads\" target=\"_top\">Here</a> you will find the location from which you can download the CILogon OSG CA root cert that is needed to be added to the OS X Keychain (for example) or to the trusted issuer stores in the various browsers.");
			out.write("<p />");
			out.write("The following is a sample of the page.  ");
			out.write("<p />");
			out.write("       <img alt=\"CA_web.png\" src=\"/oim/images/CA_web.png\" />  <br />    ");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Installing_Certifying_Authority\"></a> Installing Certifying Authority certificates into a web browser </span></h2>");
			out.write("<p />");
			out.write("To import the CA certificates into your browser, download the <a href=\"https://cilogon.org/cilogon-osg.pem\" target=\"_top\">cilogon-osg.pem</a> file and then follow these instructions.  These instructions are for Firefox but most browsers have something similar.");
			out.write("<p /> <ol>");
			out.write("<li> Go to Preferences -&gt; Advanced -&gt; Certificates -&gt; View Certificates -&gt; Authorities:       <p> <img alt=\"CA_absent.png\" src=\"/oim/images/CA_absent.png\" /></p>");
			out.write("</li> <li> Click \"Import\" and select the certificate just downloaded.       <p> <img alt=\"CA_import.png\" src=\"/oim/images/CA_import.png\" />  </p>");
			out.write("</li> <li> After you select the certificate, this window will be shown      <p> <img alt=\"CA_trust.png\" src=\"/oim/images/CA_trust.png\" />  </p>      Click on the first two \"Trust\" boxes and click \"OK\" to save it.");
			out.write("</li> <li> Successful import will show the new CA as follows      <p> <img alt=\"CA_present.png\" src=\"/oim/images/CA_present.png\" />  </p>");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("<p />");
			out.write("<p />");
			out.write("<h1><a name=\"Requesting_a_New_User_Certificat\"></a> Requesting a New User Certificate </h1>");
			out.write("<p /> <ol>");
			out.write("<li> Start your browser and go to     <a href=\"https://oim.opensciencegrid.org/oim/certificaterequestuser\" target=\"_top\">https://oim.opensciencegrid.org/oim/certificaterequestuser</a>.");
			out.write("</li> <li> Select the appropriate VO from the drop down menu.       <p> <img alt=\"select_vo.png\" src=\"/oim/images/select_vo.png\" /> </p>");
			out.write("</li> <li> After having read through the OSG Policy Agreement, check the \"I AGREE\" box and click on Submit.      <p> <img alt=\"/oim/images/CA_agreement.png\" />  </p>");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("<h1><a name=\"VO_Registration\"></a> VO Registration </h1>");
			out.write("<p />");
			out.write("In most cases, you will have to separately apply for VO membership. For that, you will likely use <a href=\"/bin/view/Documentation/GlossaryOfTerms#DefsVomsAdmin\" class=\"twikiAnchorLink\">VOMS-Admin server</a>. It requires that you have your certificate before applying.");
			out.write("<p />");
			out.write("Find out the URL of your VOMS-Admin");
			out.write("server either from your VO administrator, or the <a href=\"https://my.opensciencegrid.org/vosummary?all_vos=on&amp;active=on&amp;active_value=1&amp;datasource=summary\" target=\"_top\">list at MyOSG</a>.");
			out.write("<p />");
			out.write("<h2 class=\"twikinetRoundedAttachments\"><span class=\"twikinetHeader\"><a name=\"Registration_Using_a_VOMS_Admin\"></a> Registration Using a VOMS-Admin Server </span></h2>");
			out.write("<p /> <ol>");
			out.write("<li> Go to the voms-admin URL for your VO. Make sure      that the DN listed is the one for the certificate      that you just got, then fill out the form and      click \"register\". If you should belong to a particular group,      either send an email to the VO administrator or, if there is a      comments box on the page, mention that there.     <span class=\"twistyPlugin twikiMakeVisibleInline\">  <span id=\"twistyIdDocumentationCertificateGetWeb2show\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleopen-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Screenshot</span></a> </span> <span id=\"twistyIdDocumentationCertificateGetWeb2hide\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleclose-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Hide</span></a> </span>  </span><!--/twistyPlugin twikiMakeVisibleInline--> <span class=\"twistyPlugin\"><span id=\"twistyIdDocumentationCertificateGetWeb2toggle\" class=\"twistyContent twikiMakeHidden twistyInited\">     <br />         <img alt=\"vomsadmin_initialB.png\" src=\"/oim/imagesvomsadmin_initialB.png\" />     </span></span> <!--/twistyPlugin-->");
			out.write("</li> <li> Wait for the email from the VOMS-Admin server, and      click on the URL in it to complete the request.     <span class=\"twistyPlugin twikiMakeVisibleInline\">  <span id=\"twistyIdDocumentationCertificateGetWeb3show\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleopen-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Screenshot</span></a> </span> <span id=\"twistyIdDocumentationCertificateGetWeb3hide\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleclose-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Hide</span></a> </span>  </span><!--/twistyPlugin twikiMakeVisibleInline--> <span class=\"twistyPlugin\"><span id=\"twistyIdDocumentationCertificateGetWeb3toggle\" class=\"twistyContent twikiMakeHidden twistyInited\">     <br />         <img alt=\"vomsadmin_request_confirmedB.png\" src=\"/oim/images/vomsadmin_request_confirmedB.png\" />     </span></span> <!--/twistyPlugin-->");
			out.write("</li> <li> Wait for an email triggered by a human, the VO      administrator, saying that you are approved.");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("  <span class=\"twistyPlugin twikiMakeVisibleInline\">  <span id=\"twistyIdDocumentationCertificateGetWeb4show\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleopen-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Verification</span></a> </span> <span id=\"twistyIdDocumentationCertificateGetWeb4hide\" class=\"twistyTrigger twikiUnvisited twistyHidden twistyInited\"><a href=\"#\"><img src=\"/twiki/pub/TWiki/TWikiDocGraphics/toggleclose-small.gif\" border=\"0\" alt=\"\" /><span class=\"twikiLinkLabel twikiUnvisited\">Hide</span></a> </span>  </span><!--/twistyPlugin twikiMakeVisibleInline--> <span class=\"twistyPlugin\"><span id=\"twistyIdDocumentationCertificateGetWeb4toggle\" class=\"twistyContent twikiMakeHidden twistyInited\">");
			out.write("     <br />    ");
			out.write("  To confirm this step, visit the");
			out.write("VOMS-Admin server again and check that you get a");
			out.write("page like this:<br />");    
			out.write("<img alt=\"vomsadmin_doneB.png\" src=\"/oim/images/vomsadmin_doneB.png\" />");
			out.write("</span></span> <!--/twistyPlugin-->");
			out.write("<p />");
			out.write("<h1><a name=\"Exporting_the_Certificate_to_Dis\"></a> Exporting the Certificate to Disk </h1>");
			out.write("<p />");
			out.write("To export your certificate,");
			out.write("<p /> <ol>");
			out.write("<li> Open the certificate manager:     <a href=\"http://kb.mozillazine.org/Menu_differences_in_Windows,_Linux,_and_Mac\" target=\"_top\">Options/Preferences</a> -&gt; Advanced -&gt; Certificates -&gt; View Certificates.   Select the certificate that you would like to export, and press   \"Backup\".<br />       <img alt=\"user_cert.png\" src=\"/oim/images/user_cert.png\" />");
			out.write("</li> <li> When prompted, type in the name of the file to hold   the new certificate. If you name it \"usercred.p12\"   then grid programs can automatically recognize it.");
			out.write("</li> <li> When prompted, set a password for the   certificate. You'll need this to be able to use the   certificate later.");
			out.write("</li> <li> After pressing OK, you should see:<br />       <img alt=\"cert_export_acknowledgement.png\" src=\"/oim/images/cert_export_acknowledgement.png\" />");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("<h1><a name=\"Transferring_the_Certificate_to\"></a> Transferring the Certificate to the Submit Host </h1>");
			out.write("<p />");
			out.write("You next need to transfer your certificate to the");
			out.write("machine from which you'll be submitting jobs, the");
			out.write("<em>submit host</em>. ");
			out.write("<p /> <ol>");
			out.write("<li> Find out from your VO the name of a submit host.");
			out.write("</li> <li> One way to do the transfer is with the /scp/ program.   <pre class=\"screen\">");
			out.write("  $ scp -p usercred.p12 <font color=\"#ff0000\">YOUR_USERNAME@SUBMIT_HOSTNAME:</font>");
			out.write("   usercred.p12                                   100% 5084     5.0KB/s   00:00</pre>   Here you should replace YOUR_USERNAME with your user   id on the submit host, and SUBMIT_HOSTNAME with the   name of the submit host. The \"@\" and \":\" symbols are   important.     Another way to do the transfer is with <a href=\"http://www.thegeekstuff.com/2011/06/windows-sftp-scp-clients/\" target=\"_top\">a GUI scp/sftp client</a>.");
			out.write("</li> <li> Log into the remote host:   <pre class=\"screen\">");
			out.write("   $ ssh <font color=\"#ff0000\">YOUR_USERNAME@SUBMIT_HOSTNAME </font></pre>");
			out.write("</li> <li> Make a directory called \".globus\", and move the   certificate into that directory:   <pre class=\"screen\">");
			out.write("   $ mkdir .globus ");
			out.write("   $ mv usercred.p12 .globus/</pre> ");
			out.write("</li> <li> Generate .pem files needed by globus   <pre class=\"screen\">");
			out.write("  openssl pkcs12 -in usercred.p12 -clcerts -nokeys -out $HOME/.globus/usercert.pem");
			out.write("  openssl pkcs12 -in usercred.p12 -nocerts -out $HOME/.globus/userkey.pem</pre>");
			out.write("</li> <li> Make sure that the permissions are correct:   <pre class=\"screen\">");
			out.write("   $ chmod 400 usercred.p12</pre>");
			out.write("</li></ol> ");
			out.write("<p />");
			out.write("<p />");
			out.write("<h1><a name=\"Next_Steps\"></a> Next Steps </h1>");
			out.write("<p />");
			out.write("The next steps are to <a href=\"/bin/view/Documentation/UserTestingResource\" class=\"twikiLink\">run a test job</a>,");
			out.write("and then <a href=\"/bin/view/Documentation/UsingTheGrid#Running_Jobs\" class=\"twikiAnchorLink\">real jobs</a>.");
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
