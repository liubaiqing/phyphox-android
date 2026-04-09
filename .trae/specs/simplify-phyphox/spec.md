# Phyphox 项目精简 Spec

## Why
用户需要精简 Phyphox Android 项目，仅保留核心功能：陀螺仪数据检测、性能优化（多线程处理）和远程控制功能。移除其他所有传感器支持、复杂实验配置和不必要的功能模块，使项目更轻量且保持原有核心功能正常运行。

## What Changes
- **保留** 陀螺仪传感器检测功能 (SensorInput.java 及相关)
- **保留** 多线程性能优化代码 (updateData/updateViews 线程机制)
- **保留** 远程控制功能 (RemoteServer.java 及 Web 界面)
- **保留** 核心数据缓冲区系统 (DataBuffer.java)
- **保留** 基础实验框架 (Experiment.java, PhyphoxExperiment.java)
- **移除** 其他传感器支持 (加速度计、磁力计、GPS、麦克风、摄像头等)
- **移除** 蓝牙功能 (Bluetooth 目录)
- **移除** 网络连接功能 (NetworkConnection 目录)
- **移除** 相机/深度相机功能 (camera 目录)
- **移除** 复杂的实验列表和分类功能
- **移除** 数据导出/分享功能 (除远程控制导出外)
- **移除** 音频输入输出功能
- **简化** 实验配置文件，仅保留陀螺仪相关示例

## Impact
- **Affected specs**: 传感器输入系统、远程控制 API、数据缓冲区管理
- **Affected code**: 
  - `SensorInput.java` - 保留但仅支持陀螺仪
  - `RemoteServer.java` - 完全保留
  - `Experiment.java` - 移除其他传感器初始化
  - `PhyphoxExperiment.java` - 移除其他输入类型
  - `ExpView.java` - 保留基础视图功能
  - 删除 `Bluetooth/` 目录
  - 删除 `NetworkConnection/` 目录
  - 删除 `camera/` 目录
  - 删除 `AudioOutput.java`, `GpsInput.java` 等

## ADDED Requirements
### Requirement: 精简后的陀螺仪检测
The system SHALL provide gyroscope data detection functionality identical to the original implementation.

#### Scenario: 陀螺仪数据采集
- **WHEN** 用户启动实验
- **THEN** 系统应能通过 SensorManager 注册陀螺仪监听器
- **AND** 实时采集 X/Y/Z 三轴角速度数据
- **AND** 支持配置采样率和数据平均策略

### Requirement: 保留多线程性能优化
The system SHALL maintain the original multi-threading architecture for performance.

#### Scenario: 数据处理和视图更新分离
- **WHEN** 实验运行时
- **THEN** 数据分析应在独立线程 (updateData) 执行
- **AND** UI 更新应在主线程 (updateViews) 执行
- **AND** 使用 dataLock 保证线程安全

### Requirement: 保留远程控制功能
The system SHALL provide complete remote control via HTTP web interface.

#### Scenario: Web 远程控制
- **WHEN** 用户启用远程服务器
- **THEN** 应启动 HTTP 服务器 (默认端口 8080)
- **AND** 支持通过浏览器访问控制界面
- **AND** 支持开始/停止测量
- **AND** 支持实时数据查看和导出

## MODIFIED Requirements
### Requirement: 传感器支持范围
**Original**: 支持加速度计、陀螺仪、磁力计、GPS、麦克风、光线、压力等多种传感器
**Modified**: 仅支持陀螺仪传感器
- 保留 `SensorInput.SensorName.gyroscope` 枚举
- 保留 `Sensor.TYPE_GYROSCOPE` 类型映射
- 移除其他传感器类型枚举和映射

### Requirement: 实验配置
**Original**: 支持复杂的实验配置，包括多种输入源和分析模块
**Modified**: 简化实验配置，仅支持陀螺仪输入
- 保留基础实验框架
- 保留数据缓冲区系统
- 保留分析模块接口
- 移除其他传感器输入配置

## REMOVED Requirements
### Requirement: 蓝牙设备支持
**Reason**: 非核心功能，与陀螺仪检测和远程控制无关
**Migration**: 删除 Bluetooth/ 目录及相关代码

### Requirement: 网络连接功能 (NetworkConnection)
**Reason**: 非核心功能，与远程控制 HTTP 服务器不同
**Migration**: 删除 NetworkConnection/ 目录及相关代码

### Requirement: 相机/深度相机功能
**Reason**: 非核心功能，与陀螺仪检测无关
**Migration**: 删除 camera/ 目录及相关代码

### Requirement: GPS 定位功能
**Reason**: 非核心功能
**Migration**: 删除 GpsInput.java 及相关代码

### Requirement: 音频输入输出
**Reason**: 非核心功能
**Migration**: 删除 AudioOutput.java 及相关代码
