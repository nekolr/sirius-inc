package com.nekolr.view.controller;


import com.nekolr.App;
import com.nekolr.model.Setting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主视图控制器
 *
 * @author nekolr
 */
public class MainController implements Initializable {

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

    }

    public void build(MouseEvent event) {
        // 检查空字段
        if (validField()) {
            // 执行主逻辑
            executeMainLogic();
        }
    }

    /**
     * 执行主逻辑
     */
    private void executeMainLogic() {
        Setting userSetting = new Setting();
        userSetting.setSvnRepositoryURL(svnRepositoryURL.getText());
        userSetting.setCompiledProjectDir(compiledProjectDirField.getText());
        userSetting.setTargetUpdatePackageDir(targetUpdatePackageDirField.getText());
        userSetting.setVersionNumbers(versionNumbersField.getText());
        userSetting.setUsername(usernameField.getText());
        userSetting.setPassword(passwordField.getText());

        try {
            App.run(userSetting);
            alert("成功", "", "执行完毕");
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
