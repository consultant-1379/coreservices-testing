package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.results.FrameWorkResult;

public class DBManager implements Manager {
    private final String server;
    private final String username;
    private final String password;
    private final String dbPort;
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

    public DBManager(final String server, final String username, final String password, final String dbPort) {

        final String urlPrefix = "://";
        this.server = server.substring(server.indexOf(urlPrefix) + urlPrefix.length(), server.lastIndexOf(":"));
        this.username = username;
        this.password = password;
        this.dbPort = dbPort;
    }

    public String executeFixedQuery(final String query) throws ArrestItException {
        String result = "";
        con = null;
        stmt = null;
        rs = null;
        final int queryTimeOutInSecs = SQL_QUERY_TIMEOUT_IN_MINUTES * 60;
        slf4jLogger.debug("Master Value query being sent: " + query);

        try {
            con = getConnection();
            stmt = con.createStatement();
            slf4jLogger.debug("Master Query going to DB " + query);
            stmt.setQueryTimeout(queryTimeOutInSecs);
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                result = rs.getObject(1).toString();
            }
        } catch (final SQLException e) {
            slf4jLogger.error(e.getMessage());
            result = "Sql Exception";
            throw new ArrestItException("Sql Exception while sending Master query to db: " + e.getMessage() + "\t" + query);

        } finally {
            closeConnection();
        }

        return result;
    }

    public Map<String, String> executeQuery(final String query, final Map<String, String> sqlVars, final String rowIndex, final TestCase tc)
            throws ArrestItException {
        con = null;
        stmt = null;
        rs = null;
        final int queryTimeOutInSecs = SQL_QUERY_TIMEOUT_IN_MINUTES * 60;

        slf4jLogger.debug("Query being sent: " + query);
        slf4jLogger.debug("Sql variables to be found: " + sqlVars.keySet());

        try {
            slf4jLogger.debug("Query going to DB " + query);
            con = getConnection();
            stmt = con.createStatement();
            stmt.setMaxRows(1);
            stmt.setQueryTimeout(queryTimeOutInSecs);
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                for (final String key : sqlVars.keySet()) {

                    if (rs.getObject(key) == null && StringUtils.containsIgnoreCase(query, "sum")) {
                        sqlVars.put(key, "0");

                    } else if (rs.getObject(key) == null) {
                        sqlVars.put(key, null);
                    } else {
                        sqlVars.put(key, rs.getObject(key).toString());
                    }
                }
            }
            if (tc.getTestResultHolder() != null) {
                tc.getTestResultHolder().addTestResult(new FrameWorkResult("Successfully executed sql:  " + query, rowIndex, true));
            }

        } catch (final SQLException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Sql Exception while sending query to db: " + e.getMessage() + "\t" + query);
        } finally {
            closeConnection();
        }

        return sqlVars;
    }

    public List<Row> executeQuery(final String query) throws ArrestItException {
        final List<Row> results = new ArrayList<Row>();
        con = null;
        stmt = null;
        rs = null;
        final int queryTimeOutInSecs = SQL_QUERY_TIMEOUT_IN_MINUTES * 60;

        slf4jLogger.debug("Query being sent: " + query);

        try {
            con = getConnection();
            stmt = con.createStatement();
            stmt.setQueryTimeout(queryTimeOutInSecs);
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                final ResultSetMetaData rsmd = rs.getMetaData();
                final Map<String, String> columnValue = new HashMap<String, String>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    final String columnName = rsmd.getColumnLabel(i);
                    columnValue.put(columnName, rs.getString(i));
                }
                results.add(new Row(columnValue));
            }

        } catch (final SQLException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Sql Exception while sending query to db: " + e.getMessage() + "\t" + query);
        } finally {
            closeConnection();
        }

        return results;
    }

    public void executeUpdate(final String sql) throws ArrestItException {
        con = null;
        stmt = null;
        rs = null;
        final int queryTimeOutInSecs = SQL_QUERY_TIMEOUT_IN_MINUTES * 60;

        slf4jLogger.debug("Query being sent: " + sql);

        try {
            con = getConnection();
            stmt = con.createStatement();
            stmt.setQueryTimeout(queryTimeOutInSecs);
            stmt.executeUpdate(sql);
        } catch (final SQLException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Sql Exception while sending query to db: " + e.getMessage() + "\t" + sql);
        } finally {
            closeConnection();
        }
    }

    private Connection getConnection() throws ArrestItException {
        try {
            final Driver driver = (Driver) Class.forName(DB_DRIVER).newInstance();

            final Properties props = new Properties();
            props.put("REMOTEPWD", ",,CON=" + ARREST_IT_CONNECTION_NAME);
            props.put("user", this.username);
            props.put("password", this.password);

            final String url = "jdbc:sybase:Tds:" + this.server + ":" + dbPort;

            return driver.connect(url, props);
        } catch (final InstantiationException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not instantiate DB driver" + e.getMessage());
        } catch (final IllegalAccessException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not access DB driver" + e.getMessage());
        } catch (final ClassNotFoundException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not find DB driver class" + e.getMessage());
        } catch (final SQLException e) {
            slf4jLogger.error(e.getMessage());
            throw new ArrestItException("Could not connect to dB" + e.getMessage());
        }
    }

    public void closeConnection() {
        if (rs != null) {
            try {
                rs.close();

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnectionOnShutDown() {
        if (rs != null) {
            try {
                rs.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
