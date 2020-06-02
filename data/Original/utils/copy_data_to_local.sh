#Copy All
mkdir -p data/hdfs_read_write

docker cp nodemaster:/home/hadoop/ExplainQuery/ data/
docker cp nodemaster:/home/hadoop/JobBlocks/ data/
docker cp nodemaster:/home/hadoop/QueryDataBlocks/ data/
docker cp nodemaster:/home/hadoop/QueryJobID/ data/
docker cp nodemaster:/home/hadoop/FilesInfo/ data/
docker cp nodemaster:/home/hadoop/ClusterInfo/ data/

docker cp node2:/home/hadoop/hdfs_read.log data/hdfs_read_write/hdfs_read_node2.log
docker cp node3:/home/hadoop/hdfs_read.log data/hdfs_read_write/hdfs_read_node3.log
docker cp node4:/home/hadoop/hdfs_read.log data/hdfs_read_write/hdfs_read_node4.log
docker cp node5:/home/hadoop/hdfs_read.log data/hdfs_read_write/hdfs_read_node5.log
docker cp node6:/home/hadoop/hdfs_read.log data/hdfs_read_write/hdfs_read_node6.log

docker cp nodemaster:/home/hadoop/QueryExecutionTime.log data/QueryExecutionTime.log
