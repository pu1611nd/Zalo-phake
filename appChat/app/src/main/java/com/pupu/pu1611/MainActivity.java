package com.pupu.pu1611;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pupu.pu1611.Activity.AddPostActivity;
import com.pupu.pu1611.Activity.QRScannerActivity;
import com.pupu.pu1611.Activity.UserActivity;
import com.pupu.pu1611.Adapter.MainViewPagerAdapter;
import com.pupu.pu1611.Fragment.Fragment_List_Chat;
import com.pupu.pu1611.Fragment.Fragment_Notification;
import com.pupu.pu1611.Fragment.Fragment_Phone_Book;
import com.pupu.pu1611.Fragment.Fragment_Status;
import com.pupu.pu1611.Fragment.Fragment_User;
import com.pupu.pu1611.databinding.ActivityMainBinding;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity{
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private int currentPage = 0;

    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        getToken();
        checkForBatteryOptimizations();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.searchView.setOnClickListener(v->{
            binding.toolbar.getMenu().clear();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        });
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Xử lý sự kiện tìm kiếm
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Xử lý sự kiện thay đổi nội dung tìm kiếm
                return false;
            }
        });

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Ẩn tất cả các MenuItem
                binding.toolbar.getMenu().clear();
                // Hiển thị nút "Back"
                ActionBar actionBar = getSupportActionBar();
                binding.searchView.setVisibility(View.GONE);
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);

                }
                if (item.getItemId() == R.id.notification) {
                    Fragment_Notification newFragment = new Fragment_Notification();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();


                    return true;
                }else if(item.getItemId() == R.id.scanQR){
                    Intent intent = new Intent(getApplicationContext(), QRScannerActivity.class);
                    startActivity(intent);
                    return true;
                }else {

                    return false;
                }
            }

        });

        binding.toolbar.setNavigationOnClickListener(v -> this.onBackPressed());


    }
    // back
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            // Hiển thị lại các MenuItem
            onCreateOptionsMenu(binding.toolbar.getMenu());
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            binding.searchView.setVisibility(View.VISIBLE);
            return;
        }

        super.onBackPressed();
    }


    private void init(){
        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(),getLifecycle());
        mainViewPagerAdapter.addFragment(new Fragment_List_Chat());
        mainViewPagerAdapter.addFragment(new Fragment_Phone_Book());
        mainViewPagerAdapter.addFragment(new Fragment_Status());
        mainViewPagerAdapter.addFragment(new Fragment_User());
        binding.myViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.myViewPager.setAdapter(mainViewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.myTabLayout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chat_line_round_svgrepo_com));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.book_contact_ui_web_svgrepo_com));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.clock_svgrepo_com));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.user_1_svgrepo_com));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.myViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.myViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                currentPage = position;
                invalidateOptionsMenu();
                switch (position){
                    case 0:
                        binding.fabNewChat.setVisibility(View.VISIBLE);
                        binding.fabNewChat.setImageResource(R.drawable.ic_add_chat);
                        binding.fabNewChat.setOnClickListener(v->{
                            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
                        break;
                    case 1:
                        binding.fabNewChat.setVisibility(View.VISIBLE);
                        binding.fabNewChat.setImageResource(R.drawable.ic_baseline_call_24);
                        break;
                    case 2:
                        binding.fabNewChat.setVisibility(View.VISIBLE);
                        binding.fabNewChat.setImageResource(R.drawable.ic_baseline_add_24);
                        binding.fabNewChat.setOnClickListener(v->{
                            Intent intent = new Intent(getApplicationContext(), AddPostActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
                        break;
                    default:
                        binding.fabNewChat.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }


    private void showMessage (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showMessage("them token that bai"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentPage == 0) {
            // Tạo menu phù hợp cho trang 1
            getMenuInflater().inflate(R.menu.menu_item_chat, menu);

        } else if (currentPage == 1) {
            // Tạo menu phù hợp cho trang 2
            getMenuInflater().inflate(R.menu.menu_item_phonebook, menu);
        } else if (currentPage == 2) {
            // Tạo menu phù hợp cho trang 3
            getMenuInflater().inflate(R.menu.menu_item_status, menu);
        } else if (currentPage == 3) {
            // Tạo menu phù hợp cho trang 4
            getMenuInflater().inflate(R.menu.menu_item_user, menu);
        }

        return true;
    }

    private void checkForBatteryOptimizations(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("warning");
                builder.setMessage("Battery optization i enable");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForBatteryOptimizations();
        }
    }
}