package com.example.week13

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BatteryBR : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
            //수신됐을때 할일을 여기에 작성

        //전원이 연결 됐을 때
        if(intent?.action == Intent.ACTION_POWER_CONNECTED)
        {
            Toast.makeText(context, "전원 연결", Toast.LENGTH_SHORT).show()
        }
        //전원이 해제 됐을 때
        else if(intent?.action == Intent.ACTION_POWER_DISCONNECTED)
        {
            Toast.makeText(context, "전원 연결 해제", Toast.LENGTH_SHORT).show()
        }



    }


}