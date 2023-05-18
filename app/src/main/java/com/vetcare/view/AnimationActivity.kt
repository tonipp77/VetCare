package com.vetcare.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import com.vetcare.R
import com.vetcare.databinding.ActivityAnimationBinding
import com.vetcare.databinding.ActivityLoginBinding

class AnimationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // creamos un listener apuntando al motionlayout, e implementamos
        // la interfaz TransitionListener
        binding.mlAnimation.addTransitionListener(object : TransitionListener{
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {

            }
            // cuando se termina la animación ejecutamos el cambio de activity
            // si estamos en end de la animación
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {

                if (currentId == R.id.end){
                    moveToLoginActivity()
                }

            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {

            }

        })

    }
    // método para movernos a Login cerrando la activity actual
    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}