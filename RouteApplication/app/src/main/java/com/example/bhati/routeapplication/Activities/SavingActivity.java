package com.example.bhati.routeapplication.Activities;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.bhati.routeapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.File;
import java.util.ArrayList;
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
    private Button btnUpload;
    private VideoView videoView;
    private ToggleButton btnPlay;
    private Marker currentLocationMarker;
    private ArrayList<LatLng> list;
    private LatLng point1;
    private LatLng seek_point;
    private LatLng point2;
    private static final String TAG = "SavingActivtiy";
    private File videoFile;
    private boolean isVideoIsPlaying;
    private Marker marker_start_point, marker_end_point;
    private long pauseTime;
    private ValueAnimator markerAnimator;
    private boolean isVideoCompleted;
    private String str;
    private String[] arrayStr;

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

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_saving);
        list = new ArrayList<>();
        mapOfPosts = new HashMap<Integer, Integer>();

        btnUpload = findViewById(R.id.btnUpload);
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
        ;
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
                if (mapOfPosts.size() > 0)
                {
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
                }
                else
                {
                Toast.makeText(getApplicationContext(),"first play video",Toast.LENGTH_SHORT).show();
                }
            }
        });

        TestCordiate();
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

    }

    private void enableLocation() {
        initializeLocationEngine();
        //initializeLocationLayer();
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {

        addMarker(point1);
        addMarkerEndPoint(point2);
        Location point1_location = new Location("Start");
        point1_location.setLatitude(point1.getLatitude());
        point1_location.setLongitude(point1.getLongitude());
        setCameraPosition(point1_location);

        draw_ployline(list);
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
        Icon icon = iconFactory.fromResource(R.drawable.marker_blue);
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
        map.addPolyline(new PolylineOptions()
                .width(10f)
                .color(Color.GREEN)
                .alpha(1f)
                .addAll(latLngList));
    }

    @SuppressLint("NewApi")
    private void marker_anim(long time) {
        Log.d("GET_SPEED", "time: " + time);
        if (isVideoCompleted) {
            marker_start_point.setPosition(point1);
        }
        if (isVideoIsPlaying) {
            //double distance = marker_start_point.getPosition().distanceTo(list.get(count));
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
}
