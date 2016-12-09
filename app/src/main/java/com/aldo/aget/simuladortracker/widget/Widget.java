package com.aldo.aget.simuladortracker.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.aldo.aget.simuladortracker.MainActivity;
import com.aldo.aget.simuladortracker.R;
import com.aldo.aget.simuladortracker.Service.ServicioTrack;

import android.graphics.Bitmap;
import android.widget.RemoteViews;

/**
 * Created by Work on 09/12/16.
 */

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notificador_escritorio);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            views.removeAllViews(R.layout.notificador_escritorio);
            if(ServicioTrack.isInstanceCreated()){
                views.setImageViewResource(R.id.imgWidget,R.drawable.ic_location_on_blue);
            }else{
                views.setImageViewResource(R.id.imgWidget,R.drawable.ic_location_off_blue);
            }




            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


}
