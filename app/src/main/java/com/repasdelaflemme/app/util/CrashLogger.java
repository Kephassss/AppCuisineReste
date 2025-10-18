package com.repasdelaflemme.app.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashLogger {
    private static final String TAG = "CrashLogger";
    private static final String FILE_NAME = "crash.log";

    private CrashLogger() {}

    public static void write(Context ctx, Thread t, Throwable e) {
        if (ctx == null || e == null) return;
        try {
            File dir = ctx.getFilesDir();
            if (dir == null) return;
            File out = new File(dir, FILE_NAME);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("=== Crash ===");
            pw.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()));
            pw.println("Thread: " + (t != null ? t.getName() : "unknown"));
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            try (FileWriter fw = new FileWriter(out, true)) {
                fw.write(sw.toString());
            }
        } catch (IOException io) {
            Log.e(TAG, "Failed to write crash log", io);
        }
    }
}

