package com.example.quickchat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.quickchat.Utilities.Constants;
import com.example.quickchat.Utilities.PreferenceManager;
import com.example.quickchat.adapters.UsersAdapters;
import com.example.quickchat.databinding.ActivityUserBinding;
import com.example.quickchat.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class userActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database =  FirebaseFirestore.getInstance();
        database.collection((Constants.KEY_COLLECTION_USERS))
                .get()
                .addOnCompleteListener(task ->{
                    loading(false);
                    String currentUserId= preferenceManager.getString(Constants.KEY_USER_IN);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size()>0){
                            UsersAdapters userAdapters = new UsersAdapters(users);
                            binding.usersRecyclerView.setAdapter((userAdapters));
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }
                });
    }
    private void showErrorMessage (){
        binding.textErrorMessage.setText(String.format("%s", "No user availaible"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }
}