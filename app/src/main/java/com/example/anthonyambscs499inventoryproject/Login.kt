package com.example.anthonyambscs499inventoryproject

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.mindrot.jbcrypt.BCrypt
import androidx.core.view.isVisible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    private var textTitle: TextView? = null
    private var textUsername: TextView? = null
    private var textPassword: TextView? = null
    private var textConfirmPassword: TextView? = null
    private var enterUsername: EditText? = null
    private var enterPassword: EditText? = null
    private var enterConfirmPassword: EditText? = null
    private var buttonSignIn: Button? = null
    private var buttonCreateAccount: Button? = null
    private var buttonLogin: Button? = null
    private var buttonSignUp: Button? = null
    private var buttonSwitchSignUp: Button? = null
    private var buttonSwitchLogin: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val apiService = RetrofitService.instance

        textTitle = findViewById<TextView>(R.id.textViewTitle)
        textUsername = findViewById<TextView>(R.id.textViewUsername)
        textPassword = findViewById<TextView>(R.id.textViewPassword)
        textConfirmPassword = findViewById<TextView>(R.id.textViewConfirmPassword)
        enterUsername = findViewById<EditText>(R.id.editTextUsername)
        enterPassword = findViewById<EditText>(R.id.editTextPassword)
        enterConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        buttonSignIn = findViewById<Button>(R.id.buttonSignIn)
        buttonCreateAccount = findViewById<Button>(R.id.buttonCreateAccount)
        buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonSignUp = findViewById<Button>(R.id.buttonSignUp)
        buttonSwitchSignUp = findViewById<Button>(R.id.buttonSwitchSignUp)
        buttonSwitchLogin = findViewById<Button>(R.id.buttonSwitchLogin)

        textUsername!!.visibility = View.GONE
        textPassword!!.visibility = View.GONE
        textConfirmPassword!!.visibility = View.GONE
        enterUsername!!.visibility = View.GONE
        enterPassword!!.visibility = View.GONE
        enterConfirmPassword!!.visibility = View.GONE
        buttonLogin!!.visibility = View.GONE
        buttonSignUp!!.visibility = View.GONE
        buttonSwitchSignUp!!.visibility = View.GONE
        buttonSwitchLogin!!.visibility = View.GONE

        buttonLogin!!.isEnabled = false
        buttonSignUp!!.isEnabled = false

        // Click buttonSignIn to go to login page
        buttonSignIn!!.setOnClickListener {
            buttonSignIn!!.visibility = View.GONE
            buttonCreateAccount!!.visibility = View.GONE
            buttonLogin!!.visibility = View.VISIBLE
            textUsername!!.visibility = View.VISIBLE
            textPassword!!.visibility = View.VISIBLE
            enterUsername!!.visibility = View.VISIBLE
            enterPassword!!.visibility = View.VISIBLE
            buttonSwitchSignUp!!.visibility = View.VISIBLE
        }

        // Click buttonCreateAccount to go to sign up page
        buttonCreateAccount!!.setOnClickListener {
            buttonSignIn!!.visibility = View.GONE
            buttonCreateAccount!!.visibility = View.GONE
            buttonSignUp!!.visibility = View.VISIBLE
            textUsername!!.visibility = View.VISIBLE
            textPassword!!.visibility = View.VISIBLE
            textConfirmPassword!!.visibility = View.VISIBLE
            enterUsername!!.visibility = View.VISIBLE
            enterPassword!!.visibility = View.VISIBLE
            enterConfirmPassword!!.visibility = View.VISIBLE
            buttonSwitchLogin!!.visibility = View.VISIBLE
        }

        // Click buttonOnSwitchLogin to go to login page
        buttonSwitchLogin!!.setOnClickListener {
            buttonLogin!!.visibility = View.VISIBLE
            buttonSignUp!!.visibility = View.GONE
            textUsername!!.visibility = View.VISIBLE
            textPassword!!.visibility = View.VISIBLE
            textConfirmPassword!!.visibility = View.GONE
            enterConfirmPassword!!.visibility = View.GONE
            enterUsername!!.visibility = View.VISIBLE
            enterPassword!!.visibility = View.VISIBLE
            buttonSwitchSignUp!!.visibility = View.VISIBLE
            buttonSwitchLogin!!.visibility = View.GONE
            updateButtonStates()
        }

        // Click buttonSwitchSignUp to go to sign up page
        buttonSwitchSignUp!!.setOnClickListener {
            buttonSignUp!!.visibility = View.VISIBLE
            buttonLogin!!.visibility = View.GONE
            textUsername!!.visibility = View.VISIBLE
            textPassword!!.visibility = View.VISIBLE
            textConfirmPassword!!.visibility = View.VISIBLE
            enterUsername!!.visibility = View.VISIBLE
            enterPassword!!.visibility = View.VISIBLE
            enterConfirmPassword!!.visibility = View.VISIBLE
            buttonSwitchLogin!!.visibility = View.VISIBLE
            buttonSwitchSignUp!!.visibility = View.GONE
            updateButtonStates()
        }

        // Click buttonLogin to login
        buttonLogin!!.setOnClickListener {
            val username = enterUsername!!.text.toString().trim { it <= ' ' }
            val password = enterPassword!!.text.toString().trim { it <= ' ' }
            val user = User(username, password)

            apiService.validateUser(user).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful && response.body() == true) {
                        Toast.makeText(this@Login, "Logged in", Toast.LENGTH_SHORT).show()
                        val mainIntent: Intent =
                            Intent(
                                this@Login,
                                MainActivity::class.java
                            )
                        mainIntent.putExtra("loggedIn", true)
                        startActivity(mainIntent)
                        finish()
                    } else if (response.isSuccessful && response.body() == false) {
                        Toast.makeText(this@Login, "Invalid credentials", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@Login, "Error signing in", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Boolean?>, t: Throwable) {
                    Toast.makeText(this@Login, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG)
                        .show()
                    t.printStackTrace()
                }
            })

        }

        // Hash the password
        fun hashPassword(plainPassword: String): String {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt())
        }

        // Click buttonSignUp
        buttonSignUp!!.setOnClickListener {
            val username = enterUsername!!.text.toString().trim { it <= ' ' }
            val password = enterPassword!!.text.toString().trim { it <= ' ' }
            val confirmPassword = enterConfirmPassword!!.text.toString().trim { it <= ' ' }

            if (password != confirmPassword) {
                Toast.makeText(this@Login, "Passwords must match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            apiService.isUsernameUnique(username).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    Log.d("API_DEBUG", "Response from isUsernameUnique: ${response.body()}")
                    if (response.isSuccessful && response.body() == true) {
                        val bcryptHashString: String = hashPassword(password)
                        val newUser = User(username, bcryptHashString)

                        apiService.addNewUser(newUser).enqueue(object : Callback<Boolean> {
                            override fun onResponse(call: Call<Boolean>, accountResp: Response<Boolean>) {
                                if (accountResp.isSuccessful && accountResp.body() == true) {
                                    Toast.makeText(this@Login, "Account created", Toast.LENGTH_SHORT).show()
                                    val mainIntent = Intent(this@Login, MainActivity::class.java)
                                    mainIntent.putExtra("loggedIn", true)
                                    startActivity(mainIntent)
                                    finish()
                                } else {
                                    Toast.makeText(this@Login, "Error creating account", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                Toast.makeText(this@Login, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                                t.printStackTrace()
                            }
                        })
                    } else {
                        Toast.makeText(this@Login, "Username already in use", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(this@Login, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                    t.printStackTrace()
                }
            })
        }


        // TextWatcher for enterUsername, enterPassword, and enterConfirmPassword fields
        val fieldWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateButtonStates()
            }

            override fun afterTextChanged(s: Editable) {}
        }

        enterUsername!!.addTextChangedListener(fieldWatcher)
        enterPassword!!.addTextChangedListener(fieldWatcher)
        enterConfirmPassword!!.addTextChangedListener(fieldWatcher)
    }

    // Dynamically enable and disable buttonSignUp and buttonLogin
    private fun updateButtonStates() {
        val username = enterUsername!!.text.toString().trim { it <= ' ' }
        val password = enterPassword!!.text.toString().trim { it <= ' ' }
        val confirmPassword = enterConfirmPassword!!.text.toString().trim { it <= ' ' }

        val enableSignUp =
            buttonSignUp!!.isVisible && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
        val enableLogin =
            buttonLogin!!.isVisible && username.isNotEmpty() && password.isNotEmpty()

        buttonSignUp!!.isEnabled = enableSignUp
        buttonLogin!!.isEnabled = enableLogin
    }
}