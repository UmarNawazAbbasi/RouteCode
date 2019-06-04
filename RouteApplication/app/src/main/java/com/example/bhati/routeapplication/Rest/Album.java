package com.example.bhati.routeapplication.Rest;

public class Album {
    public String id;
    public String latLngs;
    public String speech;
    public String duration;
    public String video_url;
    public String video_name;
    public String time;
    public String date;
    public String size;
    public String city;
    public String name;
    public String audio_file;
    public String recorder_file;

    public Album() {
    }

    public Album(String id, String latLngs, String speech, String duration,
                 String video_url, String video_name, String time,
                 String date, String size, String city , String name) {
        this.id = id;
        this.latLngs = latLngs;
        this.speech = speech;
        this.duration = duration;
        this.video_url = video_url;
        this.video_name = video_name;
        this.time = time;
        this.date = date;
        this.size = size;
        this.city = city;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(String latLngs) {
        this.latLngs = latLngs;
    }

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getAudio_file() {
        return audio_file;
    }

    public void setAudio_file(String audio_file) {
        this.audio_file = audio_file;
    }

    public String getRecorder_file() {
        return recorder_file;
    }

    public void setRecorder_file(String recorder_file) {
        this.recorder_file = recorder_file;
    }
}
