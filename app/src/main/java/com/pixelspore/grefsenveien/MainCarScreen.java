package com.pixelspore.grefsenveien;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.AppManager;
import androidx.car.app.SurfaceCallback;
import androidx.car.app.SurfaceContainer;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.navigation.model.NavigationTemplate;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import androidx.car.app.model.Template;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.car.app.CarToast;

import androidx.core.graphics.drawable.IconCompat;
import androidx.car.app.model.CarIcon;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainCarScreen extends Screen implements SurfaceCallback {

    private String garageStatus = "";
    private String gateStatus = "";
    private Bitmap cameraBitmap = null;
    private String imageTimestamp = "";
    private SurfaceContainer mSurfaceContainer;
    private Rect mVisibleArea;

    private final android.os.Handler mUpdateHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable mImageUpdater = new Runnable() {
        @Override
        public void run() {
            fetchImageFromUrl(BuildConfig.S3_IMAGE_URL);
            mUpdateHandler.postDelayed(this, 60000); // 60 sekunder
        }
    };

    public MainCarScreen(@NonNull CarContext carContext) {
        super(carContext);
        
        // Registrer oss for å få tilgang til tegne-lerretet i Android Auto
        carContext.getCarService(AppManager.class).setSurfaceCallback(this);
        
    }

    @Override
    public void onSurfaceAvailable(@NonNull SurfaceContainer surfaceContainer) {
        mSurfaceContainer = surfaceContainer;
        drawCameraImage();
        
        // Start automatisk oppdatering hvert minutt
        mUpdateHandler.removeCallbacks(mImageUpdater);
        mUpdateHandler.post(mImageUpdater);
    }

    @Override
    public void onVisibleAreaChanged(@NonNull Rect visibleArea) {
        mVisibleArea = visibleArea;
        drawCameraImage();
    }

    @Override
    public void onSurfaceDestroyed(@NonNull SurfaceContainer surfaceContainer) {
        mSurfaceContainer = null;
        
        // Stopp automatisk oppdatering når tegneflaten forsvinner
        mUpdateHandler.removeCallbacks(mImageUpdater);
    }

    private void drawCameraImage() {
        if (mSurfaceContainer == null || mSurfaceContainer.getSurface() == null || cameraBitmap == null) {
            return;
        }

        try {
            Canvas canvas = mSurfaceContainer.getSurface().lockCanvas(null);
            if (canvas != null) {
                // Tegn sort bakgrunn først
                canvas.drawColor(android.graphics.Color.BLACK);

                // Beregn posisjon i henhold til synlig område, slik at app-menyer ikke dekker bildet
                int drawWidth = mVisibleArea != null ? mVisibleArea.width() : canvas.getWidth();
                int drawHeight = mVisibleArea != null ? mVisibleArea.height() : canvas.getHeight();
                int offsetX = mVisibleArea != null ? mVisibleArea.left : 0;
                int offsetY = mVisibleArea != null ? mVisibleArea.top : 0;
                
                // Endret fra Math.max til ren bredde-skalering for å unngå at vi kutter sidene
                float scale = (float) drawWidth / cameraBitmap.getWidth();
                
                int scaledWidth = Math.round(cameraBitmap.getWidth() * scale);
                int scaledHeight = Math.round(cameraBitmap.getHeight() * scale);
                
                // Siden vi matcher bredden 100% av det synlige området
                int left = offsetX;
                // Sentrer i høyden i det synlige området
                int top = offsetY + (drawHeight - scaledHeight) / 2;

                Rect destRect = new Rect(left, top, left + scaledWidth, top + scaledHeight);
                canvas.drawBitmap(cameraBitmap, null, destRect, new Paint());

                // Tegn tidsstempel nede i høyre hjørne av den SYNLIGE skjermen hvis vi har det
                if (imageTimestamp != null && !imageTimestamp.isEmpty()) {
                    Paint textPaint = new Paint();
                    textPaint.setColor(android.graphics.Color.WHITE);
                    // Redusert tekststørrelse
                    textPaint.setTextSize(26f);
                    textPaint.setAntiAlias(true);

                    Paint bgPaint = new Paint();
                    bgPaint.setColor(android.graphics.Color.BLACK);
                    // Gjør bakgrunnen lett transparent (valgfritt, 200/255 alpha)
                    bgPaint.setAlpha(200);

                    // Beregn tekstens bredde og høyde
                    Rect textBounds = new Rect();
                    textPaint.getTextBounds(imageTimestamp, 0, imageTimestamp.length(), textBounds);
                    
                    int textPadding = 16;
                    int edgeMargin = 40; // Avstand fra hjørnet av skjermen
                    int textWidth = textBounds.width();
                    int textHeight = textBounds.height();

                    // Plasser nede til høyre i det synlige området
                    int rectLeft = offsetX + drawWidth - textWidth - (textPadding * 2) - edgeMargin;
                    int rectTop = offsetY + drawHeight - textHeight - (textPadding * 2) - edgeMargin;
                    int rectRight = offsetX + drawWidth - edgeMargin;
                    int rectBottom = offsetY + drawHeight - edgeMargin;

                    // Tegn svart boks
                    canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, bgPaint);
                    
                    // Tegn hvit tekst inni boksen (Y er baseline for teksten)
                    canvas.drawText(imageTimestamp, rectLeft + textPadding, rectBottom - textPadding, textPaint);
                }

                mSurfaceContainer.getSurface().unlockCanvasAndPost(canvas);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        ActionStrip actionStrip = new ActionStrip.Builder()
            .addAction(
                new Action.Builder()
                    .setTitle("Garasje")
                    .setFlags(Action.FLAG_IS_PERSISTENT)
                    .setIcon(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_material_garage_door)).build())
                    .setBackgroundColor(CarColor.DEFAULT)
                    .setOnClickListener(() -> makeUrlRequest("garasjen"))
                    .build())
            .addAction(
                new Action.Builder()
                    .setTitle("Port")
                    .setFlags(Action.FLAG_IS_PERSISTENT)
                    .setIcon(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_material_outdoor_garden)).build())
                    .setBackgroundColor(CarColor.DEFAULT)
                    .setOnClickListener(() -> makeUrlRequest("porten"))
                    .build())
            .addAction(
                new Action.Builder()
                    .setTitle("Oppdater")
                    .setFlags(Action.FLAG_IS_PERSISTENT)
                    .setBackgroundColor(CarColor.DEFAULT)
                    .setOnClickListener(() -> {
                        CarToast.makeText(getCarContext(), "Henter nytt bilde...", CarToast.LENGTH_SHORT).show();
                        // Omstart timeren for å unngå dobbelhenting rett etter manuelt klikk
                        mUpdateHandler.removeCallbacks(mImageUpdater);
                        mUpdateHandler.post(mImageUpdater);
                    })
                    .build())
            .build();

        return new NavigationTemplate.Builder()
            .setActionStrip(actionStrip)
            .build();
    }

    private void fetchImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                // Legg på et unikt tidsstempel for å tvinge Nabu Casa / proxyer til å gi oss et ferskt bilde (cache-busting)
                String urlWithTimestamp = imageUrl + (imageUrl.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();
                Log.d("GrefsenveienApp", "Fetching image from: " + urlWithTimestamp);
                URL url = new URL(urlWithTimestamp);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false); // FORCING FRESH DOWNLOAD
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                Log.d("GrefsenveienApp", "Response Code: " + responseCode);

                if (responseCode == 200) {
                    // Hent tidsstempel fra headers (Last-Modified foretrekkes, ellers Date)
                    String lastMod = connection.getHeaderField("Last-Modified");
                    String dateHdr = connection.getHeaderField("Date");
                    Log.d("GrefsenveienApp", "Header Last-Modified: " + lastMod);
                    Log.d("GrefsenveienApp", "Header Date: " + dateHdr);
                    
                    String dateHeader = lastMod;
                    if (dateHeader == null) {
                        dateHeader = dateHdr;
                    }
                    
                    // Forenkle datoformatet for visning (valgfritt)
                    String finalTimestampStr = "Ukjent tid";
                    if (dateHeader != null) {
                        try {
                            // Standard HTTP-datoformat er "EEE, dd MMM yyyy HH:mm:ss zzz"
                            SimpleDateFormat httpFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                            Date parsedDate = httpFormat.parse(dateHeader);
                            if (parsedDate != null) {
                                SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                                finalTimestampStr = displayFormat.format(parsedDate);
                            } else {
                                finalTimestampStr = dateHeader;
                            }
                        } catch (Exception e) {
                            Log.e("GrefsenveienApp", "Failed to parse date header: " + dateHeader, e);
                            // Hvis parsing feiler, bruk råverdien
                            finalTimestampStr = dateHeader;
                        }
                    } else {
                        Log.w("GrefsenveienApp", "No date headers found in HTTP response");
                    }
                    Log.d("GrefsenveienApp", "Final imageTimestamp string: " + finalTimestampStr);

                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap != null) {
                        // Sørg for at vi er på UI-tråden før vi bytter ut variablene som Canvasen tegner
                        final String newTimestamp = finalTimestampStr;

                        getCarContext().getMainExecutor().execute(() -> {
                            cameraBitmap = bitmap;
                            imageTimestamp = newTimestamp;
                            drawCameraImage();
                            invalidate();
                        });
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void makeUrlRequest(String targetName) {
        // Vis en umiddelbar beskjed om at vi prøver å sende signalet
        String statusText;
        if (targetName.equals("garasjen")) {
            statusText = "Åpner/lukker Garasje...";
        } else {
            statusText = "Åpner/lukker Port...";
        }
        CarToast.makeText(getCarContext(), statusText, CarToast.LENGTH_SHORT).show();

        // Make request in background thread
        new Thread(() -> {
            try {
                URL url;
                if (targetName.equals("garasjen")) {
                    url = new URL(BuildConfig.GARAGE_WEBHOOK_URL);
                } else {
                    url = new URL(BuildConfig.GATE_WEBHOOK_URL);
                }
                
                android.util.Log.i("GrefsenveienApp", "MainCarScreen -> Calling webhook: " + url.toString());
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                // Get response code 
                int responseCode = connection.getResponseCode();

                connection.disconnect();

                // Update UI on main thread with result
                getCarContext().getMainExecutor().execute(() -> {
                    String resultMsg;
                    if (responseCode == 200) {
                        resultMsg = "Vellykket";
                    } else {
                        resultMsg = "Feilkode " + responseCode + " (" + targetName + ")";
                    }
                    CarToast.makeText(getCarContext(), resultMsg, CarToast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                getCarContext().getMainExecutor().execute(() -> {
                    String errorMsg = "Nettverksfeil for " + targetName;
                    CarToast.makeText(getCarContext(), errorMsg, CarToast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
