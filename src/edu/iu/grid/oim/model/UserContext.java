package edu.iu.grid.oim.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;

//provides easy access to various object that are user specific
public class UserContext {
    static Logger log = Logger.getLogger(UserContext.class);  
    private DivRepRoot divrep_root;
    private String divrep_pageid = null;
	private DivRepPage divrep_pageroot = null;
	
	private Authorization auth = new Authorization();
	//private Connection connection;
	private String request_url;
	private HttpSession session;
	private String remote_addr;
	
	//let's lookup once per user request (not session)
    private DataSource oimds;
    //private Connection oim;

	//stores the reason for current transaction (used for log table)
	//why should this work? because *usually* all update within a session occurs under common purpose.
	private String comment;
	public void setComment(String _comment) { comment = _comment; }
	public String getComment() { return comment; }
	public String getRemoteAddr() { return remote_addr; }
	public HttpSession getSession() { return session; }
	
	public UserContext(HttpServletRequest request) throws AuthorizationException
	{	
		//don't store request object because it can get stale really fast... (was causing issue when divrep tries to get session from it)
		//request = _request;	
		
		session = request.getSession();
		auth = new Authorization(request);
		setRequestURL(request);
		divrep_root = DivRepRoot.getInstance(request.getSession());
		divrep_pageid = request.getRequestURI() + request.getQueryString();
		remote_addr = request.getRemoteAddr();
	}
	
	public Connection getConnection() throws SQLException {
		if(oimds == null) {
		    try {
		    	log.debug("Looking for jdbc connection");
		    	Context initContext = new InitialContext();
		    	Context envContext  = (Context)initContext.lookup("java:/comp/env");
		    	oimds = (DataSource)envContext.lookup("jdbc/oim");
		    	log.debug(oimds.toString());
		    } catch( NamingException ne ) {
		    	throw new RuntimeException( "Unable to aquire data source", ne );
		    }	
		}
		/*
		if(oim == null || oim.isClosed()) {
	    	log.debug("Connecting..");
			oim = oimds.getConnection();
	    	log.debug(oim.toString());
		}
		return oim;
		*/
    	log.debug("Connecting..");
		Connection oim = oimds.getConnection();
    	log.debug(oim.toString());
		return oim;
	}
	
	public static UserContext getGuestContext()
	{
		return new UserContext();
	}
	private UserContext()
	{
	}

	/*
	//call this function to ensure that any update made to divrep object will be serialized across container
	public void storeDivRepSession()
	{		
		try {
			if(divrep_root != null) {	
				divrep_root.setSession(session);
			}
		} catch (IllegalStateException e) {
			log.info("Failed to reset session for divrep (it's okay if this caused by invalidated session)", e);
		}
	}
	*/
	
	
	//make sure to close the connection as soon as you are done (inside the same function that you call connectOIM)
	
	public Authorization getAuthorization()
	{
		return auth;
	}
	public DivRepPage getPageRoot()
	{
		if(divrep_pageroot == null) {
			log.debug("Initializing divrep page root for: " + divrep_pageid);
			divrep_pageroot = divrep_root.initPage(divrep_pageid);
		}
		return divrep_pageroot;
	}
	
	public String getRequestURL()
	{
		return request_url;
	}
	
	private void setRequestURL(HttpServletRequest request) {
		request_url = "";
		if(request != null) {
			request_url += request.getRequestURI();
			if(request.getQueryString() != null) {
				request_url += "?" + request.getQueryString();
			}
		}
	}
}
