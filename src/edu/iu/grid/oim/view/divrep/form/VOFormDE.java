package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.Static;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.FormElementBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextAreaFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.UniqueValidator;
import com.webif.divrep.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.VOReport;

import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.VOReportNameModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOVOModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ResourceServices;
import edu.iu.grid.oim.view.divrep.ResourceWLCG;
import edu.iu.grid.oim.view.divrep.VOReportNameFqan;
import edu.iu.grid.oim.view.divrep.VOReportNames;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class VOFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(VOFormDE.class); 
   
    private Context context;
	private Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextFormElement long_name;
	private TextAreaFormElement description;
	private TextAreaFormElement app_description;
	private TextAreaFormElement community;
	private TextFormElement footprints_id;
	private SelectFormElement sc_id;
	private CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	private CheckBoxFormElement child_vo;
	private SelectFormElement parent_vo;
	
	private VOReport vorep_consolidator;
	private VOReportNames vo_report_name_div;
	
	class FieldOfScience extends DivRep
	{
		Button add_fs;
		TextFormElement new_fs; 
		
		public FieldOfScience(DivRep _parent, final VORecord rec) throws SQLException {
			super(_parent);
			
			populateList(rec);
			
			new_fs = new TextFormElement(this);
			new_fs.setLabel("Or, you can add a new field of science");
			new_fs.setWidth(230);
			
			add_fs = new Button(this, "Add");
			add_fs.setStyle(Button.Style.ALINK);
			add_fs.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					String name = new_fs.getValue();
					if(name == null || name.trim().length() == 0) {
						alert("Please enter field of science to add");
						return;
					}
					name = name.trim();
					for(CheckBoxFormElement elem : field_of_science.values()) {
						if(name.equals(elem.getLabel())) {
							alert("'" + name + "' already exists in the list");
							return;
						}
					}

					try {
						//add new field of science						
						FieldOfScienceModel fsmodel = new FieldOfScienceModel(context);	
						FieldOfScienceRecord newrec = new FieldOfScienceRecord();
						newrec.name = name;
						fsmodel.insert(newrec);

						//repopulate the list
						populateList(rec);
						FieldOfScience.this.redraw();
						
						//select newly created fs
						CheckBoxFormElement elem = findFieldOfScience(name);
						elem.setValue(true);
						
						new_fs.setValue(null);
					} catch (SQLException e1) {
						log.error(e1);
					}
				}}
			);
		}
		private void populateList(VORecord rec) throws SQLException
		{
			FieldOfScienceModel fsmodel = new FieldOfScienceModel(context);
			field_of_science = new HashMap();
			for(FieldOfScienceRecord fsrec : fsmodel.getAll()) {
				CheckBoxFormElement elem = new CheckBoxFormElement(this);
				field_of_science.put(fsrec.id, elem);
				elem.setLabel(fsrec.name);
			}
			
			if(rec.id != null) {
				//select currently selected field of science
				VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(context);
				for(VOFieldOfScienceRecord fsrec : vofsmodel.getByVOID(rec.id)) {
					CheckBoxFormElement check = field_of_science.get(fsrec.field_of_science_id);
					check.setValue(true);
				}
			}
		}
		
		private CheckBoxFormElement findFieldOfScience(String name)
		{
			for(CheckBoxFormElement elem : field_of_science.values()) {
				if(elem.getLabel().equals(name)) {
					return elem;
				}
			}
			return null;
		}
		private HashMap<Integer, CheckBoxFormElement> field_of_science;
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			
			out.write("<h2>Field of Science</h2>");
	
			
			out.write("<p>Select Field Of Science(s) applicable to this VO</p>");
			
			//sort the field_of_science by name and render
			TreeSet<CheckBoxFormElement> sorted = new TreeSet<CheckBoxFormElement>(new Comparator<CheckBoxFormElement>() {
				public int compare(CheckBoxFormElement o1,
						CheckBoxFormElement o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
			sorted.addAll(field_of_science.values());
			for(CheckBoxFormElement elem : sorted) {
				elem.render(out);
			}
		
			
			out.write("<table><tr><td>");
			new_fs.render(out);
			out.write("</td><td valign=\"bottom\">&nbsp;");
			add_fs.render(out);
			out.write("</td></tr></table>");

			out.write("<br/>");
			out.write("</div>");
		}	
	}
	private FieldOfScience field_of_science_de;
	
	class URLs extends DivRep
	{
		public URLs(DivRep _parent, VORecord rec) {
			super(_parent);
			
			new Static(this, "<h2>Relevant URLs</h2>");
			primary_url = new TextFormElement(this);
			primary_url.setLabel("Primary URL");
			primary_url.setValue(rec.primary_url);
			primary_url.addValidator(UrlValidator.getInstance());
			primary_url.setRequired(true);
			primary_url.setSampleValue("http://www-cdf.fnal.gov");

			aup_url = new TextFormElement(this);
			aup_url.setLabel("AUP URL");
			aup_url.setValue(rec.aup_url);
			aup_url.addValidator(UrlValidator.getInstance());
			aup_url.setRequired(true);
			aup_url.setSampleValue("http://www-cdf.fnal.gov");

			membership_services_url = new TextFormElement(this);
			membership_services_url.setLabel("Membership Services (VOMS) URL");
			membership_services_url.setValue(rec.membership_services_url);
			membership_services_url.addValidator(UrlValidator.getInstance());
			membership_services_url.setRequired(true);
			membership_services_url.setSampleValue("https://voms.fnal.gov:8443/voms/cdf/");

			purpose_url = new TextFormElement(this);
			purpose_url.setLabel("Purpose URL"); 
			purpose_url.setValue(rec.purpose_url);
			purpose_url.addValidator(UrlValidator.getInstance());
			purpose_url.setRequired(true);
			purpose_url.setSampleValue("http://www-cdf.fnal.gov");

			support_url = new TextFormElement(this);
			support_url.setLabel("Support URL"); 
			support_url.setValue(rec.support_url);
			support_url.addValidator(UrlValidator.getInstance());
			support_url.setRequired(true);
			support_url.setSampleValue("http://cdfcaf.fnal.gov");
		}

		public TextFormElement primary_url;
		public TextFormElement aup_url;
		public TextFormElement membership_services_url;
		public TextFormElement purpose_url;
		public TextFormElement support_url;
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			primary_url.render(out);
			aup_url.render(out);
			membership_services_url.render(out);
			purpose_url.render(out);
			support_url.render(out);
			out.write("<br/></div>");
		}
	}
	private URLs urls;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		6, //vo manager
		3, //admin contact       -- Formerly operations contact for VOs
		2, //security contact
		5, //misc contact
	};
	private HashMap<Integer, ContactEditor> contact_editors = new HashMap();
	
	public VOFormDE(Context _context, VORecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		new Static(this, "<h2>Basic VO Information</h2>");
		
		//new Static(this, "<h2>Sub-VO Mapping</h2>");
		new Static(this, "<p>Check below if this VO is a sub-VO of an existing VO. For example, FermilabMinos is a sub VO of the Fermilab VO.</p>");
		child_vo = new CheckBoxFormElement(this);
		child_vo.setLabel("This is a sub-VO");
		//indent the parent VO stuff
		new Static(this, "<div class=\"indent\">");
		//pull vos for unique validator
		HashMap<Integer, String> vos = getVONames();
		if(id != null) { //if doing update, remove my own name (I can use my own name)
			vos.remove(id);
		}
		parent_vo = new SelectFormElement(this, vos);
		parent_vo.setLabel("Select a Parent VO");
		hideParentVOSelector(true);
		child_vo.addEventListener(new EventListener() {
			public void handleEvent(Event e) {	
				if(((String)e.value).compareTo("true") == 0) {
					hideParentVOSelector(false);
				} else {
					hideParentVOSelector(true);
				}
			}
		});
		if(id != null) {
			VOModel model = new VOModel(context);
			VORecord parent_vo_rec = model.getParentVO(id);
			if(parent_vo_rec != null) {
				parent_vo.setValue(parent_vo_rec.id);
				child_vo.setValue(true);
				hideParentVOSelector(false);				
			}
			// AG: Need to clean this up; especially for VOs that are not child VOs of a parent
			// .. perhaps a yes/no first?
		}
		parent_vo.addEventListener(new EventListener () {
			public void handleEvent(Event e) {
				handleParentVOSelection(Integer.parseInt((String)e.value));
			}
		});
		new Static(this, "</div>");
		
		//new Static(this, "<p>Add/modify basic information about this VO</p>");

		// Name is not an editable field except for GOC staff
		name = new TextFormElement(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(vos.values()));
		name.setRequired(true);
		name.setSampleValue("CDF");
		if (auth.allows("admin")) {
			name.setDisabled(true);
		}

		long_name = new TextFormElement(this);
		long_name.setLabel("Enter the Long Name for this VO");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true); // TODO: agopu should this be required?
		long_name.setSampleValue("Collider Detector at Fermilab");

		sc_id = new SelectFormElement(this, getSCNames());
		sc_id.setLabel("Select a Support Center that will support this VO");
		sc_id.setValue(rec.sc_id);
		sc_id.setRequired(true);
		
		urls = new URLs(this, rec);

		new Static(this, "<h3>Extended Descriptions</h3>");
		description = new TextAreaFormElement(this);
		description.setLabel("Enter a Description for this VO");
		description.setValue(rec.description);
		description.setRequired(true);
		description.setSampleValue("Collider Detector at Fermilab");

		app_description = new TextAreaFormElement(this);
		app_description.setLabel("Enter an Application Description");
		app_description.setValue(rec.app_description);
		app_description.setRequired(true);
		app_description.setSampleValue("CDF Analysis jobs will be run");

		community = new TextAreaFormElement(this);
		community.setLabel("Describe the Community this VO serves");
		community.setValue(rec.community);
		community.setRequired(true);
		community.setSampleValue("The Collider Detector at Fermilab (CDF) experimental collaboration is committed to studying high energy particle collisions");
		
		field_of_science_de = new FieldOfScience(this, rec);

		new Static(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<VOContactRecord>> voclist_grouped = null;
		if(id != null) {
			VOContactModel vocmodel = new VOContactModel(context);
			ArrayList<VOContactRecord> voclist = vocmodel.getByVOID(id);
			voclist_grouped = vocmodel.groupByContactTypeID(voclist);
		} else {
			//set user's contact as submitter
			voclist_grouped = new HashMap<Integer, ArrayList<VOContactRecord>>();

			ArrayList<VOContactRecord> submitter_list = new ArrayList<VOContactRecord>();
			VOContactRecord submitter = new VOContactRecord();
			submitter.contact_id = auth.getContactID();
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			submitter_list.add(submitter);
			voclist_grouped.put(1/*submitter*/, submitter_list);
			
			// Should we make a function for these steps and call it 4 times? -agopu
			ArrayList<VOContactRecord> manager_list = new ArrayList<VOContactRecord>();
			VOContactRecord manager = new VOContactRecord();
			manager.contact_id = auth.getContactID();
			manager.contact_rank_id = 1;//primary
			manager.contact_type_id = 6;//manager
			manager_list.add(manager);
			voclist_grouped.put(6/*manager*/, manager_list);

			ArrayList<VOContactRecord> admin_contact_list = new ArrayList<VOContactRecord>();
			VOContactRecord primary_admin = new VOContactRecord();
			primary_admin.contact_id = auth.getContactID();
			primary_admin.contact_rank_id = 1;//primary
			primary_admin.contact_type_id = 3;//admin
			admin_contact_list.add(primary_admin);
			voclist_grouped.put(3/*admin*/, admin_contact_list);
		
			ArrayList<VOContactRecord> security_contact_list = new ArrayList<VOContactRecord>();
			VOContactRecord primary_security_contact= new VOContactRecord();
			primary_security_contact.contact_id = auth.getContactID();
			primary_security_contact.contact_rank_id = 1;//primary
			primary_security_contact.contact_type_id = 2;//security_contact
			security_contact_list.add(primary_security_contact);
			voclist_grouped.put(2/*security_contact*/, security_contact_list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		for(int contact_type_id : contact_types) {
			ContactEditor editor = createContactEditor(voclist_grouped, ctmodel.get(contact_type_id));
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type_id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			contact_editors.put(contact_type_id, editor);
		}

		// Handle reporting names
		new Static(this, "<h2>Reporting Names for your VO</h2>");
		ContactModel cmodel = new ContactModel (context);
		VOReportNameModel vorepname_model = new VOReportNameModel(context);
		VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(context);

		ArrayList<VOReportNameRecord> vorepname_records = vorepname_model.getAll();
		vo_report_name_div = new VOReportNames(this, vorepname_records, cmodel);
		if(id != null) {
			for(VOReportNameRecord vorepname_rec : vorepname_model.getAllByVOID(id)) {
				VOReportContactModel vorcmodel = new VOReportContactModel(context);
				Collection<VOReportContactRecord> vorc_list = vorcmodel.getAllByVOReportNameID(vorepname_rec.id);
				Collection<VOReportNameFqanRecord> vorepnamefqan_list = vorepnamefqan_model.getAllByVOReportNameID(vorepname_rec.id);
				vo_report_name_div.addVOReportName(vorepname_rec, vorepnamefqan_list, vorc_list);
			}
		} else {
			vo_report_name_div.addVOReportName(
					new VOReportNameRecord(), 
					new ArrayList<VOReportNameFqanRecord>(), 
					new ArrayList<VOReportContactRecord>()
			);		
		}
		if(auth.allows("admin")) {
			new Static(this, "<h2>Administrative Tasks</h2>");
		}
		footprints_id = new TextFormElement(this);
		footprints_id.setLabel("Footprints ID");
		footprints_id.setValue(rec.footprints_id);
		footprints_id.setRequired(true);
		if(!auth.allows("admin")) {
			footprints_id.setHidden(true);
		}

		active = new CheckBoxFormElement(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElement(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private void hideParentVOSelector(Boolean b)
	{
		parent_vo.setHidden(b);
		parent_vo.redraw();
	}

	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<VOContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new Static(this, "<h3>" + ctrec.name + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(voclist != null) {
			ArrayList<VOContactRecord> clist = voclist.get(ctrec.id);
			if(clist != null) {
				for(VOContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	private HashMap<Integer, String> getSCNames() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SCRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private HashMap<Integer, String> getVONames() throws AuthorizationException, SQLException
	{
		//pull all VOs
		VOModel model = new VOModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(VORecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	private void handleParentVOSelection(Integer parent_vo_id) {
		VOModel model = new VOModel (context);
		try {
			VORecord parent_vo_rec = model.get(parent_vo_id);
			
			if ((urls.primary_url.getValue() == null) || (urls.primary_url.getValue().length() == 0)) {
				urls.primary_url.setValue(parent_vo_rec.primary_url);
			}
			if ((urls.aup_url.getValue() == null) || (urls.aup_url.getValue().length() == 0)) {
				urls.aup_url.setValue(parent_vo_rec.aup_url);
			}
			if ((urls.membership_services_url.getValue() == null) || (urls.membership_services_url.getValue().length() == 0)) {
				urls.membership_services_url.setValue(parent_vo_rec.membership_services_url);
			}
			if ((urls.purpose_url.getValue() == null) || (urls.purpose_url.getValue().length() == 0)) {
				urls.purpose_url.setValue(parent_vo_rec.purpose_url);
			}
			if ((urls.support_url.getValue() == null) || (urls.support_url.getValue().length() == 0)) {
				urls.support_url.setValue(parent_vo_rec.support_url);
			}
			urls.redraw();
			
			if (sc_id.getValue() == null) {
				sc_id.setValue(parent_vo_rec.sc_id);
				sc_id.redraw();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Boolean doSubmit() 
	{
		VORecord rec = new VORecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.long_name = long_name.getValue();
		rec.description = description.getValue();
		rec.primary_url = urls.primary_url.getValue();
		rec.aup_url = urls.aup_url.getValue();
		rec.membership_services_url = urls.membership_services_url.getValue();
		rec.purpose_url = urls.purpose_url.getValue();
		rec.support_url = urls.support_url.getValue();
		rec.app_description = app_description.getValue();
		rec.community = community.getValue();
		rec.sc_id = sc_id.getValue();
		rec.footprints_id = footprints_id.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ArrayList<VOContactRecord> contacts = getContactRecordsFromEditor();
		
		ArrayList<Integer> field_of_science_ids = new ArrayList();
		for(Integer id : field_of_science_de.field_of_science.keySet()) {
			CheckBoxFormElement elem = field_of_science_de.field_of_science.get(id);
			if(elem.getValue()) {
				field_of_science_ids.add(id);
			}
		}
		
		Boolean ret = true;
		VOModel model = new VOModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, 
						contacts, 
						parent_vo.getValue(), 
						field_of_science_ids,
						vo_report_name_div.getVOReports());
			} else {
				model.updateDetail(rec, 
						contacts, 
						parent_vo.getValue(), 
						field_of_science_ids,
						vo_report_name_div.getVOReports());
			}
		} catch (Exception e) {
			alert(e.getMessage());
			log.error(e);
			ret = false;
		}
		context.close();
		return ret;
	}
	
	//retrieve contact records from the contact editor.
	//be aware that VOContactRecord's vo_id is not populated.. you need to fill it out with
	//appropriate vo_id later
	private ArrayList<VOContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<VOContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditor editor = contact_editors.get(type_id);
			HashMap<ContactRecord, Integer> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				VOContactRecord rec = new VOContactRecord();
				Integer rank_id = contacts.get(contact);
				rec.contact_id = contact.id;
				rec.contact_type_id = type_id;
				rec.contact_rank_id = rank_id;
				list.add(rec);
			}
		}
		
		return list;
	}
}