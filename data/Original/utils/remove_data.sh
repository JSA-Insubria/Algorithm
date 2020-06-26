#!/bin/bash

firstNode=2
lastNode=11

#Remove All

docker exec -u hadoop -it nodemaster rm -r /home/hadoop/ExplainQuery/
docker exec -u hadoop -it nodemaster rm -r /home/hadoop/JobBlocks/
docker exec -u hadoop -it nodemaster rm -r /home/hadoop/QueryDataBlocks/
docker exec -u hadoop -it nodemaster rm -r /home/hadoop/QueryJobID/

for (( i=$firstNode; i<=$lastNode; i++ )) do
	docker exec -u hadoop -it node$i rm /home/hadoop/hdfs_read.log
	docker exec -u hadoop -it node$i rm /home/hadoop/DataStreamer.log
done

docker exec -u hadoop -it nodemaster rm /home/hadoop/QueryExecutionTime.log
docker exec -u hadoop -it nodemaster rm /home/hadoop/QueryCPUTime.log
docker exec -u hadoop -it nodemaster rm /home/hadoop/DataStreamer.log
docker exec -u hadoop -it nodemaster rm /home/hadoop/containers.log
