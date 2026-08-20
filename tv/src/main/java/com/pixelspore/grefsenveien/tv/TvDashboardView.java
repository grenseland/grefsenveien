package com.pixelspore.grefsenveien.tv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pixelspore.grefsenveien.DetailDashboardData;
import com.pixelspore.grefsenveien.DetailDashboardFetcher;
import com.pixelspore.grefsenveien.DetailDashboardRenderer;

/**
 * Single-screen dashboard matching Android Auto Detaljer (3 columns × 4 rows).
 */
public final class TvDashboardView extends FrameLayout {

    private static final long UPDATE_INTERVAL_MS = 60_000L;
    private static final int BG_COLOR = Color.parseColor("#0B0E14");
    private static final int GAP_DP = 8;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            requestData(true);
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Nullable private DetailDashboardData data;
    private boolean fetchInProgress;
    private boolean fetchFailed;

    private GridLayout grid;
    private View loadingView;

    private WidgetPanel panelOutdoorTemp;
    private WidgetPanel panelRain12w;
    private WidgetPanel panelNow;
    private WidgetPanel panelLightning;
    private WidgetPanel panelTempMinMax60d;
    private WidgetPanel panelSoil;
    private WidgetPanel panelWeatherCam;
    private WidgetPanel panelYardCam;
    private WidgetPanel panelRoomGrid;
    private WidgetPanel panelSunPath;
    private WidgetPanel panelMailboxCam;

    public TvDashboardView(Context context) {
        super(context);
        init(context);
    }

    public TvDashboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TvDashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(BG_COLOR);
        // Keep titles/chrome subordinate to charts and camera frames on large TV cells.
        DetailDashboardRenderer.setUiScaleMultiplier(0.72f);
        int gap = dp(context, GAP_DP);

        grid = new GridLayout(context);
        grid.setColumnCount(3);
        grid.setRowCount(4);
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setPadding(0, 0, 0, 0);
        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Row 0
        panelOutdoorTemp = addCell(context, WidgetType.OUTDOOR_TEMP, 0, 0, 1, 1, gap);
        panelRain12w = addCell(context, WidgetType.RAIN_12W, 0, 1, 1, 1, gap);
        panelNow = addCell(context, WidgetType.NOW, 0, 2, 1, 1, gap);
        // Row 1
        panelLightning = addCell(context, WidgetType.LIGHTNING, 1, 0, 1, 1, gap);
        panelTempMinMax60d = addCell(context, WidgetType.TEMP_MINMAX_60D, 1, 1, 1, 1, gap);
        panelSoil = addCell(context, WidgetType.SOIL, 1, 2, 1, 1, gap);
        // Row 2 — weather spans 2 cols
        panelWeatherCam = addCell(context, WidgetType.WEATHER_CAM, 2, 0, 1, 2, gap);
        panelYardCam = addCell(context, WidgetType.YARD_CAM, 2, 2, 1, 1, gap);
        // Row 3
        panelRoomGrid = addCell(context, WidgetType.ROOM_GRID, 3, 0, 1, 1, gap);
        panelSunPath = addCell(context, WidgetType.SUN_PATH, 3, 1, 1, 1, gap);
        panelMailboxCam = addCell(context, WidgetType.MAILBOX_CAM, 3, 2, 1, 1, gap);

