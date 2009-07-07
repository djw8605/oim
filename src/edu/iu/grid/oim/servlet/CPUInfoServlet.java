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

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepEvent;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class CPUInfoServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CPUInfoServlet.class);  
	
    public CPUInfoServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		auth.check("edit_measurement"); 
		
		try {

			//construct view
			MenuView menuview = new MenuView(context, "cpuinfo");;
			ContentView contentview = createContentView();
			
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
		CpuInfoModel model = new CpuInfoModel(context);
		Collection<CpuInfoRecord> cpus = model.getAll();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>CPU Information</h1>"));
	
		for(CpuInfoRecord rec : cpus) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
				
			RecordTableView table = new RecordTableView();
			contentview.add(table);

		 	table.addRow("Name", rec.name);
			table.addRow("Normalization Constant", rec.normalization_constant.toString());
			table.addRow("Notes", rec.notes);
	
			class EditButtonDE extends DivRepButton
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(DivRepEvent e) {
					redirect(url);
				}
			};
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/cpuinfoedit?cpu_info_id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New CPU Info record");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "cpuinfoedit"));
		
		return view;
	}
}
