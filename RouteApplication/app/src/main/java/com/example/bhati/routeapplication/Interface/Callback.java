package com.example.bhati.routeapplication.Interface;

import java.io.File;

public interface Callback {
    void onProgress(String progress);
    void onSuccess(File convertedFile , String type);
    void onFailure(Exception error);
    void onNotAvailable(Exception error);
    void onFinish();
}
