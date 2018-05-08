package com.example.mom.firecontacts;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
        //Authentication 身份驗證
        FirebaseAuth auth;
        FirebaseAuth.AuthStateListener authListener;
        private String userUID;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            auth = FirebaseAuth.getInstance();  //取得物件參照
            authListener = new FirebaseAuth.AuthStateListener() {
                //authListener能在帳號登入或登出時擷取狀態事件傾聽器 , 會自動呼叫onAuthStateChanged(),並取得驗證物件firebaseAuth
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();  //getCurrentUser()取得目前登入使用者物件user
                    if(user != null){  //登入成功
                        Log.d("onAuthStateChanged" , "登入:"+user.getUid());
                        userUID = user.getUid();
                    }
                    else{  //登入失敗
                        Log.d("onAuthStateChanged" , "已登出");
                    }
                }
            };
        }
        //每次LoginActivity首次顯現or從背景返回時 , 都會自動開啟傾聽事件
        @Override
        protected void onStart() {
            super.onStart();
            auth.addAuthStateListener(authListener);
        }
        //每次LoginActivity進入背景or結束時 , 都會自動停止傾聽事件
        @Override
        protected void onStop() {
            super.onStop();
            auth.removeAuthStateListener(authListener);
            auth.signOut();
        }
        //Email登入功能 , 以Firebase中的Email帳號作為app帳號登入
        //取得輸入的email,password , 呼叫FirebaseAuth類別的signInWithEmailAndPassword()進行帳號與密碼的登入
        public void Login(View view){
            final String email = ((EditText)findViewById(R.id.ed_email)).getText().toString();
            final String password = ((EditText)findViewById(R.id.ed_password)).getText().toString();
            Log.d("AUTH" , email +"/"+ password);
            auth.signInWithEmailAndPassword(email , password)   //利用email,password登入
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {  //工作完成後會自動執行的Callback方法
                            if(!task.isSuccessful()){       //task.isSuccessful = false 登入失敗
                                Log.d("onComplete" , "登入失敗");
                                register(email , password);  //另設register()處理,以輸入的email,password註冊 , email,password要設為final這裡才可以使用
                            }
                        }
                    });
        }
        //Email註冊帳號對話筐
        private void register(final String email, final String password){
            new AlertDialog.Builder(this )
                    .setTitle("登入問題")
                    .setMessage("無此帳號,是否以此帳號與密碼註冊")
                    .setPositiveButton("註冊", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createUser(email, password);  //另設createUser()註冊帳號
                        }
                    })
                    .setNegativeButton("取消" , null)
                    .show();
        }
        //Email註冊帳號
        private void createUser(String email , String password){
            auth.createUserWithEmailAndPassword(email , password)  //以輸入的email,password註冊帳號
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            String message = task.isComplete()? "註冊成功":"註冊失敗";
                            new AlertDialog.Builder(LoginActivity.this )
                                    .setMessage(message)
                                    .setPositiveButton("OK",null)
                                    .show();
                        }
                    });
        }

        //將資料儲存到Firebase上
        private void setUserData(){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");
            usersRef.child(userUID).child("phone").setValue("1234567"); //在users記錄下新增子紀錄,在此以userUID作為紀錄名稱
            usersRef.child(userUID).child("nickname").setValue("Mark");
            //需要更新記錄值 , 可使用Map搭配updateChildren()
            Map<String , Object> data = new HashMap<>();
            data.put("nickname","Mark123");
            usersRef.child(userUID).updateChildren(data,
                    new DatabaseReference.CompletionListener() {    //更動資料的callback,完成更動資料時,會自動執行onComplete()
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                //正確完成
                            }
                            else{
                                //發生錯誤
                            }
                        }
                    });
        }
        //使用push() , 新增資料時,讓資料有唯一值(流水號) ,Firebase會依照時間產生不重複的辨識值Unique ID
        //假設每個會員都能儲存自己的好友,且每個好友都有一個不重複的時間戳記
        private void pushFriend(String name){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");    //取得會員紀錄參照
            DatabaseReference friendsRef = usersRef.child(userUID).child("friends").push();   //取得會員下的friends記錄參照, 並呼叫push()
            Map<String , Object> friend = new HashMap<>();  //產生一個好友資訊 , 使用Map儲存
            friend.put("name" , name);
            friend.put("phone" , "123456789");
            friendsRef.setValue(friend);    //設定friend資料
            String friendID = friendsRef.getKey();  //取得該資料的辨識值
            Log.d("FRIEDN" , friendID+"");
        }
}
