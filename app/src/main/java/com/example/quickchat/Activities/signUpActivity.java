package com.example.quickchat.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.quickchat.Utilities.Constants;
import com.example.quickchat.Utilities.PreferenceManager;
import com.example.quickchat.databinding.ActivitySignUpBinding;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


public class signUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();

    }
    private void setListeners(){
        binding.inputTextSignIn.setOnClickListener(v -> onBackPressed());
        binding.btn.setOnClickListener(v ->{
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v  ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION  );
            pickImage.launch(intent);
        });
    }
    private  void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputCode.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_IN,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent =new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());

                });
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()*previewWidth /bitmap.getWidth()  ;
        Bitmap previewBitmap= Bitmap.createScaledBitmap(bitmap, previewWidth,previewHeight , false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT );
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData()!= null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputstream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
                            binding.imagePro.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private Boolean isValidSignUpDetails(){
        if (encodedImage == null){
            showToast("select Profile Image");
            return false;

        }else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
        }
        else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast(("Enter valid email"));
            return false;

        } else if (binding.inputCode.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
            
        } else if (binding.inputCodeConfirm.getText().toString().trim().isEmpty()) {
            showToast("Confirm the Password");
            return false;
        } else if (!binding.inputCode.getText().toString().equals(binding.inputCodeConfirm.getText().toString())) {
            showToast("Password & Confirm Password must be same");
            return false;
        }
        else {
            return true;
        }
    }
    private void loading(boolean isLoading){
        if (isLoading){
            binding.btn.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }
    }
}