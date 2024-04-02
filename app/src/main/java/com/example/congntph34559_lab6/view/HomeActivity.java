package com.example.congntph34559_lab6.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.congntph34559_lab6.adapter.FruitAdapter;
import com.example.congntph34559_lab6.databinding.ActivityHomeBinding;
import com.example.congntph34559_lab6.model.Fruit;
import com.example.congntph34559_lab6.model.Response;
import com.example.congntph34559_lab6.services.HttpRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends AppCompatActivity implements FruitAdapter.FruitClick {
    static ActivityHomeBinding binding;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String token;
    @SuppressLint("StaticFieldLeak")
    private static FruitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("INFO", MODE_PRIVATE);

        token = sharedPreferences.getString("token", "");
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
        userListener();
    }

    private void userListener() {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddFruitActivity.class);
                intent.putExtra("titleAdd", "Add fruit");
                intent.putExtra("btnAdd", "Add");
                startActivity(intent);
                finish();
            }
        });
    }


    Callback<Response<ArrayList<Fruit>>> getListFruitResponse = new Callback<Response<ArrayList<Fruit>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    ArrayList<Fruit> ds = response.body().getData();
                    getData(ds);
//                    Toast.makeText(HomeActivity.this, response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Fruit>>> call, Throwable t) {

        }
    };


    public void getData(ArrayList<Fruit> ds) {
        adapter = new FruitAdapter(this, ds, this);
        binding.rcvFruit.setAdapter(adapter);
    }

    @Override
    public void delete(Fruit fruit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Thông báo").setMessage("Bạn có chắc chắn muốn xóa không?").setPositiveButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                httpRequest.callAPI().deleteFruit(fruit.get_id()).enqueue(new Callback<Response<Fruit>>() {
                    @Override
                    public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus() == 200) {
                                sharedPreferences = getSharedPreferences("INFO", MODE_PRIVATE);

                                token = sharedPreferences.getString("token", "");
                                httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
                                Toast.makeText(HomeActivity.this, "Delete thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<Fruit>> call, Throwable t) {
                        Log.e("zzzzzzzz", "onFailure: " + t.getMessage());
                    }
                });
            }
        }).show();


    }

    @Override
    public void edit(Fruit fruit) {
        Intent intent = new Intent(HomeActivity.this, AddFruitActivity.class);
        intent.putExtra("titleEdit", "Update fruit");
        intent.putExtra("btnEdit", "Update");
        intent.putExtra("name", fruit.getName());
        intent.putExtra("quantity", fruit.getQuantity());
        intent.putExtra("price", fruit.getPrice());
        intent.putExtra("status", fruit.getStatus());
        intent.putExtra("description", fruit.getDescription());
        intent.putExtra("id", fruit.get_id());
        intent.putExtra("image", fruit.getImage().get(0).toString());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
    }
}