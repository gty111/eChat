package android.example.echat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connect extends AsyncTask<Void,Void,Void> {

    private WeakReference<Context> context;
    private WeakReference<AVLoadingIndicatorView> loading;

    public Connect(Context context,
                   AVLoadingIndicatorView loading){
        this.context = new WeakReference<>(context);
        this.loading = new WeakReference<>(loading);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            MainActivity.socket = new Socket(MainActivity.IP,
                    Integer.parseInt(MainActivity.Port));
            InputStream inStream = MainActivity.socket.getInputStream();
            OutputStream outStream = MainActivity.socket.getOutputStream();
            MainActivity.in = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
            MainActivity.out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
            MainActivity.state = true;
            Message msg = new Message();
            msg.what = 2;
            MainActivity.mHandler.sendMessage(msg);
        } catch (Exception e) {
            MainActivity.socket = null;
            MainActivity.state = false;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        if(MainActivity.state){
            Toast.makeText(context.get(),"网络连接成功", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context.get(),"网络连接失败", Toast.LENGTH_SHORT).show();
        }

        loading.get().smoothToHide();
    }
}
