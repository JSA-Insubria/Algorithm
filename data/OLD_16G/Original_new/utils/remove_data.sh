#Remove All

docker exec -u hadoop -it nodemaster rm -r /home/hadoop/ExplainQuery/
docker exec -u hadoop -it nodemaster rm -r /home/hadoop/JobBlocks/
docker exec -u hadoop -it nodemaster rm -r /home/hadoop/QueryDataBlocks/

docker exec -u hadoop -it node2 rm /home/hadoop/hdfs_read.log
docker exec -u hadoop -it node3 rm /home/hadoop/hdfs_read.log
docker exec -u hadoop -it node4 rm /home/hadoop/hdfs_read.log
docker exec -u hadoop -it node5 rm /home/hadoop/hdfs_read.log
docker exec -u hadoop -it node6 rm /home/hadoop/hdfs_read.log

docker exec -u hadoop -it nodemaster rm /home/hadoop/QueryExecutionTime.log
