package org.deidentifier.arx.examples;

import org.deidentifier.arx.*;

/**
 * @className : Example4DB
 * @Author : mulming@163.com
 * @Date : 2020/12/30
 * @Version 1.0
 * @Description : TODO
 */
public class Example4DB {
    public static void main(String[] args) throws Exception {
        //1 create source
        // if db2
        DataSource jdbcSource = DataSource.createJDBCSource("jdbc:db2://ip:port/dbName",
                "userName",
                "pswd",
                null,
                "schemaName",
                "tableName");

        // if oracle
        DataSource jdbcSource1 = DataSource.createJDBCSource("jdbc:oracle:thin:@//ip:port/serviceName",
                "userName",
                "pswd",
                "schemaName",
                "schemaName",
                "tableName");

        // if mysql
        DataSource jdbcSource2 = DataSource.createJDBCSource("jdbc:mysql://ip:port/dbName",
                "userName",
                "pswd",
                "dbname",
                "dbname",
                "tableName");

        // if selserver
        DataSource jdbcSource3 = DataSource.createJDBCSource("jdbc:sqlserver://ip:port;DatabaseName=xxx;selectMethod=cursor",
                "userName",
                "pswd",
                "dbName",
                "schemaName",
                "tableName");

        //2 create data
        Data data = Data.create(jdbcSource);


    }
}
