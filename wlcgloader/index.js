
var http = require('https');
var fs = require('fs');

var _ = require('lodash');
var async = require('async');
var wlcg = require('./wlcg');
var xml2js = require('xml2js');
var mysql = require('mysql');

var config = require('./config.json');

var con = mysql.createConnection(config.oim_dburl);

//used to find reomved records
var current_sites = [];
var current_endpoints = [];

function parseSite(insite) {
    /*
    site sample
    { '$': { ID: '117', PRIMARY_KEY: '196G0', NAME: 'wuppertalprod' },
        PRIMARY_KEY: [ '196G0' ],
        SHORT_NAME: [ 'wuppertalprod' ],
        OFFICIAL_NAME: [ 'Bergische Universitaet Wuppertal Fachbereich C - Physik, Wuppertal, Germany' ],
        GOCDB_PORTAL_URL: [ 'https://goc.egi.eu/portal/index.php?Page_Type=Site&id=117' ],
        HOME_URL: [ 'http://www.pleiades.uni-wuppertal.de/' ],
        CONTACT_EMAIL: [ 'lcg-admin@physik.uni-wuppertal.de' ],
        CONTACT_TEL: [ '+49 202 439-3521' ],
        GIIS_URL: [ 'ldap://grid-bdii.physik.uni-wuppertal.de:2170/mds-vo-name=wuppertalprod,o=grid' ],
        TIER: [ '2' ],
        COUNTRY_CODE: [ 'DE' ],
        COUNTRY: [ 'Germany' ],
        ROC: [ 'NGI_DE' ],
        PRODUCTION_INFRASTRUCTURE: [ 'Production' ],
        CERTIFICATION_STATUS: [ 'Certified' ],
        TIMEZONE: [ 'Europe/Berlin' ],
        LATITUDE: [ '51.245278' ],
        LONGITUDE: [ '7.149444' ],
        CSIRT_EMAIL: [ 'lcg-admin@physik.uni-wuppertal.de' ],
        DOMAIN: [ [Object] ],
        EXTENSIONS: [ '' ] }
    */
    var site = {};
    for(var key in insite) {
        var value = insite[key];
        switch(key) {
        case '$':
            //ignore
            break;
        case 'PRIMARY_KEY':
        case 'SHORT_NAME':
        case 'OFFICIAL_NAME':
        case 'SITE_DESCRIPTION':
        case 'GOCDB_PORTAL_URL':
        case 'HOME_URL':
        case 'SUBGRID':
        case 'ALARM_EMAIL':
        case 'CONTACT_EMAIL':
        case 'CONTACT_TEL':
        case 'GIIS_URL':
        case 'TIER':
        case 'COUNTRY_CODE':
        case 'COUNTRY':
        case 'ROC':
        case 'PRODUCTION_INFRASTRUCTURE':
        case 'CERTIFICATION_STATUS':
        case 'TIMEZONE':
        case 'CSIRT_EMAIL':
        case 'EXTENSIONS':
        case 'SITE_IP':
        case 'SITE_IPV6':
            //just unwrap
            site[key] = value[0]; 
            break;
        case 'LATITUDE':
        case 'LONGITUDE':
            site[key] = parseFloat(value[0]);
            break;
        case 'DOMAIN':
            var name = value[0].DOMAIN_NAME[0];
            site.DOMAIN_NAME = name;
            break;
        default:
            console.log("ignoring unknown key in wlcg site record: "+key+" = "+value);
        }
    }
    current_sites.push(site.PRIMARY_KEY);
    //console.dir(site);
    return site;
}

