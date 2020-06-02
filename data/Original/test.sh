#!/bin/bash

docker exec -it -u hadoop nodemaster /home/hadoop/hive/bin/hive -f /home/hadoop/dataset/tpch/createtable_loaddata.hql
sh utils/copy_data_to_docker.sh
sh utils/remove_data.sh
docker exec -it -u hadoop nodemaster /home/hadoop/hadoop/bin/hdfs balancer -filesInfo
docker exec -it -u hadoop nodemaster /home/hadoop/hadoop/bin/hdfs balancer -clusterInfo

for j in {1..22}; do
	for i in {1..5}; do
		docker exec -it -u hadoop nodemaster /home/hadoop/hive/bin/hive -f /home/hadoop/dataset/tpch/query/query-"$j".hql;
		mkdir -p q"$j"/q"$j"_"$i"/hdfs_read_write
		docker cp nodemaster:/home/hadoop/ExplainQuery/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/JobBlocks/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/QueryDataBlocks/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/QueryJobID/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/FilesInfo/ q"$j"/q"$j"_"$i"/
		docker cp nodemaster:/home/hadoop/ClusterInfo/ q"$j"/q"$j"_"$i"/
		docker cp node2:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node2.log
		docker cp node3:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node3.log
		docker cp node4:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node4.log
		docker cp node5:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node5.log
		docker cp node6:/home/hadoop/hdfs_read.log q"$j"/q"$j"_"$i"/hdfs_read_write/hdfs_read_node6.log
		docker cp nodemaster:/home/hadoop/QueryExecutionTime.log q"$j"/q"$j"_"$i"/QueryExecutionTime.log
		sh utils/remove_data.sh
	done
done

