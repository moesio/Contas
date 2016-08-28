package com.seimos.contas.fragment;

import java.util.Calendar;
import java.util.Timer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.seimos.contas.R;
import com.seimos.contas.activity.Home;
import com.seimos.contas.exception.CollectNotAllowedException;
import com.seimos.contas.manager.Manager;
import com.seimos.contas.model.Collect;
import com.seimos.contas.service.NotifyPendindCollectScheduler;

public class Form extends Fragment {

	private Manager manager;
	private Button btnSave;
	private Button btnCancel;
	private EditText editOM;
	private EditText editDC;
	private DatePicker datePicker;

	private class SaveListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnSave:
				save();
				break;
			case R.id.btnCancel:
				clearFields();
				break;
			default:
				break;
			}
		}

		private void clearFields() {
			Calendar date = Calendar.getInstance();
			datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
			editOM.setText("");
			editDC.setText("");

			editOM.requestFocus();
		}

		private void save() {

			Calendar date = Calendar.getInstance();
			date.set(Calendar.YEAR, datePicker.getYear());
			date.set(Calendar.MONTH, datePicker.getMonth());
			date.set(Calendar.DATE, datePicker.getDayOfMonth());

			String omString = editOM.getText().toString();
			String dcString = editDC.getText().toString();
			Double om = Double.valueOf(omString.length() == 0 ? "0" : omString);
			Double dc = Double.valueOf(dcString.length() == 0 ? "0" : dcString);

			if (Double.valueOf(om) > 0 || Double.valueOf(dc) > 0) {
				Collect collection = new Collect().setDate(date).setSent(false).setOm(om).setDc(dc);
				try {
					if (manager.save(collection)) {
						clearFields();
						Toast.makeText(getActivity(), R.string.database_save_ok, Toast.LENGTH_LONG).show();

						// TODO Corrigir esse acoplamento
						Home activity = (Home) getActivity();
						List listFragment = (List) activity.getmTabsAdapter().getItem(0);
						listFragment.getAdapter().refresh();

//						scheduleNotification(date);
					}
				} catch (CollectNotAllowedException e) {
					new AlertDialog.Builder(getActivity()).setMessage(R.string.collect_forbidden).setPositiveButton(android.R.string.ok, null).show();
				}
			} else {
				Toast.makeText(getActivity(), R.string.nothing_to_do, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		manager = new Manager(getActivity());
		btnSave = (Button) getView().findViewById(R.id.btnSave);
		btnCancel = (Button) getView().findViewById(R.id.btnCancel);
		editOM = (EditText) getView().findViewById(R.id.editOM);
		editDC = (EditText) getView().findViewById(R.id.editDC);
		datePicker = (DatePicker) getView().findViewById(R.id.datePicker);

		SaveListener saveListener = new SaveListener();
		btnSave.setOnClickListener(saveListener);
		btnCancel.setOnClickListener(saveListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_form, container, false);
		return view;
	}

	private void scheduleNotification(Calendar date) {
		NotifyPendindCollectScheduler task = new NotifyPendindCollectScheduler(getActivity());

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, setToNextDayAtNoon(date).getTime(), 86400000L); // repeat every 24 hours
	}

	private Calendar setToNextDayAtNoon(Calendar date) {
		date.add(Calendar.DATE, 1);
		date.set(Calendar.HOUR_OF_DAY, 12);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);

		if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			date.add(Calendar.DATE, 1);
		}
		if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			date.add(Calendar.DATE, 1);
		}

		return date;
	}

}
