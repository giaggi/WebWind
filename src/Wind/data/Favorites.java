package Wind.data;

import Wind.Core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Favorites {

    private static final Logger LOGGER = Logger.getLogger(Favorites.class.getName());

    public Favorites() {
    }

    public List<Long> getFavorites(String userid) {

        List<Long> list = new ArrayList<Long>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM favorites WHERE personid='" + userid + "' ORDER BY id;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long spotid = rs.getLong("spotid");
                list.add(spotid);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public long isFavorite(String personid, long spotid) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM favorites WHERE personid='" + personid + "' AND spotid=" + spotid + ";";
            ResultSet rs = stmt.executeQuery(sql);
            long res;
            if (rs.next()) {
                res = rs.getLong("id");
            } else {
                res = -1;
            }

            rs.close();
            stmt.close();
            conn.close();
            return res;

        } catch (SQLException se) {
            se.printStackTrace();
            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    public long insert(String personid, long spotid) {

        LOGGER.info("insert: personid=" + personid + "spotid=" + spotid);
        int lastid;

        long id = isFavorite(personid,spotid);
        if (id > 0)
            return id;

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "INSERT INTO favorites (spotid, personid)" +
                    " VALUES (" + spotid + ",'" + personid + "') ";
            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }
            stmt.close();
            conn.close();
            return lastid;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return 0;
        }
    }

    public static long delete(String personid, long spotid) {

        int deletedItems = 0;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM favorites WHERE personid='" + personid + "'";
            if (spotid != -1)
                sql += " AND spotid=" + spotid + ";";
            Statement stmt = conn.createStatement();
            deletedItems = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return deletedItems;
    }
}
