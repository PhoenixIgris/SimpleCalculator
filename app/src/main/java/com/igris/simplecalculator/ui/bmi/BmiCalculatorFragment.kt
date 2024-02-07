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
import com.igris.simplecalculator.data.Patient
import com.igris.simplecalculator.databinding.FragmentBmiBinding
import dagger.hilt.android.AndroidEntryPoint
import health.sunya.bmi.BMI_VALUE_INTENT
import health.sunya.bmi.MainBmiActivity

@AndroidEntryPoint
class BmiCalculatorFragment : Fragment() {

    private lateinit var patient: Patient
    private var _binding: FragmentBmiBinding? = null

    private val binding get() = _binding!!
    private val bmiTestLauncher =
        getBmiTestLauncher(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        setView()
        return binding.root
    }

    private fun setView() {
        binding.buttonSubmit.setOnClickListener {
            if (validateInputs()) {
                startNextActivity()
            }
        }
    }

    private fun startNextActivity() {
        binding.apply {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString().trim()
            val age = editTextAge.text.toString().trim().toInt()
            val synced = checkBoxSynced.isChecked
            patient = Patient(
                patientName = name,
                patientEmail = email, gender = gender, age = age, synced = synced
            )
            val intent = Intent(requireContext(), MainBmiActivity::class.java).apply {
                putExtra("Patients", Gson().toJson(patient))
            }
            bmiTestLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getBmiTestLauncher(parentFragment: Fragment): ActivityResultLauncher<Intent> {
        return parentFragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val resultIntent = activityResult.data!!
                addResultFragment(
                    parentFragment,
                    BmiResultFragment.newInstance(
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

    private fun validateInputs(): Boolean {
        binding.apply {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString().trim()
            val ageStr = editTextAge.text.toString().trim()
            val age = if (ageStr.isNotEmpty()) ageStr.toInt() else 0

            if (name.isEmpty()) {
                editTextName.error = "Name is required"
                return false
            }

            if (email.isEmpty()) {
                editTextEmail.error = "Email is required"
                return false
            }

            if (gender.isEmpty()) {
                return false
            }

            if (age <= 0) {
                editTextAge.error = "Age must be greater than 0"
                return false
            }
        }
        return true
    }
}