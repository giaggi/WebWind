package Wind;

import com.google.android.gcm.server.Message;
/*import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import com.google.appengine.repackaged.org.joda.time.LocalTime;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatterBuilder;*/

import org.json.JSONException;
import org.json.JSONObject;
import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by giacomo on 25/06/2015.
 */
public class AlarmModel {

    public static final String NotificationType_Alarm = "Alarm";
    public static final String NotificationType_Info = "info";

    private static final Logger LOGGER = Logger.getLogger(AlarmModel.class.getName());

    public static final int Spot_Valmadrera = 0;
    public static final int Spot_VassilikiPort = 1;
    public static final int Spot_Follonica = 2;
    public static final int Spot_Scarlino = 3;
    public static final int Spot_Abbadia = 4;
    public static final int Spot_Gera = 5;
    public static final int Spot_Dongo = 6;
    public static final int Spot_Dervio = 7;
    public static final int Spot_Vassiliki = 8;
    public static final int Spot_Dakhla = 9;
    public static final int Spot_Talamone = 10;
    public static final int Spot_Jeri = 11;
    public static final int Spot_Colico = 12;
    public static final int Spot_Sorico = 13;

    private ArrayList<PullData> mSpotDataList = new ArrayList<PullData>();
    //private LocalTime HighWindNotificationTime = getCurrentTime();
    private ArrayList<ArrayList<MeteoStationData>> meteoHistory;// = new ArrayList<ArrayList<MeteoStationData>>();

    public AlarmModel() {
        meteoHistory = new ArrayList<ArrayList<MeteoStationData>>();
    }

