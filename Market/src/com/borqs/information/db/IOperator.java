package com.borqs.information.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.borqs.information.InformationBase;

public interface IOperator {
	
	void add(InformationBase i);
	void add(List<InformationBase> items);
	boolean update(long id, InformationBase i);
	boolean update(long id, ContentValues values);
	boolean updateReadStatus(long id, boolean b);
	boolean updateAllReadStatus(boolean b);
//	public boolean delete(long id);
//	public InformationBase load(long id);
Cursor loadAll(String queryType);
	Cursor loadThisWeek(String queryType);
	
	long getLastModifyDate();
	int getUnReadCount();
	int getThisWeekUnReadCount();
	
	void delete(String where, String selectionArgs[]);
	
//	public int getCount();
//	public long[] batchAdd(List<InformationBase> ia);
//  public long add(JSON obj);
//	public long[] getDuplicateIds(long[] ids);
	
}
