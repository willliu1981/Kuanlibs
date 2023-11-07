package idv.kuan.kuanandroidlibs.databases.provider;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import idv.kuan.libs.databases.BaseDBFactory;


public class AndroidSQLiteFactory extends BaseDBFactory {
    public static String TargetDBName = "test.db";
    private static final String DBNameInAssets = "test.db";

    private Context context;


    public AndroidSQLiteFactory(Context context) {
        this.context = context;
    }

    @Override
    public Connection getConnection(String commands[]) {
        /*
        command1 是Assets的DB name;
            省略 command[0] 將以DBNameInAssets 為值於command1
        command2 是android 手機中的DB name
            省略 command[1] 將以DefaultSuffixUrl 為值於command2
         */

        String command1 = "";
        String command2 = "";


        if (commands == null || !(commands.length >= 2)) {
            //擇日再改丟出Exception
            return null;
        }

        if (commands[0] == null || commands[0].equals("")) {
            command1 = DBNameInAssets;
        } else {
            command1 = commands[0] + ".db";
        }

        if (commands[1] == null || commands[1].equals("")) {
            command2 = TargetDBName;
        } else {
            command2 = commands[1] + ".db";
        }

        String url = "jdbc:sqldroid:" + copyDatabaseFromAssets(context, command1, command2);

        try {
            Class.forName("org.sqldroid.SQLDroidDriver");
            return DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            showToast("conn error:");
            System.out.println("dbg ADBF: conn error:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String copyDatabaseFromAssets(Context context, String sourceDBName, String targetDBName) {
        String targetFileStrPath = context.getFilesDir().getPath();
        File file = context.getFileStreamPath(targetDBName);
        if (!file.exists()) {
            showToast("start copy db...");
            try {
                copyAssetsFileTo(context, sourceDBName, targetFileStrPath, targetDBName);
                showToast("copy db finish");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Toast.makeText(context, "db is exists:", Toast.LENGTH_SHORT).show();
            //System.out.println("xxx ADBConn :db is exists:"+file.getAbsolutePath());
        }
        return file.getAbsolutePath();

    }

    private void copyAssetsFileTo(Context context, String fromAssetsFilename, String targetPath, String toFileName) throws IOException {

        OutputStream myOutput = new FileOutputStream(targetPath + "/" + toFileName);
        byte[] buffer = new byte[1024];
        int length;
        InputStream myInput = context.getAssets().open(fromAssetsFilename);
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();

    }

    private void showToast(String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
