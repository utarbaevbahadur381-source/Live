package com.live.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Tariff(val name: String, val price: Int, val gb: Int, val minutes: Int)

val tariffs = listOf(
    Tariff("Старт", 1500, 10, 300),
    Tariff("Лайв", 3000, 30, 1000),
    Tariff("Макс", 5000, 100, 3000)
)

class AppState {
    var balance by mutableStateOf(2500)
    var tariff by mutableStateOf(tariffs[1])
    var message by mutableStateOf("")
    val history = mutableStateListOf("Добро пожаловать в Лайв")
}

val Green = Color(0xFF22C55E)
val DarkGreen = Color(0xFF16A34A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Green)) { App() }
        }
    }
}

@Composable
fun App() {
    val s = remember { AppState() }
    var tab by remember { mutableStateOf(0) }
    val names = listOf("Главная", "Тарифы", "Пополнить", "История")
    val marks = listOf("🏠", "📦", "💳", "🕘")
    Scaffold(
        bottomBar = {
            NavigationBar {
                names.forEachIndexed { i, n ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i; s.message = "" },
                        icon = { Text(marks[i]) },
                        label = { Text(n, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { p ->
        Box(Modifier.padding(p)) {
            when (tab) {
                0 -> Home(s)
                1 -> Tariffs(s)
                2 -> TopUp(s)
                else -> History(s)
            }
        }
    }
}

@Composable
fun Home(s: AppState) {
    val t = s.tariff
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().background(Green, RoundedCornerShape(24.dp)).padding(24.dp)
        ) {
            Text("Баланс", color = Color.White)
            Text("${s.balance} ₸", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text("Тариф: ${t.name}", color = Color.White)
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Интернет", fontWeight = FontWeight.Medium)
                LinearProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxWidth(), color = Green)
                Text("Осталось ${t.gb * 6 / 10} из ${t.gb} ГБ")
                Text("Минуты", fontWeight = FontWeight.Medium)
                LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth(), color = DarkGreen)
                Text("Осталось ${t.minutes * 4 / 10} из ${t.minutes} мин")
            }
        }
    }
}

@Composable
fun Tariffs(s: AppState) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (s.message.isNotEmpty()) Text(s.message, color = DarkGreen, fontWeight = FontWeight.Medium)
        }
        items(tariffs) { t ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${t.gb} ГБ, ${t.minutes} минут")
                    Text("${t.price} ₸ в месяц", color = DarkGreen)
                    Button(
                        onClick = {
                            if (s.tariff == t) {
                                s.message = "Тариф ${t.name} уже подключён"
                            } else if (s.balance >= t.price) {
                                s.balance -= t.price
                                s.tariff = t
                                s.history.add("Подключён тариф ${t.name}: -${t.price} ₸")
                                s.message = "Тариф ${t.name} подключён"
                            } else {
                                s.message = "Недостаточно средств"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) { Text("Подключить") }
                }
            }
        }
    }
}

@Composable
fun TopUp(s: AppState) {
    var amount by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Пополнение баланса", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = amount,
            onValueChange = { v -> amount = v.filter { it.isDigit() }.take(7) },
            label = { Text("Сумма, ₸") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(500, 1000, 3000).forEach { v ->
                OutlinedButton(onClick = { amount = v.toString() }) { Text("$v") }
            }
        }
        Button(
            onClick = {
                val v = amount.toIntOrNull() ?: 0
                if (v > 0) {
                    s.balance += v
                    s.history.add("Пополнение: +$v ₸")
                    s.message = "Баланс пополнен на $v ₸"
                    amount = ""
                } else {
                    s.message = "Введите сумму"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) { Text("Пополнить") }
        if (s.message.isNotEmpty()) Text(s.message, color = DarkGreen, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun History(s: AppState) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(s.history.reversed()) { h ->
            Card(Modifier.fillMaxWidth()) { Text(h, Modifier.padding(16.dp)) }
        }
    }
}
