package com.example.getandpostrequests

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var nameEditText: EditText
    lateinit var namesTextView: TextView
    lateinit var addButton: Button
    lateinit var getButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameEditText = findViewById(R.id.edt_name)
        namesTextView = findViewById(R.id.tv_names)
        namesTextView.movementMethod = ScrollingMovementMethod()
        addButton = findViewById(R.id.btn_addName)
        getButton = findViewById(R.id.btn_getNames)

        addButton.setOnClickListener {
            if(nameEditText.text.trim().isNotEmpty()){
                addName(nameEditText.text.toString())

            }else{
                Toast.makeText(this,"please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        getButton.setOnClickListener {
            getNames()
        }
    }

    private fun addName(name: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val jsonObject = JSONObject()
            try {
                jsonObject.put("name", name)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://dojo-recipes.herokuapp.com/custom-people/")
                .post(requestBody)
                .build()

            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                if(response.code == 201){
                    withContext(Dispatchers.Main){
                        nameEditText.text.clear()
                        Toast.makeText(this@MainActivity,"added successfully",Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,"something went wrong",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun getNames() {
        namesTextView.text = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url("https://dojo-recipes.herokuapp.com/custom-people/")
                    .build()
                val response =
                    withContext(Dispatchers.Default) {
                        okHttpClient.newCall(request).execute()
                    }
                if (response != null) {
                    if (response.code == 200) {
                        val jsonArray = JSONArray(response.body!!.string())
                        Log.d("HELP", jsonArray.toString())
                        for(index in 0 until jsonArray.length()){
                            val nameObj = jsonArray.getJSONObject(index)
                            val name = nameObj.getString("name")
                            withContext(Main){
                                namesTextView.text = "${namesTextView.text}\n$name"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("TextViewActivity", e.message.toString())
            }
        }
    }
}