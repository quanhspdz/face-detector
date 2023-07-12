package com.example.facedectector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
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

    private val faceFeaturePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 5f
    }

    private val faceRects = mutableListOf<Rect>()
    private var imageRectWidth: Float = 0f
    private var imageRectHeight: Float = 0f
    private var isBackCam: Boolean = true
    private var faceList = mutableListOf<Face>()

    fun setFaces(faces: List<Face>, imageRectWidth: Float, imageRectHeight: Float, isBackCam: Boolean) {
        this.imageRectWidth = imageRectWidth
        this.imageRectHeight = imageRectHeight
        this.isBackCam = isBackCam

        faceRects.clear()
        faceList.clear()
        for (face in faces) {
            faceRects.add(face.boundingBox)
            faceList.add(face)
        }
        invalidate()
    }

    private fun getBoxRect(imageRectWidth: Float, imageRectHeight: Float, faceBoundingBox: Rect, isBackCam: Boolean): RectF {
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

        return if (isBackCam) {
            mappedBox
        } else {
            mappedBox
                .apply {
                    left = centerX + (centerX - left)
                    right = centerX - (right - centerX)
                }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (face in faceList) {
            val mappedRect = getBoxRect(imageRectWidth, imageRectHeight, face.boundingBox, isBackCam)
            canvas.drawRect(mappedRect, faceRectPaint)
            drawFaceFeatures(canvas, face)
        }
    }

    private fun drawFaceFeatures(canvas: Canvas, face: Face) {
        val faceContours = mutableListOf<List<PointF>>()

        val contourTypes = listOf(
            FaceContour.FACE,
            FaceContour.LEFT_EYEBROW_TOP,
            FaceContour.LEFT_EYEBROW_BOTTOM,
            FaceContour.RIGHT_EYEBROW_TOP,
            FaceContour.RIGHT_EYEBROW_BOTTOM,
            FaceContour.LEFT_EYE,
            FaceContour.RIGHT_EYE,
            FaceContour.UPPER_LIP_TOP,
            FaceContour.UPPER_LIP_BOTTOM,
            FaceContour.LOWER_LIP_TOP,
            FaceContour.LOWER_LIP_BOTTOM,
            FaceContour.NOSE_BRIDGE,
            FaceContour.NOSE_BOTTOM
        )

        for (contourType in contourTypes) {
            val contour = face.getContour(contourType)
            val points = contour?.points
            if (points != null) {
                faceContours.add(points)
            }
        }

        faceFeaturePaint.color = ColorUtils.setAlphaComponent(faceFeaturePaint.color, 128)

        for (contourPoints in faceContours) {
            val path = Path()
            for (i in contourPoints.indices) {
                val point = contourPoints[i]
                val mappedPoint = PointF(
                    getMappedX(point.x, face.boundingBox),
                    getMappedY(point.y, face.boundingBox)
                )
                if (i == 0) {
                    path.moveTo(mappedPoint.x, mappedPoint.y)
                } else {
                    path.lineTo(mappedPoint.x, mappedPoint.y)
                }
            }
            path.close()
            canvas.drawPath(path, faceFeaturePaint)
        }
    }

    private fun getMappedX(x: Float, faceBoundingBox: Rect): Float {
        val boxRect = getBoxRect(imageRectWidth, imageRectHeight, faceBoundingBox, isBackCam)
        val mappedX = if (isBackCam) {
            boxRect.left + (x - faceBoundingBox.left) * boxRect.width() / faceBoundingBox.width()
        } else {
            boxRect.right - (x - faceBoundingBox.left) * boxRect.width() / faceBoundingBox.width()
        }
        return mappedX
    }

    private fun getMappedY(y: Float, faceBoundingBox: Rect): Float {
        val boxRect = getBoxRect(imageRectWidth, imageRectHeight, faceBoundingBox, isBackCam)
        return boxRect.top + (y - faceBoundingBox.top) * boxRect.height() / faceBoundingBox.height()
    }
}