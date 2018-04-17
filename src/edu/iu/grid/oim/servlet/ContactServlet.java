package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class ContactServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ContactServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_my_contact");

		try {	
			BootMenuView menuview = new BootMenuView(context, "contact");
			ContentView contentview = null;
			SideContentView sideview = null;
			
			//display either list, or a single resource
			ContactRecord rec = null;
			String contact_id_str = request.getParameter("id");
			System.out.println("ContactServlet contact ID ->" +  contact_id_str);
			if(contact_id_str != null) {
				ContactModel model = new ContactModel(context);
				Integer contact_id = Integer.parseInt(contact_id_str);
				rec = model.get(contact_id);
				contentview = new ContentView(context);
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("OSG Contact", "contact");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				if(model.canEdit(rec.id)) {
					contentview.add(new HtmlView("<p class=\"pull-right\"><a class=\"btn\" href=\"contactedit?id=" + rec.id + "\">Edit</a></p> "));
				}
				//contentview.add(new HtmlView("<p class=\"pull-right\"><a class=\"btn\" href=\"contactedit\">Register New Contact</a></p> "));
				
				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				contentview.add(createContent(context, rec)); //false = no edit button	
				
				//sideview = createRecordSideView(context);
			} else {
				//pull list of all contacts
				contentview = createContentView(context);
				sideview = createListSideView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, sideview);//createSideView(context, rec, model.canEdit(rec.id)));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{
		ContactModel model = new ContactModel(context);
		ArrayList<ContactRecord> contacts = model.getAll();

		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.name.compareToIgnoreCase(b.name); // We are comparing based on name
			}
		});
		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.disable.compareTo(b.disable); // We are comparing based on bool disable (disabled ones will go in the end)
			}
		});

		ContentView contentview = new ContentView(context);	
		
		ArrayList<ContactRecord> editable_contacts = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> editable_disabled_contacts = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> readonly_contacts = new ArrayList<ContactRecord>();
		for(ContactRecord rec : contacts) {
		    //    System.out.println("******************contact ID from Content View ->" + rec.id + "-- " + model.canEdit(rec.id));
		        if(model.canEdit(rec.id)) {
				if (rec.disable) {
					editable_disabled_contacts.add(rec);
				} else {
					editable_contacts.add(rec);
				}
			} else {
				readonly_contacts.add(rec);
			}
		    
		    
		}
		//System.out.println(contacts.toString());

		return createContentViewHelper(context, contentview, editable_contacts, editable_disabled_contacts, readonly_contacts);
	}

	protected ContentView createContentViewHelper (UserContext context, ContentView contentview, 
			Collection<ContactRecord> editable_contacts, 
			Collection<ContactRecord> editable_disabled_contacts, 
			Collection<ContactRecord> readonly_contacts) 
		throws ServletException, SQLException
	{  
		DNModel dnmodel = new DNModel(context);
		
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<p class=\"pull-right\"><a class=\"btn\" href=\"contactedit\"><i class=\"icon-plus-sign\"></i> Register New Contact</a></p>"));
		}
		contentview.add(new HtmlView("<h2>OSG Contacts</h2>"));
		
		if(editable_contacts.size() != 0) {
			//contentview.add(new HtmlView("<h2>Editable</h2>"));
			//contentview.add(new HtmlView("<p>You have edit access to following contacts</p>"));
	
			ItemTableView table = new ItemTableView(3);
			for(ContactRecord rec : editable_contacts) {
				table.add(new HtmlView(getContactHeader(dnmodel, rec, true)));
				//contentview.add(showContact(rec, true)); //true = show edit button
			}
			contentview.add(table);
		}

		if(readonly_contacts.size() != 0) {
			contentview.add(new HtmlView("<h2>Read-Only</h2>"));
			contentview.add(new HtmlView("<p>The following are the contacts currently registered with OIM for which you do not have edit access.</p>"));
	
			ItemTableView table = new ItemTableView(4);
			for(ContactRecord rec : readonly_contacts) {
				table.add(new HtmlView(getContactHeader(dnmodel, rec, false)));
				//contentview.add(showContact(rec, false)); //false = no edit button
			}
			contentview.add(table);
		}
		
		if(context.getAuthorization().allows("admin") && editable_disabled_contacts.size() != 0) {
			contentview.add(new HtmlView("<h2>Disabled (Admin Only)</h2>"));
			//contentview.add(new HtmlView("<p>The following are the contacts that are currently disabled.</p>"));
	
			ItemTableView table = new ItemTableView(4);
			for(ContactRecord rec : editable_disabled_contacts) {
				table.add(new HtmlView(getContactHeader(dnmodel, rec, true)));
				//contentview.add(showContact(rec, true)); //true = show edit button
			}
			contentview.add(table);
		}	
		
		return contentview;
	}
	private String getContactHeader(DNModel dnmodel, ContactRecord rec, boolean edit)
	{
		String image, name_to_display;
		if(rec.person == true) {
			//image = "<img align=\"top\" src=\"images/user.png\"/> ";
			//count number of DNs associated
			Integer count = null;
			try {
				ArrayList<DNRecord> dns = dnmodel.getEnabledByContactID(rec.id);
				if(dns.size() > 0) {
					String tooltip = "";
					for(DNRecord dn : dns) {
						tooltip += dn.dn_string + "<br>";
					}
					image = "<span ref=\"tooltip\" title=\""+tooltip+"\" class=\"label label-success\">"+dns.size()+" DN</span></a>";
				} else {
					image = "<span class=\"label label-warning\">No DN</span>";
				}
			} catch (SQLException e) {
				log.error("Failed to load dn list for contact id:" + rec.id, e);
				image = "?";
			}
		} else {
			//image = "<img align=\"top\" src=\"images/group.png\"/> "; 
			image = "<span class=\"label\">Non Personal</span>";
		}
		String url = "";
		if(edit) {
			url = "contactedit?id="+rec.id;
		} else {
			url = "contact?id="+rec.id;
		}
		if(rec.disable == false) {
			name_to_display = "<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(rec.name)+"</a> "+image;
		} else {
			name_to_display = "<a href=\""+url+"\" class=\"disabled\">"+StringEscapeUtils.escapeHtml(rec.name)+"</a> "+image;
		}
		return name_to_display;
	}
	
	public DivRep createContent(UserContext context, final ContactRecord rec) {
		
		RecordTableView table = new RecordTableView();
		try {	
			table.addRow("Primary Email", new HtmlView("<a class=\"mailto\" href=\"mailto:"+rec.primary_email+"\">"+StringEscapeUtils.escapeHtml(rec.primary_email)+"</a>"));
			table.addRow("Secondary Email", rec.secondary_email);

			table.addRow("Primary Phone", rec.primary_phone);
			table.addRow("Primary Phone Ext", rec.primary_phone_ext);

			table.addRow("Secondary Phone", rec.secondary_phone);
			table.addRow("Secondary Phone Ext", rec.secondary_phone_ext);
			
			table.addRow("SMS Address", rec.sms_address);
			
			if(rec.person == false) {
				table.addRow("Personal Information", new HtmlView("(Not a personal contact)"));
			} else {
				RecordTableView personal_table = new RecordTableView("inner_table");
				table.addRow("Personal Information", personal_table);
	
				personal_table.addRow("Address Line 1", rec.address_line_1);
				personal_table.addRow("Address Line 2", rec.address_line_2);
				personal_table.addRow("City", rec.city);
				personal_table.addRow("State", rec.state);
				personal_table.addRow("ZIP Code", rec.zipcode);
				personal_table.addRow("Country", rec.country);
				personal_table.addRow("Instant Messaging", rec.im);
	
				String img = rec.photo_url;
				if(rec.photo_url == null || rec.photo_url.length() == 0) {
					img = "images/noavatar.gif";
				} 
				personal_table.addRow("Photo", new HtmlView("<img class=\"avatar\" src=\""+img+"\"/>"));
				personal_table.addRow("Contact Preference", rec.contact_preference);	
				personal_table.addRow("Time Zone", rec.timezone);
				personal_table.addRow("Profile", new HtmlView("<div>"+StringEscapeUtils.escapeHtml(rec.profile)+"</div>"));
				personal_table.addRow("Use TWiki", rec.use_twiki);
				personal_table.addRow("TWiki ID", rec.twiki_id);
			}
			
			table.addRow("Contact Associations", new ContactAssociationView(context, rec.id));
			
			//table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			
			DNModel dnmodel = new DNModel(context);
			if(context.getAuthorization().allows("admin")) {
				String submitter_dn = null;
				if(rec.submitter_dn_id != null) {
					DNRecord dn = dnmodel.get(rec.submitter_dn_id);
					submitter_dn = dn.dn_string;
				}
				table.addRow("Submitter DN", submitter_dn);
			}

			if(context.getAuthorization().allows("admin")) {
				final ArrayList<DNRecord> dnrecs = dnmodel.getByContactID(rec.id);
				table.addRow("Associated DNs", new IView() {

					@Override
					public void render(PrintWriter out) {
						if(dnrecs.size() == 0) {
							out.write("<p class=\"muted\">No DNs are associated with this contact.</p>");
						} else {
							out.write("<ul>");
							for(DNRecord rec : dnrecs) {
								out.write("<li>");
								if(rec.disable) {
									out.write("<span class=\"pull-right label\">Disabled DN</span>");
								}
								out.write(StringEscapeUtils.escapeHtml(rec.dn_string)+"</li>");
							}
							out.write("</ul>");
						}
					}
				});		
				
				//show quota if there is any DN associated (including disabled)
				if(dnrecs.size() > 0) {
					//Certificate Quota
					ConfigModel config = new ConfigModel(context);
					table.addRow("User Certificate Requests", rec.count_usercert_year + " (max "+config.QuotaUserCertYearMax.getString()+" per year)");
					table.addRow("Host Certificate Approvals (Year)", rec.count_hostcert_year + " (max "+config.QuotaUserHostYearMax.getString()+" per year)");
					table.addRow("Host Certificate Approvals (Today)", rec.count_hostcert_day + " (max "+config.QuotaUserHostDayMax.getString()+" per day)");
				}
			}
						
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
	}
	
	private SideContentView createListSideView(UserContext context) {
		SideContentView view = new SideContentView();
		view.add(new HtmlView("This page shows a list of contacts on OIM. Contacts can be a person or a mailing list or a service that needs to be registered on OIM to access privileged information on other OSG services. <p><br/> You as a registered OIM user will be able to edit any contact you added. GOC staff are able to edit all contacts including previous de-activated ones. <p><br/> If you want to map a certain person or group contact (and their email/phone number) to a resource, VO, SC, etc. but cannot find that contact already in OIM, then you can add a new contact. <p><br/>  Note that if you add a person as a new contact, that person will still not be able to perform any actions inside OIM until they register their X509 certificate on OIM."));		
		//view.addContactGroupFlagLegend();
		return view;
	}
}
