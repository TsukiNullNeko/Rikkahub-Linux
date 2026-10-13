# RikkaHub Linux 移植进度

> 本文件记录 Android → Linux 桌面移植的当前状态、已完成工作与剩余任务。

## 技术栈决策

| 项目 | 选择 |
|---|---|
| UI | Compose Multiplatform 1.13.0-alpha02（Kotlin 2.4.20） |
| Material3 | 强制 1.13.0-alpha02（插件默认映射到 1.9.0，缺少 Expressive API） |
| 数据库 | Room 2.8.5 KMP + BundledSQLiteDriver（JVM） |
| 配置存储 | DataStore Preferences（JVM） |
| 依赖注入 | Koin 4.2.2（koin-compose / koin-compose-viewmodel） |
| 打包 | jpackage → .deb（内置 JRE，amd64） |

## 已完成

### 1. 平台兼容层 \`:platform\`（编译通过 ✅）
用同名包实现了约 70 个 Android API 的桌面等价物，使 app 源码几乎无需改动即可编译：

- **android.***：Context、Uri、Intent、Bundle、Log、Base64、SharedPreferences、Clipboard、
  Notification/NotificationChannel、PendingIntent、Service/Activity、UsageStats（降级）、
  MediaStore/CalendarContract/DocumentsContract、Bitmap/BitmapFactory/Matrix/ExifInterface、
  AudioRecord/MediaRecorder（javax.sound 真实实现）、MediaMetadataRetriever、WebView（外部浏览器）、
  MimeTypeMap、DateUtils、Xml（xmlpull+kxml2）
- **androidx.***：navigation3（NavKey/NavDisplay/entryProvider/decorators 完整实现）、
  activity（ActivityResultContracts + AWT 文件选择器）、lifecycle（ProcessLifecycleOwner）、
  compose.ui.platform.LocalContext、compose.ui.res.stringResource（对接生成的多语言表）、
  core（ContextCompat/NotificationCompat/FileProvider/ColorUtils/createBitmap）、
  exifinterface、browser.customtabs、annotation
- **org.koin.androidx.compose.koinViewModel** → 转发到多平台实现
- **io.github.g00fy2.quickie**（二维码扫描）→ zxing 从图片文件解码
- **com.google.firebase**（统计/崩溃上报）→ 空实现

### 2. 库模块（全部编译通过 ✅）
\`:hugeicons\`（裁剪出 166 个用到的图标）、\`:common\`、\`:ai\`、\`:highlight\`、\`:material3\`
（vendor material-color-utilities 源码）、\`:document\`（MuPDF → PDFBox）、\`:search\`、
\`:speech\`（media3 → javax.sound + jlayer；Android TTS → espeak-ng）、\`:workspace\`、
\`:oauth\`、\`:mediagen\`、\`:ui\`

### 3. 资源系统
- 构建期从 res/values*/strings.xml 生成 \`R\` 与多语言字符串表（1583 条 × 6 语言）
- \`R.drawable\` / \`R.font\` / \`R.raw\` / \`R.mipmap\` / \`R.color\` 常量生成
- 字体：\`desktopFont()\` 通过 CMP \`LoadedFont\` 从 classpath 加载 TTF
- LaTeX：jlatexmath（JVM）渲染为图片
- 图标：裁剪版 hugeicons 模块

### 4. 数据层
- Room KMP 迁移：\`Room.databaseBuilder<AppDatabase>(name = path).setDriver(BundledSQLiteDriver())\`
- 11 个迁移脚本适配 \`SQLiteConnection\`（\`SQLiteCompat.kt\` 提供 execSQL/query 兼容）
- FTS5 全文搜索：JDBC（sqlite-jdbc）实现，中文分词降级为 unicode61
- 备份/恢复：JDBC 快照（VACUUM INTO + integrity_check）

### 5. 桌面功能替换
- 终端：ProcessBuilder + Compose 输出区（原 Termux/proot）
- 画板：纯 Compose 实现（原 androidx.ink）
- 视频播放：交给系统播放器（原 media3）
- 浮动窗/权限/截屏统计：桌面无对应能力，优雅降级
- 图片预览：Compose HorizontalPager（原 jvziyaoyao image-viewer）

## 剩余工作

- [ ] app UI 层剩余编译错误（约 150 个，分布在 ~40 个文件）
- [ ] \`Main.kt\`：Compose Desktop 主窗口 + 托盘 + 单实例
- [ ] .deb 打包（jpackage + 桌面项 + 图标）
- [ ] 无头环境下的启动冒烟测试（xvfb）
- [ ] 推送到 GitHub：https://github.com/yuexusi/Rikkahub-Linux

## 构建方式

\`\`\`bash
# 需要本地代理（依赖下载）：
export GRADLE=/path/to/gradle-9.7.1/bin/gradle
$GRADLE :desktop:compileKotlin -g /tmp/gradle-home
\`\`\`
