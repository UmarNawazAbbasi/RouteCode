package com.example.bhati.routeapplication.Activities;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioRecord;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.bhati.routeapplication.Model.Recorder;
import com.example.bhati.routeapplication.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.protobuf.ByteString;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



//import com.example.audiolib.AndroidAudioConverter;
//import com.example.audiolib.callback.IConvertCallback;
//import com.example.audiolib.model.AudioFormat;

public class SavingActivity extends AppCompatActivity implements OnMapReadyCallback /*, LocationEngineListener*/ {

    String videoUri;
    private MapView mapView;
    private MapboxMap map;
    private Location originLocation;
    private Button btnUpload, btnSpeechToText;
    private VideoView videoView;
    private ToggleButton btnPlay;
    private Marker currentLocationMarker;
    private ArrayList<LatLng> list;
    private ArrayList<LatLng> list_overlay_polyline;
    private ArrayList<Recorder> list1;
    private LatLng point1;
    private LatLng seek_point;
    private LatLng point2;
    private static final String TAG = "SavingActivtiy";
    private File videoFile;
    private boolean isVideoIsPlaying;
    private Marker marker_start_point, marker_end_point,intial_marker;
    private long pauseTime;
    private ValueAnimator markerAnimator;
    private boolean isVideoCompleted;
    private String str;
    private String str1;
    private String str_aurdio_file;
    private String[] arrayStr;
    private String[] arrayStr1;

    private FirebaseAuth mAuth;
    int count = 0;
    double speed = 0;
    long time_to_speed = 0;
    Handler handler;
    double distance = 0;
    long d = 0;
    long rem_time = 0;
    private ImageView imgLogout;
    SeekBar seekbar_video;
    int last_seekbarvalue;
    Map<Integer, Integer> mapOfPosts;
    int RECORDER_BPP = 16;
    private static int RECORDER_SAMPLERATE = 8000;
    ProgressDialog progress;
    FFmpeg fFmpeg;
    ProgressBar progress_bar_speechto_text;
    Polyline mpolines;
    Polyline mpolines1;
    double smallestDistance = 50;
    Location closestLocation;
    Icon icon_strt;
    Icon icon_playing;
    Icon icon_pause;
    boolean is_pollyline_tounched=false;
    int previous_second;
    List<List<LatLng>> lists_pollline = new ArrayList<List<LatLng>>();
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_saving);
        list = new ArrayList<>();
        list1 = new ArrayList<>();
        list_overlay_polyline=new ArrayList<>();
        mapOfPosts = new HashMap<Integer, Integer>();
        initialize();
        btnUpload = findViewById(R.id.btnUpload);
        progress_bar_speechto_text = findViewById(R.id.progress_bar_speechto_text);
        btnSpeechToText = findViewById(R.id.btnSpeechToText);
        seekbar_video = findViewById(R.id.seekbar_video);
        imgLogout = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            imgLogout.setVisibility(View.VISIBLE);
        } else {
            imgLogout.setVisibility(View.GONE);
        }
        btnUpload.setOnClickListener(v -> {

            if (mAuth.getCurrentUser() != null) {
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(SavingActivity.this, imgLogout);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.signout, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        mAuth.signOut();
                        Toast.makeText(SavingActivity.this, "Sign out successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SavingActivity.this, Home.class));
                        finish();
                        return true;
                    }
                });

                popup.show();//showing popup menu

            }
        });
