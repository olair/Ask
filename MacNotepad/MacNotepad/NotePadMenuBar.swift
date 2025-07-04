import Cocoa

class NotePadMenuBar: NSObject {
    
    // MARK: - Properties
    private var statusItem: NSStatusItem
    private var popover: NSPopover
    private var noteViewController: NoteViewController
    private var eventMonitor: EventMonitor?
    
    // MARK: - Initialization
    override init() {
        // 创建状态栏项目
        statusItem = NSStatusBar.system.statusItem(withLength: NSStatusItem.squareLength)
        
        // 创建弹出窗口
        popover = NSPopover()
        noteViewController = NoteViewController()
        
        super.init()
        
        setupStatusItem()
        setupPopover()
        setupEventMonitor()
    }
    
    // MARK: - Setup Methods
    private func setupStatusItem() {
        if let button = statusItem.button {
            button.image = NSImage(systemSymbolName: "note.text", accessibilityDescription: "记事本")
            button.action = #selector(togglePopover)
            button.target = self
        }
    }
    
    private func setupPopover() {
        popover.contentSize = NSSize(width: 350, height: 400)
        popover.behavior = .transient
        popover.contentViewController = noteViewController
    }
    
    private func setupEventMonitor() {
        eventMonitor = EventMonitor(mask: [.leftMouseDown, .rightMouseDown]) { [weak self] event in
            if let strongSelf = self, strongSelf.popover.isShown {
                strongSelf.closePopover(sender: event)
            }
        }
    }
    
    // MARK: - Actions
    @objc private func togglePopover() {
        if popover.isShown {
            closePopover(sender: nil)
        } else {
            showPopover(sender: nil)
        }
    }
    
    private func showPopover(sender: Any?) {
        if let button = statusItem.button {
            popover.show(relativeTo: button.bounds, of: button, preferredEdge: NSRectEdge.minY)
            eventMonitor?.start()
        }
    }
    
    private func closePopover(sender: Any?) {
        popover.performClose(sender)
        eventMonitor?.stop()
    }
}

// MARK: - Event Monitor
class EventMonitor {
    private var monitor: Any?
    private let mask: NSEvent.EventTypeMask
    private let handler: (NSEvent?) -> Void
    
    init(mask: NSEvent.EventTypeMask, handler: @escaping (NSEvent?) -> Void) {
        self.mask = mask
        self.handler = handler
    }
    
    deinit {
        stop()
    }
    
    func start() {
        monitor = NSEvent.addGlobalMonitorForEvents(matching: mask, handler: handler)
    }
    
    func stop() {
        if monitor != nil {
            NSEvent.removeMonitor(monitor!)
            monitor = nil
        }
    }
}

// MARK: - Note View Controller
class NoteViewController: NSViewController {
    
    // MARK: - UI Elements
    private var inputTextView: NSTextView!
    private var historyTableView: NSTableView!
    private var scrollView: NSScrollView!
    private var historyScrollView: NSScrollView!
    private var saveButton: NSButton!
    private var clearButton: NSButton!
    
    // MARK: - Data
    private var noteStorage = NoteStorage.shared
    private var notes: [Note] = []
    
    // MARK: - Lifecycle
    override func loadView() {
        view = NSView(frame: NSRect(x: 0, y: 0, width: 350, height: 400))
        setupUI()
        loadNotes()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupTableView()
    }
    
