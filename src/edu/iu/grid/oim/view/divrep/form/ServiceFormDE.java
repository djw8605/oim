package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divrep.Event;
import com.webif.divrep.Static;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.IntegerValidator;
import com.webif.divrep.form.validator.UniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.MetricServiceModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.MetricService;

public class ServiceFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(ServiceFormDE.class); 
    private Context context;
    
	protected Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextFormElement description;
	private TextFormElement port;
	private SelectFormElement service_group_id;
	private MetricService metric_service;
	
	public ServiceFormDE(Context _context, ServiceRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;

		//pull metric names for unique validator
		HashMap<Integer, String> service_names = getServiceNames();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			service_names.remove(id);
		}
		name = new TextFormElement(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(service_names.values()));
		name.setRequired(true);
		
		description = new TextFormElement(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		
		port = new TextFormElement(this);
		port.setLabel("Port");
		port.addValidator(new IntegerValidator());
		if(rec.port != null) {
			port.setValue(rec.port.toString());
		}
		
		HashMap<Integer, String> kv = new HashMap<Integer, String>();
		ServiceGroupModel sgmodel = new ServiceGroupModel(context);
		for(ServiceGroupRecord sgrec : sgmodel.getAll()) {
			kv.put(sgrec.id, sgrec.name);
		}
		service_group_id = new SelectFormElement(this, kv);
		service_group_id.setLabel("Service Group");
		service_group_id.setValue(rec.service_group_id);
		service_group_id.setRequired(true);
		
		new Static(this, "<h3>RSV Metrics</h3>");
		MetricModel mmodel = new MetricModel(context);
		HashMap<Integer, String> metric_kv = new HashMap<Integer, String>();
		for(MetricRecord mrec : mmodel.getAll()) {
			metric_kv.put(mrec.id, mrec.common_name);
		}
		metric_service = new MetricService(this, metric_kv);
		MetricServiceModel msmodel = new MetricServiceModel(context);
		if(id != null) {
			for(MetricServiceRecord srec : msmodel.getAllByServiceID(rec.id)) {
				metric_service.addMetric(srec);
			}
		}
	}
	
	private HashMap<Integer, String> getServiceNames() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		ServiceModel model = new ServiceModel(context);
		for(ServiceRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		try {
			auth.check("admin");
			
			ServiceRecord rec = new ServiceRecord();
			rec.id = id;
			rec.name = name.getValue();
			String port_str = port.getValue();
			if(port_str != null && port_str.length() != 0) {
				rec.port = Integer.parseInt(port_str);
			}
			rec.description = description.getValue();
			rec.service_group_id = service_group_id.getValue();
			
			ServiceModel model = new ServiceModel(context);
			if(rec.id == null) {
				model.insertDetail(rec, metric_service.getMetricServiceRecords());
			} else {
				model.updateDetail(rec, metric_service.getMetricServiceRecords());
			}
		} catch (Exception e) {
			log.error(e);
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}