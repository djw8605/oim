package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.DNModel;

import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.Utils;
import edu.iu.grid.oim.view.TableView.Row;

public class SiteServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SiteServlet.class);  
	
    public SiteServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setContext(request);
		auth.check("admin");
		
		try {	
			//construct view
			MenuView menuview = createMenuView("admin");
			ContentView contentview = createContentView();
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Site",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		SiteModel model = new SiteModel(context);
		Collection<SiteRecord> sites = model.getAll();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Administrativs Sites</h1>"));
	
		for(SiteRecord rec : sites) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);

		 	table.addRow("Long Name", rec.long_name);
			table.addRow("Description", rec.description);
			table.addRow("Street", rec.address_line_1);
			table.addRow("Address line 2", rec.address_line_2);
			table.addRow("City", rec.city);
			table.addRow("State", rec.state);
			table.addRow("Zipcode", rec.zipcode);
			table.addRow("Country", rec.country);
			table.addRow("Longitude", rec.longitude);
			table.addRow("Latitude", rec.latitude);

			table.addRow("Facility", getFacilityName(rec.facility_id));
			table.addRow("Support Center", getSCName(rec.sc_id));
//			table.addRow("Submitter Contact", getSubmitterName(rec.submitter_dn_id));
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);

			class EditButtonDE extends ButtonDE
			{
				String url;
				public EditButtonDE(DivEx parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/siteedit?site_id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private String getSCName(Integer sc_id) throws SQLException
	{
		if(sc_id == null) return null;
		SCModel model = new SCModel(context);
		SCRecord sc = model.get(sc_id);	
		if(sc == null) {
			return null;
		}
		return sc.name;
	}
	
	private String getFacilityName(Integer facility_id) throws SQLException
	{
		if(facility_id == null) return null;
		FacilityModel model = new FacilityModel(context);
		FacilityRecord facility = model.get(facility_id);	
		if(facility == null) {
			return null;
		}
		return facility.name;
	}

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Administrative Site");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getDivExRoot(), "siteedit"));
		view.add("About", new HtmlView("This page shows a list of administratives sites in various facilities that GOC staff maintain."));		
		return view;
	}
}
