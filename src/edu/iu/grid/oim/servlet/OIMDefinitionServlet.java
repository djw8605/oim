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

public class OIMDefinitionServlet extends ServletBase  {
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
		
				out.write("</div>");

				out.write("<ul>");
				out.write("<li> <a href='/oim/oimdefinition#About_This_Document'> About This Document</a>");
				out.write("</li></ul> ");
				out.write("</li> <li> <a href='#Definition_of_Terms_as_used_in_t'> Definition of Terms as used in the OSG Information Management (OIM) Project</a> <ul>");
				out.write("<li> <a href='/oim/oimdefinition#OIM_Home_Page'> OIM Home Page</a>");
				out.write("</li> <li> <a href='/oim/oimdefinition#OIM_Topology_Visual_Diagram'> OIM Topology - Visual Diagram </a>");
				out.write("</li> <li> <a href='/oim/oimdefinition#Terms'> Terms</a>");
				
				out.write("</li></ul> ");
				out.write("</li></ul> ");
				out.write("</div>");
				out.write("<p />");
				out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='About_This_Document'></a> About This Document </span></h2>");
				out.write("This document defines some basic terms and concepts used in the OSG Information Management (OIM) Project.");
				out.write("<p />");
				out.write("<p />");
				out.write("<p />");
				out.write("<a name='Definition_of_Terms_as_used_in_t'></a> <h2><div class='twikinetHeader'>Definition of Terms as used in the OSG Information Management (OIM) Project </div></h2>");
				out.write("OIM defines the topology used by various OSG systems and services; it is based on the OSG Trash/Trash/Blueprint Document available at <a href='http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18' target='_top'>http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18</a>. For example, MyOSG, BDII, Gratia all use topology defined in OIM.");
				out.write("<p />");
				out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='OIM_Home_Page'></a> OIM Home Page </span></h2> <ul>");
				out.write("<li> <a href='https://oim.grid.iu.edu' target='_top'>https://oim.grid.iu.edu</a> -- Requires user to be register an X509 certificate issued by an <a href='http://software.grid.iu.edu/cadist/' target='_top'>OSG approved Certifying Authority (CA)</a> via their web browser.");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='OIM_Topology_Visual_Diagram'></a> OIM Topology - Visual Diagram </span></h2>");
				out.write("<iframe src='http://docs.google.com/present/embed?id=ddtgc5bt_113fp3fmvgp&amp;size=l' frameborder='0' width='700' height='559'></iframe>");
				out.write("<p />");
				out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Terms'></a> Terms </span></h2>");
				out.write("<p />");
				out.write("<h3><a name='Facility'></a> Facility </h3> <ul>");
				out.write("<li> The OSG Trash/Trash/Blueprint Document defines a facility as \"A collection of Sites under a single administrative domain.");
				out.write("</li> <li> OIM Uses the same definition. ");
				out.write("</li> <li> Examples: Indiana University, Fermi National Accelarator Laboratory \"");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name='Site'></a> Site </h3> <ul>");
				out.write("<li> The OSG Trash/Trash/Blueprint Document defines a site as \"A named collection of Services, Providers and Resources for administrative purposes.\""); 
				out.write("</li> <li> OIM uses the same definition (referred to as \"Administrative Sites\")");
				out.write("</li> <li> Examples: UCSD CMS T2, ATLAS SWT2 OU, STAR Brookhaven ");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name='Resource_Group'></a> Resource Group </h3> <ul>");
				out.write("<li> This term is not defined in the OSG Trash/Trash/Blueprint document");
				out.write("</li> <li> A resource group is a logical grouping of various resource-service mappings within a site. For example, the IU_Physics site in the Indiana University facility could group 4 gatekeepers and 1 SRM SE under one resource_group called IU_Physics_1");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name=\"Resource\"></a> Resource </h3> <ul>");
				out.write("<li> The OSG Trash/Trash/Blueprint Document defines a resource as \"A source of supply, support, or aid, esp. one that can be readily drawn upon when needed.\" ");
				out.write("</li> <li> In OIM a resource constitutes a grid resource provided to the OSG community. These resources will provide services in the form of Compute Elements, Storage Elements (SRM V1, SRM V2, Bestman/XrootD, or GSIFTP) , Central Infrastructure Services, or Site Level Infrastructure Services. ");
				out.write("</li> <li> Examples: BU_ATLAS_Tier2, CIT_CMS_T2, UFlorida-IHEPA ");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name=\"Service\"></a> Service </h3> <ul>");
				out.write("<li> In OIM a service runs on a resource to let grid users run jobs. Compute Elements, Storage Elements (SRM V1, SRM V2, Bestman/XrootD, or GSIFTP) , Central Infrastructure Services, or Site Level Infrastructure are examples services. ");
				out.write("</li> <li> <a href='http://myosg.grid.iu.edu/misccount/index?datasource=count&amp;count_total=on&amp;count_sg_501=on&amp;count_sg_101=on&amp;count_sg_1=on&amp;count_sg_201=on&amp;count_sg_1000=on&amp;count_sg_301=on&amp;count_sg_401=on' target='_top'>List</a> of possible OIM Services grouped by Service Group.");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<p />");
				out.write("<h3><a name='Virtual_Site'></a> Virtual Site </h3> <ul>");
				out.write("<li> The OSG Trash/Trash/Blueprint Document defines a Virtual Site as \"A set of sites that agree to use the same policies in order to act as an administrative unit. Sites and Facilities negotiate a common administrative context to form a \"virtual&rdquo; site or facility.\""); 
				out.write("</li> <li> OIM uses the same definition."); 
				out.write("</li> <li> Examples: MWT2 ");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name='Support_Center_SC'></a> Support Center (SC) </h3> <ul>");
				out.write("<li> OSG defines a Support Center as a contact or group of contacts that provide support for a resource's or virtual organization's user community. ");
				out.write("</li> <li> OIM uses the same definition. ");
				out.write("</li> <li> Examples: USCMS Support Center, Community Support Center ");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name='Virtual_Organization_VO'></a> Virtual Organization (VO) </h3> <ul>");
				out.write("<li> The OSG Trash/Trash/Blueprint Document defines a Virtual Organization as \"A dynamic collection of Users, Resources and Services for sharing of Resources (Globus definition). A VO is party to contracts between Resource Providers &amp; VOs which govern resource usage &amp; policies. A subVO is a sub-set of the Users and Services within a VO which operates under the contracts of the parent.\""); 
				out.write("</li> <li> OIM uses the same definition."); 
				out.write("</li> <li> Examples: USATLAS, SBGrid, LIGO ");
				out.write("</li></ul> ");
				out.write("<p />");
				out.write("<h3><a name='Contact'></a> Contact </h3> <ul>");
				out.write("<li> OIM defines contact as an individual or group responsible for addressing queries regarding OSG resources or virtual organizations or support centers. Contact types are explained below - most contact types allow one primary and one secondary contact for that type, and some allow multiple tertiary contacts.");
				out.write("</li> <li> <strong>Support Center Contacts</strong>  <ul>");
				out.write("<li> Operations contact (Ticketing) - <a href='http://ticket.grid.iu.edu/' target='_top'>GOC tickets</a> when assigned to a support center are sent to the primary Operations contact's email address for that SC.");
				out.write("</li> <li> Notifications contact - GOC notifications (<a href='http://osggoc.blogspot.com/' target='_top'>also available via RSS</a>) are sent to both primary and secondary notification contacts for an SC");
				out.write("</li> <li> Security contact - Security notifications sent out by the OSG security team are sent to primary and secondary security contacts for a SC if the notification is relevant to resources or VOs that SC may support.");
				out.write("</li> <li> Miscellaneous contact - Contacts who do not fall under any of the above types but would like to be able to edit a support center can be added as miscellaneous contact");
				out.write("</li></ul> ");
				out.write("</li> <li> <strong>Resource Contacts</strong>  <ul>");
				out.write("<li> Administrative contact - <a href='http://ticket.grid.iu.edu/' target='_top'>GOC tickets</a> when assigned to a support center of a resource are also sent to the primary resource admin contact (usually the sysadmin of a resource) via the ticket's CC mechanism. Admin contacts are also contacted by GOC staff and others to deal with system-administrative problems.");
				out.write("</li> <li> Reporting contact - Gratia-accounting based resource-usage reports are sent out daily/weekly to one or more contacts listed as report contacts for a resource.");
				out.write("</li> <li> Security contact - Security notifications sent out by the OSG security team are sent to primary and secondary security contacts for a resource if the notification is relevant to that or all resource(s).");
				out.write("</li> <li> Miscellaneous contact - Contacts who do not fall under any of the above types but would like to be able to edit a resource can be added as miscellaneous contact");
				out.write("</li></ul>"); 
				out.write("</li> <li> <strong>VO Contacts</strong>  <ul>");
				out.write("<li> Manager contact - VO Managers usually make decisions on what VOs are allowed to run on their VO-owned resources, who are users of a VO, etc.");
				out.write("</li> <li> Administrative contact -- A primary contact for ticketing and assorted issues. This is typically a user/application support person or a help desk.");
				out.write("</li> <li> Security contact - Security notifications sent out by the OSG security team are sent to primary and secondary security contacts for a VO em>OSG-GOC SC</em> and {Admin, Security} contact for the <em>IUPUI-ITB</em> resource.");
				out.write("</li></ul> ");
				out.write("</li> <li> Example: Gratia Ops Group is Operations Contact for <em>Gratia</em> SC; Rob Quick is Operations Contact for the <em>OSG-GOC SC</em> and {Admin, Security} contact for the <em>IUPUI-ITB</em> resource.</li></ul> </div>");
				
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
