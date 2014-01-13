package com.example.hellomap;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class HisListActivity extends Activity {
	int[] drawAble=new int[]{R.drawable.icon_marka,R.drawable.icon_markb,R.drawable.icon_markc,R.drawable.icon_markd
	    	,R.drawable.icon_marke,R.drawable.icon_markf,R.drawable.icon_markg,R.drawable.icon_markh,R.drawable.icon_marki,R.drawable.icon_markj};
	private MapView mMapView=null;
	private MapController mMapController = null;
	MyOverlay mOverlay = null;
	PopupOverlay pop=null;
	OverlayItem item=null;
	Button button=null;
	double cLon[]=new double[10];
	double cLat[]=new double[10];
	String locTime[]=new String[10];
	LocationSqliteHelper lh=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    new MapApplication().getInstance().addActiity(this);
	    lh = new LocationSqliteHelper(HisListActivity.this);
	    setTitle("您定位过的历史记录");
	    button = new Button(this);
        button.setBackgroundResource(R.drawable.popup);
		Bundle extras = getIntent().getExtras(); 
		ArrayList<String> list=lh.getNearestLocation();
		ArrayList<String> timeList=lh.getNearestTime();
        setContentView(R.layout.activity_history);
        mMapView = (MapView)findViewById(R.id.bmapViewHis);
        mMapController = mMapView.getController();
        mMapController.enableClick(true);
        mMapController.setZoom(15);
        mMapView.setBuiltInZoomControls(true);
	    if(list.isEmpty()){
			
        }
        else
	    {
		   for(int i=0;i<list.size()&&i<10;i++){
			  String locations[]=list.get(i).split(",");
			  cLon[i]=Double.valueOf(locations[0]);
			  cLat[i] = Double.valueOf(locations[1]);
			  locTime[i]=timeList.get(i);
		  }
	    }
	    mOverlay = new MyOverlay(getResources().getDrawable(drawAble[0]),mMapView);
	    GeoPoint p[]=new GeoPoint[list.size()] ;
	    for(int i=0;i<list.size()&&i<10;i++){
	    	p[i] = new GeoPoint((int)(cLat[i]* 1E6), (int)(cLon[i]* 1E6));
	        item = new OverlayItem(p[i],"位置","");
	        item.setMarker(getResources().getDrawable(drawAble[i]));
	        mOverlay.addItem(item);
	    }
	   
	    mMapView.getOverlays().add(mOverlay);
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay(mMapView);
        mMapView.getOverlays().add(graphicsOverlay);
    	//添加折线
        graphicsOverlay.setData(drawLine(p));
        mMapController.setCenter(p[0]);
        //执行地图刷新使生效
        mMapView.refresh();
        pop=new PopupOverlay(mMapView,null);
	}
	public  Graphic drawLine(GeoPoint p[]){
		
	    //构建线
  		Geometry lineGeometry = new Geometry();
  		lineGeometry.setPolyLine(p);
  		//设定样式
  		Symbol lineSymbol = new Symbol();
  		Symbol.Color lineColor = lineSymbol.new Color();
  		lineColor.red = 0;
  		lineColor.green = 0;
  		lineColor.blue = 255;
  		lineColor.alpha = 255;//透明度
  		lineSymbol.setLineSymbol(lineColor, 5);
  		//生成Graphic对象
  		Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
  		return lineGraphic;
	}
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.his_menu, menu);
	        return true;
	    }
	    
	    @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
	    	switch(item.getItemId()){
	    	case R.id.menuAbout:
	    		//System.out.println("about");
	    		new  AlertDialog.Builder(this) 
	            .setTitle("关于HelloMap" ) 
	            .setMessage("基于百度地图的应用 \n"+"作者:姚梦科  \n 联系方式: dream0708@163.com" )  
	      .setPositiveButton("确定" ,  null ) .show();  
	    		break;
	    	case R.id.menuExit:
	    		//System.out.println("exit");
	    		new MapApplication().getInstance().exit();
	    		break;
	    	default:break;
	    	}
			return super.onOptionsItemSelected(item);
		}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		mOverlay.removeAll();
		mMapView.refresh();
		super.onBackPressed();
	}
	 @Override
	    protected void onPause() {
	        mMapView.onPause();
	        super.onPause();
	    }
	    
	    @Override
	    protected void onResume() {
	        mMapView.onResume();
	        super.onResume();
	    }
	    
	    @Override
	    protected void onDestroy() {
	    	//退出时销毁定位
	        
	        mMapView.destroy();
	        super.onDestroy();
	    }
	    
	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	    	super.onSaveInstanceState(outState);
	    	mMapView.onSaveInstanceState(outState);
	    	
	    }
	    
	    @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    	super.onRestoreInstanceState(savedInstanceState);
	    	mMapView.onRestoreInstanceState(savedInstanceState);
	    }
	    public class MyOverlay extends ItemizedOverlay{

			public MyOverlay(Drawable defaultMarker, MapView mapView) {
				super(defaultMarker, mapView);
			}
			@Override
			public boolean onTap(int index){
				//OverlayItem item = getItem(index);
				button.setText("您的定位时间是: \n"+locTime[index]+"\n 经度: "+cLon[index]+" 纬度: "+cLat[index]);
				GeoPoint pt = new GeoPoint((int)(cLat[index]* 1E6), (int)(cLon[index]* 1E6));
					// 弹出自定义View
				pop.showPopup(button, pt, 32);
				return true;
			}
			
			@Override
			public boolean onTap(GeoPoint pt , MapView mMapView){
				if (pop != null){
	                pop.hidePop();
	                mMapView.removeView(button);
				}
				return false;
			}
	    	
	    }
	
}
