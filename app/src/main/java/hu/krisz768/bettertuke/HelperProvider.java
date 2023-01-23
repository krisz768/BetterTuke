package hu.krisz768.bettertuke;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import hu.krisz768.bettertuke.models.BusAttributes;

public class HelperProvider {
    private static final Bitmap[] BitmapContainer = new Bitmap[28];
    private static JSONObject BusAttributes;

    private static boolean IsOfflineTextDisplayed = false;

    public static boolean displayOfflineText() {
        return !IsOfflineTextDisplayed;
    }

    public static void setOfflineTextDisplayed() {
        IsOfflineTextDisplayed = true;
    }

    public static void RenderAllBitmap(Context ctx) {
        BitmapContainer[0] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_stop1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_stop, com.google.android.material.R.attr.colorPrimary, ctx, true));
        BitmapContainer[1] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_stop1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_stop, com.google.android.material.R.attr.colorOutline, ctx, true));
        BitmapContainer[2] = HelperProvider.BitmapFromVector(R.drawable.bus_marker_small, com.google.android.material.R.attr.colorOutline, ctx, true);
        BitmapContainer[3] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_place_pointer1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_place_pointer, com.google.android.material.R.attr.colorOutline, ctx, true));
        BitmapContainer[4] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus1, com.google.android.material.R.attr.colorPrimary, ctx, true),HelperProvider.BitmapFromVector(R.drawable.bus_small, com.google.android.material.R.attr.colorOnPrimary, ctx, false));

        BitmapContainer[5] = HelperProvider.BitmapFromVector(R.drawable.bustrack_start_end, com.google.android.material.R.attr.colorOutline, ctx, false);
        BitmapContainer[6] = overlay(HelperProvider.BitmapFromVector(R.drawable.bustrack_starthalf_1, com.google.android.material.R.attr.colorPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bustrack_half_1, com.google.android.material.R.attr.colorOutline, ctx, false));
        BitmapContainer[7] = HelperProvider.BitmapFromVector(R.drawable.bustrack_startfull, com.google.android.material.R.attr.colorPrimary, ctx, false);

        BitmapContainer[8] = HelperProvider.BitmapFromVector(R.drawable.bustrack_empty, com.google.android.material.R.attr.colorOutline, ctx, false);
        BitmapContainer[9] = overlay(HelperProvider.BitmapFromVector(R.drawable.bustrack_half, com.google.android.material.R.attr.colorPrimary, ctx, false),HelperProvider.BitmapFromVector(R.drawable.bustrack_half_1, com.google.android.material.R.attr.colorOutline, ctx, false));
        BitmapContainer[10] = HelperProvider.BitmapFromVector(R.drawable.bustrack_full, com.google.android.material.R.attr.colorPrimary, ctx, false);
        BitmapContainer[11] = overlay(HelperProvider.BitmapFromVector(R.drawable.bustrack_start_end, com.google.android.material.R.attr.colorOutline, ctx, false),HelperProvider.BitmapFromVector(R.drawable.bustrack_empty_incom, com.google.android.material.R.attr.colorPrimary, ctx, false));

        BitmapContainer[12] = createFlippedBitmap(HelperProvider.BitmapFromVector(R.drawable.bustrack_start_end, com.google.android.material.R.attr.colorOutline, ctx, false), false, true);
        BitmapContainer[13] = overlay(HelperProvider.BitmapFromVector(R.drawable.bustrack_end_inc, com.google.android.material.R.attr.colorOutline, ctx, false),HelperProvider.BitmapFromVector(R.drawable.bustrack_empty_incom, com.google.android.material.R.attr.colorPrimary, ctx, false));
        BitmapContainer[14] = HelperProvider.BitmapFromVector(R.drawable.bustrack_half, com.google.android.material.R.attr.colorPrimary, ctx, false);
        BitmapContainer[15] = HelperProvider.BitmapFromVector(R.drawable.bustrack_full, com.google.android.material.R.attr.colorPrimary, ctx, false);

        BitmapContainer[16] = HelperProvider.BitmapFromVector(R.drawable.electric,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[17] = HelperProvider.BitmapFromVector(R.drawable.lowfloor,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[18] = HelperProvider.BitmapFromVector(R.drawable.airconditioner,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[19] = HelperProvider.BitmapFromVector(R.drawable.wifi,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[20] = HelperProvider.BitmapFromVector(R.drawable.usb,com.google.android.material.R.attr.colorPrimary,ctx,false);

        BitmapContainer[21] = HelperProvider.BitmapFromVector(R.drawable.favicon_01,com.google.android.material.R.attr.colorOutline,ctx,false);
        BitmapContainer[22] = HelperProvider.BitmapFromVector(R.drawable.favicon_full_01,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[23] = overlay(HelperProvider.BitmapFromVector(R.drawable.directionforwardarrow_01,com.google.android.material.R.attr.colorPrimary,ctx,false),HelperProvider.BitmapFromVector(R.drawable.directionbackwardsarrow_01,com.google.android.material.R.attr.colorOutline,ctx,false));
        BitmapContainer[24] = overlay(HelperProvider.BitmapFromVector(R.drawable.directionforwardarrow_01,com.google.android.material.R.attr.colorOutline,ctx,false),HelperProvider.BitmapFromVector(R.drawable.directionbackwardsarrow_01,com.google.android.material.R.attr.colorPrimary,ctx,false));
        BitmapContainer[25] = HelperProvider.BitmapFromVector(R.drawable.direction1wayarrow,com.google.android.material.R.attr.colorPrimary,ctx,false);
        BitmapContainer[26] = HelperProvider.BitmapFromVector(R.drawable.location_pinpoint,com.google.android.material.R.attr.colorPrimary,ctx,true);
        BitmapContainer[27] = HelperProvider.BitmapFromVector(R.drawable.navigation,com.google.android.material.R.attr.colorPrimary,ctx,false);
    }

    @NonNull
    public static Bitmap getBitmap(Bitmaps bitmap) {
        switch (bitmap){
            case MapStopNotSelected:
                return BitmapContainer[1];
            case MapPlace:
                return BitmapContainer[2];
            case MapSmallPlace:
                return BitmapContainer[3];
            case MapBus:
                return BitmapContainer[4];
            case TrackStartEmpty:
                return BitmapContainer[5];
            case TrackStartHalf:
                return BitmapContainer[6];
            case TrackStartFull:
                return BitmapContainer[7];
            case TrackNormalEmpty:
                return BitmapContainer[8];
            case TrackNormalHalf:
                return BitmapContainer[9];
            case TrackNormalFull:
                return BitmapContainer[10];
            case TrackNormalInc:
                return BitmapContainer[11];
            case TrackEndEmpty:
                return BitmapContainer[12];
            case TrackEndInc:
                return BitmapContainer[13];
            case TrackEndHalf:
                return BitmapContainer[14];
            case TrackEndFull:
                return BitmapContainer[15];
            case Electric:
                return BitmapContainer[16];
            case LowFloor:
                return BitmapContainer[17];
            case AirConditioner:
                return BitmapContainer[18];
            case Wifi:
                return BitmapContainer[19];
            case Usb:
                return BitmapContainer[20];
            case FaviconOff:
                return BitmapContainer[21];
            case FaviconOn:
                return BitmapContainer[22];
            case DirectionForward:
                return BitmapContainer[23];
            case DirectionBackwards:
                return BitmapContainer[24];
            case DirectionOneWay:
                return BitmapContainer[25];
            case LocationPin:
                return BitmapContainer[26];
            case Navigation:
                return BitmapContainer[27];
            case MapStopSelected:
            default:
                return BitmapContainer[0];
        }
    }

    public enum Bitmaps {
        MapStopSelected,
        MapStopNotSelected,
        MapPlace,
        MapSmallPlace,
        MapBus,
        TrackStartEmpty,
        TrackStartHalf,
        TrackStartFull,
        TrackNormalEmpty,
        TrackNormalHalf,
        TrackNormalFull,
        TrackNormalInc,
        TrackEndEmpty,
        TrackEndInc,
        TrackEndHalf,
        TrackEndFull,
        Electric,
        LowFloor,
        AirConditioner,
        Wifi,
        Usb,
        FaviconOff,
        FaviconOn,
        DirectionForward,
        DirectionBackwards,
        DirectionOneWay,
        LocationPin,
        Navigation
    }

    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        bmp1.recycle();
        bmp2.recycle();
        return bmOverlay;
    }

    private static Bitmap BitmapFromVector(int vectorResId, int ColorResId, Context ctx, boolean shadow) {
        Drawable vectorDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ctx, vectorResId));

        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(ColorResId, typedValue, true);

        int color = ContextCompat.getColor(ctx, typedValue.resourceId);

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        vectorDrawable.setTint(color);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        if (shadow) {
            return addShadow(bitmap, bitmap.getHeight(), bitmap.getWidth(), Color.BLACK, 10, 3, 5);
        } else {
            return bitmap;
        }
    }

    public static Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap addShadow(final Bitmap bm, final int dstHeight, final int dstWidth, int color, int size, float dx, float dy) {
        final Bitmap mask = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ALPHA_8);

        final Matrix scaleToFit = new Matrix();
        final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
        final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
        scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        final Matrix dropShadow = new Matrix(scaleToFit);
        dropShadow.postTranslate(dx, dy);

        final Canvas maskCanvas = new Canvas(mask);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskCanvas.drawBitmap(bm, scaleToFit, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        maskCanvas.drawBitmap(bm, dropShadow, paint);

        final BlurMaskFilter filter = new BlurMaskFilter(size, BlurMaskFilter.Blur.NORMAL);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setMaskFilter(filter);
        paint.setFilterBitmap(true);

        final Bitmap ret = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        final Canvas retCanvas = new Canvas(ret);
        retCanvas.drawBitmap(mask, 0,  0, paint);
        retCanvas.drawBitmap(bm, scaleToFit, null);
        mask.recycle();
        return ret;
    }

    public static String GetMapTheme(Context ctx) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        String PrimaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        String PrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        String SecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        String SecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        String Surface = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceInverse, typedValue, true);
        String OnSurfaceVariant = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int waterColor = MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#3299a8"));
        String WaterColor = String.format("#%06X", (0xFFFFFF & waterColor));

        int railColor = MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#8d8e8f"));
        String RailColor = String.format("#%06X", (0xFFFFFF & railColor));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

        int highwayStroke = MaterialColors.getColorRoles(ctx,ContextCompat.getColor(ctx, typedValue.resourceId)).getOnAccent();
        String HighwayStroke = String.format("#%06X", (0xFFFFFF & highwayStroke));

        int TextStroke = 0;

        int nightModeFlags =
                ctx.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            TextStroke = -100;
        }

        return "[\n" +
                "  {\n" +
                "    \"featureType\": \"all\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + Surface  + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"all\",\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"lightness\": " + TextStroke + "\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative.locality\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + OnSurfaceVariant + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + Surface + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#6b9a76\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.arterial\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.arterial\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + HighwayStroke + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.local\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.local\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + RailColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit.station\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + WaterColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#515c6d\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"lightness\": -20\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";
    }

    public static hu.krisz768.bettertuke.models.BusAttributes getBusAttributes(Context ctx,String PlateNumber) {
        hu.krisz768.bettertuke.models.BusAttributes oneBusAttributes = new BusAttributes(PlateNumber,"",0,-1,0,-1,0,0,0);
        if (BusAttributes == null) {
            InputStream inputStream = ctx.getResources().openRawResource(R.raw.buses);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder text = new StringBuilder();
            while (true) {
                try {
                    String temp = br.readLine();
                    if (temp == null)
                        break;
                    else
                        text.append(temp);

                } catch (Exception e) {
                    return oneBusAttributes;
                }
            }
            try {
                BusAttributes = new JSONObject(text.toString());
            } catch (JSONException e) {
                return oneBusAttributes;
            }
        }
        int i = 0;
        try {
            while(i<BusAttributes.getJSONArray("buses").length()) {
                if(BusAttributes.getJSONArray("buses").getJSONObject(i).getString("platenumber").equals(PlateNumber))
                    break;
                i++;
            }
            if(i==BusAttributes.getJSONArray("buses").length()) {
                return oneBusAttributes;
            } else {
                oneBusAttributes=new BusAttributes(PlateNumber,
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getString("type"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("propulsion"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("articulated"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("lowfloor"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("doors"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("airconditioner"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("wifi"),
                        BusAttributes.getJSONArray("buses").getJSONObject(i).getInt("usb"));
            }
        } catch (JSONException e) {
            return oneBusAttributes;
        }
        return oneBusAttributes;
    }
}
