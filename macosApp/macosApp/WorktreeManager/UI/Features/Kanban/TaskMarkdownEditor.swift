import AppKit
import Shared
import SwiftUI

struct TaskMarkdownEditor: View {
    @Binding var text: String
    let labels: KanbanLabels

    @State private var selection = NSRange(location: 0, length: 0)

    var body: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.xs) {
            HStack(spacing: DS.Spacing.xs) {
                formatButton(icon: "bold", action: applyBold)
                    .keyboardShortcut("b", modifiers: [.command])
                formatButton(icon: "italic", action: applyItalic)
                    .keyboardShortcut("i", modifiers: [.command])
                formatButton(icon: "chevron.left.forwardslash.chevron.right", action: applyCode)
                    .keyboardShortcut("e", modifiers: [.command])
                formatButton(icon: "list.bullet", action: applyBulletList)
                formatButton(icon: "checklist", action: applyChecklist)
                formatButton(icon: "text.quote", action: applyQuote)
                formatButton(icon: "link", action: applyLink)
                    .keyboardShortcut("k", modifiers: [.command])
            }

            NativeTextView(
                text: $text,
                selection: $selection
            )
            .frame(minHeight: 140, maxHeight: 260)
            .overlay(
                RoundedRectangle(cornerRadius: DS.Radius.sm)
                    .stroke(DS.Colors.border, lineWidth: 1)
            )

            HStack(spacing: DS.Spacing.sm) {
                Text(labels.markdownSupported)
                    .font(.caption)
                    .foregroundStyle(DS.Colors.textSecondary)
                Spacer()
                Text("\(text.count)")
                    .font(.caption)
                    .foregroundStyle(DS.Colors.textSecondary)
            }
        }
    }

    @ViewBuilder
    private func formatButton(
        icon: String,
        action: @escaping () -> Void,
    ) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 12, weight: .medium))
                .frame(width: 16, height: 16)
        }
        .buttonStyle(.bordered)
        .controlSize(.small)
    }

    private func applyBold() {
        applyInline(prefix: "**", suffix: "**")
    }

    private func applyItalic() {
        applyInline(prefix: "_", suffix: "_")
    }

    private func applyCode() {
        applyInline(prefix: "`", suffix: "`")
    }

    private func applyLink() {
        applyInline(prefix: "[", suffix: "]()")
    }

    private func applyBulletList() {
        applyLinePrefix("- ")
    }

    private func applyChecklist() {
        applyLinePrefix("- [ ] ")
    }

    private func applyQuote() {
        applyLinePrefix("> ")
    }

    private func applyInline(
        prefix: String,
        suffix: String,
    ) {
        let safeRange = clampedSelection(in: text)
        let nsText = text as NSString
        let before = nsText.substring(to: safeRange.location)
        let selected = nsText.substring(with: safeRange)
        let after = nsText.substring(from: safeRange.location + safeRange.length)
        let content = selected
        let replacement = prefix + content + suffix
        text = before + replacement + after
        let prefixLength = (prefix as NSString).length
        let contentLength = (content as NSString).length
        selection = NSRange(location: safeRange.location + prefixLength, length: contentLength)
    }

    private func applyLinePrefix(_ prefix: String) {
        let safeRange = clampedSelection(in: text)
        let nsText = text as NSString
        if safeRange.length == 0 {
            let lineRange = nsText.lineRange(for: safeRange)
            text = nsText.replacingCharacters(in: NSRange(location: lineRange.location, length: 0), with: prefix)
            selection = NSRange(location: safeRange.location + (prefix as NSString).length, length: 0)
            return
        }
        let lineRange = nsText.lineRange(for: safeRange)
        let chunk = nsText.substring(with: lineRange)
        let prefixed = chunk.components(separatedBy: "\n").map { prefix + $0 }.joined(separator: "\n")
        text = nsText.replacingCharacters(in: lineRange, with: prefixed)
        selection = NSRange(location: lineRange.location, length: (prefixed as NSString).length)
    }

    private func clampedSelection(in value: String) -> NSRange {
        let length = (value as NSString).length
        let location = max(0, min(selection.location, length))
        let maxLength = max(0, length - location)
        let selectedLength = max(0, min(selection.length, maxLength))
        return NSRange(location: location, length: selectedLength)
    }
}

private struct NativeTextView: NSViewRepresentable {
    @Binding var text: String
    @Binding var selection: NSRange

    func makeCoordinator() -> Coordinator {
        Coordinator(text: $text, selection: $selection)
    }

    func makeNSView(context: Context) -> NSScrollView {
        let scrollView = NSTextView.scrollableTextView()
        scrollView.drawsBackground = false
        guard let textView = scrollView.documentView as? NSTextView else {
            return scrollView
        }
        textView.delegate = context.coordinator
        textView.string = text
        textView.font = NSFont.monospacedSystemFont(ofSize: 13, weight: .regular)
        textView.textContainerInset = NSSize(width: 8, height: 8)
        textView.isRichText = false
        textView.importsGraphics = false
        textView.isAutomaticQuoteSubstitutionEnabled = false
        textView.isAutomaticDashSubstitutionEnabled = false
        textView.isAutomaticLinkDetectionEnabled = false
        textView.isAutomaticDataDetectionEnabled = false
        context.coordinator.textView = textView
        return scrollView
    }

    func updateNSView(
        _ nsView: NSScrollView,
        context: Context,
    ) {
        guard let textView = nsView.documentView as? NSTextView else {
            return
        }
        context.coordinator.textView = textView
        if textView.string != text {
            textView.string = text
        }
        let safeSelection = clampedSelection(in: text, range: selection)
        if textView.selectedRange() != safeSelection {
            textView.setSelectedRange(safeSelection)
        }
    }

    private func clampedSelection(
        in value: String,
        range: NSRange,
    ) -> NSRange {
        let length = (value as NSString).length
        let location = max(0, min(range.location, length))
        let maxLength = max(0, length - location)
        let selectedLength = max(0, min(range.length, maxLength))
        return NSRange(location: location, length: selectedLength)
    }

    final class Coordinator: NSObject, NSTextViewDelegate {
        @Binding var text: String
        @Binding var selection: NSRange
        weak var textView: NSTextView?

        init(
            text: Binding<String>,
            selection: Binding<NSRange>,
        ) {
            _text = text
            _selection = selection
        }

        func textDidChange(_ notification: Notification) {
            guard let view = notification.object as? NSTextView else {
                return
            }
            text = view.string
            selection = view.selectedRange()
        }

        func textViewDidChangeSelection(_ notification: Notification) {
            guard let view = notification.object as? NSTextView else {
                return
            }
            selection = view.selectedRange()
        }
    }
}
