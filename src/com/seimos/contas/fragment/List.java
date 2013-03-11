package com.seimos.contas.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.seimos.contas.R;
import com.seimos.contas.manager.Manager;
import com.seimos.contas.model.Collect;

public class List extends ListFragment {

	private SimpleDateFormat format;
	private Adapter adapter;
	private Button btnRefresh;
	private Button btnCloseMonth;
	private Manager manager;

	@Override
	public void onListItemClick(ListView l, View v, int position, final long id) {
		android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					if (manager.remove(id)) {
						adapter.refresh();
					}
					break;
				}
			}
		};

		AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.setTitle(getResources().getString(R.string.title_dialog_confirm));
		dialog.setMessage(getResources().getString(R.string.txt_confirm_deletion));
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(android.R.string.yes), listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(android.R.string.no), listener);
		dialog.show();
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Context context = getActivity();

		manager = new Manager(context);
		format = new SimpleDateFormat(context.getResources().getString(R.string.date_format));
		adapter = new Adapter(context);
		setListAdapter(adapter);

		btnRefresh = (Button) getView().findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapter.refresh();
			}

		});

		btnCloseMonth = (Button) getView().findViewById(R.id.btnCloseMonth);

		btnCloseMonth.setOnClickListener(new OnClickListener() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onClick(View v) {
				adapter.refresh();

				AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
				dialog.setTitle(getResources().getString(R.string.title_dialog_summary));

				final java.util.List<Collect> list = adapter.getList();
				double om = 0, cmsr = 0, dc = 0;
				for (Collect collect : list) {
					om += collect.getOm();
					cmsr += collect.getCmsr();
					dc += collect.getDc();
				}

				final String formattedMessage = String.format("<p>" + //
						"<b>" + getResources().getString(R.string.txt_om) + "</b> %1$,.2f<br/>" + //
						"<b>" + getResources().getString(R.string.txt_cmsr) + "</b> %2$,.2f<br/>" + //
						"<b>" + getResources().getString(R.string.txt_dc) + "</b> %3$,.2f" + //
						"</p>", om, cmsr, dc);
				dialog.setMessage(Html.fromHtml(formattedMessage));

				android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case AlertDialog.BUTTON_POSITIVE:

							if (list.isEmpty()) {
								Toast.makeText(getActivity(), R.string.list_empty, Toast.LENGTH_SHORT).show();
							} else {

								LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);
								View view = inflater.inflate(R.layout.layout_extra_value, null);

								final EditText editExtraValue = (EditText) view.findViewById(R.id.editExtraValue);

								new AlertDialog.Builder(getActivity()).setView(view)
										.setNeutralButton(android.R.string.cancel, null)
										.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {

												double om = 0, cmsr = 0;
												Calendar baseDate = null;
												for (Collect collect : list) {
													if (baseDate == null) {
														baseDate = collect.getDate();
													}
													om += collect.getOm();
													cmsr += collect.getCmsr();
												}

												double extraValue = Double.valueOf(editExtraValue.getText().toString());
												SimpleDateFormat format = new SimpleDateFormat("MMMM/yyyy");

												String smsMessage = String.format(
														getResources().getString(R.string.title_dialog_summary)
																+ " "
																+ format.format(baseDate.getTime())
																+ "\n"
																+ getResources().getString(R.string.txt_om) //
																+ " %1$,.2f\n"
																+ getResources().getString(R.string.txt_cmsr) //
																+ " %2$,.2f\n"
																+ getResources().getString(R.string.txt_extra_value) //
																+ " %3$,.2f\n" + "---------------\n" //
																+ getActivity().getString(R.string.total) //
																+ " %4$,.2f", om, cmsr, extraValue,
														(om + cmsr + extraValue));
												Intent smsIntent = new Intent(Intent.ACTION_VIEW);
												smsIntent.setType("vnd.android-dir/mms-sms");
												smsIntent.putExtra("sms_body", smsMessage);
												startActivity(smsIntent);
											}

										}).show();
							}
							break;
						case AlertDialog.BUTTON_NEUTRAL:
							new AlertDialog.Builder(getActivity()).setMessage(R.string.txt_confirm_clear)
									.setTitle(R.string.title_dialog_confirm)
									.setNeutralButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											manager.clear();
											adapter.refresh();
										}
									}).show();
							//							Toast.makeText(getActivity(), "Fake clear", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				};

				dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.btn_send), listener);
				dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.btn_cancel), listener);
				dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.btn_clear), listener);

				dialog.show();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_list, container, false);
		return view;
	}

	public class Adapter extends BaseAdapter {

		private java.util.List<Collect> list = Collections.emptyList();
		private Context context;

		public Adapter(Context context) {
			this.context = context;

			refresh();
		}

		public java.util.List<Collect> getList() {
			return list;
		}

		public void refresh() {
			list = manager.list();

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return list.get(position).getId();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_item, null);

			TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
			TextView txtOM = (TextView) view.findViewById(R.id.txtOM);
			TextView txtCMSR = (TextView) view.findViewById(R.id.txtCMSR);
			TextView txtDC = (TextView) view.findViewById(R.id.txtDC);
			TextView txtTotal = (TextView) view.findViewById(R.id.txtTotal);
			CheckBox chkSent = (CheckBox) view.findViewById(R.id.chkSent);
			
			final Collect collect = list.get(position);
			Calendar date = collect.getDate();
			Boolean sent = collect.getSent();
			Double om = collect.getOm();
			Double cmsr = collect.getCmsr();
			Double dc = collect.getDc();
			Double total = om + cmsr + dc;

			OnCheckedChangeListener listener = new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					collect.setSent(isChecked);
					if (!manager.update(collect)) {
						adapter.refresh();
					}
				}
			};
			chkSent.setOnCheckedChangeListener(listener);
			
			txtDate.setText(format.format(date.getTime()));
			chkSent.setChecked(sent);
			txtOM.setText(String.format(" %1$,.2f", om));
			txtCMSR.setText(String.format(" %1$,.2f", cmsr));
			txtDC.setText(String.format(" %1$,.2f", dc));
			txtTotal.setText(String.format(" %1$,.2f", total));

			return view;
		}

	}

}
