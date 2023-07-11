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
import android.widget.Toast
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.face.Face
import kotlin.math.ceil

class FaceBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val faceRectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val faceRects = mutableListOf<Rect>()
    private var imageRectWidth: Float = 0f
    private var imageRectHeight: Float = 0f

    fun setFaces(faces: List<Face>, imageRectWidth: Float, imageRectHeight: Float) {
        this.imageRectWidth = imageRectWidth
        this.imageRectHeight = imageRectHeight

        faceRects.clear()
        for (face in faces) {
            faceRects.add(face.boundingBox)
        }
        invalidate()
    }

    private fun getBoxRect(imageRectWidth: Float, imageRectHeight: Float, faceBoundingBox: Rect): RectF {
        val scaleX = width.toFloat() / imageRectHeight
        val scaleY = height.toFloat() / imageRectWidth
        val scale = scaleX.coerceAtLeast(scaleY)

        val offsetX = (width.toFloat() - ceil(imageRectHeight * scale)) / 2.0f
        val offsetY = (height.toFloat() - ceil(imageRectWidth * scale)) / 2.0f

        val mappedBox = RectF().apply {
            left = faceBoundingBox.right * scale + offsetX
            top = faceBoundingBox.top * scale + offsetY
            right = faceBoundingBox.left * scale + offsetX
            bottom = faceBoundingBox.bottom * scale + offsetY
        }

        val centerX = width.toFloat() / 2

        return mappedBox.apply {
            left = centerX + (centerX - left)
            right = centerX - (right - centerX)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (rect in faceRects) {
            val mappedRect = getBoxRect(imageRectWidth, imageRectHeight, rect)
            canvas.drawRect(mappedRect, faceRectPaint)
        }
    }
}