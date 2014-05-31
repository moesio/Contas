package com.seimos.contas.service;

import java.util.Calendar;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.seimos.contas.R;
import com.seimos.contas.activity.Home;
import com.seimos.contas.manager.Manager;

public class NotifyPendindCollectScheduler extends TimerTask {

	private Context context;
	private Manager manager;

	public NotifyPendindCollectScheduler(Context context) {
		this.context = context;
		manager = new Manager(context);
	}

	@Override
	public void run() {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		if (today != Calendar.SATURDAY && today != Calendar.SUNDAY) {
			if (!manager.havePendingCollect()) {
				this.cancel();
			} else {
				CharSequence text = context.getText(R.string.pending_item);
				Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, Home.class), 0);
				notification.setLatestEventInfo(context, context.getText(R.string.app_name), text, contentIntent);

				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(R.string.list_empty, notification);

			}
		}
	}

}
