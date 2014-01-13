package com.example.hellomap;



import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class LocationSqliteHelper extends SQLiteOpenHelper {
	public static final String tableName="location";
	public static final String ID="id";
    public static final String CLON="cLon";
    public static final String CLAT="cLat";
    public static final String TIME="locTime";
    public static final String DETAIL="detail";
    private SQLiteDatabase db;
    public static final String databaseName="location.db";
	public LocationSqliteHelper(Context context) {
		super(context, databaseName, null, 2);
		// TODO Auto-generated constructor stub
		db=getWritableDatabase();
		System.out.println("database inint");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.db=db;
		db.execSQL("create table if not exists location ( _id integer primary key autoincrement ," +
				"cLon varchar , cLat varchar ,locTime  varchar, detail varchar )");
		Log.e("database", "onCreate");
	}
	public void save(double clon,double clat,String time,String detail){
		ContentValues cv=new ContentValues();
		cv.put(LocationSqliteHelper.CLON, String.valueOf(clon));
		cv.put(LocationSqliteHelper.CLAT, String.valueOf(clat));
		cv.put(LocationSqliteHelper.TIME, time);
		cv.put(LocationSqliteHelper.DETAIL, detail);
		db.insert(LocationSqliteHelper.tableName, null, cv);
	}
	public String getLatest(){
		SQLiteDatabase db = getWritableDatabase();  
		Cursor cursor=db.query(LocationSqliteHelper.tableName,new String[]{LocationSqliteHelper.CLON,LocationSqliteHelper.CLAT} , null, null, null, null, null);
		String location="121.401291,31.317318";
		if(cursor.getCount()!=0){
		    cursor.moveToLast();
			String clon=cursor.getString(0);
			String clat=cursor.getString(1);
			System.out.println(clon+","+clat);
			location=clon+","+clat;
		}
		cursor.close();
		return location;
	}
	public ArrayList<String> getNearestLocation(){
		ArrayList<String> list=new ArrayList<String>();
		SQLiteDatabase db = getWritableDatabase();  
		Cursor cursor=db.query(LocationSqliteHelper.tableName,new String[]{LocationSqliteHelper.CLON,LocationSqliteHelper.CLAT} , null, null, null, null, null);
		
		if(cursor.getCount()!=0){
		    cursor.moveToLast();
		    while(!cursor.isBeforeFirst()){
		    	String clon=cursor.getString(0);
				String clat=cursor.getString(1);
				list.add(clon+","+clat);
				
				cursor.moveToPrevious();
		    }
		}
		cursor.close();
		return list;
	}
	public ArrayList<String> getNearestTime(){
		ArrayList<String> list=new ArrayList<String>();
		SQLiteDatabase db = getWritableDatabase();  
		Cursor cursor=db.query(LocationSqliteHelper.tableName,new String[]{LocationSqliteHelper.TIME} , null, null, null, null, null);
		
		if(cursor.getCount()!=0){
		    cursor.moveToLast();
		    while(!cursor.isBeforeFirst()){
		    	list.add(cursor.getString(0));
				
				cursor.moveToPrevious();
		    }
		}
		cursor.close();
		return list;
	}
	public ArrayList<String> getNearestDetail(){
		ArrayList<String> list=new ArrayList<String>();
		SQLiteDatabase db = getWritableDatabase();  
		Cursor cursor=db.query(LocationSqliteHelper.tableName,new String[]{LocationSqliteHelper.DETAIL} , null, null, null, null, null);
		
		if(cursor.getCount()!=0){
		    cursor.moveToLast();
		    while(!cursor.isBeforeFirst()){
		    	list.add(cursor.getString(0));
				
				cursor.moveToPrevious();
		    }
		}
		cursor.close();
		return list;
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
	public void close() {  
        if (db != null)  
            db.close();  
    }  

}
