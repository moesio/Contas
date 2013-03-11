package com.seimos.contas.fragment;

import java.util.Calendar;

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
import com.seimos.contas.exception.CollectNotAllowedException;
import com.seimos.contas.manager.Manager;
import com.seimos.contas.model.Collect;

public class Form extends Fragment {

	private Manager manager;
	private Button btnSave;
	private Button btnCancel;
	private EditText editOM;
	private EditText editCMSR;
	private EditText editDC;
	private DatePicker datePicker;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		manager = new Manager(getActivity());
		btnSave = (Button) getView().findViewById(R.id.btnSave);
		btnCancel = (Button) getView().findViewById(R.id.btnCancel);
		editOM = (EditText) getView().findViewById(R.id.editOM);
		editCMSR = (EditText) getView().findViewById(R.id.editCMSR);
		editDC = (EditText) getView().findViewById(R.id.editDC);
		datePicker = (DatePicker) getView().findViewById(R.id.datePicker);

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar date = Calendar.getInstance();
				date.set(Calendar.YEAR, datePicker.getYear());
				date.set(Calendar.MONTH, datePicker.getMonth());
				date.set(Calendar.DATE, datePicker.getDayOfMonth());

				String omString = editOM.getText().toString();
				String cmsrString = editCMSR.getText().toString();
				String dcString = editDC.getText().toString();
				Double om = Double.valueOf(omString.length() == 0 ? "0" : omString);
				Double cmsr = Double.valueOf(cmsrString.length() == 0 ? "0" : cmsrString);
				Double dc = Double.valueOf(dcString.length() == 0 ? "0" : dcString);

				Collect collection = new Collect().setDate(date).setSent(false).setOm(om).setCmsr(cmsr).setDc(dc);
				try {
					if (manager.save(collection)) {
						clearFields();
						Toast.makeText(getActivity(), getResources().getString(R.string.database_save_ok),
								Toast.LENGTH_LONG).show();
					}
				} catch (CollectNotAllowedException e) {
					new AlertDialog.Builder(getActivity()).setMessage(R.string.collect_forbidden)
							.setPositiveButton(android.R.string.ok, null).show();
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearFields();
			}

		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_form, container, false);
		return view;
	}

	private void clearFields() {
		Calendar date = Calendar.getInstance();
		datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
		editOM.setText("");
		editCMSR.setText("");
		editDC.setText("");

		editOM.requestFocus();
	}

}
