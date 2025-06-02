package com.example.week13

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast

class SMSBR : BroadcastReceiver() {
    //여기서 브로드캐스트를 받아서 이 함수로 전달됨
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            //Telephony에서 받아올 수 있음
            val msg = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val msgSender = msg[0].originatingAddress
            //msg를 하나로 묶어주는 함수
            val msgBody = msg.joinToString(separator = "")
            // separator -> 뭘 구분으로 나눠서 string화할지
             {
                //메시지 바디만 가지고 연결해줘 라는 뜻
                it.messageBody
            }
            //Toast.makeText(context, "$msgSender : $msgBody" , Toast.LENGTH_SHORT).show()
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("msgSender", msgSender)
            intent.putExtra("msgBody", msgBody)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            // 앱이 없으면 새로 생성(onCreate부터 시작)
            // 앱이 top이면 intent로 정보만 보내고 재사용(newIntent부터 시작)
            // 앱이 아래에 있으면 아래거 위에 있는거 다 지우고 재사용

            val pendingIntent = PendingIntent.getActivity(
                context, 100, intent,
                //intent 수정 권한 지정
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            context?.let{
                val application = context.applicationContext as MyApplication
                if(application.isForeGround)
                    context.startActivity(intent)
                else
                    makeNotification(context, "$msgSender" , pendingIntent)
            }


        }




    }
}