package uk.ac.aber.dcs.pid4.tizencommunication;

import java.io.Serializable;

public class Tizen_Message implements Serializable {
    private int duration, sensor_type;
    private int receiver_sensor_type;
    private String receiver_reading_accuracy, receiver_reading, receiver_reading_timestamp;

    //Constructor for a start command message
    public Tizen_Message(int durat_s, int type_sens) {
        this.duration = durat_s;
        this.sensor_type = type_sens;
    }

    //Constructor for receiving Data (receiving payload)
    public Tizen_Message(int type_sens, String sens_acc, String sens_reading, String timestamp) {
        this.receiver_sensor_type = type_sens;
        this.receiver_reading_accuracy = sens_acc;
        this.receiver_reading = sens_reading;
        this.receiver_reading_timestamp = timestamp;
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

    public String getReceiver_reading() {
        return receiver_reading;
    }

    public void setReceiver_reading(String receiver_reading) {
        this.receiver_reading = receiver_reading;
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
