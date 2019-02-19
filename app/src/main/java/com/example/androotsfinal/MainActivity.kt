package com.example.androotsfinal

import android.app.ActionBar
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazonaws.amplify.generated.graphql.CreateTodoMutation
import com.amazonaws.amplify.generated.graphql.CreateUserMutation
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.exception.ApolloException
import type.CreateTodoInput
import type.CreateUserInput
import android.icu.util.ULocale.getCountry
import android.icu.util.ULocale.getLanguage
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var client: AWSAppSyncClient

    private val locale = Locale.getAvailableLocales()
    private val languages = ArrayList<String>()
    private val knownLanguages = ArrayList<String>()
    private val countries = ArrayList<String>()
    private val fromCountries = ArrayList<String>()
    private val USER_FIRST_NAME = ""

    //used to add margin to view programmatically
    private val params = ActionBar.LayoutParams(
        WRAP_CONTENT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = AWSAppSyncClient.builder()
            .context(applicationContext)
            .awsConfiguration( AWSConfiguration(applicationContext))
            .build()

        populateLanguageSpinner()
        populateCountrySpinners()
        languagesSpinnerListener()
        fromCountriesSpinnerListener()
//        livedCountriesSpinnerListener()
    }

    // populate the languages spinner
    private fun populateLanguageSpinner() {
        var language: String
        //add each locale to languages array
        for (loc in locale) {
            language = loc.displayLanguage
            if (language.isNotEmpty() && !languages.contains(language)) {
                languages.add(language)
            }
        }
        Collections.sort(languages, String.CASE_INSENSITIVE_ORDER)
        val languagesSpinner = findViewById<Spinner>(R.id.languagesSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languagesSpinner.adapter = adapter
    }

    fun languagesSpinnerListener() {
//        val textViewArray = ArrayList<TextView>() //empty array list of TextViews
        val languagesSpinner = findViewById<Spinner>(R.id.languagesSpinner)
        val languagesLayout = findViewById<LinearLayout>(R.id.chosen_languages_layout)
        var isFirstLanguageSelection = true

        languagesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                if (isFirstLanguageSelection) {
                    isFirstLanguageSelection = false
                } else {
                    val mTextView = TextView(applicationContext)
                    mTextView.text = languages[position]
                    mTextView.setTextAppearance(R.style.selected_tag_theme)
                    params.setMargins(12, 12, 12, 12)
                    mTextView.layoutParams = params
                    //add new textview to layout
//                    textViewArray.add(mTextView)
                    // add the language to the known languages array also
                    knownLanguages.add(languages[position])
                    //add view to layout
                    languagesLayout.addView(mTextView)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
                return
            }
        }
    }

    // populate the languages spinner
    private fun populateCountrySpinners() {
        var country: String
        //add each locale to languages array
        for (loc in locale) {
            country = loc.displayCountry
            if (country.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER)
        val fromCountrySpinner = findViewById<Spinner>(R.id.fromCountrySpinner)
        val fromCountryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        fromCountrySpinner.adapter = fromCountryAdapter

//        val livedCountrySpinner = findViewById<Spinner>(R.id.livedCountrySpinner)
//        val livedCountryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
//        livedCountrySpinner.adapter = livedCountryAdapter
    }

    fun fromCountriesSpinnerListener() {
        val mTextViewArray = ArrayList<TextView>() //empty array list of TextViews
        val fromCountrySpinner = findViewById<Spinner>(R.id.fromCountrySpinner)
        val fromCountryLayout = findViewById<LinearLayout>(R.id.fromCountryLayout)
        var isFirstCountrySelection = true

        fromCountrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                if (isFirstCountrySelection) {
                    isFirstCountrySelection = false
                } else {
                    val mTextView = TextView(applicationContext)
                    mTextView.text = countries[position]
                    mTextView.setTextAppearance(R.style.selected_tag_theme)
                    params.setMargins(12, 12, 12, 12)
                    mTextView.layoutParams = params
                    fromCountries.add(countries[position])
                    fromCountryLayout.addView(mTextView)
//                    mTextViewArray.add(mTextView)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
                return
            }
        }
    }

//
//    fun livedCountriesSpinnerListener() {
//        val textViewArray = ArrayList<TextView>() //empty array list of TextViews
//        val livedCountrySpinner = findViewById<Spinner>(R.id.languagesSpinner)
//        val livedCountriesLayout = findViewById<LinearLayout>(R.id.livedCountryLayout)
//
//        livedCountrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
//                val mTextView = TextView(applicationContext)
//                mTextView.text = countries[position]
//                textViewArray.add(mTextView)
//                livedCountriesLayout.addView(mTextView)
//            }
//
//            override fun onNothingSelected(adapterView: AdapterView<*>) {
//                return
//            }
//        }
//    }

    //submit user mutation
    fun clickSubmitUserButton (view: View) {
        val firstName = findViewById<EditText>(R.id.firstNameInput).text.toString()
        val secondName = findViewById<EditText>(R.id.secondNameInput).text.toString()
        val story = findViewById<EditText>(R.id.storyInput).text.toString()

        runMutation(firstName, secondName, story, knownLanguages, fromCountries)

        val intent = Intent(this, DisplayUserActivity::class.java).apply {
            putExtra(USER_FIRST_NAME, firstName)
        }
        startActivity(intent)
    }

    fun runMutation(firstName : String, secondName : String, story : String, knownLanguages : ArrayList<String>, from : ArrayList<String>) {
        val createUserInput = CreateUserInput.builder()
            .firstName(firstName)
            .lastName(secondName)
            .story(story)
            .languages(knownLanguages)
            .from(from)
            .build()

        client.mutate(CreateUserMutation.builder().input(createUserInput).build())
            .enqueue(mutationCallback)
    }
    private val mutationCallback = object : GraphQLCall.Callback<CreateUserMutation.Data>() {
        override fun onResponse(response: com.apollographql.apollo.api.Response<CreateUserMutation.Data>) {
            Log.e("TEST", response.data().toString()) //To change body of created functions use File | Settings | File Templates.
        }

        override fun onFailure(e: ApolloException) {
            Log.e("TEST", e.toString())
        }
    }
}
