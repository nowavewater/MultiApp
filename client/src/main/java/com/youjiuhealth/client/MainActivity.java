package com.youjiuhealth.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    /**
     * Command to the service to display a message
     */
    static final int MSG_SAY_HELLO = 1;

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean bound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            bound = false;
        }
    };

    public void sayHello(View v) {
        if (!bound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView helloTextView = findViewById(R.id.hello_text_view);
        helloTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sayHello(view);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // Bind to the service
            String activityToStart = "com.youjiuhealth.multiapp.MessengerService";
            Class<?> clazz = Class.forName(activityToStart);
            Intent intent = new Intent(this, clazz);
            intent.setPackage("com.youjiuhealth.multiapp");
            Timber.i("on created intent");
            bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE);
            Timber.i("on bind message service");
        } catch (ClassNotFoundException ignored) {
            Timber.e("on class exception");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }
}
