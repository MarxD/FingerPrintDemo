package marx.jr.fingerprintdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    Button btn_touch;
    IntentFilter filter;
    private static String BROADCAST_CODE = "BROADCAST_CODE_DIALOG";
    Intent intent;
    AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        filter = new IntentFilter(BROADCAST_CODE);
        setContentView(R.layout.activity_main);
        btn_touch = (Button) findViewById(R.id.Btn_Touch);
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


    private void fingerPrintAuthentication()
    {

        if(!MApplication.hasFingerPrintHardware())
        {
            dialog = new AlertDialog.Builder(this).setMessage("无指纹设备").setCancelable(true).create();
            dialog.show();
            return;
        }

        final FingerprintManager manager = getSystemService(FingerprintManager.class);
        final CancellationSignal mCancellationSignal;
        mCancellationSignal = new CancellationSignal();
        dialog = new AlertDialog.Builder(this).setMessage("Touch♂Me").setCancelable(true).create();
        if (dialog.isShowing())
            dialog.dismiss();
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                mCancellationSignal.cancel();
            }
        });
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
                    manager.authenticate(null, mCancellationSignal, 0, getCallback(), null);
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

    private  FingerprintManager.AuthenticationCallback getCallback()
    {
        FingerprintManager.AuthenticationCallback callback = new FingerprintManager.AuthenticationCallback()
        {

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString)
            {
                super.onAuthenticationError(errorCode, errString);
                Log.i("finger_touch", "onAuthenticationError---" + errString + "|" + errorCode + "|不可再验");
                intent = new Intent(BROADCAST_CODE);
                intent.putExtra("successOrError", true);
                intent.putExtra("msg", errString);
                sendBroadcast(intent);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString)
            {
                super.onAuthenticationHelp(helpCode, helpString);
                Log.i("finger_touch", "onAuthenticationHelp---" + helpString + "|" + helpCode + "|其他原因验证失败，可再验证");
                intent = new Intent(BROADCAST_CODE);
                intent.putExtra("msg", helpString);
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
                Log.i("finger_touch", "onAuthenticationFailed--" + "非登录指纹，可再验证");
                intent = new Intent(BROADCAST_CODE);
                intent.putExtra("msg", "验证失败，请重试");
                sendBroadcast(intent);
            }
        };

        return  callback;
    }




    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String msg = intent.getStringExtra("msg");
            Boolean successOrError = intent.getBooleanExtra("successOrError", false);
            Timer timer = new Timer();
            TimerTask task;
            dialog.setMessage(msg);
            if (successOrError)
            {
                task = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dialog.dismiss();

                            }
                        });
                    }
                };

                timer.schedule(task, 1000);
            } else
            {
                task = new TimerTask()
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
                timer.schedule(task, 1000);

            }


        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (dialog.isShowing())
            dialog.dismiss();
        unregisterReceiver(receiver);
    }
}
