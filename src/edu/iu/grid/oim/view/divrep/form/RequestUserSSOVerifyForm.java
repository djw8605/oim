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


public class RequestUserSSOVerifyForm extends DivRepForm
{
    static Logger log = Logger.getLogger(RequestUserSSOVerifyForm.class);
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
	

	public RequestUserSSOVerifyForm(final UserContext context, String origin_url) {
		
	    super(context.getPageRoot(), origin_url);
	    //Create ra & sponsor list                                                                                                                                                          
	    this.context = context;
	    auth = context.getAuthorization();
	    
		new DivRepStaticContent(this, "<h2>Your account needs to be verified to be allowed to modify any information on the website.</h2>");
				
		new DivRepStaticContent(this, "<h3>Sponsor</h3>");
		new DivRepStaticContent(this, "<p class=\"help-block\">Please select VO that should approve your request.</p>");
		DivRepToggler help = new DivRepToggler(this) {
			@Override
			public DivRep createContent() {
				return new DivRepStaticContent(this, 
					"<div class=\"well\">"+
					"<h4>Virtual Organization</h4>" +
					"<p class=\"\">If you do not know which VO to select, please open a <a target=\"_blank\" href=\"https://ticket.grid.iu.edu\">GOC Ticket</a> for an assistance.</p>" +
					"<p class=\"\">If your VO does not appear, it currently has no RA agents assigned to it. Please contact GOC or the VO managers.</p>" +
					"<p class=\"\">If you just need to access OSG secure web servers (OIM, DocDB, etc.), you may select the <b>MIS</b> VO.</p>" +
					"<h4>What is a sponsor?</h4>" +
					"A sponsor is a member of the VO who knows you and can vouch for your membership such as PI or supervisor." +
					"</div>");
			}
		};
		help.setShowHtml("<u class=\"pull-right\">Help me choose</u>");
		help.setHideHtml("");

		VOModel vo_model = new VOModel(context);
		VOContactModel model = new VOContactModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				//ignore disabled vo
				if(vo_rec.disable == true) {
					continue;
				}
				
				//check if the VO has at least 1 RA specified
				ArrayList<VOContactRecord> crecs = model.getByVOID(vo_rec.id);
				boolean hasra = false;
				for(VOContactRecord crec : crecs) {
					if(crec.contact_type_id.equals(11)) { //RA
						hasra = true;
						break;
					}
				}
				if(hasra) {
					keyvalues.put(vo_rec.id, vo_rec.name);
				}
			}
			vo = new DivRepSelectBox(this, keyvalues);
			vo.setLabel("Virtual Organization");
			vo.setRequired(true);
			vo.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent event) {
					//lookup sponsor and if there is one - then require user to select one
					try {
						if(vo.getValue() == null) {
							sponsor.setHidden(true);
							sponsor_manual.setHidden(true);
							sponsor_manual.setRequired(false);
						} else {
							sponsor.setHidden(true);
							ArrayList<ContactRecord> sponsors = getSponsor(vo.getValue());
							Collections.sort(sponsors, new Comparator<ContactRecord> () {
								public int compare(ContactRecord a, ContactRecord b) {
									return a.name.compareToIgnoreCase(b.name);
								}
							});
							LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
							for(ContactRecord sponsor : sponsors) {
								if(auth.isUser()) {
									keyvalues.put(sponsor.id, sponsor.name + " <" + sponsor.primary_email + ">");
								} else {
									keyvalues.put(sponsor.id, sponsor.name);
								}
							}
							sponsor.setValues(keyvalues);
							sponsor.setHidden(false);	
							if(sponsors.size() > 0) {
								
								//select primary sponsor if available
								Integer primary_sponsor_id = getPrimarySponsorId(vo.getValue());
								if(primary_sponsor_id != null) {
									sponsor.setValue(primary_sponsor_id);
								} else {
									//select 1st sponsor
									Integer first_id = sponsors.get(0).id;
									sponsor.setValue(first_id);
								}
								sponsor.setHidden(false);
								
								//no need for sponsor detail
								sponsor_manual.setHidden(true);
								sponsor_manual.setRequired(false);
							} else {
								sponsor.setHidden(true);
								sponsor_manual.setHidden(false);
								sponsor_manual.setRequired(true);
							}
						}
						sponsor.redraw();
						sponsor_manual.redraw();
					} catch (NumberFormatException e) {
						log.error("Failed to parse vo ID for "+ event.value, e);
					} catch (SQLException e) {
						log.error("Failed to lookup vo  for ID:"+ event.value, e);
					}
				}
			});
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
		
		sponsor = new DivRepSelectBox(this);
		sponsor.setLabel("Sponsor");
		sponsor.setNullLabel("(Manually Specify)");
		sponsor.setHidden(true);
		sponsor.addEventListener(new DivRepEventListener() {
			@Override
			public void handleEvent(DivRepEvent event) {
				if(sponsor.getValue() == null) {
					sponsor_manual.setHidden(false);
					sponsor_manual.setRequired(true);
				} else {
					sponsor_manual.setHidden(true);
					sponsor_manual.setRequired(false);
				}
				
				sponsor_manual.redraw();
			}
		});
		
		sponsor_manual = new SponsorManual(this);
		sponsor_manual.setHidden(true);
		sponsor_manual.setRequired(false);
		
		request_comment = new DivRepTextArea(this);
		request_comment.setLabel("Comments");
		request_comment.setSampleValue("Please enter any comments, or request you'd like to make for RA agents / Sponsors.");
		
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
		Integer vo_id= vo.getValue();
                
		//System.out.println("VO name"+vo_name);
		String ranames;
		//SSORecord requester;
		//requester = auth.getSSORecord();
		
		String voranames = "";                                                                                                                                                       
		VOContactModel vomodel = new VOContactModel(context);                                                                                                                         
		ContactModel vocmodel = new ContactModel(context);                                                                                                                            
		ArrayList<VOContactRecord> vocrecs;
                Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		SSORecord requester = auth.getSSORecord();
		ContactRecord requester_contact = auth.getContact();
		// Integer vo_id= vo.getValue();                                                                                                                                            


		try{ 
		    try {         
			vocrecs = vomodel.getByVOID(vo_id);                                                                                                                                  
       			for(VOContactRecord vocrec : vocrecs) {                                                                                                                               
    
			    ContactRecord vocontactrec = vocmodel.get(vocrec.contact_id);                                                                                                     
     
			    if(vocrec.contact_type_id.equals(11)) { //11 - ra                                                                                                                 
       
				ticket.ccs.add(vocontactrec.primary_email);                                                                                                                   
  
				System.out.println("vo: "+ vocontactrec.primary_email);
				if(voranames.length() != 0) {                                                                                                                                 
				    voranames += ", ";                                                                                                                                        
				}                                                                                                                                                     
				voranames += vocontactrec.name;                                                                                                                               
			    }  
			}                                                                                                                                                                    
     
		    } catch (SQLException e1) {                                                                                                                                           
			log.error("Failed to lookup RA information - ignoring", e1);                                                                                                         
     
		    }                                                                                                                                                                        

		    if(sponsor_manual.getValue()!=null){
			System.out.println("This is manual sponsor information"+sponsor_manual.getValue());
			//ticket.ccs.add(sponsor_manual.getValue());

		    }
         
		    System.out.println("vornames: " + voranames);
		    System.out.println("vornames 1: " + voranames);
		    
		    /*
		      
		      ArrayList<ContactRecord> emails_sponsors = getSponsor(vo.getValue());
		      
		      for(ContactRecord email_sponsor : emails_sponsors) {
		      System.out.println("sponsor_email"+email_sponsor.primary_email);
		      
		      }
		      }*/
		
		    //sponsor
		    ContactRecord sponsor_rec = null;
		    if(sponsor.getValue() != null) {
			ContactModel cmodel = new ContactModel(context);
			try {
			    sponsor_rec = cmodel.get(sponsor.getValue());
			} catch (SQLException e) {
			    log.error("Failed to lookup contact with id: " + sponsor.getValue());
			    alert(e.getMessage());
			    return false;
			}
		    } else {
			//sponsor not selected from drop down - use manual sponsor
			sponsor_rec = sponsor_manual.getRecord();
		    }
		    
		    /*    
		    Footprints fp = new Footprints(context);
		    FPTicket ticket = fp.new FPTicket();
		    SSORecord requester = auth.getSSORecord();
                    ContactRecord requester_contact = auth.getContact();
		    // Integer vo_id= vo.getValue();
		    */
		    //check 

		    String ticket_id_existing = requester.ticket_id;

		    ticket.description = "Dear "+voranames+", \n\n An unverified user; "+ requester.given_name +" " +requester.family_name+"  <"+requester.email+"> has requested an account approval. Please determine this request's authenticity, and approve / disapprove at https://oim-dev1.grid.iu.edu/oim/"+"requestuserssoverifyconfirmation?id="+requester.id +"\n\n"+"User has selected a registered sponsor: "+sponsor_rec.name+" who has been CC-ed to this request. "+"\n\n" + request_comment.getValue();


		    if(ticket_id_existing == null){
			ticket.title = "SSO Verification Request from " + requester.given_name +" " +requester.family_name;
			ticket.name = requester.given_name +" " +requester.family_name;
			ticket.email = requester.email;
			ticket.phone = requester_contact.primary_phone;
			ticket.metadata.put("SUBMITTED_VIA","OIM/SSORequestForm");
			ticket.metadata.put("SUBMITTER_NAME", requester.given_name +" " +requester.family_name);
			ticket.metadata.put("ASSOCIATED_VO_ID", Integer.toString(vo_id));
			ticket.ccs.add(sponsor_rec.primary_email);
			ticket.ccs.add("osg-ra@opensciencegrid.org"); //TODO - make this configurable                                                                                                              
			ticket.nextaction = "OSG RA to process request";
			String ticket_id = fp.open(ticket);
			log.info("Opened SSO Verification Request ticket with ID:" + ticket_id);
			alert("Opened ticket ID:" + ticket_id);
			SSOModel ssomodel = new SSOModel(context);
			ssomodel.updateSSOverify(requester.id, sponsor_rec.id,ticket_id, vo_id);
		    }else{
			fp.update(ticket, ticket_id_existing);
		    }

		  

		    /*
		    	if(rec != null) {
				redirect("requestuserssoverify?id="+rec.id); //TODO - does this work? I haven't tested it
			}
		    */	
			context.message(MessageType.SUCCESS, "Successfully requested has been sent to RA admin");
			//redirect("/"); //TODO - does this work? I haven't tested it  

			return true;
		} catch (Exception e) {
			log.error("Failed to submit request..", e);
			alert("Sorry, failed to submit request: " + e.toString());
			return false;
		}
	}
}
