package hu.krisz768.bettertuke.Gtfs;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class GTFSDatabaseDownload implements Runnable {
    private final Context ctx;
    private final String DATABASEFILE;
    public boolean RetCode;

    private OnProgressChange onProgressChange;

    public GTFSDatabaseDownload(Context ctx, OnProgressChange onProgressChange) {
        this.ctx = ctx;
        this.onProgressChange = onProgressChange;
        DATABASEFILE = (new File(ctx.getFilesDir() + "/Database", "gtfs.db")).getAbsolutePath();
    }

    public interface OnProgressChange {
        void onNewProgress(int Step);
    }

    @Override
    public void run() {
        try {
            int StepCounter = 1;

            BufferedInputStream in = new BufferedInputStream(new URL("https://mobilitas.biokom.hu/gtfs/").openStream());

            File tempFile = File.createTempFile("download", "gtfsdatabase", ctx.getCacheDir());
            tempFile.deleteOnExit();
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
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

            if (onProgressChange != null) {
                onProgressChange.onNewProgress(StepCounter);
                StepCounter++;
            }

            GTFSDatabase.DeleteDatabase(ctx);
            GTFSDatabase db = new GTFSDatabase(ctx);

            FileInputStream fin = new FileInputStream(tempFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().toString().equals("routes.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertRoutes(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("calendar.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertCalendar(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("calendar_dates.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertCalendarDates(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("shapes.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertShapes(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("stop_times.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertStopTimes(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("stops.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertStops(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("transfers.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertTransfers(strUnzipped);
                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }
                if (ze.getName().toString().equals("trips.txt")) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        result.write(buf, 0, count);
                    }
                    String strUnzipped = result.toString("UTF-8");
                    db.InsertTrips(strUnzipped);

                    if (onProgressChange != null) {
                        onProgressChange.onNewProgress(StepCounter);
                        StepCounter++;
                    }
                }

                zin.closeEntry();
            }
            zin.close();




            //Process


            tempFile.delete();

            GTFSContentLength contentLength = new GTFSContentLength();
            Thread thread = new Thread(contentLength);
            thread.start();
            thread.join();
            String Online = contentLength.ContentLength;

            UserDatabase userDatabase = new UserDatabase(ctx);
            userDatabase.SetPreference("GTFS-cl",Online);

            RetCode = true;
        } catch (Exception e) {
            log(e.toString());
            RetCode = false;
        }

    }

    private void log (String msg) {
        Log.e("GTFSDownloader", msg);
    }
}
