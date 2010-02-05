package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.servlet.ActionServlet;

public class Page implements IView {
	static Logger log = Logger.getLogger(Page.class);  
	
    protected Context context;
    
	private IView header;
	private IView menu;
	private IView content;
	private IView footer;
	private IView side;
	
	public Page(Context _context, IView _menu, IView _content, IView _side)
	{
		context = _context;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("__STATICBASE__", StaticConfig.getStaticBase());
		params.put("__APPNAME__", StaticConfig.getApplicationName());
		params.put("__VERSION__", StaticConfig.getVersion());
		
		try {
			String request_uri = context.getRequestURL();
			request_uri = URLEncoder.encode(request_uri, "UTF-8");
			params.put("__REF__", request_uri);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		} 

		if(context.getAuthorization().isGuest()) {
			params.put("__DN__", "Guest");
		} else {
			params.put("__DN__", context.getAuthorization().getUserDN());
		}
		
		header = new HtmlFileView("header.txt", params);
		footer = new HtmlFileView("footer.txt", params);
		menu = _menu;
		content = _content;
		side = _side;
	}

	
	public void render(PrintWriter out)
	{
		header.render(out);
		menu.render(out);
		side.render(out);
		content.render(out);		
		footer.render(out);
	}
}
