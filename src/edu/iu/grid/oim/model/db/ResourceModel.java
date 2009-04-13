package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.webif.divex.form.CheckBoxFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class ResourceModel extends SmallTableModelBase<ResourceRecord> {
    static Logger log = Logger.getLogger(ResourceModel.class);  
    
    public ResourceModel(Authorization auth) 
    {
    	super(auth, "resource");
    }
    ResourceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceRecord(rs);
	}
	
	public ArrayList<ResourceRecord> getByGroupID(int group_id) throws SQLException
	{
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
		for(RecordBase rec : getCache()) {
			ResourceRecord vcrec = (ResourceRecord)rec;
			if(vcrec.resource_group_id == group_id) list.add(vcrec);
		}
		return list;
	}
	public Collection<ResourceRecord> getAllEditable() throws SQLException
	{	   
		ArrayList<ResourceRecord> list = new ArrayList();
    	//only select record that is editable
	    for(RecordBase id : getCache()) {
	    	ResourceRecord rec = (ResourceRecord)id;
	    	if(canEdit(rec.id)) {
	    		list.add(rec);
	    	}
	    }	    	
	    return list;
	}
	
	public boolean canEdit(int id)
	{
		if(auth.allows("admin")) return true;
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(auth);
		Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContactID());
		for(VOContactRecord rec : vcrecs) {
			list.add(rec.vo_id);
		}
		return list;
	}
	public ResourceRecord get(int id) throws SQLException {
		ResourceRecord keyrec = new ResourceRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceRecord)it);
		}
		return list;
	}
	
	public void insertDetail(ResourceRecord rec, 
			ArrayList<String> resource_aliases,
			ArrayList<ResourceContactRecord> contacts,
			ArrayList<ResourceServiceRecord> resource_services) throws Exception
	{
		try {
			
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert resource itself and insert() will set rec.id with newly created id
			insert(rec);
			
			//process contact information
			ResourceContactModel cmodel = new ResourceContactModel(auth);
			//reset vo_id on all contact records
			for(ResourceContactRecord vcrec : contacts) {
				vcrec.resource_id = rec.id;
			}
			cmodel.update(cmodel.getByResourceID(rec.id), contacts);
		
			//process resource alias
			ResourceAliasModel ramodel = new ResourceAliasModel(auth);
			ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
			for(String alias : resource_aliases) {
				ResourceAliasRecord rarec = new ResourceAliasRecord();
				rarec.resource_id = rec.id;
				rarec.resource_alias = alias;
				list.add(rarec);
			}
			ramodel.insert(list);		
			
			//process resource services
			ResourceServiceModel rsmodel = new ResourceServiceModel(auth);
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			rsmodel.insert(resource_services);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(ResourceRecord rec,
			ArrayList<String> resource_aliases,
			ArrayList<ResourceContactRecord> contacts,
			ArrayList<ResourceServiceRecord> resource_services) throws Exception
	{
		//Do insert / update to our DB
		try {		
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
			//process contact information
			ResourceContactModel cmodel = new ResourceContactModel(auth);
			//reset vo_id on all contact records
			for(ResourceContactRecord vcrec : contacts) {
				vcrec.resource_id = rec.id;
			}
			cmodel.update(cmodel.getByResourceID(rec.id), contacts);
			
			//process resource alias
			ResourceAliasModel ramodel = new ResourceAliasModel(auth);
			ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
			for(String alias : resource_aliases) {
				ResourceAliasRecord rarec = new ResourceAliasRecord();
				rarec.resource_id = rec.id;
				rarec.resource_alias = alias;
				list.add(rarec);
			}
			ramodel.update(ramodel.getAllByResourceID(rec.id), list);	
		
			//process resource services
			ResourceServiceModel rsmodel = new ResourceServiceModel(auth);
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			rsmodel.update(rsmodel.getAllByResourceID(rec.id), resource_services);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}

