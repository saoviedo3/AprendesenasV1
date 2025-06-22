package com.espe.aprendesenasv1.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.espe.aprendesenasv1.R
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.example.mlkitobjectdetection.GraphicOverlay
import com.example.mlkitobjectdetection.ObjectGraphic
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
class DetectionFragment : Fragment(R.layout.fragment_detection) {
    private lateinit var previewView: PreviewView
    private lateinit var imgResult: ImageView
    private lateinit var graphicOverlay: GraphicOverlay

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val REQUEST_CAMERA = 1001
    private var pendingSign: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Sólo guardamos la letra en pendingSign
        val                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        sign = requireArguments().getString("sign") ?: ""
        pendingSign = sign

        previewView    = view.findViewById(R.id.previewView)
        imgResult      = view.findViewById(R.id.imgResult)
        graphicOverlay = view.findViewById(R.id.graphicOverlay)


        view.findViewById<TextView>(R.id.tvSignTitle).text = "Letra $sign"
        val imgRef = view.findViewById<ImageView>(R.id.imgReference)
        val instr = view.findViewById<TextView>(R.id.tvInstructions)

        when(sign) {
            "A" -> {
                imgRef.setImageResource(R.drawable.ref_a)
                instr.text = "Coloca el pulgar sobre los dedos…"
            }
            "B" -> {
                imgRef.setImageResource(R.drawable.ref_b)
                instr.text = "Extiende todos los dedos…"
            }
        }
        // … código de ref_a / ref_b …

        view.findViewById<Button>(R.id.btnTry).setOnClickListener {
            // Cuando pulsan Intentar, comprobamos permiso y arrancamos
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {
                startCamera(sign)
            } else {
                // Pedimos permiso, y si lo dan, en onRequestPermissionsResult arrancamos
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concedió el permiso, arrancamos la cámara con la letra pendiente
                pendingSign?.let { startCamera(it) }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCamera(targetSign: String) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 1) Preview
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // 2) ML Kit
            val localModel = LocalModel.Builder()
                .setAssetFilePath("model_meta.tflite")
                .build()
            val options = CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.6f)
                .build()
            val detector = ObjectDetection.getClient(options)

            // 3) ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(detector, imageProxy, targetSign)
                    }
                }

            // 4) Bind
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImageProxy(
        detector: com.google.mlkit.vision.objects.ObjectDetector,
        imageProxy: ImageProxy,
        targetSign: String
    ) {
        val image = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
        detector.process(image)
            .addOnSuccessListener { detectedObjects ->
                graphicOverlay.clear()
                var found = false
                for (obj in detectedObjects) {
                    graphicOverlay.add(ObjectGraphic(graphicOverlay, obj))
                    if (obj.labels.any { it.text.equals(targetSign, true) }) {
                        found = true
                    }
                }
                graphicOverlay.postInvalidate()
                imgResult.visibility = if (found) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { /* opcional: logging */ }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
