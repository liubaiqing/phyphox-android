# Checklist - Phyphox 项目精简验证

## Phase 1: 删除非核心目录和文件
- [x] Bluetooth 目录已删除
- [x] NetworkConnection 目录已删除
- [x] camera 目录已删除
- [x] AudioOutput.java 已删除
- [x] GpsInput.java 已删除
- [x] GpsGeoid.java 已删除
- [x] CameraInput.kt 已删除（如存在）
- [x] DepthInput.java 已删除（如存在）
- [x] BluetoothScanner.java 已删除

## Phase 2: SensorInput.java 精简
- [x] SensorName 枚举仅保留 gyroscope 和 custom
- [x] resolveSensorName 方法仅保留陀螺仪映射
- [x] getDescriptionRes 方法仅保留陀螺仪描述
- [x] getUnit 方法仅保留陀螺仪单位
- [x] findSensor 方法已简化

## Phase 3: PhyphoxExperiment.java 修改
- [x] audioRecord 相关字段已移除
- [x] gpsIn 字段已移除
- [x] bluetoothInputs 和 bluetoothOutputs 字段已移除
- [x] depthInput 字段已移除
- [x] cameraInput 字段已移除
- [x] networkConnections 字段已移除
- [x] init 方法仅初始化陀螺仪
- [x] startAllIO 方法仅启动陀螺仪
- [x] stopAllIO 方法仅停止陀螺仪

## Phase 4: Experiment.java 修改
- [x] 蓝牙相关字段和方法已移除
- [x] 网络连接相关方法已移除
- [x] GPS 相关代码已移除
- [x] 音频相关代码已移除
- [x] 菜单选项已简化
- [x] onSaveInstanceState 已简化

## Phase 5: RemoteServer.java 保留
- [x] RemoteServer.java 未做修改（完全保留）
- [x] index.html 未做修改（完全保留）
- [x] style.css 未做修改（完全保留）
- [x] handleConfig 方法仅返回陀螺仪配置

## Phase 6: ExpView.java 修改
- [x] 相机相关视图元素已移除
- [x] 基础视图功能保留

## Phase 7: 实验配置文件
- [x] gyroscope.phyphox 已保留
- [x] 其他实验配置文件已删除
- [x] bluetooth 实验目录已删除

## Phase 8: PhyphoxFile.java 修改
- [x] audio 输入解析已移除
- [x] gps 输入解析已移除
- [x] bluetooth 输入解析已移除
- [x] camera 输入解析已移除
- [x] gyroscope 输入解析保留

## Phase 9: 资源文件和清单
- [x] AndroidManifest.xml 已更新
- [x] 蓝牙权限已移除
- [x] GPS/位置权限已移除
- [x] 相机权限已移除
- [x] 录音权限已移除
- [x] 网络权限保留

## Phase 10: 最终验证
- [x] 项目精简完成
- [x] 陀螺仪数据采集功能保留
- [x] 远程控制 HTTP 服务器保留
- [x] 多线程处理机制保留
- [x] 核心数据缓冲区系统保留
