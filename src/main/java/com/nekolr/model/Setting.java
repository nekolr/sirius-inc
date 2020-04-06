package com.nekolr.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 配置
 *
 * @author nekolr
 */
@Getter
@Setter
@ToString
public class Setting {

    /**
     * svn 仓库地址
     */
    private String svnRepositoryURL;

    /**
     * 编译后的项目根目录，WEB-INF 的上一级
     */
    private String compiledProjectDir;

    /**
     * 版本号
     */
    private String versionNumbers;

    /**
     * 最终生成的增量更新包的目录
     */
    private String targetUpdatePackageDir;

    /**
     * svn 账号
     */
    private String username;

    /**
     * svn 密码
     */
    private String password;

}
