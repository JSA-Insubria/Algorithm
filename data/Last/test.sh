#!/bin/bash

firstNode=2
lastNode=11

docker exec -it -u hadoop nodemaster /home/hadoop/hive/bin/hive -f /home/hadoop/dataset/tpch/createtable_loaddata.hql
sh utils/copy_data_to_docker.sh
sh utils/remove_data.sh
docker exec -it -u hadoop nodemaster /home/hadoop/hadoop/bin/hdfs balancer -filesInfo
docker exec -it -u hadoop nodemaster /home/hadoop/hadoop/bin/hdfs balancer -clusterInfo

for j in {1..22}; do
	for i in {1..5}; do
		docker exec -it -u hadoop nodemaster /home/hadoop/hive/bin/hive -f /home/hadoop/dataset/tpch/query/query-"$j".hql;
		mkdir -p q"$j"/q"$j"_"$i"/hdfs_read_write
		mkdir -p q"$j"/q"$j"_"$i"/data_streamer
		docker cp nodemaster:/home/hadoop/ExplainQuery/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/JobBlocks/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/QueryDataBlocks/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/QueryJobID/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/FilesInfo/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/ClusterInfo/ q"$j"/q"$j"_"$i"/
		
		for (( n=$firstNode; n<=$lastNode; n++ )) do
			docker cp node$n:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node$n.log
			docker cp node$n:/home/hadoop/DataStreamer.log q"$j"/q"$j"_"$i"/data_streamer/DataStreamer_node$n.log
		done

		docker cp nodemaster:/home/hadoop/containers.log q"$j"/q"$j"_"$i"/containers.log
		docker cp nodemaster:/home/hadoop/DataStreamer.log q"$j"/q"$j"_"$i"/data_streamer/DataStreamer_nodemaster.log
		docker cp nodemaster:/home/hadoop/QueryExecutionTime.log q"$j"/q"$j"_"$i"/QueryExecutionTime.log
		sh utils/remove_data.sh
	done
done

