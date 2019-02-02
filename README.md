# CCVolti
Code Clone detector by Vectorization based On Lsh and Tf-Idf.

# Requirement
 - Windows
 - Java 1.8
 
# Setup
## Step 1
The project uses Maven with Maven Assembly Plugin.
The following command builds a runnable jar `ccvolti.jar`:

         mvn package

The project  includes Eclipse project file and `pom.xml` for dependencies. 

## Step 2
Place `ccvolti.jar` and `FALCONN` directory in the same directory.

# Usage
```
java -jar clonedetector.jar -d [dirname] -l [lang] <*options>
```

```
 -cs,--charset <charset>    set the name of character encoding ( default: UTF-8 )
 -d, --dir <dirname>        select directory for clone detection
 -h, --help                 display help
 -l, --lang <lang>          select language from following ( default: java )
                                * java
                                * c
                                * csharp
 -oc,--outputcsv <*.csv>    select csv file name for output
 -ot,--outputtxt <*.txt>    select text file name for output
 -on,--outputnotifier       select notifier file name for output
 -ocs,--outputcloneset      select text file name for output clone set
     --sim <value>          set threshold of similarity for clone detection
                                ( 0.0<=sim<=1.0 ) ( default: 0.9 )
     --size <value>         set threshold of size for method  ( 0<=size ) ( default: 50 )
     --sizeb <value>        set threshold of size for block  ( 0<=size ) ( default: same as size for method )
 -t, --threads <value>      set the number of threads. 0 indicates max threads. ( default: 1 )
```

# Licence
This software is released under the MIT License, see LICENSE.

This project uses the following components.

 - ANTLR4 (http://www.antlr.org/license.html): BSD License
 - Apache (http://www.apache.org/licenses/LICENSE-2.0):  Apache License, Version 2.0
 - FALCONN (https://falconn-lib.org/): MIT License
 - Cygwin (https://cygwin.com/licensing.html): LGPL License
 - JGraphT (http://jgrapht.org/): EPL License
