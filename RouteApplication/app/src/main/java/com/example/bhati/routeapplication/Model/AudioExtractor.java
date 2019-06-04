package com.example.bhati.routeapplication.Model;

import android.content.Context;

import com.example.bhati.routeapplication.Interface.Callback;

import java.io.File;
import java.io.IOException;

public class AudioExtractor {
    private Context context;
    private File video;
    private Callback FFMpegCallBack;
    private String outputPath;
    private String outputFileName;

    public void AudioExtractor(Context context)
    {
        this.context = context;
    }

    public void setVideo(File video) {
        this.video = video;
    }

    public void setFFMpegCallBack(Callback FFMpegCallBack) {
        this.FFMpegCallBack = FFMpegCallBack;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void extract()
    {
        if (!video.exists())
        {
            FFMpegCallBack.onFailure(new IOException("FILE NOT FOUND"));
            return;
        }
        if (!video.canRead())
        {
            FFMpegCallBack.onFailure(new Exception("Can't read the file"));
            return;
        }

        String outputLocation = String.valueOf(Utils.getConvertedFile(outputPath , outputFileName));
       // String format = arrayOf("-i", video!!.path, "-vn", "-ar", "44100", "-ac", "2", "-ab", "192", "-f", "mp3", outputLocation.path);
    }
}
