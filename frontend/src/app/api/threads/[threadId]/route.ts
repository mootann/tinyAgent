import { NextRequest } from "next/server";

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8000";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ threadId: string }> }
) {
  const { threadId } = await params;
  const { searchParams } = new URL(request.url);
  const wantMessages = searchParams.get("messages") === "true";

  try {
    const url = `${BACKEND_URL}/api/threads/${threadId}${wantMessages ? "?messages=true" : ""}`;
    const response = await fetch(url);
    if (!response.ok) return new Response("Backend error", { status: 502 });
    const data = await response.json();
    return Response.json(data);
  } catch {
    return Response.json({ messages: [] });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ threadId: string }> }
) {
  const { threadId } = await params;

  try {
    const response = await fetch(`${BACKEND_URL}/api/threads/${threadId}`, {
      method: "DELETE",
    });
    if (!response.ok) return new Response("Backend error", { status: 502 });
    const data = await response.json();
    return Response.json(data);
  } catch {
    return new Response("Backend unavailable", { status: 502 });
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ threadId: string }> }
) {
  const { threadId } = await params;

  try {
    const body = await request.json();
    const response = await fetch(`${BACKEND_URL}/api/threads/${threadId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!response.ok) return new Response("Backend error", { status: 502 });
    const data = await response.json();
    return Response.json(data);
  } catch {
    return new Response("Backend unavailable", { status: 502 });
  }
}
