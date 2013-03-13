package com.seimos.contas.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.seimos.contas.R;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final Context context;

	public DatabaseHelper(Context context) {
		super(context, context.getString(R.string.app_name), null, Integer.valueOf(context
				.getString(R.string.database_version)));
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDb(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String[] versions = context.getResources().getStringArray(R.array.database_upgrade);
		for (int i = oldVersion; i < newVersion; i++) {
			String[] statements = versions[i - 1].split(";");
			for (String statement : statements) {
				db.execSQL(statement);
			}
		}
	}

	private void createDb(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			String[] tables = context.getResources().getStringArray(R.array.database_tables);
			for (String table : tables) {
				db.execSQL(table);
			}
		} catch (SQLException e) {
			Log.e(context.getString(R.string.app_name), context.getString(R.string.database_access_error));
		} finally {
			db.endTransaction();
		}
	}
}
