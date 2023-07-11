package com.example.facedectector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.face.Face

class FaceBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val faceRectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val faceRects = mutableListOf<RectF>()

    fun setFaces(faces: List<Face>) {
        faceRects.clear()
        for (face in faces) {
            faceRects.add(face.boundingBox.toRectF())
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (rect in faceRects) {
            canvas.drawRect(rect, faceRectPaint)
        }
    }
}