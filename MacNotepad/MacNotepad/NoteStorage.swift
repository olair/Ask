import Foundation

// MARK: - Note Model
struct Note: Codable {
    let id: UUID
    let content: String
    let timestamp: Date
    
    init(content: String, timestamp: Date) {
        self.id = UUID()
        self.content = content
        self.timestamp = timestamp
    }
}

// MARK: - Note Storage
class NoteStorage {
    
    // MARK: - Singleton
    static let shared = NoteStorage()
    
    // MARK: - Properties
    private let userDefaults = UserDefaults.standard
    private let notesKey = "SavedNotes"
    private let maxNotesCount = 50 // 最多保存50条记录
    
    // MARK: - Initialization
    private init() {}
    
    // MARK: - Public Methods
    
    /// 保存记事
    func saveNote(_ note: Note) {
        var notes = loadNotes()
        
        // 在开头插入新记事（最新的在前面）
        notes.insert(note, at: 0)
        
        // 限制记事数量
        if notes.count > maxNotesCount {
            notes = Array(notes.prefix(maxNotesCount))
        }
        
        saveNotes(notes)
    }
    
    /// 加载所有记事
    func loadNotes() -> [Note] {
        guard let data = userDefaults.data(forKey: notesKey) else {
            return []
        }
        
        do {
            let notes = try JSONDecoder().decode([Note].self, from: data)
            return notes.sorted { $0.timestamp > $1.timestamp } // 按时间降序排列
        } catch {
            print("Error loading notes: \(error)")
            return []
        }
    }
    
    /// 删除指定记事
    func deleteNote(withId id: UUID) {
        var notes = loadNotes()
        notes.removeAll { $0.id == id }
        saveNotes(notes)
    }
    
    /// 清空所有记事
    func clearAllNotes() {
        userDefaults.removeObject(forKey: notesKey)
    }
    
    /// 获取记事数量
    func getNotesCount() -> Int {
        return loadNotes().count
    }
    
    /// 搜索记事
    func searchNotes(containing searchText: String) -> [Note] {
        let allNotes = loadNotes()
        guard !searchText.isEmpty else { return allNotes }
        
        return allNotes.filter { note in
            note.content.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    // MARK: - Private Methods
    
    /// 保存记事数组到 UserDefaults
    private func saveNotes(_ notes: [Note]) {
        do {
            let data = try JSONEncoder().encode(notes)
            userDefaults.set(data, forKey: notesKey)
        } catch {
            print("Error saving notes: \(error)")
        }
    }
}

// MARK: - Note Extensions
extension Note {
    
    /// 获取格式化的时间字符串
    var formattedTimestamp: String {
        let formatter = DateFormatter()
        
        // 如果是今天，只显示时间
        if Calendar.current.isDateInToday(timestamp) {
            formatter.timeStyle = .short
            return "今天 \(formatter.string(from: timestamp))"
        }
        // 如果是昨天
        else if Calendar.current.isDateInYesterday(timestamp) {
            formatter.timeStyle = .short
            return "昨天 \(formatter.string(from: timestamp))"
        }
        // 其他日期显示完整日期时间
        else {
            formatter.dateStyle = .short
            formatter.timeStyle = .short
            return formatter.string(from: timestamp)
        }
    }
    
    /// 获取内容预览（用于列表显示）
    var preview: String {
        let maxLength = 50
        let cleanContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        
        if cleanContent.count <= maxLength {
            return cleanContent
        } else {
            return String(cleanContent.prefix(maxLength)) + "..."
        }
    }
    
    /// 获取第一行内容作为标题
    var title: String {
        let lines = content.components(separatedBy: .newlines)
        let firstLine = lines.first?.trimmingCharacters(in: .whitespaces) ?? ""
        
        if firstLine.isEmpty {
            return "无标题"
        }
        
        let maxLength = 30
        if firstLine.count <= maxLength {
            return firstLine
        } else {
            return String(firstLine.prefix(maxLength)) + "..."
        }
    }
}