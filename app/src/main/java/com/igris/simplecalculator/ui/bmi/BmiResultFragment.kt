package com.igris.simplecalculator.ui.bmi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.igris.simplecalculator.R
import com.igris.simplecalculator.data.BmiResults
import com.igris.simplecalculator.data.Patient
import com.igris.simplecalculator.databinding.FragmentBmiResultBinding
import dagger.hilt.android.AndroidEntryPoint
import health.sunya.bmi.BMI_VALUE_INTENT
import health.sunya.bmi.BmiTestResults
import health.sunya.bmi.MainBmiActivity
import sunya.health.utils.constants.IntentParams.PATIENT_PARAM


private const val BMI_RESULT_PARAMS = "BmiResult"


@AndroidEntryPoint
class BmiResultFragment : Fragment() {
    private var _binding: FragmentBmiResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var bmiResult: BmiResults
    private val bmiTestLauncher =
        getBmiTestLauncher(this)
    private lateinit var patient: Patient
    private lateinit var bmiTestResults: BmiTestResults

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            patient = Gson().fromJson(it.getString(PATIENT_PARAM)!!, Patient::class.java)
            bmiTestResults =
                Gson().fromJson(it.getString(BMI_RESULT_PARAMS)!!, BmiTestResults::class.java)

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBmiResultBinding.inflate(layoutInflater)
        setUpValues()
        setBmiResult()
        binding.bmiResultFrgPrintBtn.setOnClickListener {
            shareResults()
        }
        return binding.root
    }

    private fun shareResults() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        val shareMessage = bmiResult.getResultString()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }


    private fun setBmiResult() {
        val note = if (bmiTestResults.bmiPercentile.isEmpty()) {
            arrayListOf(
                "Bmi Category: " + bmiTestResults.bmiCategory,
                "Age: " + bmiTestResults.age.toString(),
                "Height: " + bmiTestResults.height,
                "Weight: " + bmiTestResults.weight.toString() + "kg",
            )
        } else {
            arrayListOf(
                "Bmi Percentile: " + bmiTestResults.bmiPercentile,
                "Bmi Category: " + bmiTestResults.bmiCategory,
                "Age: " + bmiTestResults.age.toString(),
                "Height: " + bmiTestResults.height,
                "Weight: " + bmiTestResults.weight.toString() + "kg",
            )
        }
        bmiResult = BmiResults(

            bmiValue = bmiTestResults.bmiValue.toString(),
            bmiResultList = note,
            false
        )
    }


    private fun setUpValues() {
        binding.apply {
            bmiTestResultBmiValueTV.text = bmiTestResults.bmiValue.toString()
            bmiTestResultPercentileValueTV.text = bmiTestResults.bmiPercentile
            bmiTestResultCatValueTV.text = bmiTestResults.bmiCategory
            actBmiGoHomeBtn.setOnClickListener {
                requireActivity().finish()
            }
            actBmiRestartBtn.setOnClickListener {
                val patientJson = Gson().toJson(patient)
                val intent = Intent(context, MainBmiActivity::class.java)
                intent.putExtra(PATIENT_PARAM, patientJson.toString())
                bmiTestLauncher.launch(intent)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(
            patient: Patient,
            result: String,
        ) =
            BmiResultFragment().apply {
                arguments = Bundle().apply {
                    putString(PATIENT_PARAM, Gson().toJson(patient))
                    putString(BMI_RESULT_PARAMS, result)
                }
            }
    }


    private fun getBmiTestLauncher(parentFragment: Fragment): ActivityResultLauncher<Intent> {
        return parentFragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val resultIntent = activityResult.data!!
                addResultFragment(
                    parentFragment,
                    newInstance(
                        patient,
                        resultIntent.getStringExtra(BMI_VALUE_INTENT)!!
                    )
                )
            }
        }
    }


    private fun addResultFragment(parentFragment: Fragment, requiredFragment: Fragment) {
        parentFragment.parentFragmentManager.beginTransaction()
            .replace(
                R.id.nav_host_fragment_content_main,
                requiredFragment
            )
            .commit()
    }
}