//        Bundle bundle = getIntent().getExtras().getBundle("bundle_values");
        Bundle bundle = getIntent().getBundleExtra("bundle_values");
        videoUri = bundle.getString("uri");
        str = bundle.getString("listLatLng");
        str1 = bundle.getString("listOthers");
        Log.d("RECEIVED_STRING", "IS:" + str1);
        str_aurdio_file = bundle.getString("AUDIOFILE");
        //Log.d(TAG, "onCreate: original = "+str);
        str = str.replace("[", "");
        str = str.replace("]", "");
        str = str.replace("[latlng", "");
        str = str.replace(", altitude=0.0", "");
        str = str.replace("LatLng", "");
        str = str.replace("latitude=", "");
        str = str.replace("longitude=", "");
        arrayStr = str.split(",");

        double[] lat = new double[arrayStr.length + 1];
        double[] lng = new double[arrayStr.length + 1];
        for (int i = 0; i < arrayStr.length; i++) {
            if (i % 2 == 0) {
                lat[i] = Double.parseDouble(arrayStr[i]);
            } else {
                lng[i] = Double.parseDouble(arrayStr[i]);
            }

            Log.d(TAG, "onCreate:lat  =  " + i + " = " + lat[i] + " j = " + i + " lng =  " + lng[i]);

        }

        for (int i = 0; i < arrayStr.length; i++) {
            if (lat[i] != 0 && lng[i + 1] != 0) {
                Log.d(TAG, "onCreate: lat = " + i + " " + lat[i] + " lng " + lng[i + 1]);
                list.add(new LatLng(lat[i], lng[i + 1]));
            }
        }
        populateRecorder(str1);
        btnPlay = findViewById(R.id.btnPlay);
        videoView = findViewById(R.id.videoView);
        seekbar_video.setVisibility(View.GONE);
        videoView.setVideoURI(Uri.parse(videoUri));
        //Audio_Converter();

        btnPlay.setOnCheckedChangeListener((buttonView, isChecked) -> {

            mapSecodsWithCordiates(list.size(), videoView.getDuration());
            seekbar_video.setVisibility(View.VISIBLE);
            Log.d("TOTAL", "DATAPOINTS" + list.size());
            if (isChecked) {
                mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 100);
                videoView.start();
                isVideoIsPlaying = true;
                isVideoCompleted = false;
                if (list != null)
                    marker_anim(getVideoTime());
            } else {
                videoView.pause();
                pauseTime = videoView.getCurrentPosition();
                Log.d(TAG, "onCreate: " + videoView.getCurrentPosition());
                isVideoIsPlaying = false;
                isVideoCompleted = false;
                // count = count - 1;
                // time_to_speed = 2 * time_to_speed;

                if (list != null)
                    marker_anim(getVideoTime());
                // marker_anim(pauseTime);
            }
        });

        videoView.setOnCompletionListener(mp -> {
            isVideoCompleted = true;
            btnPlay.setChecked(false);
            count = 0;

            //marker_anim(getVideoTime());
            //marker_start_point.setPosition(point1);
        });
        //videoFile = new File(videoUri);
        Mapbox.getInstance(this, "pk.eyJ1IjoiZGVlcHNoaWtoYTc3NyIsImEiOiJjamk2cno3dmEwNDBxM3JwcDFlb2ZtNTMzIn0.jVGIfJplTqKXFg6SROl_9g");
        // setContentView(R.layout.activity_home);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        if (list != null) {
            int n = list.size() - 1;
            point1 = list.get(0);
            point2 = list.get(n);

        }
        seekbar_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int mProgressAtStartTracking;
            private final int SENSITIVITY = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // handle progress change
                Log.d("SEEKBAR", "CHANGING" + seekBar.getProgress());
