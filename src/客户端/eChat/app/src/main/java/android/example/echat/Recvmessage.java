package android.example.echat;

import android.os.Message;
import android.util.Log;

public class Recvmessage extends Thread {

    @Override
    public void run() {
        try {
            String user = MainActivity.in.readLine();
            String message = MainActivity.in.readLine();
            Log.d("qwer",user);
            Log.d("qwer",message);
            if(!user.equals("connect_num")){
                MainActivity.ContentList.addLast(message);
                MainActivity.UserList.addLast(user);
                MainActivity.TypeList.addLast(2);
                Message msg = new Message();
                msg.what = 1;
                MainActivity.mHandler.sendMessage(msg);
            }else{
                MainActivity.people_num = Integer.parseInt(message);
                Message msg = new Message();
                msg.what = 4;
                MainActivity.mHandler.sendMessage(msg);
            }
        }catch (java.net.SocketException e){
            MainActivity.state = false;
            Message msg = new Message();
            msg.what = 3;
            MainActivity.mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
