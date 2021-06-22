#!bin/bash


~/Downloads/apache-jmeter-5.4.1/bin/jmeter -n -t jmeter-test-plan-with-less-endpoints.jmx -l results.jtl
~/Downloads/apache-jmeter-5.4.1/bin/jmeter -g results.jtl -o dashboard/

