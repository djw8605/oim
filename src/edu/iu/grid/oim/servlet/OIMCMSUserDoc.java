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

public class OIMCMSUserDoc extends ServletBase  {
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
			out.write("<h1><a name=\"OSG_PKI_Transition_Impact_on_End\"></a> OSG PKI Transition Impact on End Users </h1>");
			out.write("<p />");
			out.write("<div class=\"twikiToc\"> <ul>");
			out.write("<li> <a href=\"#OSG_PKI_Transition_Impact_on_End\"> OSG PKI Transition Impact on End Users</a> <ul>");
			out.write("<li> <a href=\"#Who_is_impacted\"> Who is impacted?</a>");
			out.write("</li> <li> <a href=\"#What_is_the_impact\"> What is the impact?</a>");
			out.write("</li> <li> <a href=\"#What_are_the_next_steps\"> What are the next steps?</a>");
			out.write("</li> <li> <a href=\"#How_to_Register_Your_New_Certifi\"> How to Register Your New Certificate with CMS Services</a>");
			out.write("</li></ul> ");
			out.write("</li></ul> ");
			out.write("</div>");
			out.write("<p />");
			out.write("<h2><a name=\"Who_is_impacted\"></a> <a name=\"Who_is_impacted\"></a>Who is impacted? </h2>");
			out.write("<p />");
			out.write("Any user who has a personal certificate from DOEGrids CA");
			out.write("<h2><a name=\"What_is_the_impact\"></a> <a name=\"What_is_the_impact\"></a>What is the impact? </h2>");
			out.write("<p />");
			out.write("After March 23 2013, DOEGrids CA will stop issuing or renewing certificates. Users should follow the steps outlined below to obtain certificates.");
			out.write("<h2><a name=\"What_are_the_next_steps\"></a> <a name=\"What_are_the_next_steps\"></a>What are the next steps? </h2> <ol>");
			out.write("<li> If a user is entitled to get certificates from the CERN CA, we strongly recommend that the user should apply to CERN CA for a certificate. If you are a member of CMS, you should be able to get CERN CA certificates easily. We believe that obtaining CERN CA certificates will be easier than switching to OSG CA since most of the authentication will have already been done when you became a member of CMS. If the user has a CERN account, then s/he can obtain a certificate from the CERN CA automatically within a few minutes. The steps for obtaining certificates from the CERN CA are explained at <a href=\"/twiki/bin/view/CMSPublic/PersonalCertificate\" class=\"twikiLink\">PersonalCertificate</a>.");
			out.write("</li> <li> If the user is not a member of CMS and has no account at CERN, and/or is unable to obtain a certificate from the CERN CA, then they should apply to OSG CA for a certificate. The instructions on how to do so are available <a target=\"_top\" href=\"https://www.opensciencegrid.org/bin/view/Security/PKIDocumentationIndex#Documentation_for_End_Users\">here</a>");
			out.write("</li> <li> After obtaining the certificate, import it into your browser and the file system. This step is not any different than what we used to do with DOEGrids CA.");
			out.write("</li> <li> Register the new DN with CMS VOMS, and all other services that require certificate authentication. Your new certificate has a new Distinguished Name (DN). You must register this DN with all service providers, such as VOMS, that allow access based on users' certificate DN names. Below is a list of CMS services that require this. If you need access to these services, you should follow the steps below and register your certificate with the services.");
			out.write("</li> <li> Test access with any of the services that need certificate authentication.");
			out.write("</li></ol> ");
			out.write("<h2><a name=\"How_to_Register_Your_New_Certifi\"></a> How to Register Your New Certificate with CMS Services </h2>");
			out.write("<p />");
			out.write("You must register your new certificate with these services to continue accessing them. Because these services authorize users based on their certificates, if you do not register your new certificate, the services will not recognize you and will not provide access to you.");
			out.write("<p /> <ul>");
			out.write("<li> VOMS: Please use a modern browser version, Firefox (Mozilla) is recommended. <ul>");
			out.write("<li> If you have already registered a personal certificate (e.g., from the DOE CA) with the CMS VO in the past, and the DN from that certificate has not yet expired, then please follow these instructions for how to register an additional new personal certificate (e.g., from the CERN CA) with the CMS VO.<a  href=\"https://twiki.cern.ch/twiki/bin/view/CMSPublic/VoRegForExistingMember\">https://twiki.cern.ch/twiki/bin/view/CMSPublic/VoRegForExistingMember</a> (If you renew certificates, then your DN stays the same.  If your certificate has expired, then a new certificate will have a new DN.)");
			out.write("</li> <li> If you have never registered a personal certificate with the CMS VO in the past (or don't know), or the DN from the certificate which you have registered with the CMS VO has expired (or don't know), then please follow these instructions for how to register your new personal certificate (e.g., from the CERN CA) with the CMS VO.<a  href=\"https://twiki.cern.ch/twiki/bin/view/CMSPublic/CERNcert2VO4newVOMSuser\">https://twiki.cern.ch/twiki/bin/view/CMSPublic/CERNcert2VO4newVOMSuser</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> CERN Single Sign On (SSO) allows access to Twiki and Indico. Go to <a target=\"_top\" href=\"https://ca.cern.ch/ca/\">https://ca.cern.ch/ca/</a>. On the left side, under User Certificates, select \"Map a non-CERN certificate to your account\". Follow the directions on the CERN web page.");
			out.write("</li> <li> REBUS.");
			out.write("</li> <li>  <a  href=\"https://twiki.cern.ch/twiki/bin/view/CMSPublic/GgusReg\">GGUS</a>");
			out.write("</li> <li> OIM, <a rel=\"nofollow\" href=\"https://twiki.cern.ch/twiki/bin/edit/CMS/MyOSG?topicparent=CMS.EndUsers;nowysiwyg=0\" title=\"this topic does not yet exist; you can create tit\">MyOSG</a>, OSG Ticketing system,");
			out.write("</li> <li> Crab/siteDB: see <a href=\"https://twiki.cern.ch/twiki/bin/view/CMS/SiteDBForCRAB#Adding_your_DN_to_your_profile\" target=\"_top\">https://twiki.cern.ch/twiki/bin/view/CMS/SiteDBForCRAB#Adding_your_DN_to_your_profile</a>");
			out.write("</li> <li> site local storage (grant write permission on the local site storage to the new DN of the person running local <a href=\"/twiki/bin/view/CMS/PhEDEx\" class=\"twikiLink\">PhEDEx</a> download agents for that site).");
					
			
		
		
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
