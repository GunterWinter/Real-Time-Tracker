package com.nguyenquocthai.real_time_tracker.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.nguyenquocthai.real_time_tracker.Fragments.AboutFragment;
import com.nguyenquocthai.real_time_tracker.Service.AcceptShare;
import com.nguyenquocthai.real_time_tracker.Adapter.NotificationsAdapter;
import com.nguyenquocthai.real_time_tracker.Fragments.JoinCircleFragment;
import com.nguyenquocthai.real_time_tracker.Fragments.MyCircleFragment;
import com.nguyenquocthai.real_time_tracker.Fragments.ProfileFragment;
import com.nguyenquocthai.real_time_tracker.Service.ListFriend;
import com.nguyenquocthai.real_time_tracker.Model.NotificationItem;
import com.nguyenquocthai.real_time_tracker.Model.Users;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;
import com.nguyenquocthai.real_time_tracker.R;
import com.nguyenquocthai.real_time_tracker.Model.SharedViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initiation();
        setupToolbarAndDrawer();
        getProfile();
        setupMap();
        callpermissionlistener();
        checkNotificationEnabled();
        initLocationFab();
        getFMCToken();
        handleIntentData();
        setupNotificationsButton();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent hiện tại của Activity
    }
    // get from MyCircleFragment
    private void handleIntentDataForMap() {
        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel.getLocationData().observe(this, latLng -> {
            // Cập nhật UI hoặc bản đồ với dữ liệu mới
            if (latLng != null && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
            }
        });
    }

    void getFMCToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                databaseReference.child(current_uid).child("fcmToken").setValue(token);
            }
        });
    }
    // When u online
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { // Get data when u using app
            showAlertDialog("Hey there! "+intent.getExtras().getString("name")+" would like to share their location with you ",intent.getExtras().getString("userID"));
        }
    };
    private void setupBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("com.yourapp.FCM_MESSAGE"));
    }
    @Override
    protected void onStart() {
        super.onStart();
        setOnline();
        update_location();
        setupBroadcastReceiver();
    }
    @Override
    protected void onStop() {
        super.onStop();
        setOffline();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
    //onDestroy đang vô dụng
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setOffline();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Log.d("Info","onDestroy");
    }
    private void showAlertDialog(String message,String userID) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mediaPlayer.start();
                    AcceptShare acceptShare = new AcceptShare(userID,MainActivity.this);
                    acceptShare.Execute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    mediaPlayer.start();
                    //No button clicked
                    break;
            }
        };
        if (alertDialogBuilder == null) {
            alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener);
        }
        alertDialogBuilder.setMessage(message).show();
    }
    // When u click notification
    private void handleIntentData() {
        if (getIntent().getExtras() != null) { // Get data when u not using app
            String name = getIntent().getExtras().getString("name");
            String userID = getIntent().getExtras().getString("userID");
            if (name != null && !name.isEmpty() && userID != null && !userID.isEmpty()) {
                showAlertDialog("Hey there! " + name + " would like to share their location with you ", userID);
            }
        }
    }
    private void setupNotificationsButton() {
        notificationButton = findViewById(R.id.button_notifications);
        notificationButton.setOnClickListener(v -> {
            mediaPlayer.start();
            if (notificationPopup == null) {
                initNotificationPopup();
            }
            toggleNotificationPopup();
        });
    }
    private void initNotificationPopup() {
        // Inflate the custom notification panel layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.notification_panel_layout, null);

        // Initialize the PopupWindow
        notificationPopup = new PopupWindow(customView, getWidthPanelNotification(), LayoutParams.WRAP_CONTENT);
        notificationPopup.setElevation(5.0f);
        notificationPopup.setOutsideTouchable(true);
        notificationPopup.setFocusable(true);

        // Initialize the RecyclerView
        recyclerView = customView.findViewById(R.id.notificationsRecyclerView); // id panel
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(getNotificationItems());
        adapter.setOnNotificationItemClickListener(new NotificationsAdapter.OnNotificationItemClickListener() {
            @Override
            public void onNotificationItemClick(NotificationItem item) {
                mediaPlayer.start();
                showAlertDialog(item.getMessage(), item.getUserID());
            }
        });

        recyclerView.setAdapter(adapter);

        // Now that the PopupWindow and RecyclerView are initialized, start listening for notifications
        getNotification();
    }
    private void toggleNotificationPopup() {
        int xOffset = notificationButton.getWidth() - getWidthPanelNotification(); // Calculate the offset
        if (notificationPopup.isShowing()) {
            notificationPopup.dismiss();
        } else {
            notificationPopup.showAsDropDown(notificationButton, xOffset, 0);
        }
    }
    private List<NotificationItem> getNotificationItems() {
        List<NotificationItem> items = new ArrayList<>();
        return items;
    }
    public void addNotification(NotificationItem newNotification) {
        if (adapter != null) {
            Log.d("notificationadd",newNotification.getUserID());
            adapter.getNotificationItems().add(0, newNotification); // Add the new notification
            adapter.notifyItemInserted(0); // Notify the adapter
            recyclerView.scrollToPosition(0); // Scroll to the top of the RecyclerView
        } else {
        }
    }
    private void getNotification(){
        // Giả sử bạn muốn lấy 10 thông báo mới nhất
        notifiReference.orderByChild("timestamp").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa các thông báo cũ trước khi thêm mới
                adapter.clearNotifications();

                // Duyệt qua các mục trong snapshot
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    NotificationItem notification = childSnapshot.getValue(NotificationItem.class);
                    if (notification != null) {
                        Log.d("notificationID", notification.getUserID());
                        addNotification(notification);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }
    private int getWidthPanelNotification(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        return screenWidth / 2-40;
    }
    private void setupMap() {
        //set up google map on container
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        handleIntentDataForMap();
        update_location();
    }
    private void initLocationFab() {
        fab = findViewById(R.id.fab_current_location);
        fab.setOnClickListener(view -> {
            mediaPlayer.start();
            if(mMap != null && latLng != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
            } else {
                showToast("Still waiting for current location...");
            }
        });
    }
    private void update_location() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED) {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            onLocationReceived(location);
                        }
                    });

            // Create location request to start receiving updates
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(3000) // Update interval
                    .setFastestInterval(5000); // Fastest interval

            // Start location updates
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else {
            callpermissionlistener();
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    onLocationReceived(location);
                }
            }
        }
    };
    private void onLocationReceived(Location location) {
        final double lat = location.getLatitude();
        final double lon = location.getLongitude();
        latLng = new LatLng(lat, lon);

        // Update the map with the new location
        if (mMap != null) {
            if (currentUserMarker == null) {
                currentUserMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Your current location"));
                databaseReference.child(current_uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String imageUrl = snapshot.child("image_url").getValue(String.class);
                        if(imageUrl!=null && !imageUrl.isEmpty())
                            getMarkerBitmapFromView(imageUrl, currentUserMarker);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                if(check==false)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
            } else {
                currentUserMarker.setPosition(latLng);
            }

            listFriend.getListFriend(new ListFriend.DataStatus() {
                @Override
                public void DataIsLoaded(List<Users> users) {
                    for (Users user : users) {
                        LatLng friendLatLng = new LatLng(user.getLatitude(), user.getLongitude());
                        Marker marker = friendMarkers.get(user.getId());
                        if (marker == null) {
                            marker = mMap.addMarker(new MarkerOptions().position(friendLatLng).title(user.getLastname() + " " + user.getFirstname()));
                            friendMarkers.put(user.getId(), marker);
                        } else {
                            marker.setPosition(friendLatLng);
                        }

                        if (user.getImage_url() != null && !user.getImage_url().isEmpty()) {
                            getMarkerBitmapFromView(user.getImage_url(), marker);
                        }
                    }
                }
            });
        }

        // Update Firebase with the new location
        Map<String, Object> update = new HashMap<>();
        update.put("latitude", lat);
        update.put("longitude", lon);
        databaseReference.child(current_uid).updateChildren(update);
    }
    // Display friend's image
    private void getMarkerBitmapFromView(String imageUrl, Marker markerToUpdate) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        CircleImageView markerImageView = customMarkerView.findViewById(R.id.user_marker_icon);

        Picasso.get().load(imageUrl).placeholder(R.drawable.ic_user).into(markerImageView, new Callback() {
            @Override
            public void onSuccess() {
                customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
                Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                customMarkerView.draw(canvas);
                markerToUpdate.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }

            @Override
            public void onError(Exception e) {
                Log.e("MarkerIconLoadError", "Error loading marker icon", e);
            }
        });
    }
    private void showToast(String message){
        if (currentToast != null) {
            currentToast.cancel(); // turn off the notification
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        currentToast.show();
    }
    private void setOnline() {
        databaseReference.child(current_uid).child("isOnline").setValue(1);
    }
    private void setOffline(){
        if(current_uid!=null)
            databaseReference.child(current_uid).child("isOnline").setValue(0);
    }
    private void callpermissionlistener() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        String rationale = "Please provide location permission so that you can ...";
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("location permission")
                .setSettingsDialogTitle("warrning");

        Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                mapFragment.getMapAsync(MainActivity.this);
                update_location();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                callpermissionlistener();
            }
        });
    }
    private void checkNotificationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (!notificationManager.areNotificationsEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle("Cần Quyền Thông Báo")
                        .setMessage("Ứng dụng này cần quyền để hiển thị thông báo. Vui lòng bật thông báo cho ứng dụng trong cài đặt hệ thống.")
                        .setPositiveButton("Đến Cài Đặt", (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            backpressedwarrning();
        }
        //super.onBackPressed();
    }

    public void backpressedwarrning(){
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.setTitle("Exit");
        alert.show();
    }
    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        View header = navigationView.getHeaderView(0);
        nameView = header.findViewById(R.id.textview_name);
        emailView = header.findViewById(R.id.textview_email);
        avatar = header.findViewById(R.id.image_avatar);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) { // MainActivity
            mediaPlayer.start();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ProfileFragment || currentFragment instanceof JoinCircleFragment || currentFragment instanceof MyCircleFragment || currentFragment instanceof AboutFragment) {
                // Remove the current fragment to show the MainActivity's content
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            fab.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_profile) {
            mediaPlayer.start();
            if (profileFragment == null) {
                profileFragment = new ProfileFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
            fab.setVisibility(View.GONE);
        } else if (itemId == R.id.nav_joiningc) {
            mediaPlayer.start();
            if (joinCircleFragment == null) {
                joinCircleFragment = new JoinCircleFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, joinCircleFragment)
                    .addToBackStack(null)
                    .commit();
            fab.setVisibility(View.GONE);
        } else if (itemId == R.id.nav_invite) {
            mediaPlayer.start();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/lain");
            i.putExtra(Intent.EXTRA_TEXT,"https://www.google.com/maps/@"+latLng.latitude+","+latLng.longitude+",17z");
            startActivity(i.createChooser(i,"Share using: "));
        } else if(itemId==R.id.nav_friend){
            mediaPlayer.start();
            if (myCircleFragment == null) {
                myCircleFragment = new MyCircleFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, myCircleFragment)
                    .addToBackStack(null)
                    .commit();
            fab.setVisibility(View.GONE);
        } else if (itemId == R.id.nav_about) {
            mediaPlayer.start();
            if (aboutFragment == null) {
                aboutFragment = new AboutFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, aboutFragment)
                    .addToBackStack(null)
                    .commit();
        } else if (itemId == R.id.nav_logout) {
            mediaPlayer.start();
            if (auth.getCurrentUser() != null) {
                if (fusedLocationProviderClient != null && locationCallback != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                }
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                    databaseReference.child(current_uid).child("fcmToken").removeValue();
                    setOffline();
                    auth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void getProfile(){
        databaseReference.child(current_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ProfileFragment", "Data changed");
                if (snapshot.exists()) {
                    String firstname = snapshot.child("firstname").getValue(String.class);
                    String lastname = snapshot.child("lastname").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String image = snapshot.child("image_url").getValue(String.class);
                    emailView.setText(email);
                    nameView.setText(lastname+" "+firstname);
                    if(image!="null"){
                        Picasso.get().load(image).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void Initiation() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        current_uid = user != null ? user.getUid() : "";
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        notifiReference = FirebaseDatabase.getInstance().getReference("users").child(current_uid).child("notification");
        listFriend = new ListFriend(current_uid);
        mediaPlayer = MediaPlayer.create(this,R.raw.click);
    }
    private DrawerLayout drawerLayout;
    private TextView nameView,emailView;
    private CircleImageView avatar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference,notifiReference;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private AlertDialog.Builder builder;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng latLng;
    private String current_uid;
    private FloatingActionButton fab;
    private Toast currentToast = null; // Biến để lưu trạng thái của thông báo hiện tại
    private ImageButton notificationButton;
    private PopupWindow notificationPopup;
    private NotificationsAdapter adapter;
    private RecyclerView recyclerView;
    private AlertDialog.Builder alertDialogBuilder;
    private ListFriend listFriend;
    private ProgressbarLoader loader;
    private boolean check=false;
    private Marker currentUserMarker;
    private Map<String, Marker> friendMarkers = new HashMap<>();
    private ProfileFragment profileFragment;
    private JoinCircleFragment joinCircleFragment;
    private MyCircleFragment myCircleFragment;
    private AboutFragment aboutFragment;
    private MediaPlayer mediaPlayer;

}