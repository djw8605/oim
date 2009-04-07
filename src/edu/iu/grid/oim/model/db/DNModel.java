package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DNModel extends SmallTableModelBase<DNRecord> {
    static Logger log = Logger.getLogger(DNModel.class);  
    
    public DNModel(Authorization auth) 
    {
    	super(auth, "dn");
    }
    DNRecord createRecord(ResultSet rs) throws SQLException
	{
		return new DNRecord(rs);
	}
	
	public DNRecord getByDNString(String dn_string) throws SQLException
	{
		for(RecordBase it : getCache()) 
		{
			DNRecord rec = (DNRecord)it;
			if(rec.dn_string.compareTo(dn_string) == 0) {
				return rec;
			}
		}
		return null;
	}
}