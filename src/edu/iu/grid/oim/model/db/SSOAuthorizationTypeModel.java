package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

import edu.iu.grid.oim.model.db.record.SSOAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;

public class SSOAuthorizationTypeModel extends SSOSmallTableModelBase<SSOAuthorizationTypeRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeModel.class);  
    
    public SSOAuthorizationTypeModel(UserContext context) 
    {
    	super(context, "contact_authorization_type_index");
    }
    SSOAuthorizationTypeRecord createRecord() throws SQLException
	{
		return new SSOAuthorizationTypeRecord();
	}
	public Collection<Integer> getAuthorizationTypesByDNID(Integer contact_authorization_type_id) throws SQLException
	{
	    System.out.println("from inside getAuthorizationTypesByDNID");

		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
		  
			SSOAuthorizationTypeRecord rec = (SSOAuthorizationTypeRecord)it;
			//System.out.println("inside getAuthorizationTypesByDNID contact_authorization_type_id - " + contact_authorization_type_id + "  --> " + rec.contact_authorization_type_id);
			if(rec.contact_authorization_type_id.compareTo(contact_authorization_type_id) == 0) {
			
			    	list.add(rec.authorization_type_id);
			    	}
		}
		
		System.out.println(list);
		return list;
	}
	public Collection<SSOAuthorizationTypeRecord> getAllByDNID(Integer contact_authorization_type_id) throws SQLException
	{
		ArrayList<SSOAuthorizationTypeRecord> list = new ArrayList();
		System.out.println("inside getAllByDNIN");

		for(RecordBase it : getCache()) 
		{
			SSOAuthorizationTypeRecord rec = (SSOAuthorizationTypeRecord)it;
			if(rec.contact_authorization_type_id.compareTo(contact_authorization_type_id) == 0) {
				list.add(rec);
				}
		}
		return list;		
	}
    public String getName()
    {
    	return "SSO Authorization Type";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("contact_authorization_type_id")) {
			SSOModel model = new SSOModel(context);
			SSORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.dn_string + ")";
		} else if(field_name.equals("authorization_type_id")) {
			AuthorizationTypeModel model = new AuthorizationTypeModel(context);
			AuthorizationTypeRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
}
