#!/bin/bash
hive -f createtable_loaddata.hql
for i in {1..4}; do hive -f query/query-1.hql; done
for i in {1..25}; do hive -f query/query-2.hql; done
for i in {1..1}; do hive -f query/query-3.hql; done
for i in {1..3}; do hive -f query/query-4.hql; done
for i in {1..1}; do hive -f query/query-5.hql; done
for i in {1..1}; do hive -f query/query-6.hql; done
for i in {1..1}; do hive -f query/query-7.hql; done
for i in {1..1}; do hive -f query/query-8.hql; done
for i in {1..1}; do hive -f query/query-9.hql; done
for i in {1..1}; do hive -f query/query-10.hql; done
for i in {1..1}; do hive -f query/query-11.hql; done
for i in {1..1}; do hive -f query/query-12.hql; done
for i in {1..22}; do hive -f query/query-13.hql; done
for i in {1..3}; do hive -f query/query-14.hql; done
for i in {1..1}; do hive -f query/query-15.hql; done
for i in {1..37}; do hive -f query/query-16.hql; done
for i in {1..1}; do hive -f query/query-17.hql; done
for i in {1..2}; do hive -f query/query-18.hql; done
for i in {1..23}; do hive -f query/query-19.hql; done
for i in {1..11}; do hive -f query/query-20.hql; done
for i in {1..1}; do hive -f query/query-21.hql; done
for i in {1..1}; do hive -f query/query-22.hql; done