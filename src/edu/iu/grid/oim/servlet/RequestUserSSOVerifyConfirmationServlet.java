package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepTextArea;


import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.form.RequestUserSSOVerifyConfForm;


import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.SSOModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.GenericView;


public class RequestUserSSOVerifyConfirmationServlet extends ServletBase  {
    private static final long serialVersionUID = 1L;
    public String sso_id;
    public String dirty_id;
    private Authorization auth;
    private Integer i = 1;
    private UserContext context;
    private ConfigModel config;
    private Footprints fp;
    private SSORecord requester;
    private SSORecord activation_record;

    private FPTicket ticket ;
    private ContactRecord requester_contact;
    private String answer = null;
    public HttpServletResponse inside_response;
    public HttpServletRequest inside_request;
    private SSOModel ssomodel;
    public String first_name;
    public String last_name;
    public String idp_name;
    public String email;

    static Logger log = Logger.getLogger(RequestUserSSOVerifyServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    GenericView pane;

	    try{
		context = new UserContext(request);
		config = new ConfigModel(context);
		//Config home_content = config.new Config(config, "certificate_request_user", "");                                                       
		inside_response = response;
		inside_request = request;
		auth = context.getAuthorization();
		BootMenuView menuview = new BootMenuView(context, "home");
                //BootPage page = new BootPage(context, menuview, createContent(context, dirty_id), null);                                                                      
                ContentView contentview = new ContentView(context);
                BootPage page;

                if(auth.isUser()) {
		    
		    dirty_id = request.getParameter("id");		
		    fp = new Footprints(context);
		    ticket = fp.new FPTicket();
		    requester = auth.getSSORecord();
		    requester_contact = auth.getContact();
		    String sponsor_name = requester.given_name +" "+requester.family_name;
		    
		    ticket.phone = requester_contact.primary_phone;
		    
		    ssomodel = new SSOModel(context);
		    Integer sso_id_int=  Integer.parseInt(dirty_id);
		    activation_record = ssomodel.getBySSOID(sso_id_int);
		    first_name =activation_record.given_name;
		    last_name=activation_record.given_name;
		    idp_name=activation_record.idp_name;
		    email=activation_record.email;

		    String answer =  ssomodel.updateSSOverifyPair(requester_contact.id, dirty_id);
		    System.out.println("Confirmation page, update ticket:"+answer);		    
		    if(answer!=null){
			page = new BootPage(context, menuview, createContent(context, dirty_id, first_name, last_name, idp_name,email, sponsor_name, answer), null);
			
		    }else{
		    	
			contentview.add(new HtmlView("<h2>You are not authorized to verify and approve this account for "+activation_record.given_name+" "+activation_record.family_name+".</h2>"));
			//page = new BootPage(context, menuview, createContent(context, dirty_id), null);
			page = new BootPage(context, menuview, contentview, null);
			
		    }
		    
		}else{

		    contentview.add(new HtmlView("<h3>Please make sure to login to be able to approve this account.</h3>"));
		    //page = new BootPage(context, menuview, createContent(context, dirty_id), null);                                                                       
		    page = new BootPage(context, menuview, contentview, null);

		}
		
		page.render(response.getWriter());

	    }
	    catch(Exception e){
		throw new ServletException(e);

	    }
	}
	
    protected IView createContent(final UserContext context, final String dirty_id, final String first_name, final String last_name, final String idp_name, final String email, final String sponsor_name, final String answer) throws ServletException
	{
		return new IView(){
			@Override
			public void render(PrintWriter out) {
			    out.write("<div id=\"content\">");
			    out.write("<div class=\"row-fluid\">");
			    out.write("<div class=\"span3\">");
			    
			    // CertificateMenuView menu = new CertificateMenuView(context, "certificaterequestuser");
			    //menu.render(out);
			    out.write("</div>"); //span3
			    
			    out.write("<div class=\"span9\">");

                            //out.write("Dear "+sponsor_name+",<br>");
                            out.write("You have been identified and asked to verify an account access for <b>"+first_name +" " + last_name+ "</b> (<i>"+email+"</i>) from "+idp_name+"");
                            out.write("<br><br>");

                            out.write("Please either decline or approve the account.<br><br>");

			    class RevokeButton extends DivRepButton {
				    int idx;
				    public RevokeButton(DivRep parent, int idx) {
					super(parent, "Decline");
					addClass("btn-primary ");
					addClass("divrep_button");
					//addClass("pull-right");
					this.idx = idx;
				    }
				    protected void onClick(DivRepEvent e) {
					
					System.out.println("Deny the CERT: "+ dirty_id);
					try{
					    ticket.description = "Account has been DECLINED";
					    fp.update(ticket,answer);
					    ssomodel.updateSSOverifyConfirmation(dirty_id,0,requester.contact_id);
					    try{
						context.message(MessageType.SUCCESS, "Successfully declined a request with: " +dirty_id );
						//js("location.reload()");
						js("window.location.replace('http://oim.grid.iu.edu');");
						System.out.println("Go here ttps://oim.grid.iu.edu");
						//inside_response.sendRedirect("https://oim-dev1.grid.iu.edu");
						return;
					    }
					    catch(Exception e1){
						System.out.println("redirect error : " + e1.getMessage());
						    e1.printStackTrace();
					    }
					}
					catch(SQLException e2){
					    e2.printStackTrace();
					}
				    }
				}
				  
				class ConfirmButton extends DivRepButton {
                                    int idx;
                                    public ConfirmButton(DivRep parent, int idx) {
                                        super(parent, "Approve");
                                        addClass("btn-primary ");
                                        addClass("divrep_submit");
                                        addClass("divrep_submit");
                                        this.idx = idx;
                                    }
                                    protected void onClick(DivRepEvent e) {
				
					try{
					    ticket.description = "Account has been VERIFIED.";

					    fp.update(ticket,answer);
					    ssomodel.updateSSOverifyConfirmation(dirty_id,1,requester.contact_id);
					    context.message(MessageType.SUCCESS, "Successfully approved a request with: " +dirty_id );

					    js("window.location.replace('http://oim.grid.iu.edu');");

					}
					catch(SQLException e3){
					    e3.printStackTrace();
					    
					}
							
                                        System.out.println("Confirm the CERT: "+ dirty_id);
                                    }
                                }


				ConfirmButton confirm_button = new ConfirmButton(context.getPageRoot(), i);
                                confirm_button.render(out);
				out.write("&nbsp;");
				RevokeButton button = new RevokeButton(context.getPageRoot(), i);
				button.render(out);
		
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
			}
		};
	}
}
