package com.pixelspore.grefsenveien;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DetailDashboardData {

    public static final class DailyTempRange {
        public final int dayOfMonth;
        public final float min;
        public final float max;
        public final boolean hasData;

        public DailyTempRange(int dayOfMonth, float min, float max, boolean hasData) {
            this.dayOfMonth = dayOfMonth;
            this.min = min;
            this.max = max;
            this.hasData = hasData;
        }
    }

    public static final class LightningEvent {
        public final long timeMs;
        public final float distanceKm;

        public LightningEvent(long timeMs, float distanceKm) {
            this.timeMs = timeMs;
            this.distanceKm = distanceKm;
        }
    }

    // Outdoor temperature curves (hour-of-day vs °C)
    public List<float[]> todayTempPoints = new ArrayList<>();
    public List<float[]> yesterdayTempPoints = new ArrayList<>();
    public List<float[]> twoDaysAgoTempPoints = new ArrayList<>();
    public List<float[]> sixtyDayAvgTempPoints = new ArrayList<>();

    public float todayTempMin = 14f;
    public float todayTempMax = 16f;

    // Rain
    public final float[] rainByWeek = new float[12];
    public float todayRainMm = 0f;
    public float[] hourlyRain = new float[24];

    // Soil
    public List<float[]> soilPoints = new ArrayList<>();

    // Room temperatures
    public float valJonatan = 23.3f;
    public float valLoftsgang = 25.5f;
    public float valKontor = 26.1f;
    public float valBad = 24.1f;
    public float valVinterhage = 30.0f;
    public float valKjokken = 23.2f;
    public float valLiteBad = 23.2f;
    public float valMats = 22.1f;
    public float valStue = 25.1f;
    public float valGang3 = 23.5f;
    public float valSoverom = 22.2f;
    public float valGang4 = 22.6f;
    public float valVaskerom = 23.7f;

    // Room lights, top dot first. null means the room has no light on that slot.
    @Nullable public Boolean valStueLightInnerst = null;
    @Nullable public Boolean valStueLightDimmer = null;
    @Nullable public Boolean valBadLightSpotter = null;
    @Nullable public Boolean valBadLightTaklys = null;
    @Nullable public Boolean valKontorLight = null;
    @Nullable public Boolean valJonatanLight = null;
    @Nullable public Boolean valLoftsgangLight = null;
    @Nullable public Boolean valGang4Light = null;
    @Nullable public Boolean valVaskeromLight = null;

    // Motion detection times
    public long valStueMotionTime = 0L;
    public long valLoftsgangMotionTime = 0L;
    public long valGang4MotionTime = 0L;
    public long valJonatanMotionTime = 0L;
    public long valBadMotionTime = 0L;
    public long valVaskeromMotionTime = 0L;

    // Current weather readings
    public float valHumidity = 45f;
    public float valSolarRad = 0f;
    public float valSolarEnergy24h = 0f;
    public float valRainRate = 0f;

    // Sun
    public float sunAzimuth = 180f;
    public float sunElevation = 0f;
    public long sunNextRisingMs = 0L;
    public long sunNextSettingMs = 0L;

    // Lightning
    public List<LightningEvent> lightningEvents7d = new ArrayList<>();
    public float lastLightningDistanceKm = -1f;
    public float nearestLightningDistanceKm = -1f;
    public int lightningCount7d = 0;
    public long lightningWindowStartMs = 0L;
    public long lightningWindowEndMs = 0L;

    // Daily temp min/max (60 days)
    public List<DailyTempRange> tempMinMax60d = new ArrayList<>();
    public float temp60dPeriodMin = Float.NaN;
    public float temp60dPeriodMax = Float.NaN;

    // Camera bitmaps + timestamps
    @Nullable public Bitmap weatherBitmap = null;
    public String weatherTimestamp = "";
    @Nullable public Bitmap yardBitmap = null;
    public String yardTimestamp = "";
    @Nullable public Bitmap mailboxBitmap = null;
    public String mailboxTimestamp = "";

    // Weather camera battery
    public int weatherBatteryPercent = -1;
    public int weatherBatteryMinPercent = -1;
    public int weatherBatteryMaxPercent = -1;

    // Current outdoor temp (convenience, derived from todayTempPoints)
    public float currentOutdoorTemp = 15f;
}
