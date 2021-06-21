#!bin/bash

jmeter -n -t tqs-jmeter-test-plan.jmx -l results.jtl
jmeter -g results.jtl -o dashboard/

