package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.Utils;
import edu.iu.grid.oim.view.TableView.Row;

public class FacilityServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FacilityServlet.class);  
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		//Authorization auth = context.getAuthorization();
		
		try {	
			//construct view
			MenuView menuview = new MenuView(context, "facility");
			ContentView contentview = createContentView(context);
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			//bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Topology", "topology");
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(context, menuview, contentview, createSideView(context));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{
		//pull list of all sites
		FacilityModel model = new FacilityModel(context);
		ArrayList<FacilityRecord> facilities = model.getAll();
		Collections.sort(facilities, new Comparator<FacilityRecord> () {
			public int compare(FacilityRecord a, FacilityRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});

		ContentView contentview = new ContentView(context);	
		contentview.add(new HtmlView("<h1>Facilities</h1>"));
	
		for(FacilityRecord rec : facilities) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.addRow("Description", rec.description);
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);

			class EditButtonDE extends DivRepButton
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onClick(DivRepEvent e) {
					redirect(url);
				}
			};
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), "facilityedit?facility_id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Facility");
				url = _url;
			}
			protected void onClick(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "facilityedit"));
		view.add("About", new HtmlView("This page shows a list of facilities that all registered OIM users are able to edit. We ask that you please refrain from editing facilities that are not directly related to you unless there is a specific reason to do so! All changes are audited by GOC staff."));
		return view;
	}
}
