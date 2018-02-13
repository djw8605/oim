package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.SideContentView;

public class OIMRegistrationServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		//Authorization auth = context.getAuthorization();
		
		BootMenuView menuview = new BootMenuView(context, "home");
		BootPage page = new BootPage(context, menuview, new Content(context), createSideView(context));
		page.addExCSS("home.css");

		GenericView header = new GenericView();
		//header.add(new HtmlView("<h1>OSG Information Management System</h1>"));
		//header.add(new HtmlView("<p class=\"lead\">Defines the topology used by various OSG services based on the <a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18\">OSG Blueprint Document</a></p>"));

		page.setPageHeader(header);
		
		page.render(response.getWriter());
	}
	
	class Content implements IView {
		UserContext context;
		public Content(UserContext context) {
			this.context = context;
		}
		
		@Override
		public void render(PrintWriter out) {
			//out.write("<div>");
			Authorization auth = context.getAuthorization();
		
			out.write("<div class='twikiToc'> <ul>");
			out.write("<li> <a href='/oim/oimregistration#OIM_Registration_Instructions'> OIM Registration Instructions</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#About_this_Document'> About this Document</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Requirements'> Requirements</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Registering_Yourself'> Registering Yourself</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#Required_Information'> Required Information</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Optional_Information'> Optional Information</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href='/oim/oimregistration#Facility_Registration'> Facility Registration</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Site_Registration'> Site Registration</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#Required_Information_AN1'> Required Information</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Optional_Information_AN1'> Optional Information</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href='/oim/oimregistration#Resource_Group_Registration'> Resource Group Registration</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#Required_Information_AN2'> Required Information</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href='/oim/oimregistration#Resource_or_Service_Registration'> Resource or Service Registration</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#Required_Information_AN3'> Required Information</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Optional_Information_AN2'> Optional Information</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href='/oim/oimregistration#VO_Registration'> VO Registration</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#Required_Information_AN4'> Required Information</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Optional_Information_AN3'>Optional Information</a>");
			out.write("</li></ul> ");
			out.write("</li> <li> <a href='/oim/oimregistration#Support_Center_Registration'> Support Center Registration</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#Removal_or_Deletion_of_OIM_Regis'> Removal or Deletion of OIM Registrations</a> <ul>");
			out.write("<li> <a href='/oim/oimregistration#When_to_Request_Deletion_of_an_O'> When to Request Deletion of an OIM Registration</a>");
			out.write("</li> <li> <a href='/oim/oimregistration#When_to_Request_Disabling_of_an'> When to Request Disabling of an OIM Registration</a>");
	
			out.write("</li></ul> ");
			out.write("</li></ul> ");
			out.write("</li></ul> ");
			out.write("</div>");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='About_this_Document'></a> About this Document </span></h2>");
			out.write("<p />");
			out.write("OSG often requires users and other collaborators to register some contact information with the Grid Operations Center. ");
			out.write("There are some situations in which the GOC will need to contact OSG users, such as in matters concerning the OSG <a "); 
			out.write("href='http://osg-docdb.opensciencegrid.org/cgi-bin/RetrieveFile?docid=86&amp;extension=pdf' target='_top'>acceptable use policy</a> ");
			out.write("and <a href='http://osg-docdb.opensciencegrid.org/cgi-bin/RetrieveFile?docid=87&amp;extension=pdf' target='_top'>service agreement</a> ");
			out.write("that apply to all OSG participants. A detailed explanation of the different fields and terms for registration follows. ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Requirements'></a> Requirements </span></h2> ");
			out.write("<p />");
			out.write("Any user who wishes to register in OSG Information Management System (OIM) will need a valid OSG approved Certifying Authority (CA) x509 Certificate. If you have an x509 certificate, but are not sure if OSG trusts the issuer, the list of Certifying Authorities that OSG trusts can be found in the <a href='http://software.grid.iu.edu/cadist/' target='_top'>CA distribution of the software cache</a>. In order for OIM to recognize an x509 certificate, a user must have done one of the following with his or her certificate:");
			out.write("<p /> <ul>");
			out.write("<li> loaded it directly in his or her web browser <strong>OR</strong>");
			out.write("</li> <li> placed it in a certificate managing software (such as mac keychain) that is compatible with his or her browser. ");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("If you do not have a valid <a href='http://software.grid.iu.edu/cadist/' target='_top'>OSG approved Certifying Authority (CA)</a> x509 Certificate, but you do work with OSG, please submit a certificate request at the <a href='http://software.grid.iu.edu/cert/' target='_top'>OSG certificate request page</a>. ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Registering_Yourself'></a> Registering Yourself </span></h2>");
			out.write("<p />");
			out.write("Before you register information about any OSG service, organization or other entity, you will need to give us some basic contact information about yourself. If you have not already done this, once you arrive at <a href='https://oim.opensciencegrid.org/' target='_top'>OIM</a> with your x509 certificate, you will be prompted to register with the following message:");
			out.write("<p />");
			out.write("<b>'OIM requires the Distinguished Name (DN) of an X509 certificate issued by an OSG-approved Certifying Authority (CA) to be registered in order to proceed. ");
			out.write("The following unregistered DN was detected from your web browser: ");
			out.write("/DC=org/DC=(your Certifying Authority appears here)/OU=People/CN=(your common name appears here)");
			out.write("Please register your certificate's DN with the OIM system using the Register menu item above, so you can be allowed to proceed further.");
			out.write("If you believe, you have previously registered this DN, or are not sure how to register, or have any other questions, please open a ticket with the OSG Grid Operations Center (GOC).'</b>");
			out.write("<p />");
			out.write("Click on 'Login' button and your browser should ask for your certificate stored on your browser or your keychain. Select your OSG certified DN. Then, you should see Register button which will forward you to the registration form.");
			out.write("<p />");
			out.write("<h3><a name='Required_Information'></a> Required Information </h3>");
			out.write("The following information is <strong>required</strong> in the fields on the <strong>Register</strong> form");
			out.write("<p /> <ul>");
			out.write("<li> Your Certificate DN - This should be automatically gleaned from your x509 certificate and will appear in the format DC=org/DC=SomeCA/OU=People/CN=SomeUniqueName");
			out.write("</li> <li> Your Full Name - Please enter this in the first-name last-name format, i.e. 'John Smith' instead of 'Smith, John' or  'John'");
			out.write("</li> <li> Your Email Address - This is our primary method of contact if a problem occurs. You will be asked to re-enter your email address for confirmation.");
			out.write("</li> <li> Primary Phone - the telephone number where it is easiest to reach you in the event of an emergency or if email contact fails.");
			out.write("</li> <li> City - If you work and live in different cities, enter the city where you work.");
			out.write("</li> <li> State - If you work and live in different US states, enter the state where you work.");
			out.write("</li> <li> Zipcode - If you work and live in different zipcodes, enter the zipcode where you work.");
			out.write("</li> <li> Country");
			out.write("</li> <li> Time Zone - If you work and live in different time zones,enter the time zone where you work.");
			out.write("</li> <li> Profile - Enter a short explanation of your OSG affiliation. Full Bios are accepted if you like.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h3><a name='Optional_Information'></a> Optional Information </h3>");
			out.write("The following information is <strong>optional</strong> but helpful. If you do not wish to give certain information here, or it is inapplicable, please leave it blank rather than entering something like 'none.'");
			out.write("<p /> <ul>");
			out.write("<li> Secondary Email - a backup email address if the first becomes unavailable for any reason.");
			out.write("</li> <li> Primary Phone Extension - Your private extension, if applicable, for your primary phone.");
			out.write("</li> <li> Secondary Phone - a telephone number to use in the event that the primary phone is unavailable.");
			out.write("</li> <li> Secondary Phone Extension - Your private extension, if applicable, for your secondary phone.");
			out.write("</li> <li> SMS Address - an email address that will trigger a text message to your phone. For example, the number +1 (444) 555-3333 might be <a"); 
			out.write("href='mailto&#58;4445553333&#64;vtext&#46;com'>4445553333&#64;vtext.com</a> (for a verizon cell phone), <a href='mailto&#58;4445553333@txt.att.net'>4445553333@txt.att.net</a> (for an AT&amp;T cell phone) or <a href='mailto:4445553333@messaging.sprintpcs.com'>4445553333@messaging.sprintpcs.com</a> (for a sprint cell phone). If you would like to participate, you can contact your cellular provider for instructions or simply test whether your SMS address works on your phone by sending yourself a test email. The GOC only uses SMS addresses for critical alerts.");
			out.write("</li> <li> Enter Additional Contact Preferences - This is a text area where you can explain to us how you would like your contact information to be used.");
			out.write("</li> <li> Address Line 1 - Your street address, such as '112 maple street'");
			out.write("</li> <li> Address Line 2 - Your building address or other more detailed coordinates, if applicable, such as '3rd floor' 'Research Technology Department' 'Room 58' 'Apartment 13' 'Care of Rita Hayworth' or such items.");
			out.write("</li> <li> Instant Messaging Information - This is designed to only be human readable, so you may enter information here in whatever form you like, but be sure to specify your username and the service you are using. If you are using a jabber service, please also include your domain. <ul>");
			out.write("<li> For example, the following input is acceptable: 'aim: OSGdude jabber: <a href='mailto&#58;osgdude&#64;iupui&#46;edu'>osgdude&#64;iupui.edu</a> google: <a href='mailto&#58;osgdude&#64;gmail&#46;com'>osgdude&#64;gmail.com</a> 2nd jabber (but only for fnal items) <a href='mailto&#58;osgdude&#64;fnal&#46;gov'>osgdude&#64;fnal.gov</a>.' ");
			out.write("</li> <li> No one will attempt to contact you by this method unless you request it.");
			out.write("</li></ul> ");
			out.write("</li> <li> Use OSG TWiki - Put a checkmark in this checkbox if you wish to create an account for the <a href='https://twiki.grid.iu.edu' target='_top'>OSG TWiki</a>. The TWiki is OSG's collaborative documentation environment.");
			out.write("</li> <li> OSG TWiki ID - This should be a combination of upper and lowercase letters without spaces and generally follows the format FirstnameLastname, for example Marilyn Monroe would be MarilynMonroe rather than Marilyn Monroe or marilynmonroe. This will typically be generated automatically, but if there is already a TWiki account listed with your first and last name, this field will be blank, allowing you to either tether your existing TWiki account to your x509 certificate, or to choose a TWiki username if you have never generated an account.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("Once you have completed all of the required fields and any optional fields you wish to enter, you may click the 'Submit' button and you will be registered and admitted into the OIM System.");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Facility_Registration'></a> Facility Registration </span></h2>");
			out.write("<p />");
			out.write("To register a new facility, make sure you have logged in to OIM, then select Topology. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim54-show\" class=\"showLink\" onclick=\"showHide('oim54');return false;\">show more</a></p><div id=\"oim54\" class=\"more\">");
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim54-hide\" class=\"hideLink\" onclick=\"showHide('oim54');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-5.png' border='0'>");
			out.write("</div>");
			out.write("<p />");
			out.write("You should see 'Add Facility' button toward the top right corner of the page. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim71-show\" class=\"showLink\" onclick=\"showHide('oim71');return false;\">show more</a></p><div id=\"oim71\" class=\"more\">");

                        out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim71-hide\" class=\"hideLink\" onclick=\"showHide('oim71');return false;\">hide</a></p>");


			out.write("<img src='/oim/images/OIM-7.png' border='0'>");
                        out.write("</div>");
			
			out.write("<p />");
			out.write("A 'facility' in OIM terms is simply an institution where a resource or service is located, for example: Indiana University, Brookhaven National Laboratory, National Science Foundation DC). A facility registration just requires two things: <ul>");
			out.write("<li> Facility Name (required) - A human readable name for your facility. Spaces are acceptable.");
			out.write("</li> <li> Description (optional) - An expanded description of this facility. ");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Site_Registration'></a> Site Registration </span></h2>");
			out.write("<p />");
			out.write("To register a new site, make sure you have logged in to OIM, then select Topology.");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim5-show\" class=\"showLink\" onclick=\"showHide('oim5');return false;\">show more</a></p><div id=\"oim5\" class=\"more\">");
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim5-hide\" class=\"hideLink\" onclick=\"showHide('oim5');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-5.png' border='0'>");
			out.write("</div>");
			out.write("<p />");
			out.write("Navigate to the Facility that you'd like to add your site in, then click 'Add Site' button. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"reg11-show\" class=\"showLink\" onclick=\"showHide('reg11');return false;\">show more</a></p><div id=\"reg11\" class=\"more\">");
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"reg11-hide\" class=\"hideLink\" onclick=\"showHide('reg11');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/oim-site-reg-1.png' border='0'>");
			out.write("</div>");

			out.write("<p />");
			out.write("A \"site\" in OIM is smaller than a facility, and typically represents an academic department or a computer cluster. ");
			out.write("<p />");
			out.write("<h3><a name='Required_Information_AN1'></a> Required Information </h3>");
			out.write("The following information is <strong>required</strong> in the fields on the site registration form <ul>");
			out.write("<li> Select the facility that contains this site - A site should be housed within a registered OSG facility. If the facility has not been registered, please complete the facility registration before proceeding with the site registration.");
			out.write("</li> <li> Enter the site's short name - A site's short name is generally an acronym that is easily stated (for example \"AGLT2\" for ATLAS Great Lakes Tier 2, 'CIGI' for CyberInfrastructure and Geospatial Information out.write(Laboratory, etc)");
			out.write("</li> <li> Support Center - A site should have an associated support center which will take in inquiries related to this site and its resources.");
			out.write("</li> <li> City - The city where a site is located.");
			out.write("</li> <li> Zipcode - The postal code where a site is located.");
			out.write("</li> <li> Country - The country where a site is located.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h3><a name='Optional_Information_AN1'></a> Optional Information </h3>");
			out.write("The following information is <strong>optional</strong> but helpful. If you do not wish to give certain information here, or it is inapplicable, please leave it blank rather than entering something like 'none.' <ul>");
			out.write("<li> Street Address");
			out.write("</li> <li> State");
			out.write("</li> <li> Latitude");
			out.write("</li> <li> Longitude");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Resource_Group_Registration'></a> Resource Group Registration </span></h2>");
			out.write("<p />");
			out.write("A resource group is a logical grouping of CEs, SEs, etc. that make up one unit. Resource groups are referred to as 'sites' by many people on the OSG. ");
			out.write("<p />");
			out.write("To register a new resource group, make sure you have logged in to OIM, then select Topology. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim51-show\" class=\"showLink\" onclick=\"showHide('oim51');return false;\">show more</a></p><div id=\"oim51\" class=\"more\">");
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim51-hide\" class=\"hideLink\" onclick=\"showHide('oim51');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-5.png' border='0'>");
			out.write("</div>");
			out.write("<p />");
			out.write("<p />");
			out.write("Navigate to the Facility &amp; Site that you'd like to register your resource group in, then click either the 'Add Production Resource Group' or 'Add ITB Resource Group' button depending on which is appropriate.");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim6-show\" class=\"showLink\" onclick=\"showHide('oim6');return false;\">show more</a></p><div id=\"oim6\" class=\"more\">");			
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim6-hide\" class=\"hideLink\" onclick=\"showHide('oim6');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-6.png' border='0'>");
			out.write("</div>");
			out.write("<p />");
			out.write("<h3><a name='Required_Information_AN2'></a> Required Information </h3> <ul>");
			out.write("<li> Name - a name for the resource group. A short unique name for this resource group that will appear in <a href='https://myosg.grid.iu.edu/' target='_top'>MyOSG</a> or other catalogs as an identifier for out.write(\"this resource <strong>the  Name should not include spaces. Use a dash (-) or underscore (_) instead.  If spaces are present, they will automatically be converted to underscores.</strong> *Also note that any special characters apart from [A-Z], [a-z], [0-9], [-_] will be removed by the validation code.");
			out.write("</li> <li> Site - the associated site with this resource group.");
			out.write("</li> <li> OSG Grid Type - Select whether this is a production (OSG) or test resource (OSG-ITB).");
			out.write("</li> <li> Description - A description of this resource group.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Resource_or_Service_Registration'></a> Resource or Service Registration </span></h2>");
			out.write("<p />");
			out.write("To register a new resource, make sure you have logged in to OIM, then select Topology. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"reg1-show\" class=\"showLink\" onclick=\"showHide('reg1');return false;\">show more</a></p><div id=\"reg1\" class=\"more\">");
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"reg1-hide\" class=\"hideLink\" onclick=\"showHide('reg1');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/oim-resource-reg_1.png' border='0'>");
			out.write("</div>");

			out.write("<p />");
			out.write("Navigate to the Resource Group that you'd like to register your resource in, then click the 'Add Resource' button.");
			out.write("<p />");
			
			out.write("<a href=\"/oim/oimregistration#\" id=\"reg2-show\" class=\"showLink\" onclick=\"showHide('reg2');return false;\">show more</a></p><div id=\"reg2\" class=\"more\">");	
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"reg2-hide\" class=\"hideLink\" onclick=\"showHide('reg2');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/oim-resource-reg_2.png' border='0'>");
			out.write("</div>");

			out.write("<p />");
			out.write("The instructions below give descriptions of each of the fields to be filled in on the form.");
			out.write("<p />");
			out.write("<p />");
			out.write("<h3><a name='Required_Information_AN3'></a> Required Information </h3>");
			out.write("The following information is <strong>required</strong> in the fields on the resource registration field.");
			out.write("<p /> <ul>");
			out.write("<li> <strong>Name</strong> - a short unique name for this resource that will appear in <a href='https://myosg.grid.iu.edu/' target='_top'>MyOSG</a> or other catalogs as an identifier for this resource <strong>the  Name should not include spaces. Use a dash (-) or underscore (_) instead.  If spaces are present, they will automatically be converted to underscores.</strong> *Also note that any special characters apart from [A-Z], [a-z], [0-9], [-_] will be removed by the validation code.");
			out.write("</li> <li> <strong>Fully Qualified Domain Name</strong> - The fully qualified domain name <FQDN> for this resource that the catalog will use for contacting this resource for additional detailed information.");
			out.write("</li> <li> <strong>Site</strong> - The associated site for this resource.");
			out.write("</li> <li> <strong>Resource Group</strong> - The associated resource group for this resource.");
			out.write("</li> <li> <strong>Short Description</strong> - A short description of your resource.");
			out.write("</li> <li> <strong>Information URL</strong> - This should point at a web page description of your OSG resource with information about usage, access, and other information you deem necessary to use your resource.");
			out.write("</li> <li> <strong>Service</strong> - You can select which services your resource will run by selecting a service from the service menu. You can add more services by clicking the 'Add New Service' link. This is where you specify whether this resource is a Compute Element, a Storage Element, or other services.");
			out.write("</li> <li> <strong>VO Owners</strong> - Select the VO and percentage of ownership that the given VO has.  If more than one VO owns a resource, then you can add more VOs by clicking the 'Add New Owner' link.");
			out.write("</li> <li> <strong>Contact Information</strong> - Contacts for various resource attributes will be added here. <ul>");
			out.write("<li> <strong>Administrative Contact - Primary</strong> - This is the person we should call if a problem with the resource is not getting resolved via the OSG Support Center Model. Choose <strong>Assign New</strong>, from here you can find a currently registered OIM Contact or Add an Unlisted Contact. Add an Unlisted Contact should be used for mailing lists and non-registered contacts. (<strong>Note</strong> we would like all non-automated contacts to be registered with OIM and will actively pursue registration of individuals who are entered into the Unlisted Contact status.");
			out.write("</li> <li> <strong>Security Contact - Primary</strong> - This is the person we should call if there is a security incident that affects the resource. Choose <strong>Assign New</strong>, from here you can find a currently registered OIM Contact or Add an Unlisted Contact. Add an Unlisted Contact should be used for mailing lists and non-registered contacts. (<strong>Note</strong> we would like all non-automated contacts to be registered with OIM and will actively pursue registration of individuals who are entered into the Unlisted Contact status.  ");
			out.write("</li> <li> <strong>Resource Report Contact</strong> - This is the person that will be contacted when reports about the resource are distributed.  You can add multiple people here.");
			out.write("</li> <li> <strong>Acceptable Use Policy (AUP) Agreement</strong> You must agree to the Acceptable Use Policies.");
			out.write("</li></ul> ");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h3><a name='Optional_Information_AN2'></a> Optional Information </h3>");
			out.write("<p /> <ul>");
			out.write("<li> <strong>FQDN Aliases</strong> - To add a DNS Alias to your resource, click the Add New Alias link.");
			out.write("</li> <li> <strong>Administrative Contact - Secondary/Tertiary</strong> - A backup to the primary admin. The same process as described for primary admin is used for secondary admin. Please do not repeat the primary contact information for secondary contact.");
			out.write("</li> <li> <strong>Security Contact - Secondary/Tertiary</strong> - A backup to the primary security contact. The same process as described for primary security contact is used for secondary security contact. Please do not repeat the primary contact information for secondary contact.");

			out.write("</li> <li> <strong>Miscellaneous Contact</strong> - This is a contact that is just associated with the resource, but doesn't have a specific role and/or doesn't need to be contacted for security or administrative issues.");
			out.write("</li> <li> <strong>WLCG Interoperability Information</strong> - You can check this checkbox if you have a resource that is associated with WLCG. You should be prepared to enter whether you want to participate in WLCG BDII, monitoring and accounting, your storage and tape capacity, as well as your HEPSPEC and <span class='twikiNewLink'>KS12K<a href='/bin/edit/Operations/KS12K?topicparent=Operations.OIMRegistrationInstructions' rel='nofollow' title='KS12K (this topic does not yet exist; you can create it)'>?</a></span> values.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='VO_Registration'></a> VO Registration </span></h2>");
			out.write("<p />");
			out.write("To register a new Virtual Organization in OIM, click the 'Virtual Organizations' link near the top of the page. ");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim4-show\" class=\"showLink\" onclick=\"showHide('oim4');return false;\">show more</a></p><div id=\"oim4\" class=\"more\">"); out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim4-hide\" class=\"hideLink\" onclick=\"showHide('oim4');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-4.png' border='0'>");
			out.write("</div>");

			out.write("<p />");
			out.write("Then click the \"Add New Virtual Organization\" button to the right.");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim2-show\" class=\"showLink\" onclick=\"showHide('oim2');return false;\">show more</a></p><div id=\"oim2\" class=\"more\">"); 	
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim2-hide\" class=\"hideLink\" onclick=\"showHide('oim2');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-2.png' border='0'>");
			out.write("</div>");

			out.write("<p />");
			out.write("If this VO is a sub-VO, you can check the appropriate checkbox and select the parent VO.");
			out.write("<p />");
			out.write("<a href=\"/oim/oimregistration#\" id=\"oim3-show\" class=\"showLink\" onclick=\"showHide('oim3');return false;\">show more</a></p><div id=\"oim3\" class=\"more\">"); 	
			out.write("<p><a href=\"/oim/oimregistration#\" id=\"oim3-hide\" class=\"hideLink\" onclick=\"showHide('oim3');return false;\">hide</a></p>");
			out.write("<img src='/oim/images/OIM-3.png' border='0'>");
			out.write("</div>");	
			out.write("<p />");
			out.write("The items below give descriptions of each of the fields to be filled in on the form:");
			out.write("<h3><a name='Required_Information_AN4'></a> Required Information </h3> <ul>");
			out.write("<li> <strong>Name</strong> - A short and unique name for the VO that will be used as an identifier in various places of the OSG infrastructure");
			out.write("</li> <li> <strong>VO Long Name</strong> - The full name of the VO without acronyms.");
			out.write("</li> <li> <strong>Support Center</strong> - Choose your SC from the dropdown list.");
			out.write("</li> <li> <strong>Description</strong> - Enter some text to introduce your VO.");
			out.write("</li> <li> <strong>Community</strong> - Describe the users and common purpose that comprises your VO.");
			out.write("</li> <li> *Application Description - brief description of the application(s) being run on OSG including resource requirements at a level useful for resource providers to consider (or a URL to such description)");
			out.write("</li> <li> <strong>This VO has or will have users who do OSG-dependent scientific research</strong> - This should remain checked unless you are registering a VO which: <ul>");
			out.write("<li> does not use OSG resources <strong>AND</strong>");
			out.write("</li> <li> wants to donate resources to OSG <strong>OR</strong>");
			out.write("</li> <li> only wants to use OSG as a certificate provider.");
			out.write("</li></ul> ");
			out.write("</li> <li> <strong>Enter an Application Description</strong> - Describe what jobs typically do on a resource.");
			out.write("</li> <li> <strong>Primary URL</strong> - URL for a human readable information about this VO.");
			out.write("</li> <li> <strong>Field of Science</strong> - Select the appropriate field of science that your VO works with. You may select more than one, or add a new field at the bottom of the list.");
			out.write("</li> <li> <strong>Contacts</strong> section: Choose appropriate contacts by searching for name of a person, and selecting one.  <ul>");
			out.write("<li> If you are not sure what a certain contact type means, please refer to <a href='/oim/oimdefinitions#Contact' target='_top'>the definitions page</a>.");
			out.write("</li> <li> If a contact, whose name you are searching for, does not already exist in OIM, then you can instructing him or her to follow <a"); 
			out.write("href='https://www.opensciencegrid.org/bin/view/Operations/OIMRegistrationInstructions#Registering_Yourself' target='_top'>these instructions</a>.");
			out.write("</li></ul> ");
			out.write("</li> <li> <strong>Report Names</strong> - This section allows you to define report names for this VO. More information is included on the VO Registration Form.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h3><a name='Optional_Information_AN3'></a> Optional Information </h3> <ul>");
			out.write("<li> <strong>AUP URL</strong> - URL for a human readable statement of the VOs usage policy.");
			out.write("</li> <li> <strong>Membership Service URL (VOMS)</strong> - the URL for the <span class='twikiNewLink'>VOMS service<a"); 
			out.write("href='/bin/edit/Documentation/AdminDocVOMS?topicparent=Operations.OIMRegistrationInstructions' rel='nofollow' title='VOMS service (this topic does not yet exist; you can create it)'>?</a></span>");
			out.write("</li> <li> Purpose URL - URL for a human readable statement about the purpose of this VO");
			out.write("</li> <li> Support URL - URL that OSG will provide to members of this VO seeking user support ");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("To complete registration, click the 'Submit' button.");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Support_Center_Registration'></a> Support Center Registration </span></h2>");
			out.write("<p />");
			out.write("Support Centers -- large or small -- in OSG operations are charged with creating, receiving, and responding to trouble tickets from the OSG Grid Operations Center (GOC). ");
			out.write("<p />");
			out.write("To register a new support center, click on the '*Support Centers*' top menu item, and then click on the '*Add New Support Center*' button on the right hand side.  The instructions below give descriptions of each of the fields to be filled in on the form. <ul>");
			out.write("<li> <strong>Support Center Name</strong> - A shortened form of the Support Center's name. (example: GOC)");
			out.write("</li> <li> <strong>Support Center Long Name</strong> - A full name for the Support Center. (example: Grid Operations Center)");
			out.write("</li> <li> <strong>Brief Description</strong> - a one line description of this support center.");
			out.write("</li> <li> <strong>Community Served</strong> - A brief description of the community supported.");
			out.write("</li> <li> <strong>Contacts</strong> section: Choose appropriate contacts by searching for name of a person, and selecting one.  <ul>");
			out.write("<li> If you are not sure what a certain contact type means, please refer to <a href='/oim/definition#Contact' target='_top'>the definitions page</a>.");
			out.write("</li> <li> If a contact, whose name you are searching for, does not already exist in OIM, then you can add them by instructing him or her to follow <a"); 
			out.write("href='https://www.opensciencegrid.org/bin/view/Operations/OIMRegistrationInstructions#Registering_Yourself' target='_top'>these instructions</a>.");
			out.write("</li></ul> ");
			out.write("</li> <li> Click submit when you are done with data entry");
			out.write("</li> <li> <strong>Acceptable Use Policy (AUP) Agreement</strong> You must agree to the Acceptable Use Policies.");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<strong>Follow-up</strong>: Once you submit a new SC registration, a <a href='https://ticket.grid.iu.edu/goc/list/open' target='_top'>ticket</a> will be created under name, and appropriate parties including yourself will be notified via email. Follow the instructions on the email to ensure your new registration is activated and ready to go!");
			out.write("<p />");
			out.write("<h2 class='twikinetRoundedAttachments'><span class='twikinetHeader'><a name='Removal_or_Deletion_of_OIM_Regis'></a> Removal or Deletion of OIM Registrations </span></h2>");
			out.write("<p />");
			out.write("Entities registered in OIM will often deprecate or terminate their relationship with OSG. In most scenarios, it is more appropriate to disable an OIM entry than it is to delete it. We have found that we will still receive inquiries from time to time that reference these retired entities and the information we store in OIM remains useful.");
			out.write("<p />");
			out.write("<h3><a name='When_to_Request_Deletion_of_an_O'></a> When to Request Deletion of an OIM Registration </h3>");
			out.write("<p />");
			out.write("If any of the following situations apply, contact <a href='https://www.opensciencegrid.org/bin/view/ReleaseDocumentation/HelpProcedure#Grid_Operations_Center' target='_top'>the Grid Operations Center</a> with a deletion request. If the request is made by email, please ensure that your email is digitally signed.");
			out.write("<p /> <ul>");
			out.write("<li> There are duplicate OIM entries");
			out.write("</li> <li> There are fraudulent OIM entries");
			out.write("</li> <li> There are orphaned entries (for example, when all of the resources are moved out of one resource group and it is now empty)");
			out.write("</li></ul> ");
			out.write("<p />");
			out.write("<h3><a name='When_to_Request_Disabling_of_an'></a> When to Request Disabling of an OIM Registration </h3>");
			out.write("<p />");
			out.write("If any of the following situations apply, contact <a href='https://www.opensciencegrid.org/bin/view/ReleaseDocumentation/HelpProcedure#Grid_Operations_Center' target='_top'>the Grid Operations Center</a> with a disable request. If the request is made by email, please ensure that your email is digitally signed.");
			out.write("<p /> <ul>");
			out.write("<li> A person has ceased to be employed by OSG.");
			out.write("</li> <li> A computing resource is being retired or removed from OSG");
			out.write("</li> <li> A Virtual Organization or is being dissolved or will no longer use OSG.");
			out.write("</li> <li> A Support Center will no longer support OSG.");
			out.write("</li></ul> ");
			out.write("<p />");
			
		
			//	out.write("</div>");
		
		}
	}
    
	    private SideContentView createSideView(UserContext context)
	{
		SideContentView contentview = new SideContentView();
		Authorization auth = context.getAuthorization();
		
		
		//contentview.add(new LinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures", "Operating Procedures", true));

		if(auth.isUser()) {
		    //contentview.addContactLegend();
		}
		
		return contentview;
	}
	
	@SuppressWarnings("serial")
	class Confirmation extends DivRep
	{
		final ContactRecord crec;
		final ContactModel cmodel;
		final UserContext context;
		
		public Confirmation(Integer contact_id, UserContext _context) throws SQLException {
			super(_context.getPageRoot());
			
	    	cmodel = new ContactModel(_context);
	    	crec = (ContactRecord) cmodel.get(contact_id);//.clone();	    	
	    	context = _context;
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
					
		}	
	}
}
