/**
 * Created by winnerlbm on 2019/4/10.
 */
Ext.define('acsweb.util.Chinesization', {
    convert: function (viewer) {
        if (viewer) {
            //（0）通过API修改鼠标操作---左键平移、中键缩放、右键旋转,代码如下：
            viewer.scene.screenSpaceCameraController.zoomEventTypes = [Cesium.CameraEventType.WHEEL, Cesium.CameraEventType.PINCH];
            viewer.scene.screenSpaceCameraController.tiltEventTypes = [Cesium.CameraEventType.PINCH, Cesium.CameraEventType.RIGHT_DRAG];

            //（1）修改sceneModePicker：
            if (viewer.sceneModePicker) {
                viewer.sceneModePicker.viewModel.tooltip3D = "三维";
                viewer.sceneModePicker.viewModel.tooltip2D = "二维";
                viewer.sceneModePicker.viewModel.tooltipColumbusView = "哥伦布视图";
            }

            //(2）修改homeButton：
            if (viewer.homeButton) {
                viewer.homeButton.viewModel.tooltip = "初始位置";
            }

            //(3）修改navigationHelpButton：
            if (viewer.navigationHelpButton) {
                viewer.navigationHelpButton.viewModel.tooltip = "操作指南";
                let clickHelper = viewer.navigationHelpButton.container.getElementsByClassName("cesium-click-navigation-help")[0];
                let touchHelper = viewer.navigationHelpButton.container.getElementsByClassName("cesium-touch-navigation-help")[0];

                let button = viewer.navigationHelpButton.container.getElementsByClassName("cesium-navigation-button-right")[0];
                button.innerHTML = button.innerHTML.replace(">Touch", ">手势");
                button = viewer.navigationHelpButton.container.getElementsByClassName("cesium-navigation-button-left")[0];
                button.innerHTML = button.innerHTML.replace(">Mouse", ">鼠标");

                let click_help_pan = clickHelper.getElementsByClassName("cesium-navigation-help-pan")[0];
                click_help_pan.innerHTML = "平移";
                let click_help_pan_details = click_help_pan.parentNode.getElementsByClassName("cesium-navigation-help-details")[0];
                click_help_pan_details.innerHTML = "按下左键 + 拖动";

                let click_help_zoom = clickHelper.getElementsByClassName("cesium-navigation-help-zoom")[0];
                click_help_zoom.innerHTML = "旋转";
                click_help_zoom.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "按下右键+拖动";
                click_help_zoom.parentNode.getElementsByClassName("cesium-navigation-help-details")[1].innerHTML = "";

                let click_help_rotate = clickHelper.getElementsByClassName("cesium-navigation-help-rotate")[0];
                click_help_rotate.innerHTML = "缩放";
                click_help_rotate.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "滚动鼠标滚轮";
                click_help_rotate.parentNode.getElementsByClassName("cesium-navigation-help-details")[1].innerHTML = "";
                //触屏操作
                let touch_help_pan = touchHelper.getElementsByClassName("cesium-navigation-help-pan")[0];
                touch_help_pan.innerHTML = "平移";
                touch_help_pan.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "单指拖动";

                let touch_help_zoom = touchHelper.getElementsByClassName("cesium-navigation-help-zoom")[0];
                touch_help_zoom.innerHTML = "缩放";
                touch_help_zoom.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "双指捏合";

                let touch_help_tilt = touchHelper.getElementsByClassName("cesium-navigation-help-rotate")[0];
                touch_help_tilt.innerHTML = "俯仰";
                touch_help_tilt.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "双指同向拖动";

                let touch_help_rotate = touchHelper.getElementsByClassName("cesium-navigation-help-tilt")[0];
                touch_help_rotate.innerHTML = "旋转";
                touch_help_rotate.parentNode.getElementsByClassName("cesium-navigation-help-details")[0].innerHTML = "双指反向拖动";
            }

            //(4)全屏按钮修改：
            /*if (viewer.fullscreenButton) {
                viewer.fullscreenButton.viewModel.tooltip = viewer.fullscreenButton.viewModel.isFullscreen ? "退出全屏" : "全屏模式";
            }*/
        }
    }
});

let chineseUtil = new acsweb.util.Chinesization();