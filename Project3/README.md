# Advanced Operating System Project 3


## Team Member
- Jianjun Du (jxd151630)
- Jing Zhou (jxz160330)
- Lizhong Zhang (lxz160730)

## Complie
Run `mvn package` in the folder, compiled and packaged `Project3-1.0-SNAPSHOT-jar-with-dependencies.jar` would be in `target` folder.

## Run
To run the compiled jar file, use following command in `dcXX.utdallas.edu` VMs.

```
java -jar Project3-1.0-SNAPSHOT-jar-with-dependencies.jar <config file location>

# Example:
# java -jar Project3-1.0-SNAPSHOT-jar-with-dependencies.jar ~/launch/config.txt
```

Also, the script `launch.sh` and `cleanup.sh` are modified to use the above command with `$HOME/launch/config.txt` as default configuration file.

## Output
After running the project, it will output three files under current folder, first one is `record-with-paramenters.txt` which records each node requests, enters and
quits from critical section with timestamp. Second one is `result-with-parameters.txt` file which calculates synchronization delay, response time, message 
complexity and system throughput, and the last file is `result-with-parameters-average.txt` file which update the latest running result for different parameters.  ``