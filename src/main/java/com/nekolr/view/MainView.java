package com.nekolr.view;

import com.nekolr.util.ClassUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import static com.nekolr.Constants.TITLE;

/**
 * 主视图
 *
 * @author nekolr
 */
public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 打开主视图
        openMainView(primaryStage);

    }

    /**
     * 打开主视图
     *
     * @param primaryStage
     */
    public void openMainView(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader();
        try {
            Parent root = loader.load(ClassUtils.getDefaultClassLoader().getResourceAsStream("Layout.fxml"));
            // 标题
            primaryStage.setTitle(TITLE);
            primaryStage.setScene(new Scene(root));
            // 防止最大化
            primaryStage.setResizable(false);
            // 图标
            primaryStage.getIcons().add(new Image(ClassUtils.getDefaultClassLoader().getResourceAsStream("sirius-inc.png")));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
