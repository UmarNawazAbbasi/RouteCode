package com.example.bhati.routeapplication.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.bhati.routeapplication.Database.DBHelper;
import com.example.bhati.routeapplication.Interface.Callback;
import com.example.bhati.routeapplication.Model.GPSTracker;
import com.example.bhati.routeapplication.Model.LatLngInterpolator;
import com.example.bhati.routeapplication.Model.MarkerAnimation;
import com.example.bhati.routeapplication.R;
import com.example.bhati.routeapplication.Servicess.background_location_updates;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.protobuf.ByteString;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Home extends AppCompatActivity
        implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, Callback , SurfaceHolder.Callback {
    private MapView mapView;
    public static MapboxMap map;
    public static LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private PermissionsManager permissionsManager;
    private Button btnCamera;
    private static final int VIDEO_CAPTURED = 1010;
    private VideoView videoView;
    private Uri videoFileUri;
    private Button btnFiles;

    private Location originLocation;
    private Marker currentLocationMarker;
    private Marker marker;
    private boolean isVideoCapturing;
    private OfflineManager offlineManager;
    private ArrayList<LatLng> list;
    private boolean isCurrentlocation;
    private FirebaseAuth mAuth;
    private GPSTracker gpsTracker;
    Timer timer;
    ArrayList<LatLng> arrayList;
    Bundle bundle = new Bundle();
    MyReceiver myReceiver;
    FFmpeg fFmpeg;
    private DBHelper myDb;
    File file;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean receiversRegistered;
    // private float size;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //String compressFilePath;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    private Button startRecording = null;
    File video;
    private android.hardware.Camera mCamera;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_home);
        initialize();

        preferences = getSharedPreferences("isVideoCapturing", MODE_PRIVATE);
        Mapbox.getInstance(this, getString(R.string.access_token));
        mapView = findViewById(R.id.mapView);
        btnFiles = findViewById(R.id.btnFiles);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        btnCamera = findViewById(R.id.btnCamera);
        myDb = new DBHelper(this);
        if (myDb.getAllData().size() < 1) {
            CreatExcelSheet();
        }
        btnCamera.setOnClickListener(v -> {

//            new AlertDialog.Builder(this, R.style.MyDialogTheme)
//                    .setTitle("Alert")
//                    .setMessage("User can select the video Resolution from there Phone Settings before start capturing video")
//                    .setPositiveButton("OK", (dialog, which) -> {
//                        dialog.dismiss();
//                        isVideoCapturing = true;
//                        editor = preferences.edit();
//                        editor.putBoolean("is_video_capturing", true);
//                        editor.apply();
//
//                        Intent captureVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                        ///**/ captureVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//                        startActivityForResult(captureVideoIntent, VIDEO_CAPTURED);
//                    })
//                    .setCancelable(false)
//                    .show();
            //Start_Service();
            // speech();
            Intent cameraview=new Intent(Home.this,CamreaView.class);
            startActivity(cameraview);
        });
        btnFiles.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, FileScreenActivity.class));
            finish();
        });
        list = new ArrayList<>();
        timer = new Timer();
        arrayList = new ArrayList<>();
        myReceiver = new MyReceiver();
        timer.scheduleAtFixedRate(new TimerTask() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
//                Start_Service();
            }
        }, 0, 5000);//1 Minutes
    }

    Intent ServiceIntent;

    private void Start_Service() {
        ServiceIntent = new Intent(Home.this, background_location_updates.class);
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

    public void initialize() {
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

    float size;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURED) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getParcelableExtra("file");
                try {
                    AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                    FileInputStream fis = videoAsset.createInputStream();
                    File root = new File(Environment.getExternalStorageDirectory(), "/RouteApp");  //you can replace RecordVideo by the specific folder where you want to save the video
                    if (!root.exists()) {
                        System.out.println("No directory");
                        root.mkdirs();
                    }


                    file = new File(root, "android_" + System.currentTimeMillis() + ".mp4");
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fis.close();
                    fos.close();
                    size = file.length();
                    size = size / 1024;
                    uri = Uri.fromFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                videoFileUri = data.getData();
//                File file = new File(String.valueOf(videoFileUri));
//
//                final String path = getPathFromURI(videoFileUri);

//                if (path != null) {
////                    File f = new File(path);
////                    size = f.length();
////                    size = size/1024;
////                    uri = Uri.fromFile(f);
//                }

                Dialog dialog = new Dialog(Home.this);
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
                    Dialog dialogUsername = new Dialog(Home.this, R.style.MyDialogTheme);
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
                            File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
                            if (!folder.exists()) { folder.mkdir();
                            }
                            final long currentTimeMillis = System.currentTimeMillis();
                            final String audio_file_path = currentTimeMillis + ".mp3";
                            storeDataInDb(finalUri, file, size, name,audio_file_path);
                            convertToAudio(file,audio_file_path,folder);
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
                    Intent captureVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(captureVideoIntent, VIDEO_CAPTURED);
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());
                dialog.setCancelable(false);
                dialog.show();
                isVideoCapturing = false;
                editor = preferences.edit();
                editor.putBoolean("is_video_capturing", false);
                editor.apply();

            } else {
                if (arrayList.size() > 0) {
                    arrayList.clear();
                }
                isVideoCapturing = false;
                editor = preferences.edit();
                editor.putBoolean("is_video_capturing", false);
                editor.apply();
            }

        }

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
//            boolean i = myDb.insertData(finalUri, file.getName(),
//                    "speech", arrayList.toString(), date,
//                    size_in_mbs + " MB", time, date,
//                    city_name, username,audio_file_path);
//            SaveDataLocallly(username, city_name, date, time, getVideoTime(finalUri.toString()), size_in_mbs + "MB",
//                    finalUri.toString(), file.getName(),arrayList.toString(),audio_file_path);
//            if (i) {
//                arrayList.clear();
//            }
        } else {
            deleteUnUsedFile(finalUri.getPath());
            Toast.makeText(Home.this, "No Points to save.", Toast.LENGTH_SHORT).show();
        }

    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private String GetCurrentLocationName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(Home.this);
        if (geocoder.isPresent()) {
            try {
                geocoder = new Geocoder(Home.this, Locale.getDefault());
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        map = mapboxMap;

        offlineManager = OfflineManager.getInstance(this);
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapboxMap.getStyleUrl(),
                null,
                10,
                20,
                this.getResources().getDisplayMetrics().density);

        locationEnable();
        mapboxMap.getUiSettings().setZoomControlsEnabled(true);
        mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(true);
        mapboxMap.getUiSettings().setAllGesturesEnabled(true);
    }

    private void register_Reciever() {
        if (!receiversRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(background_location_updates.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);
            receiversRegistered = true;
        }
    }

    void locationEnable() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            intialLocationEngine();
            intializLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    void intialLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.requestLocationUpdates();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        originLocation = lastLocation;
        setCamerpostion(originLocation);
        if (originLocation != null) {
            addMarker(new LatLng(originLocation.getLatitude(), originLocation.getLongitude()));
        }
        locationEngine.addLocationEngineListener(this);
    }


    @SuppressLint("WrongConstant")
    void intializLocationLayer() {
        LocationLayerOptions options = LocationLayerOptions.builder(this)
                .minZoom(15.0)
                .build();
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine, options);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
        locationLayerPlugin.setRenderMode(RenderMode.COMPASS);
        locationLayerPlugin.onStart();
    }

    void setCamerpostion(Location location) {
        if (location != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                    .zoom(17) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }

    }

    private void addMarker(LatLng latLng) {
        if (latLng != null) {
            if (currentLocationMarker == null) {
                currentLocationMarker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(latLng + "")
                        .title("Starting point")
                );
            }
        }
    }

    private void addNewMarker(LatLng latLng) {
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .snippet(latLng + "")
                    .title("You are here")
            );
        } else {
            MarkerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Spherical());
        }
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (originLocation != null) {
            addMarker(new LatLng(originLocation.getLatitude(), originLocation.getLongitude()));
        }

        if (location != null) {
            if (isVideoCapturing) {
                if (!isCurrentlocation) {
                    originLocation = location;
                    setCamerpostion(originLocation);
                }

                //Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
                setCamerpostion(location);
                addNewMarker(new LatLng(location.getLatitude(), location.getLongitude()));
            }

        }

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            locationEnable();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        register_Reciever();
        if (locationEngine != null)
            locationEngine.requestLocationUpdates();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        register_Reciever();
        //registerReceiver(myReceiver);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @SuppressLint("MissingPermission")
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

    @Override
    public void onProgress(String progress) {

    }

    @Override
    public void onSuccess(File convertedFile, String type) {

    }

    @Override
    public void onFailure(Exception error) {

    }

    @Override
    public void onNotAvailable(Exception error) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    String fileName;

    private void convertToAudio(File video,String file_path,File folder) {
        fFmpeg = FFmpeg.getInstance(Home.this);
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

                    //  Log.d("FFMpeg:success", message);
                    new AudioToSpeechCoverter().execute(file_path);
                    //convertAudioToSpeech(file_path);
                    progress.dismiss();
                    // muestraOpciones();
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

    private String compressVideo(File video) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) {
            folder.mkdir();
        }
        final long currentTimeMillis = System.currentTimeMillis(); // check current time while taking photo
        final String audio_file_path = currentTimeMillis + ".mp4";
        String file_path = folder + "/" + audio_file_path;

        return file_path;

    }

    private class AudioToSpeechCoverter extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            convertAudioToSpeech(strings[0]);
            // AuthImplicit();
            return null;
        }
    }

    SpeechSettings settings;

    private void convertAudioToSpeech(String file_path) {
        Log.d("HOME", "convertAudioToSpeech: " + file_path);
        InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.credentials);
        try {
            settings =
                    SpeechSettings.newBuilder().setCredentialsProvider(
                            () -> GoogleCredentials.fromStream(stream)
                    ).build();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("convertAudioToSpeech", "convertAudioToSpeech: ", e);
        }
        try {
            SpeechClient speech = SpeechClient.create(settings);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path path = Paths.get(file_path);
                byte[] data = Files.readAllBytes(path);
                ByteString audioBytes = ByteString.copyFrom(data);
                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setSampleRateHertz(16000)
                        .setLanguageCode("en-US")
                        .build();
                RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setContent(audioBytes)
                        .build();

                RecognizeResponse response = speech.recognize(config, audio);
                List<SpeechRecognitionResult> results = response.getResultsList();
                for (SpeechRecognitionResult result : results) {
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    Log.d("HOME", "convertAudioToSpeech: " + alternative.getTranscript());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("convertAudioToSpeech", "convertAudioToSpeech: ", e);
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

    public void CreatExcelSheet() {
        String Fnamexls = "localDb" + ".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        if(directory.exists())
        {
         Log.d("Directory","Exists");
            ReadExcel();//to read data

        }
        else {
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
                WritableSheet sheet = workbook.createSheet("First Sheet", 0);
                Label cell_user_name = new Label(0, 0, "User Name");
                Label cell_city_name = new Label(1, 0, "City Name");
                Label cell_date = new Label(2, 0, "Date");
                Label cell_time = new Label(3, 0, "Time");
                Label cell_duration = new Label(4, 0, "Duration");
                Label cell_size = new Label(5, 0, "Video Size");
                Label cell_uri = new Label(6, 0, "Video Path");
                Label cell_videname = new Label(7, 0, "Video Name");
                Label cell_videcordinated = new Label(7, 0, "Video Cordinates");
                Label cell_audio_file = new Label(8, 0, "Audio Path");
                try {
                    sheet.addCell(cell_user_name);
                    sheet.addCell(cell_city_name);
                    sheet.addCell(cell_date);
                    sheet.addCell(cell_time);
                    sheet.addCell(cell_duration);
                    sheet.addCell(cell_size);
                    sheet.addCell(cell_uri);
                    sheet.addCell(cell_videname);
                    sheet.addCell(cell_videcordinated);
                    sheet.addCell(cell_audio_file);
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
       }

    public void deleteUnUsedFile(String path) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ONDESTROYED","CALLED");
        try {
            stopService(new Intent(Home.this, background_location_updates.class));

        }
        catch (Exception ex)
        {

        }
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
   public void ReadExcel()
   {
       String Fnamexls = "localDb" + ".xls";
       File sdCard = Environment.getExternalStorageDirectory();
       File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
       File file = new File(directory, Fnamexls);
       Workbook w;
       try
       {
          String uri = null;
          String filname;
          String speech;
          String cordniates = null;

          String date = null;
          String size= null;
          String time = null;
          String date1;
          String cityname = null;
          String username= null;
          String audio_file_path=null;
           w = Workbook.getWorkbook(file);
           Sheet sheet = w.getSheet("First Sheet");
           if(sheet.getRows()>1) {
               for (int j = 1; j < sheet.getRows(); j++) {
                   Cell cell = sheet.getCell(0, j);

                   for (int i = 0; i < sheet.getColumns(); i++) {
                       Cell cel = sheet.getCell(i, j);
                       Log.d("ELEMENT_CELL", "IS : " + cel.getContents());
                       if (i == 0) {
                           username = cel.getContents();
                       } else if (i == 1) {
                           cityname = cel.getContents();
                       } else if (i == 2) {
                           date = cel.getContents();
                           date1 = cel.getContents();
                       } else if (i == 3) {
                           time = cel.getContents();
                       } else if (i == 4) {

                       } else if (i == 5) {
                           size = cel.getContents();
                       } else if (i == 6) {
                           uri = cel.getContents();
                       } else if (i == 7) {
                           cordniates = cel.getContents();
                       }
                       else if (i == 8) {
                           audio_file_path = cel.getContents();
                       }
                   }
//                   boolean i = myDb.insertData(Uri.parse(uri), "file_name",
//                           "speech", cordniates, date,
//                           size.replace("MB","") + " MB", time, date,
//                           cityname, username,audio_file_path);
//                   Log.d("ISInserted","IS "+i);
//                   continue;
               }
           }
       }
       catch (Exception ex)
       {
        Log.d("READ_EXCEPTION","IS : "+ex.getMessage());
       }
   }
   public void captureVideoDailogue()
   {
       ToggleButton btnrescordaudio;
       Dialog dialog = new Dialog(Home.this);
       dialog.setContentView(R.layout.layout_capture_dialogue);
       mCamera = Camera.open();
       surfaceView = (SurfaceView) dialog.findViewById(R.id.surface_camera);
       btnrescordaudio=dialog.findViewById(R.id.btnrescordaudio);
       surfaceHolder = surfaceView.getHolder();
       surfaceHolder.addCallback(this);
       surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       try {
           startRecording();
       } catch (IOException e) {
           e.printStackTrace();
       }
       btnrescordaudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked)
               {

               }
               else
               {

               }
           }
       });

   }
    protected void startRecording() throws IOException
    {
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();

        mrec.setCamera(mCamera);

        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile("/sdcard/zzzz.3gp");

        mrec.prepare();
        mrec.start();
    }

}
