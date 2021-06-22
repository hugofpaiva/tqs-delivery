# JMeter test plans

This directory contains:
- 2 test JMeter test plans: `jmeter-test-plan-with-1thread-riders.jmx` and `jmeter-test-plan-with-less-endpoints.jmx`
- a script `run-jmeter-tests.sh` that was used to generate the results and log file, and the dashboard website
- results file: `results.jtl`; the file that contains the information from the `Simple Data Writer` listener
- log file: `jmeter.log`; a file produced when running the command in the script
- the directory `dashboard` that contains the website produced by the second command on the script

#### File `jmeter-test-plan-with-1thread-riders.jmx`

This file contains a test plan where almost all endpoins were used, except for one. Because all the threads are using the same user, there were some concurrency issues where the user was, for example, reviewing the same delivery twice, which produced some unwanted errors. A solution to this problem was to reduce the number of threads to one for the thread group where these endpoins were used. This is what was done here. The `Riders Pool` thread group, there is only one thread which does all the requests 70 times each.

This file was the first approach to this problem, and is kept here just to show what has been done. All the files related to the result were created using the other test plan.

#### File `jmeter-test-plan-with-less-endpoints.jmx`

Another solution to help prevent the mentioned problem could be to simply not make request to the conflicting endpoints, and this way it would still be possible to use many threads, allowing for a better test of our system. This was the approach we decided to use. Therefore, the files related to the results of the test that are present in this directory were created using this test plan.