//                last_seekbarvalue=seekBar.getProgress();
//                if(mapOfPosts.containsKey(seekBar.getProgress()))
//                {
//        Log.d("ALREADY","found"+seekBar.getProgress()+"COUNT"+mapOfPosts.get(seekBar.getProgress()));
//                }
//                else
//                {
//                    mapOfPosts.put(seekBar.getProgress(), count);
//                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mProgressAtStartTracking = seekBar.getProgress();
                Log.d("SEEKBAR", "START_TRACKING");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("SEEKBAR", "STOP_TRACKING");
                if (mapOfPosts.size() > 0) {
                    videoView.seekTo(seekBar.getProgress());
                    videoView.pause();
                    isVideoIsPlaying = false;
                    isVideoCompleted = false;
                    btnPlay.setChecked(false);
                    Log.d("PROGRESS", "AT :" + seekBar.getProgress());
                    if (list != null) {
                        marker_anim(getVideoTime());
                    }
                    UpdateMarker(seekBar.getProgress());
                } else {
                    Toast.makeText(getApplicationContext(), "first play video", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TestCordiate();
        btnSpeechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        Toast.makeText(getApplicationContext(),"Purchase Api",Toast.LENGTH_SHORT).show();
                normalizeFile();
            }
        });
        Readlatlng();
        showAccuracyDialogue();

        IconFactory iconFactory = IconFactory.getInstance(SavingActivity.this);
         icon_strt = iconFactory.fromResource(R.drawable.marker_red);
         icon_playing = iconFactory.fromResource(R.drawable.marker_moveable);
        icon_pause = iconFactory.fromResource(R.drawable.marker_red);
    }

    private long getVideoTime() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.parse(videoUri));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        Log.d(TAG, "getVideoTime: " + timeInMillisec);
        retriever.release();
        return timeInMillisec;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(SavingActivity.this, Home.class));
        finish();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

        if (list != null)
            enableLocation();
        // map.setOnMyLocationChangeListener(this);
        intialMarkerClick();
    }

    private void enableLocation() {
        initializeLocationEngine();
        //initializeLocationLayer();
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {

        addMarker(point1);
        //start marker
        intialMarker(point1);
        addMarkerEndPoint(point2);
        Location point1_location = new Location("Start");
        point1_location.setLatitude(point1.getLatitude());
        point1_location.setLongitude(point1.getLongitude());
        setCameraPosition(point1_location);

        draw_ployline(list);
        AddNewPollyLine();
    }

    private void setCameraPosition(Location location) {

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                .zoom(17) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position));

    }

    private void addMarker(LatLng latLng) {
        IconFactory iconFactory = IconFactory.getInstance(SavingActivity.this);
        //  Drawable iconDrawable = ContextCompat.getDrawable(SavingActivity.this, R.drawable.marker_red);
        Icon icon = null;
        if(is_pollyline_tounched) {
             icon = iconFactory.fromResource(R.drawable.marker_moveable);
        }
        else
        {
            icon = iconFactory.fromResource(R.drawable.marker_blue);
        }
        marker_start_point = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(icon)
                .snippet(latLng + "")
                .title("Start point"));
    }

    private void addMarkerEndPoint(LatLng latLng) {
        IconFactory iconFactory = IconFactory.getInstance(SavingActivity.this);
        //  Drawable iconDrawable = ContextCompat.getDrawable(SavingActivity.this, R.drawable.marker_red);
        Icon icon = iconFactory.fromResource(R.drawable.marker_red);

        marker_end_point = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(icon)
                .snippet(latLng + "")
                .title("End point"));
    }

    private void draw_ployline(List<LatLng> latLngList) {
        mpolines = map.addPolyline(new PolylineOptions()
                .width(20f)
                .color(Color.GREEN)
                .alpha(1f)
                .addAll(latLngList));
    }

    @SuppressLint("NewApi")
    private void marker_anim(long time) {
        Log.d("GET_SPEED", "time: " + time);

        if (isVideoCompleted) {
            marker_start_point.setPosition(point1);

//            marker_start_point.setIcon(icon_strt);
        }
        if (isVideoIsPlaying) {
            //double distance = marker_start_point.getPosition().distanceTo(list.get(count));
            marker_start_point.setIcon(icon_playing);
            Log.d("GET_SPEED", "run: " + time / (list.size() - 1));
            time_to_speed = time / (list.size());
            //time_to_speed = rem_time + time_to_speed;
            handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if ((list.size()) > count) {
                        Log.d(TAG, "run: is running");
                        markerAnimator = ObjectAnimator.ofObject(marker_start_point, "position",
                                new LatLngEvaluator(), marker_start_point.getPosition(), list.get(count));
                        markerAnimator.setDuration(time_to_speed);
                        markerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        markerAnimator.start();

                        d = d + time_to_speed;
                        Log.e(TAG, "run: d = :" + d + " count: " + count);
                        count++;
                        handler.postDelayed(this, time_to_speed);
                        map.animateCamera(CameraUpdateFactory.newLatLng(marker_start_point.getPosition()));
                    } else {
                        Log.d(TAG, "run: stopped");
                    }
                }
            };
            handler.post(runnable);
        } else {
            Toast.makeText(this, "Video is Paused = " + count, Toast.LENGTH_SHORT).show();
            // Toast.makeText(this, "List size = "+list.size(), Toast.LENGTH_SHORT).show();
            if ((list.size() > 1)) {
                ///count = count - 1;
                markerAnimator.pause();
                handler.removeCallbacksAndMessages(null);

                    marker_start_point.setIcon(icon_pause);

                // rem_time = rem_time + (time_to_speed + rem_time);
            }
        }


    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    public Handler mSeekbarUpdateHandler = new Handler();
    public Runnable mUpdateSeekbar = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            int totatmili = 0, rem = 0;
            seekbar_video.setMax(videoView.getDuration());
            seekbar_video.setProgress(videoView.getCurrentPosition());
            //  sharedclass.lastplayedduration=videoView.getCurrentPosition();
            totatmili = videoView.getDuration();
            rem = totatmili - videoView.getCurrentPosition();
            //  }
            long minutes = TimeUnit.MILLISECONDS.toMinutes(rem);
            int minutmili = (((int) minutes) * 60000);
            int durat = rem - minutmili;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durat);
            if (seconds < 10) {
//                txtremainingduration.setText((String.valueOf( minutes+":0"+seconds)));
            } else {
//                txtremainingduration.setText((String.valueOf(minutes + ":" + seconds)));
            }
            mSeekbarUpdateHandler.postDelayed(this, 50);
        }

    };

    public void UpdateMarker(int n_progressbar) {
        Log.d("CURRENT", "POINT : " + count);
        Log.d("TOTAL_VIDEO", "DURATION : " + videoView.getDuration());
        Log.d("SEEKTO", "DURATION : " + n_progressbar);
        int remaining_time = videoView.getDuration() - n_progressbar;
        Log.d("REMAINING_TIME", "IS : " + remaining_time);
        Log.d("HashMap", "Size : " + mapOfPosts.size());
        int speed = 1000;
//     marker_anim(remaining_time);
        try {
            if (mapOfPosts.size() > 0) {

                Log.d("SEEKER_SECOND", "IS :" + n_progressbar / 1000);
                int found_value = mapOfPosts.get(n_progressbar / 1000);
                Log.d("Found", "Value" + found_value);
                markerAnimator = ObjectAnimator.ofObject(marker_start_point, "position",
                        new LatLngEvaluator(), list.get(count), list.get(found_value));
                markerAnimator.setDuration(time_to_speed);
                markerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                markerAnimator.start();
                count = found_value;
            } else {

            }
        }
        catch (Exception ex)
        {
    Log.d("Exception_marker","is:"+ex.getMessage());
        }

       /* int vt = videoView.getDuration();
        double point_min = vt / list.size();
        if (n_progressbar >= point_min)*/
    }

    public void mapSecodsWithCordiates(int coordinates, int dutration) {
        Log.d("TIME", "IS :" + dutration);
        Log.d("COORDIATES", "IS :" + coordinates);
        double factor = (double) coordinates / (dutration / 1000);
        Log.d("FACTOR", "IS :" + factor);
        for (int i = 0; i < dutration / 1000; i++) {
            Log.d("KEY_PAIR", "IS : " + Math.round(i * factor));
            mapOfPosts.put(i, i);
        }
    }

    public void TestCordiate() {
        for (int i = 0; i < list.size(); i++) {
            Log.d("MAIN_CORDIATES", "ARE : " + list.get(i) + "index" + i);
        }
    }

    public void normalizeFile() {

        //While data come from microphone.

        Log.d("RECIVED_AUDIO", "IS :" + str_aurdio_file);
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/RouteApp");
        File file = new File(directory, str_aurdio_file);
        File folder = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder.exists()) {
            folder.mkdir();
        }
        final long currentTimeMillis = System.currentTimeMillis();
        final long currentTimeMillis1 = System.currentTimeMillis();
        File folder1 = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder1.exists()) {
            folder1.mkdir();
        }
        final String audio_file_path = currentTimeMillis + ".mp3";
        final String normalize_file_path = currentTimeMillis1 + ".mp3";
        File outfile = new File(folder, audio_file_path);
        File normailzefile = new File(folder, normalize_file_path);
        String nomralize_command = "-y -i " + file + " -af apad,atrim=0:3,loudnorm=I=-16:TP=-1.5:LRA=11:measured_I=-23.54:measured_TP=-7.96:measured_LRA=0.00:measured_thresh=-34.17:offset=7.09:linear=true:print_format=summary -ar 16k " + normailzefile;
        String[] cmd = nomralize_command.split(" ");
        progress_bar_speechto_text.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            public void run() {

                getText(file.getAbsolutePath());
            }
        }).start();

        new CountDownTimer(10000, 1000) {
            public void onFinish() {
                // When timer is finished
                // Execute your code here
                progress_bar_speechto_text.setVisibility(View.GONE);
            }

            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();
//        speechToText(file,outfile);
//        try {
//            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
//                @Override
//                public void onStart() {
//                    super.onStart();
//                    //  Log.d("FFMpeg", "onStart: ");
//                    progress.setMessage("Nomalizing please wait...");
//                    progress.show();
//
//                }
//
//                @Override
//                public void onProgress(String message) {
////                    progress.setMessage(message);
//                    //Log.d("FFMpeg", message);
//                    Log.d("Nomalizing_OnProgress","IS :"+message);
//                }
//
//                @Override
//                public void onFailure(String message) {
//                    // Log.d("FFMpeg",message);
//                    Log.d("Nomalizing__onFailure","IS :"+message);
//                    progress.dismiss();
//                }
//
//                @Override
//                public void onSuccess(String message) {
//                    Log.d("Nomalizing_Success","IS :"+message);
//                    speechToText(normailzefile,outfile);
//
//                }
//
//                @Override
//                public void onFinish() {
//
//                    }
//            });
//        } catch (FFmpegCommandAlreadyRunningException e) {
//            // Log.e("FFMpeg", "convertToAudio: " , e);
//            e.printStackTrace();
//        }

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

    public void createSpeechToText(String body) {

        final long currentTimeMillis = System.currentTimeMillis();
        File folder1 = new File(Environment.getExternalStorageDirectory() + "/RouteApp");
        if (!folder1.exists()) {
            folder1.mkdir();
        }
        File gpxfile = new File(folder1, currentTimeMillis + ".txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(gpxfile);

            writer.append(body);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            Log.d("Writing", "Exception" + ex.getMessage());
        }
    }

    public void getText(String file_path) {

        try {
            InputStream stream = getResources().openRawResource(R.raw.credentials);
            SpeechSettings settings =
                    SpeechSettings.newBuilder().setCredentialsProvider(
                            new CredentialsProvider() {
                                @Override
                                public Credentials getCredentials() throws IOException {
                                    return GoogleCredentials.fromStream(stream);
                                }
                            }
                    ).build();
            SpeechClient speech = com.google.cloud.speech.v1p1beta1.SpeechClient.create(settings);
            // The path to the audio file to transcribe
            String fileName = file_path;
            Log.d("FILE_Name", "IS :" + fileName);
            // Reads the audio file into memory
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US")
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                Log.d("Transcription:", alternative.getTranscript());
                createSpeechToText(String.valueOf(alternative));
            }
            speech.close();
        } catch (Exception ex) {

            Log.d("TRANSLATING", "EXCEPTION :" + ex.getLocalizedMessage());

        }
        //For more, refer this link
    }

    public void intializeSox() {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
//                    Sox con=new Sox("");


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }


        }.execute();
    }

    public void intialMarkerClick() {

        map.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                Log.d("Clicked", "latitude" + point.getLatitude());
                Log.d("Clicked", "longitude" + point.getLongitude());
                Location closetlocation = new Location("closet");
                LatLng latLng = null;
                int position = 0;
                float[] results = new float[1];
                for (int i = 0; i < list.size(); i++) {

                    Location startPoint = new Location("locationA");
                    startPoint.setLatitude(point.getLatitude());
                    startPoint.setLongitude(point.getLongitude());

                    Location endPoint = new Location("locationA");
                    endPoint.setLatitude(list.get(i).getLatitude());
                    endPoint.setLongitude(list.get(i).getLongitude());
                    double distance = startPoint.distanceTo(endPoint);
                    Log.d("DISTANCE", "IS" + distance);
                    if(smallestDistance == 50 || distance < smallestDistance){
                        closetlocation.setLatitude(list.get(i).getLatitude());
                        closetlocation.setLongitude(list.get(i).getLongitude());
                        closestLocation = closetlocation;
                        smallestDistance = distance;
                        position=i;
                        latLng=new LatLng(list.get(i).getLatitude(),list.get(i).getLongitude());
                    }
                }
//
                if(latLng!=null) {
                    addMarkerNew(smallestDistance, latLng, position);
                }
                else
                {
                Toast.makeText(getApplicationContext(),"Please click on path",Toast.LENGTH_SHORT).show();
                }

            }
        });
