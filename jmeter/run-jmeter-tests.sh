#!bin/bash

jmeter -n -t jmeter-test-plan-with-less-endpoints.jmx -l results.jtl
jmeter -g results.jtl -o dashboard/