        loadingView = new LoadingOverlay(context);
        loadingView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(grid);
        addView(loadingView);
        setContentVisible(false);
    }

    private WidgetPanel addCell(Context context, WidgetType type, int row, int col,
                                int rowSpan, int colSpan, int gap) {
        WidgetPanel panel = new WidgetPanel(context, type);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(row, rowSpan, 1f),
                GridLayout.spec(col, colSpan, 1f));
        lp.width = 0;
        lp.height = 0;
        lp.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
        panel.setLayoutParams(lp);
        grid.addView(panel);
        return panel;
    }

    private void setContentVisible(boolean visible) {
        grid.setVisibility(visible ? VISIBLE : GONE);
        loadingView.setVisibility(visible ? GONE : VISIBLE);
    }

    public void startUpdating() {
        handler.removeCallbacks(updater);
        requestData(false);
        handler.post(updater);
    }

    public void stopUpdating() {
        handler.removeCallbacks(updater);
    }

    private void requestData(boolean forceRefresh) {
        if (fetchInProgress) return;
        if (!forceRefresh && data != null) return;

        fetchInProgress = true;
        fetchFailed = false;
        if (data == null) {
            setContentVisible(false);
            loadingView.invalidate();
        }

        DetailDashboardFetcher.fetch(getContext(), new DetailDashboardFetcher.Callback() {
            @Override
            public void onDataReady(@NonNull DetailDashboardData newData) {
                handler.post(() -> applyData(newData));
            }

            @Override
            public void onError() {
                handler.post(() -> applyFetchFailed());
            }
        });
    }

    private void applyData(@NonNull DetailDashboardData newData) {
        data = newData;
        fetchInProgress = false;
        fetchFailed = false;
        setContentVisible(true);
        invalidateAll();
    }

    private void applyFetchFailed() {
        fetchInProgress = false;
        fetchFailed = true;
        if (data == null) {
            loadingView.invalidate();
        }
    }

    private void invalidateAll() {
        panelOutdoorTemp.invalidate();
        panelRain12w.invalidate();
        panelNow.invalidate();
        panelLightning.invalidate();
        panelTempMinMax60d.invalidate();
        panelSoil.invalidate();
        panelWeatherCam.invalidate();
        panelYardCam.invalidate();
        panelRoomGrid.invalidate();
        panelSunPath.invalidate();
        panelMailboxCam.invalidate();
    }

    private static int dp(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private enum WidgetType {
        OUTDOOR_TEMP, RAIN_12W, NOW, LIGHTNING, TEMP_MINMAX_60D, SOIL,
        WEATHER_CAM, YARD_CAM, ROOM_GRID, SUN_PATH, MAILBOX_CAM
    }

    private final class WidgetPanel extends View {
        private final WidgetType type;

        WidgetPanel(Context context, WidgetType type) {
            super(context);
            this.type = type;
            setFocusable(false);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            DetailDashboardData d = data;
            if (d == null) return;
            float w = getWidth();
            float h = getHeight();
            if (w <= 0f || h <= 0f) return;
            float S = DetailDashboardRenderer.widgetScale(w, h);

            switch (type) {
                case OUTDOOR_TEMP:
                    DetailDashboardRenderer.drawOutdoorTemp(canvas, d, w, h);
                    break;
                case RAIN_12W:
                    DetailDashboardRenderer.drawRain12w(canvas, d, w, h);
                    break;
                case NOW:
                    DetailDashboardRenderer.drawNow(canvas, d, w, h, getContext());
                    break;
                case LIGHTNING:
                    DetailDashboardRenderer.drawLightning(canvas, d, w, h);
                    break;
                case TEMP_MINMAX_60D:
                    DetailDashboardRenderer.drawTempMinMax60d(canvas, d, w, h);
                    break;
                case SOIL:
                    DetailDashboardRenderer.drawSoil(canvas, d, w, h);
                    break;
                case WEATHER_CAM:
                    DetailDashboardRenderer.drawCamera(canvas, d.weatherBitmap, "V\u00c6R",
                            d.weatherTimestamp, w, h, false, true, S,
                            d.weatherBatteryPercent, d.weatherBatteryMinPercent, d.weatherBatteryMaxPercent);
                    break;
                case YARD_CAM:
                    DetailDashboardRenderer.drawCamera(canvas, d.yardBitmap, "G\u00c5RDSPLASSEN",
                            d.yardTimestamp, w, h, false, true, S, -1, -1, -1);
                    break;
                case ROOM_GRID:
                    DetailDashboardRenderer.drawRoomGrid(canvas, d, w, h);
                    break;
                case SUN_PATH:
                    DetailDashboardRenderer.drawSunPath(canvas, d, w, h);
                    break;
                case MAILBOX_CAM:
                    DetailDashboardRenderer.drawCamera(canvas, d.mailboxBitmap, "POSTKASSEN",
                            d.mailboxTimestamp, w, h, false, true, S, -1, -1, -1);
                    break;
            }
        }
    }

    private final class LoadingOverlay extends View {
        LoadingOverlay(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(BG_COLOR);
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.parseColor("#8E9AA8"));
            textPaint.setTextSize(Math.max(22f, getWidth() * 0.025f));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            textPaint.setTextAlign(Paint.Align.CENTER);
            String msg = (fetchFailed && data == null)
                    ? getContext().getString(R.string.tv_load_error)
                    : getContext().getString(R.string.tv_loading);
            canvas.drawText(msg, getWidth() / 2f, getHeight() / 2f, textPaint);
        }
    }
}
