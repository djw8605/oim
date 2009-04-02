package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeActionRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class AuthorizationTypeActionModel extends SmallTableModelBase<AuthorizationTypeActionRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeActionModel.class);  
    
    public AuthorizationTypeActionModel(Authorization auth) 
    {
    	super(auth, "authorization_type_action");
    }
    AuthorizationTypeActionRecord createRecord(ResultSet rs) throws SQLException
	{
		return new AuthorizationTypeActionRecord(rs);
	}
	
	public Collection<Integer> getActionByAuthTypeID(Integer authorization_type_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
			AuthorizationTypeActionRecord rec = (AuthorizationTypeActionRecord)it;
			if(rec.authorization_type_id.compareTo(authorization_type_id) == 0) {
				list.add(rec.action_id);
			}
		}
		return list;
	}
}