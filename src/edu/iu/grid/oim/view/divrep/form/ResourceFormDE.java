package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceServiceDetailModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOResourceOwnershipModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.divrep.AUPConfirmation;
import edu.iu.grid.oim.view.divrep.Confirmation;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.URLListEditor;
import edu.iu.grid.oim.view.divrep.ResourceGroupSelector;
import edu.iu.grid.oim.view.divrep.ResourceServices;
import edu.iu.grid.oim.view.divrep.ResourceWLCG;
import edu.iu.grid.oim.view.divrep.VOResourceOwnership;

public class ResourceFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ResourceFormDE.class); 
   
    private UserContext context;
    
	protected Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	private DivRepTextBox fqdn;
	private DivRepTextBox url;
	
	private DivRepCheckBox active;
	private DivRepCheckBox disable;
	private ResourceGroupSelector resource_group_id;
	private URLListEditor aliases;
	private ResourceServices resource_services;
	private VOResourceOwnership owners;

	private DivRepCheckBox wlcg;
	private ResourceWLCG wlcg_section;
	
	private Confirmation confirmation;
	
	private DivRepTextArea comment;
	
	static public ArrayList<ContactTypeRecord.Info> ContactTypes;
	static {
		ContactTypes = new ArrayList<ContactTypeRecord.Info>();
		ContactTypes.add(new ContactTypeRecord.Info(1, "A contact who has registered this resource"));
		ContactTypes.add(new ContactTypeRecord.Info(9, "Gratia-accounting based resource-usage reports are sent out daily/weekly to these contacts"));
		ContactTypes.add(new ContactTypeRecord.Info(3, "GOC tickets when assigned to a support center of a resource are also sent to the primary resource admin contact (usually the sysadmin of a resource) via the ticket's CC mechanism. Admin contacts are also contacted by GOC staff and others to deal with system-administrative problems"));
		ContactTypes.add(new ContactTypeRecord.Info(2, "Security notifications sent out by the OSG security team are sent to primary and secondary security contacts for this resource"));
		ContactTypes.add(new ContactTypeRecord.Info(5, "Contacts who do not fall under any of the above types but would like to be able to edit this resource can be added as miscellaneous contact"));
	}
	
	private HashMap<Integer, ContactEditor> contact_editors = new HashMap();
	
	public ResourceFormDE(UserContext _context, ResourceRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		new DivRepStaticContent(this, "<h2>Resource Information</h2>");

		//pull vos for unique validator
		HashMap<Integer, String> resources = getResources();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resources.remove(id);
		}
		
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(resources.values()));
		name.setRequired(true);
		name.setSampleValue("Indiana_Sample_CE");
		
		fqdn = new DivRepTextBox(this);
		fqdn.setLabel("Fully Qualified Domain Name (FQDN) of this resource");
		fqdn.setValue(rec.fqdn);
		fqdn.addValidator(new DivRepUniqueValidator<String>(resources.values()));
		fqdn.setRequired(true);
		fqdn.setSampleValue("gate01.sample.edu");

		resource_group_id = new ResourceGroupSelector(this, context);
		resource_group_id.setRequired(true);
		if(rec.resource_group_id != null) {
			resource_group_id.setValue(rec.resource_group_id);
		}

		description = new DivRepTextArea(this);
		description.setLabel("Short Description");
		description.setValue(rec.description);
		description.setRequired(true);
		description.setSampleValue("This is a hidden gatekeeper accessible from the FermiGrid site only. It reports to Gratia and RSV directly but offsite jobs can only get to it via the FermiGrid job gateway fermigridosg1");
				
		url = new DivRepTextBox(this);
		url.setLabel("Information URL");
		url.setValue(rec.url);
		url.addValidator(DivRepUrlValidator.getInstance());
		url.setRequired(true);
		url.setSampleValue("http://sample.edu/information");
		
		new DivRepStaticContent(this, "<h3>Resource FQDN Aliases (If Applicable)</h3>");
		new DivRepStaticContent(this, "<p>If you used a DNS alias as their main gatekeeper or SE head node FQDN (as defined above), then you can add real host name(s) here as reverse alias(es).</p>");
		aliases = new URLListEditor (this);
		ResourceAliasModel ramodel = new ResourceAliasModel(context);
		if(id != null) {
			for(ResourceAliasRecord rarec : ramodel.getAllByResourceID(id)) {
				aliases.addUrl(rarec.resource_alias);
			}
		}
		
		new DivRepStaticContent(this, "<h2>Resource Services</h2>");
		//new DivRepStaticContent(this, "<p>Add, remove, modify services associated with your resource. For example, a CE or an SRM.</p>");
		ServiceModel smodel = new ServiceModel(context);
		resource_services = new ResourceServices(this, context/*, smodel.getAll()*/);

		ResourceServiceModel rsmodel = new ResourceServiceModel(context);
		if(id != null) {
			ResourceServiceDetailModel rsdmodel = new ResourceServiceDetailModel(context);
			ArrayList<ResourceServiceDetailRecord> details = rsdmodel.getAllByResourceID(id);
			for(ResourceServiceRecord rarec : rsmodel.getByResourceID(id)) {
				resource_services.addService(rarec, details);
			}
		} else {
			//add new one
			resource_services.addService(new ResourceServiceRecord(), null);
		}
		resource_services.setRequired(true);

		// Resource ownership stuff
		new DivRepStaticContent(this, "<h2>Resource Ownership</h2>");
		//new DivRepStaticContent(this, "<p>Add/modify VO ownership of this resource.</p>");
		VOModel vo_model = new VOModel(context);
		owners = new VOResourceOwnership(this, vo_model.getAll());
		VOResourceOwnershipModel voresowner_model = new VOResourceOwnershipModel(context);
		if(id != null) {
			for(VOResourceOwnershipRecord voresowner_rec : voresowner_model.getAllByResourceID(id)) {
				owners.addOwner(voresowner_rec);
			}
		} else {
			//add new one
			owners.addOwner(new VOResourceOwnershipRecord());
		}

		ToolTip tip = new ToolTip("Add, remove, modify various types of contacts associated with your resource. These contacts have the authorization to modify this resource. Each contact entry field shows you a list of contacts as you type a name.");
		new DivRepStaticContent(this, "<h2>Contact Information "+tip.render()+"</h2>");
		//new DivRepStaticContent(this, "<p>Add, remove, modify various types of contacts associated with your resource. These contacts have the authorization to modify this resource. Each contact entry field shows you a list of contacts as you type a name.</p>");
		
		HashMap<Integer/*contact_type_id*/, ArrayList<ResourceContactRecord>> voclist_grouped = null;
		if(id != null) {
			ResourceContactModel vocmodel = new ResourceContactModel(context);
			ArrayList<ResourceContactRecord> voclist = vocmodel.getByResourceID(id);
			voclist_grouped = vocmodel.groupByContactTypeID(voclist);
		} else {
			//set user's contact as submitter
			voclist_grouped = new HashMap<Integer, ArrayList<ResourceContactRecord>>();
			
			//prepopulate submitter
			ArrayList<ResourceContactRecord> submitter_list = new ArrayList<ResourceContactRecord>();
			ResourceContactRecord submitter = new ResourceContactRecord();
			submitter.contact_id = auth.getContact().id;
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			submitter_list.add(submitter);
			voclist_grouped.put(1/*submitter*/, submitter_list);
			
			//prepopulatee admin contact
			ArrayList<ResourceContactRecord> admin_list = new ArrayList<ResourceContactRecord>();
			ResourceContactRecord admin = new ResourceContactRecord();
			admin.contact_id = auth.getContact().id;
			admin.contact_rank_id = 1;//primary
			admin.contact_type_id = 3;//admin
			admin_list.add(admin);
			voclist_grouped.put(3/*admin*/, admin_list);
		
			//security contact
			ArrayList<ResourceContactRecord> security_list = new ArrayList<ResourceContactRecord>();
			ResourceContactRecord security = new ResourceContactRecord();
			security.contact_id = auth.getContact().id;
			security.contact_rank_id = 1;//primary
			security.contact_type_id = 2;//security
			security_list.add(security);
			voclist_grouped.put(2/*security*/, security_list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		for(ContactTypeRecord.Info contact_type : ContactTypes) {
			tip = new ToolTip(contact_type.desc);
			ContactEditor editor = createContactEditor(voclist_grouped, ctmodel.get(contact_type.id), tip);
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type.id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			if(contact_type.id != 5 && contact_type.id != 9) { //5 = misc, 9 = resource report
				editor.setMinContacts(ContactRank.Primary, 1);
			}
			if(contact_type.id == 2 || contact_type.id == 3) {
				editor.setMaxContacts(ContactRank.Secondary, 3);
			}
			contact_editors.put(contact_type.id, editor);
		}
		
		new DivRepStaticContent(this, "<h2>WLCG Interoperability Information (If Applicable)</h2>");
		//new DivRepStaticContent(this, "<p>By enabling this section you agree to publish Resource Information (GLUE via BDII), Monitoring Data (RSV), and Accounting Data (OIM Capacity and Benchmarking) to the WLCG and/or EGI.</p>");

		wlcg = new DivRepCheckBox(this);
		wlcg.setLabel("This is a WLCG resource");

		//indent the whole WCLG things
		//new DivRepStaticContent(this, "<div class=\"divrep_indent\">");
		wlcg_section = new ResourceWLCG(this, context, null);
		hideWLCGElement(true);

		wlcg.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {	
				if(((String)e.value).compareTo("true") == 0) {
					hideWLCGElement(false);
				} else {
					hideWLCGElement(true);
				}
			}
		});

		if(id != null) {
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			ResourceWLCGRecord wrec = wmodel.get(rec.id);
			if(wrec != null) {
				wlcg.setValue(true);
				wlcg_section.setWlcgRecord(wrec);
				hideWLCGElement(false);				
			}
		}
		
		new DivRepStaticContent(this, "<h2>Confirmation</h2>");
		confirmation = new Confirmation(this, rec, auth);

		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}
		active = new DivRepCheckBox(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
		
		if(id == null) {
			AUPConfirmation aup = new AUPConfirmation(this);
		}
		
		comment = new DivRepTextArea(this);
		comment.setLabel("Update Comment");
		comment.setSampleValue("Please provide a reason for this update.");
		
		submitbutton.addClass("btn");
	}
	
	private void hideWLCGElement(Boolean b)
	{
		wlcg_section.setHidden(b);
		wlcg_section.redraw();
	}
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<ResourceContactRecord>> voclist, ContactTypeRecord ctrec, ToolTip tip) throws SQLException
	{
		new DivRepStaticContent(this, "<h3>" + StringEscapeUtils.escapeHtml(ctrec.name) + " " + tip.render() + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(voclist != null) {
			ArrayList<ResourceContactRecord> clist = voclist.get(ctrec.id);
			if(clist != null) {
				for(ResourceContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
		//Construct VORecord
		ResourceRecord rec = new ResourceRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.fqdn = fqdn.getValue();
		rec.url = url.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		rec.resource_group_id = resource_group_id.getValue();
		rec.confirmed = confirmation.getTimestamp();
		
		//If WLCG is on, then create wlcg record
		ResourceWLCGRecord wrec = null;
		if(wlcg.getValue()) {
			wrec = wlcg_section.getWlcgRecord();
		}
		
		context.setComment(comment.getValue());
		
		ResourceModel model = new ResourceModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, 
						aliases.getURLs(), 
						getContactRecordsFromEditor(), 
						wrec,
						resource_services.getResourceServiceRecords(),
						resource_services.getResourceServiceDetailsRecords(),
						owners.getOwners());
				context.message(MessageType.SUCCESS, "Successfully registered new resource. A ticket has been generated and OSG Operations will alert you when they have reviewed and activated your resource.");
				
				try {
					//Traverse OIM hirearchy to find the Footprint ID of the associated SC
					ResourceGroupModel rgmodel = new ResourceGroupModel(context);
					ResourceGroupRecord rgrec = rgmodel.get(rec.resource_group_id);
					SiteModel smodel = new SiteModel(context);
					SiteRecord srec = smodel.get(rgrec.site_id);
					SCModel scmodel = new SCModel(context);
					SCRecord screc = scmodel.get(srec.sc_id);
					
					//create footprint ticket
					Footprints fp = new Footprints(context);
					fp.createNewResourceTicket(rec.name, screc);//, vo_name);
				} catch (Exception fpe) {
					log.error("Failed to open footprints ticket: ", fpe);
				}
			} else {
				model.updateDetail(rec, 
						aliases.getURLs(), 
						getContactRecordsFromEditor(),
						wrec,
						resource_services.getResourceServiceRecords(),
						resource_services.getResourceServiceDetailsRecords(),
						owners.getOwners());
				context.message(MessageType.SUCCESS, "Successfully updated a resource.");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
			log.error("Failed to insert/update record", e);
			return false;
		}
	}
	
	//retrieve contact records from the contact editor.
	//be aware that VOContactRecord's vo_id is not populated.. you need to fill it out with
	//appropriate vo_id later
	private ArrayList<ResourceContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<ResourceContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditor editor = contact_editors.get(type_id);
			HashMap<ContactRecord, ContactRank> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				ResourceContactRecord rec = new ResourceContactRecord();
				ContactRank rank = contacts.get(contact);
				rec.contact_id = contact.id;
				rec.contact_type_id = type_id;
				rec.contact_rank_id = rank.id;
				list.add(rec);
			}
		}
		
		return list;
	}
	
	private HashMap<Integer, String> getResources() throws SQLException
	{
		ResourceModel model = new ResourceModel(context);
		HashMap<Integer, String> resources = new HashMap();
		for(ResourceRecord rec : model.getAll()) {
			resources.put(rec.id, rec.name);
		}
		return resources;
	}
	
	public Boolean isValidResourceFQDN(String url)
	{
		if(fqdn.getValue().equals(url)) return true;
		ArrayList<String> as = aliases.getURLs();
		if(as.contains(url)) return true;
		
		return false;
	}
}
