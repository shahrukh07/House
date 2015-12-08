package jc.house.activities;

import android.os.Bundle;
import android.view.Window;

import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import jc.house.R;
import jc.house.utils.ToastUtils;
import jc.house.views.TitleBar;

public class MapActivity extends com.tencent.tencentmap.mapsdk.map.MapActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);
        TitleBar titleBar = (TitleBar)this.findViewById(R.id.titlebar);
        titleBar.setTitle("地图详情");
        this.mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        this.initMapView();
    }

    private void initMapView() {
        this.mapView.getMap().setZoom(16);
        Marker marker = mapView.getMap().addMarker(new MarkerOptions().position(new LatLng(39.899201, 116.424866)).title("大北京").anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon)));
        marker.setDraggable(true);
        marker.showInfoWindow();
        Marker marker2 = mapView.getMap().addMarker(new MarkerOptions().position(new LatLng(39.909201, 116.436866)).title("大北京").anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon)));
        marker2.setDraggable(true);
        marker2.showInfoWindow();
        this.mapView.getMap().setOnInfoWindowClickListener(new TencentMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ToastUtils.show(MapActivity.this, "It's mapView info window");
            }
        });
        this.mapView.getMap().setCenter(new LatLng(39.90, 116.425));
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
