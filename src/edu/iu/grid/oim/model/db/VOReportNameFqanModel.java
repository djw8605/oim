package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;

public class VOReportNameFqanModel extends SmallTableModelBase<VOReportNameFqanRecord> {
    static Logger log = Logger.getLogger(VOReportNameFqanModel.class); 

	public VOReportNameFqanModel(Authorization _auth) {
		super(_auth, "vo_report_name_fqan");
	}
	VOReportNameFqanRecord createRecord(ResultSet rs) throws SQLException
	{
		return new VOReportNameFqanRecord(rs);
	}
	public ArrayList<VOReportNameFqanRecord> getAll() throws SQLException
	{
		ArrayList<VOReportNameFqanRecord> list = new ArrayList<VOReportNameFqanRecord>();
		for(RecordBase it : getCache()) {
			list.add((VOReportNameFqanRecord)it);
		}
		return list;
	}
	public ArrayList<VOReportNameFqanRecord> getAllByVOReportNameID(int vo_report_name_id) throws SQLException
	{
		ArrayList<VOReportNameFqanRecord> list = new ArrayList<VOReportNameFqanRecord>();
		for(VOReportNameFqanRecord it : getAll()) {
			if(it.vo_report_name_id.compareTo(vo_report_name_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
}
