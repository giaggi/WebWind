package windalarm.meteodata;

//import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;

import Wind.Core;
import Wind.Device;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giacomo on 07/06/2015.
 */
public class MeteoStationData {

    private static final Logger LOGGER = Logger.getLogger(MeteoStationData.class.getName());

    int id;
    public Double speed;
    public Double averagespeed = -1.0;
    public String direction;
    public Double directionangle;
    public Double trend;
    //public String date;
    //public String time;
    public java.util.Date datetime;
    public Double temperature;
    public Double pressure;
    public Double humidity;
    public Double rainrate;
    public java.util.Date sampledatetime;
    public String spotName = "name";
    public String source = "source";
    public Integer spotID = -1;
    public String webcamurl = "";

    private static String[] directionSymbols = {"E", "ESE", "SE", "SSE", "S",
            "SSW", "SW", "SWW", "W",
            "WNW", "NW", "NNW", "N",
            "NNE", "NE", "ENE"};
    private ArrayList<ArrayList<String>> symbolList = new ArrayList<ArrayList<String>>();

    public MeteoStationData() {

        ArrayList<String> symbols;

        /// RIABILITARE
        /*symbols = Lists.newArrayList("E"); // 0
        symbolList.add(symbols);
        symbols = Lists.newArrayList("NEE", "ENE");//1
        symbolList.add(symbols);
        symbols = Lists.newArrayList("NE", "EN");//2
        symbolList.add(symbols);
        symbols = Lists.newArrayList("NNE", "ENE");//3
        symbolList.add(symbols);
        symbols = Lists.newArrayList("N");//4
        symbolList.add(symbols);
        symbols = Lists.newArrayList("NWN", "NNW", "NON", "NNO");//5
        symbolList.add(symbols);
        symbols = Lists.newArrayList("NW", "WN", "NO", "ON");//6
        symbolList.add(symbols);
        symbols = Lists.newArrayList("WNW", "NWW", "ONO", "NON");//7
        symbolList.add(symbols);
        symbols = Lists.newArrayList("W", "O");//8
        symbolList.add(symbols);
        symbols = Lists.newArrayList("WSW", "SWW", "OSO", "SSO");//9
        symbolList.add(symbols);
        symbols = Lists.newArrayList("SW", "WS", "SO", "OS");//10
        symbolList.add(symbols);
        symbols = Lists.newArrayList("SSW", "SWS", "SSO", "SOS");//11
        symbolList.add(symbols);
        symbols = Lists.newArrayList("S");//12
        symbolList.add(symbols);
        symbols = Lists.newArrayList("SSE", "SEE");//13
        symbolList.add(symbols);
        symbols = Lists.newArrayList("SE", "ES"); // 14
        symbolList.add(symbols);
        symbols = Lists.newArrayList("ESE", "SEE"); // 15
        symbolList.add(symbols);*/


    }


    public double getAngleFromDirectionSymbol(String symbol) {

        for (int i = 0; i < symbolList.size(); i++) {
            for (int k = 0; k < symbolList.get(i).size(); k++) {
                if (symbolList.get(i).get(k).equals(symbol))
                    return i * 22.5;
            }
        }
        /*for (int i = 0; i < directionSymbols.length; i++) {
            if(symbol.equals(directionSymbols[i]))
                return i*22.5;
        }*/
        return -1;
    }

    public String toJson() {

        JSONObject obj = new JSONObject();

        try {
            obj.put("speed", speed);
            obj.put("avspeed", averagespeed);
            obj.put("direction", direction);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if (datetime != null)
                obj.put("date", dateFormat.format(datetime));
            if (sampledatetime != null)
                obj.put("date", dateFormat.format(sampledatetime));
            //obj.put("time", time);
            obj.put("temperature", temperature);
            obj.put("pressure", pressure);
            obj.put("humidity", humidity);
            obj.put("rainrate", rainrate);

            //obj.put("sampledatetime", /*sampledatetime*/date + " " + time);
            obj.put("spotname", spotName);
            obj.put("source", source);
            obj.put("directionangle", directionangle);
            obj.put("trend", trend);
            obj.put("spotid", spotID);
            obj.put("webcamurl", webcamurl);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();
    }

    public String fromJson(JSONObject obj) {

        try {
            if (obj.has("speed"))
                speed = obj.getDouble("speed");
            if (obj.has("avspeed"))
                averagespeed = obj.getDouble("avspeed");
            if (obj.has("direction"))
                direction = obj.getString("direction");
            if (obj.has("datetime")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    datetime = formatter.parse(obj.getString("datetime"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (obj.has("temperature"))
                temperature = obj.getDouble("temperature");
            if (obj.has("pressure"))
                pressure = obj.getDouble("pressure");
            if (obj.has("humidity"))
                humidity = obj.getDouble("humidity");
            if (obj.has("rainrate"))
                rainrate = obj.getDouble("rainrate");
            if (obj.has("sampledatetime")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    sampledatetime = formatter.parse(obj.getString("datetime"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            if (obj.has("spotname"))
                spotName = obj.getString("spotname");
            if (obj.has("source"))
                source = obj.getString("source");
            if (obj.has("directionangle"))
                directionangle = obj.getDouble("directionangle");
            if (obj.has("trend"))
                directionangle = obj.getDouble("trend");
            if (obj.has("spotid"))
                spotID = obj.getInt("spotid");
            if (obj.has("webcamurl"))
                webcamurl = obj.getString("webcamurl");


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();
    }

    public static Double knotsToKMh(double knots) {

        double kmh = knots * 1.85200;
        kmh = Math.round(kmh * 10.0);
        kmh = kmh / 10;
        return kmh;
    }

    public int insert(/*Device device*/) {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDatetime = "''";
            if (datetime != null)
                strDatetime = "'" + df.format(datetime) + "'";
            String strSampleDatetime = "''";
            if (sampledatetime != null)
                strSampleDatetime = "'" + df.format(sampledatetime) + "'";

            String sql;
            sql = "INSERT INTO wind (spotid, sampledatetime, datetime, speed, averagespeed, direction, directionangle, temperature, humidity, pressure)" +
                    " VALUES (" + spotID + "," + strDatetime + "," + strSampleDatetime + "," + speed + "," + averagespeed + ",'" + direction + "'," + directionangle + "," + temperature + "," + humidity + "," + pressure + ") ";

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            /*if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                //lastid = device.id;
            }*/

            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return 0;
        }

        //read(); // reload data
        return 1;
    }

    public List<MeteoStationData> getHistory(int spotId, Date startDate, Date endDate) {

        LOGGER.info("getHistory");
        List<MeteoStationData> list = new ArrayList<MeteoStationData>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            String strStartDate = "'" + df.format(startDate) + "'";
            String strEndDate = "'" + df.format(endDate) + "'";

            String sql;
            sql = "SELECT * FROM wind WHERE spotid=" + spotId
                    + " AND sampledatetime BETWEEN " + strStartDate + " and " + strEndDate + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MeteoStationData md = new MeteoStationData();
                md.id = rs.getInt("id");
                md.datetime = rs.getTimestamp("datetime");
                md.spotID = rs.getInt("spotid");
                md.sampledatetime = rs.getTimestamp("sampledatetime");
                md.speed = rs.getDouble("speed");
                md.averagespeed = rs.getDouble("averagespeed");
                md.directionangle = rs.getDouble("directionangle");
                md.direction = rs.getString("direction");
                md.temperature = rs.getDouble("temperature");
                md.humidity = rs.getDouble("humidity");
                md.pressure = rs.getDouble("pressure");

                list.add(md);
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

}