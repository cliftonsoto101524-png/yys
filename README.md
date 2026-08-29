# 阴阳师辅助 v3.0

## 项目简介

阴阳师辅助软件v3.0是一个基于Root权限的Android自动化工具，用于辅助网易游戏《阴阳师》的日常操作。本项目从v2.0半成品框架全面升级，包含17个自动化脚本模块。

## 功能特性

### 核心功能
- **图像匹配引擎**：基于OpenCV的多分辨率模板匹配
- **Root命令执行**：持久化Shell会话，支持点击、滑动、输入等操作
- **悬浮窗控制**：可拖拽悬浮窗，提供脚本启停/截图/录制快捷操作
- **反检测模块**：随机化点击时间和坐标，模拟人类操作模式
- **云端更新**：支持版本检查、APK下载、模板更新
- **自动截图收集**：定时截图功能，辅助模板制作
- **卡死重连**：自动检测并处理游戏断线、卡死情况

### 脚本列表（17个）

| 脚本 | 功能描述 |
|------|---------|
| 日常任务 | 签到、任务、免费召唤、商店、好友、寮贡献 |
| 御魂副本 | 自动刷御魂，支持指定层数和八岐大蛇 |
| 百鬼夜行 | 自动砸百鬼，优先SSR/SP |
| 觉醒副本 | 火/风/水/雷属性觉醒本 |
| 御灵副本 | 神龙/白藏主/黑豹/孔雀御灵 |
| 业原火 | 自动刷业原火 |
| 世界复读 | 世界频道自动复读 |
| 狩猎战 | 自动参与寮狩猎战 |
| 悬赏封印 | 自动接受和完成悬赏 |
| 结界卡寄养 | 自动在好友结界寄养 |
| 庭院小纸人 | 自动领取小纸人奖励 |
| 阴界之门 | 自动挑战阴界之门 |
| 逢魔之时 | 探索和BOSS战 |
| 召唤狗粮 | 自动召唤 |
| 贪食鬼吃御魂 | 自动喂御魂 |
| UP选择 | 自动选择UP式神 |
| 逢魔答题 | 自动逢魔答题 |
| 卡死重连 | 防卡死守护 |

## 技术栈

- **语言**：Java
- **最低API**：Android 8.0 (API 26)
- **目标API**：Android 14 (API 34)
- **图像处理**：OpenCV Android SDK 4.9.0
- **网络**：OkHttp 4.12.0 + Gson 2.10.1
- **UI**：Material Components + ConstraintLayout
- **存储**：SharedPreferences

## 项目结构

```
YysAssistantV3/
├── app/
│   ├── build.gradle              # App模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml   # 应用清单
│       ├── java/com/yys/root/    # Java源码
│       │   ├── MainActivity.java           # 主界面
│       │   ├── SettingsActivity.java       # 设置界面
│       │   ├── ScriptConfigActivity.java   # 脚本配置
│       │   ├── TemplateManagerActivity.java # 模板管理
│       │   ├── FloatService.java           # 悬浮窗服务
│       │   ├── ScriptRunnerService.java    # 后台脚本服务
│       │   ├── BootReceiver.java           # 开机启动
│       │   ├── YysApplication.java         # 应用类
│       │   ├── ScriptEngine.java           # 脚本引擎
│       │   ├── ImageMatcher.java           # 图像匹配
│       │   ├── RootShell.java              # Root命令执行
│       │   ├── YysAuto.java                # 脚本基类
│       │   ├── ConfigManager.java          # 配置管理
│       │   ├── TemplateManager.java        # 模板管理
│       │   ├── CloudUpdateManager.java     # 云端更新
│       │   ├── ScreenshotCollector.java    # 截图收集
│       │   ├── AntiDetection.java          # 反检测
│       │   ├── ScriptAdapter.java          # 脚本列表适配器
│       │   └── scripts/                    # 17个脚本类
│       ├── res/                  # 资源文件
│       │   ├── layout/           # XML布局
│       │   ├── values/           # 颜色/字符串/主题
│       │   ├── drawable/         # 矢量图形
│       │   ├── menu/             # 菜单
│       │   └── xml/              # 配置文件
│       └── assets/scripts/       # 脚本JSON配置
├── build.gradle                  # 项目构建配置
└── settings.gradle               # 项目设置
```

## 编译说明

### 环境要求
1. Android Studio Hedgehog (2023.1.1) 或更高版本
2. JDK 17
3. Android SDK API 34
4. OpenCV Android SDK 4.9.0（需手动导入或使用Maven依赖）

