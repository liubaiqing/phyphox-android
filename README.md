# 陀螺仪 (Gyroscope)

陀螺仪是phyphox应用的修改版本，专为中国大学生物理学术竞赛（CUPT）演示设计。它使用智能手机中的传感器实时测量和可视化手机的姿态。

## 原属仓库

本项目基于phyphox-android仓库，这是一个使用智能手机传感器进行物理实验的开源应用。原属仓库可以在 https://github.com/phyphox/phyphox-android 找到。

## 修改内容

1. **简化界面**：移除了不必要的功能和语言，只保留中文语言支持
2. **3D渲染**：使用OpenGL ES添加了手机姿态的3D可视化
3. **多传感器支持**：扩展支持多种传感器，包括加速度传感器、线性加速度传感器、重力传感器和旋转矢量传感器
4. **启动界面**：添加了带有项目信息的自定义启动界面
5. **界面改进**：调整了界面颜色，仅使用黑白色调

## 功能

1. **实时3D姿态可视化**：显示手机的3D模型和坐标轴（X: 红色, Y: 绿色, Z: 蓝色）
2. **多传感器数据采集**：以高采样率从多个传感器收集数据
3. **数据导出**：支持以多种格式（Excel, CSV）导出传感器数据
4. **缩放控制**：允许放大和缩小3D模型
5. **传感器数据显示**：以图表和数字格式显示实时传感器数据

## 使用方法

1. 启动应用并在启动界面点击"明白"
2. 从主菜单选择"多传感器姿态测量"
3. 点击右上角的播放按钮开始数据采集
4. 实时查看手机姿态的3D模型
5. 使用缩放按钮调整3D模型大小
6. 点击停止按钮停止数据采集
7. 使用导出按钮导出收集的数据

## 代码风格

该应用及其所有部分由学生和研究人员开发，他们不一定具有软件开发背景。因此，您会在我们的代码中发现许多不符合最佳实践的段落。任何改进我们代码的帮助都受到欢迎。

## 项目结构

本仓库包含应用的Android版本源代码。整个项目分布在多个仓库中：

* **phyphox-android**
  Android源代码，包含phyphox-experiments和phyphox-webinterface作为子仓库

* **phyphox-experiments**
  Phyphox实验定义，随应用一起提供

* **phyphox-ios**
  iOS源代码，包含phyphox-experiments和phyphox-webinterface作为子仓库

* **phyphox-translation**
  包含实验定义和应用商店条目的翻译。通过Python脚本手动同步到实验仓库。其主要目的是为我们的翻译系统方便地提供可翻译资源。

* **phyphox-webeditor**
  基于Web的编辑器，用于在GUI中创建和修改phyphox实验文件

* **phyphox-webinterface**
  这是当"远程访问"功能激活时，应用中的Web服务器提供的Web界面
  
总体文档（例如phyphox文件格式或REST API）可以在我们的[phyphox.org Wiki](https://phyphox.org/wiki)中找到。

## 分支

我们将最新发布版本的代码保存在"master"分支中，而小的开发工作在"development"分支中进行。更大的更改和长期开发在其他分支中进行，这些分支最终会合并到"dev-next"分支中。在一些仓库中，您还会发现"translation"分支，它通常与当前的"development"或"dev-next"分支相同或非常接近，并链接到我们的翻译系统，以控制翻译人员何时能够处理新的文本段落。

## 贡献

我们鼓励对我们的项目做出任何贡献。然而，由于项目的复杂性以及它在世界各地学校中的使用，在任何代码进入最终版本的phyphox（在应用商店中分发）之前，有一些事情需要考虑：
* 注意UI的变化。许多教师依赖简单一致的工作流程，不给学生太多干扰。此外，他们可能已经创建了一些工作表，当界面变化时需要更新。因此，尝试以简单而精益的方式添加新功能。
* Android和iOS版本应保持尽可能相似。我们接受两个版本UI的轻微变化，如果它们遵循每个平台的明显设计标准（例如在Android上使用复选标记，但在iOS上使用告诉操作的按钮，或在Android上使用FAB和在iOS上使用ActionBar条目），并且一个版本可能获得在另一个平台上不可能的功能（例如在Android上读取光传感器，这在iOS上无法完成，或在Android上获取GPS的卫星数量）。但是，如果您提供的新功能也可以在另一个平台上实现，我们不会将其包含在最终应用中，直到我们（或您或其他人）也将其移植到另一个平台。再次强调，这个应用在世界各地的课堂中使用，我们希望在两个平台上提供非常相似的体验，这样教师就不必两次解释phyphox的使用方法。
* 翻译不是通过git直接完成的。如果您想翻译应用，请联系我们，以便我们为您在我们的翻译系统上设置一个账户。
无论如何，如果您计划贡献的不仅仅是一个小的bug修复或优化，最好先联系我们，这样我们可以一起计划并在我们的开发中考虑您的计划。

## 使用的库

### FFTW

phyphox的这部分使用fftw来计算傅里叶变换（http://www.fftw.org）。FFTW在GNU通用公共许可证（v2或更新版本，因此我们使用v3以与phyphox许可证兼容）下分发。FFTW的版权归2003, 2007-11 Matteo Frigo和2003, 2007-11麻省理工学院所有。

### jlhttp

phyphox的这部分使用jlhttp库（[freeutils.net/source/jlhttp](https://www.freeutils.net/source/jlhttp/)）来创建远程访问功能的Web服务器。它在GNU通用公共许可证下发布。非常感谢[Amichai R.](https://github.com/amichair)提供这个库并将phyphox迁移到它。

### ZXing

phyphox的这部分使用ZXing（Zebra Crossing）库来读取QR码（https://github.com/zxing/zxing）。它在Apache许可证v2下获得许可，带有以下声明：

**BARCODE4J声明**

Barcode4J
版权归2002-2010 Jeremias Märki所有
版权归2005-2006 Dietmar Bürkle所有

本软件的部分内容是根据Apache许可证第5节贡献的。贡献者列在：
http://barcode4j.sourceforge.net/contributors.html

**JCOMMANDER声明**

版权归2010 Cedric Beust cedric@beust.com所有

### Eclipse Paho MQTT

Android版本的MQTT通信使用Hannes Achleitner的MQTT Android Service（https://github.com/hannesa2/paho.mqtt.android，Apache许可证2.0），该服务又使用Eclipse Paho MQTT库（https://www.eclipse.org/paho，Eclipse分发许可证1.0），请参阅各自的网页以获取有关许可证和贡献者的详细信息。

