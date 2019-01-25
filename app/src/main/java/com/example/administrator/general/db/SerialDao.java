package com.example.administrator.general.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.general.DataBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018\11\6 0006.
 * 导入资料的数据库
 */

public class SerialDao {

    private MyOpenHelper helper;
    private SQLiteDatabase db;

    public SerialDao(Context context) {
        helper = new MyOpenHelper(context);
    }

    //写增删改查的方法
    public void init() {
        //打开数据库
        db = helper.getReadableDatabase();
    }

    //添加的方法
    public boolean insert(DataBean dataBean) {
        boolean isExist = isNewsExist(dataBean);
        if (isExist) {
            db.close();
            return false; //返回添加失败
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("number", dataBean.getNumber());
            contentValues.put("name", dataBean.getName());
            contentValues.put("quantity", dataBean.getQuantity());
            db.insert("Customer", null, contentValues);
            db.close();
            return true;//返回添加成功
        }
    }


    //删除的方法
    public void delete() {
        init();
        //根据newsURL进行数据删除
        db.delete("Customer", null, null);
        db.close();
    }


//    //查询的方法-多条件查询
//    public String select(String str) {
//        init();
//        String companyName = null;
//        Cursor cursor = db.query("Customer", null, "fenyunNumber = ?", new String[]{str}, null, null, null);
////        Cursor cursor = db.query("Customer", null, null, null, null, null, null);
////        Cursor cursor = db.query("SerialNumberBiao1",null,"model = ? and brand = ?", new String[]{str,s},null,null,null);
//        while (cursor.moveToNext()) {
//            companyName = cursor.getString(cursor.getColumnIndex("name"));
//        }
//        return companyName;
//    }

    //查询的方法
    public List<DataBean> select1(String str){
        init();
        List<DataBean> list = new ArrayList<>();
        Cursor cursor = db.query("Customer", null, "number = ?", new String[]{str}, null, null, null);
        while (cursor.moveToNext()) {
            String number= cursor.getString(cursor.getColumnIndex("number"));
            String name= cursor.getString(cursor.getColumnIndex("name"));
            String quantity= cursor.getString(cursor.getColumnIndex("quantity"));
            DataBean dataBean = new DataBean();
            dataBean.setName(name);
            dataBean.setNumber(number);
            dataBean.setQuantity(quantity);
            list.add(dataBean);
        }
        return  list;
    }


    //判断是否存在
    public boolean isNewsExist(DataBean dataBean) {
        init();
        Cursor cursor = db.query("Customer", null, "number = ?", new String[]{dataBean.getNumber()}, null, null, null);
//        Log.i("Tag",newsInfo.getUrl());
        if (cursor.moveToFirst()) {
            return true; // 已经存在该数据
        } else {
            return false;//不存在
        }
    }
}
