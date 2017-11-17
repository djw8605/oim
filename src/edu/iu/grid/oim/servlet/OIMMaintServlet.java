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
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.SideContentView;

public class OIMMaintServlet extends ServletBase  {
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
		header.add(new HtmlView("<h1>OSG Information Management System</h1>"));
		header.add(new HtmlView("<p class=\"lead\">Defines the topology used by various OSG services based on the <a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18\">OSG Blueprint Document</a></p>"));

		page.setPageHeader(header);
		
		//HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("maint.html"));
		//fileview.render(out);


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
			//HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("maint.html"));
			//fileview.render(out);
			//out.write("");
			out.write("<ul>");
			out.write("<li> <a href='/oim/oimmaint#Information_About_the_Resource_M'> Information About the Resource Maintenance Tool in OIM</a> <ul>");
			out.write("<li> <a href='/oim/oimmaint#About_This_Document'> About This Document</a></li>");
			out.write("<li> <a href='/oim/oimmaint#Introduction_to_the_OIM_Schedule'> Introduction to the OIM Scheduled Maintenance Tool</a></li>");
			out.write("<li> <a href='/oim/oimmaint#Adding_a_New_Maintenance_Window'> Adding a New Maintenance Window</a></li>");
			out.write("<li> <a href='/oim/oimmaint#Deleting_a_Maintenance_Window'> Deleting a Maintenance Window</a></li>");
			out.write("<li> <a href='/oim/oimmaint#Modifying_a_Maintenance_Window'> Modifying a Maintenance Window</a></li>");
			out.write("<li> <a href='/oim/oimmaint#References'> References</a> <ul>");
			out.write("</div>");
			out.write("<a name='About_This_Document'></a><h2><div class='twikinetHeader'> About This Document </div></h2>");
			out.write(" This document is a brief discussion of the Resource Maintenance Tool in OIM");
			out.write("<p />");
			out.write("<a name='Introduction_to_the_OIM_Schedule'></a> Introduction to the OIM Scheduled Maintenance Tool");
			out.write("<p />");
			out.write("The <a href='https://oim.grid.iu.edu' target='_top'>OSG Information Management (OIM)</a> project has implemented a scheduled maintenance tool. It is important for OSG Users and Operations to have ");
			out.write("accurate records for scheduled resource maintenance times. This will help users schedule jobs and operations to accurately calculate reports and not penalize resources for routine scheduled maintenance. ");
			out.write("<p />");
			out.write("You will only be able to schedule maintenance for registered resources if you are listed as an administrator of that resource in <a "); out.write("href='https://twiki.grid.iu.edu/bin/view/Documentation/GlossaryO#DefsOpenScienceGridInformationManagement' target='_top'>OIM</a>. ");
			out.write("<p />");
			out.write("NOTE: To access the OIM system you will need to have a valid OSG approved  certificate loaded into your browser. ");
			out.write("<p />");
			out.write("<h2><a name='Adding_a_New_Maintenance_Window'></a><div class='twikinetHeader'> Adding a New Maintenance Window</div> </span></h2>");
			out.write("<p />");
			out.write("To access the maintenance records for your resource follow the OIM menu. <strong>Registrations -&gt; Resources -&gt; Your Resources -&gt; <em>Resource Name</em> -&gt; Maintenance</strong>. This will show you all current maintenance records for the <em>Resource Name</em> you have selected. ");
			out.write("<p />");
			out.write("To add a new maintenance window click on the <strong>Add Maintenance</strong> link. ");
			out.write("<p />");
			out.write("From the page you will choose the service that will be out (ie. Compute Element, SRM V1 Storage Element...), the Severity based on the outage time you expect (Outage, Intermittent Outage, No Outage Expected), the Start and End time of the maintenance in UTC/GMT, and a description of the maintenance. ");
			out.write("<p />");
			out.write("When this information is entered click the <strong>Add Maintenance</strong> link to submit the window. ");
			out.write("<p />");
			out.write("Your maintenance record should now show in the <strong>Registrations -&gt; Resources -&gt; Your Resources -&gt; <em>Resource Name</em> -&gt; Maintenance</strong> menu. ");
			out.write("<p />");
			out.write("<h2><a name='Deleting_a_Maintenance_Window'></a> <div class='twikinetHeader'> Deleting a Maintenance Window </div></h2>");
			out.write("<p />");
			out.write("Click <strong>Cancel Maintenance</strong> on the maintenance window you would like to delete from the <strong>Registrations -&gt; Resources -&gt; Your Resources -&gt; <em>Resource Name</em> -&gt;");
			out.write("Maintenance</strong> menu. ");
			out.write("<p />");
			out.write("Confirm you would like to cancel this maintenance by clicking <strong>Yes</strong>. ");
			out.write("<p />");
			out.write("<h2><a name='Modifying_a_Maintenance_Window'></a> <div class='twikinetHeader'> Modifying a Maintenance Window <div></h2>");
			out.write("<p />");
			out.write("Click the <strong>Edit</strong> link on the maintenance window you would like to modify from the <strong>Registrations -&gt; Resources -&gt; Your Resources -&gt; <em>Resource Name</em> -&gt; ");
			out.write("Maintenance</strong> menu. ");
			out.write("<p />");
			out.write("Update any fields you would like to modify and click <strong>Update Maintenance</strong> when you have made modifications. ");
			out.write("<p />");
				  
		
		}
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView contentview = new SideContentView();
		Authorization auth = context.getAuthorization();
		
		

		//if(auth.isUser()) {
		//	contentview.addContactLegend();
		///	}
		
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
