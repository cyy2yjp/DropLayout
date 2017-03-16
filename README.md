# DropLayout
这里自己动手dropTopLayout实现的一个类库 但这个类本身似乎就有问题 有人提出了优化http://www.jianshu.com/p/22f213f0c42f 
我自己重写的时候碰到了很多问题，一些问题虽然在上链接中有提示 但是远远没有自己写一边后清晰<br>在使用ViewDraghelper 碰到两个简单的问题<br><ul><li>忘
记给getViewVerticalDragRange赋值，代码注释上说如果返回0，无法返回正确的获取view，结果我在clampViewPositionVertical中就可以限制view活动的范围，
range忘了写，根据以往看到的demo 直接在onInterceptTouchEvent方法结尾返回true，果然也是可以滑动，但是这样在嵌套时事件永远无法传递到子view，
在shouldInterceptTouchEvent 结尾判断mDragState == STATE_DRAGGING时 如果range == 0 ，状态永远是0
</li><li>如果cancel时，ViewDragHelp 没有调用processTouchEvent 那么状态用户是STATE_DRAGGING 事件依然无法分发</li></ul><br> 
另外手动调用dragContentView.dispatchTouchEvent(ev); 可以继续分发事件