    // MARK: - UI Setup
    private func setupUI() {
        // 主标题
        let titleLabel = NSTextField(labelWithString: "快速记事本")
        titleLabel.font = NSFont.boldSystemFont(ofSize: 16)
        titleLabel.alignment = .center
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(titleLabel)
        
        // 输入区域
        scrollView = NSScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.hasVerticalScroller = true
        scrollView.hasHorizontalScroller = false
        scrollView.autohidesScrollers = true
        
        inputTextView = NSTextView()
        inputTextView.isRichText = false
        inputTextView.isVerticallyResizable = true
        inputTextView.isHorizontallyResizable = false
        inputTextView.textContainer?.widthTracksTextView = true
        inputTextView.textContainer?.containerSize = NSSize(width: 320, height: CGFloat.greatestFiniteMagnitude)
        inputTextView.font = NSFont.systemFont(ofSize: 14)
        inputTextView.string = ""
        
        scrollView.documentView = inputTextView
        view.addSubview(scrollView)
        
        // 按钮区域
        let buttonStackView = NSStackView()
        buttonStackView.orientation = .horizontal
        buttonStackView.distribution = .fillEqually
        buttonStackView.spacing = 10
        buttonStackView.translatesAutoresizingMaskIntoConstraints = false
        
        saveButton = NSButton(title: "保存", target: self, action: #selector(saveNote))
        saveButton.bezelStyle = .rounded
        
        clearButton = NSButton(title: "清空", target: self, action: #selector(clearInput))
        clearButton.bezelStyle = .rounded
        
        buttonStackView.addArrangedSubview(saveButton)
        buttonStackView.addArrangedSubview(clearButton)
        view.addSubview(buttonStackView)
        
        // 历史记录标题
        let historyLabel = NSTextField(labelWithString: "历史记录")
        historyLabel.font = NSFont.boldSystemFont(ofSize: 14)
        historyLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(historyLabel)
        
        // 历史记录表格
        historyScrollView = NSScrollView()
        historyScrollView.translatesAutoresizingMaskIntoConstraints = false
        historyScrollView.hasVerticalScroller = true
        historyScrollView.hasHorizontalScroller = false
        historyScrollView.autohidesScrollers = true
        
        historyTableView = NSTableView()
        historyTableView.headerView = nil
        
        let column = NSTableColumn(identifier: NSUserInterfaceItemIdentifier("NoteColumn"))
        column.title = "记录"
        column.width = 300
        historyTableView.addTableColumn(column)
        
        historyScrollView.documentView = historyTableView
        view.addSubview(historyScrollView)
        
        // 约束设置
        NSLayoutConstraint.activate([
            // 标题
            titleLabel.topAnchor.constraint(equalTo: view.topAnchor, constant: 10),
            titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            titleLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            
            // 输入区域
            scrollView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 10),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            scrollView.heightAnchor.constraint(equalToConstant: 80),
            
            // 按钮区域
            buttonStackView.topAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: 10),
            buttonStackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            buttonStackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            buttonStackView.heightAnchor.constraint(equalToConstant: 30),
            
            // 历史记录标题
            historyLabel.topAnchor.constraint(equalTo: buttonStackView.bottomAnchor, constant: 15),
            historyLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            historyLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            
            // 历史记录表格
            historyScrollView.topAnchor.constraint(equalTo: historyLabel.bottomAnchor, constant: 5),
            historyScrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            historyScrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            historyScrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -10)
        ])
    }
    
    private func setupTableView() {
        historyTableView.dataSource = self
        historyTableView.delegate = self
        historyTableView.doubleAction = #selector(doubleClickedNote)
    }
    
    // MARK: - Actions
    @objc private func saveNote() {
        let text = inputTextView.string.trimmingCharacters(in: .whitespacesAndNewlines)
        
        guard !text.isEmpty else {
            showAlert(message: "请输入内容后再保存")
            return
        }
        
        let note = Note(content: text, timestamp: Date())
        noteStorage.saveNote(note)
        loadNotes()
        inputTextView.string = ""
        
        showAlert(message: "保存成功！")
    }
    
    @objc private func clearInput() {
        inputTextView.string = ""
    }
    
    @objc private func doubleClickedNote() {
        let selectedRow = historyTableView.selectedRow
        guard selectedRow >= 0 && selectedRow < notes.count else { return }
        
        let note = notes[selectedRow]
        inputTextView.string = note.content
    }
    
    // MARK: - Helper Methods
    private func loadNotes() {
        notes = noteStorage.loadNotes()
        historyTableView.reloadData()
    }
    
    private func showAlert(message: String) {
        let alert = NSAlert()
        alert.messageText = message
        alert.alertStyle = .informational
        alert.addButton(withTitle: "确定")
        alert.runModal()
    }
}

// MARK: - Table View Data Source & Delegate
extension NoteViewController: NSTableViewDataSource, NSTableViewDelegate {
    
    func numberOfRows(in tableView: NSTableView) -> Int {
        return notes.count
    }
    
    func tableView(_ tableView: NSTableView, objectValueFor tableColumn: NSTableColumn?, row: Int) -> Any? {
        guard row < notes.count else { return nil }
        
        let note = notes[row]
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        
        let preview = note.content.count > 30 ? 
            String(note.content.prefix(30)) + "..." : 
            note.content
        
        return "\(formatter.string(from: note.timestamp))\n\(preview)"
    }
    
    func tableView(_ tableView: NSTableView, heightOfRow row: Int) -> CGFloat {
        return 40
    }
}