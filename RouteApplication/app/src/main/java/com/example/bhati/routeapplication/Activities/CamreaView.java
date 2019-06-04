package com.example.bhati.routeapplication.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.bhati.routeapplication.Database.DBHelper;
import com.example.bhati.routeapplication.R;
import com.example.bhati.routeapplication.Servicess.background_location_updates;
import com.example.bhati.routeapplication.Testing.Test;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static com.example.bhati.routeapplication.Servicess.background_location_updates.Latitude;
import static com.example.bhati.routeapplication.Servicess.background_location_updates.Longitude;

public class CamreaView extends AppCompatActivity  implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    private Button startRecording = null;
    private android.hardware.Camera mCamera;
    String vido_file_path;
    ToggleButton btnrescordaudio;
    Button recoderbtn,recoder_stop;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean isVideoCapturing;
    ArrayList<LatLng> arrayList;
    ArrayList<String> arrayList_recorder;
    Bundle bundle = new Bundle();
    private DBHelper myDb;
    FFmpeg fFmpeg;
    File video_file;
    MyReceiver myReceiver;
    Timer timer;
    Intent ServiceIntent;
    private boolean receiversRegistered;
    Thread thread;
    boolean is_userRecordingAudio=false;
    ArrayList<String> arrayList_video;
    TextView recorder_timer;
     Timer t;
    int minute =0, seconds = 0, hour = 0;
    JSONObject obj ;
    JSONArray jsonArray;
    JSONObject finalobject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_capture_dialogue);
        arrayList = new ArrayList<>();
        arrayList_recorder=new ArrayList<>();
        arrayList_video = new ArrayList<>();
        obj = new JSONObject();
         jsonArray = new JSONArray();
        myDb = new DBHelper(this);
        myReceiver = new MyReceiver();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Start_Service();
            }
        }, 0, 5000);//1 Minutes
        setUiWidgets();
        initializefFmpeg();
    }
    protected void startRecording() throws IOException
    {
        final long currentTimeMillis = System.currentTimeMillis();
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) { folder.mkdir();
        }
        vido_file_path = currentTimeMillis + ".mp4";
        video_file = new File(folder, vido_file_path);
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(video_file.getPath());
        mrec.prepare();
        mrec.start();
    }
    public void createAudioFile()
    {
        final long currentTimeMillis = System.currentTimeMillis();
        final String audio_file_path = currentTimeMillis + ".mp3";
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) { folder.mkdir();
        }
        File outfile = new File(folder, audio_file_path);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null){
            Camera.Parameters parameters = mCamera.getParameters();
            List<String>    focusModes = parameters.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {

                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void IntialValues()
    {
        preferences = getSharedPreferences("isVideoCapturing", MODE_PRIVATE);
    }
    public void setUiWidgets()
    {
        mCamera = Camera.open();
        recorder_timer=findViewById(R.id.recorder_timer);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        btnrescordaudio=findViewById(R.id.btnrescordaudio);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CamreaView.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        createAudioFile();
        IntialValues();
        btnrescordaudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {

                    is_userRecordingAudio=true;
                }
                else
                {
                    is_userRecordingAudio=false;
                }
            }
        });
        recoderbtn=findViewById(R.id.recoder);
        recoderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    StartRecodingTime();
                    recordingThreadStart();
                    recoderbtn.setEnabled(false);
                    recoderbtn.setAlpha(0.5f);
                    recoder_stop.setEnabled(true);
                    recoder_stop.setAlpha(1f);
                    isVideoCapturing = true;
                    editor = preferences.edit();
                    editor.putBoolean("is_video_capturing", true);
                    editor.apply();
                    startRecording();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        recoder_stop=findViewById(R.id.recoder_stop);
        recoder_stop.setEnabled(false);
        recoder_stop.setAlpha(0.5f);
        recoder_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                     finalobject = new JSONObject();
                    finalobject.put("Data", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                is_userRecordingAudio=false;
                t.cancel();
                mrec.stop();
                mCamera.stopPreview();
                isVideoCapturing = false;
                editor = preferences.edit();
                editor.putBoolean("is_video_capturing", false);
                editor.apply();
                Uri file_uri=Uri.fromFile(video_file);
                float file_size=video_file.length();
                Log.d("FILE_URI","IS:"+file_uri);
                Log.d("FILE_SIZE","IS:"+file_size);
                Log.d("FILE_path","IS:"+video_file);
                Log.d("LATLNG","SIZE:"+arrayList.size());
                Log.d("OTHERS","ARE :"+finalobject.toString());
                Log.d("OTHERS","ARE :"+finalobject.length());
                dialogueSave(file_uri,video_file,file_size);
            }
        });
    }
    public void dialogueSave(Uri uri,File file,float size)
    {
        Dialog dialog = new Dialog(CamreaView.this);
        dialog.setContentView(R.layout.dialog);
        Button btnPlay = dialog.findViewById(R.id.btnPlay);
        ImageView btnCancel = dialog.findViewById(R.id.btnCancelImage);
        Button btnRetry = dialog.findViewById(R.id.btnRetry);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        VideoView videoView = dialog.findViewById(R.id.videoView);
        Uri finalUri = uri;
        btnPlay.setOnClickListener(v -> {
            videoView.setVideoURI(finalUri);
            videoView.start();

        });
        btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            recoder_stop.setEnabled(false);
            recoder_stop.setAlpha(0.5f);
            recoderbtn.setEnabled(true);
            recoderbtn.setAlpha(1);
            Dialog dialogUsername = new Dialog(CamreaView.this, R.style.MyDialogTheme);
            dialogUsername.setContentView(R.layout.username);
            dialogUsername.setCancelable(false);
            EditText username = dialogUsername.findViewById(R.id.edUsername);
            Button btnSave2 = dialogUsername.findViewById(R.id.btnSave);
            Button btnCancel2 = dialogUsername.findViewById(R.id.btnCancel);

            btnSave2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = username.getText().toString();
                    if (TextUtils.isEmpty(name)) {
                        username.setError("Required field");
                        return;
                    }

                    dialogUsername.dismiss();
                    final long currentTimeMillis = System.currentTimeMillis();
                    final String audio_file_path = currentTimeMillis + ".mp3";
                    CreatSubExcelSheet(video_file.getName());
                    storeDataInDb(finalUri, file, size, name,audio_file_path);

                }
            });

            btnCancel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogUsername.dismiss();
                }
            });

            dialogUsername.show();


            //  convertToAudio(new File(String.valueOf(finalUri)));  //TODO convert Speech to text
            /**/
        });

        btnRetry.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }
    private void storeDataInDb(Uri finalUri, File file, Float size, String username,String audio_file_path) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String date = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(calendar.getTime());
        size = size / 1000;

        String size_in_mbs = String.format("%.2f", size);
        if (arrayList.size() > 0) {
            String city_name = GetCurrentLocationName(arrayList.get(0).getLatitude(), arrayList.get(0).getLongitude());
            boolean i = myDb.insertData(finalUri, file.getName(),
                    "speech", arrayList.toString(), date,
                    size_in_mbs + " MB", time, date,
                    city_name, username,audio_file_path,finalobject.toString());
            SaveDataLocallly(username, city_name, date, time, getVideoTime(finalUri.toString()), size_in_mbs + "MB",
                    finalUri.toString(), file.getName(),arrayList.toString(),audio_file_path);
            if (i) {
                arrayList.clear();
                File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
                if (!folder.exists()) { folder.mkdir();
                }
                Log.d("AUDIO_LIST","Size:"+arrayList_video.size());

                for (int j=0;j<arrayList_video.size();j++)
                {
                    Log.d("ITEM_Name","Is"+arrayList_video.get(j));
                    String[] separated = arrayList_video.get(j).split(",");
                    String point_lat =separated[0]; // this wil   l contain "Fruit"
                    String point_long_time=separated[1];
                    Log.d("REMIANING","STRING"+point_long_time);
                    String point_time=separated[2];
                    String point_systemtime=separated[3];
                    saveSubListSheet(getCurrentDate(),point_systemtime,point_lat,point_long_time,file.getName(),point_time,audio_file_path,"",
                            video_file.getName(),j+2);
                }
                convertToAudio(file,audio_file_path,folder);
            }
        } else {
            deleteUnUsedFile(finalUri.getPath(),file);
            Toast.makeText(CamreaView.this, "No Points to save.", Toast.LENGTH_SHORT).show();
        }

    }
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            //Log.d("BroadcastReceiver", "onReceive:");
            // Bundle bundle = getIntent().getExtras().getBundle("LatLngBundle");


            bundle = arg1.getBundleExtra("LatLngBundle");

            if (bundle != null) {
                arrayList = bundle.getParcelableArrayList("LatLng");
                // Log.d("BroadcastReceiver", "onReceive: array = " + arrayList);
            }
        }

    }
    private String GetCurrentLocationName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(CamreaView.this);
        if (geocoder.isPresent()) {
            try {
                geocoder = new Geocoder(CamreaView.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String add = address.getLocality();
                    return add;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        return null;
    }
    private String getVideoTime(String videoUri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.parse(videoUri));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );
        Log.d("Adapter", "getVideoTime: "+timeInMillisec);
        retriever.release();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        // mTimeText.setText("Time: " + dateFormat.format(timeInMillisec));
        //timeInMillisec = 5000;
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeInMillisec),
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMillisec)),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));

        return hms;
    }
    public void deleteUnUsedFile(String path,File file) {
        File fdelete = new File(path);
        try {
            if (fdelete.exists()) {
                file.getCanonicalFile().delete();
                getApplicationContext().deleteFile(file.getName());
            } else {
                Log.d("file Deleted :", "IS :" + path);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void SaveDataLocallly(String user_name, String city_name, String date, String time, String duaration,
                                 String size, String video_path, String video_name,String cordniate,String audio_file) {
        int row = myDb.getAllData().size();
        String Fnamexls = "localDb" + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        Workbook workbook;
        try {
            int a = 1;
            workbook = Workbook.getWorkbook(file, wbSettings);
            WritableWorkbook wb = Workbook.createWorkbook(file, workbook);
            //workbook.createSheet("Report", 0);
            WritableSheet sheet = wb.getSheet("First Sheet");
            Label cell_user_name = new Label(0, row, user_name);
            Label cell_city_name = new Label(1, row, city_name);
            Label cell_date = new Label(2, row, date);
            Label cell_time = new Label(3, row, time);
            Label cell_duration = new Label(4, row, duaration);
            Label cell_size = new Label(5, row, size);
            Label cell_path = new Label(6, row, video_path);
            Label cell_name = new Label(7, row, video_name);
            Label cell_cordniate = new Label(7, row, cordniate);
            Label cell_file_audio = new Label(8, row, audio_file);

            try {
                sheet.addCell(cell_user_name);
                sheet.addCell(cell_city_name);
                sheet.addCell(cell_date);
                sheet.addCell(cell_time);
                sheet.addCell(cell_duration);
                sheet.addCell(cell_size);
                sheet.addCell(cell_path);
                sheet.addCell(cell_name);
                sheet.addCell(cell_cordniate);
                sheet.addCell(cell_file_audio);

            } catch (RowsExceededException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            wb.write();
            wb.close();
            try {
                workbook.close();
            } catch (Exception ex) {

            }
            //createExcel(excelSheet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (BiffException e) {


        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
    private void convertToAudio(File video,String file_path,File folder) {
        fFmpeg = FFmpeg.getInstance(CamreaView.this);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setIndeterminate(true);

        //String command  = "ffmpeg -i "+video+" -vn -ar 44100 -ac 2 -ab 192k -f mp3 Sample.mp3";
        //String command  = "ffmpeg -i +"+video+" -vn -acodec copy output-audio.aac";


        //fileName = fileName + filePath.substring(i);
        //int i = path.indexOf(".");
        //fileName = fileName + path.substring(i);
        // String command = "-y -i " + video + " -an " + folder + "/" + "Hussain_abc.mp4";
        String command = "-y -i " + video + " -b:a 192K -vn " + folder + "/" + "" + file_path;
        String[] cmd = command.split(" ");

        try {
            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    //  Log.d("FFMpeg", "onStart: ");
                    progress.setMessage("Please wait...");
                    progress.show();
                }

                @Override
                public void onProgress(String message) {
//                    progress.setMessage(message);
                    //Log.d("FFMpeg", message);
                }

                @Override
                public void onFailure(String message) {
                    // Log.d("FFMpeg",message);
                    progress.dismiss();
                }

                @Override
                public void onSuccess(String message) {

                    progress.dismiss();

                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Log.e("FFMpeg", "convertToAudio: " , e);
            e.printStackTrace();
        }

    }
    public void initializefFmpeg() {
        fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.d("FFMPEG", "onFailure: ");
                }

                @Override
                public void onSuccess() {
                    Log.d("FFMPEG", "onSuccess: ");
                }

                @Override
                public void onStart() {
                    Log.d("FFMPEG", "onStart: ");
                }

                @Override
                public void onFinish() {
                    Log.d("FFMPEG", "onFinish: ");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
    private void Start_Service() {
        ServiceIntent = new Intent(CamreaView.this, background_location_updates.class);
        ServiceIntent.putExtra("is_video_capturing", isVideoCapturing);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try
            {
                startForegroundService(ServiceIntent);
            }
            catch (Exception ex)
            {

            }
        } else {
            startService(ServiceIntent);
        }
        register_Reciever();
    }
    private void register_Reciever() {
        if (!receiversRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(background_location_updates.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);
            receiversRegistered = true;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            if (receiversRegistered) {
                unregisterReceiver(myReceiver);
                receiversRegistered = false;
            }
        } catch (Exception e) {

        }
    }
    public void CreatSubExcelSheet(String sheet_name) {
        String Fnamexls = sheet_name + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
            Log.d("Make", "Directory");
            directory.mkdirs();
            File file = new File(directory, Fnamexls);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            try {
                int a = 1;
                workbook = Workbook.createWorkbook(file, wbSettings);
                //workbook.createSheet("Report", 0);
                WritableSheet sheet = workbook.createSheet(sheet_name, 0);
                Label cell_date = new Label(0, 0, "Date");
                Label cell_time = new Label(1, 0, "Time");
                Label cell_lattitude = new Label(2, 0, "Latitude");
                Label cell_longitude = new Label(3, 0, "Latongitude");
                Label cell_media = new Label(4, 0, "Media");
                Label cell_audio_time = new Label(5, 0, "Audio Time");
                Label cell_audio_path = new Label(6, 0, "Audio Path");
                Label cell_text = new Label(7, 0, "text");
                try {
                    sheet.addCell(cell_date);
                    sheet.addCell(cell_time);
                    sheet.addCell(cell_lattitude);
                    sheet.addCell(cell_longitude);
                    sheet.addCell(cell_media);
                    sheet.addCell(cell_audio_time);
                    sheet.addCell(cell_audio_path);
                    sheet.addCell(cell_text);
                } catch (RowsExceededException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (WriteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                workbook.write();
                try {
                    workbook.close();
                } catch (WriteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //createExcel(excelSheet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }

    }
    public void saveSubListSheet(String Date, String time, String latitude, String longitude, String mdeia,
                                 String audio_time, String audio_path, String text,
                                 String file_name,int row )
    {

        String Fnamexls = file_name + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        Workbook workbook;
        try {
            int a = 1;
            workbook = Workbook.getWorkbook(file, wbSettings);
            WritableWorkbook wb = Workbook.createWorkbook(file, workbook);
            //workbook.createSheet("Report", 0);
            WritableSheet sheet = wb.getSheet(file_name);
            Label cell_date = new Label(0, row, Date);
            Label cell_time = new Label(1, row, time);
            Label cell_latitude = new Label(2, row, latitude);
            Label cell_longitude = new Label(3, row, longitude);
            Label cell_mdeia = new Label(4, row, mdeia);
            Label cell_audio_time = new Label(5, row, audio_time);
            Label cell_audio_path = new Label(6, row, audio_path);
            Label cell_audio_text = new Label(7, row, text);

            try {
                sheet.addCell(cell_date);
                sheet.addCell(cell_time);
                sheet.addCell(cell_latitude);
                sheet.addCell(cell_longitude);
                sheet.addCell(cell_mdeia);
                sheet.addCell(cell_audio_time);
                sheet.addCell(cell_audio_path);
                sheet.addCell(cell_audio_text);

            } catch (RowsExceededException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            wb.write();
            wb.close();
            try {
                workbook.close();
            } catch (Exception ex) {

            }
            //createExcel(excelSheet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (BiffException e) {


        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
    public void recordingThreadStart()
    {

         thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000);
                        if(is_userRecordingAudio)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            Log.d("THREAD", "WORKING" + Latitude);
                            Log.d("THREAD", "WORKING" + Longitude);
                            Log.d("VIDEO","TIME :"+""
                                    + (hour > 9 ? hour : ("0" + hour)) + " : "
                                    + (minute > 9 ? minute : ("0" + minute))
                                    + " : "
                                    + (seconds > 9 ? seconds : "0" + seconds));
                            arrayList_video.add(Latitude+","+Longitude+","
                                    + (hour > 9 ? hour : ("0" + hour)) + " : "
                                    + (minute > 9 ? minute : ("0" + minute))
                                    + " : "
                                    + (seconds > 9 ? seconds : "0" + seconds)+","+currentTime);
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
    public void StartRecodingTime()
    {
         t = new Timer("hello", true);
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                recorder_timer.post(new Runnable() {
                    public void run() {
                        seconds++;
                        if (seconds == 60) {
                            seconds = 0;
                            minute++;
                        }
                        if (minute == 60) {
                            minute = 0;
                            hour++;
                        }
                        recorder_timer.setText(""
                                + (hour > 9 ? hour : ("0" + hour)) + " : "
                                + (minute > 9 ? minute : ("0" + minute))
                                + " : "
                                + (seconds > 9 ? seconds : "0" + seconds));
                        obj = new JSONObject();
                        String time=(hour > 9 ? hour : ("0" + hour)) + " : "
                                + (minute > 9 ? minute : ("0" + minute))
                                + " : "
                                + (seconds > 9 ? seconds : "0" + seconds);
                        try {
                            obj.put("LATITUDE", Latitude);
                            obj.put("LONGITUDE", Longitude);
                            obj.put("TIME", time);

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        jsonArray.put(obj);
                    }
                });

            }
        }, 1000, 1000);
    }
    public String  getCurrentDate()
    {
        String today_date=null;
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        today_date=formattedDate;
        return today_date;
    }
}
