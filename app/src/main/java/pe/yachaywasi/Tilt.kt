package pe.yachaywasi

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

/**
 * Lectura REAL del acelerómetro (TYPE_ACCELEROMETER) con filtro pasa-bajos,
 * calibración de posición neutral y zona muerta. La pantalla está fija en vertical.
 */
class Tilt(private val sm: SensorManager?) : SensorEventListener {
    private val sensor: Sensor? = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val available: Boolean get() = sensor != null

    @Volatile var fx = 0f
    @Volatile var fy = 0f
    @Volatile var nx = 0f
    @Volatile var ny = 0f
    private var has = false

    fun start() { if (sensor != null) sm?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME) }
    fun stop() { sm?.unregisterListener(this); has = false }
    fun calibrate() { nx = fx; ny = fy }

    override fun onSensorChanged(e: SensorEvent) {
        val x = e.values[0]
        val y = e.values[1]
        if (!has) { fx = x; fy = y; nx = x; ny = y; has = true }
        else { fx += ALPHA * (x - fx); fy += ALPHA * (y - fy) }
    }
    override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}

    /**
     * Vector en pantalla, cada eje en [-1,1] (+x derecha, +y abajo).
     * Inclinar a la derecha -> x del sensor baja -> Tito a la derecha.
     * Inclinar hacia adelante (borde superior abajo) -> y del sensor baja -> Tito sube (avanza).
     */
    fun vector(dead: Float = 0.9f, full: Float = 5f): Pair<Float, Float> =
        Pair(shape(-(fx - nx), dead, full), shape(fy - ny, dead, full))

    companion object {
        const val ALPHA = 0.15f
        fun shape(v: Float, dead: Float, full: Float): Float {
            val a = abs(v)
            if (a < dead) return 0f
            return sign(v) * min(1f, (a - dead) / (full - dead))
        }
    }
}

/** El sensor se registra solo mientras la pantalla está visible (RESUMED) y se libera en PAUSE. */
@Composable
fun rememberTilt(active: Boolean = true): Tilt {
    val ctx = LocalContext.current
    val tilt = remember { Tilt(ctx.getSystemService(Context.SENSOR_SERVICE) as? SensorManager) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, active) {
        val obs = LifecycleEventObserver { _, ev ->
            if (ev == Lifecycle.Event.ON_RESUME && active) tilt.start()
            else if (ev == Lifecycle.Event.ON_PAUSE) tilt.stop()
        }
        owner.lifecycle.addObserver(obs)
        if (active && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) tilt.start()
        onDispose { owner.lifecycle.removeObserver(obs); tilt.stop() }
    }
    return tilt
}
