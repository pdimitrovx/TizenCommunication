package uk.ac.aber.dcs.pid4.tizencommunication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private static TextView mTextStatus; //navigation stuff (used to display status app)
    private static MessageAdapter mMessageAdapter; //todo mesasge stuff, describe ite etc
    private ListView mMessageListView; //todo message stuff
    private boolean isServiceBound = false;
    private static boolean start_Btn_clicked;
    private Service_SAP mConsumerService = null;

    private Spinner spinner_time, spinner_sensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_Btn_clicked = false;
        mTextStatus = findViewById(R.id.app_status);
        //todo rename and tidy list view
        mMessageListView = findViewById(R.id.lvMessage);
        mMessageAdapter = new MessageAdapter();
        mMessageListView.setAdapter(mMessageAdapter);
        // Bind service
        isServiceBound = bindService(new Intent(MainActivity.this,
                Service_SAP.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        // Clean up connections
        if (isServiceBound == true && mConsumerService != null) {
            updateTextView("Disconnected");
            mMessageAdapter.clear();
            mConsumerService.clearToast();
        }
        // Un-bind service
        if (isServiceBound) {
            unbindService(mConnection);
            isServiceBound = false;
        }
        start_Btn_clicked = false;
        super.onDestroy();
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.findPeerAgentBtn: {
                if (isServiceBound == true && mConsumerService != null) {
                    mConsumerService.findPeers();
                    start_Btn_clicked = false;
                }
                break;
            }
            case R.id.start_btn: {
                if (isServiceBound == true && start_Btn_clicked == false && mConsumerService != null) {
                    if (mConsumerService.sendData(":300:505:Hello Message!") != -1) {
                        //todo wtf is it sendButtonClicked = true;
                        start_Btn_clicked = true;
                    } else {
                        //todo wtfff sendButtonClicked = false;
                        start_Btn_clicked = false;
                    }
                }
                break;
            }
            default:
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((Service_SAP.LocalBinder) service).getService();
            updateTextView("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            isServiceBound = false;
            updateTextView("onServiceDisconnected");
        }
    };

    public static void addMessage(String data) {
        mMessageAdapter.addMessage(new Message(data));
    }

    public static void updateTextView(final String str) {
        mTextStatus.setText(str);
    }

    public static void updateButtonState(boolean enable) {
        start_Btn_clicked = enable;
    }

    private class MessageAdapter extends BaseAdapter {
        private static final int MAX_MESSAGES_TO_DISPLAY = 20;
        private List<Message> mMessages;

        public MessageAdapter() {
            mMessages = Collections.synchronizedList(new ArrayList<Message>());
        }

        void addMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
                        mMessages.remove(0);
                        mMessages.add(msg);
                    } else {
                        mMessages.add(msg);
                    }
                    notifyDataSetChanged();
                    mMessageListView.setSelection(getCount() - 1);
                }
            });
        }

        void clear() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.clear();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageRecordView = null;
            if (inflator != null) {
                messageRecordView = inflator.inflate(R.layout.message, null);
                TextView tvData = (TextView) messageRecordView.findViewById(R.id.tvData);
                Message message = (Message) getItem(position);
                tvData.setText(message.data);
            }
            return messageRecordView;
        }
    }

    private static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }

    public void initializeSpinners() {
        spinner_time = findViewById(R.id.time_spinner);
        spinner_sensor = findViewById(R.id.sensor_spinner);

        // Create an ArrayAdapter with array of strings w.\ default spinner layout
        ArrayAdapter<CharSequence> adapter_spinner_time = ArrayAdapter.createFromResource(this,
                R.array.spinner_time_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter_spinner_sensor = ArrayAdapter.createFromResource(this,
                R.array.spinner_sensor_array, android.R.layout.simple_spinner_item);
    }

}
//added the service connection thing that is needed in order to set up the sercive bound thing. Explain taht
//added         android:onClick="mOnClick" to each button so the trigger that method that has a switch case statement in it!
