package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.MeshConfigGroupModel;
import edu.iu.grid.oim.model.db.MeshConfigMemberModel;
import edu.iu.grid.oim.model.db.MeshConfigParamModel;
import edu.iu.grid.oim.model.db.MeshConfigTestModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceDetailModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.MeshConfigMemberRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.ResourceServiceListEditor;
import edu.iu.grid.oim.view.divrep.VOSelector;
import edu.iu.grid.oim.view.divrep.ResourceServiceListEditor.ResourceInfo;

public class MeshConfigServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(MeshConfigServlet.class);  

    final Integer SERVICE_GROUP_PERFSONAR_MONIOTIRNG = 1003;

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		BootMenuView menuview = new BootMenuView(context, "meshconfig");
		try {
			ConfigForm config = new ConfigForm(context, "home");
			BootPage page = new BootPage(context, menuview, new DivRepWrapper(config), null);
			page.render(response.getWriter());		
		} catch (SQLException e) {
			log.error("Failed to render Config form");
		}
	}
		
	//contains current state of everything
	class ConfigForm extends DivRepForm {
		UserContext context;
		LinkedHashMap<Integer, String> service_types = new LinkedHashMap();
		
		GroupsDiv groupsdiv;
		ParamsDiv paramsdiv;
		TestsDiv testsdiv;

		class TestDiv extends DivRepFormElement {
			
			LinkedHashMap<Integer, String> mesh_types = new LinkedHashMap();

			Integer id;
			
			DivRepTextBox name;
			VOSelector vo;
			DivRepCheckBox disable;
			
			DivRepSelectBox service;
			DivRepSelectBox type;
			DivRepSelectBox param;
			DivRepSelectBox group_a;
			DivRepSelectBox group_b;

			protected TestDiv(DivRep parent, MeshConfigTestRecord rec) {
				super(parent);
				
				mesh_types = new LinkedHashMap();
				mesh_types.put(0, "DISJOINT");
				mesh_types.put(1, "MESH");
				mesh_types.put(2, "STAR");		
						
				name = new DivRepTextBox(this);
				name.setLabel("Name");
				name.setRequired(true);
		
				vo = new VOSelector(this, context);
				vo.setRequired(true);
				
				disable = new DivRepCheckBox(this);
				disable.setLabel("Disable");
				
				service = new DivRepSelectBox(this);
				service.setLabel("Service Type");
				service.setRequired(true);
				service.setValues(service_types);
				service.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						load_keyvalues();
						
						//reset to null
						param.setValue(null); 
						group_a.setValue(null);
						group_b.setValue(null);
						
						showhide();
						TestDiv.this.redraw();
					}
				});
				
				type = new DivRepSelectBox(this);
				type.setLabel("Mesh Type");
				type.setRequired(true);
				type.setValues(mesh_types);
				type.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						showhide();
						TestDiv.this.redraw();
					}
				});
				
				group_a = new DivRepSelectBox(this);
				group_a.setLabel("Group A");
				
				group_b = new DivRepSelectBox(this);
				group_b.setLabel("Group B");
				
				param = new DivRepSelectBox(this);
				param.setLabel("Parameters");
				param.setRequired(true);
				
				
				if(rec != null) {
					id = rec.id;
					name.setValue(rec.name);
					vo.setValue(rec.vo_id);
					disable.setValue(rec.disable);
					service.setValue(rec.service_id);
					type.setValue(meshTypeStringToInteger(rec.type));
					param.setValue(rec.param_id);
					group_a.setValue(rec.groupa_id);
					group_b.setValue(rec.groupb_id);
				} else {
					//come up with a new ID
					Integer nextid = 0;
					for(TestDiv div : testsdiv.tests) {
						if(nextid <= div.id) {
							nextid = div.id+1;
						}
					}
					id = nextid;
				}
				
				load_keyvalues();
				showhide();
			}
			
			private Integer meshTypeStringToInteger(String type) {
				for(Integer id : mesh_types.keySet()) {
					String mtype = mesh_types.get(id);
					if(mtype.equals(type)) {
						return id;
					}
				}
				return null;
			}
			
			private void showhide() {				
				param.setHidden(true);
				type.setHidden(true);
				if(service.getValue() != null) {
					param.setHidden(false);
					type.setHidden(false);
				}
				
				//hide everything by default
				group_a.setRequired(false);
				group_a.setHidden(true);
				group_b.setRequired(false);
				group_b.setHidden(true);
				
				if(service.getValue() != null && type.getValue() != null) {
					switch(mesh_types.get(type.getValue())) {
					case "MESH":
						//only show group A
						group_a.setRequired(true);
						group_a.setHidden(false);
						break;
					case "DISJOINT":
					case "STAR":
						//show both
						group_a.setRequired(true);
						group_a.setHidden(false);
						group_b.setRequired(true);
						group_b.setHidden(false);
						break;
					}
				}
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"well\">");

				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4\">");
					
					vo.render(out);
				out.write("</div>");
				out.write("<div class=\"span8\">");
					name.render(out);
				out.write("</div>");
				out.write("</div>");

				//vo / service / params
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4\">");
					service.render(out);
				out.write("</div>");
				out.write("<div class=\"span8\">");
					param.render(out);
				out.write("</div>");
				out.write("</div>");

				//mesh and groups
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4\">");
					type.render(out);
				out.write("</div>");
				out.write("<div class=\"span4\">");
					group_a.render(out);
				out.write("</div>");
				out.write("<div class=\"span4\">");
					group_b.render(out);			
				out.write("</div>");//sapn4
				out.write("</div>");//row-fluid
				
				disable.render(out);
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}

			public void load_keyvalues() {
				LinkedHashMap<Integer, String> groups_keyvalues = new LinkedHashMap();
				for(GroupDiv group : groupsdiv.groups) {
					Integer service_id = group.service.getValue();
					if(service_id != null && service_id.equals(service.getValue())) {
						groups_keyvalues.put(group.id, group.name.getValue());
					}
				}	
				group_a.setValues(groups_keyvalues);	
				group_b.setValues(groups_keyvalues);	
				
				LinkedHashMap<Integer, String> param_keyvalues = new LinkedHashMap();
				for(ParamDiv param : paramsdiv.params) {
					Integer service_id = param.service.getValue();
					if(service_id != null && service_id.equals(service.getValue())) {
						param_keyvalues.put(param.id, param.name.getValue());
					}
				}	
				param.setValues(param_keyvalues);	
			}		
		}
		
		class ParamDiv extends DivRepFormElement {
			
			Integer id;
			DivRepTextBox name;
			DivRepTextArea params;
			DivRepSelectBox service;
			Integer previous_service_id;
			
			protected ParamDiv(DivRep parent, MeshConfigParamRecord rec) {
				super(parent);
				
				name = new DivRepTextBox(this);
				name.setLabel("Parameter Set Name");
				name.setRequired(true);
				name.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						testsdiv.load_keyvalues();
						testsdiv.redraw();
					}
				});
				
				params = new DivRepTextArea(this);
				params.setLabel("Parameters");
				params.setRequired(true);
				params.setHeight(250);

				service = new DivRepSelectBox(this);
				service.setLabel("Service Type");
				service.setRequired(true);
				service.setValues(service_types);
				service.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						//make sure this param is not already used by any tests
						for(TestDiv test : testsdiv.tests) {
							Integer param_id = test.param.getValue();
							if(param_id != null && param_id.equals(id)) {
								alert("This parameter set is currently used by 1 or more tests. Please unassociated this parameter set from all test before making this change.");
								service.setValue(previous_service_id);
								validate(); //need to revalidate to get rid of "this is a required field"
								service.redraw();
								return;
							}
						}
						previous_service_id = service.getValue();
	
						loadTemplate();
						
						testsdiv.load_keyvalues();
						testsdiv.redraw();
						showhide();
					}	
				});
			
				if(rec != null) {
					id = rec.id;
					name.setValue(rec.name);
					params.setValue(rec.params);
					service.setValue(rec.service_id);
					previous_service_id = rec.service_id;
				} else {
					//come up with a new ID (is this safe?)
					Integer nextid = 0;
					for(ParamDiv div : paramsdiv.params) {
						if(nextid <= div.id) {
							nextid = div.id+1;
						}
					}
					id = nextid;
				}
				showhide();
			}

			private void showhide() {
				params.setHidden(true);
				if(service.getValue() != null) {
					params.setHidden(false);
				}
			}
			
			private void loadTemplate() {
				ConfigModel config = new ConfigModel(context);
				String key = null;
				Integer service_id = service.getValue();
				if(service_id != null) {
					switch(service_id) {
					case 130: //net.perfSONAR.Bandwidth
						key = "meshconfig.default.params.net.perfSONAR.Bandwidth";
						break;
					case 131: //net.perfSONAR.Latency
						key = "meshconfig.default.params.net.perfSONAR.Latency";
						break;
					}
				}
				String template = "";
				if(key != null) {
					Config conf = config.new Config(config, key, "{\"na\":\"update me\"}");
					template = conf.getString();
				}
				params.setValue(template);
				params.redraw();
			}
			
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
		
				//vo / service / params
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4\">");
					service.render(out);
				out.write("</div>");
				out.write("<div class=\"span8\">");
					name.render(out);
				out.write("</div>");
				out.write("</div>");
				
				params.render(out);
				out.write("</div>");
			}
			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		
		class ParamsDiv extends DivRepFormElement {
			DivRepButton add;

			ArrayList<ParamDiv> params = new ArrayList<ParamDiv>();
			protected ParamsDiv(DivRep parent) {
				super(parent);
				
				add = new DivRepButton(this, "Add New Parameter Set") {
					protected void onClick(DivRepEvent e) {
						params.add(new ParamDiv(ParamsDiv.this, null));
						ParamsDiv.this.redraw();
					}				
				};
				add.addClass("btn");	
				
				//loading params
				MeshConfigParamModel model = new MeshConfigParamModel(context);
				try {
					for(MeshConfigParamRecord rec : model.getAll()) {
						ParamDiv div = new ParamDiv(context.getPageRoot(), rec);
						params.add(div);
					}
				} catch (SQLException e) {
					log.error("failed to load meshconfig tests", e);
				}
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span9\">");

				for(ParamDiv div: params) {
					div.render(out);
				}
				
				out.write("<p>");
				add.render(out);
				out.write("</p>");
				
				out.write("</div>");
				
				out.write("<div class=\"span3\">");
				//HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
				//view.render(out);
				out.write("</div>");
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}
		}
		
		class TestsDiv extends DivRepFormElement {
			DivRepButton add;

			ArrayList<TestDiv> tests = new ArrayList<TestDiv>();
			
			protected TestsDiv(DivRep parent) {
				super(parent);
				
				add = new DivRepButton(this, "Add New Test") {
					protected void onClick(DivRepEvent e) {
						tests.add(new TestDiv(TestsDiv.this, null));
						TestsDiv.this.redraw();
					}				
				};
				add.addClass("btn");

				//loading tests
				MeshConfigTestModel tmodel = new MeshConfigTestModel(context);
				try {
					for(MeshConfigTestRecord rec : tmodel.getAll()) {
						TestDiv tdiv = new TestDiv(context.getPageRoot(), rec);
						tests.add(tdiv);
					}
				} catch (SQLException e) {
					log.error("failed to load meshconfig tests", e);
				}
			}
			
			protected void load_keyvalues() {
				for(TestDiv test : tests) {
					test.load_keyvalues();
				}
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span9\">");

				for(TestDiv div: tests) {
					div.render(out);
				}
				
				out.write("<p>");
				add.render(out);
				out.write("</p>");
				
				out.write("</div>");
				
				out.write("<div class=\"span3\">");
				HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
				view.render(out);
				out.write("</div>");
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}
		}
		
		class GroupDiv extends DivRepFormElement {
			Integer id;
			DivRepTextBox name;
			DivRepSelectBox service;
			Integer previous_service_id;
			ResourceServiceListEditor resources;
			
			protected GroupDiv(DivRep parent, MeshConfigGroupRecord rec) {
				super(parent);
				
				name = new DivRepTextBox(this);
				name.setLabel("Group Name");
				name.setRequired(true);
				name.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						testsdiv.load_keyvalues();
						testsdiv.redraw();
					}
				});
				
				service = new DivRepSelectBox(this);
				service.setLabel("Service Type");
				service.setRequired(true);
				service.setValues(service_types);
				service.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						//make sure this group is not already used by any tests
						for(TestDiv test : testsdiv.tests) {
							Integer group_a = test.group_a.getValue();
							Integer group_b = test.group_b.getValue();
							if(
								(group_a != null && group_a.equals(id)) || 
								(group_b != null && group_b.equals(id))
							) {
								alert("This group is currently used by 1 or more tests. Please unassociated this group from all test before making this change.");
								service.setValue(previous_service_id);
								validate(); //need to revalidate to get rid of "this is a required field"
								service.redraw();
								return;
							}
						}
						previous_service_id = service.getValue();
	
						resources.clear();
						
						testsdiv.load_keyvalues();
						testsdiv.redraw();
						showhide();
					}	
				});
			
				final ResourceModel rmodel = new ResourceModel(context);
				final ResourceServiceModel smodel = new ResourceServiceModel(context);
				final ResourceServiceDetailModel dmodel = new ResourceServiceDetailModel(context);
				resources = new ResourceServiceListEditor(this) {
					protected ResourceServiceListEditor.ResourceInfo getDetailByResourceID(Integer id) throws SQLException {
						ResourceServiceListEditor.ResourceInfo info = new  ResourceServiceListEditor.ResourceInfo();
						info.rec = rmodel.get(id);
						if(service.getValue() != null) {
							ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), id, "endpoint");
							if(detail != null) {
								info.detail = detail.value;
							}
						}
						return info;
					}
					protected Collection<ResourceServiceListEditor.ResourceInfo> getAvailableResourceRecords() throws SQLException {
						ArrayList<ResourceServiceListEditor.ResourceInfo> recs = new ArrayList<ResourceServiceListEditor.ResourceInfo>();
						//find all resource/service record for currently selected service
						ArrayList<ResourceServiceRecord> rsrecs = smodel.getByServiceID(service.getValue());
						//lookup all resource record for each resource I found
						for(ResourceServiceRecord rsrec : rsrecs) {
							ResourceRecord rec = rmodel.get(rsrec.resource_id);
							if(rec.disable) continue;
							ResourceServiceListEditor.ResourceInfo info = new ResourceServiceListEditor.ResourceInfo();
							info.rec = rec;
							ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), rec.id, "endpoint");
							if(detail != null) {
								info.detail = detail.value;
							}
							recs.add(info);
						}
						return recs;
					}
				};
				resources.setLabel("Resources");
				resources.setRequired(true);
				
				if(rec != null) {
					id = rec.id;
					name.setValue(rec.name);
					service.setValue(rec.service_id);
					previous_service_id = rec.service_id;

					//should I really be loading from DB? 
					MeshConfigMemberModel model = new MeshConfigMemberModel(context);
					try {
						for(MeshConfigMemberRecord mrec : model.getByGroupID(rec.id)) {
							ResourceServiceListEditor.ResourceInfo info = resources.new ResourceInfo();
							info.rec = rmodel.get(mrec.resource_id);
							ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), rec.id, "endpoint");
							if(detail != null) {
								info.detail = detail.value;
							}
							resources.addSelected(info);
						}
					} catch (SQLException e1) {
						log.error("Failed to load resource info");
					}
				} else {
					//come up with a new ID
					Integer nextid = 0;
					for(GroupDiv div : groupsdiv.groups) {
						if(nextid <= div.id) {
							nextid = div.id+1;
						}
					}
					id = nextid;
				}
				
				showhide();
			}
			
			protected void showhide() {
				resources.setHidden(true);
				if(service.getValue() != null) {
					resources.setHidden(false);
				}
			}
			
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
		
				//vo / service / params
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span4\">");
					service.render(out);
				out.write("</div>");
				out.write("<div class=\"span8\">");
					name.render(out);
				out.write("</div>");
				out.write("</div>");
				
				resources.render(out);
				out.write("</div>");
			}
			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		
		class GroupsDiv extends DivRepFormElement {
			DivRepButton add;
			ArrayList<GroupDiv> groups = new ArrayList<GroupDiv>();

			protected GroupsDiv(DivRep parent) {
				super(parent);
				
				add = new DivRepButton(this, "Add New Group") {
					protected void onClick(DivRepEvent e) {
						groups.add(new GroupDiv(GroupsDiv.this, null));
						GroupsDiv.this.redraw();
					}				
				};
				add.addClass("btn");
				
				//load groups from DB
				MeshConfigGroupModel gmodel = new MeshConfigGroupModel(context);
				try {
					for(MeshConfigGroupRecord rec : gmodel.getAll()) {
						GroupDiv tdiv = new GroupDiv(context.getPageRoot(), rec);
						groups.add(tdiv);
					}
				} catch (SQLException e) {
					log.error("failed to load meshconfig tests", e);
				}
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span9\">");				
				for(GroupDiv div: groups) {
					div.render(out);
				}
				
				out.write("<p>");
				add.render(out);
				out.write("</p>");
				
				out.write("</div>");
				
				out.write("<div class=\"span3\">");
				//HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
				//view.render(out);
				out.write("</div>");
				
				out.write("</div>"); //row-fluid
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}
		}
		
		public ConfigForm(UserContext context, String origin_url) throws AuthorizationException, SQLException
		{	
			super(context.getPageRoot(), origin_url);
			this.context = context;
			
			//load service_types
			try {
				ServiceModel smodel = new ServiceModel(context);
				ArrayList<ServiceRecord> srecs = smodel.getByServiceGroupID(SERVICE_GROUP_PERFSONAR_MONIOTIRNG);
				Collections.sort(srecs, new Comparator<ServiceRecord> () {
					public int compare(ServiceRecord a, ServiceRecord b) {
						return a.name.compareToIgnoreCase(b.name);
					}
				});
				for(ServiceRecord srec : srecs) {
					//if(vo_rec.disable) continue;
					service_types.put(srec.id, srec.name);
				}
			} catch (SQLException e) {
				log.error("Failed to load perfsonar service records");
			}	
			
			new DivRepStaticContent(this,"<h2>Mesh Config Administrator</h2>");
			
			class TabContent extends DivRepFormElement {
				
				protected TabContent(DivRep parent) {
					super(parent);
					groupsdiv = new GroupsDiv(this);
					paramsdiv = new ParamsDiv(this);
					testsdiv = new TestsDiv(this);
				}
				
				@Override
				public void render(PrintWriter out) {
				
					BootTabView tabview = new BootTabView();
					tabview.addtab("Host Groups", renderGroupPane());
					tabview.addtab("Parameter Sets", renderParamPane());
					tabview.addtab("Tests", renderConfigPane());
					tabview.render(out);
				}

				@Override
				protected void onEvent(DivRepEvent e) {
					// TODO Auto-generated method stub
				}	
			};
			new TabContent(this);
		}
		
		protected IView renderConfigPane() {
			return new IView(){
				public void render(PrintWriter out) {
					testsdiv.render(out);
				}
			};
		}
		protected IView renderGroupPane() {
		
			return new IView(){
				public void render(PrintWriter out) {
					groupsdiv.render(out);
				}
			};
		}
		protected IView renderParamPane() {
			return new IView(){
				public void render(PrintWriter out) {
					paramsdiv.render(out);
				}
			};
		}
		
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected Boolean doSubmit() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
}
