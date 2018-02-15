package edu.iu.grid.oim.servlet;

////////////////////////////////
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.model.db.SSOSmallTableModelBase;

import edu.iu.grid.oim.lib.StaticConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.json.*;
import javax.servlet.http.HttpSession;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.util.*;

////////////////////////////////

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

///////////////////////
import java.net.URI;
import java.net.URL;

import com.nimbusds.oauth2.sdk.client.*;
import com.nimbusds.oauth2.sdk.token.*;
import com.nimbusds.oauth2.sdk.util.*;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.oauth2.sdk.auth.*;
import com.nimbusds.oauth2.sdk.http.*;
import com.nimbusds.oauth2.sdk.*;

import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.*;
import com.nimbusds.openid.connect.sdk.id.*;
import com.nimbusds.openid.connect.sdk.op.*;
import com.nimbusds.openid.connect.sdk.rp.*;
import com.nimbusds.openid.connect.sdk.util.*;



public class SSOServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		SmallTableModelBase.emptyAllCache();
		SSOSmallTableModelBase.emptyAllCache();
	
		//String referrer = request.getHeader("referer");
		
		try {
		     URI authzEndpoint = new URI("https://cilogon.org/authorize");
		 
		    
		     ClientID clientID = new ClientID(StaticConfig.conf.getProperty("cilogon.clientID"));
		    // The requested scope values for the token	\
		     String clientIDstr = StaticConfig.conf.getProperty("cilogon.clientID");
		    
		    Scope scope = new Scope("openid", "email","profile","org.cilogon.userinfo");
		    String scopestr = "scope=openid+email+profile+org.cilogon.userinfo";
		    Secret clientSecret = new Secret(StaticConfig.conf.getProperty("cilogon.client_secret"));
		    String clientSecretstr = StaticConfig.conf.getProperty("cilogon.client_secret");
		    // The client callback URI, typically pre-registered with the server \
		    HttpSession session = request.getSession();


		    URI callback = new URI(StaticConfig.conf.getProperty("cilogon.callback"));

		    String redirectURLstr =StaticConfig.conf.getProperty("cilogon.callback");
		    // Generate random state string for pairing the response to the request \
		    
		    State state = new State();
		    // Build the request                                                             
		    Nonce nonce = new Nonce();
		
		    if(request.getParameter("code")=="" || request.getParameter("code")==null){
			try {
			    String referrer = request.getHeader("referer");
			    
                            session.setAttribute("referer", referrer);

			    // Compose the request (in code flow)
			    AuthenticationRequest req = new AuthenticationRequest(
						 authzEndpoint,
						 new ResponseType(ResponseType.Value.CODE),
						 Scope.parse("openid email profile org.cilogon.userinfo"),
						 clientID,
						 callback,
						 state,
						 nonce);

			    URI requestURI = req.toURI();

			    String str = requestURI.toString();
			    System.out.println("#########################################  Hello World 3");

			    response.sendRedirect(str);
			}
			
			catch(Exception exception) {
			    System.out.println("Caught Exception No Code: " + exception);
			}
		    }else{
			String code =  request.getParameter("code");

			try{
			    String url = "https://cilogon.org/oauth2/token";
			    //String url="http://mvkrenz.grid.iu.edu";
			    URL obj = new URL(url);
			    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			    
			    //add reuqest header
			    con.setRequestMethod("POST");
			    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			    String data = "grant_type=authorization_code&client_id=" + clientIDstr +
				"&client_secret=" + clientSecretstr + 
				"&code=" +code +
				"&redirect_uri=" + redirectURLstr;
			    
			    // Send post request
			    con.setDoOutput(true);
			    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			    wr.writeBytes(data);
			    wr.flush();
			    wr.close();
			    
			    int responseCode = con.getResponseCode();
			    System.out.println("\nSending 'POST' request to URL : " + url);
			    System.out.println("Post parameters : " + data);
			    System.out.println("Response Code : " + responseCode);
			    
			    BufferedReader in = new BufferedReader(
								   new InputStreamReader(con.getInputStream()));
			    String inputLine;
			    StringBuffer response5 = new StringBuffer();
			    
			    while ((inputLine = in.readLine()) != null) {
				response5.append(inputLine);
			    }
			    in.close();
			    
			    //print result
			    String jsonstr= response5.toString();
			    JSONObject json = (JSONObject) JSONValue.parseWithException(jsonstr);
			    String access_token = (String) json.get("access_token");
                            String refresh_token = (String) json.get("refresh_token");
                            String id_token = (String) json.get("id_token");
                            String token_type = (String) json.get("token_type");
                            //Integer expires_in = (Integer) json.get("expires_in");


			    System.out.print("############################# "+ access_token+ " <= access token");
			    String url_userinfo = "https://cilogon.org/oauth2/userinfo";
                            URL objuserinfo = new URL(url_userinfo);
                            HttpsURLConnection conuserinfo = (HttpsURLConnection) objuserinfo.openConnection();

                            //add reuqest header                                                                                              
                            conuserinfo.setRequestMethod("POST");
                            conuserinfo.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conuserinfo.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                            String datauserinfo = "access_token=" + access_token;

                            // Send post request                                                                                              
                            conuserinfo.setDoOutput(true);
                            DataOutputStream wr_userinfo = new DataOutputStream(conuserinfo.getOutputStream());
                            wr_userinfo.writeBytes(datauserinfo);
                            wr_userinfo.flush();
                            wr_userinfo.close();

			    // int responseCode1 = conuserinfo.getResponseCode();
                            
                            BufferedReader in_userinfo = new BufferedReader(
									    new InputStreamReader(conuserinfo.getInputStream()));
                            String inputLine_userinfo;
                            StringBuffer response5_userinfo = new StringBuffer();

                            while ((inputLine_userinfo = in_userinfo.readLine()) != null) {
                                response5_userinfo.append(inputLine_userinfo);
                            }
                            in_userinfo.close();

                            //print result                                                                                                    
			    System.out.println("before json parse");
                            String jsonstr_userinfo= response5_userinfo.toString();
                            JSONObject json_userinfo = (JSONObject) JSONValue.parseWithException(jsonstr_userinfo);
                            String access_email = (String) json_userinfo.get("email");

			    //HttpSession session = request.getSession();

			    //  String user_access=  (String) session.getAttribute("user_access");
			   
			    session.setAttribute("user_access", access_email);
                            String family_name = (String) json_userinfo.get("family_name");
			    String given_name = (String) json_userinfo.get("given_name");
			    String idp = (String) json_userinfo.get("idp");
			    String idp_name = (String) json_userinfo.get("idp_name");
			    String eptid = (String) json_userinfo.get("eptid");

			    session.setAttribute("OIDC_CLAIM_access_token",eptid);
			    session.setAttribute("OIDC_CLAIM_family_name", family_name);
			    session.setAttribute("OIDC_CLAIM_given_name", given_name);
			    session.setAttribute("OIDC_CLAIM_idp", idp);
			    session.setAttribute("OIDC_CLAIM_idp_name", idp_name);


			    String user_access=  (String) session.getAttribute("user_access");

			    System.out.println(user_access + " < = session email");

			    System.out.println(access_email + "<== access email"); 
                            System.out.print("############################# "+ access_email+ " <= access email");
			    HttpSession session1 = request.getSession(false);
			    String referer = (String)session1.getAttribute("referer");

			    if(referer=="" || referer == null ){
			    response.sendRedirect("/");
			    }else{
				response.sendRedirect(referer);

			    }
			    
			}

                        catch(Exception exception) {
                            System.out.println("Caught Exception Code Passed: " + exception);
                        }

		    }
		}

		catch(Exception exception) {
		    System.out.println("Caught Exception Main: " + exception);
		}
			
		///////////////////////////////////////////////////////////////////		
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
		    //disable authorization on the home pag 6/29/2017

		    	Authorization auth = context.getAuthorization();
		    	if(auth.isUser()) {
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
			//old link - http://pki1.doegrids.org/ca/
			String text = "<p>OIM requires an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to authenticate.</p>"+
					"<p><a class=\"btn btn-info\" href=\"/oim/certificaterequestuser\">Request New Certificate</a></p>"+
					"If you already have a certificate installed on your browser, please login.</p><p><a class=\"btn btn-info\" href=\""+context.getSecureUrl()+"\">Login</a></p>";
			
			contentview.add(new HtmlView("<div class=\"alert alert-info\"><p>"+text+"</p></div>"));
			}
		
		contentview.add(new HtmlView("<h2>Documentations</h2>"));
		contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMTermDefinition", "OIM Definitions", true));
		contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions", "Registration", true));
		contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool", "Resource Downtime", true));
		//contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures", "Operating Procedures", true));

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
}
