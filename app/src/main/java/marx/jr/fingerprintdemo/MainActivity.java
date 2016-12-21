package marx.jr.fingerprintdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.PrivateKey;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    Button btn_touch;
    FingerprintManager manager;
    IntentFilter filter;
    private static String BROADCAST_CODE = "BROADCAST_CODE_DIALOG";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        filter = new IntentFilter(BROADCAST_CODE);
        setContentView(R.layout.activity_main);
        btn_touch = (Button) findViewById(R.id.Btn_Touch);
        manager = getSystemService(FingerprintManager.class);
        btn_touch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fingerPrintAuthentication();
            }
        });
        registerReceiver(receiver, filter);
    }

    AlertDialog dialog;

    private void fingerPrintAuthentication()
    {
        dialog = new AlertDialog.Builder(this).setMessage("Touch♂Me").setCancelable(false).create();
        if (dialog.isShowing())
            dialog.dismiss();
        dialog.show();
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
                {
                    intent = new Intent(BROADCAST_CODE);
                    intent.putExtra("msg", "无指纹验证权限！");
                    intent.putExtra("successOrError", true);
                    sendBroadcast(intent);
                    return;
                }
                if (manager.isHardwareDetected() && manager.hasEnrolledFingerprints())
                {
                    manager.authenticate(null, null, 0, callback, null);
                } else
                {
                    intent = new Intent(BROADCAST_CODE);
                    intent.putExtra("msg", "无指纹采集设备");
                    intent.putExtra("successOrError", true);
                    sendBroadcast(intent);
                }
            }
        });
        t.start();


    }


    private FingerprintManager.AuthenticationCallback callback = new FingerprintManager.AuthenticationCallback()
    {

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString)
        {
            super.onAuthenticationError(errorCode, errString);
            Log.i("finger_touch", "error" + errString + "|" + errorCode + "|不可再验");
            intent = new Intent(BROADCAST_CODE);
            intent.putExtra("successOrError", true);
            intent.putExtra("msg", errString);
            sendBroadcast(intent);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString)
        {
            super.onAuthenticationHelp(helpCode, helpString);
            Log.i("finger_touch", "Help" + helpString + "|" + helpCode + "|其他原因验证失败，可再验证");
            intent = new Intent(BROADCAST_CODE);
            intent.putExtra("msg", "其他验证失败");
            sendBroadcast(intent);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result)
        {
            super.onAuthenticationSucceeded(result);
            Log.i("finger_touch", "Success!!!");
            intent = new Intent(BROADCAST_CODE);
            intent.putExtra("msg", "验证成功");
            intent.putExtra("successOrError", true);
            sendBroadcast(intent);
        }

        @Override
        public void onAuthenticationFailed()
        {
            super.onAuthenticationFailed();
            Log.i("finger_touch", "faild" + "|非登录指纹，可再验证");
            intent = new Intent(BROADCAST_CODE);
            intent.putExtra("msg", "验证失败，请重试");
            sendBroadcast(intent);
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(dialog.isShowing())
            dialog.dismiss();
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String msg = intent.getStringExtra("msg");
            Boolean successOrError = intent.getBooleanExtra("successOrError", false);
            if (dialog != null)
                dialog.setMessage(msg);
            if (successOrError)
            {
                TimerTask task = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (dialog != null && dialog.isShowing())
                                {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000);
            } else
            {
                TimerTask task = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dialog.setMessage("Touch♂Me");
                            }
                        });

                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000);

            }


        }
    };

}
