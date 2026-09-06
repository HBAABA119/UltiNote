package com.example.data.local

import com.example.data.model.DrawingLayer
import com.example.data.model.DrawingStroke
import com.example.data.model.LayerBlendMode
import com.example.data.model.PhotoAnnotation
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.StickerAnnotation
import com.example.data.model.StrokePoint
import com.example.data.model.TextAnnotation
import com.example.data.model.ToolType
import org.json.JSONArray
import org.json.JSONObject

object SerializationHelpers {

    fun strokesToJson(strokes: List<DrawingStroke>): String {
        val array = JSONArray()
        for (stroke in strokes) {
            val obj = JSONObject()
            obj.put("id", stroke.id)
            obj.put("color", stroke.color)
            obj.put("strokeWidth", stroke.strokeWidth.toDouble())
            obj.put("toolType", stroke.toolType.name)
            obj.put("alpha", stroke.alpha.toDouble())
            obj.put("isHighlighter", stroke.isHighlighter)
            obj.put("layerId", stroke.layerId)

            val pointsArray = JSONArray()
            for (p in stroke.points) {
                val pObj = JSONObject()
                pObj.put("x", p.x.toDouble())
                pObj.put("y", p.y.toDouble())
                pObj.put("p", p.pressure.toDouble())
                pObj.put("t", p.timestamp)
                pointsArray.put(pObj)
            }
            obj.put("points", pointsArray)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToStrokes(json: String?): List<DrawingStroke> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        val list = mutableListOf<DrawingStroke>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val color = obj.optLong("color", 0xFF000000L)
                val strokeWidth = obj.optDouble("strokeWidth", 3.0).toFloat()
                val toolTypeName = obj.optString("toolType", ToolType.PEN_BALLPOINT.name)
                val toolType = try {
                    ToolType.valueOf(toolTypeName)
                } catch (e: Exception) {
                    ToolType.PEN_BALLPOINT
                }
                val alpha = obj.optDouble("alpha", 1.0).toFloat()
                val isHighlighter = obj.optBoolean("isHighlighter", false)
                val layerId = obj.optString("layerId", "default")

                val points = mutableListOf<StrokePoint>()
                val pointsArray = obj.optJSONArray("points")
                if (pointsArray != null) {
                    for (j in 0 until pointsArray.length()) {
                        val pObj = pointsArray.getJSONObject(j)
                        val x = pObj.optDouble("x", 0.0).toFloat()
                        val y = pObj.optDouble("y", 0.0).toFloat()
                        val p = pObj.optDouble("p", 1.0).toFloat()
                        val t = pObj.optLong("t", 0L)
                        points.add(StrokePoint(x, y, p, t))
                    }
                }
                list.add(
                    DrawingStroke(
                        id = id,
                        points = points,
                        color = color,
                        strokeWidth = strokeWidth,
                        toolType = toolType,
                        alpha = alpha,
                        isHighlighter = isHighlighter,
                        layerId = layerId
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun textBlocksToJson(blocks: List<TextAnnotation>): String {
        val array = JSONArray()
        for (b in blocks) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("text", b.text)
            obj.put("x", b.x.toDouble())
            obj.put("y", b.y.toDouble())
            obj.put("fontSize", b.fontSize.toDouble())
            obj.put("color", b.color)
            obj.put("isBold", b.isBold)
            obj.put("layerId", b.layerId)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToTextBlocks(json: String?): List<TextAnnotation> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        val list = mutableListOf<TextAnnotation>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TextAnnotation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        fontSize = obj.optDouble("fontSize", 18.0).toFloat(),
                        color = obj.optLong("color", 0xFF2D3748L),
                        isBold = obj.optBoolean("isBold", false),
                        layerId = obj.optString("layerId", "default")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun shapesToJson(shapes: List<ShapeAnnotation>): String {
        val array = JSONArray()
        for (s in shapes) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("type", s.type.name)
            obj.put("startX", s.startX.toDouble())
            obj.put("startY", s.startY.toDouble())
            obj.put("endX", s.endX.toDouble())
            obj.put("endY", s.endY.toDouble())
            obj.put("strokeWidth", s.strokeWidth.toDouble())
            obj.put("color", s.color)
            obj.put("isFilled", s.isFilled)
            obj.put("layerId", s.layerId)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToShapes(json: String?): List<ShapeAnnotation> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        val list = mutableListOf<ShapeAnnotation>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeName = obj.optString("type", ShapeType.LINE.name)
                val shapeType = try {
                    ShapeType.valueOf(typeName)
                } catch (e: Exception) {
                    ShapeType.LINE
                }
                list.add(
                    ShapeAnnotation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        type = shapeType,
                        startX = obj.optDouble("startX", 0.0).toFloat(),
                        startY = obj.optDouble("startY", 0.0).toFloat(),
                        endX = obj.optDouble("endX", 0.0).toFloat(),
                        endY = obj.optDouble("endY", 0.0).toFloat(),
                        strokeWidth = obj.optDouble("strokeWidth", 3.0).toFloat(),
                        color = obj.optLong("color", 0xFF2D3748L),
                        isFilled = obj.optBoolean("isFilled", false),
                        layerId = obj.optString("layerId", "default")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun stickersToJson(stickers: List<StickerAnnotation>): String {
        val array = JSONArray()
        for (s in stickers) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("stickerKey", s.stickerKey)
            obj.put("x", s.x.toDouble())
            obj.put("y", s.y.toDouble())
            obj.put("scale", s.scale.toDouble())
            obj.put("rotation", s.rotation.toDouble())
            obj.put("layerId", s.layerId)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToStickers(json: String?): List<StickerAnnotation> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        val list = mutableListOf<StickerAnnotation>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StickerAnnotation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        stickerKey = obj.optString("stickerKey", "star"),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        layerId = obj.optString("layerId", "default")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun layersToJson(layers: List<DrawingLayer>): String {        val array = JSONArray()
        for (l in layers) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("name", l.name)
            obj.put("isVisible", l.isVisible)
            obj.put("opacity", l.opacity.toDouble())
            obj.put("blendMode", l.blendMode.name)
            obj.put("isLocked", l.isLocked)
            obj.put("order", l.order)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToLayers(json: String?): List<DrawingLayer> {
        if (json.isNullOrBlank() || json == "[]") {
            return listOf(
                DrawingLayer(
                    id = "default",
                    name = "Base Layer",
                    isVisible = true,
                    opacity = 1.0f,
                    blendMode = LayerBlendMode.NORMAL,
                    isLocked = false,
                    order = 0
                )
            )
        }
        val list = mutableListOf<DrawingLayer>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val name = obj.optString("name", "Layer ${i + 1}")
                val isVisible = obj.optBoolean("isVisible", true)
                val opacity = obj.optDouble("opacity", 1.0).toFloat()
                val blendModeName = obj.optString("blendMode", LayerBlendMode.NORMAL.name)
                val blendMode = try {
                    LayerBlendMode.valueOf(blendModeName)
                } catch (e: Exception) {
                    LayerBlendMode.NORMAL
                }
                val isLocked = obj.optBoolean("isLocked", false)
                val order = obj.optInt("order", i)

                list.add(
                    DrawingLayer(
                        id = id,
                        name = name,
                        isVisible = isVisible,
                        opacity = opacity,
                        blendMode = blendMode,
                        isLocked = isLocked,
                        order = order
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (list.isEmpty()) {
            list.add(DrawingLayer(id = "default", name = "Base Layer", isVisible = true, opacity = 1.0f, blendMode = LayerBlendMode.NORMAL, isLocked = false, order = 0))
        }
        return list.sortedBy { it.order }
    }

    fun photosToJson(photos: List<PhotoAnnotation>): String {
        val array = JSONArray()
        for (p in photos) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("filePath", p.filePath)
            obj.put("x", p.x.toDouble())
            obj.put("y", p.y.toDouble())
            obj.put("width", p.width.toDouble())
            obj.put("height", p.height.toDouble())
            obj.put("rotation", p.rotation.toDouble())
            obj.put("layerId", p.layerId)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToPhotos(json: String?): List<PhotoAnnotation> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        val list = mutableListOf<PhotoAnnotation>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PhotoAnnotation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        filePath = obj.optString("filePath", ""),
                        x = obj.optDouble("x", 120.0).toFloat(),
                        y = obj.optDouble("y", 200.0).toFloat(),
                        width = obj.optDouble("width", 600.0).toFloat(),
                        height = obj.optDouble("height", 450.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        layerId = obj.optString("layerId", "default")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
