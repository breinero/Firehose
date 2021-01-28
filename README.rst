========
Firehose
========

:Description: A MongoDB csv import tool
:Author: Bryan Reinero <breinero@gmail.com>

Overview 
========

Firehose is a instrumented, multithreaded eximport tool for parsing delimeter separated files (e.g. csv), and benchmark the import speed. Reading a CSV file and pushin those records into MongoDB is a three step process,

- Read the next line from the file
- Parse the line, converting it into a object prepped for insertion
- Insert the new object into MongoDB

It's helpful to know how much time each of these steps take so I can identify bottlnecks in larger, long-running import jobs.Firehose collects and averages the duration of each step, and then pretty-print the running average to the console in real time, refreshing the console each second.

::

 { 
    threads: 2, 
    "lines read": 100000, 
    samples: {
        units: "microseconds",
        "reporting interval ms": 5,
        total: {
            mean: 221.32, 
            median: 181.5, 
            std: 82.68293015354203, 
            count: 50, 
            total: 11066.0
        },
        readline: {
            mean: 4.160000000000002, 
            median: 1.0, 
            std: 14.412806097073416, 
            count: 50, 
            total: 208.0
        },
        build: {
            mean: 14.291666666666666, 
            median: 15.0, 
            std: 3.439188012365297, 
            count: 48, 
            total: 686.0
        },
        insert: {
            mean: 170.28571428571428, 
            median: 158.0, 
            std: 39.36368885152915, 
            count: 49, 
            total: 8344.0
        }
    } 
 }

This output tells me that inserts are taking an average of 170 microseconds, as averaged over a time interval of 5 milliseconds. During this interval I inserted 49 documents. 

Build and Run Firehose
-------------------------------
 
::

 $ gradle run --args="-f test.csv -d , -h _id:float,count:float,sum:float,name:string"

Usage
-----

.. list-table::
   :header-rows: 1
   :widths: 10,25,20,90

   * - **option**
     - **long form**
     - **type**
     - **description**
   * - -cr
     - --noPretty
     -        
     - print out in CR-delimited lines. Default is console mode pretty printing (when possible)
   * - -f,
     - --file 
     - <filepath>               
     - filename to import
   * - -fs,
     - --fsync 
     -                   
     - write concern: wait for page flush
   * - -h,
     - --headers 
     - <name:type>         
     - ',' delimited list of columns
   * - -j,
     - --journal
     -                
     - enable write concern wait for journal commit
   * - -m,
     - --mongos 
     - <host:port>           
     - ',' delimited list of mongodb hosts to connect to. Default localhost:27017
   * - -ns,
     - --namespace 
     - <namespace>    
     - target database and collection this work will use (format: 'db.col')
   * - -pi,
     - --printInterval  
     - <seconds>
     - print output every n seconds
   * - -ri,
     - --reportInterval
     - <seconds>        
     - average stats over a time interval of i milliseconds
   * - -t,
     - --threads 
     - <threads>         
     - number of worker threads. Default 1
   * - -v,
     - --verbose
     -            
     - Enable verbose output
   * - -wc,
     - --writeConcern 
     - <concern>   
     - write concern. Default = w:1

JMX Integration
---------------

Firehose uses Java Management Extensions (JMX) to make it even easier monitor and manage your application. Firehose exposes Interval data with MBeans so you can gather performance metrics from a remote JMX client such as JConsole. Additionally The worker pool can be stopped, started or resized at will from the remote client without having to restart the application. More information on JMX is available `in the documentation <http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html>`_.

Dependencies
------------

Firehose is supported and somewhat tested on Java 1.11

Additional dependencies are:
    - `MongoDB Java Driver <http://docs.mongodb.org/ecosystem/drivers/java/>`_
    - `JUnit 4 <http://junit.org/>`_
    - `Apache Commons CLI 1.2 <http://commons.apache.org/proper/commons-cli/>`_
    - `Apache Commons Math 3 3.4 <http://commons.apache.org/proper/commons-math/>`_

    
License
-------
Copyright (C) {2013}  {Bryan Reinero}

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.


Disclaimer
----------
This software is not supported by MongoDB, Inc. under any of their commercial support subscriptions or otherwise. Any usage of Firehose is at your own risk. Bug reports, feature requests and questions can be posted in the Issues section here on github.

To Do
-----
- Accept piped input from stdine
- Write Javadocs
- Accept json input
- Accept mongoexport formated csv's
- fix README formatting
