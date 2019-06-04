package com.example.bhati.routeapplication.Testing;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Projection;

public class DraggableMarker {

//    private static final String TAG = "map.DraggableMarker";
//
//    private boolean mIsDragged;
//    private static final RectF mTempRect = new RectF();
//    private static final PointF mTempPoint = new PointF();
//    private float mDx, mDy;
//
//    public DraggableMarker(String title, String description, LatLng latLng) {
//        super(title, description, latLng);
//        mIsDragged = false;
//    }
//
//    public DraggableMarker(MapView mv, String aTitle, String aDescription, LatLng aLatLng)
//    {
//        super(mv, aTitle, aDescription, aLatLng);
//        mIsDragged = false;
//    }
//
//    public boolean drag(View v, MotionEvent event) {
//        final int action = event.getActionMasked();
//        if(action == MotionEvent.ACTION_DOWN) {
//            Projection pj = ((MapView)v).getProjection();
//            RectF bound = getDrawingBounds(pj, mTempRect);
//            if(bound.contains(event.getX(), event.getY())) {
//                mIsDragged = true;
//                PointF p = getPositionOnScreen(pj, mTempPoint);
//                mDx = p.x - event.getX();
//                mDy = p.y - event.getY();
//            }
//        }
//        if(mIsDragged) {
//            if((action == MotionEvent.ACTION_CANCEL) ||
//                    (action == MotionEvent.ACTION_UP)) {
//                mIsDragged = false;
//            } else {
//                Projection pj = ((MapView)v).getProjection();
//                ILatLng pos = pj.fromPixels(event.getX() + mDx, event.getY() + mDy);
//                setPoint(new LatLng(pos.getLatitude(), pos.getLongitude()));
//            }
//        }
//
//        return mIsDragged;
//    }

}
