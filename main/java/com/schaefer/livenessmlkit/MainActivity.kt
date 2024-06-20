/* This file is part of [BarrierFreeNav].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.schaefer.livenessmlkit

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.schaefer.livenesscamerax.domain.model.CameraSettings
import com.schaefer.livenesscamerax.domain.model.StepLiveness
import com.schaefer.livenesscamerax.domain.model.StorageType
import com.schaefer.livenesscamerax.navigation.LivenessEntryPoint
import com.schaefer.livenessmlkit.adapter.ImageListAdapter
import com.schaefer.livenessmlkit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val livenessEntryPoint = LivenessEntryPoint
    private val mutableStepList = arrayListOf<StepLiveness>()
    private val imageListAdapter = ImageListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.rvImageList.adapter = imageListAdapter
        setContentView(binding.root)

        launchLivenessActivity()
    }

    private fun launchLivenessActivity() {
        binding.btnStartLiveness.setOnClickListener {
            getSelectedSteps()
            when (mutableStepList.isEmpty()) {
                true -> showToast()
                false ->
                    livenessEntryPoint.startLiveness(
                        cameraSettings = CameraSettings(
                            livenessStepList = mutableStepList,
                            storageType = StorageType.INTERNAL
                        ),
                        context = this,
                    ) { livenessCameraXResult ->
                        if (livenessCameraXResult.error == null) {
                            val listOfImages = arrayListOf<ByteArray>().apply {
                                livenessCameraXResult.createdBySteps?.let { photoResultList ->
                                    this.addAll(
                                        photoResultList.map {
                                            Base64.decode(it.fileBase64, Base64.NO_WRAP)
                                        }
                                    )
                                }
                            }

                            imageListAdapter.imageList = listOfImages
                            binding.tvListResult.text =
                                getString(R.string.result_list, listOfImages.size.toString())
                            binding.groupResultList.isVisible = true
                        } else {
                            livenessCameraXResult.error?.let {
                                Log.e(
                                    this.localClassName,
                                    it.toString()
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun getSelectedSteps() {
        mutableStepList.clear()
        binding.chipGroupSteps.forEach { chip ->
            (chip as Chip).takeIf { it.isChecked }?.let {
                mutableStepList.add(it.mapToLivenessStep())
            }
        }
    }

    private fun View.mapToLivenessStep(): StepLiveness {
        return when (this.id) {
            R.id.chipStepSmile -> StepLiveness.STEP_SMILE
            R.id.chipStepBlink -> StepLiveness.STEP_BLINK
            R.id.chipStepMovementLeft -> StepLiveness.STEP_HEAD_LEFT
            R.id.chipStepMovementRight -> StepLiveness.STEP_HEAD_RIGHT
            else -> StepLiveness.STEP_LUMINOSITY
        }
    }

    private fun showToast() {
        Toast.makeText(
            this,
            "You need to select at least one step",
            Toast.LENGTH_LONG
        ).show()
    }
}
