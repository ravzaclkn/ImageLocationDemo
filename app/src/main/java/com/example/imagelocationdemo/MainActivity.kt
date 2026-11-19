package com.example.imagelocationdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.imagelocationdemo.ui.theme.ImageLocationDemoTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageLocationDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenWithCard(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.welcome_title))
            Text(text = stringResource(R.string.welcome_description))
        }
    }
}

@Composable
fun UserLocationMapScreen(
    permissionGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val current = LatLng(it.latitude, it.longitude)
                    userLocation = current
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(current, 15f)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(text = "Your current location")

        Spacer(modifier = Modifier.height(12.dp))

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            cameraPositionState = cameraPositionState
        ) {
            userLocation?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "You are here"
                )
            }
        }
    }
}

@Composable
fun LocationPermissionExample(
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(hasPermission) {
        onPermissionResult(hasPermission)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        onPermissionResult(granted)
        if (granted) {
            Toast.makeText(
                context,
                context.getString(R.string.location_granted),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.location_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasPermission) {
                stringResource(R.string.location_granted)
            } else {
                stringResource(R.string.location_denied)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (isGranted) {
                    hasPermission = true
                    onPermissionResult(true)
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        ) {
            Text(stringResource(R.string.permission_button))
        }
    }
}

@Composable
fun ScreenWithCard(modifier: Modifier = Modifier) {
    var hasPermission by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.geolocation),
            contentDescription = "Map preview"
        )

        Text(text = stringResource(R.string.app_name))

        PermissionCard()

        if (hasPermission) {
            UserLocationMapScreen(permissionGranted = true)
        } else {
            LocationPermissionExample(
                onPermissionResult = { granted -> hasPermission = granted }
            )
        }
    }
}