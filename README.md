# Android无障碍信息节点例子
此实例项目将指导您如何为自己的View添加更详细的无障碍信息，以便视听缺陷用户和自动化系统使用您的App。

特别适合使用SurfaceView或者自定义绘制View内容的情况。

## 文字指导（均可在项目源码中找到实例）

1. 在您的View中重写onInitializeAccessibilityNodeInfo方法：
```kotlin
override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
    val infoCompat = AccessibilityNodeInfoCompat.wrap(info)
    infoCompat.addChild(this, 此处为您绘制的可交互对象的一个虚拟id)
    //可以添加更多
}

```
2. 实现AccessibilityNodeProvider，并将其作为自己试图的无障碍信息节点提供：
```kotlin
override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider {
    return provider
}
```

3.在自己的AccessibilityNodeProvider中，提供自己的View及交互节点的虚拟id的AccessibilityNodeInfo：
```Kotlin
override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
            //HOST_VIEW_ID则为MyView自身，我们需要为其创建AccessibilityNodeInfo
            if (virtualViewId == HOST_VIEW_ID) {
                val nodeInfo = AccessibilityNodeInfo.obtain(this@MyView)
                //需要手动调用原onInitializeAccessibilityNodeInfo方法，否则数据不会得到填充
                onInitializeAccessibilityNodeInfo(nodeInfo)
                return nodeInfo
            } else if (virtualViewId == xxx) {
                //以下提供SecureTestView绘制的内容的虚拟AccessibilityNodeInfo
                val nodeInfo = AccessibilityNodeInfoCompat.wrap(AccessibilityNodeInfo.obtain())
                //需要设置来源，否则只会有第一项出现在树中
                nodeInfo.setSource(this@MyView,virtualViewId)
                nodeInfo.className = "绘制的可交互控件的类名称"
                nodeInfo.uniqueId = "一个唯一Id"
                nodeInfo.viewIdResourceName = "view的id文本名称"
                //注意，根据绘制情况设置是否对用户可见，将影响自动化分析界面时的过滤
                nodeInfo.isVisibleToUser = true
                //设置其在屏幕中的位置
                nodeInfo.setBoundsInScreen(
                    Rect(left,top,right,bottom)
                )
                nodeInfo.text = "控件的文本内容"
                //设置一些可交互的属性
                nodeInfo.isClickable = true
                return nodeInfo.unwrap()
            }else if (virtualViewId == xxxx){
                //...更多的
            }

            return super.createAccessibilityNodeInfo(virtualViewId)
        }
```