## 代码托管
[![](https://jitpack.io/v/wandersnail/commons-helper.svg)](https://jitpack.io/#wandersnail/commons-helper)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wandersnail/commons-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wandersnail/commons-helper)
[![Download](https://api.bintray.com/packages/wandersnail/android/commons-helper/images/download.svg) ](https://bintray.com/wandersnail/android/commons-helper/_latestVersion)


## 使用

1. module的build.gradle中的添加依赖，自行修改为最新版本，需要哪个就依赖哪个，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'com.github.wandersnail:commons-helper:latestVersion'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，有时jitpack会抽风，同步不下来。添加完再次同步即可。
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
		maven { url 'https://dl.bintray.com/wandersnail/android/' }
	}
}
```

## 功能

- SharedPreferences、Log、UI、图片、数学、文件操作、加密、网络、日期、数据库等工具类
- wifi、Toast、zip、存储帮助类
- 一些基类