#Android #SDK接入文档 
> 感谢使用 [图普科技（https://www.tuputech.com）](https://www.tuputech.com) AR、手势识别、人脸识别SDK。该文档主要面向 Android 的开发与接入，帮助开发者更方便、快捷地接入和集成使用我们的SDK. 

### 接入准备
* 开始集成 SDK 之前，您需要获取 **AppKey** 和 **SecretKey**，请在[图普科技](https://www.tuputech.com/register)提交注册申请；
* 在[SDK官网](http://mobile.tuputech.com/sdk)下载指定的压缩包。

### 商务 & 技术支持
若对该文档有进一步疑问，或需要**技术支持以及商务咨询**，可通过电话 ~020-29898058~ 或邮箱 ~bd@tuputech.com~ 直接联系我们。

---

## 1. 快速集成

把仓库 clone 到本地，其中 sdk 文件在 `Demo/TPAndroidMVPDemo/libs` 文件夹中，分别为`鉴权 (TPAuthentication.aar) `、`人脸识别 (TPFace.aar) `、`AR (TPGraphics.aar) `、`手势识别 (TPGestureDetect.aar) ` 四个 aar 文件；

### 1.1 导入 aar 文件

将 `Demo/TPAndroidMVPDemo/libs` 文件夹中的 aar 文件放入项目下的 libs 文件夹内，然后在 build.gradle 中添加依赖：

```groovy
dependencies {
    implementation fileTree(include: ['*.aar'], dir: 'libs')
    implementation 'com.google.protobuf:protobuf-lite:3.0.1' // 由于鉴权依赖于 protobuf 项目，因此依赖鉴权时，需添加protobuf的依赖
}
```

### 1.2 声明 SDK 权限
编辑 `AndroidManifest.xml` 并加入以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
> 参考：[Declaring Permissions | Android Developers](https://developer.android.com/training/permissions/declaring.html)  

### 1.3 初始化人脸识别SDK
在 SDK 使用之前，需要调用以下方法进行初始化工作：
```java
TPFaceAPI.init(
        final Context context,                    // Context 对象
        final String appKey,                      // 申请到的图普appKey
        final String secretKey,                   // 申请到的图普secretKey
        final TupuSDKBootstrapCallback callback   // 初始化回调
);
```

> 初始化接口主要用于完成鉴权认证、读取模型资源等步骤的初始化。  

---

## 2. 人脸识别

> ~人脸识别SDK~包括人脸识别、人脸检测和人脸属性功能，开发者通过调用识别接口，获得 TupuTech.FaceResult 对象，该对象包含错误码和人脸 (Face) 对象数组(见#2.3节).

### 2.1 视频帧识别
通过传入视频帧数据(通常为摄像头返回的数据)获得 FaceResult :
```java
/**
 * TPFaceAPI.onFaceFrame
 * @param data 画面数据
 * @param width 画面宽度
 * @param height 画面高度
 * @param mirror 是否镜像(一般手机前置相机需要镜像)
 * @param degree 枚举, 旋转角度(一般手机前置相机需逆时针旋转270°,后置90°)
 * @param FrameType 枚举, 画面数据类型(I420, NV21, NV12, ARGB, ABGR, I420888)
 * @return FaceResult 
 */
TPFaceAPI.onFaceFrame(byte[] data, int width, int height, boolean mirror, FrameDegree degree, FrameType FrameType);
```
> **优化建议**  
> 该接口一般在摄像头的帧回调中调用，但会对 CPU 有一定的性能消耗。可通过控制调用该系列方法的次数，把 FPS 控制在一个相对可接受范围内，以适配 CPU 性能较差的 Android 设备。  

### 2.2 图像识别
通过传入图像获得 FaceResult :
```java
/**
 * TPFaceAPI.onFaceImage
 * @param image 图像
 * @param mirror 是否镜像
 * @param degree 旋转角度
 * @return FaceResult
 */
TPFaceAPI.onFaceImage(Bitmap image, boolean mirror, FrameDegree degree);
```

### 2.3 人脸检测结果 FaceResult
无论是对视频帧或图像进行检测，调用接口后都将返回 FaceResult 对象，该类包含`错误码 (ErrorCode) `和`人脸 (Face) 对象数组`。

#### 2.3.1 错误码 ErrorCode
错误码为枚举类型，可通过 FaceResult.getErrCode() 方法获取，值如下:
```java
ErrorCode.Success: 检测成功
ErrorCode.DLSessionBootError: SDK初始化失败
ErrorCode.DLSessionRunError: SDK运行失败
ErrorCode.PreProcessError: 预处理失败
ErrorCode.ParseResultError: 解析结果失败
ErrorCode.NoFaceDetect: 没检测到人脸
```

#### 2.3.2 人脸 Face
Face 对象在 FaceResult 中以数组形式存在，通过 FaceResult.getArrayCount() 方法，可以获得图像中人脸数量；通过 FaceResult.getArrayList() 方法，可以获得人脸 (Face) 对象的数组。Face 类的定义如下:
```java
class Face {
    Rect rect;
    FaceLandmark landmark;
    FaceFeature feature;
    FacePortrait portrait;
}

class Rect {
    float left, right, bottom, top; // 人脸矩形的四个边，通过canvas可直接绘制矩形框起人脸
    float faceness; // 人脸置信度，推荐使用Landmark中的faceness
    float faceId; // 人脸在~当前画面~中的id，不可作为人脸唯一标注
}

class FaceLandmark {      
    List<Point> points; // 关键点数组
    float pitch; // pitch轴角度(如低头抬头)
    float yaw; // yaw轴角度(如摇头)
    float roll; // roll轴角度(如歪脖子)
    float faceness; // 人脸置信度
    float faceId; // 人脸在~当前画面~中的id
}

class FaceFeature {
    List<float> feature; // 人脸唯一标志
}

class FacePortrait {
    List<GenderEntry> gender; // 性别数组，结果包括多个可能性别，每个性别有置信度，可通过置信度判断人物最可能的性别
    List<AgeEntry> age; // 年龄数组，数据结构与性别相同
    List<EmotionEntry> emotion; // 表情数组，数据结构与性别相同
    List<AppearanceEntry> appearance; // 颜值数组， 数据结构与性别相同
}

class GenderEntry {
    Gender gender; // 性别枚举
    float threshold; // 置信度
}

class AgeEntry {
    Age age; // 年龄枚举
    float threshold; // 置信度
}

class EmotionEntry {
    Emotion emotion; // 表情枚举
    float threshold; // 置信度
}

class AppearanceEntry {
    Appearance appearance; // 颜值枚举
    float threshold; // 置信度
}

class Point {
    float x, y; // 关键点位置
}

enum Gender {
    Male(0), 
    Female(1), 
    UnKnown(2), 
    MultiPersons(3);
}

enum Age {
    Age_0_1(0), 
    Age_2_5(1), 
    Age_6_10(2), 
    Age_11_15(3), 
    Age_16_20(4), 
    Age_21_25(5), 
    Age_31_40(6), 
    Age_41_50(7), 
    Age_51_60(8), 
    Age_61_80(9), 
    Age_80(10), 
    Age_Others(11), 
    Age_26_30(12);
}

enum Emotion {
    Other(0),
    OtherEmotion(1),
    Happy(2),
    Angry(3),
    Sad(4),
    Fear(5),
    Hate(6),
    Neutral(7);
}

enum Appearance {
    Beautiful(0),
    Nice(1),
    Ordinary(2),
    Ugly(3),
    Others(4);
}

```

---

## 3. AR

我们在 `TPGraphics` 中提供了大量接口，包括了~美颜~、~大眼瘦脸~、~2D、3D贴纸~等功能。这些功能需要依赖人脸识别 SDK 返回的**人脸关键点**，调用者无需关注二者之间数据的兼容性，详情请参考 Demo.

### 3.1 初始化
初始化接口完成 TPGraphics 的初始化工作。

#### 3.1.1 创建对象
创建 TPGraphicsFilter 对象需传入 Context :
```java
TPGraphicsFilter mTPGraphicsFilter = new TPGraphicsFilter(mContext);
```

#### 3.1.2 初始化
```java
@Override
public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    // 初始化eglContext
    mTPGraphicsFilter.setupRenderContext();
    // 激活eglContext
    mTPGraphicsFilter.activeRenderContext();
    // 设置滤镜链
    mTPGraphicsFilter
                .startWithTextureInput()  
                .add3DStickerFilter("sticker3D/head.obj") // 初始化3D贴纸
                .addCircleFaceIndicator(30, 238, 249) // 初始化人脸框贴纸
                .add2DStickerFilter() // 初始化2D贴纸
                .addBeautyFilter() // 初始化美颜
                .addDeformFilter("deforms/deform.json") // 初始化大眼瘦脸
                .endWithTextureOutput();
    mTPGraphicsFilter.setSmallFaceDegree(0f); // 设置瘦脸默认值
    mTPGraphicsFilter.setBigEyeDegree(0f); // 设置大眼默认值
    mTPGraphicsFilter.setBeautyDegree(0f); // 设置美颜默认值
}
```

> 如果使用 SurfaceView, 则在 renderer 的 `onSurfaceCreated()` 回调中初始化 EGL Context. （注：无论是否使用 SurfaceView ，都要确保当前 `eglContext` 存在的情况下调用此 API)  

#### 3.1.3 设置 Texture 大小

Texture 的大小一般设置为 SurfaceView 的大小，需保存起来，供渲染时使用。

```java
@Override
public void onGLSurfaceChanged(GL10 gl, int width, int height) {
    mTextureWidth = width;
    mTextureHeight = height;
}
```

> 如果使用 SurfaceView , 则在 renderer 的 `onGLSurfaceChanged()` 回调中获取 Texture 大小. 

#### 3.1.4 将人脸数据传入 TPGraphicsFilter

 TPGraphicsFilter 的渲染需要人脸识别 SDK 返回的**人脸关键点**，需要将 FaceResult 中的人脸数据转换为 float 的二维数组，再将其通过 TPGraphicsFilter.onNewFacelandmarkResult() 方法传递给 TPGraphicsFilter 。

```java

float[][] raw = faceResult2Raw(faceResult); // 获得二维数组
mTPGraphicsFilter.onNewFacelandmarkResult(raw); // 将数据传入TPGraphicsFilter

/**
 * 将FaceResult中的人脸数据转换为float的二维数组
 */
private float[][] faceResult2Raw(TupuTech.FaceResult result) {
    if (result.getArrayCount() == 0) {
        return new float[][]{};
    }
    float[][] ret = new float[result.getArrayCount() * 3][];
    int id = 0;
    for (TupuTech.Face f :
            result.getArrayList()) {
        float[] points = new float[166];
        int i = 0;
        for (TupuTech.Point point : f.getLandmark().getPointsList()) {
            points[i] = point.getX();
            points[i + 1] = point.getY();
            i += 2;
        }
        TupuTech.Rect r = f.getRect();
        float[] box = {
                r.getLeft(),
                r.getTop(),
                r.getRight(),
                r.getBottom()};
        float[] pose = {
                f.getLandmark().getPitch(),
                f.getLandmark().getYaw(),
                f.getLandmark().getRoll()};
        ret[id] = points;
        ret[id + 1] = box;
        ret[id + 2] = pose;
        id += 3;
    }
    return ret;
}

```
> TPGraphicsFilter.onNewFacelandmarkResult() 方法推荐在渲染线程进行，如 CameraView 的渲染线程


### 3.2 渲染

```java

private PassThroughFilter mPassThroughRenderer = new PassThroughFilter();
private GLOESTextureAdapter mOESAdapter = new GLOESTextureAdapter();

@Override
public void onDrawFrame(GL10 gl, int oesTexture) {
    // 旋转角度
    Rotation rot = (mMainView.getFacing() == CameraView.FACING_FRONT) ?
            Rotation.ROTATION_270 : Rotation.ROTATION_90;
    // 水平翻转(手机的后置相机需要翻转)
    boolean mirror = mMainView.getFacing() != CameraView.FACING_FRONT;

    mOESAdapter.adjustRotation(rot, mirror);

    // * 预处理
    int normalTexture = mOESAdapter.preProcess(oesTexture, null);

    // * 把数据喂给滤镜链的第一个成员 textureInput, 滤镜链开始工作
    mTPGraphicsFilter.processTexture(normalTexture, mTextureWidth, mTextureHeight, 0);
    // * 获得滤镜链处理后的纹理
    int textureAfterProcessing = mTPGraphicsFilter.getOutputTexture();
    // * 切回默认的FrameBuffer
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    // * 渲染结果
    mPassThroughRenderer.onDraw(textureAfterProcessing);
    mTPGraphicsFilter.recycleTexture();
}

```

> 如果使用 SurfaceView, 则在 renderer 的 `onDrawFrame()` 回调中进行渲染

### 3.3 设置渲染参数

我们可以通过 TPGraphicsFilter 的方法来设置要渲染的 3D 贴纸、 2D 贴纸、瘦脸程度、美颜程度、大眼程度等：

```java
// 设置2D贴纸
mTPGraphicsFilter.load2DStickerAsync(stickerPath, new TPStickerLoaderCallback.TP2DStickerLoadCallback() {
    @Override
    public void on2DStickerLoadComplete(long stickerNativePtr) {
        mTPGraphicsFilter.render2DSticker(stickerNativePtr); // 推荐在渲染线程进行
    }
});
// 移除2D贴纸
mTPGraphicsFilter.render2DSticker(0);

// 设置3D贴纸
mTPGraphicsFilter.load3DStickerAsync(sticker.getPath(), new TPStickerLoaderCallback.TP3DStickerLoadCallback() {
    @Override
    public void on3DStickerLoadComplete(long stickerNativePtr) {
        mTPGraphicsFilter.render3DSticker(stickerNativePtr); // 推荐在渲染线程进行
    }
});
// 移除3D贴纸
mTPGraphicsFilter.render3DSticker(0);

// 设置美颜程度
mTPGraphicsFilter.setBeautyDegree(ratio); // 推荐在渲染线程进行
// 设置大眼程度
mTPGraphicsFilter.setBigEyeDegree(ratio); // 推荐在渲染线程进行
// 设置瘦脸程度
mTPGraphicsFilter.setSmallFaceDegree(ratio); // 推荐在渲染线程进行

```

--- 

## 4. 手势识别

> 手势识别 SDK 可以检测画面中人物的手势，感知用户进行~单手/双手比心~、~剪刀石头布~、~OK~、~点赞~、~666~、~摇滚~、~感谢~、~抱拳~等动作。

### 4.1 初始化手势识别 SDK
在 SDK 使用之前，需要调用以下方法进行初始化工作：
```java
TPGestureDetectAPI.init(
        final Context context,                       // Context 对象
        final String appKey,                      // 申请到的图普appKey
        final String secretKey,                   // 申请到的图普secretKey
        final TupuSDKBootstrapCallback callback   // 初始化回调
);
```

### 4.2 识别

手势识别 SDK 与人脸识别 SDK 相似，都可对视频帧和图像进行识别，甚至使用方式也几乎完全相同:
```java
/**
 * TPGestureDetectAPI.onGestureFrame 对视频帧进行手势识别
 * @param data 画面数据
 * @param width 画面宽度
 * @param height 画面高度
 * @param mirror 是否镜像(一般手机前置相机需要镜像)
 * @param degree 枚举, 旋转角度(一般手机前置相机需逆时针旋转270°,后置90°)
 * @param FrameType 枚举, 画面数据类型(I420, NV21, NV12, ARGB, ABGR, I420888)
 * @return TupuTech.GestureResult
 */
TPGestureDetectAPI.onGestureFrame(byte[] data, int width, int height, boolean mirror, FrameDegree degree, FrameType FrameType);

/**
 * TPGestureDetectAPI.onGestureImage 对图像进行手势识别
 * @param image 图像
 * @param mirror 是否镜像
 * @param degree 旋转角度
 * @return TupuTech.GestureResult
 */
TPGestureDetectAPI.onGestureImage(Bitmap image, boolean mirror, FrameDegree degree);

```

### 4.3 手势识别结果 GestureResult

GestureResult 类中，包括错误码 (ErrorCode) 和手势 (Gesture) 对象数组。

#### 4.3.1 错误码 ErrorCode

手势识别结果中的错误码，与 FaceResult 中的错误码完全一致，在此不再累赘。

#### 4.3.2 手势 Gesture

与 FaceResult 一样，手势 (Gesture) 在 GestureResult 也是以数组形式存在，可通过 GestureResult.getArrayList() 方法获得手势数组。
与 Face 不同的是，每个 Gesture 对象中，包含了 GestureEntry 的数组。 GestureEntry 中包括了可能的手势类型和置信度。 Gesture 类的定义如下：

```java
class Gesture {
    Rect rect; // 与Face中的Rect相同
    List<GestureEntry> gesture; // 手势数组
}

class GestureEntry {
    GestureType type; // 手势类别
    float threshold; // 置信度
}

enum GestureType {
    Scissors(0), // 剪刀
    Stone(1), // 石头
    Paper(2), // 布
    Heart(4), // 双手比心
    SingleHandHeart(5), // 单手比心
    Ok(5), // OK
    Good(6), // 大拇指、点赞
    Sixsixsix(7), // 666
    LetsRock(8), // 摇滚
    Beg(9), // 拜年(双手抱拳)
    Thanks(10), // 作揖(武侠式抱拳)
    PalmUp(11), // 手心向上(像托着东西)
    OthersGesture(12); 
}

```
