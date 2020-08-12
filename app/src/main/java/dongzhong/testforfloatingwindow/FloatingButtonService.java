package dongzhong.testforfloatingwindow;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lintao on 2018/5/30.
 */

public class FloatingButtonService extends Service implements Runnable {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private FloatingOnTouchListener floatingOnTouchListener;
    private TextView content;
    private TextView time;
    private TextView num;
    private Bundle b;

    String[] arr = TxtToArr("/sdcard/content.txt");
    int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 540;
        layoutParams.height = 540;
        layoutParams.x = 300;
        layoutParams.y = 300;

        LayoutInflater flater= LayoutInflater.from(getApplicationContext());
        Share.myview = flater.inflate(R.layout.floating_display,null);
        Share.myview.setBackgroundColor(Color.WHITE);
        Share.myview.getBackground().setAlpha(128);

        FloatingButtonService mythread = new FloatingButtonService();
        new Thread(mythread).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            content = Share.myview.findViewById(R.id.content);
            content.setTextColor(Color.BLACK);
            System.out.println(content.getTextSize());
            content.setMovementMethod(ScrollingMovementMethod.getInstance());

            time = Share.myview.findViewById(R.id.time);
            time.setTextColor(Color.BLACK);

            num = Share.myview.findViewById(R.id.num);
            num.setTextColor(Color.BLACK);

                b = msg.getData();
                int id = b.getInt("ID");
                content.setText(arr[id]);
                num.setText(String.valueOf(id));
                int countdown = b.getInt("Time");
                time.setText(String.valueOf(countdown));
        }
    };

    public String[] TxtToArr(String name) {
        // 使用ArrayList来存储每行读取到的字符串
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(name);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            String s = arrayList.get(i);
            array[i] = s;
        }
        // 返回数组
        return array;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {

            windowManager.addView(Share.myview,layoutParams);
            floatingOnTouchListener = new FloatingOnTouchListener();
            Share.myview.setOnTouchListener(floatingOnTouchListener);

        }
    }

    @Override
    public void run() {
        b = new Bundle();
        while (true) {
            int countdown = 15;
            if (count == arr.length)
                count = 0;

//            content.setText(arr[count]);
            b.putInt("ID", count);
            Message msg = handler.obtainMessage();
            msg.setData(b);
            msg.sendToTarget();
//            System.out.println(Share.testShare == null);
            count++;

            while (countdown >= 0){
                b.putInt("Time", countdown);
                msg = handler.obtainMessage();
                msg.setData(b);
                msg.sendToTarget();
                countdown--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
