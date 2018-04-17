package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

import edu.iu.grid.oim.model.db.record.SSORecord;
import edu.iu.grid.oim.model.db.SSOAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.SSOModel;
import javax.servlet.http.HttpSession;


public class ContactModel extends SmallTableModelBase<ContactRecord> {
    static Logger log = Logger.getLogger(ContactModel.class);  

    public ContactModel(UserContext _context) 
    {
    	super(_context, "contact");
    }
    public String getName()
    {
    	return "Contact";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("submitter_dn_id")) {
			DNModel model = new DNModel(context);
			if(value.equals(LogModel.NULL_TOKEN)) return value;
			DNRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.dn_string + ")";
		} else if(field_name.equals("service_id")) {
			ServiceModel model = new ServiceModel(context);
			ServiceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	ContactRecord createRecord() throws SQLException
	{
		return new ContactRecord();
	}
    
	public ContactRecord get(int id) throws SQLException {
		ContactRecord keyrec = new ContactRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ContactRecord> getAll() throws SQLException
	{
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();
		for(RecordBase it : getCache()) {
			list.add((ContactRecord)it);
		}
		return list;
	}
	public ContactRecord getEnabledByemail(String email) throws SQLException
	{
		for(RecordBase it : getCache()) {
			ContactRecord rec = (ContactRecord)it;
			if(rec.disable) continue;
			if(rec.primary_email != null && rec.primary_email.equals(email)) {
				return rec;
			}
			if(rec.secondary_email != null && rec.secondary_email.equals(email)) {
				return rec;
			}
		}
		return null;
	}
	
	public ArrayList<ContactRecord> getConfirmationExpiredPersonalContacts() throws SQLException
	{
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();
		for(ContactRecord it : getAll()) {
			if(it.person && it.disable == false && it.isConfirmationExpired()) {	
				list.add(it);
			}
		}
		return list;	
	}
	public ArrayList<ContactRecord> getAllNonDisabled() throws SQLException
	{
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();
		for(ContactRecord it : getAll()) {
			if(it.disable == false) {	
				list.add(it);
			}
		}
		return list;			
	}
	
	public ArrayList<ContactRecord> getAllEditable() throws SQLException
	{	   
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();

    	//only select record that is editable
	    for(RecordBase rec : getCache()) {
	    	ContactRecord crec = (ContactRecord)rec;
	    	if(canEdit(crec.id)) {
	    		list.add(crec);
	    	}
	    }	    	
	    return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
	    //   HttpSession session = request.getSession(false);
	    SSOModel dnmodel = new SSOModel(context);
	    
	    HashSet<Integer> list = new HashSet<Integer>();
		for(ContactRecord rec : getAll()) {
			//allow editing if user is submitter_dn
		    //System.out.println("************* submitter DN ID " + rec.id + " -- " +auth.getContactID() );
		    //if(rec.submitter_dn_id != null && rec.submitter_dn_id.compareTo(auth.getDNID()) == 0)  {
		    if( rec.id.equals(auth.getContactID()))  {

				//only allow editing if the contact is not yet associated with any enabled DN
				ArrayList<SSORecord> dnrecs = dnmodel.getEnabledByContactID(rec.id);
				if(dnrecs.size() == 0) {
					list.add(rec.id);
				}
		    }
		}
		return list;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		return canEdit(id);
	}
	public boolean canEdit(int vo_id)
	{
		if(auth.allows("admin")) return true;
		if(auth.allows("admin_contacts")) return true;
		
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(vo_id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
	public String generateTwikiID(String full_name, ContactRecord rec_ignore) throws SQLException 
	{
		StringBuffer twiki_id = new StringBuffer();
		
		//create twiki name from name
		String tokens[] = full_name.split(" ");
		for(String token : tokens) {
			token = token.trim().toLowerCase();
			if(token.length() == 0) continue;
			
			//capitalize the first char
			char ch = Character.toUpperCase(token.charAt(0));
			twiki_id.append(ch);
			twiki_id.append(token.substring(1));
		}
		
		//find any collision - if there is, iterate until no collision
		String unused_twiki_id = twiki_id.toString();
		int count = 2; //start with #2 (because #1 already exist)
		while(isTWikiIDExist(unused_twiki_id, rec_ignore)) {
			unused_twiki_id = twiki_id.toString() + count++;
		}
		
		return unused_twiki_id;
	}
	
	public Boolean isTWikiIDExist(String twikiid, ContactRecord rec_ignore) throws SQLException {
		for(RecordBase it : getCache()) {
			ContactRecord crec = (ContactRecord)it;
			if(crec == rec_ignore) continue;
			if(crec.disable) continue;//ignore disabled
			if(crec.twiki_id.equals(twikiid)) {
				return true;
			}
		}
		return false;
	}
	
	//NO-AC
	public void resetCertsDailyCount() throws SQLException {
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    stmt.execute("UPDATE "+table_name+" SET `count_hostcert_day` = 0;");	
	    stmt.close();
	    conn.close();
	}
	
	//NO-AC
	public void resetCertsYearlyCount() throws SQLException {
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    stmt.execute("UPDATE "+table_name+" SET `count_hostcert_year` = 0, `count_usercert_year` = 0");	
	    stmt.close();
	    conn.close();
	}
	public ArrayList<ContactRecord> getBySubmitterDNID(Integer id) throws SQLException {
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();
	    for(RecordBase rec : getCache()) {
	    	ContactRecord crec = (ContactRecord)rec;
	    	if(crec.submitter_dn_id != null && crec.submitter_dn_id.equals(id)) {
	    		list.add(crec);
	    	}
	    }	    	
	    return list;
	}
}