function parseEndpoint(inend) {
    /*
{ '$': { PRIMARY_KEY: '5G0' },
  PRIMARY_KEY: [ '5G0' ],
  HOSTNAME: [ 'hep001.kisti.re.kr' ],
  GOCDB_PORTAL_URL: [ 'https://goc.egi.eu/portal/index.php?Page_Type=Service&id=5' ],
  HOSTDN: [ '/C=KR/O=KISTI/O=GRID/O=KISTI/CN=host/hep001.kisti.re.kr' ],
  BETA: [ 'N' ],
  SERVICE_TYPE: [ 'CE' ],
  HOST_IP: [ '150.183.246.101' ],
  CORE: [ '' ],
  IN_PRODUCTION: [ 'Y' ],
  NODE_MONITORED: [ 'N' ],
  SITENAME: [ 'KR-KISTI-HEP' ],
  COUNTRY_NAME: [ 'South Korea' ],
  COUNTRY_CODE: [ 'KR' ],
  ROC_NAME: [ 'AsiaPacific' ],
  URL: [ '' ],
  EXTENSIONS: [ '' ] }
    */
    var end = {};
    for(var key in inend) {
        var value = inend[key];
        switch(key) {
        case '$':
            //ignore
            break;
        case 'PRIMARY_KEY':
        case 'HOSTNAME':
        case 'GOCDB_PORTAL_URL':
        case 'HOSTDN':
        case 'BETA':
        case 'SERVICE_TYPE':
        case 'HOST_IP':
        case 'HOST_IPV6':
        case 'CORE':
        case 'IN_PRODUCTION':
        case 'NODE_MONITORED':
        case 'SITENAME':
        case 'COUNTRY_NAME':
        case 'COUNTRY_CODE':
        case 'ROC_NAME':
        case 'URL':
        case 'ENDPOINTS':
        case 'EXTENSIONS':
        case 'HOST_OS':
        case 'HOST_ARCH':
            //just unwrap
            end[key] = value[0]; 
            break;
        default:
            console.log("ignoring unknown key in wlcg endpoint record: "+key+" = "+value);
        }
    }
    current_endpoints.push(end.PRIMARY_KEY);
    return end;
}

function getxml(method, cache_path, cb) {
    fs.stat(cache_path, function(err, stat) {
        var now = new Date().getTime();
        var endTime = 0;
        if(!err) {
            endTime = new Date(stat.ctime).getTime() + 3600*1000; //cache expires in 1 hour
        }
        if(now > endTime) {
            console.log(cache_path+" is too old (or doesn't exist).. reloading");
            wlcg.load_xml(method, cache_path, function(err, data) {
                fs.writeFile(cache_path, data, function(err){
                    if(err) throw(err);
                    cb(null, data);
                });
            });
        } else {
            console.log(cache_path+" is fresh.. reusing");
            fs.readFile(cache_path, cb);
        }
    });
}

function upsertSite(site, cb) {
    if(site.LONGITUDE == undefined) {
        site.LONGITUDE = null;
    }
    if(site.LATITUDE == undefined) {
        site.LATITUDE = null;
    }
    con.query("SELECT * FROM wlcg_site WHERE primary_key = "+con.escape(site.PRIMARY_KEY), function(err, rows) {
        if(err) throw err;
        if(rows.length == 0) {
            console.log("inserting new site "+site.PRIMARY_KEY);
            //console.dir(site);
            con.query("INSERT INTO wlcg_site (primary_key, short_name, official_name, longitude, latitude, country, timezone, contact_email) VALUES ("+
                con.escape(site.PRIMARY_KEY)+", "+
                con.escape(site.SHORT_NAME)+", "+
                con.escape(site.OFFICIAL_NAME)+", "+
                site.LONGITUDE+", "+
                site.LATITUDE+", "+
                con.escape(site.COUNTRY)+", "+
                con.escape(site.TIMEZONE)+", "+
                con.escape(site.CONTACT_EMAIL)+
            ")", cb);
        } else {
            var old = rows[0];
            if(
                old.short_name != site.SHORT_NAME ||
                old.official_name != site.OFFICIAL_NAME ||
                old.longitude != site.LONGITUDE ||
                old.latitude != site.LATITUDE ||
                old.country != site.COUNTRY ||
                old.timezone != site.TIMEZONE ||
                old.contact_email != site.CONTACT_EMAIL) {
                con.query("UPDATE wlcg_site SET "+
                    " short_name = "+con.escape(site.SHORT_NAME)+", "+
                    " official_name = "+con.escape(site.OFFICIAL_NAME)+", "+
                    " longitude = "+site.LONGITUDE+", "+
                    " latitude = "+site.LATITUDE+", "+
                    " country = "+con.escape(site.COUNTRY)+", "+
                    " timezone = "+con.escape(site.TIMEZONE)+", "+
                    " contact_email = "+con.escape(site.CONTACT_EMAIL)+
                " WHERE primary_key = "+con.escape(site.PRIMARY_KEY), cb);
            } else {
                //no change
                cb(null);
            }
        }
    });
    //console.log("inserting site:"+site.SHORT_NAME);
}

