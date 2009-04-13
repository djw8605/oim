package edu.iu.grid.oim.view.divex.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.FormDE;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;

import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class SiteFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(SiteFormDE.class); 
   
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE long_name;
	private TextAreaFormElementDE description;
	private TextFormElementDE address_line_1;
	private TextFormElementDE address_line_2;
	private TextFormElementDE city;
	private TextFormElementDE state;
	private TextFormElementDE zipcode;
	private TextFormElementDE country;
	private TextFormElementDE latitude;
	private TextFormElementDE longitude;
	private SelectFormElementDE sc_id;
	private SelectFormElementDE facility_id;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	
	public SiteFormDE(DivEx parent, SiteRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Details</h2>");
		
		//pull sites for unique validator
		HashMap<Integer, String> sites = getSites();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			sites.remove(id);
		}

		facility_id = new SelectFormElementDE(this, getFacilities());
		facility_id.setLabel("Facility");
		facility_id.setValue(rec.facility_id);
		facility_id.setRequired(true);

		name = new TextFormElementDE(this);
		name.setLabel("Site Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(sites.values()));
		name.setRequired(true);
		
		long_name = new TextFormElementDE(this);
		long_name.setLabel("Long Name");
		long_name.setValue(rec.long_name);
		long_name.setRequired(false);
				
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(false);

		address_line_1 = new TextFormElementDE(this);
		address_line_1.setLabel("Street Address");
		address_line_1.setValue(rec.address_line_1);
		address_line_1.setRequired(false);

		address_line_2 = new TextFormElementDE(this);
		address_line_2.setLabel("Address Line 2");
		address_line_2.setValue(rec.address_line_2);
		address_line_2.setRequired(false);

		city = new TextFormElementDE(this);
		city.setLabel("City");
		city.setValue(rec.city);
		city.setRequired(false);

		// Need to make this dropdown? probably not.
		state = new TextFormElementDE(this);
		state.setLabel("State");
		state.setValue(rec.state);
		state.setRequired(true);

		zipcode = new TextFormElementDE(this);
		zipcode.setLabel("Zipcode");
		zipcode.setValue(rec.zipcode);
		zipcode.setRequired(true);

		// Need to make this drop down. -agopu
		country = new TextFormElementDE(this);
		country.setLabel("Country");
		country.setValue(rec.country);
		country.setRequired(true);
		
		sc_id = new SelectFormElementDE(this, getSCs());
		sc_id.setLabel("Support Center");
		sc_id.setValue(rec.sc_id);
		sc_id.setRequired(true);

		active = new CheckBoxFormElementDE(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElementDE(this);
		disable.setLabel("Disabled");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private HashMap<Integer, String> getSites() throws AuthorizationException, SQLException
	{
		SiteModel model = new SiteModel(auth);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SiteRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private HashMap<Integer, String> getSCs() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(auth);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SCRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	private HashMap<Integer, String> getFacilities() throws AuthorizationException, SQLException
	{
		FacilityModel model = new FacilityModel(auth);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(FacilityRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	protected Boolean doSubmit() {

		// Moved try block beginning from line 208 to handled SQL exception.. -agopu
		try {
			//Construct SiteRecord
			SiteRecord rec = new SiteRecord();
			rec.id = id;
		
			rec.name = name.getValue();
			rec.long_name = long_name.getValue();
			rec.description = description.getValue();

			rec.address_line_1 = address_line_1.getValue();
			rec.address_line_2 = address_line_2.getValue();
			rec.city           = city.getValue();
			rec.state          = state.getValue();
			rec.zipcode        = zipcode.getValue();
			rec.country        = country.getValue();
			
			rec.sc_id = sc_id.getValue();
			rec.facility_id = facility_id.getValue();
	
			rec.active = active.getValue();
			rec.disable = disable.getValue();
			
			/*
			//fetch long/lat information from http://www.geonames.org/source-code/
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
			//searchCriteria.setQ(city.getValue() + ", " + state.getLabel() + ", " + country.getLabel());
			searchCriteria.setQ("zurich");
			ToponymSearchResult searchResult;
			try {
				searchResult = WebService.search(searchCriteria);
				for (Toponym toponym : searchResult.getToponyms()) {
					System.out.println(toponym.getName()+" "+ toponym.getCountryName());
				}
			} catch (Exception e) {
				log.error(e);
			}
			*/
			
			SiteModel model = new SiteModel(auth);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			return false;
		}
		return true;
	}
}
