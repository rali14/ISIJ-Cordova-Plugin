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

    public Date getUpcomingAdhaanTime(Date afterDateTime) {
        return this.findUpcomingAdhaanTime(0, afterDateTime);
    }

    private Date findUpcomingAdhaanTime(int dayAppend, Date afterDateTime) {
        Calendar now = GregorianCalendar.getInstance();

        now.add(GregorianCalendar.DAY_OF_MONTH, dayAppend);

        int hourNum = now.get(GregorianCalendar.HOUR_OF_DAY);
        int dayNum = now.get(GregorianCalendar.DAY_OF_MONTH) - 1;
        int monthNum = now.get(GregorianCalendar.MONTH);

        now.set(Calendar.SECOND, 0);



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
                  salaatTimeCal.setTime(now.getTime());
                  salaatTimeCal.set(Calendar.SECOND, 0);
                
                
                  int tmp = Integer.parseInt(salaatTimeArray[0]) % 12;
                
                  if (tmp == 0) {
                      tmp = 12;
                  }
                  salaatTimeCal.set(GregorianCalendar.HOUR, tmp);
                  
                  salaatTimeCal.set(GregorianCalendar.MINUTE, Integer.parseInt(salaatTimeArray[1]));

                  if (i > 2) {
                    salaatTimeCal.set(GregorianCalendar.AM_PM, GregorianCalendar.PM);
                  } else {
                    salaatTimeCal.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
                  }


                  if (currenTime.before(salaatTimeCal.getTime()) && afterDateTime == null) {

                        nextSalaatTime = salaatTimeCal.getTime();
                        break;

                  } else if (afterDateTime != null) {


                             //Create a slight time different between salaat time and skip time
                             Calendar atCal = GregorianCalendar.getInstance();
                             atCal.setTime(afterDateTime);
                             atCal.add(GregorianCalendar.SECOND, 30);

                        if (salaatTimeCal.getTime().after(atCal.getTime())) {

                            nextSalaatTime = salaatTimeCal.getTime();
                            break;
                        }
                  }
            }

            if (nextSalaatTime != null) {
                return nextSalaatTime;
            } else {

                return findUpcomingAdhaanTime(dayAppend + 1, afterDateTime);
            }

        } catch (Exception e) {
          e.printStackTrace();
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
