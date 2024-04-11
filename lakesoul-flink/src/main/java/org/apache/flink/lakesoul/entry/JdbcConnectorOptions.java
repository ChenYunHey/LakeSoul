package org.apache.flink.lakesoul.entry;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

public class JdbcConnectorOptions {
    public static final ConfigOption<String> JDBC_CONNECTOR = ConfigOptions
            .key("jdbc.connector")
            .stringType()
            .defaultValue("jdbc")
            .withDescription("Specify what connector to use, here should be 'jdbc'.");

    public static final ConfigOption<String> JDBC_URL = ConfigOptions
            .key("jdbc.url")
            .stringType()
            .noDefaultValue()
            .withDescription("The JDBC database url.");

    public static final ConfigOption<String> JDBC_TABLE_NAME = ConfigOptions
            .key("jdbc.table.name")
            .stringType()
            .noDefaultValue()
            .withDescription("The name of JDBC table to connect.");

    public static final ConfigOption<String> JDBC_DRIVER = ConfigOptions
            .key("jdbc.driver")
            .stringType()
            .noDefaultValue()
            .withDescription("The class name of the JDBC driver to use to connect to this URL, if not set, it will automatically be derived from the URL.");


}
