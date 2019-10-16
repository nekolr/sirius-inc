package com.nekolr.view.controller;

import cn.hutool.core.io.IoUtil;
import com.nekolr.service.ApplicationService;
import com.nekolr.model.Setting;
import com.nekolr.util.LogUtils;
import com.nekolr.util.YmlUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import static com.nekolr.Constants.USER_LAST_SETTING_FILE;

/**
 * 主视图控制器
 *
 * @author nekolr
 */
public class MainViewController implements Initializable {

    @FXML
    private TextField svnRepositoryURL;
    @FXML
    private TextField compiledProjectDirField;
    @FXML
    private TextField targetUpdatePackageDirField;
    @FXML
    private TextField versionNumbersField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 启动前加载上次的配置
        try {
            File lastSettingFile = new File(USER_LAST_SETTING_FILE);
            if (lastSettingFile.exists()) {
                Setting setting = YmlUtils.loadYml(new FileInputStream(lastSettingFile), Setting.class);
                svnRepositoryURL.setText(setting.getSvnRepositoryURL());
                compiledProjectDirField.setText(setting.getCompiledProjectDir());
                targetUpdatePackageDirField.setText(setting.getTargetUpdatePackageDir());
                versionNumbersField.setText(setting.getVersionNumbers());
                usernameField.setText(setting.getUsername());
                passwordField.setText(setting.getPassword());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void build(MouseEvent event) {
        // 检查空字段
        if (validField()) {
            // 执行主逻辑
            executeMainLogic();
            // 保存当前配置
            saveCurrentSetting();
        }
    }

    /**
     * 填充设置实体
     *
     * @return
     */
    private Setting fillSetting() {
        Setting setting = new Setting();
        setting.setSvnRepositoryURL(svnRepositoryURL.getText());
        setting.setCompiledProjectDir(compiledProjectDirField.getText());
        setting.setTargetUpdatePackageDir(targetUpdatePackageDirField.getText());
        setting.setVersionNumbers(versionNumbersField.getText());
        setting.setUsername(usernameField.getText());
        setting.setPassword(passwordField.getText());
        return setting;
    }

    /**
     * 持久化当前用户配置
     */
    private void saveCurrentSetting() {
        Setting setting = this.fillSetting();
        String content = YmlUtils.dumpObject(setting);

        File currentSettingFile = new File(USER_LAST_SETTING_FILE);
        try {
            IoUtil.write(new FileOutputStream(currentSettingFile), "UTF-8", true, content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行主逻辑
     */
    private void executeMainLogic() {
        Setting userSetting = this.fillSetting();
        try {
            boolean hasError = ApplicationService.run(userSetting);
            if (hasError) {
                alert("成功", "存在部分错误，请查看日志：" + LogUtils.getLogFile(), "执行完毕");
            } else {
                alert("成功", "", "执行完毕");
            }
        } catch (Exception e) {
            alert("失败", "", e.getMessage());
        }
    }

    /**
     * 检查空字段
     */
    private boolean validField() {
        if (svnRepositoryURL.getText() == null || "".equals(svnRepositoryURL.getText())) {
            alert("错误", "", "svn 仓库地址不能为空");
            return false;
        }

        if (compiledProjectDirField.getText() == null || "".equals(compiledProjectDirField.getText())) {
            alert("错误", "", "编译后项目根目录不能为空");
            return false;
        }

        if (targetUpdatePackageDirField.getText() == null || "".equals(targetUpdatePackageDirField.getText())) {
            alert("错误", "", "增量包输出目录不能为空");
            return false;
        }

        if (versionNumbersField.getText() == null || "".equals(versionNumbersField.getText())) {
            alert("错误", "", "版本号不能为空");
            return false;
        }

        return true;
    }

    /**
     * 弹出窗口
     *
     * @param title
     * @param headerText
     * @param message
     */
    private void alert(String title, String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.show();
    }
}