function upsertEndpoint(ep, cb) {
    async.parallel({
        site_id: function(next) {
            con.query("SELECT primary_key from wlcg_site WHERE short_name = "+con.escape(ep.SITENAME), function(err, rows) {
                if(err) return next(err);
                if(rows.length !== 1) {
                    console.log("couldn't find site_id:"+ep.SITENAME+" on endpoint hostname: "+ep.HOSTNAME);
                    next(null, null);
                } else {
                    next(null, rows[0].primary_key);
                }
            });
        },
        service_id: function(next) {
            con.query("SELECT id from service WHERE name = "+con.escape(ep.SERVICE_TYPE), function(err, rows) {
                if(err) return next(err);
                if(rows.length !== 1) {
                    //console.log("couldn't find service_id for :"+ep.SERVICE_TYPE+" on endpoint hostname: "+ep.HOSTNAME);
                    next(null, null); //set it to null
                } else {
                    next(null, rows[0].id);
                }
            });
        },
    }, function(err, fkeys) {
        if(err) return cd(err);
        if(fkeys.site_id == null) return cb(); //skip records with unknown site_id.. (marian says this is a bug?)
        var in_prod = (ep.IN_PRODUCTION == 'Y' ? 1 : 0);
        con.query("SELECT * FROM wlcg_endpoint WHERE primary_key = "+con.escape(ep.PRIMARY_KEY), function(err, rows) {
            if(rows.length == 0) {
                console.log("inserting new endpoint "+ep.PRIMARY_KEY);
                con.query("INSERT INTO wlcg_endpoint (primary_key, site_id, hostname, host_ip, service_type, service_id, in_production, roc_name) VALUES ("+
                    con.escape(ep.PRIMARY_KEY)+", "+
                    con.escape(fkeys.site_id)+", "+
                    con.escape(ep.HOSTNAME)+", "+
                    con.escape(ep.HOST_IP)+", "+
                    con.escape(ep.SERVICE_TYPE)+", "+
                    fkeys.service_id+", "+
                    in_prod+", "+
                    con.escape(ep.ROC_NAME)+
                ")", cb);
            } else {
                var old = rows[0];
                if(
                    old.site_id != fkeys.site_id  ||
                    old.hostname != ep.HOSTNAME ||
                    old.host_ip != ep.HOST_IP ||
                    old.service_type != ep.SERVICE_TYPE ||
                    old.service_id != fkeys.service_id ||
                    old.in_production != in_prod ||
                    old.roc_name != ep.ROC_NAME ) {
                    con.query("UPDATE wlcg_endpoint SET "+
                        " site_id = "+con.escape(fkeys.site_id)+", "+
                        " hostname = "+con.escape(ep.HOSTNAME)+", "+
                        " host_ip = "+con.escape(ep.HOST_IP)+", "+
                        " service_type = "+con.escape(ep.SERVICE_TYPE)+", "+
                        " service_id = "+fkeys.service_id+", "+
                        " in_production = "+in_prod+", "+
                        " roc_name = "+con.escape(ep.ROC_NAME)+
                    " WHERE primary_key = "+con.escape(ep.PRIMARY_KEY), cb);
                } else {
                    //no change
                    cb(null);
                }
             }
        });
    });
}

/* we can't just duplicate latency service to traceroute.. we don't know what the primary key will be, and guessing it will break the config later
function add_traceroute(endpoints) {
    var new_endpoints = [];
    endpoints.forEach(function(endpoint) {
        if(endpoint.SERVICE_TYPE == 'net.perfSONAR.Latency') {
            //look for traceroute endpoint under this site
            var found = false;
            endpoints.forEach(function(other) {
                if(other.SERVICE_TYPE == 'net.perfSONAR.Traceroute' && other.SITENAME == endpoint.SITENAME) {
                    found = true;
                }
            });
            if(!found) {
                console.log("adding traceroute service for "+endpoint.SITENAME);
                var new_endpoint = _.clone(endpoint, true);
                new_endpoint.SERVICE_TYPE = 'net.perfSONAR.Traceroute';
                new_endpoints.push(new_endpoint);
            }
        }
    });
    console.dir(new_endpoints);
    return endpoints.concat(new_endpoints);
}
*/

