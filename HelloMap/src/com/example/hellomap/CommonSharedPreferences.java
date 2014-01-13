package com.example.hellomap;

import android.content.Context;
import android.content.SharedPreferences;

public class CommonSharedPreferences {
	SharedPreferences sp;
	Context context;
	SharedPreferences.Editor spEditor;
	public CommonSharedPreferences(Context context,String name){
		this.context=context;
		sp=context.getSharedPreferences(name, 0);
	}
	public void putValues(String key,Integer value){
		spEditor=sp.edit();
		spEditor.putInt(key, value);
		spEditor.commit();
	}
	public int getValues(String key){
		return sp.getInt(key, 0);
	}

}
