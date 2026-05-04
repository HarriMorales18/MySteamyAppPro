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
      // Accedemos al almacenamiento de Capacitor
      SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
      String favoriteGameJson = prefs.getString("favoriteGame", null);

      if (favoriteGameJson != null) {
        // 🟢 SÍ HAY FAVORITO: Mostramos el juego y ocultamos el mensaje vacío
        views.setViewVisibility(R.id.widget_game_content, View.VISIBLE);
        views.setViewVisibility(R.id.widget_empty_state, View.GONE);

        JSONObject game = new JSONObject(favoriteGameJson);

        // 1. Extraer datos básicos del juego
        String title = game.optString("title", "Unknown Game");
        String thumbUrl = game.optString("thumb", "");

        // Asignar el título principal
        views.setTextViewText(R.id.widget_title, title);

        // 2. Limpiar el carrusel (ViewFlipper) para evitar que se dupliquen datos viejos
        views.removeAllViews(R.id.widget_deals_flipper);

        // 3. Extraer el arreglo de ofertas (deals) e inyectarlos en el carrusel
        JSONArray deals = game.optJSONArray("deals");

        if (deals != null && deals.length() > 0) {
          for (int i = 0; i < deals.length(); i++) {
            JSONObject deal = deals.getJSONObject(i);

            // Inflar el diseño individual para esta oferta
            RemoteViews dealView = new RemoteViews(context.getPackageName(), R.layout.widget_deal_item);

            // Extraer datos de la oferta específica
            String salePrice = deal.optString("salePrice", "0.00");
            String normalPrice = deal.optString("normalPrice", "0.00");
            double savings = deal.optDouble("savings", 0);

            // 🔥 LA MAGIA: Leemos directamente el nombre de la tienda enviado desde Ionic
            String storeName = deal.optString("storeName", "Digital Store");

            // Asignar textos a esta vista
            dealView.setTextViewText(R.id.widget_store, storeName);
            dealView.setTextViewText(R.id.widget_sale_price, "$" + salePrice);
            dealView.setTextViewText(R.id.widget_normal_price, "$" + normalPrice);
            dealView.setTextViewText(R.id.widget_savings, "-" + Math.round(savings) + "%");

            // Tachar el precio normal
            dealView.setInt(R.id.widget_normal_price, "setPaintFlags",
              Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

            // Añadir esta franja de precio terminada al ViewFlipper principal
            views.addView(R.id.widget_deals_flipper, dealView);
          }
        }

        // 4. Cargar imagen de fondo en un hilo secundario (Async)
        new Thread(() -> {
          try {
            URL url = new URL(thumbUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            views.setImageViewBitmap(R.id.widget_image, bmp);
            // Actualizar el widget de nuevo una vez cargada la imagen
            appWidgetManager.updateAppWidget(appWidgetId, views);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }).start();

      } else {
        // 🔴 NO HAY FAVORITO: Ocultamos el juego y mostramos el mensaje vacío
        views.setViewVisibility(R.id.widget_game_content, View.GONE);
        views.setViewVisibility(R.id.widget_empty_state, View.VISIBLE);
      }
    } catch (Exception e) {
      // Si ocurre un error al leer el JSON
      views.setViewVisibility(R.id.widget_game_content, View.GONE);
      views.setViewVisibility(R.id.widget_empty_state, View.VISIBLE);
      e.printStackTrace();
    }

    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  // ¡El método getStoreName() fue completamente eliminado porque ya no se necesita! 🧹
}
