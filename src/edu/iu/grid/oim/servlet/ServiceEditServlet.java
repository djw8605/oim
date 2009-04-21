package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.DivExRoot;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.ServiceFormDE;

public class ServiceEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ServiceEditServlet.class);  
	private String current_page = "service";	

    public ServiceEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		auth.check("admin");
		
		ServiceRecord rec;
		String title;

		//if osg_grid_type_id is provided then we are doing update, otherwise do new.
		// AG: Do we need any request parameter-value checks?
		String id_str = request.getParameter("id");
		if(id_str != null) {
			//pull record to update
			int id = Integer.parseInt(id_str);
			ServiceModel model = new ServiceModel(auth);
			try {
				rec = model.get(id);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Service";
		} else {
			rec = new ServiceRecord();
			title = "New Service";	
		}
	
		ServiceFormDE form;
		String origin_url = Config.getApplicationBase()+"/"+current_page;
		try {
			form = new ServiceFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		Page page = new Page(createMenuView("admin"), contentview, createSideView());	
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("Misc-no-op", new HtmlView("Misc-no-op"));
		return view;
	}
}