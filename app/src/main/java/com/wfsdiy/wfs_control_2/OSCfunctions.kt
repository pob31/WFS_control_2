package com.wfsdiy.wfs_control_2

import android.content.Context

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.compareTo
import kotlin.times

fun getPaddedBytes(input: String, charsets: java.nio.charset.Charset = Charsets.UTF_8): ByteArray {
    val stringBytes = input.toByteArray(charsets)
    val lenWithNull = stringBytes.size + 1
    val paddedLen = (lenWithNull + 3) and (-4)
    val result = ByteArray(paddedLen)
    stringBytes.copyInto(result)
    return result
}

fun Int.toBytesBigEndian(): ByteArray {
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}

fun Float.toBytesBigEndian(): ByteArray {
    return this.toBits().toBytesBigEndian()
}

fun sendOscPosition(context: Context, markerId: Int, x: Float, y: Float, isCluster: Boolean = false) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()
            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }
            val (canvasWidth, canvasHeight) = CanvasDimensions.getCurrentDimensions()
            val markerRadius = CanvasDimensions.getCurrentMarkerRadius()
            // Map 0.0-1.0 range to effective drawing area (canvas minus marker diameter)
            val effectiveWidth = canvasWidth - (markerRadius * 2f)
            val effectiveHeight = canvasHeight - (markerRadius * 2f)
            val normalizedX = if (effectiveWidth > 0f) (x - markerRadius) / effectiveWidth else 0f
            val normalizedY = if (effectiveHeight > 0f) 1f - ((y - markerRadius) / effectiveHeight) else 0f
            val addressPattern = if (isCluster) "/cluster/positionXY" else "/marker/positionXY"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            val typeTagBytes = getPaddedBytes(",iff")
            val markerIdBytes = markerId.toBytesBigEndian()
            val normalizedXBytes = normalizedX.toBytesBigEndian()
            val normalizedYBytes = normalizedY.toBytesBigEndian()
            val oscPacketBytes = addressPatternBytes + typeTagBytes + markerIdBytes + normalizedXBytes + normalizedYBytes
            DatagramSocket().use { socket ->
                val address = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, address, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscClusterZ(context: Context, ClusterId: Int, normalizedZ: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPattern = "/cluster/positionZ"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            // OSC type tag for an integer (clusterID) followed by a float (normalizedZ)
            val typeTagBytes = getPaddedBytes(",if")
            val ClusterIdBytes = ClusterId.toBytesBigEndian()
            val normalizedZBytes = normalizedZ.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + ClusterIdBytes + normalizedZBytes

            DatagramSocket().use { socket ->
                val address = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, address, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscArrayAdjustCommand(context: Context, oscAddress: String, arrayId: Int, value: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPatternBytes = getPaddedBytes(oscAddress)
            // Type tag for an integer (arrayId) followed by a float (value)
            val typeTagBytes = getPaddedBytes(",if")
            val arrayIdBytes = arrayId.toBytesBigEndian()
            val valueBytes = value.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + arrayIdBytes + valueBytes

            DatagramSocket().use { socket ->
                val inetAddress = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, inetAddress, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscMarkerOrientation(context: Context, markerId: Int, angle: Int) {
    android.util.Log.d("OSC", "sendOscMarkerOrientation called: markerId=$markerId, angle=$angle")
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPattern = "/marker/orientation"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            // Type tag for two integers (markerId, angle)
            val typeTagBytes = getPaddedBytes(",ii")
            val markerIdBytes = markerId.toBytesBigEndian()
            val angleBytes = angle.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + markerIdBytes + angleBytes

            DatagramSocket().use { socket ->
                val inetAddress = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, inetAddress, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscMarkerDirectivity(context: Context, markerId: Int, directivity: Float) {
    android.util.Log.d("OSC", "sendOscMarkerDirectivity called: markerId=$markerId, directivity=$directivity")
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPattern = "/marker/directivity"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            // Type tag for integer and float (markerId, directivity)
            val typeTagBytes = getPaddedBytes(",if")
            val markerIdBytes = markerId.toBytesBigEndian()
            val directivityBytes = directivity.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + markerIdBytes + directivityBytes

            DatagramSocket().use { socket ->
                val inetAddress = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, inetAddress, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscClusterRotation(context: Context, clusterId: Int, angle: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPattern = "/cluster/rotation"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            // Type tag for an integer (clusterId) followed by a float (angle)
            val typeTagBytes = getPaddedBytes(",if")
            val clusterIdBytes = clusterId.toBytesBigEndian()
            val angleBytes = angle.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + clusterIdBytes + angleBytes

            DatagramSocket().use { socket ->
                val inetAddress = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, inetAddress, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendOscClusterScale(context: Context, clusterId: Int, factor: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                return@launch
            }

            val addressPattern = "/cluster/scale"
            val addressPatternBytes = getPaddedBytes(addressPattern)
            // Type tag for an integer (clusterId) followed by a float (factor)
            val typeTagBytes = getPaddedBytes(",if")
            val clusterIdBytes = clusterId.toBytesBigEndian()
            val factorBytes = factor.toBytesBigEndian()

            val oscPacketBytes = addressPatternBytes + typeTagBytes + clusterIdBytes + factorBytes

            DatagramSocket().use { socket ->
                val inetAddress = InetAddress.getByName(ipAddressStr)
                val packet = DatagramPacket(oscPacketBytes, oscPacketBytes.size, inetAddress, outgoingPort)
                socket.send(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun parseOscString(buffer: ByteBuffer): String {
    val bytes = mutableListOf<Byte>()
    while (buffer.hasRemaining()) {
        val byte = buffer.get()
        if (byte == 0.toByte()) break
        bytes.add(byte)
    }
    val currentPos = buffer.position()
    val align = (currentPos + 3) and -4
    if (align > currentPos) buffer.position(align)
    return String(bytes.toByteArray(), Charsets.UTF_8)
}

fun parseOscInt(buffer: ByteBuffer): Int {
    return buffer.int
}

fun parseOscFloat(buffer: ByteBuffer): Float {
    return buffer.float
}

typealias OscDataCallback = (id: Int, name: String?, position: Offset?, isCluster: Boolean) -> Unit
typealias OscStageDimensionCallback = (value: Float) -> Unit
typealias OscNumberOfInputsCallback = (count: Int) -> Unit
typealias OscClusterZCallback = (ClusterId: Int, normalizedZ: Float) -> Unit

fun parseAndProcessOscPacket(
    data: ByteArray,
    canvasWidth: Float,
    canvasHeight: Float,
    onOscDataReceived: OscDataCallback,
    onStageWidthChanged: OscStageDimensionCallback? = null,
    onStageDepthChanged: OscStageDimensionCallback? = null,
    onStageHeightChanged: OscStageDimensionCallback? = null,
    onStageOriginXChanged: OscStageDimensionCallback? = null,
    onStageOriginYChanged: OscStageDimensionCallback? = null,
    onStageOriginZChanged: OscStageDimensionCallback? = null,
    onNumberOfInputsChanged: OscNumberOfInputsCallback? = null,
    onClusterZChanged: OscClusterZCallback? = null
) {
    if (data.isEmpty()) {
        return
    }
    val buffer = ByteBuffer.wrap(data)
    buffer.order(ByteOrder.BIG_ENDIAN)

    try {
        val address = parseOscString(buffer)

        when {
            address == "/inputs" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",i") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val count = parseOscInt(buffer)
                if (count >= 0 && count <= MAX_INPUTS) { // Allow 0 to effectively disable inputs
                    onNumberOfInputsChanged?.invoke(count)
                } else {
                }
            }
            address == "/stage/width" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val width = parseOscFloat(buffer)
                onStageWidthChanged?.invoke(width)
            }
            address == "/stage/depth" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val depth = parseOscFloat(buffer)
                onStageDepthChanged?.invoke(depth)
            }
            address == "/stage/height" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {

                    return
                }
                if (buffer.remaining() < 4) {

                    return
                }
                val height = parseOscFloat(buffer)

                onStageHeightChanged?.invoke(height)
            }
            address == "/stage/originX" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val originX = parseOscFloat(buffer)
                onStageOriginXChanged?.invoke(originX)
            }
            address == "/stage/originY" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val originY = parseOscFloat(buffer)
                onStageOriginYChanged?.invoke(originY)
            }
            address == "/stage/originZ" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    return
                }
                if (buffer.remaining() < 4) {
                    return
                }
                val originZ = parseOscFloat(buffer)
                onStageOriginZChanged?.invoke(originZ)
            }
            address.startsWith("/marker/") || address.startsWith("/cluster/") -> {
                val isClusterMessage = address.startsWith("/cluster/")
                val baseAddress = if(isClusterMessage) address.removePrefix("/cluster/") else address.removePrefix("/marker/")

                when (baseAddress) {
                    "positionXY" -> {
                        if (!buffer.hasRemaining() || parseOscString(buffer) != ",iff") {

                            return
                        }
                        if (buffer.remaining() < 12) {

                            return
                        }
                        val id = parseOscInt(buffer)
                        val normX = parseOscFloat(buffer)
                        val normY = parseOscFloat(buffer)

                        if (canvasWidth > 0f && canvasHeight > 0f) {
                            // Map 0.0-1.0 range back to effective drawing area (canvas minus marker diameter)
                            val markerRadius = CanvasDimensions.getCurrentMarkerRadius()
                            val effectiveWidth = canvasWidth - (markerRadius * 2f)
                            val effectiveHeight = canvasHeight - (markerRadius * 2f)
                            val denormalizedX = (normX * effectiveWidth + markerRadius).coerceIn(markerRadius, canvasWidth - markerRadius)
                            val denormalizedY = ((1f - normY) * effectiveHeight + markerRadius).coerceIn(markerRadius, canvasHeight - markerRadius)
                            onOscDataReceived(id, null, Offset(denormalizedX, denormalizedY), isClusterMessage)
                        } else {

                        }
                    }
                    "name" -> {
                        if (isClusterMessage) {

                            return // ClusterMarkers don't have names
                        }
                        if (!buffer.hasRemaining()) {

                            return
                        }
                        val typeTags = parseOscString(buffer)


                        if (typeTags.startsWith(",i")) {
                            if (buffer.remaining() < 4) {

                                return
                            }
                            val id = parseOscInt(buffer)

                            if (typeTags == ",is") {
                                var newName = parseOscString(buffer)
                                newName = newName.take(24).trimEnd()

                                onOscDataReceived(id, newName, null, isClusterMessage)
                            } else if (typeTags == ",i") {

                                onOscDataReceived(id, "", null, isClusterMessage)
                            } else {

                            }
                        } else {

                        }
                    }
                    "positionZ" -> {
                        if (!isClusterMessage) {

                            return
                        }
                        // Expecting Type Tag: ",if" (integer for cluster ID, float for normalized Z)
                        if (!buffer.hasRemaining() || parseOscString(buffer) != ",if") {

                            return
                        }
                        if (buffer.remaining() < 8) { // 4 bytes for int (ID) + 4 bytes for float (Z)

                            return
                        }
                        val clusterId = parseOscInt(buffer)
                        val normalizedZ = parseOscFloat(buffer)

                        onClusterZChanged?.invoke(clusterId, normalizedZ) // <-- CALL THE NEW CALLBACK
                    }
                    else -> {

                    }
                }
            }
            else -> {

            }
        }
    } catch (e: Exception) {

        e.printStackTrace()
    }
}

fun startOscServer(
    context: Context,
    onOscDataReceived: OscDataCallback,
    onStageWidthChanged: OscStageDimensionCallback? = null,
    onStageDepthChanged: OscStageDimensionCallback? = null,
    onStageHeightChanged: OscStageDimensionCallback? = null,
    onStageOriginXChanged: OscStageDimensionCallback? = null,
    onStageOriginYChanged: OscStageDimensionCallback? = null,
    onStageOriginZChanged: OscStageDimensionCallback? = null,
    onNumberOfInputsChanged: OscNumberOfInputsCallback? = null,
    onClusterZChanged: OscClusterZCallback? = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        var serverSocket: DatagramSocket? = null
        try {
            val (incomingPortStr, _, _) = loadNetworkParameters(context)
            val incomingPort = incomingPortStr.toIntOrNull()
            if (incomingPort == null || !isValidPort(incomingPortStr)) {

                return@launch
            }
            serverSocket = DatagramSocket(incomingPort)
            serverSocket.soTimeout = 1000 // Set 1 second timeout to prevent blocking




            val buffer = ByteArray(1024)
            while (isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    serverSocket.receive(packet)
                    val receivedData = packet.data.copyOf(packet.length)
                    val remoteAddress = packet.address.hostAddress

                    val (canvasWidth, canvasHeight) = CanvasDimensions.getCurrentDimensions()
                    parseAndProcessOscPacket(
                        receivedData,
                        canvasWidth,
                        canvasHeight,
                        onOscDataReceived,
                        onStageWidthChanged,
                        onStageDepthChanged,
                        onStageHeightChanged,
                        onStageOriginXChanged,
                        onStageOriginYChanged,
                        onStageOriginZChanged,
                        onNumberOfInputsChanged,
                        onClusterZChanged
                    )
                } catch (e: java.net.SocketTimeoutException) {

                    continue
                } catch (e: IOException) {

                    break
                } catch (e: Exception) {

                }
            }
        } catch (e: java.net.SocketException) {

        } catch (e: Exception) {

            e.printStackTrace()
        } finally {
            serverSocket?.close()

        }
    }
}

fun isValidPort(port: String): Boolean {
    val portNumber = port.toIntOrNull()
    return portNumber != null && portNumber in 1..65535
}

fun isValidIpAddress(ip: String): Boolean {
    val ipRegex = Regex("""^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""")
    return ipRegex.matches(ip)
}
