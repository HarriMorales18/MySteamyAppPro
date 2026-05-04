package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;

public class FavoriteWidget extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorite);

    try {
      SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
      String favoriteGameJson = prefs.getString("favoriteGame", null);

      if (favoriteGameJson != null) {
        JSONObject game = new JSONObject(favoriteGameJson);
        String title = game.optString("title", "Cargando...");
        String thumbUrl = game.optString("thumb", "");
        JSONArray deals = game.optJSONArray("deals");

        // 🟢 PASO 1: Mostrar estructura inmediatamente
        views.setViewVisibility(R.id.widget_game_content, View.VISIBLE);
        views.setViewVisibility(R.id.widget_empty_state, View.GONE);
        views.setTextViewText(R.id.widget_title, title);

        // Limpiamos y reseteamos el flipper al primer hijo (índice 0)
        views.removeAllViews(R.id.widget_deals_flipper);
        views.setDisplayedChild(R.id.widget_deals_flipper, 0);

        // Actualización rápida para que el usuario vea el título mientras carga el resto
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // 🟢 PASO 2: Hilo secundario para imágenes y ofertas
        new Thread(() -> {
          try {
            // Descargar imagen principal
            if (!thumbUrl.isEmpty()) {
              URL url = new URL(thumbUrl);
              Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
              views.setImageViewBitmap(R.id.widget_image, bmp);
            }

            if (deals != null && deals.length() > 0) {
              for (int i = 0; i < deals.length(); i++) {
                JSONObject deal = deals.getJSONObject(i);
                RemoteViews dealView = new RemoteViews(context.getPackageName(), R.layout.widget_deal_item);

                // Datos de la oferta
                String salePrice = deal.optString("salePrice", "0.00");
                String normalPrice = deal.optString("normalPrice", "0.00");
                double savings = deal.optDouble("savings", 0);
                String storeName = deal.optString("storeName", "Tienda");
                String storeIconUrl = deal.optString("storeIcon", "");

                dealView.setTextViewText(R.id.widget_store, storeName);
                dealView.setTextViewText(R.id.widget_sale_price, "$" + salePrice);
                dealView.setTextViewText(R.id.widget_normal_price, "$" + normalPrice);
                dealView.setTextViewText(R.id.widget_savings, "-" + Math.round(savings) + "%");
                dealView.setInt(R.id.widget_normal_price, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

                // Descargar icono de tienda
                if (!storeIconUrl.isEmpty()) {
                  try {
                    Bitmap iconBmp = BitmapFactory.decodeStream(new URL(storeIconUrl).openConnection().getInputStream());
                    dealView.setImageViewBitmap(R.id.widget_store_icon, iconBmp);
                  } catch (Exception e) { e.printStackTrace(); }
                }

                views.addView(R.id.widget_deals_flipper, dealView);
              }
            }

            // Forzar que empiece en la primera oferta cargada
            views.setDisplayedChild(R.id.widget_deals_flipper, 0);

            // Actualización final con todo cargado
            appWidgetManager.updateAppWidget(appWidgetId, views);

          } catch (Exception e) {
            e.printStackTrace();
          }
        }).start();

      } else {
        views.setViewVisibility(R.id.widget_game_content, View.GONE);
        views.setViewVisibility(R.id.widget_empty_state, View.VISIBLE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
