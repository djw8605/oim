package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;

import edu.iu.grid.oim.model.db.SSOModel;

public class SSORecord extends RecordBase {

    @Key public Integer id;
    public String dn_string;
    public String email;
    public String email1;
    public String email2;
    public String email3;
    public Integer contact_id;
    public Boolean disable;
    public String idp;

    //load from existing record                                                                                                                                   
    public SSORecord(ResultSet rs) throws SQLException { super(rs); }
    //for creating new record                                                                                                                                     
    public SSORecord() {}
}

