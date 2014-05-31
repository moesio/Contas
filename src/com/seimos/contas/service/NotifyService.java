package com.seimos.contas.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotifyService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static String getName() {
		return NotifyService.class.getSimpleName();
	}
	
	
}
