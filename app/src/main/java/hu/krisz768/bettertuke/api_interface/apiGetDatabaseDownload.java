package hu.krisz768.bettertuke.api_interface;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

public class apiGetDatabaseDownload implements Runnable {

    private final Context ctx;
    private final String DATABASEFILE;
    protected final String API_URL = "http://menobusz.tukebusz.hu/";
    public boolean RetCode;

    public  apiGetDatabaseDownload(Context ctx) {
        this.ctx = ctx;
        DATABASEFILE = (new File(ctx.getFilesDir() + "/Database", "track.db")).getAbsolutePath();
    }

    @Override
    public void run() {
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(API_URL + "android/db/track.zip").openStream());

            //download
            File TempFile = File.createTempFile("download", "database", ctx.getCacheDir());
            TempFile.deleteOnExit();
            FileOutputStream fileOutputStream = new FileOutputStream(TempFile);
            try {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                fileOutputStream.close();
            } finally {
                fileOutputStream.close();
                in.close();
            }

            //copy and unzip temp->data

            File DatabaseFile = new File(DATABASEFILE);
            File ParentDir = DatabaseFile.getParentFile();

            boolean SuccessfullyCreate = false;

            if (ParentDir != null) {
                SuccessfullyCreate = ParentDir.mkdirs();
            }

            if (!SuccessfullyCreate) {
                RetCode = false;
                return;
            }

            SuccessfullyCreate = DatabaseFile.createNewFile();

            if (!SuccessfullyCreate) {
                RetCode = false;
                return;
            }

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(TempFile))) {
                try (OutputStream out = new FileOutputStream(DatabaseFile)) {
                    int count;
                    byte[] buf = new byte[1024];
                    while ((count = zis.read(buf)) != -1) {
                        out.write(buf, 0, count);
                    }
                }
            }

            RetCode = true;
        } catch (Exception e) {
            log("Error :(" + e);
            RetCode = false;
        }

    }

    private void log (String msg) {
        Log.e("ApiInterface", msg);
    }
}
