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

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class HelperProvider {
    public static BitmapDescriptor BitmapFromVector(int vectorResId, boolean primary, Context ctx) {
        Drawable vectorDrawable = ContextCompat.getDrawable(ctx, vectorResId);

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        TypedValue typedValue = new TypedValue();
        if (primary) {
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        } else {
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        }

        int color = ContextCompat.getColor(ctx, typedValue.resourceId);
        vectorDrawable.setTint(color);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(addShadow(bitmap, bitmap.getHeight(), bitmap.getWidth(), Color.BLACK, 3, 1, 3));
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

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        String OnPrimaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        String PrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true);
        //String OnPrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));





        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        String SecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        String OnSecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        String SecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true);
        //String OnSecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));




        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);
        //String TertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnTertiary, typedValue, true);
        //String OnTertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiaryContainer, typedValue, true);
        String TertiaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, typedValue.resourceId)));

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
                "        \"color\": \"" + TertiaryContainerColor + "\"\n" +
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
                "        \"color\": \"" + PrimaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.arterial\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + OnSecondaryColor + "\"\n" +
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
                "        \"color\": \"" + OnPrimaryColor + "\"\n" +
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
                "        \"color\": \"#2f3948\"\n" +
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
                "        \"color\": \"#17263c\"\n" +
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
