package com.example.mobile

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobile.ui.theme.MobileTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileTheme {
                val navController = rememberNavController()
                ReminderScreen(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Black)
    systemUiController.setNavigationBarColor(color = Color.Black)

    val context = LocalContext.current
    var selectedTime by remember { mutableStateOf("20:00") }
    var reminderMessage by remember { mutableStateOf("") }
    var vibrationEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(false) }

    val sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
    val savedMessage = sharedPreferences.getString("reminderMessage", "")
    val savedVibration = sharedPreferences.getBoolean("vibrationEnabled", false)
    val savedSound = sharedPreferences.getBoolean("soundEnabled", false)

    LaunchedEffect(Unit) {
        reminderMessage = savedMessage.orEmpty()
        vibrationEnabled = savedVibration
        soundEnabled = savedSound
    }

    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    fun saveSettings(message: String, vibration: Boolean, sound: Boolean) {
        with(sharedPreferences.edit()) {
            putString("reminderMessage", message)
            putBoolean("vibrationEnabled", vibration)
            putBoolean("soundEnabled", sound)
            apply()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(hour: Int, minute: Int, message: String, vibration: Boolean, sound: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (message.isBlank()) {
            Toast.makeText(context, "Reminder message cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminderMessage", message)
            putExtra("vibrationEnabled", vibration)
            putExtra("soundEnabled", sound)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime.timeInMillis,
            pendingIntent
        )
        Toast.makeText(context, "Reminder set for $selectedTime", Toast.LENGTH_SHORT).show()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SET A REMINDER.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 18.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Building a routine helps bring structure.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { timePickerDialog.show() },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFD0B3FF),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(42.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = "Clock Icon",
                    tint = Color.Black
                )
                Text(
                    text = selectedTime,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Icon",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = reminderMessage,
            onValueChange = { reminderMessage = it },
            placeholder = { Text("Enter your reminder message") },
            singleLine = false,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vibration", color = Color.White)
            Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sound", color = Color.White)
            Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                saveSettings(reminderMessage, vibrationEnabled, soundEnabled)
                val timeParts = selectedTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                scheduleNotification(hour, minute, reminderMessage, vibrationEnabled, soundEnabled)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB18AFF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(42.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "Set Reminder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            TextButton(onClick = { /* Handle record later action */ }) {
                Text(
                    text = "Record it later?",
                    color = Color(0xFFB18AFF),
                    fontSize = 14.sp
                )
            }
            TextButton(onClick = { snoozeReminder(context, 10) }) {
                Text(
                    text = "Snooze for 10 min",
                    color = Color(0xFFB18AFF),
                    fontSize = 14.sp
                )
            }

        }
    }
}

fun snoozeReminder(context: Context, snoozeMinutes: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val snoozeTime = Calendar.getInstance().apply {
        add(Calendar.MINUTE, snoozeMinutes)
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("reminderMessage", "Snoozed Reminder")
        putExtra("vibrationEnabled", true)
        putExtra("soundEnabled", true)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime.timeInMillis, pendingIntent)
    Toast.makeText(context, "Reminder snoozed for $snoozeMinutes minutes", Toast.LENGTH_SHORT).show()
}

@RequiresApi(Build.VERSION_CODES.O)
fun onReceive(context: Context, intent: Intent) {
    val reminderMessage = intent.getStringExtra("reminderMessage") ?: "Remember to check in!"
    val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", false)
    val soundEnabled = intent.getBooleanExtra("soundEnabled", false)

    if (vibrationEnabled) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    if (soundEnabled) {
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, notificationSound)?.play()
    }

    Toast.makeText(context, reminderMessage, Toast.LENGTH_LONG).show()
}

class ReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val reminderMessage = intent.getStringExtra("reminderMessage") ?: "Remember to check in!"
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", false)
        val soundEnabled = intent.getBooleanExtra("soundEnabled", false)

        if (vibrationEnabled) {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        if (soundEnabled) {
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, notificationSound)?.play()
        }

        Toast.makeText(context, reminderMessage, Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderScreenPreview() {
    MobileTheme {
        val navController = rememberNavController()
        ReminderScreen(navController)
    }
}
