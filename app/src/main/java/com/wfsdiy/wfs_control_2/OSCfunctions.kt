package com.wfsdiy.wfs_control_2

import android.content.Context
import android.util.Log
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

fun sendOscPosition(context: Context, markerId: Int, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float, isCluster: Boolean = false) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()
            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                Log.e("OSC_MESSAGE", "Invalid or out-of-range outgoing port: $outgoingPortStr. OSC message not sent.")
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                Log.e("OSC_MESSAGE", "Invalid or blank IP Address: '$ipAddressStr'. OSC message not sent.")
                return@launch
            }
            val normalizedX = if (canvasWidth > 0) x / canvasWidth else 0f
            val normalizedY = if (canvasHeight > 0) y / canvasHeight else 0f
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
                Log.d("OSC_MESSAGE", "OSC position packet sent to $ipAddressStr:$outgoingPort for ID $markerId (isCluster: $isCluster).")
            }
        } catch (e: Exception) {
            Log.e("OSC_MESSAGE", "Error sending OSC position packet: ${e.message}", e)
        }
    }
}

fun sendOscClusterZ(context: Context, ClusterId: Int, normalizedZ: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                Log.e("OSC_MESSAGE", "Invalid outgoing port for ClusterZ: $outgoingPortStr. Message not sent.")
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                Log.e("OSC_MESSAGE", "Invalid IP Address for ClusterZ: '$ipAddressStr'. Message not sent.")
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
                Log.d("OSC_MESSAGE", "OSC ClusterZ packet sent to $ipAddressStr:$outgoingPort for ClusterID $ClusterId (Z: $normalizedZ).")
            }
        } catch (e: Exception) {
            Log.e("OSC_MESSAGE", "Error sending OSC ClusterZ packet: ${e.message}", e)
        }
    }
}