### 编译步骤
1. 将项目导入Android Studio
2. 同步Gradle依赖
3. 如需使用本地OpenCV SDK，修改 `app/build.gradle` 中的OpenCV依赖配置
4. Build -> Make Project (Ctrl+F9)
5. Build -> Generate Signed Bundle/APK

### OpenCV配置（如Maven依赖不可用）
如果Maven仓库中的OpenCV依赖无法下载：
1. 下载OpenCV Android SDK 4.9.0
2. 导入为Module：`File -> New -> Import Module`
3. 修改 `app/build.gradle`：
```gradle
implementation project(':opencv')
```

## 使用说明

### 前置条件
1. **Root权限**：设备必须已Root（Magisk等）
2. **悬浮窗权限**：首次启动需授予悬浮窗权限
3. **存储权限**：需读写外部存储权限
4. **游戏安装**：需安装《阴阳师》官方客户端

### 首次运行
1. 安装APK并打开应用
2. 授予Root权限（Magisk弹窗）
3. 授予悬浮窗权限
4. 授予存储权限
5. 将截图模板放入 `/sdcard/YysTemplates/` 目录

### 模板目录结构
```
/sdcard/YysTemplates/
├── common/           # 通用模板（按钮、弹窗等）
├── daily/            # 日常任务模板
├── huntu/            # 御魂副本模板
├── baigui/           # 百鬼夜行模板
├── getu/             # 觉醒副本模板
├── liaotu/           # 御灵副本模板
├── yeyuanhuo/        # 业原火模板
├── world_repeat/     # 世界复读模板
├── hunting/          # 狩猎战模板
├── xuanshang/        # 悬赏封印模板
├── jiyang/           # 结界卡寄养模板
├── xiaozhiren/       # 庭院小纸人模板
├── yinjie/           # 阴界之门模板
├── fengmo/           # 逢魔之时模板
├── summon/           # 召唤模板
├── tanshigui/        # 贪食鬼模板
├── upselect/         # UP选择模板
├── fengmo_answer/    # 逢魔答题模板
└── reconnect/        # 重连模板
```

### 模板命名规范
- 按钮：`button_name.png`
- 界面元素：`element_name.png`
- 战斗相关：`start_battle.png`, `victory.png`, `defeat.png`, `reward_ok.png`
- 通用：`ok.png`, `confirm.png`, `close.png`, `back.png`, `skip.png`

### 运行脚本
1. 在主界面点击脚本卡片进入配置
2. 调整参数（如层数、次数等）
3. 点击"启动脚本"
4. 或点击悬浮窗的播放按钮启动最近使用的脚本

## 注意事项

1. **Root稳定性**：确保Root环境稳定，推荐使用Magisk
2. **屏幕分辨率**：图像匹配支持多分辨率，但建议模板基于常用分辨率（1080x1920/1080x2400）制作
3. **游戏更新**：游戏UI更新可能导致模板失效，需及时更新模板
4. **反检测**：建议开启反检测模式以降低被封风险
5. **耗电**：后台运行脚本会增加耗电，建议连接充电器使用

## 配置说明

### 全局设置（SettingsActivity）
- **开机自动启动**：设备重启后自动启动悬浮窗
- **反检测模式**：随机化操作时间和坐标
- **自动重连**：检测断线并自动重连
- **匹配阈值**：图像匹配相似度阈值（0.5-1.0）
- **点击延迟**：每次点击后的等待时间
- **截图质量**：截图保存质量

### 脚本配置（ScriptConfigActivity）
每个脚本有独立的配置参数，如：
- 副本层数
- 运行次数
- 目标属性/式神
- 消息内容等

## 云端更新API格式

### 检查更新
```
GET /check?device={device_id}&version={current_version}
```
响应：
```json
{
  "hasUpdate": true,
  "version": "3.1.0",
  "changelog": "更新内容...",
  "fileSize": 12345678
}
```

### 下载更新
```
GET /download?version={new_version}
```

## 已知限制

1. 当前环境无法编译APK，需在用户远程环境编译
2. 截图模板需用户自行收集和补充
3. 游戏版本更新后可能需要重新制作模板
4. 部分功能依赖设备Root状态

## 版本历史

- **v3.0.0** - 完整重构，17个脚本，反检测，云端更新，悬浮窗增强
- **v2.0** - 基础框架（半成品）

## License

仅供学习研究使用，请遵守游戏用户协议和相关法律法规。
