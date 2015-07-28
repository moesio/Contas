package com.seimos.contas.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.seimos.contas.dao.Dao;
import com.seimos.contas.exception.CollectNotAllowedException;
import com.seimos.contas.model.Collect;

public class Manager {

	private Dao dao;
	private SimpleDateFormat format;

	public Manager(Context context) {
		this.dao = new Dao(context);
		format = (SimpleDateFormat) SimpleDateFormat.getInstance();//new SimpleDateFormat("yyyy-MM-dd");
	}

	public boolean save(Collect collect) throws CollectNotAllowedException {
		List<Collect> list = retrieveCollectFromOtherMonth(collect);

		if (list.isEmpty()) {
			return dao.save(collect);
		} else {
			throw new CollectNotAllowedException();
		}
	}

	private List<Collect> retrieveCollectFromOtherMonth(Collect collect) {
		int month = collect.getDate().get(Calendar.MONTH);
		int year = collect.getDate().get(Calendar.YEAR);

		Calendar cMin = Calendar.getInstance();
		Calendar cMax = Calendar.getInstance();

		cMin.set(Calendar.YEAR, year);
		cMin.set(Calendar.MONTH, month);
		cMin.set(Calendar.DATE, 1);

		cMax.set(Calendar.YEAR, year);
		cMax.set(Calendar.MONTH, month + 1);
		cMax.set(Calendar.DATE, 1);

		List<Collect> list = dao.retrieve(dao.COLUMNS, "date < ? or date >= ?", new String[] { format.format(cMin.getTime()), format.format(cMax.getTime()) });
		return list;
	}

	public List<Collect> list() {
		return dao.list();
	}

	public boolean remove(long id) {
		return dao.remove(id);
	}

	public void clear() {
		dao.clear();
	}

	public boolean update(Collect collect) {
		return dao.update(collect);
	}

	public boolean havePendingCollect() {
		List<Collect> list = dao.retrieve(dao.COLUMNS, "sent = ?", new String[] { "0" });
		return !list.isEmpty();
	}

}
