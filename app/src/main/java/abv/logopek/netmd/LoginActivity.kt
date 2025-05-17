package abv.logopek.netmd

import abv.logopek.netmd.netschoolapi.NetSchoolApi
import abv.logopek.netmd.ui.theme.NetMDTheme
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetMDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Login(innerPadding)
                }
            }
        }
    }

    @Composable
    fun Login(p: PaddingValues){
        var login by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var scid by remember { mutableStateOf("") }
        var tUrl by remember { mutableStateOf("") }

        var passwordVisible by remember { mutableStateOf(false) }
        val visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        val icon = if (passwordVisible) painterResource(R.drawable.visibility) else painterResource(R.drawable.visibility_off)

        var waitForLogin by remember { mutableStateOf(true) }
        var badLogin by remember { mutableStateOf(false) }
        val context = LocalContext.current
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(p).fillMaxSize()) {
            OutlinedTextField(login, {login = it}, label = { Text("Логин") }, keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
            OutlinedTextField(password,
                {password = it},
                label = { Text("Пароль") },
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Password),
                visualTransformation = visualTransformation,
                trailingIcon = {
                    IconButton(onClick = {passwordVisible = !passwordVisible}) {
                        Icon(
                            icon, ""
                        )
                    }
                }
            )
            OutlinedTextField(scid, {scid=it}, label = { Text("SCID") },  keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
            OutlinedTextField(tUrl, {tUrl = it}, label = {Text("URL")},  keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
            Button(onClick = {
                runBlocking {
                    waitForLogin = true
                    badLogin = false
                    withContext(Dispatchers.IO){
                        val nsApi = NetSchoolApi(tUrl)
                        if (nsApi.login(login, password, scid)){
                            waitForLogin = false
                            badLogin = false

                        }
                        else{
                            badLogin = true
                        }
                    }
                }

            }) {
                Text("Войти")
            }
        }
        if(badLogin){
            Toast.makeText(context, "Не удалось войти!", Toast.LENGTH_LONG).show()
        }
        if(!waitForLogin && !badLogin){
            val sharedPreferences = getSharedPreferences("ltp", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("login", login)
                putString("password", password)
                putString("scid", scid)
                putString("t_url", tUrl)
            }
            startActivity(Intent(LocalContext.current, MainActivity::class.java))
            finish()
        }


    }
}
