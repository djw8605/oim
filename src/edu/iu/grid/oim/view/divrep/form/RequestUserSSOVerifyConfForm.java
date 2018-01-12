package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepReCaptcha;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepToggler;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIValidator;
import com.divrep.common.DivRepButton;


import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SSOModel;

import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;

import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.UserCNEditor;
import edu.iu.grid.oim.view.divrep.ChoosePassword;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;

import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.Footprints.FPTicket;


public class RequestUserSSOVerifyConfForm extends DivRepForm
{
    static Logger log = Logger.getLogger(RequestUserSSOVerifyConfForm.class);
    private UserContext context;
    private Authorization auth;
    
    //subscriber identify
    private DivRepTextBox fullname;
    private DivRepTextBox email;
    private DivRepTextBox phone;
    private DivRepTextBox city, state, country, zipcode;
    private DivRepSelectBox timezone;
    private HashMap<Integer, String> timezone_id2tz;
    
    private UserCNEditor cn; //only for OIM user
    private DivRepSelectBox sponsor;
    
    private SponsorManual sponsor_manual;//used when manually specified
    
    private DivRepTextArea request_comment;
    
    private DivRepTextArea profile;
    private DivRepCheckBox use_twiki;
    private DivRepTextBox twiki_id;
    
    private ChoosePassword choose_password;
    //private DivRepCheckBox agreement;
    
    private DivRepSelectBox vo;
    private String sso_id;
    class SponsorManual extends DivRepFormElement {
		
	//sponsor info manually selected
	protected DivRepTextBox sponsor_fullname;
	protected DivRepTextBox sponsor_email;
	
	public SponsorManual(DivRep parent) {
	    super(parent);
	    
	    sponsor_fullname = new DivRepTextBox(this);
	    sponsor_fullname.setLabel("Full Name");
	    sponsor_fullname.setRequired(true);
	    sponsor_fullname.addValidator(new CNValidator(CNValidator.Type.USER)); //WHY??
	    
	    sponsor_email = new DivRepTextBox(this);
	    sponsor_email.setLabel("Email");
	    sponsor_email.setRequired(true);
	    sponsor_email.addValidator(new DivRepEmailValidator());
	}
		
		@Override
		protected void onEvent(DivRepEvent event) {
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			if(!isHidden()) {
				out.write("<h3>Sponsor Detail</h3>");
				sponsor_fullname.render(out);
				sponsor_email.render(out);
			}
			out.write("</div>");
		}
		
		@Override
		public void setRequired(Boolean b) {
			sponsor_fullname.setRequired(b);
			sponsor_email.setRequired(b);
		}
		public ContactRecord getRecord() {
			ContactRecord rec = new ContactRecord();
			rec.name = sponsor_fullname.getValue();
			rec.primary_email = sponsor_email.getValue();
			return rec;
		}
 	}
	
	class DuplicateEmailValidator implements DivRepIValidator<String> {
		private ArrayList<DNRecord> dnrecs = null;
		private ContactRecord owner = null;
		
		@Override
		public Boolean isValid(String email) {
			ContactModel model = new ContactModel(context);
			dnrecs = null;
			owner = null;
			try {
				owner = model.getEnabledByemail(email);
				if(owner == null) {
					//not such email
					return true;
				} else {
					DNModel dnmodel = new DNModel(context);
					dnrecs = dnmodel.getEnabledByContactID(owner.id);
					if(dnrecs.size() == 0) {
						//registered to contact with no dn - user can take over
						dnrecs = null;
						return true;
					}
					
					//we have conflict
					return false;
				}
			} catch (SQLException e) {
				log.error("Failed to validate duplicate email", e);
			}
			return true;
		}

		@Override
		public String getErrorMessage() {
			StringBuffer msg = new StringBuffer();
			
			return msg.toString();
		}
	}
	
    public RequestUserSSOVerifyConfForm(final UserContext context, String origin_url, String id) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		sso_id = id;
		new DivRepStaticContent(this, "<h2>Your account needs to be verified to be allowed to modify any information on the website.</h2>");
				
			
		request_comment = new DivRepTextArea(this);
		request_comment.setLabel("Comments");
		request_comment.setSampleValue("Please enter any comments, or request you'd like to make for RA agents / Sponsors.");
		//RevokeButton button = new RevokeButton(context.getPageRoot(), i);
		//button.render(out);		
		//new DivRepStaticContent(this, "<h2>OSG Policy Agreement</h2>");
		
		//new CertificateAUPDE(this);
	}
	
	protected ArrayList<ContactRecord> getSponsor(Integer vo_id) throws SQLException {
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<ContactRecord> sponsors = new ArrayList<ContactRecord>();
		ArrayList<VOContactRecord> crecs = model.getByVOID(vo_id);
		for(VOContactRecord crec : crecs) {
			if(crec.contact_type_id.equals(12)) { //sponsors
				ContactRecord sponsor = cmodel.get(crec.contact_id);
				sponsors.add(sponsor);
			}
		}
		return sponsors;
	}
	protected Integer getPrimarySponsorId(Integer vo_id) throws SQLException {
		VOContactModel model = new VOContactModel(context);
		ArrayList<VOContactRecord> crecs = model.getByVOID(vo_id);
		for(VOContactRecord crec : crecs) {
			if(crec.contact_type_id.equals(12) && crec.contact_rank_id.equals(1)) { //primary sponsor
				return crec.contact_id;
			}
		}
		return null;
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Boolean doSubmit() {
	    //ContactRecord user;
		SSORecord user;
		//SSORecord requester;
		//requester = auth.getSSORecord();

		
		//do certificate request with no csr
		try {

		    Footprints fp = new Footprints(context);
		    FPTicket ticket = fp.new FPTicket();
		    SSORecord requester = auth.getSSORecord();
                    ContactRecord requester_contact = auth.getContact();

		    ticket.phone = requester_contact.primary_phone;
		    ticket.description = "" + request_comment.getValue();

		    //    String ticket_id = fp.open(ticket);
		    
		    SSOModel ssomodel = new SSOModel(context);
		    System.out.println("This is SSOID insdie doSubmit in RequestUserSSOVerifyConfForm: "+ sso_id);
		    String answer =  ssomodel.updateSSOverifyPair(requester_contact.id, sso_id);
		    
		    if(answer!=null){
			fp.update(ticket,answer);
			ssomodel.updateSSOverifyConfirmation(sso_id,1,requester_contact.id);
		    }else{
			context.message(MessageType.ERROR, "You are not authorized verify this account");

		    }
		    
		    context.message(MessageType.SUCCESS, "The account has bee successfully verified");
		    
		    return true;
		} catch (Exception e) {
		    log.error("Failed to submit request..", e);
		    alert("Sorry, failed to submit request: " + e.toString());
		    return false;
		}
	}
}
