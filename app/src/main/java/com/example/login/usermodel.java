package com.example.login;

import com.google.firebase.database.IgnoreExtraProperties;

public class usermodel {


    public String password;
        public String email;

        public usermodel() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public usermodel( String email,String password) {
            this.password = password;
            this.email = email;
        }


}
