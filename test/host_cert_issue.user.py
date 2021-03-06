#!/usr/bin/python
import urllib
import httplib
import time

import simplejson
import base64

import M2Crypto
ctx = M2Crypto.SSL.Context()
ctx.load_cert("/home/hayashis/.globus/soichi.2014.pem")

request_id = 3931

headers = {'Content-type': "application/x-www-form-urlencoded", 'User-Agent': 'OIMGridAPIClient/0.1 (OIM Grid API)'}
conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx)
conn.set_debuglevel(1)
params = urllib.urlencode({'host_request_id': request_id}, doseq=True)

conn.request('POST', "/oim/rest?action=host_certs_issue", params, headers)
response = conn.getresponse()
data = simplejson.loads(response.read())
cookie = response.getheader("set-cookie")

#debug
print response.status, response.reason
print data

conn.close()
