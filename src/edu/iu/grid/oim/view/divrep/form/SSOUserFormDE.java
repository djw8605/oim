package edu.iu.grid.oim.view.divrep.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;

import edu.iu.grid.oim.model.db.SSOAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.SSOModel;

import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;


import edu.iu.grid.oim.model.db.record.SSOAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;

import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class SSOUserFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(UserFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
    public static  int id;
    String ids;
    
    private DivRepTextBox dn_string;
    private ContactEditor contact;
    private HashMap<Integer/*auth_type*/, DivRepCheckBox> auth_types = new HashMap();
    private DivRepCheckBox disable;
    
    public SSOUserFormDE(UserContext _context,SSORecord rec, String ids, String origin_url) throws AuthorizationException, SQLException
    {	
	super(_context.getPageRoot(), origin_url);
	context = _context;
	
	auth = context.getAuthorization();
	
	System.out.println("rec.id = " +ids );
	id = Integer.parseInt(ids);
	
        System.out.println("rec.id integer = " +rec.id );

	SSOModel cmodel = new SSOModel(context);
	SSORecord crec = cmodel.get(id);
	//contact.addSelected(crec, 1);//1 = is for primary (I know.. the api is not consistent with setMinContact() above)
	//}

	new DivRepStaticContent(this, "<h3> <span color='red'>"+crec.given_name+" "+crec.family_name+"</span> "+id+"</h3>");

	//new DivRepStaticContent(this, "<h3>"+crec.name+"<h3>");
	
	new DivRepStaticContent(this, "<h3>Authorization Types</h3>");
	AuthorizationTypeModel atmodel = new AuthorizationTypeModel(context);
	SSOAuthorizationTypeModel dnatmodel = new SSOAuthorizationTypeModel(context);
	for(AuthorizationTypeRecord atrec : atmodel.getAll()) {
	    if(atrec.id == 0) continue; //ignore guest
	    DivRepCheckBox elem = new DivRepCheckBox(this);
	    elem.setLabel(atrec.name);
	    auth_types.put(atrec.id, elem);
	}
	if(id != 0) {
	    //   System.out.println("id integer not null: " +id );

	    Collection<Integer> dnatrecs = dnatmodel.getAuthorizationTypesByDNID(id);
	    for(Integer auth_type : dnatrecs) {
		auth_types.get(auth_type).setValue(true);
	    }
	}
	new DivRepStaticContent(this, "<h3>Administration</h3>");
	disable = new DivRepCheckBox(this);
	disable.setLabel("Disable");
	Boolean status_flag = false;
        System.out.println("sso record status change: "+crec.disabled);

	if (crec.disabled>0){
            status_flag = true;
	}

	System.out.println("sso record status change: "+status_flag);
	disable.setValue(status_flag);
	
    }   
	
    protected Boolean doSubmit() 
    {
	//SSORecord rec = new SSORecord();

	//rec.id = id;
	//rec.disable = disable.getValue();
        Boolean status_value = disable.getValue(); 
	System.out.println("ID in doSubmit: " + id );
        System.out.println("IDs in doSubmit: " +ids );

	//rec.email = dn_string.getValue();
	//rec.disable = disable.getValue();
	System.out.println("this is a disable variable"+disable.getValue());
	//just grab first contact record (always one contact per one dn)
	//Collection<ContactRecord> contact_recs = contact.getContactRecords().keySet();
	//for(ContactRecord crec : contact_recs) {
	//    rec.contact_id = crec.id;
	//    break;
	//}
	
	ArrayList<Integer/*auth_type*/> auths = new ArrayList();
	for(Integer auth_type : auth_types.keySet()) {
	    DivRepCheckBox elem = auth_types.get(auth_type);
	    if(elem.getValue()) {
		auths.add(auth_type);
	    }
	    
	}

	//Do insert / update to our DB
	try {
	    auth.check("admin");
	    
	    SSOModel model = new SSOModel(context);
	    //if(id == null) {
	    //	    model.insertSSOprivs(id, auths);
	    //	    context.message(MessageType.SUCCESS, "Successfully assigned privileges to a contact record.");
		
		//create footprint ticket
		//Footprints fp = new Footprints(context);
	    //	} else {
	       System.out.println(auths.toString());

	       model.updateSSOprives(id, auths);
               model.updateSSOstatus(id, status_value);

	       context.message(MessageType.SUCCESS, "Successfully updated a contact record authorization.");
		    //	}

	    return true;
	} catch (Exception e) {
	    log.error(e);
	    alert(e.getMessage());
	    return false;
	}
    }
    
    @Override
	protected void onEvent(DivRepEvent e) {
	// TODO Auto-generated method stub
	
    }
}
