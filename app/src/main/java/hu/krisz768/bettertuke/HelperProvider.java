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

import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

public class HelperProvider {

    private static Bitmap[] BitmapContainer = new Bitmap[16];

    public static void RenderAllBitmap(Context ctx) {
        //map
        BitmapContainer[0] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_stop1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_stop, com.google.android.material.R.attr.colorPrimary, ctx, true));
        BitmapContainer[1] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_stop1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_stop, com.google.android.material.R.attr.colorOutline, ctx, true));
        BitmapContainer[2] = HelperProvider.BitmapFromVector(R.drawable.bus_marker_small, com.google.android.material.R.attr.colorOutline, ctx, true);
        BitmapContainer[3] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus_place_pointer1, com.google.android.material.R.attr.colorOnPrimary, ctx, false), HelperProvider.BitmapFromVector(R.drawable.bus_place_pointer, com.google.android.material.R.attr.colorOutline, ctx, true));
        BitmapContainer[4] = overlay(HelperProvider.BitmapFromVector(R.drawable.bus1, com.google.android.material.R.attr.colorPrimary, ctx, true),HelperProvider.BitmapFromVector(R.drawable.bus, com.google.android.material.R.attr.colorOnPrimary, ctx, false));
        //Track
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
    }

    public static Bitmap getBitmap(Bitmaps bitmap) {
        switch (bitmap){
            case MapStopSelected:
                return BitmapContainer[0];
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
        }

        return null;
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
        TrackEndFull

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

    private static Bitmap BitmapFromVector(int vectorResId, int ColorResid, Context ctx, boolean shadow) {
        Drawable vectorDrawable = ContextCompat.getDrawable(ctx, vectorResId);

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(ColorResid, typedValue, true);

        int color = ContextCompat.getColor(ctx, typedValue.resourceId);

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

        //ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        //String OnPrimaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        String PrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true);
        //String OnPrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));





        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        String SecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        //String OnSecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        String SecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true);
        //String OnSecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));




        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);
        //String TertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnTertiary, typedValue, true);
        //String OnTertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiaryContainer, typedValue, true);
        //String TertiaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnTertiaryContainer, typedValue, true);
        //String OnTertiaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
        //String Background = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true);
        //String OnBackground = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        String Surface = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        //String OnSurface = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceInverse, typedValue, true);
        String OnSurfaceVariant = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int waterColor = MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#3299a8"));
        String WaterColor = String.format("#%06X", (0xFFFFFF & waterColor));

        int railColor = MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#8d8e8f"));
        String RailColor = String.format("#%06X", (0xFFFFFF & railColor));

        int parkColor = MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#247802"));
        String ParkColor = String.format("#%06X", (0xFFFFFF & parkColor));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

        int highwayStroke = MaterialColors.getColorRoles(ctx,ContextCompat.getColor(ctx, typedValue.resourceId)).getOnAccent();
        String HighwayStroke = String.format("#%06X", (0xFFFFFF & highwayStroke));


        int TextStroke = 0;

        int nightModeFlags =
                ctx.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                TextStroke = -100;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                TextStroke = 0;
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                TextStroke = 0;
                break;
        }


        String JSON = "[\n" +
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
                "        \"color\": \"" + ParkColor + "\"\n" +
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
                "        \"color\": \"#d59563\"\n" +
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


        return JSON;
    }
}
