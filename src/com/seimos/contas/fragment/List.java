package com.seimos.contas.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seimos.contas.R;
import com.seimos.contas.manager.Manager;
import com.seimos.contas.model.Collect;

/**
 * Mostra a listagem dos valores ordenados por dia
 * @author moesio @ gmail.com
 * @date Mar 12, 2013 2:10:21 PM
 */
public class List extends ListFragment {

	private SimpleDateFormat format;
	private Adapter adapter;
	private Button btnRefresh;
	private Button btnCloseMonth;
	private Manager manager;
	private java.util.List<Collect> list = Collections.emptyList();

	private class ClickItemListener implements AdapterView.OnItemLongClickListener, OnItemClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			Collect collect = (Collect) view.getTag();
			android.content.DialogInterface.OnClickListener listener = new ItemRemovalDialogConfirmListener(collect);

			AlertDialog itemRemovalDialog = new AlertDialog.Builder(getActivity()).create();
			itemRemovalDialog.setTitle(getResources().getString(R.string.title_dialog_confirm));
			itemRemovalDialog.setMessage(getResources().getString(R.string.txt_confirm_deletion));
			itemRemovalDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(android.R.string.yes), listener);
			itemRemovalDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(android.R.string.no), listener);
			itemRemovalDialog.show();
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TextView txtTotal = (TextView) view.findViewById(R.id.txtTotal);
			String total = txtTotal.getText().toString();

			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(total);
			
			Toast.makeText(getActivity(), getString(R.string.value_copied).concat(" ").concat(total), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Ouve o click do checkbox da listagem
	 * @author moesio @ gmail.com
	 * @date Mar 12, 2013 2:08:47 PM
	 */
	private class ListItemSentCheckboxChangeListener implements OnClickListener {

		@Override
		public void onClick(final View view) {

			final CheckBox chkSent = (CheckBox) view;
			final boolean checked = chkSent.isChecked();

			AlertDialog confirmDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.txt_confirm)
					.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							chkSent.setChecked(!checked);
						}
					}).setMessage(R.string.txt_confirm_sent).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Collect collect = (Collect) view.getTag();
							collect.setSent(checked);
							if (!manager.update(collect)) {
								adapter.refresh();
							}
						}
					}).create();
			confirmDialog.show();
		}
	}

	/**
	 * Confirmação de envio de SMS após a entrada do valor extra que será adicionado ao relatório
	 * @author moesio @ gmail.com
	 * @date Mar 12, 2013 2:11:32 PM
	 */
	private class ConfirmSmsSendDialogListener implements DialogInterface.OnClickListener {
		private final EditText editExtraValue;

		private ConfirmSmsSendDialogListener(EditText editExtraValue) {
			this.editExtraValue = editExtraValue;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {

			double om = 0;
			Calendar baseDate = null;
			for (Collect collect : list) {
				if (baseDate == null) {
					baseDate = collect.getDate();
				}
				om += collect.getOm();
			}

			double extraValue;
			try {
				extraValue = Double.valueOf(editExtraValue.getText().toString());
			} catch (NumberFormatException e) {
				extraValue = 0;
			}
			SimpleDateFormat format = new SimpleDateFormat("MMMM/yyyy");

			String messageBody = String.format(
					getResources().getString(R.string.title_dialog_summary) + " " + format.format(baseDate.getTime()) + "\n" + getResources().getString(R.string.txt_om) //
							+ " %1$,.2f\n" + getResources().getString(R.string.txt_extra_value) //
							+ " %2$,.2f\n" + "---------------\n" //
							+ getActivity().getString(R.string.total) //
							+ " %3$,.2f", om, extraValue, (om + extraValue));
			
			  Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		        sharingIntent.setType("text/plain");
//		        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
		        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageBody);
//		        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
//		        startActivity(Intent.createChooser(sharingIntent, "algo"));
		        startActivity(sharingIntent);
			
//			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//			smsIntent.setType("vnd.android-dir/mms-sms");
//			smsIntent.putExtra("sms_body", smsMessage);
//			startActivity(smsIntent);
		}
	}

	/**
	 * Ouve os botões da caixa de diálogo do resumo do mês
	 * @author moesio @ gmail.com
	 * @date Mar 12, 2013 2:12:33 PM
	 */
	private class SummaryDialogToolbarListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				// It will ask for an extra value, through a dialog. It's the left most button
				askExtraValue();
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				// It's the middle button
				clearList();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				// Do nothing. It's cancel button. The right most one.
				break;
			}
		}

		private void askExtraValue() {
			if (list.isEmpty()) {
				Toast.makeText(getActivity(), R.string.list_empty, Toast.LENGTH_SHORT).show();
			} else {

				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.layout_extra_value, null);

				final EditText editExtraValue = (EditText) view.findViewById(R.id.editExtraValue);

				ConfirmSmsSendDialogListener confirmSmsSendDialogListener = new ConfirmSmsSendDialogListener(editExtraValue);
				AlertDialog confirmSmsSendDialog = new AlertDialog.Builder(getActivity()).setView(view).setNeutralButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, confirmSmsSendDialogListener).create();
				confirmSmsSendDialog.show();
			}
		}

		private void clearList() {
			AlertDialog clearListDialog = new AlertDialog.Builder(getActivity()).setMessage(R.string.txt_confirm_clear).setTitle(R.string.title_dialog_confirm)
					.setNeutralButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							manager.clear();
							adapter.refresh();
						}
					}).create();
			clearListDialog.show();
		}

	}

	/**
	 * Ouve os botões da tela principal da listagem
	 * @author moesio @ gmail.com
	 * @date Mar 12, 2013 2:16:03 PM
	 */
	private class ToolbarListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRefresh:
				adapter.refresh();
				break;
			case R.id.btnSummary:
				adapter.refresh();
				closeMonth();
				break;
			}
		}

		private void closeMonth() {
			adapter.refresh();

			AlertDialog summaryDialog = new AlertDialog.Builder(getActivity()).create();
			summaryDialog.setTitle(getResources().getString(R.string.title_dialog_summary));

			final java.util.List<Collect> list = adapter.getList();
			double om = 0, dc = 0;
			for (Collect collect : list) {
				om += collect.getOm();
				dc += collect.getDc();
			}

			final String formattedMessage = String.format("<p>" + //
					"<b>" + getResources().getString(R.string.txt_om) + "</b> %1$,.2f<br/>" + //
					"<b>" + getResources().getString(R.string.txt_dc) + "</b> %2$,.2f<br/>" + //
					"<b>" + getResources().getString(R.string.total) + "</b> %3$,.2f" + //
					"</p>", om, dc, (om + dc));
			summaryDialog.setMessage(Html.fromHtml(formattedMessage));

			android.content.DialogInterface.OnClickListener listener = new SummaryDialogToolbarListener();

			summaryDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.btn_send), listener);
			summaryDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.btn_cancel), listener);
			summaryDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.btn_close_month), listener);

			summaryDialog.show();
		}
	}

	/**
	 * Ouve o click no item da listagem. Permite apagá-lo.
	 * @author moesio @ gmail.com
	 * @date Mar 12, 2013 2:17:44 PM
	 */
	private class ItemRemovalDialogConfirmListener implements DialogInterface.OnClickListener {

		private Collect collect;

		public ItemRemovalDialogConfirmListener(Collect collect) {
			this.collect = collect;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				if (manager.remove(collect.getId())) {
					adapter.refresh();
				}
				break;
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Context context = getActivity();

		manager = new Manager(context);
		format = new SimpleDateFormat(context.getResources().getString(R.string.date_format));
		adapter = new Adapter(context);
		setListAdapter(adapter);

		btnRefresh = (Button) getView().findViewById(R.id.btnRefresh);
		btnCloseMonth = (Button) getView().findViewById(R.id.btnSummary);

		ToolbarListener toolbarListener = new ToolbarListener();
		btnRefresh.setOnClickListener(toolbarListener);
		btnCloseMonth.setOnClickListener(toolbarListener);

		this.getListView().setLongClickable(true);

		ClickItemListener clickItemListener = new ClickItemListener();
		getListView().setOnItemLongClickListener(clickItemListener);
		getListView().setOnItemClickListener(clickItemListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_list, container, false);
		return view;
	}

	public class Adapter extends BaseAdapter {

		private Context context;
		private ListItemSentCheckboxChangeListener listItemSentCheckboxChangeListener = new ListItemSentCheckboxChangeListener();

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
			Collect collect = list.get(position);

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_item, null);
			int color;
			if (position % 2 == 0) {
				color = 0x22ffffff;
			} else {
				color = 0x00ffffff;
			}
			view.setBackgroundColor(color);
			view.setTag(collect);

			TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
			TextView txtOM = (TextView) view.findViewById(R.id.txtOM);
			TextView txtDC = (TextView) view.findViewById(R.id.txtDC);
			TextView txtTotal = (TextView) view.findViewById(R.id.txtTotal);
			CheckBox chkSent = (CheckBox) view.findViewById(R.id.chkSent);
			chkSent.setTag(collect);

			Calendar date = collect.getDate();
			Boolean sent = collect.getSent();
			Double om = collect.getOm();
			Double dc = collect.getDc();
			Double total = om + dc;

			listItemSentCheckboxChangeListener = new ListItemSentCheckboxChangeListener();
			chkSent.setOnClickListener(listItemSentCheckboxChangeListener);

			txtDate.setText(format.format(date.getTime()));
			chkSent.setChecked(sent);
			txtOM.setText(String.format(" %1$,.2f", om));
			txtDC.setText(String.format(" %1$,.2f", dc));
			txtTotal.setText(String.format(" %1$,.2f", total));

			return view;
		}
	}

	public Adapter getAdapter() {
		return adapter;
	}

}
