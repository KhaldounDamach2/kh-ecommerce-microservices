import { useEffect, useRef, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { chatErrorText, sendChatMessage } from "../api/ai";

const MAX_MESSAGE_LENGTH = 2000; // same limit as ai-service

// AI text can contain content from the database (written by sellers), so the display
// must never load or link to anything: no images (a URL the browser would fetch on
// its own) and no links (phishing). Their text is kept. Raw HTML is never rendered.
function AssistantMarkdown({ text }) {
  return (
    <div className="chat-markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        disallowedElements={["img", "a"]}
        unwrapDisallowed
      >
        {text}
      </ReactMarkdown>
    </div>
  );
}

export default function ChatPanel() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]); // [{ role: "user" | "assistant", content }]
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const endRef = useRef(null);

  // Keep the newest message in view.
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, sending, error, open]);

  // const handleSend = async (event) => { ... };
  const handleSend = async (event) => {
    event.preventDefault();
    const text = input.trim();
    if (!text || sending) return;

    const history = messages; // the conversation BEFORE this message
    setMessages([...messages, { role: "user", content: text }]);
    setInput("");
    setError(null);
    setSending(true);

    try {
      const reply = await sendChatMessage(text, history);
      setMessages((current) => [
        ...current,
        { role: "assistant", content: reply },
      ]);
    } catch (err) {
      setMessages(history); // take the unanswered message back out ...
      setInput(text); // ... and put it back in the input field, ready to retry
      setError(chatErrorText(err));
    } finally {
      setSending(false);
    }
  };

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="fixed bottom-6 right-6 z-50 rounded-full bg-blue-600 px-5 py-3 text-white shadow-lg hover:bg-blue-700"
        aria-label="Open the shopping assistant"
      >
        💬 Ask our assistant
      </button>
    );
  }

  return (
    <div className="fixed bottom-6 right-6 z-50 flex h-[32rem] w-[22rem] max-w-[calc(100vw-2rem)] flex-col rounded-xl border bg-white shadow-2xl">
      <div className="flex items-center justify-between rounded-t-xl bg-blue-600 px-4 py-2 text-white">
        <span className="font-semibold">Shopping assistant</span>
        <button
          type="button"
          onClick={() => setOpen(false)}
          aria-label="Close the assistant"
          className="text-xl"
        >
          ×
        </button>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto p-3 text-sm">
        {messages.length === 0 && (
          <p className="text-gray-500">
            Hi! Ask me about our products, for example "Do you have a laptop
            under $800?". Customers can also ask about their orders.
          </p>
        )}

        {messages.map((m, index) => (
          <div
            key={index}
            className={
              m.role === "user" ? "flex justify-end" : "flex justify-start"
            }
          >
            <div
              className={
                m.role === "user"
                  ? "max-w-[85%] whitespace-pre-wrap rounded-lg bg-blue-600 px-3 py-2 text-white"
                  : "max-w-[85%] rounded-lg bg-gray-100 px-3 py-2"
              }
            >
              {m.role === "user" ? (
                m.content
              ) : (
                <AssistantMarkdown text={m.content} />
              )}
            </div>
          </div>
        ))}

        {sending && <p className="text-gray-400">The assistant is thinking…</p>}
        {error && (
          <p className="rounded bg-red-50 px-3 py-2 text-red-700">{error}</p>
        )}
        <div ref={endRef} />
      </div>

      <form onSubmit={handleSend} className="flex gap-2 border-t p-2">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          maxLength={MAX_MESSAGE_LENGTH}
          placeholder="Type your question…"
          className="flex-1 rounded border px-3 py-2 text-sm"
          disabled={sending}
        />
        <button
          type="submit"
          disabled={sending || !input.trim()}
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white disabled:opacity-50"
        >
          Send
        </button>
      </form>
    </div>
  );
}
