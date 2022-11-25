package com.example.sns_project

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FriendMessagingService : FirebaseMessagingService() {
    // 토큰이 변경될 때(앱 재설치) 호출
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    // 메시지를 받을 때 호출, 전면에서 실행 중이 아닐 경우 알림으로 나타난다.
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FriendMessagingService", "From: ${message.from}")
        Log.d("FriendMessagingService", "Data: ${message.data}")
    }
}