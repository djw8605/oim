package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.form.RequestUserSSOVerifyForm;

public class RequestUserSSOVerifyServlet extends ServletBase  {
    private static final long serialVersionUID = 1L;
    private Authorization auth;

    static Logger log = Logger.getLogger(RequestUserSSOVerifyServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
                auth = context.getAuthorization();

		if(!request.isSecure()) {
			//force redirection to https - this form could transmit identity/encryption password
			response.sendRedirect(context.getSecureUrl());
			return;
		}

		//          if(auth.isUser()) {
		    BootMenuView menuview = new BootMenuView(context, "home");
		    BootPage page = new BootPage(context, menuview, createContent(context), null);
		    page.render(response.getWriter());
		    //}
	}
	
	protected IView createContent(final UserContext context) throws ServletException
	{
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span3\">");
				
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				//CertificateQuotaModel quota = new CertificateQuotaModel(context);
				
				//editable content
				ConfigModel config = new ConfigModel(context);
				Config home_content = config.new Config(config, "certificate_request_user", "");
				Authorization auth = context.getAuthorization();
				
				//out.write(home_content.getString());
				
				
				if(auth.isUser()) {
				    RequestUserSSOVerifyForm form = new RequestUserSSOVerifyForm(context, "requestuserverifyform");
				    form.render(out);	
				}else{
				    out.write("<h3>Please login.</h3>");

				}

				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
			}
		};
	}
}