async.series([
    /*
    function(done) {
        console.log("get list of wlcg_endoint primary keys");
        con.query("SELECT primary_key from wlcg_endpoint", function(err, rows) {
            if(err) return done(err);
            rows.forEach(function(row) {
                current_wlcg_endpoint_keys.push(row.primary_key);
            });
            //console.dir(current_wlcg_endpoint_keys);
            done();
        });
    },
    function(done) {
        console.log("get list of wlcg_site primary keys");
        con.query("SELECT primary_key from wlcg_site", function(err, rows) {
            if(err) return done(err);
            rows.forEach(function(row) {
                current_wlcg_site_keys.push(row.primary_key);
            });
            //console.dir(current_wlcg_site_keys);
            done();
        });
    },
    */
    function(done) {
        getxml('get_site', 'cache/sites.xml', function(err, xml) {
            if(err) throw err;
            var parser = new xml2js.Parser();
            parser.parseString(xml, function(err, _data) {
                if(err) throw err;
                data = _data;
                var sites = _.map(data.results.SITE, parseSite);
                //store this for debugging purpose..
                fs.writeFileSync('cache/sites.parsed.json', JSON.stringify(sites, null, 4));
                console.log("upserting site table");
                async.eachSeries(sites, upsertSite, done);
            });
        });
    },
    function(done) {
        getxml('get_service_endpoint', 'cache/service_endpoints.xml', function(err, xml) {
            if(err) throw err;
            var parser = new xml2js.Parser();
            parser.parseString(xml, function(err, _data) {
                if(err) throw err;
                data = _data;
                var endpoints = _.map(data.results.SERVICE_ENDPOINT, parseEndpoint);
                //store this for debugging purpose..
                fs.writeFileSync('cache/service_endpoints.parsed.json', JSON.stringify(endpoints, null, 4));

                //endpoints = add_traceroute(endpoints);

                console.log("upserting endpoint table");
                async.eachSeries(endpoints, upsertEndpoint, done);
            });
        });
    },
    function(done) {
        con.query("SELECT primary_key FROM wlcg_endpoint", function(err, rows) {
            if(err) throw err;
            async.eachSeries(rows, function(row, next) {
                if(!~current_endpoints.indexOf(row.primary_key)) {
                    console.log("endpoint:"+row.primary_key+" gone .. removing");
                    con.query("DELETE from wlcg_endpoint WHERE primary_key = "+con.escape(row.primary_key), next);
                } else {
                    next();
                }
            }, done);
        });
    },
    function(done) {
        con.query("SELECT primary_key FROM wlcg_site", function(err, rows) {
            if(err) throw err;
            async.eachSeries(rows, function(row, next) {
                if(!~current_sites.indexOf(row.primary_key)) {
                    console.log("site:"+row.primary_key+" gone .. removing");
                    con.query("DELETE from wlcg_site WHERE primary_key = "+con.escape(row.primary_key), next);
                } else {
                    next();
                }
            }, done);
        });
    }
], function(err, cb) {
    if(err) throw err;
    console.log("all done.. calling oim/clear_cache");
    var ret = "";
    http.get("https://localhost/oim/rest?action=clear_cache", function(res) {
        if(res.statusCode != 200) {
            console.error("oim/clear_cache returned "+res.statusCode);
        }
        close_mysql();
    }).on('error', function(e) {
        console.error("couldn't make clear_cache call");
        console.error(e);
        close_mysql();
    }).on('data', function(chunk) {
        ret += chunk;
    }).on('end', function() {
        console.log(ret);
    });
});

function close_mysql() {
    con.end(function(err) {
        console.log("closed mysql");
    });
}

/*
setTimeout(10*1000, function() {
});
*/
