package cordova.plugin.isij;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

public class SalaatTimesProvider {

    private JSONArray salaatTimesArray;

    public SalaatTimesProvider(InputStream jsonInputStream) {
        this.parseJSON(this.preloadJSONFile(jsonInputStream));
    }

    public Date getUpcomingAdhaanTime() {
        return this.findUpcomingAdhaanTime(0);
    }

    private Date findUpcomingAdhaanTime(int dayAppend) {
        Calendar now = GregorianCalendar.getInstance();

        now.roll(GregorianCalendar.DAY_OF_MONTH, dayAppend);

        int hourNum = now.HOUR_OF_DAY;
        int dayNum = now.DAY_OF_MONTH - 1;
        int monthNum = now.MONTH ;



        try {
            JSONArray todaysTimes = salaatTimesArray.getJSONArray(monthNum).getJSONArray(dayNum);
            Date nextSalaatTime = null;
            //Figure out current time
            for (int i = 0; i < todaysTimes.length(); i++) {


                // Skip first two (Imsaak, Sunrise, Sunset)
                if (i == 0 || i == 2 || i == 4) {
                    continue;
                }

                Date currenTime = now.getTime();

                  String[] salaatTimeArray = todaysTimes.getString(i).split(":");
                  Calendar salaatTimeCal = GregorianCalendar.getInstance();

                  salaatTimeCal.set(GregorianCalendar.HOUR, Integer.parseInt(salaatTimeArray[0]) % 12);
                  salaatTimeCal.set(GregorianCalendar.MINUTE, Integer.parseInt(salaatTimeArray[1]));

                  if (i > 2) {
                    salaatTimeCal.set(GregorianCalendar.AM_PM, GregorianCalendar.PM);
                  } else {
                    salaatTimeCal.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
                  }

                  if (currenTime.before(salaatTimeCal.getTime())) {
                    nextSalaatTime = salaatTimeCal.getTime();
                    break;
                  }

            }

            if (nextSalaatTime != null) {
                return nextSalaatTime;
            } else {
                return findUpcomingAdhaanTime(dayAppend + 1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    private String preloadJSONFile(InputStream is) {
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void parseJSON(String json) {
        try {
            this.salaatTimesArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
