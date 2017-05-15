package in.udiboy.beet_sync;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;


public class DownloadService extends IntentService {
    private String mFilepath, mFilename, mShortFilename;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;


    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String baseUrl = intent.getStringExtra("baseUrl");
        mFilepath = intent.getStringExtra("filepath");
        mFilename = mFilepath.substring(mFilepath.lastIndexOf('/')+1);
        mFilepath = mFilepath.substring(0, mFilepath.lastIndexOf('/'));
        if(mFilename.length() > 20){
            mShortFilename = mFilename.substring(0,19)+"...";
        } else {
            mShortFilename = mFilename;
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Download")
                .setContentText("Downloading File: " + mShortFilename)
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();

        //mFilepath = Uri.encode(mFilepath, "/");
        BeetsyncInterface bsAPI = retrofit.create(BeetsyncInterface.class);
        Call<ResponseBody> downloadRequest = bsAPI.downloadSong(mFilepath+"/"+mFilename);

        try {
            Response<ResponseBody> response = downloadRequest.execute();
            if(response.isSuccessful())
                downloadFile(response.body());
            else
                Log.e("DownloadService", response.errorBody().string());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(ResponseBody body) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),mFilepath);
        File outputFile = new File(outputDir, mFilename);
        outputDir.mkdirs();
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            //Download download = new Download();
            //download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

             //   download.setCurrentFileSize((int) current);
             //   download.setProgress(progress);
                sendNotification(progress, (int) current);
                timeCount++;
            }

            output.write(data, 0, count);
        }

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(mShortFilename+" downloaded");
        notificationManager.notify(0, notificationBuilder.build());

        output.flush();
        output.close();
        bis.close();

    }

    public void sendNotification(int progress, int currentSize){
        notificationBuilder.setProgress(100,progress,false);
        notificationBuilder.setContentText("Downloading file: "+mFilename+" - "+ currentSize +"/"+totalFileSize +" MB");
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}
