package com.example.androotsfinal

import android.app.ActionBar
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.amazonaws.amplify.generated.graphql.GetUserQuery
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_display_user.*

class DisplayUserActivity : AppCompatActivity() {

    private lateinit var client: AWSAppSyncClient

    private var firstName : String = ""
    private var secondName : String = ""
    private var story : String = ""
    private var languages = ArrayList<String>()
    private var from = ArrayList<String>()
    private var user : String = ""
    private var counter : Int = 0
    private var isFirstTime = true

    //used to add margin to view programmatically
    private val params = ActionBar.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_user)

        client = AWSAppSyncClient.builder()
            .context(applicationContext)
            .awsConfiguration(AWSConfiguration(applicationContext))
            .build()

        // Get the Intent that started this activity and extract the string
        val id = intent.getStringExtra("USER_ID")

//        val hello = findViewById<TextView>(R.id.name)
//        hello.text = "hello"

        getUserInfo(id)
    }

    private fun getUserInfo(id : String) {
        client.query(
            GetUserQuery.builder().id(id)
            .build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(getUserCallback)
    }

    private val getUserCallback = object : GraphQLCall.Callback<GetUserQuery.Data>() {
        override fun onResponse( response : com.apollographql.apollo.api.Response<GetUserQuery.Data> ) {

            //grab user data
            val user = response.data()?.getUser()
            firstName = user?.firstName().toString()
            secondName = user?.lastName().toString()
            story = user?.story().toString()
            languages = ArrayList(user?.languages())
            from = ArrayList(user?.from())

            displayUserInfo()
            counter ++
            Log.e("counter", counter.toString())

            Log.e("Results", response
                .data()?.getUser()?.toString())
        }

        override fun onFailure(e: ApolloException) {
            Log.e("ERROR", "something went wrong")
        }
    }

    fun displayUserInfo() {
        if (isFirstTime) {
            isFirstTime = false
        } else {
            runOnUiThread {
                //set name
                val nameTextView = findViewById<TextView>(R.id.name)
                val name = firstName.capitalize() + " " + secondName.capitalize()
                nameTextView.text = name

                //set story
                val myStory = findViewById<TextView>(R.id.story)
                myStory.text = story

                //set languages dynamically
                for (lang in languages) {
                    val langTextView = TextView(applicationContext)
                    langTextView.text = lang
                    langTextView.setTextAppearance(R.style.selected_tag_theme)
                    params.setMargins(12, 12, 12, 12)
                    langTextView.layoutParams = params
                    myLanguagesLayout.addView(langTextView)
                    Log.e("test", lang)
                }

                //set from countries dynamically
                for (country in from) {
                    val mTextView = TextView(applicationContext)
                    mTextView.text = country
                    mTextView.setTextAppearance(R.style.selected_tag_theme)
                    params.setMargins(12, 12, 12, 12)
                    mTextView.layoutParams = params
                    myCountriesLayout.addView(mTextView)
                }
            }
        }

    }

}

