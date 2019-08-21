## 代码托管
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/common-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/common-helper)
[![Download](https://api.bintray.com/packages/wandersnail/androidx/common-helper/images/download.svg) ](https://bintray.com/wandersnail/androidx/common-helper/_latestVersion)


## 使用

1. module的build.gradle中的添加依赖，自行修改为最新版本，需要哪个就依赖哪个，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'com.github.wandersnail:common-helper:latestVersion'
	implementation 'com.github.wandersnail:common-base:latestVersion'
	implementation 'com.github.wandersnail:common-utils:latestVersion'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，添加完再次同步即可。
```
allprojects {
	repositories {
		...
		mavenCentral()
		maven { url 'https://dl.bintray.com/wandersnail/androidx/' }
	}
}
```

## 功能

- SharedPreferences、Log、UI、图片、数学、文件操作、加密、网络、日期、数据库等工具类
- wifi、Toast、zip、存储帮助类
- 一些基类
