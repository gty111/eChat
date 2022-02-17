package android.example.echat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static RecyclerView mRecyclerView;
    static ListAdapter mAdapter;
    FloatingActionButton fab;
    TextView input;
    static LinkedList<String> UserList = new LinkedList<>();
    static LinkedList<String> ContentList = new LinkedList<>();
    static LinkedList<Integer> TypeList = new LinkedList<>();
    static String user;
    static String IP;
    static String Port;
    int max_len = 6;
    String sharedPrefFile = "android.example.echat";
    SharedPreferences mPreferences;
    SharedPreferences.Editor preferencesEditor;
    static Socket socket;
    static BufferedReader in;
    static PrintWriter out;
    static volatile boolean state = false;
    AVLoadingIndicatorView loading;
    static Handler mHandler;
    static int people_num=1;
    TextView mpeople_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mpeople_num = findViewById(R.id.peopleNum);
        loading = findViewById(R.id.loading);
        input = findViewById(R.id.input);
        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);
        preferencesEditor = mPreferences.edit();

        user = mPreferences.getString("user",gen_user());

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if(!state){
                Toast.makeText(getApplicationContext(),"网络未连接",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String toSend = input.getText().toString().replace("\n","");
            if(toSend.length()!=0){
                input.setText("");
                int size = ContentList.size();
                TypeList.addLast(1);
                UserList.addLast(user);
                ContentList.addLast(toSend);
                Objects.requireNonNull(mRecyclerView.getAdapter()).notifyItemInserted(size);
                mRecyclerView.smoothScrollToPosition(size);
                new Thread(){
                    public void run(){
                        out.println(user);
                        out.println(toSend);
                        out.flush();
                    }
                }.start();
            }else{
                Toast.makeText(getApplicationContext(), "请输入一些内容",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Create recycler view.
        mRecyclerView = findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ListAdapter(this,UserList,ContentList,TypeList);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mHandler = new MyHandler();




        IP = mPreferences.getString("IP","");
        Port = mPreferences.getString("PORT","");

        new Connect(MainActivity.this,loading).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    int size = ContentList.size()-1;
                    Objects.requireNonNull(mRecyclerView.getAdapter()).notifyItemInserted(size);
                    mRecyclerView.smoothScrollToPosition(size);
                    mAdapter.notifyDataSetChanged();
                    new Recvmessage().start();
                    break;
                case 2:
                    new Recvmessage().start();
                    break;
                case 3:
                    Toast.makeText(mRecyclerView.getContext(), "网络断开",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    mpeople_num.setText(String.valueOf(people_num));
                    new Recvmessage().start();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = this;
        if(item.getItemId()==R.id.change){
            LayoutInflater factory = LayoutInflater.from(context);
            final View textEntryView = factory.inflate(R.layout.dialog1,null);
            EditText editText = textEntryView.findViewById(R.id.nickname);
            editText.setText(user);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("修改昵称");
            builder.setView(textEntryView);
            builder.setPositiveButton("确认", (dialog, which) -> {
                String in = editText.getText().toString().replace("\n","");
                if(in.length()>0&&in.length()<=max_len){
                    user = in;
                    preferencesEditor.putString("user",user);
                    preferencesEditor.apply();
                    Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context,
                            "昵称长度不能为0或大于"+ max_len,
                            Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else if(item.getItemId()==R.id.connect){
            if(state){
                Toast.makeText(context,"网络已连接",Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.dialog2,null);
            final EditText text_ip = textEntryView.findViewById(R.id.ip);
            final EditText text_port = textEntryView.findViewById(R.id.port);

            text_ip.setText(mPreferences.getString("IP",""));
            text_port.setText(mPreferences.getString("PORT",""));

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("建立TCP连接");

            builder.setView(textEntryView);

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    IP = text_ip.getText().toString();
                    Port = text_port.getText().toString();
                    preferencesEditor.putString("IP",IP);
                    preferencesEditor.putString("PORT",Port);
                    preferencesEditor.apply();
                    loading.smoothToShow();
                    new Connect(MainActivity.this,loading).execute();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();  //创建对话框
            dialog.show();

            //new Connect(MainActivity.this,loading).execute();
        }
        return super.onOptionsItemSelected(item);
    }

    String gen_user(){
        Random r = new Random();
        StringBuilder tmp = new StringBuilder("user");
        int len_t = max_len - tmp.length();
        for(int i=0;i<len_t;i++){
            tmp.append(r.nextInt(10));
        }
        return tmp.toString();
    }

    public void num_show(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        if(state){
            builder.setTitle("当前共有"+ people_num +"个小伙伴在聊天室呢");
            builder.setPositiveButton("确认", (dialog, which) -> {

            });
        } else{
            builder.setTitle("还未连接喔，请重新连接");
            builder.setPositiveButton("重新连接", (dialog, which) -> new Connect(MainActivity.this,loading).execute());
        }
        AlertDialog dialog = builder.create();  //创建对话框
        dialog.show();
    }

}