fun sendOscArrayAdjustCommand(context: Context, oscAddress: String, arrayId: Int, value: Float) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (_, outgoingPortStr, ipAddressStr) = loadNetworkParameters(context)
            val outgoingPort = outgoingPortStr.toIntOrNull()

            if (outgoingPort == null || !isValidPort(outgoingPortStr)) {
                Log.e("OSC_MESSAGE", "Invalid outgoing port for ArrayAdjust: $outgoingPortStr. Message not sent.")
                return@launch
            }
            if (ipAddressStr.isBlank() || !isValidIpAddress(ipAddressStr)) {
                Log.e("OSC_MESSAGE", "Invalid IP Address for ArrayAdjust: '$ipAddressStr'. Message not sent.")
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
                Log.d("OSC_MESSAGE", "OSC ArrayAdjust packet sent to $ipAddressStr:$outgoingPort - Address: $oscAddress, ArrayID: $arrayId, Value: $value")
            }
        } catch (e: Exception) {
            Log.e("OSC_MESSAGE", "Error sending OSC ArrayAdjust packet: ${e.message}", e)
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
    onNumberOfInputsChanged: OscNumberOfInputsCallback? = null,
    onClusterZChanged: OscClusterZCallback? = null
) {
    if (data.isEmpty()) {
        Log.w("OSC_PARSE", "Empty data packet received.")
        return
    }
    val buffer = ByteBuffer.wrap(data)
    buffer.order(ByteOrder.BIG_ENDIAN)

    try {
        val address = parseOscString(buffer)
        Log.d("OSC_PARSE", "Received OSC Address: $address")

        when {
            address == "/inputs" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",i") {
                    Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',i'.")
                    return
                }
                if (buffer.remaining() < 4) {
                    Log.w("OSC_PARSE", "Not enough data for integer argument for $address.")
                    return
                }
                val count = parseOscInt(buffer)
                Log.i("OSC_PARSE", "Parsed Number of Inputs: $count")
                if (count >= 0 && count <= MAX_INPUTS) { // Allow 0 to effectively disable inputs
                    onNumberOfInputsChanged?.invoke(count)
                } else {
                    Log.w("OSC_PARSE", "Received invalid number of inputs: $count. Must be between 0 and $MAX_INPUTS.")
                }
            }
            address == "/stage/width" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',f'.")
                    return
                }
                if (buffer.remaining() < 4) {
                    Log.w("OSC_PARSE", "Not enough data for float argument for $address.")
                    return
                }
                val width = parseOscFloat(buffer)
                Log.i("OSC_PARSE", "Parsed Stage Width: $width")
                onStageWidthChanged?.invoke(width)
            }
            address == "/stage/depth" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',f'.")
                    return
                }
                if (buffer.remaining() < 4) {
                    Log.w("OSC_PARSE", "Not enough data for float argument for $address.")
                    return
                }
                val depth = parseOscFloat(buffer)
                Log.i("OSC_PARSE", "Parsed Stage Depth: $depth")
                onStageDepthChanged?.invoke(depth)
            }
            address == "/stage/height" -> {
                if (!buffer.hasRemaining() || parseOscString(buffer) != ",f") {
                    Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',f'.")
                    return
                }
                if (buffer.remaining() < 4) {
                    Log.w("OSC_PARSE", "Not enough data for float argument for $address.")
                    return
                }
                val height = parseOscFloat(buffer)
                Log.i("OSC_PARSE", "Parsed Stage Height: $height")
                onStageHeightChanged?.invoke(height)
            }
            address.startsWith("/marker/") || address.startsWith("/cluster/") -> {
                val isClusterMessage = address.startsWith("/cluster/")
                val baseAddress = if(isClusterMessage) address.removePrefix("/cluster/") else address.removePrefix("/marker/")

                when (baseAddress) {
                    "positionXY" -> {
                        if (!buffer.hasRemaining() || parseOscString(buffer) != ",iff") {
                            Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',iff'.")
                            return
                        }
                        if (buffer.remaining() < 12) {
                            Log.w("OSC_PARSE", "Not enough data for iff arguments for $address. Remaining: ${buffer.remaining()}")
                            return
                        }
                        val id = parseOscInt(buffer)
                        val normX = parseOscFloat(buffer)
                        val normY = parseOscFloat(buffer)
                        Log.i("OSC_PARSE", "Parsed Position - ID: $id, NormX: $normX, NormY: $normY, IsCluster: $isClusterMessage")
                        if (canvasWidth > 0f && canvasHeight > 0f) {
                            val denormalizedX = (normX * canvasWidth).coerceIn(0f, canvasWidth)
                            val denormalizedY = (normY * canvasHeight).coerceIn(0f, canvasHeight)
                            onOscDataReceived(id, null, Offset(denormalizedX, denormalizedY), isClusterMessage)
                        } else {
                            Log.w("OSC_PARSE", "Canvas dimensions zero ($canvasWidth x $canvasHeight), cannot denormalize position for ID $id.")
                        }
                    }
                    "name" -> {
                        if (isClusterMessage) {
                            Log.d("OSC_PARSE", "Ignoring name message for cluster marker: $address")
                            return // ClusterMarkers don't have names
                        }
                        if (!buffer.hasRemaining()) {
                            Log.w("OSC_PARSE", "No type tags found after $address address.")
                            return
                        }
                        val typeTags = parseOscString(buffer)
                        Log.d("OSC_PARSE", "Received OSC Type Tags for $address: $typeTags")

                        if (typeTags.startsWith(",i")) {
                            if (buffer.remaining() < 4) {
                                Log.w("OSC_PARSE", "Not enough data for ID for $address. Remaining: ${buffer.remaining()}")
                                return
                            }
                            val id = parseOscInt(buffer)

                            if (typeTags == ",is") {
                                var newName = parseOscString(buffer)
                                newName = newName.take(24).trimEnd()
                                Log.i("OSC_PARSE", "Parsed Name - ID: $id, New Name: '$newName', IsCluster: $isClusterMessage")
                                onOscDataReceived(id, newName, null, isClusterMessage)
                            } else if (typeTags == ",i") {
                                Log.i("OSC_PARSE", "Parsed Name (Blank) - ID: $id, IsCluster: $isClusterMessage")
                                onOscDataReceived(id, "", null, isClusterMessage)
                            } else {
                                Log.w("OSC_PARSE", "Ignoring $address message with unhandled type tags: $typeTags")
                            }
                        } else {
                            Log.w("OSC_PARSE", "Ignoring $address message with unexpected initial type tag: $typeTags")
                        }
                    }
                    "positionZ" -> {
                        if (!isClusterMessage) {
                            Log.w("OSC_PARSE", "Received /marker/positionZ. This app handles /cluster/positionZ. Ignoring.")
                            return
                        }
                        // Expecting Type Tag: ",if" (integer for cluster ID, float for normalized Z)
                        if (!buffer.hasRemaining() || parseOscString(buffer) != ",if") {
                            Log.w("OSC_PARSE", "Ignoring $address message with incorrect or missing type tag. Expected ',if'.")
                            return
                        }
                        if (buffer.remaining() < 8) { // 4 bytes for int (ID) + 4 bytes for float (Z)
                            Log.w("OSC_PARSE", "Not enough data for if arguments for $address. Remaining: ${buffer.remaining()}")
                            return
                        }
                        val clusterId = parseOscInt(buffer)
                        val normalizedZ = parseOscFloat(buffer)
                        Log.i("OSC_PARSE", "Parsed Cluster Z - ID: $clusterId, NormZ: $normalizedZ")
                        onClusterZChanged?.invoke(clusterId, normalizedZ) // <-- CALL THE NEW CALLBACK
                    }
                    else -> {
                        Log.d("OSC_PARSE", "Ignoring message with unhandled marker/cluster address part: $baseAddress (Full: $address)")
                    }
                }
            }
            else -> {
                Log.d("OSC_PARSE", "Ignoring message with unhandled OSC address: $address")
            }
        }
    } catch (e: Exception) {
        Log.e("OSC_PARSE", "Error parsing OSC packet: ${e.message}", e)
        e.printStackTrace()
    }
}

