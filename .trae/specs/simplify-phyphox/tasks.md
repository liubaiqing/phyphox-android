# Tasks - Phyphox 项目精简

## Phase 1: 删除非核心目录和文件

- [ ] Task 1.1: 删除 Bluetooth 目录
  - 删除 `app/src/main/java/de/rwth_aachen/phyphox/Bluetooth/` 整个目录
  - 验证编译不受影响

- [ ] Task 1.2: 删除 NetworkConnection 目录
  - 删除 `app/src/main/java/de/rwth_aachen/phyphox/NetworkConnection/` 整个目录
  - 验证编译不受影响

- [ ] Task 1.3: 删除 camera 目录
  - 删除 `app/src/main/java/de/rwth_aachen/phyphox/camera/` 整个目录
  - 验证编译不受影响

- [ ] Task 1.4: 删除非核心 Java 文件
  - 删除 `AudioOutput.java`
  - 删除 `GpsInput.java`
  - 删除 `GpsGeoid.java`
  - 删除 `CameraInput.kt` (如存在)
  - 删除 `DepthInput.java` (如存在)
  - 验证编译不受影响

## Phase 2: 修改 SensorInput.java 仅保留陀螺仪

- [ ] Task 2.1: 精简 SensorName 枚举
  - 仅保留 `gyroscope` 和 `custom`
  - 移除其他传感器类型枚举值

- [ ] Task 2.2: 精简 resolveSensorName 方法
  - 仅保留 `gyroscope` 到 `Sensor.TYPE_GYROSCOPE` 的映射
  - 移除其他传感器类型映射

- [ ] Task 2.3: 精简 getDescriptionRes 方法
  - 仅保留陀螺仪相关的描述资源映射

- [ ] Task 2.4: 精简 getUnit 方法
  - 仅保留陀螺仪的单位 (rad/s)

- [ ] Task 2.5: 精简 findSensor 方法
  - 移除其他传感器的供应商传感器检测逻辑
  - 保留陀螺仪检测逻辑

## Phase 3: 修改 PhyphoxExperiment.java 移除其他输入

- [ ] Task 3.1: 移除非陀螺仪输入相关字段
  - 移除 `audioRecord` 相关字段
  - 移除 `gpsIn` 字段
  - 移除 `bluetoothInputs` 和 `bluetoothOutputs` 字段
  - 移除 `depthInput` 字段
  - 移除 `cameraInput` 字段
  - 移除 `networkConnections` 字段

- [ ] Task 3.2: 修改 init 方法
  - 仅初始化陀螺仪传感器
  - 移除其他输入的初始化代码

- [ ] Task 3.3: 修改 startAllIO 方法
  - 仅启动陀螺仪传感器
  - 移除其他输入/输出的启动代码

- [ ] Task 3.4: 修改 stopAllIO 方法
  - 仅停止陀螺仪传感器
  - 移除其他输入/输出的停止代码

## Phase 4: 修改 Experiment.java 移除其他功能

- [ ] Task 4.1: 移除蓝牙相关代码
  - 移除 `bluetoothConnectionSuccessful` 字段
  - 移除 `deviceInfoAdapter` 字段
  - 移除 `connectedDevices` 字段
  - 移除 `connectBluetoothDevices` 方法
  - 移除 `showBluetoothConnectedDeviceInfo` 方法

- [ ] Task 4.2: 移除网络连接相关代码
  - 移除 `connectNetworkConnections` 方法
  - 移除 `networkScanDialogDismissed` 方法

- [ ] Task 4.3: 移除 GPS 相关代码
  - 移除 GPS 权限和初始化代码

- [ ] Task 4.4: 移除音频相关代码
  - 移除 `audioOutput` 字段
  - 移除音频初始化代码

- [ ] Task 4.5: 简化菜单选项
  - 移除与已删除功能相关的菜单项
  - 保留远程控制、开始/停止测量、清除数据等核心功能

- [ ] Task 4.6: 简化 onSaveInstanceState
  - 移除已删除字段的保存逻辑

## Phase 5: 修改 RemoteServer.java 和相关文件

- [ ] Task 5.1: 保留 RemoteServer.java 不变
  - 该文件需要完全保留，不做修改

- [ ] Task 5.2: 保留 Web 界面文件
  - 保留 `index.html` 不变
  - 保留 `style.css` 不变

- [ ] Task 5.3: 修改 handleConfig 方法
  - 在返回的 config JSON 中仅包含陀螺仪输入信息
  - 移除其他传感器输入的配置信息

## Phase 6: 修改 ExpView.java 和相关视图

- [ ] Task 6.1: 移除相机相关视图元素
  - 移除与 camera/depth 相关的视图元素

- [ ] Task 6.2: 保留基础视图功能
  - 保留 graphElement、valueElement 等基础视图
  - 保留 editElement 用于用户输入

## Phase 7: 简化实验配置文件

- [ ] Task 7.1: 创建简化版陀螺仪实验配置
  - 创建 `gyroscope_only.phyphox` 配置文件
  - 仅配置陀螺仪输入
  - 配置基础图表显示

- [ ] Task 7.2: 删除其他实验配置文件
  - 删除 `gps.phyphox`
  - 删除 `roll.phyphox`
  - 删除 `pendulum.phyphox`
  - 删除 `sensordb.phyphox`

## Phase 8: 修改 PhyphoxFile.java 解析器

- [ ] Task 8.1: 移除其他传感器解析逻辑
  - 移除 audio、gps、bluetooth、camera 输入解析
  - 保留 gyroscope 输入解析

- [ ] Task 8.2: 简化实验加载逻辑
  - 移除对已删除功能的依赖检查

## Phase 9: 清理资源文件和清单

- [ ] Task 9.1: 清理字符串资源
  - 移除已删除功能的字符串资源
  - 保留陀螺仪和远程控制相关字符串

- [ ] Task 9.2: 修改 AndroidManifest.xml
  - 移除蓝牙权限
  - 移除 GPS/位置权限
  - 移除相机权限
  - 移除录音权限
  - 保留网络权限（远程控制需要）

## Phase 10: 验证和测试

- [ ] Task 10.1: 编译验证
  - 确保项目能够成功编译
  - 无编译错误

- [ ] Task 10.2: 功能验证
  - 验证陀螺仪数据采集正常
  - 验证远程控制功能正常
  - 验证多线程处理正常

# Task Dependencies
- Task 2.x 依赖于 Task 1.x 完成（删除无关文件后修改保留文件）
- Task 3.x 依赖于 Task 2.x 完成
- Task 4.x 依赖于 Task 3.x 完成
- Task 5.x 可以并行执行（RemoteServer 基本不变）
- Task 6.x 依赖于 Task 1.x 完成
- Task 7.x 可以并行执行
- Task 8.x 依赖于 Task 2.x 和 Task 3.x 完成
- Task 9.x 依赖于所有修改任务完成
- Task 10.x 依赖于所有其他任务完成
