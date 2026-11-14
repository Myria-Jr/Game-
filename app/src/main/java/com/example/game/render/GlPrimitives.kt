package com.example.game.render

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GlShaderProgram {
    private val programId: Int

    val aPosition: Int
    val aNormal: Int
    val uMvpMatrix: Int
    val uModelMatrix: Int
    val uColor: Int
    val uEmissive: Int
    val uFogColor: Int
    val uFogDensity: Int
    val uCameraPos: Int

    init {
        val vertexSrc = """
            uniform mat4 uMvpMatrix;
            uniform mat4 uModelMatrix;
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            varying vec3 vNormal;
            varying vec3 vWorldPos;
            void main() {
                vec4 worldPos = uModelMatrix * vec4(aPosition, 1.0);
                vWorldPos = worldPos.xyz;
                vNormal = mat3(uModelMatrix) * aNormal;
                gl_Position = uMvpMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        val fragmentSrc = """
            precision mediump float;
            uniform vec4 uColor;
            uniform float uEmissive;
            uniform vec3 uFogColor;
            uniform float uFogDensity;
            uniform vec3 uCameraPos;
            varying vec3 vNormal;
            varying vec3 vWorldPos;
            void main() {
                vec3 norm = normalize(vNormal);
                vec3 lightDir = normalize(vec3(0.42, 0.85, 0.32));
                float diff = max(dot(norm, lightDir), 0.0);
                float lighting = clamp(0.42 + diff * 0.68 + uEmissive, 0.0, 1.6);
                vec3 litColor = uColor.rgb * lighting;
                float dist = distance(uCameraPos, vWorldPos);
                float fogFactor = clamp(exp(-pow(dist * uFogDensity, 1.6)), 0.0, 1.0);
                vec3 finalRgb = mix(uFogColor, litColor, fogFactor);
                gl_FragColor = vec4(finalRgb, uColor.a);
            }
        """.trimIndent()

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexSrc)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }

        aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        aNormal = GLES20.glGetAttribLocation(programId, "aNormal")
        uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMvpMatrix")
        uModelMatrix = GLES20.glGetUniformLocation(programId, "uModelMatrix")
        uColor = GLES20.glGetUniformLocation(programId, "uColor")
        uEmissive = GLES20.glGetUniformLocation(programId, "uEmissive")
        uFogColor = GLES20.glGetUniformLocation(programId, "uFogColor")
        uFogDensity = GLES20.glGetUniformLocation(programId, "uFogDensity")
        uCameraPos = GLES20.glGetUniformLocation(programId, "uCameraPos")
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        return shader
    }
}

class GlMeshes {
    val cubeVertexBuffer: FloatBuffer
    val cubeNormalBuffer: FloatBuffer
    val cubeVertexCount = 36

    val quadVertexBuffer: FloatBuffer
    val quadNormalBuffer: FloatBuffer
    val quadVertexCount = 6

    init {
        val cubeVerts = floatArrayOf(
            // Front (+Z)
            -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,
            // Back (-Z)
             0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,
             0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f,
            // Left (-X)
            -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f,
            // Right (+X)
             0.5f, -0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,
             0.5f, -0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,
            // Top (+Y)
            -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f,
            // Bottom (-Y)
            -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f
        )

        val cubeNormals = floatArrayOf(
            0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,
            0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f,
            -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f,
            1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
            0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
            0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f
        )

        val quadVerts = floatArrayOf(
            -0.5f, 0f,  0.5f,  0.5f, 0f,  0.5f,  0.5f, 0f, -0.5f,
            -0.5f, 0f,  0.5f,  0.5f, 0f, -0.5f, -0.5f, 0f, -0.5f
        )
        val quadNormals = floatArrayOf(
            0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
            0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f
        )

        cubeVertexBuffer = toBuffer(cubeVerts)
        cubeNormalBuffer = toBuffer(cubeNormals)
        quadVertexBuffer = toBuffer(quadVerts)
        quadNormalBuffer = toBuffer(quadNormals)
    }

    private fun toBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }
    }
}
