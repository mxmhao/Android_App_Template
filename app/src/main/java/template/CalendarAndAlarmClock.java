package template;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * https://developer.android.com/guide/topics/providers/calendar-provider
 * https://www.jianshu.com/p/4820e02b2ee4
 */
public class CalendarAndAlarmClock {

    private static Uri calanderURL = CalendarContract.Calendars.CONTENT_URI;
    private static Uri calanderEventURL = CalendarContract.Events.CONTENT_URI;
    private static Uri calanderRemiderURL = CalendarContract.Reminders.CONTENT_URI;

    private static long calenderId = -1;

    /**
     * 是否没有读写权限
     * @param activity
     * @return true没有读写权限, false有读写权限
     */
    public static boolean hasNoReadWritePermission(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED;
    }
    /**
     * 请求读写权限
     * 需要重写的方法:
     * @see ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])
     * 用户操作后回调重写方法
     * */
    public static void requestReadWritePermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, requestCode);
    }

    private static void init(Activity activity) {
        if (calenderId <= 0) {
            Cursor userCursor = activity.getContentResolver()
                    .query(calanderURL, null, null, null, null);
            //如果小于1代表没有账户
            if (userCursor.getCount() < 1) {
                //添加账户
                calenderId = initCalendars(activity, "antinker");
            } else {
                //注意：是向最后一个账户添加，开发者可以根据需要改变添加事件 的账户
                userCursor.moveToLast();
                calenderId = userCursor.getLong(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            }
            userCursor.close();
        }
    }

    /**
     * 添加一个属于自己的账户，方便以后取这个账户的提醒事件
     *
     * @param activity
     * @param accountName 账户名
     */
    private static long initCalendars(Activity activity, String accountName) {
        /*
         * 小米默认账户
         * allowedReminders=0,1
         * sync_events=1
         * canModifyTimeZone=1
         * canOrganizerRespond=1
         * maxReminders=5
         * _id=1
         * visible=1
         * calendar_color=-30720
         * account_name=account_name_local
         * account_type=LOCAL
         * calendar_displayName=calendar_displayname_local
         * deleted=0
         * ownerAccount=owner_account_local
         * calendar_access_level=700
         * canPartiallyUpdate=0
         */
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, "com.min.test");

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);//日历账户名称
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange");
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, accountName);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, -30720);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange")
                .build();
        //插入账号数据
        Uri uri = activity.getContentResolver().insert(calendarUri, value);
        if (null != uri) {
            return ContentUris.parseId(uri);
        }
        return -1;
    }

    /**
     * 日期时间转时间戳
     * @param time 时间 格式 yyyy/mm/dd/hh/mm
     * @return
     */
    private static long getTime(String time) {
        Calendar calendar = Calendar.getInstance();
        String[] startTime = time.split("/");
        calendar.set(Calendar.YEAR, Integer.parseInt(startTime[0]));//年
        calendar.set(Calendar.MONTH, Integer.parseInt(startTime[1])-1);//月
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startTime[2]));//日
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[3]));//时
        calendar.set(Calendar.MINUTE, Integer.parseInt(startTime[4]));//分
        return calendar.getTimeInMillis();
    }

    /**
     * 获取日历事件
     *
     * @param context
     */
    public static JSONArray queryCalendarEvent(Activity context, long eventId) throws JSONException {
        Uri uri = calanderEventURL;
        if (eventId > 0) {
            uri = ContentUris.withAppendedId(calanderEventURL, eventId);
        }
        Cursor cursor = context.getContentResolver()
                .query(uri, null,
                        CalendarContract.Events.MUTATORS + " = ?",
                        new String[]{context.getPackageName()}, null);
        JSONArray ja = new JSONArray();
        JSONObject jo;
        while (cursor.moveToNext()) {
            jo = new JSONObject();
            jo.put("title", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
            jo.put("description", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)));
            jo.put("dtStart", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
            String rrule = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RRULE));
            if (null != rrule) {
                String[] keyValues = rrule.split(";");

                String[] keyValue = keyValues[0].split("=");
                jo.put("freq", keyValue[1].toLowerCase());

                keyValue = keyValues[1].split("=");
                jo.put("interval", keyValue[1]);
            }
            ja.put(jo);
        }
        cursor.close();
        return ja;
    }

    /**
     * 添加日历事件或更新
     *
     * @param context
     * @param title         日程标题
     * @param description   备注信息
     * @param dtStart       提醒开始时间, 时间戳
     * @param freq          频率
     * @param interval      频率间隔
     * @return
     */
    public static int addCalendarEvent(Activity context, long eventId, String title, String description, long dtStart, String freq, short interval) {
        if (dtStart <= 0) {
            Toast.makeText(context, "提醒开始时间不能为空！", Toast.LENGTH_SHORT).show();
            return -1;
        }

        init(context);

        //rrule组装
        switch (freq.toLowerCase()) {
            case "daily"://天
                break;
            case "weekly"://周
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dtStart);
                String[] weeklys = new String[]{"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
                String wk = ";BYDAY=" + weeklys[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            }
            break;
            case "monthly"://月
                break;
            case "yearly"://年
                break;
        }

        //FREQ=WEEKLY;INTERVAL=2;WKST=SU;BYDAY=WE   //WKST得根据地区调整?
        String rrlue = "FREQ=" + freq.toUpperCase() + ";INTERVAL" + interval;

        ContentValues event = new ContentValues();
        // 插入账户
        event.put(CalendarContract.Events.CALENDAR_ID, 1);
//        event.put(CalendarContract.Events.MUTATORS, context.getPackageName());//同步的包名，不用自己设置，系统默认设置成自己的包名
        event.put(CalendarContract.Events.TITLE, title);
        event.put(CalendarContract.Events.DESCRIPTION, description);
        event.put(CalendarContract.Events.DURATION, "P10M");//我们默认持续时间10分钟
        event.put(CalendarContract.Events.RRULE, rrlue);//重复规则,https://www.cnblogs.com/ice5/p/14023771.html
//        event.put(CalendarContract.Events.HAS_ALARM, true);//是否含有提醒，这个不用设置，关联Reminders是会自动设置为true
//        event.put(CalendarContract.Events.ALLOWED_REMINDERS, CalendarContract.Reminders.METHOD_ALERT);//是否提示,默认0，1
        event.put(CalendarContract.Events.DTSTART, dtStart);//提醒开始时间
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());  //这个是时区，必须有，
        Uri newEvent;
        if (eventId <= 0) {
            //添加事件
            newEvent = context.getContentResolver().insert(calanderEventURL, event);
        } else {
            Uri uri = ContentUris.withAppendedId(calanderEventURL, eventId);
            return context.getContentResolver().update(uri, event, null, null);
        }
        if (null == newEvent) {
            return -1;
        }

        //事件提醒的设定
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        if (context.getContentResolver().insert(calanderRemiderURL, values) == null) {
            return -1;
        }
        return 0;
    }

    /**
     * 删除日历事件
     */
    public static int deleteCalendarEvent(Activity context, long id) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id), null, null);
    }

    //必须的权限：<uses-permission android:name=”com.android.alarm.permission.SET_ALARM” /> 
    private void createAlarm(Activity activity, String message, int hour, int minutes, int resId) {
        ArrayList<Integer> testDays = new ArrayList<>();
        testDays.add(Calendar.MONDAY);//周一
        testDays.add(Calendar.TUESDAY);//周二
        testDays.add(Calendar.FRIDAY);//周五

        String packageName = activity.getPackageName();
        Uri ringtoneUri = Uri.parse("android.resource://" + packageName + "/" + resId);

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)//ACTION_DISMISS_ALARM
                //闹钟的小时
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                //闹钟的分钟
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                //响铃时提示的信息
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                //用于指定该闹铃触发时是否振动
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                //一个 content: URI，用于指定闹铃使用的铃声，也可指定 VALUE_RINGTONE_SILENT 以不使用铃声。
                //如需使用默认铃声，则无需指定此 extra。
                .putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri)
                //对于一次性闹铃，无需指定此 extra
                .putExtra(AlarmClock.EXTRA_DAYS, testDays)
                //如果为true，则调用startActivity()不会进入手机的闹钟设置界面
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
