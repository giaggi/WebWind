package Wind;


//import com.google.appengine.repackaged.org.joda.time.LocalTime;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giacomo on 14/06/2015.
 */
public class AlarmServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AlarmServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // to accept the json data

        String jsonData = request.getParameter("json");
        String regId = request.getParameter("regId");
        String deletekey = request.getParameter("delete");
        String ringkey = request.getParameter("ring");
        String snoozekey = request.getParameter("snooze");
        //String deviceId = request.getParameter("deviceId");
        String strAlarmId = request.getParameter("alarmId");
        String test = request.getParameter("test");

        int alarmid;
        if (strAlarmId != null)
            alarmid = Integer.valueOf(strAlarmId);
        else
            alarmid = 0;


        try {
            if (deletekey != null) {

                LOGGER.info("delete alarm  id=" + alarmid);
                WindDatastore.deleteAlarm(alarmid);

            } else if (ringkey != null) {

                AlarmLog al = new AlarmLog();
                al.insert("ring enable", alarmid, regId, 0);

                LOGGER.info("update ring date regId " + regId + ";id " + alarmid);

                Date date = Core.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                WindDatastore.updateAlarmLastRingDate(regId, Integer.valueOf(alarmid), date/*sdf.parse(date + " " + time)*/);

            } else if (snoozekey != null) {

                int snoozeMinutes = Integer.valueOf(request.getParameter("minutes"));
                AlarmLog al = new AlarmLog();
                al.insert("snooze alarm", alarmid, regId, snoozeMinutes);
                LOGGER.info("snooze regId " + regId + ";id " + alarmid + ";snooze minutes=" + snoozeMinutes);
                WindDatastore.updateAlarmSnoozeMinutes(regId, Integer.valueOf(alarmid), snoozeMinutes);

            } else if (test != null && test.equals("true")) {

                LOGGER.info("testing alarm");
                Date localDate = Core.getDate();
                double speed = 29.0;
                double avspeed = 29.0;
                int spotId = 0;

                List<Alarm> list = WindDatastore.getAlarms();
                Iterator<Alarm> iterator = list.iterator();
                while (iterator.hasNext()) {

                    Alarm alarm = iterator.next();
                    if (alarm.id == alarmid) {

                        AlarmModel.sendAlarm(alarm.deviceId, alarm, speed, avspeed, localDate, spotId);
                    }
                }


            } else {

                JSONObject json = new JSONObject(jsonData);
                Alarm alarm = new Alarm(json);
                Core.windDatastore.saveAlarm(alarm);

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.println(jsonData);
                out.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("JSONException=" + e.toString());


            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //response.getWriter().write(e.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            String deviceId = request.getParameter("deviceId");
            int id = Integer.valueOf(deviceId);

            List<Alarm> alarms = WindDatastore.getAlarmsFromDeviceID(id);
            if (alarms == null) {
                LOGGER.info("deviceId " + deviceId + " not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"alarms\" : [");

            for (int i = 0; i < alarms.size(); i++) {
                String jsonText = alarms.get(i).toJson();
                LOGGER.info("jsonText " + jsonText);

                out.println(jsonText);
                if (i != alarms.size() - 1)
                    out.println(",");
            }
            out.println("] }");
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
