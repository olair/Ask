import Cocoa

@main
class AppDelegate: NSObject, NSApplicationDelegate {
    
    var notePadMenuBar: NotePadMenuBar?
    
    func applicationDidFinishLaunching(_ aNotification: Notification) {
        // 创建菜单栏应用
        notePadMenuBar = NotePadMenuBar()
        
        // 隐藏 Dock 图标（因为是菜单栏应用）
        NSApp.setActivationPolicy(.accessory)
    }
    
    func applicationWillTerminate(_ aNotification: Notification) {
        // 清理资源
    }
    
    func applicationSupportsSecureRestorableState(_ app: NSApplication) -> Bool {
        return true
    }
}