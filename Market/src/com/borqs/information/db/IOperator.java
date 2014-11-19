package com.borqs.information.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.borqs.information.InformationBase;

public interface IOperator {
	
	public void add(InformationBase i);
	public void add(List<InformationBase> items);
	public boolean update(long id, InformationBase i);
	public boolean update(long id, ContentValues values);
	public boolean updateReadStatus(long id, boolean b);
	public boolean updateAllReadStatus(boolean b);
//	public boolean delete(long id);
//	public InformationBase load(long id);
	public Cursor loadAll(String queryType);
	public Cursor loadThisWeek(String queryType);
	
	public long getLastModifyDate();
	public int getUnReadCount();
	public int getThisWeekUnReadCount();
	
	public void delete(String where, String selectionArgs[]);
	
//	public int getCount();
//	public long[] batchAdd(List<InformationBase> ia);
//  public long add(JSON obj);
//	public long[] getDuplicateIds(long[] ids);
	
}
