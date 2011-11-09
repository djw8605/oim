package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;

import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.form.ServiceFormDE;

public class ServiceDetails extends DivRepFormElement {
    static Logger log = Logger.getLogger(ServiceFormDE.class); 
	private ServiceDetailsContent content = null;
	private Integer service_id = null;
	private Context context;
	
	public ServiceDetails(Context context, DivRep _parent) {
		super(_parent);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\" class=\"indent\">");
		if(content != null) {
			content.render(out);
		}
		out.write("</div>");
	}
	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean validate()
	{	
		boolean valid = true;
		if(content == null || !content.validate()) {
			valid = false;
		}
		redraw();
		return valid;
	}
	
	public void setService(Integer service_id, ArrayList<ResourceServiceDetailRecord> details_recs) 
	{		
		content = null;
		if(service_id != null) {
			try {
				//pull service details
				ServiceModel smodel = new ServiceModel(context);
				ServiceRecord srec = smodel.get(service_id);
				if(srec.type.equals("GRID")) {
					content = new GridServiceDetails(this, srec);
				} else if(srec.type.equals("CENTRAL")) {
					content = new CentralServiceDetails(this, srec);
				} else {
					log.warn("Unknown service type for " + srec.type);
				}
 				
				//content = new VMHostServiceDetails(this); break;
			} catch (SQLException e) {
				log.error("Failed to obtain detail for service id " + service_id, e);
			}	
		}
		
		if(content != null) {
			if(details_recs != null) {
				//pull details for this service
				HashMap<String, String> values = new HashMap<String, String>();
				for(ResourceServiceDetailRecord details_rec : details_recs) {
					if(details_rec.service_id.equals(service_id)) {
						values.put(details_rec.key, details_rec.value);
					}
				}
				content.setValues(values);
			}
		}
		redraw();
		this.service_id = service_id;
	}

	public ArrayList<ResourceServiceDetailRecord> getServiceDetailsRecords() {
		if(content == null) return null;
		
		ArrayList<ResourceServiceDetailRecord> recs = new ArrayList<ResourceServiceDetailRecord>();
		HashMap<String, String> values = content.getValues();
		if(values != null) {
			for(String key : values.keySet()) {
				String value = values.get(key);
				ResourceServiceDetailRecord rec = new ResourceServiceDetailRecord();
				rec.service_id = service_id;
				rec.key = key;
				rec.value = value;
				recs.add(rec);
			}
		}
		return recs;
	}
}
