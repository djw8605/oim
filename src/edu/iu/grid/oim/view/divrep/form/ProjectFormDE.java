package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.FOSRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CampusGridModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.ProjectPublicationModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectPublicationRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.FOSEditor;
import edu.iu.grid.oim.view.divrep.ProjectPublicationEditor;
import edu.iu.grid.oim.view.divrep.form.validator.ProjectNameValidator;

public class ProjectFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ProjectFormDE.class); 
    static Integer vo_cg_offset = 10000;
   
    private UserContext context;
	private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	private DivRepTextBox organization;
	private DivRepTextBox department;
	private DivRepSelectBox parent;
	private ContactEditor pi;
	private FOSEditor field_of_science_de;
	private ProjectPublicationEditor publications;
	private Timestamp submit_time;
	private DivRepCheckBox disable;
	
	private DivRepTextArea comment;
	
	public ProjectFormDE(UserContext _context, ProjectRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		LinkedHashMap<Integer, String> project_names = getProjectNames();
		if(id != null) { //if doing update, remove my own name (I can use my own name)
			project_names.remove(id);
		}
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(project_names.values()));
		name.addValidator(new ProjectNameValidator());
		new DivRepStaticContent(this, "<p class=\"help-block\">* Leave it blank to autogenerate.</p>");

		description = new DivRepTextArea(this);
		description.setLabel("Description / Abstract of work");
		description.setValue(rec.desc);
		description.setRequired(true);
		description.setSampleValue("This project's goal is to ...");
		
		organization = new DivRepTextBox(this);
		organization.setLabel("Organization");
		organization.setValue(rec.organization);
		organization.setRequired(true);
		organization.setSampleValue("Indiana University");
		
		department = new DivRepTextBox(this);
		department.setLabel("Department");
		department.setValue(rec.department);
		department.setRequired(true);
		department.setSampleValue("Bioinformatics");

		//Combine VO & CG listing into 1 by adding some ID offset for CG (large enough to avoid ID conflict)
		parent = new DivRepSelectBox(this);
		parent.setLabel("Sponsor Virtual Organization / Campus Grid");
		
		LinkedHashMap<Integer, String> vokvs = new LinkedHashMap<Integer, String>();
		VOModel vomodel = new VOModel(context);
		ArrayList<VORecord> vos = vomodel.getAll();
		Collections.sort(vos, new Comparator<VORecord> (){
			public int compare(VORecord a, VORecord b) {
				return a.name.compareToIgnoreCase(b.name); // We are comparing based on name
			}
		});
		for(VORecord vorec : vos) {
			if(vomodel.canEdit(vorec.id)) {
				vokvs.put(vorec.id, vorec.name);
			}
		}
		parent.addGroup("Virtual Organization", vokvs);
		parent.setRequired(true);
		
		LinkedHashMap<Integer, String> cgkvs = new LinkedHashMap<Integer, String>();
		CampusGridModel cgmodel = new CampusGridModel(context);
		ArrayList<CampusGridRecord> cgs = cgmodel.getAll();
		Collections.sort(cgs, new Comparator<CampusGridRecord> (){
			public int compare(CampusGridRecord a, CampusGridRecord b) {
				return a.name.compareToIgnoreCase(b.name); // We are comparing based on name
			}
		});
		for(CampusGridRecord cgrec : cgs) {
			if(cgmodel.canEdit(cgrec.id)) {
				cgkvs.put(cgrec.id + vo_cg_offset, cgrec.name);
			}
		}
		parent.addGroup("CampusGrids", cgkvs);
		
		if(rec.vo_id != null) {
			parent.setValue(rec.vo_id);
		}
		if(rec.cg_id != null) {
			parent.setValue(rec.cg_id+vo_cg_offset);
		}
	
		ContactModel cmodel = new ContactModel(context);
		pi = new ContactEditor(this, cmodel, false, false);
		pi.setLabel("Principal Investigator");
		pi.setMinContacts(ContactRank.Primary, 1);
		pi.setShowRank(false);
		if(rec.id != null) {
			ContactRecord pi_contact = cmodel.get(rec.pi_contact_id);
			pi.addSelected(pi_contact, ContactRank.Primary);
		}
		new DivRepStaticContent(this, "<p class=\"help-block\">* If you can't find a PI, please register first at <a href=\"contactedit\" target=\"_blank\">New Contact</a>. You don't have to refersh this page after adding a new contact.</p>");
		
		ArrayList<Integer> fos_selected = new ArrayList<Integer>();
		if(rec.fos_id != null) {
			fos_selected.add(rec.fos_id);
		}
		
		new DivRepStaticContent(this, "<h3>Field Of Science</h3>");
		FieldOfScienceModel fmodel = new FieldOfScienceModel(context);
		field_of_science_de = new FOSEditor(this, fmodel, false);
		field_of_science_de.setMin(FOSRank.Primary, 1); //required
		field_of_science_de.setShowRank(false); //only primary.
		if(rec.id != null) {
			FieldOfScienceRecord fos = fmodel.get(rec.fos_id);
			field_of_science_de.addSelected(fos, FOSRank.Primary);	
		}
		new DivRepStaticContent(this, "<p class=\"help-block\">* If you can't find the field of science you are trying to enter, please <a href=\"https://ticket.opensciencegrid.org\" target='_blank'\">submit GOC ticket</a> and request to add a new field of science.</p>");
		
		publications = new ProjectPublicationEditor(this);
		if(rec.id != null) {
			ProjectPublicationModel ppmodel = new ProjectPublicationModel(context);
			for(ProjectPublicationRecord prec : ppmodel.getAllByProjectId(rec.id)) {
				publications.addPublication(prec);
			}
		}
		
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true); //I feel setDisable is better choice.. but to be consistent with other views.
		} 
	
		submit_time = rec.submit_time; //used to keep existing value
		
		comment = new DivRepTextArea(this);
		comment.setLabel("Update Comment");
		comment.setSampleValue("Please provide a reason for this update.");		
	}

	private LinkedHashMap<Integer, String> getProjectNames() throws AuthorizationException, SQLException
	{
		//pull all VOs
		ProjectModel model = new ProjectModel(context);
		ArrayList<ProjectRecord> recs = model.getAll();
		Collections.sort(recs, new Comparator<ProjectRecord> () {
			public int compare(ProjectRecord a, ProjectRecord b) {
				return a.name.compareToIgnoreCase(b.name);
			}
		});
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(ProjectRecord rec : recs) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	protected Boolean doSubmit() 
	{
		ProjectRecord rec = new ProjectRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.desc = description.getValue();
		rec.organization = organization.getValue();
		rec.department = department.getValue();
		ArrayList<ContactRecord> pis = pi.getContactRecordsByRank(ContactRank.Primary);
		ContactRecord pi = pis.get(0);
		rec.pi_contact_id = pi.id;
		if(parent.getValue() < vo_cg_offset) {
			rec.vo_id = parent.getValue();
			rec.cg_id = null;
		} else {
			rec.vo_id = null;
			rec.cg_id = parent.getValue() - vo_cg_offset;
		}
		rec.disable = disable.getValue();
		
		ArrayList<FieldOfScienceRecord> foss = field_of_science_de.getFOSRecordsByRank(FOSRank.Primary);
		rec.fos_id = foss.get(0).id;
		
		if(rec.name == null || rec.name.isEmpty()) {
			//autogenerate unique ID
			FieldOfScienceModel fosmodel = new FieldOfScienceModel(context);
			FieldOfScienceRecord fos;
			try {
				fos = fosmodel.get(rec.fos_id);
				String prefix = "OSG-" + fos.name.substring(0, 3).toUpperCase();
				Integer i = 100;
				ProjectModel pmodel = new ProjectModel(context);
				String pname;
				while(true) {
					pname = prefix + String.format( "%05d", i);
					i++;
					//make sure pname is unique
					ProjectRecord prec = pmodel.getByName(pname);
					if(prec == null) {
						break;
					}
					if(rec.id != null && rec.id.equals(prec.id)) {
						//collision with my own record is ok.
						break;
					}
				}
				rec.name = pname;
			} catch (SQLException e) {
				log.error("failed to find fos name", e);
				return false;
			}
		}

		context.setComment(comment.getValue());
				
		ProjectModel model = new ProjectModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, publications.getPublications());
				context.message(MessageType.SUCCESS, "Successfully created a new Project");
			} else {
				rec.submit_time = submit_time; //reset with existing value
				model.updateDetail(rec, publications.getPublications());
				context.message(MessageType.SUCCESS, "Successfully updated a Project.");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
			log.error("Failed to insert/update record", e);
			return false;
		}
	}
}