fun startOscServer(
    context: Context,
    getCanvasDimensions: () -> Pair<Float, Float>,
    onOscDataReceived: OscDataCallback,
    onStageWidthChanged: OscStageDimensionCallback? = null,
    onStageDepthChanged: OscStageDimensionCallback? = null,
    onStageHeightChanged: OscStageDimensionCallback? = null,
    onNumberOfInputsChanged: OscNumberOfInputsCallback? = null,
    onClusterZChanged: OscClusterZCallback? = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        var serverSocket: DatagramSocket? = null
        try {
            val (incomingPortStr, _, _) = loadNetworkParameters(context)
            val incomingPort = incomingPortStr.toIntOrNull()
            if (incomingPort == null || !isValidPort(incomingPortStr)) {
                Log.e("OSC_SERVER", "Invalid or out-of-range incoming port for OSC server: $incomingPortStr. Server not started.")
                return@launch
            }
            serverSocket = DatagramSocket(incomingPort)
            Log.i("OSC_SERVER", "OSC Server started and listening on port $incomingPort")
            val buffer = ByteArray(1024)
            while (isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    serverSocket.receive(packet)
                    val receivedData = packet.data.copyOf(packet.length)
                    val remoteAddress = packet.address.hostAddress
                    Log.i("OSC_SERVER", "Received UDP packet from $remoteAddress, length: ${packet.length} bytes.")
                    val (canvasWidth, canvasHeight) = getCanvasDimensions()
                    parseAndProcessOscPacket(
                        receivedData,
                        canvasWidth,
                        canvasHeight,
                        onOscDataReceived,
                        onStageWidthChanged,
                        onStageDepthChanged,
                        onStageHeightChanged,
                        onNumberOfInputsChanged,
                        onClusterZChanged
                    )
                } catch (e: java.net.SocketTimeoutException) {
                    Log.d("OSC_SERVER", "Socket timeout, checking active status.")
                    continue
                } catch (e: IOException) {
                    if (isActive) Log.e("OSC_SERVER", "IOException during socket.receive: ${e.message}", e)
                    break
                } catch (e: Exception) {
                    if (isActive) Log.e("OSC_SERVER", "Error processing received packet: ${e.message}", e)
                }
            }
        } catch (e: java.net.SocketException) {
            Log.e("OSC_SERVER", "SocketException (e.g., port $serverSocket?.localPort already in use): ${e.message}", e)
        } catch (e: Exception) {
            Log.e("OSC_SERVER", "Failed to start or run OSC server: ${e.message}", e)
            e.printStackTrace()
        } finally {
            serverSocket?.close()
            Log.i("OSC_SERVER", "OSC Server stopped.")
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