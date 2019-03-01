package com.example.bhati.routeapplication.Model;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class Utils {
    String outputPath;
    public String get()
    {
        String path = Environment.getExternalStorageDirectory().toString() + File.separator + "Route_Application" + File.separator;
        File folder = new File(path);
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        return path;
    }

    public File copyFileToExternalStorage(int id , String name , Context context)
    {
        String path = outputPath + name;
        return new File(path);
    }

    public static File getConvertedFile(String folder, String fileName)
    {
        File f = new File(folder);
        if (!f.exists())
        {
            f.mkdirs();
        }
        return new File(f.getAbsolutePath()+File.separator+fileName);
    }
}
