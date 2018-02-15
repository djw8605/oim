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

import edu.iu.grid.oim.model.db.SSOModel;
import edu.iu.grid.oim.model.db.record.SSORecord;

import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.SideContentView;

public class HomeServlet extends ServletBase  {
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
			if(auth.isUser()) {
			

				try {
				    SSORecord ssouser = auth.getSSORecord();
				    SSOVerification verf = new SSOVerification(ssouser.id, context);
				    verf.render(out);
                                } catch (SQLException e) {
				    log.error(e);
                                }

				try {
				    ContactRecord user = auth.getContact();
				    Confirmation conf = new Confirmation(user.id, context);
				    conf.render(out);
				} catch (SQLException e) {
				    log.error(e);
                                }


				//show entities that this user is associated
				try {
					ContactAssociationView caview = new ContactAssociationView(context, auth.getContact().id);
					caview.showNewButtons(true);
					caview.render(out);
				} catch (SQLException e) {
					log.error(e);
				}
			} else {
				//guest view
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4 hotlink\" onclick=\"document.location='topology';\">");
				out.write("<h2>Topology</h2>");
				out.write("<p>Defines resource hierarchy</p>");
				out.write("<img src=\"images/topology.png\">");
				out.write("</div>");
				out.write("<div class=\"span4 hotlink\" onclick=\"document.location='vo';\">");
				out.write("<h2>Virtual Organization</h2>");
				out.write("<p>Defines access for group of users</p>");
				out.write("<img src=\"images/voicon.png\">");
				out.write("</div>");
				out.write("<div class=\"span4 hotlink\" onclick=\"document.location='sc';\">");
				out.write("<h2>Support Centers</h2>");
				out.write("<p>Defines who supports virtual organization</p>");
				out.write("<img src=\"images/scicon.png\">");
				out.write("</div>");
				out.write("</div>");
			}
			//out.write("</div>");
		}
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView contentview = new SideContentView();
		Authorization auth = context.getAuthorization();
		
		if(auth.isUnregistered()) {
			contentview.add(new HtmlView("<div class=\"alert alert-info\"><p>Your certificate is not yet registered with OIM.</p><p><a class=\"btn btn-info\" href=\"register\">Register</a></p></div>"));
		} else if(auth.isDisabled()) {
			contentview.add(new HtmlView("<div class=\"alert alert-danger\"><p>Your contact or DN is disabled. Please contact GOC for more information.</p><a class=\"btn btn-danger\" href=\"https://ticket.opensciencegrid.org\">Contact GOC</a></p></div>"));
		} else if(!auth.isUser()) {
		    String text = "<p><a class=\"btn btn-info\" href=\"/oim/sso/\">Login</a></p>";	
		
		    contentview.add(new HtmlView("<div class=\"alert alert-info\"><p>"+text+"</p></div>"));
		}
		
		contentview.add(new HtmlView("<h2>Documentations</h2>"));
	
		contentview.add(new LinkView("/oim/oimdefinition", "OIM Definitions", true));
		contentview.add(new LinkView("/oim/oimregistration", "Registration", true));
		contentview.add(new LinkView("/oim/oimmaint", "Resource Downtime", true));

		if(auth.isUser()) {
			contentview.addContactLegend();
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
			if(crec.isConfirmationExpired()) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<h2>Content Confirmation</h2>");
			
				out.write("<p class=\"divrep_round divrep_elementerror\">You have not recently confirmed that your information in OIM is current</p>");
		
				out.write("<p>The last time you confirmed your profile information was "+crec.confirmed.toString()+"</p>");
				out.write("<p>Please go to the ");
				out.write("<a href=\"profileedit\">My Profile</a>");
				out.write(" page to check your profile information</p>");
				out.write("</div>");
			}
		}	
	}


    @SuppressWarnings("serial")
        class SSOVerification extends DivRep
        {
	    final SSORecord crec;
	    final SSOModel cmodel;
	    final UserContext context;

	    public SSOVerification(Integer sso_id, UserContext _context) throws SQLException {
		super(_context.getPageRoot());

                cmodel = new SSOModel(_context);
                crec = (SSORecord) cmodel.getBySSOID(sso_id);//.clone();                                                                                                              
                context = _context;
	    }

	    protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub                                                                                                                             

	    }

	    public void render(PrintWriter out) {
		System.out.println("verified: "+crec.verified);

		Integer disabled_h = crec.disabled;
                Integer declined_h = crec.declined;
                Integer verified_h = crec.verified;

		if(disabled_h.equals(0)) {

		    if(declined_h.equals(1)){
			out.write("<p class=\"divrep_round divrep_elementerror\">Your account verification request has been declined. Please contact help@opensciencegrid.org with any further questions.</p>");
		    }else if(verified_h.equals(0)) {

			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<h2>Account Verification</h2>");
			
			out.write("<p class=\"divrep_round divrep_elementerror\">Your account has to be verified before you can add or edit any information in OIM</p>");
						
			out.write("<p>Please go to the ");
			out.write("<a href=\"requestuserssoverify\">Verify Account here</a>");
			out.write(" page to submit a request</p>");
			out.write("</div>");
			
		    }

		}else{
		    out.write("<p class=\"divrep_round divrep_elementerror\">Your account has been disabled. Please contact help@opensciencegrid.org with any further questions.</p>");
		    
		}
	    }
		
	}
}
