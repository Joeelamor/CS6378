# Advanced Operating System Project 1

## Team Member
- Jianjun Du (jxd151630)
- Jing Zhou (jxz160330)
- Zhang Lizhong (lxz160730)

## Complie
Run `mvn package` in the folder, compiled and packaged `Project1-1.0-SNAPSHOT.jar` would be in `target` folder.

## Run
To run the compiled jar file, use following command in `dcXX.utdallas.edu` VMs.

```
java -jar Project1-1.0-SNAPSHOT.jar <config file location>

# Example:
# java -jar Project1-1.0-SNAPSHOT.jar ~/launch/config.txt
```

Also, the script `launch.sh` and `cleanup.sh` is modified to use the above command with `$HOME/launch/config.txt` as default configuration file.

## Program Output

The program will print all incoming/outgoing connection requests in standard output, followed with all received messages like this: `received from 0 with {0=0, 1=1, 2=2, 3=1, 4=2, 5=1, 6=1, 7=1, 8=2, 9=1, 10=2}`

After `10 * node number` seconds, the program will stop sending message and print k-hop neighbors for each k in following format.

```
Node 5's khop: {
	0:[5], 
	1:[0, 7, 8, 9], 
	2:[1, 3, 4, 6, 10], 
	3:[2]
}
```
