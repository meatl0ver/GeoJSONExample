package raui.imashev.geojsonexample

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import raui.imashev.geojsonexample.api.ApiFactory


class MainActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener,
    UserLocationObjectListener {


    private val CAMERA_TARGET = Point(42.82811107000001, 132.44988040500007)

    private lateinit var userLocationLayer: UserLocationLayer

    private lateinit var drivingRouter: DrivingRouter
    private lateinit var drivingSession: DrivingSession

    private val compositeDisposable = CompositeDisposable()
    private lateinit var mapview: MapView
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var animationHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("887084b3-ad0e-489f-b482-50c81114e417")
        MapKitFactory.initialize(this)
        DirectionsFactory.initialize(this);
        setContentView(R.layout.activity_main)
        val disposable = ApiFactory.apiService.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                printLines(it.features[0].geometry.coordinates[0][0])
            }, {
                Log.d("testtesttest", it.message.toString())
            })
        compositeDisposable.add(disposable)

        mapview = findViewById(R.id.mapview)
        mapview.map.move(
            //установка стартовой точки
            CameraPosition(CAMERA_TARGET, 11.0f, 0.0f, 0.0f)
        )
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapview.map.mapObjects.addCollection()
        animationHandler = Handler()


        //установка локации пользователя
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapview.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = false
        userLocationLayer.setObjectListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onStop() {
        super.onStop()
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    //метод для отрисовки пути
    private fun printLines(coordinates: List<List<Double>>) {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()

        val requestPoints: ArrayList<RequestPoint> = ArrayList()

        for (i in coordinates) {
            val a = i[1]
            val b = i[0]
            //отрисовка точек на пути
            printLocationPoint(a, b)
            requestPoints.add(
                RequestPoint(
                    Point(a, b),
                    RequestPointType.WAYPOINT,
                    null
                )
            )
        }
        drivingSession =
            drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
    }


    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            mapObjects.addPolyline(route.geometry)
        }
    }


    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = ""
        if (p0 is RemoteError) {
            errorMessage = "RemoteError"
        } else if (p0 is NetworkError) {
            errorMessage = "NetworkError"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    //метод для отрисовки точек на пути
    private fun printLocationPoint(a: Double, b: Double) {
        val mark = mapObjects.addPlacemark(Point(a + 0.0005, b + 0.0003))
        mark.opacity = 1f
        mark.setIcon(ImageProvider.fromResource(this, R.drawable.location_point))
        mark.isDraggable = false
        mapObjects.addCircle(Circle(Point(a, b), 22f), Color.BLUE, 2f, Color.BLUE)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF(
                (mapview.width() * 0.5).toFloat(),
                (mapview.height * 0.5).toFloat()
            ), PointF(
                (mapview.width() * 0.5).toFloat(),
                (mapview.height * 0.83).toFloat()
            )
        )

    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }


}
