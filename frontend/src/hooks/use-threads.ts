"use client";

import { useCallback, useEffect, useState } from "react";
import type { Thread } from "@/lib/types";

export function useThreads() {
  const [threads, setThreads] = useState<Thread[]>([]);
  const [activeThreadId, _setActiveThreadId] = useState<string | null>(() => {
    // Restore from URL hash on mount
    if (typeof window !== "undefined") {
      const hash = window.location.hash.slice(1);
      return hash || null;
    }
    return null;
  });

  // Sync activeThreadId to URL hash
  const setActiveThreadId = useCallback((id: string | null) => {
    _setActiveThreadId(id);
    if (typeof window !== "undefined") {
      if (id) {
        window.history.replaceState(null, "", `#${id}`);
      } else {
        window.history.replaceState(null, "", window.location.pathname);
      }
    }
  }, []);

  const refresh = useCallback(async () => {
    try {
      const res = await fetch("/api/threads");
      if (res.ok) {
        const data = await res.json();
        setThreads(data);
      }
    } catch {
      // Backend might not be running
    }
  }, []);

  // Load threads on mount
  useEffect(() => {
    refresh();
  }, [refresh]);

  const createThread = useCallback(async (): Promise<string> => {
    try {
      const res = await fetch("/api/threads", { method: "POST" });
      if (res.ok) {
        const data = await res.json();
        // Support both Java backend (threadId) and Python backend (thread_id)
        const threadId = data.thread_id || data.threadId;
        setActiveThreadId(threadId);
        await refresh();
        return threadId;
      }
    } catch {
      // fallback
    }
    // Fallback: local-only thread
    const id = `local_${Date.now().toString(36)}`;
    setActiveThreadId(id);
    return id;
  }, [refresh]);

  const updateTitle = useCallback(
    async (threadId: string, firstMessage: string) => {
      try {
        await fetch(`/api/threads/${threadId}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          // Send both formats for compatibility with Java (firstMessage) and Python (first_message) backends
          body: JSON.stringify({ first_message: firstMessage, firstMessage: firstMessage }),
        });
        await refresh();
      } catch {
        // non-critical
      }
    },
    [refresh],
  );

  const deleteThread = useCallback(
    async (threadId: string) => {
      try {
        await fetch(`/api/threads/${threadId}`, { method: "DELETE" });
        if (activeThreadId === threadId) {
          setActiveThreadId(null);
        }
        await refresh();
      } catch {
        // non-critical
      }
    },
    [activeThreadId, refresh],
  );

  const switchThread = useCallback((threadId: string) => {
    setActiveThreadId(threadId);
  }, []);

  return {
    threads,
    activeThreadId,
    createThread,
    switchThread,
    deleteThread,
    updateTitle,
    refresh,
  };
}
