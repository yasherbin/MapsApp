package com.my.mapsapp.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.my.mapsapp.R
import com.my.mapsapp.data.api.RetrofitBuilder
import com.my.mapsapp.data.model.Location
import com.my.mapsapp.data.repository.MainRepository
import com.my.mapsapp.databinding.ActivityMainBinding
import com.my.mapsapp.ui.base.ViewModelFactory
import com.my.mapsapp.ui.main.viewmodel.MainViewModel

class MainActivity : FragmentActivity(),
    FailureDialogFragment.FailureDialogListener {

    private lateinit var viewModel: MainViewModel
    var mapView: MapView? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val retrofitService = RetrofitBuilder.apiService
        val mainRepository = MainRepository(retrofitService)
        viewModel =
            ViewModelProvider(this, ViewModelFactory(mainRepository)).get(MainViewModel::class.java)
        mapView = binding.mapView
        val annotationApi = mapView?.annotations
        val pointAnnotationManager = annotationApi?.createPointAnnotationManager()

        viewModel.isError.observe(this) {
            if (it) {
                pointAnnotationManager?.deleteAll()
                binding.cardView.visibility = View.GONE
                binding.bottomPane.visibility = View.GONE
                binding.refreshButton.visibility = View.GONE
                showFailureDialog()
            }
        }
        viewModel.loading.observe(this, Observer {
            if (it) {
                binding.cardView.visibility = View.VISIBLE
                binding.loadingTextView.visibility = View.VISIBLE
                binding.bottomPane.visibility = View.GONE
                binding.refreshButton.visibility = View.GONE
            } else {
                binding.loadingTextView.visibility = View.GONE
                binding.bottomPane.visibility = View.VISIBLE
                binding.refreshButton.visibility = View.VISIBLE
            }
        })
        viewModel.locationsList.observe(this) { list ->
            mapView?.getMapboxMap()?.loadStyleUri(
                Style.MAPBOX_STREETS
            ) {
                var index = 0
                addAnnotationToMap(list[index], pointAnnotationManager)
                binding.previousButton.setOnClickListener {
                    if (index == 0) index = list.size - 1
                    else index--
                    addAnnotationToMap(list[index], pointAnnotationManager)
                }
                binding.nextButton.setOnClickListener {
                    if (index == list.size - 1) index = 0
                    else index++
                    addAnnotationToMap(list[index], pointAnnotationManager)
                }
            }
        }
        viewModel.getLocations()
        binding.refreshButton.setOnClickListener {
            viewModel.getLocations()
        }
    }

    private fun addAnnotationToMap(
        location: Location,
        pointAnnotationManager: PointAnnotationManager?
    ) {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.red_marker
        )?.let {
            val point = Point.fromLngLat(location.latlng[1], location.latlng[0])
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
            pointAnnotationManager?.deleteAll()
            pointAnnotationManager?.create(pointAnnotationOptions)
            val cameraPosition = CameraOptions.Builder()
                .zoom(3.0)
                .center(point)
                .build()
            mapView?.getMapboxMap()?.flyTo(cameraPosition)
            binding.nameView.text = location.name
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {

            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun showFailureDialog() {
        val dialog = FailureDialogFragment()
        dialog.show(supportFragmentManager, "FailureDialogFragment")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        viewModel.getLocations()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        binding.refreshButton.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}