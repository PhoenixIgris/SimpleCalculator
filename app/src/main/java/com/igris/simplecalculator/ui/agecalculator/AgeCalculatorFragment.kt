package com.igris.simplecalculator.ui.agecalculator

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.igris.simplecalculator.R
import com.igris.simplecalculator.databinding.FragmentAgeCalculatorBinding
import com.igris.simplecalculator.utils.AppUtils.getCurrentDayMonthYear
import com.igris.simplecalculator.utils.AppUtils.onTextChanged
import com.igris.simplecalculator.utils.MyPreferences
import dagger.hilt.android.AndroidEntryPoint
import health.sunya.bmi.BuildConfig

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private const val TAG = "AgeCalculatorActivity"

@AndroidEntryPoint
class AgeCalculatorFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var preferences: MyPreferences
    private lateinit var binding: FragmentAgeCalculatorBinding


    private var birthYear = 0
    private var birthMonth = 0
    private var birthDay = 0
    private var todayYear = 0
    private var todayMonth = 0
    private var todayDay = 0
    private var isDateOfBirthSelected = false
    private var interAd: InterstitialAd? = null
    private var isFirstTimeAdsShown = true


    private fun validateDate(
        inputDay: Int,
        inputMonth: Int,
        inputYear: Int, textView: TextView, isToday: Boolean
    ) {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(inputYear, inputMonth - 1, inputDay)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                else -> "Invalid Date"
            }

            if (day == inputDay && (month + 1) == inputMonth && year == inputYear) {
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_symbol_color
                    )
                )
                textView.text = dayName
                if (isToday) {
                    todayDay = inputDay
                    todayMonth = inputMonth
                    todayYear = inputYear
                } else {
                    birthDay = inputDay
                    birthMonth = inputMonth
                    birthYear = inputYear
                }
            } else {
                textView.text = "Invalid Date"
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
            }
        } catch (e: Exception) {
            textView.text = "Invalid Date"
            textView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
            e.printStackTrace()
        }
    }

    private fun getAdvertisingId(context: Context): String? {
        val adId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        Log.d("AdIdFetcher", "Advertising ID: $adId")
        return adId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgeCalculatorBinding.inflate(layoutInflater)
        preferences = MyPreferences(requireContext())
        if (preferences.lastCalculationMode) {
            val tokenType = object : TypeToken<Map<AgeParameters, String>>() {}.type
            if (preferences.ageHistory != null) {
                val resultMap: Map<AgeParameters, String> = Gson().fromJson(
                    preferences.ageHistory,
                    tokenType
                )
                setHistoryData(resultMap)
            } else {
                setInitialValue()
            }
        } else {
            setInitialValue()
        }
        setListener()
        isFirstTimeAdsShown = true
        MobileAds.initialize(requireContext()) { }
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest) // Replace with your actual test device ID
        if (BuildConfig.DEBUG) {
            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(getAdvertisingId(requireContext())))
                .build()

            MobileAds.setRequestConfiguration(requestConfiguration)
        }

        loadIntAd()

        return binding.root
    }


    private fun showIntAd() {
        Log.e("@@@", "@@@@ CHALL ");
        if (interAd != null) {
            Log.e("@@@", "@@@@ CHALL ad not null ");
            interAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                }

                override fun onAdShowedFullScreenContent() {
                    //Input your code here
                    super.onAdShowedFullScreenContent()
                }


                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    isFirstTimeAdsShown = false
                    shareResults()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }


                override fun onAdClicked() {
                    super.onAdClicked()
                }
            }
            interAd?.show(requireActivity())
        } else {
            Log.e("@@@", "@@@@ CHALL ad null")
            shareResults()
        }
    }

    private fun loadIntAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), "ca-app-pub-5471092371977143/5659460090", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interAd = interstitialAd
                }
            })
    }

    private fun setHistoryData(data: Map<AgeParameters, String>) {
        setInitialValue()
        todayDay = getIntValue(data[AgeParameters.TODAY_DAY])
        todayMonth = getIntValue(data[AgeParameters.TODAY_MONTH])
        todayYear = getIntValue(data[AgeParameters.TODAY_YEAR])
        birthDay = getIntValue(data[AgeParameters.BIRTH_DAY])
        birthMonth = getIntValue(data[AgeParameters.BIRTH_MONTH])
        birthYear = getIntValue(data[AgeParameters.BIRTH_YEAR])
        binding.todayDayValue.setText(data[AgeParameters.TODAY_DAY])
        binding.todayMonthValue.setText(data[AgeParameters.TODAY_MONTH])
        binding.todayYearValue.setText(data[AgeParameters.TODAY_YEAR])
        binding.todayDay.text = data[AgeParameters.TODAY_YEAR_DAY]
        binding.birthDayValue.setText(data[AgeParameters.BIRTH_DAY])
        binding.birthMonthValue.setText(data[AgeParameters.BIRTH_MONTH])
        binding.birthYearValue.setText(data[AgeParameters.BIRTH_YEAR])
        binding.dateOfBirthDay.text = data[AgeParameters.BIRTH_YEAR_DAY]
        setData(data)
    }

    private fun getIntValue(value: String?): Int {
        return if (!value.isNullOrEmpty()) {
            value.toInt()
        } else {
            0
        }
    }

    private fun setInitialValue() {
        getCurrentDayMonthYear().let {
            todayDay = it.first
            todayMonth = it.second
            todayYear = it.third
            binding.todayDayValue.setText(String.format("%02d", todayDay))
            binding.todayMonthValue.setText(String.format("%02d", todayMonth))
            binding.todayYearValue.setText(String.format("%04d", todayYear))
        }
        birthYear = 0
        birthMonth = 0
        birthDay = 0

        binding.birthDayValue.setText(String.format("%02d", birthMonth))
        binding.birthMonthValue.setText(String.format("%02d", birthMonth))
        binding.birthYearValue.setText(String.format("%04d", birthYear))
        binding.dateOfBirthDay.text = ""
        setData(getInitialValues())
    }

    private fun setListener() {
        binding.todayDayCard.setOnClickListener {
            isDateOfBirthSelected = false
        }
        binding.dateOfBirthCard.setOnClickListener {
            isDateOfBirthSelected = true
        }
        binding.todayDayValue.onTextChanged {
            todayDay = it.toInt()
            validateDate(todayDay, todayMonth, todayYear, binding.todayDay, true)
            if (it.length >= 2) {
                binding.todayMonthValue.requestFocus()
            }
        }
        binding.todayMonthValue.onTextChanged {
            todayMonth = it.toInt()
            validateDate(todayDay, todayMonth, todayYear, binding.todayDay, true)
            if (it.length >= 2) {
                binding.todayYearValue.requestFocus()
            }

        }
        binding.todayYearValue.onTextChanged {
            todayYear = it.toInt()
            validateDate(todayDay, todayMonth, todayYear, binding.todayDay, true)
            if (it.length >= 4) {
                binding.birthDayValue.requestFocus()
            }
        }
        binding.birthDayValue.onTextChanged {
            birthDay = it.toInt()
            validateDate(birthDay, birthMonth, birthYear, binding.dateOfBirthDay, false)
            if (it.length >= 2) {
                binding.birthMonthValue.requestFocus()
            }
        }
        binding.birthMonthValue.onTextChanged {
            birthMonth = it.toInt()
            validateDate(birthDay, birthMonth, birthYear, binding.dateOfBirthDay, false)
            if (it.length >= 2) {
                binding.birthYearValue.requestFocus()
            }
        }
        binding.birthYearValue.onTextChanged {
            birthYear = it.toInt()
            validateDate(birthDay, birthMonth, birthYear, binding.dateOfBirthDay, false)
        }
        binding.todayDayDatePicker.setOnClickListener {
            isDateOfBirthSelected = false
            showDatePickerDialog()
        }
        binding.dateOfBirthDatePicker.setOnClickListener {
            isDateOfBirthSelected = true
            showDatePickerDialog()
        }

        binding.btnClear.setOnClickListener {
            hideKeyboard()
            clearAll()
        }
        binding.btnCalculate.setOnClickListener {
            hideKeyboard()
            setData(calculateAgeAndNextBirthday())
        }
        binding.sharebtn.setOnClickListener {
            if (isFirstTimeAdsShown) {
                showIntAd()
            } else {
                shareResults()
            }

        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun shareResults() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        val shareMessage = buildShareMessage(calculateAgeAndNextBirthday())
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun buildShareMessage(result: Map<AgeParameters, Any>): String {
        val shareMessage = StringBuilder()
        shareMessage.append("*AGE CALCULATION* \n \n")
        shareMessage.append("*YOUR AGE\n\n")
        shareMessage.append("YEARS= ${result[AgeParameters.AGE_YEARS]}\n")
        shareMessage.append("MONTHS= ${result[AgeParameters.AGE_MONTHS]}\n")
        shareMessage.append("DAY = ${result[AgeParameters.AGE_DAYS]}\n\n")

        shareMessage.append("----------------\n\n")

        shareMessage.append("*Extras\n\n")
        shareMessage.append("Total Years= ${result[AgeParameters.TOTAL_YEARS]}\n")
        shareMessage.append("Total Months = ${result[AgeParameters.TOTAL_MONTHS]}\n")
        shareMessage.append("Total Weeks = ${result[AgeParameters.TOTAL_WEEKS]}\n")
        shareMessage.append("Total Days= ${result[AgeParameters.TOTAL_DAYS]}\n")
        shareMessage.append("Total Hours= ${result[AgeParameters.TOTAL_HOURS]}\n")
        shareMessage.append("Total Minutes= ${result[AgeParameters.TOTAL_MINUTES]}\n")
        shareMessage.append("Total Seconds= ${result[AgeParameters.TOTAL_SECONDS]}\n\n")

        shareMessage.append("----------------\n\n")

        shareMessage.append("*Next Birthday\n\n")
        shareMessage.append("Months = ${result[AgeParameters.NEXT_BIRTHDAY_MONTHS]}\n")
        shareMessage.append("Days = ${result[AgeParameters.NEXT_BIRTHDAY_DAYS]}\n")

        shareMessage.append("----------------\n\n")

        shareMessage.append("\nDownload the Calculator One here at : https://play.google.com/store/apps/details?id=" + requireActivity().application.packageName)

        return shareMessage.toString().trim()
    }


    private fun setData(calculateAgeAndNextBirthday: Map<AgeParameters, Any>) {
        binding.apply {
            ageYears.text = calculateAgeAndNextBirthday[AgeParameters.AGE_YEARS].toString()
            ageMths.text = calculateAgeAndNextBirthday[AgeParameters.AGE_MONTHS].toString()
            ageDays.text = calculateAgeAndNextBirthday[AgeParameters.AGE_DAYS].toString()
            ageNextMths.text =
                calculateAgeAndNextBirthday[AgeParameters.NEXT_BIRTHDAY_MONTHS].toString()
            ageNextDays.text =
                calculateAgeAndNextBirthday[AgeParameters.NEXT_BIRTHDAY_DAYS].toString()
            extraInfoList.removeAllViews()
            val extraList = calculateAgeAndNextBirthday.filter {
                it.key != AgeParameters.TODAY_YEAR &&
                        it.key != AgeParameters.TODAY_YEAR_DAY &&
                        it.key != AgeParameters.TODAY_YEAR &&
                        it.key != AgeParameters.TODAY_MONTH &&
                        it.key != AgeParameters.TODAY_DAY &&
                        it.key != AgeParameters.BIRTH_YEAR &&
                        it.key != AgeParameters.BIRTH_MONTH &&
                        it.key != AgeParameters.BIRTH_DAY &&
                        it.key != AgeParameters.BIRTH_YEAR_DAY &&
                        it.key != AgeParameters.AGE_YEARS &&
                        it.key != AgeParameters.AGE_MONTHS &&
                        it.key != AgeParameters.AGE_DAYS &&
                        it.key != AgeParameters.NEXT_BIRTHDAY_MONTHS &&
                        it.key != AgeParameters.NEXT_BIRTHDAY_DAYS &&
                        it.key != AgeParameters.NEXT_BIRTHDAY_FORMATTED
            }
            for ((key, value) in extraList) {
                val customView = addExtraView(key.key, value.toString())
                extraInfoList.addView(customView)
            }
        }


        if (preferences.lastCalculationMode) {
            preferences.ageHistory = Gson().toJson(calculateAgeAndNextBirthday)
        }
    }

    private fun addExtraView(key: String, value: String): View {
        val inflater = LayoutInflater.from(requireContext())
        val rowView = inflater.inflate(R.layout.age_extra_info_list_view, null)

        val titleTextView = rowView.findViewById<TextView>(R.id.title)
        val valueTextView = rowView.findViewById<TextView>(R.id.value)

        titleTextView.text = "$key:"
        valueTextView.text = value

        return rowView
    }

    private fun calculateAgeAndNextBirthday(
    ): Map<AgeParameters, Any> {
        val todayDate = Calendar.getInstance()
        todayDate.set(todayYear, todayMonth, todayDay)
        val birthDate = Calendar.getInstance().apply {
            set(birthYear, birthMonth - 1, birthDay)  // Month is zero-based in Calendar
        }

        // Calculate age
        var ageYears = todayYear - birthYear
        var ageMonths = todayMonth - birthMonth
        var ageDays = todayDay - birthDay

        if (ageDays < 0) {
            val lastMonth = todayDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            ageDays += lastMonth
            ageMonths--
        }

        if (ageMonths < 0) {
            ageMonths += 12
            ageYears--
        }
        val currentDate = Calendar.getInstance()
        // Calculate next birthday
        val nextBirthday = Calendar.getInstance().apply {
            set(todayYear, birthMonth - 1, birthDay)
            if (before(currentDate)) {
                add(Calendar.YEAR, 1)
            }
        }

        // Calculate difference until next birthday
        val timeDifference = currentDate.timeInMillis - birthDate.timeInMillis
        val totalYears = ageYears
        val totalMonths = ageYears * 12 + ageMonths
        Log.e(
            TAG,
            "calculateAgeAndNextBirthday: $timeDifference ${currentDate.time} ${birthDate.time}",
        )
        // Corrected calculation for next birthday, total days, and total weeks
        var nextBirthdayMonths = nextBirthday.get(Calendar.MONTH) - currentDate.get(Calendar.MONTH)
        var nextBirthdayDays =
            nextBirthday.get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_MONTH)
        if (nextBirthdayDays < 0) {
            nextBirthdayMonths--
            nextBirthdayDays += currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        val totalDays = (timeDifference / (1000 * 60 * 60 * 24)).toInt()
        val totalWeeks = totalDays / 7

        val totalHours = (timeDifference / (1000 * 60 * 60)).toInt()
        val totalMinutes = (timeDifference / (1000 * 60)).toInt()
        val totalSeconds = (timeDifference / 1000).toInt()

        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        val nextBirthdayFormatted = dateFormat.format(nextBirthday.time)

        return mapOf(
            AgeParameters.TODAY_DAY to binding.todayDayValue.text.toString(),
            AgeParameters.TODAY_MONTH to binding.todayMonthValue.text.toString(),
            AgeParameters.TODAY_YEAR to binding.todayYearValue.text.toString(),
            AgeParameters.TODAY_YEAR_DAY to binding.todayDay.text.toString(),
            AgeParameters.BIRTH_DAY to binding.birthDayValue.text.toString(),
            AgeParameters.BIRTH_MONTH to binding.birthMonthValue.text.toString(),
            AgeParameters.BIRTH_YEAR to binding.birthYearValue.text.toString(),
            AgeParameters.BIRTH_YEAR_DAY to binding.dateOfBirthDay.text.toString(),
            AgeParameters.AGE_YEARS to ageYears,
            AgeParameters.AGE_MONTHS to ageMonths,
            AgeParameters.AGE_DAYS to ageDays,
            AgeParameters.NEXT_BIRTHDAY_MONTHS to nextBirthdayMonths,
            AgeParameters.NEXT_BIRTHDAY_DAYS to nextBirthdayDays,
            AgeParameters.NEXT_BIRTHDAY_FORMATTED to nextBirthdayFormatted,
            AgeParameters.TOTAL_YEARS to totalYears,
            AgeParameters.TOTAL_MONTHS to totalMonths,
            AgeParameters.TOTAL_WEEKS to totalWeeks,
            AgeParameters.TOTAL_DAYS to totalDays,
            AgeParameters.TOTAL_HOURS to totalHours,
            AgeParameters.TOTAL_MINUTES to totalMinutes,
            AgeParameters.TOTAL_SECONDS to totalSeconds
        )
    }

    private fun getInitialValues(): Map<AgeParameters, Any> {
        return mapOf(
            AgeParameters.TODAY_YEAR to "0000",
            AgeParameters.TODAY_MONTH to "00",
            AgeParameters.TODAY_DAY to "00",
            AgeParameters.BIRTH_YEAR to "0000",
            AgeParameters.BIRTH_MONTH to "00",
            AgeParameters.BIRTH_DAY to "00",
            AgeParameters.AGE_YEARS to "0",
            AgeParameters.AGE_MONTHS to "0",
            AgeParameters.AGE_DAYS to "0",
            AgeParameters.NEXT_BIRTHDAY_MONTHS to "0",
            AgeParameters.NEXT_BIRTHDAY_DAYS to "0",
            AgeParameters.NEXT_BIRTHDAY_FORMATTED to "0",
            AgeParameters.TOTAL_YEARS to "0",
            AgeParameters.TOTAL_MONTHS to "0",
            AgeParameters.TOTAL_WEEKS to "0",
            AgeParameters.TOTAL_DAYS to "0",
            AgeParameters.TOTAL_HOURS to "0",
            AgeParameters.TOTAL_MINUTES to "0",
            AgeParameters.TOTAL_SECONDS to "0",
        )
    }


    private fun clearAll() {
        setInitialValue()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, day)
        datePickerDialog.show()
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (isDateOfBirthSelected) {
            birthDay = dayOfMonth
            birthMonth = month + 1
            birthYear = year
            binding.birthDayValue.setText(String.format("%02d", birthMonth))
            binding.birthMonthValue.setText(String.format("%02d", birthMonth))
            binding.birthYearValue.setText(String.format("%04d", birthYear))

        } else {
            todayDay = dayOfMonth
            todayMonth = month + 1
            todayYear = year
            binding.todayDayValue.setText(String.format("%02d", todayDay))
            binding.todayMonthValue.setText(String.format("%02d", todayMonth))
            binding.todayYearValue.setText(String.format("%04d", todayYear))

        }
    }


}