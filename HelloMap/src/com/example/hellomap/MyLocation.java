package com.example.hellomap;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
public class MyLocation extends Activity {
    
	// 定位相关
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	AlertDialog.Builder builder=null;
	int defaultSelected=0;
	public static final String SELECED="SELECED";
	boolean priorityGPS=true;
	//定位图层
	LocationsOverlay myLocationOverlay = null;
	//弹出泡泡图层
	private PopupOverlay   pop  = null;//弹出泡泡图层，浏览节点时使用
	private TextView  popupText = null;//泡泡view
	private View viewCache = null;
	MKSearch mSearch = null;
	//地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
	//如果不处理touch事件，则无需继承，直接使用MapView即可
	MyLocationMapView mMapView = null;	// 地图View
	private MapController mMapController = null;
	ItemizedOverlay mOverlay = null;
	//UI相关
	OnCheckedChangeListener radioButtonListener = null;
	Button requestLocButton = null,listButton=null;
	OverlayItem item=null;
    LocationSqliteHelper lh=null;
	LocationClientOption option=null;
	
	CommonSharedPreferences cPS=null;
	double mlatitude;
	double mlongitude;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MapApplication().getInstance().addActiity(this);
        setContentView(R.layout.activity_locationoverlay);
        CharSequence titleLable="您最近一次的位置";
        setTitle(titleLable);
        cPS=new CommonSharedPreferences(this,"MyPreferences");
        lh=new LocationSqliteHelper(MyLocation.this);
        builder=new AlertDialog.Builder(this);
        //---------------------------------------------------
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;   
        final int screenHeight = dm.heightPixels - 10;   
        requestLocButton = (Button)findViewById(R.id.buttonRequest);
        OnClickListener btnClickListener = new OnClickListener() {
        	public void onClick(View v) {
        		    setTitle("定位中.....");
					requestLocClick();
				}
			
		};
		requestLocButton.setOnTouchListener(new OnTouchListener(){
        	int lastX=0,lastY=0;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action=event.getAction();
				
				switch(action){
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();   
				    lastY = (int) event.getRawY();  
					break;
				case MotionEvent.ACTION_MOVE:
					int dx =(int)event.getRawX() - lastX;   
				    int dy =(int)event.getRawY() - lastY;   
				    int left = v.getLeft() + dx;   
				    int top = v.getTop() + dy;   
				    int right = v.getRight() + dx;   
				    int bottom = v.getBottom() + dy;   
				    if(left < 0){   
				      left = 0;   
				      right = left + v.getWidth();   
				    }   
				    if(right > screenWidth){   
				      right = screenWidth;   
				      left = right - v.getWidth();   
				    }   
				    if(top < 0){   
				      top = 0;   
				      bottom = top + v.getHeight();   
				    }   
				    if(bottom > screenHeight){   
				      bottom = screenHeight;   
				      top = bottom - v.getHeight();   
				    }   
				    v.layout(left, top, right, bottom);   
				    Log.i("", "position：" + left +", " + top + ", " + right + ", " + bottom);
				     //将当前的位置再次设置
				    lastX = (int) event.getRawX();   
				    lastY = (int) event.getRawY();   
				    break;   
				  case MotionEvent.ACTION_UP:   //脱离
				    break;    
				}
				return false;
			}
			
        });
	
	    requestLocButton.setOnClickListener(btnClickListener);
        listButton=(Button)findViewById(R.id.buttonList);
        listButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				ArrayList<String> location=lh.getNearestLocation();
				ArrayList<String> times=lh.getNearestTime();
				ArrayList<String> details=lh.getNearestDetail();
				if(location.isEmpty()){
					noLocation();
				}
				else{
					Intent intent = new Intent(MyLocation.this, ListViewHistroy.class);
					intent.putStringArrayListExtra("locations", location);
					intent.putStringArrayListExtra("times", times);
					intent.putStringArrayListExtra("details", details);
					MyLocation.this.startActivity(intent);
				}
				
			}
        	
        });
        MapApplication app=(MapApplication)this.getApplication();
        mSearch = new MKSearch();
        mSearch.init(app.mBMapManager, new MKSearchListener() {
            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
            }
            
			public void onGetAddrResult(MKAddrInfo res, int error) {
			if (error != 0) {
					String str = String.format("错误号：%d", error);
					return;
			}
			//地图移动到该点
			//mMapView.getController().animateTo(res.geoPt);	
			if (res.type == MKAddrInfo.MK_GEOCODE){
					//地理编码：通过地址检索坐标点
					String strInfo = String.format("纬度：%f 经度：%f", res.geoPt.getLatitudeE6()/1e6, res.geoPt.getLongitudeE6()/1e6);
					
			}
			if (res.type == MKAddrInfo.MK_REVERSEGEOCODE){
					//反地理编码：通过坐标点检索详细地址及周边poi
					String strInfo = res.strAddr;
					//locations.add(strInfo);
					SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss     ");  
					Date   curDate   =   new   Date(System.currentTimeMillis());
		    		String   time   =   formatter.format(curDate);  
					lh.save(locData.longitude, locData.latitude,time,strInfo);
					
			}
			}
		
			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			
			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
				
			}

			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
				
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
				
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			
			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				
			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				
			}
        });
        
		//地图初始化
        mMapView = (MyLocationMapView)findViewById(R.id.bmapView2);
        mMapController = mMapView.getController();
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        mMapView.setBuiltInZoomControls(true);
        
        String location=lh.getLatest();
        //System.out.println(location);
        String locations[]=location.split(",");
        double cLon= Double.valueOf(locations[0]);
        double cLat= Double.valueOf(locations[1]);
        GeoPoint p ;
        Bundle extras = getIntent().getExtras(); 
        if(extras!=null){
        	cLon=extras.getDouble("cLon", Double.valueOf(locations[0]));
        	cLat=extras.getDouble("cLat", Double.valueOf(locations[1]));
        }
        p = new GeoPoint((int)(cLat * 1E6), (int)(cLon * 1E6));
        mOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.icon_gcoding),mMapView);	
        item = new OverlayItem(p,"我的位置","");
        item.setMarker(getResources().getDrawable(R.drawable.icon_gcoding));
        mOverlay.addItem(item);
        mMapController.setCenter(p);
        mMapView.getOverlays().add(mOverlay);
        mMapView.refresh();
      
        //定位初始化
        createPaopao();
        mLocClient = new LocationClient( this );
        locData = new LocationData();
        
        mLocClient.registerLocationListener( myListener );
        option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        if(priorityGPS){
        	option.setPriority(LocationClientOption.GpsFirst);
        }
        else{
        	option.setPriority(LocationClientOption.NetWorkFirst);
        }
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);

        //定位图层初始化
		myLocationOverlay = new LocationsOverlay(mMapView);
		//设置定位数据
	    myLocationOverlay.setData(locData);
	    //添加定位图层
	   
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		//修改定位数据后刷新图层生效
		mMapView.refresh();
        
    }
    public void noLocation(){
    	new  AlertDialog.Builder(this) 
        .setTitle("温馨提醒" ) 
        .setMessage("你还没有定位记录，请使用定位" )
  .setPositiveButton("确定" ,  null ) .show();  
    }
    /**
     * 手动触发一次定位请求
     */
    public void requestLocClick(){
    	mLocClient.start();
        mLocClient.requestLocation();
        mOverlay.removeItem(item);
        
        //创建 弹出泡泡图层
        mMapView.refresh();
        Toast.makeText(MyLocation.this, "正在定位……", Toast.LENGTH_SHORT).show();
    }
    
    /**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao(){
		viewCache = getLayoutInflater().inflate(R.layout.custom_text_view, null);
        popupText =(TextView) viewCache.findViewById(R.id.textcache);
        //泡泡点击响应回调
        PopupClickListener popListener = new PopupClickListener(){
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
        };
        pop = new PopupOverlay(mMapView,popListener);
        MyLocationMapView.pop = pop;
	}
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;
            
            mlatitude = locData.latitude = location.getLatitude();
            mlongitude= locData.longitude = location.getLongitude();
            SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss     ");  
    		Date   curDate   =   new   Date(System.currentTimeMillis());
    		String   time   =   formatter.format(curDate);  
            // lh.save(locData.longitude, locData.latitude,time);
            // System.out.println(locData.longitude+"  "+ locData.latitude);
            GeoPoint ptCenter = new GeoPoint((int)(mlatitude*1E6), (int)(mlongitude*1E6));
			//反Geo搜索
			mSearch.reverseGeocode(ptCenter);
            // setTitle("经度 "+locData.latitude+" 纬度 "+locData.longitude);
            locData.accuracy = location.getRadius();
            locData.direction = location.getDerect();
            myLocationOverlay.setData(locData);
            //更新图层数据执行刷新后生效
            mMapView.refresh();
            //是手动触发请求或首次定位时，移动到定位点
            Log.d("LocationOverlay", "receive location, animate to it");
            mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
            mLocClient.stop();
            setTitle("您现在的位置");
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }
    
    //继承MyLocationOverlay重写dispatchTap实现点击处理
  	public class LocationsOverlay extends MyLocationOverlay{

  		public LocationsOverlay(MapView mapView) {
  			super(mapView);
  			// TODO Auto-generated constructor stub
  		}
  		@Override
  		protected boolean dispatchTap() {
  			// TODO Auto-generated method stub
  			//处理点击事件,弹出泡泡
  			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText("我的位置:  \n" +
					"经度  "+locData.latitude+" \n"+
					"纬度  "+locData.longitude);
			pop.showPopup(BMapUtil.getBitmapFromView(popupText),
					new GeoPoint((int)(locData.latitude*1e6), (int)(locData.longitude*1e6)),
					12);
  			return true;
  		}
  		
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
        if (mLocClient != null)
            mLocClient.stop();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
    	switch(item.getItemId())
    	{
    	case R.id.menuAbout:
    		//System.out.println("about");
    		new  AlertDialog.Builder(this) 
            .setTitle("关于HellMap" ) 
            .setMessage("基于百度地图的应用 \n"+"作者:姚梦科  \n 联系方式: dream0708@163.com" )  
      .setPositiveButton("确定" ,  null ) .show();  
    		break;
    	case R.id.menuExit:
    		//System.out.println("exit");
    		new MapApplication().getInstance().exit();
    		break;
    	case R.id.menuPriorities:
    		 builder.setTitle("请选择优先级");
    		 builder.setSingleChoiceItems(new String[]{"GPS优先","网络优先"}, cPS.getValues(SELECED), new DialogInterface.OnClickListener() {
    				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					switch(which){
    					case 0:
    						System.out.println("GPS");
    						cPS.putValues(SELECED, 0);
    						priorityGPS=true;
    						break;
    					case 1:
    						System.out.println("NET WORK");
    						cPS.putValues(SELECED, 1);
    						priorityGPS=false;
    						break;
    					}
    					
    				}
    			
    			});
    		 builder.setPositiveButton("确定", null);
    		 builder.create().show();
    		 break;
    	}
		return super.onOptionsItemSelected(item);
	}
    
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		new AlertDialog.Builder(this).setTitle("确认退出吗？")  
	    
	    .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	        // 点击“确认”后的操作  
	        	new MapApplication().getInstance().exit();
	        }  
	    })  
	    .setNegativeButton("返回", new DialogInterface.OnClickListener() {  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	        // 点击“返回”后的操作,这里不设置没有任何操作  
	        }  
	    }).show();  
	}

}
/**
 * 继承MapView重写onTouchEvent实现泡泡处理操作
 * @author hejin
 *
 */
class MyLocationMapView extends MapView{
	static PopupOverlay   pop  = null;//弹出泡泡图层，点击图标使用
	public MyLocationMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MyLocationMapView(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public MyLocationMapView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	@Override
    public boolean onTouchEvent(MotionEvent event){
		if (!super.onTouchEvent(event)){
			//消隐泡泡
			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}
}

