package cl.pistolapiumpium.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class ShakeDetector(
    context: Context,
    private val onShakeCallback: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastTime: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && (curTime - lastTime) > 100) {
            val diffTime = curTime - lastTime
            lastTime = curTime
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000
            if (speed > 800) {
                onShakeCallback()
            }
            lastX = x; lastY = y; lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }
}
