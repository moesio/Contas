package com.seimos.contas.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.seimos.contas.R;
import com.seimos.contas.database.DatabaseUtil;
import com.seimos.contas.model.Collect;

public class Dao {

	private Context context;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	public final String TABLE;
	public final String[] COLUMNS = { "_ID", "DATE", "SENT", "OM", "CMSR", "DC" };

	public Dao(Context context) {
		this.context = context;
		TABLE = context.getResources().getString(R.string.database_table_collect);
	}

	public boolean save(Collect collect) {
		SQLiteDatabase database = DatabaseUtil.open(context);

		ContentValues values = new ContentValues();
		String format2 = format.format(collect.getDate().getTime());
		values.put("date", format2);
		values.put("sent", collect.getSent());
		values.put("om", collect.getOm());
		values.put("cmsr", collect.getCmsr());
		values.put("dc", collect.getDc());

		long id = database.insert(TABLE, null, values);
		database.close();

		return id != 0;
	}

	public List<Collect> list() {
		SQLiteDatabase database = DatabaseUtil.open(context);

		List<Collect> list = new ArrayList<Collect>();
		Cursor cursor;
		try {
			cursor = database.query(TABLE, COLUMNS, null, null, null, null, COLUMNS[1]);
			list = extract(cursor);
		} catch (Exception e) {
			Log.e(context.getString(R.string.app_name), context.getString(R.string.database_access_error));
		}
		database.close();
		return list;
	}

	private List<Collect> extract(Cursor cursor) {
		List<Collect> list;
		if (cursor.moveToFirst()) {
			Collect collect;
			list = new ArrayList<Collect>();
			do {
				collect = new Collect();
				Integer id = cursor.getInt(cursor.getColumnIndex(COLUMNS[0]));
				String date = cursor.getString(cursor.getColumnIndex(COLUMNS[1]));
				Boolean sent = cursor.getShort(cursor.getColumnIndex(COLUMNS[2])) != 0;
				Double om = cursor.getDouble(cursor.getColumnIndex(COLUMNS[3]));
				Double cmsr = cursor.getDouble(cursor.getColumnIndex(COLUMNS[4]));
				Double dc = cursor.getDouble(cursor.getColumnIndex(COLUMNS[5]));

				Calendar formattedDate;
				try {
					formattedDate = Calendar.getInstance();
					formattedDate.setTime(format.parse(date));
				} catch (ParseException e) {
					formattedDate = null;
				}

				collect.setId(id).setDate(formattedDate).setSent(sent).setOm(om).setCmsr(cmsr).setDc(dc);
				list.add(collect);
			} while (cursor.moveToNext());
		} else {
			list = Collections.emptyList();
		}
		return list;
	}

	public boolean remove(long id) {
		SQLiteDatabase database = DatabaseUtil.open(context);

		int affectedRows = database.delete(TABLE, COLUMNS[0] + "= " + Long.toString(id), null);

		database.close();
		return affectedRows > 0;
	}

	public List<Collect> retrieve(String[] projection, String selection, String[] selectionArgs) {
		SQLiteDatabase database = DatabaseUtil.open(context);
		List<Collect> list = new ArrayList<Collect>();
		
		Cursor cursor;
		try {
			cursor = database.query(TABLE, projection, selection, selectionArgs, null, null, null);
			list = extract(cursor);
		} catch (Exception e) {
			Log.e(context.getString(R.string.app_name), context.getString(R.string.database_access_error));
		}

		database.close();
		return list;
	}

	public void clear() {
		SQLiteDatabase database = DatabaseUtil.open(context);
		database.delete(TABLE, "1 = 1", null);
		database.close();
	}

	public boolean update(Collect collect) {
		SQLiteDatabase database = DatabaseUtil.open(context);

		ContentValues values = new ContentValues();
		values.put("_id", collect.getId());
		values.put("date", format.format(collect.getDate().getTime()));
		values.put("sent", collect.getSent());
		values.put("om", collect.getOm());
		values.put("cmsr", collect.getCmsr());
		values.put("dc", collect.getDc());

		int affectedRows = database.update(TABLE, values, "_id = ?", new String[] { Integer.toString(collect.getId()) });
		database.close();

		return affectedRows != 0;
	}
}