    public String getSpotName(int spotID) {

        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotID() == spotID)
                return mSpotDataList.get(i).getName();
        }
        return "";
    }

    public void addSpotData(PullData pd) {

        mSpotDataList.add(pd);
        meteoHistory.add(new ArrayList<MeteoStationData>());
    }

    public ArrayList<Spot> getSpotList() {

        ArrayList<Spot> list = new ArrayList<Spot>();
        for (int i = 0; i < mSpotDataList.size(); i++) {

            list.add(new Spot(mSpotDataList.get(i).getName(), mSpotDataList.get(i).getSpotID()));
        }
        return list;
    }

    public void pullData() {

        for (int i = 0; i < mSpotDataList.size(); i++) {

            mSpotDataList.get(i).pull();
        }
    }

    public boolean Add(MeteoStationData md, int spotID) {

        int index = getIndexFromID(spotID);
        if (index < 0) return false;

        meteoHistory.get(index).add(md);

        if (meteoHistory.get(index).size() > 1000)
            meteoHistory.get(index).remove(0);

        //LocalTime localTime = AlarmModel.getCurrentTime();
        //Date localDate = AlarmModel.getCurrentDate();
        return true;

    }

    public void evaluate() {

        //Date localTime = AlarmModel.getCurrentTime();
        //Date localDate = AlarmModel.getCurrentDate();
        Date localTime = Core.getDate();
        Date localDate = localTime;

        LOGGER.info("--->evaluate ALARMS " + localDate.toString());

        for (int spot = 0; spot < meteoHistory.size(); spot++) {
            // fa un loop su tutti gli spot per trovare gli allarmi attivi
            int len = meteoHistory.get(spot).size();
            if (len < 1)
                continue;
            MeteoStationData md = meteoHistory.get(spot).get(len - 1);
            evaluateAlarms(md.speed, md.averagespeed, localTime, localDate, md.spotID);
            evaluateNotifications(md);
        }
        LOGGER.info("<---evaluate ALARMS");
    }

    /*public ArrayList<MeteoStationData> getLast() {
        if (meteoHistory.size() == 0)
            return null;

        ArrayList<MeteoStationData> list = new ArrayList<MeteoStationData>();

        for (int i = 0; i < meteoHistory.size();i++) {




            int len = meteoHistory.get(i).size();
            if (len == 0) continue;
            MeteoStationData md = meteoHistory.get(i).get(len-1);
            list.add(md);

        }
        return list;
    }*/
    public MeteoStationData getLastfromID(int id) {

        int index = getIndexFromID(id);
        if (index < 0 || index >= meteoHistory.size()) return null;

        int len = meteoHistory.get(index).size();
        if (len == 0) return null;

        MeteoStationData md = meteoHistory.get(index).get(len - 1);
        return md;
    }

    public int getIndexFromID(int spotID) {

        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotID() == spotID)
                return i;
        }
        return -1;
    }

    public ArrayList<MeteoStationData> getHistory(int spotID) {

        int index = getIndexFromID(spotID);
        if (index < 0) return null;

        return meteoHistory.get(index);
    }

    public void evaluateNotifications(MeteoStationData md) {

       /* LocalTime current = new LocalTime();


        if (Double.valueOf(md.averagespeed) > 15.0) {

            HighWindNotificationTime = getCurrentTime();

            Message message = new Message.Builder()
                    // .collapseKey(collapsekey) // se c'? gi? un messaggio con lo
                    // stesso collapskey e red id allora l'ultimo sostituir? il
                    // precedente
                    // .timeToLive(3).delayWhileIdle(true) // numero di secondi per
                    // i quali il messagio rimane in coda (default 4 week)
                    .addData("title", "titoloy")
                    .addData("message", "allarme")
                    .addData("notificationtype", SendPushMessages.NotificationType_Info)
                    .build();

            SendPushMessages sp = new SendPushMessages();
            sp.init();
            //sp.send("vento forte",SendPushMessages.App_WindAlarm,message);

        }
        if (Double.valueOf(md.averagespeed) > 18.0) {

            Message message = new Message.Builder()
                    // .collapseKey(collapsekey) // se c'? gi? un messaggio con lo
                    // stesso collapskey e red id allora l'ultimo sostituir? il
                    // precedente
                    // .timeToLive(3).delayWhileIdle(true) // numero di secondi per
                    // i quali il messagio rimane in coda (default 4 week)
                    .addData("title", "vento in aumento")
                    .addData("message", "vento in aumento. Vento medio " + md.averagespeed + "vento " + md.speed)
                    .addData("notificationtype", SendPushMessages.NotificationType_Info)
                    .build();

            SendPushMessages sp = new SendPushMessages();
            sp.init();
            //sp.send("vento forte",SendPushMessages.App_WindAlarm,message);

        }*/
    }


    public static void evaluateAlarms(Double speed, Double avspeed, Date localTime, Date localDate, long spotId) {
        LOGGER.info("evaluateAlarms localTime=" + localTime);
        LOGGER.info("LocalDate=" + localDate);
        List<Alarm> list = WindDatastore.getActiveAlarm(speed, avspeed, localTime, localDate, spotId);

        LOGGER.info("list.size=" + list.size());

        for (int i = 0; i < list.size(); i++) {
            String registrationId = list.get(i).regId;
            Alarm alarm = list.get(i);
            //Long alarmId = list.get(i).id;
            LOGGER.info("i=" + i);
            sendAlarm(registrationId, alarm, speed, avspeed, localTime, localDate, spotId);
            //Result result = sp.sendSingleMessage(registrationId, message);
            //WindDatastore.updateAlarmLastRingDate(registrationId, alarm.id, localTime, localDate);
        }
    }

    public static void sendAlarm(String regId, Alarm alarm, Double speed, Double avspeed, Date currentTime, Date currentDate, long spotId) {

        LOGGER.info("sendAlarm spotID=" + spotId);

        AlarmLog al = new AlarmLog();
        al.insert("sendalarm",alarm.id,regId);
        //SendPushMessages sp = new SendPushMessages();
        //sp.init();
        Message notification = new Message.Builder()
                // .collapseKey(collapsekey) // se c'? gi? un messaggio con lo
                // stesso collapskey e red id allora l'ultimo sostituir? il
                // precedente
                // .timeToLive(3).delayWhileIdle(true) // numero di secondi per
                // i quali il messagio rimane in coda (default 4 week)
                .addData("title", "titolox")
                .addData("alarmId", ""+alarm.id)
                .addData("spotID", "" + alarm.spotID)
                .addData("startDate", "" + alarm.startDate)
                .addData("startTime", "" + alarm.startTime)
                .addData("lastRingTime", "" + alarm.lastRingTime)
                .addData("endDate", "" + alarm.endDate)
                .addData("endTime", "" + alarm.endTime)
                .addData("avspeed", "" + alarm.avspeed)
                .addData("speed", "" + alarm.speed)

                .addData("curspeed", "" + speed)
                .addData("curavspeed", "" + avspeed)
                .addData("curlocalTime", "" + currentTime)
                .addData("curlocalDate", "" + currentDate)
                .addData("curspotId", "" + spotId)

                .addData("notificationtype", AlarmModel.NotificationType_Alarm)
                .build();

        Core.sendPushNotification(regId, notification);
    }



    public boolean getHistoricalMeteoData() {// inizializza historical meteo data


        String htmlResultString = "";
        MeteoStationData meteoStationData = new MeteoStationData();


        String path = "https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec";

        path += "?sampledata=100";
        /*if (HomeServlet.ProductionEnvironment)
            path += "&production=true";
        else
            path += "&production=false";*/


        return true;//callGET(path,Spot_Valmadrera);
    }

    protected double getAverage(int spotID) {

        int index = getIndexFromID(spotID);
        if (index < 0) return 0;

        ArrayList<MeteoStationData> list = getHistory(index);

        double average = 0;
        int samples = 5;
        if (samples > list.size())
            samples = list.size();

        for (int i = 0; i < samples; i++) {
            average += list.get(list.size() - 1 - i).speed;
        }
        average = average / samples;
        average = Math.round(average * 10) / 10;
        return average;
    }

    protected double getTrend(int spotID) {

        int index = getIndexFromID(spotID);
        if (index < 0) return 0;

        ArrayList<MeteoStationData> list = getHistory(index);

        double trend = 0;
        if (list == null || list.size() == 0)
            return 0;

        int samples = 5;
        if (samples > list.size())
            samples = list.size();

        double initialAverageSpeed = list.get(list.size() - samples).averagespeed;

        for (int i = 1; i < samples; i++) {
            trend += (list.get(list.size() - samples + i).averagespeed - initialAverageSpeed);
        }
        trend = Math.round(trend * 10) / 10;
        return trend;
    }

    private boolean callGET(String path, int spot) {

        path += "&spot=" + getSpotName(spot);

        String htmlResultString;
        boolean result;
        try {
            URL jsonurl = new URL(path);

            LOGGER.info("jsonurl=" + jsonurl);

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(false);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("GET");
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            htmlResultString = "";
            while ((line = rd.readLine()) != null) {
                htmlResultString += line;
            }
            reader.close();

            int res = connection.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {

                //String txt = htmlResultString;
                try {
                    JSONObject json = new JSONObject(htmlResultString);
                    org.json.JSONArray jsonArray = json.getJSONArray("meteodata");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObject2 = jsonArray.getJSONObject(i);
                        MeteoStationData md = new MeteoStationData();
                        md.fromJson(jObject2);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                result = callGET(location, spot);
                // OK
                //result = true;
            } else {
                // Server returned HTTP error code.
                LOGGER.info("Server returned HTTP error code" + res);
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}