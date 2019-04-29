package uk.ac.aber.dcs.pid4.tizencommunication;

import java.io.Serializable;

public class Tizen_Message implements Serializable {
    private int duration, sensor_type;
    private int receiver_sensor_type;
    private String receiver_reading_accuracy;
    private String receiver_reading_heart_rate;
    private String receiver_reading_x, receiver_reading_y, receiver_reading_z;
    private String raw_data_whole_msg;
    private String receiver_reading_timestamp;


    //Constructor for a start command message
    public Tizen_Message(int durat_s, int type_sens) {
        this.duration = durat_s;
        this.sensor_type = type_sens;
    }

    //Constructor for receiving Data (receiving payload) - HRM
    public Tizen_Message(int type_sens, String sens_hr, String sens_accur, String time_epoch) {
        this.receiver_sensor_type = type_sens;
        this.receiver_reading_heart_rate = sens_hr;
        this.receiver_reading_accuracy = sens_accur;
        this.receiver_reading_timestamp = time_epoch;
    }

    //Constructor for receiving Data (receiving payload) - ACCELEROMETER
    public Tizen_Message(int type_sens, String sens_x, String sens_y, String sens_z, String sens_accur, String time_epoch) {
        this.receiver_sensor_type = type_sens;
        this.receiver_reading_x = sens_x;
        this.receiver_reading_y = sens_y;
        this.receiver_reading_z = sens_z;
        this.receiver_reading_accuracy = sens_accur;
        this.receiver_reading_timestamp = time_epoch;
    }

    //Raw Data Constructor
    public Tizen_Message(String all_data) {
        this.raw_data_whole_msg = all_data;
    }

    //Empty Constructor
    public Tizen_Message() {

    }

    public int getReceiver_sensor_type() {
        return receiver_sensor_type;
    }

    public void setReceiver_sensor_type(int receiver_sensor_type) {
        this.receiver_sensor_type = receiver_sensor_type;
    }

    public String getReceiver_reading_accuracy() {
        return receiver_reading_accuracy;
    }

    public void setReceiver_reading_accuracy(String receiver_reading_accuracy) {
        this.receiver_reading_accuracy = receiver_reading_accuracy;
    }

    public String getReceiver_reading_heart_rate() {
        return receiver_reading_heart_rate;
    }

    public void setReceiver_reading_heart_rate(String receiver_reading_heart_rate) {
        this.receiver_reading_heart_rate = receiver_reading_heart_rate;
    }

    public String getReceiver_reading_timestamp() {
        return receiver_reading_timestamp;
    }

    public void setReceiver_reading_timestamp(String receiver_reading_timestamp) {
        this.receiver_reading_timestamp = receiver_reading_timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(int sensor_type) {
        this.sensor_type = sensor_type;
    }

    //custom string builder for command
    public String command_Builder() {
        String type = String.valueOf(this.sensor_type);
        String time = String.valueOf(this.duration);
        String command_to_return = type + ":" + time;
        return command_to_return;
    }

}
