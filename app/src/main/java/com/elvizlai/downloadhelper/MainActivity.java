package com.elvizlai.downloadhelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ProgressDialog dialog = new ProgressDialog(this);
        final EditText urlInput = (EditText) findViewById(R.id.pathInput);

        final AsyncTask mAsyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    URL url = new URL(urlInput.getText().toString());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    JSONObject object = new JSONObject();
                    object.put("appName", "H9");

                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(object.toString());
                    writer.flush();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] buf = new byte[1024];
                    int len = 0;
                    InputStream is = connection.getInputStream();
                    while ((len = is.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    baos.flush();

                    return baos.toString("utf8");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                String jsonStr = (String) o;
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    String android_path = jsonObject.getString("android_path");
                    startDownload(android_path, dialog);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Button confirmBtn = (Button) findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setTitle("下载");
                dialog.setMessage("当前下载进度");
                dialog.setCancelable(false);
                dialog.show();
                mAsyncTask.execute();
            }
        });


    }

    private void startDownload(final String url, final ProgressDialog dialog) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String[] temp = url.split("/");
                    String fileName = "/" + temp[temp.length - 1];
                    DownloadTask.getFile(url, Environment.getExternalStorageDirectory() + fileName, dialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(runnable);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
