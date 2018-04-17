package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.SSOModel;

import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;

import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.SiteFormDE;
import edu.iu.grid.oim.view.divrep.form.SSOUserFormDE;
import javax.servlet.http.HttpSession;

public class SSOUserEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(UserEditServlet.class);  
	private String current_page = "ssouser";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		String ids = request.getParameter("id");
		System.out.println("here is get ID" + ids);
		String title;

		SSORecord rec;
		HttpSession session = request.getSession(false);
		String user_access = (String)session.getAttribute("user_access");
             
		try {
			//if site_id is provided then we are doing update, otherwise do new.
			//String dn_id_str = request.getParameter("email");
			if(user_access != null) {
				//pull record to update
			    String ssoemail = user_access;
			    SSOModel ssomodel = new SSOModel(context);
			    rec = ssomodel.getByEmail(ssoemail);
			    title = "Update User";
			} else {
			    rec = new SSORecord();
			    title = "New User";	
			}
		
			ContentView contentview = new ContentView(context);
			
			if(rec != null) {
			    SSOUserFormDE form = new SSOUserFormDE(context, rec, ids, current_page);
				
				//put the form in a view and display
				contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
				contentview.add(new DivRepWrapper(form));
				
				//setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Administration",  "admin");
				bread_crumb.addCrumb("Users",  "user");
				bread_crumb.addCrumb(rec.dn_string, null);
				contentview.setBreadCrumb(bread_crumb);
			} else {
				//no user ID found..
				contentview.add(new HtmlView("<h1>Can't find User ID</h1>"));
				contentview.add(new HtmlView("<p>Perhaps the user has been removed?</p>"));
			}
	
			BootPage page = new BootPage(context, new BootMenuView(context, "admin"), contentview, createSideView());			
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		return null;
	}
}