//    mapView.getMap().setOnMapClickListener(new MapboxMap.OnMapClickListener() {
//
//        @Override
//        public void onMapClick(LatLng latlng) {
//            // TODO Auto-generated method stub
//
//            if (marker_start_point != null) {
//                marker_start_point.remove();
//            }
//            Marker marker = mMap.addMarker(new MarkerOptions()
//                    .position(latlng)
//                    .icon(BitmapDescriptorFactory
//                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//            System.out.println(latlng);
//
//        }
//    });
    }

    public void Readlatlng() {
        for (int i = 0; i < list.size(); i++) {
            Log.d("LATITUDE", "IS :" + list.get(i).getLatitude());
            Log.d("Longitude", "IS :" + list.get(i).getLongitude());
        }
    }

    public void showAccuracyDialogue() {
        new AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setTitle("Alert")
                .setMessage("For Better Accuracy please Zoom map then move marker")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    public void populateRecorder(String recorder_Str) {
        try
        {
            JSONObject jsnobject = new JSONObject(recorder_Str);
            JSONArray jsonArray = jsnobject.getJSONArray("Data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);
                if(c.has("LATITUDE"))
                {
                    Log.d("Latitude","IS"+c.getString("LATITUDE"));
                    Log.d("Longitude","IS"+c.getString("LONGITUDE"));
                    Log.d("Time","IS"+c.getString("TIME"));
                    Recorder recorder = new Recorder(c.getString("LATITUDE"),
                            c.getString("LONGITUDE"),
                            c.getString("TIME"));
                    list1.add(recorder);

                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
   public void addMarkerNew(double distance,LatLng point,int i)
   {
    if (distance < 50) {
        // If distance is less than 100 meters, this is your polyline
        marker_start_point.remove();
        is_pollyline_tounched=true;
        addMarker(point);
        for (int j=0;j<list1.size();j++)
        {
            Log.d("Compare_lat","is:"+list1.get(j).getLat());
            Log.d("Compare_lat1","is:"+list.get(i).getLatitude());
            if(list1.get(j).getLat().trim().equals(String.valueOf(list.get(i).getLatitude()).trim()))
            {
                String time=list1.get(j).getTime().replaceAll(" ","");

                Log.d("Time","IS"+time);
                String[] separated = time.split(":");
                int hours=Integer.parseInt(separated[0])*3600000;
                int minutes=Integer.parseInt(separated[1])*60000;
                int seconds=Integer.parseInt(separated[2])*1000;
                int total_time=hours+minutes+seconds;
                Log.d("MIlliseconds","are"+total_time);
                videoView.seekTo(total_time);
                Log.d("MARKER_PROGRESS","IS :"+seekbar_video.getProgress());
                count=i;
                Log.d("PREVIOS_SECONDS","IS:"+previous_second);
                Log.d("CURRENT_SECONDS","IS:"+seconds);

        if(previous_second+1==seconds)
        {
            list_overlay_polyline.add(new LatLng(Double.parseDouble(list1.get(j).getLat()),Double.parseDouble(list1.get(j).getLat())));

        }
        else
        {
            if(list_overlay_polyline.size()>0)
            {
                //addOverLayPlouline(list_overlay_polyline);

            }
        }
                previous_second++;
                //
                return;
            }

        }

    }
   }
   public void intialMarker(LatLng latLng)
   {
       IconFactory iconFactory = IconFactory.getInstance(SavingActivity.this);
       //  Drawable iconDrawable = ContextCompat.getDrawable(SavingActivity.this, R.drawable.marker_red);
       Icon icon = null;
       icon = iconFactory.fromResource(R.drawable.marker_blue);
       intial_marker = map.addMarker(new MarkerOptions()
               .position(latLng)
               .icon(icon)
               .snippet(latLng + "")
               .title("Start point"));
   }
   public void addOverLayPlouline(List<LatLng> latLngList)
   {
       PolylineOptions lineOptions = new PolylineOptions();
      map.addPolyline(lineOptions
               .width(10f)
               .color(Color.RED)
               .alpha(1f)
               .addAll(latLngList));
      Log.d("MapPoly","added");
       //list_overlay_polyline.clear();

   }
   public void AddNewPollyLine()
   {
       List<LatLng> list = new ArrayList<>();
       List<LatLng> listnew = new ArrayList<>();
       List<LatLng> listnew1 = new ArrayList<>();
       List<LatLng> listnew2 = new ArrayList<>();
       for (int j=0;j<list1.size();j++)
       {
               String time=list1.get(j).getTime().replaceAll(" ","");
               Log.d("Time","IS"+time);
               String[] separated = time.split(":");
               int hours=Integer.parseInt(separated[0]);
               int minutes=Integer.parseInt(separated[1]);
               int seconds=Integer.parseInt(separated[2]);
               int total_time=hours+minutes+seconds;
               Log.d("PREVIOS_SECONDS","IS:"+previous_second);
             //  Log.d("CURRENT_SECONDS","IS:"+seconds);

               if(seconds-1==previous_second)
               {
                   if(j>=10 & j<=30)
                   {
                       list.add(new LatLng(Double.parseDouble(list1.get(j).getLat()), Double.parseDouble(list1.get(j).getLng())));
                       lists_pollline.add(list);
                   }
                   else if (j>=40 & j<=60)
                    {
                        listnew.add(new LatLng(Double.parseDouble(list1.get(j).getLat()), Double.parseDouble(list1.get(j).getLng())));
                        lists_pollline.add(listnew);
                   }
                   else if (j>=70 & j<=80)
                   {
                       listnew1.add(new LatLng(Double.parseDouble(list1.get(j).getLat()), Double.parseDouble(list1.get(j).getLng())));
                       lists_pollline.add(listnew1);
                   }
                   else
                   {
//                       listnew2.add(new LatLng(Double.parseDouble(list1.get(j).getLat()), Double.parseDouble(list1.get(j).getLng())));
//                       lists_pollline.add(listnew2);
                   }

               }
               else
               {

               }
//           if(j<20||j>80)
//           {

               //list_overlay_polyline.add(new LatLng(Double.parseDouble(list1.get(j).getLat()), Double.parseDouble(list1.get(j).getLng())));
//           }
//               }
//               else
//               {
//                   if(list_overlay_polyline.size()>0)
//                   {


previous_second=seconds;

               //
               //return;
           }
           Log.d("Pollline_list","is"+lists_pollline.size());
           for (int k=0;k<lists_pollline.size();k++)
           {
               Log.d("ListLatitude","size"+k);
               Log.d("ListLatitude","size"+lists_pollline.get(k));
               addOverLayPlouline(lists_pollline.get(k));
           }


       }

}
