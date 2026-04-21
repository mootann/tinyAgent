import { NextRequest } from "next/server";

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8000";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ threadId: string }> }
) {
  const { threadId } = await params;

  const response = await fetch(`${BACKEND_URL}/api/clarifications/pending/${threadId}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  if (!response.ok) {
    return new Response("Backend error", { status: response.status });
  }

  const data = await response.json();
  return Response.json(data);
}
