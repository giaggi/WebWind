/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package Wind.servlet;

import Wind.Core;
import Wind.User;
import Wind.data.Device;
import Wind.data.Devices;
import Wind.notification.PushNotificationThread;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            String regId = request.getParameter("regid");
            Devices devices = new Devices();
            PrintWriter out = response.getWriter();

            if (devices != null) {
                Device device = devices.getDeviceFromRegId(regId);
                response.setContentType("application/json");
                out.println("{\"id\" : \"" + device.id + "\" }");
            }

            out.close();

            setSuccess(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {

        String registeruser = getParameter(req, "registeruser");
        String registerdevice = getParameter(req, "registerdevice");

        if (registeruser != null && registeruser.equals("true")) {

            String tokenid = getParameter(req, "tokenid");
            User user = new User();
            if (!Core.validateTokenId(tokenid,user)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain");
                return;
            }
            int userid = Core.addUser(user.personid,user.name,user.email,user.photo);

            resp.setContentType("application/json");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                out.println("{\"id\" : \"" + userid + "\", \"personid\" : \"" + user.personid + "\" }");
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();

            setSuccess(resp);

        } else if (registerdevice != null && registerdevice.equals("true")) {
            String regId = getParameter(req, "regId");
            Device device = new Device();
            device.regId = regId;
            device.id = 0;
            device.name = getParameter(req, "devicename");
            device.date = Core.getDate();
            int deviceId = Core.addDevice(device);

            resp.setContentType("application/json");
            PrintWriter out;
            try {
                out = resp.getWriter();
                String str = "{\"deviceid\" : " + deviceId + "}"; //", \"userid\" : " + userid + " }";
                out.print(str);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            setSuccess(resp);
        }
    }

    protected String getParameter(HttpServletRequest req, String parameter,
                                  String defaultValue) {
        String value = req.getParameter(parameter);
        if (isEmptyOrNull(value)) {
            value = defaultValue;
        }
        return value.trim();
    }

    protected String getParameter(HttpServletRequest req, String parameter)
            throws ServletException {
        String value = req.getParameter(parameter);
        if (isEmptyOrNull(value)) {
            return null;
        }
        return value.trim();
    }

    protected void setSuccess(HttpServletResponse resp) {
        setSuccess(resp, 0);
    }

    protected void setSuccess(HttpServletResponse resp, int size) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setContentLength(size);
    }

    protected boolean isEmptyOrNull(String value) {
        return value == null || value.trim().length() == 0;
    }


}
