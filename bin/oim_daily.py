#!/usr/bin/python
import urllib
import httplib
import json
import sys
import M2Crypto

M2Crypto.SSL.Connection.clientPostConnectionCheck = None

conn = M2Crypto.httpslib.HTTPSConnection("localhost")

#do quota daly reset
#conn = httplib.HTTPConnection("localhost")
conn.request("POST", "/oim/rest?action=reset_daily_quota&version=1")
response = conn.getresponse()
if response.status != 200:
	print "reset_daily_quota (request) failed:",response.status,response.reason
	sys.exit(response.status)
else:
	data = json.loads(response.read())
	status = data["status"]
	if status != "OK":
		print "reset_daily_quota failed:", data["detail"]

conn.close()
#process expired certs
conn2 = M2Crypto.httpslib.HTTPSConnection("localhost")

conn2.request("POST", "/oim/rest?action=find_expired_cert_request&version=1")
response2 = conn2.getresponse()
if response2.status != 200:
	print "find_expired_cert_request (request) failed: ",response2.status,response2.reason
	sys.exit(response2.status)
else:
	data2 = json.loads(response2.read())
	status = data2["status"]
	if status != "OK":
		print "find_expired_cert_request failed:", data2["detail"]

conn.close()
