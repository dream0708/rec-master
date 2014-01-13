package com.example.hellomap;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListViewHistroy extends Activity {
	ListView listView=null;
	ArrayList<String> list=null;
	ArrayList<String> timeList=null;
    ArrayList<String> locations=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("定位历史");
		new MapApplication().getInstance().addActiity(this);
		MapApplication app=(MapApplication)this.getApplication();
		Bundle extras = getIntent().getExtras(); 
		list=extras.getStringArrayList("locations");
		timeList=extras.getStringArrayList("times");
		locations=extras.getStringArrayList("details");
		setContentView(R.layout.mylistview);
		listView=(ListView)findViewById(R.id.listView);
		listView.setAdapter(new BaseAdapter(){

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return locations.size();
			}

			@Override
			public Object getItem(int id) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int id) {
				// TODO Auto-generated method stub
				return id;
			}

			@Override
			public View getView(int id, View view, ViewGroup vGroup) {
				// TODO Auto-generated method stub
				view = View.inflate(ListViewHistroy.this, R.layout.info_show, null);
				
				TextView title = (TextView)view.findViewById(R.id.title);
				TextView desc = (TextView)view.findViewById(R.id.smallTitle);
				title.setText(locations.get(id));
				desc.setText(timeList.get(id));
				return view;
			}
			
		});
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> Adapterview, View view, int id,
					long lid) {
				// TODO Auto-generated method stub
				String locations[]=list.get(id).split(",");
				double cLon=Double.valueOf(locations[0]);
				double cLat = Double.valueOf(locations[1]);
				
				Intent intent=new Intent(ListViewHistroy.this,MyLocation.class);
				intent.putExtra("cLon", cLon);
				intent.putExtra("cLat", cLat);
				startActivity(intent);
			}
			
		});
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
    	case R.id.menuHis:
    		Intent intent=new Intent(ListViewHistroy.this,HisListActivity.class);
			startActivity(intent);
			break;
    	case R.id.menuExit:
    		//System.out.println("exit");
    		new MapApplication().getInstance().exit();
    		break;
    	default:break;
    	}
		return super.onOptionsItemSelected(item);
	}

}
