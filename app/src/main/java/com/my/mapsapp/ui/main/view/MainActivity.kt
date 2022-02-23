package com.my.mapsapp.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
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

var mapView: MapView? = null
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

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

        viewModel.errorMessage.observe(this, {
        })

        viewModel.loading.observe(this, Observer {
            if (it) {
                binding.loadingTextView.visibility = View.VISIBLE
                binding.previousButton.visibility = View.GONE
                binding.nextButton.visibility = View.GONE
            } else {
                binding.loadingTextView.visibility = View.GONE
                binding.previousButton.visibility = View.VISIBLE
                binding.nextButton.visibility = View.VISIBLE
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
    }

    private fun addAnnotationToMap(
        location: Location,
        pointAnnotationManager: PointAnnotationManager?
    ) {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.red_marker
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(location.latlng[1], location.latlng[0]))
                .withIconImage(it)
            pointAnnotationManager?.deleteAll()
            pointAnnotationManager?.create(pointAnnotationOptions)
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