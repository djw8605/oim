package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CampusGridRecord extends RecordBase 
{
	@Key public Integer id;
	public String name;
	public String description;
	public String fqdn; //submit/login host
	public String gratia; //link to the gratia probe output (ask Dan Frase)
	public Integer maturity; //defined here > https://twiki.grid.iu.edu/bin/view/CampusGrids/DeployedCampusInfrastructures
	public Double longitude, latitude; // Need to change this to float but it is text in the DB right now
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public CampusGridRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public CampusGridRecord() {}
	public String getName() {
		return name;
	}
}
