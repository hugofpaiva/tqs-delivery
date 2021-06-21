/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "rider - ask for new order"], "isController": false}, {"data": [1.0, 500, 1500, "rider - get current order"], "isController": false}, {"data": [1.0, 500, 1500, "rider - get reviews statistics"], "isController": false}, {"data": [1.0, 500, 1500, "manager - get all stores' info"], "isController": false}, {"data": [1.0, 500, 1500, "manager - get all riders info"], "isController": false}, {"data": [1.0, 500, 1500, "rider - update order status"], "isController": false}, {"data": [1.0, 500, 1500, "manager - get general statistics"], "isController": false}, {"data": [1.0, 500, 1500, "manager - get riders' statistics"], "isController": false}, {"data": [1.0, 500, 1500, "manager - login"], "isController": false}, {"data": [1.0, 500, 1500, "client/store - make a purchase"], "isController": false}, {"data": [1.0, 500, 1500, "rider - login"], "isController": false}, {"data": [1.0, 500, 1500, "rider - get all their orders"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 578, 0, 0.0, 23.461937716262987, 2, 249, 7.0, 86.10000000000002, 89.0, 180.67000000000098, 38.81278538812785, 39.47399138379667, 13.870636814229117], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["rider - ask for new order", 35, 0, 0.0, 8.942857142857145, 6, 31, 7.0, 13.0, 29.39999999999999, 31.0, 4.30822255046775, 3.5004308222550464, 1.4388790158788773], "isController": false}, {"data": ["rider - get current order", 69, 0, 0.0, 4.536231884057969, 3, 11, 4.0, 7.0, 8.0, 11.0, 8.570363929946591, 6.974580021115389, 2.8958456247671096], "isController": false}, {"data": ["rider - get reviews statistics", 70, 0, 0.0, 3.3000000000000003, 2, 8, 3.0, 5.0, 5.450000000000003, 8.0, 8.565834557023983, 4.03196509422418, 2.894315192119432], "isController": false}, {"data": ["manager - get all stores' info", 25, 0, 0.0, 4.12, 3, 7, 4.0, 5.0, 6.399999999999999, 7.0, 8.719916288803628, 6.122675597314266, 2.912315791768399], "isController": false}, {"data": ["manager - get all riders info", 25, 0, 0.0, 6.840000000000001, 5, 18, 6.0, 7.400000000000002, 14.999999999999993, 18.0, 8.677542519958347, 5.092971732905242, 2.9320602655328014], "isController": false}, {"data": ["rider - update order status", 69, 0, 0.0, 6.666666666666669, 5, 13, 6.0, 9.0, 11.0, 13.0, 8.575689783743476, 4.103480184253045, 3.601119733407905], "isController": false}, {"data": ["manager - get general statistics", 25, 0, 0.0, 5.039999999999999, 4, 9, 5.0, 6.400000000000002, 8.399999999999999, 9.0, 8.726003490401396, 4.328915794066317, 2.948434773123909], "isController": false}, {"data": ["manager - get riders' statistics", 25, 0, 0.0, 3.6, 2, 7, 3.0, 4.400000000000002, 6.399999999999999, 7.0, 8.744316194473592, 4.320921869534803, 2.9717012067156348], "isController": false}, {"data": ["manager - login", 25, 0, 0.0, 91.44, 84, 132, 88.0, 111.60000000000001, 126.6, 132.0, 8.434547908232119, 5.675198739035088, 2.0345052083333335], "isController": false}, {"data": ["client/store - make a purchase", 70, 0, 0.0, 36.30000000000001, 10, 249, 17.0, 89.69999999999993, 234.70000000000007, 249.0, 24.339360222531294, 10.871558262343534, 13.619583405771905], "isController": false}, {"data": ["rider - login", 70, 0, 0.0, 88.5285714285714, 81, 202, 86.0, 90.0, 92.45, 202.0, 7.773459189339256, 5.207610355358134, 1.8674521099389227], "isController": false}, {"data": ["rider - get all their orders", 70, 0, 0.0, 10.428571428571432, 7, 24, 10.0, 13.0, 16.450000000000003, 24.0, 8.54283622162558, 34.821353658164504, 2.828145975713937], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 578, 0, null, null, null, null, null, null, null, null, null, null], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
