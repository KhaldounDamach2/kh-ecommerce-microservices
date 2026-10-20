import axiosInstance from "./axios";

// One chat may take up to 40 s on the server (CHAT_TIME_BUDGET_SECONDS in ai-service),
// so this request waits longer than the 10 s default of axiosInstance.
const CHAT_TIMEOUT_MS = 45_000;

// ai-service accepts at most 10 earlier turns, each at most 4000 characters.
const MAX_HISTORY_TURNS = 10;
const MAX_TURN_LENGTH = 4000;

// history: the conversation BEFORE this message, as [{ role, content }, ...]
export const sendChatMessage = (message, history) =>
  axiosInstance
    .post(
      "/ai/chat",
      {
        message,
        history: history
          .slice(-MAX_HISTORY_TURNS)
          .map(({ role, content }) => ({ role, content: content.slice(0, MAX_TURN_LENGTH) })),
      },
      { timeout: CHAT_TIMEOUT_MS },
    )
    .then((response) => response.data.reply);

// Turns a failed request into a sentence for the chat window.
export const chatErrorText = (error) => {
  const status = error.response?.status;
  if (status === 429) {
    const seconds = error.response.headers["retry-after"];
    return `You are sending messages too fast. Please wait ${seconds ?? "a few"} seconds.`;
  }
  if (status === 503) {
    return error.response.data?.detail ?? "The assistant is not available right now.";
  }
  if (status === 422) {
    return "That message could not be sent (it may be too long).";
  }
  if (error.code === "ECONNABORTED") {
    return "The assistant took too long to answer. Please try again.";
  }
  return "Something went wrong. Please try again.";
};
