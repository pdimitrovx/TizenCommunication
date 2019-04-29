package uk.ac.aber.dcs.pid4.tizencommunication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {
    private static TextView mTextStatus; //navigation stuff (used to display status app)
    private static MessageAdapter mMessageAdapter; //todo mesasge stuff, describe ite etc
    private ListView mMessageListView; //todo message stuff
    private boolean isServiceBound = false;
    private static boolean start_Btn_clicked;
    private Service_SAP mConsumerService = null;
    private boolean watch_is_connected = false;
    private Spinner spinner_time, spinner_sensor;
    private Button startReadingBtn, stopReading_btn;
    private static boolean is_reading_started = false;
    private static long sayBackPress;
    static boolean date_set = false;
    static String today = "error_set_today";
    private static FirebaseDatabase mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeSpinners();
        start_Btn_clicked = false;
        mTextStatus = findViewById(R.id.app_status);
        //todo rename and tidy list view
        mMessageListView = findViewById(R.id.lvMessage);
        mMessageAdapter = new MessageAdapter();
        mMessageListView.setAdapter(mMessageAdapter);
        // Bind service
        isServiceBound = bindService(new Intent(MainActivity.this,
                Service_SAP.class), mConnection, Context.BIND_AUTO_CREATE);



       /* mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d("Firebase", "onDataChange: Added information to database: \n" +
                        dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });


    */
    }

    @Override
    public void onBackPressed() {
        // Write a message to the database


        if (sayBackPress + 1500 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(MainActivity.this, "Press once again to exit!", Toast.LENGTH_SHORT).show();
            sayBackPress = System.currentTimeMillis();
        }
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

    //CHeck if watch is connected
    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.findPeerAgentBtn: {
                if (isServiceBound == true && mConsumerService != null) {
                    mConsumerService.findPeers();
                    start_Btn_clicked = false;
                    watch_is_connected = true;
                    Toast toast_is_watch_connected = makeText(this, "The watch is ready", Toast.LENGTH_SHORT);
                    toast_is_watch_connected.show();

                } else {
                    watch_is_connected = false;
                    Toast toast_is_watch_connected = makeText(this, "Connectivity issue!", Toast.LENGTH_SHORT);
                    toast_is_watch_connected.show();
                }
                break;
            }
            case R.id.start_btn: {
                if (isServiceBound && !start_Btn_clicked && mConsumerService != null) {
                    int sensor_type_selected = spinner_sensor.getSelectedItemPosition();
                    int sensor_time_selected = spinner_time.getSelectedItemPosition();

                    if (sensor_type_selected >= 0 && sensor_type_selected <= 2) {
                        if (sensor_time_selected >= 0 && sensor_time_selected <= 7) {
                            Tizen_Message command_message_objbect = new Tizen_Message(sensor_type_selected, sensor_time_selected);
                            String command_message = command_message_objbect.command_Builder();
                            date_set = false;

                            if (mConsumerService.sendData(command_message) != -1) {
                                startReadingBtn.setEnabled(false);
                                stopReading_btn.setEnabled(true);
                                start_Btn_clicked = true;
                                is_reading_started = true;

                            } else {
                                start_Btn_clicked = false;
                                startReadingBtn.setEnabled(true);
                                is_reading_started = false;
                            }

                        } else {
                            //some error or no selection
                            Toast toast1 = makeText(this, "Select Time !!", Toast.LENGTH_SHORT);
                            toast1.show();
                        }
                    } else {
                        //some error or no selection
                        Toast toast2 = makeText(this, "Select Sensor !!", Toast.LENGTH_SHORT);
                        toast2.show();
                    }
                } else {
                    Toast toast2 = makeText(this, "Error: Try initializing again", Toast.LENGTH_SHORT);
                    toast2.show();
                }
                break;
            }
            case R.id.stopReading_btn: {
                if (isServiceBound && mConsumerService != null) {
                    if (mConsumerService.sendData("test") != -1) {
                        mConsumerService.sendData("STOP");
                        startReadingBtn.setEnabled(true);
                        stopReading_btn.setEnabled(false);
                        is_reading_started = false;
                    }
                }
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

        startReadingBtn = findViewById(R.id.start_btn);
        stopReading_btn = findViewById(R.id.stopReading_btn);


        // Create an ArrayAdapter with array of strings w.\ default spinner layout
        ArrayAdapter<CharSequence> adapter_spinner_time = ArrayAdapter.createFromResource(this,
                R.array.spinner_time_array, android.R.layout.simple_spinner_item);
        adapter_spinner_time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter_spinner_sensor = ArrayAdapter.createFromResource(this,
                R.array.spinner_sensor_array, android.R.layout.simple_spinner_item);
        adapter_spinner_sensor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapters
        spinner_time.setAdapter(adapter_spinner_time);
        spinner_sensor.setAdapter(adapter_spinner_sensor);

    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    private static void sendToFirebase(Tizen_Message msg, boolean is_hrm, String today) {
        if (is_hrm) {


            //database key is the time epoch (from Tizen)

            FirebaseDatabase database = getDatabase();
            DatabaseReference myRef = database.getReference("hrm/"+today + msg.getReceiver_reading_timestamp());
            myRef.setValue(msg);
        }

    }

    //https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.9
    public static void dataReceived(String data_received) {
        String extracted_command; //the data between the prefixes, start end
        String sensor_identifier; //the first word in the message, i.e sensor identifier
        String HRM_id = "HRM_START"; //the sensor identifier for Heart Rate Sensor
        String ACC_id = "ACC_START"; //the identifier for Accelerometer sensor

        StringTokenizer sensor_type_tokenizer = new StringTokenizer(data_received, ":");
        sensor_identifier = sensor_type_tokenizer.nextToken();


        Log.d("data_received1", "First token is: " + sensor_identifier);

        if (sensor_identifier.equals(HRM_id) && is_reading_started) {

            while (!date_set){
                //database parrent is the time to the second when the reading began
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss/");
                Date dateOnly = new Date();
                today = (dateFormat.format(dateOnly));
                date_set = true;
            }

            //data is from hrm sensor
            extracted_command = StringUtils.substringBetween(data_received, "HRM_START:", ":END_HRM");
            StringTokenizer sensor_data_tokenizer = new StringTokenizer(extracted_command, ":");
            String heart_rate = sensor_data_tokenizer.nextToken();
            String accuracy = sensor_data_tokenizer.nextToken();
            String str_epoch_timestamp_from_sensor = sensor_data_tokenizer.nextToken();

            Log.d("data_received", "heart_rate: " + heart_rate);
            Log.d("data_received", "timestamp_from_sensor: " + str_epoch_timestamp_from_sensor);
            Log.d("data_received", "accuracy: " + accuracy);
            Tizen_Message hrm_reading = new Tizen_Message(0, heart_rate, accuracy, str_epoch_timestamp_from_sensor);
            sendToFirebase(hrm_reading, true, today);


        } else if (sensor_identifier.equals(ACC_id) && is_reading_started) {
            extracted_command = StringUtils.substringBetween(data_received, "ACC_START:", ":END_ACC");
            StringTokenizer sensor_data_tokenizer = new StringTokenizer(extracted_command, ":");
            String x_axis = sensor_data_tokenizer.nextToken();
            String y_axis = sensor_data_tokenizer.nextToken();
            String z_axis = sensor_data_tokenizer.nextToken();

            String accuracy = sensor_data_tokenizer.nextToken();
            String epoch_timestamp = sensor_data_tokenizer.nextToken();
            long epoch_timestamp_int = Integer.valueOf(epoch_timestamp);


            Log.d("data_received", "x axis : " + x_axis);
            Log.d("data_received", "y axis : " + y_axis);
            Log.d("data_received", "z axis : " + z_axis);

            Log.d("data_received", "timestamp: " + epoch_timestamp_int);
            Log.d("data_received", "accuracy: " + accuracy);
        }
    }

    private static long time_epoch_androidSys() {
        long epoch = System.currentTimeMillis() / 1000;
        return epoch;
    }


}
//added the service connection thing that is needed in order to set up the sercive bound thing. Explain taht
//added         android:onClick="mOnClick" to each button so the trigger that method that has a switch case statement in it!
