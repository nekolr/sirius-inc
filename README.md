## 简介
这是一个 Java Web 项目增量打包工具。

## 预览
![preview](https://github.com/nekolr/sirius-inc/blob/master/media/sirius-inc.png)

## 设计思路
通过 svn 命令查询提交记录中修改的文件，然后在编译后的项目目录中寻找并复制。

## 注意事项
- 不会打包 pom.xml 文件（如果修改了 pom.xml 文件，一般修改的是依赖包，则需要手工将新的 jar 包放到部署根目录的 `/WEB-INF/lib` 中）。
- 不会打包测试代码和测试资源文件。
- 不会进行常量引用关联查找（比如： A.java 定义了一个 static final 常量，有若干个 java 文件引用了这个常量。如果修改了这个常量的值，那么打包时只会将 A.java 文件打包，而依赖这个常量的那几个 java 文件就不会打包）。

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fnekolr%2Fsirius-inc.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fnekolr%2Fsirius-inc?ref=badge_large)
