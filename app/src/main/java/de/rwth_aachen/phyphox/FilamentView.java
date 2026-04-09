package de.rwth_aachen.phyphox;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FilamentView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private FloatBuffer mVertexBuffer;
    private IntBuffer mIndexBuffer;
    private FloatBuffer mAxisVertexBuffer;
    private float[] mRotationMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float mScale = 1.0f;

    // 手机尺寸：160.4mm x 75.1mm x 8.4mm
    private static final float WIDTH = 75.1f / 1000.0f;  // 转换为米
    private static final float HEIGHT = 160.4f / 1000.0f;
    private static final float THICKNESS = 8.4f / 1000.0f;

    // 顶点数据
    private static final float[] VERTICES = {
            // 前面
            -WIDTH/2, -HEIGHT/2, THICKNESS/2,
            WIDTH/2, -HEIGHT/2, THICKNESS/2,
            WIDTH/2, HEIGHT/2, THICKNESS/2,
            -WIDTH/2, HEIGHT/2, THICKNESS/2,
            // 后面
            -WIDTH/2, -HEIGHT/2, -THICKNESS/2,
            WIDTH/2, -HEIGHT/2, -THICKNESS/2,
            WIDTH/2, HEIGHT/2, -THICKNESS/2,
            -WIDTH/2, HEIGHT/2, -THICKNESS/2
    };

    // 索引数据
    private static final int[] INDICES = {
            // 前面
            0, 1, 2,
            0, 2, 3,
            // 后面
            4, 5, 6,
            4, 6, 7,
            // 左面
            0, 3, 7,
            0, 7, 4,
            // 右面
            1, 5, 6,
            1, 6, 2,
            // 上面
            3, 2, 6,
            3, 6, 7,
            // 下面
            0, 4, 5,
            0, 5, 1
    };

    // 颜色数据
    private static final float[] COLORS = {
            // 前面
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            // 后面
            0.3f, 0.3f, 0.3f, 1.0f,
            0.3f, 0.3f, 0.3f, 1.0f,
            0.3f, 0.3f, 0.3f, 1.0f,
            0.3f, 0.3f, 0.3f, 1.0f
    };

    // 坐标轴顶点数据
    private static final float[] AXIS_VERTICES = {
            // X轴（红色）：从原点(0,0,0)到(0.15,0,0)
            0.0f, 0.0f, 0.0f,
            0.15f, 0.0f, 0.0f,
            // Y轴（绿色）：从原点(0,0,0)到(0,0.15,0)
            0.0f, 0.0f, 0.0f,
            0.0f, 0.15f, 0.0f,
            // Z轴（蓝色）：从原点(0,0,0)到(0,0,0.15)
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.15f
    };

    // 坐标轴颜色数据
    private static final float[] AXIS_COLORS = {
            // X轴（红色）
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            // Y轴（绿色）
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            // Z轴（蓝色）
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    public FilamentView(Context context) {
        super(context);
        init();
    }

    public FilamentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(3);
        setRenderer(this);
        // 改为按需渲染，只有当旋转矩阵或缩放发生变化时才渲染
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // 初始化旋转矩阵为单位矩阵
        setIdentityMatrix(mRotationMatrix);
        setIdentityMatrix(mModelMatrix);
        setIdentityMatrix(mViewMatrix);
        setIdentityMatrix(mProjectionMatrix);
        setIdentityMatrix(mMVPMatrix);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 设置背景色
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 编译顶点着色器
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);

        // 编译片段着色器
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

        // 创建程序
        mProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgram, vertexShader);
        GLES30.glAttachShader(mProgram, fragmentShader);
        GLES30.glLinkProgram(mProgram);

        // 获取属性和 uniform 位置
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        mColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor");
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 初始化顶点缓冲区
        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4);
        vertexBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = vertexBuffer.asFloatBuffer();
        mVertexBuffer.put(VERTICES);
        mVertexBuffer.position(0);

        // 初始化索引缓冲区
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(INDICES.length * 4);
        indexBuffer.order(ByteOrder.nativeOrder());
        mIndexBuffer = indexBuffer.asIntBuffer();
        mIndexBuffer.put(INDICES);
        mIndexBuffer.position(0);

        // 初始化坐标轴顶点缓冲区
        ByteBuffer axisVertexBuffer = ByteBuffer.allocateDirect(AXIS_VERTICES.length * 4);
        axisVertexBuffer.order(ByteOrder.nativeOrder());
        mAxisVertexBuffer = axisVertexBuffer.asFloatBuffer();
        mAxisVertexBuffer.put(AXIS_VERTICES);
        mAxisVertexBuffer.position(0);

        // 设置相机位置
        setIdentityMatrix(mViewMatrix);
        // 相机在z轴1米处
        mViewMatrix[14] = -1.0f;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置视口
        GLES30.glViewport(0, 0, width, height);

        // 计算投影矩阵
        float aspect = (float) width / height;
        float near = 0.1f;
        float far = 100.0f;
        float fov = 45.0f;
        float f = (float) (1.0 / Math.tan(Math.toRadians(fov / 2)));

        setIdentityMatrix(mProjectionMatrix);
        mProjectionMatrix[0] = f / aspect;
        mProjectionMatrix[5] = f;
        mProjectionMatrix[10] = (far + near) / (near - far);
        mProjectionMatrix[11] = -1.0f;
        mProjectionMatrix[14] = (2 * far * near) / (near - far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除颜色缓冲区
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // 使用程序
        GLES30.glUseProgram(mProgram);

        // 启用深度测试
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // 绑定顶点数据
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // 绑定颜色数据
        FloatBuffer colorBuffer = ByteBuffer.allocateDirect(COLORS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(COLORS);
        colorBuffer.position(0);
        GLES30.glVertexAttribPointer(mColorHandle, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
        GLES30.glEnableVertexAttribArray(mColorHandle);

        // 计算 MVP 矩阵
        // 重置模型矩阵为单位矩阵
        setIdentityMatrix(mModelMatrix);
        // 应用缩放
        mModelMatrix[0] = mScale;
        mModelMatrix[5] = mScale;
        mModelMatrix[10] = mScale;
        // 应用旋转 - 调整旋转方向以匹配手机实际运动
        float[] invertedRotationMatrix = new float[16];
        // 旋转矩阵的逆矩阵等于其转置矩阵（因为它是正交矩阵）
        invertedRotationMatrix[0] = mRotationMatrix[0];
        invertedRotationMatrix[1] = mRotationMatrix[4];
        invertedRotationMatrix[2] = mRotationMatrix[8];
        invertedRotationMatrix[3] = mRotationMatrix[12];
        invertedRotationMatrix[4] = mRotationMatrix[1];
        invertedRotationMatrix[5] = mRotationMatrix[5];
        invertedRotationMatrix[6] = mRotationMatrix[9];
        invertedRotationMatrix[7] = mRotationMatrix[13];
        invertedRotationMatrix[8] = mRotationMatrix[2];
        invertedRotationMatrix[9] = mRotationMatrix[6];
        invertedRotationMatrix[10] = mRotationMatrix[10];
        invertedRotationMatrix[11] = mRotationMatrix[14];
        invertedRotationMatrix[12] = mRotationMatrix[3];
        invertedRotationMatrix[13] = mRotationMatrix[7];
        invertedRotationMatrix[14] = mRotationMatrix[11];
        invertedRotationMatrix[15] = mRotationMatrix[15];
        multiplyMatrices(mModelMatrix, invertedRotationMatrix, mModelMatrix);
        // 应用视图矩阵
        multiplyMatrices(mMVPMatrix, mViewMatrix, mModelMatrix);
        // 应用投影矩阵
        multiplyMatrices(mMVPMatrix, mProjectionMatrix, mMVPMatrix);

        // 设置 MVP 矩阵
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 绘制手机模型
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, INDICES.length, GLES30.GL_UNSIGNED_INT, mIndexBuffer);

        // 绘制坐标轴
        // 绑定坐标轴顶点数据
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, mAxisVertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // 绑定坐标轴颜色数据
        FloatBuffer axisColorBuffer = ByteBuffer.allocateDirect(AXIS_COLORS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        axisColorBuffer.put(AXIS_COLORS);
        axisColorBuffer.position(0);
        GLES30.glVertexAttribPointer(mColorHandle, 4, GLES30.GL_FLOAT, false, 0, axisColorBuffer);
        GLES30.glEnableVertexAttribArray(mColorHandle);

        // 设置线条宽度
        GLES30.glLineWidth(3.0f);

        // 绘制坐标轴线条
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, AXIS_VERTICES.length / 3);

        // 禁用属性数组
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mColorHandle);
    }

    public void updateRotation(float[] rotationMatrix) {
        System.arraycopy(rotationMatrix, 0, mRotationMatrix, 0, 16);
        // 触发渲染
        requestRender();
    }

    public void setScale(float scale) {
        mScale = scale;
        requestRender();
    }

    public void render() {
        // 触发渲染
        requestRender();
    }



    public void release() {
        // 释放资源
        if (mProgram != 0) {
            GLES30.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    // 顶点着色器代码
    private static final String VERTEX_SHADER_CODE = "#version 300 es\n" +
            "layout (location = 0) in vec3 aPosition;\n" +
            "layout (location = 1) in vec4 aColor;\n" +
            "uniform mat4 uMVPMatrix;\n" +
            "out vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);\n" +
            "    vColor = aColor;\n" +
            "}\n";

    // 片段着色器代码
    private static final String FRAGMENT_SHADER_CODE = "#version 300 es\n" +
            "precision mediump float;\n" +
            "in vec4 vColor;\n" +
            "out vec4 fragColor;\n" +
            "void main() {\n" +
            "    fragColor = vColor;\n" +
            "}\n";

    // 编译着色器
    private int compileShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }

    // 设置矩阵为单位矩阵
    private void setIdentityMatrix(float[] matrix) {
        for (int i = 0; i < 16; i++) {
            matrix[i] = 0.0f;
        }
        matrix[0] = 1.0f;
        matrix[5] = 1.0f;
        matrix[10] = 1.0f;
        matrix[15] = 1.0f;
    }

    // 矩阵乘法：result = a * b
    private void multiplyMatrices(float[] result, float[] a, float[] b) {
        float[] temp = new float[16];
        temp[0] = a[0] * b[0] + a[4] * b[1] + a[8] * b[2] + a[12] * b[3];
        temp[1] = a[1] * b[0] + a[5] * b[1] + a[9] * b[2] + a[13] * b[3];
        temp[2] = a[2] * b[0] + a[6] * b[1] + a[10] * b[2] + a[14] * b[3];
        temp[3] = a[3] * b[0] + a[7] * b[1] + a[11] * b[2] + a[15] * b[3];

        temp[4] = a[0] * b[4] + a[4] * b[5] + a[8] * b[6] + a[12] * b[7];
        temp[5] = a[1] * b[4] + a[5] * b[5] + a[9] * b[6] + a[13] * b[7];
        temp[6] = a[2] * b[4] + a[6] * b[5] + a[10] * b[6] + a[14] * b[7];
        temp[7] = a[3] * b[4] + a[7] * b[5] + a[11] * b[6] + a[15] * b[7];

        temp[8] = a[0] * b[8] + a[4] * b[9] + a[8] * b[10] + a[12] * b[11];
        temp[9] = a[1] * b[8] + a[5] * b[9] + a[9] * b[10] + a[13] * b[11];
        temp[10] = a[2] * b[8] + a[6] * b[9] + a[10] * b[10] + a[14] * b[11];
        temp[11] = a[3] * b[8] + a[7] * b[9] + a[11] * b[10] + a[15] * b[11];

        temp[12] = a[0] * b[12] + a[4] * b[13] + a[8] * b[14] + a[12] * b[15];
        temp[13] = a[1] * b[12] + a[5] * b[13] + a[9] * b[14] + a[13] * b[15];
        temp[14] = a[2] * b[12] + a[6] * b[13] + a[10] * b[14] + a[14] * b[15];
        temp[15] = a[3] * b[12] + a[7] * b[13] + a[11] * b[14] + a[15] * b[15];

        System.arraycopy(temp, 0, result, 0, 16);
    }
}
