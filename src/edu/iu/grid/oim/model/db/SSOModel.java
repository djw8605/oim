package edu.iu.grid.oim.model.db;

import java.sql.*;
//import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import javax.servlet.http.HttpServletRequest;
//import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
//import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

import edu.iu.grid.oim.model.db.record.SSOAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.SSORecord;
import javax.servlet.http.HttpSession;


public class SSOModel extends SSOSmallTableModelBase<SSORecord> {
    static Logger log = Logger.getLogger(SSOModel.class);  
    
    public SSOModel(UserContext context) 
    {
    	super(context, "contact_authorization_type");
    }
    public String getName()
    {
    	return "SSO";
    }
    public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
	    if(field_name.equals("contact_id")) {
		ContactModel model = new ContactModel(context);
		ContactRecord rec = model.get(Integer.parseInt(value));
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
    SSORecord createRecord() throws SQLException
    {
	return new SSORecord();
    }
	
    //TODO - make this more efficient..
    public SSORecord getByEmail(String email) throws SQLException
    {
	String email_toLower = email.toString().toLowerCase();
	System.out.println("hello");
	if(email != null) {
	    for(RecordBase it : getCache()) 
		{
		    SSORecord rec = (SSORecord)it;
		    
		    
		    if(rec.email.toString().toLowerCase().compareTo(email_toLower) == 0 ) {
			
			System.out.println(rec.email+"<-- email from inside getbyEmail 2");
			return rec;
		    }
		    
		    if(rec.email1 != null){
			
			if(rec.email1.toString().toLowerCase().compareTo(email_toLower) == 0 ) {
			    System.out.println(rec.email+"<-- email from inside getbyEmail 3-1");
			    return rec;
			}
		    }
		    
		    if(rec.email2 != null){
			
			
			if(rec.email2.toString().toLowerCase().compareTo(email_toLower) == 0 ) {
			    System.out.println(rec.email+"<-- email from inside getbyEmail 4-1");
                                        return rec;
			}
		    }
		    
		    if(rec.email3 != null){
			if(rec.email3.toString().toLowerCase().compareTo(email_toLower) == 0 ) {
			    System.out.println(rec.email+"<-- email from inside getbyEmail 5");
			    return rec;
			}
		    }
		    
		}
	    System.out.println("inside getbyEmail");
	}
	return null;
    }
    
    //TODO - make this more efficient..
    public SSORecord getEnabledByDNString(String dn_string) throws SQLException
    {
	if(dn_string != null) {
	    for(RecordBase it : getCache()) 
		{
		    SSORecord rec = (SSORecord)it;
		    if(rec.dn_string.compareTo(dn_string) == 0 && rec.disable == false) {
			return rec;
		    }
		}
	}
	System.out.println("inside getEnabledByDNString");
	return null;
    }
	
    public ArrayList<SSORecord> getByContactID(int contact_id) throws SQLException
    {
	ArrayList<SSORecord> list = new ArrayList<SSORecord>();
	for(RecordBase it : getCache()) 
	    {
		SSORecord rec = (SSORecord)it;
		if(rec.contact_id.equals(contact_id)) {
		    list.add(rec);
		}
	    }
	System.out.println("inside getByContactID");
	return list;
    }
    public ArrayList<SSORecord> getEnabledByContactID(int contact_id) throws SQLException
    {
	ArrayList<SSORecord> list = new ArrayList<SSORecord>();
	for(RecordBase it : getCache()) 
	    {
		SSORecord rec = (SSORecord)it;
		//System.out.println("getEnabledByContactID: " + rec.contact_id + " == " + contact_id   );
		//if(rec.contact_id.equals(contact_id) && rec.disable == false) {
		
		if(rec.contact_id.equals(contact_id)) {
		    list.add(rec);
		}
	    }
	System.out.println("inside getEnabledByContactID");
	return list;
    }
	
    public SSORecord get(int id) throws SQLException {
	SSORecord keyrec = new SSORecord();
	keyrec.id = id;
	System.out.println("inside get SSORecord");
	return get(keyrec);
    }
    public ArrayList<SSORecord> getAll() throws SQLException
    {
	ArrayList<SSORecord> list = new ArrayList<SSORecord>();
	for(RecordBase it : getCache()) {
	    list.add((SSORecord)it);
	}
	return list;
    }
    
    public void insertDetail(SSORecord rec, ArrayList<Integer> auth_types) throws SQLException
    {
	Connection conn = connectSSO();
	try {		
	    //process detail information
	    conn.setAutoCommit(false);
	    
			//insert rec itself and get the new ID
	    insert(rec);
	    
	    //insert auth_type
	    SSOAuthorizationTypeModel amodel = new SSOAuthorizationTypeModel(context);
	    ArrayList<SSOAuthorizationTypeRecord> arecs = new ArrayList<SSOAuthorizationTypeRecord>();
	    for(Integer auth_type : auth_types) {
		SSOAuthorizationTypeRecord arec = new SSOAuthorizationTypeRecord();
		arec.contact_authorization_type_id = rec.id;
		arec.authorization_type_id = auth_type;
		arecs.add(arec);
	    }
	    amodel.insert(arecs);
	    
			conn.commit();
			conn.setAutoCommit(true);
	} catch (SQLException e) {
	    log.error(e);
	    log.info("Rolling back SSO detail insert transaction.");
	    if(conn != null) {
		conn.rollback();
		conn.setAutoCommit(true);
	    }
	    
	    //re-throw original exception
	    throw e;
	}	
    }
    
    public void updateDetail(SSORecord rec, ArrayList<Integer> auth_types) throws SQLException
    {
	//Do insert / update to our DB
	Connection conn = connectSSO();
	try {
	    //process detail information
	    conn.setAutoCommit(false);
	    
	    update(get(rec), rec);
	    
			//update auth_type
	    SSOAuthorizationTypeModel amodel = new SSOAuthorizationTypeModel(context);
	    ArrayList<SSOAuthorizationTypeRecord> arecs = new ArrayList<SSOAuthorizationTypeRecord>();
	    for(Integer auth_type : auth_types) {
		SSOAuthorizationTypeRecord arec = new SSOAuthorizationTypeRecord();
		arec.contact_authorization_type_id = rec.id;
		arec.authorization_type_id = auth_type;
		arecs.add(arec);
	    }
	    amodel.update(amodel.getAllByDNID(rec.id), arecs);
	    
	    conn.commit();
	    conn.setAutoCommit(true);
	} catch (SQLException e) {
	    log.error(e);
	    log.info("Rolling back SSO detail update transaction.");
	    if(conn != null) {
		conn.rollback();
		conn.setAutoCommit(true);
	    }
	    //re-throw original exception
	    throw e;
	}			
    }
    
    //mvkrenz added 6/1/2017
    public void ifContactExistAdd(String user_email, String dn_string, HttpServletRequest request) throws SQLException {
	Connection conn = connectOIM();
	Connection sso_conn = connectSSO();
	HttpSession session = request.getSession(false);
	Integer sso_dn_id =0;
	String user_dn_sso = dn_string;
	int sso_exist =0;
	int contact_exist =0;
	int dn_exist =0;
	int sso_id = 0;
	int contact_id= 0;
	int dn_contact_id =0;
	int dn_id = 0;
	int authorization_type_id;
	int contact_authorization_type_id;
	Integer last_inserted_id = null;
	//String user_access = (String)request.getHeader("OIDC_CLAIM_email"); \
	//(String)session.getAttribute
	String sso_user_dn_tmp = (String)request.getHeader("SSL_CLIENT_S_DN");
	String OIDC_CLAIM_access_token = (String)session.getAttribute("OIDC_CLAIM_access_token");
	String OIDC_CLAIM_email = (String)session.getAttribute("OIDC_CLAIM_email");
	String OIDC_CLAIM_family_name = (String)session.getAttribute("OIDC_CLAIM_family_name");
	String OIDC_CLAIM_given_name = (String)session.getAttribute("OIDC_CLAIM_given_name"); 
	String OIDC_CLAIM_idp = (String)session.getAttribute("OIDC_CLAIM_idp");
	String OIDC_CLAIM_idp_name = (String)session.getAttribute("OIDC_CLAIM_idp_name");
	String primary_email = user_email;
	
	ResultSet index_rs = null;
	Statement index_stmt = conn.createStatement();
	
	System.out.println("*************************** this is user DN "+ sso_user_dn_tmp);
	System.out.println("*************************** OIDC_CLAIM_access_token "+ OIDC_CLAIM_access_token);
	System.out.println("*************************** OIDC_CLAIM_email "+ user_email);
	System.out.println("*************************** OIDC_CLAIM_family_name "+ OIDC_CLAIM_family_name);
	System.out.println("*************************** OIDC_CLAIM_given_name "+ OIDC_CLAIM_given_name);
	
	// check if this email is already there
	String select_sso ="select * from contact_authorization_type where email='"+user_email+"' or email1='"+user_email+"' or email2='"+user_email+"' or email3='"+user_email+"' order by id desc limit 1";
	System.out.println(select_sso);
	Statement sso_stmt = sso_conn.createStatement();
	ResultSet sso_rs = sso_stmt.executeQuery(select_sso);
	
	if (sso_rs.next()){
	    System.out.println("There is SSO");
	    sso_exist=1;
	    
	    sso_id = sso_rs.getInt("id");
	}
	
	String select_contact ="select * from contact where primary_email='"+user_email+"' or secondary_email='"+user_email+"' order by id desc limit 1";
	System.out.println(select_contact);
	Statement contact_stmt = conn.createStatement();
	ResultSet contact_rs = contact_stmt.executeQuery(select_contact);
	
	if (contact_rs.next()){
	    System.out.println("There is Contact Record");
	    
	    contact_exist=1;
	    contact_id = contact_rs.getInt("id");
	}
	
	String select_dn ="select * from contact left join dn on dn.contact_id=contact.id  where dn_string='"+user_dn_sso+"'";
	System.out.println(select_dn);
	Statement dn_stmt = conn.createStatement();
	ResultSet dn_rs = dn_stmt.executeQuery(select_dn);
	String p_email0= "";
	String s_email0 ="";
	
	if (dn_rs.next()){
	    System.out.println("There is DN");
	    
	    dn_exist=1;
	    dn_contact_id = dn_rs.getInt("contact_id");
	    dn_id = dn_rs.getInt("id");
	    p_email0 = dn_rs.getString("primary_email");
	    s_email0 = dn_rs.getString("secondary_email");
	}
	
	String contact_insert_sql = "INSERT INTO contact (name, primary_email, profile,use_twiki,twiki_id,count_usercert_year,count_hostcert_day,count_hostcert_year ) values ('"+OIDC_CLAIM_given_name+" "+OIDC_CLAIM_family_name+"', '"+primary_email+"','none',1,'no1',0,0,0)";
	
	PreparedStatement statement = conn.prepareStatement(contact_insert_sql, Statement.RETURN_GENERATED_KEYS);
	//  Statement stmt = conn.createStatement();
	
	String sso_insert_sql = "INSERT INTO contact_authorization_type (given_name,family_name, email,contact_id,access_token,idp,idp_name,created) values ('"+OIDC_CLAIM_given_name+"' ,'"+OIDC_CLAIM_family_name+"','"+primary_email+"'," + contact_id + ",'"+OIDC_CLAIM_access_token+"','"+OIDC_CLAIM_idp+"' ,'"+OIDC_CLAIM_idp_name+"',now())";
	
	PreparedStatement insert_sso_stmt = sso_conn.prepareStatement(sso_insert_sql, Statement.RETURN_GENERATED_KEYS);

	if(sso_exist==1 && contact_exist==0){
		    
	    statement.executeUpdate();
	    ResultSet rs = statement.getGeneratedKeys();
	    while (rs.next()) {
		System.out.println("--------- SSO YES --- Contact NO");
		System.out.println("INSERT CONTACT"+ contact_insert_sql);
		
		last_inserted_id = rs.getInt(1);
		String update_sso ="update contact_authorization_type  set contact_id='"+last_inserted_id+"' where id="+sso_id+"";
		System.out.println(update_sso);
		Statement update_sso_stmt = sso_conn.createStatement();
		
		update_sso_stmt.executeUpdate(update_sso);
		update_sso_stmt.close();
	    }	//need to create contact

	}else if(sso_exist==0 && contact_exist==1){
	    System.out.println("INSERT SSO:  \n" +sso_insert_sql +  "\n");
	    //insert_sso_stmt.executeUpdate(sso_insert_sql);
	    
	    insert_sso_stmt.executeUpdate();
	    ResultSet rs_sso = insert_sso_stmt.getGeneratedKeys();
	    
	    while (rs_sso.next()) {
		System.out.println("--------- SSO NO --- Contact YES");
		contact_authorization_type_id  = rs_sso.getInt(1);
		System.out.println("last inserted contact_authorization_type_id "+ contact_authorization_type_id);
		
		String select_dn_contact ="select dn.id as dn_id, primary_email, secondary_email from contact left join dn on contact.id=dn.contact_id where contact_id="+ contact_id +" order by dn.id";
		System.out.println(select_dn_contact);
		Statement dn_contact_stmt = conn.createStatement();
		ResultSet dn_contact_rs = dn_contact_stmt.executeQuery(select_dn_contact);
			
		if (dn_contact_rs.next()) {
		    String p_email = dn_contact_rs.getString("primary_email");
		    String s_email = dn_contact_rs.getString("secondary_email");
		    String s_email_seq= " ";
		    
		    if(s_email!=""){
			s_email_seq = " , email2='" + s_email + "'" ;
		    }
		    
		    String update_dn_contact_with_contact ="update contact_authorization_type  set email1='"+p_email+"' " + s_email_seq + "  where id="+contact_authorization_type_id+"";
		    Statement update_dn_contact_stmt_with_contact = sso_conn.createStatement();
		    update_dn_contact_stmt_with_contact.executeUpdate(update_dn_contact_with_contact );
		    update_dn_contact_stmt_with_contact.close();
		}    

		dn_contact_rs.beforeFirst();

		while (dn_contact_rs.next()) {
				    
		    String email_seq;
		    dn_id = dn_contact_rs.getInt("dn_id");
		    
		    index_rs = null;
		    index_stmt = conn.createStatement();
		  		  
		    index_stmt.execute("SELECT * FROM dn_authorization_type WHERE dn_id = " + dn_id); 
		    System.out.println("SELECT * FROM dn_authorization_type WHERE dn_id = " + dn_id +"\n ");
		    index_rs = index_stmt.getResultSet();
		  
		    while (index_rs.next()) {
			authorization_type_id =index_rs.getInt("authorization_type_id");
			
			Statement insert_index_stmt = sso_conn.createStatement();

			insert_index_stmt.execute("delete from contact_authorization_type_index where contact_authorization_type_id="+contact_authorization_type_id+" and authorization_type_id="+authorization_type_id+"");

			insert_index_stmt.execute("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+contact_authorization_type_id+","+authorization_type_id+")");
			System.out.println("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+contact_authorization_type_id+","+authorization_type_id+")");
			
		    } 
		}
		
	    }
	    
	}else if(dn_exist==1 && sso_exist==0 && contact_exist==0){
	    String update_contact_with_secondary_email ="update contact set secondary_email=\""+primary_email+"\"  where id=" + dn_contact_id ;
	    Statement update_contact_secondary = conn.createStatement();
	    update_contact_secondary.executeUpdate(update_contact_with_secondary_email);
	    update_contact_secondary.close();
	    
	    String select_dn_contact ="select * from contact_authorization_type where contact_id="+ dn_contact_id +"";               
	    Statement dn_contact_stmt = sso_conn.createStatement();
	    ResultSet dn_contact_rs = dn_contact_stmt.executeQuery(select_dn_contact);                                               
            
	    if(dn_contact_rs.next()){                                                                                                
		
		String email_seq;                                                                                                    
                
		sso_dn_id = dn_contact_rs.getInt("id");                                                                    
		contact_authorization_type_id= dn_contact_rs.getInt("id");                                                           
		String email = dn_contact_rs.getString("email");                                                                     
		String email1 = dn_contact_rs.getString("email1");                                                                   
		String email2 = dn_contact_rs.getString("email2");                                                                   
		String email3 = dn_contact_rs.getString("email3");
		if(email1==""){                                                                                                             			    
		    email_seq= "email1";                                                                                                                    
		}else{                                                                                                                                                              
		    if(email2==""){                                                                                                                                                 
			email_seq= "email2";                                                                                                                                        
		    }else{                                                                                                                                                          
			email_seq= "email3";                                                                                                                                        
		    }                                                                                                                                                               
		}                                                                              
                                                                                     
		String update_dn_contact_with_contact2 ="update contact_authorization_type set "+email_seq+"=\""+primary_email+"\"  where id=" + contact_authorization_type_id;
	
		Statement update_dn_contact_stmt_with_contact2 = sso_conn.createStatement();
		update_dn_contact_stmt_with_contact2.executeUpdate(update_dn_contact_with_contact2);
		update_dn_contact_stmt_with_contact2.close();

		String select_dn_contact3 ="select dn.id as dn_id, primary_email, secondary_email from contact left join dn on contact.id=dn.contact_id where contact_id="+ dn_contact_id +" order by dn.id";
               
                System.out.println(select_dn_contact3);
                Statement dn_contact_stmt3 = conn.createStatement();
                ResultSet dn_contact_rs3 = dn_contact_stmt3.executeQuery(select_dn_contact3);

		while (dn_contact_rs3.next()) {

                    String email_seq3;
                    dn_id = dn_contact_rs3.getInt("dn_id");

                    index_rs = null;
                    index_stmt = conn.createStatement();

                    System.out.println("this DN id " + dn_id);
  
		    if (index_stmt.execute("SELECT * FROM dn_authorization_type WHERE dn_id = " + dn_id)) {
                        index_rs = index_stmt.getResultSet();
                        while (index_rs.next()) {
                            authorization_type_id =index_rs.getInt("authorization_type_id");
			    Statement insert_index_stmt5 = sso_conn.createStatement();
			    insert_index_stmt5.execute("delete from contact_authorization_type_index where contact_authorization_type_id="+contact_authorization_type_id+" and authorization_type_id="+authorization_type_id+"");
                            insert_index_stmt5.execute("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+sso_dn_id+","+authorization_type_id+")");
                        }
                    }
                }
		
	    }else{

		String sso_insert_sql_dn11 = "INSERT INTO contact_authorization_type (given_name,family_name, email,contact_id,access_token,idp,idp_name,created) values ('"+OIDC_CLAIM_given_name+"' ,'"+OIDC_CLAIM_family_name+"','"+primary_email+"'," + dn_contact_id + ",'"+OIDC_CLAIM_access_token+"','"+OIDC_CLAIM_idp+"' ,'"+OIDC_CLAIM_idp_name+"',now())";

		System.out.println("INSERT SSO IF DN IS FOUND: "+ sso_insert_sql_dn11 );
		
		PreparedStatement insert_sso_stmt_dn11 = sso_conn.prepareStatement(sso_insert_sql_dn11, Statement.RETURN_GENERATED_KEYS);
		
		insert_sso_stmt_dn11.executeUpdate();
                System.out.println("Executed the command " );

		ResultSet rs9 = insert_sso_stmt_dn11.getGeneratedKeys();
		System.out.println("NO associated DN with SSO");
		if(rs9.next()){
	
		    contact_authorization_type_id = rs9.getInt(1);
		    
		    String select_dn_contact1 ="select dn.id as dn_id, primary_email, secondary_email from contact left join dn on contact.id=dn.contact_id where contact_id="+ dn_contact_id +" order by dn.id";

		    System.out.println(select_dn_contact1);
		    Statement dn_contact_stmt1 = conn.createStatement();
		    ResultSet dn_contact_rs1 = dn_contact_stmt1.executeQuery(select_dn_contact1);
		    
		    while (dn_contact_rs1.next()) {
			
			//String email_seq;
			dn_id = dn_contact_rs1.getInt("dn_id");
			System.out.println(dn_id);
			index_rs = null;
			index_stmt = conn.createStatement();
			if (index_stmt.execute("SELECT * FROM dn_authorization_type WHERE dn_id = " + dn_id)) {
			    
			    index_rs = index_stmt.getResultSet();
			    while (index_rs.next()) {
				authorization_type_id =index_rs.getInt("authorization_type_id");
				
				Statement insert_index_stmt4 = sso_conn.createStatement();
				insert_index_stmt4.execute("delete from contact_authorization_type_index where contact_authorization_type_id="+contact_authorization_type_id+" and authorization_type_id="+authorization_type_id+"");
				insert_index_stmt4.execute("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+contact_authorization_type_id+","+authorization_type_id+")");
				System.out.println("Inserting Authorization Types");
			    }
			}
		    }
		}
	    }
	    
	}else if(sso_exist==0 && contact_exist==0 && dn_exist==0){
	
	System.out.println("not email and not dn registered");
	System.out.println(contact_insert_sql);
	
	statement.executeUpdate();
	ResultSet rs_dn = statement.getGeneratedKeys();
	while (rs_dn.next()) {
	    
	    Integer contact_id1 = rs_dn.getInt(1);
		
	    String sso_insert_sql1 = "INSERT INTO contact_authorization_type (given_name,family_name, email,contact_id,access_token,idp,idp_name,created) values ('"+OIDC_CLAIM_given_name+"' ,'"+OIDC_CLAIM_family_name+"','"+primary_email+"'," + contact_id1 + ",'"+OIDC_CLAIM_access_token+"','"+OIDC_CLAIM_idp+"' ,'"+OIDC_CLAIM_idp_name+"',now())";
		    
	    PreparedStatement insert_sso_stmt1 = sso_conn.prepareStatement(sso_insert_sql1, Statement.RETURN_GENERATED_KEYS);
	    
	    insert_sso_stmt1.executeUpdate();
	    ResultSet rs_sso1 = insert_sso_stmt1.getGeneratedKeys();
	    
	    while (rs_sso1.next()) {
		System.out.println("--------- SSO NO --- Contact NO --- DN NO");
		contact_authorization_type_id  = rs_sso1.getInt(1);
		System.out.println("last inserted contact_authorization_type_id "+ contact_authorization_type_id);
		
		Statement insert_index_stmt = sso_conn.createStatement();
		
		insert_index_stmt.execute("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+contact_authorization_type_id+",1)");
		System.out.println("INSERT INTO contact_authorization_type_index (contact_authorization_type_id, authorization_type_id) VALUES ("+contact_authorization_type_id+",1)");
	    }
	}
	
    }
    
    emptyCache();
    fillCache();
    
    statement.close();
    //insert_sso_stmt.close();                                                                                                                         
    conn.close();    
    sso_conn.close();
}
/* - removing DN causes octompus-removing issue
   public void removeDN(DNRecord rec) throws SQLException
   {
   //remove DN and all authorization associated with that DN
   updateDetail(rec, new ArrayList<Integer>());
   
   //set submitter_dn_id to null for removed dn
   ContactModel cmodel = new ContactModel(context);
   ArrayList<ContactRecord> crecs = cmodel.getBySubmitterDNID(rec.id);
   for(ContactRecord crec : crecs) {
			crec.submitter_dn_id = null;
			cmodel.update(crec);
			log.info("contact id: " + crec.id + " submitter_dn_id has been reset to null");
		}
		
		//set resource_downtime dn_id to null for removed dn
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
		ArrayList<ResourceDowntimeRecord> drecs = dmodel.getByDNID(rec.id);
		for(ResourceDowntimeRecord drec : drecs) {
			drec.dn_id = null;
			dmodel.update(drec);
			log.info("downtime id: " + drec.id + " dn_id has been reset to null");
		}
		
		//set resource_downtime dn_id to null for removed dn
		SiteModel smodel = new SiteModel(context);
		ArrayList<SiteRecord> srecs = smodel.getByDNID(rec.id);
		for(SiteRecord srec : srecs) {
			srec.submitter_dn_id = null;
			smodel.update(srec);
			log.info("site id: " + srec.id + " submitter_dn_id has been reset to null");
		}
		
		//then remove the dn itself
		super.remove(rec);
	}
	*/
}